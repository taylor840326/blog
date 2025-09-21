## Jsoup概要
-----


### 1. 概要

jsoup是一款Java的HTML解析器，主要用来对HTML解析。

在爬虫的时候，当我们用HttpClient之类的框架，获取到网页源码之后，需要从网页源码中取出我们想要的内容，

就可以使用jsoup这类HTML解析器了。可以非常轻松的实现。

虽然jsoup也支持从某个地址直接去爬取网页源码，但是只支持HTTP，HTTPS协议，支持不够丰富。

所以，**主要还是用来对HTML进行解析**。

其中，**要被解析的HTML可以是一个HTML的字符串，可以是一个URL，可以是一个文件**。

org.jsoup.Jsoup把输入的HTML转换成一个org.jsoup.nodes.Document对象，然后从Document对象中取出想要的元素。

org.jsoup.nodes.Document继承了org.jsoup.nodes.Element，Element又继承了org.jsoup.nodes.Node类。里面提供了丰富的方法来获取HTML的元素。


### 2. 解析过程

### 2.1. 从URL获取HTML来解析

```java
Document doc = Jsoup.connect("http://www.baidu.com/").get();
String title = doc.title();
```

其中Jsoup.connect("xxx")方法返回一个org.jsoup.Connection对象。

在Connection对象中，我们可以执行get或者post来执行请求。但是在执行请求之前，我们可以使用Connection对象来设置一些请求信息。

比如：头信息，cookie，请求等待时间，代理等等来模拟浏览器的行为。

```java
Document doc = Jsoup.connect("http://example.com")
  .data("query", "Java")
  .userAgent("Mozilla")
  .cookie("auth", "token")
  .timeout(3000)
  .post();
```

### 2.2. 获取Document对象

获得Document对象后，接下来就是解析Document对象，并从中获取我们想要的元素了。

Document中提供了丰富的方法来获取指定元素。

### 2.2.1. 使用DOM的方式来取得

```text
getElementById(String id)：通过id来获取
getElementsByTag(String tagName)：通过标签名字来获取
getElementsByClass(String className)：通过类名来获取
getElementsByAttribute(String key)：通过属性名字来获取
getElementsByAttributeValue(String key, String value)：通过指定的属性名字，属性值来获取
getAllElements()：获取所有元素
```


### 2.2.2. 通过类似于css或jQuery的选择器来查找元素

使用的是Element类的下记方法：

```java
public Elements select(String cssQuery)
```

通过传入一个类似于CSS或jQuery的选择器字符串，来查找指定元素。

例子：

```java
File input = new File("/tmp/input.html");
Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");

Elements links = doc.select("a[href]"); //带有href属性的a元素
Elements pngs = doc.select("img[src$=.png]");
  //扩展名为.png的图片

Element masthead = doc.select("div.masthead").first();
  //class等于masthead的div标签

Elements resultLinks = doc.select("h3.r > a"); //在h3元素之后的a元素
```

### 2.2.3. 选择器的更多语法(可以在org.jsoup.select.Selector中查看到更多关于选择器的语法)：

