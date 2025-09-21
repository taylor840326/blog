## Spring Boot和jOOQ整合
-----

在当前微服务盛行的情况下，Spring Boot 或 Spring Cloud 为基础的微服务体系是主流， 也是目前业务场景中新的选型方向

相对于直接使用Spring来说，利用Spring Boot来整合jOOQ相对来说简单很多

### 1. Maven 依赖

Spring Boot 官方有对 jOOQ 的支持，所以只需要简单的引用 spring-boot-starter-jooq 和对应的jdbc驱动即可

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.2.1.RELEASE</version>
</parent>
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jooq</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
    <dependency>
        <groupId>org.jooq</groupId>
        <artifactId>jooq-codegen</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### 2. 配置

Spring Boot最大的一个特性就是有很多 AutoConfiguration 自动配置, spring-boot-starter-jooq 依赖于 spring-boot-starter-jdbc， 其自动配置了数据源，事务管理器等

spring-boot-starter-jooq 自动配置了 org.jooq.Configuration 和 org.jooq.DSLContext 对象。 我们只需要在 src/main/resources/application.yml 内写好数据源相关配置，其他的一切都可以交给Spring Boot进行处理

```yaml
src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/learn-jooq?serverTimezone=GMT%2B8
    username: root
    password: root
```

### 3. 运行/测试

直接通过main方法启动 Spring Boot 服务

```java
@SpringBootApplication
public class Section8Main {
    public static void main(String[] args) {
        SpringApplication.run(Section8Main.class);
    }
}
```

测试用例内，使用 SpringBootTest 注解

```java
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Section8Main.class)
@Transactional
@Rollback
public class BaseTest {
    @Autowired
    DSLContext dslContext;

    @Autowired
    Configuration configuration;

    @Autowired
    TransactionManager transactionManager;

    @Test
    public void empty() {
        Assertions.assertNotNull(dslContext);
        Assertions.assertNotNull(configuration);
        Assertions.assertNotNull(transactionManager);
    }
}
```

### 4. JooqAutoConfiguration 源码解析

以下是jOOQ的自动配置源码，从 spring-boot-autoconfigure 内拷贝而来，可以看出此配置会在数据源配置和事务配置之后执行

对于我们经常用到的就是 Bean dslContext 和 jooqConfiguration ，大部分自动配置的Bean都会使用 @ConditionalOnMissingBean 注解， 此注解标识在没有某个Bean的情况下，才会执行该注解所标注的配置

```java
org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration
/**
 * {@link EnableAutoConfiguration Auto-configuration} for JOOQ.
 *
 * @author Andreas Ahlenstorf
 * @author Michael Simons
 * @author Dmytro Nosan
 * @since 1.3.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(DSLContext.class)
@ConditionalOnBean(DataSource.class)
@AutoConfigureAfter({ DataSourceAutoConfiguration.class, TransactionAutoConfiguration.class })
public class JooqAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(ConnectionProvider.class)
	public DataSourceConnectionProvider dataSourceConnectionProvider(DataSource dataSource) {
		return new DataSourceConnectionProvider(new TransactionAwareDataSourceProxy(dataSource));
	}

	@Bean
	@ConditionalOnBean(PlatformTransactionManager.class)
	public SpringTransactionProvider transactionProvider(PlatformTransactionManager txManager) {
		return new SpringTransactionProvider(txManager);
	}

	@Bean
	@Order(0)
	public DefaultExecuteListenerProvider jooqExceptionTranslatorExecuteListenerProvider() {
		return new DefaultExecuteListenerProvider(new JooqExceptionTranslator());
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnMissingBean(DSLContext.class)
	@EnableConfigurationProperties(JooqProperties.class)
	public static class DslContextConfiguration {

		@Bean
		public DefaultDSLContext dslContext(org.jooq.Configuration configuration) {
			return new DefaultDSLContext(configuration);
		}

		@Bean
		@ConditionalOnMissingBean(org.jooq.Configuration.class)
		public DefaultConfiguration jooqConfiguration(JooqProperties properties, ConnectionProvider connectionProvider,
				DataSource dataSource, ObjectProvider<TransactionProvider> transactionProvider,
				ObjectProvider<RecordMapperProvider> recordMapperProvider,
				ObjectProvider<RecordUnmapperProvider> recordUnmapperProvider, ObjectProvider<Settings> settings,
				ObjectProvider<RecordListenerProvider> recordListenerProviders,
				ObjectProvider<ExecuteListenerProvider> executeListenerProviders,
				ObjectProvider<VisitListenerProvider> visitListenerProviders,
				ObjectProvider<TransactionListenerProvider> transactionListenerProviders,
				ObjectProvider<ExecutorProvider> executorProvider) {
			DefaultConfiguration configuration = new DefaultConfiguration();
			configuration.set(properties.determineSqlDialect(dataSource));
			configuration.set(connectionProvider);
			transactionProvider.ifAvailable(configuration::set);
			recordMapperProvider.ifAvailable(configuration::set);
			recordUnmapperProvider.ifAvailable(configuration::set);
			settings.ifAvailable(configuration::set);
			executorProvider.ifAvailable(configuration::set);
			configuration.set(recordListenerProviders.orderedStream().toArray(RecordListenerProvider[]::new));
			configuration.set(executeListenerProviders.orderedStream().toArray(ExecuteListenerProvider[]::new));
			configuration.set(visitListenerProviders.orderedStream().toArray(VisitListenerProvider[]::new));
			configuration.setTransactionListenerProvider(
					transactionListenerProviders.orderedStream().toArray(TransactionListenerProvider[]::new));
			return configuration;
		}
	}
}
```

如果我们需要使用多数据源，可以在启动入口中的 @SpringBootApplication 中使用 exclude 选项，不让 Spring Boot 自动配置数据源和 jOOQ 的配置。最后参考一下上一篇多数据源的文章内容，然后进行同样的配置即可，Spring Boot 本质上也是对 Spring 框架的整合而已

```java
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, 
                                  JooqAutoConfiguration.class, 
                                  TransactionAutoConfiguration.class})
```

### 5. 内容总结

本章源码: https://github.com/k55k32/learn-jooq/tree/master/section-8

通过本章，可以看出Spring Boot的整合相对于直接使用Spring来说，节省了很多工作，很多配置都由Spring Boot帮我们自动配置了。所以之后的文章都会以此为基础来讲解，之后的源码演示也会基于本章的源码进行扩展

另外，整合相关的章节就到此结束了，之后的章节中，会解决一些实际业务中的问题。 例如jOOQ当前生成的DAO只包含了一些基础的查询。 在业务开发中经常会遇到的多条件查询，分页查询，关联查询等都不是很支持。 接下来的章节中，就会针对一些实际的业务问题进行讲解