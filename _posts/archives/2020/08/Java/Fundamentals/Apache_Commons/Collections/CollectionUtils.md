## CollectionUtils
-----

### 1. 简介
				
Apache Commons Collections库的CollectionUtils类提供各种实用方法，用于覆盖广泛用例的常见操作。 

它有助于避免编写样板代码。 这个库在jdk 8之前是非常有用的，但现在Java 8的Stream API提供了类似的功能。


### 2. 检查是否为空元素

CollectionUtils的addIgnoreNull()方法可用于确保只有非空(null)值被添加到集合中。

### 2.1. 声明

以下是org.apache.commons.collections4.CollectionUtils.addIgnoreNull()的声明 - 

```java
public static <T> boolean addIgnoreNull(Collection<T> collection, T object)
```

### 2.2. 参数

1. collection - 要添加到的集合，不能为null值。 
1. object - 要添加的对象，如果为null，则不会添加。

### 2.3. 返回值

如果集合已更改，则返回为True。

### 2.4. 示例

以下示例显示org.apache.commons.collections4.CollectionUtils.addIgnoreNull()方法的用法。在示例中试图添加一个空值和一个非空值。

```java
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

public class CollectionUtilsTester {
   public static void main(String[] args) {
      List<String> list = new LinkedList<String>();

      CollectionUtils.addIgnoreNull(list, null);
      CollectionUtils.addIgnoreNull(list, "a");

      System.out.println(list);

      if(list.contains(null)) {
         System.out.println("Null value is present");
      } else {
         System.out.println("Null value is not present");
      }
   }
}
```

### 2.5. 执行上面示例代码，得到以下结果

```java
[a]
Null value is not present
```

### 3. 合并两个排序列表

CollectionUtils的collate()方法可用于合并两个已排序的列表。


### 3.1. 声明

以下是org.apache.commons.collections4.CollectionUtils.collate()方法的声明

```java
public static <O extends Comparable<? super O>> List<O> 
   collate(Iterable<? extends O> a, Iterable<? extends O> b)
```

### 3.2. 参数

1. a - 第一个集合，不能为null。
1. b - 第二个集合不能为null。

### 3.3. 返回值

一个新的排序列表，其中包含集合a和b的元素。

### 3.4. 异常

NullPointerException - 如果其中一个集合为null。

### 3.5. 示例

以下示例显示了用法org.apache.commons.collections4.CollectionUtils.collate()方法。

我们将合并两个已排序的列表，然后打印已合并和已排序的列表。

```java
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

public class CollectionUtilsTester {
   public static void main(String[] args) {
      List<String> sortedList1 = Arrays.asList("A","C","E");
      List<String> sortedList2 = Arrays.asList("B","D","F");
      List<String> mergedList = CollectionUtils.collate(sortedList1, sortedList2);
      System.out.println(mergedList); 
   }
}
```

### 3.6. 执行上面示例代码，得到以下结果 

```text
[A, B, C, D, E, F]
```

### 4.转换列表

CollectionUtils的collect()方法可用于将一种类型的对象列表转换为不同类型的对象列表。

### 4.1. 声明

以下是org.apache.commons.collections4.CollectionUtils.collect()方法的声明 - 

```java
public static <I,O> Collection<O> collect(Iterable<I> inputCollection, 
   Transformer<? super I,? extends O> transformer)
```
### 4.2. 参数

inputCollection - 从中获取输入的集合可能不为null。transformer - 要使用的transformer可能为null。

### 4.3. 返回值

换结果(新列表)。

### 4.4. 示例

以下示例显示org.apache.commons.collections4.CollectionUtils.collect()方法的用法。 

将通过解析String中的整数值来将字符串列表转换为整数列表。

```java
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;

public class CollectionUtilsTester {
   public static void main(String[] args) {
      List<String> stringList = Arrays.asList("1","2","3");

      List<Integer> integerList = (List<Integer>) CollectionUtils.collect(stringList, 
         new Transformer<String, Integer>() {

         @Override
         public Integer transform(String input) {
            return Integer.parseInt(input);
         }
      });

      System.out.println(integerList);
   }
}
```

