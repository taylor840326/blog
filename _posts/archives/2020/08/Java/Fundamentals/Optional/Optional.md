## Optional
-----

新版本的Java 8引入了一个新的Optional类。Optional类的Javadoc描述如下：

### 2. API介绍

### 2.1. of

为非null的值创建一个Optional。

of方法通过工厂方法创建Optional类。需要注意的是，创建对象时传入的参数不能为null。如果传入参数为null，则抛出NullPointerException 。

```java
//调用工厂方法创建Optional实例
Optional<String> name = Optional.of("Sanaulla");
//传入参数为null，抛出NullPointerException.
Optional<String> someNull = Optional.of(null);
```

### 2.2. ofNullable

为指定的值创建一个Optional，如果指定的值为null，则返回一个空的Optional。

ofNullable与of方法相似，唯一的区别是可以接受参数为null的情况。示例如下：

```java
//下面创建了一个不包含任何值的Optional实例
//例如，值为'null'
Optional empty = Optional.ofNullable(null);
```

### 2.3. empty

一个空的Optional。

```java
Optional<String> empty = Optional.empty();
```

### 2.4. isPresent

如果值存在返回true，否则返回false。

```java
//isPresent方法用来检查Optional实例中是否包含值
if (name.isPresent()) {
  //在Optional实例内调用get()返回已存在的值
  System.out.println(name.get());//输出Sanaulla
}
```

### 2.5. get

如果Optional有值则将其返回，否则抛出NoSuchElementException。

```java
//执行下面的代码会输出：No value present 
try {
  //在空的Optional实例上调用get()，抛出NoSuchElementException
  System.out.println(empty.get());
} catch (NoSuchElementException ex) {
  System.out.println(ex.getMessage());
}
```

### 2.6. ifPresent

如果Optional实例有值则为其调用consumer，否则不做处理

```java
//ifPresent方法接受lambda表达式作为参数。
//lambda表达式对Optional的值调用consumer进行处理。
        optional.ifPresent(s -> {
            s = s + "S";
            log.debug(s);
        });
        log.debug(optional.get());
```

通过ifPresent修改的值，再次通过get获取的时候不会改变

### 2.7. orElse

如果有值则将其返回，否则返回指定的其它值。

如果Optional实例有值则将其返回，否则返回orElse方法传入的参数。示例如下：

```java
//如果值不为null，orElse方法返回Optional实例的值。
//如果为null，返回传入的消息。
//输出：There is no value present!
System.out.println(empty.orElse("There is no value present!"));
//输出：Sanaulla
System.out.println(name.orElse("There is some value!"));
```

### 2.8. orElseGet

orElseGet与orElse方法类似，区别在于得到的默认值。orElse方法将传入的字符串作为默认值，orElseGet方法可以接受Supplier接口的实现用来生成默认值。示例如下：

```java
//orElseGet与orElse方法类似，区别在于orElse传入的是默认值，
//orElseGet可以接受一个lambda表达式生成默认值。
//输出：Default Value
System.out.println(empty.orElseGet(() -> "Default Value"));
//输出：Sanaulla
System.out.println(name.orElseGet(() -> "Default Value"));
```

### 2.9. orElseThrow

如果有值则将其返回，否则抛出supplier接口创建的异常。

在orElseGet方法中，我们传入一个Supplier接口。然而，在orElseThrow中我们可以传入一个lambda表达式或方法，如果值不存在来抛出异常。

```java
try {
  //orElseThrow与orElse方法类似。与返回默认值不同，
  //orElseThrow会抛出lambda表达式或方法生成的异常 

  empty.orElseThrow(ValueAbsentException::new);
} catch (Throwable ex) {
  //输出: No value present in the Optional instance
  System.out.println(ex.getMessage());
}
```

### 2.10. map

如果有值，则对其执行调用mapping函数得到返回值。如果返回值不为null，则创建包含mapping返回值的Optional作为map方法返回值，否则返回空Optional。

