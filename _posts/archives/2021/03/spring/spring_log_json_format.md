## SpringBoot输出JSON格式的日志
-----

日常工作中需要对SpringBoot日志进行格式化，主要是为了传递给ELK日志平台做日志分析。

在SpringBoot框架下实现方案主要有如下三种：

1. 自定义Converter方案
1. 重新复写Appender和Layout类，自定义实现
1. 使用logstash-logback-encoder来实现

通常SpringBoot框架使用了Logback实现日志的输出。

Logback继承自log4j，Logback适用于不同的使用场景。

Logback被分成三个不同的模块:

1. logback-core
1. logback-classic。可以看做是Log4j的一个优化版本.
1. logback-access。可以与Servlet容器进行融合。

## 1. 自定义Converter实现

这种方式需要所有变量都要处理，灵活性较差

### 1.1. 创建日志格式化实现类

需要继承ch.qos.logback.classic.pattern.ClassicConverter这个类，然后覆写convert方法实现日志的转换逻辑。

```java
package net.anumbrella.spring.log.config;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.json.JSONObject;

/**
 * @auther anumbrella
 */

public class JsonLogConverter extends ClassicConverter {

    private JSONObject object = new JSONObject();

    @Override
    public String convert(ILoggingEvent iLoggingEvent) {
        object.put("msg",iLoggingEvent.getMessage());
        object.put("level", iLoggingEvent.getLevel().levelStr);
        object.put("threadName", iLoggingEvent.getThreadName());
        object.put("method", iLoggingEvent.getLoggerName());
        return object.toString();
    }
}
```

### 1.2. 创建配置文件


```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true" scanPeriod="30 seconds" packagingData="true">

    <!-- conversionWord为自定义变量,converterClass为转换规则类-->
    <conversionRule conversionWord="customJson"
                    converterClass="net.anumbrella.spring.log.config.JsonLogConverter" />

    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%customJson%n</pattern>
        </encoder>
    </appender>

    <root level="debug">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

### 1.3. 修改SpringBoot配置文件

在SpringBoot配置文件中添加如下配置使日志转换规则生效。

```yaml
logging.config=classpath:logback-converter.xml
```

### 1.4. 使用

```java
@SpringBootApplication
@Slf4j
public class TestApplication implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplicationBuilder springApplicationBuilder = new SpringApplicationBuilder(TestApplication.class);
		springApplicationBuilder.run();
	}

    @Override
    public void run(String... args) throws Exception {
        //需要定义customJson变量，否则无法通过自定义的Converter进行日志转换，也无法输出日志
        log.info("customJson","hehe");
    }
```