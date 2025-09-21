## Compact Vs. Pretty Printing for JSON Output Format

-----

一般情况下Gson类提供的 API已经能满足大部分的使用场景。

但有时需要更多特殊、强大的功能时，这时候就引入一个新的类 GsonBuilder。

GsonBuilder从名上也能知道是用于构建Gson实例的一个类，要想改变Gson默认的设置必须使用该类配置Gson

GsonBuilder用法：　

//各种配置  
//生成配置好的Gson

```java
Gson gson = new GsonBuilder().create();
```

　　
### 1. 处理null值

Gson在默认情况下是不自动导出值为null的键

如：
```java
public class User {
     public String name;
     public int age;
　　　//省略
     public String email;
}
Gson gson = new Gson();
User user = new User(张三",24);
System.out.println(gson.toJson(user));  //{"name":"张三","age":24}
```
email字段是没有在json中出现的，当在调试时需要导出完整的json串时或API接中要求没有值必须用Null时，就会比较有用。

```java
Gson gson = new GsonBuilder().serializeNulls() .create();
User user = new User("张三", 24);
System.out.println(gson.toJson(user)); //{"name":"张三","age":24,"email":null}
```


### 2. 格式化输出、日期时间及其它：

```java
Gson gson = new GsonBuilder()
    //序列化null
    .serializeNulls()
    // 设置日期时间格式，另有2个重载方法
    // 在序列化和反序化时均生效
    .setDateFormat("yyyy-MM-dd")
    // 禁此序列化内部类
     .disableInnerClassSerialization()
    //生成不可执行的Json（多了 )]}' 这4个字符）
    .generateNonExecutableJson()
     //禁止转义html标签
    .disableHtmlEscaping()
    //格式化输出
    .setPrettyPrinting()
    .create();
```

### 3. 字段过滤的四种方法

字段过滤是Gson中比较常用的技巧，特别是在Android中，在处理业务逻辑时可能需要在设置的POJO中加入一些字段，但显然在序列化的过程中是不需要的，并且如果序列化还可能带来一个问题就是：循环引用 。

那么在用Gson序列化之前为不防止这样的事件情发生，你不得不作另外的处理。
         
以一个商品分类Category 为例：
```json
{
  "id": 1,
  "name": "电脑",
  "children": [
    {
      "id": 100,
      "name": "笔记本"
    },
    {
      "id": 101,
      "name": "台式机"
    }
  ]
}
```

一个大分类，可以有很多小分类，那么显然我们在设计Category类时Category本身既可以是大分类，也可以是小分类。

并且为了处理业务，我们还需要在子分类中保存父分类，最终会变成下面的情况：

```java
public class Category {     
	public int id;
	public String name;
	public List<Category> children;
    //因业务需要增加，但并不需要序列化
	public Category parent; 
 }
```

但是上面的parent字段是因业务需要增加的，那么在序列化时并不需要，所以在序列化时就必须将其排除。

那么在Gson中如何排除符合条件的字段呢？下面提供4种方法，大家可根据需要自行选择合适的方式。

### 3.1. 基于注解@Expose

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.FIELD})
public @interface Expose{
   boolean serialize() default true;//默认序列化生效
   boolean deserialize() default true;//默认反序列化生效
}
```
@Expose 注解从名字上就可以看出是暴露的意思，所以该注解是用于对外暴露字段的。

该注解必须和GsonBuilder配合使用：

```java
Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();//不配置时注解无效
```
         
使用规则：

简单说来就是需要导出的字段上加上@Expose 注解，不导出的字段不加。注意是不导出的不加。

由于两个属性都有默认的值true，所有值为true的属性都是可以不写的。如果两者都为true，只写 @Expose 就可以。

拿上面的例子来说就是：
```java
public class Category {
    @Expose public int id;// 等价于 @Expose(deserialize = true, serialize = true)
    @Expose public String name;
    @Expose public List<Category> children;
    public Category parent;  //不需要序列化，等价于 @Expose(deserialize = false, serialize = false)
}
```

         
### 3.2. 基于版本和注解@Since @Until

Gson在对基于版本的字段导出提供了两个注解 @Since 和 @Until，需要和GsonBuilder.setVersion(Double)配合使用

Since和Until注解的定义：
```java
 @Documented
 @Retention(RetentionPolicy.RUNTIME)
 @Target({java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.TYPE})
 public @interface Since{
     double value();
 }

 @Documented
 @Retention(RetentionPolicy.RUNTIME)
 @Target({java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.TYPE})
 public @interface Until{
    double value();
 }