### 4.5. 执行上面示例代码，

```text
[1, 2, 3]
```

### 5. 使用filter()方法过滤列表

CollectionUtils的filter()方法可用于过滤列表以移除不满足由谓词传递提供的条件的对象。

### 5.1. 声明

以下是org.apache.commons.collections4.CollectionUtils.filter()方法的声明 -

```java
public static <T> boolean filter(Iterable<T> collection,
   Predicate<? super T> predicate)
```
1. collection - 从中获取输入的集合可能不为null。
1. predicate - 用作过滤器的predicate可能为null。

### 5.2. 返回值

如果通过此调用修改了集合，则返回true，否则返回false。

### 5.3. 示例

以下示例显示org.apache.commons.collections4.CollectionUtils.filter()方法的用法。 这个示例中将过滤一个整数列表来获得偶数。

```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

public class CollectionUtilsTester {
   public static void main(String[] args) {

      List<Integer> integerList = new ArrayList<Integer>();
      integerList.addAll(Arrays.asList(1,2,3,4,5,6,7,8));

      System.out.println("Original List: " + integerList);
      CollectionUtils.filter(integerList, new Predicate<Integer>() {

         @Override
         public boolean evaluate(Integer input) {
            if(input.intValue() % 2 == 0) {
               return true;
            }
            return false;
         }
      });

      System.out.println("Filtered List (Even numbers): " + integerList);
   }
}
```

### 5.4. 执行上面示例代码，得到以下结果 - 

```text
Original List: [1, 2, 3, 4, 5, 6, 7, 8]
Filtered List (Even numbers): [2, 4, 6, 8]
```

### 6. 使用filterInverse()方法过滤列表

CollectionUtils的filterInverse()方法可用于过滤列表以移除满足谓词传递提供的条件的对象。

### 6.1. 声明

以下是org.apache.commons.collections4.CollectionUtils.filterInverse()方法的声明 - 

```java
public static <T> boolean filterInverse(Iterable<T> collection,
   Predicate<? super T> predicate)
```

### 6.2. 参数

1. collection - 从中获取输入的集合，可能不为null。
1. predicate - 用作过滤器的predicate可能为null。

### 6.3. 返回值

如果通过此调用修改了集合，则返回true，否则返回false。

### 6.4. 示例

以下示例显示org.apache.commons.collections4.CollectionUtils.filterInverse()方法的用法。 这个示例中将过滤一个整数列表来获得奇数。

```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

public class CollectionUtilsTester {
   public static void main(String[] args) {

      List<Integer> integerList = new ArrayList<Integer>();
      integerList.addAll(Arrays.asList(1,2,3,4,5,6,7,8));

      System.out.println("Original List: " + integerList);
      CollectionUtils.filterInverse(integerList, new Predicate<Integer>() {

         @Override
         public boolean evaluate(Integer input) {
            if(input.intValue() % 2 == 0) {
               return true;
            }
            return false;
         }
      });

      System.out.println("Filtered List (Odd numbers): " + integerList);
   }
}
```

### 6.5. 执行上面示例代码，得到以下结果 - 

```text
Original List: [1, 2, 3, 4, 5, 6, 7, 8]
Filtered List (Odd numbers): [1, 3, 5, 7]
```

### 7. 检查非空列表

CollectionUtils的｀isNotEmpty()｀方法可用于检查列表是否为null而不用担心null列表。 因此，在检查列表大小之前，不需要将无效检查放在任何地方。

### 7.1. 声明

以下是org.apache.commons.collections4.CollectionUtils.isNotEmpty()方法的声明 - 

```java
public static boolean isNotEmpty(Collection<?> coll)
```

### 7.2. 参数

coll - 要检查的集合，可能为null。

