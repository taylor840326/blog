## 常见Gson的操作
-----

### 1. 原始类型 (Primitives Type)和引用类型

Java中变量有两种类型。分别是原始类型和引用类型。

如下表格所示：

|序号|原始类型 |引用类型|
|:---|---|---:|
|1|char|Character|
|1|boolean|Boolean|
|1|byte|Byte|
|1|short|Short|
|1|int|Integer|
|1|long|Long|
|1|float|Float|
|1|double|Double|

Gson对这两种类型得处理请参考如下代码：

```java
/*序列化原始类型*/
String intToJson= gson.toJson(1);
String stringToJson= gson.toJson("abc");
String longToJson = gson.toJson(new Long(100));
String flatToJson = gson.toJson(new Float(0.11));
String booleanToJson = gson.toJson(true);
System.out.println(intToJson);
System.out.println(stringToJson);
System.out.println(longToJson);
System.out.println(booleanToJson);
System.out.println(flatToJson);

/*反序列号原始类型*/
Integer jsonToInteger = gson.fromJson(intToJson, Integer.class);
String jsonToString = gson.fromJson(stringToJson, String.class);
Long jsonToLong = gson.fromJson(longToJson, Long.class);
Boolean jsonToBoolean = gson.fromJson(booleanToJson, Boolean.class);
Float jsonToFloat = gson.fromJson(flatToJson, Float.class);
System.out.println(jsonToInteger);
System.out.println(jsonToString);
System.out.println(jsonToLong);
System.out.println(jsonToBoolean);
System.out.println(jsonToFloat);

/*反序列化成想要的类型*/
String jsonConvertIntsToString = gson.fromJson(intToJson, String.class);
System.out.println(jsonConvertIntsToString);
```

### 2. 数组 (Array)

```java
Gson gson = new Gson();
int[] ints = {1,2,3,4,5};
ArrayList<String> strings = new ArrayList<>();
strings.add("beijing");
strings.add("shanghai");
strings.add("guangzhou");

String stringJson = gson.toJson(strings.toArray());
System.out.println(stringJson);
String[] jsonString = gson.fromJson(stringJson, String[].class);
for (String j : jsonString) {
    System.out.println(j);
}
```

### 3. 集合类型 (Collections)

```java
ArrayList<Integer> ints = new ArrayList<>();
ints.add(1);
ints.add(2);
ints.add(3);
ints.add(4);
String intsJson = gson.toJson(ints);
System.out.println(intsJson);
/*这里用到了对泛型的支持，详见泛型部分*/
Type type = new TypeToken<ArrayList<Integer>>() {}.getType();
List<Integer> result = gson.fromJson(intsJson, type);
result.stream().forEach(System.out::println);

HashSet<String> strings = new HashSet<>();
strings.add("beijing");
strings.add("shanghai");
strings.add("guangzhou");
String stringJson = gson.toJson(strings);
Type stringJsonType = new TypeToken<HashSet<String>>() {}.getType();
HashSet<String> jsonToString = gson.fromJson(stringJson, stringJsonType);
jsonToString.stream().forEach(System.out::println);

HashMap<String, String> map01 = new HashMap<>();
map01.put("name","beijing map");
map01.put("cmd","ls");
String mapToJson = gson.toJson(map01);
Type mapJsonType = new TypeToken<HashMap<String, String>>() {}.getType();
HashMap<String,String> jsonToMap = gson.fromJson(mapToJson, mapJsonType);
jsonToMap.keySet().stream().forEach(row -> {
    System.out.println(jsonToMap.get(row));
});
```

### 4. 类 (Class)

### 4.1. 简单类

Gson对一个类的处理要注意如下内容：

* 最好使用关键字private修饰成员变量
* 没有必要使用注解标注一个成员变量是否需要序列化或反序列化。
    * 所有当前类中的成员变量（包括继承自所有父类的成员变量）都默认支持序列化和反序列化。
* 如果一个变量被声明为transient，则这个变量将不会被序列化和反序列化。
* 对nulls对象的执行方法:
    * 序列化时，一个空的成员变量将会在输出中被省去。
    * 反序列化时，在JSON字符串中缺失的字段将会在相应的成员变量中变为空。
* 如果一个成员变量由synthetic关键字标注，在JSON序列化或者反序列化的过程中将会被忽略。
* 如果成员变量对应的是外部类中的内部类，匿名类，本地类则会被忽略，从而不被序列化或反序列化。


### 4.1.1. 最简单得类

```java
public class Student {
    private Long id;
    private String sname;
    private String address;

    transient private String sex;   //此列会被忽略掉

    public Student() {
    }

    public Student(Long id, String sname, String address, String sex) {
        this.id = id;
        this.sname = sname;
        this.address = address;
        this.sex = sex;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Long getId() {
        return id;
    }

    public String getSname() {
        return sname;
    }

    public String getAddress() {
        return address;
    }

    public String getSex() {
        return sex;
    }
}
```

对上面得类进行序列化和反序列化

生成JSON

