## Update By Query && Reindex API
-----

### 1. 使用场景

一般在以下几种情况时，我们需要重建索引

1. 索引的Mapping发生变更： 字段类型更改、分词器和字典更新
1. 索引的settings发生变更： 索引的主分片数发生改变
1. 集群内，集群间需要做数据迁移

Elasticsearch的内置提供的API

1. Update By Query: 在现有的索引上重建
1. Reindex: 在其他索引上重建索引


### 2. Update By Query


### 3. Reindex