## Elasticsearch搜索查询语法
-----

### 1. 查询简介

Elasticsearch Search API提供两种查询方式

1. URI Search
1. RequestBody Search

指定查询的索引

|语法|范围|
|:---|---:|
|/_search|集群上所有的索引|
|/index1/_search|index1|
|/index1,index-2/_search|index1和index2|
|/index*/_search|以index开头的索引|

### 1.1. URI查询

使用"q"，指定查询字符串

"query string syntax",KV键值对

语法：

```html
GET /movies/_search?q=2012&df=title&sort=year:desc&from=0&size=10&timeout=1s
{
    "profile": true
}

q指定查询语句。使用Query String Syntax
df默认字段，不指定时会对所有字段进行查询
sort排序
from和size用于分页
profile可以查看查询是如何被执行
```

带profile查询

```html
GET /movies/_search?q=2012&df=title
{
    "profile":"true"
}
```
### 指定字段 v.s 泛查询

q=title:2012 / q=2012

泛查询。正对_all所有字段
```html
GET /movies/_search?q=2012
```

指定字段
```html
GET /movies/_search?q=title:2012
```

### Term v.s Phrase

Beautiful Mind 等效于 Beautiful OR Mind

"Beautiful Mind"等效于Beautiful AND Mind。Phrase查询，还要求前后顺序保持一致。

使用引号Phrase查询

```html
GET /movies/_search?q=title:"Beautiful Mind"
{
    "profile":"true"
}
```

Mind为泛查询

```html
GET /movies/_search?q=title:Beautiful Mind
{
    "profile": "tue"
}
```

### 分组与引号

title:(Beautiful AND Mind)

title="Beautiful Mind"

```html
GET /movies/_search?q=title:(Beautiful Mind)
{
    "profile":"true"
}
```

### 布尔操作

AND/OR/NOT 或者&& / || !

查找美丽心灵

```html
GET /movies/_search?q=title:(Beautiful AND Mind)
GET /movies/_search?q=title:(Beautiful NOT Mind)
GET /movies/_search?q=title:(Beautiful %2Mind)
```

### 范围查询

区间表示： []闭区间，{}开区间

```html
year:{2019 TO 2018}
year:[* TO 2018]
```

```html
GET /movies/_search?q=year:>2018
```


### 算数符号

```html
year:>2010
year:(>2010 && <=2018)
year:(+>2010 +<=2018)
```

### 通配符查询(通配符查询效率低，占用内存大，不建议使用。特别是放在最前面)

```text
?代表1个字符
*代表0或多个字符
```

```html
GET /movies/_search?q=title:b*
```

### 正则表达式

```text
title:[bt]oy
```

### 模糊匹配与近似查询

```text
title:befutifl~1
title:"lord rings"~2
```

后面的数字表示模糊的字符数

可以对查询的内容进行容错

```text
GET /movies/_search?q=title:beautifl~1

GET /movies/_search?q=title:"Lord Rings"~2
```


### 1.2. RequestBody查询

```bash
curl -XGET "http://localhost:9200/kibana_sample_data_ecommerce/_search" -H 'Content-Type:application/json' -d
'{
    "query": {
        "match_all": {}
    }
}'
```

### 1.2.1. 分页

```html
POST /movies/_search
{
    "from":10,
    "size":20,
    "query": {
        "match_all": {}
    }
}
```

from从0开始，默认返回10个结果

获取靠后的翻页成本较高


### 1.2.2. 排序

```html
GET /movies/_search
{
    "sort":[{"created_date":"desc"}],
    "from":10,
    "size":20,
    "query": {
        "match_all": {}
    }
}
```

最好在**数字型**和**日期型**字段上排序

因为对于多值类型或者分析过的字段排序，系统会选一个值，无法得知该值。

### 1.2.3. 对_source进行过滤

过滤_source可以只显示想要的字段

```html
GET /movies/_search
{
    "_source": ["name","price","order_date"],
    "from":10,
    "size":5,
    "query": {
        "match_all": {}
    }
}
```

如果_source没有存储，那就只返回匹配的文档的元数据

_source支持使用通配符

```text
_source["name*","desc*"]
```

### 1.2.4. 脚本字段

通过脚本字段可以对某个列计算后新生成一个字段

```html
GET /movies/_search
{
    "script_fields": {
        "new_field":{           //新字段的名称为new_field
            "script": {
                "lang":"painless",
                "source":"doc['order_date'].value+'hello'"
            }
        }
    },
    "from":10,
    "size":5,
    "query":{
        "match_all": {}
    }
}
```

订单中有不同的汇率，需要结合汇率对订单价格进行排序.

### 1.2.5. 使用查询表达式 - Match

```html
GET /movies/_doc/_search
{
    "query": {
        "match":{
            "comment":"Last Christmas"          //默认是OR操作
        }
    }
}
```

```html
GET /movies/_doc/_search
{
    "query":{
        "match":{
            "comment":{
                "query":"Last Christmas",
                "operator": "AND"           //需要强制指定为AND操作
            }
        }
    }
}
```

### 1.2.6. 短语搜索 - Match Phrase

```html
GET /movies/_doc/_search
{
    "query":{
        "match_phrase"{
            "comment":{
                "query":"Song Last Christmas",
                "slop":1            //允许哥哥英文字符中间有一个其他字符。比如： Song A Last B Christmas.
            }
        }
    }
}
```

### 1.2.7 Query String 查询

类似URI查询

```html
POST /movies/_search
{
    "query":{
        "query_string":{
            "default_field":"name",
            "query":"Ruan AND Yiming"
        }
    }
}
```

```html
POST /movies/_search
{
    "query":{
        "query_string":{
            "fields":["name","about"],
            "query":"(Ruan AND Yiming) OR (Java AND Elasticsearch)"
        }
    }
}
```