Java SE 1.5引入了注解，程序员通过注解可以为程序编写元数据（metadata）。根据 Oracle 官方文档，注解的定义如下：“注解是元数据的一种形式，提供与程序相关的数据，该数据不是程序本身的一部分”。 

可以在代码中的任何位置使用注解，比如类、方法和变量上使用。从 Java 8开始，注解也可以用于类型声明。

注解代码与程序本身没有任何直接关系，只有其他程序或 JVM 利用注解信息实现特定功能。

注解语法

注解由字符 @ 和注解名组成，即 @AnnotationName。当编译器发现这个语法该元素时，会把它解析为注解。例如：

@ExampleAnnotation
public class SampleClass {
}
上面的注解称为 ExampleAnnotation，标记在 SampleClass 上。

注解可以包含属性，在声明注解时以键值对的形式给出。例如： 

@ExampleAnnotation(name = ”first name”, age = 35)
public void simpleMethod() {
}
请注意，这里 ExampleAnnotation 是一个方法注解。如果注解只包含一个属性，在声明注解时可以忽略属性名。示例如下：

@ExampleAnnotation(“I am the only property”)
public void simpleMethod() {
}
一个元素可以使用多个注解。比如下面这个示例：

@Annotation1
@Annotation2(“Another Annotation”)
public class SimpleClass {
}
从 J2SE 8 开始，可以为同一个元素多次使用相同的注解，例如：

@ExampleAnnotation(“Annotation used”)
@ExampleAnnotation(“Annotation repeated”)
public class SimpleClass {
}
在 @Repeatable 部分会对此进行详细地讨论。

Java 预定义注解

Java 支持一组预先定义好的注解。下面介绍了Java Core 中提供的注解：

@Retention: 该注解用来修饰其他注解，并标明被修饰注解的作用域。其 value 的属性值包含3种：

SOURCE：注解仅在源代码中可用。编译器和 JVM 会忽略此注解，因此在运行时不可用；

CLASS：编译器会处理该注解，但 JVM 不会处理，因此在运行时不可用；

RUNTIME：JVM 会处理该注解，可以在运行时使用。

@Target: 该注解标记可以应用的目标元素：

ANNOTATION_TYPE：可修饰其他注解；

CONSTRUCTOR：可以修饰构造函数；

FIELD：可以修饰字段或属性；

LOCAL_VARIABLE：可以修饰局部变量；

METHOD：可以修饰 method；

PACKAGE：可以修饰 package 声明；

PARAMETER：可以修饰方法参数；

TYPE：可以修饰 Class、Interface、Annotation 或 enum 声明；

PACKAGE：可以修饰 package 声明；

TYPE_PARAMETER：可以修饰参数声明；

TYPE_USE：可以修饰任何类型。

@Documented: 该注解可以修饰其他注解，表示将使用 Javadoc 记录被注解的元素。

@Inherited: 默认情况下，注解不会被子类继承。但是，如果把注解标记为 @Inherited，那么使用注解修饰 class 时，子类也会继承该注解。该注解仅适用于 class。注意：使用该注解修饰接口时，实现类不会继承该注解。

@Deprecated: 标明不应该使用带此注解的元素。使用这个注解，编译器会对应生成告警。该注解可以应用于 method、class 和字段。

@SuppressWarnings: 告诉编译器由于特定原因不产生告警。

@Override: 该注解通知编译器，该元素正在覆盖（Override）父类中的元素。覆盖元素时，不强制要求加上该注解。但是当覆盖没有正确完成时，例如子类方法的参数与父类参数不同或者返回类型不匹配时，可以帮助编译器生成错误。

@SafeVarargs: 该注解断言（Assert）方法或构造函数代码不会对其参数执行不安全（Unsafe）操作。

@Repeatable 注解

该注解表示可以对同一个元素多次使用相同的注解。

下面这个例子有助于更清楚地理解。

首先，需要定义一个可重复修饰 class 的注解。

@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.TYPE_USE)
@Repeatable (RepeatableAnnotationContainer.class)
public @interface RepeatableAnnotation() {
    String values();
}
RepeatableAnnotation可以重复修饰元素。

接下来，定义 RepeatableAnnotationContainer注解类型。这是一个注解类型容器，包含一个 RepeatableAnnotation 类型的数组。

public @interface RepeatableAnnotationContainer {
    RepeatableAnnotation [] value();
}
现在，Repeatable 可以元素进行多次注释。

