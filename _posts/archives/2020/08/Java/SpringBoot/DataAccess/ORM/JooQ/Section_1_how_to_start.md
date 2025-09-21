## JooQ
-----

### 1. jooQ 简介

JooQ是一个ORM框架，利用其生成的Java代码和流畅的API，可以快速构建**有类型约束的安全的SQL语句**

JooQ使我们的重心可以放在业务逻辑上，而Java与SQL的基础交互部分，都可以交给JooQ去处理。

JooQ通用支持很多数据库，而且有商业版本和社区版本区别，商业版本和社区版本区别主要是支持数据库不一样，可以在其授权说明页面上看到各个版本对于数据库的支持情况，开源版本只支持部分开源数据库如MySQL等，这已经能满足大部分公司需求，本系列教程也是基于MySQL数据库进行

JooQ的核心优势是可以将数据库表结构映射为Java类，包含表的基本描述和所有表字段。通过JooQ提供的API，配合生成的Java代码，可以很方便的进行数据库操作

生成的Java代码字段类型是根据数据库映射成的Java类型，在进行设置和查询操作时，因为是Java代码，**都会有强类型校验**，所以对于数据的输入，是天然安全的，极大的减少了SQL注入的风险

JooQ的代码生成策略是根据配置全量生成，任何对于数据库的改动，如果会影响到业务代码，在编译期间就会被发现，可以及时进行修复

本系列文章依赖基础环境如下，不排除因为教程需要会添加其他依赖，都会在项目中标明

```text
MySQL 5.6 或 更高
JDK 1.8
JooQ - 3.12.3
JUnit 5
Maven 3.6.0
```


### 2. 如何开始

使用JooQ时，一般的开发流程为: 

1. 创建/更新 数据库表 
1. 通过JooQ插件生成Java代码 
1. 进行业务逻辑开发

### 3. 测试数据库

导入初始化数据库脚本:

https://github.com/k55k32/learn-jooq/blob/master/learn-jooq.sql
https://github.com/k55k32/learn-jooq/blob/master/learn-jooq-2.sql

### 4. Maven配置

JooQ提供了Maven插件jooq-codegen-maven，通过配置可以进行代码生成操作，配置项主要是jdbc连接，目标数据库，表，以及生成的路径包名等

