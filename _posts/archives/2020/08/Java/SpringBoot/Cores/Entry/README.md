## 入口
-----

@SpringBootApplication注解可以看作是@Configuration ，@EnableAutoConfiguration，@ComponentScan注解的集合

根据 SpringBoot 官网，这三个注解的作用分别是：

1. @EnableAutoConfiguration：启用 SpringBoot 的自动配置机制
1. @ComponentScan： 扫描被@Component (@Service,@Controller)注解的 bean，注解默认会扫描该类所在的包下所有的类。
1. @Configuration：允许在 Spring 上下文中注册额外的 bean 或导入其他配置类