package com.imsql.crawldemo.compent;

import com.imsql.crawldemo.entity.GoodItems;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class HtmlParseUtils {
    public List<GoodItems> parse(String keyword) throws Exception{
        String url = "https://search.jd.com/Search?keyword=" + keyword + "&enc=utf-8";
        Document parser = Jsoup.parse(new URL(url), 3000);
        Element goodsList = parser.getElementById("J_goodsList");
        Elements elements = goodsList.getElementsByTag("li");
        ArrayList<GoodItems> list = new ArrayList<>();
        for (Element element : elements) {
            String images = element.getElementsByTag("img").eq(0).attr("src");
            if(images == null || images.length()==0){
                continue;
            }
            GoodItems goodItems = new GoodItems();
            String rawPrice= element.getElementsByClass("p-price").eq(0).text();
            String title = element.getElementsByClass("p-name").text();
            goodItems.setTitle(title);
            goodItems.setImage(images);
            goodItems.setPrice(rawPrice);
            list.add(goodItems);
        }
        return list;
    }
}
