## 为DAO添加分页扩展
-----

为什么要对DAO进行扩展？ 在之前的篇幅中，我有介绍到，由jOOQ生成的DAO带有一些基础的CURD方法，另外还会针对每个字段生成一些查询方法。 但是这些方法比较单一，都是以单一字段为基础，进行查询操作，不足以面对复杂的业务场景。

例如,我们业务中常用到的分页查询、多条件查询等，都没有在被封装在DAO中，只能用 DSLContext 的API来完成。 其实这些方法比较通用，我们可以通过一些的封装，让我们能够以更简单的方式进行这些操作

### 1. 如何进行

通过jOOQ生成的DAO代码可以发现，所有的DAO都继承了 DAOImpl 抽象类，那么我们只需要自己创建一个类去继承 DAOImpl 然后让所有的DAO类继承我们自己创建的类，那么就可以在自己创建的类内，来扩展一些公共方法，给每个DAO去使用

假设我们创建的类名称叫 AbstractExtendDAOImpl，以 S1UserDao 为例，继承关系如下:

S1UserDao [extends] -> AbstractExtendDAOImpl [extends] -> DAOImpl [implement] -> DAO
但是jOOQ生成的代码每次都是根据配置全量生成的，他会删除原有的目标路径然后重新生成，所以在DAO生成后直接修改生成后的代码是不可行的。那么我们只能在生成阶段，去修改最终的生成目标代码

jOOQ 生成器的配置内可以通过 configuration -> generator -> name 来配置具体的生成逻辑，默认使用的是 org.jooq.codegen.JavaGenerator

```xml
<generator>
    <!-- ... -->
    <name>org.jooq.codegen.JavaGenerator</name>
    <!-- ... -->
</generator>
```

我们的目标就是创建一个类继承 JavaGenerator ， 重写某些DAO类的生成方法来实现我们想要的效果，JavaGenerator 类可以重写的方法如下。我们主要是要对DAO进行扩展，所以我们本次重写的方法就是 generateDao

```java
generateArray(SchemaDefinition, ArrayDefinition)           // Generates an Oracle array class
generateArrayClassFooter(ArrayDefinition, JavaWriter)      // Callback for an Oracle array class footer
generateArrayClassJavadoc(ArrayDefinition, JavaWriter)     // Callback for an Oracle array class Javadoc

generateDao(TableDefinition)                               // Generates a DAO class
generateDaoClassFooter(TableDefinition, JavaWriter)        // Callback for a DAO class footer
generateDaoClassJavadoc(TableDefinition, JavaWriter)       // Callback for a DAO class Javadoc

generateEnum(EnumDefinition)                               // Generates an enum
generateEnumClassFooter(EnumDefinition, JavaWriter)        // Callback for an enum footer
generateEnumClassJavadoc(EnumDefinition, JavaWriter)       // Callback for an enum Javadoc

generateInterface(TableDefinition)                         // Generates an interface
generateInterfaceClassFooter(TableDefinition, JavaWriter)  // Callback for an interface footer
generateInterfaceClassJavadoc(TableDefinition, JavaWriter) // Callback for an interface Javadoc

generatePackage(SchemaDefinition, PackageDefinition)       // Generates an Oracle package class
generatePackageClassFooter(PackageDefinition, JavaWriter)  // Callback for an Oracle package class footer
generatePackageClassJavadoc(PackageDefinition, JavaWriter) // Callback for an Oracle package class Javadoc

generatePojo(TableDefinition)                              // Generates a POJO class
generatePojoClassFooter(TableDefinition, JavaWriter)       // Callback for a POJO class footer
generatePojoClassJavadoc(TableDefinition, JavaWriter)      // Callback for a POJO class Javadoc

generateRecord(TableDefinition)                            // Generates a Record class
generateRecordClassFooter(TableDefinition, JavaWriter)     // Callback for a Record class footer
generateRecordClassJavadoc(TableDefinition, JavaWriter)    // Callback for a Record class Javadoc

generateRoutine(SchemaDefinition, RoutineDefinition)       // Generates a Routine class
generateRoutineClassFooter(RoutineDefinition, JavaWriter)  // Callback for a Routine class footer
generateRoutineClassJavadoc(RoutineDefinition, JavaWriter) // Callback for a Routine class Javadoc

generateSchema(SchemaDefinition)                           // Generates a Schema class
generateSchemaClassFooter(SchemaDefinition, JavaWriter)    // Callback for a Schema class footer
generateSchemaClassJavadoc(SchemaDefinition, JavaWriter)   // Callback for a Schema class Javadoc

generateTable(SchemaDefinition, TableDefinition)           // Generates a Table class
generateTableClassFooter(TableDefinition, JavaWriter)      // Callback for a Table class footer
generateTableClassJavadoc(TableDefinition, JavaWriter)     // Callback for a Table class Javadoc

generateUDT(SchemaDefinition, UDTDefinition)               // Generates a UDT class
generateUDTClassFooter(UDTDefinition, JavaWriter)          // Callback for a UDT class footer
generateUDTClassJavadoc(UDTDefinition, JavaWriter)         // Callback for a UDT class Javadoc

generateUDTRecord(UDTDefinition)                           // Generates a UDT Record class
generateUDTRecordClassFooter(UDTDefinition, JavaWriter)    // Callback for a UDT Record class footer
generateUDTRecordClassJavadoc(UDTDefinition, JavaWriter)   // Callback for a UDT Record class Javadoc
```

