### 1. 集合运算

### 1.1. 交集

```java
List intersect = list1.stream() .filter(list2::contains) .collect(Collectors.toList());
```

### 1.2. 并集

```java
List listAll = list1.parallelStream().collect(toList()); List listAll2 = list2.parallelStream().collect(toList()); listAll.addAll(listAll2);
```

### 1.3. 并集去重

```java
List listAllDistinct = listAll.stream() .distinct().collect(toList());
```

### 1.3. 差集

```java
list1 - list2 
List reduce1 = list1.stream().filter(item -> !list2.contains(item)).collect(toList());

list2 - list1 
List reduce2 = list2.stream().filter(item -> !list1.contains(item)).collect(toList());
```
