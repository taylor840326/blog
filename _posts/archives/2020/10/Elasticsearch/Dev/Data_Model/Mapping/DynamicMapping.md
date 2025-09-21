## Dynamci Mapping
-----

在写入文档的时候，如果索引不存在会自动创建索引。

Dynamic Mapping的机制，似的我们无需手动定义Mappings。Elasticsearch会自动根据文档信息推算出字段的类型

但是有时候会推算的不对，例如地理位置信息

当类型如果设置不对时，会导致一些功能无法正常运行。例如Range查询

查看一个索引Mappings的方法

```html
GET /movies/_mappings
{
    ...
}
```

### 1. 类型自动识别

Elasticsearch Mappings字段类型自动识别给予JSON的数据类型

|JSON类型|Elasticsearch类型|
|:---|---:|
|字符串| 匹配日期格式，设置成Date;匹配数字设置为float或者long，该选项默认关闭;设置为text,并且增加keyword字段|
|布尔值|boolean|
|浮点数|float|
|整数|long|
|对象| Object|
|数组|由第一个非空数值的类型所决定|
|空值|忽略|

```html
GET mapping_test/_doc/1
{
    "firstName":"Chan",
    "lastName":"Jackie",
    "loginDate":"2018-07-24T10:29:48.103Z"
}

GET mapping_test/_mapping

```


### 2. 能否更改Mapping的字段类型

对字段类型的修改分为两种情况：

#### 2.1. 新增加字段

Dynamic设为true时，一旦有新增字段的文档写入，Mapping也会同时被更新

Dynamic设为false时，Mapping不会被更新，新增字段的数据无法被索引，但是信息会出现在_source中

Dynamic设置成Strict，文档写入失败

### 2.2. 对已有字段

一旦已经有数据写入，就不再支持修改字段定义

Lucene实现的倒排索引，一旦生成就不允许修改

如果希望改变字段类型，必须reindex API,重建索引

### 2.3. 原因

如果修改了字段的数据类型，会导致已被索引的属性无法被搜索

但是如果是新增加的字段，就不会有这样的影响。

### 3. 控制Dynamic Mappings

| |true|false|strict|
|:---|---|---|---:|
|文档可索引|yes|yes|no|
|字段可索引| yes|no|no|
|Mapping被更新|yes|no|no|

```html
PUT movies
{
    "mappings":{
        "_doc":{
            "dynamic":"false"
        }
    }
}
```

当dynamic被设置成false的时候，存在新增字段的数据写入，该数据可以被索引，但是新增字段被丢弃。

当设置成strict的时候，数据写入直接出错。