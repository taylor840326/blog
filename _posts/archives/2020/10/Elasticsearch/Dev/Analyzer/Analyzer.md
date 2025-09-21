## Elasticsearch 内置分词器和中文分词器
-----

### 1. 分词器

### 1.1. Analysis和Analyzer

Analysis： 文本分析是把全文本转换一系列单词(term/token)的过程，也叫分词。Analysis是通过Analyzer来实现的。

当一个文档被索引时，每个Field都可能会创建一个倒排索引。可以再Mapping中设置不索引该Field。

**倒排索引的过程就是将文档通过Analyzer分成一个个的Term，每个Term都指向包含这个Term的文档集合**

当执行查询时，Elasticsearch会根据搜索类型决定是否对Query进行Analyze，然后和倒排索引中的Term进行相关性查询，匹配相应的文档。

### 1.2. Analyzer组成

分析器(Analyzer)都由三种构建块组成： Character Filter、Tokenizers、Token Filter

三者排序： Character Filter -> Tokenizers -> Token Filter

三者个数： Analyzer = CharFilters (0个或多个) + Toenizer(恰好1个) + TokenFilters(0个或多个)


#### Character Filter字符过滤器

在一段文本进行分词之前，先进行预处理，比如说最常见的就是，过滤html标签（<span>hello<span> --> hello），& --> and（I&you --> I and you）

#### Tokenizers分词器

英文分词可以根据空格将单词分开，中文分词可以采用机器学习算法来分词。

#### Token Filter过滤器

将切分的单词进行加工。大小写转换、去掉词、或者增加词

### 1.3. _analyzer API的三种使用方法

#### 直接指定Analyzer进行测试

```json
GET /_analyze
{
  "analyzer": "standard",
  "text": "Hello the world."
}
```

#### 指定索引的字段进行测试

```json
POST books/_analyze
{
  "field": "title",
  "text": "Hello the world."
}
```

#### 自定义分词器进行测试

```json
POST /_analyze
{
  "tokenizer": "standard",
  "filter": ["lowercase"],
  "text": "Hello the world."
}
```

### 2. Elasticsearch的内置分词器

|分词器|用途|
|:---|---:|
|Standard Analyzer | 默认分词器，按词切分，小写处理|
|Simple Analyzer | 按照非字母切分(符号被过滤), 小写处理|
|Stop Analyzer | 小写处理，停用词过滤(the,a,is)|
|Whitespace Analyzer | 按照空格切分，不转小写|
|Keyword Analyzer | 不分词，直接将输入当作输出|
|Patter Analyzer | 正则表达式，默认\W+(非字符分割)|
|Language | 提供了30多种常见语言的分词器|
|Customer Analyzer| 自定义分词器|

### 2.1. Elasticsearch常用内置分词器

### 2.1.1. Standard Analyzer

Standard时默认的分词器。它提供了基于语法的标记化(基于Unicode文本分割算法)，适用于大多数语言

### 2.1.1.1. 示例：

```json
POST _analyze
{
  "analyzer": "standard",
  "text": "Hello World 我爱北京天安门"
}
```

得到结果

```json
{
  "tokens" : [
    {
      "token" : "hello",
      "start_offset" : 0,
      "end_offset" : 5,
      "type" : "<ALPHANUM>",
      "position" : 0
    },
    {
      "token" : "world",
      "start_offset" : 6,
      "end_offset" : 11,
      "type" : "<ALPHANUM>",
      "position" : 1
    },
    {
      "token" : "我",
      "start_offset" : 12,
      "end_offset" : 13,
      "type" : "<IDEOGRAPHIC>",
      "position" : 2
    },
    {
      "token" : "爱",
      "start_offset" : 13,
      "end_offset" : 14,
      "type" : "<IDEOGRAPHIC>",
      "position" : 3
    },
    {
      "token" : "北",
      "start_offset" : 14,
      "end_offset" : 15,
      "type" : "<IDEOGRAPHIC>",
      "position" : 4
    },
    {
      "token" : "京",
      "start_offset" : 15,
      "end_offset" : 16,
      "type" : "<IDEOGRAPHIC>",
      "position" : 5
    },
    {
      "token" : "天",
      "start_offset" : 16,
      "end_offset" : 17,
      "type" : "<IDEOGRAPHIC>",
      "position" : 6
    },
    {
      "token" : "安",
      "start_offset" : 17,
      "end_offset" : 18,
      "type" : "<IDEOGRAPHIC>",
      "position" : 7
    },
    {
      "token" : "门",
      "start_offset" : 18,
      "end_offset" : 19,
      "type" : "<IDEOGRAPHIC>",
      "position" : 8
    }
  ]
}

```

### 2.1.1.2. 参数

可以再索引模板上添加词分词器的参数

```json
PUT new_index
{
  "settings": {
    "analysis": {
      "analyzer": {
        "my_english_analyzer": {
          "type": "standard",       #设置分词器为standard
          "max_token_length": 5,    #设置分词最大为5
          "stopwords": "_english_"  #设置过滤词
        }
      }
    }
  }
}
```


### 2.1.2. Simple Analyzer

Simple分词器是当它遇到只要不是字母的字符，就将文本解析成Term，而且所有的Term都是小写的。

### 2.1.2.1. 示例