### 2. 自定义代码块

首先，我们先定义一下之前说到的 AbstractExtendDAOImpl

```java
public abstract class AbstractExtendDAOImpl<R extends UpdatableRecord<R>, P, T> extends DAOImpl<R, P, T>
        implements ExtendDao<R, P, T> {
    // ...
}
```

以 S1UserDao 为例，我们最终目标是，此类生成的时候，直接继承 AbstractExtendDAOImpl 类

### 默认生成

```java
import org.jooq.impl.DAOImpl;
public class S1UserDao extends DAOImpl<S1UserRecord, S1UserPojo, Integer> {
    // ...
}
```

### 自定义生成

```java
import com.diamondfsd.jooq.learn.extend.AbstractExtendDAOImpl;
public class S1UserDao extends AbstractExtendDAOImpl<S1UserRecord, S1UserPojo, Integer> {
    // ...
}
```

我们需要修改的地方很小，就是将其继承的父类替换掉， DAOImpl -> AbstractExtendDAOImpl

### 2.1.  重写生成器

通过继承 JavaGenerator 可以重写指定代码块的生成逻辑，那么我们这里创建 CustomJavaGenerator 类继承 JavaGenerator， 重写 generateDao 来完成我们想要的效果

我们先看一下官方提供的 JavaGenerator.generateDAO 源码，这个方法有两个重载，一个是控制流程负责文件创建，输出，另一个是填充内容，负责文件内容的构建和输出

```java
protected void generateDao(TableDefinition table) {
    JavaWriter out = newJavaWriter(getFile(table, Mode.DAO));
    log.info("Generating DAO", out.file().getName());
    generateDao(table, out);
    closeJavaWriter(out);
}
protected void generateDao(TableDefinition table, JavaWriter out) {
    // ... 代码过多，不做展示
}
```


我们有两个方案来实现我们想要的效果

#### 方案一:

将两个方法都完全复制到 CustomJavaGenerator 类内，然后修改指定的关键字符串就可以实现我们想要的效果。优点是简单，明了，可控性强，缺点是具体的生成代码过多，我们进行修改时，如果遇到jOOQ的版本更新，可能会有点问题

#### 方案二:

通过调用父级方法完成代码内容填充，然后编码进行替换指定的字符串实现我们想要的效果，此方法优点是改动小，不会影响原来的逻辑，缺点是万一jOOQ 类名做了修改（此情况一般不会出现）会出现兼容问题，需要更新我们替换的字符串内容

这里我采用的是第二种方案，比较简单， 具体的实现逻辑也很简单。 

重写 generateDao 方法

1. 添加 import com.diamondfsd.jooq.learn.extend.AbstractExtendDAOImpl; 代码块
1. 调用父类方法进行代码生成
1. 获取生成后的文件内容
1. 删除 import org.jooq.impl.DAOImpl;
1. 将 extends DAOImpl 替换为 extends AbstractExtendDAOImpl;
1. 重新写入到目标文件内

```java
public class CustomJavaGenerator extends JavaGenerator {
    private static final JooqLogger log = JooqLogger.getLogger(CustomJavaGenerator.class);


    /**
     * 重写了 generateDao， 具体的生成逻辑还是调用父级的方法，只是在生成完成后，获取文件内容，
     * 然后对文件指定的内容进行替换操作
     *
     * @param table
     */
    @Override
    protected void generateDao(TableDefinition table) {
        super.generateDao(table);
        File file = getFile(table, GeneratorStrategy.Mode.DAO);
        if (file.exists()) {
            try {
                String fileContent = new String(FileCopyUtils.copyToByteArray(file));
                String oldExtends = " extends DAOImpl";
                String newExtends = " extends AbstractExtendDAOImpl";
                fileContent = fileContent.replace("import org.jooq.impl.DAOImpl;\n", "");
                fileContent = fileContent.replace(oldExtends, newExtends);
                FileCopyUtils.copy(fileContent.getBytes(), file);
            } catch (IOException e) {
                log.error("generateDao error: {}", file.getAbsolutePath(), e);
            }
        }
    }

    @Override
    protected void generateDao(TableDefinition table, JavaWriter out) {
        // 用于生成 import com.diamondfsd.jooq.learn.extend.AbstractExtendDAOImpl 内容
        out.ref(AbstractExtendDAOImpl.class);
        super.generateDao(table, out);

    }
}
```

自定义代码生成器写好后，将 jOOQ 插件配置的代码生成器目标修改为 CustomJavaGenerator

```xml
<generator>
    <name>com.diamondfsd.jooq.learn.CustomJavaGenerator</name>
    <!-- ... -->
</generator>
```

