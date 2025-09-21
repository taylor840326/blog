### 查询结果处理
-----


POJO是一个简单的Java对象，主要是包含一些属性和 getter/setter 方法，在业务中常用到的是用于传输数据以及作为参数传递。 在Web应用的场景中，也通常用来和前端做数据交互

jOOQ的代码生成器能够帮我们根据表结构生成对应的POJO，能很大程度上减少我们自己创建POJO的工作量，当然，此功能也是大部分ORM框架的必备功能。本章主要讲解各种方式将数据结果转换为我们想要的格式

### 1. Fetch 系列 API

查询操作通常以fetch API 作为结束API，例如常用的有，所有的读取类方法都差不多，掌握一个就能很快的举一反三

### 1.1. 读取多条

1. fetch 读取集合
1. fetchSet 读取并返回一个Set集合，常用于去重
1. fetchArray 读取并返回一个数组

### 1.2. 读取单条

1. fetchOne 读取单条记录，如果记录超过一条会报错
1. fetchAny 读取单条记录，如果有多条，会取第一条数据
1. fetchSingle 读取单条记录，如果记录为空或者记录超过一条会报错

### 1.3. 读取并返回Map

1. fetchMap 读取并返回一个Map
1. fetchGroups 读取并返回一个分组Map

### 2. fetch

作为一个常用的读取多条记录的API，其他几个读取多条的方法和这个方法类似，只是返回值不同

fetchSet, fetchArray 方法和 fetch 方法一样，都是返回多条数据，只是返回的格式不同，fetch通常返回List或者jOOQ的Result对象

接下来介绍一下几个方法重载的返回值

### 2.1. fetch()

无参调用此方法，返回的是一个Result<Record>结果集对象

```java
Result<Record> records = dslContext.select().from(S1_USER).fetch();
```

### 2.2. fetch(RecordMapper mapper)

RecordMapper接口的提供map方法，用于来返回数据。map 方法传入一个 Record 对象。可以使用lambda表达式将 Record 对象转换成一个指定类型的POJO

```java
List<S1UserPojo> userPojoList = dslContext.select()
            .from(S1_USER)
            .where(S1_USER.ID.eq(1))
            .fetch(r -> r.into(S1UserPojo.class));
```

多表查询，字段相同时，直接用into方法将结果集转换为POJO时，相同字段名称的方法会以最后一个字段值为准。这时候，我们可以现将结果集通过 into(Table table) 方法将结果集转换为指定表的Record对象，然后再into进指定的POJO类中

```java
// 多表关联查询，查询s2_user_message.id = 2的数据，直接into的结果getId()却是1
// 这是因为同时关联查询了s1_user表，该表的id字段值为1
List<S2UserMessage> userMessage = dslContext.select().from(S2_USER_MESSAGE)
        .leftJoin(S1_USER).on(S1_USER.ID.eq(S2_USER_MESSAGE.USER_ID))
        .where(S2_USER_MESSAGE.ID.eq(2))
        .fetch(r -> r.into(S2UserMessage.class));
// userMessage.getId() == 1

// 将结果集into进指定的表描述中，然后在into至指定的POJO类
List<S2UserMessage> userMessage2 = dslContext.select().from(S2_USER_MESSAGE)
        .leftJoin(S1_USER).on(S1_USER.ID.eq(S2_USER_MESSAGE.USER_ID))
        .where(S2_USER_MESSAGE.ID.eq(2))
        .fetch(r -> {
            S2UserMessage fetchUserMessage = r.into(S2_USER_MESSAGE).into(S2UserMessage.class);
            fetchUserMessage.setUsername(r.get(S1_USER.USERNAME));
            return fetchUserMessage;
        });
// userMessage.getId() == 2
```

### 2.3. fetch(Field<?> field)

Field是一个接口，代码生成器生成的表字段常量例如 S1_USER.ID, 都实现了 Field 接口，这个重载可以直接取出指定表字段，会自动根据传入的字段推测其类型
```java

List<Integer> id = dslContext.select().from(S1_USER).where(S1_USER.ID.eq(1))
        .fetch(S1_USER.ID);
```