```text
tagname: 通过标签查找元素，比如：a
　　ns|tag: 通过标签在命名空间查找元素，比如：可以用 fb|name 语法来查找 <fb:name> 元素
　　#id: 通过ID查找元素，比如：#logo
　　.class: 通过class名称查找元素，比如：.masthead
　　[attribute]: 利用属性查找元素，比如：[href]
　　[^attr]: 利用属性名前缀来查找元素，比如：可以用[^data-] 来查找带有HTML5 Dataset属性的元素
　　[attr=value]: 利用属性值来查找元素，比如：[width=500]
　　[attr^=value], [attr$=value], [attr=value]: 利用匹配属性值开头、结尾或包含属性值来查找元素，比如：[href=/path/]
　　[attr~=regex]: 利用属性值匹配正则表达式来查找元素，比如： img[src~=(?i).(png|jpe?g)]
　　*: 这个符号将匹配所有元素

Selector选择器组合使用
　　el#id: 元素+ID，比如： div#logo
　　el.class: 元素+class，比如： div.masthead
　　el[attr]: 元素+class，比如： a[href]
　　任意组合，比如：a[href].highlight
　　ancestor child: 查找某个元素下子元素，比如：可以用.body p 查找在"body"元素下的所有 p元素
　　parent > child: 查找某个父元素下的直接子元素，比如：可以用div.content > p 查找 p 元素，也可以用body > * 查找body标签下所有直接子元素
　　siblingA + siblingB: 查找在A元素之前第一个同级元素B，比如：div.head + div
　　siblingA ~ siblingX: 查找A元素之前的同级X元素，比如：h1 ~ p
　　el, el, el:多个选择器组合，查找匹配任一选择器的唯一元素，例如：div.masthead, div.logo

伪选择器selectors
　　:lt(n): 查找哪些元素的同级索引值（它的位置在DOM树中是相对于它的父节点）小于n，比如：td:lt(3) 表示小于三列的元素
　　:gt(n):查找哪些元素的同级索引值大于n，比如： div p:gt(2)表示哪些div中有包含2个以上的p元素
　　:eq(n): 查找哪些元素的同级索引值与n相等，比如：form input:eq(1)表示包含一个input标签的Form元素
　　:has(seletor): 查找匹配选择器包含元素的元素，比如：div:has(p)表示哪些div包含了p元素
　　:not(selector): 查找与选择器不匹配的元素，比如： div:not(.logo) 表示不包含 class="logo" 元素的所有 div 列表
　　:contains(text): 查找包含给定文本的元素，搜索不区分大不写，比如： p:contains(jsoup)
　　:containsOwn(text): 查找直接包含给定文本的元素
　　:matches(regex): 查找哪些元素的文本匹配指定的正则表达式，比如：div:matches((?i)login)
　　:matchesOwn(regex): 查找自身包含文本匹配指定正则表达式的元素

```

注意：
**上述伪选择器索引是从0开始的，也就是说第一个元素索引值为0，第二个元素index为1等**

◆通过上面的选择器，我们可以取得一个Elements对象，它继承了ArrayList对象，里面放的全是Element对象。

### 2.3. 取出需要的内容

接下来我们要做的就是从Element对象中，取出我们真正需要的内容。

通常有下面几种方法：

### 2.3.1. Element.text()

这个方法用来取得一个元素中的文本。

### 2.3.2. Element.html()或Node.outerHtml()

这个方法用来取得一个元素中的html内容

### 2.3.3. Node.attr(String key)

获得一个属性的值，例如取得超链接<a href="">中href的值

综合实例：采集开源中国项目信息

