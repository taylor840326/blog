## 搜索建议

-----

### 1. 概要

现在的搜索引擎，一般都会提供Sugget as your type的功能

帮助用户在输入搜索的过程中，进行自动不全或者纠错。通过协助用户输入更加精准的关键词，提高后续钩锁阶段文档匹配的程度。

在google上搜索，一开始会自动不全。当输入导一定长度，如因为单词拼写错误无法补全，就会开始提示相似的此或者句子。


搜索引擎这样的功能在Elasticsearch中是通过Suggester API的方式实现的。

原理： 将输入的文本分解为Token，然后在索引的字典里查找相似的term并返回

根据不同的场景，Elasticsearch设计了4中类别的Suggesters：

1. Term && Phrase Suggester
1. Complete && Context Suggester


### Term Suggester

Suggester是一种特殊类型的搜索。text里是调用时候提供的文本，通常来自于用户界面上用户输入的内容

用户输入的lucen是一个错误的拼写

回到指定的字段body上搜索，当无法搜索导结果时返回建议的词

```html
POST /articles/_search
{
    "size":1,
    "query": {
        "match":{
            "body": "lucen rock"
        }
    },
    "suggest": {
        "term-suggestion":{
            "text": "lucen rock",
            "term": {
                "suggest_mode": "missing",
                "field":"body"
            }
        }
    }
}
```

### Term Suggester - Missing Mode

1. Missing - 如索引中已经存在，就不提供建议
1. Popular - 推荐出现频率更加高的词
1. Always - 无论是否存在，都提供建议


### Phrase Suggester

Phrase Suggester在Term Suggester上增加了一些额外的逻辑

新加了一些参数：

1. Suggest Mode: missing,popular,always
1. Max Errors: 最多可以拼错的terms数
1. Confidence:限制返回结果数，默认为1


### 示例

```html
POST articles/_bulk
{"index":{}}
{"body":"lucene is very cool"}
{"index":{}}
{"body":"Elasticsearch builds on top of lucene"}
{"index":{}}
{"body":"Elasticsearch rocks"}
{"index":{}}
{"body":"elastic is the company behind ELK stack"}
{"index":{}}
{"body":"Elk stack rocks"}
{"index":{}}
{"body":"elasticsearch is rock solid"}

POST /articles/_search
{
    "size":1,
    "query":{
        "match": {
            "body":"lucen rock
        }
    },
    "suggest":{
        "term-suggestion":{
            "text": "lucen rock",
            "term": {
                "suggest_mode": "missing",
                "field":"body"
            }
        }
    }
}

POST /articles/_search
{
    "suggest":{
        "term-suggestion":{
            "text": "lucen rock",
            "term": {
                "suggest_mode": "popular",
                "field": "body"
            }
        }
    }
}

POST /articles/_search
{
    "suggest": {
        "term-suggestion":{
            "text":"lucen rock",
            "term": {
                "suggest_mode":"always",
                "field":"body"
            }
        }
    }
}

POST /articles/_search
{
    "suggest": {
        "term-suggestion":{
            "text":"lucen rock",
            "term": {
                "suggest_mode":"always",
                "field":"body",
                "prefix_length":0,
                "sort": "frequency"
            }
        }
    }
}

POST /articles/_search
{
    "suggest": {
        "my-suggestion":{
            "text":"lucne and elasticsear rock",
            "phrase": {
                "field":"body",
                "max_errors":2,
                "confidence":0,
                "highlight": {
                    "pre_tag": "<em>",
                    "post_tag": "</em>"
                }
            }
        }
    }
}
```
