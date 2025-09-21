新的接口被添加到支持bag。 Bag接口定义了一个集合，它可以计算一个对象出现在集合中的次数。 例如，如果Bag包含{a，a，b，c}，则getCount("a")方法将返回2，而uniqueSet()返回唯一值。
接口声明以下是org.apache.commons.collections4.Bag<E>接口的声明 - 
public interface Bag<E>
   extends Collection<E>
Java



编号
方法
描述




1
boolean add(E object)
(冲突)将指定的对象到Bag的一个副本。


2
boolean add(E object, int nCopies)
将指定对象的nCopies副本添加到Bag中。


3
boolean containsAll(Collection<?> coll)
(冲突)如果包包含给定集合中的所有元素，并且尊重基数，则返回true。


4
int getCount(Object object)
返回包中当前给定对象的出现次数(基数)。


5
Iterator<E> iterator()
在整个成员集上返回一个迭代器，包括由于基数而产生的副本。


6
boolean remove(Object object)
从Bag中移除所有给定的对象。


7
boolean remove(Object object, int nCopies)
从Bag中删除指定对象的nCopies副本。


8
boolean removeAll(Collection<?> coll)
删除给定集合中的所有元素，尊重基数。


9
boolean retainAll(Collection<?> coll)
移除不在给定集合中的所有Bag成员，尊重基数。


10
int size()
返回Bag中所有类型对象的总数。


11
Set<E> uniqueSet()
返回Bag中的一组唯一元素。



方法继承该接口从以下接口继承方法 -

java.util.Collection
Bag接口示例
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;

public class BagTester {
   public static void main(String[] args) {
      Bag<String> bag = new HashBag<>();

      //add "a" two times to the bag.
      bag.add("a" , 2);

      //add "b" one time to the bag.
      bag.add("b");

      //add "c" one time to the bag.
      bag.add("c");

      //add "d" three times to the bag.
      bag.add("d",3);

      //get the count of "d" present in bag.
      System.out.println("d is present " + bag.getCount("d") + " times.");
      System.out.println("bag: " +bag);

      //get the set of unique values from the bag
      System.out.println("Unique Set: " +bag.uniqueSet());

      //remove 2 occurrences of "d" from the bag
      bag.remove("d",2);
      System.out.println("2 occurences of d removed from bag: " +bag);
      System.out.println("d is present " + bag.getCount("d") + " times.");
      System.out.println("bag: " +bag);
      System.out.println("Unique Set: " +bag.uniqueSet());
   }
}
Java
执行上面示例代码，得到以下结果 - 
d is present 3 times.
bag: [2:a,1:b,1:c,3:d]
Unique Set: [a, b, c, d]
2 occurences of d removed from bag: [2:a,1:b,1:c,1:d]
d is present 1 times.
bag: [2:a,1:b,1:c,1:d]
Unique Set: [a, b, c, d]

//原文出自【易百教程】，商业转载请联系作者获得授权，非商业请保留原文链接：https://www.yiibai.com/commons_collections/commons_collections_bag.html