通过这样配置，调用代码生成器时，会通过我们自定义的类里的逻辑，进行代码生成

### 2.2. 分页查询封装

AbstractExtendDAOImpl 类由我们自己创建，所有生成的DAO都继承了此类， 那么进行公共方法的扩展就很简单，直接在此类里写一些扩展的公共方法即可

这里我定义了一个接口 ExtendDAO 用于定义一些公共方法

```java
public interface ExtendDAO<R extends UpdatableRecord<R>, P, T> extends DAO<R, P, T> {

    /**
     * 获取 DSLContext
     *
     * @return DSLContext
     */
    DSLContext create();

    /**
     * 条件查询单条记录
     *
     * @param condition 约束条件
     * @return <p>
     */
    P fetchOne(Condition condition);

    /**
     * 条件查询单条记录
     *
     * @param condition 约束条件
     * @return Optional<P>
     */
    Optional<P> fetchOneOptional(Condition condition);

    /**
     * 条件查询多条，并排序
     *
     * @param condition  约束条件
     * @param sortFields 排序字段
     * @return POJO 集合
     */
    List<P> fetch(Condition condition, SortField<?>... sortFields);

    /**
     * 读取分页数据
     *
     * @param pageResult 分页参数
     * @param condition  约束条件
     * @param sortFields 排序字段
     * @return 分页结果集
     */
    PageResult<P> fetchPage(PageResult<P> pageResult, Condition condition, SortField<?>... sortFields);

    /**
     * 读取分页数据
     *
     * @param pageResult      分页参数
     * @param selectLimitStep 查询语句
     * @return 分页结果集
     */
    PageResult<P> fetchPage(PageResult<P> pageResult, SelectLimitStep<?> selectLimitStep);

    /**
     * 任意类型读取分页数据
     *
     * @param pageResult      分页参数
     * @param selectLimitStep 查询语句
     * @param mapper          结果映射方式
     * @param <O>             返回类型的泛型
     * @return 分页结果集
     */
    <O> PageResult<O> fetchPage(PageResult<O> pageResult, SelectLimitStep<?> selectLimitStep,
                                RecordMapper<? super Record, O> mapper);

    /**
     * 任意类型读取分页数据
     *
     * @param pageResult      分页参数
     * @param selectLimitStep 查询语句
     * @param pojoType        POJO类型
     * @param <O>             返回类型的泛型
     * @return 分页结果集
     */
    <O> PageResult<O> fetchPage(PageResult<O> pageResult, SelectLimitStep<?> selectLimitStep,
                                Class<O> pojoType);

}
```

AbstractExtendDAOImpl 实现 ExtendDAO 接口，其他的一些实现就不在这里展示了，大家可以看一下源码，这里着重给大家讲解一下分页的实现

这里分页使用的是 MySQL 的 SQL_CALC_FOUND_ROWS 关键字和 SELECT FOUND_ROWS() 语句。 在查询之前，获得原始SQL语句，并且在 select 后拼接上 SQL_CALC_FOUND_ROWS 关键字，在SQL执行完毕后通过 SELECT FOUND_ROWS() 可以查询出总行数

```java
public abstract class AbstractExtendDAOImpl<R extends UpdatableRecord<R>, P, T> extends DAOImpl<R, P, T>
        implements ExtendDAO<R, P, T> {


    @Override
    public DSLContext create() {
        return DSL.using(configuration());
    }

    @Override
    public <O> PageResult<O> fetchPage(PageResult<O> pageResult, SelectLimitStep<?> selectLimitStep,
                                       RecordMapper<? super Record, O> mapper) {
        int size = pageResult.getPageSize();
        int start = (pageResult.getCurrentPage() - 1) * size;
        // 在页数为零的情况下小优化，不查询数据库直接返回数据为空集合的分页包装类
        if (size == 0) {
            return new PageResult<>(Collections.emptyList(), start, 0, 0);
        }
        String pageSql = selectLimitStep.getSQL(ParamType.INLINED);
        String SELECT = "select";

        pageSql = SELECT + " SQL_CALC_FOUND_ROWS " +
                pageSql.substring(pageSql.indexOf(SELECT) + SELECT.length())
                + " limit ?, ? ";

        List<O> resultList = create().fetch(pageSql, start, size).map(mapper);
        Long total = create().fetchOne("SELECT FOUND_ROWS()").into(Long.class);
        PageResult<O> result = pageResult.into(new PageResult<>());
        result.setData(resultList);
        result.setTotal(total);
        return result;
    }
}
```

### 3. 内容总结

本章源码: https://github.com/k55k32/learn-jooq/tree/master/section-9

本章通过使用自定义的代码生成器来完成对DAO进行扩展。 目前只扩展了基础的DAO，添加了一些条件查询方法和分页查询的封装。 主要是希望大家能够了解到对于自定义代码生成器的一些使用方式和可以重写的方法。掌握这些对于jOOQ的功能自定义能够起很大的帮助