## Search Template和Index Alias
-----

### 1. Search Template

主要用途是： 解耦程序 和 搜索DSL

可以利用Search Template的模板语言把搜索参数化，这样就可以给不同的人用了

```html
#定义Search Template
POST _scripts/tmdb
{
    "script":{
        "lang":"mustache",
        "source":{
            "_source":["title","overview"],
            "size":20,
            "query":{
                "multi_match":{
                    "query": "{{q}}",
                    "fields":["title","overview]
                }
            }
        }
    }
}

#应用参数
POST tmdb/_search/template
{
    "id":"tmdb",
    "params":{
        "q": "basketball with cartoon aliens"
    }
}
```


### 2. Index Alias实现零停机运维

```html
POST _aliases
{
    "actions":[
        {
            "add":{
                "index":"comments-2019-03-15",
                "alias": "comments-today"       //为索引定义一个别名
            }
        }
    ]
}

#通过别名读写数据
PUT comments-today/_doc/1
{
    "movie":"The Matrix",
    "rating":"5",
    "comment": "Neo is the one!"
}

#创建别名的时候同时创建一个过滤器
POST _aliases
{
    "actions":[
        "add":{
            "index":"movies-2019a",
            "alias": "movies-latest-highrate",
            "filter": {
                "range":{
                    "rating":{
                        "gte":4
                    }
                }
            }
        }
    ]
}


#可以实现不修改程序读写不同索引
```