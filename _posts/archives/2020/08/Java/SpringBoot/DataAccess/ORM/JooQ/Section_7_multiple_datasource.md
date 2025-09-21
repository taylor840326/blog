## 多数据源处理
-----

目前在微服务框架下，比较提倡单服务单库操作。但是某些历史包袱或者其他因素导致，导致需要在项目中同时访问多个数据库，本篇文章主要是讲解对于对于此情况的一些处理方式

多数据库分为两种情况，两种情况的处理方式不太一样，第一种比较简单，第二种相对复杂一点，接下来会给大家仔细讲解两种情况的处理方式

1. 多个数据库在 同一实例
1. 多个数据库在 不同实例

### 1. 多库同实例

这种情况解决方案比较简单，只需要在代码生成的时，配置生成多个库。

```xml
 <generator>
    <!-- ... -->
    <database>
      <schemata>
        <schema>
          <inputSchema>learn-jooq</inputSchema>
        </schema>
        <schema>
          <inputSchema>learn-jooq-2</inputSchema>
        </schema>
        <!-- other schema config ... -->
      </schemata>
    </database>
    <!-- ... -->
</generator>
```

因为在同一实例内，多个数据库可以共用同一个数据源。jOOQ渲染SQL时，可以带上数据库名称（此配置默认开启），不会受数据库连接参数中所带的数据库名称所影响， 例如

查询 learn-jooq库的s1_user表：

```java

查询 `learn-jooq-2`库的`s7_user`表： ```select `learn-jooq-2`.`s7_user`.`id` from `learn-jooq-2`.`s7_user`
```

### 2. 多库不同实例

这种情况对于同实例的情况来说，处理起来相对比较复杂，因为不同实例，那么肯定的需要使用不同的数据库连接，即不同的数据源，不同的事务管理器等等

### 2.1 代码生成

多数据源的情况下，由于每个数据库的连接等配置不同，所以针对每个数据库实例都需要有一套配置。 这里利用 maven 插件的 executions 选项，可以同时配置多个执行器，另外由于配置过多，不再将这些配置放在 pom.xml 文件。把每一个数据库的配置抽离出来，作为单独的文件，这里测试时有两个数据库所以创建两个文件:

src/main/resources/codegen-config-jooq-learn.xml

src/main/resources/codegen-config-jooq-learn-2.xml

文件内容就不贴了，和之前的插件配置一样，不同的是数据库的连接和库名等等一些基础配置。 除了一些基础配置外，需要注意的是目标的包名需要设置为不同的路径，不可放在同一个包内，因为两者会相互覆盖

```xml
<configuration>
    <!-- ...  -->
    <generator>
        <!-- ...  -->
        <target>
            <packageName>com.diamondfsd.jooq.learn.codegen.learn_jooq</packageName>
            <directory>/src/main/java</directory>
        </target>
    </generator>
</configuration>
```

pom.xml 的插件配置变为如下内容:

```xml
<plugin>
    <groupId>org.jooq</groupId>
    <artifactId>jooq-codegen-maven</artifactId>
    <version>3.12.3</version>
    <executions>
        <execution>
            <id>learn-jooq</id>
            <configuration>
                <configurationFile>src/main/resources/codegen-config-jooq-learn.xml</configurationFile>
            </configuration>
        </execution>
        <execution>
            <id>learn-jooq-2</id>
            <configuration>
                <configurationFile>src/main/resources/codegen-config-jooq-learn-2.xml</configurationFile>
            </configuration>
        </execution>
    </executions>
</plugin>
```

在此情况下，如果要生成代码，需要执行 mvn jooq-codegen:generate@{execution.id} 命令，针对不同的配置，指定执行ID进行执行

```bash
mvn jooq-codegen:generate@learn-jooq
mvn jooq-codegen:generate@learn-jooq-2
```

### 2.2. 数据源配置

对于多数据源的情况下，由于jOOQ生成的代码是一样的，所以主要是要处理jOOQ生成的DAO内需要注入的 Configuration 对象。Configuration 对象主要适用于存放数据源对象，SQL方言，以及一些其他的渲染配置等等

首先，在 jdbc.properties 内配置多个数据库连接的参数

```yaml
datasource1.jdbc.url=jdbc:mysql://localhost:3306/learn-jooq?serverTimezone=GMT%2B8
datasource1.jdbc.username=root
datasource1.jdbc.password=root


datasource2.jdbc.url=jdbc:mysql://localhost:3306/learn-jooq-2?serverTimezone=GMT%2B8
datasource2.jdbc.username=root
datasource2.jdbc.password=root
```

Spring 数据源/事务配置，这里由于有多个数据库连接，所以配置了多个数据源，并且需要对每个数据源配置事务管理器

申明 TX_LEARN_JOOQ 和 TX_LEARN_JOOQ_2 常量的目的是因为在多数据源情况下，因为有多个事务管理器，所以我们在使用 @Transaction 注解时，根据不同的数据库，需要显式的指定需要的事务管理器。 如果希望有一个默认的事务管理器，可以在默认的事务管理器定义上同时加上 @Primary 注解

```java
@Configuration
@EnableTransactionManagement
@PropertySource("classpath:jdbc.properties")
public class DataSourceConfig {

