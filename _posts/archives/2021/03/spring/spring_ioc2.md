## spring_ioc2
------

上篇文章介绍了 Spring IOC 中最重要的两个概念——容器和Bean，以及如何使用 Spring 容器装配Bean。本文接着记录 Spring 中 IOC 的相关知识。

部分参考资料：
《Spring实战（第4版）》
《轻量级 JavaEE 企业应用实战（第四版）》
Spring 官方文档
W3CSchool Spring教程
易百教程 Spring教程

一、Spring 容器中的 Bean 的常用属性
Bean的作用域
目前，scope的取值有5种取值：
在Spring 2.0之前，有singleton和prototype两种；
在Spring 2.0之后，为支持web应用的ApplicationContext，增强另外三种：request，session和global session类型，它们只适用于web程序，通常是和XmlWebApplicationContext共同使用。

singleton： 单例模式，在整个 Spring IOC 容器中只会创建一个实例。默认即为单例模式。
prototype：原型模式，每次通过 getBean 方法获取实例时，都会创建一个新的实例。
request:在同一次Http请求内，只会生成一个实例，只在 Web 应用中使用 Spring 才有效。
session：在同义词 Http 会话内，只会生成一个实例，只在 Web 应用中使用 Spring 才有效。
global session：只有应用在基于porlet的web应用程序中才有意义，它映射到porlet的global范围的session，如果普通的servlet的web 应用中使用了这个scope，容器会把它作为普通的session的scope对待。
配置方式：
(1) XML 文件配置：

<bean id="helloSpring" class="com.sharpcj.hello.HelloSpring" scope="ConfigurableBeanFactory.SCOPE_SINGLETON"> <!-- singleton -->
    <property name="name" value="Spring"/>
</bean>
(2) 注解配置：

@Component
@Scope("singleton")
public class HelloSpring {

}
Bean 的延迟加载
默认情况下，当容器启动之后，会将所有作用域为单例的bean创建好，如配置 lazy-init值为true,表示延迟加载，即容器启动之后，不会立即创建该实例。
(1) XML文件配置：

<bean id="mb1" class="com.sharpcj.hello.HelloSpring" lazy-init="true"></bean>
(2) 注解配置:

@Component
@Lazy
@Scope("singleton")
public class HelloSpring {

}
Bean 初始化和销毁前后回调方法
Bean 初始化回调和销毁回调
HelloSpring.java

package com.sharpcj.cycle;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class HelloSpring implements InitializingBean, DisposableBean{
    public HelloSpring(){
        System.out.println("构造方法");
    }

    public void xmlInit(){
        System.out.println("xml Init");
    }

    public void xmlDestory(){
        System.out.println("xml Destory");
    }

    @PostConstruct
    public void init(){
        System.out.println("annotation Init");
    }

    @PreDestroy
    public void destory(){
        System.out.println("annotation Destory");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("interface afterPropertiesSet");
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("interface destroy");
    }
}

(1) XML文件配置：

<bean id="hello" class="com.sharpcj.cycle.HelloSpring" init-method="xmlInit" destroy-method="xmlDestory"/>
(2) 注解配置：

@PostConstruct
public void init(){
    System.out.println("annotation Init");
}

@PreDestroy
public void destory(){
    System.out.println("annotation Destory");
}
另外 Bean 可以实现 org.springframework.beans.factory.InitializingBean 和 org.springframework.beans.factory.DisposableBean 两个接口。
执行结果：

构造方法
interface afterPropertiesSet
xml Init
interface destroy
xml Destory
或者

构造方法
annotation Init
interface afterPropertiesSet
annotation Destory
interface destroy
二、工厂模式创建 Bean
创建 Bean 有三种方式：通过调用构造方法创建 Bean, 调用实例工厂方法创建Bean，调用静态工厂方法创建 Bean。

