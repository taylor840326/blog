## 分片原理
-----

### 什么是ES的分片

ES中最小的工作单元， 是一个Lucene的Index

问题：

1. 为什么ES的搜索是近实时的(1秒后被搜到)
1. ES如何保证在断电时数据也不会丢失
1. 为什么删除文档，并不会立刻释放空间

### 倒排索引不可变性

倒排索引采用Immutable Design，一旦生成，不可更改

不可变性带来的好处如下：

1. 无需考虑并发写文件的问题，避免了锁机制带来的性能问题
1. 一旦读入内核的文件系统缓存，便留在那里。只要文件系统存有足够的空间，大部分请求就会直接请求内存，不会命中磁盘，提升了很大的性能。
1. 缓存容易生成和维护，数据可以被压缩

不可变更性，带来了挑战：如果需要让一个新的文档可以被搜索，需要重建整个索引。

### 什么时Refresh

将Index Buffer写入Segment的过程叫Refresh。Refresh不执行fsync操作。

Refresh频率： 默认1秒发生一次，可通过index.refresh_interval配置。Refresh后，数据就可以被搜索到了。这也是为什么Elasticsearch被称为近实时搜索

如果系统有大量的数据写入，那就会产生很多的Segment

Index Buffer被占满时，会出发Refresh，默认值时JVM的10%。

### 什么是TransactionLog

Segment写入磁盘的过程相对耗时，借助文件系统缓存，Refresh时，先将Segemnt写入缓存以开发查询

为了保证数据不会丢失。所以在Index文档时，同时写TransactionLog，高版本开始TransactionLog默认落盘。每个分片有一个TransactionLog文件

在ES Refresh时，IndexBuffer被清空，TransactionLog不会清空

### 什么是Flush

调用Refresh，Index Buffer清空并且Refresh

调用fsync，将缓存中的Segments写入磁盘

清空TransactionLog

默认30分钟调用一次

TransactionLog慢（默认512MB）

### Merge

Segment很多，需要被定期合并。合并后可以减少Segments。删除已经标记为删除的文档。

ES和Lucene会自动执行Merge操作。  

```html
POST my_index/_forcemerge
```