## 并发控制

-----

**悲观并发控制**

假定有变更冲突的可能。会对资源加锁，防止冲突。例如数据库行锁

**乐观并发控制**

假定冲突是不会发生的，不会阻塞正在尝试的操作。如果数据库再读写中被修改，更新将失败。应用程序决定如何解决冲突，例如重试更新，使用新的数据或者将错误报告给用户。

ES采用的是乐观并发控制


### ES的乐观并发控制

ES中的文档是不可变更的。如果你更新的一个文档，会将该文档标记为删除，同时增加一个全新的文档，同时文档版本加1

内部版本控制

```text
if_seq_no + if_primary_term
```

使用外部版本（使用其他数据库作为主要数据存储）

```text
version +version_type = external
```

```html
PUT products/_doc/1
{
    "title":"iphone",
    "count":1000
}

GET products/_doc/1

PUT products/_doc/1?if_seq_no=0&if_primary_term=1
{
    "title":"iphone",
    "count":1000
}

PUT products/_doc/1?version=30--&version_type=external
{
    "title":"iphone",
    "count":100
}
```

