google Guava包的reflection解析
译者：万天慧(武祖)

由于类型擦除，你不能够在运行时传递泛型类对象——你可能想强制转换它们，并假装这些对象是有泛型的，但实际上它们没有。

举个例子：

ArrayList<String> stringList = Lists.newArrayList();
ArrayList<Integer> intList = Lists.newArrayList();
System.out.println(stringList.getClass().isAssignableFrom(intList.getClass()));
returns true, even though ArrayList<String> is not assignable from ArrayList<Integer>
Guava提供了TypeToken, 它使用了基于反射的技巧甚至让你在运行时都能够巧妙的操作和查询泛型类型。想象一下TypeToken是创建，操作，查询泛型类型（以及，隐含的类）对象的方法。

Guice用户特别注意：TypeToken与类Guice的TypeLiteral很相似，但是有一个点特别不同：它能够支持非具体化的类型，例如T，List<T>，甚至是List<? extends Number>；TypeLiteral则不能支持。TypeToken也能支持序列化并且提供了很多额外的工具方法。

背景：类型擦除与反射
Java不能在运行时保留对象的泛型类型信息。如果你在运行时有一个ArrayList<String>对象，你不能够判定这个对象是有泛型类型ArrayList<String>的 —— 并且通过不安全的原始类型，你可以将这个对象强制转换成ArrayList<Object>。

但是，反射允许你去检测方法和类的泛型类型。如果你实现了一个返回List的方法，并且你用反射获得了这个方法的返回类型，你会获得代表List<String>的ParameterizedType。

TypeToken类使用这种变通的方法以最小的语法开销去支持泛型类型的操作。

介绍
获取一个基本的、原始类的TypeToken非常简单：

TypeToken<String> stringTok = TypeToken.of(String.class);
TypeToken<Integer> intTok = TypeToken.of(Integer.class);
为获得一个含有泛型的类型的TypeToken —— 当你知道在编译时的泛型参数类型 —— 你使用一个空的匿名内部类：

TypeToken<List<String>> stringListTok = new TypeToken<List<String>>() {};
或者你想故意指向一个通配符类型：

TypeToken<Map<?, ?>> wildMapTok = new TypeToken<Map<?, ?>>() {};
TypeToken提供了一种方法来动态的解决泛型类型参数，如下所示：

static <K, V> TypeToken<Map<K, V>> mapToken(TypeToken<K> keyToken, TypeToken<V> valueToken) {
    return new TypeToken<Map<K, V>>() {}
        .where(new TypeParameter<K>() {}, keyToken)
        .where(new TypeParameter<V>() {}, valueToken);
}
...
TypeToken<Map<String, BigInteger>> mapToken = mapToken(
    TypeToken.of(String.class),
    TypeToken.of(BigInteger.class)
);
TypeToken<Map<Integer, Queue<String>>> complexToken = mapToken(
   TypeToken.of(Integer.class),
   new TypeToken<Queue<String>>() {}
);
注意如果mapToken只是返回了new TypeToken>()，它实际上不能把具体化的类型分配到K和V上面，举个例子

class Util {
    static <K, V> TypeToken<Map<K, V>> incorrectMapToken() {
        return new TypeToken<Map<K, V>>() {};
    }
}
System.out.println(Util.<String, BigInteger>incorrectMapToken());
// just prints out "java.util.Map<K, V>"
或者，你可以通过一个子类（通常是匿名）来捕获一个泛型类型并且这个子类也可以用来替换知道参数类型的上下文类。

abstract class IKnowMyType<T> {
    TypeToken<T> type = new TypeToken<T>(getClass()) {};
}
...
new IKnowMyType<String>() {}.type; // returns a correct TypeToken<String>
使用这种技术，你可以，例如，获得知道他们的元素类型的类。

查询
TypeToken支持很多种类能支持的查询，但是也会把通用的查询约束考虑在内。

支持的查询操作包括：

方法	描述
getType()	获得包装的java.lang.reflect.Type.
getRawType()	返回大家熟知的运行时类
getSubtype(Class<?>)	返回那些有特定原始类的子类型。举个例子，如果这有一个Iterable并且参数是List.class，那么返回将是List。
getSupertype(Class<?>)	产生这个类型的超类，这个超类是指定的原始类型。举个例子，如果这是一个Set并且参数是Iterable.class，结果将会是Iterable。
isAssignableFrom(type)	如果这个类型是 assignable from 指定的类型，并且考虑泛型参数，返回true。List<? extends Number>是assignable from List，但List没有.
getTypes()	返回一个Set，包含了这个所有接口，子类和类是这个类型的类。返回的Set同样提供了classes()和interfaces()方法允许你只浏览超类和接口类。
isArray()	检查某个类型是不是数组，甚至是<? extends A[]>。
getComponentType()	返回组件类型数组。
resolveType
resolveType是一个可以用来“替代”context token（译者：不知道怎么翻译，只好去stackoverflow去问了）中的类型参数的一个强大而复杂的查询操作。例如，

