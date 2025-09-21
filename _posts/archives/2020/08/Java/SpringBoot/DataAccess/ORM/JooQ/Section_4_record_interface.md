## Record 详解
-----


在之前的章节中，基本每章都有出现的 Record 接口，也做过一些简单的讲解和一些代码示例。作为jOOQ中最重要的一个接口，本章将详细讲解 Record 的各种API和基础用法

### 1. Record 形式

Record 是jOOQ定义的用于储存数据库结果记录的一个接口，其主要是将一个表字段的列表和值的列表使用相同的顺序储存在一起，可以看做是一个用于储存列/值的映射的对象。通常有以下几种形式

### 1.1. 表记录

与数据库表一一对应，如果包含主键，会继承UpdatableRecordImpl类，该类提供了使用 update, delete API进行数据操作。进行查询操作时，jOOQ会将结果集包装为一个TableRecord 对象。 在使用代码生成器的时候，会生成更详细的表记录类，包含表的每个字段操作等，通常以表名为开头 XxxxRecord

此类 Record 对象一般都有对应字段的getter/setter方法，但其都只是去调用get/set方法。其储存的方式还是通过两个数组来储存对应列/值的数据的，所以Record对象是不能被JSON直接序列化和反序列化的

### 1.2. UDT 记录

通常用于 Oracle 等支持用户自定义数据类型的数据库记录，这里接触较少，不作讲解

### 1.3. 明确数据的记录

通用记录类型的一种，当你的字段不超过22个时，会根据字段个数反射成 Record1, Record2 … Record22 类的对象。这些对象需要的泛型个数和后面的数字一致，类型和字段类型一致。jOOQ自动生成的Record对象里，如果字段个数不超过 22 个，会同时实现 Record[N] 接口

举个例子

 s1_user 这张表有6个字段，其生成的 Record 对象，继承了 UpdatableRecordImpl 并且实现了 Record6 接口。Record6接口的泛型参数和表字段类型一一对应，类型顺序和数据库内字段顺序一致

```java
class S1UserRecord extends UpdatableRecordImpl<S1UserRecord>
    implements Record6<Integer, String, String, String, Timestamp, Timestamp>
```

s4_columen_gt22 这张表来用于演示，该表的字段因为超过了22个，所以不会去实现Record[N] 接口，只继承了UpdatableRecordImpl

```java
class S4ColumenGt22Record extends UpdatableRecordImpl<S4ColumenGt22Record>
```

再看看 Record[N] 的接口定义，此接口主要是提供了获取字段，获取值，设置值的方法。由泛型决定字段/值类型和顺序，N 决定字段/值的个数。此接口的目的其实也很简单，就是为了更快速的操作指定位置的字段/值

```java
interface Record[N]<T1, ... T[N]> {
    // 获取字段
    Field<T1> field1();
    ...
    Field<T[N]> field[N]();

    // 获取值
    T1 value1();
    ...
    T[N] valueN();

    // 设置值
    Record[1]<T1> value1(T1 value)
    ...
    Record[N]<TN> value1(T[N] value)
}
```

### 2. 创建 Record 对象

这里说的创建Record对象，指的是在jOOQ已经生成了对应表的Record类的情况下，创建Record进行。使用 Record 提供的方法，我们可以方便的做一些针对表数据的操作。创建对象的方式有以下几种

### 2.1. new

由jOOQ生成的Record对象，可以通过 new 的方式直接创建一个实例。 通过直接 new 的方式创建对象，由于没有连接相关信息，无法直接进行 insert, update, delete 方法的调用。但是可以通过 DSLContext 的API进行操作数据操作，通过这种方式创建的Record对象可以理解为一个单纯的数据储存对象

```java
S1UserRecord s1UserRecord = new S1UserRecord();
s1UserRecord.setUsername("new1");
s1UserRecord.setEmail("diamondfsd@gmail.com");
// insert会报错，因为没有连接配置
s1UserRecord.insert();

// 不需要获取主键，直接执行insert
int row = dslContext.insertInto(S1_USER).set(s1UserRecord)
        .execute()

// 执行insert并返回相应字段
Integer id = dslContext.insertInto(S1_USER).set(s1UserRecord)
                .returning(S1_USER.ID)
                .fetchOne().getId();
```

### 2.2. newRecord

在获取到 DSLContext 实例后，可以使用 dslContext.newRecord(Table<?> table) 方法来创建一个指定表的Record对象，这也是比较常用的方法。通过此方法创建的对象包含了dslContext内的数据连接配置，可以直接进行 insert, update, delete 等操作