调用构造器创建 Bean
这是最常见的情况， 当我们通过配置文件，或者注解的方式配置 Bean， Spring 会通过调用 Bean 类的构造方法，来创建 Bean 的实例。通过 xml 文件配置，明确指定 Bean 的 class 属性，或者通过注解配置，Spring 容器知道 Bean 的完整类名，然后通过反射调用该类的构造方法即可。

调用实例工厂方法创建 Bean
直接上代码：
Ipet.java

package com.sharpcj.factorytest;

public interface IPet {
    void move();
}
Dog.java

package com.sharpcj.factorytest;

public class Dog implements IPet {
    @Override
    public void move() {
        System.out.println("Dog can run!");
    }
}
Parrot.java

package com.sharpcj.factorytest;

public class Parrot implements IPet {
    @Override
    public void move() {
        System.out.println("Parrot can fly!");
    }
}
工厂类， PetFactory.java

package com.sharpcj.factorytest;

public class PetFactory {
    public IPet getPet(String type){
        if ("dog".equals(type)) {
            return new Dog();
        } else if ("parrot".equals(type)){
            return new Parrot();
        } else {
            throw new IllegalArgumentException("pet type is illegal!");
        }
    }
}
resources 文件夹下配置文件， factorybeantest.xml

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

        <bean id="petFactory" class="com.sharpcj.factorytest.PetFactory"></bean>

        <bean id="dog" factory-bean="petFactory" factory-method="getPet">
            <constructor-arg value="dog"></constructor-arg>
        </bean>

        <bean id="parrot" factory-bean="petFactory" factory-method="getPet">
            <constructor-arg value="parrot"></constructor-arg>
        </bean>
</beans>
测试类，AppTest.java

package com.sharpcj.factorytest;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class AppTest {
    public static void main(String[] args) {
        Resource resource = new ClassPathResource("factorybeantest.xml");
        BeanFactory factory = new DefaultListableBeanFactory();
        BeanDefinitionReader bdr = new XmlBeanDefinitionReader((BeanDefinitionRegistry) factory);
        bdr.loadBeanDefinitions(resource);

        Dog dog = (Dog) factory.getBean("dog");
        Parrot parrot = (Parrot) factory.getBean("parrot");
        dog.move();
        parrot.move();
    }
}
程序结果：


可以看到，程序正确执行了。注意看配置文件中，我们并没有配置 dog 和 parrot 两个 Bean 类的 class 属性，而是配置了他们的 factory-bean 和 factory-method两个属性，这样，Spring 容器在创建 dog 和 parrot 实例时会先创建 petFactory 的实例，然后再调用其工厂方法，创建对应的 dog 和 parrot 实例。

另外，假设我们在测试类中通过 factory 获取 Bean 实例时，传入一个非法的参数，会如何？ PetFactory 类工厂方法的代码,看起来会抛出我们自定义的异常？
比如调用如下代码：

factory.getBean("cat");
结果是：


结果说明，Spring 本身就处理了参数异常，因为我们并没有在配置文件中配置中配置 name 为 “cat” 的 Bean, 所以，Spring 容器抛出了此异常，程序执行不到工厂方法里去了。

调用静态工厂方法创建 Bean
抛开 Spring 不谈，相比实例工厂方法，其实我们平时用的更多的可能是静态工厂方法。 Spring 当然也有静态工厂方法创建 Bean 的实现。下面我们修改工厂方法为静态方法：

package com.sharpcj.staticfactorytest;

import com.sharpcj.factorytest.Dog;
import com.sharpcj.factorytest.IPet;
import com.sharpcj.factorytest.Parrot;

public class PetFactory {
    public static IPet getPet(String type){
        if ("dog".equals(type)) {
            return new Dog();
        } else if ("parrot".equals(type)){
            return new Parrot();
        } else {
            throw new IllegalArgumentException("pet type is illegal!");
        }
    }
}
此时我们也应该修改配置文件，这里我们重新创建了一个配置文件， staticfactorbeantest.xml

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="dog" class="com.sharpcj.staticfactorytest.PetFactory" factory-method="getPet">
        <constructor-arg value="dog"></constructor-arg>
    </bean>
    <bean id="parrot" class="com.sharpcj.staticfactorytest.PetFactory" factory-method="getPet">
        <constructor-arg value="parrot"></constructor-arg>
    </bean>
