## Codec

-----

### 1. 简介

Apache Commons Codec提供常见编码器和解码器的实现。

### 2. 依赖

配置Maven依赖

```xml
<dependency>
    <groupId>commons-codec</groupId>
    <artifactId>commons-codec</artifactId>   <!-- 提供Hex、DigestUtils、Base64等 -->
    <version>1.11</version>
</dependency>
```

### 3. URL编码

应用：要放在url里的值，需要URL编码

```java
System.out.println(URLEncoder.encode("原文", "UTF-8"));
```

### 4. 摘要 MD5

应用：用户密码加密

```java
MessageDigest md = MessageDigest.getInstance("MD5");  // JDK提供的MD5方法
byte[] md5Bytes = md.digest("原文".getBytes());  // 加密字节序列 得到 128个比特的摘要，即16字节
System.out.println(Hex.encodeHex(md5Bytes));  // 每4个比特转成一位16进制数
 
System.out.println( DigestUtils.md5Hex("原文"));  // Apache common codec 提供的MD5方法，最终调用的还是JDK
```


### 5. 摘要 SHA

应用：数字签名

```java
SHA-1（160比特）、SHA2（SHA-256、SHA-384、SHA-512）

MessageDigest md = MessageDigest.getInstance("SHA-256");  // JDK提供的SHA方法
byte[] md5Bytes = md.digest("原文".getBytes());
System.out.println(Hex.encodeHex(md5Bytes));

System.out.println( DigestUtils.sha256Hex("原文"));  // Apache common codec 提供的SHA方法，最终调用的还是JDK
```


### 6. 带有密钥的摘要 Hmac

应用：会话认证MAC

```java
SecretKey sk = KeyGenerator.getInstance("HmacMD5").generateKey();  // 得到密钥
SecretKey rsk = new SecretKeySpec(sk.getEncoded(), "HmacMD5"); // 格式化密钥

Mac mac = Mac.getInstance("HmacMD5");   // 确定算法
mac.init(rsk);  // 确定密钥
byte[] digest = mac.doFinal("原文".getBytes());  // 加密
System.out.println(Hex.encodeHexString(digest));
```

### 7. 对称密码 之 Base64编码

每6比特转成一位64进制数

应用：字节数组对应的字符串是乱码时，可以进行Base64编码，使得能显示成简单的字符串

```java
BASE64Encoder encoder = new BASE64Encoder();  // JDK提供
String enStr = encoder.encode("原文".getBytes());  // 编码
enStr = enStr.replaceAll("[\s\t\n\r]", "");    // 去掉可能有的换行符
System.out.println(enStr);
BASE64Decoder decoder = new BASE64Decoder();
System.out.println(new String(decoder.decodeBuffer(enStr))); // 解码

byte[] result = Base64.encodeBase64("原文".getBytes()); // Apache common codec提供，不会带换行符
String enStr = new String(result);  // 编码
enStr = enStr.replaceAll("=", "");    // 去掉末尾可能有的等号
System.out.println(new String(Base64.decodeBase64(result)));  // 解码
```


### 8. 对称密码之 AES

```java
// 生成key
KeyGenerator kg = KeyGenerator.getInstance("AES");
kg.init(128);  // 确定密钥长度
byte[] keyBytes = kg.generateKey().getEncoded();
// 格式化key
Key key = new SecretKeySpec(keyBytes, "AES");

Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); // 确定算法
cipher.init(Cipher.ENCRYPT_MODE, key);    // 确定密钥
byte[] result = cipher.doFinal("原文".getBytes());  // 加密
System.out.println(Base64.encodeBase64String(result));  // 不进行Base64编码的话，那么这个字节数组对应的字符串就是乱码

cipher.init(Cipher.DECRYPT_MODE, key); // 进入解密模式
System.out.println(new String(cipher.doFinal(result))); // 解密
对称加密之 基于口令的密码PBE
String pwd = "hogen";     // 口令
PBEKeySpec keySpec = new PBEKeySpec(pwd.toCharArray());  // 密钥格式化
SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");  // 密钥工厂
Key key = factory.generateSecret(keySpec);

SecureRandom random = new SecureRandom();  // 强随机数生成器
byte[] salt = random.generateSeed(8);  // 盐
PBEParameterSpec parameterSpec = new PBEParameterSpec(salt, 100);  // PBE参数格式化

Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");  // 确定算法
cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec); // 确定口令 和 盐
byte[] result = cipher.doFinal("原文".getBytes()); // 加密
System.out.println(Base64.encodeBase64String(result));

cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec); // 进入解密模式
System.out.println(new String(cipher.doFinal(result))); // 解密
```

### 9. 非对称加密 RSA

以下示例为私钥加密，公钥解密

```java
// 生成密钥对
KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
keyPairGenerator.initialize(512);
KeyPair keyPair = keyPairGenerator.generateKeyPair();
RSAPublicKey rsaPublicKey = (RSAPublicKey)keyPair.getPublic();
RSAPrivateKey rsaPrivateKey = (RSAPrivateKey)keyPair.getPrivate();
// 格式化私钥
PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(rsaPrivateKey.getEncoded());
KeyFactory keyFactory = KeyFactory.getInstance("RSA");
PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

Cipher cipher = Cipher.getInstance("RSA");  // 确定算法
cipher.init(Cipher.ENCRYPT_MODE, privateKey);  // 确定加密密钥
byte[] result = cipher.doFinal("原文".getBytes());  // 加密
System.out.println(Base64.encodeBase64String(result));

// 格式化公钥
X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(rsaPublicKey.getEncoded());
keyFactory = KeyFactory.getInstance("RSA");
PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);

cipher = Cipher.getInstance("RSA"); // 确定算法
cipher.init(Cipher.DECRYPT_MODE, publicKey);  // 确定公钥
System.out.println(new String(cipher.doFinal(result))); // 解密
```

### 参考资料 

```html
作者：saoraozhe3hao
链接：https://www.jianshu.com/p/cf5d511d2db0
来源：简书
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
```
