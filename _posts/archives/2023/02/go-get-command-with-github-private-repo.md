<font size=6>go get命令使用github.com的私有仓库 </font> 

------

有的时候自己编写的GO语言项目需要用到一些私有库。

通常这些私有库需要做一些配置才能让go get命令成功把代码拉取到本地

## 生成自己的访问秘钥

在github.com上为自己的账号设置访问token，并配置好权限。

![进入到个人设定页面](images/1.png)
![选择开发者设定](images/2.png)
![选择生成个人访问Token](images/3.png)
![生成新的Token](images/4.png)

生成新的Token的时候一定要注意勾选的权限。建议选择仓库读写即可，不要带其他权限。

## 修改git配置

```txt
git config --global url."https://USERNAME:TOKEN@github.com/".insteadOf "https://github.com/"
```
执行以上命令后再$HOME/.gitconfig文件机会生成如下配置

![gitconfig](images/5.png)

## 修改go env值

从Go 1.11开始所有依赖默认由mod管理， go命令会从goproxy.io上下载依赖包，并且会对下载的软件包和代码库进行安全校验。

但，如果使用的是私有库，则就无法从goproxy.io上下载软件并且也无法进行校验。所以，使用私有仓库需要做如下设定。

|需要|私有仓库信息| 作用范围| 说明|
|:---|---|---|---:|
|1|github.com/taylor840326/xxx-xxx|仓库|只作用在该仓库上|
|2|github.com/taylor840326|组织或用户|作用到组织或用户所属的仓库上|
|3|github.com|全域|github.com全域的所有仓库|

```bash
export GOPRIVATE="github.com/taylor840326/xxx-xxx"
```

设定好GOPRIVATE后GONOPROXY和GONOSUMDB也随着设定了。这表示配置在GOPRIVATE上的仓库不走代理和不进行信息的校验。

## 使用go get下载

以上配置都设定好以后就可以尝试使用go get命令下载配置了

go get 命令的职责下载源码并且将依赖添加至go.mod文件中。

```bash
$ go get github.com/taylor840326/xxx-xxx
```

按照官方文档对go get说明罗列出go get命令又如下几种用法:

更新一个指定的模块

```bash
$ go get golang.org/x/net

加上-u选项会拉最新的模块变更，否则本地存在就不重复拉取
```

对一个模块进行指定版本的升级或降级

```bash
$ go get golang.org/x/net@v0.3.2
```

更新把指定模块的指定分支的最新提交

```bash
$ go get golang.org/x/net@master
```

移除某个模块

```bash
$ go get golang.org/x/net@none
```