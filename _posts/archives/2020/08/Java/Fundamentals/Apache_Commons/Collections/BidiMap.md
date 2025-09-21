新接口被添加到支持双向映射。 使用双向映射，可以使用值查找键，并且可以使用键轻松查找值。
接口声明以下是org.apache.commons.collections4.BidiMap <K，V>接口的声明 -
public interface BidiMap<K,V>
   extends IterableMap<K,V>
Java
以下是接口的方法列表 - 



编号
方法
描述




1
K getKey(Object value)
获取当前映射到指定值的键。


2
BidiMap<V,K> inverseBidiMap()
获取该映射的键和值的键视图。


3
V put(K key, V value)
将键值对放入映射中，替换之前的任何一对。


4
K removeValue(Object value)
删除当前映射到指定值的键值对(可选操作)。


5
Set<V> values()
返回此映射中包含的值的Set视图。



方法继承该接口继承了以下接口的方法 -

org.apache.commons.collections4.Getorg.apache.commons.collections4.IterableGetorg.apache.commons.collections4.Putjava.util.Map
BidiMap接口实例
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.TreeBidiMap;

public class BidiMapTester {
   public static void main(String[] args) {
      BidiMap<String, String> bidi = new TreeBidiMap<>();

      bidi.put("One", "1");
      bidi.put("Two", "2");
      bidi.put("Three", "3");

      System.out.println(bidi.get("One")); 
      System.out.println(bidi.getKey("1"));
      System.out.println("Original Map: " + bidi);

      bidi.removeValue("1"); 
      System.out.println("Modified Map: " + bidi);
      BidiMap<String, String> inversedMap = bidi.inverseBidiMap();  
      System.out.println("Inversed Map: " + inversedMap);
   }
}
Java
执行上面示例代码，得到以下结果 - 
1
One
Original Map: {One=1, Three=3, Two=2}
Modified Map: {Three=3, Two=2}
Inversed Map: {2=Two, 3=Three}


//原文出自【易百教程】，商业转载请联系作者获得授权，非商业请保留原文链接：https://www.yiibai.com/commons_collections/commons_collections_bidimap.html

