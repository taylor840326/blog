## 搜索和聚合Aggregation
-----

Precosion指除了相关的结果，还返回了多少不相关的结果

Recall衡量有多少相关的结果，实际上并没有返回

精确值包括：数字、日期和某夕具体的字符串

全文本： 是需要被检索的非结构文本。
 
Analysis是将文本转换成倒排索引中的Terms的过程

Elasticsearch的Analyzer是Char Filter => Tokenizer => Token Filter的过程

要善于利用_analyze API去测试Analyzer

Elasticsearch搜索支持URI Search 和REST Body两种

Elasticsearch提供了Bucket/Metric/Pipeline/Matrix四种方式的聚合