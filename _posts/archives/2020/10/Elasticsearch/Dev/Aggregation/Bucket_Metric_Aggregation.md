## Bucket && Metric 聚合分析及嵌套聚合

-----


### Aggregation语法

Aggregation数据Search的一部分。

一般情况下，建议将其Size指定为0，这样返回的结果中只包含聚合的结果。

```text
"aggregations" : {                                          //和Query同级的关键字
    "<aggregation_name>" : {                                //自定义的聚合名字
        "<aggregation_type>" : {
            <aggregation_body>                              //聚合的定义：不同的Type+Body
        }
        [,"meta" : {  [<meta_data_body>] } ]?
        [,"aggregations" : { [<sub_aggregation>]+ } ]?      //子聚合查询
    }
    [,"<aggregation_name_2>" : { ... } ]*                   //可以包含多个同级的聚合查询
}
```

### Metric Aggregation

*单值分析*

只输出一个分析结果

min/max/avg/sum

Cardinality(类似云Distinct Count)

**多值分析**

输出多个分析结果

stats/ extended stats

percentile/ percentile rank

top hits (排在前面的示例)


### Terms Aggregation

字段需要打开fielddata,才能进行Terms Aggregation

keyword默认支持doc_values

Text需要再Mapping中enable，会按照分词后的结果进行分组

**Demo**

对job和job.keyword进行聚合

对性别进行Terms聚合

指定bucket size

```html
# 对keyword进行聚合
POST employees/_search
{
    "size":0,
    "aggs": {
        "jobs":{
            "terms":{
                "field": "job.keyword"
            }
        }
    }
}

#对Text字段进行terms聚合查询，失败
#因为job字段是text类型，需要开启fielddata
POST employees/_search
{
    "size":0,
    "aggs":{
        "jobs":{
            "terms":{
                "field": "job"
            }
        }
    }
}

#开启fielddata
PUT employees/_mapping
{
    "properties":{
        "job": {
            "type":"text",
            "fielddata":true
        }
    }
}

#对text字段进行terms分词。分词后的terms
POST employees/_search
{
    "size":0,
    "aggs": {
        "jobs": {
            "terms":{
                "field": "job"
            }
        }
    }
}

POST employees/_search
{
    "size":0,
    "aggs": {
        "jobs": {
            "terms":{
                "field": "job.keyword"      //因为有keyword，就不会分词了。
            }
        }
    }
}

#对job.keyword和job进行terms聚合，分桶的总数并不一样
POST employees/_search
{
    "size":0,
    "aggs":{
        "cardinate":{           //计算唯一值
            "cardinality": {
                "field": "job.keyword"
            }
        }
    }
}
```

### Bucket Size & Top Hits Demo

当获取分桶后，桶内最匹配的顶部文档列表

Size： 按年龄分桶，找出指定数据量的分桶信息

Top Hits: 查看哥哥工种中，年纪最大的3名员工

```html
POST employees/_search
{
    "size":0,
    "aggs": {
        "jobs":{
            "terms": {
                "field": "job.keyword"
            },
            "aggs": {
                "old_employee":{
                    "top_hits":{
                        "size":3,
                        "sort": [
                            {
                                "age":{
                                    "order": "desc"
                                }
                            }
                        ]
                    }
                }
            }
        }
    }
}
```

### 优化terms聚合的性能

当聚合非常频繁，而索引在不断的写入。

```html
PUT index
{
    "mappings": {
        "properties": {
            "foo": {
                "type": "keyword",
                "eager_global_ordinals": true           //每当有新数据进来的时候，这个term就会被加载到cache当中。
            }
        }
    }
}
```

### Range && Histogram 聚合

按照数字的范围，进行分桶

在Range Aggregation中，可以自定义key

Demo：

按照工具的Range分桶

按照工资的间隔(Histogram)分桶

```html
POST employees/_search
{
    "size":0, 
    "aggs": {
        "salary_range": {
            "range": {
                "field":"salary",
                "ranges":[
                    {
                        "to":10000          // 0 to 10000
                    },
                    {
                        "from":10000,       // 10000 to 20000
                        "to":20000
                    },
                    {
                        "key": ">20000",    //指定key名称  
                        "from": 20000       // > 20000
                    }
                ]
            }
        }
    }
}

#Salary Histogram，工资0到10万，以5000一个区间进行分桶
POST employees/_search
{
    "size":0,
    "aggs": {
        "salary_histogram": {
            "histogram": {
                "field": "salary",
                "interval": 5000,
                "extended_bounds":{
                    "min":0,
                    "max":100000
                }
            }
        }
    }
}
```

```html
#嵌套查询1，按照工作类型分桶，并同级工资信息
POST employees/_search
{
    "size":0,
    "aggs": {
        "Job_salary_stats":{
            "terms": {
                "field":"job.keyword"
            },
            "aggs": {
                "salary":{
                    "stats": {
                        "field":"salary"
                    }
                }
            }
        }
    }
}

#多次嵌套。根据工作类型分桶，然后按照性别分桶，计算工资的统计信息
POST employees/_search
{
    "size":0,
    "aggs": {
        "Job_gender_stats":{
            "terms": {
                "field":"job.keyword"
            },
            "aggs": {
                "gender_stats": {
                    "terms":{
                        "field": "gender"
                    },
                    "aggs": {
                        "salary_stats": {
                            "stats": {
                                "field": "salary"
                            }
                        }
                    }
                }
            }
        }
    }
}
```