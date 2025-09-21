JDK Map接口很难作为迭代在EntrySet或KeySet对象上迭代。 MapIterator提供了对Map的简单迭代。下面的例子说明了这一点。
MapIterator接口示例
import org.apache.commons.collections4.IterableMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.HashedMap;

public class MapIteratorTester {
   public static void main(String[] args) {
      IterableMap<String, String> map = new HashedMap<>();

      map.put("1", "One");
      map.put("2", "Two");
      map.put("3", "Three");
      map.put("4", "Four");
      map.put("5", "Five");

      MapIterator<String, String> iterator = map.mapIterator();
      while (iterator.hasNext()) {
         Object key = iterator.next();
         Object value = iterator.getValue();

         System.out.println("key: " + key);
         System.out.println("Value: " + value);

         iterator.setValue(value + "_");
      }

      System.out.println(map);
   }
}
Java
执行上面示例代码，得到以下结果 - 
key: 3
Value: Three
key: 5
Value: Five
key: 2
Value: Two
key: 4
Value: Four
key: 1
Value: One
{3=Three_, 5=Five_, 2=Two_, 4=Four_, 1=One_}



//原文出自【易百教程】，商业转载请联系作者获得授权，非商业请保留原文链接：https://www.yiibai.com/commons_collections/commons_collections_mapiterator.html

