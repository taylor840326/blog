## 测试相关

-----

@ActiveProfiles一般作用于测试类上，用于声明生效的Spring配置文件

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@Slf4j
public abstract class TestBeans(){
    ...
}
```

@Test注解声明一个方法为测试方法

@Transactional被声明的测试方法的数据会回滚，避免污染测试数据

@WithMockUser Spring Security提供的用来模拟一个真是用户，并且可以赋予权限
```java
@Test
@Transactional
@WithMockUser(username = "user01",authorities = "ROLE_TEACHER")
public void testFunc() throws Exception {
    ...
}
```
