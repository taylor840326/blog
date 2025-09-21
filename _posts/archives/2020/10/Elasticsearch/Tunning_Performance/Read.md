## 提升集群读性能

-----

### 1. 尽量Denormalize数据

Elasticsearch不等于关系型数据库

尽可能不要使用嵌套类型的数据(Denormalize)，从而获取最佳的性能

使用Nested类型的数据。查询速度会慢几倍

使用Parent/ Child关系。查询速度会慢几百倍


### 2. 数据建模

尽量将数据先行计算，然后保存到Elasticseach中。尽量避免查询时的Script计算

尽量使用Filter Context，利用缓存机制减少不必要的算分

集合profile，explain API分析慢查询的问题，持续优化数据模型

**严禁使用`*`开头通配符的Terms查询**

### 3. 优化示例

### 3.1. 避免查询时脚本

```html
PUT blogs/_doc/1
{
    "title":"elasticsearch"
}

GET blogs/_search
{
    "query": {
        "bool":{
            "must":[
                "match":{
                    "title": "elasticsearch"
                }
            ],
            "filter":{
                "script":{
                    "script":{
                        "source": "doc['title.keyword'].value.length()>5"       //数据写入的过程中就对数据长度进行一个计算
                    }
                }
            }
        }
    }
}
可以在index文档时，使用Ingest Pipeline计算并写入某个字段
```

### 3.2. 使用Query Context

```html
GET blogs/_search
{
    "query": {
        "bool": {
            "must": [
                {
                    "match": {
                        "title": "elasticsearch"
                    }
                },
                {
                    "range": {
                        "publish_date": {
                            "gte":2017,
                            "lte":2019
                        }
                    }
                }
            ]
        }
    }
}

以上查询无法使用缓存

GET blogs/_search
{
    "query": {
        "bool":{
            "must": {
                {"match": {"title": "elasticsearch"}}.
                "filter": {             //可以使用到elasticsearhc的cache
                    "range": {
                        "publish_date": {
                            "gte": 2017,
                            "lte":2019
                        }
                    }
                }
            }
        }
    }
}
```

### 3.3. 聚合文档消耗内存

聚合查询会消耗内存，特别时针对很大的数据集进行聚合运算。

如果可以控制聚合的数量，就能减少内存的开销

当需要使用不同的Query Scopt，可以使用Filter Bucket

```html
GET blogs/_search
{
    "size":0,
    "query": {
        "bool": {
            "filter": {
                "range": {
                    "runtime": {
                        "lte": 100
                    }
                }
            }            
        }
    },
    "aggs": {
        "myagg":{
            "range":{
                "field":"",
                "ranges":[
                    "from": 50,
                    "to": 100
                ]
            }
        }
    }
}
```

### 3.4. 使用通配符的表达式

通配符开头的正则，性能非常糟糕。需要避免使用

```html
GET blogs/_search
{
    "query": {
        "wildcard": {
            "title": {
                "value": "*elastic*"
            }
        }
    }
}
```

### 3.5. 优化分片

**避免Over Sharing**

一个查询需要访问每一个分片，分片过多。会导致不必要的查询开销

**结合应用场景，控制单个分派你的尺寸**

Search: 20GB

Loggin: 40GB

**Force-merge Read_only索引**

使用基于时间序列的索引，将只读的索引进行force merge，减少segment的数量