</beans>
测试代码：

package com.sharpcj.staticfactorytest;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class Apptest {
    public static void main(String[] args) {
        Resource resource = new ClassPathResource("staticfactorybeantest.xml");
        BeanFactory factory = new DefaultListableBeanFactory();
        BeanDefinitionReader bdr = new XmlBeanDefinitionReader((BeanDefinitionRegistry) factory);
        bdr.loadBeanDefinitions(resource);

        Dog dog = (Dog) factory.getBean("dog");
        Parrot parrot = (Parrot) factory.getBean("parrot");
        dog.move();
        parrot.move();
    }
}
测试结果如下：


结果正常，这里注意配置文件，使用静态工厂方法是，配置文件中我们并没有配置 PetFactory， 而在配置
dog 和 parrot 时，我们配置的 class 属性的值是工厂类的完整类名com.sharpcj.staticfactorytest.PetFactory，同事配置了 factory-method属性。

调用实例工厂方法和调用静态工厂方法创建 Bean 的异同
调用实例工厂方法和调用静态工厂方法创建 Bean 的用法基本相似，区别如下：

配置实例工厂方法创建 Bean,必须将实例工厂配置成 Bean 实例；而配置静态工厂方法创建 Bean,则无需配置工厂 Bean;
配置实例工厂方法创建 Bean,必须使用 factory-bvean 属性确定工厂 Bean; 而配置静态工厂方法创建 Bean,则使用 class 属性确定静态工厂类。
相同之处如下：
都需要使用 factory-method 指定生产 Bean 实例的工厂方法；
工厂方法如果需要参数，都使用 <constructor-arg.../> 元素指定参数值；
普通的设值注入，都使用 <property.../>元素确定参数值。
三、FactoryBean 和 BeanFactory
FactoryBean 和 BeanFactory 是两个极易混淆的概念，需要理解清楚。下面分别来明说这两个概念。

FactoryBean
FactoryBean 翻译过来就是 工厂Bean 。需要说明的是，这里的 FactoryBean 和上一节提到的工厂方法创建Bean不是一个概念，切莫不要把实例工厂创建 Bean 时，配置的工厂 Bean ，和 FactoryBean 混为一谈。两者没有联系，上一节说的是标准的工厂模式，Spring 只是通过调用工厂方法来创建 Bean 的实例。
这里的所说的 工厂 Bean 是一种特殊的 Bean 。它需要实现 FactoryBean 这个接口。

FactoryBean 接口提供了三个方法：

T getObject() throws Exception;
Class<?> getObjectType();
boolean isSingleton() {return true;}
当自定义一个类实现了FactoryBean接口后，将该类部署在 Spring 容器里，再通过 Spring 容器调用 getBean 方法获取到的就不是该类的实例了，而是该类实现的 getObject 方法的返回值。这三个方法意义如下：

getObject() 方法返回了该工厂Bean 生成的 java 实例。
getObjectType() 该方法返回该工厂Bean 生成的 java 实例的类型。
isSingleton() 该方法返回该工厂Bean 生成的 java 实例是否为单例。
下面举一个例子：
定义一个类 StringFactoryBean.java

package com.sharpcj.factorybeantest;

import org.springframework.beans.factory.FactoryBean;

public class StringFactoryBean implements FactoryBean<Object> {
    private String type;
    private String originStr;

    public void setType(String type) {
        this.type = type;
    }

    public void setOriginStr(String originStr) {
        this.originStr = originStr;
    }

