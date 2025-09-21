## SpringBoot整合actuator prometheus
-----

## 1. 概述

SpringBoot提供了actuator包来方便完成指标数据的统计，结合prometheus可以实现指标数据统计、报警和查询的需求。

## 2. 添加依赖

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
	<groupId>io.micrometer</groupId>
	<artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

## 3. 修改配置

在主配置文件中添加如下配置，用于暴露各种指标接口。

include表示只暴露的接口。prometheus用于拉取指标，health用于健康检查。

tags表示在指标生成中添加额外的标签信息。


```ini
server.port=8899
spring.application.name=TestApplication
management.endpoints.web.exposure.include=prometheus,health
management.metrics.tags.application=${spring.application.name}
```

### 4. 验证配置是否生效。

启动程序，通过访问如下地址访问指标数据

```html
查看有哪些指标信息可以导出
$ curl localhost:8899/actuator
查看导出的prometheus指标。
$ curl localhost:8899/actuator/prometheus
```

## 4. 添加consul自动发现采集

如果当前环境已经配置好了prometheus和consul服务，用于服务自发现。

可以在自己编写的程序中做如下配置实现服务自发现

### 4.1. 添加依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-consul-discovery</artifactId>
    <version>2.1.2.RELEASE</version>
</dependency>
```

### 4.2. 添加配置

```ini
spring:
  application:
    name: test-sprintboot
  cloud:
    consul:
      discovery:
        health-check-interval: 10s
        health-check-path: '/actuator/health'
        # 300s后服务未能在consul中恢复注册，则在consul上下线此instance，300s保证告警能够发出
        health-check-critical-timeout: 300s
        instance-id: ${spring.application.name}-${random.int[1,999]}
        prefer-ip-address: true
        register: true
        deregister: true
        # 用于标记本应用归属哪个环境
        tags: dev
      enabled: true
      host: ${consul-host}
      port: ${consul-port:80}
```

经过如上配置，启动服务后就能再consul页面中看到注册的新服务。consul也会定时的去检查应用的存活状态。


## 5. 解决health=down问题

有的时候访问/actuator/health的时候，返回

```json
{
    "status":"down"
}
```

通过在application.properties(yml)中添加如下配置打印详细信息

```yml
management:
  endpoint: 
    health:
      show-details: always   # 加上这段配置，显示健康检查的详情信息，发现问题
  endpoints:
    web:
      exposure:
        include: "*"
  metrics:
    tags:
      application: ${spring.application.name}
```

## 6. 如何定制度量指标

定制度量指标主要通过如下几个方式完成：

1. 通过MeterRegister注册Meter
1. 实现MeterBinder接口让SpringBoot自动绑定
1. 通过MeterFilter进行定制

实现

以订单系统为例，假设每次查询一次订单信息就有一个计数器加一。

service/OrderService.java

```java
@Service
public class OrderService implement MeterBinder {
  private Counter counter;

  public void addCounter(){
    counter.increment();
  }

  @Override
  public void bindTo(MeterRegistry registry){
    counter = registry.counter("order_counter","system","order");
  }
}
```

api/OrderAPI.java

```java
@RestController
public class OrderAPI {
  @Authwired
  private OrderService orderService;

  @GetMapping("/order")
  public void getOrder(){
    orderSerivce.addCounter();
  }
}
```

通过如下方式访问：

```bash
$ curl localhost:8899/actuator/prometheus
可以找到名称是order_counter的指标项。值会随着每次访问而累加。
```



## 7. 参考资料

```html
https://www.cnblogs.com/cjsblog/p/11556029.html
```
