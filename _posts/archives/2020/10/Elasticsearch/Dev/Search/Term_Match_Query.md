## Term查询和全文本查询Match
-----

term的重要性：

term是表达语义的最小单位。搜索和利用统计语言模型进行自然语言处理都需要处理term


特点：

1. Term level Query: Term Query / Range Query / Exists Query / Prefix Query / Wildcard Query
1. 在ES中，Term查询**对输入不做分词**。会将输入作为一个整体，在倒排索引中查找准确的词项，并且使用相关度算分公式为每个包含该词项的文档进行**相关度**算分- 例如 “Apple Store”
1. 可以通过Constant Score将查询转换成一个Filtering避免算分，并利用缓存提高性能


### 1. 示例

```html
DELETE products

PUT products
{
    "settings":{
        "number_of_shards": 1
    }
}

POST /products/_bulk
{"index":{"_id":1}}
{"productID":"XHDK-A-1293-#fJ3","desc":"iPhone"}
{"index":{"_id":2}}
{"productID":"KDKE-B-9947-#kL5","desc":"iPad"}
{"index":{"_id":3}}
{"productID":"JOKL-X-1937-#pV7","desc":"MBP"}

GET /products

POST /products/_search
{
    "query":{
        "term":{
            "desc":{
                "value":"iPhone"    //查询不到。因为默认standard分词后把单词改成小写了。
                "value":"iphone"    //可以查询的到。
            }
        }
    }
}

```


### 多字段Mapping和Term查询

```html
GET /products/_mapping
{
    "products":{
        "mappings":{
            "properties":{
                "desc": {
                    "type":"text",
                    "fields":{
                        "keyword":{
                            "type":"keyword",
                            "ignore_above":256
                        }
                    }
                },
                "productID":{
                    "type":"text",
                    "fields":{
                        "keyword":{
                            "type":"keyword",
                            "ignore_above":256
                        }
                    }
                }
            }
        }
    }
}

POST /products/_serach
{
    "query":{
        "term":{
            "productID.keyword":{       //强制严格匹配，不走分词
                "value": "XHDK-A-1293-#fJ3"
            }
        }
    }
}

    "max_score": 0.9808292,
    "hits":[
        {
            "_index":" products",
            "_type":"_doc",
            "_id":"1",
            "_score": 0.9808292,
            "_source":{
                "productID": "XHDK-A-1293-#fJ3",
                "desc":"iPhone"
            }
        }
    ]
```


### 符合查询 - Constant Score转为Filter

将Query转成Filter，忽略TF-IDF计算，避免相关性算分得开销

Filter可以有效利用缓存

跳过结果得算分

```html
POST /products/_search
{
    "explain":true,
    "query":{
        "constant_score":{
            "filter":{
                "term":{
                    "productID.keyword":"XHDK-A-1293-#fJ3"
                }
            }
        }
    }
}
```

### 基于全文得查询

基于全文得查询： Match Query/ Match Phrase Query/ Query String Query

特点：

1. 索引和搜索时都会进行分词，查询字符串先传递到一个合适得分词器，然后生成一个供查询得词项列表
1. 查询得时候，先回对输入得查询进行分词，然后每个词项逐个进行底层得查询，最终将结果进行合并。并为每个文档生成一个算分。
例如：查“Matrix reloaded”，会查到包括Matrix或者reloaded得所有结果。

