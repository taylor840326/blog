
分类: ElasticSearchundefined

pre_tags 前缀标签
post_tags 后缀标签
tags_schema 设置为styled可以使用内置高亮样式
require_field_match 多字段高亮需要设置为false
使用highlight为查询结果增加高亮效果

Copy
{
  "query": {
    "bool": {
      "must": [
        {"match": {"name":"牛仔"}}
      ]
    }
  },
  "highlight": {
    "fields": {
      "name": {}
    }
  }
}
默认使用<em>标签包裹高亮字段

Copy
"hits" : [
      {
        "_index" : "idx_product",
        "_type" : "_doc",
        "_id" : "eZfUTXEBZypi2P-SpqrE",
        "_score" : 0.88044095,
        "_source" : {
          "proId" : "2",
          "name" : "牛仔男外套",
          "desc" : "牛仔外套男装春季衣服男春装夹克修身休闲男生潮牌工装潮流头号青年春秋棒球服男 7705浅蓝常规 XL",
          "timestamp" : 1576313264451,
          "createTime" : "2019-12-13 12:56:56"
        },
        "highlight" : {"name" : ["<em>牛仔</em>男外套"]}
      },
      {
        "_index" : "idx_product",
        "_type" : "_doc",
        "_id" : "fZfXTXEBZypi2P-SPqrD",
        "_score" : 0.62191015,
        "_source" : {
          "proId" : "6",
          "name" : "HLA海澜之家牛仔裤男",
          "desc" : "HLA海澜之家牛仔裤男2019时尚有型舒适HKNAD3E109A 牛仔蓝(A9)175/82A(32)",
          "timestamp" : 1576314265571,
          "createTime" : "2019-12-18 15:56:56"
        },
        "highlight" : {"name" : ["HLA海澜之家<em>牛仔</em>裤男"]}
      }
    ]
使用tag_schema:styled可以使用es内置高亮样式

Copy
{
  "query": {
    "bool": {
      "must": [
        {"term": {"name":"牛仔"}}
      ]
    }
  },
  "highlight": {
    "tags_schema": "styled", 
    "fields": {
      "name": {}
    }
  }
}
Copy
"highlight" : {
          "name" : ["""<em class="hlt1">牛仔</em>男外套"""]
        }
"highlight" : {
          "name" : ["""HLA海澜之家<em class="hlt1">牛仔</em>裤男"""]
        }
        
highlight默认只支持单个属性高亮，使用require_field_match属性置为false则可以使所有属性高亮

Copy
{
  "query": {
    "bool": {
      "must": [
        {"term": {"name":"牛仔"}}]
    }
  },
  "highlight": {
    "pre_tags": ["<font color='red'>"],
    "post_tags": ["<font/>"],
    "require_field_match": "false", 
    "fields": {
      "name": {},
      "desc": {}
    }
  }
}
Copy
"hits" : [
      {
        "_index" : "idx_product",
        "_type" : "_doc",
        "_id" : "eZfUTXEBZypi2P-SpqrE",
        "_score" : 0.88044095,
        "_source" : {
          "proId" : "2",
          "name" : "牛仔男外套",
          "desc" : "牛仔外套男装春季衣服男春装夹克修身休闲男生潮牌工装潮流头号青年春秋棒球服男 7705浅蓝常规 XL",
          "timestamp" : 1576313264451,
          "createTime" : "2019-12-13 12:56:56"
        },
        "highlight" : {
          "name" : [
            "<font color='red'>牛仔<font/>男外套"
          ],
          "desc" : [
            "<font color='red'>牛仔<font/>外套男装春季衣服男春装夹克修身休闲男生潮牌工装潮流头号青年春秋棒球服男 7705浅蓝常规 XL"
          ]
        }
      },
      {
        "_index" : "idx_product",
        "_type" : "_doc",
        "_id" : "fZfXTXEBZypi2P-SPqrD",
        "_score" : 0.62191015,
        "_source" : {
          "proId" : "6",
          "name" : "HLA海澜之家牛仔裤男",
          "desc" : "HLA海澜之家牛仔裤男2019时尚有型舒适HKNAD3E109A 牛仔蓝(A9)175/82A(32)",
          "timestamp" : 1576314265571,
          "createTime" : "2019-12-18 15:56:56"
        },
        "highlight" : {
          "name" : [
            "HLA海澜之家<font color='red'>牛仔<font/>裤男"
          ],
          "desc" : [
            "HLA海澜之家<font color='red'>牛仔<font/>裤男2019时尚有型舒适HKNAD3E109A <font color='red'>牛仔<font/>蓝(A9)175/82A(32)"
          ]
        }
      }
    ]
作者： 海向

出处：https://www.cnblogs.com/haixiang/p/12642230.html

本站使用「CC BY 4.0」创作共享协议，转载请在文章明显位置注明作者及出处。

« 上一篇： volatile原理
» 下一篇： 使用elasticsearch搭建自己的搜索系统
posted @ 2020-04-06 15:15  海向  阅读(358)  评论(0)  编辑  收藏
注