## 自我测试
-----

#### 判断题： ES支持使用HTTP PUT 写入新文档，并通过ES生成文档id

错！需要用POST方法写入新文档。

#### 判断题： update一个文档，需要使用HTTP PUT

错！update一个文档，必须使用POST方法，PUT方法只能用来做Index胡子哦喝Create

#### 判断题： Index一个已经存在的文档，旧的文档会先被删除，新的文档再被写入。同时版本号加1

对

#### 尝试描述创建一个新的文档到一个不存在的索引中，背后会发生一些什么操作？

默认情况下，会创建相应的索引。并且自己设置Mapping。当然，实际情况还是要看是否有合适的Index Template。

#### ES7中的合法的type是什么？

ES7中一个索引只能有一个type，合法名字是_doc

#### 精确值和全文的本质区别是什么？

精确值不会被Analyzer分词，全文本会。

#### Analyzer由那几个部分组成？

Char Filter -> Tokenizer -> Token Filter

#### 尝试描述match和match_phrase的区别

match中的terms之间是OR的关系，match_phrase中的terms之间是AND的关系。并且term之间位置关系也影响搜索的结果。

#### 如果你希望match_phrase匹配到更多结果，你应该配置查询中什么参数？

slop。

#### 如果Mapping的Dynamic设置成strict，索引一个包含新增字段的恩当时会发生什么？

直接报错。

#### 如果Mapping的Dynamic设置成false，索引一个包含新增字段的文档时会发生什么？

文档被索引，新的字段在_source中可见。但是该字段无法搜索。

#### 判断： 可以把一个字段的类型从integer改成long，因为这两个类型是兼容的

错！字段类型修改，必须要重新reindex操作

#### 判断： 你可以在Mapping文件中为indexing和searching指定不同的analyzer

对。可以在mapping中为index和search指定不同的analyizer

#### 判断： 字段类型为text的字段，一定可以被全文检索

错！如果这个字段的index=false就无法被搜索。

