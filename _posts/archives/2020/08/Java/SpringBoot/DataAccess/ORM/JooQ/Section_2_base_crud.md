## 基础CURD
-----

通过 DSLContext API 和 Record API，可以完成基础CURD操作。本篇主要通过一些实例代码，讲解最基础的用法。后面的相关篇幅中，也会说到一些扩展以及其他高级用法

从此篇幅开始，以下代码块中不再详细编写关于DSLContext的创建过程，具体可以看 section-1 中讲解的基础初始化方式

1. dslContext 代表DSLContext实例
1. S1_USER 由jOOQ插件生成的表描述常量
1. S1_USER.* 由jOOQ插件生成的表内字段常量

### 1. Insert

jOOQ的数据操作通常有两种方式， 第一种是使用 DSLContext API 以类SQL的语法进行调用，第二种是利用 Record API 进行调用

### 1.1. 类SQL方式

插入操作，最基础的方式，是以写SQL语句的习惯，调用API进行插入，支持批量插入

// 类SQL语法 insertInto 方法第一个参数通常是表常量

```java
dslContext.insertInto(S1_USER, S1_USER.USERNAME, S1_USER.ADDRESS, S1_USER.EMAIL)
        .values("username1", "demo-address1", "diamondfsd@gmail.com")
        .values("username2", "demo-address2", "diamondfsd@gmail.com")
        .execute();
```

// newRecord() 方法标识添加一条记录，通过链式调用，支持批量插入
```java
dslContext.insertInto(S1_USER)
        .set(S1_USER.USERNAME, "usernameSet1")
        .set(S1_USER.EMAIL, "diamondfsd@gmail.com")
        .newRecord()
        .set(S1_USER.USERNAME, "usernameSet2")
        .set(S1_USER.EMAIL, "diamondfsd@gmail.com")
        .execute();
```

### 1.2. Record API

除了通过编写类SQL的API方式插入数据之外，还可以通过Record的API进行插入数据

dslContext.newRecord 方法根据表来创建一个Record对象，可以通过 record.insert() 方法插入数据

```java
S1UserRecord record = dslContext.newRecord(S1_USER);
record.setUsername("usernameRecord1");
record.setEmail("diamondfsd@gmail.com");
record.setAddress("address hello");
record.insert();
```


### 1.3. 批量插入

dslContext.batchInsert(Collection<? extends TableRecord<?>> records)方法，可以进行批量插入操作，由jOOQ生成的S1UserRecord实现了TableRecord接口

```java
List<S1UserRecord> recordList = IntStream.range(0, 10).mapToObj(i -> {
    S1UserRecord s1UserRecord = new S1UserRecord();
    s1UserRecord.setUsername("usernameBatchInsert" + i);
    s1UserRecord.setEmail("diamondfsd@gmail.com");
    return s1UserRecord;
}).collect(Collectors.toList());
dslContext.batchInsert(recordList).execute();
```


### 1.4.  插入后获取自增主键

### 1.4.1. 通过类SQL方式

通过此方法插入数据，可以通过 returning API读取想要返回的数据，此语法支持返回多个值，通过fetchOne()方法可以取到一个Record对象
```java
Integer userId = dslContext.insertInto(S1_USER,
    S1_USER.USERNAME, S1_USER.ADDRESS, S1_USER.EMAIL)
    .values("username1", "demo-address1", "diamondfsd@gmail.com")
    .returning(S1_USER.ID)
    .fetchOne().getId();
```

### 1.4.2. Record API

通过此方法，自增的主键会自动存入record中
```java
S1UserRecord record = dslContext.newRecord(S1_USER);
record.setUsername("usernameRecord1");
record.setEmail("diamondfsd@gmail.com");
record.setAddress("address hello");
record.insert();
```

// 这里的id是插入后数据库返回的自增ID，会自动存入record中，可以通过get方法获取
record.getId();

### 1.5. 主键重复处理

可以针对主键重复时，做两种操作，一种是忽略插入，一种是进行更新操作

### 1.5.1. 主键重复忽略插入

```java
int affecteRow = dslContext.insertInto(S1_USER,
    S1_USER.ID, S1_USER.USERNAME)
    .values(1, "username-1")
    .onDuplicateKeyIgnore()
    .execute();
```

// 这里执行完，返回affecteRow影响行数为0
// 生成的SQL: insert ignore into `learn-jooq`.`s1_user` (`id`, `username`) values (1, 'username-1')