```xml
<properties>
    <jooq.version>3.12.3</jooq.version>
</properties>

<dependencies>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.18</version>
    </dependency>

    <!-- base jooq dependency -->
    <dependency>
        <groupId>org.jooq</groupId>
        <artifactId>jooq</artifactId>
        <version>${jooq.version}</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <source>1.8</source>
                <target>1.8</target>
            </configuration>
        </plugin>

        <!-- 代码生成器插件 -->
        <plugin>
            <groupId>org.jooq</groupId>
            <artifactId>jooq-codegen-maven</artifactId>
            <version>${jooq.version}</version>
            <configuration>
                <jdbc>
                    <driver>com.mysql.cj.jdbc.Driver</driver>
                    <url>jdbc:mysql://127.0.0.1:3306/learn-jooq?serverTimezone=GMT%2B8</url>
                    <user>root</user>
                    <password>root</password>
                </jdbc>
                <generator>
                    <database>
                        <includes>s1-.*</includes>
                        <inputSchema>learn-jooq</inputSchema>
                    </database>
                    <target>
                        <packageName>com.diamondfsd.jooq.learn.codegen</packageName>
                        <directory>/src/main/java</directory>
                    </target>
                </generator>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 5. 代码生成

代码生成的原理就是通过读取数据库的元数据，将其转换为Java代码，并生成指定的文件，存放到配置好的指定目录

JooQ的生成代码的目标路径建议配置单独的子包，因为每次代码生成都是全量的，如果和其他业务代码混合在一起，会被生成器误删

通过此命令里可以调用 jooq-codegen-maven 插件进行代码生成

```bash
mvn jooq-codegen:generate
```

代码生成器执行完成后，会生成以下目录:
```text
├─src/main/java/.../codegen ---- // 生成路径
│ ├─tables --------------------- // 表定义目录
│ │ ├─S1User ------------------- // s1_user 表描述包含: 字段，主键，索引，所属Schema
│ │ └─records ------------------ // 表操作对象目录
│ │   └─S1UserRecord ----------- // s1_user 表操作对象，包含字段get,set方法
│ ├─DefaultCatalog ------------- // Catalog对象，包含Schema常量
│ ├─Indexes -------------------- // 当前数据库所有的所有常量
│ ├─Keys ----------------------- // 当前数据库所有表主键，唯一索引等常量
│ ├─LearnJooq ------------------ // 数据库`learn-jooq`常量，包含该库所有表描述常量
│ └─Tables --------------------- // 所有数据库表常量
```

### 6. 基础查询操作

```java
public static void main(String[] args) {

    String jdbcUrl = "jdbc:mysql://localhost:3306/learn-jooq?serverTimezone=GMT%2B8";
    String jdbcUsername = "root";
    String jdbcPassword = "root";

    // 获取 JDBC 链接
    try (Connection connection = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword)) {
        // 获取 JooQ 执行器
        DSLContext dslContext = DSL.using(connection, SQLDialect.MYSQL);

        // fetch方法可以返回一个结果集对象 Result
        // JooQ的Result对象实现了List接口，可以直接当做集合使用
        Result<Record> recordResult = dslContext.select().from(S1_USER).fetch();
        recordResult.forEach(record -> {
            Integer id = record.getValue(S1_USER.ID);
            String username = record.getValue(S1_USER.USERNAME);
            System.out.println("fetch Record     id: " + id + " , username: " + username);
        });

        // 通过 Record.into 方法可以将默认Record对象，转换为表的Record对象，例如S1UserRecord
        // Result 接口也定义了into方法，可以将整个结果集转换为指定表Record的结果集
        // 通过 S1UserRecord 可以通过get方法直接获得表对象
        // 所有表的XXXRecord对象都是实现了 Record 对象的子类
        Result<S1UserRecord> userRecordResult = recordResult.into(S1_USER);
        userRecordResult.forEach(record -> {
            Integer id = record.getId();
            String username = record.getUsername();
            System.out.println("into S1UserRecord   id: " + id + " , username: " + username);
        });

        // fetchInto方法可以可以传入任意class类型，或者表常量
        // 会直接返回任意class类型的List集合，或者指定表Record的结果集对象
        List<S1UserRecord> fetchIntoClassResultList = dslContext.select().from(S1_USER).fetchInto(S1UserRecord.class);
        Result<S1UserRecord> fetchIntoTableResultList = dslContext.select().from(S1_USER).fetchInto(S1_USER);

        System.out.println("fetchIntoClassResultList: \n" + fetchIntoClassResultList.toString());
        System.out.println("fetchIntoTableResultList: \n" + fetchIntoTableResultList.toString());

    } catch (SQLException e) {
        e.printStackTrace();
    }
}
```

以上代码将会输出:

```java
fetch Record     id: 1 , username: demo1
fetch Record     id: 2 , username: admin1
into S1UserRecord   id: 1 , username: demo1
into S1UserRecord   id: 2 , username: admin1
fetchIntoClassResultList: 
[+----+--------+--------------------+------------------------+---------------------+---------------------+
|  id|username|email               |address                 |create_time          |update_time          |
+----+--------+--------------------+------------------------+---------------------+---------------------+
|   1|demo1   |demo1@diamondfds.com|China Guangdong Shenzhen|2019-12-27 16:41:42.0|2019-12-27 16:41:42.0|
+----+--------+--------------------+------------------------+---------------------+---------------------+
, +----+--------+---------------------------+-------------------------+---------------------+---------------------+
|  id|username|email                      |address                  |create_time          |update_time          |
+----+--------+---------------------------+-------------------------+---------------------+---------------------+
|   2|admin1  |admin1@diamondfsd@gmail.com|China Guanddong Guangzhou|2019-12-27 16:41:42.0|2019-12-27 16:41:42.0|
+----+--------+---------------------------+-------------------------+---------------------+---------------------+
]
fetchIntoTableResultList: 
+----+--------+---------------------------+-------------------------+---------------------+---------------------+
|  id|username|email                      |address                  |create_time          |update_time          |
+----+--------+---------------------------+-------------------------+---------------------+---------------------+
|   1|demo1   |demo1@diamondfds.com       |China Guangdong Shenzhen |2019-12-27 16:41:42.0|2019-12-27 16:41:42.0|
|   2|admin1  |admin1@diamondfsd@gmail.com|China Guanddong Guangzhou|2019-12-27 16:41:42.0|2019-12-27 16:41:42.0|
+----+--------+---------------------------+-------------------------+---------------------+---------------------+
```

### 7. Jooq重要接口:

1. org.jooq.Result 结果集接口，此接口实现了List接口，可以当做一个集合来操作，是一个数据库查询结果集的包装类，除了集合的相关方法，该接口还提供了一些结果集转换，格式化，提取字段等方法。通常我们查询出来的结果都是此接口的实现类，掌握好此接口是JooQ的基础接口，基本所有的SQL查询操作，都会碰到这个接口
1. org.jooq.Record 此接口再使用关系型数据库时，主要用于定义数据库表记录，储存的内容是一条表记录的字段和值，每个值会储存对应字段的类型，可以通过通用的 getValue(Field field) 方法，取到对应字段的值，也可以将这个接口看做是一条记录的字段/值映射

在使用了代码生成器后，会基于此接口生成对应表的实现类，该实现类基于数据库表字段生成所有字段的 get/set 方法，可以通过 getXXX/setXXX(..) 直观的获取或设置指定的值用于读取/更新等后续操作。对于编码来说，代码的可读性大大提升

1. org.jooq.DSLContext JooQ的核心接口之一，可以理解为一个SQL执行器，通过静态方法 DSL.using，可以获取一个 DSLContext 实例，此实例抽象了所有对于SQL的操作API，可以通过其提供的API方便的进行SQL操作

```java
/**
* 通过数据库连接和方言配置来创建一个执行器对象
* @param connection 数据库连接
* @param dialect 指定方言， 传入此参数的目的是，在JooQ渲染SQL的语句的时候，会根据SQL方言配置，使用不同的语法规则去生成SQL语句字符串。
*/
public static DSLContext using(Connection connection, SQLDialect dialect) {
    return new DefaultDSLContext(connection, dialect, null);
}
```

### 参考资料

```html
https://zhuanlan.zhihu.com/p/103834378
https://jooq.diamondfsd.com/learn/section-1-how-to-start.html
```