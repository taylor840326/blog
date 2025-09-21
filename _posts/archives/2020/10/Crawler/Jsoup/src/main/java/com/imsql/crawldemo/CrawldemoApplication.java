package com.imsql.crawldemo;

import com.imsql.crawldemo.compent.HtmlParseUtils;
import com.imsql.crawldemo.entity.GoodItems;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@SpringBootApplication
public class CrawldemoApplication implements CommandLineRunner {

    @Autowired
    HtmlParseUtils htmlParseUtils;

    public static void main(String[] args) {
        SpringApplication.run(CrawldemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        RestClientBuilder client = RestClient.builder(
                new HttpHost("127.0.0.1", 9200, "http")
        );
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(client);

        BulkRequest bulkRequest = new BulkRequest();

        String[] keywords = {"华为", "小米", "dell", "lenovo", "羽绒服", "电饭锅", "有机", "电脑数码"};

        for (String keyword : keywords) {
            List<GoodItems> goods = htmlParseUtils.parse(keyword);
            for (GoodItems good : goods) {
                HashMap<String, Object> stringStringHashMap = new HashMap<>();
                stringStringHashMap.put("title", good.getTitle());
                stringStringHashMap.put("image", good.getImage());
                stringStringHashMap.put("price", good.getPrice());

                bulkRequest.add(new IndexRequest("goods").source(stringStringHashMap));
            }
            BulkResponse responses = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            System.out.println(responses.buildFailureMessage());
        }

    }
}
