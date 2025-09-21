使用Spring JDBC持久化Java开源爬虫框架WebCollector爬取的数据
BY BRIEFCOPY · PUBLISHED 2016年4月25日 · UPDATED 2016年12月11日

1.导入Spring JDBC的依赖
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>5.1.31</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-jdbc</artifactId>
    <version>4.0.5.RELEASE</version>
</dependency>

<dependency>
    <groupId>commons-dbcp</groupId>
    <artifactId>commons-dbcp</artifactId>
    <version>1.4</version>
</dependency>
2.创建一个JDBCHelper类
import java.util.HashMap;
import org.apache.commons.dbcp.BasicDataSource;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author hu
 */
public class JDBCHelper {

    public static HashMap<String, JdbcTemplate> templateMap 
        = new HashMap<String, JdbcTemplate>();

    public static JdbcTemplate createMysqlTemplate(String templateName, 
            String url, String username, String password, 
            int initialSize, int maxActive) {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setInitialSize(initialSize);
        dataSource.setMaxActive(maxActive);
        JdbcTemplate template = new JdbcTemplate(dataSource);
        templateMap.put(templateName, template);
        return template;
    }

    public static JdbcTemplate getJdbcTemplate(String templateName){
        return templateMap.get(templateName);
    }

}
可以调用JDBCHelper的getJdbcTemplate方法来获取一个JdbcTemplate，考虑到有些爬虫可能会用到多个数据库，这里可以为每个JdbcTemplate指定名称，如果已经创建了一个名为xxx的JdbcTemplate，第二次调用getJdbcTemplate(xxx)时，不会二次创建，而是返回已经创建的JdbcTemplate。

3.初始化一个JdbcTemplate
例如我们要创建一个包含了id,url,title,html四列的表：

JdbcTemplate jdbcTemplate = null;
try {
    jdbcTemplate = JDBCHelper.createMysqlTemplate("mysql1",
            "jdbc:mysql://localhost/testdb?useUnicode=true&characterEncoding=utf8",
            "root", "password", 5, 30);

    /*创建数据表*/
    jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS tb_content ("
            + "id int(11) NOT NULL AUTO_INCREMENT,"
            + "title varchar(50),url varchar(200),html longtext,"
            + "PRIMARY KEY (id)"
            + ") ENGINE=MyISAM DEFAULT CHARSET=utf8;");
    System.out.println("成功创建数据表 tb_content");
} catch (Exception ex) {
    jdbcTemplate = null;
    System.out.println("mysql未开启或JDBCHelper.createMysqlTemplate中参数配置不正确!");
}
4.在WebCollector的visit方法中使用jdbcTemplate持久化数据
if (jdbcTemplate != null) {
    int updates=jdbcTemplate.update("insert into tb_content"
        +" (title,url,html) value(?,?,?)",
            title, page.getUrl(), page.getHtml());
    if(updates==1){
        System.out.println("mysql插入成功");
    }
}