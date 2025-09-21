## SpringBoot项目完成目录结构及解析
-----

## 1. 通常一个基于SpringBoot编写的项目的目录结构有

```text
project
    -> src
        -> main
            -> java
                -> package //com.sample.prject com.sample公司一级域名,project项目名
                    -> 子包 (api/service/entity...)
```



|序号|目录名称|目录用途|目录可能用到的注解|
|:-----|-----|-----|-----:|
|1| api|保存所有API相关代码|Controller,RestController,GetMapping,PostMapping,DeleteMapping,PutMapping...|

