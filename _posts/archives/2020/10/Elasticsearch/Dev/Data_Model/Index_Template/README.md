## Index Template和Dynamic Template
-----

Index Template可以帮助你设定Mappings和Settings，并按照一定的规则自动匹配到新创建的索引之上。

模板仅在一个索引被新创建时，才会产生作用。修改模板不会影响已创建的索引。

你可以设定多个索引模板，这些设置会被"merge"在一起

你可以指定order的数值，控制"merging"的过程。

### 1. Index Template的工作方式

当一个索引被新建时：

1. 应用Elasticsearch默认的settings和mappings
1. 应用order数值低的Index Template中的设定
1. 应用order高的Index Template中的设定，之前的设定会被覆盖
1. 应用创建索引时，用户所指定的settings和mappings，并覆盖之前模板中的设定。

### 2. 两个模板的例子

```html
PUT _template/template_default
{
    "index_patterns": ["*"],
    "order": 0,
    "version": 1,
    "settings": {
        "number_of_shards":1,
        "number_of_replicas":1
    }
}
```


```html
PUT _template/template_test
{
    "index_patterns":["test*"],
    "order":1,
    "settings":{
        "number_of_shards":1,
        "number_of_replicas":2
    },
    "mappings": {
        "date_detection": false,
        "numeric_detection":true
    }
}
```

### 示例

```html
PUT ttemplate/_doc/1
{
    "someNumber":"1",
    "someDate":"2019/01/01"
}

GET ttemplate/_mapping
{
    "ttemplate":{
        "mappings":{
            "properties":{
                "someDate": {
                    "type":"date",
                    "format":"yyyy/MM/dd HH:mm:ss||yyyy/MM/dd||epoch_millis"
                },
                "someNumber":{
                    "type":"text",          //没有正确识别出数字类型
                    "fields": {
                        "keyword":{
                            "type":"keyword",
                            "ignore_above":256
                        }
                    }
                }
            }
        }
    }
}
```

如果先创建章节2中template，则新插入数据。再查看索引的mapping信息。可以看到数据类型被成功匹配了。

### 3. Dynamic Template

根据Elasticsearch识别的数据类型，结合字段名称，来动态设定字段类型。

所有的字符串类型都设定成keyword，或者关闭keyword字段

is开头的字段都设置成boolean

long_开头的都设置成long类型

### 示例

```html
PUT my_test_index
{
    "mappings":{
        "dynamic_templates":[
            {
                "full_name":{
                    "path_match": "name.*",
                    "path_unmatch": "*.middle",
                    "mapping":{
                        "type": "text",
                        "copy_to": "full_name"
                    }
                }
            }
        ]
    }
}
```

Dynamic Template是定义在某个索引的Mapping中。

Template有一个名称

匹配规则是一个数组

为匹配到字段设置Mapping

### 示例2

```html
PUT my_index/_doc/1
{
    "firstName":"Ruan",
    "isVIP":"true"
}

GET my_index/_mapping
{
    ...
    "properties":{
        "firstName":{
            "type":"keyword"
        },
        "isVIP":{
            "type":"boolean"
        }
    }
}

DELETE my_index

PUT my_index
{
    "mappings":{
        "dynamic_templates":[
            {
                "full_name":{
                    "path_match":"name.*",
                    "path_unmatch": "*.middle",
                    "mapping": {
                        "type":"text",
                        "copy_to": "full_name"
                    }
                }
            }
        ]
    }
}

PUT my_index/_doc/1
{
    "name":{
        "first": "John",
        "middle": "Winston",
        "last": "Lennon"
    }
}

GET my_index/_search?q=full_name:John
```