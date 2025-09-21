WebCollector 教程——MetaData
BY BRIEFCOPY · 2017年11月27日

WebCollector的MetaData是提升爬虫开发效率最好的特性之一，本教程通过一个抓取搜索引擎的实例来解释MetaData是如何简化开发的。

抓取搜索引擎时，一般需要将每个搜索关键字对应的搜索结果页URL作为种子放入爬虫。以采集必应搜索为例，当需要采集的关键词为”手抓饼”、”肉夹馍”和”牛肉面”时，需要将下面三个URL作为种子（起始页）放入爬虫：

http://cn.bing.com/search?q=手抓饼
http://cn.bing.com/search?q=肉夹馍
http://cn.bing.com/search?q=牛肉面
爬虫成功采集这这些页面的源码后会将其交给解析程序，现在问题来了，解析程序可以轻松获得搜索结果页中的网页链接，但对于多线程爬虫，解析程序大多如下所示，在这样的程序中，怎么才能知道当前解析程序拿到的搜索结果页对应的是哪个关键词？

public void visit(String url, String html){
  //根据url和html解析网页信息
}
一个常见的笨办法就是从url中解析出关键词，例如对于上面的搜索URL，根据”q=”的位置就可以定位出关键词的位置。但是当URL比较复杂时，例如在抓取搜索引擎时，我们希望抓取一个关键词前2页的信息，上面的种子URL会变成：

http://cn.bing.com/search?q=手抓饼&first=1
http://cn.bing.com/search?q=手抓饼&first=11
http://cn.bing.com/search?q=肉夹馍&first=1
http://cn.bing.com/search?q=肉夹馍&first=11
http://cn.bing.com/search?q=牛肉面&first=1
http://cn.bing.com/search?q=牛肉面&first=11
在解析时，需要同时获取当前搜索页对应的关键字和页号，如果还用上面这个笨办法，解析程序的开发会变得较为麻烦。

另一个笨办法对此做了一些改进，将:

http://cn.bing.com/search?q=手抓饼&first=1
修改为

http://cn.bing.com/search?q=手抓饼&first=1#{"q":"手抓饼","first":1}
井号之后的部分不会影响Http请求，解析程序获取URL中#之后的部分，可方便获取关键词和页号信息。但这依然是个笨办法，开发者需要自己拼接URL，在解析时也需截取字符串、做JSON解析等。

WebCollector的MetaData特性可以直接解决这个问题，例如使用MetaData特性时，种子注入和部分页面解析如下：

//注入
addSeed(new CrawlDatum("http://cn.bing.com/search?q=手抓饼&first=1").meta("keyword", "手抓饼").meta(pageNum, 1));

//解析
@Override
public void visit(Page page, CrawlDatums next){
  String keyword = page.meta("keyword");
  int pageNum = Integer.valueOf(page.meta("pageNum"));
  //其它代码
}
相对于上面两个笨办法，这段代码非常精简，完全不需要开发者自己进行字符串拼接和解析。

MetaData特性使得WebCollector特别适合复杂爬取任务的开发。例如当爬虫对需要解析多种异构页面时，可通过MetaData功能为每个页面标注类别。网页的类别往往在网页被探测到时容易获取，在解析时难以获取，而MetaData正好提供了在探测网页时为网页标注类别的功能。例如抓取某电商网站，电商入口页的左栏是热销商品链接，右栏是热门店家信息，在解析首页时，就可以利用MetaData特性将左栏中探测到的URL标上”kind”:”商品”，将右栏中探测到的URL标上”kind”:”店家”，将它们一起放入后续任务(next)中。当爬虫成功获取商品或店家页面的源码并将其交给解析程序时，开发者可以直接通过page.meta("kind")来获取页面的类型，而不需要通过URL的正则匹配等方法来处理该问题，且很多场景中，一些异构页面的URL利用正则难以区分。