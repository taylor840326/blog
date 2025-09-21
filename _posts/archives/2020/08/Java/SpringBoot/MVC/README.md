## MVC
-----


## 1.常用HTTP请求

5种常见的请求类型有

1. GET ：请求从服务器获取特定资源。举个例子：GET /users（获取所有学生）
1. POST ：在服务器上创建一个新的资源。举个例子：POST /users（创建学生）
1. PUT ：更新服务器上的资源（客户端提供更新后的整个资源）。举个例子：PUT /users/12（更新编号为 12 的学生）
1. DELETE ：从服务器删除特定的资源。举个例子：DELETE /users/12（删除编号为 12 的学生）
1. PATCH ：更新服务器上的资源（客户端提供更改的属性，可以看做作是部分更新），使用的比较少，这里就不举例子了。

### 3.1.GET

@GetMapping("users)注解等价于@RequestMapping(value="/users",method = ReuestMehtod.GET)

```java
@GetMapping("/users")
public ResponseEntity<List<Users>> getALLUsers(){
    return userRepostiroy.findAll();
}
```

### 3.1.POST

@PostMapping("users)等价于@RequestMapping(value="/users",method = RequestMethod.POST)

```java
@PostMapping("/users")
public ResponseEntity<User> createUser(@Valid @RequestBody UserCreateReuqest userCreateRequest){
    return userRepository.save(user);
}
```

### 3.1.PUT

@PutMapping("/users/{userId})等价于@RequestMapping(value = "/users/{userId}",method = RequestMethod.PUT)

```java
@PutMapping("/users/{userId}")
public ResponseEntity<User> updateUser(@PathVariable(value = "userId") Long userId,@Valid @RequestBody UserUpdateRequest userUpdateRequest){
    ...
}
```

### 3.1.DELETE

@Deletemapping("/usrs/{userId})等价于@RequestMapping(value = "/users/{userId}",method = RequestMethod.DELETE)

```java
@DeleteMapping("/users/{userId}")
public ResponseEntity<User> deleteUser(@PathVariable(value = "userId") Long userId){
    ....
}
```

### 3.1.PATCH

一般实际项目中，都是PUT方法不够用了采用PATCH请求去更新数据

```java
@PatchMapping("/profile")
public ResponseEntity<User> updateStudent(@RequestBody StudentUpdateRquest studentUpdateRequest){
    studentRepository.updateDetail(studentUpdateRquest);
    return ResponseEntity.ok().build();
}
```

## 2.前后端传值

### 4.1.@PathVariable和@RequestParam

@PathVariable用于获取路径参数

@RequestParam用于获取参训参数

```java
@GetMapping("/klasses/{klassId}/teachers")
public List<Teacher> getKlassRelatedTeacher(@PathVariable("klassId") Long klassId,@RequestParam(value = "type",required = false) String type){
    ...
}
```

如果我们请求的url是/klass/{123456}/teachers?type=web

则服务获取到的数据就是：klassId = 123456,type = web

### 4.2.@RequestBody

@RequestBody注解用于读取Request请求(GET/POST/PUT/DELETE)的body部分，并且Content-Type为application/json格式的数据。

接受到的数据会自动兵丁到Java 对象上。

系统会使用HttpMessageConverter或者自定义的HttpMessageConverter将请求的body中的json字符串转换为Java对象。

```java
@PostMapping("/sign-up")
public ResponseEntity signUp(@RequestBody @Valid UserRegisterRequest userRegisterReuqest){
    userService.save(userRegisterReuqest);
    return ResponseEntity.ok().build();
}
```

UserRegisterRequest对象定义如下

```java
@Data 
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterRequest {
    
    @NotBlank
    private String userName;
    
    @NotBlank
    private String password;
    
    @FullName
    @NotBlank
    private String fullName;
}
```

如果对接口发送一个POST请求，并且body的JSON数据为

```html
curl -XPOST http://localhost:8080/api/users/sign-up -d 

{
  "userName": "coder",
  "fullName": "beijing",
  "password": "123456"
}
```

这样json里面的数据就会被绑定在UserRegisterRequest类上。

需要注意：
1. 一个请求方法只可以有一个@RequestBody，但是可以有多个@RequestParam或者@PathVariable
1. 如果你的方法必须要有两个@RequestBody来接受数据的话，大概率是你的数据库设计或者系统设计出问题了。
