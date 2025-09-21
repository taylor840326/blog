## Elasticsearch中mapping全解实战
-----

### 1. Mapping简介

mapping 是用来定义文档及其字段的存储方式、索引方式的手段，例如利用mapping 来定义以下内容：

1. 哪些字段需要被定义为全文检索类型
1. 哪些字段包含number、date类型等
1. 格式化时间格式
1. 自定义规则，用于控制动态添加字段的映射

Mapping会吧JSON文档映射成Lucene所需要的扁平格式

一个Mapping属于一个索引的Type:

1. 每个文档都属于一个Type
1. 一个Type有一个Mapping定义
1. 7.0开始，不需要在Mapping定义中指定Type信息

### 2. Mapping Type

每个索引都拥有唯一的 mapping type，用来决定文档将如何被索引。

mapping type由下面两部分组成:

**Meta-fields**

元字段用于自定义如何处理文档的相关元数据。 元字段的示例包括文档的_index，_type，_id和_source字段。

**Fields or properties**

映射类型包含与文档相关的字段或属性的列表。

### 3. 分词器最佳实践

因为后续的keyword和text设计分词问题，这里给出分词最佳实践。

即**索引时用ik_max_word，搜索时分词器用ik_smart**，这样索引时最大化的将内容分词，搜索时更精确的搜索到想要的结果。

例如我想搜索的是小米手机，我此时的想法是想搜索出小米手机的商品，而不是小米音响、小米洗衣机等其他产品，也就是说商品信息中必须只有小米手机这个词。

我们后续会使用"search_analyzer": "ik_smart"来实现这样的需求。

### 4. 字段类型

1. 一种简单的数据类型。例如text、keyword、double、boolean、long、date、ip类型。
1. 也可以是一种分层的json对象（支持属性嵌套）。
1. 也可以是一些不常用的特殊类型，例如geo_point、geo_shape、completion

针对同一字段支持多种字段类型可以更好地满足我们的搜索需求。

例如一个string类型的字段可以设置为text来支持全文检索，与此同时也可以让这个字段拥有keyword类型来做排序和聚合;

另外我们也可以为字段单独配置分词方式，例如"analyzer": "ik_max_word",

### 4.1. text 类型

text类型的字段用来做全文检索，例如邮件的主题、淘宝京东中商品的描述等。

这种字段在被索引**存储前先进行分词**，存储的是分词后的结果，而不是完整的字段。

text字段不适合做排序和聚合。

如果是一些结构化字段，分词后无意义的字段建议使用keyword类型，例如邮箱地址、主机名、商品标签等。

### 4.1.1. 常用参数包含以下

1. analyzer：用来分词，包含**索引存储阶段**和**搜索阶段**（其中查询阶段可以被search_analyzer参数覆盖），该参数默认设置为index的analyzer设置或者standard analyzer
1. index：是否可以被搜索到。默认是true
1. fields：Multi-fields允许同一个字符串值同时被不同的方式索引，例如用不同的analyzer使一个field用来排序和聚类，另一个同样的string用来分析和全文检索。下面会做详细的说明
1. search_analyzer：这个字段用来指定**搜索阶段**时使用的分词器，默认使用analyzer的设置
1. search_quote_analyzer：搜索遇到短语时使用的分词器，默认使用search_analyzer的设置

### 4.2. keyword 类型

keyword用于**索引结构化内容**（例如电子邮件地址，主机名，状态代码，邮政编码或标签）的字段。

这些字段被**拆分后不具有意义**，所以在es中应索引完整的字段，而不是分词后的结果。

通常用于**过滤**（例如在博客中根据发布状态来查询所有已发布文章），**排序**和**聚合**。

keyword只能按照字段精确搜索，例如根据文章id查询文章详情。

如果想根据本字段进行全文检索相关词汇，可以使用text类型。

```json
PUT my_index
{
  "mappings": {
    "properties": {
      "tags": {
        "type":  "keyword"
      }
    }
  }
}
```

### 4.2.1. 常用参数包含以下

index：是否可以被搜索到。默认是true

fields：Multi-fields允许同一个字符串值同时被不同的方式索引，例如用不同的analyzer使一个field用来排序和聚类，另一个同样的string用来分析和全文检索。下面会做详细的说明

null_value：如果该字段为空，设置的默认值，默认为null

ignore_above：设置索引字段大小的阈值。该字段不会索引大小超过该属性设置的值，默认为2147483647，代表着可以接收任意大小的值。但是这一值可以被PUT Mapping Api中新设置的ignore_above来覆盖这一值。

### 4.3. date类型

支持排序，且可以通过format字段对时间格式进行格式化。

json中没有时间类型，所以在es在规定可以是以下的形式：