    @Override
    public Object getObject() throws Exception {
        if("builder".equals(type) && originStr != null){
            return new StringBuilder(originStr);
        } else if ("buffer".equals(type) && originStr != null) {
            return new StringBuffer(originStr);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Class<?> getObjectType() {
        if("builder".equals(type)){
            return StringBuilder.class;
        } else if ("buffer".equals(type)) {
            return StringBuffer.class;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
配置文件， factorybean.xml

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="strFactoryBean" class="com.sharpcj.factorybeantest.StringFactoryBean">
        <property name="type" value="buffer"/>
        <property name="originStr" value="hello"/>
    </bean>

</beans>
测试类 AppTest.java

package com.sharpcj.factorybeantest;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class AppTest {
    public static void main(String[] args) {

        ApplicationContext context = new ClassPathXmlApplicationContext("factorybean.xml");

        System.out.println(context.getBean("strFactoryBean"));
        System.out.println(context.getBean("strFactoryBean").getClass().toString());

    }
}

结果如下：


那有没有办法把获取 FactoryBean 本身的实例呢？当然可以，如下方式

context.getBean("&strFactoryBean")
在getBean方法是，在Bean id 前面增加&符号。

package com.sharpcj.factorybeantest;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AppTest {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("factorybean.xml");
        System.out.println(context.getBean("strFactoryBean"));
        System.out.println(context.getBean("strFactoryBean").getClass().toString());
        System.out.println(context.getBean("&strFactoryBean").getClass().toString());
    }
}
结果如下：


BeanFactory
其实在前面的例子中，AppTest.java 类中我们已经使用过 BeanFactory 了, BeanFactory 也是一个接口。Spring 有两个核心的接口： BeanFactory 和 ApplicationContext ，其中 ApplicationContext 是 BeanFactory 的子接口，他们都可以代表 Spring 容器。Spring 容器是生成 Bean 实例的工厂，并管理容器中的 Bean 。
BeanFactory 包含如下几个基本方法：

boolean containsBean(String name) // 判断Spring容器中是否包含 id 为 name 的 Bean 实例
<T> getBean(Class<T> requeriedType) // 获取Spring容器中属于 requriedType 类型的、唯一的 Bean 实例。
Object getBean(String name) // 返回容器中 id 为 name 的 Bean 实例
<T> getBean(String name, Class requiredType) // 返回容器中 id 为name，并且类型为 requriedType 的Bean
Class<T> getType(String name) // 返回 id 为 name 的 Bean 实例的类型
四、Bean 后处理器 和 容器后处理器
Spring 提供了两种常用的后处理使得 Spring 容器允许开发者对 Spring 容器进行扩展，分别是 Bean 后处理器和容器后处理器。

Bean 后处理器
Bean 后处理器是一种特殊的 Bean， 它可以对容器中的 Bean 进行后处理，对 Bean 进行额外加强。这种特殊的 Bean 不对外提供服务，它主要为容器中的目标 Bean 进行扩展，例如为目标 Bean 生成代理等。

Bean 后处理器需要实现 BeanPostProcessor 接口，该接口包含如下两个方法：

Object postProcessBeforeInitialization(Object bean, String beanName)
Object postProcessAfterInitialization(Object bean, String beanName)
这两个方法的第一个参数都表示即将进行后处理的 Bean 实例，第二个参数是该 Bean 的配置 id ，这两个方法会在目标 Bean 初始化之前和初始化之后分别回调。

看例子：
新建一个类，PetBeanPostProcessor.java 重写上述两个方法。

package com.sharpcj.beanpostprocessor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class PetBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if ("dog".equals(beanName)) {
            System.out.println("准备初始化 dog ...");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if ("parrot".equals(beanName)) {
            System.out.println("parrot 初始化完成 ... ");
        }
        return bean;
    }
}
配置文件, beanpostprocessor.xml：

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
    <bean id="dog" class="com.sharpcj.beanpostprocessor.Dog"/>
    <bean id="parrot" class="com.sharpcj.beanpostprocessor.Parrot"/>
    <bean class="com.sharpcj.beanpostprocessor.PetBeanPostProcessor"/>
</beans>
最后看测试代码：AppTest.java

package com.sharpcj.beanpostprocessor;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AppTest {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("beanpostprocessor.xml");
        Dog dog = (Dog) context.getBean("dog");
        Parrot parrot = (Parrot) context.getBean("parrot");
        dog.move();
        parrot.move();
    }
}
执行结果如下：


可以看到，我们像配置其它 Bean 一样配置该 Bean 后处理器,但是我们没有配置 id ，这是因为我们使用的 ApplicationContext 作为 Spring 容器，Spring 容器会自动检测容器中所有的 Bean ，如果发现某个 Bean 实现了 BeanPostProcessor 接口，ApplicationContext 就会自动将其注册为 Bean 后处理器。 如果使用 BeanFactory 作为 Spring 的容器，则需手动注册 Bean 后处理器。这时，需要在配置文件中为 Bean 后处理器指定 id 属性，这样容器可以先获取到 Bean 后处理器的对象，然后注册它。如下：

配置文件：

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
    <bean id="dog" class="com.sharpcj.beanpostprocessor.Dog"/>
    <bean id="parrot" class="com.sharpcj.beanpostprocessor.Parrot"/>
    <bean id="petBeanPostProcessor" class="com.sharpcj.beanpostprocessor.PetBeanPostProcessor"/>
</beans>
测试代码：

Resource resource = new ClassPathResource("beanpostprocessor.xml");
DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
new XmlBeanDefinitionReader(beanFactory).loadBeanDefinitions(resource);

PetBeanPostProcessor petBeanPostProcessor = (PetBeanPostProcessor) beanFactory.getBean("petBeanPostProcessor");
beanFactory.addBeanPostProcessor(petBeanPostProcessor);

Dog dog = (Dog) beanFactory.getBean("dog");
Parrot parrot = (Parrot) beanFactory.getBean("parrot");
dog.move();
parrot.move();
上面例子中我们只是在实例化 Bean 前后打印了两行 Log ， 那么实际开发中 Bean 后处理有什么用处呢？其实 Bean 后处理器的作用很明显，相当于一个拦截器，对目标 Bean 进行增强，在目标 Bean 的基础上生成新的 Bean。 若我们需要对容器中某一批 Bean 进行增强处理，则可以考虑使用 Bean 后处理器，结合前面一篇文章讲到到代理模式，可以想到，我们完全可以通过 Bean 后处理器结合代理模式做更多实际工作，比如初始化，深圳完全改变容器中一个或者一批 Bean 的行为。
你可以配置多个 BeanPostProcessor 接口，通过设置 BeanPostProcessor 实现的 Ordered 接口提供的 order 属性来控制这些 BeanPostProcessor 接口的执行顺序。

容器后处理器
容器后处理器则是对容器本身进行处理。容器后处理器需要实现 BeanFactoryPostProcessor 接口。该接口必须实现如下一个方法：

postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
类似于 BeanPostProcessor , ApplicationContext 可以自动检测到容器中的容器后处理器，并自动注册，若使用 BeanFactory 作为 Spring 容器，则需要手动获取到该容器后处理器的对象来处理该 BeanFactory 容器。
例子：容器后处理器， PetBeanFactoryPostProcessor.java

package com.sharpcj.beanfactorypostprocessor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class PetBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        System.out.println("容器后处理器没有对容器做改变...");
    }
}
配置文件：

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
    <bean id="dog" class="com.sharpcj.beanfactorypostprocessor.Dog"/>
    <bean id="parrot" class="com.sharpcj.beanfactorypostprocessor.Parrot"/>
    <bean id="petBeanFactoryPostProcessor" class="com.sharpcj.beanfactorypostprocessor.PetBeanFactoryPostProcessor"/>
