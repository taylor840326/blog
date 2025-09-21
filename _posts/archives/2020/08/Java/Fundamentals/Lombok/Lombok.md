## Lombok
-----

### 1.简介
 
### 1.1. Lombok工作原理分析

在Lombok使用的过程中，只需要添加相应的注解，无需再为此写任何代码。自动生成的代码到底是如何产生的呢？核心之处就是对于注解的解析上。

JDK5引入了注解的同时，也提供了两种解析方式。

1. 运行时解析。运行时能够解析的注解，必须将@Retention设置为RUNTIME，这样就可以通过反射拿到该注解。java.lang,reflect反射包中提供了一个接口AnnotatedElement，该接口定义了获取注解信息的几个方法，Class、Constructor、Field、Method、Package等都实现了该接口，对反射熟悉的朋友应该都会很熟悉这种解析方式。
1. 编译时解析

编译时解析有两种机制，分别简单描述下：

1. APT(Annotation Processing Tool)。
apt自JDK5产生，JDK7已标记为过期，不推荐使用，JDK8中已彻底删除，自JDK6开始，可以使用Pluggable Annotation Processing API来替换它，apt被替换主要有2点原因：
api都在com.sun.mirror非标准包下
没有集成到javac中，需要额外运行
1. Pluggable Annotation Processing API。
JSR 269自JDK6加入，作为apt的替代方案，它解决了apt的两个问题，javac在执行的时候会调用实现了该API的程序，这样我们就可以对编译器做一些增强，这时javac执行的过程如下：
这里写图片描述

Lombok本质上就是一个实现了“JSR 269 API”的程序。在使用javac的过程中，它产生作用的具体流程如下：

1. javac对源代码进行分析，生成了一棵抽象语法树（AST）
1. 运行过程中调用实现了“JSR 269 API”的Lombok程序
1. 此时Lombok就对第一步骤得到的AST进行处理，找到@Data注解所在类对应的语法树（AST），然后修改该语法树（AST），增加getter和setter方法定义的相应树节点
1. javac使用修改后的抽象语法树（AST）生成字节码文件，即给class增加新的节点（代码块）

### 1.2. Lombok的优缺点

优点：
1. 能通过注解的形式自动生成构造器、getter/setter、equals、hashcode、toString等方法，提高了一定的开发效率
让代码变得简洁，不用过多的去关注相应的方法
1. 属性做修改时，也简化了维护为这些属性所生成的getter/setter方法等

缺点：
1. 不支持多种参数构造器的重载
虽然省去了手动创建getter/setter方法的麻烦，但大大降低了源代码的可读性和完整性，降低了阅读源代码的舒适度

## 2. Lombok注解

### 2.1. @Getter and @Setter

可以用@Getter / @Setter注释任何字段（当然也可以注释到类上）,让lombok自动生成默认的getter / setter方法。

默认生成的方法是public的，如果要修改方法修饰符可以设置AccessLevel的值，例如：@Getter(access = AccessLevel.PROTECTED)

应用在类上

```java
@Setter
@Getter
public class User {
    private String name;
    private String address;
}
```

应用到字段上

```java
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
public class User {
    @Getter(AccessLevel.PROTECTED) @Setter private Integer id;
    @Getter @Setter private String name;
    @Getter @Setter private String phone;
}
```

### 2.2. @ToString

生成toString()方法。

默认情况下，它会按顺序（以逗号分隔）打印你的类名称以及每个字段。

可以这样设置不包含哪些字段@ToString(exclude = "id") / @ToString(exclude = {"id","name"})

如果继承的有父类的话，可以设置callSuper 让其调用父类的toString()方法，例如：@ToString(callSuper = true)

```java
import lombok.ToString;
@ToString(exclude = {"id","name"})
public class User {
  private Integer id;
  private String name;
  private String phone;
}
```

### 2.3. @EqualsAndHashCode

生成hashCode()和equals()方法。

