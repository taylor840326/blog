

### 2. 实战

### 2.1. 叶子查询子句

叶子查询子句在特定字段中查找特定值，例如match，term或range查询。 这些查询可以自己使用。

### 2.2. 复合查询子句

复合查询子句包装其他叶查询或复合查询，并用于以逻辑方式组合多个查询（例如bool或dis_max查询），或更改其行为(例如constant_score查询)。

复合查询子句包含以下几种：

1. bool query
1. boosting query
1. constant_score query
1. dis_max query
1. function_score query

我们通常只会用到**bool**查询

查询子句的行为会有所不同，具体取决于它们是在查询上下文中(Query)还是在过滤器(Filter)上下文中使用。

### 3. 全文检索

在query context中，查询子句回答**此文档与该查询子句的匹配程度如何**的问题。

除了确定文档是否匹配外，查询子句还计算_score元字段中的相关性得分。

es的搜索结果也默认根据_score排名返回。

### 3.1. match

match是标准的全文检索

在匹配之前会先对查询关键字进行分词

可以指定分词器来覆盖mapping中设置的搜索分词器

首先**超级羽绒服**关键字先会被分词为**超级**、**羽绒服** ,然后再去es中查询与这两个分词相匹配的文档。

依据相关度即分值得到以下结果。

```json
GET idx_pro/_search
{
  "query": {
    "match": {
      "name": {
        "query": "超级羽绒服",
        "analyzer": "ik_smart"
      }
    }
  }
}
```

```text
冬天超级暖心羽绒服 冬天暖心羽绒服 冬日羽绒服 花花公子羽绒服 花花公子暖心羽绒服
```

不使用配置的话可以采用如下简写方式

```json
"query": {
  "match": {"name": "超级羽绒服"}
}
```

### 3.2. match_phrase

可以搜索分词相邻的结果，eg 根据**新疆苹果** 可以搜到**香甜新疆苹果**而搜不到**新疆香甜苹果**

可以使用slop指定两个匹配的token位置距离的最大值。

可以使用analyzer指定分词器，覆盖mapping中设置的search_analyzer

如下我们对**花花公子羽绒服**进行分词后发现，返回结果除了每个token之外，还拥有位置信息start_offset和end_offset。

位置信息可以被保存在倒排索引(Inverted Index)中，像match_phrase这样位置感知(Position-aware)的查询能够使用位置信息来匹配那些含有正确单词出现顺序的文档，且在这些单词之间没有插入别的单词。

```json
GET idx_pro/_analyze
{
  "text":"花花公子羽绒服",
  "analyzer" : "ik_smart"
}


{
  "tokens" : [
    {
      "token" : "花花公子",
      "start_offset" : 0,
      "end_offset" : 4,
      "type" : "CN_WORD",
      "position" : 0
    },
    {
      "token" : "羽绒服",
      "start_offset" : 4,
      "end_offset" : 7,
      "type" : "CN_WORD",
      "position" : 1
    }
  ]
}
```

如下是我们的样本数据

```text

冬日工装裤 花花公子帅气外套 花花公子外套 冬天暖心羽绒服 冬日羽绒服 花花公子羽绒服 花花公子暖心羽绒服
冬天超级暖心羽绒服
```

我们查询**超级羽绒服**搜索不到数据，因为没有**超级羽绒服**这样的短语存在。


```json
GET idx_pro/_search
{
  "query": {
    "match_phrase": {
      "name": "超级羽绒服"
    }
  }
}
```

搜索**暖心羽绒服**即可搜索到如下三个数据

因为**暖心羽绒服**被分词为**暖**、 **心**、 **羽绒服**三部分，搜索到的结果必须符合他们三个分词的位置紧挨着。

```json
GET idx_pro/_search
{
  "query": {
    "match_phrase": {
      "name": "暖心羽绒服"
    }
  }
}
```

```text
冬天暖心羽绒服 冬天超级暖心羽绒服 花花公子暖心羽绒服
```

我们在设置了slop后允许超级和羽绒服这两个分词后的token距离最大值为2，可以搜索到如下数据了。因为冬天超级暖心羽绒服分词结果为冬天，超级,暖，心，羽绒服，超级与羽绒服距离正好为2，所以能匹配到。

```json
GET idx_pro/_search
{
  "query": {
    "match_phrase": {
      "name": {
        "query": "超级羽绒服",
        "analyzer": "ik_smart",
        "slop": 2
      }
    }
  }
}
```

```text
冬天超级暖心羽绒服
```

## 3.3. Filter