//map方法执行传入的lambda表达式参数对Optional实例的值进行修改。
//为lambda表达式的返回值创建新的Optional实例作为map方法的返回值。
Optional<String> upperName = name.map((value) -> value.toUpperCase());
System.out.println(upperName.orElse("No value found"));
```
  
### 2.11. flatMap

如果有值，为其执行mapping函数返回Optional类型返回值，否则返回空Optional。flatMap与map（Funtion）方法类似，区别在于flatMap中的mapper返回值必须是Optional。调用结束时，flatMap不会对结果用Optional封装。

```java
//flatMap与map（Function）非常类似，区别在于传入方法的lambda表达式的返回类型。
//map方法中的lambda表达式返回值可以是任意类型，在map函数返回之前会包装为Optional。 
//但flatMap方法中的lambda表达式返回值必须是Optionl实例。 
upperName = name.flatMap((value) -> Optional.of(value.toUpperCase()));
System.out.println(upperName.orElse("No value found"));//输出SANAULLA
```

### 2.12. filter

如果有值并且满足断言条件返回包含该值的Optional，否则返回空Optional。

读到这里，可能你已经知道如何为filter方法传入一段代码。是的，这里可以传入一个lambda表达式。对于filter函数我们应该传入实现了Predicate接口的lambda表达式。

```java
//filter方法检查给定的Option值是否满足某些条件。
//如果满足则返回同一个Option实例，否则返回空Optional。
Optional<String> longName = name.filter((value) -> value.length() > 6);
System.out.println(longName.orElse("The name is less than 6 characters"));//输出Sanaulla

//另一个例子是Optional值不满足filter指定的条件。
Optional<String> anotherName = Optional.of("Sana");
Optional<String> shortName = anotherName.filter((value) -> value.length() > 6);
//输出：name长度不足6字符
System.out.println(shortName.orElse("The name is less than 6 characters"));
```

## 3. 使用 Java8 Optional 的正确姿势
Java 8 增加了一些很有用的 API, 其中一个就是 Optional. 如果对它不稍假探索, 只是轻描淡写的认为它可以优雅的解决 NullPointException 的问题, 于是代码就开始这么写了

```java
Optional user = ……
if (user.isPresent()) {
return user.getOrders();
} else {
return Collections.emptyList();
}
```

那么不得不说我们的思维仍然是在原地踏步, 只是本能的认为它不过是 User 实例的包装, 这与我们之前写成

```java
User user = .....
if (user != null) {
    return user.getOrders();
} else {
    return Collections.emptyList();
}
```

实质上是没有任何分别. 这就是我们将要讲到的使用好 Java 8 Optional 类型的正确姿势.
直白的讲, 当我们还在以如下几种方式使用 Optional 时, 就得开始检视自己了
- 调用 isPresent() 方法时
- 调用 get() 方法时
- Optional 类型作为类/实例属性时
- Optional 类型作为方法参数时

isPresent() 与 obj != null 无任何分别, 我们的生活依然在步步惊心. 而没有 isPresent() 作铺垫的 get() 调用在 IntelliJ IDEA 中会收到告警

所以 Optional 中我们真正可依赖的应该是除了 isPresent() 和 get() 的其他方法:
- public<U> Optional<U> map(Function<? super T, ? extends U> mapper)
- public T orElse(T other)
- public T orElseGet(Supplier<? extends T> other)
- public void ifPresent(Consumer<? super T> consumer)
- public Optional<T> filter(Predicate<? super T> predicate)
- public<U> Optional<U> flatMap(Function<? super T, Optional<U>> mapper)
- public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X

先又不得不提一下 Optional 的三种构造方式: Optional.of(obj), Optional.ofNullable(obj) 和明确的 Optional.empty()

Optional.of(obj): 它要求传入的 obj 不能是 null 值的, 否则还没开始进入角色就倒在了 NullPointerException 异常上了.

Optional.ofNullable(obj): 它以一种智能的, 宽容的方式来构造一个 Optional 实例. 来者不拒, 传 null 进到就得到 Optional.empty(), 非 null 就调用 Optional.of(obj).

那是不是我们只要用 Optional.ofNullable(obj) 一劳永逸, 以不变应二变的方式来构造 Optional 实例就行了呢? 那也未必, 否则 Optional.of(obj) 何必如此暴露呢, 私有则可?

本人的观点是:
1. 当我们非常非常的明确将要传给 Optional.of(obj) 的 obj 参数不可能为 null 时, 比如它是一个刚 new 出来的对象(Optional.of(new User(…))), 或者是一个非 null 常量时;
2. 当想为 obj 断言不为 null 时, 即我们想在万一 obj 为 null 立即报告 NullPointException 异常, 立即修改, 而不是隐藏空指针异常时, 我们就应该果断的用 Optional.of(obj) 来构造 Optional 实例, 而不让任何不可预计的 null 值有可乘之机隐身于 Optional 中.

存在即返回, 无则提供默认值
```java
return user.orElse(null);  //而不是 return user.isPresent() ? user.get() : null;
return user.orElse(UNKNOWN_USER); 
```

存在即返回, 无则由函数来产生
```java
return user.orElseGet(() -> fetchAUserFromDatabase()); //而不要 return user.isPresent() ? user: fetchAUserFromDatabase();
```

存在才对它做点什么

```java
user.ifPresent(System.out::println);
```

//而不要下边那样
```java
if (user.isPresent()) {
  System.out.println(user.get());
}
```

map 函数隆重登场

当 user.isPresent() 为真, 获得它关联的 orders, 为假则返回一个空集合时, 我们用上面的 orElse, orElseGet 方法都乏力时, 那原本就是 map 函数的责任, 我们可以这样一行

```java
return user.map(u -> u.getOrders()).orElse(Collections.emptyList())
```

//上面避免了我们类似 Java 8 之前的做法
```java
if(user.isPresent()) {
  return user.get().getOrders();
} else {
  return Collections.emptyList();
}
```

map 是可能无限级联的, 比如再深一层, 获得用户名的大写形式

```java
return user.map(u -> u.getUsername())
           .map(name -> name.toUpperCase())
           .orElse(null);
