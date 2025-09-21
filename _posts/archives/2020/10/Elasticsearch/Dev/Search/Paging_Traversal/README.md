## 分页和遍历 -  From/Size/Search After && Scroll API

-----

### From /Size

默认情况下，查询按照相关度算分排序，返回前10条记录

容易理解的分页方案：
1. From 开始位置
1. Size 期望获取文档的总数

```html
POST my_index/_search
{
    "from":10,
    "size":20,
    "query":{
        "match_all":{}
    }
}
```

ES天生就是分布式的。查询信息，但是数据分别保存在多个分片，多个机器上。ES天生就需要满足排序的需要（按照相关度算分）

当一个查询: From = 990, Size = 10

会在每个分片上先都获取1000个文档。然后通过Coordinating Node聚合所有结果。最后再通过排序选取前1000个文档

页数越深，占用内存越多。为了避免深度分页带来内存开销。ES有一个设定，默认限定到10000个文档

```text
index.max_result_window = 10000
```

### Search After避免深度分页的问题

避免深度分页的性能问题，可以实时获取下一页文档信息。

不支持指定页数(From)

只能往下翻

第一步搜索需要指定sort，并且保证值时唯一的（可以通过加入_id保证唯一性）

然后使用上一次，最后一个文档的sort值进行查询

### Scroll API

创建一个快照，有新的数据写入以后，无法被查到

每次查询后，输入上一次的Scroll id

```html
POST /users/_search?scroll=5m
{
    "size": 1,
    "query":{
        "match_all":{}
    }
}

POST /_search/scroll
{
    "scroll":"1m",
    "scroll_id":"xxxxx"
}
```

### 不同的搜索类型和使用场景

Regular

需要实时获取顶部的部分文档。例如查询最新的订单

Scroll

需要全部文档，例如导出全部数据

Pagination

使用From和Size

如果需要深度分组，则选用SearchAfter
