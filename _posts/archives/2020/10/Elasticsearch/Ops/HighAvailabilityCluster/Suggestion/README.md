## 运维相关建议
-----

### 集群的生命周期管理

**预上线**

1. 评估用户的需求及使用场景
1. 数据建模
1. 容量规划
1. 选择合适的部署架构
1. 性能测试


**上线**

1. 监控流量
1. 定期检查潜在问题。防患于未然，发现错误的使用方式，机时增加机器
1. 对索引进行优化(Index Lifecycle Management),检测是否存在不均衡而导致有部分节点过热
1. 定期数据备份
1. 定期滚动升级


**下架前监控流量**

下架前监控流量，实现Stage Decommission

### 部署的建议

根据实际场景，选择合适的部署方式，选择合理的硬件配置

1. 搜索类
1. 日志/指标类

部署要考虑**反亲和性(Anti-Affinity)**

尽量将机器分散在不同的机架。例如，3台Master节点必须分散在不同的机架上

善用Shard Filtering进行配置


### 使用要遵循一定的规范

**Mapping**

生产环境中索引应该考虑禁止Dynamic Index Mapping,避免过多字段导致Cluster State占用过多

禁止索引自动创建的功能，创建时必须提供Mapping或通过Index Template进行设定


```html
PUT _cluster/settings
{
    "persistent":{
        "action.auto_create_index": false
    }
}

PUT _cluster/settings
{
    "persistent":{
        "action.auto_create_index": ".monitoring-*,logstash-*"
    }
}
```

**设置Slowlogs**

设置Slowlogs发现一些性能不好，甚至时错误的使用Pattern的情况

例如：

1. 错误的将网址映射成keyword，然后用通配符查询。应该使用text,结合URL分词器
1. 严禁一切"*"开头的通配符查询


### 对重要的数据进行备份

集群备份

```html
https://www.elastic.co/guide/en/elasticsearch/reference/7.1/modules-snapshots.html
```

### 定期更新到新版本

ES在新版本中会持续对性能做出优化；提供更多的新功能

circuit breaker实现的改进

修复一些已知的bug和安全隐患

### ES的版本

Elasticsearch的版本格式时x.y.z

x -> Major

y -> Minor

z -> Pathch

Elasticsearch可以使用上一个主版本的索引

7.x可以使用6.x/7.x， 不支持使用5.x

5.x可以使用2.x


### ES集群的升级方式

**Rolling Upgrade**

没有downtime

```html
https://www.elastic.co/guide/en/elasticsearch/reference/7.1/colling-upgrades.html
```

**Full Cluster Restart**

集群在更新期间不可用

升级更快

升级步骤：

1. 停止索引数据，同时备份集群
1. Disable Shard Allocation (Persistent)
1. 执行Synced Flush
1. 关闭并更新所有节点
1. 先运行所有Master节点，再运行其他节点
1. 等集群变黄后打开Shard Allocation

```html
PUT _cluster/settings
{
    "persistent":{
        "cluster.routing.allocation.enable":"primaries"
    }
}

POST _flush/synced
```

### 移动索引的分片

把分片从一个节点移动到另外一个节点

使用场景：

当一个数据节点上有过多Hot Shards的时候，可以通过手动分配分片到特定的节点解决。


```html
POST _cluster/reroute
{
    "commands":[
        {
            "move":{
                "index": "index_name",
                "shard": 0,
                "from_node": "node_name_1",
                "to_node":"node_name_2"
            }
        }
    ]
}
```

### 从集群中移除一个节点

应用场景：

想要维护某一个节点，需要把节点移除出集群。同时又不希望导致集群的颜色变黄或变红

```html
PUT _cluster/settings
{
    "transient": {
        "cluster.routing.allocation.exclude._ip":"the_ip_of_you_node"
    }
}
```

### 控制Allocation和Recovery的速率

```html
# change the number of moving shards to balance the cluster
PUT _cluster/settings
{
    "transient": {
        "cluster.routing.allocation.cluster_concurrent_rebalance": 2
    }
}

# change the number of shards being recovered simultanceously per node
PUT _cluster/settings
{
    "transient": {
        "cluster.routing.allocation.node_concurrent_recoveries": 5
    }
}

#change the reocvery speed
PUT _cluster/setttings
{
    "transient": {
        "indices.recovery.max_bytes_per_sec": "80mb"
    }
}

#change the number of concurrent streams for a recovery on a single node
PUT _cluster/settings
{
    "transient": {
        "indices.recovery.concurrent_streams": 6
    }
}
```

### Synced Flush

使用场景： 需要重启一个节点

通过synced flush ,可以再索引上放置一个sync id。这样可以提供这些分片Recovery的时间

```html
POST _flush/synced
```

### 清空节点上的缓存

使用场景：

节点上出现了高内存占用。可以执行清除缓存的操作。这个操作会影响集群的性能，但是会避免你的集群出现OOM的问题。

```html
POST _cache/clear
```

### 控制搜索的队列

使用场景：

当搜索的相应时间过长，看到有"reject"指标的增加，都可以适当增加该数值

```html
PUT _cluster/setttings
{
    "transient":{
        "threadpool.search.queue_size": 2000
    }
}
```

### 设置Circuit Beaker

使用场景：

设置各类Circuit Breaker。避免OOM的发生。

```html
PUT _cluster/settings
{
    "persisent": {
        "indices.breaker.total.limit": "40%"
    }
}
```