    public static final String TX_LEARN_JOOQ = "learnJooqTransactionManager";
    public static final String TX_LEARN_JOOQ_2 = "learnJooq2TransactionManager";

    @Bean
    public PlatformTransactionManager learnJooqTransactionManager(
            @Autowired
            @Qualifier("learnJooqDataSource")
                    DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public PlatformTransactionManager learnJooq2TransactionManager(
            @Autowired
            @Qualifier("learnJooq2DataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public DataSource learnJooqDataSource(
            @Value("${datasource1.jdbc.url}")
                    String url,
            @Value("${datasource1.jdbc.username}")
                    String username,
            @Value("${datasource1.jdbc.password}")
                    String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        return new TransactionAwareDataSourceProxy(new HikariDataSource(config));
    }

    @Bean
    public DataSource learnJooq2DataSource(
            @Value("${datasource2.jdbc.url}")
                    String url,
            @Value("${datasource2.jdbc.username}")
                    String username,
            @Value("${datasource2.jdbc.password}")
                    String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        return new TransactionAwareDataSourceProxy(new HikariDataSource(config));
    }
}
```


### 2.3. jOOQ配置

jOOQ 配置主要是因为 DAO 内的代码是自动生成的，我们不太好修改，而且每次改了过后重新生成代码的时候又会被还原，这里使用的解决方案是。

首先根据数据源声明多个 DefaultConfiguration Bean，这些Bean注入对应的数据源， 然后声明 org.jooq.Configuration Bean， 在声明函数里获取一个 Map<String, DefaultConfiguration> 对象，此对象由Spring自动注入，根据Bean的名称和实例对象组成一个Map。 同时获取一个 InjectionPoint 实例，此对象储存了 @Autowired 注解所在位置的一些源信息。在此场景下，获取 InjectionPoint 对象可以获得 DAO 所在的包路径。 根据不同的包路径，选择不同的 DefaultConfiguration 对象进行注入

```java
@Configuration
@Import(DataSourceConfig.class)
public class JooqConfiguration {

    @Bean
    @Primary
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public org.jooq.Configuration configuration(
            Map<String, DefaultConfiguration> configurationMap,
            InjectionPoint injectionPoint) {
        AnnotatedElement annotatedElement = injectionPoint.getAnnotatedElement();
        if (Constructor.class.isAssignableFrom(annotatedElement.getClass())) {
            Class declaringClass = ((Constructor) annotatedElement).getDeclaringClass();
            String packageName = declaringClass.getPackage().getName();
            org.jooq.Configuration configuration;
            switch (packageName) {
                case "com.diamondfsd.jooq.learn.codegen.learn_jooq.tables.daos":
                    configuration = configurationMap.get("learnJooqConfiguration");
                    break;
                case "com.diamondfsd.jooq.learn.codegen.learn_jooq_2.tables.daos":
                    configuration = configurationMap.get("learnJooq2Configuration");
                    break;
                default:
                    throw new NoSuchBeanDefinitionException("no target switch");
            }
            return configuration;
        }
        throw new NoSuchBeanDefinitionException("no target switch");
    }

    @Bean
    public DefaultConfiguration learnJooqConfiguration(@Autowired
                                                       @Qualifier("learnJooqDataSource")
                                                               DataSource learnJooqDataSource) {
        DefaultConfiguration configuration = new DefaultConfiguration();
        configuration.set(SQLDialect.MYSQL);
        configuration.set(learnJooqDataSource);
        configuration.settings().setRenderSchema(false);
        return configuration;
    }

    @Bean
    public DefaultConfiguration learnJooq2Configuration(@Autowired
                                                        @Qualifier("learnJooq2DataSource")
                                                                DataSource learnJooq2DataSource) {
        DefaultConfiguration configuration = new DefaultConfiguration();
        configuration.set(SQLDialect.MYSQL);
        configuration.set(learnJooq2DataSource);
        configuration.settings().setRenderSchema(false);
        return configuration;
    }
}
```

### 2.4. 代码演示

通过以上这些配置后，我们就能在多数据源的情况下，愉快的使用jOOQ了， 和平时的使用方式一致， DAO对象还是可以自动注入。 并且可以通过显式的指定事务管理器使用 @Transaction 注解

```java
@Autowired
S1UserDao s1UserDao;

@Transactional(TX_LEARN_JOOQ)
public void insert() {
    S1UserPojo s1UserPojo = new S1UserPojo();
    s1UserPojo.setUsername("username");
    s1UserDao.insert(s1UserPojo);
}


@Autowired
S7UserDao s7UserDao;

@Test
@Transactional(TX_LEARN_JOOQ_2)
public void insert() {
    S7UserPojo s7UserPojo = new S7UserPojo();
    s7UserPojo.setUsername("s7username");
    s7UserDao.insert(s7UserPojo);
}
```

### 3. 内容总结

本章源码: https://github.com/k55k32/learn-jooq/tree/master/section-7

对于多数据源的情况下，主要是在一些历史代码迁移的时候，会采用的方案。正常现代的微服务体系下，一般一个服务只会使用一个数据源。这里提出的解决方案比较简单，是通过DAO的包名使用不同的数据源配置，得以让Spring来管理自动注入，不影响业务层的使用