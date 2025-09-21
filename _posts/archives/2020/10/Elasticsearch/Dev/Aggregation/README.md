## 聚合Aggregation
-----

Elasticsearch除了搜索以外，提供的针对ES数据进行统计分析的功能

ES的聚合提供实时性高、与Hadoop深度融合的特点

通过聚合，我们会得到一个数据的概览，是分析和总结全套的数据，而不是寻找单个文档。

ES的聚合性能高，只需要一条语句，就可以从Elasticsearch得到分析结果，无需在客户端自己去实现分析逻辑。


### 1. ES聚合的分类

Bucket Aggregation -> 一些列满足特定条件的文档的集合

Metric Aggregation -> 一些数学运算，可以对文档字段进行统计分析。计算最大值、最小值和平均值等。

Pipeline Aggregation -> 对其他的聚合结果进行二次聚合

Matrix Aggregation -> 支持多个字段的操作并提供一个结果矩阵


### 2. Bucket && Metric

```SQL
SELECT COUNT(brand)     -> Metric  执行一系列的统计方法
FROM cars
GROUP BY brand          -> Bucket  类似Group，对文档进行分组
```

**Bucket**

查看航班目的地的统计信息

```json
GET kibana_sample_data_flights/_search
{
  "size": 0,
  "aggs": {
    "flight_dest": {
      "terms": {
        "field": "DestCountry"
      }  
    }  
  }
}

{
  ...
  "hits": {
    "total": 123423,
    "max_score": 0.0,
    "hits": []
  },
  "aggregations": {
    "flight_dest": {
      "doc_count_error_upper_bound": 0,
      "sum_other_doc_count": 3187,
      "bucket": [
        {
          "key": "IT",
          "doc_count": 2371
        } ,
        {
          "key": "US",
          "doc_count": 1987
        } ,
        {
          "key": "CN",
          "doc_count": 1096
        } ,
        {
          "key": "CA",
          "doc_count": 944
        } 
      ]
    }
  }
}
```


**Metric**

Metric会基于数据集计算结果，除了支持在字段上进行计算，同样也支持在脚本(painless script)产生的结果上进行计算

大多数Metric是数学计算，仅输出一个值

min/max/sum/avg/cardinality

部分Metric支持输出多个数值

stats/percentiles/percentile_ranks

```html
GET kibana_sample_data_flights/_search
{
  "size": 0,
  "aggs": {
    "flight_dest": {
      "terms": {
        "field": "DestCountry"
      },
      "aggs":{
           "average_price":{
                "avg":{
                    "field":"AvgTicketPrice"
                }
            },
           "max_price":{
                "avg":{
                    "field":"AvgTicketPrice"
                }
            },
           "min_price":{
                "avg":{
                    "field":"AvgTicketPrice"
                }
            }
      }  
    }  
  }
}

{
  ...
  "hits": {
    "total": 123423,
    "max_score": 0.0,
    "hits": []
  },
  "aggregations": {
    "flight_dest": {
      "doc_count_error_upper_bound": 0,
      "sum_other_doc_count": 3187,
      "bucket": [
        {
          "key": "IT",
          "doc_count": 2371,
          "max_price":{value: 1199.729xxxx} 
          "min_price":{value: 100.729xxxx} 
          "average_price":{value: 595.729xxxx} 
        } ,
        ...
      ]
    }
  }
}
```

### 嵌套

```html
GET kibana_sample_data_flights/_search
{
  "size": 0,
  "aggs": {
    "flight_dest": {
      "terms": {
        "field": "DestCountry"
      },
      "aggs":{
           "average_price":{
                "avg":{
                    "field":"AvgTicketPrice"
                }
            },
            "weather":{
                "term":{
                    "field":"DestWeather"
                }
            }
            ...
      }  
    }  
  }
}

{
  ...
  "hits": {
    "total": 123423,
    "max_score": 0.0,
    "hits": []
  },
  "aggregations": {
    "flight_dest": {
      "doc_count_error_upper_bound": 0,
      "sum_other_doc_count": 3187,
      "bucket": [
        {
          "key": "IT",
          "doc_count": 2371,
          "weather":{
            "doc_count_error_upper_bound": 0,
            "sum_other_doc_count":0,
            "bucket":[
                {
                    "key":"Sunny",
                    "doc_count":209
                }
                ...
            ]
          }
          "max_price":{value: 1199.729xxxx} 
          "min_price":{value: 100.729xxxx} 
          "average_price":{value: 595.729xxxx} 
        } ,
        ...
      ]
    }
  }
}
```