```

使用规则：

当GsonBuilder中设置的版本大于等于Since的值时该字段导出，小于Until的值时该该字段导出；当一个字段被@Since和@Until同时注解时，需两者同时满足条件。

```java
public class SinceUntilSample {
    @Since(4) public String since;//大于等于Since
    @Until(5) public String until;//小于Until
    @Since(4) @Until(5) public String all;//大于等于Since且小于Until
 }
 
 Gson gson = new GsonBuilder().setVersion(version).create();
 System.out.println(gson.toJson(sinceUntilSample));
```
        
         
1. 当version <4时，结果：{"until":"until"}
1. 当version >=4 && version <5时，结果：{"since":"since","until":"until","all":"all"}
1. 当version >=5时，结果：{"since":"since"}
         
         
### 3.3. 基于访问修饰符

什么是修饰符？

不知道的话建议看一下java.lang.reflect.Modifier类，这是一个工具类，里面为所有修饰符定义了相应的静态字段，并提供了很多静态工具方法。

```java
System.out.println(Modifier.toString(Modifier.fieldModifiers()));//public protected private static final transient volatile
```

使用方式：

使用GsonBuilder.excludeFieldsWithModifiers构建gson，支持int型的可变参数，参数值由java.lang.reflect.Modifier提供。

```java
Gson gson = new GsonBuilder()
.excludeFieldsWithModifiers(Modifier.FINAL, Modifier.STATIC, Modifier.PRIVATE)//排除了具有private、final或stati修饰符的字段
.create();
```

### 3.4. 基于策略(自定义规则)

上面介绍的了3种排除字段的方法，说实话我除了@Expose以外，其它的都是只在Demo用上过。

用得最多的就是马上要介绍的自定义规则啦，好处是功能强大、灵活，缺点是相比其它3种方法稍麻烦一点，但也仅仅只是相对其它3种稍麻烦一点点而已。
         
基于策略是利用Gson提供的ExclusionStrategy接口，同样需要使用GsonBuilder，相关API 2个，分别是addSerializationExclusionStrategy 和addDeserializationExclusionStrategy，分别针对序列化和反序化时。这里以序列化为例：

```java
Gson gson = new GsonBuilder()
.addSerializationExclusionStrategy(new ExclusionStrategy() {
    @Override
    //返回值决定要不要排除该字段，return true为排除
    public boolean shouldSkipField(FieldAttributes f) {
        //根据字段名排除
        if ("finalField".equals(f.getName())) return true; 

        //获取Expose注解
        Expose expose = f.getAnnotation(Expose.class); 
        //根据Expose注解排除
        if (expose != null && expose.deserialize() == false) return true; 
            return false;
        }

    @Override
    //直接排除某个类 ，return true为排除
    public boolean shouldSkipClass(Class<?> clazz) {
        return (clazz == int.class || clazz == Integer.class);
    }
})
.create();
```
         
 ### 4. 自定义POJO与JSON的字段映射规则
 
GsonBuilder提供了setFieldNamingPolicy和setFieldNamingStrategy两个方法，用来设置字段序列和反序列时字段映射的规则。
          
1、GsonBuilder.setFieldNamingPolicy方法与 Gson 提供的另一个枚举类FieldNamingPolicy配合使用：

```java
  class User {
    String emailAddress;
  }
  Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();//默认
  User user = new User("baiqiantao@sina.com");
  System.out.println(gson.toJson(user));
```
         
该枚举类提供的五种实现方式的效果分别为：
FieldNamingPolicy	结果
1. IDENTITY 个性/特性/恒等式	{"emailAddress":"baiqiantao@sina.com"}
1. LOWER_CASE_WITH_DASHES 小写+破折号	{"email-address":"baiqiantao@sina.com"}
1. LOWER_CASE_WITH_UNDERSCORES 小写+下划线	{"email_address":"baiqiantao@sina.com"}
1. UPPER_CAMEL_CASE 驼峰式+首字母大写	{"EmailAddress":"baiqiantao@sina.com"}
1. UPPER_CAMEL_CASE_WITH_SPACES 驼峰式+空格	{"Email Address":"baiqiantao@sina.com"}
          
2、GsonBuilder.setFieldNamingStrategy方法需要与Gson提供的FieldNamingStrategy接口配合使用，用于实现将POJO的字段与JSON的字段相对应。

上面的FieldNamingPolicy实际上也实现了FieldNamingStrategy接口，也就是说FieldNamingPolicy也可以使用setFieldNamingStrategy方法。

public enum FieldNamingPolicy implements FieldNamingStrategy

用法：
```java
  Gson gson = new GsonBuilder().setFieldNamingStrategy(new FieldNamingStrategy() {
    @Override
    public String translateName(Field f) {//实现自己的规则
        return null;
    }
  })
  .create();
