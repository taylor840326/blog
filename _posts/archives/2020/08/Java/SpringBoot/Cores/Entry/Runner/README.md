##  CommandLineRunner和ApplicationRunner的使用
-----

### 1. 简介

CommandLineRunner和ApplicationRunner是Spring Boot所提供的接口，他们都有一个run()方法。

所有实现他们的Bean都会在Spring Boot服务启动之后自动地被调用。

由于这个特性，它们是一个理想地方去做一些初始化的工作。CommandLineRunner和ApplicationRunner的作用是相同的。

这两个接口不同之处在于CommandLineRunner接口的run()方法接收String数组作为参数，而ApplicationRunner接口的run()方法接收ApplicationArguments对象作为参数。

当程序启动时，我们传给main()方法的参数可以被实现CommandLineRunner和ApplicationRunner接口的类的run()方法访问。

我们可以创建多个实现CommandLineRunner和ApplicationRunner接口的类。为了使他们按一定顺序执行，可以使用@Order注解或实现Ordered接口。

### 2.实现

### 2.1.单个实现

可以把单个实现放在SpringBoot的主入口类中，也可以配合Component注解单独放在一个类文件中。

### 2.1.1.实现放在SpringBoot的主入口类

当要实现一个很简单的命令行工具的时候可以把CommandLineRunner的实现方法直接放在SpringBoot主入口类中。

[示例](samples/sample01)

### 2.1.2.实现单独放在一个类中

为了代码整洁，我们需要把CommandLineRunner实现放在一个独立的类中。

[示例](samples/sample02)


### 2.2.多个实现

多个CommandLineRunner实现要考虑到执行的顺序，可以使用Order注解帮助实现。

[示例](samples/sample03)


### 参考链接

```html
https://www.jianshu.com/p/d01f6849a099

```
