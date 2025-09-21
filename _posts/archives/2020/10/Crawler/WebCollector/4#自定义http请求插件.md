WebCollector从2.72版本开始，默认使用OkHttpRequester作为Http请求插件。继承OkHttpRequester可以轻松地定制各种Http请求功能，如设置User-Agent、Cookie等Http请求头，设置请求方法（GET/POST）和表单数据等。

官网地址：https://github.com/CrawlScript/WebCollector

OkHttpRequester是一个Requester插件（Http请求插件），这里补充一下，Requester插件的源码如下：

public interface Requester{
 
     Page getResponse(String url) throws Exception;
 
     Page getResponse(CrawlDatum datum) throws Exception;
}
其中第一个方法Page getResponse(String url)是第二个方法Page getResponse(CrawlDatum datum)的一个快捷方式。一般情况下，第一个方法的实现方式如下，只是为了给用户一个能输入url字符串来请求响应的接口。

@Override
public Page getResponse(String url) throws Exception {
    return getResponse(new CrawlDatum(url));
}
OkHttpRequester已经实现了Page getResponse(CrawlDatum datum)方法，继承OkHttpRequester来定制Http请求一般只需要覆盖它的Request.Builder createRequestBuilder(CrawlDatum crawlDatum)方法。在每次发送Http请求前，都需要为OkHttp创建一个Request.Builder对象，它负责构建当前这次Http请求的Request（请求）。Http头信息、Http请求方式以及表单数据等都需要在Request.Builder中设置。因此覆盖OkHttpRequester的Request.Builder createRequestBuilder(CrawlDatum crawlDatum)方法就可以满足大部分的Http请求定制需求。

下面的例子继承了OkHttpRequester做了一个简单的可以定制User-Agent和Cookie的Requester插件： 

import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.net.OkHttpRequester;
import cn.edu.hfut.dmic.webcollector.plugin.rocks.BreadthCrawler;
import okhttp3.Request;
 
/**
 * 教程：使用WebCollector自定义Http请求
 * 可以自定义User-Agent和Cookie
 *
 * @author hu
 */
public class GithubCrawler extends BreadthCrawler {
 
    // 自定义的请求插件
    // 可以自定义User-Agent和Cookie
    public static class MyRequester extends OkHttpRequester {
 
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36";
        String cookie = "name=abcdef";
 
        // 每次发送请求前都会执行这个方法来构建请求
        @Override
        public Request.Builder createRequestBuilder(CrawlDatum crawlDatum) {
            // 这里使用的是OkHttp中的Request.Builder
            // 可以参考OkHttp的文档来修改请求头
            return super.createRequestBuilder(crawlDatum)
                    .addHeader("User-Agent", userAgent)
                    .addHeader("Cookie", cookie);
        }
 
    }
    
    public GithubCrawler(String crawlPath) {
        super(crawlPath, true);
 
        // 设置请求插件
        setRequester(new MyRequester());
 
        // 爬取github about下面的网页
        addSeed("https://github.com/about");
        addRegex("https://github.com/about/.*");
 
    }
 
    public void visit(Page page, CrawlDatums crawlDatums) {
        System.out.println(page.doc().title());
    }
 
    public static void main(String[] args) throws Exception {
        GithubCrawler crawler = new GithubCrawler("crawl");
        crawler.start(2);
    }
}