</beans>
测试代码：

package com.sharpcj.beanfactorypostprocessor;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AppTest {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("beanfactorypostprocessor.xml");
        Dog dog = (Dog) context.getBean("dog");
        Parrot parrot = (Parrot) context.getBean("parrot");
        dog.move();
        parrot.move();
    }
}
结果如下：



容器后处理器的作用对象是容器本身，展开 BeanFactoryPostProcessor 接口的继承关系，我们可以看到 Spring 本身提供了很多常见的容器后处理器。


其中一些在实际开发中很常用，如属性占位符配置器 PropertyPlaceholderConfigurer 、 重写占位符配置器 PropertyOverrideConfigurer 等。

五、BeanFactoryAware 和 BeanNameAware
让 Bean 获取 Spring 容器
程序启动时，初始化 Spring 容器，我们已经知道如何通过容器，获取 Bean 的实例方式,形如：

BeanFactory factory = xxx ;
factory.getBean(xxx...);
在某些特殊情况下，我们需要让 Bean 获取 Spring 容器，这个如何实现呢?
我们只需要让 Bean 实现 BeanFactoryAware 接口，该接口只有一个方法：

void setBeanFactory(BeanFactory beanFactory);
该方法的参数即指向创建该 Bean 的 BeanFactory ，这个 setter 方法看起来有点奇怪，习惯上在 java 中 setter 方法都是由程序员调用，传入参数，而此处的方法则由 Spring 调用。与次类似的，还有 ApplicationContextAware 接口，需要实现一个方法

