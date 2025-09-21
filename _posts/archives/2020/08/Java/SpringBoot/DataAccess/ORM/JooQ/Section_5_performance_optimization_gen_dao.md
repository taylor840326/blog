## interfaces 和 daos
-----

本章主要讲解一下 jOOQ 代码生成器的 interfaces 和 daos 配置， 这两个配置分别是生成表对应的接口和DAO

### 1. interfaces

jOOQ在代码生成时，可以通过此配置，来为POJO和Record生成一个接口与表一一对应的接口，接口定义了getter/setter方法，并且定义了接口类型为参数的 from/into 方法。通过此配置，相同表的POJO和Record之间互相转换的性能会有所提升

Maven 配置

```xml
<configuration>
    <!-- ...  -->
    <generator>
        <generate>
            <pojos>true</pojos>
            <interfaces>true</interfaces>
        </generate>
        <!-- ... -->
    </generator>
</configuration>
```

### 1.1. 代码演示

接口代码的定义

```java
public interface IS1User extends Serializable {
    // getter/setter
    // ...

    public void from(com.diamondfsd.jooq.learn.codegen.tables.interfaces.IS1User from);

    public <E extends com.diamondfsd.jooq.learn.codegen.tables.interfaces.IS1User> E into(E into);
}
```

POJO 和 Record 生成时会实现此接口

```java
// S1UserPojo
public class S1UserPojo implements IS1User {
    //...
}

// S1UserRecord
public class S1UserRecord extends UpdatableRecordImpl<S1UserRecord> 
    implements Record6<Integer, String, String, String, Timestamp, Timestamp>, IS1User { 
}
```

POJO 和 Record 类除了原有的 getter/setter 方法外，会同时实现接口定义的 from/into 方法，生成的实现代码如下:

```java
@Override
public void from(IS1User from) {
    setId(from.getId());
    setUsername(from.getUsername());
    setEmail(from.getEmail());
    setAddress(from.getAddress());
    setCreateTime(from.getCreateTime());
    setUpdateTime(from.getUpdateTime());
}

@Override
public <E extends IS1User> E into(E into) {
    into.from(this);
    return into;
}
```

可以看到生成的into方法实际调用的是来源对象的from方法，这个from方法和父级的 from(Object any) 不同的是。此方法是通过主动调用set方法进行值设置。from(Object obj)这类通用型的转换方法，都是通过反射进行值设置。一个是直接方法调用，一个是反射调用，显然通过接口生成的 from/into 方法实现，有着更高的性能优势

接口的好处还有可以将Record和POJO之间互相关联起来，这个在之后做通用DAO的时候会用到，这里不做详解

### 2. daos

主流的ORM框架，都可以通过反向工程来生成和表一一对应的DAO，jOOQ在代码生成器生成的时候，可以通过配置 daos 选项，来生成DAO类

Maven 配置
```xml
<configuration>
    <!-- ...  -->
    <generator>
        <generate>
            <pojos>true</pojos>
            <interfaces>true</interfaces>
            <daos>true</daos>
        </generate>
        <!-- ... -->
    </generator>
</configuration>
```

### 2.1. 代码演示

以 s1_user 表来说，会生成名为 S1UserDao 的类，此类继承自 org.jooq.impl.DAOImpl, org.jooq.impl.DAOImpl实现了 org.jooq.DAO 接口

首先我们看一下 org.jooq.DAO 接口的定义

```java
/**
 * 此接口为一个通用DAO接口，为POJO提供了一系列的通用操作方法
 *
 * @param <R> Record 类型
 * @param <P> POJO 类型
 * @param <T> 主键类型，如果是单字段主键，则为该字段类型，
 *            如果是联合主键，会使用 `Record[N]<1Type, ... NType>` 作为类型
 *            
 */
public interface DAO<R extends TableRecord<R>, P, T> { 

    void insert(P object) throws DataAccessException;
    void insert(P... objects) throws DataAccessException;
    void insert(Collection<P> objects) throws DataAccessException;

    void update(P object) throws DataAccessException;
    void update(P... objects) throws DataAccessException;
    void update(Collection<P> objects) throws DataAccessException;

    void delete(P object) throws DataAccessException;
    void delete(P... objects) throws DataAccessException;
    void delete(Collection<P> objects) throws DataAccessException;
    void deleteById(T... ids) throws DataAccessException;
    void deleteById(Collection<T> ids) throws DataAccessException;

    boolean exists(P object) throws DataAccessException;
    boolean existsById(T id) throws DataAccessException;

    long count() throws DataAccessException;

    List<P> findAll() throws DataAccessException;

    P findById(T id) throws DataAccessException;

    <Z> List<P> fetch(Field<Z> field, Z... values) throws DataAccessException;

    <Z> List<P> fetchRange(Field<Z> field, Z lowerInclusive, Z upperInclusive) throws DataAccessException;

    <Z> P fetchOne(Field<Z> field, Z value) throws DataAccessException;

    <Z> Optional<P> fetchOptional(Field<Z> field, Z value) throws DataAccessException;
}
```

