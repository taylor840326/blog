## 对泛型得处理
-----
### 1. Gson中使用泛型


### 1.1. 泛型对数组或者集合类型的影响

当要通过Gson解析一个数组或者集合类型时，一般有两种方式：

1. 使用数组
1. 使用List

而List对于增删都是比较方便的，所以实际使用是还是List比较多

数组比较简单：
```java
Gson gson = new Gson();
String jsonArray = "[\"Android\",\"Java\",\"PHP\"]";
String[] strings = gson.fromJson(jsonArray, String[].class);
```

而对于List将上面的代码中的 String[].class 直接改为 List<String>.class 是不行的。

对于Java来说List<String> 和List<User> 这俩个的字节码文件只一个那就是List.class，这是Java泛型使用时要注意的问题**泛型擦除**

为了解决的上面的问题，Gson提供了TypeToken来实现对泛型的支持，所以将以上的数据解析为List<String>时需要这样写

```java
Gson gson = new Gson();
String jsonArray = "[\"Android\",\"Java\",\"PHP\"]";
String[] strings = gson.fromJson(jsonArray, String[].class);
List<String> stringList = gson.fromJson(jsonArray, new TypeToken<List<String>>() {}.getType());
```

TypeToken的构造方法是protected修饰的,所以上面才会写成new TypeToken<List<String>>() {}.getType() 而不是 new TypeToken<List<String>>().getType()

### 1.2. 泛型解析对接口POJO的设计影响

泛型的引入可以减少无关的代码,例如如下数据

```json
{"code":"0","message":"success","data":{}}
{"code":"0","message":"success","data":[]}
```

我们真正需要的是data所包含的数据，而code只使用一次，message则几乎不用。

如果Gson不支持泛型或不知道Gson支持泛型的同学一定会这么定义POJO

```java
public class UserResponse {
    public int code;
    public String message;
    public User data;
}
```

当适配其它接口的时候又重新定义一个XXResponse将data的类型改成XX，很明显code，和message被重复定义了多次。

通过泛型可以将code和message字段抽取到一个Result的类中，这样只需要编写data字段所对应的POJO即可：

```java
public class Result<T> {
    public int code;
    public String message;
    public T data;
} 
```
 
这样对于data字段是User时则可以写为 Result<User> ,当是个列表的时候为 Result<List<User>>。