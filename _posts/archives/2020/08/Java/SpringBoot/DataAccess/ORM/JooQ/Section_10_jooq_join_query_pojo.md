## POJO的扩展
-----

由 jOOQ 生成的 POJO 是针对单个表的字段进行生成。在关联查询中，通常我们要将多个表的数据存放在一个类里。这种情况下，我们可以自行创建一个类，去添加我们需要的多表字段成员变量

对于这种比较常见的情况来说，手动创建类显得很繁琐，我们可以在生成 POJO 的同时，创建一个空白的并且继承原有POJO的类，需要添加字段的时候，直接在我们生成的类上添加想要的其他表字段

要想达到以上效果，我们还是需要自定义代码生成器，添加针对这些继承类（这里先称之为 Entity）的创建代码逻辑。通过之前的需求分析，这些代码创建后需要由我们自行修改的，所以只能生成一次， 文件存在的时候就不能再进行覆盖或者被代码生成器删除

所有生成的 Entity 我们需要放在 jOOQ 配置的目标包平行的父级包内。这样 jOOQ 在生成代码前清空指定目录的包内容时，我们自己的包不会被波及到。并且在生成逻辑中，需要判断目标 Entity 文件是否已存在，如果存在就忽略

通过这些逻辑，在我们为 Entity 添加关联表的成员变量的时候，就不会被 jOOQ 的代码生成器所覆盖或删除。可以安全的去增加代码，实现我们想要的逻辑

### 1. Entity 生成

一个初始的 Entity 很简单，只需要继承 jOOQ 生成的 POJO 即可

```java
package com.diamondfsd.jooq.learn.entity;

import com.diamondfsd.jooq.learn.jooq.tables.pojos.S1UserPojo;

public class S1User extends S1UserPojo {

}
```

想要实现这样的生成，我在自定义的代码生成器 CustomJavaGenerator 内添加了以下内容

首先重写了父级的 generatePojo 方法，在生成完原有的 POJO 后，调用自定义的 generateEntity 方法，创建Entity

```java
@Override
protected void generatePojo(TableDefinition table) {
    super.generatePojo(table);
    // 在生成完POJO后，生成 Entity
    generateEntity(table);
}
```

具体generateEntity的实现内容大家可以自行查看 CustomJavaGenerator 文件的源码，相对来说比较简单，因为只是字符串的拼接

在 Entity 创建完成后， 我们还需要对生成的DAO进行调整， 将原来泛型内的 POJO 类替换为 Entity 类，是通过替换字符串完成的，具体大家可以在源码中的 generateDao 方法中看到

调整后，以 s1_user 表为例， 最终生成的 DAO 内容如下，替换了泛型和构造函数内目标POJO的类型

```java
// ...
import com.diamondfsd.jooq.learn.entity.S1User;
// ...

@Repository
public class S1UserDao extends ExtendDAOImpl<S1UserRecord, S1User, Integer> { 
    public S1UserDao() {
        super(TS1User.S1_USER, S1User.class);
    }

    @Autowired
    public S1UserDao(Configuration configuration) {
        super(TS1User.S1_USER, S1User.class, configuration);
    }
    // ...
}
```

通过这样的操作后， 我们 DAO 所有方法的返回值都是都是我们自定义的 Entity 类很方便进行扩展

例如我们需要查询 s2_user_message 表关联 s1_user 查出用户消息所属用户的用户名，可以直接在我们生成的Entity S2UserMessage 上添加字段，然后在查询的时候，into 到此类即可

```java
public class S2UserMessage extends S2UserMessagePojo {
    String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
@Test
void leftJoinUsernameTest() {
    List<S2UserMessage> userMessageList = userMessageDao.create()
       .select(S2_USER_MESSAGE.ID, S2_USER_MESSAGE.MESSAGE_TITLE, S1_USER.USERNAME)
       .from(S2_USER_MESSAGE)
       .leftJoin(S1_USER).on(S1_USER.ID.eq(S2_USER_MESSAGE.USER_ID))
       .fetchInto(S2UserMessage.class);

    assertTrue(userMessageList.size() > 0);
    List<String> usernameList = userMessageList.stream()
            .map(S2UserMessage::getUsername)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    assertEquals(userMessageList.size(), usernameList.size());
}
```

### 2. 内容总结

本章源码: https://github.com/k55k32/learn-jooq/tree/master/section-10

本章较为简短，主要是告诉大家可以通过自定代码生成器做一些其他扩展操作。 利用代码生成器的特性，可以方便快捷的生成任意的代码，很方便和通用。 如果是在公司的项目中，这些都可以单独抽离出来作为一个扩展包让每个服务去引用。通过生成器生成基础的项目框架，不仅比手动创建效率高，而且不容易出错，还可以很好的代码结构