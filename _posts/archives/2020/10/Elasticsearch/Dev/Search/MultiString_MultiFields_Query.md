## 多字符串多字段的查询
-----

在Elasticsearch中，有Query和Filter两种不同的Context

1. Query Context: 相关性算分
1. Filter Context: 不需要算分(Yes Or No)，可以利用Cache,获得更好的性能

### Bool查询

一个Bool查询，是一个或者多个查询子句的组合

相关性并不只是全文本检索的专利。也适用于yes|no子句。匹配的子句越多，相关性评分越高。

如果多条查询子句被合并为一条复合查询语句，比如bool查询。则每个查询子句计算得出的评分会被合并到总的相关性评分中。

|子句关键字| 作用|
|:---|---:|
|mush|必须匹配。贡献算分|
|should|选择性匹配,只要有一个条件满足。贡献算分|
|must_not|Filter Context查询子句，必须不能匹配|
|filter| Filter Context必须匹配，但是不贡献算分|


### 示例

```html
GET products/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "term": {
            "price": {
              "value": "30"
            }
          }
        }
      ],
      "filter": [
        {
          "term": {
            "avaliable": "true"
          }
        }
      ],
      "must_not": [
        {
          "range": {
            "price": {
              "lte": 10
            }
          }
        }
      ],
      "should": [
        {
          "term": {
            "productID.keyword": {
              "value": "XHDK-A-1293-#fJ3"
            }
          }
        }
      ]
    }
  }
}
```