### 1.5.2. 主键重复进行更新

```java
dslContext.insertInto(S1_USER)
    .set(S1_USER.ID, 1)
    .set(S1_USER.USERNAME, "duplicateKey-insert")
    .set(S1_USER.ADDRESS, "hello world")
    .onDuplicateKeyUpdate()
    .set(S1_USER.USERNAME, "duplicateKey-update")
    .set(S1_USER.ADDRESS, "update")
    .execute();
```

// 生成SQL: insert into `learn-jooq`.`s1_user` (`id`, `username`, `address`) values (1, 'duplicateKey-update', 'hello world') on duplicate key update `learn-jooq`.`s1_user`.`username` = 'duplicateKey-update', `learn-jooq`.`s1_user`.`address` = 'update'

### 2. Update

update和insert的用法类似，都是有两种方式进行操作

### 2.1. 类SQL方式

```java
dslContext.update(S1_USER)
    .set(S1_USER.USERNAME, "apiUsername-1")
    .set(S1_USER.ADDRESS, "update-address")
    .where(S1_USER.ID.eq(1))
    .execute()
```

### 2.2. Record API

Record 方式默认通过主键进行作为update语句的where条件

```java
S1UserRecord record = dslContext.newRecord(S1_USER);
record.setId(1);
record.setUsername("usernameUpdate-2");
record.setAddress("record-address-2");
record.update();
```

// 生成SQL:  update `learn-jooq`.`s1_user` set `learn-jooq`.`s1_user`.`id` = 1, `learn-jooq`.`s1_user`.`username` = 'usernameUpdate-2', `learn-jooq`.`s1_user`.`address` = 'record-address-2' where `learn-jooq`.`s1_user`.`id` = 1


```java
S1UserRecord record2 = dslContext.newRecord(S1_USER);
record2.setUsername("usernameUpdate-noID");
record2.update();
```

// 生成SQL: update `learn-jooq`.`s1_user` set `learn-jooq`.`s1_user`.`username` = 'usernameUpdate-noID' where `learn-jooq`.`s1_user`.`id` is null

### 2.3. 批量更新

可以使用 dslContext.batchUpdate 进行批量更新，批量更新还是通过主键条件进行拼接update语句，和之前规则相同

```java
S1UserRecord record1 = new S1UserRecord();
record1.setId(1);
record1.setUsername("batchUsername-1");
S1UserRecord record2 = new S1UserRecord();
record2.setId(2);
record2.setUsername("batchUsername-2");

List<S1UserRecord> userRecordList = new ArrayList<>();
userRecordList.add(record1);
userRecordList.add(record2);
dslContext.batchUpdate(userRecordList).execute();
```

### 3. Select

查询操作基本都是通过类SQL的语法进行操作

### 3.1. 单表查询

基本查询方法，默认查询指定表的所有字段，返回一个结果集的包装，通过Result.into方法，可以将结果集转换为任意指定类型集合，当然也可以通过 Record.getValue 方法取得任意字段值，值类型依赖于字段类型

// select `learn-jooq`.`s1_user`.`id`, `learn-jooq`.`s1_user`.`username`, `learn-jooq`.`s1_user`.`email`, `learn-jooq`.`s1_user`.`address`, `learn-jooq`.`s1_user`.`create_time`, `learn-jooq`.`s1_user`.`update_time` from `learn-jooq`.`s1_user`
Result<Record> fetchResult = dslContext.select().from(S1_USER).fetch();
List<S1UserRecord> result = fetch.into(S1UserRecord.class);

// select `learn-jooq`.`s1_user`.`id`, `learn-jooq`.`s1_user`.`username`, `learn-jooq`.`s1_user`.`email`, `learn-jooq`.`s1_user`.`address`, `learn-jooq`.`s1_user`.`create_time`, `learn-jooq`.`s1_user`.`update_time` from `learn-jooq`.`s1_user` where `learn-jooq`.`s1_user`.`id` in (1, 2)
Result<Record> fetchAll = dslContext.select().from(S1_USER)
                .where(S1_USER.ID.in(1, 2)).fetch();
fetchAll.forEach(record -> {
    Integer id = record.getValue(S1_USER.ID);
    String username = record.getValue(S1_USER.USERNAME);
    String address = record.getValue(S1_USER.ADDRESS);
    Timestamp createTime = record.getValue(S1_USER.CREATE_TIME);
    Timestamp updateTime = record.getValue(S1_USER.UPDATE_TIME);
});

