## 单字符串多字段

-----

### 1. 应用场景

### 1.1. 最佳字段(Best Fields)

当字段之间相互竞争，又相互关联。例如title和body这样的字段。评分来自最匹配字段

### 1.2. 多数字段(Most Fields)

处理英文内容时： 一种常见的手段是，在主字段(English Analyzer)，抽取词干、加入同义词，以匹配更多的文档。

相同的文本，加入子字段(Standard Analyzer)，以提供更加精确的匹配，其他字段作为匹配文档提高相关度的信号

匹配字段越多越好

### 1.3. 混合字段(Cross Field)

对于某些实体，例如人名、地址、图书信息。

需要在多个字段中确定信息，单个字段只能作为整体的一部分。

希望在任何这些列出的字段中找到尽可能多的词。

### 示例

```html
PUT /blogs/_doc/1
{
  "title":"Quick brown rabbits",
  "body":"Brown rabbits are commonly seen."
}

PUT /blogs/_doc/2
{
  "title":"Keeping pets healthy",
  "body":"My quick brown fox eats rabbits on a regular basis."
}

#从相关性来说，文档2的评分应该更高。但结果确实文档1更高
POST /blogs/_search
{
  "query": {
    "bool": {
      "should": [
        {
          "match": {
            "title": "Brown fox"
          }
        },
        {
          "match": {
            "body": "Brown fox"
          }
        }
      ]
    }
  }
}
```

上例中，title和body相互竞争。但是不应该将分数简单叠加，二十应该找到单个最佳匹配的字段的评分。

Disjunction Max Query 查询

将任何与任一查询匹配的文档作为结果返回。采用字段上最匹配的评分最终评分返回。

```html

GET /blogs/_search
{
  "query": {
    "dis_max": {
      "tie_breaker": 0.7,
      "boost": 1.2,
      "queries": [
        {"match": {
          "title": "Quick fox"
        }},
        {
          "match": {
            "body": "Quick fox"
          }
        }
      ]
    }
  }
}
```

通过Tie Broker参数调整

1. 获得最佳匹配语句的评分 _score
1. 将其他匹配语句的评分与tie_breaker相乘
1. 对以上评分求和并规范化

Tie Breaker是一个介于0-1之间的浮点数。0代表使用最佳匹配；1代表所有语句同等重要。



### Multi Match Query

Best Fields 是默认类型，可以不用指定

Minimum should match等参数可以传递生成的query中

```html
GET /blogs/_search
{
  "query": {
    "multi_match": {
      "type": "best_fields", 
      "query": "Quick pets",
      "fields": ["title","body"],
      "tie_breaker": 0.2,
      "minimum_should_match": "20%"
    }
  }
}
```