@RepeatableAnnotation (“I am annotating the class”)
@RepeatableAnnotation (“I am annotating the class again”)
@RepeatableAnnotation (“I am annotating the class for the third time”)
public class RepeatedAnnotationExample {function(){   //外汇跟单www.gendan5.com}
在程序中要获取注解中的值，先要获取容器中的数组，数组的每个元素将包含一个值。例如：

@RepeatableAnnotation (“I am annotating the class”)
@RepeatableAnnotation (“I am annotating the class again”)
@RepeatableAnnotation(“I am annotating the class for the third time”)
public class RepeatableAnnotationExample {
    public static void main(String [] args) {
        Class object = RepeatableAnnotationExample.class
        Annotation[] annotations = object.getAnnotations();
for (Annotation annotation : annotations) {
    RepeatableAnnotationContainer rac = (RepeatableAnnotationContainer) annotation;
    RepeatableAnnotation [] raArray = rac.value();
    for (RepeatableAnnotation ra : raArray) {
        System.out.println(ra.value);
    }
}
}
}
执行结果：

I am annotating the class
I am annotating the class again
I am annotating the class for the third time.
类型注解

Java 8发布后，注解可以用于任何类型（Type），这意味着只要可以使用类型的地方就能使用注解。例如，使用新运算符创建类实例、类型转换、用 implements 实现接口、抛出异常等，这种注解称为类型注解。 

这种注解能够帮助分析与改进 Java 程序，提供更强大的类型检查。Java 8发布前，Java 没有提供类型检查框架。但是通过类型注解可以开发类型检查框架，对 Java 程序进行检查。

举例来说，假设我们希望特定变量在程序执行过程中始终不为 null。可以编写一个自定义插件 NonNull，并为特定变量加上该注解进行检查。变量声明如下：

@NonNull String notNullString;
编译代码时，如果发现任何可能将变量赋值为 null 的代码，编译器会检查潜在问题并给出告警。

自定义注解

Java 允许程序员自定义注解。自行定义注解的语法：

public @interface CustomAnnotation { }
上面的代码会创建一个 CustomAnnotation新注解。@Interface 关键字可用于自定义注解。

自定义注解时，必须设置两个必填属性。可以在定义中增加其他属性，但这两个重要属性是必需的，即 Retention Policy 和 Target。

这两个属性（注解）被用来修饰自定义注解。此外，在自定义注解时可以定义属性。例如：

@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.ELEMENT)
public @interface CustomAnnotation {
    public String name() default “Mr Bean”;
    public String dateOfBirth();
}
上面的自定义注解中，Retention Policy 为 RUNTIME，这表明该注解可以在 JVM 运行时使用；Target 设为 ELEMENT，表示该注解可以修饰任何元素与类型。 

此外，它还具有两个属性：name 与 dateOfBirth。其中，name 属性默认值为 Mr Bean， dateOfBirth 没有默认值。

注意，声明的 Method 没有带任何参数以及 throws 语句。此外，返回类型仅限于 String、class、enum、注解以及上述类型的数组。

现在，可以像下面这样使用自定义注解：

@CustomAnnotation (dateOfBirth = “1980-06-25”)
public class CustomAnnotatedClass {
}
同样，可以使用 @Target(ElementType.METHOD) 创建自定义注解修饰 method。

获取注解及属性

Java Reflection API 提供了几种方法，可以在运行时中从 class、method 和其他元素中获取注解。 

AnnotatedElement接口定义了所有的方法，其中最重要的一个是：

getAnnotations(): 返回指定元素的所有注解，包括定义元素时未明确写出的注解。

isAnnotationPresent(annotation): 检查注解在当前元素上是否可用。

getAnnotation(class): 获取 class 参数使用的注解，如果参数不存在注解返回 null。

这个 class 支持 java.lang.Class、java.lang.reflect.Method 和 java.lang.reflect.Field，基本上可以适用任何的 Java 元素。

下面的示例程序展示了如何获取自定义注解的相关信息：

public static void main(String [] args) {
Class object = CustomAnnotatedClass.class;
// 从类中获取所有注解
Annotation[] annotations = object.getAnnotations();
for( Annotation annotation : annotations ) {
System.out.println(annotation);
}
// 检查是否存在注解
if( object.isAnnotationPresent( CustomAnnotationClass.class ) ) {
// 获取需要的注解
Annotation annotation = object.getAnnotation(CustomAnnotationClass.class) ;
System.out.println(annotation);
}
// 获取注解属性
for(Annotation annotation : annotations) {
System.out.println(“name: “ + annotation.name());
System.out.println(“Date of Birth: “+ annotation.dateOfBirth());
}
// 对所有方法执行相同的操作
for( Method method : object.getDeclaredMethods() ) {
if( method.isAnnotationPresent( CustomAnnotationMethod.class ) ) {
Annotation annotation = method.getAnnotation(CustomAnnotationMethod.class );
System.out.println( annotation );
}
}
}
总结

注解逐渐成为 J2EE 开发栈的重要组成部分，在开发任何企业级应用都需要用到。现如今，几乎所有流行的开发库出于不同目的都开始使用注解，比如代码质量分析、单元测试、XML 解析、依赖项注入等。大量使用了注解的开发库有 Hibernate、Spring MVC、Findbugs、JAXB 和 JUnit。 
