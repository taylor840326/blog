## 事务

-----

@Transactional注解一般可以作用在类或者方法上

1. 作用于类上：当把@Transactional注解放在类上时，表示该类的所有public方法都配置相同的事务属性信息。
1. 作用于方法上：当类配置了@Transactional注解，方法也配置了@Transactional注解，方法的事务会覆盖类的事务配置信息.

在要开启事务的方法上使用@Transactional注解即可开启事务

```java
@Transactional(rollbackFor = Exception.class)
public void save(){
    ...
}
```

我们知道Exception分为运行时异常RuntimeException和非运行时异常。

在@Transactional注解中如果不配置rollbackFor属性，那么事务只会在遇到RuntimeException的时候才会回滚；加上rollbackFor=Exception.class可以让事务在遇到非运行时异常时也可以回滚。