TypeToken<Function<Integer, String>> funToken = new TypeToken<Function<Integer, String>>() {};

TypeToken<?> funResultToken = funToken.resolveType(Function.class.getTypeParameters()[1]));
// returns a TypeToken<String>
TypeToken将Java提供的TypeVariables和context token中的类型变量统一起来。这可以被用来一般性地推断出在一个类型相关方法的返回类型：

TypeToken<Map<String, Integer>> mapToken = new TypeToken<Map<String, Integer>>() {};
TypeToken<?> entrySetToken = mapToken.resolveType(Map.class.getMethod("entrySet").getGenericReturnType());
// returns a TypeToken<Set<Map.Entry<String, Integer>>>
Invokable
Guava的Invokable是对java.lang.reflect.Method和java.lang.reflect.Constructor的流式包装。它简化了常见的反射代码的使用。一些使用例子：

方法是否是public的?
JDK:

Modifier.isPublic(method.getModifiers())
Invokable:

invokable.isPublic()
方法是否是package private?
JDK:

!(Modifier.isPrivate(method.getModifiers()) || Modifier.isPublic(method.getModifiers()))
Invokable:

invokable.isPackagePrivate()
方法是否能够被子类重写？
JDK:

!(Modifier.isFinal(method.getModifiers())
|| Modifiers.isPrivate(method.getModifiers())
|| Modifiers.isStatic(method.getModifiers())
|| Modifiers.isFinal(method.getDeclaringClass().getModifiers()))
Invokable:

invokable.isOverridable()
方法的第一个参数是否被定义了注解@Nullable？
JDK:

for (Annotation annotation : method.getParameterAnnotations[0]) {
    if (annotation instanceof Nullable) {
        return true;
    }
}
return false;
Invokable:

invokable.getParameters().get(0).isAnnotationPresent(Nullable.class)
构造函数和工厂方法如何共享同样的代码？
你是否很想重复自己，因为你的反射代码需要以相同的方式工作在构造函数和工厂方法中？

Invokable提供了一个抽象的概念。下面的代码适合任何一种方法或构造函数：

invokable.isPublic();
invokable.getParameters();
invokable.invoke(object, args);
List的List.get(int)返回类型是什么？
Invokable提供了与众不同的类型解决方案：

Invokable<List<String>, ?> invokable = new TypeToken<List<String>>()        {}.method(getMethod);
invokable.getReturnType(); // String.class
Dynamic Proxies
newProxy()
实用方法Reflection.newProxy(Class, InvocationHandler)是一种更安全，更方便的API，它只有一个单一的接口类型需要被代理来创建Java动态代理时

JDK:

Foo foo = (Foo) Proxy.newProxyInstance(
Foo.class.getClassLoader(),
new Class<?>[] {Foo.class},
invocationHandler);
Guava:

Foo foo = Reflection.newProxy(Foo.class, invocationHandler);
AbstractInvocationHandler
有时候你可能想动态代理能够更直观的支持equals()，hashCode()和toString()，那就是：

一个代理实例equal另外一个代理实例，只要他们有同样的接口类型和equal的invocation handlers。
一个代理实例的toString()会被代理到invocation handler的toString()，这样更容易自定义。
AbstractInvocationHandler实现了以上逻辑。

除此之外，AbstractInvocationHandler确保传递给handleInvocation(Object, Method, Object[]))的参数数组永远不会空，从而减少了空指针异常的机会。

ClassPath
严格来讲，Java没有平台无关的方式来浏览类和类资源。不过一定的包或者工程下，还是能够实现的，比方说，去检查某个特定的工程的惯例或者某种一直遵从的约束。

ClassPath是一种实用工具，它提供尽最大努力的类路径扫描。用法很简单：

ClassPath classpath = ClassPath.from(classloader); // scans the class path used by classloader
for (ClassPath.ClassInfo classInfo : classpath.getTopLevelClasses("com.mycomp.mypackage")) {
  ...
}
在上面的例子中，ClassInfo是被加载类的句柄。它允许程序员去检查类的名字和包的名字，让类直到需要的时候才被加载。

值得注意的是，ClassPath是一个尽力而为的工具。它只扫描jar文件中或者某个文件目录下的class文件。也不能扫描非URLClassLoader的自定义class loader管理的class，所以不要将它用于关键任务生产任务。

Class Loading
工具方法Reflection.initialize(Class…)能够确保特定的类被初始化——执行任何静态初始化。

使用这种方法的是一个代码异味，因为静态伤害系统的可维护性和可测试性。在有些情况下，你别无选择，而与传统的框架，操作间，这一方法有助于保持代码不那么丑。

原创文章，转载请注明： 转载自并发编程网 – ifeve.com本文链接地址: google Guava包的reflection解析