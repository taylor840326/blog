## jOOQ 和 Spring 整合
-----

本章主要讲解 jOOQ 和 Spring 的基础整合方式，包括数据源的自动注入，DAO自动注入，以及事务管理。

### 1. Spring Maven 相关依赖

在原来依赖的基础上，还需要添加 Spring 相关的依赖，另外，利用Spring
Spring 的基本依赖主要是其中的容器，注解，以及数据源相关依赖，连接池这里用的HikariCP，号称 Java 性能最高的数据库连接池

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-core</artifactId>
    <version>5.2.2.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-beans</artifactId>
    <version>5.2.2.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>5.2.2.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-jdbc</artifactId>
    <version>5.2.2.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-test</artifactId>
    <version>5.2.2.RELEASE</version>
    <scope>test</scope>
</dependency>

<!-- 数据源 -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>3.4.2</version>
</dependency>
```

## 2. 主要配置

### 2.1. 数据源配置

我们将数据库的相关配置放到资源文件目录下的 jdbc.properties 文件内

```java
datasource.jdbc.url=jdbc:mysql://localhost:3306/learn-jooq?serverTimezone=GMT%2B8
datasource.jdbc.username=root
datasource.jdbc.password=root
```

接下来是配置数据源和事务管理器，并且启用注解式事务管理

```java
@Configuration
@EnableTransactionManagement
@PropertySource("classpath:jdbc.properties")
public class DataSourceConfig {
    @Value("${datasource.jdbc.url}")
    private String url;

    @Value("${datasource.jdbc.username}")
    private String username;

    @Value("${datasource.jdbc.password}")
    private String password;

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        return new TransactionAwareDataSourceProxy(new HikariDataSource(config));
    }

}
```

### 3. jOOQ 相关配置

jOOQ的代码生成器可以通过相关配置在代码生成的时候，自动为DAO类添加 @Repository 注解将其声明为SpringBean。并且会自动在有参构造器 XxxxDao(Configuration configuration) 上添加 @Autowried 注解

代码生成器配置:
在之前的基础上，添加 springAnnotations 配置

```xml
<generator>
    <strategy>
        <name>com.diamondfsd.jooq.learn.CustomGeneratorStrategy</name>
    </strategy>
    <generate>
        <pojos>true</pojos>
        <daos>true</daos>
        <interfaces>true</interfaces>
        <springAnnotations>true</springAnnotations>
    </generate>
</generator>
```

生成的DAO例如 S1UserDao:

```java
@Repository
public class S1UserDao extends DAOImpl<S1UserRecord, S1UserPojo, Integer> {
    // ...
    @Autowired
    public S1UserDao(Configuration configuration) {
        super(TS1User.S1_USER, S1UserPojo.class, configuration);
    }
    // ...
```

可以看出，DAO的初始化，需要有个 Configuration 实例，因此我们需要声明一些jOOQ相关的Bean，除了Configuration实例外，经常使用 DSLContext 也可以声明为SpringBean

```xml
@Configuration
@Import(DataSourceConfig.class)
public class JooqConfiguration {
    @Bean
    public DSLContext dslContext(org.jooq.Configuration configuration) {
        return DSL.using(configuration);
    }

    @Bean
    public org.jooq.Configuration configuration(DataSource dataSource) {
        org.jooq.Configuration configuration = new DefaultConfiguration();
        configuration.set(SQLDialect.MYSQL);
        configuration.set(dataSource);
        return configuration;
    }
}
```

至此，通过这些配置，我们可以启动一个 Spring + jOOQ 整合的项目，添加一个入口文件:

```java
@ComponentScan
public class Section6Main {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(Section6Main.class);

        S1UserDao bean = context.getBean(S1UserDao.class);