默认情况下，它将使用所有非静态，非transient字段。

但可以通过在可选的exclude参数中来排除更多字段。或者，通过在parameter参数中命名它们来准确指定希望使用哪些字段。

```java
@EqualsAndHashCode(exclude={"id", "shape"})
public class EqualsAndHashCodeExample {
  private transient int transientVar = 10;
  private String name;
  private double score;
  private Shape shape = new Square(5, 10);
  private String[] tags;
  private transient int id;
  
  public String getName() {
    return this.name;
  }
  
  @EqualsAndHashCode(callSuper=true)
  public static class Square extends Shape {
    private final int width, height;
    
    public Square(int width, int height) {
      this.width = width;
      this.height = height;
    }
  }
}
对比代码如下：

import java.util.Arrays;
public class EqualsAndHashCodeExample {
 private transient int transientVar = 10;
 private String name;
 private double score;
 private Shape shape = new Square(5, 10);
 private String[] tags;
 private transient int id;

 public String getName() {
   return this.name;
 }
 
 @Override public boolean equals(Object o) {
   if (o == this) return true;
   if (!(o instanceof EqualsAndHashCodeExample)) return false;
   EqualsAndHashCodeExample other = (EqualsAndHashCodeExample) o;
   if (!other.canEqual((Object)this)) return false;
   if (this.getName() == null ? other.getName() != null : !this.getName().equals(other.getName())) return false;
   if (Double.compare(this.score, other.score) != 0) return false;
   if (!Arrays.deepEquals(this.tags, other.tags)) return false;
   return true;
 }
 
 @Override public int hashCode() {
   final int PRIME = 59;
   int result = 1;
   final long temp1 = Double.doubleToLongBits(this.score);
   result = (result*PRIME) + (this.name == null ? 43 : this.name.hashCode());
   result = (result*PRIME) + (int)(temp1 ^ (temp1 >>> 32));
   result = (result*PRIME) + Arrays.deepHashCode(this.tags);
   return result;
 }
 
 protected boolean canEqual(Object other) {
   return other instanceof EqualsAndHashCodeExample;
 }
 
 public static class Square extends Shape {
   private final int width, height;
   
   public Square(int width, int height) {
     this.width = width;
     this.height = height;
   }
   
   @Override public boolean equals(Object o) {
     if (o == this) return true;
     if (!(o instanceof Square)) return false;
     Square other = (Square) o;
     if (!other.canEqual((Object)this)) return false;
     if (!super.equals(o)) return false;
     if (this.width != other.width) return false;
     if (this.height != other.height) return false;
     return true;
   }
   
   @Override public int hashCode() {
     final int PRIME = 59;
     int result = 1;
     result = (result*PRIME) + super.hashCode();
     result = (result*PRIME) + this.width;
     result = (result*PRIME) + this.height;
     return result;
   }
   
   protected boolean canEqual(Object other) {
     return other instanceof Square;
   }
 }
}
```

### 2.4. @NoArgsConstructor, @RequiredArgsConstructor, @AllArgsConstructor

@NoArgsConstructor生成一个无参构造方法。当类中有final字段没有被初始化时，编译器会报错，此时可用@NoArgsConstructor(force = true)，然后就会为没有初始化的final字段设置默认值 0 / false / null。对于具有约束的字段（例如@NonNull字段），不会生成检查或分配，因此请注意，正确初始化这些字段之前，这些约束无效。

```java
import lombok.NoArgsConstructor;
import lombok.NonNull;
@NoArgsConstructor(force = true)
public class User {
    @NonNull private Integer id;
    @NonNull private String name;
    private final String phone ;
}
```

@RequiredArgsConstructor会生成构造方法（可能带参数也可能不带参数），如果带参数，这参数只能是以final修饰的未经初始化的字段，或者是以@NonNull注解的未经初始化的字段
@RequiredArgsConstructor(staticName = "of")会生成一个of()的静态方法，并把构造方法设置为私有的

