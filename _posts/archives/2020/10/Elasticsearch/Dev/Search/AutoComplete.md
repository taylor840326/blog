## 自动补全与基于上下文的提示
-----

Elasticsearch中通过Completion Suggester提供了自动补全的功能。

用户每输入一个字符，就需要即时发送一个查询请求导后端查找匹配项

对性能要求比较苛刻。Elastic search采用了不同的数据结构，并非通过倒排索引来完成。

而是将Analyze的数据编码成FST和索引一起存放。FST会被ES整个加载导内存，所以速度很快。

FST只能用于前缀查找


### 实现Completion Suggester的一些必要步骤

定义Mapping，使用completion类型

索引数据

运行suggest查询，得到搜索建议

```html
POST articles
{
    "mappings":{
        "properties": {
            "title_completion": {
                "type": "completion"
            }
        }
    }
}

写入数据
...

查询
POST articles/_search?pretty
{
    "size":0,
    "suggest": {
        "article-suggester": {
            "prefix":"e",
            "completion":{
                "field": "title_completion"
            }
        }
    }
}
```

### 什么时Context Suggester
 
 Completion Suggester的扩展
 
 可以在搜索中加入更多的上下文，例如，输入"star"
 1. 与咖啡相关。 建议"Starbucks"
 1. 与电影相关。 建议“star wars”
 
### 实现基于上下文感知的推荐Context Suggester

可以定义两种类型的Context

1. Category - 任意的字符串
1. Geo - 地理位置信息

实现Context Suggester的具体步骤

1. 定制一个Mapping
1. 索引数据，并且为每个文档加入Context信息
1. 结合Context进行Suggestion查询


```html
PUT comments/_mapping
{
    "properties": {
        "comment_autocomplete":{
            "type":"completion",
            "contexts": [
                {
                    "type":"category",
                    "name": "comment_category"
                }
            ]
        }
    }
}

POST comments/_doc
{
    "comment":"I love the star war movies",
    "comment_autocomplete":{
        "input": ["start wars],
        "contexts": {
            "comment_category": "movies"
        }
    }
}

POST comments/_doc
{
    "comment":"Where can I find a Starbucks",
    "comment_autocomplete":{
        "input": ["starbucks"],
        "contexts": {
            "comment_category": "coffee"
        }
    }
}

```