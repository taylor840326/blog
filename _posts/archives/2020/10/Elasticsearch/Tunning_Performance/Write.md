## 提升集群的写性能
-----

### 1. 概要

写性能优化的目标： 增大写吞吐量（Events Per Second)，越高越好

**客户端**

多线程，批量写

可以通过性能测试，确定最佳文档数量

多线程：需要观察是否有HTTP 429返回，实现Retry以及线程数量的自动调节

**服务端**

单个性能问题，往往是多个因素造成的。

需要先分解问题，在单个节点上进行调整并且结合测试，尽可能压榨硬件资源，以达到最高吞吐量

使用更好的硬件。观察CPU 、IO等使用情况

观察线程切换/ 堆栈的使用情况


### 2. 服务器端优化写入性能的一些手段

**降低IO操作**

使用ES自动生成的文档id

调整ES相关的配置。比如Refresh Interval

**降低CPU和存储开销**

减少不必要的分词

避免不需要的doc_values

文档的字段尽量保证相同的顺序，可以提高文档的压缩率

**尽可能做到写入和分片的负载均衡，实现水平扩展**

Shard Filtering 

Write Load Balancer

**调整Bulk线程池和队列**

### 3. 所有的优化一定要基于一个高质量的文档的建模

### 3.1. 关闭无关的功能

只需要聚合不需要搜索。Index设置成false

不需要算分。 Norms设置成false

不要对字符串使用默认的dynamic mapping。字段数量过多，会对性能产生比较大的影响。

index_options控制在创建倒排索引时，哪些内容会被添加到倒排索引中。优化这些设置，一定成都可以节约CPU

关闭_source，减少IO操作。适合指标型数据

```html
PUT my_index
{
    "mappings": {
        "properties": {
            "foo": {
                "type": "integer",
                "index": false
            }
        }
    }
}

PUT my_index
{
    "mappings": {
        "properties": {
            "foo": {
                "type": "text",
                "norms": false
            }
        }
    }
}
```

### 3.2. 针对性能的取舍

如果需要追求极致的写入速度，可以牺牲数据可靠性及搜索实时性以换取性能

**牺牲可靠性**

将副本分片设置为0，写入完毕再调整回去

**牺牲搜索实时性**

增加Refresh Interval的时间

**牺牲可靠性**

修改Translog的设置


### 4. 数据写入的过程

**Refresh**

将文档先保存再Index Buffer中，以refresh_interval为间隔时间，定期清空buffer，生成segment。

借助文件系统缓存特性，先将segment放在文件系统缓存中，并开放查询，以提升搜索的实时性

**Translog**

Segment没有写入磁盘，即便发生了宕机，重启后数据也能恢复。默认配置时**每次请求都会落盘**

**Flush**

删除旧的translog文件

生成Segment并写入磁盘，更新commit point并写入磁盘。ES自动完成，可优化点不多。


### 4.1. Refresh Interval

**降低Refresh的频率**

增加refresh_interval的数值，默认为1s。如果设置成-1，会急用自动refresh

这样的好处：
1. 避免过于频繁的refresh，而生成过多的segment文件。
1. 但是会降低搜索的实时性

增大静态配置参数indices.memory.index_buffer_size

默认时10%，会导致自动出发refresh


### 4.2. Translog

降低写磁盘的频率，但是会降低容灾能力

index.translog.durability: 默认时request，每个请求都落盘。设置成async异步写入

index.translog.sync_interval： 设置为60s，每分钟执行一次

index.translog.flush_threshod_size：默认512MB，可以适当调大。当translog超过该值，会出发flush


### 4.3. 分片设定

副本再写入时设为0，完成后再增加

合理设置主分片数，确保所有索引均匀分配在所有数据节点上

index.routing.allocation.total_shar_per_node：限定每个索引在每个节点上可分配的主分片数

分片设定示例：

5个节点的集群。索引有5个分片，1个副本。应该如何设置？

(5+5)/5 = 2

生产环境中要适当调大这个数字，避免有节点下线时，分片无法正常迁移。


### 4.4. Bulk，线程池和队列大小

**客户端**

单个bulk请求体的数据量不要太大，官方建议大约5-15mb

写入端bulk请求超时需要足够长，建议60s以上

写入端尽量将数据轮询打到不同节点。


**服务器端**

索引创建属于计算密集型任务，应该使用固定大小的线程池来配置。来不及处理的放入队列，线程数应该配置成CPU核心数+1，避免过多的上下文切换。

队列大小可以适当增加，不要过大，否则占用的内存会成为GC的负担


索引设定的示例：

```html
PUT myindex 
{
    "settings": {
        "index": {
            "refresh_interval": "30s",              //30秒一次Refresh
            "number_of_shards": "2"
        },
        "routing": {
            "allocation": {
                "total_shards_per_node": "3"        //控制分片，避免数据热点
            }
        },
        "translog":{
            "sync_interval": "30s",                 //降低translog落盘
            "durability": "async"
        },
        "number_of_replicas":0
    },
    "mappings": {
        "dynamic":false,                            //避免不必要的字段索引。必要时可以通过update by query索引必要的字段
        "properties":{
            ....
        }
    }
}
```