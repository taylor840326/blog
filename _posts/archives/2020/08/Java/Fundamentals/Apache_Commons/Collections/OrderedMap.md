OrderedMap是映射的新接口，用于保留添加元素的顺序。 LinkedMap和ListOrderedMap是两种可用的实现。 此接口支持Map的迭代器，并允许在Map中向前或向后两个方向进行迭代。 下面的例子说明了这一点。
示例代码
OrderedMapTester.java - 
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.map.LinkedMap;

public class OrderedMapTester {
   public static void main(String[] args) {
      OrderedMap<String, String> map = new LinkedMap<String, String>();
      map.put("One", "1");
      map.put("Two", "2");
      map.put("Three", "3");

      System.out.println(map.firstKey());
      System.out.println(map.nextKey("One"));
      System.out.println(map.nextKey("Two"));  
   }
}
Java
执行上面示例代码，得到以下结果 - 
One
Two
Three//原文出自【易百教程】，商业转载请联系作者获得授权，非商业请保留原文链接：https://www.yiibai.com/commons_collections/commons_collections_orderedmap.html

