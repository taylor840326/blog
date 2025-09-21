## Relevance相关行和相关性算分
-----

相关性 - Relevance

搜索的相关性算分,描述了一个**文档**和**查询语句**匹配的程度。ES会对每个匹配的查询条件的结果进行算分_score

打分的本质时排序，需要把最符合用户需求的文档排在前面。ES5之前，默认的相关性算分采用TF-IDF，现在采用BM25


### 词频TF

词频Term Frequency：检索词在一篇文档中出现的频率。检索词出现的次数除以文档的总字节数

度量一条查询和结果文档相关性的简单方法：简单将搜索中每一个词的TF进行相加

Stop Word。例如”的“这个词在文档中出现了很多次，但是对贡献相关度几乎没有用处，不应该考虑他们的TF。

### 逆文档频率IDF

DF: 检索词在所有文档中出现的频率

IDF：简单说 = Log(全部文档数/检索词出现过的文档总数)

TF-IDF本质上就是将TF求和变成了**加权求和**

TF-IDF被公认为时信息检索领域最重要的发明

除了在信息检索，在文献分类和其他相关领域都有着非常广泛的应用

IDF的概念，最早是剑桥大学的“斯巴克.琼斯”提出的。

1970、1980年代萨尔顿和罗宾逊进行了进一步的证明和研究，并用香农信息论做了证明

相待搜索引擎，对TF-IDF进行了大量细微的优化


### 示例

```html
PUT testscore/_bulk
{"index":{"_id":1}}
{"content":"we use Elasticsearch to power the search"}
{"index":{"_id":2}}
{"content":"we like elasticsearch"}
{"index":{"_id":3}}
{"content":"The scoring of documents is caculated by the scoring formula"}
{"index":{"_id":4}}
{"content":"you know, for search"}

GET /testscore/_search
{
  "explain": true,
  "query": {
    "match": {
      //"content":"you"
      "content": "elasticsearch"
      //"content":"the"
      }
  }
}

```

### Boosting Relevance影响打分的权重

Boosting是控制相关度的一种手段： 索引、字段或者查询子条件

Boosting参数的含义：

1. 当boost > 1 时，打分的相关度相对性提升
1. 当0 < boost < 1时，打分的权重相对性降低
1. 当boost < 0时，贡献负分

```html

POST testscore/_search
{
  "query": {
    "boosting": {
      "positive": {
        "term": {
          "content": {
            "value": "elasticsearch"
          }
        }
      },
      "negative": {
        "term": {
          "content": {
            "value": "like"
          }
        }
      },
      "negative_boost": 0.2
    }
  }
}
```