```java
package com.company;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws IOException {
        // write your code here
        Set<String> setUrls = new HashSet<>();
        for(int i = 1; i <= 5; i++)
        {
            String strUrl = "https://www.oschina.net/project/list?company=0&sort=score&lang=0&recommend=false&p="+i;
            setUrls.add(strUrl);
        }

        Set<String> setProjUrls = new HashSet<>();
        for(String stringUrl : setUrls)
        {
            Document document = Jsoup.connect(stringUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; rv:30.0) Gecko/20100101 Firefox/30.0")
                    .get();
            //  System.out.println(document);
            Elements elements = document.select("div.box.item");
            for(Element element : elements)
            {
                Elements eleUrl = element.select("div.box-aw a");
                String strPrjUrl = eleUrl.attr("href");
                setProjUrls.add(strPrjUrl);
                //  System.out.println(strPrjUrl);
                Elements eleTitle = eleUrl.select(".title");
                String strTitle = eleTitle.text();
                // System.out.println(strTitle);
                Elements eleSummary = eleUrl.select(".summary");
                String strSummary = eleSummary.text();
                //  System.out.println(strSummary);
            }
        }

        for(String stringUrl : setProjUrls)
        {
            Document document = Jsoup.connect(stringUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; rv:30.0) Gecko/20100101 Firefox/30.0")
                    .get();
            Elements elements = document.select("div.box-aw a h1");

            String strTitle = elements.text();
            System.out.println("标题：" + strTitle);

            Elements elementsSection = document.select("section.list");

            int nSize = elementsSection.get(0).children().size();

            if(nSize == 0)
                continue;

            Element elementProtocol = elementsSection.get(0).child(0);
            Elements elesPro = elementProtocol.select("span");
            String strPro = elesPro.text();
            System.out.println("开源协议：" + strPro);

            nSize--;
            if(nSize == 0)
                continue;

            Element elementLan = elementsSection.get(0).child(1);
            Elements elesLan = elementLan.select("span").get(0).children();
            StringBuilder strlan = new StringBuilder();
            for(Element ele : elesLan)
            {
                String strLanTemp = ele.text();
                if(strLanTemp.indexOf("查看源码")>=0)
                    break;
                strlan.append(strLanTemp+",");
            }
            if(elesLan.size()>0)
            {
                String strLanguage = strlan.toString().substring(0,strlan.length()-1);
                System.out.println("开发语言：" + strLanguage);
            }


            nSize--;
            if(nSize == 0)
                continue;

            Element elementOS = elementsSection.get(0).child(2);
            Elements elesOS = elementOS.select("span");
            String strOS = elesOS.text();
            System.out.println("操作系统：" + strOS);

            nSize--;
            if(nSize == 0)
                continue;

            Element elementAuthor = elementsSection.get(0).child(3);
            Elements elesAuthor = elementAuthor.select("a.link");
            String strAuthor= elesAuthor.text();
            System.out.println("软件作者；" + strAuthor);

            System.out.println("---------------------");
        }

    }
}
```

爬取腾讯首页全部图片

```java
package com.company;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class meizi {
    /**
     * 下载图片到指定目录
     *
     * @param filePath 文件路径
     * @param imgUrl   图片URL
     */
    public static void downImages(String filePath, String imgUrl) {
        // 若指定文件夹没有，则先创建
        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 截取图片文件名
        String fileName = imgUrl.substring(imgUrl.lastIndexOf('/') + 1, imgUrl.length());

        try {
            // 文件名里面可能有中文或者空格，所以这里要进行处理。但空格又会被URLEncoder转义为加号
            String urlTail = URLEncoder.encode(fileName, "UTF-8");
            // 因此要将加号转化为UTF-8格式的%20
            imgUrl = imgUrl.substring(0, imgUrl.lastIndexOf('/') + 1) + urlTail.replaceAll("\\+", "\\%20");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 写出的路径
        File file = new File(filePath + File.separator + fileName);

        try {
            // 获取图片URL
            URL url = new URL(imgUrl);
            // 获得连接
            URLConnection connection = url.openConnection();
            // 设置10秒的相应时间
            connection.setConnectTimeout(10 * 1000);
            // 获得输入流
            InputStream in = connection.getInputStream();
            // 获得输出流
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            // 构建缓冲区
            byte[] buf = new byte[1024];
            int size;
            // 写入到文件
            while (-1 != (size = in.read(buf))) {
                out.write(buf, 0, size);
            }
            out.close();
            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        // 利用Jsoup获得连接
        Connection connect = Jsoup.connect("http://www.qq.com");
        try {
            // 得到Document对象
            Document document = connect.get();
            // 查找所有img标签
            Elements imgs = document.getElementsByTag("img");
            System.out.println("共检测到下列图片URL：");
            System.out.println("开始下载");
            // 遍历img标签并获得src的属性
            for (Element element : imgs) {
                //获取每个img标签URL "abs:"表示绝对路径
                String imgSrc = element.attr("abs:src");
                // 打印URL
                System.out.println(imgSrc);
                //下载图片到本地
                meizi.downImages("d:/img", imgSrc);
            }
            System.out.println("下载完成");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

解析json(悟空问答网案例)

```java
package com.company;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

