## Pipeline 聚合分析
-----

管道的概念： 支持对聚合分析的结果，再次进行聚合分析

Pipeline的分析结果会输出到原结果中，根绝位置的不同，分为两类

**Sibling**

结果和现有分析结果同级

1. max/ min / avg / sum 
1. stats / extended stats 
1. percentiles

**Parent**

1. derivative 求导
1. cumultive sum 累计求和
1. moving fuction 滑动窗口


结果内嵌到现有的聚合分析结果之中

例子：

```html
POST employees/_search
{
    "size":0,
    "aggs": {
        "jobs":{
            "terms": {
                "field": "job.keyword",
                "size": 10
            },
            "aggs": {
                "avg_salary":{
                    "avg": {
                        "field":"salary"
                    }
                }
            }
        }
    },
    "min_salary_by_job": {                          //结果和其他的聚合同级
        "min_bucket": {                             //min_bucket求之前结果的最小值
            "buckets_path":"jobs>avg_salary         //通过bucket_path关键字指定路径
        }
    }
}
```