### 7.3. 返回值

如果非空且非null，则返回为:true。

### 7.4. 示例

以下示例显示org.apache.commons.collections4.CollectionUtils.isNotEmpty()方法的用法。 在这示例中将检查一个列表是否为空。

```java
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

public class CollectionUtilsTester {
   public static void main(String[] args) {
      List<String> list = getList();
      System.out.println("Non-Empty List Check: " + checkNotEmpty1(list));
      System.out.println("Non-Empty List Check: " + checkNotEmpty1(list));
   }

   static List<String> getList() {
      return null;
   } 

   static boolean checkNotEmpty1(List<String> list) {
      return !(list == null || list.isEmpty());
   }

   static boolean checkNotEmpty2(List<String> list) {
      return CollectionUtils.isNotEmpty(list);
   } 
}
```

### 7.5. 执行上面示例代码，得到以下结果 - 
```text

Non-Empty List Check: false
Non-Empty List Check: false
```

### 8. 检查空的列表

CollectionUtils的isEmpty()方法可用于检查列表是否为空。 因此，在检查列表大小之前，不需要将无效检查放在任何地方。


### 8.1. 声明

以下是org.apache.commons.collections4.CollectionUtils.isEmpty()方法的声明 -

```java
public static boolean isEmpty(Collection<?> coll)
```

### 8.2. 参数

coll - 要检查的集合，可能为null。

### 8.3. 返回值

如果为空或为null，则返回为true。

### 8.4.示例

以下示例显示org.apache.commons.collections4.CollectionUtils.isEmpty()方法的用法。在这个示例中将检查一个列表是否为空。

```java
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

public class CollectionUtilsTester {
   public static void main(String[] args) {
      List<String> list = getList();
      System.out.println("Empty List Check: " + checkEmpty1(list));
      System.out.println("Empty List Check: " + checkEmpty1(list));
   }

   static List<String> getList() {
      return null;
   } 

   static boolean checkEmpty1(List<String> list) {
      return (list == null || list.isEmpty());
   }

   static boolean checkEmpty2(List<String> list) {
      return CollectionUtils.isEmpty(list);
   } 
}
```

### 8.5. 执行上面示例代码，得到以下结果 - 

```text
Empty List Check: true
Empty List Check: true
```

### 9. 检查子列表

CollectionUtils的isSubCollection()方法可用于检查集合是否包含给定集合。

### 9.1. 声明

以下是org.apache.commons.collections4.CollectionUtils.isSubCollection()方法的声明 -
```java
public static boolean isSubCollection(Collection<?> a,
   Collection<?> b)
```

### 9.2. 参数

1. a - 第一个(子)集合不能为空。
1. b - 第二个(超集)集合不能为空。

当且仅当a是b的子集合时才为true。

### 9.3. 示例

以下示例显示org.apache.commons.collections4.CollectionUtils.isSubCollection()方法的用法。 这个示例中将检查一个列表是否是另一个列表的一部分。

```java
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

public class CollectionUtilsTester {
   public static void main(String[] args) {
      //checking inclusion
      List<String> list1 = Arrays.asList("A","A","A","C","B","B");
      List<String> list2 = Arrays.asList("A","A","B","B");

      System.out.println("List 1: " + list1);
      System.out.println("List 2: " + list2);
      System.out.println("Is List 2 contained in List 1: " 
         + CollectionUtils.isSubCollection(list2, list1));
   }
}
```

### 9.4. 执行上面示例代码，得到以下结果 - 
```text
List 1: [A, A, A, C, B, B]
List 2: [A, A, B, B]
Is List 2 contained in List 1: true
```

### 10. 集合相交

CollectionUtils的intersection()方法可用于获取两个集合(交集)之间的公共对象部分。

### 10.1. 声明

以下是org.apache.commons.collections4.CollectionUtils.intersection()方法的声明 - 

```java
public static <O> Collection<O> intersection(Iterable<? extends O> a,
   Iterable<? extends O> b)
```

