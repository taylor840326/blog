## 文档CRUD和Index Mapping
-----

除了CRUD操作外，Elasticsearch还提供了bulk、mget和msearch等操作。从提升性能的角度上说，建议使用。但是，单次操作的数据量不要过大，以免引发性能问题。

每个索引都有一个Mapping定义，包含文档的字段和类型、字段的Analyzer的相关配置。

Mapping可以被动态的创建，为了避免一些错误的类型推算或者满足特定的需求，可以显示的定义Mapping。

Mapping可以动态创建，也可以显示定义。你可以在Mapping中定制Analyzer。

你可以为字段指定定制化的analyzer，也可以为查询字符串指定search_analyzer

Index Template可以定义Mapping和Settings，并自动的应用到新创建的索引之上，建议要合理的使用Index Template

Dynamic Template支持在具体的索引上指定规则，为新增的字段指定相应的Mappings。