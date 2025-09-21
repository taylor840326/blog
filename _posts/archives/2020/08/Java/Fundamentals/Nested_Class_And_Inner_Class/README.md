## 静态内部类和非静态内部类的区别
-----

### 1. static静态修饰符

在程序中任何变量或者代码都是在编译时由系统自动分配内存和存储的。

static修饰符表示静态的，**在类加载时JVM会把它放在方法区**，被本类以及本类中所有实例所公用。

在编译后所分配的内存会一直存在，直到程序退出内存才会释放这个空间。

如果一个被所有实例公用的方法申明为static，不用每个实例初始化的时候重新分配单独的内存空间，这样就可以大大节省内存空间。

### 2. 内部类

定义在一个类内部的类叫内部类，包含内部类的类成为外部类。

内部类可以声明public、protected、private等访问限制符，也可以申明为abstract供其他内部类或者外部类继承和扩展。

还可以使用static、final修饰满足不同的应用场景。

外部类按照常规的类访问方式使用内部类，唯一的差别时外部类可以访问内部类的所有方法和属性。包括私有的方法和属性。

### 3.静态内部类

只有内部类才能被声明为静态类，也就是静态内部类。

定义静态内部类有如下约定：

1. 只能在把内部类定义成静态类。
1. 静态内部类与外层类绑定，即使没有创建外层类的对象，它一样存在。
1. 静态内部类的方法可以是静态的方法，也可以是非静态的方法。静态方法可以在外层通过静态类调用，非静态方法必须要创建类的对象后才能调用。
1. 内部类只能引用外部类的static的成员变量。
1. 如果一个内部类不是被定义成静态内部类，那么这个内部类的成员变量或者成员方法是不能被定义成静态的。

```java
public class OutClassTest {
    int out1=1;
    static int out2=1;
    void out(){
        System.out.println("非静态");
    }
    static void outstatic(){
        System.out.println("静态");
    }
    public class InnerClass{
        void InnerClass(){
            System.out.println("InnerClass!");
            System.out.println(out1);
            System.out.println(out2);
            out();
            outstatic();//静态内部类只能够访问外部类的静态成员
        }

      // static void inner(){}  static int i=1; 非静态内部类不能有静态成员（方法、属性）
    }
    public static class InnerstaticClass{
        void InnerstaticClass(){
            System.out.println("InnerstaticClass");
          //  System.out.println(out1);out(); 静态内部类只能够访问外部类的静态成员
            System.out.println(out2);
            outstatic();
        }
        static void innerstatic(){}  static int i=1;//静态内部类能有静态成员（方法、属性）
    }
    public static void main(String args[]){
       OutClassTest a=new OutClassTest();
        OutClassTest.InnerstaticClass b=new OutClassTest.InnerstaticClass();//创建静态内部类
        OutClassTest.InnerClass c=a.new InnerClass();//创建非静态内部类
    }
}
```

### 4.总结

1. 是否能拥有静态成员

静态内部类可以有静态成员（包括：方法、属性），而非静态内部类则不能有静态成员（包括：方法、属性）。

2. 访问外部类的成员

静态内部类只能访问外部类的静态成员，而非静态内部类可以访问外部类的所有成员（包括： 方法、属性）。

3. 静态内部类和非静态内部类在创建时有区别

假设类A有静态内部类B和非静态内部类C，则创建B和C的区别为：

```java
public class A {
    public A() {
    }
    private String aname;

    public void setAname(String aname) {
        this.aname = aname;
    }

    /*静态内部类B*/
    public static class  B {
        public B() {
        }

        public void setBname(String bname) {
            this.bname = bname;
        }

        private String bname;
    }

    /*非静态内部类C*/
    public class C{
        public C() {
        }

        public void setCname(String cname) {
            this.cname = cname;
        }

        private String cname;
    }
}
```

初始化的时候
```java
A a = new A();
A.B b = new A.B();
A.C c = a.new C();
```