其实准确来说，ES中的查询操作分为2种：查询（query）和过滤（filter）。

查询即是之前提到的query查询，它（查询）默认会计算每个返回文档的得分，然后根据得分排序。

而过滤（filter）只会筛选出符合的文档，并不计算得分，且它可以缓存文档。所以，单从性能考虑，过滤比查询更快。

换句话说，过滤适合在大范围筛选数据，而查询则适合精确匹配数据。

一般应用时，应先使用过滤操作过滤数据，然后使用查询匹配数据。

在Filter context中，查询子句回答问题“此文档是否与此查询子句匹配？”答案是简单的“是”或“否”，即不计算分数。

过滤器上下文主要用于过滤结构化数据，例如：

```text

该食品的生产日期是否在2018-2019之间
该商品的状态是否为"已上架"
```

**Ps：常用过滤器将由Elasticsearch自动缓存，以提高性能。**

## 4. bool组合查询

bool查询可以组合多种叶子查询，包含如下：

1. must：出现于匹配查询当中，有助于匹配度(_score)的计算
1. filter：必须满足才能出现，属于过滤，不会影响分值的计算，但是会过滤掉不符合的数据
1. should：该条件下的内容是应该满足的内容，如果符合会增加分值，不符合降低分值，不会不显示
1. must_not：满足的内容不会出现，与filter功能相反，属于过滤，不会影响分值的计算，但是会过滤掉不符合的数据

### 5. term-level query

我们可以使用term-level根据结构化的数据（例如ip、商品的id、价格等分词后无意义的数据）来精准查询文档，

与full-text全文检索不同，查询的关键字不进行分词，直接去es中匹配文档。

常见的term-level级别的查询有（其他查询请参考官网）：

### 4.1. term query

返回文档中精确包含关键字的文档，苏布尔贵族大米不会分词，直接去es中匹配文档

```json
GET idx_item/_search
{
  "query": {
    "term": {"title": "东北贵族大米"}
  }
}
```

### 4.2. terms query

相当于多个term查询

```json
GET idx_item/_search
{
  "query": {
    "terms": {"title": ["苏泊尔","小米"]}
  }
}
```

### 4.3. exists query

返回有name字段的文档，注意，如下情况将搜索不到文档：

该字段的值为null或者是[],空字符串是可以搜索到的""
该字段在mapping中设置了index:false
该字段长度超出了mapping中的ignore_above的设置
The field value was malformed and ignore_malformed was defined in the mapping

```json
GET idx_pro/_search
{
  "query": {
    "exists": {
      "field": "name"
    }
  }
}
```

### 4.4. range query

Returns documents that contain terms within a provided range.

```json
GET _search
{
    "query": {
        "range" : {
            "age" : {
                "gte" : "2019-12-10",
                "lte" : "2020-11-11",
                "format" : "yyyy-MM-dd"
            }
        }
    }
}
```

### 4.5. ids query

根据文档的_id返回文档

```json
GET /_search
{
    "query": {
        "ids" : {
            "values" : ["1", "4", "100"]
        }
    }
}
```

### 4.6. 示例#

must_not和filter用来过滤，而should是应该满足的条件，不是必须满足的条件，会影响分值的计算。

```json
GET idx_pro/_search
{
  "query": {
    "bool": {
      "must": [
        {    
          "match": {
            "name": "花花公子羽绒服"
          }
        }
      ],
      "must_not": [
        {
          "term": {
            "proId": 6
          }
        }
      ], 
      "should": [
        {
          "terms": {
            "name.keyword": ["花花公子暖心羽绒服", "花花公子外套"]
          }
        }
      ], 
      "filter": {
        "range": {
          "createTime": {
            "gte": "2019-12-12 17:56:56",
            "lte": "2019-12-19 17:56:56",
            "format": "yyyy-MM-dd HH:mm:ss"
          }
        }
      }
    }
  }
}
```


### 参考资料
作者： 海向

出处：https://www.cnblogs.com/haixiang/p/12095578.html

本站使用「CC BY 4.0」创作共享协议，转载请在文章明显位置注明作者及出处。

« 上一篇： 单例模式
» 下一篇： Java阻塞队列
posted @ 2019-12-25 11:08  海向  阅读(5119)  评论(6)  编辑  收藏

分类: ElasticSearch 标签: match与term查询, es搜索语法, ElasticSearch全文检索

💛原文地址为https://www.cnblogs.com/haixiang/p/12095578.html，转载请注明出处!
🍎es与SpringBoot的整合以及常用CRUD、搜索API已被作者封装,开箱即用效果很好,欢迎star谢谢!github
