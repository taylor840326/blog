## 集合类型转换

-----

### 1. 集合类型转换

### 1.1. Stream转List

```java
List<Integer> integers = Arrays.asList(3, 2, 2, 3, 7, 4, 5);
List<Integer> toList = integers.stream().collect(Collectors.toList());
```

### 1.2. Stream转LinkedList

```java
List<Integer> integers = Arrays.asList(3, 2, 2, 3, 7, 4, 5);
LinkedList<Integer> toLinkedList = integers.stream().collect(Collectors.toCollection(LinkedList::new));
```

### 1.3. Stream转Set

```java
List<Integer> integers = Arrays.asList(3, 2, 2, 3, 7, 4, 5);
Set<Integer> toSet = integers.stream().collect(Collectors.toSet());
```

### 1.4. 转成CopyOnWriteArrayList

```java
List<Integer> integers = Arrays.asList(3, 2, 2, 3, 7, 4, 5);
CopyOnWriteArrayList<Integer> toCopyOnWriteList = integers.stream().collect(Collectors.toCollection(CopyOnWriteArrayList::new));
```

### 1.5. 收集后转换为不可变List

```java
List<Integer> integers = Arrays.asList(3, 2, 2, 3, 7, 4, 5);
ImmutableList<Integer> immutableList = integers.stream().collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
```

### 1.6. List转Map

```java
/*Lombok*/
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
class Student {
    private Long id;
   private String name;
}
ArrayList<Student> students = new ArrayList<>();
for (int i = 1; i <= 10; i++) {
    Student student = new Student();
    student.setId((long)i);
    student.setName("s" + i);
    students.add(student);
}

Map<Long, String> listToMap = students.stream().collect(Collectors.toMap(Student::getId, Student::getName));
for (Long aLong : listToMap.keySet()) {
    System.out.println(listToMap.get(aLong));
}
```

**需要注意的是** toMap 如果集合对象有重复的key，会报错Duplicate key...

可以用Collectors.toMap的第三个参数 (k1,k2)->k1 来设置，如果有重复的key,则保留key1,舍弃key2 

```java
/*Lombok*/
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
class Student {
    private Long id;
   private String name;
}
ArrayList<Student> students = new ArrayList<>();
for (int i = 1; i <= 10; i++) {
    Student student = new Student();
    student.setId((long)i%5);
    student.setName("s" + i);
    students.add(student);
}

Map<Long, String> listToMap = students.stream().collect(Collectors.toMap(Student::getId, Student::getName,(k1,k2)->k1));
for (Long aLong : listToMap.keySet()) {
    System.out.println(listToMap.get(aLong));
}
```

### 1.7. List 转 List<Map<String,Object>> 

有时候需要将List转换成List<Map<String,Object>>的形式，一般是把Map当成一个Tuple2类型来用了。

```java
/*Lombok*/
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
class Student {
    private Long id;
   private String name;
}
ArrayList<Student> students = new ArrayList<>();
for (int i = 1; i <= 10; i++) {
    Student student = new Student();
    student.setId((long)i%5);
    student.setName("s" + i);
    students.add(student);
}

List<HashMap<Long, String>> stuListMap = students.stream()
    .map(stu -> {
        HashMap<Long, String> longStringHashMap = new HashMap<>();
        longStringHashMap.put(stu.getId(), stu.getName());
        return longStringHashMap;
    })
    .collect(Collectors.toList());

for (Long aLong : listToMap.keySet()) {
    System.out.println(listToMap.get(aLong));
}
```
或者另外一种实现

```java
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
class Student {
        private Long id;
    private String name;
}
ArrayList<Student> students = new ArrayList<>();
for (int i = 1; i <= 20; i++) {
    Student student = new Student();
    student.setId((long) i % 5);
    student.setName("s" + i);
    students.add(student);
}

ArrayList<Map<Long,String>> stuListMap02 = students.stream()
    .collect(ArrayList::new, (list, p) -> {
        HashMap<Long, String> longStringHashMap = new HashMap<>();
        longStringHashMap.put(p.getId(), p.getName());
        list.add(longStringHashMap);
    }, List::addAll);

for (Map<Long, String> longStringMap : stuListMap02) {
    for (Long aLong : longStringMap.keySet()) {
    System.out.println(longStringMap.get(aLong));
    }
}

```

