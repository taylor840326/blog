## 跨集群搜索
-----

### 1. 水平扩展的痛点

**单集群 - 当水平扩展时，节点数不能无线增加** 

当集群的meta信息（节点、索引、集群状态）过多，会导致更新压力变大，单个Active Master会成为性能瓶颈，导致整个集群无法正常工作

**早期版本跨集群搜索方案**

早期版本，通过Tribe Node可以实现多集群访问的需求，但是还是存在一定的问题

1. Tribe Node会以Client Node的方式加入每个集群。集群中Master节点的任务变更需要Tribe Node的回应才能继续。
1. Tribe Node不保存Cluster State信息，一旦重启，初始化很慢。
1. 当多个集群存在索引重名的情况时，只能设置一种Prefer规则
1. Tribe Node的方案已经再5.3版本开始弃用了。

新版本的ES(5.3以后版本)引入了跨集群搜索的功能（Cross Cluster Search），推荐使用

1. 允许任何节点扮演Federated节点，以轻量的方式，将搜索请求进行代理。
1. 不需要以Client Node的形式计入其他集群。



### 2. 如何开启Cross Cluster Search设置

```html
PUT _cluster/settings
{
    "persistent":{
        "cluster":{
            "remote":{
                "cluster_one":{
                    "seeds":["127.0.0.1:9300"]
                },
                "cluster_two":{
                    "seeds":["127.0.0.1:9301"]
                },
                "cluster_three":{
                    "seeds":["127.0.0.1:9302"]
                }
            }
        }
    }
}

GET /cluster_one:tmdb,movies/_search
{
    "query":{
        "match":{
            "title":"matrix"
        }
    }
}

PUT _cluster/settings
{
    "persistent":{
        //设置当远程集群失去响应的时候，搜索可以跳过这个集群继续执行
        "cluster.remote.cluster_two.skip_unavailable": true
    }
}
```