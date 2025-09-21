## JPA
-----


### 8.1.创建表

@Entity注解声明一个类对应一个数据库实体

@Table注解作用在类上，设置表名

```java
@Entity
@Table(name = "t_role")
public class Role {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
}
```

### 8.2.创建主键

@Id注解用于声明一个字段为主键

使用@Id声明之后，我们还需要定义主键的生成策略。可以使用@GeneratedValue指定主键生成策略。

对于主键生成的方法在JPA中有两种,分别是

1. 通过@GeneratedValue直接使用JPA内置提供的四种主键生成策略
1. 通过@GenericGenerator声明一个主键策略，然后@GeneratedValue使用这个策略

#### 8.2.1.使用JPA内置的四种策略
JPA使用枚举定义了4种常用的主键生成策略

```java
public enum GenerationType {
    /*
    使用一个特定的数据表格来保存主键
    持久化引擎通过关系数据库的一张特定的表格来生成主键
    */
    TABLE,
    /*
    在某些数据库中不支持主键自增长，他们提供了另外一种叫SEQUENCE的主键生成机制。比如Oracle，PostgreSQL。
    */
    SEQUENCE,
    /*
    主键自增长
    */
    IDENTITY,
    /*
    把主键生成策略交给持久化引擎Persistence Engine
    持久化引擎会根据数据库在以上三种主键生成策略中选择其中一种
    */
    AUTO
}
```

@GeneratedValue注解默认使用的策略时GenerationType.AUTO。

如果后端数据库使用的时MySQL，则GenerationType.IDENTITY策略比较普通一点。

如果是分布式系统的话需要另外考虑使用分布式ID

#### 8.2.2.使用自定义策略

除了上面使用JPA默认的4中策略以外，还可以使用@GenericGenerator注解声明一种主键策略，然后使用@GeneratedValue注解应用这个策略

```java
...
@Id 
@GenericGenerator(name = "identityIdGenerator",strategy = "identity")
@GeneratedValue(generator = "identityIdGenerator")
private Long id;
...
```

以上定义主键策略等同于使用默认的主键策略IDENTITY

```java
...
@Id 
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
...
```

JPA额外提供的主键生成策略有如下几种：
```java
    //详见DefaultIdentifierGeneratorFactory类
    public DefaultIdentifierGeneratorFactory() {
        this.register("uuid2", UUIDGenerator.class);
        this.register("guid", GUIDGenerator.class);
        this.register("uuid", UUIDHexGenerator.class);
        this.register("uuid.hex", UUIDHexGenerator.class);
        this.register("assigned", Assigned.class);
        this.register("identity", IdentityGenerator.class);
        this.register("select", SelectGenerator.class);
        this.register("sequence", SequenceStyleGenerator.class);
        this.register("seqhilo", SequenceHiLoGenerator.class);
        this.register("increment", IncrementGenerator.class);
        this.register("foreign", ForeignGenerator.class);
        this.register("sequence-identity", SequenceIdentityGenerator.class);
        this.register("enhanced-sequence", SequenceStyleGenerator.class);
        this.register("enhanced-table", TableGenerator.class);
    }
```

### 8.3.设置字段类型

@Column注解用于声明字段

```java
@Column(name= "user_name",nullable=false,length=32)
private String userName;
```

设置字段并且加默认值（比较常用）

```java
Column(columnDefinition = "tinyint(1) default 1")
private Boolean enabled;
```

### 8.4.指定不持久化特定字段

@Transient注解用于声明不需要与数据库映射的字段，在保存的时候不需要保存进数据库。

```java
@Entity(name="USER")
public class User {
    ...
    @Transient
    private String secrect;
}
```

除了使用@Transient声明以外，还可以采用下面几种方法

```java
static String secrect;  //static修饰的变量不会持久化
final String secrect = "111111";    //final修饰的变量不会持久化
transient String secrect;   //transient修饰的变量不会持久化
```

### 8.5.声明大字段

@Lob注解用于声明某个字段为大字段


简化声明

```java
@Lob
private String content;
```

更详细的声明

```java
@Lob        
/*
指定Lob类型数据库的获取策略
FetchType.EAGER表示非延迟获取
FetchType.LAZY表示延迟获取
*/
@Basic(fetch = FetchType.EAGER)
/*
定义字段的类型
*/
@Column(name = "content",columnDefinition = "LONGTEXT NOT NULL")
private String content;
```

### 8.6.创建枚举类型的字段

```java
public enum Gender {
    MALE("男性")，
    FEMALE("女性");
    
    private String value;
    Gender(String str){
        value = str;    
    }
}

@Entity
@Table(name = "role")
public class Role{
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private Gender gender; 
    ...
}
```
数据库里面对应存储的是MALE/FEMALE

### 8.7.增加审计功能

只要继承了AbstractAuditBase的类都会默认加上下面四个字段

```java
@Data 
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclas
@EntityListeners(value = AuditingEntityListener.class)
public abstract class AbstractAuditBase{

    @CreatedDate
    @Column(updateable = false)
    @JsonIgnore
    private Instant createdAt;

    @LastModifiedDate
    @JsonIgnore
    private Instant updatedAt;

    @CreatedBy
    @Column(updateable=false)
    @JsonIgnore
    private String createdBy;
    
    @LastModifiedBy
    @JsonIgnore
    private String updatedBy;
}
```

对应的审计功能对应的配置类可能是下面的

```java
@Configuration
@EnableJpaAuditing
public class AuditSecurityConfiguration{
    @Bean
    AuditorAware<String> auditorAware() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext())
                            .map(SecurityContext::getAuthentication)
                            .filter(Authentication::isAuthenticated)
                            .map(Authentication::getName);
    }   
}
```

@CreatedDate注解表示改字段为创建时间字段。在这个实体被insert的时候，会设置此字段的值。

@CreatedBy注解表示该字段为创建人，在执行insert操作的时候会创建字段。

@LastModifiedDate注解表示该字段为修改时间字段。

@LastModifiedBy注解表示该字段更新人。

@EnableJpaAuditing注解表是开启JPA的审计功能。

### 8.8.删除/修改数据

@Modifying注解提示JPA该操作是修改操作，修改操作还要配合@Transactional注解使用

```java
@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    void deleteByUserName(String userName);
}
```

### 8.9.关联关系

@OneToOne注解表示一对一关系

@OneToMany注解表示一对多关系

@ManyToOne注解表示多对一关系

@ManyToMany注解表示多对多关系