```

这要搁在以前, 每一级调用的展开都需要放一个 null 值的判断

```java
User user = .....
if(user != null) {
  String name = user.getUsername();
  if(name != null) {
    return name.toUpperCase();
  } else {
    return null;
  }
} else {
  return null;
}
```

小结
使用 Optional 时尽量不直接调用 Optional.get() 方法, Optional.isPresent() 更应该被视为一个私有方法, 应依赖于其他像 Optional.orElse(), Optional.orElseGet(), Optional.map() 等这样的方法.


### 1. 前言

Optional 是 Java8 提供的为了解决 null 安全问题的一个 API。

善用 Optional 可以使我们代码中很多繁琐、丑陋的设计变得十分优雅。

这篇文章是建立在你对 Optional 的用法有一定了解的基础上的，如果你还不太了解 Optional，可以先去看看相关教程，或者查阅 Java 文档。

使用 Optional，我们就可以把下面这样的代码进行改写。
```java
public static String getName(User u) {
    if (u == null || u.name == null)
        return "Unknown";
    return u.name;
}
```

不过，千万不要改写成这副样子。

```java
public static String getName(User u) {
    Optional<User> user = Optional.ofNullable(u);
    if (!user.isPresent())
        return "Unknown";
    return user.get().name;
}
```

这样改写非但不简洁，而且其操作还是和第一段代码一样。无非就是用 isPresent 方法来替代 u==null。这样的改写并不是 Optional 正确的用法，我们再来改写一次。

```java
public static String getName(User u) {
    return Optional.ofNullable(u)
                    .map(user->user.name)
                    .orElse("Unknown");
}
```

这样才是正确使用 Optional 的姿势。那么按照这种思路，我们可以安心的进行链式调用，而不是一层层判断了。看一段代码：

```java
public static String getChampionName(Competition comp) throws IllegalArgumentException {
    if (comp != null) {
        CompResult result = comp.getResult();
        if (result != null) {
            User champion = result.getChampion();
            if (champion != null) {
                return champion.getName();
            }
        }
    }
    throw new IllegalArgumentException("The value of param comp isn't available.");
}
```

由于种种原因（比如：比赛还没有产生冠军、方法的非正常调用、某个方法的实现里埋藏的大礼包等等），我们并不能开心的一路 comp.getResult ().getChampion ().getName () 到底。而其他语言比如 kotlin，就提供了在语法层面的操作符加持：comp?.getResult ()?.getChampion ()?.getName ()。所以讲道理在 Java 里我们怎么办！

Java 用户听了都想打人

让我们看看经过 Optional 加持过后，这些代码会变成什么样子。

```
public static String getChampionName(Competition comp) throws IllegalArgumentException {
    return Optional.ofNullable(comp)
            .map(c->c.getResult())
            .map(r->r.getChampion())
            .map(u->u.getName())
            .orElseThrow(()->new IllegalArgumentException("The value of param comp isn't available."));
}
```

这就很舒服了。Optional 给了我们一个真正优雅的 Java 风格的方法来解决 null 安全问题。虽然没有直接提供一个操作符写起来短，但是代码看起来依然很爽很舒服。更何况？. 这样的语法好不好看还见仁见智呢。
还有很多不错的使用姿势，比如为空则不打印可以这么写：
string.ifPresent(System.out::println);

Optional 的魅力还不止于此，Optional 还有一些神奇的用法，比如 Optional 可以用来检验参数的合法性。

```
public void setName(String name) throws IllegalArgumentException {
    this.name = Optional.ofNullable(name).filter(User::isNameValid)
                        .orElseThrow(()->new IllegalArgumentException("Invalid username."));
}
```

这样写参数合法性检测，应该足够优雅了吧。
2019-10-13 补充 Optional 的本质，提出若干应用建议。

不过这还没完，上面的两个例子其实还不能完全反应出 Optional 的设计意图。事实上，我们应该更进一步，减少 Optional.ofNullable 的使用。为什么呢？因为 Optional 是被设计成用来代替 null 以表示不确定性的，换句话说，只要一段代码可能产生 null，那它就可以返回 Optional。而我们选择用 Optional 代替 null 的原因，是 Optional 提供了一个把若干依赖前一步结果的处理结合在一起的途径。举个例子，在我们调用一个网站的登录接口的时候，大概会有以下的步骤：

发送 HTTP 请求，得到返回。
（依赖：接口的返回）解析返回，如将 Json 文本形式的返回结果转化为对象形式。
（依赖：解析的结果）判断结果是否成功。
（依赖：若成功调用的结果）取得鉴权令牌。
（依赖：获得的令牌）进行处理。
其中，第 2-5 步的每一个步骤都依赖于前一个步骤，而前一个步骤传递过来的数据都具不确定性（有可能是 null）。所以，我们可以把它们接受的数据都设计成 Optional。第 1-4 步每一个步骤的结果也具备不确定性，所以我们也把它们的结果设计成 Optional。最后到了第 5 步，我们终于要对一切的结果进行处理了：如果成功获得令牌就存储，失败就提示用户。所以这一步，我们采用如 orElse 之类的方法来消除不确定性。于是我们最后的设计就可以是：

结果 String（可能是 null） –包装–>  Optional<String>
Optional<String>  –解析–>  Optional<Json 对象 >
Optional<Json 对象>  –Filter 判断成功–>  Optional<Json 对象 >
Optional<Json 对象>  –取鉴权令牌–>  Optional<AuthToken>
对 Optional<AuthToken> 进行处理，消除 Optional
Optional 就像一个处理不确定性的管道，我们在一头丢进一个可能是 null 的东西（接口返回结果），经过层层处理，最后消除不确定性。Optional 在过程中保留了不确定性，从而把对 null 的处理移到了若干次操作的最后，以减少出现 NPE 错误的可能。于是，Optional 应用的建议也呼之欲出了：

适用于层级处理（依赖上一步操作）的场合。
产生对象的方法若可能返回 null，可以用 Optional 包装。
尽可能延后处理 null 的时机，在过程中使用 Optional 保留不确定性。
尽量避免使用 Optional 作为字段类型。
最后说句题外话，这种依赖上一步的操作也叫 Continuation。而 Optional 的这种接受并组合多个 Continuation 的设计风格就是 Continuation-passing style（CPS）。

### 参考资料

使用 Java8 Optional 的正确姿势 – 隔叶黄莺 Unmi Blog (https://unmi.cc/proper-ways-of-using-java8-optional/)

```html
https://blog.csdn.net/u011669700/article/details/79464437

https://www.oracle.com/technical-resources/articles/java/java8-optional.html

https://blog.csdn.net/L_Sail/article/details/78868673

https://blog.kaaass.net/archives/764
```