```

注意： @SerializedName注解拥有最高优先级，在加有@SerializedName注解的字段上FieldNamingStrategy不生效！
          
### 5. TypeAdapter 自定义(反)序列化

TypeAdapter 是Gson自2.0（源码注释上说的是2.1）开始版本提供的一个抽象类，用于接管某种类型的序列化和反序列化过程，包含两个主要方法 write(JsonWriter,T) 和 read(JsonReader)，其它的方法都是final方法并最终调用这两个抽象方法。
```java
public abstract class TypeAdapter<T> {
    public abstract void write(JsonWriter out, T value) throws IOException;
    public abstract T read(JsonReader in) throws IOException;
//其它final方法就不贴出来了，包括toJson、toJsonTree、fromJson、fromJsonTree和nullSafe等方法。
}
```

注意：TypeAdapter 以及 JsonSerializer 和 JsonDeserializer 都需要与 .registerTypeAdapter 或 .registerTypeHierarchyAdapter 配合使用，下面将不再重复说明。
               
### 5.1. TypeAdapter 使用示例1

```java
               User user = new User("包青天", 24, "baiqiantao@sina.com";
               Gson gson = new GsonBuilder()
               .registerTypeAdapter(User.class, new UserTypeAdapter())//为User注册TypeAdapter
               .create();
               
               System.out.println(gson.toJson(user));
```

UserTypeAdapter的定义：

```java
public class UserTypeAdapter extends TypeAdapter<User> {
    @Override
    public void write(JsonWriter out, User value) throws IOException {
        out.beginObject();
        out.name("name").value(value.name);
        out.name("age").value(value.age);
        out.name("email").value(value.email);
        out.endObject();
    }
               
    @Override
    public User read(JsonReader in) throws IOException {
        User user = new User();
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
            case "name":
                user.name = in.nextString();
                break;
            case "age":
                user.age = in.nextInt();
                break;
            case "email":
            case "email_address":
            case "emailAddress":
                user.email = in.nextString();
                break;
            }
        }
        in.endObject();
        return user;
    }
}
```
             
当我们为 User.class 注册了 TypeAdapter 之后，那些之前介绍的@SerializedName、FieldNamingStrategy、Since、Until、Expos通通都黯然失色，失去了效果，只会调用我们实现的 UserTypeAdapter.write(JsonWriter, User) 方法，我想怎么写就怎么写。
               
### 5.2. TypeAdapter 使用示例2

再说一个场景，之前已经说过Gson有一定的容错机制，比如将字符串 "24" 转成整数24，但如果有些情况下给你返了个空字符串怎么办？虽然这是服务器端的问题，但这里我们只是做一个示范，不改服务端的逻辑我们怎么容错。

根据我们上面介绍的，我只需注册一个 TypeAdapter 把 Integer/int 的序列化和反序列化过程接管就行了：

```java
Gson gson = new GsonBuilder()
    .registerTypeAdapter(Integer.class, new TypeAdapter<Integer>() {//接管【Integer】类型的序列化和反序列化过程
    //注意，这里只是接管了Integer类型，并没有接管int类型，要接管int类型需要添加【int.class】
    @Override
    public void write(JsonWriter out, Integer value) throws IOException {
        out.value(String.valueOf(value));
        @Override
        public Integer read(JsonReader in) throws IOException {
            try {
                return Integer.parseInt(in.nextString());
            } catch (NumberFormatException e) {
                return -1;//当时Integer时，解析失败时返回-1
            }
        }
    })
    .registerTypeAdapter(int.class, new TypeAdapter<Integer>() {//接管【int】类型的序列化和反序列化过程
    //泛型只能是引用类型，而不能是基本类型
        @Override
        public void write(JsonWriter out, Integer value) throws IOException {
            out.value(String.valueOf(value));
        }
        @Override
        public Integer read(JsonReader in) throws IOException {
            try {
                return Integer.parseInt(in.nextString());
            } catch (NumberFormatException e) {
                return -2;//当时int时，解析失败时返回-2
            }
        }
    })
   .create();

int i = gson.fromJson("包青天", Integer.class); //-1
int i2 = gson.fromJson("包青天", int.class); //-2
System.out.println(i + "  " + i2);//-1  -2
```
              
### 5.3. Json(De)Serializer

JsonSerializer 和JsonDeserializer 不用像TypeAdapter一样，必须要实现序列化和反序列化的过程，你可以据需要选择，如只接管序列化的过程就用 JsonSerializer ，只接管反序列化的过程就用 JsonDeserializer ，如上面的需求可以用下面的代码。

```java
Gson gson = new GsonBuilder()
    .registerTypeAdapter(Integer.class, new JsonDeserializer<Integer>() {
        @Override
        public Integer deserialize(JsonElement j, Type t, JsonDeserializationContext c) throws JsonParseException {
        try {
            return j.getAsInt();
        } catch (NumberFormatException e) {
            return -1;
        }
    }
})
.create();
```
              
下面是所有数字(Number的子类)都转成序列化为字符串的例子：

```java
JsonSerializer<Number> numberJsonSerializer = new JsonSerializer<Number>() {
    @Override
    public JsonElement serialize(Number src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(String.valueOf(src));
    }
};

Gson gson = new GsonBuilder()
	.registerTypeAdapter(Integer.class, numberJsonSerializer)
	.registerTypeAdapter(Long.class, numberJsonSerializer)
	.registerTypeAdapter(Float.class, numberJsonSerializer)
	.registerTypeAdapter(Double.class, numberJsonSerializer)
	.create();
```               
             
### 6. 泛型与继承

使用 registerTypeAdapter 时不能使用父类来替上面的子类型，这也是为什么要分别注册而不直接使用Number.class的原因。

不过换成 registerTypeHierarchyAdapter 就可以使用 Number.class 而不用一个一个的单独注册子类啦！

```java
Gson gson = new GsonBuilder()
.registerTypeHierarchyAdapter (Number.class, numberJsonSerializer)
.create();
```

这种方式在List上体现更为明显，当我们使用registerTypeAdapter为List.class注册TypeAdapter时，其对List的子类(如ArrayList.class)并无效，所以我们必须使用registerTypeHierarchyAdapter方法注册。
               
两者的区别：
               
| |registerTypeAdapter|registerTypeHierarchyAdapter|
|:---|---|---:|
|支持泛型|是|否|
|支持继承|否|是|
               
注意：
如果一个被序列化的对象本身就带有泛型，且注册了相应的TypeAdapter，那么必须调用Gson.toJson(Object,Type)，明确告诉Gson对象的类型；否则，将跳过此注册的TypeAdapter。

```java
Type type = new TypeToken<List<User>>() {}.getType();//被序列化的对象带有【泛型】
               
TypeAdapter<List<User>> typeAdapter = new TypeAdapter<List<User>>() { .../省略实现的方法/ };
               
Gson gson = new GsonBuilder()
.registerTypeAdapter(type, typeAdapter)//注册了与此type相应的TypeAdapter
.create();

String result = gson.toJson(list, type);//明确指定type时才会使用注册的TypeAdapter托管序列化和反序列化
String result2 = gson.toJson(list);//不指定type时使用系统默认的机制进行序列化和反序列化
```
              
### 6.1. TypeAdapterFactory

TypeAdapterFactory，见名知意，用于创建 TypeAdapter 的工厂类。

使用方式：与GsonBuilder.registerTypeAdapterFactory配合使用，通过对比Type，确定有没有对应的TypeAdapter，没有就返回null，有则返回(并使用)自定义的TypeAdapter。

```java
Gson gson = new GsonBuilder()
.registerTypeAdapterFactory(new TypeAdapterFactory() {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (type.getType() == Integer.class || type.getType() == int.class) return intTypeAdapter;
               return null;
        }
    })
.create();
```
              
### 6.2. 注解 @JsonAdapter

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.FIELD})//作用在类或字段上
public @interface JsonAdapter{
    Class<?> value();
    boolean nullSafe() default true;
}
```

上面说JsonSerializer和JsonDeserializer都要配合 GsonBuilder.registerTypeAdapter 使用，但每次使用都要注册也太麻烦了，JsonAdapter注解就是为了解决这个痛点的。使用方法：

```java
@JsonAdapter(UserTypeAdapter.class) //加在类上
public class User {
    public String name;
    public int age;
}
```

使用时就不需要再使用 GsonBuilder去注册 UserTypeAdapter 了。

注意：JsonAdapter的优先级比 GsonBuilder.registerTypeAdapter 的优先级还高。

### 参考资料

```html
https://www.cnblogs.com/jpfss/p/9082143.html
```