## 如何使用Java Stream编写等效的常用SQL语句

-----

### 1. 简介

### 2. 准备数据


```java
ArrayList<SchoolClass> schoolClasses = new ArrayList<>();
ArrayList<Student> students = new ArrayList<>();
for (int i = 1; i < 5; i++) {
    SchoolClass schoolClass = new SchoolClass();
    schoolClass.setId((long) i);
    schoolClass.setName("C0" + i);
    schoolClasses.add(schoolClass);
}

for (int j = 1; j < 100; j++) {
    Student student = new Student();
    student.setId((long) j);
    student.setName("stu0" + j);
    student.setAge((int) (Math.random() * 100));
    student.setCid((long) j % 5);
    students.add(student);
}
```

### 3. SQL

### 3.1. SELECT * FROM students WHERE age >20;

```java
List<Student> stuAgeGt20 = students.stream()
.filter(stu -> stu.getAge() > 20)
.collect(Collectors.toList());
```


### 3.2. SELECT * FROM students WHERE cid != 3

```java
List<Student> stuCidNotEq3 = students.stream()
.filter(stu -> Long.compare(stu.getCid(), (long) 3) != 0)
.collect(Collectors.toList());
```


### 3.3. SELECT * FROM student WHERE age between 20 and 50

```java
List<Student> stuAgeBetween20And50 = students.stream()
.filter(stu -> stu.getAge() > 20)
.filter(stu -> stu.getAge() < 50)
.collect(Collectors.toList());
```

### 3.4. SELECT * FROM students WHERE name like 'stu%'

```java
List<Student> stuNamePrefixStu = students.stream()
.filter(stu -> stu.getName().startsWith("stu"))
.collect(Collectors.toList());
```


### 3.5. SELECT DISTINCT(*) FROM students

```java
List<Student> stuDistinct = students.stream()
.distinct()
.collect(Collectors.toList());
```

### 3.6. SELECT * FROM students ORDER BY id asc LIMIT 10

```java
List<Student> stuSorted = students.stream()
.sorted(new Comparator<Student>() {
    @Override
    public int compare(Student o1, Student o2) {
        int diff = o1.getAge() - o2.getAge();
        if(diff >0) {
            return 1;
        }else if(diff <0){
            return -1;
        }else{
            return 0;
        }
    }
})
.limit(10)
.collect(Collectors.toList());
```

### 3.8 SELECT students.* FROM students,class WHERE student.cid = class.id

```java
List<Student> stuContainsCid = students.stream()
.filter(stu -> {
    boolean contains = schoolClasses.stream()
    .map(c -> c.getId())
    .collect(Collectors.toList())
    .contains(stu.getCid());
    return contains;
})
.collect(Collectors.toList());
```

### 3.9. SELECT * from students WHERE cid in (SELECT id FROM class where id = 3);

```java
List<Student> stuCidEq3 = students.stream()
.filter(stu -> {
    boolean contains = schoolClasses.stream()
    .filter(c -> c.getId() == 3)
    .collect(Collectors.toList())
    .contains(stu.getCid());
    return contains;
})
.collect(Collectors.toList());
```


### 3.10. SELECT age,count() FROM students GROUP BY age having count() > 2

```java
Map<Integer, Long> stuAgeCountMap = students.stream()
.collect(Collectors.groupingBy(Student::getAge,Collectors.counting()));
Map<Integer, Long> stuAgeCountGt2 = Maps.filterValues(stuAgeCountMap, m -> m > 2);
//        for (Integer integer : stuAgeCountGt2.keySet()) {
//            System.out.println(stuAgeCountGt2.get(integer));
//        }
//或者使用Map自带的删除函数
stuAgeCountMap.values().removeIf(m -> m <= 2);
```


### 3.11. SELECT students.name as stuName,class.name as className FROM students left join class on students.cid = class.id

```java
/*保存最终计算结果*/
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
class Tuple2<T1,T2>{
    private T1 o1;
    private T2 o2;

}

List<Tuple2> stuNameClassNameTuple = students.stream()
.map(stu -> {
    Tuple2 resultTuple = new Tuple2();
    HashMap<String, String> stringStringHashMap = new HashMap<>();
    Map<Long,String> css = schoolClasses.stream()
    .collect(Collectors.toMap(SchoolClass::getId, SchoolClass::getName));
    if (css.keySet().contains(stu.getCid())) {
        resultTuple.setO1(stu.getName());
        resultTuple.setO2(css.get(stu.getCid()));
    } else {
        resultTuple.setO1(stu.getName());
        resultTuple.setO2(null);
    }
    return resultTuple;
})
.collect(Collectors.toList());
//        for (Tuple2 tuple :stuNameClassNameTuple) {
//            System.out.println(tuple.getO1() +":"+tuple.getO2());
//        }
```


### 参考资料

```html
https://blog.jooq.org/2015/08/13/common-sql-clauses-and-their-equivalents-in-java-8-streams/

```