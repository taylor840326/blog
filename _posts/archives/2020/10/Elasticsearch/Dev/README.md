## 总结

-----

### 1. 搜索与算分

**结构化搜索与非结构化搜索**

Term查询和基于全文本Match搜索的区别：

1. 当使用Term查询的时候，无论被查询的字段是text类型还是keyword类型。都不会做分词处理。
1. 当使用Match的时候，被查询的字段是text类型，会先分词再查询；当被查询字段是一个keyword类型，ES会先把查询转换成Term查询再搜索，所以不会做分词处理。

对于需要做精确匹配的字段、需要做聚合分析的字段，字段类型设置为Keyword

**Query Context v.s Filter Context**

Filter Context可以避免算分，并且利用缓存。性能由于Query Context

Boolean查询中Filter和Must Not都属于Filter Context

**搜索的算分**

TF-IDF

通过boosting值来调节ES的算分

**单字符串多字段查询(multi-match)**

Best_Field / Most_Fields / Cross_Field

**提高搜索的相关性**

多语言： 设置子字段和不同的分词器提升搜索的效果

推荐： Search Template分离代码逻辑和搜索DSL

多去监控用户的搜索的效果


**聚合**

Bucket / Metric / Pipeline

**分页**

From & Size / Search After / Scroll API

要避免深度分页，对于数据导出等操作，可以使用Scroll API


### 2. Elasticsearch的分布式模型

文档的分布式存储

文档通过Hash算法，route并存储到相应的分片


分片机器内部的工作机制

Segment / Transaction Logs / Refresh / Merge

分布式查询和聚合分析的内部机制

Query Then Fetch: IDF不是基于全局，而是基于分片计算。因此，数据量少的时候算分不准.

### 3. 数据建模的重要性

数据建模

1. ES如何处理管理关系
1. 数据建模的常见步骤
1. 建模的最佳实践

建模相关的工具

1. Index Template
1. Dynamic Template
1. Ingest Node
1. Update By Quyer
1. Reindex
1. Index Alias

最佳实践：

1. 避免过多的字段
1. 避免wildcard查询
1. 再Mapping中设置合适的字段

