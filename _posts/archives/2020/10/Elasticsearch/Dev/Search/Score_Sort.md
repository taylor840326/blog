## 算分与排序
-----

### 1. 概要

Elasticsearch默认会以文档的相关度算分进行排序

可以通过指定一个或者多个字段进行排序

使用相关度算分排序，不能满足特定条件

无法针对相关度，对排序实现更多控制。


### Function Score Query

可以在查询结束后，对每一个匹配的文档进行一系列的重新算分，根据新生成的分数进行排序

提供了集中默认的计算分值的函数

1. Weight: 为每一个文件设置一个简单而不被规范化的权重
1. Field Value Factor: 使用该数值来修改_score，例如将“热度”和“点赞数”作为算分的参考因素
1. Random Score: 为么一个用户使用一个不同的，随机算分结果
1. 衰减函数： 以某个字段的值为标准，距离某个值越近，得分越高
1. Script Score: 自定义脚本完全控制所需逻辑

```html
PUT /blogs/_doc/1
{
    "title": "About popularity",
    "content": "In this post we will talk about ...",
    "votes": 6
}

POST /blogs/_search
{
    "query": {
        "function_score":{
            "query":{
                "multi_match":{
                    "query": "popularity",
                    "fields": ["title","content"]
                }
            },
            "field_value_factor":{
                "field": "votes",
                "factor": 0.1       //引入factor后。新的算分 = 老的算分 * log(1 + factor * 投票数)
            }
        }
    }
}


新的算分= 老的算分 * 投票数

1. 投票数为0
1. 投票数很大时
```