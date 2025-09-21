## StringUtils

-----

### 1. 前言

上一篇博文已经讲解了lang3下面的很多的API，但是StringUtils留在本文专门讲解。因为这个工具类在日常使用中实在太多了。

本文的讲解方式为：直接看实例，而不做过多的文字描述解释

### 2. 实例

### 2.1. public static boolean isEmpty(CharSequence cs)

这个可能用得是非常多的，null和空串都被定义为empty了哟

```java
StringUtils.isEmpty(null)      = true
StringUtils.isEmpty("")        = true
StringUtils.isEmpty(" ")       = false  //注意这里是false
StringUtils.isEmpty("bob")     = false
StringUtils.isEmpty("  bob  ") = false
```


### 2.2. public static boolean isAnyEmpty(CharSequence… css)
任意一个参数为空的话，返回true。如果这些参数都不为空的话返回false。在写一些判断条件的时候，这个方法还是很实用的。

```java
StringUtils.isAnyEmpty(null)             = true
StringUtils.isAnyEmpty(null, "foo")      = true
StringUtils.isAnyEmpty("", "bar")        = true
StringUtils.isAnyEmpty("bob", "")        = true
StringUtils.isAnyEmpty("  bob  ", null)  = true
StringUtils.isAnyEmpty(" ", "bar")       = false //注意这个是false哦
StringUtils.isAnyEmpty("foo", "bar")     = false 
```

### 2.3. public static boolean isNoneEmpty(CharSequence… css) 和isAnyEmpty取返

### 2.4. public static boolean isBlank(CharSequence cs)

判断字符对象是不是空字符串，注意与isEmpty的区别。相当于深度的isEmpty

```java
StringUtils.isBlank(null)      = true
StringUtils.isBlank("")        = true
StringUtils.isBlank(" ")       = true //注意此处是null哦  这和isEmpty不一样的
StringUtils.isBlank("bob")     = false
StringUtils.isBlank("  bob  ") = false
```

### 2.5. isAnyBlank、isNoneBlank这里就不再解释了

### 2.6. public static String trim(String str)

移除字符串两端的空字符串，制表符char <= 32如：\n \t 如果为null返回null

```java
StringUtils.trim(null)          = null
StringUtils.trim("")            = ""
StringUtils.trim("     ")       = ""
StringUtils.trim("abc")         = "abc"
StringUtils.trim("    abc    ") = "abc"
```

变体有如下：
public static String trimToNull(String str) //如果是null就返回null，否则trim之后返回
public static String trimToEmpty(String str)

### 2.7. public static int ordinalIndexOf(CharSequence str, CharSequence searchStr,int ordinal)

这个方法有时候会很有用的。字符串在另外一个字符串里，出现第Ordinal次的位置

```java
StringUtils.ordinalIndexOf("aabaabaa", "a", 1)  = 0
StringUtils.ordinalIndexOf("aabaabaa", "a", 2)  = 1
StringUtils.ordinalIndexOf("aabaabaa", "b", 1)  = 2
StringUtils.ordinalIndexOf("aabaabaa", "b", 2)  = 5
StringUtils.ordinalIndexOf("aabaabaa", "ab", 1) = 1
StringUtils.ordinalIndexOf("aabaabaa", "ab", 2) = 4
StringUtils.ordinalIndexOf("aabaabaa", "", 1)   = 0 //空串永远访问0
StringUtils.ordinalIndexOf("aabaabaa", "", 2)   = 0 //空串永远访问0
```

对应的有：lastOrdinalIndexOf方法

### 2.8. public static boolean containsAny(CharSequence cs,char… searchChars)

是否包含后面数组中的任意对象，返回true（和List里的containsAll有点像）

```java
StringUtils.containsAny(null, *)                = false
StringUtils.containsAny("", *)                  = false
StringUtils.containsAny(*, null)                = false
StringUtils.containsAny(*, [])                  = false
StringUtils.containsAny("zzabyycdxx",['z','a']) = true
StringUtils.containsAny("zzabyycdxx",['b','y']) = true
StringUtils.containsAny("aba", ['z'])           = false
```

### 2.9. public static String substring(String str,int start)

这个系列有的时候很有用，特别是下面的衍生方法：

```java
//从左边开始截取指定个数
public static String left(String str,int len)
//从右边开始截取指定个数
public static String right(String str,int len)
//从中间的指定位置开始截取  指定个数
public static String mid(String str,int pos,int len)
```


### 2.10. public static String join(T… elements)、public static String join(Object[] array,char separator)

默认使用空串Join

```java
StringUtils.join(null)            = null
StringUtils.join([])              = ""
StringUtils.join([null])          = ""
StringUtils.join(["a", "b", "c"]) = "abc"
StringUtils.join([null, "", "a"]) = "a"
```

自定义符号：特定字符串连接数组，很多情况下还是蛮实用，不用自己取拼字符串