void setApplicationContext(ApplicationContext applicationContext);
下面通过例子来说明：
这次我们的 Dog 类，修改了：

package com.sharpcj.beanfactoryaware;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class Dog implements IPet, BeanFactoryAware {

    private BeanFactory factory;

    @Override
    public void move() {
        System.out.println("Dog can run!");
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        factory = beanFactory;
    }

    public void test() {
        Parrot parrot = (Parrot) factory.getBean("parrot");
        parrot.move();
    }

}
测试代码，AppTest.java

package com.sharpcj.beanfactoryaware;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AppTest {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("beanfactoryaware.xml");
        Dog dog = (Dog) context.getBean("dog");
        dog.test();
    }
}
输出结果：

Parrot can fly!
结果表明，我们确实在 Dog 类里面获取到了 Spring 容器，然后通过该容器创建了 Parrot 实例。

获取 Bean 本身的 id
有时候，当我们在开发一个 Bean 类时，Bean 何时被部署到 Spring 容器中，部署到 Spring 容器中的 id 又是什么，开发的时候我们需要提前预知，这是就可以借助 Spring 提供的 BeanNameAware 接口,该接口提供一个方法：

void setBeanName(String name);
用法与上面一样，这里不再过多解释，修改上面的例子：
Dog.java

package com.sharpcj.beanfactoryaware;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;

public class Dog implements IPet, BeanFactoryAware, BeanNameAware {

    private BeanFactory factory;

    private String id;

    @Override
    public void move() {
        System.out.println("Dog can run!");
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        factory = beanFactory;
    }

    public void test() {
        System.out.println("Dog 的 id 是： " + id);
    }

    @Override
    public void setBeanName(String name) {
        id = name;
    }
}
测试类：AppTest.java

package com.sharpcj.beanfactoryaware;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AppTest {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("beanfactoryaware.xml");
        Dog dog = (Dog) context.getBean("dog");
        dog.test();
    }
}
结果：

Dog 的 id 是： dog
六、ApplicationContext 的事件机制
ApplicationContext 的事件机制是观察者模式的实现，由 事件源、事件和事件监听器组成。通过 ApplicationEvent类 和 ApplicationListener 接口实现。
Spring 事件机制的两个重要成员：

ApplicationEvent： 容器事件，必须由 ApplicationContext 发布
ApplicationListener：事件监听器，可由容器中任何 Bean 担任。
事件机制原理：有 ApplicationContext 通过 publishEvent() 方法发布一个实现了ApplicationEvent接口的事件，任何实现了 ApplicationListener接口的 Bean 充当事件监听器，可以对事件进行处理。这个原理有点类似于 Android 里面广播的实现。
下面给出一个例子：
ITeacher.java

package com.sharpcj.appevent;

public interface ITeacher {
    void assignWork();
}
ChineseTeacher.java

package com.sharpcj.appevent;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

