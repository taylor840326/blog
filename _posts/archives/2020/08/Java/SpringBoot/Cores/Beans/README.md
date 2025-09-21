## Bean
-----


### 2.1.@Autowird

@Autowird注解自动导入对象到类中，被注入进的类同样要被Spring容器管理。

比如Service类注入到Controller类中。

```java
@Service
public class UserService{
...
}

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowird
    UserService userService;
    ...

}
```

### 2.2.@Component/@Repository/@Service/@Controller

我们一般使用 @Autowired 注解让 Spring 容器帮我们自动装配 bean。要想把类标识成可用于 @Autowired 注解自动装配的 bean 的类,可以采用以下注解实现：

1. @Component ：通用的注解，可标注任意类为 Spring 组件。如果一个 Bean 不知道属于哪个层，可以使用@Component 注解标注。
1. @Repository : 对应持久层即 Dao 层，主要用于数据库相关操作。
1. @Service : 对应服务层，主要涉及一些复杂的逻辑，需要用到 Dao 层。
1. @Controller : 对应 Spring MVC 控制层，主要用户接受用户请求并调用 Service 层返回数据给前端页面。

### 2.3.@RestController

@RestController注解是@Controller和@ResponseBody的合集,表示这是个控制器 bean,并且是将函数的返回值直 接填入 HTTP 响应体中,是 REST 风格的控制器。

注：现在都是前后端分离，说实话我已经很久没有用过@Controller。如果你的项目太老了的话，就当我没说。

单独使用 @Controller 不加 @ResponseBody的话一般使用在要返回一个视图的情况，这种情况属于比较传统的 Spring MVC 的应用，对应于前后端不分离的情况。@Controller +@ResponseBody 返回 JSON 或 XML 形式数据

### 2.4.@Scope

声明 Spring Bean 的作用域，使用方法：

```java
@Bean 
@Scope("singleton")
public Person personSingleton() {
    return new Person();
}
```

四种常见的 Spring Bean 的作用域：

1. singleton : 唯一 bean 实例，Spring 中的 bean 默认都是单例的。
1. prototype : 每次请求都会创建一个新的 bean 实例。
1. request : 每一次 HTTP 请求都会产生一个新的 bean，该 bean 仅在当前 HTTP request 内有效。
1. session : 每一次 HTTP 请求都会产生一个新的 bean，该 bean 仅在当前 HTTP session 内有效。

### 2.5.@Configuration

一般用来声明配置类，可以使用 @Component注解替代，不过使用Configuration注解声明配置类更加语义化。

```java
@Configuration
public class AppConfig{
    
    @Bean
    public TransferService transferService(){
        return new TransferServiceImpl();
    }
}
```