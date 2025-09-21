## 使用HttpClient和RestTemplate访问Http资源
-----

## 1. 使用HttpClient

```java
		CloseableHttpClient client = HttpClientBuilder.create().build();
		ArrayList<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("regionId","gz"));
		params.add(new BasicNameValuePair("limit","10"));

		URI uri = new URIBuilder().setScheme("https").setHost("open.didiyunapi.com")
				.setPath("/dicloud/i/compute/dc2/list")
				.setParameters(params)
				.build();

		HttpPost httpPost = new HttpPost(uri);
		httpPost.setHeader("Authorization","Bearer xxxxxx");

		CloseableHttpResponse resp = client.execute(httpPost);
		System.out.println(EntityUtils.toString(resp.getEntity()));
```

## 1. HttpMessageConverter

在使用RestTemplate作为客户端接收服务端的请求时，会经常遇到如下异常：

```bash
Could not extract response: no suitable HttpMessageConverter found for response type
```

如下示例：

```java
try {
 ResponseEntity responseEntity = restTemplate.getForEntity(url, Employee[].class);
 for (Employee employee : responseEntity.getBody()) {
  System.out.println(employee);
 }
} catch (Exception e) {
 e.printStackTrace();
 System.out.println("Error:-" + e.getMessage());
}
```

异常栈如下所示:

```java
org.springframework.web.client.RestClientException: Could not extract response: no suitable HttpMessageConverter found for response type [class [Lcom.technicalkeeda.bean.Employee;] and content type [application/octet-stream]
 at org.springframework.web.client.HttpMessageConverterExtractor.extractData(HttpMessageConverterExtractor.java:109)
 at org.springframework.web.client.RestTemplate$ResponseEntityResponseExtractor.extractData(RestTemplate.java:812)
 at org.springframework.web.client.RestTemplate$ResponseEntityResponseExtractor.extractData(RestTemplate.java:796)
 at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:576)
 at org.springframework.web.client.RestTemplate.execute(RestTemplate.java:529)
 at org.springframework.web.client.RestTemplate.getForEntity(RestTemplate.java:261)
 at com.technicalkeeda.test.App.getEmployees(App.java:79)
 at com.technicalkeeda.test.App.main(App.java:32)
```

## 2. 可以找到的解决方案有两种

## 方案1

通过ClientHttpRequestFactory去创建restTemplate的http连接，这个类继承自Apache HttpComponents HttpClient。

如下代码所示：

```java
ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault());
RestTemplate restTemplate = new RestTemplate(requestFactory);
```

同理，可以使用Apache HttpComponents HttpClient代替RestTemplate。

## 方案2

通过上面的报错可以得知没有http内容类型application/octe-stream对应的Http消息转换器，所以可以指定一个。

```java
RestTemplate restTemplate = new RestTemplate();
MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM));
restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);
```


## 3. 使用RestTemplate访问DiDi云主机资源

新建配置类

```java
package com.imsql.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DiDiRestTemplate {
    private static String Token = "3b1539e12fd35e729cd3e6e6a0c03bb7e84515200b3e5a2eb9723ea69bec4a2c";

    @Bean
    public UriComponentsBuilder getDiDiBaseUriComponent(){
        UriComponentsBuilder baseUriComponentsBuilder = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("open.didiyunapi.com")
                .cloneBuilder();
        return baseUriComponentsBuilder;
    }

    @Bean
    public RequestEntity<Void> getDiDiDc2RequestEntity(){
        LinkedMultiValueMap<String, String> stringStringLinkedMultiValueMap = new LinkedMultiValueMap<>();
        stringStringLinkedMultiValueMap.add("regionId","gz");
        stringStringLinkedMultiValueMap.add("start","0");
        stringStringLinkedMultiValueMap.add("limit","1000");


        UriComponentsBuilder dc2UriBuilder = getDiDiBaseUriComponent().queryParams(stringStringLinkedMultiValueMap)
                .path("/dicloud/i/compute/dc2/list")
                .cloneBuilder();

        UriComponents dc2UriComponent = dc2UriBuilder.build();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(Token);
        RequestEntity<Void> requestEn = RequestEntity.post(dc2UriComponent.toUri())
                .headers(httpHeaders)
                .build();

        return requestEn;
    }


    @Bean
    public RestTemplate getDiDiDc2Client(){

        RestTemplate restTemplate = new RestTemplate();
        List<HttpMessageConverter<?>> httpMessageConverters = new ArrayList<>();

        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
        ArrayList<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        gsonHttpMessageConverter.setSupportedMediaTypes(mediaTypes);
        httpMessageConverters.add(gsonHttpMessageConverter);
        restTemplate.setMessageConverters(httpMessageConverters);
        return restTemplate;
    }
}

```

使用客户端

```java
package com.imsql.test;
import com.google.gson.JsonObject;
import com.imsql.test.config.DiDiRestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

@SpringBootApplication
@Slf4j
public class TestApplication implements CommandLineRunner {

	@Autowired
    private DiDiRestTemplate diDiRestTemplate;

	public static void main(String[] args) {
		SpringApplicationBuilder springApplicationBuilder = new SpringApplicationBuilder(TestApplication.class);
		springApplicationBuilder.bannerMode(Banner.Mode.OFF);
		springApplicationBuilder.web(WebApplicationType.NONE);
		springApplicationBuilder.run();
	}

	@Override
	public void run(String... args) throws Exception {

		try {

			RequestEntity<Void> diDiDc2RequestEntity = diDiRestTemplate.getDiDiDc2RequestEntity();
			ResponseEntity<JsonObject> jsonObject = diDiRestTemplate.getDiDiDc2Client().exchange(diDiDc2RequestEntity, JsonObject.class);

			System.out.println(jsonObject.toString());
		}catch (RestClientException e){

			e.printStackTrace();
		}
	}
}

```