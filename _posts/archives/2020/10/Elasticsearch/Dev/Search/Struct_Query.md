### 结构化搜索

-----

### 1. 结构化数据

结构化搜索(Structured Search)是指对结构化数据的搜索

日期、布尔类型和数字都是结构化的

文本也可以是结构化的：

1. 如彩色笔可以有离散的颜色集合：红red、绿green、蓝blue
1. 一个博客可能被标记了标签。例如：分布式Distributed和搜索Search
1. 电商网站上的商品都有UPCs（通用产品码Universal Product Codes）或其他的唯一标识，他们都需要遵循严格规定的，结构化的格式


### 2. ES中的结构化搜索

布尔、时间、日期和数字这类结构化数据：有精确的格式，可以对这些格式进行逻辑操作。

包括比较数字或时间的范围，或判定两个值大小。

结构化的文本可以做精确匹配或者部分匹配：Term查询 /  Prefix前缀查询

结构化结果只有“是”或“否”两个值。根据场景需要，可以决定结构化搜索是否需要打分。

```html

#结构化搜索，精确匹配
DELETE products

POST /products/_bulk
{"index":{"_id":1}}
{"price":10,"avaliable":true,"date":"2018-01-01","productID":"XHDK-A-1293-#fJ3"}
{"index":{"_id":2}}
{"price":20,"avaliable":true,"date":"2019-01-01","productID":"KDKE-B-9947-#kL5"}
{"index":{"_id":3}}
{"price":30,"avaliable":true,"productID":"JODL-X-1937-#pV7"}
{"index":{"_id":4}}
{"price":30,"avaliable":false,"productID":"QQPX-R-3956-#aD8"}


GET /products/_mapping

{
  "products" : {
    "mappings" : {
      "properties" : {
        "avaliable" : {
          "type" : "boolean"
        },
        "date" : {
          "type" : "date"
        },
        "desc" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        },
        "price" : {
          "type" : "long"
        },
        "productID" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        }
      }
    }
  }
}



##对布尔值match查询，有算分
GET /products/_search
{
  "profile": "true",
  "explain": true,
  "query": {
    "term": {
      "avaliable": {
        "value": true
      }
    }
  }
}

#对布尔值，通过constant score转成filtering，没有算分
GET /products/_search
{
  "profile": "true",
  "explain": true,
  "query":{
    "constant_score": {
      "filter": {
        "term": {
          "avaliable": true
        }
      },
      "boost": 1.2
    }
  }
}


#数字类型term
GET /products/_search
{
  "profile": "true",
  "explain": true,
  "query": {
    "term": {
      "price": {
        "value": 10
      }
    }
  }
}

#数字类型range查询
GET /products/_search
{
  "profile": "true",
  "explain": true,
  "query": {
    "constant_score": {
      "filter": {
        "range": {
          "price": {
            "gte": 10,
            "lte": 30
          }
        }
      },
      "boost": 1.2
    }
  }
}


#日期range查询
GET /products/_search
{
  "query": {
    "constant_score": {
      "filter": {
        "range": {
          "date": {
            "gte": "now-1y"
          }
        }
      },
      "boost": 1.2
    }
  }
}

# 年 -> y
# 月 -> M
# 周 -> w
# 天 -> d
# 小时 -> H/h
# 分钟 -> m
# 秒 -> s

#exists数值
GET /products/_search
{
  "query": {
    "constant_score": {
      "filter": {
        "exists": {
          "field": "date"
        }
      },
      "boost": 1.2
    }
  }
}


#处理多值字段
POST /movies/_bulk
{"index":{"_id":1}}
{"title":"Father of the Bridge Part II","year":1995,"genre":"Comedy"}
{"index":{"_id":2}}
{"title":"Dave","year":1993,"genre":["Comedy","Romance"]}


#处理多值字段，term查询时包含，而不是等于
GET /movies/_search
{
  "query": {
    "constant_score": {
      "filter": {
        "term": {
          "genre.keyword": "Comedy"
        }
      },
      "boost": 1.2
    }
  }
}
#可以添加一个count字段，结合布尔判断做到精确匹配

```