```java
S1UserRecord s1UserRecord = dslContext.newRecord(S1_USER);
s1UserRecord.setUsername("newRecord1");
s1UserRecord.setEmail("diamondfsd@gmail.com");
s1UserRecord.insert();
```

### 2.3. fetch

通过fetch*方法读取到结果 Record 对象，同样带有数据库连接相关配置，并且带有查询结果的数据。可以直接进行数据操作

```java
S1UserRecord s1UserRecord = dslContext.selectFrom(S1_USER).where(S1_USER.ID.eq(1))
        .fetchOne();
s1UserRecord.setEmail("hello email");
int row = s1UserRecord.update();

//

S1UserRecord fetchIntoUserRecord = dslContext.select().from(S1_USER)
        .where(S1_USER.ID.eq(1))
        .fetchOneInto(S1UserRecord.class);
fetchIntoUserRecord.setEmail("hello email2");
int row2 = fetchIntoUserRecord.update();
```

### 3. 数据库交互API

### 3.1. insert

此方法进行数据插入操作，几个重载可以指定插入的数据字段

1. insert() 插入所有set后的字段
1. insert(Field<?> ... feilds) 插入指定set后的字段
1. insert(Collection<? extends Field<?>> fields) 插入指定set后的字段

需要注意的是，插入的字段必须显式的进行set操作，才会在最终的SQL语句中体现出来

```java
// 常规用法
S1UserRecord userRecord = dslContext.newRecord(S1_USER);
userRecord.setUsername("insertUsername");
userRecord.setEmail("email");
userRecord.insert();
// insert into `learn-jooq`.`s1_user` (`username`, `email`)
// values ('insertUsername', 'email')

// 指定字段插入
userRecord = dslContext.newRecord(S1_USER);
userRecord.setUsername("insertUsername");
userRecord.setEmail("email");
userRecord.insert(S1_USER.USERNAME, S1_USER.ADDRESS);
// insert into `learn-jooq`.`s1_user` (`username`)
// values ('insertUsername')
```

### 3.2. update

此方法进行更新操作，和 insert 方法类似，重载的也一样

1. update() 更新所有set后的字段
1. update(Field<?> ... feilds) 更新指定set后的字段
1. update(Collection<? extends Field<?>> fields) 更新指定set后的字段

重载参数的目的是为了约束更新的字段，同样的，只有经过set的字段，才会被更新处理

```java
S1UserRecord userRecord = dslContext.selectFrom(S1_USER)
                .where(S1_USER.ID.eq(1))
                .fetchSingle();
userRecord.setEmail(null);
userRecord.setUsername("hello");
userRecord.update(S1_USER.USERNAME, S1_USER.ADDRESS);
// update `learn-jooq`.`s1_user`
// set `learn-jooq`.`s1_user`.`username` = 'hello'
// where `learn-jooq`.`s1_user`.`id` = 1
```

### 2.3. delete

delete 方法根据主键进行数据删除操作

### 2.3.1. delete() 根据主键删除数据

```java
// 有主键值
S1UserRecord userRecord = dslContext.newRecord(S1_USER);
userRecord.setId(1);
userRecord.delete();
// delete from `learn-jooq`.`s1_user` where `learn-jooq`.`s1_user`.`id` = 1

// 无主键值
userRecord = dslContext.newRecord(S1_USER);
userRecord.delete();
// delete from `learn-jooq`.`s1_user` where `learn-jooq`.`s1_user`.`id` is null
```

### 3. 数据处理API

### 3.1. get

get 系列方法主要是用于获取字段值

get(..) 可以取任意字段的值，非常通用，有很多重载，基本涵盖了所有业务场景，包括取值，转换。 这里列举几个常见用法

```java

Record record = dslContext.select().from(S1_USER).limit(1).fetchOne();
// 直接取出指定字段数据
Integer id = record.get(S1_USER.ID);
// 取出数据并指定转换的数据类型
Long createTimeLong = record.get(S1_USER.CREATE_TIME, Long.class);
get[FieldName]() jOOQ生成和表字段一一对应的getter方法，可以通过此方法直接获取指定字段，实际调用的是 get(fieldIndex) 方法

S1UserRecord userRecord = dslContext.select().from(S1_USER)
        .fetchAny().into(S1_USER);
Integer id = userRecord.getId();
Timestamp createTime = userRecord.getCreateTime();
value[N] 方法为Record[N] 接口提供的类，查询明确个数的字段值时，可以快速的获取对应位置下标的值

Record3<Integer, String, Timestamp> record3 = dslContext
        .select(S1_USER.ID,
                S1_USER.USERNAME,
                S1_USER.CREATE_TIME)
        .from(S1_USER)
        .fetchAny();

Integer id = record3.value1();
String username = record3.value2();
Timestamp createTime = record3.get(S1_USER.CREATE_TIME);
```