        List<S1UserPojo> s1UserPojo = bean.findAll();

    }
}
```

### 4. 测试用例

测试用例在项目开发中，能够起到很大的作用，特别是测试覆盖率高了以后，对于项目的稳定性会起到很大的帮助。但是对于后端大部分的程序，测试用例很多是针对数据库进行操作，难免会影响数据库的数据。

这时可以通过一些方法，利用事务的特性，在测试用例执行完成后，进行回滚操作。这样就可以避免产生脏数据，或者污染数据等

### 4.1. JDBC 方式事务回滚

之前的系列文章里，大部分代码都是通过测试用例完成的，为了不对数据库造成污染，在执行测试用例后都会对数据进行 rollback 操作，具体的实现方式是通过测试框架的 @BeforeEach @AfterEach 注解完成的，具体实现代码片段如下

其原理为每一个测试方法执行之前，现将数据库连接的 autoCommit 改为false，不自动提交，在方法执行后，调用连接对象的 rollback 方法，进行回滚操作

```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseTest {
    DSLContext dslContext;
    Connection connection;

    Logger log = LoggerFactory.getLogger(this.getClass());

    @BeforeAll
    public void initDSLContext() throws SQLException {
        String jdbcUrl = "jdbc:mysql://localhost:3306/learn-jooq?serverTimezone=GMT%2B8";
        String jdbcUsername = "root";
        String jdbcPassword = "root";
        connection = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
        dslContext = DSL.using(connection, SQLDialect.MYSQL);

        Integer fetchOneResult = dslContext.select().fetchOne().into(Integer.class);
        Assertions.assertNotNull(fetchOneResult);
        Assertions.assertEquals(1, fetchOneResult.intValue());
    }

    @BeforeEach
    public void beginTransaction() throws SQLException {
        connection.setAutoCommit(false);
    }

    @AfterEach
    public void rollbackTransaction() throws SQLException {
        connection.rollback();
    }
}
```

#### 4.2. Spring 方式事务回滚

在使用Spring后，可以通过spring-jdbc内的事务管理器来进行实物操作，基础的测试类改为如下，只需要添加几个注解即可

```java
@SpringJUnitConfig(Section6Main.class)
@Transactional
@Rollback
public class BaseTest {
    @Autowired
    DSLContext dslContext;
}
```

可以通过此测试用例来校验，在 insert 测试用例结束之后，数据会回滚，所以在 findById 执行的时候，通过之前暂存的 insertUserId 是找不到对应数据的，这样可以验证事务是否工作

```java
class S1UserDaoTest extends BaseTest {
    @Autowired
    S1UserDao s1UserDao;

    Integer insertUserId = null;

    @Test
    public void findAll() {
        List<S1UserPojo> userAll = s1UserDao.findAll();
        Assertions.assertTrue(userAll.size() > 0);
    }

    @Test
    public void insert() {
        S1UserPojo s1UserPojo = new S1UserPojo();
        s1UserPojo.setUsername("hell username");
        s1UserDao.insert(s1UserPojo);
        Assertions.assertNotNull(s1UserPojo.getId());
        insertUserId = s1UserPojo.getId();
    }

    @Test
    public void findById() {
        S1UserPojo findById = s1UserDao.findById(1);
        Assertions.assertNotNull(findById);

        if (insertUserId != null) {
            S1UserPojo userPojo = s1UserDao.findById(insertUserId);
            Assertions.assertNull(userPojo);
        }
    }
}
```

另外还需要注意一点的是，使用 Junit 5 时，在运行 mvn test 命令时，对应 maven-surefire-plugin 的版本需要 2.22.0 以上，如果在发现使用 mvn test 命令没反应或者运行测试用例个数为0的时候，检查一下此配置

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>2.22.2</version>
</plugin>
```

### 5. 内容总结

本章源码: https://github.com/k55k32/learn-jooq/tree/master/section-6

本章介绍最基础的jOOQ和Spring的整合，其实内容并不多，主要是介绍了对于Spring注解生成的支持，以及事务的管理还有就是对于测试用例自动回滚的支持方法。是很基础的整合教程。 之后还会介绍和SpringBoot的整合。 了解本章内容，对于之后的内容能够起到很大的帮助