## Elasticsearch Cache
-----

### 1. Elasticsearch缓存分类

ES的缓存都JVM堆中，缓存主要包括如下三大类:

Node Query Cache (Filter Context)

Shard Query Cahce (Cache Query 的结果)

Fielddata Cache



### 1.1. Node Query Cache

每个节点有一个Node Query Cache，由该节点所有分片(shard)共享，只缓存Filter Context的相关内容

Cache采用LRU算法

这个Cache大小需要在每个Data Node上配置，并且是静态配置

Node Level: indices.queryies.cache.size => 10%

Index Level: index.queries.cache.enabled -> true

### 1.2. Shard Request Cache

```html
PUT blogs
{
    "settings": {
        "index.requests.cache.enable": false
    }
}

GET blogs/_search?request_cache=true
{
    "size":0,
    "aggs": {
        "1":{
            "term":{
                "field":"colors"
            }
        "
    }
}
```

**缓存每个分片上的查询结果**

指挥缓存设置了size=0的查询结果，不会缓存hits。但是会缓存Aggregations和Suggestions。

**Cache Key**

LRU算法，将整个JSON查询串作为key,与JSON对象的顺序相关

**静态配置**

数据节点： indices.requests.cache.size -> 1%


### 1.3. Fielddata Cache

除了Text类型，默认都采用doc_values。节省了内存

Aggregation的Global ordinals也保存在Fielddata cache中

Text类型的字段需要打开Fielddata才能对其进行聚合和排序

Text经过分词，排序和聚合效果不佳，建议不要轻易使用

参数indices.fielddata.cache.size 默认无限制，可以控制其大小避免GC

### 2. 缓存失效

**Node Query Cache**

保存的是Segment级缓存命中的结果。Segment被合并后，缓存会失效

**Shard Request Cahce**

分片Refresh的时候，Shard Request Cache会失效。

如果Shard对应的数据频繁发生变化，该缓存的效率会很差

**Fielddata Cache**

Segment被合并后，会失效

### 3. 管理内存的重要性

Elasticsearch高效运维依赖与内存的合理分配

可以物理内存一半分配给JVM，一半留给操作系统，缓存索引文件

内存问题，依法的问题

长时间GC，影响节点，导致集群相应缓慢

OOM，导致节点丢失

### 4. 查看节点内存使用情况

```html
GET _cat/nodes?v

GET _nodes/stats/indices?pretty

GET _cat/nodes?v&h=name,queryCacheMemory,queryCacheEvictions,requestCahceMemory,requestCacheHitCount,request_cache.miss_count

GET _cat/nodes?h=name,port,segments.memory,segments.index_writer_memory,fielddata.memory_size,query_cache.memory_size,request_cache.memory_size&v
```

### 5. 一些常见的内存问题

### 5.1. Segment个数过多，导致Full GC

**现象：** 集群整体相应缓慢，也没有特别多的数据读写，但是发现节点在持续进行Full GC

**分析：** 查看Elasticsearch的内存使用，发现segments.memory占用很大空间

**解决：** 通过force merge，把segments合并成一个

**建议：**

对于不再写入和更新的索引，可以将其设置成只读。同时，进行force merge操作。如果问题依然存在，则需要考虑扩容。

对索引进行force merge，还可以减少对global_ordinals数据结构的构建，减少fielddata cache的开销

### 5.2. Field data cache过大，导致FullGC

**现象** 集群整体相应缓慢，也没有特别多的数据读写。但是发现节点在持续进行Full GC

**分析** 查看Elasticsearch的内存使用，发现fielddata.memory.size占用很大空间。同时数据不存在写入和更新，也执行过segment merge。

**解决** 将indices.fielddata.cache.size设小，重启节点，堆内存恢复正常

**建议**

Fielddata Cache的构建比较重，Elasticsearch不会主动释放，所以这个值应该设置的保守些。

如果业务上确实有所需要，可以通过增加节点，扩容解决。


### 5.3. 复杂的嵌套聚合，导致集群Full GC

**现象** 节点响应缓慢，继续进行Full GC

**分析** 导出Dump文件，发现内存中由大量的Bucket对象，查看日志，发现复杂的嵌套聚合。

**解决** 优化聚合

**建议**

在大量数据集上进行嵌套聚合查询，需要很大的堆内存来完成。

如果业务场景确实需要，则需要增加硬件进行扩展。

同时，为了避免这类查询影响整个集群，需要设置Circuit Breaker和search.max_buckets的数值


### 6. Circuit Breaker

就是一个断路器的概念，通过多种断路器避免不合理的操作引发OOM的问题。

每个断路器可以指定内存使用的限制

Parent circuit breaker: 设置所有的熔断器可以使用的内存的总量

Fielddata circuit breaker: 加载fieldata锁需要的内存

Request circuit breaker: 防止每个请求级数据结构超过一定的内存（例如聚合计算的内存）

in flight circuti breaker: request中的断路器

Accounting request circuit breaker； 请求结束后不能放的对象锁占用的内存。


获取circuit breaker的统计信息

```html
GET _cat/_nodes/stats/breaker?

如果tripped大于0，说明有过熔断的情况

Limit size和estimated size越接近，越可能引发熔断
```

千万不要出发了熔断，就盲目调大参数。有可能会导致集群出问题，也不应该盲目调小参数，需要进行评估

建议升级到7.x版本，更好的circuit breaker的实现机制

增加了indices.breaker.total.user_readl_memory配置项，可以更加精准的分析内存状况，避免OOM