### 1.8. 转换到toMap时，如果value为null,会报空指针异常 

示例：

```java
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
class Student {
      private Long id;
      private String name;
}
ArrayList<Student> students = new ArrayList<>();
for (int i = 1; i <= 20; i++) {
      Student student = new Student();
      student.setId((long)i);
      if(i% 5 == 0){
          student.setName(null); //因为是null，下面的toMap会报空指针异常。
      }else{
          student.setName("s" + i);
      }
      students.add(student);
}

Map<Long, String> stuListToMap = students.stream().collect(Collectors.toMap(Student::getId, Student::getName));
for (Long i : stuListToMap.keySet()) {
    System.out.println(stuListToMap.get(i));
}
```

解决办法一：

```java
Map<String, List> resultMaps = Arrays.stream(dictTypes) .collect(Collectors.toMap(i -> i, i -> Optional.ofNullable(dictMap.get(i)).orElse(new ArrayList<>()), (k1, k2) -> k2));
```

解决办法二：

```java
Map<String, List> resultMaps = Arrays.stream(dictTypes) .filter(i -> dictMap.get(i) != null).collect(Collectors.toMap(i -> i, dictMap::get, (k1, k2) -> k2));
```

解决办法三：

```java
Map<String, String> memberMap = list.stream().collect(HashMap::new, (m,v)-> m.put(v.getId(), v.getImgPath()),HashMap::putAll); System.out.println(memberMap);
```

解决办法四：

```java
Map<String, String> memberMap = new HashMap<>(); list.forEach((answer) -> memberMap.put(answer.getId(), answer.getImgPath())); System.out.println(memberMap);

Map<String, String> memberMap = new HashMap<>(); for (Member member : list) { memberMap.put(member.getId(), member.getImgPath()); }
```



### 1.8. Map转List

```java
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
class Student {
      private Long id;
      private String name;
      private Long age;
}

HashMap<Long, Student> studentMap = new HashMap<>();
for (int i = 1; i < 10; i++) {
     studentMap.put((long)i,new Student((long)i,"Stu"+i,(long)(i*10)/3));
}

//Map转换成List<Student>
List<Student> result = studentMap.keySet().stream()
          .map(stu -> studentMap.get(stu))
          .collect(Collectors.toList());

//第二中转换方法
List<Student> result02 = studentMap.entrySet().stream()
          .map(stu -> stu.getValue())
          .collect(Collectors.toList());

//根据Map的Key排序
List<Student> result03 = studentMap.entrySet().stream()
          .sorted(Comparator.comparing(e -> e.getKey()))
          .map(stu -> stu.getValue())
          .collect(Collectors.toList());

List<Student> result04 = studentMap.entrySet().stream()
         .sorted(Comparator.comparing(Map.Entry::getKey))
         .map(stu -> stu.getValue())
         .collect(Collectors.toList());

List<Student> result05 = studentMap.entrySet().stream()
          .sorted(Map.Entry.comparingByKey())
          .map(stu -> stu.getValue())
          .collect(Collectors.toList());
```

### 1.8. Map转Map

```java
//示例1 Map<String, List> 转 Map<String,User> 

Map<String,List> map = new HashMap<>(); 
map.put("java", Arrays.asList("1.7", "1.8"));
map.entrySet().stream();

@Getter
@Setter 
@AllArgsConstructor 
public static class User{ 
    private List versions; 
}

Map<String, User> collect = map.entrySet().stream() 
    .collect(Collectors.toMap( item -> item.getKey(), item -> new User(item.getValue())));

//示例2 Map<String,Integer> 转 Map<String,Double> 

Map<String, Integer> pointsByName = new HashMap<>(); 
Map<String, Integer> maxPointsByName = new HashMap<>();

Map<String, Double> gradesByName = pointsByName.entrySet().stream()
    .map(entry -> new AbstractMap.SimpleImmutableEntry<>( entry.getKey(), ((double) entry.getValue() / maxPointsByName.get(entry.getKey())) * 100d)) 
    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
```