### 3.2. 关联查询

多表关联查询也很简单，和写SQL的方法类似，关联查询出来的结果集，可以自定义一个POJO来储存数据

新建一个POJO，用于储存查询结果

```java
public class UserMessagePojo {
    private String username;
    private String messageTitle;
    private String messageContent;
    //... getter/setter
}
```

之前说过通过into方法可以将结果或结果集转换为任意类型，jOOQ会通过反射的方式，将对应的字段值填充至指定的POJO中。通过关联查询的结果集，可以使用此方法将查询结果转换至指定类型的集合。

```java
Result<Record3<String, String, String>> record3Result =
        dslContext.select(S1_USER.USERNAME,
        S2_USER_MESSAGE.MESSAGE_TITLE,
        S2_USER_MESSAGE.MESSAGE_CONTENT)
        .from(S2_USER_MESSAGE)
        .leftJoin(S1_USER).on(S1_USER.ID.eq(S2_USER_MESSAGE.USER_ID))
        .fetch();
List<UserMessagePojo> userMessagePojoList = record3Result.into(UserMessagePojo.class);
```

### 4. Delete

### 4.1. 类SQL方式

通过此方式可以灵活的构建条件进行删除操作

```java

dslContext.delete(S1_USER).where(S1_USER.USERNAME.eq("demo1")).execute();
```

### 4.2. Record API方式

Record.detele() 方法可以进行删除操作，通过调用此方法是根据对应表的主键作为条件进行删除操作

```java
S1UserRecord record = dslContext.newRecord(S1_USER);
record.setId(2);
int deleteRows = record.delete();
// deleteRows = 1
// SQL: delete from `learn-jooq`.`s1_user` where `learn-jooq`.`s1_user`.`id` = 2

S1UserRecord record2 = dslContext.newRecord(S1_USER);
record2.setUsername("demo1");
int deleteRows2 = record2.delete();
// deleteRows == 0
// SQL: delete from `learn-jooq`.`s1_user` where `learn-jooq`.`s1_user`.`id` is null
```

### 4.3. 批量删除

通过此方法可以进行批量删除操作

```java
S1UserRecord record1 = new S1UserRecord();
record1.setId(1);
S1UserRecord record2 = new S1UserRecord();
record2.setId(2);
dslContext.batchDelete(record1, record2).execute();
// 
List<S1UserRecord> recordList = new ArrayList<>();
recordList.add(record1);
recordList.add(record2);
dslContext.batchDelete(recordList).execute();
```

### 5. POJO和代码生成器配置

之前代码中，进行关联查询时，对于结果集的处理比较麻烦，需要自己创建POJO类进行操作。在实际业务中，多表关联查询是很经常的事情，如果每个查询的结果都需要自己创建POJO来储存数据，那也是不小的工作了，而且操作起来还很繁琐，需要确认每个字段的名称以及类型

对于这样的情况，可以通过jOOQ的代码生成器来解决。代码生成器可以配置在生成代码时，同时生成和表一一对应的POJO，只需要在生成器配置generator块中，加上相关配置即可:

```xml
<generator>
    <generate>
        <pojos>true</pojos>
    </generate>
    <!-- ...  -->
</generator>
```

通过以上配置，代码生成的时候，会同时生成和表一一对应的POJO类。因为jOOQ的代码生成每次都是全量生成的，那么我们在编写相关业务代码的时候，不能去修改jOOQ生成的所有代码，那么在关联查询的时候，我们如果要基于原有的某个POJO添加其他字段，那么我们可以自己创建一个和表名一致的类，然后继承该POJO对象，在添加上我们需要的字段，例如本篇代码实例中的s2_user_message表，在这里我们需要将这张表和s1_user表进行关联查询，为了是查出用户ID对应的用户名

那么我们之前定义的 UserMessagePojo 变成直接继承POJO类 S2UserMessage，然后添加需要关联查询的字段名:

```java
public class UserMessagePojo extends S2UserMessage {
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
```

添加POJO的生产配置后，jOOQ最终生成的目录如下图，比之前来说，多了一个pojos包，用于存放所有POJO

