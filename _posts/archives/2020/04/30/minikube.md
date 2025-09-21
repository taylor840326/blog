## 使用minikube搭建k8s测试环境

-----

## 1.前言

网上有关使用minikube搭建k8s环境的文章很多，但是为了自己后面检索方便，还是单独记录下来。

本文档只说明如何在我的工作电脑上使用minikube+kvm2启动一套k8s测试环境。

## 2.前提条件

我的工作电脑系统是Ubuntu 18.04,我要在这个系统上通过minikube+kvm2搭建k8s环境。

## 3.搭建过程

### 3.1.安装qemu-kvm及kvm2自启程序。

首先需要先安装qemu-kvm及周边工具

```bash
# apt -y install qemu-kvm libvirt-bin virtinst bridge-utils libosinfo-bin libguestfs-tools virt-top 
```

安装完成后需要创建一个网桥设备，在Ubuntu系统上使用netplan来管理网络。按照如下步骤开启这个功能

```bash
# modprobe vhost_net

# echo vhost_net >>/etc/modules

# vim /etc/netplan/xxx-netcfg.yaml
network:
  version: 2
  renderer: networkd
  ethernets:
    ens3:  #在我的环境中使用的网口设备是ens3
      dhcp4: no
      # disable existing configuration for ethernet
      #addresses: [10.0.0.30/24]
      #gateway4: 10.0.0.1
      #nameservers:
        #addresses: [10.0.0.10]
      dhcp6: no

  # add configuration for bridge interface
  bridges:
    br0:
      interfaces: [ens3]   #把ens3设备加入到网桥br0，并设置静态网络地址。
      dhcp4: no
      addresses: [10.0.0.30/24]
      gateway4: 10.0.0.1
      nameservers:
        addresses: [10.0.0.10]
      parameters:
        stp: false
      dhcp6: no

# reboot
重启服务器
# ip addr
重启收确认网络配置已经生效
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host
       valid_lft forever preferred_lft forever
2: ens3: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc fq_codel master br0 state UP group default qlen 1000
    link/ether 52:54:00:ac:76:41 brd ff:ff:ff:ff:ff:ff
3: br0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc noqueue state UP group default qlen 1000
    link/ether aa:b3:1c:89:71:0e brd ff:ff:ff:ff:ff:ff
    inet 10.0.0.30/24 brd 10.0.0.255 scope global br0
       valid_lft forever preferred_lft forever
    inet6 fe80::a8b3:1cff:fe89:710e/64 scope link
       valid_lft forever preferred_lft forever
4: virbr0: <NO-CARRIER,BROADCAST,MULTICAST,UP> mtu 1500 qdisc noqueue state DOWN group default qlen 1000
    link/ether 52:54:00:cb:78:ae brd ff:ff:ff:ff:ff:ff
    inet 192.168.122.1/24 brd 192.168.122.255 scope global virbr0
       valid_lft forever preferred_lft forever
5: virbr0-nic: <BROADCAST,MULTICAST> mtu 1500 qdisc fq_codel master virbr0 state DOWN group default qlen 1000
    link/ether 52:54:00:cb:78:ae brd ff:ff:ff:ff:ff:ff

```
以上部分我直接从别的网站拷贝过来的。特把网站地址贴到下面

```html
https://www.server-world.info/en/note?os=Ubuntu_18.04&p=kvm&f=1
```

安装好qemu-kvm后，你可以尝试使用virt-manager图形命令创建一个虚拟机，查看虚拟机是否可以创建并启动成功。这里不再重复说明

下面需要安装minikube的kvm2启动程序docker-machine-driver-kvm2-[amd64|x86_64]。注意：amd64和x86_64是两个完全不同的文件，你的CPU是哪家的就下载哪个。

```bash
# wget -O /usr/bin/docker-machine-driver-kvm2-amd64  https://github.com/kubernetes/minikube/releases/download/v1.10.0-beta.2/docker-machine-driver-kvm2-amd64
或者

# wget -O /usr/bin/docker-machine-driver-kvm2-x86_64   https://github.com/kubernetes/minikube/releases/download/v1.10.0-beta.2/docker-machine-driver-kvm2-x86_64

我的机器是AMD的CPU，所以我就下载amd64那个文件。

# chmod +x /usr/bin/docker-machine-driver-kvm2-amd64
文件赋予可执行权限

```

至此，底层虚拟机软件栈准备完成。


### 3.2.安装minikube、kubectl

下面开始安装minikube和kubectl。

minikube也很好安装，kubernetes的github有专门的minikube仓库。从仓库下载编译好的二进制文件到制定目录即可。

```bash
# wget -O /usr/bin/minikube https://github.com/kubernetes/minikube/releases/download/v1.10.0-beta.2/minikube-linux-amd64

# chmod +x /usr/bin/minikube

```

kubernetes的kubectl工具使用阿里云的kubernetes镜像安装，方法如下所示：

```bash
apt-get update && apt-get install -y apt-transport-https
curl https://mirrors.aliyun.com/kubernetes/apt/doc/apt-key.gpg | apt-key add - 
cat <<EOF >/etc/apt/sources.list.d/kubernetes.list
deb https://mirrors.aliyun.com/kubernetes/apt/ kubernetes-xenial main
EOF  
apt-get update
apt-get install -y kubectl

```

以上步骤详见阿里云镜像站地址：

```html
https://developer.aliyun.com/mirror/kubernetes

```

### 3.3.启动k8s测试环境


下面就可以尝试使用minikube启动k8s测试环境了。

我把自己的启动脚本贴出来

```bash
#!/bin/bash

minikube start \
	--vm-driver kvm2 \
	--nodes=1 \
	--cpus=4 \
	--memory=12g \
	--disk-size=128g \
	--image-mirror-country=cn \
	--iso-url=https://kubernetes.oss-cn-hangzhou.aliyuncs.com/minikube/iso/minikube-v1.9.0.iso  \
	--registry-mirror=https://y6sfrven.mirror.aliyuncs.com  \
	--image-repository=registry.cn-hangzhou.aliyuncs.com/google_containers

```
