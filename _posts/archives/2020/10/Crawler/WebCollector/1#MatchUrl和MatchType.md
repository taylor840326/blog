WebCollector 教程——MatchUrl 和 MatchType
BY BRIEFCOPY · 2017年11月27日

MatchUrl和MatchType是WebCollector新特性中的trick，可以大大提升爬虫开发的效率（尤其是MatchType）。

MatchUrl
在早期的用WebCollector开发的爬虫中，下面这种代码很常见：

 @Override
    public void visit(Page page, CrawlDatums next) {
        //如果是博客内容页（根据URL正则判断）
        if(Pattern.matches(page.matchUrl("http://blog.csdn.net/.*/article/details/.*", page.url()){
          //抽取代码
        }
    }
在新版本的WebCollector中，可以直接用page.matchUrl(String regex)来判断当前页面URL是否与正则匹配

 @Override
    public void visit(Page page, CrawlDatums next) {
        //如果是博客内容页（根据URL正则判断）
        if (page.matchUrl("http://blog.csdn.net/.*/article/details/.*")) {
          //抽取代码
        }
    }
MatchType
阅读此教程之前建议先学习WebCollector教程——MetaData。

MetaData特性使得WebCollector在注入或探测链接时为其添加附属信息，提升开发效率，在附属信息中，页面类型信息是一个及其重要的信息。这里说的页面类型不是Http协议中的Content-Type信息，而是用户自定义的用于区分不同解析方案的信息，例如在采集豆瓣图书时，会遇到标签页、列表页和图书详情页，只有知道页面的类型，才可以选取对应的抽取及新链接探测方案。

大多数其它的Java爬虫是通过网页URL的正则匹配来判断页面类型的，但正则匹配并不能解决所有的问题，对于一些网站，可能不同类型的页面享有相同的URL模式。抛开URL正则，换个角度思考。当我们设计豆瓣图书爬虫时（手动解析），有如下的关系：

A.注入种子页（标签页）
B.从标签页中解析获得列表页的URL
C.从列表页中解析获得内容页的URL
与其根据URL正则获得页面的类型，为何不直接在上述A（注入）、B（探测）、C（探测）时直接将页面类型标记在链接（CrawlDatum）中呢？在新版本WebCollector中，可通过如下几种形式为链接（CrawlDatum）添加类型信息。

CrawlDatum datum = new CrawlDatum("网页URL", "页面类型");
CrawlDatum datum = new CrawlDatum("网页URL").type("页面类型");
//在Crawler中注入时
addSeed("网页URL", "页面类型");
//在visit中往next中添加探测到的链接时
next.add("网页URL", "页面类型");
在解析时，可直接通过page.matchType("页面类型")来判断网页是否符合指定的页面类型。

注意：页面类型信息本质是MetaData中一个key为”type”的附属信息，建议开发者在使用MetaData特性时不要将自定义的附属信息的key设置为”type”，以免发生冲突。

下面是一个完整的利用MatchType特性抓取豆瓣图书的代码：

import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.ram.RamCrawler;


/**
 * 
 * WebCollector 2.40新特性 page.matchType
 * 在添加CrawlDatum时（添加种子、或在抓取时向next中添加任务），
 * 可以为CrawlDatum设置type信息
 * 
 * type的本质也是meta信息，为CrawlDatum的附加信息
 * 在添加种子或向next中添加任务时，设置type信息可以简化爬虫的开发
 * 
 * 例如在处理列表页时，爬虫解析出内容页的链接，在将内容页链接作为后续任务
 * 将next中添加时，可设置其type信息为content（可自定义），在后续抓取中，
 * 通过page.matchType("content")就可判断正在解析的页面是否为内容页
 * 
 * 设置type的方法主要有3种：
 * 1）添加种子时，addSeed(url,type)
 * 2）向next中添加后续任务时：next.add(url,type)或next.add(links,type)
 * 3）在定义CrawlDatum时：crawlDatum.type(type)
 *
 * @author hu
 */
public class DemoTypeCrawler extends RamCrawler {

    /*
        该教程是DemoMetaCrawler的简化版

        该Demo爬虫需要应对豆瓣图书的三种页面：
        1）标签页（taglist，包含图书列表页的入口链接）
        2）列表页（booklist，包含图书详情页的入口链接）
        3）图书详情页（content）

        另一种常用的遍历方法可参考TutorialCrawler
     */
    @Override
    public void visit(Page page, CrawlDatums next) {

        if(page.matchType("taglist")){
            //如果是列表页，抽取内容页链接
            //将内容页链接的type设置为content，并添加到后续任务中
             next.add(page.links("table.tagCol td>a"),"booklist");
        }else if(page.matchType("booklist")){
            next.add(page.links("div.info>h2>a"),"content");
        }else if(page.matchType("content")){
            //处理内容页，抽取书名和豆瓣评分
            String title=page.select("h1>span").first().text();
            String score=page.select("strong.ll.rating_num").first().text();
            System.out.println("title:"+title+"\tscore:"+score);
        }

    }

    public static void main(String[] args) throws Exception {
        DemoTypeCrawler crawler = new DemoTypeCrawler();
        crawler.addSeed("https://book.douban.com/tag/","taglist");


        /*可以设置每个线程visit的间隔，这里是毫秒*/
        //crawler.setVisitInterval(1000);
        /*可以设置http请求重试的间隔，这里是毫秒*/
        //crawler.setRetryInterval(1000);
        crawler.setThreads(30);
        crawler.start(3);
    }

}