/**
 * Created by Administrator on 2018/8/8.
 */
public class hello {
    public static void main(String[] args) throws IOException {

        Connection.Response res = Jsoup.connect("https://www.wukong.com/wenda/web/nativefeed/brow/?concern_id=6300775428692904450&t=1533714730319&_signature=DKZ7mhAQV9JbkTPBachKdgyme4")
                .header("Accept", "*/*")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Accept-Language","zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
                .header("Content-Type", "application/json;charset=UTF-8")
                .header("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:48.0) Gecko/20100101 Firefox/48.0")
                .timeout(10000).ignoreContentType(true).execute();//.get();
        String body = res.body();
        System.out.println(body);
        JSONObject jsonObject2 = JSON.parseObject(body);
        JSONArray jsonArray = jsonObject2.getJSONArray("data");

        //JSONArray jsonArray1 = JSONArray.parseArray(JSON_ARRAY_STR);//因为JSONArray继承了JSON，所以这样也是可以的

        //遍历方式1
        int size = jsonArray.size();
        for (int i = 0; i < size; i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if(jsonObject.containsKey("question"))
            {
                JSONObject jsonObject3 = jsonObject.getJSONObject("question");
                String qid = jsonObject3.getString("qid");
                System.out.println(qid);
            }

        }
    }
}
```

### 4. fastjson补充

json字符串-数组类型与JSONArray之间的转换

```java
/**
     * json字符串-数组类型与JSONArray之间的转换
     */
    public static void testJSONStrToJSONArray(){

        JSONArray jsonArray = JSON.parseArray(JSON_ARRAY_STR);
        //JSONArray jsonArray1 = JSONArray.parseArray(JSON_ARRAY_STR);//因为JSONArray继承了JSON，所以这样也是可以的

        //遍历方式1
        int size = jsonArray.size();
        for (int i = 0; i < size; i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            System.out.println(jsonObject.getString("studentName")+":"+jsonObject.getInteger("studentAge"));
        }

        //遍历方式2
        for (Object obj : jsonArray) {
            JSONObject jsonObject = (JSONObject) obj;
            System.out.println(jsonObject.getString("studentName")+":"+jsonObject.getInteger("studentAge"));
        }
    }
```

复杂json格式字符串与JSONObject之间的转换

```java
/**
     * 复杂json格式字符串与JSONObject之间的转换
     */
    public static void testComplexJSONStrToJSONObject(){

        JSONObject jsonObject = JSON.parseObject(COMPLEX_JSON_STR);
        //JSONObject jsonObject1 = JSONObject.parseObject(COMPLEX_JSON_STR);//因为JSONObject继承了JSON，所以这样也是可以的
        
        String teacherName = jsonObject.getString("teacherName");
        Integer teacherAge = jsonObject.getInteger("teacherAge");
        JSONObject course = jsonObject.getJSONObject("course");
        JSONArray students = jsonObject.getJSONArray("students");

    }
```

另一个实例（采集悟空问答某个问题的评论信息）

```java
package com.company;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;

public class meizi {


    public static void main(String[] args) {
        // 利用Jsoup获得连接
        Connection connect = Jsoup.connect("https://www.wukong.com/question/6586953004245582083/");
        try {
            // 得到Document对象
            Document document = connect.get();

            Elements elements = document.select(".question-name");
            System.out.println(elements.get(0).text());

            Elements elements2 = document.select(".answer-item");
            for(Element element : elements2)
            {
                Elements elements3  = element.select(".answer-user-avatar img");
                System.out.println(elements3.attr("abs:src"));

                elements3  = element.select(".answer-user-name");
                System.out.println(elements3.text());

                elements3  = element.select(".answer-user-tag");
                System.out.println(elements3.text());

                elements3  = element.select(".answer-text");
                System.out.println(elements3.text());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```


### 参考资料

作者：数据萌新
链接：https://www.jianshu.com/p/fd5caaaa950d
来源：简书
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。