### 2.4. fetch(String fieldName, Class<?> type)

可以直接通过字段名称字符串获取指定字段值，可以通过第二个参数指定返回值，如果不指定，返回Object

```java
List<Integer> idList = dslContext.select().from(S1_USER).where(S1_USER.ID.eq(1))
        .fetch("id", Integer.class);
```

### 2.5. fetch(int fieldIndex, Class<?> type)

可以通过查询字段下标顺序进行查询指定字段，可以通过第二个参数指定返回值，如果不指定，返回Object

```java
List<Integer> idList = dslContext.select(S1_USER.ID, S1_USER.USERNAME)
        .from(S1_USER).where(S1_USER.ID.eq(1)).fetch(0, Integer.class);
```

### 2.6. fetch*

fetchSet, fetchArray, fetchAny, fetchOne, fetchSingle 这几个方法的和 fetch 方法的用法一致，只是返回值不同，这里不做详解

fetchAny, fetchOne, fetchSingle 方法返回单条数据，但是对于 数据为空、SQL结果为多条数据 的情况下，处理方式各不相同

|方法名|无数据|多条数据|单条数据|
|:---|---|---|---:|
|fetchAny|返回空|返回第一条|正常返回|
|fetchOne|返回空|抛出异常|正常返回|
|fetchSingle|抛出异常|抛出异常|正常返回|


异常信息

1. org.jooq.exception.TooManyRowsException 多条数据时抛出异常
1. org.jooq.exception.NoDataFoundException 无数据时抛出异常

### 2.7. fetchMap

此方法可以将结果集处理为一个Map格式，此方法有很多重载，这里介绍几个常用的，注意，此方法作为key的字段必须确定是在当前结果集中是唯一的，如果出现重复key，此方法会抛出异常

### 2.7.1 fetchMap(Field<K> field, Class<V> type)

以表字段值为key，返回一个 K:V 的Map对象

```java
Map<Integer, S1UserPojo> idUserPojoMap = dslContext.select().from(S1_USER)
                .fetchMap(S1_USER.ID, S1UserPojo.class);
```

### 2.7.2 fetchMap(Feild<K> field, Field<V> field)

以表字段值为key，返回一个 K:V 的Map对象

```java
Map<Integer, String> idUserNameMap = dslContext.select().from(S1_USER)
                .fetchMap(S1_USER.ID, S1_USER.USERNAME);
```

### 2.8. fetchGroups

此方法可以将结果集处理为一个Map格式，和fetchMap类似，只不过这里的值为一个指定类型的集合，通常在处理一对多数据时会用到

### 2.8.1 fetchGroups(Field<K> field, Class<V> type)

以表字段值为Key，返回一个K:List<V> 的Map对象

```java

Map<Integer, List<S2UserMessage>> userIdUserMessageMap = dslContext.select().from(S2_USER_MESSAGE)
                .fetchGroups(S2_USER_MESSAGE.USER_ID, S2UserMessage.class);
```

### 2.8.2 fetchGroups(Field<K> keyField, Field<V> valueField)


以表字段值为Key，返回一个K:List<V> 的Map对象

```java
Map<Integer, List<Integer>> userIdUserMessageIdMap = dslContext.select().from(S2_USER_MESSAGE)
                .fetchGroups(S2_USER_MESSAGE.USER_ID, S2_USER_MESSAGE.ID);
```

### 3. 内容总结

本章源码: https://github.com/k55k32/learn-jooq/tree/master/section-3

文章主要讲解的是各种类型的读取API，掌握好这些API对于jOOQ的使用很有帮助

本章中出现的一下几个接口可能是在编码过程中经常遇到的

Field<T> 接口

由代码生成器生成的所有表字段常量，都是此接口的实现类，包含了字段一些基本信息，例如字段名称，字段数据类型等，所有读取类方法都有基于此字段参数的重载

RecordMapper<? super R, E> mapper 接口

该接口很简单，主要是提供一个 map 方法给大家去实现。此接口只有一个方法，可以通过lambda表达式快速实现该接口。所有读取类方法都有基于此接口参数的重载

```java
public interface RecordMapper<R extends Record, E> {
    E map(R record);
}
```
