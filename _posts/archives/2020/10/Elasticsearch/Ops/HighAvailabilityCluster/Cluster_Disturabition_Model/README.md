## 集群分布式模型、选主和脑裂问题
-----

### 分布式特性

Elasticsearch的分布式架构带来的好处

1. 存储的水平扩容，支持PB级数据
1. 提高系统的可用性，部分节点停止服务，整个集群的服务不受影响

Elasticsearch 的分布式架构

1. 不同的集群通过不同的名字来区分。默认名字elasticsearch
1. 通过配置文件修改，或者再命令行中 -E cluster.name= geektime进行设定

### 节点

节点是一个Elasticsearch的实例。其本质上就是一个Java进程

一台机器上可以运行多个Elasticsearch进程，但是生产环境一般建议一台机器上就运行一个Elasticsearch实例

每个节点都有自己的名字，通过配置文件的配置，或者启动时候-E node.name = geektime指定

每个节点再启动之后，会分配一个UID，保存再data目录下。

###  Coordinating Node

处理请求的节点，叫Coordinating Node

路由请求到正确的节点，例如创建索引的请求，需要路由到Master节点

所有节点默认都是Coordinating Node

通过将其他类型设置成false，使其成为Dedicated Coordinating Node


### Data Node

可以保存数据的节点，叫做Data Node

节点启动后，默认就是数据节点。可以设置node.data: false禁止

Data Node的职责

保存分片数据。再数据扩展上起到了至关重要的作用(由Master Node决定如何把分片分发到数据节点上)

通过增加数据节点： 可以解决**数据水平扩展**和解决**数据单点**的问题


### Master Node

Master Node的职责

1. 处理创建、删除索引等请求/ 决定分片被分配到哪个节点 / 负责索引的创建和删除
1. 维护并且更新Cluster State

Master Node的最佳实践

1. Master节点非常重要，在部署上需要考虑解决单点的问题
1. 为一个集群设置多个Master节点 / 每个节点只承担Master的单一角色


### Master Eligible Nodes && 选主流程

一个集群，支持配置多个Master Eligible节点。这些节点可以在必要时(如Master节点出现故障，网络故障时)参与选主流程，成为Master节点

每个节点启动后，默认就是一个Master Eligible节点。可以设置node.master: false禁止

当集群内第一个Master Eligible节点启动的时候，它会将自己选举成Master节点

### Master Eligible Nodes 的选主的过程

互相ping对方，Node Id低 的会成为被选举的节点

其他节点会加入集群，但是不承担Master节点的角色。一旦发现被选中的主节点丢失，就会选举出新的Master节点

### 脑裂问题

Split-Brain分布式系统的经典网络问题。当出现网络问题，一个节点和其他节点无法链接的时候

1. Node2和Node3会重新选举Master
1. Node1自己还是作为Master，组成一个集群，同时更新Cluster State
1. 导致2个master，维护不同的Cluster State。当网络恢复时，无法选择正确的Master。

### 如何避免脑裂的问题

**限定一个选举条件，设置quorum(仲裁)**。

只有在Master Eligible节点数大于quorum时，才能进行选举

Quorom = (master节点总数/2) + 1

当3个Master Eligible时，设置discovery.zen.minimum_master_nodes 为2,即可避免脑裂

**从7.0开始的配置** 

移除minimum_master_nodes参数，让Elasticsearch自己选择可以形成仲裁的节点。

典型的主节点选举现在只需要很短的时间就可以完成。集群的伸缩变得更加安全、更容易。并且可能造成丢失数据的系统配置选项更少了。

节点更清楚地记录他们的状态，有助于诊断为什么他们不能加入集群或为什么无法选举主节点


### 配置节点类型

一个节点默认情况下是一个Master Eligible，data和Igest节点

|节点类型| 配置参数| 默认值|
|:---|---|---:|
|Master Eligible| node.master| true|
|Data| node.data| true|
|Ingest| node.ingest| true|
|coordinating only|无|设置上面三个参数全部为false|
|Machine Learing| node.ml|true(需要enable x-pack)|