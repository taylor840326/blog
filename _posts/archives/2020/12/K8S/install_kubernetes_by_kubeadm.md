## 使用Kubeadm安装Kubernetes环境

-----

## 1. 概述

本文主要描述使用kubeadm命令安装kubernetes的过程。

kubeadm/kubelet/kubectl命令使用阿里云提供的软件源仓库。Docker镜像使用阿里云提供的相应镜像仓库。
 

## 2. 环境介绍

安装最简环境需要最少1台物理服务器或者虚拟机。

为了后期方面做实验，建议使用3台服务作为基础硬件环境。

假设需要在有如下服务器上搭建kubernetes环境。

|操作系统|主机名|内网IP地址|外网IP地址|功能|配置|
|:---|---|---|---|---|---:|
|Ubuntu 20.04|hadoop01|192.168.56.11/24|172.18.9.11/20|主节点|2核8G|
|Ubuntu 20.04|hadoop02|192.168.56.12/24|172.18.9.12/20|从节点|2核8G|
|Ubuntu 20.04|hadoop03|192.168.56.13/24|172.18.9.13/20|从节点|2核8G|
 
**注意:**
1. 请确保CPU至少2核，内存建议8GB。
1. 内网IP用于K8S各个组件的数据交互；各个Pods的数据交互。
1. 外网IP用于后面和外面组件进行交互。


## 3. 安装前准备

### 3.1. 主机名

确保3台主机的 /etc/hostname 已经修改为正确的主机名。

修改主机名可以采用hostnamectl set-hostname完成

```bash
hadoop01
# hostnamectl set-hostname hadoop01
hadoop02
# hostnamectl set-hostname hadoop02
hadoop03
# hostnamectl set-hostname hadoop03
```

### 3.2. 时区

保证3台服务器的时区是一样的。

强制更改时区为上海，执行以下命令

```bash
hadoop01
# timedatectl set-timezone "Asia/Shanghai"
hadoop02
# timedatectl set-timezone "Asia/Shanghai"
hadoop03
# timedatectl set-timezone "Asia/Shanghai"
```

### 3.3. 安装ntp及ntpdate

在三台服务器上都要执行如下命令安装时间同步服务

```bash
hadoop01/hadoop02/hadoop03
# apt-get install -y ntpdate
```

### 3.4. 禁用swap

在所有主机上执行如下命令禁用swap

```bash
hadoop01/hadoop02/hadoop03
#sudo sed -i '/swap/ s/^/#/' /etc/fstab
#sudo swapoff -a
```

### 3.5. 安装Docker

在所有主机上执行如下命令安装docker。

```bash
hadoop01/hadoop02/hadoop03
# apt -y install docker.io
```

### 3.6. 配置kubernetes的安装源.

由于墙的原因，国内安装Kubernetes非常不方便，好在阿里云提供了Kubernetes的软件源。

在所有主机上执行如下命令添加apt key以及软件源

```bash
hadoop01/hadoop02/hadoop03
# apt update && sudo apt install -y apt-transport-https curl
# curl -s https://mirrors.aliyun.com/kubernetes/apt/doc/apt-key.gpg | sudo apt-key add -

#echo "deb https://mirrors.aliyun.com/kubernetes/apt/ kubernetes-xenial main" >>/etc/apt/sources.list.d/kubernetes.list
``` 

### 3.7. 安装Kubernetes三件套

在所有主机上安装kubernetes的kubeadm/kubectl/kubelet三件套。

```bash
hadoop01/hadoop02/hadoop03
# apt update
# apt install -y kubelet kubeadm kubectl
阻止软件在执行apt upgrade命令时自动更新
# apt-mark hold kubelet kubeadm kubectl
```
 
## 4. 安装Kubernetes集群


### 4.1. 初始化Kubernets主服务

在hadoop01节点上使用root用户环境执行如下命令

```bash
hadoop01
# kubeadm init --image-repository registry.aliyuncs.com/google_containers --kubernetes-version v1.20.1 --pod-network-cidr=192.168.0.0/24 --ignore-preflight-errors="Swap" --apiserver-advertise-address="192.168.56.11"| tee /etc/kube-server-key
```