```java
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
public class User {
  @NonNull private Integer id ;
  @NonNull private String name = "bbbb";
  private final String phone;
}
//另外一个
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor(staticName = "of")
public class User {
  @NonNull private Integer id ;
  @NonNull private String name = "bbbb";
  private final String phone;
}
```

@AllArgsConstructor 生成一个全参数的构造方法

```java
import lombok.AllArgsConstructor;
import lombok.NonNull;
@AllArgsConstructor
public class User {
  @NonNull private Integer id ;
  @NonNull private String name = "bbbb";
  private final String phone;
}
```

### 2.5. @Data

@Data 包含了 @ToString、@EqualsAndHashCode、@Getter / @Setter和@RequiredArgsConstructor的功能

### 2.6. @Accessors

@Accessors 主要用于控制生成的getter和setter
主要参数介绍

fluent boolean值，默认为false。此字段主要为控制生成的getter和setter方法前面是否带get/set
chain boolean值，默认false。如果设置为true，setter返回的是此对象，方便链式调用方法
prefix 设置前缀 例如：@Accessors(prefix = "abc") private String abcAge 当生成get/set方法时，会把此前缀去掉


### 2.7. @Synchronized

给方法加上同步锁
```java
import lombok.Synchronized;
public class SynchronizedExample {
   private final Object readLock = new Object();
   
  @Synchronized
  public static void hello() {
    System.out.println("world");
  }
   
  @Synchronized
  public int answerToLife() {
    return 42;
 }
  @Synchronized("readLock")
  public void foo() {
    System.out.println("bar");
   }
}
//等效代码
public class SynchronizedExample {
private static final Object $LOCK = new Object[0];
private final Object $lock = new Object[0];
private final Object readLock = new Object();
 
public static void hello() {
  synchronized($LOCK) {
    System.out.println("world");
  }
}
 
public int answerToLife() {
 synchronized($lock) {
    return 42;
  }
}

public void foo() {
  synchronized(readLock) {
    System.out.println("bar");
  }
}
}
```

### 2.8. @Wither

提供了给final字段赋值的一种方法

```java
//使用lombok注解的
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.Wither;
public class WitherExample {
  @Wither private final int age;
  @Wither(AccessLevel.PROTECTED) @NonNull private final String name;
   
  public WitherExample(String name, int age) {
    if (name == null) throw new NullPointerException();
    this.name = name;
    this.age = age;
  }
}
//等效代码
import lombok.NonNull;
public class WitherExample {
private final int age;
private @NonNull final String name;

public WitherExample(String name, int age) {
 if (name == null) throw new NullPointerException();
 this.name = name;
 this.age = age;
}

public WitherExample withAge(int age) {
 return this.age == age ? this : new WitherExample(age, name);
}

protected WitherExample withName(@NonNull String name) {
 if (name == null) throw new java.lang.NullPointerException("name");
 return this.name == name ? this : new WitherExample(age, name);
}
}
```

### 2.9. @onX

在注解里面添加注解的方式

直接看代码

```java
public class SchoolDownloadLimit implements Serializable {
    private static final long serialVersionUID = -196412797757026250L;

    @Getter(onMethod = @_({@Id,@Column(name="id",nullable=false),@GeneratedValue(strategy= GenerationType.AUTO)}))
    @Setter
    private Integer id;

    @Getter(onMethod = @_(@Column(name="school_id")))
    @Setter
    private Integer schoolId;


    @Getter(onMethod = @_(@Column(name = "per_download_times")))
    @Setter
    private Integer perDownloadTimes;

    @Getter(onMethod = @_(@Column(name = "limit_time")))
    @Setter
    private Integer limitTime;

    @Getter(onMethod = @_(@Column(name = "download_to_limit_an_hour")))
    @Setter
    private Integer downloadToLimitInHour;

    @Getter(onMethod = @_(@Column(name = "available")))
    @Setter
    private Integer available = 1;
}
```

