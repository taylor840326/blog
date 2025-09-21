## 从源代码编译flink
-----

本文档记录从源代码编译flink-1.9.2的过程。

其他版本的flink与之类似。


### 1.前提条件

本人在编译flink的过程中遇到了一些小坑，在这里记录下来。希望能帮助你编译成功。


### 1.1.硬件条件

要想能顺利把flink编译完成，推荐硬件配置如下

CPU：最少双核物理机或虚拟机
内存： 建议物理内存8GB以上
硬盘空间： 10GB

这个只是建议配置，可以根据自己的情况酌情选择。

### 1.2.软件条件

如果想顺利编译flink，可能还需要你做如下准备工作。

### 1.2.1.操作系统

操作系统的选择推荐使用Linux，你可以选择CentOS、Ubuntu、Debian、Deepin等等发行版

不过为了能让你顺利搭建起一个系统环境，个人推荐

|序号|操作系统|版本|
|-----|-----|-----|
|1|CentOS| 7或8|
|2|Ubuntu| 18.04|
|3|Debian| 10|
|4|Deepin| 15.10+|

Windows全系列就不推荐了，如果想自虐的话可以尝试在Windows上编译。应该会让你感到非常酸爽。

### 1.2.2.Java

Java的JDK版本推荐还是用8吧，随大溜的话可能碰到的问题更少。

已经记得要在当前的系统环境变量上配置好JAVA_HOME呦！！！

### 1.2.3.Maven

我个人用的Maven版本是3.6.3，可以顺利把flink编译出来。根据自己的情况选择。

另外flink官网有对Maven版本的提示，官方文档保平安。

### 1.2.4.Nexus3

最后就要说Nexus3服务了，为什么编译flink还要用到它。我的经验是在你编译flink的时候总是提示找不到一个以来包

```txt
kafka-schema-registry-client-3.3.1.jar
```

这个依赖包就要自己下载，然后上传到Nexus3服务上供我们mvn编译使用。

### 1.2.5.nodejs

我的经验是不用你提前准备这个node的编译环境，flink在编译的时候就会自动准备好nodejs环境。

我们要做的就是让网络畅通。


## 2.编译过程

### 2.0.配置好系统环境变量

编译前需要确认系统环境变量是否已经准备好，我贴出我的一些配置

```bash
export MAVEN_OPTS="-Xmx4g -XX:ReservedCodeCacheSize=1g"

export BIGDATA_HOME=$HOME/Applications
export JAVA_HOME=$BIGDATA_HOME/jdk1.8.0_231
export MAVEN_HOME=$BIGDATA_HOME/apache-maven-3.6.3

```
以上信息保存在.bashrc文件中。

### 2.1.下载flink源码

通常我个人推荐使用git从github上把官方的源码仓库下载下来，这样就可以使用git进行版本控制。

所以，执行如下命令把flink下载到本地

```bash
git clone https://github.com/apache/flink.git
```

下载到本地后切换到制定的tag版本

```bash
git checkout release-1.9.2
```

如果你的flink将来是要泡在yarn上，并且还要用到hdfs保存checkpoint等信息，就要还下载一个flink-shade-7.0软件包，因为flink编译的时候依赖这个包。

下载方法为

```bash
wget https://archive.apache.org/dist/flink/flink-shaded-7.0/flink-shaded-7.0-src.tgz
```
下载完成后解压备用。


### 2.2.编译flink-shaded包

首先，一定要把flink-shaded包编译完成，并且推送到自己的私有仓库中。

编译之前一定要改一下flink-shaded的pom.xml文件，把私有仓库部署地址在pom.xml文件中指明

```xml
    </build>
        <distributionManagement>
        <!-- use the following if you're not using a snapshot version. -->
        <repository>
            <id>releases</id>
            <url>http://172.18.9.250:8081/repository/maven-releases/</url>
        </repository>
        <!-- use the following if you ARE using a snapshot version. -->
        <snapshotRepository>
            <id>snapshots</id>
            <url>http://172.18.9.250:8081/repository/maven-snapshots/</url>
        </snapshotRepository>
    </distributionManagement>


</project>
```

要添加的配置就是distributionManagement段，加载build和project中间。

进入flink-shaded源代码目录，执行如下命令进行编译：

```bash
mvn clean deploy -Dhadoop.version=2.7.7 -DskipTests -Drat.skip=true -P release

```
假设，我们未来flink工作的yarn和hdfs环境是hadoop-2.7.7版本。如果不是需要修改hadoop.version后面的值。

通过最后提示成功，则可以进行下一步。


### 2.3.编译flink

注意，flink的版本和flink-shaded的版本是一一对应的。如果版本不对，在编译的时候会报错。根据报错的信息找到对应的flink-shaded版本。

这时可以尝试编译flink，编译的命令为

```bash
mvn clean package -DskipTests -Dfast -e -X -Dhadoop.version=2.7.7 
```

编译是一个很漫长的过程，如果一切顺利。编译完成后会在flink目录生成一个build-target链接，这个链接指定的目录就是编译好的flink二进制文件所在目录。

但是，我的经验是不会那么顺利。我遇到的情况有

### 2.3.1.缺少kafaka-schema-registry-client-3.3.1包


如果编译的时候提示缺少这个包，这时就需要手工下载这个包然后上传到私有仓库供编译flink使用。

方法如下：

下载某个包

```bash
wget http://packages.confluent.io/maven/io/confluent/kafka-schema-registry-client/3.3.1/kafka-schema-registry-client-3.3.1.jar
```


把下载好的包推送到私有仓库

假设下载后保存的路径为/tmp/kafka-schema-registry-client-3.3.1.jar

```bash
mvn install:install-file -DgroupId=io.confluent -DartifactId=kafka-schema-registry-client -Dversion=3.3.1 -Dpackaging=jar -Dfile=/tmp/kafka-schema-registry-client-3.3.1.jar
```

### 2.3.2. 其他不可预估错误。

重新编译几次，一般就会通过了。