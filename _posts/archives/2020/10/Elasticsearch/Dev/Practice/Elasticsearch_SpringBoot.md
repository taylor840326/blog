使用elasticsearch搭建自己的搜索系统

分类: ElasticSearchundefined

💛es搜索系统封装源码,走过路过,请帮我点个star哦！
💛原文地址为https://www.cnblogs.com/haixiang/p/12451703.html，转载请注明出处!

什么是elasticsearch#
Elasticsearch 是一个开源的高度可扩展的全文搜索和分析引擎，拥有查询近实时的超强性能。

大名鼎鼎的Lucene 搜索引擎被广泛用于搜索领域，但是操作复杂繁琐，总是让开发者敬而远之。而 Elasticsearch将 Lucene 作为其核心来实现所有索引和搜索的功能，通过简单的 RESTful 语法来隐藏掉 Lucene 的复杂性，从而让全文搜索变得简单

ES在Lucene基础上，提供了一些分布式的实现：集群，分片，复制等。

搜索为什么不用MySQL而用es#
我们本文案例是一个迷你商品搜索系统，为什么不考虑使用MySQL来实现搜索功能呢？原因如下：

MySQL默认使用innodb引擎，底层采用b+树的方式来实现，而Es底层使用倒排索引的方式实现，使用倒排索引支持各种维度的分词，可以掌控不同粒度的搜索需求。（MYSQL8版本也支持了全文检索，使用倒排索引实现，有兴趣可以去看看两者的差别）
如果使用MySQL的%key%的模糊匹配来与es的搜索进行比较，在8万数据量时他们的耗时已经达到40:1左右，毫无疑问在速度方面es完胜。
es在大厂中的应用情况#
es运用最广泛的是elk组合来对日志进行搜索分析
58安全部门、京东订单中心几乎全采用es来完成相关信息的存储与检索
es在tob的项目中也用于各种检索与分析
在c端产品中，企业通常自己基于Lucene封装自己的搜索系统，为了适配公司营销战略、推荐系统等会有更多定制化的搜索需求
es客户端选型#
spring-boot-starter-data-elasticsearch#
我相信你看到的网上各类公开课视频或者小项目均推荐使用这款springboot整合过的es客户端，但是我们要say no！



此图是引入的最新版本的依赖，我们可以看到它所使用的es-high-client也为6.8.7，而es7.x版本都已经更新很久了，这里许多新特性都无法使用，所以版本滞后是他最大的问题。而且它的底层也是highclient，我们操作highclient可以更灵活。我呆过的两个公司均未采用此客户端。

elasticsearch-rest-high-level-client#
这是官方推荐的客户端，支持最新的es，其实使用起来也很便利，因为是官方推荐所以在特性的操作上肯定优于前者。而且该客户端与TransportClient不同，不存在并发瓶颈的问题，官方首推，必为精品！

搭建自己的迷你搜索系统#
引入es相关依赖，除此之外需引入springboot-web依赖、jackson依赖以及lombok依赖等。

Copy
    <properties>
        <es.version>7.3.2</es.version>
    </properties>
		<!-- high client-->
	<dependency>
        <groupId>org.elasticsearch.client</groupId>
        <artifactId>elasticsearch-rest-high-level-client</artifactId>
        <version>${es.version}</version>
        <exclusions>
            <exclusion>
                <groupId>org.elasticsearch.client</groupId>
                <artifactId>elasticsearch-rest-client</artifactId>
            </exclusion>
            <exclusion>
                <groupId>org.elasticsearch</groupId>
                <artifactId>elasticsearch</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>org.elasticsearch</groupId>
        <artifactId>elasticsearch</artifactId>
        <version>${es.version}</version>
    </dependency>

    <!--rest low client high client以来低版本client所以需要引入-->
    <dependency>
        <groupId>org.elasticsearch.client</groupId>
        <artifactId>elasticsearch-rest-client</artifactId>
        <version>${es.version}</version>
    </dependency>