### 3.2. set

set 系列方法主要是用于设置字段值

set(..) 可以设置字段的值，通常在设置值后用于 insert/update 操作

```java
S1UserRecord userRecord = dslContext.newRecord(S1_USER);
userRecord.set(S1_USER.ID, 1);
userRecord.set(S1_USER.USERNAME, "username");
userRecord.update();
set[FieldName]() jOOQ生成和表字段一一对应的setter方法，可以通过此方法直接设置指定字段值，实际调用的是 set(fieldIndex, object) 方法

S1UserRecord userRecord = dslContext.newRecord(S1_USER);
userRecord.setId(1);
userRecord.setUsername("username");
userRecord.update();
```

### 3.3. changed

此方法用于修改字段更新标识，一般 update/insert 方法配合使用，可以设置指定字段是否 更新/储存

```java
S1UserRecord userRecord = dslContext.newRecord(S1_USER);
userRecord.setId(1);
userRecord.setUsername("username");
userRecord.setEmail(null);
userRecord.changed(S1_USER.EMAIL, false);
userRecord.update();
```

### 3.4. reset

此方法用于重置字段更新标识，效果和 changed(Field field, false) 相同

```java
S1UserRecord userRecord = dslContext.newRecord(S1_USER);
userRecord.setId(1);
userRecord.setUsername("username");
userRecord.setEmail(null);
userRecord.reset(S1_USER.EMAIL);
userRecord.update();
```

### 4. 对象转换API

Record 提供了转换类API，可以方便快捷的将Record转换为其他任意类型，也可以将任意类型填充至Record对象中。其核心主要是 from/into 系列方法

### 4.1. from

此系列方法包含 from(...), fromMap(...) , fromArray(...) 三个方法，主要是可以将任意对象填充至Record中

### 4.1.1. from(Object object)

```java
S1UserPojo userPojo = new S1UserPojo();
userPojo.setUsername("username");
userPojo.setAddress("address");
S1UserRecord userRecord = dslContext.newRecord(S1_USER);
userRecord.from(userPojo);
```

### 4.1.2. fromMap(Map<String, ?> map)

```java
Map<String, Object> userDataMap = new HashMap<>();
userDataMap.put("username", "username1");
userDataMap.put("address", "user-address-1");
S1UserRecord userRecord1 = dslContext.newRecord(S1_USER);
userRecord1.fromMap(userDataMap);
```

### 4.1.3. fromArray(Object[] array, Field<?> ... fields)

```java
Object[] userDataArray = new Object[]{"username2", "user-address-2"};
S1UserRecord userRecord2 = dslContext.newRecord(S1_USER);
userRecord2.fromArray(userDataArray, S1_USER.USERNAME, S1_USER.ADDRESS);
```

### 4.2. into

此系列方法是将Record转换为其他任意指定类型，常用的方法有

### 4.2.1. into(Class<?> type)

此方法主要是通过反射的方式，创建目标类实例，并将对应字段值设置到目标实例中，也是在业务代码中用的最多的一个重载

```java
S1UserRecord userRecord = dslContext.selectFrom(S1_USER)
        .where(S1_USER.ID.eq(id))
        .fetchOne();
S1UserPojo userPojo = userRecord.into(S1UserPojo.class);
```

### 4.2.2. into(Field<?> ... fields)

```java

Record2<Integer, String> record2 =  userRecord
                                .into(S1_USER.ID, S1_USER.USERNAME);
Integer id = record2.value1();
```

### 4.2.3. intoArray()

```java
Object[] userArray = userRecord.intoArray();
Integer fromArrayId = (Integer) userArray[0];
```

### 4.2.4. intoMap()

```java
Map<String, Object> userMap = userRecord.intoMap();
Integer mapId = userMap.get("id");
```

### 4.2.5. intoResultSet()

```java

ResultSet resultSet = userRecord.intoResultSet();
```

### 5. 内容总结

本章源码: https://github.com/k55k32/learn-jooq/tree/master/section-4

本章主要是讲解了 Record 的各种形式和常用的API，单独拿一章出来讲解 Record，主要是因为在jOOQ中，基本所有操作都是在和 Record 类接口打交道。本章对大部分常用API做了简单的示例，大家可以根据测试源码内测试用例进行参考，能更好的掌握 Record API的使用