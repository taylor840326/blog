## 排序

-----

Elasticsearch默认采用相关性算分对结果进行降序排序

可以通过设定sorting参数，自行设定排序

如果不指定_score,算分为Null

使用text类型的字段排序，需要在mappings中打开fielddata=true选项。


### 排序的过程

排序时针对字段原始内容进行的。倒排索引无法发挥作用

需要用到正排索引。通过文档id和字段快速得到字段原始内容

Elasticsearch有两种实现方法：

1. Fielddata
1. Doc Values(列示存储，对Text类型无效)

### Doc Values vs Field Data

| | Doc Values| Field Data|
|:---|---|---:|
|何时创建| 索引时，和倒排索引一起创建| 搜索时动态创建|
|创建位置|磁盘文件| JVM Heap|
|优点| 皮面大量内存占用|索引速度快，不占用额外的磁盘空间|
|缺点|降低索引速度，占用额外磁盘空间| 文档过多时，动态创建开销大，占用过多的JVM Heap空间|
|缺省值| ES 2.X之后| ES 1.X 及之前|

### 关闭Doc Values

默认启用，可以通过Mapping设置关闭。关闭后可以增加索引的速度，减少磁盘空间占用

如果重新打开，需要重新创建索引

如果明确不需要做排序及聚合分析的时候可以关闭

```html
PUT test_keywork/_mapping
{
    "properties":{
        "user_name":{
            "type":"keyword",
            "doc_values":false
        }
    }
}
```

