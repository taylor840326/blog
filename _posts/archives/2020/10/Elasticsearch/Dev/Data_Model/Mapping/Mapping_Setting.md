## 设置索引
-----

### 1. 空置当前字段是否被索引

index属性控制当前字段是否被索引。默认为true，如果设置为false，该字段不可被搜索

```html
PUT movies
{
    "mappings":{
        "properties":{
            "firstName":{
                "type": "text"
            },
            "lastName":{
                "type":"text",
                "index":false   //不会被搜索。不会创建到牌索引
            }
        }
    }
}
```

### 2.index选项

index选项提供了四种不同的级别，可以空置倒排索引记录的内容

1. docs 记录doc id
1. freqs 记录doc id和term frequencies
1. positions 记录doc id/ term frequencies/ term position
1. offsets 记录doc id/ term frequencies/ term position/ character offsets

text类型默认记录postions，其他默认为docs

记录内容越多，占用存储空间越大

### 3. 需要对null值实现搜索

```html
GET movies/_search?q=mobile:NULL

PUT movies
{
    "mappings":{
        "properties":{
            "firstName":{
                "type":"text"
            },
            "mobile":{
                "type":"keyword",
                "null_value":"NULL"         //设置成null可被搜索,只有keyword类型支持设定null_value属性
            }
        }
    }
}
```

### 4. copy_to 设置

_all在7中被copy_to所替代

满足一些特定的搜索需求

copy_to将字段的数值拷贝到目标字段，实现类似_all的作用

copy_to的目标字段不出现在_source中

```html
PUT movies
{
    "mappings":{
        "properties":{
            "firstName":{
                "type":"text",
                "copy_to":"fullName"
            },
            "lastName":{
                "type":"text",
                "copy_to":"fullName"
            }
        }
    }
}

GET movies/_search?q=fullName:(Ruan Yiming)
```

### 5. 数组类型

Elasticsearch中不提供专门的数组类型。但是任何字段，都可以包含多个相同类型的数值

```html
PUT users/_doc/1
{
    "name":"onebird",
    "interests":"reading"
}

PUT users/_doc/2
{
    "name":"twobirds",
    "interests": ["reading","music"]
}
```

### 6. 多字段类型

多字段特性可以实现以下一些功能：

```text
1. 厂商名字实现精确匹配
    需要增加一个keyword字段
2.使用不同的analyzer
    不同语言
    pinyin字段的搜索
    还支持为搜索和索引指定不同的analyzer
```

```html
PUT products
{
    "mapping":{
        "properties":{
            "company":{
                "type":"text",
                "fields": {
                    "keyword":{
                        "type":"keyword",
                        "ignore_above": 256
                    }
                }
            },
            "comment":{
                "type":"text",
                "fields":{
                    "english_comment":{
                        "type":"text",
                        "analyzer":"english",
                        "search_analyzer":"english"
                    }
                }
            }
        }
    }
}
```

精确值Exact Value不需要做分词处理

Elasticsearch为每个字段创建一个倒排索引

Exact Value在索引时，不需要做特殊的粉刺处理


