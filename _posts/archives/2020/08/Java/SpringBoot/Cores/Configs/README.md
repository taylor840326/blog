## 配置
-----

@Value("${property}") 读取比较简单的配置信息

```java
@Value("${wuhan2020}")
private String name;
```

### 5.2.@ConfigurationProperties(常用)

@ConfigurationProperties注解可以读取配置信息并于Bean绑定

```java
@Component
@ConfigurationProperties(prefix="library")
public class LibraryProperties {
    @NotEmpty
    private String location;
    private List<Book> books;
    
    @Setter
    @Getter
    @ToString
    static class Book {
        String name;
        String description;
    }
    ....
}
```

这样就可以像使用普通Spring Bean一样，将其注入到其他类中使用。

### 5.3.@PropertySource(不常用)

@PropertySource指定读取的properties文件

```java
@Component
@PropertySource("classpath:website.properties")
public class WebSite{
    @Value("${url}")
    private String url;
    ...
}
```
