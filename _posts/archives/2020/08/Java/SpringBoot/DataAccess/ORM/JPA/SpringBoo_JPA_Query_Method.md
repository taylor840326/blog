## SpringBoo JPA 7种查询方式

-----

### 1. 通过方法名来创建查询

面对简单查询的时候可以使用这种查询方法。

```java
public interface UserRepository extends Repository<User, Long> {

  List<User> findByEmailAddressAndLastname(String emailAddress, String lastname);
}
```

支持多种语法 例如：

```java
User findFirstByOrderByLastnameAsc();
 
User findTopByOrderByAgeDesc();
 
Page<User> queryFirst10ByLastname(String lastname, Pageable pageable);
 
Slice<User> findTop3ByLastname(String lastname, Pageable pageable);
 
List<User> findFirst10ByLastname(String lastname, Sort sort);
 
List<User> findTop10ByLastname(String lastname, Pageable pageable);
```

常用的命名方式对比

|Index|Keyword|Sample|JPQL snippet|
|:---|---|---|---:|
|1|And|findByLastnameAndFirstname|… where x.lastname = ?1 and x.firstname = ?2|
|2|Or|findByLastnameOrFirstname|… where x.lastname = ?1 or x.firstname = ?2|
|3|Is,Equals|findByFirstnameIs,findByFirstnameEquals|… where x.firstname = ?1|
|4|Between|findByStartDateBetween|… where x.startDate between ?1 and ?2|
|5|LessThan|findByAgeLessThan|… where x.age < ?1|
|6|LessThanEqual|findByAgeLessThanEqual|… where x.age ⇐ ?1|
|7|GreaterThan|findByAgeGreaterThan|… where x.age > ?1|
|8|GreaterThanEqual|findByAgeGreaterThanEqual|… where x.age >= ?1|
|9|After|findByStartDateAfter|… where x.startDate > ?1|
|10|Before|findByStartDateBefore|… where x.startDate < ?1|
|11|IsNull|findByAgeIsNull|… where x.age is null|
|12|IsNotNull,NotNull|findByAge(Is)NotNull|… where x.age not null|
|13|Like|findByFirstnameLike|… where x.firstname like ?1|
|14|NotLike|findByFirstnameNotLike|… where x.firstname not like ?1|
|15|StartingWith|findByFirstnameStartingWith|… where x.firstname like ?1 (parameter bound with appended %)|
|16|EndingWith|findByFirstnameEndingWith|… where x.firstname like ?1 (parameter bound with prepended %)|
|17|Containing|findByFirstnameContaining|… where x.firstname like ?1 (parameter bound wrapped in %)|
|18|OrderBy|findByAgeOrderByLastnameDesc|… where x.age = ?1 order by x.lastname desc|
|19|Not|findByLastnameNot|… where x.lastname <> ?1|
|20|In|findByAgeIn(Collection ages)|… where x.age in ?1|
|21|NotIn|findByAgeNotIn(Collection age)|… where x.age not in ?1|
|22|TRUE|findByActiveTrue()|… where x.active = true|
|23|FALSE|findByActiveFalse()|… where x.active = false|
|24|IgnoreCase|findByFirstnameIgnoreCase|… where UPPER(x.firstame) = UPPER(?1)|

2.JPQL或原生SQL查询


### 2.1. JPQL 

```java
public interface UserRepository extends JpaRepository<User, Long> {

  @Query("select u from User u where u.emailAddress = ?1")
  User findByEmailAddress(String emailAddress);
}

public interface UserRepository extends JpaRepository<User, Long> {

  @Query("select u from User u where u.firstname like %?1")
  List<User> findByFirstnameEndsWith(String firstname);
}
```

### 2.2. 原生sql查询:

