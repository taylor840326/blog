## JSON的处理
-----

### 10.1.过滤json数据

@JsonIgnoreProperties注解作用再类上用于过滤掉特定字段不返回或者不解析

```java
//生成json时将userRoles属性过滤
@JsonIgnoreProperties({userRoles})
public class User {
    private String userName;
    private String fullName;
    private String password;
    @JsonIgnore     //JsonIgnore一般用在类的属性上，作用和JsonIgnoreProperties一样
    private List<UserRole> userRoles = new ArrayList<>();
}
```

### 10.2.格式化json数据

@JsonFormat注解一般用来格式化JSON数据。

```java
@JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",timezone = 'GMT')
private Date date;
```

### 10.3.扁平化对象

@JsonUnWrapped注解用来扁平化json数据

```java
@Getter
@Setter
@ToString
public class Account {
    @JsonUnwrapped
    private Location location;
    
    @JsonUnwrapped
    private PersonInfo personInfo;

    @Getter
    @Setter
    @ToString
    public static class Location{
        private String provinceName;
        private String countyName;
    }

    @Getter
    @Setter
    @ToString
    public static class PersonInfo {
        private String userName;
        private String fullName;
    }
}
```

未扁平化之前

```json
{
  "location": {
    "provinceName": "湖北",
    "countyName": "武汉"
  },
  "personInfo": {
    "userName": "coder1234",
    "fullName": "beijing"
  } 
}
```

使用@JsonUnwrapped扁平化对象之后

```json
{
  "provinceName": "湖北"，
  "countyName": "武汉",
  "userName": "coder1234",
  "fullName": "beijing"
}
```
