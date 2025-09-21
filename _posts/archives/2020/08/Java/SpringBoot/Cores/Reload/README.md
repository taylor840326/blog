## SpringBoot 服务热重启

-----

### 1. 前言

本文档描述的SpringBoot服务热重启，是为了在开发阶段修改完一段代码后IDEA能帮助我们重启服务，以便查看修改的效果。

服务的重启一般分为两个阶段：

1. 重新编译项目
1. 重启服务

SpringBoot为我们提供了devtools工具，帮助我们在项目重新编译后重启该服务。这里需要注意devtools只负责服务的自动重启，而不负责服务的重新编译。

服务的重新编译需要IDEA来完成。


### 2. 配置步骤

### 2.1. 安装devtools开发工具

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
```

刷新依赖，确保所有依赖已经成功安装。

### 2. 设置IDEA自动编译项目

在IDEA中的设置窗口中依次点击“Build,Execution,Deployment”菜单下的“Compiler”子菜单。

在“Compiler”子菜单的右侧详细设置页面中勾选“Build project automatically”选项后保存退出


### 3. 测试

重新修改某个代码保存后直接可以访问接口查看是否已经有变化。