```java
public interface UserRepository extends JpaRepository<User, Long> {

  @Query(value = "SELECT * FROM USERS WHERE EMAIL_ADDRESS = ?1", nativeQuery = true)
  User findByEmailAddress(String emailAddress);
}

Spring Data JPA does not currently support dynamic sorting for native queries, because it would have to manipulate the actual query declared, which it cannot do reliably for native SQL. You can, however, use native queries for pagination by specifying the count query yourself, as shown in the following example:

public interface UserRepository extends JpaRepository<User, Long> {

  @Query(value = "SELECT * FROM USERS WHERE LASTNAME = ?1",
    countQuery = "SELECT count(*) FROM USERS WHERE LASTNAME = ?1",
    nativeQuery = true)
  Page<User> findByLastname(String lastname, Pageable pageable);
}
```

### 3. 另外一种原生的sql

```java
@Repository
public class SysRoleDaoImpl implements SysRoleDao {
 
    @Autowired
    private EntityManagerFactory factory;
 
    @Override
    public List< SysRole > findByUserId(String id) {
        String sql = "SELECT r.* FROM sys_role_user ru LEFT JOIN sys_role r ON ru.sys_role_id = r.id WHERE ru.sys_user_id =:userId";
        EntityManager manager = factory.createEntityManager();
        Query query = manager.createNativeQuery(sql , SysRole.class);
        query.setParameter("userId" , id);
        List list = query.getResultList();
        manager.close();
        return list;
    }
}
```

### 4.谓语QueryDSL方式

相对Specifications,推荐这种

代码写起来貌似不是那么一大片，另外也支持mongodb等其他。QEntity自动生成，不移动到对应的包里也可以用

参考

```html
http://www.querydsl.com/
https://blog.csdn.net/qq314499182/article/details/79044305
https://blog.csdn.net/liusanyu/article/details/78171513 （这个有BooleanBuilder）
https://blog.csdn.net/xiaoliuliu2050/article/details/79141847
```

```java
interface UserRepository extends CrudRepository<User, Long>, QuerydslPredicateExecutor<User> {
}
Predicate predicate = user.firstname.equalsIgnoreCase("dave")
	.and(user.lastname.startsWithIgnoreCase("mathews"));
 
userRepository.findAll(predicate);
```

### 5. Specifications

相比querydsl,实现Specification 代码片段比较大，不直观，另外貌似只是支持jpa，hibernate,jdo,lucene,不支持MongoDB，推荐querydsl

```java
public interface CustomerRepository extends CrudRepository<Customer, Long>, JpaSpecificationExecutor {
 …
}
List<T> findAll(Specification<T> spec);
public interface Specification<T> {
  Predicate toPredicate(Root<T> root, CriteriaQuery<?> query,
            CriteriaBuilder builder);
}
public class CustomerSpecs {
 
  public static Specification<Customer> isLongTermCustomer() {
    return new Specification<Customer>() {
      public Predicate toPredicate(Root<Customer> root, CriteriaQuery<?> query,
            CriteriaBuilder builder) {
 
         LocalDate date = new LocalDate().minusYears(2);
         return builder.lessThan(root.get(_Customer.createdAt), date);
      }
    };
  }
 
  public static Specification<Customer> hasSalesOfMoreThan(MontaryAmount value) {
    return new Specification<Customer>() {
      public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query,
            CriteriaBuilder builder) {
 
         // build query here
      }
    };
  }
}
List<Customer> customers = customerRepository.findAll(isLongTermCustomer());
```

### 6.Query by Example 

个人不采用，因为每次需要重新new 对象传入，使用不方便

```text
Query by Example is well suited for several use cases:

Querying your data store with a set of static or dynamic constraints.

Frequent refactoring of the domain objects without worrying about breaking existing queries.

Working independently from the underlying data store API.

Query by Example also has several limitations:

No support for nested or grouped property constraints, such as firstname = ?0 or (firstname = ?1 and lastname = ?2).

Only supports starts/contains/ends/regex matching for strings and exact matching for other property types.
```

