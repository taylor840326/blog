## 字符串处理：分割，连接，填充
-----

原文链接 译文链接 译者：沈义扬，校对：丁一

### 1. 连接器[Joiner]

用分隔符把字符串序列连接起来也可能会遇上不必要的麻烦。

如果字符串序列中含有null，那连接操作会更难。Fluent风格的Joiner让连接字符串更简单。

### 1.1. skipNulls

直接忽略值为null的字符串

```java
Joiner joiner = Joiner.on("; ").skipNulls();
return joiner.join("Harry", null, "Ron", "Hermione");

”Harry; Ron; Hermione”
```

### 3.2. useForNull(String)

useForNull(String)方法可以给定某个字符串来替换null，而不像skipNulls()方法是直接忽略null。 

### 3.3. 链接对象

Joiner也可以用来连接对象类型，在这种情况下，它会把对象的toString()值连接起来。

```java
Joiner.on(",").join(Arrays.asList(1, 5, 7)); 

returns "1,5,7"
```

**重要提示**

joiner实例总是不可变的。用来定义joiner目标语义的配置方法总会返回一个新的joiner实例。

这使得joiner实例都是线程安全的，你可以将其定义为static final常量。

### 2. 拆分器[Splitter]

JDK内建的字符串拆分工具有一些古怪的特性。

比如，String.split悄悄丢弃了尾部的分隔符。 问题：”,a,,b,”.split(“,”)返回？

“”, “a”, “”, “b”, “”

null, “a”, null, “b”, null

“a”, null, “b”

“a”, “b”

正确答案是: ””, “a”, “”, “b”。

只有尾部的空字符串被忽略了。 

Splitter使用令人放心的、直白的流畅API模式对这些混乱的特性作了完全的掌控。

```java
Splitter.on(',')
        .trimResults()
        .omitEmptyStrings()
        .split("foo,bar,,   qux");
```

上述代码返回Iterable<String>，其中包含”foo”、”bar”和”qux”。

Splitter可以被设置为按照任何模式、字符、字符串或字符匹配器拆分。

### 3.1. 拆分器工厂

|方法|描述|范例|
|:---|---|---:|
|Splitter.on(char)|按单个字符拆分|Splitter.on(‘;’)|
|Splitter.on(CharMatcher)|按字符匹配器拆分|Splitter.on(CharMatcher.BREAKING_WHITESPACE)|
|Splitter.on(String)|按字符串拆分|Splitter.on(“,   “)|
|Splitter.on(Pattern) Splitter.onPattern(String)|按正则表达式拆分|Splitter.onPattern(“\r?\n”)|
|Splitter.fixedLength(int)|按固定长度拆分；最后一段可能比给定长度短，但不会为空|Splitter.fixedLength(3)|

### 3.2. 拆分器修饰符

|方法|描述|
|:---|---:|
|omitEmptyStrings()|从结果中自动忽略空字符串|
|trimResults()|移除结果字符串的前导空白和尾部空白|
|trimResults(CharMatcher)|给定匹配器，移除结果字符串的前导匹配字符和尾部匹配字符|
|limit(int)|限制拆分出的字符串数量|

如果你想要拆分器返回List，只要使用Lists.newArrayList(splitter.split(string))或类似方法。 

**重要提示**

splitter实例总是不可变的。用来定义splitter目标语义的配置方法总会返回一个新的splitter实例。这使得splitter实例都是线程安全的，你可以将其定义为static final常量。

### 4. 字符匹配器[CharMatcher]

在以前的Guava版本中，StringUtil类疯狂地膨胀，其拥有很多处理字符串的方法：allAscii、collapse、collapseControlChars、collapseWhitespace、indexOfChars、lastIndexNotOf、numSharedChars、removeChars、removeCrLf、replaceChars、retainAllChars、strip、stripAndCollapse、stripNonDigits。 所有这些方法指向两个概念上的问题：

1. 怎么才算匹配字符？
1. 如何处理这些匹配字符？

为了收拾这个泥潭，我们开发了CharMatcher。

直观上，你可以认为一个CharMatcher实例代表着某一类字符，如数字或空白字符。事实上来说，CharMatcher实例就是对字符的布尔判断——CharMatcher确实也实现了Predicate<Character>——但类似”所有空白字符”或”所有小写字母”的需求太普遍了，Guava因此创建了这一API。

然而使用CharMatcher的好处更在于它提供了一系列方法，让你对字符作特定类型的操作：修剪[trim]、折叠[collapse]、移除[remove]、保留[retain]等等。

