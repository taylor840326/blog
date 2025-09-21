## Dockerfile 多步骤构建
-----

## 1. 简介

**本文档内容部分拷贝自互联网，如有不妥请告知**

最新版Docker将支持多步构建(Multi-stage build)。

这样使用单个Dockerfile就可以定义多个中间镜像用于构建、测试和发布等多个步骤。

因为不用保存下载依赖、下载源码包和编译的临时文件，可以有效减小最终镜像的大小。


## 2. 什么是多步骤构建

多步构建(multi-stage build)允许在Dockerfile中使用多个FROM指令。

两个FROM指令之间的所有指令会生产一个中间镜像，最后一个FROM指令之后的指令将生成最终镜像。

中间镜像中的文件可以通过COPY --from=<image-number>指令拷贝，其中image-number为镜像编号，0为第一个基础镜像。

没有被拷贝的文件都不会存在于最终生成的镜像，这样可以减小镜像大小。

FROM指令可以使用as <stage-name>来指定步骤名称(stage name):

FROM maven:3.5-jdk-8 as BUILD
下面为示例Dockerfile:这样的话，COPY指令的--from选项可以使用步骤名称代替镜像编号。

如下示例所示：

```txt
FROM maven:3.5-jdk-8 as BUILD
 
COPY src /usr/src/myapp/src
COPY pom.xml /usr/src/myapp
RUN mvn -f /usr/src/myapp/pom.xml clean package
 
FROM jboss/wildfly:10.1.0.Final
 
COPY --from=BUILD /usr/src/myapp/target/people-1.0-SNAPSHOT.war /opt/jboss/wildfly/standalone/deployments/people.war
```

一共有两个FROM指令，因此为两步构建。由Dockerfile可知:

1. maven:3.5-jdk-8 是第一步构建的基础镜像。这一步用于构建应用的WAR文件。这一步的名称为build。
1. jboss/wildfly:10.1.0.Final 是第二步构建的基础镜像。第一步构建的WAR文件通过COPY --from指令拷贝到WildFly的deloyments目录。

## 3. Docker多步构建有什么好处

仅需要一个Dockerfile来定义整个构建过程。这样，不需要定义多个Dockerfile，也不需要使用数据卷来拷贝文件。

可以为最终镜像选择合适的基础镜像，来满足生产环境的需求，这样可以有效减小最终镜像的大小。另外，构建步骤的多余文件都被丢弃了。

使用官方的WildFly镜像作为生产镜像的基础镜像，而不是手动安装和配置WildFly。这样，WildFly升级时将非常方便。

## 参考内容

原文: Creating Smaller Java Image using Docker Multi-stage Build

译者: Fundebug

为了保证可读性，本文采用意译而非直译。

Github仓库: arun-gupta/docker-java-multistage

DockerCon 2017中与Java开发者直接相关的内容有:

Docker多步构建(Docker Multi-stage build)

Oracle JRE in Docker Store

这篇博客介绍了为什么需要Docker多步构建(Docker Multi-stage build)，并且通过一个示例展示了如何构建更小的Java镜像。