### 2.10. @Builder

@Builder注释为你的类生成复杂的构建器API。
lets you automatically produce the code required to have your class be instantiable with code such as:

Person.builder().name("Adam Savage").city("San Francisco").job("Mythbusters").job("Unchained Reaction").build();

```java
//使用lombok注解的
import lombok.Builder;
import lombok.Singular;
import java.util.Set;
@Builder
public class BuilderExample {
 private String name;
 private int age;
 @Singular private Set<String> occupations;
}
//等效代码
import java.util.Set;
class BuilderExample {
  private String name;
  private int age;
  private Set<String> occupations;

  BuilderExample(String name, int age, Set<String> occupations) {
      this.name = name;
      this.age = age;
      this.occupations = occupations;
  }

  public static BuilderExampleBuilder builder() {
      return new BuilderExampleBuilder();
  }

  public static class BuilderExampleBuilder {
      private String name;
      private int age;
      private java.util.ArrayList<String> occupations;

      BuilderExampleBuilder() {
      }

      public BuilderExampleBuilder name(String name) {
          this.name = name;
          return this;
      }

      public BuilderExampleBuilder age(int age) {
          this.age = age;
          return this;
      }

      public BuilderExampleBuilder occupation(String occupation) {
          if (this.occupations == null) {
              this.occupations = new java.util.ArrayList<String>();
          }

          this.occupations.add(occupation);
          return this;
      }

      public BuilderExampleBuilder occupations(Collection<? extends String> occupations) {
          if (this.occupations == null) {
              this.occupations = new java.util.ArrayList<String>();
          }

          this.occupations.addAll(occupations);
          return this;
      }

      public BuilderExampleBuilder clearOccupations() {
          if (this.occupations != null) {
              this.occupations.clear();
          }

          return this;
      }

      public BuilderExample build() {
          // complicated switch statement to produce a compact properly sized immutable set omitted.
          // go to https://projectlombok.org/features/Singular-snippet.html to see it.
          Set<String> occupations = ...;
          return new BuilderExample(name, age, occupations);
      }

      @java.lang.Override
      public String toString() {
          return "BuilderExample.BuilderExampleBuilder(name = " + this.name + ", age = " + this.age + ", occupations = " + this.occupations + ")";
      }
  }
}
```

### 2.11. @Delegate

这个注解也是相当的牛逼，看下面的截图，它会该类生成一些列的方法，这些方法都来自与List接口

```java
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name= Constants.TABLE_SCHOOL_DOWNLOAD_LIMIT)
@RequiredArgsConstructor(staticName = "of")
@Accessors(chain = true)
@ToString
public class SchoolDownloadLimit implements Serializable {
    private static final long serialVersionUID = -196412797757026250L;

    @Getter(onMethod = @_({@Id,@Column(name="id",nullable=false),@GeneratedValue(strategy= GenerationType.AUTO)}))
    @Setter
    private Integer id;

    @Getter(onMethod = @_(@Column(name="school_id")))
    @Setter
    private Integer schoolId;

    @Getter(onMethod = @_(@Column(name = "per_download_times")))
    @Setter
    private Integer perDownloadTimes;

    @Getter(onMethod = @_(@Column(name = "limit_time")))
    @Setter
    private Integer limitTime;

    @Getter(onMethod = @_(@Column(name = "download_to_limit_an_hour")))
    @Setter
    private Integer downloadToLimitInHour;

    @Getter(onMethod = @_(@Column(name = "available")))
    @Setter
    private Integer available = 1;

    @Getter(onMethod = @_(@Column(name = "create_time")))
    @Setter
    private Date createTime;

    @Getter(onMethod = @_(@Column(name = "update_time")))
    @Setter
    private Date updateTime;
}
```

## 参考链接

```html
https://www.jianshu.com/p/365ea41b3573
https://blog.csdn.net/wanghuan1990519wha/article/details/103552076
https://projectlombok.org/
```

