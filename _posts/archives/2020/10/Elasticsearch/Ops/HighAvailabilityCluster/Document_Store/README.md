## 文档分布式存储
-----

### 文档存储在分片上

文档会处处在具体的某个主分片和副本分片上

文档到分片的映射算法

确保文档能均匀分布在所用分片上，充分利用硬件资源，避免部分机器闲置，部分机器繁忙

1. 随机Round Robin。 当查询文档1，分片数很多，需要多次查询才可能查到文档1
1. 维护文档到分片的映射关系。当文档数据量大的时候，维护成本高。
1. 实时计算，通过文档1，自动算出需要去哪个分片上获取文档

### 文档到分片的路由算法

```text
shard = hash(_routing) % number_of_primary_shards
```

Hash算法确保文档均匀分散到分片中

默认的_routing值是文档id

可以自行定制routing数值，例如用相同国家的商品，都分配到指定的shard

```html
PUT posts/_doc/100？routing=bigdata
{
    "title": "Mastering Elasticsearch",
    "body": "Let's Rock"
}
```

这就是设置Index Settings后，主分片数不能随意修改的原因。