### 10.2. 参数

1. a - 第一个(子)集合不能为null。
1. b - 第二个(超集)集合不能为null。

### 10.3. 返回值

两个集合的交集。

### 10.4. 示例

以下示例显示org.apache.commons.collections4.CollectionUtils.intersection()方法的用法。在此示例中将得到两个列表的交集。

```java
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

public class CollectionUtilsTester {
   public static void main(String[] args) {
      //checking inclusion
      List<String> list1 = Arrays.asList("A","A","A","C","B","B");
      List<String> list2 = Arrays.asList("A","A","B","B");

      System.out.println("List 1: " + list1);
      System.out.println("List 2: " + list2);
      System.out.println("Commons Objects of List 1 and List 2: " 
         + CollectionUtils.intersection(list1, list2));
   }
}
```

### 10.5. 执行上面示例代码，得到以下结果 - 
```text
List 1: [A, A, A, C, B, B]
List 2: [A, A, B, B]
Commons Objects of List 1 and List 2: [A, A, B, B]
```

### 11. 集合差集

CollectionUtils的subtract()方法可用于通过从其他集合中减去一个集合的对象来获取新集合。

### 11.1. 声明

以下是org.apache.commons.collections4.CollectionUtils.subtract()方法的声明 - 

```java
public static <O> Collection<O> subtract(Iterable<? extends O> a,
   Iterable<? extends O> b)
```

### 11.2. 参数

1. a - 要从中减去的集合，不能为null。
1. b - 要减去的集合，不能为null。

### 11.3. 返回值

两个集合的差集(新集合)。

### 11.4. 示例

以下示例显示org.apache.commons.collections4.CollectionUtils.subtract()方法的用法。在此示例中将得到两个列表的差集。

```java
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

public class CollectionUtilsTester {
   public static void main(String[] args) {
      //checking inclusion
      List<String> list1 = Arrays.asList("A","A","A","C","B","B");
      List<String> list2 = Arrays.asList("A","A","B","B");

      System.out.println("List 1: " + list1);
      System.out.println("List 2: " + list2);
      System.out.println("List 1 - List 2: " 
         + CollectionUtils.subtract(list1, list2));
   }
}
```

### 11.5. 执行上面示例代码，得到以下结果 - 
```text
List 1: [A, A, A, C, B, B]
List 2: [A, A, B, B]
List 1 - List 2: [A, C]
```

### 12. 集合联合

CollectionUtils的union()方法可用于获取两个集合的联合。

### 12.1. 声明

以下是org.apache.commons.collections4.CollectionUtils.union()方法的声明 - 

```java
public static <O> Collection<O> union(Iterable<? extends O> a,
   Iterable<? extends O> b)
```

### 12.2. 参数

1. a - 第一个集合，不能为null。
1. b - 第二个集合，不能为null。

### 12.3. 返回值

两个集合的联合。

### 12.4. 示例

以下示例显示org.apache.commons.collections4.CollectionUtils.union()方法的用法。在此示例中将得到两个列表的联合。

```java
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

public class CollectionUtilsTester {
   public static void main(String[] args) {
      //checking inclusion
      List<String> list1 = Arrays.asList("A","A","A","C","B","B");
      List<String> list2 = Arrays.asList("A","A","B","B");

      System.out.println("List 1: " + list1);
      System.out.println("List 2: " + list2);
      System.out.println("Union of List 1 and List 2: " 
         + CollectionUtils.union(list1, list2));
   }
}
```

### 12.5. 执行上面示例代码，得到以下结果 - 

```text
List 1: [A, A, A, C, B, B]
List 2: [A, A, B, B]
Union of List 1 and List 2: [A, A, A, B, B, C]
```


### 参考资料

//原文出自【易百教程】，商业转载请联系作者获得授权，非商业请保留原文链接：https://www.yiibai.com/commons_collections/commons_collections_ignore_null.html