es配置文件es-config.properties

Copy
es.host=localhost
es.port=9200
es.token=es-token
es.charset=UTF-8
es.scheme=http

es.client.connectTimeOut=5000
es.client.socketTimeout=15000
封装RestHighLevelClient

Copy
@Configuration
@PropertySource("classpath:es-config.properties")
public class RestHighLevelClientConfig {

    @Value("${es.host}")
    private String host;
    @Value("${es.port}")
    private int port;
    @Value("${es.scheme}")
    private String scheme;
    @Value("${es.token}")
    private String token;
    @Value("${es.charset}")
    private String charSet;
    @Value("${es.client.connectTimeOut}")
    private int connectTimeOut;
    @Value("${es.client.socketTimeout}")
    private int socketTimeout;

    @Bean
    public RestClientBuilder restClientBuilder() {
        RestClientBuilder restClientBuilder = RestClient.builder(
                new HttpHost(host, port, scheme)
        );

        Header[] defaultHeaders = new Header[]{
                new BasicHeader("Accept", "*/*"),
                new BasicHeader("Charset", charSet),
                //设置token 是为了安全 网关可以验证token来决定是否发起请求 我们这里只做象征性配置
                new BasicHeader("E_TOKEN", token)
        };
        restClientBuilder.setDefaultHeaders(defaultHeaders);
        restClientBuilder.setFailureListener(new RestClient.FailureListener(){
            @Override
            public void onFailure(Node node) {
                System.out.println("监听某个es节点失败");
            }
        });
        restClientBuilder.setRequestConfigCallback(builder ->
                builder.setConnectTimeout(connectTimeOut).setSocketTimeout(socketTimeout));
        return restClientBuilder;
    }

    @Bean
    public RestHighLevelClient restHighLevelClient(RestClientBuilder restClientBuilder) {
        return new RestHighLevelClient(restClientBuilder);
    }
}
封装es常用操作es搜索系统封装源码

Copy
@Service
public class RestHighLevelClientService {
    
    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private ObjectMapper mapper;

    /**
     * 创建索引
     * @param indexName
     * @param settings
     * @param mapping
     * @return
     * @throws IOException
     */
    public CreateIndexResponse createIndex(String indexName, String settings, String mapping) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        if (null != settings && !"".equals(settings)) {
            request.settings(settings, XContentType.JSON);
        }
        if (null != mapping && !"".equals(mapping)) {
            request.mapping(mapping, XContentType.JSON);
        }
        return client.indices().create(request, RequestOptions.DEFAULT);
    }

    /**
     * 判断 index 是否存在
     */
    public boolean indexExists(String indexName) throws IOException {
        GetIndexRequest request = new GetIndexRequest(indexName);
        return client.indices().exists(request, RequestOptions.DEFAULT);
    }
    
    /**
     * 搜索
    */
    public SearchResponse search(String field, String key, String rangeField, String 
                                 from, String to,String termField, String termVal, 
                                 String ... indexNames) throws IOException{
        SearchRequest request = new SearchRequest(indexNames);

        SearchSourceBuilder builder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(new MatchQueryBuilder(field, key)).must(new RangeQueryBuilder(rangeField).from(from).to(to)).must(new TermQueryBuilder(termField, termVal));
        builder.query(boolQueryBuilder);
        request.source(builder);
        log.info("[搜索语句为:{}]",request.source().toString());
        return client.search(request, RequestOptions.DEFAULT);
    }

    /**
     * 批量导入
     * @param indexName
     * @param isAutoId 使用自动id 还是使用传入对象的id
     * @param source
     * @return
     * @throws IOException
     */
    public BulkResponse importAll(String indexName, boolean isAutoId, String  source) throws IOException{
        if (0 == source.length()){
            //todo 抛出异常 导入数据为空
        }
        BulkRequest request = new BulkRequest();
        JsonNode jsonNode = mapper.readTree(source);

        if (jsonNode.isArray()) {
            for (JsonNode node : jsonNode) {
                if (isAutoId) {
                    request.add(new IndexRequest(indexName).source(node.asText(), XContentType.JSON));
                } else {
                    request.add(new IndexRequest(indexName)
                            .id(node.get("id").asText())
                            .source(node.asText(), XContentType.JSON));
                }
            }
        }
        return client.bulk(request, RequestOptions.DEFAULT);
    }
