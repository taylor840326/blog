## Java Bean参数校验

-----

JSR(Java Specification Requests)是一套JavaBean参数校验标准，它定义了很多常用的校验注解。

我们可以直接将这些注解加载我们JavaBean的属性上面，这样就可以在需要校验的时候进行参数检验。

校验的时候我们实际用的是Hibernate Validator框架。

1. Hibernate Validator 4.x是Bean Validation 1.0(JSR 303)的参考实现
1. Hibernate Validator 5.x是Bean Validation 1.1(JSR 349)的参考实现
1. Hibernate Validator 6.0是Bean Validation 2.0(JSR 380)的参考实现

SpringBoot项目的spring-boot-starter-web依赖中已经有hibernate-validator包，不需要引用相关依赖。

需要注意的是：所有的注解，推荐使用JSR注解，即javax.validation.constraints，而不是org.hibernate.validator.constraints

### 6.1.一些常用字段验证的注解

1. @NotEmpty 被注释的字符串的不能为 null 也不能为空
1. @NotBlank 被注释的字符串非 null，并且必须包含一个非空白字符
1. @Null 被注释的元素必须为 null
1. @NotNull 被注释的元素必须不为 null
1. @AssertTrue 被注释的元素必须为 true
1. @AssertFalse 被注释的元素必须为 false
1. @Pattern(regex=,flag=)被注释的元素必须符合指定的正则表达式
1. @Email 被注释的元素必须是 Email 格式。
1. @Min(value)被注释的元素必须是一个数字，其值必须大于等于指定的最小值
1. @Max(value)被注释的元素必须是一个数字，其值必须小于等于指定的最大值
1. @DecimalMin(value)被注释的元素必须是一个数字，其值必须大于等于指定的最小值
1. @DecimalMax(value) 被注释的元素必须是一个数字，其值必须小于等于指定的最大值
1. @Size(max=, min=)被注释的元素的大小必须在指定的范围内
1. @Digits (integer, fraction)被注释的元素必须是一个数字，其值必须在可接受的范围内
1. @Past被注释的元素必须是一个过去的日期
1. @Future 被注释的元素必须是一个将来的日期

### 6.2.验证请求体RequestBody

```java
@Data 
@AllArgsConstructor
@NoArgsConstructor
public class Person {
    @NotNull(message = "classId 不能为空")
    private String classId;
    
    @Size(max = 33)
    @NotNull(message = "name 不能为空")
    private String name;

    @Pattern(regexp = "((^Man$|^Woman$|^UGM$))",message = "sex 值不在可选范围")
    @NotNull(message = "sex 不能为空")
    private String sex;

    @Email(message = "email 格式不正确")
    @NotNull(message = "email 不能为空")
    private String email;
}
```

我们在需要验证的参数上加上了@Valid注解，如果验证失败则会抛出MethodArgumentNotValidException

```java
@RestController
@RequestMapping("/api")
public class PersonController {

    @PostMapping("/person")
    public ResponseEntity<Person> getPerson(@RequestBody @Valid Person person){
        return ResponseEntity.ok().body(person);
    }
}


```

### 6.3.验证请求参数Path Variables 和Request Parameters

一定不要忘记在类上加上@Validated注解，这个参数可以告诉Spring去校验方法参数

```java
@RestController
@RequestMapping("/api")
@Validated
public class PersonController {
    @GetMapping("/person/{id}")
    public ResponseEntity<Integer> getPersonByID(@Valid @PathVariable("id") @Max(value = 5,message = "超过 id 的范围了") Integer id) {
        return ResponseEntity.ok().body(id);
    }
}
```