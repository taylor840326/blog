## 注册仓库

-----

### 1. 概要

在 Elasticsearch 中通过 repository 定义备份存储类型和位置。

存储类型有：

1. 共享文件系统
1. AWS 的 S3存储
1. HDFS
1. 微软 Azure的存储
1. Google Cloud 的存储等

当然你也可以自己写代码实现国内阿里云的存储。我们这里以最简单的共享文件系统为例，你也可以在本地做实验。

### 2. 配置共享文件系统

在Elasticsearch端配置仓库的之前要保证有对应的高可用的存储服务可以使用，一下列出两个共享文件系统的配置方法

1. [HDFS](HDFS/README.md)
1. [NFS](NFS/README.md)


### 3. 创建存储库


首先，elasticsearch.yml 的配置文件中注明可以用作备份路径 path.repo ，如下所示：

path.repo: ["/mount/backups", "/mount/longterm_backups"]

配置好后，就可以使用 snapshot api 来创建一个 repository 了，如下我们创建一个名为 my_backup 的 repository。

PUT /_snapshot/my_backup
{
  "type": "fs",
  "settings": {
    "location": "/mount/backups/my_backup"
  }
}

之后我们就可以在这个 repository 中来备份数据了。

