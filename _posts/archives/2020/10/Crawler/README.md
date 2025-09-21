## JAVA开源爬虫列表及简介
-----

本文列举了一些较为常用的JAVA开源爬虫框架：

### 1. Apache Nutch

官方网站：

```html
http://nutch.apache.org/
```

1. 是否支持分布式：是
1. 可扩展性：中。Apache Nutch并不是一个可扩展性很强的爬虫，它是一个专门为搜索引擎定制的网络爬虫，虽然Apache Nutch具有一套强大的插件机制，但通过定制插件并不能修改爬虫的遍历算法、去重算法和爬取流程。
1. 适用性：Apache Nutch是为搜索引擎定制的爬虫，具有一套适合搜索引擎的URL维护机制（包括URL去重、网页更新等），但这套机制并不适合目前大多数的精抽取业务（即结构化数据采集）。
1. 上手难易度：难。需要使用者熟悉网络爬虫原理、hadoop开发基础及linux shell，且需要熟悉Apache Ant

技术讨论群：12077868

### 2.WebCollector

官方网站：
```html
https://github.com/CrawlScript/WebCollector
```

1. 是否支持分布式：该框架同时包含了单机版和分布式版两个版本
1. 可扩展性：强
1. 适用性：WebCollector适用于精抽取业务。
1. 上手难易度：简单

技术讨论群：250108697 345054141

### 3.WebMagic

官方网站：
```html
http://git.oschina.net/flashsword20/webmagic
```
1. 是否支持分布式：否
1. 可扩展性：强
1. 适用性：WebMagic适用于精抽取业务。
1. 上手难易度：简单。

技术讨论群：373225642

### 4. Crawler4j

官方网站：

```html
https://github.com/yasserg/crawler4j
```

1. 是否支持分布式：否
1. 可扩展性：低。Crawler4j实际上是一个单机版的垂直爬虫，其遍历算法是一种类似泛爬的算法，虽然可以添加一些限制，但仍不能满足目前大部分的精抽取业务。另外，Crawler4j并没有提供定制http请求的接口，因此Crawler4j并不适用于需要定制http请求的爬取业务（例如模拟登陆、多代理切换）。
1. 上手难易度：简单

### 5. Jsoup

Java HTML 解析器

官方网站:

```html
https://jsoup.org/
```