```java
Student student = new Student();
student.setId((long)1);
student.setSname("tian lei");
student.setAddress("beijing");
student.setSex("1");

String studentToJson = gson.toJson(student);
System.out.println(studentToJson);
```

解析JSON
```
String jsonString = "{\"id\":1,\"sname\":\"tian lei\",\"address\":\"beijing\",\"sex\":1}";
Student jsonToStudent = gson.fromJson(studentToJson, Student.class);
System.out.println(jsonToStudent);
System.out.println(jsonToStudent.getSname());
System.out.println(jsonToStudent.getSex());
```

### 4.1.2. 稍微复杂的类

```java
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CPUEntity {
    private Long id;
    private String cname;
    private Long usage;
}


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DiskEntity {
    private Long id;
    private String dname;
    private Long total;
    private Long used;
    private Double usage_ratio;
}

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MemoryEntity {
    private Long id;
    private String mname;
    private Long total;
    private Long usage;
    private Double usage_ratio;
}

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MetricEntity {
    private Set<CPUEntity> cpuEntity;
    private MemoryEntity memoryEntity;
    private Set<DiskEntity> diskEntity;
}

/*main*/
HashSet<CPUEntity> cpuEntities = new HashSet<>();
for (int i = 1; i < 10; i++) {
	CPUEntity cpuEntity = new CPUEntity();
	cpuEntity.setId((long) i);
	cpuEntity.setCname("c0" + i);
	cpuEntity.setUsage((long) (Math.random() * 100));
	cpuEntities.add(cpuEntity);
}

HashSet<DiskEntity> diskEntities = new HashSet<>();
for (int j = 1; j < 2; j++) {
	DiskEntity diskEntity = new DiskEntity();
	diskEntity.setId((long) j);
	diskEntity.setDname("d" + j);
	diskEntity.setTotal((long) 1024);
	diskEntity.setUsed((long) 1);
	diskEntity.setUsage_ratio((double) 0.1);
	diskEntities.add(diskEntity);
}

MemoryEntity memoryEntity = new MemoryEntity();
memoryEntity.setId((long) 1);
memoryEntity.setMname("m01");
memoryEntity.setTotal((long) 1024);
memoryEntity.setUsage((long) 1);
memoryEntity.setUsage_ratio((double) 1023);


MetricEntity metricEntity = new MetricEntity();
metricEntity.setCpuEntity(cpuEntities);
metricEntity.setMemoryEntity(memoryEntity);
metricEntity.setDiskEntity(diskEntities);

String json = gson.toJson(metricEntity);
System.out.println(json);

MetricEntity metricEntity1 = gson.fromJson(json, MetricEntity.class);
for (CPUEntity cpuEntity : metricEntity1.getCpuEntity()) {
    System.out.println(cpuEntity.getCname());
}

```

### 5. 属性重命名 @SerializedName 注解的使用
       
如果期望的JSON名称与实际类中定义的列名称对不上，就无法完成列的赋值。此时需要使用SerializedName注解帮忙解决问题

如：
       
```json
期望的json格式：
{"name":"tianlei","age":24,"emailAddress":"553948327@qq.com"}

实际：
{"name":"tianlei","age":24,"email_address":"553948327@qq.com"}
```

而此时，我们要把这个JSON字符串的数据反序列化到Person类，这个类的定义如下：

```java
package com.example.demo.entity;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Person {
    private String name;
    private Integer age;
    /*注意emailAddress字段的名称与json字符串的名称对应不上*/
    private String emailAddress;
}
```

此时如果使用gson.fromJson反序列化这个JSON字符串，则会得到如下结果

```java
Person(name=tianlei, age=36, emailAddress=null)
因为类中的列名称emailAddress和JSON字符串中的email_address无法对应，则无法赋值。
```

Gson在序列化和反序列化时需要使用反射，一般各类库都将注解放到annotations包下，打开源码在com.google.gson包下有一个annotations，里面有一个SerializedName的注解类。对于json中email_address这个属性对应POJO的属性则变成：　
       
```java
import com.google.gson.annotations.SerializedName;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Person {
    private String name;
    private Integer age;
    @SerializedName("email_address")
    private String emailAddress;
}

```       

得到正确结果

```java
Person(name=tianlei, age=36, emailAddress=553948327@qq.com)
```


### 6. 为类的字段提供备选属性名

如果接口设计不严谨或者其它地方可以重用该类，其它字段都一样，就emailAddress 字段不一样，比如有下面三种情况那怎么办？重新写一个POJO类?

```json
{"name":"包青天","age":24,"emailAddress":"ikidou@example.com"}
{"name":"包青天","age":24,"email_address":"ikidou@example.com"}
{"name":"包青天","age":24,"email":"ikidou@example.com"}
```

SerializedName注解提供了两个属性，上面用到了其中一个，另外还有一个属性alternate，接收一个String数组。

```java
@SerializedName(value = "emailAddress", alternate = {"email", "email_address"})
public String emailAddress;
```

当上面的三个属性 email_address、email、emailAddress 中出现任意一个时均可以得到正确的结果。

**当多种情况同时出时，以最后一个出现的值为准**。