创建索引，这里的settings是设置索引是否设置复制节点、设置分片个数，mappings就和数据库中的表结构一样，用来指定各个字段的类型，同时也可以设置字段是否分词（我们这里使用ik中文分词器）、采用什么分词方式。

Copy
   @Test
    public void createIdx() throws IOException {
        String settings = "" +
                "  {\n" +
                "      \"number_of_shards\" : \"2\",\n" +
                "      \"number_of_replicas\" : \"0\"\n" +
                "   }";
        String mappings = "" +
                "{\n" +
                "    \"properties\": {\n" +
                "      \"itemId\" : {\n" +
                "        \"type\": \"keyword\",\n" +
                "        \"ignore_above\": 64\n" +
                "      },\n" +
                "      \"urlId\" : {\n" +
                "        \"type\": \"keyword\",\n" +
                "        \"ignore_above\": 64\n" +
                "      },\n" +
                "      \"sellAddress\" : {\n" +
                "        \"type\": \"text\",\n" +
                "        \"analyzer\": \"ik_max_word\", \n" +
                "        \"search_analyzer\": \"ik_smart\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\" : {\"ignore_above\" : 256, \"type\" : \"keyword\"}\n" +
                "        }\n" +
                "      },\n" +
                "      \"courierFee\" : {\n" +
                "        \"type\": \"text\n" +
                "      },\n" +
                "      \"promotions\" : {\n" +
                "        \"type\": \"text\",\n" +
                "        \"analyzer\": \"ik_max_word\", \n" +
                "        \"search_analyzer\": \"ik_smart\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\" : {\"ignore_above\" : 256, \"type\" : \"keyword\"}\n" +
                "        }\n" +
                "      },\n" +
                "      \"originalPrice\" : {\n" +
                "        \"type\": \"keyword\",\n" +
                "        \"ignore_above\": 64\n" +
                "      },\n" +
                "      \"startTime\" : {\n" +
                "        \"type\": \"date\",\n" +
                "        \"format\": \"yyyy-MM-dd HH:mm:ss\"\n" +
                "      },\n" +
                "      \"endTime\" : {\n" +
                "        \"type\": \"date\",\n" +
                "        \"format\": \"yyyy-MM-dd HH:mm:ss\"\n" +
                "      },\n" +
                "      \"title\" : {\n" +
                "        \"type\": \"text\",\n" +
                "        \"analyzer\": \"ik_max_word\", \n" +
                "        \"search_analyzer\": \"ik_smart\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\" : {\"ignore_above\" : 256, \"type\" : \"keyword\"}\n" +
                "        }\n" +
                "      },\n" +
                "      \"serviceGuarantee\" : {\n" +
                "        \"type\": \"text\",\n" +
                "        \"analyzer\": \"ik_max_word\", \n" +
                "        \"search_analyzer\": \"ik_smart\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\" : {\"ignore_above\" : 256, \"type\" : \"keyword\"}\n" +
                "        }\n" +
                "      },\n" +
                "      \"venue\" : {\n" +
                "        \"type\": \"text\",\n" +
                "        \"analyzer\": \"ik_max_word\", \n" +
                "        \"search_analyzer\": \"ik_smart\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\" : {\"ignore_above\" : 256, \"type\" : \"keyword\"}\n" +
                "        }\n" +
                "      },\n" +
                "      \"currentPrice\" : {\n" +
                "        \"type\": \"keyword\",\n" +
                "        \"ignore_above\": 64\n" +
                "      }\n" +
                "   }\n" +
                "}";
        clientService.createIndex("idx_item", settings, mappings);
    }
分词技巧：