```java
StringUtils.join(null, *)               = null
StringUtils.join([], *)                 = ""
StringUtils.join([null], *)             = ""
StringUtils.join(["a", "b", "c"], ';')  = "a;b;c"
StringUtils.join(["a", "b", "c"], null) = "abc"
StringUtils.join([null, "", "a"], ';')  = ";;a" //注意这里和上面的区别
```


### 2.11. public static String deleteWhitespace(String str)

删除空格 这个方法还挺管用的。比trim给力

```java
StringUtils.deleteWhitespace(null)         = null
StringUtils.deleteWhitespace("")           = ""
StringUtils.deleteWhitespace("abc")        = "abc"
StringUtils.deleteWhitespace("   ab  c  ") = "abc"
```

### 2.12. public static String removeStart(String str,String remove)

删除以特定字符串开头的字符串，如果没有的话，就不删除。
这个方法有时候很管用啊

```java
StringUtils.removeStart(null, *)      = null
StringUtils.removeStart("", *)        = ""
StringUtils.removeStart(*, null)      = *
StringUtils.removeStart("www.domain.com", "www.")   = "domain.com"
StringUtils.removeStart("domain.com", "www.")       = "domain.com"
StringUtils.removeStart("www.domain.com", "domain") = "www.domain.com" //注意这个结果哟  并没有删除任何东西
StringUtils.removeStart("abc", "")    = "abc"
```


### 2.13. public static String rightPad(String str,int size,char padChar)

这个方法还是蛮管用的。对于生成统一长度的字符串的时候。
比如生成订单号，为了保证长度统一，可以右边自动用指定的字符补全至指定长度

```java
StringUtils.rightPad(null, *, *)     = null
StringUtils.rightPad("", 3, 'z')     = "zzz"
StringUtils.rightPad("bat", 3, 'z')  = "bat"
StringUtils.rightPad("bat", 5, 'z')  = "batzz"
StringUtils.rightPad("bat", 1, 'z')  = "bat"
StringUtils.rightPad("bat", -1, 'z') = "bat"
```

对应的：leftPad 左边自动补全

### 2.14. public static String center(String str,int size)

将字符串扩展到指定的长度。把主体放在中间，两边自动用空串补齐

```java
StringUtils.center(null, *)   = null
StringUtils.center("", 4)     = "    "
StringUtils.center("ab", -1)  = "ab"
StringUtils.center("ab", 4)   = " ab "
StringUtils.center("abcd", 2) = "abcd"
StringUtils.center("a", 4)    = " a  "
```

### 2.15. public static String capitalize(String str)、uncapitalize

首字母大、小写

### 2.16. public static String swapCase(String str)

去返大小写 大变小 小变大

```java
StringUtils.swapCase(null)                 = null
StringUtils.swapCase("")                   = ""
StringUtils.swapCase("The dog has a BONE") = "tHE DOG HAS A bone"
```

### 2.17. public static boolean isAlpha(CharSequence cs)

判断字符串是否全由字母组成 （只要存在汉字、中文、数字都为false）

```java
StringUtils.isAlpha(null)   = false
StringUtils.isAlpha("")     = false
StringUtils.isAlpha("  ")   = false
StringUtils.isAlpha("abc")  = true
StringUtils.isAlpha("ab2c") = false
StringUtils.isAlpha("ab-c") = false
```


### 2.18. public static String reverse(String str)

字符串翻转

```java
StringUtils.reverse(null)  = null
StringUtils.reverse("")    = ""
StringUtils.reverse("bat") = "tab"
```


### 2.19. public static String abbreviate(String str,int maxWidth)

缩略字符串，省略号要占三位。maxWith小于3位会报错。
这个在大篇幅需要显示的时候，很管用有木有
```java
StringUtils.abbreviate(null, *)      = null
StringUtils.abbreviate("", 4)        = ""
StringUtils.abbreviate("abcdefg", 6) = "abc..."
StringUtils.abbreviate("abcdefg", 7) = "abcdefg"
StringUtils.abbreviate("abcdefg", 8) = "abcdefg"
StringUtils.abbreviate("abcdefg", 4) = "a..."
StringUtils.abbreviate("abcdefg", 3) = IllegalArgumentException
```

高级用法：可以自定义缩略的部分内容角标

### 2.20. public static String wrap(String str,char wrapWith)

包装，用后面的字符串对前面的字符串进行包装
其实相当于前后拼了相同的串

```java
StringUtils.wrap(null, *)        = null
StringUtils.wrap("", *)          = ""
StringUtils.wrap("ab", '\0')     = "ab"
StringUtils.wrap("ab", 'x')      = "xabx"
StringUtils.wrap("ab", '\'')     = "'ab'"
StringUtils.wrap("\"ab\"", '\"') = "\"\"ab\"\""
```

isAllBlank、isAllEmpty
这些都不解释了。处理数组可变参数而已

isAllLowerCase、isAllUpperCase
判断字符串所有字符是否都是大、小写