```text
├─src/main/java/.../codegen ---- // 生成路径
│ ├─tables --------------------- // 表定义目录
│ │ ├─pojos -------------------- // 存放和表一一对应的 POJO 类 
│ │ └─records ------------------ // 表Record对象目录
│ ├─DefaultCatalog ------------- // Catalog对象，包含Schema常量
│ ├─Indexes -------------------- // 当前数据库所有的所有常量
│ ├─Keys ----------------------- // 当前数据库所有表主键，唯一索引等常量
│ ├─LearnJooq ------------------ // 数据库`learn-jooq`常量，包含该库所有表描述常量
│ └─Tables --------------------- // 所有数据库表常量
```

于此同时，我们也发现，pojos目录下的所有POJO类名和 tables 目录下所有表描述对象的类名一致，这样开发时，有个麻烦，就是引用的时候还需要去关注所在包路径，不能直观的看出哪个是表描述类或者是POJO类，而且在同一个类中同时用到POJO和表描述类时，会出现要引用全路径的类(xx.xx.xx.XXXX)的情况，降低了代码的可读性

那么如何解决这个问题呢，jOOQ在代码生成的时候，是通过 org.jooq.codegen.GeneratorStrategy 接口，来确定所有文件名称的生成规则。在代码生成器配置中，提供了参数可以自己指定该接口的实现类:

```xml
<generator>
    <strategy>
        <name>com.diamondfsd.jooq.learn.CustomGeneratorStrategy</name>
    </strategy>
    <!-- ... -->
</generator>
```

CustomGeneratorStrategy 自定义的生成器继承了原有的 DefaultGeneratorStrategy，重写了 getJavaClassName 这方法。主要是为了区别出 POJO 和 表描述的类名，通过这样的配置生成出来的POJO名称会变为类似 S2UserMessagePojo，表描述类名为 TS2UserMessage，这样能更好的区分POJO和表描述类名，避免在编码过程中产生由于import错误的类，而导致的代码问题。

```java
public class CustomGeneratorStrategy extends DefaultGeneratorStrategy {
    @Override
    public String getJavaClassName(Definition definition, Mode mode) {
        String result = super.getJavaClassName(definition, mode);
        switch (mode) {
            case POJO:
                result +="Pojo";
                break;
            case DEFAULT:
                if (definition instanceof TableDefinition) {
                    result = "T" + result;
                }
                break;
            default:
                break;
        }
        return result;
    }
}
```

这样我们可以将之前的继承类去除后缀

```java
public class S2UserMessage extends S2UserMessagePojo {
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
```

另外要注意的是，POJO的继承类，不可以放在jOOQ代码生成目标的包内，因为生成代码时，会删除指定目标包内的所有内容，所以由我们自行创建的POJO类，需要放在和代码生成器目标包的同级或者上级包内，才不会被jOOQ的代码生成器删除

例如jOOQ生成器的目标包名为: com.diamondfsd.jooq.learn.codegen

我们的继承类可以放在 com.diamondfsd.jooq.learn.xxx，或者其他更顶级的目录下，避免被生成器删除

### 6. 内容总结

本章源码: https://github.com/k55k32/learn-jooq/tree/master/section-2

所有实例代码都在src/java/test目录内，是写好的测试用例。

本章节讲解了如何进行基础的CURD操作，以及POJO生成。有很多人疑问这个POJO和Record有什么区别，因为Record也有getter/setter方法，这里给大家讲解一下

Record 的储存方式是将字段描述和值储存在两个数组中，下标一一对应。get或set的时候，其实现都是通过字段找到对应的下标，进行数组操作的，这样有个问题是无法将其通过json序列化为一个字符串。而且Record对象通常还包含一些针对字段取值的方式，主要用于操作数据时使用

POJO 是由成员变量和getter/setter组成的，是一个纯粹用于存取数据的类，可以通过json序列化和反序列化，这样在我们进行web开发的时候，可以很方便的进行数据转换处理

在实际业务中，通常我们不会直接使用由 jOOQ 生成的 POJO 类，因为 jOOQ 代码生成是全量的。我们在对POJO做一些修改的后，例如添加一些其他表的关联成员，重新生成时，代码又会被抹去。如果要直接改动jOOQ生成的代码，每次重新生成的时候，还需要去备份一遍原来的，而且还需要根据改动去改代码

解决这个问题方法也很简单，不直接去修改 POJO 类，创建一个继承 POJO 的子类。关联的字段或者其他临时的字段在子类中进行设置成员变量，这样就可以不影响 jOOQ 生成的代码，还能实现我们想要的效果