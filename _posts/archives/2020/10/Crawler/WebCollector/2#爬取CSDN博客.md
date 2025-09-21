Java开源爬虫框架WebCollector爬取CSDN博客
BY BRIEFCOPY · PUBLISHED 2016年4月25日 · UPDATED 2017年5月4日

新闻、博客爬取是数据采集中常见的需求，也是最容易实现的需求。一些开发者利用HttpClient和Jsoup等工具也可以实现这个需求，但大多数实现的是一个单线程爬虫，并且在URL去重和断点爬取这些功能上控制地不好，爬虫框架可以很好地解决这些问题，开源爬虫框架往往都自带稳定的线程池、URL去重机制和断点续爬功能。

爬虫框架往往也会自带网页解析功能，支持xpath或css选择器（底层多用Jsoup实现）。

使用爬虫框架，用户只需要告诉框架大致的爬取范围，以及每个页面的抽取规则，即可完成对网页的爬取，并抽取其中的结构化数据。

import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;

public class TutorialCrawler extends BreadthCrawler {

    public TutorialCrawler(String crawlPath, boolean autoParse) {
        super(crawlPath, autoParse);
    }

    /*
        可以往next中添加希望后续爬取的任务，任务可以是URL或者CrawlDatum
        爬虫不会重复爬取任务，从2.20版之后，爬虫根据CrawlDatum的key去重，而不是URL
        因此如果希望重复爬取某个URL，只要将CrawlDatum的key设置为一个历史中不存在的值即可
        例如增量爬取，可以使用 爬取时间+URL作为key。

        新版本中，可以直接通过 page.select(css选择器)方法来抽取网页中的信息，等价于
        page.getDoc().select(css选择器)方法，page.getDoc()获取到的是Jsoup中的
        Document对象，细节请参考Jsoup教程
    */
    @Override
    public void visit(Page page, CrawlDatums next) {
        if (page.matchUrl("http://blog.csdn.net/.*/article/details/.*")) {
            String title = page.select("div[class=article_title]").first().text();
            String author = page.select("div[id=blog_userface]").first().text();
            System.out.println("title:" + title + "\tauthor:" + author);
        }
    }

    public static void main(String[] args) throws Exception {
        TutorialCrawler crawler = new TutorialCrawler("crawler", true);
        crawler.addSeed("http://blog.csdn.net/");
        crawler.addRegex("http://blog.csdn.net/.*/article/details/.*");

        /*可以设置每个线程visit的间隔，这里是毫秒*/
        //crawler.setVisitInterval(1000);
        /*可以设置http请求重试的间隔，这里是毫秒*/
        //crawler.setRetryInterval(1000);

        crawler.setThreads(30);
        crawler.start(2);
    }

}