接口定义了一些常用的CURD操作，其中有一些操作是通过主键来完成的。注意如果没有主键的表，在代码生成时，是不会生成DAO的，例如本章代码内演示的 s4_no_primary表，就没有生成DAO

接下来看一下生成的DAO类，这里举两个例子，一个是单字段主键，一个是联合主键

s1_user 表主键id 类型为Integer 生成的DAO如下，DAOImpl 实现了所有DAO接口的方法，生成的S1UserDAO则根据表生成了针对每个字段的 fetch 方法

```java
public class S1UserDao extends DAOImpl<S1UserRecord, S1UserPojo, Integer> {
    // 通过ID范围查找 sql: where id > lowerInclusive and id < upperInclusive
    public List<S1UserPojo> fetchRangeOfId(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(TS1User.S1_USER.ID, lowerInclusive, upperInclusive);
    }

    // 通过多个ID查找 sql: where id in (values)
    public List<S1UserPojo> fetchById(Integer... values) {
        return fetch(TS1User.S1_USER.ID, values);
    }

    // 通过ID查找 sql: where id = value
    public S1UserPojo fetchOneById(Integer value) {
        return fetchOne(TS1User.S1_USER.ID, value);
    }

    // 通过username范围查找 sql: where username > lowerInclusive and username < upperInclusive
    public List<S1UserPojo> fetchRangeOfUsername(String lowerInclusive, String upperInclusive) {
        return fetchRange(TS1User.S1_USER.USERNAME, lowerInclusive, upperInclusive);
    }

    // 通过多个username查找 sql: where username in (values)
    public List<S1UserPojo> fetchByUsername(String... values) {
        return fetch(TS1User.S1_USER.USERNAME, values);
    }
    // ... fetchXxx
}
```

联合主键的生成方式是这样，对于主键的类型为 Record2<Integer, Integer>，如果是三个字段的联合主键则为 Record3<Type1, Type2, Type3> 以此类推

```java

public class S4UnionKeyDao extends DAOImpl<S4UnionKeyRecord, S4UnionKeyPojo, Record2<Integer, Integer>> {
}
```

通过代码生成器生成的DAO类，针对每个字段都生成了 fetchRangeOfXXX 范围查询, fetchByXXX 根据字段 in 查询的方法，如果是单字段主键还会生成 fetchOneByXXX 方法

这些方法能够满足基本的需求，但是对于一些复杂查询，以及多表关联查询，还是需要使用 DSLContext 的API来完成

### 3.DAO 的初始化

jOOQ生成的DAO有两个构造器，DAO是需要跟数据库交互的，所以这里必须要设置数据库连接才能正常使用

```java
public S1UserDao() {
    super(TS1User.S1_USER, S1UserPojo.class);
}

public S1UserDao(Configuration configuration) {
    super(TS1User.S1_USER, S1UserPojo.class, configuration);
}
```

初始化首先得有Configuration对象，如果已经实例化了 DSLContext， 可以用 DSLContext.configuration() 方法获取配置对象

// 方式1: 
```java
S1UserDao s1UserDao = new S1UserDao(dslContext.configuration());
```

// 方式2:
```java
S1UserDao s1UserDao = new S1UserDao();
s1UserDao.setConfiguration(dslContext.configuration());
```

### 3.1. DAO 代码演示

```java
S1UserDao s1UserDao = new S1UserDao(dslContext.configuration());

S1UserPojo s1UserPojo = s1UserDao.fetchOneById(1);

S1UserPojo userPojo = s1UserDao.findById(1);

List<S1UserPojo> s1UserPojos =
        s1UserDao.fetchByUsername(s1UserPojo.getUsername());

List<S1UserPojo> allUser = s1UserDao.findAll();
```

### 4. 内容总结

本章源码: https://github.com/k55k32/learn-jooq/tree/master/section-5

本章主要是介绍了interface接口和针对表的DAO类生成，以及简单的展示了一下DAO的代码调用

jOOQ的大部分功能到本章为止，已经介绍的差不多了。后续的章节中，将主要和Spring框架结合，介绍在实际业务中jOOQ的使用经验