```java
Person person = new Person();                          
person.setFirstname("Dave");                           
 
ExampleMatcher matcher = ExampleMatcher.matching()     
  .withIgnorePaths("lastname")                         
  .withIncludeNullValues()                             
  .withStringMatcherEnding();                          
 
Example<Person> example = Example.of(person, matcher); 
```

### 7. namequery方法

不采用，因为写在实体类上面，不够独立，官方也不推荐

```java
@Entity
@NamedQuery(name = "User.findByEmailAddress",
  query = "select u from User u where u.emailAddress = ?1")
public class User {

}
```


### 8. 其他

### 8.1. 参数形式

```java
public interface UserRepository extends JpaRepository<User, Long> {

  @Query("select u from User u where u.firstname = :firstname or u.lastname = :lastname")
  User findByLastnameOrFirstname(@Param("lastname") String lastname,
                                 @Param("firstname") String firstname);
}
```

As of version 4, Spring fully supports Java 8’s parameter name discovery based on the -parameters compiler flag. By using this flag in your build as an alternative to debug information, you can omit the @Param annotation for named parameters.

如果编译的时候加上 -parameters  这个参数，代码中就不用加@Param了？ 还是说默认就支持了，不用加@Param?


### 8.2. 排序

 如果是分页的话，分页参数pageable里面有排序，不需要额外加sort参数
 
```java
public interface UserRepository extends JpaRepository<User, Long> {

  @Query("select u from User u where u.lastname like ?1%")
  List<User> findByAndSort(String lastname, Sort sort);

  @Query("select u.id, LENGTH(u.firstname) as fn_len from User u where u.lastname like ?1%")
  List<Object[]> findByAsArrayAndSort(String lastname, Sort sort);
}

repo.findByAndSort("lannister", new Sort("firstname"));               
repo.findByAndSort("stark", new Sort("LENGTH(firstname)"));         --无效，抛异常  
repo.findByAndSort("targaryen", JpaSort.unsafe("LENGTH(firstname)")); 
repo.findByAsArrayAndSort("bolton", new Sort("fn_len")); 
```

### 8.3. SpEL 表达式，用在公共查询上

公共的数据层，如果有通用的参数，可以采用以下方法

```java
@MappedSuperclass
public abstract class AbstractMappedType {
  …
  String attribute
}

@Entity
public class ConcreteType extends AbstractMappedType { … }

@NoRepositoryBean
public interface MappedTypeRepository<T extends AbstractMappedType>
  extends Repository<T, Long> {

  @Query("select t from #{#entityName} t where t.attribute = ?1")
  List<T> findAllByAttribute(String attribute);
}

public interface ConcreteRepository
  extends MappedTypeRepository<ConcreteType> { … }
```


### 8.4. 查询返回定制

```java
class Person {
 
  @Id UUID id;
  String firstname, lastname;
  Address address;
 
  static class Address {
    String zipCode, city, street;
  }
}
 
interface PersonRepository extends Repository<Person, UUID> {
 
  Collection<Person> findByLastname(String lastname);
}
interface NamesOnly {
 
  String getFirstname();
  String getLastname();
}
interface PersonRepository extends Repository<Person, UUID> {
 
  Collection<NamesOnly> findByLastname(String lastname);
}
interface NamesOnly {
 
  @Value("#{target.firstname + ' ' + target.lastname}")
  String getFullName();
  …
}
interface NamesOnly {
 
  String getFirstname();
  String getLastname();
 
  default String getFullName() {
    return getFirstname.concat(" ").concat(getLastname());
  }
}
interface NamesOnly {
 
  String getFirstname();
  String getLastname();
 
  default String getFullName() {
    return getFirstname.concat(" ").concat(getLastname());
  }
}
class NamesOnly {
 
  private final String firstname, lastname;
 
  NamesOnly(String firstname, String lastname) {
 
    this.firstname = firstname;
    this.lastname = lastname;
  }
 
  String getFirstname() {
    return this.firstname;
  }
 
  String getLastname() {
    return this.lastname;
  }
 
  // equals(…) and hashCode() implementations
}
```