参数解释：
```txt
--image-repository 指定镜像源为阿里云的源。这样就会避免拉取镜像超时而失败。

--kubernetes-version 指定Kubernetes版本

--pod-network-cidr 指定pod网络地址。

--apiserver-advertise-address 执行apiserver的运行地址。后面从节点需要访问此IP访问apiserver。
```

上述命令执行完成后，输出信息会保存到 /etc/kube-server-key 文件中。

拷贝kubeconfig文件到家目录的.kube目录

```bash
hadoop01
# mkdir -p $HOME/.kube
# cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
# chown $(id -u):$(id -g) $HOME/.kube/config
```
 

### 4.2. 安装网络插件，让pod之间通信

在hadoop01节点上安装网路插件

```bash
hadoop01
# kubectl apply -f https://docs.projectcalico.org/v3.8/manifests/calico.yaml
```
 
### 4.3. 查看kube-system命名空间下的pod状态

网络插件安装完成后就可以执行如下命令查看kube-system命令空间下的pod状态

```bash
hadoop01
# kubectl get pod -n kube-system
```
等待1分钟，效果如下：

```txt
NAME                                       READY   STATUS    RESTARTS   AGE
calico-kube-controllers-7bd78b474d-lpfvf   0/1     Running   0          67s
calico-node-vfm28                          1/1     Running   0          67s
coredns-bccdc95cf-dm4pb                    1/1     Running   0          111s
coredns-bccdc95cf-lvhcg                    1/1     Running   0          111s
etcd-k8s-master                            1/1     Running   0          69s
kube-apiserver-k8s-master                  1/1     Running   0          67s
kube-controller-manager-k8s-master         1/1     Running   0          59s
kube-proxy-jpqsq                           1/1     Running   0          111s
kube-scheduler-k8s-master                  1/1     Running   0          56s
```
 

### 4.3. 查看加入节点命令

在hadoop01节点上查看节点加入命令
```bash
hadoop01
# cat /etc/kube-server-key | tail -2
输出：
kubeadm join 192.168.56.11:6443 --token bz16uu.olqxoh5q5bnt50sd \
    --discovery-token-ca-cert-hash sha256:9177017ff3016dbb2aadf7484f7823f8b963c989fe9ecdccbe601c9305ce000f
```

输出信息解释：
```txt
192.168.56.11:6443 apiserver的地址和访问端口
--token bz16uu.olqxoh5q5bnt50sd 临时分配的token信息
--discovery-token-ca-cert-hash sha256:9177017ff3016dbb2aadf7484f7823f8b963c989fe9ecdccbe601c9305ce000f token的ca证书hash信息
```

### 4.4. 加入从节点

在其他节点hadoop02/hadoop03上执行如下命令加入到刚刚初始化好的kubernetes集群

```bash
hadoop
# kubeadm join 192.168.56.11:6443 --token bz16uu.olqxoh5q5bnt50sd \
    --discovery-token-ca-cert-hash sha256:9177017ff3016dbb2aadf7484f7823f8b963c989fe9ecdccbe601c9305ce000f
``` 

等待5分钟，在hadoop01上查看集群的状态

```bash
hadoop01
# kubectl get nodes
NAME         STATUS   ROLES    AGE     VERSION
hadoop01     Ready    master   5m54s   v1.20.1
hadoop02     Ready    <none>   73s     v1.20.1
hadoop03     Ready    <none>   71s     v1.20.1
```
 

### 4.5. 命令补全

在hadoop01节点上执行如下命令进行命令行补全

```bash
hadoop01
# apt-get install bash-completion
# source <(kubectl completion bash)
# echo "source <(kubectl completion bash)" >> ~/.bashrc
# source  ~/.bashrc
```

### 4.6. 部署dashboard可视化插件

Kubernetes Dashboard 提供了 kubectl 的绝大部分功能。

可以通过如下命令进行安装

```bash
hadoop01
# kubectl apply -f http://mirror.faasx.com/kubernetes/dashboard/master/src/deploy/recommended/kubernetes-dashboard.yaml
``` 


## 5. 参考链接：

```html
https://blog.csdn.net/a610786189/article/details/80321727


https://www.toutiao.com/i6703112655323791884

https://www.cnblogs.com/busigulang/p/10736040.html

https://www.cnblogs.com/qingfeng2010/p/10540832.html

https://www.cnblogs.com/kenken2018/p/10340157.html
```