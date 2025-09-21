### 自定义分词器
-----

当Elasticsearch自带的分词器无法满足时，可以自定义分词器。通过自组合不同的组建实现。

需要组合的内容为：

1. Character Filter
1. Tokenizer
1. Token Filter

### 1. Character Filter

在Tokenizer之前对文本进行处理，例如增加删除及替换字符。

可以配置多个Character Filters，会影响Tokenizer的position和offset信息。

一些自带的Character Filter

1. HTML strip -> 取出HTML标签
1. Mapping -> 字符串替换
1. Pattern replace -> 正则匹配替换

### 2. Tokenizer

将原始的文本按照一定的规则，切分为词(term or token)

Elasticsearch内置的Tokenizers

whitespace/standard/uax_url_email/pattern/ keyword(不做特殊处理，输入什么直接输出)/ path hierarchy

可以用Java开发插件，实现自己的Tokenizer

### 3. Token Filters

将Tokenizer输出的单词term进行增加、修改、删除

自带的Token Filter

lowercase/ stop / synonym (添加近义词)


### 4. 示例

### 4.1. 剥离html标签

网络爬虫处理数据。

```html
POST _analyze
{
    "tokenizer":"keyword",
    "char_filter":["html_strip"],
    "text":"<b>hello world</b>"
}

结果
{
    "tokens":[
        {
            "token":"hello world",
            "start_offset":3,
            "end_offset":18,
            "type":"word",
            "position":0
        }
    ]
}
```

### 4.2. 字符替换

比如把中划线替换成下划线

```html
POST _analyze
{
     "tokenizer":"standard",
    "char_filter":[
        {
            "type":"mapping",
            "mappings": ["- => _"]
        }    
    ],
    "text":"123-456,I-test! test-990 650-555-1234"
}
```

替换表情符号

```html
POST _analyze
{
    "tokenizer":"standard",
    "char_filter":[
        {
            "type":"mapping",
            "mappings": [":) => happy",":( => sad"]
        }
    ],
    "text":["I am felling :)","Feeling :( today"]
}
```

### 4.3. 正则表达式

```html
GET _analyze
{
    "tokenizer":"standard",
    "char_filter":[
        {
            "type":"pattern_replace",
            "pattern": "http://(.*)",
            "replacement": "$1
        }
    ],
    "text":"http://www.elastic.co"
}
```

### 4.4. 过滤出路径path_hierarchy

```html
POST _analyze
{
    "tokenizer": "path_hierarchy",
    "text": "/user/ymruan/a/b/c/d/e"
}
```

### 4.5. whitespace与stop

```html
GET _analyze
{
    "tokenizer": "whitespace",
    "filter": ["stop"],         //去掉辅助词 on/the/...,但是The这个单词无法取出，需要参照下面的例子联合使用lowercase去除。
    "text": ["The rain in Spain falls mainly on the plain."] 
}
```

### 4.6. 联合使用lowercase和stop去除 不符合要求的辅助词

```html
GET _analyze
{
    "tokenizer":"whitespace",
    "filter": ["lowercase","stop"],
    "text": ["The girls in China are playing this game!"]
}
```

### 4.7. 在索引的settings里自定义analyzer

```html
PUT movies
{
    "settings": {
        "analysis":{
            "analyzer": {
                "my_custom_analyzer":{
                    "type":"custom",
                    "char_filter": ["emoticons"],
                    "tokenizer": "punctuation",
                    "filter":["lowercase","english_stop"]
                }
            },
            "tokenizer": {
                "punctuation":{
                    "type": "pattern",
                    "pattern": "[ .,!?]"
                }
            },
            "char_filter":{
                "emoticons":{
                    "type":"mapping",
                    "mappings": [":) => _happy_",":( => _sad_"]
                }
            },
            "filter":{
                "english_stop":{
                    "type":"stop",
                    "stopwords":"_english_"
                }
            }
        }
    }
}
```