```json
POST _analyze
{
  "analyzer": "simple",
  "text": ["Hello World 我爱北京天安门"]
}
```

结果

```json
{
  "tokens" : [
    {
      "token" : "hello",
      "start_offset" : 0,
      "end_offset" : 5,
      "type" : "word",
      "position" : 0
    },
    {
      "token" : "world",
      "start_offset" : 6,
      "end_offset" : 11,
      "type" : "word",
      "position" : 1
    },
    {
      "token" : "我爱北京天安门",
      "start_offset" : 12,
      "end_offset" : 19,
      "type" : "word",
      "position" : 2
    }
  ]
}
```


### 2.1.3. WhiteSpace Analyzer

遇到空格就分词

### 2.1.3.1. 示例

```json
POST _analyze
{
  "analyzer": "whitespace",
  "text": ["Hello World 我爱北京天安门"]
}
```

结果

```json
{
  "tokens" : [
    {
      "token" : "Hello",
      "start_offset" : 0,
      "end_offset" : 5,
      "type" : "word",
      "position" : 0
    },
    {
      "token" : "World",
      "start_offset" : 6,
      "end_offset" : 11,
      "type" : "word",
      "position" : 1
    },
    {
      "token" : "我爱北京天安门",
      "start_offset" : 12,
      "end_offset" : 19,
      "type" : "word",
      "position" : 2
    }
  ]
}

```

### 3. 中文分词

中文分词器现在大家比较推荐的就是“IK分词器”，还有其他的SmartCN、HanLP

### 3.1. 安装IK分词器

github地址为: 

```html
https://github.com/medcl/elasticsearch-analysis-ik
```

通过Elasticsearch的命令安装分词器

```bash
./bin/elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v7.1.0/elasticsearch-analysis-ik-7.1.0.zip
```

安装后重启ES

### 3.2. 使用IK分词器

IK有两种粒度的拆分：

1. ik_smart 会做最粗粒度的拆分
1. ik_max_word会将文本做最细粒度的拆分

### 3.2.1. ik_smart拆分

示例：

```json
POST _analyze
{
  "analyzer": "ik_smart",
  "text": ["Hello World 我爱北京天安门"]
}
```

结果：

```json
{
  "tokens" : [
    {
      "token" : "hello",
      "start_offset" : 0,
      "end_offset" : 5,
      "type" : "ENGLISH",
      "position" : 0
    },
    {
      "token" : "world",
      "start_offset" : 6,
      "end_offset" : 11,
      "type" : "ENGLISH",
      "position" : 1
    },
    {
      "token" : "我",
      "start_offset" : 12,
      "end_offset" : 13,
      "type" : "CN_CHAR",
      "position" : 2
    },
    {
      "token" : "爱",
      "start_offset" : 13,
      "end_offset" : 14,
      "type" : "CN_CHAR",
      "position" : 3
    },
    {
      "token" : "北京",
      "start_offset" : 14,
      "end_offset" : 16,
      "type" : "CN_WORD",
      "position" : 4
    },
    {
      "token" : "天安门",
      "start_offset" : 16,
      "end_offset" : 19,
      "type" : "CN_WORD",
      "position" : 5
    }
  ]
}
```

### 3.2.2. ik_max_word拆分

示例：

```json

POST _analyze
{
  "analyzer": "ik_max_word",
  "text": ["Hello World 我爱北京天安门"]
}
```

结果：

```json
{
  "tokens" : [
    {
      "token" : "hello",
      "start_offset" : 0,
      "end_offset" : 5,
      "type" : "ENGLISH",
      "position" : 0
    },
    {
      "token" : "world",
      "start_offset" : 6,
      "end_offset" : 11,
      "type" : "ENGLISH",
      "position" : 1
    },
    {
      "token" : "我",
      "start_offset" : 12,
      "end_offset" : 13,
      "type" : "CN_CHAR",
      "position" : 2
    },
    {
      "token" : "爱",
      "start_offset" : 13,
      "end_offset" : 14,
      "type" : "CN_CHAR",
      "position" : 3
    },
    {
      "token" : "北京",
      "start_offset" : 14,
      "end_offset" : 16,
      "type" : "CN_WORD",
      "position" : 4
    },
    {
      "token" : "天安门",
      "start_offset" : 16,
      "end_offset" : 19,
      "type" : "CN_WORD",
      "position" : 5
    },
    {
      "token" : "天安",
      "start_offset" : 16,
      "end_offset" : 18,
      "type" : "CN_WORD",
      "position" : 6
    },
    {
      "token" : "门",
      "start_offset" : 18,
      "end_offset" : 19,
      "type" : "CN_CHAR",
      "position" : 7
    }
  ]
}

```

### 3.3. 定制化IK分词器

IK分词器还可以收录新的词汇，是分词更加精准

假设要把“费雷尔卓德” 定为一个词汇则可以修改配置实现


### 3.3.1. 添加字典文件

字典文件是一个纯文本的文件，这个文件一行一个单词。

以本文档为例，把“费雷尔卓德”写入字典文件。

进入到Elasticsearch的config目录下，找到analysis-ik目录。创建custom.dic文件并把“费雷尔卓德”写入该文件。

修改IK的主配置文件IKAnalyzer.cfg.xml,将custom.dic加入到词配置中，对应的配置标签key为ext_dict。

