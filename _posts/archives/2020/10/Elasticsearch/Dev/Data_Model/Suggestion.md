## Elasticsearch数据建模最佳实践
-----

### 1. 如何处理关联关系

有限考虑Denormalization

当数据包含多数值对象(多个演员),同时又查询需求时。考虑使用Nested

关联文档更新非常频繁时。考虑使用Child/Parent


### 2. 避免过多字段

一个文档中，最好避免大量的字段

1. 过多的字段数不容易维护
1. Mapping信息保存在Cluster State中。数据量过大。对集群性能会有影响(Cluster State信息需要和所有的节点同步)
1. 删除或者修改数据需要reindex

默认最大字段数是1000，可以设置index.mapping.total_fields.limit限定最大字段数


#### 分析什么原因会导致文档中又成百上千的字段？

**Dynamic v.s Strict**

Dynamic(生产环境中，尽量不要打开Dynamic)

```text
true 未知字段会被自动加入
false 新字段不会被索引。但是会保存在_source中
strict 新增字段不会被索引，文档写入失败
```

Strict

可以从字段级别控制新字段的加入


### 3. 避免正则的查询

问题：
1. 正则，通配符查询，前缀查询属于Term查询，大师性能不好
1. 特别是将通配符放在开头，会导致性能的灾难

案例：

文档中某个字段包含了Elasticsearch的版本信息。例如 version: "7.1.0"

搜索所有是bug fix的版本？每个主要版本号锁关联的文档?

### 4. 避免空值引起的数值不准

通过默认值，避免空值


### 5. 为索引的Mapping加入meta信息

Mappings设置非常用药，需要从两个维度进行考虑

1. 功能： 搜索、聚合、排序
1. 性能： 存储的开销；内存的开销；搜索的性能

Mappings设置是一个迭代的工程

1. 加入新的字段很容易（必要时需要Update_by_Query)
1. 更新删除字段不允许（需要Reindex重建数据）
1. 最好能对Mappings加入Meta信息，更好的进行版本管理
1. 可以考虑将Mapping文件上传到git进行管理

```html
PUT blogs
{
    "mappings": {
        "_meta":{
            "blog_version_mapping": "1.0"
        }
    }
}
```