CharMatcher实例首先代表概念

1. 怎么才算匹配字符？然后它还提供了很多操作概念
1. 如何处理这些匹配字符？这样的设计使得API复杂度的线性增加可以带来灵活性和功能两方面的增长。

```java
String noControl = CharMatcher.JAVA_ISO_CONTROL.removeFrom(string); //移除control字符
String theDigits = CharMatcher.DIGIT.retainFrom(string); //只保留数字字符
String spaced = CharMatcher.WHITESPACE.trimAndCollapseFrom(string, ' ');
//去除两端的空格，并把中间的连续空格替换成单个空格
String noDigits = CharMatcher.JAVA_DIGIT.replaceFrom(string, "*"); //用*号替换所有数字
String lowerAndDigit = CharMatcher.JAVA_DIGIT.or(CharMatcher.JAVA_LOWER_CASE).retainFrom(string);
// 只保留数字和小写字母
注：CharMatcher只处理char类型代表的字符；它不能理解0x10000到0x10FFFF的Unicode 增补字符。这些逻辑字符以代理对[surrogate pairs]的形式编码进字符串，而CharMatcher只能将这种逻辑字符看待成两个独立的字符。
```

### 3.1. 获取字符匹配器

CharMatcher中的常量可以满足大多数字符匹配需求：

ANY	NONE	WHITESPACE	BREAKING_WHITESPACE

INVISIBLE	DIGIT	JAVA_LETTER	JAVA_DIGIT

JAVA_LETTER_OR_DIGIT	JAVA_ISO_CONTROL	JAVA_LOWER_CASE	JAVA_UPPER_CASE

ASCII	SINGLE_WIDTH		

其他获取字符匹配器的常见方法包括：

|方法|描述|
|:---|---:|
|anyOf(CharSequence)|枚举匹配字符。如CharMatcher.anyOf(“aeiou”)匹配小写英语元音|
|is(char)|给定单一字符匹配|
|inRange(char, char)|给定字符范围匹配，如CharMatcher.inRange(‘a’, ‘z’)|

此外，CharMatcher还有negate()、and(CharMatcher)和or(CharMatcher)方法。

### 4.2. 使用字符匹配器

CharMatcher提供了多种多样的方法操作CharSequence中的特定字符。其中最常用的罗列如下：

|方法|描述|
|:---|---:|
|collapseFrom(CharSequence,   char)|把每组连续的匹配字符替换为特定字符。如WHITESPACE.collapseFrom(string, ‘ ‘)把字符串中的连续空白字符替换为单个空格|
|matchesAllOf(CharSequence)|测试是否字符序列中的所有字符都匹配|
|removeFrom(CharSequence)|从字符序列中移除所有匹配字符|
|retainFrom(CharSequence)|在字符序列中保留匹配字符，移除其他字符|
|trimFrom(CharSequence)|移除字符序列的前导匹配字符和尾部匹配字符|
|replaceFrom(CharSequence,   CharSequence)|用特定字符序列替代匹配字符|

所有这些方法返回String，除了matchesAllOf返回的是boolean。

### 5. 字符集[Charsets]

不要这样做字符集处理：

```java
try {
    bytes = string.getBytes("UTF-8");
} catch (UnsupportedEncodingException e) {
    // how can this possibly happen?
    throw new AssertionError(e);
}
```

试试这样写：

```java
bytes = string.getBytes(Charsets.UTF_8);
```

Charsets针对所有Java平台都要保证支持的六种字符集提供了常量引用。尝试使用这些常量，而不是通过名称获取字符集实例。

### 6. 大小写格式[CaseFormat]

CaseFormat被用来方便地在各种ASCII大小写规范间转换字符串——比如，编程语言的命名规范。CaseFormat支持的格式如下：

|格式|范例|
|:---|---:|
|LOWER_CAMEL|lowerCamel|
|LOWER_HYPHEN|lower-hyphen|
|LOWER_UNDERSCORE|lower_underscore|
|UPPER_CAMEL|UpperCamel|
|UPPER_UNDERSCORE|UPPER_UNDERSCORE|

CaseFormat的用法很直接：

```java
CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, "CONSTANT_NAME")); // returns "constantName"
```

我们CaseFormat在某些时候尤其有用，比如编写代码生成器的时候。

参考资料

```java
原创文章，转载请注明： 转载自并发编程网 – ifeve.com本文链接地址: [Google Guava] 6-字符串处理：分割，连接，填充
```
