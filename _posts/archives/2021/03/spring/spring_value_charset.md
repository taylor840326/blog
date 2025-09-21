## Spring的Value注解读取properties配置文件，中文出现乱码的解释
-----


SpringBoot使用@Value读取.properties中文乱码及解决方法

EricZeng05 2019-07-19 08:34:50  7569  收藏 15
分类专栏： springboot 文章标签： springboot @Value properties 编码 乱码
版权
问题重现
某不知名springboot小项目，application.properties文件：

custom.param=中文属性值
1
java代码：

@SpringBootApplication
public class Application {

    @Value("${custom.param}")
    private String param;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void printText() throws UnsupportedEncodingException {
        System.out.println(param);
        System.out.println(new String(param.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
    }
}
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
控制台输出：

ä¸­æ–‡å±žæ€§å€¼
中文属性值
1
2
结论
先写结论：用@Value注解读取application.properties文件时，编码默认是ISO-8859-1，所以直接配置中文一定会乱码。注意，配置文件是springboot默认的配置文件application.properties或application-{active}.properties。其他配置文件会在原因分析中进行详解，原因分析涉及大量源码解读，如果不想烧脑深入分析的话可以直接跳到解决方案一节。

写在前面
写本文时，我查了网上几乎所有关于@Value读取.properties中文乱码的文章。一种思路是修改编码格式；另外一种是利用插件/IDE将中文预先编码，在注入到变量后直接转码为所需要的中文。遇到中文乱码修改编码方式是常规思路，所以第一种思路看似没问题，但是把springboot所有关于encoding的配置参数修改为UTF-8后，中文乱码的问题依然没有解决。

原因分析
Spring Boot版本：2.1.1.RELEASE

application.properties采用ISO-8859-1加载
自定义test.properties可以设置编码格式
.yml/.yaml默认采用UTF-8加载
application.properties文件加载
正如前文所述读取配置文件时，编码出现了问题。追踪一下spring boot是加载默认配置文件的过程，会发现org.springframework.boot.contex.config.ConfigFileApplicationListener类的loadDocuments()方法，源码如下：

private List<Document> loadDocuments(PropertySourceLoader loader, String name, Resource resource) throws IOException {
    DocumentsCacheKey cacheKey = new DocumentsCacheKey(loader, resource);
    List<Document> documents = this.loadDocumentsCache.get(cacheKey);
    if (documents == null) {
        List<PropertySource<?>> loaded = loader.load(name, resource);
        documents = asDocuments(loaded);
        this.loadDocumentsCache.put(cacheKey, documents);
    }
    return documents;
}
1
2
3
4
5
6
7
8
9
10
入参loader的类型是PropertySourceLoader，PropertySourceLoader是加载属性文件的接口，其实现有两个类：PropertiesPropertySourceLoader和YamlPropertySourceLoader。loader根据传入参数的实例调用load()方法，此处我们讨论.properties文件，接口声明和properties加载实现如下：

/*********属性文件加载接口**********/
public interface PropertySourceLoader {
    String[] getFileExtensions();
    List<PropertySource<?>> load(String name, Resource resource) throws IOException;
}
/*********properties文件加载实现**********/
public class PropertiesPropertySourceLoader implements PropertySourceLoader {

    private static final String XML_FILE_EXTENSION = ".xml";

    @Override
    public String[] getFileExtensions() {
        return new String[] { "properties", "xml" };
    }

    @Override
    public List<PropertySource<?>> load(String name, Resource resource) throws IOException {
        Map<String, ?> properties = loadProperties(resource);
        if (properties.isEmpty()) {
            return Collections.emptyList();
        }
      return Collections.singletonList(new OriginTrackedMapPropertySource(name, properties));
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private Map<String, ?> loadProperties(Resource resource) throws IOException {
      String filename = resource.getFilename();
      if (filename != null && filename.endsWith(XML_FILE_EXTENSION)) {
          return (Map) PropertiesLoaderUtils.loadProperties(resource);
      }
      return new OriginTrackedPropertiesLoader(resource).load();
  }

}
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
33
34
通过源码分析PropertiesPropertySourceLoader并不单纯的加载.properties文件，还包含.xml文件（似乎有违单一功能原则，不知道当初这样设计的初衷是啥）。顺着load()方法向下找->loadProperties(Resource)->OriginTrackedPropertiesLoader.load()->OriginTrackedPropertiesLoader.load(boolean)->OriginTrackedPropertiesLoader$CharacterReader(Resource)。
CharacterReader是OriginTrackedPropertiesLoader的内部静态类，而且只有一个构造函数，看看器构造参数就不难发现为啥application.properties是以ISO-8859-1编码加载的了：

private static class CharacterReader implements Closeable {
    // 其他代码省略
    CharacterReader(Resource resource) throws IOException {
      this.reader = new LineNumberReader(new InputStreamReader(
          resource.getInputStream(), StandardCharsets.ISO_8859_1));
    }
    // 其他代码省略
}
1
2
3
4
5
6
7
8
也就是说不论application.properties文件被设置为哪种编码格式，最终还是以ISO-8859-1的编码格式进行加载。

yml/yaml默认以UTF-8加载
让我们再看看yml/yaml格式的文件，其加载由PropertySourceLoader接口的另外一个实例YamlPropertySourceLoader实现，即接口方法load()：

List<PropertySource<?>> load(String name, Resource resource) throws IOException;
1
追一下load()的底层实现，采用org.yaml.snakeyaml.reader.UnicodeReader的实例对yml/ymal文件进行加载，而UnicodeReader实例对文件的初始化方法init()实现如下：

protected void init() throws IOException {
    if (internalIn2 != null)
        return;

    Charset encoding;
    byte bom[] = new byte[BOM_SIZE];
    int n, unread;
    n = internalIn.read(bom, 0, bom.length);

    if ((bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB) && (bom[2] == (byte) 0xBF)) {
        encoding = UTF8;
        unread = n - 3;
    } else if ((bom[0] == (byte) 0xFE) && (bom[1] == (byte) 0xFF)) {
        encoding = UTF16BE;
        unread = n - 2;
    } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)) {
        encoding = UTF16LE;
        unread = n - 2;
    } else {
        // Unicode BOM mark not found, unread all bytes
        encoding = UTF8;
        unread = n;
    }

    if (unread > 0)
        internalIn.unread(bom, (n - unread), unread);

    // Use given encoding
    CharsetDecoder decoder = encoding.newDecoder().onUnmappableCharacter(
            CodingErrorAction.REPORT);
    internalIn2 = new InputStreamReader(internalIn, decoder);
}
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
每次调用read()读文件时都会调用init()方法进行初始化，也就是这个时候确定文件的编码格式。首先读取BOM（Byte Order Mark）文件头信息，如果头信息中有UTF8/UTF16BE/UTF16LE就采用对应的编码，没有或者不是则采用UTF8编码。

自定义test.properties文件编码
采用@PropertySource(value=“classpath:test.properties”, encoding=“UTF-8”)方式读取配置文件可按照UTF-8的方式读取编码，而不是ISO-8859-1。@PropertySource配置的加载文件由ConfigurationClassParser.processPropertySource()进行解析，EncodedResource类决定最后由哪种编码格式加载文件，其方法如下：

public Reader getReader() throws IOException {
    if (this.charset != null) {
      return new InputStreamReader(this.resource.getInputStream(), this.charset);
    }
    else if (this.encoding != null) {
      return new InputStreamReader(this.resource.getInputStream(), this.encoding);
    }
    else {
      return new InputStreamReader(this.resource.getInputStream());
    }
  }
1
2
3
4
5
6
7
8
9
10
11
所以，虽然都是.properties文件，但是编码格式却是不一样的。

解决方案
自定义配置文件
使用yml/yaml配置文件
IDE/插件预编码
自定义配置文件
通过@PropertySource(value=“classpath:my.properties”, encoding=“UTF-8”)注解配置自定义文件，注意文件名不能是springboot默认的application.properties文件名称。

使用yml/yaml配置文件
将yml/yaml文件设置为UTF-8的编码格式，springboot读该文件即采用UTF-8编码。

IDE/插件预编码
采用编译器或者插件将配置文件预编码。这种方法我没试过，但是想想也知道这是很反人类的。如果有人感兴趣的话，可以参考一下[这篇博客最后一部分IDEA/eclipse的修改操作]1。

总结
在配置application.properties时，都是开发比较重要的参数，尽量使用英文，业务相关的中文配置还是不要放到这里。

https://blog.csdn.net/m0_37995707/article/details/77506184 ‘Spring Boot自定义属性以及乱码问题’ ↩︎
————————————————
版权声明：本文为CSDN博主「EricZeng05」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/formemorywithyou/article/details/96473169