java嵌套类与内部类
一、嵌套类(Nested Classes)
使用嵌套类减少了命名冲突，一个内部类可以定义在一个类中，一个方法中甚至一个表达式中。

(1)定义

A nested(嵌套) class is any class whose declaration occurs within the body of another class or interface.  A top level class is a class that is not a nested class. 

一般把定义内部类的外围类成为包装类（enclosing class)或者外部类.

(2)嵌套类分类

1、静态嵌套类 (static nested classes)

2、非静态嵌套类

非静态嵌套类就是内部类（Inner classes）。Inner Classes 不能定义为static，不能有static方法和static初始化语句块。

在JLS（java语言规范）里面是这么定义的：

An inner class is a nested class that is not explicitly or implicitly declared static. Inner classes may not declare static initializers (§8.7) or member inter- faces

非静态嵌套类分为三种：

1)成员嵌套类(member nested class)

  成员嵌套类 作为 enclosing class 的成员定义的，成员嵌套类有enclosing class属性

2)局部嵌套类(local nested class)

  局部嵌套类定义在 enclosing class 的方法里面，局部嵌套类有enclosing class 属性和enclosing method属性

3)匿名嵌套类(anonymous nested class)

  匿名嵌套类没有显示的定义一个类，直接通过new 的方法创建类的实例。一般回调模式情况下使用的比较多

 

也可以称为成员内部类(member inner classes)、局部内部类(local inner classes)、匿名内部类(anonymous inner classes)

 

二、静态嵌套类
简称静态类，最简单的嵌套类，只能访问外部类的静态成员变量与静态方法。

1、静态类不能访问外部类的非静态成员和非静态方法（不管是public还是private的）；
2、静态类的实例不需要先实例化外部类成员，可直接实例化。

3、静态类的修饰符与普通类的属性的修饰符一样，也可以使用访问控制符，可以使用final修饰，可以是abstract抽象类

 代码实例

public class Outer{
    public static class StaticNested{
       //其他代码
   }
}
 实例方式

Outer.StaticNested staticNested=new Outer.StaticNested();
 

三、成员内部类
成员内部类可以访问外部类的所有实例属性，静态属性。因为内部成员类持有一个外部对象的引用，内部类的实例可以对外部类的实例属性进行修改。

如果是public的 inner  member classes，可以通过 外部类实例.new 内部类()的方式进行创建，当调用内部类的构造器的时候，会把当前创建的内部类对象实例中持有的外部对象引用赋值为当前创建内部类的外部类实例。

内部成员类可以是使用访问控制符，可以定义为final，也可以是abstract抽象类。

 代码实例

class Outer{
    public  class MemberNested{
       //其他代码
   }
}
 

 实例方式

Outer.MemberNested staticNested=new Outer().new MemberNested();
 或者

Outer outer=new  Outer(); 
Outer.MemberNested staticNested=outer.new MemberNested();
 

四、局部内部类
(1)定义位置

   定义位置既可以是静态方法，也可以是实例方法，也可以是构造器方法，还可以是动态初始化语句块或者静态初始化语句块。

(2)修饰符限制

   局部类不能有访问控制符(private,public,protected修饰），可以是abstract的,也可以定义为final。

(3)局部类可以定义在一个static上下文里面 和 非static上下文里面。

   定义在static上下文（static初始化块，static方法）里面的local inner classes 可以访问类的静态属性。

   在static上下文定义的局部类，没有指向父类实例变量的引用，因为static方法不属于类的实例，属于类本身。而且局部类不能在外部进行创建，只能在定义位置调用的时候进行创建。

代码实例


复制代码
class Outer {
     //定义在构造方法
    public Outer() {
        final class LocalNested {
            //其他代码
        }
        LocalNested localNested=new LocalNested();
    }
    //定义在成员方法
    public void methodA() {
        abstract class LocalNested {
            //其他代码
        }
         LocalNested localNested=new LocalNested(){ };//匿名实现抽象类
    }
    //定义在静态方法
    public static void methodB() {
        abstract class LocalNested {
            //其他代码
        }
         LocalNested localNested=new LocalNested(){ };//匿名实现抽象类
    }
    //定义在动态初始化块
    {
        class LocalNested {
            //其他代码
        }
         LocalNested localNested=new LocalNested();
    }
    //定义在静态初始化块
    static {
        final class LocalNested {
            //其他代码
        }
         LocalNested localNested=new LocalNested();
    }
}
复制代码
 

五、匿名内部类
(1)定义位置

 定义位置既可以是静态方法，也可以是实例方法，也可以是构造器方法，还可以是动态初始化语句块或者静态初始化语句块。

(2)实现方式

匿名类和局部类访问规则一样，只不过内部类显式的定义了一个类，然后通过new的方式创建这个局部类实例，而匿名类直接new一个类实例，没有定义这个类。匿名类最常见的方式就是回调模式的使用，通过默认实现一个接口创建一个匿名类然后，然后new这个匿名类的实例。

代码实现


复制代码
class Outer {
     //定义在构造方法
    public Outer() {
       
        AnonymousNested anonymousNested=new AnonymousNested(){ };//匿名实现抽象类
    }
    //定义在成员方法
    public void methodA() {
       
         AnonymousNested anonymousNested=new AnonymousNested(){ };//匿名实现抽象类
    }
    //定义在静态方法
    public static void methodB() {
       
         AnonymousNested anonymousNested=new AnonymousNested(){ };//匿名实现抽象类
    }
    //定义在动态初始化块
    {
        
         AnonymousNested anonymousNested=new AnonymousNested(){ };//匿名实现抽象类
    }
    //定义在静态初始化块
    static {
       
         AnonymousNested anonymousNested=new AnonymousNested(){ };//匿名实现抽象类
    }
}
 abstract class AnonymousNested {
            //其他代码
}
复制代码

参考资料
 
```java
https://www.cnblogs.com/maokun/p/6789145.html
```