public class ChineseTeacher implements ITeacher, ApplicationListener {
    @Override
    public void assignWork() {
        System.out.println("背诵三首唐诗");
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ComplainEvent) {
            System.out.println("语文老师收到了抱怨...");
            System.out.println("抱怨的内容是：" + ((ComplainEvent) event).getMsg());
            System.out.println("认真倾听抱怨，但是作业量依然不能减少...");
        }
    }
}
MathTeacher.java

package com.sharpcj.appevent;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

public class MathTeacher implements ITeacher, ApplicationListener {
    @Override
    public void assignWork() {
        System.out.println("做三道数学题");
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        System.out.println("数学老师收到了事件，但没有判断事件类型，不作处理。。。。");
    }
}
定义一个事件 ComplainEvent.java 继承自 ApplicationContextEvent:

package com.sharpcj.appevent;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;

public class ComplainEvent extends ApplicationContextEvent {

    private String msg;

    /**
     * Create a new ContextStartedEvent.
     *
     * @param source the {@code ApplicationContext} that the event is raised for
     *               (must not be {@code null})
     */
    public ComplainEvent(ApplicationContext source) {
        super(source);
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
IStudent.java

package com.sharpcj.appevent;

public interface IStudent {
    void doWork();
}
XiaoZhang.java

package com.sharpcj.appevent;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class XiaoZhang implements IStudent, ApplicationContextAware {
    private ApplicationContext mContext;

    @Override
    public void doWork() {
        System.out.println("小张背了李白的唐诗，做了三道几何体");
    }

    public void complain() {
        ComplainEvent complainEvent = new ComplainEvent(mContext);
        complainEvent.setMsg("作业太多了");
        mContext.publishEvent(complainEvent);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.mContext = applicationContext;
    }
}
配置文件， appevent.xml：

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="chineseTeacher" class="com.sharpcj.appevent.ChineseTeacher"/>
    <bean id="mathTeacher" class="com.sharpcj.appevent.MathTeacher"/>
    <bean id="xiaoZhang" class="com.sharpcj.appevent.XiaoZhang"/>
</beans>
测试类, AppTest.java：

package com.sharpcj.appevent;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AppTest {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("appevent.xml");
        XiaoZhang xiaoZhang = (XiaoZhang) context.getBean("xiaoZhang");
        xiaoZhang.complain();
    }
}
测试结果如下：


咦，数学老师也受到了事件，为什么还受到两次事件？首先根据代码，我们能想明白，只要是容器发布了事件，所有实现了ApplicationListener接口的监听器都能接收到事件，那为什么，数学老师打印出了两条呢？我猜，容器初始化期间，本身发布了一次事件。下面稍微修改了一下代码，便验证了我的猜想是正确的。
MathTeacher.java

package com.sharpcj.appevent;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

public class MathTeacher implements ITeacher, ApplicationListener {
    @Override
    public void assignWork() {
        System.out.println("做三道数学题");
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        System.out.println("数学老师收到了事件，但没有判断事件类型，不作处理。。。。" + event.getClass().getSimpleName());
    }
}
然后再次执行，结果如下：


事实证明，容器初始化时，确实发布了一次 ContextRefreshedEvent 事件。

七、总结
既上一篇文章总结了一下 Spring 装配 Bean 的三种方式之后，这篇文章继续记录了一写 SpringIOC 的高级知识，本文没有按照一般书籍的顺序介绍 Spring 容器的相关知识，主要是从横向对几组关键概念进行对比解释，主要记录了一下 SpringIOC 中的一些关键知识点。当然 Spring IOC 其它的知识点还有很多，比如装配 Bean 时属性歧义性处理、 Bean 的组合属性、注入集合值、国际化、基于 XML Schema 的简化配置方式等。其它知识点可以通过查阅官方文档或者专业书籍学习。
接下来会再整理一篇 Spring AOP 的文章。

作者：SharpCJ

出处：https://www.cnblogs.com/joy99/p/10903567.html

本站使用「署名 4.0 国际」创作共享协议，转载请在文章明显位置注明作者及出处。