索引时最小分词，搜索时最大分词，例如"Java知音"索引时分词包含Java、知音、音、知等，最小粒度分词可以让我们匹配更多的检索需求，但是我们搜索时应该设置最大分词，用“Java”和“知音”去匹配索引库，得到的结果更贴近我们的目的，
对分词字段同时也设置keyword，便于后续排查错误时可以精确匹配搜索，快速定位。
我们向es导入十万条淘宝双11活动数据作为我们的样本数据，数据结构如下所示

Copy
{
	"_id": "https://detail.tmall.com/item.htm?id=538528948719\u0026skuId=3216546934499",
	"卖家地址": "上海",
	"快递费": "运费: 0.00元",
	"优惠活动": "满199减10,满299减30,满499减60,可跨店",
	"商品ID": "538528948719",
	"原价": "2290.00",
	"活动开始时间": "2016-11-11 00:00:00",
	"活动结束时间": "2016-11-11 23:59:59",
	"标题": "【天猫海外直营】 ReFa CARAT RAY 黎珐 双球滚轮波光美容仪",
	"服务保障": "正品保证;赠运费险;极速退款;七天退换",
	"会场": "进口尖货",
	"现价": "1950.00"
}
调用上面封装的批量导入方法进行导入

Copy
    @Test
    public void importAll() throws IOException {
        clientService.importAll("idx_item", true, itemService.getItemsJson());
    }
我们调用封装的搜索方法进行搜索，搜索产地为武汉、价格在11-149之间的相关酒产品，这与我们淘宝中设置筛选条件搜索商品操作一致。

Copy
    @Test
    public void search() throws IOException {
        SearchResponse search = clientService.search("title", "酒", "currentPrice",
                "11", "149", "sellAddress", "武汉");
        SearchHits hits = search.getHits();
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit documentFields : hits1) {
            System.out.println( documentFields.getSourceAsString());
        }
    }
我们得到以下搜索结果，其中_score为某一项的得分，商品就是按照它来排序。

Copy
    {
      "_index": "idx_item",
      "_type": "_doc",
      "_id": "Rw3G7HEBDGgXwwHKFPCb",
      "_score": 10.995819,
      "_source": {
        "itemId": "525033055044",
        "urlId": "https://detail.tmall.com/item.htm?id=525033055044&skuId=def",
        "sellAddress": "湖北武汉",
        "courierFee": "快递: 0.00",
        "promotions": "满199减10,满299减30,满499减60,可跨店",
        "originalPrice": "3768.00",
        "startTime": "2016-11-01 00:00:00",
        "endTime": "2016-11-11 23:59:59",
        "title": "酒嗨酒 西班牙原瓶原装进口红酒蒙德干红葡萄酒6只装整箱送酒具",
        "serviceGuarantee": "破损包退;正品保证;公益宝贝;不支持7天退换;极速退款",
        "venue": "食品主会场",
        "currentPrice": "151.00"
      }
    }
扩展性思考#
商品搜索权重扩展，我们可以利用多种收费方式智能为不同店家提供增加权重，增加曝光度适应自身的营销策略。同时我们经常发现淘宝搜索前列的商品许多为我们之前查看过的商品，这是通过记录用户行为，跑模型等方式智能为这些商品增加权重。
分词扩展，也许因为某些商品的特殊性，我们可以自定义扩展分词字典，更精准、人性化的搜索。
高亮功能，es提供highlight高亮功能，我们在淘宝上看到的商品展示中对搜索关键字高亮，就是通过这种方式来实现。高亮使用方式
作者： 海向

出处：https://www.cnblogs.com/haixiang/p/12867160.html

本站使用「CC BY 4.0」创作共享协议，转载请在文章明显位置注明作者及出处。

« 上一篇： elasticesearch搜索返回高亮关键字
» 下一篇： java8时间处理
posted @ 2020-05-11 09:36  海向  阅读(849)  评论(0)  编辑  收藏
注册用户登录后才能发表评