一段格式化的字符串，例如"2015-01-01"或者"2015/01/01 12:10:30"

一段long类型的数字，指距某个时间的毫秒数，例如1420070400001

一段integer类型的数字，指距某个时间的秒数

### 4.4. object类型

mapping中不用特意指定field为object类型，因为这是它的默认类型。

json类型天生具有层级的概念，文档内部还可以包含object类型进行嵌套。例如：

```json
PUT my_index/_doc/1
{ 
  "region": "US",
  "manager": { 
    "age":     30,
    "name": { 
      "first": "John",
      "last":  "Smith"
    }
  }
}
```

在es中上述对象会被按照以下的形式进行索引：

```json
{
  "region":             "US",
  "manager.age":        30,
  "manager.name.first": "John",
  "manager.name.last":  "Smith"
}
```

mapping可以对不同字段进行不同的设置

```json
PUT my_index
{
  "mappings": {
    "properties": { 
      "region": {
        "type": "keyword"
      },
      "manager": { 
        "properties": {
          "age":  { "type": "integer" },
          "name": { 
            "properties": {
              "first": { "type": "text" },
              "last":  { "type": "text" }
            }
          }
        }
      }
    }
  }
}
```

### 4.5. nest类型

nest类型是一种特殊的object类型，它允许object可以以数组形式被索引，而且数组中的某一项都可以被独立检索。

而且es中没有内部类的概念，而是通过简单的列表来实现nest效果，例如下列结构的文档：

```json
PUT my_index/_doc/1
{
  "group" : "fans",
  "user" : [ 
    {
      "first" : "John",
      "last" :  "Smith"
    },
    {
      "first" : "Alice",
      "last" :  "White"
    }
  ]
}
```

上面格式的对象会被按照下列格式进行索引，因此会发现一个user中的两个属性值不再匹配，alice和white失去了联系

```json
{
  "group" :        "fans",
  "user.first" : [ "alice", "john" ],
  "user.last" :  [ "smith", "white" ]
}
```

### 4.6. range类型

支持以下范围类型：

|类型|范围|
|:---|---:|
|integer_range|-2的31次 到 2的31次-1.|
|float_range|32位单精度浮点数|
|long_range|-2的63次 到 2的63次-1.|
|double_range|64位双精度浮点数|
|date_range|unsigned 64-bit integer milliseconds|
|ip_range|ipv4和ipv6或者两者的混合|

使用范例为：

```json
PUT range_index
{
  "settings": {
    "number_of_shards": 2
  },
  "mappings": {
    "properties": {
      "age_range": {
        "type": "integer_range"
      },
      "time_frame": {
        "type": "date_range", 
        "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"
      }
    }
  }
}

PUT range_index/_doc/1?refresh
{
  "age_range" : { 
    "gte" : 10,
    "lte" : 20
  },
  "time_frame" : { 
    "gte" : "2015-10-31 12:00:00", 
    "lte" : "2015-11-01"
  }
}
```

### 5. 实战：同时使用keyword和text类型

注：**term是查询时对关键字不分词，keyword是索引时不分词**

上述我们讲解过keyword和text一个不分词索引，一个是分词后索引，我们利用他们的fields属性来让当前字段同时具备keyword和text类型。

首先我们创建索引并指定mapping，为title同时设置keyword和text属性

```json
PUT /idx_item/
{
  "settings": {
    "index": {
        "number_of_shards" : "2",
        "number_of_replicas" : "0"
    }
  },
  "mappings": {
    "properties": {
      "itemId" : {
        "type": "keyword",
        "ignore_above": 64
      },
      "title" : {
        "type": "text",
        "analyzer": "ik_max_word", 
        "search_analyzer": "ik_smart", 
        "fields": {
          "keyword" : {"ignore_above" : 256, "type" : "keyword"}
        }
      },
      "desc" : {"type": "text", "analyzer": "ik_max_word"},
      "num" : {"type": "integer"},
      "price" : {"type": "long"}
    }
  }
}
```

我们已经往es中插入以下数据

```text
_index	_type	_id	_score	itemId	title	desc	num	Price
idx_item	_doc	rvsX-W4Bo-iJGWqbQ8dk	1	1	苏泊尔煮饭SL3200	让煮饭更简单，让生活更快乐	100	200
idx_item	_doc	sPsY-W4Bo-iJGWqbscfU	1	3	厨房能手威猛先生	你煲粥，我洗锅	100	30
idx_item	_doc	r_sX-W4Bo-iJGWqbhMew	1	2	苏泊尔煲粥好能手型号SL322	你煲粥，我煲粥，我们一起让煲粥更简单	100	190
```

title=”苏泊尔煮饭SL3200“ 根据text以及最细粒度分词设置"analyzer": "ik_max_word"，在es中按照以下形式进行索引存储
```json

{ "苏泊尔","煮饭", "sl3200", "sl","3200"}
```


title.keyword=”苏泊尔煮饭SL3200因为不分词，所以在es中索引存储形式为

```text
苏泊尔煮饭SL3200
```

我们首先对title.keyword进行搜索，只能搜索到第一条数据，因为match搜索会将关键字分词然后去搜索，分词后的结果包含"苏泊尔煮饭SL3200"所以搜索成功，我们将搜索关键字改为苏泊尔、煮饭等都不会查到数据。

```json
GET idx_item/_search
{
  "query": {
    "bool": {
      "must": {
        "match": {"title.keyword": "苏泊尔煮饭SL3200"}
        }
    }
  }
}
```

我们改用term搜索，他搜索不会分词，正好与es中的数据精准匹配，也只有第一条数据，我们将搜索关键字改为苏泊尔、煮饭等都不会查到数据。

```json
GET idx_item/_search
{
  "query": {
    "bool": {
      "must": {
        "term": {"title.keyword": "苏泊尔煮饭SL3200"}
        }
    }
  }
}
```

我们继续对title使用match进行查询，结果查到了第一条和第三条数据，因为它们在es中被索引的数据包含苏泊尔关键字

```json
GET idx_item/_search
{
  "query": {
    "bool": {
      "must": {"match": {"title": "苏泊尔"}
        }
    }
  }
}
```

我们如果搜索苏泊尔煮饭SL3200会发现没有返回数据，因为title在索引时没有苏泊尔煮饭SL3200这一项，而term时搜索关键字也不分词，所以无法匹配到数据。但是我们将内容改为苏泊尔时，就可以搜索到第一条和第三条内容，因为第一条和第三条的title被分词后的索引包含苏泊尔字段，所以可以查出第一三条。

```json
"term": {"title": "苏泊尔煮饭SL3200"}
```


### 6. 实战：格式化时间、以及按照时间排序

我们创建索引idx_pro，将mytimestamp和createTime字段分别格式化成两种时间格式

```json
PUT /idx_pro/
{
  "settings": {
    "index": {
        "number_of_shards" : "2",
        "number_of_replicas" : "0"
    }
  },
  "mappings": {
    "properties": {
      "proId" : {
        "type": "keyword",
        "ignore_above": 64
      },
      "name" : {
        "type": "text",
        "analyzer": "ik_max_word", 
        "search_analyzer": "ik_smart", 
        "fields": {
          "keyword" : {"ignore_above" : 256, "type" : "keyword"}
        }
      },
      "mytimestamp" : {
        "type": "date",
        "format": "epoch_millis"
      },
      "createTime" : {
        "type": "date",
        "format": "yyyy-MM-dd HH:mm:ss"
      }
    }
  }
}
```

插入四组样本数据

```json
POST idx_pro/_doc
{
  "proId" : "1",
  "name" : "冬日工装裤",
  "timestamp" : 1576312053946,
  "createTime" : "2019-12-12 12:56:56"
}
POST idx_pro/_doc
{
  "proId" : "2",
  "name" : "冬日羽绒服",
  "timestamp" : 1576313210024,
  "createTime" : "2019-12-10 10:50:50"
}
POST idx_pro/_doc
{
  "proId" : "3",
  "name" : "花花公子外套",
  "timestamp" : 1576313239816,
  "createTime" : "2019-12-19 12:50:50"
}
POST idx_pro/_doc
{
  "proId" : "4",
  "name" : "花花公子羽绒服",
  "timestamp" : 1576313264391,
  "createTime" : "2019-12-12 11:56:56"
}
```

我们可以使用sort参数来进行排序，并且支持数组形式，即同时使用多字段排序，只要改为[]就行

```json
GET idx_pro/_search
{
  "sort":{"createTime": {"order": "asc"}}, 
  "query": {
    "bool": {
      "must": {"match_all": {}}
    }
  }
}
```

我们也可以使用range参数来搜索指定时间范围内的数据，当然range也支持integer、long等类型

```json
GET idx_pro/_search
{
  "query": {
    "bool": {
      "must": {
        "range": {
          "timestamp": {
            "gt": "1576313210024",
            "lt": "1576313264391"
          }
        }
      }
    }
  }
}
```

### 参考资料

分类: ElasticSearchundefined

💛原文地址为https://www.cnblogs.com/haixiang/p/12040272.html，转载请注明出处!
🍎es与SpringBoot的整合以及常用CRUD、搜索API已被作者封装,开箱即用效果很好,欢迎star谢谢!github
