---
layout: post
title:  k8s安装与试验
date:   2019-01-03 16:52:12 +08:00
category: 微服务架构
tags: docker
comments: true
---

* content
{:toc}

对于微服务来说，容器化技术的平台搭建和管理作为重要知识，这里介绍k8s！












## 常用命令

1、直接关闭防火墙

systemctl stop firewalld.service #停止firewall

systemctl disable firewalld.service #禁止firewall开机启动

*Docker 运行在 CentOS 7 上，要求系统为64位、系统内核版本为 3.10 以上;
*克隆vmware中的centos7:
```
1: vim  /etc/sysconfig/network-scripts/ifcfg-ens33

2.hostnamectl set-hostname centos77.magedu.com

2:vim /etc/hosts

3:systemctl restart network

centos7_2.com  192.168.42.101   master节点
centos7_3.com 192.168.42.102    node节点
centos7_4.com 192.168.42.103    node节点

```
yum程序占用解决方案：
* rm -f /var/run/yum.pid

*  yum - updatesd

*scp k8s_images.tar.bz2 root@192.168.42.103:/root/temp

* cp -r k8s_images/. /root/k8s_images 复制目标位置的目录不存在

docker的卸载和指定版本安装

journalctl -xeu kubelet
用来查看kubelet执行有什么错误。

解决端口占用
netstat -lnp|grep 8080

kill -9 [PID]

## 开始

## 1.准备基础环境

我们将使用kubeadm部署3个节点的 Kubernetes Cluster
flannel
**节点详细信息：**

| 节点主机名 | 节点IP         | 节点角色 | 操作系统  | 节点配置 |
| ---------- | -------------- | -------- | --------- | -------- |
| k8s-master | 192.168.58.103 | master   | CentOS7.4 | 2C4G     |
| k8s-node1  | 192.168.58.102 | node     | CentOS7.4 | 2C4G     |
| k8s-node2  | 192.168.58.102 | node     | CentOS7.4 | 2C4G     |

**节点组件分布：**
Master 和 Node 节点由于分工不一样，所以安装的服务不同，最终安装完毕，Master 和 Node 启动的核心服务分别如下：
| Master节点 | Node节点 |
| —— | —— |
| kube-apiserver | kube-proxy |
| kube-controller-manager | kube-flannel|
| kube-scheduler | other apps |
| kube-proxy | |
| etcd | |
| coredns | |
| kube-flannel | |
无特殊说明以下操作在所有节点执行：
**修改主机名：**

```
#master节点:
hostnamectl set-hostname k8s-master
#node1节点：
hostnamectl set-hostname k8s-node1
#node2节点:
hostnamectl set-hostname k8s-node2
```



**基本配置：**

```
#修改/etc/hosts文件
cat >> /etc/hosts << EOF
192.168.58.103 k8s-master
192.168.58.102 k8s-node1
192.168.58.101 k8s-node2
EOF

#关闭防火墙和selinux
systemctl stop firewalld && systemctl disable firewalld
sed -i 's/^SELINUX=enforcing$/SELINUX=disabled/' /etc/selinux/config && setenforce 0

#关闭swap
swapoff -a
yes | cp /etc/fstab /etc/fstab_bak
cat /etc/fstab_bak |grep -v swap > /etc/fstab
```



**配置时间同步**
使用chrony同步时间，配置master节点与网络NTP服务器同步时间，所有node节点与master节点同步时间。

配置master节点：

```
#安装chrony：
yum install -y chrony
#注释默认ntp服务器
sed -i 's/^server/#&/' /etc/chrony.conf
#指定上游公共 ntp 服务器，并允许其他节点同步时间
cat >> /etc/chrony.conf << EOF
server 0.asia.pool.ntp.org iburst
server 1.asia.pool.ntp.org iburst
server 2.asia.pool.ntp.org iburst
server 3.asia.pool.ntp.org iburst
allow all
EOF
#重启chronyd服务并设为开机启动：
systemctl enable chronyd && systemctl restart chronyd
#开启网络时间同步功能
timedatectl set-ntp true
```



配置所有node节点：
(注意修改master IP地址)

```
#安装chrony：
yum install -y chrony
#注释默认服务器
sed -i 's/^server/#&/' /etc/chrony.conf
#指定内网 master节点为上游NTP服务器
echo server 192.168.58.103 iburst >> /etc/chrony.conf
#重启服务并设为开机启动：
systemctl enable chronyd && systemctl restart chronyd
```



所有节点执行chronyc sources命令，查看存在以^*开头的行，说明已经与服务器时间同步

**设置网桥包经过iptalbes**
RHEL / CentOS 7上的一些用户报告了由于iptables被绕过而导致流量路由不正确的问题。创建/etc/sysctl.d/k8s.conf文件，添加如下内容：

```
cat <<EOF >  /etc/sysctl.d/k8s.conf
vm.swappiness = 0
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
net.ipv4.ip_forward = 1
EOF

# 使配置生效
modprobe br_netfilter
sysctl -p /etc/sysctl.d/k8s.conf
```



**kube-proxy开启ipvs的前提条件**
由于ipvs已经加入到了内核的主干，所以为kube-proxy开启ipvs的前提需要加载以下的内核模块：
在所有的Kubernetes节点执行以下脚本:

```
cat > /etc/sysconfig/modules/ipvs.modules <<EOF
#!/bin/bash
modprobe -- ip_vs
modprobe -- ip_vs_rr
modprobe -- ip_vs_wrr
modprobe -- ip_vs_sh
modprobe -- nf_conntrack_ipv4
EOF

#执行脚本
chmod 755 /etc/sysconfig/modules/ipvs.modules && bash /etc/sysconfig/modules/ipvs.modules && lsmod | grep -e ip_vs -e nf_conntrack_ipv4
```



上面脚本创建了/etc/sysconfig/modules/ipvs.modules文件，保证在节点重启后能自动加载所需模块。 使用lsmod | grep -e ip_vs -e nf_conntrack_ipv4命令查看是否已经正确加载所需的内核模块。
接下来还需要确保各个节点上已经安装了ipset软件包。 为了便于查看ipvs的代理规则，最好安装一下管理工具ipvsadm。

```
# yum install ipset ipvsadm -y
```



**安装Docker**
Kubernetes默认的容器运行时仍然是Docker，使用的是kubelet中内置dockershim CRI实现。需要注意的是，Kubernetes 1.13最低支持的Docker版本是1.11.1，最高支持是18.06，而Docker最新版本已经是18.09了，故我们安装时需要指定版本为18.06.1-ce。

```
#配置docker yum源
yum-config-manager \
    --add-repo \
http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo

#安装指定版本，这里安装18.06
yum list docker-ce --showduplicates | sort -r
yum install -y docker-ce-18.06.1.ce-3.el7
systemctl start docker && systemctl enable docker
```



脚本安装docker-ce并配置daocloud镜像加速(可选)：

```
bash Install_docker-ce.sh
```



**安装kubeadm、kubelet、kubectl**
官方安装文档可以参考：
<https://kubernetes.io/docs/setup/independent/install-kubeadm/>

- kubelet 在群集中所有节点上运行的核心组件, 用来执行如启动pods和containers等操作。
- kubeadm 引导启动k8s集群的命令行工具，用于初始化 Cluster。
- kubectl 是 Kubernetes 命令行工具。通过 kubectl 可以部署和管理应用，查看各种资源，创建、删除和更新各种组件。

```
#配置kubernetes.repo的源，由于官方源国内无法访问，这里使用阿里云yum源
cat <<EOF > /etc/yum.repos.d/kubernetes.repo
[kubernetes]
name=Kubernetes
baseurl=https://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el7-x86_64/
enabled=1
gpgcheck=1
repo_gpgcheck=1
gpgkey=https://mirrors.aliyun.com/kubernetes/yum/doc/yum-key.gpg https://mirrors.aliyun.com/kubernetes/yum/doc/rpm-package-key.gpg
EOF

#在所有节点上安装指定版本 kubelet、kubeadm 和 kubectl
yum install -y kubelet-1.13.1 kubeadm-1.13.1 kubectl-1.13.1

#启动kubelet服务
systemctl enable kubelet && systemctl start kubelet
```

## 2.部署master节点

完整的官方文档可以参考：
<https://kubernetes.io/docs/setup/independent/create-cluster-kubeadm/>
<https://kubernetes.io/docs/reference/setup-tools/kubeadm/kubeadm-init/>
**Master节点执行初始化**：

注意这里执行初始化用到了- -image-repository选项，指定初始化需要的镜像源从阿里云镜像仓库拉取。

```
kubeadm init \
    --apiserver-advertise-address=192.168.58.103 \
    --image-repository registry.aliyuncs.com/google_containers \
    --kubernetes-version v1.13.1 \
    --pod-network-cidr=10.244.0.0/16
```



**初始化命令说明：**

```
--apiserver-advertise-address
```

指明用 Master 的哪个 interface 与 Cluster 的其他节点通信。如果 Master 有多个 interface，建议明确指定，如果不指定，kubeadm 会自动选择有默认网关的 interface。

```
--pod-network-cidr
```

指定 Pod 网络的范围。Kubernetes 支持多种网络方案，而且不同网络方案对 –pod-network-cidr 有自己的要求，这里设置为 10.244.0.0/16 是因为我们将使用 flannel 网络方案，必须设置成这个 CIDR。

```
--image-repository
```

Kubenetes默认Registries地址是 k8s.gcr.io，在国内并不能访问 gcr.io，在1.13版本中我们可以增加–image-repository参数，默认值是 k8s.gcr.io，将其指定为阿里云镜像地址：registry.aliyuncs.com/google_containers。

```
--kubernetes-version=v1.13.1  
```

关闭版本探测，因为它的默认值是stable-1，会导致从[https://dl.k8s.io/release/stable-1.txt下载最新的版本号，我们可以将其指定为固定版本（最新版：v1.13.1）来跳过网络请求。](https://dl.k8s.io/release/stable-1.txt%E4%B8%8B%E8%BD%BD%E6%9C%80%E6%96%B0%E7%9A%84%E7%89%88%E6%9C%AC%E5%8F%B7%EF%BC%8C%E6%88%91%E4%BB%AC%E5%8F%AF%E4%BB%A5%E5%B0%86%E5%85%B6%E6%8C%87%E5%AE%9A%E4%B8%BA%E5%9B%BA%E5%AE%9A%E7%89%88%E6%9C%AC%EF%BC%88%E6%9C%80%E6%96%B0%E7%89%88%EF%BC%9Av1.13.1%EF%BC%89%E6%9D%A5%E8%B7%B3%E8%BF%87%E7%BD%91%E7%BB%9C%E8%AF%B7%E6%B1%82%E3%80%82)

**初始化过程如下：**

```
[root@k8s-master ~]# kubeadm init \
> --image-repository registry.aliyuncs.com/google_containers \
> --kubernetes-version v1.13.1 \
> --pod-network-cidr=10.244.0.0/16
[init] Using Kubernetes version: v1.13.1
[preflight] Running pre-flight checks
[preflight] Pulling images required for setting up a Kubernetes cluster
[preflight] This might take a minute or two, depending on the speed of your internet connection
[preflight] You can also perform this action in beforehand using 'kubeadm config images pull'
[kubelet-start] Writing kubelet environment file with flags to file "/var/lib/kubelet/kubeadm-flags.env"
[kubelet-start] Writing kubelet configuration to file "/var/lib/kubelet/config.yaml"
[kubelet-start] Activating the kubelet service
[certs] Using certificateDir folder "/etc/kubernetes/pki"
[certs] Generating "etcd/ca" certificate and key
[certs] Generating "etcd/healthcheck-client" certificate and key
[certs] Generating "apiserver-etcd-client" certificate and key
[certs] Generating "etcd/server" certificate and key
[certs] etcd/server serving cert is signed for DNS names [k8s-master localhost] and IPs [192.168.58.103 127.0.0.1 ::1]
[certs] Generating "etcd/peer" certificate and key
[certs] etcd/peer serving cert is signed for DNS names [k8s-master localhost] and IPs [192.168.58.103 127.0.0.1 ::1]
[certs] Generating "ca" certificate and key
[certs] Generating "apiserver" certificate and key
[certs] apiserver serving cert is signed for DNS names [k8s-master kubernetes kubernetes.default kubernetes.default.svc kubernetes.default.svc.cluster.local] and IPs [10.96.0.1 192.168.58.103]
[certs] Generating "apiserver-kubelet-client" certificate and key
[certs] Generating "front-proxy-ca" certificate and key
[certs] Generating "front-proxy-client" certificate and key
[certs] Generating "sa" key and public key
[kubeconfig] Using kubeconfig folder "/etc/kubernetes"
[kubeconfig] Writing "admin.conf" kubeconfig file
[kubeconfig] Writing "kubelet.conf" kubeconfig file
[kubeconfig] Writing "controller-manager.conf" kubeconfig file
[kubeconfig] Writing "scheduler.conf" kubeconfig file
[control-plane] Using manifest folder "/etc/kubernetes/manifests"
[control-plane] Creating static Pod manifest for "kube-apiserver"
[control-plane] Creating static Pod manifest for "kube-controller-manager"
[control-plane] Creating static Pod manifest for "kube-scheduler"
[etcd] Creating static Pod manifest for local etcd in "/etc/kubernetes/manifests"
[wait-control-plane] Waiting for the kubelet to boot up the control plane as static Pods from directory "/etc/kubernetes/manifests". This can take up to 4m0s
[apiclient] All control plane components are healthy after 21.009858 seconds
[uploadconfig] storing the configuration used in ConfigMap "kubeadm-config" in the "kube-system" Namespace
[kubelet] Creating a ConfigMap "kubelet-config-1.13" in namespace kube-system with the configuration for the kubelets in the cluster
[patchnode] Uploading the CRI Socket information "/var/run/dockershim.sock" to the Node API object "k8s-master" as an annotation
[mark-control-plane] Marking the node k8s-master as control-plane by adding the label "node-role.kubernetes.io/master=''"
[mark-control-plane] Marking the node k8s-master as control-plane by adding the taints [node-role.kubernetes.io/master:NoSchedule]
[bootstrap-token] Using token: 60syk6.vnplamkn3zhwu3s3
[bootstrap-token] Configuring bootstrap tokens, cluster-info ConfigMap, RBAC Roles
[bootstraptoken] configured RBAC rules to allow Node Bootstrap tokens to post CSRs in order for nodes to get long term certificate credentials
[bootstraptoken] configured RBAC rules to allow the csrapprover controller automatically approve CSRs from a Node Bootstrap Token
[bootstraptoken] configured RBAC rules to allow certificate rotation for all node client certificates in the cluster
[bootstraptoken] creating the "cluster-info" ConfigMap in the "kube-public" namespace
[addons] Applied essential addon: CoreDNS
[addons] Applied essential addon: kube-proxy

Your Kubernetes master has initialized successfully!

To start using your cluster, you need to run the following as a regular user:

  mkdir -p $HOME/.kube
  sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
  sudo chown $(id -u):$(id -g) $HOME/.kube/config

You should now deploy a pod network to the cluster.
Run "kubectl apply -f [podnetwork].yaml" with one of the options listed at:
  https://kubernetes.io/docs/concepts/cluster-administration/addons/

You can now join any number of machines by running the following on each node
as root:

  kubeadm join 192.168.58.103:6443 --token 60syk6.vnplamkn3zhwu3s3 --discovery-token-ca-cert-hash sha256:7d50e704bbfe69661e37c5f3ad13b1b88032b6b2b703ebd4899e259477b5be69

[root@k8s-master ~]#
```

(注意记录下初始化结果中的kubeadm join命令，部署worker节点时会用到)

初始化过程说明：

1. [preflight] kubeadm 执行初始化前的检查。
2. [kubelet-start] 生成kubelet的配置文件”/var/lib/kubelet/config.yaml”
3. [certificates] 生成相关的各种token和证书
4. [kubeconfig] 生成 KubeConfig 文件，kubelet 需要这个文件与 Master 通信
5. [control-plane] 安装 Master 组件，会从指定的 Registry 下载组件的 Docker 镜像。
6. [bootstraptoken] 生成token记录下来，后边使用kubeadm join往集群中添加节点时会用到
7. [addons] 安装附加组件 kube-proxy 和 kube-dns。
8. Kubernetes Master 初始化成功，提示如何配置常规用户使用kubectl访问集群。
9. 提示如何安装 Pod 网络。
10. 提示如何注册其他节点到 Cluster。

**配置 kubectl**

kubectl 是管理 Kubernetes Cluster 的命令行工具，前面我们已经在所有的节点安装了 kubectl。Master 初始化完成后需要做一些配置工作，然后 kubectl 就能使用了。
依照 kubeadm init 输出的最后提示



```

#追加sudo权限,并配置sudo免密
sed -i '/^root/a\centos  ALL=(ALL)       NOPASSWD:ALL' /etc/sudoers

#保存集群安全配置文件到当前用户.kube目录
su - centos
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config

#启用 kubectl 命令自动补全功能（注销重新登录生效）
echo "source <(kubectl completion bash)" >> ~/.bashrc
```



需要这些配置命令的原因是：Kubernetes 集群默认需要加密方式访问。所以，这几条命令，就是将刚刚部署生成的 Kubernetes 集群的安全配置文件，保存到当前用户的.kube 目录下，kubectl 默认会使用这个目录下的授权信息访问 Kubernetes 集群。
如果不这么做的话，我们每次都需要通过 export KUBECONFIG 环境变量告诉 kubectl 这个安全配置文件的位置。
配置完成后centos用户就可以使用 kubectl 命令管理集群了。

**查看集群状态：**

```
[root@k8s-master ~]$ kubectl get cs
NAME                 STATUS    MESSAGE              ERROR
scheduler            Healthy   ok
controller-manager   Healthy   ok
etcd-0               Healthy   {"health": "true"}
[centos@k8s-master ~]$
```



确认各个组件都处于healthy状态。
**查看节点状态**

```
[root@k8s-master ~]$ kubectl get nodes
NAME         STATUS     ROLES    AGE   VERSION
k8s-master   NotReady   master   36m   v1.13.1
[centos@k8s-master ~]$
```



可以看到，当前只存在1个master节点，并且这个节点的状态是 NotReady。
使用 kubectl describe 命令来查看这个节点（Node）对象的详细信息、状态和事件（Event）：

```
[root@k8s-master ~]$ kubectl describe node k8s-master
......
Events:
  Type    Reason                   Age                From                    Message
  ----    ------                   ----               ----                    -------
  Normal  Starting                 33m                kubelet, k8s-master     Starting kubelet.
  Normal  NodeHasSufficientMemory  33m (x8 over 33m)  kubelet, k8s-master     Node k8s-master status is now: NodeHasSufficientMemory
  Normal  NodeHasNoDiskPressure    33m (x8 over 33m)  kubelet, k8s-master     Node k8s-master status is now: NodeHasNoDiskPressure
  Normal  NodeHasSufficientPID     33m (x7 over 33m)  kubelet, k8s-master     Node k8s-master status is now: NodeHasSufficientPID
  Normal  NodeAllocatableEnforced  33m                kubelet, k8s-master     Updated Node Allocatable limit across pods
  Normal  Starting                 33m                kube-proxy, k8s-master  Starting kube-proxy.
```



通过 kubectl describe 指令的输出，我们可以看到 NodeNotReady 的原因在于，我们尚未部署任何网络插件，kube-proxy等组件还处于starting状态。
另外，我们还可以通过 kubectl 检查这个节点上各个系统 Pod 的状态，其中，kube-system 是 Kubernetes 项目预留的系统 Pod 的工作空间（Namepsace，注意它并不是 Linux Namespace，它只是 Kubernetes 划分不同工作空间的单位）：

```
[centos@k8s-master ~]$ kubectl get pod -n kube-system -o wide
NAME                                 READY   STATUS    RESTARTS   AGE   IP              NODE         NOMINATED NODE   READINESS GATES
coredns-78d4cf999f-7jdx7             0/1     Pending   0          29m   <none>          <none>       <none>           <none>
coredns-78d4cf999f-s6mhk             0/1     Pending   0          29m   <none>          <none>       <none>           <none>
etcd-k8s-master                      1/1     Running   0          34m   192.168.58.103   k8s-master   <none>           <none>
kube-apiserver-k8s-master            1/1     Running   0          34m   192.168.58.103   k8s-master   <none>           <none>
kube-controller-manager-k8s-master   1/1     Running   0          34m   192.168.58.103   k8s-master   <none>           <none>
kube-proxy-przwf                     1/1     Running   0          34m   192.168.58.103   k8s-master   <none>           <none>
kube-scheduler-k8s-master            1/1     Running   0          34m   192.168.58.103   k8s-master   <none>           <none>
[centos@k8s-master ~]$
```



可以看到，CoreDNS依赖于网络的 Pod 都处于 Pending 状态，即调度失败。这当然是符合预期的：因为这个 Master 节点的网络尚未就绪。
集群初始化如果遇到问题，可以使用kubeadm reset命令进行清理然后重新执行初始化。

**部署网络插件**
要让 Kubernetes Cluster 能够工作，必须安装 Pod 网络，否则 Pod 之间无法通信。
Kubernetes 支持多种网络方案，这里我们使用 flannel
执行如下命令部署 flannel：
kubectl apply -f kube-flannel.yml

```
[centos@k8s-master ~]$ kubectl apply -f kube-flannel.yml
clusterrole.rbac.authorization.k8s.io/flannel created
clusterrolebinding.rbac.authorization.k8s.io/flannel created
serviceaccount/flannel created
configmap/kube-flannel-cfg created
daemonset.extensions/kube-flannel-ds-amd64 created
daemonset.extensions/kube-flannel-ds-arm64 created
daemonset.extensions/kube-flannel-ds-arm created
daemonset.extensions/kube-flannel-ds-ppc64le created
daemonset.extensions/kube-flannel-ds-s390x created
[centos@k8s-master ~]$
```



部署完成后，我们可以通过 kubectl get 重新检查 Pod 的状态：

```
[centos@k8s-master ~]$ kubectl get pod -n kube-system -o wide
NAME                                 READY   STATUS    RESTARTS   AGE   IP              NODE         NOMINATED NODE   READINESS GATES
coredns-78d4cf999f-7jdx7             1/1     Running   0          11h   10.244.0.3      k8s-master   <none>           <none>
coredns-78d4cf999f-s6mhk             1/1     Running   0          11h   10.244.0.2      k8s-master   <none>           <none>
etcd-k8s-master                      1/1     Running   1          11h   192.168.58.103   k8s-master   <none>           <none>
kube-apiserver-k8s-master            1/1     Running   1          11h   192.168.58.103   k8s-master   <none>           <none>
kube-controller-manager-k8s-master   1/1     Running   1          11h   192.168.58.103   k8s-master   <none>           <none>
kube-flannel-ds-amd64-lkf2f          1/1     Running   0          10h   192.168.58.103   k8s-master   <none>           <none>
kube-proxy-przwf                     1/1     Running   1          11h   192.168.58.103  k8s-master   <none>           <none>
kube-scheduler-k8s-master            1/1     Running   1          11h   192.168.58.103  k8s-master   <none>           <none>
[centos@k8s-master ~]$
```



可以看到，所有的系统 Pod 都成功启动了，而刚刚部署的flannel网络插件则在 kube-system 下面新建了一个名叫kube-flannel-ds-amd64-lkf2f的 Pod，一般来说，这些 Pod 就是容器网络插件在每个节点上的控制组件。
Kubernetes 支持容器网络插件，使用的是一个名叫 CNI 的通用接口，它也是当前容器网络的事实标准，市面上的所有容器网络开源项目都可以通过 CNI 接入 Kubernetes，比如 Flannel、Calico、Canal、Romana 等等，它们的部署方式也都是类似的“一键部署”。
再次查看master节点状态已经为ready状态：

```
[centos@k8s-master ~]$ kubectl get nodes
NAME         STATUS   ROLES    AGE   VERSION
k8s-master   Ready    master   11h   v1.13.1
[centos@k8s-master ~]$
```



至此，Kubernetes 的 Master 节点就部署完成了。如果你只需要一个单节点的 Kubernetes，现在你就可以使用了。不过，在默认情况下，Kubernetes 的 Master 节点是不能运行用户 Pod 的。

## 3.部署worker节点

Kubernetes 的 Worker 节点跟 Master 节点几乎是相同的，它们运行着的都是一个 kubelet 组件。唯一的区别在于，在 kubeadm init 的过程中，kubelet 启动后，Master 节点上还会自动运行 kube-apiserver、kube-scheduler、kube-controller-manger 这三个系统 Pod。
在 k8s-node1 和 k8s-node2 上分别执行如下命令，将其注册到 Cluster 中：

```
#执行以下命令将节点接入集群
kubeadm join 192.168.58.103:6443 --token 67kq55.8hxoga556caxty7s --discovery-token-ca-cert-hash sha256:7d50e704bbfe69661e37c5f3ad13b1b88032b6b2b703ebd4899e259477b5be69

#如果执行kubeadm init时没有记录下加入集群的命令，可以通过以下命令重新创建
kubeadm token create --print-join-command
```



在k8s-node1上执行kubeadm join ：

```
[root@k8s-node1 ~]# kubeadm join 192.168.58.103:6443 --token 67kq55.8hxoga556caxty7s --discovery-token-ca-cert-hash sha256:7d50e704bbfe69661e37c5f3ad13b1b88032b6b2b703ebd4899e259477b5be69
[preflight] Running pre-flight checks
[discovery] Trying to connect to API Server "192.168.58.103:6443"
[discovery] Created cluster-info discovery client, requesting info from "https://192.168.58.103:6443"
[discovery] Requesting info from "https://192.168.58.103:6443" again to validate TLS against the pinned public key
[discovery] Cluster info signature and contents are valid and TLS certificate validates against pinned roots, will use API Server "192.168.58.103:6443"
[discovery] Successfully established connection with API Server "192.168.58.103:6443"
[join] Reading configuration from the cluster...
[join] FYI: You can look at this config file with 'kubectl -n kube-system get cm kubeadm-config -oyaml'
[kubelet] Downloading configuration for the kubelet from the "kubelet-config-1.13" ConfigMap in the kube-system namespace
[kubelet-start] Writing kubelet configuration to file "/var/lib/kubelet/config.yaml"
[kubelet-start] Writing kubelet environment file with flags to file "/var/lib/kubelet/kubeadm-flags.env"
[kubelet-start] Activating the kubelet service
[tlsbootstrap] Waiting for the kubelet to perform the TLS Bootstrap...
[patchnode] Uploading the CRI Socket information "/var/run/dockershim.sock" to the Node API object "k8s-node1" as an annotation

This node has joined the cluster:
* Certificate signing request was sent to apiserver and a response was received.
* The Kubelet was informed of the new secure connection details.

Run 'kubectl get nodes' on the master to see this node join the cluster.

[root@k8s-node1 ~]#
```



重复执行以上操作将k8s-node2也加进去（注意重新执行kubeadm token create –print-join-command）。
然后根据提示，我们可以通过 kubectl get nodes 查看节点的状态：

```
[centos@k8s-master ~]$ kubectl get nodes
NAME         STATUS   ROLES    AGE    VERSION
k8s-master   Ready    master   11h    v1.13.1
k8s-node1    Ready    <none>   24m    v1.13.1
k8s-node2    Ready    <none>   4m9s   v1.13.1
[centos@k8s-master ~]$
```



nodes状态全部为ready，由于每个节点都需要启动若干组件，如果node节点的状态是 NotReady，可以查看所有节点pod状态，确保所有pod成功拉取到镜像并处于running状态：

```
[centos@k8s-master ~]$ kubectl get pod --all-namespaces -o wide
NAMESPACE     NAME                                 READY   STATUS    RESTARTS   AGE     IP              NODE         NOMINATED NODE   READINESS GATES
kube-system   coredns-78d4cf999f-7jdx7             1/1     Running   0          11h     10.244.0.3      k8s-master   <none>           <none>
kube-system   coredns-78d4cf999f-s6mhk             1/1     Running   0          11h     10.244.0.2      k8s-master   <none>           <none>
kube-system   etcd-k8s-master                      1/1     Running   1          12h     192.168.58.103   k8s-master   <none>           <none>
kube-system   kube-apiserver-k8s-master            1/1     Running   1          12h     192.168.58.103  k8s-master   <none>           <none>
kube-system   kube-controller-manager-k8s-master   1/1     Running   1          12h     192.168.58.103   k8s-master   <none>           <none>
kube-system   kube-flannel-ds-amd64-d2r8p          1/1     Running   0          6m43s   192.168.58.102   k8s-node2    <none>           <none>
kube-system   kube-flannel-ds-amd64-d85c6          1/1     Running   0          27m     192.168.58.101   k8s-node1    <none>           <none>
kube-system   kube-flannel-ds-amd64-lkf2f          1/1     Running   0          11h     192.168.58.103  k8s-master   <none>           <none>
kube-system   kube-proxy-k8jx8                     1/1     Running   0          6m43s   192.168.58.102   k8s-node2    <none>           <none>
kube-system   kube-proxy-n95ck                     1/1     Running   0          27m     192.168.58.101  k8s-node1    <none>           <none>
kube-system   kube-proxy-przwf                     1/1     Running   1          12h     192.168.58.103   k8s-master   <none>           <none>
kube-system   kube-scheduler-k8s-master            1/1     Running   1          12h     192.168.58.103   k8s-master   <none>           <none>
[centos@k8s-master ~]$
```



这时，所有的节点都已经 Ready，Kubernetes Cluster 创建成功，一切准备就绪。
如果pod状态为Pending、ContainerCreating、ImagePullBackOff 都表明 Pod 没有就绪，Running 才是就绪状态。
如果有pod提示Init:ImagePullBackOff，说明这个pod的镜像在对应节点上拉取失败，我们可以通过 kubectl describe pod 查看 Pod 具体情况，以确认拉取失败的镜像：

```
[centos@k8s-master ~]$ kubectl describe pod kube-flannel-ds-amd64-d2r8p --namespace=kube-system
......
Events:
  Type     Reason     Age                 From                Message
  ----     ------     ----                ----                -------
  Normal   Scheduled  2m14s               default-scheduler   Successfully assigned kube-system/kube-flannel-ds-amd64-lzx5v to k8s-node2
  Warning  Failed     109s                kubelet, k8s-node2  Failed to pull image "quay.io/coreos/flannel:v0.10.0-amd64": rpc error: code = Unknown desc = Error response from daemon: Get https://quay.io/v2/: net/http: TLS handshake timeout
  Warning  Failed     109s                kubelet, k8s-node2  Error: ErrImagePull
  Normal   BackOff    108s                kubelet, k8s-node2  Back-off pulling image "quay.io/coreos/flannel:v0.10.0-amd64"
  Warning  Failed     108s                kubelet, k8s-node2  Error: ImagePullBackOff
  Normal   Pulling    94s (x2 over 2m6s)  kubelet, k8s-node2  pulling image "quay.io/coreos/flannel:v0.10.0-amd64"
```



这里看最后events输出内容，可以看到在下载 image 时失败，如果网络质量不好，这种情况是很常见的。我们可以耐心等待，因为 Kubernetes 会重试，我们也可以自己手工执行 docker pull 去下载这个镜像。

```
[root@k8s-node2 ~]# docker pull quay.io/coreos/flannel:v0.10.0-amd64
v0.10.0-amd64: Pulling from coreos/flannel
ff3a5c916c92: Already exists
8a8433d1d437: Already exists
306dc0ee491a: Already exists
856cbd0b7b9c: Already exists
af6d1e4decc6: Already exists
Digest: sha256:88f2b4d96fae34bfff3d46293f7f18d1f9f3ca026b4a4d288f28347fcb6580ac
Status: Image is up to date for quay.io/coreos/flannel:v0.10.0-amd64
[root@k8s-node2 ~]#
```



如果无法从 quay.io/coreos/flannel:v0.10.0-amd64 下载镜像，可以从阿里云或者dockerhub镜像仓库下载，然后改回原来的tag即可：

```
docker pull registry.cn-hangzhou.aliyuncs.com/kubernetes_containers/flannel:v0.10.0-amd64
docker tag registry.cn-hangzhou.aliyuncs.com/kubernetes_containers/flannel:v0.10.0-amd64 quay.io/coreos/flannel:v0.10.0-amd64
docker rmi registry.cn-hangzhou.aliyuncs.com/kubernetes_containers/flannel:v0.10.0-amd64
```



查看master节点下载了哪些镜像：

```
[centos@k8s-master ~]$ sudo docker images
REPOSITORY                                                        TAG                 IMAGE ID            CREATED             SIZE
registry.aliyuncs.com/google_containers/kube-proxy                v1.13.1             fdb321fd30a0        2 weeks ago         80.2MB
registry.aliyuncs.com/google_containers/kube-apiserver            v1.13.1             40a63db91ef8        2 weeks ago         181MB
registry.aliyuncs.com/google_containers/kube-scheduler            v1.13.1             ab81d7360408        2 weeks ago         79.6MB
registry.aliyuncs.com/google_containers/kube-controller-manager   v1.13.1             26e6f1db2a52        2 weeks ago         146MB
registry.aliyuncs.com/google_containers/coredns                   1.2.6               f59dcacceff4        8 weeks ago         40MB
registry.aliyuncs.com/google_containers/etcd                      3.2.24              3cab8e1b9802        3 months ago        220MB
quay.io/coreos/flannel                                            v0.10.0-amd64       f0fad859c909        11 months ago       44.6MB
registry.aliyuncs.com/google_containers/pause                     3.1                 da86e6ba6ca1        12 months ago       742kB
[centos@k8s-master ~]$
```



查看node节点下载了哪些镜像：

```
[root@k8s-node1 ~]# docker images
REPOSITORY                                           TAG                 IMAGE ID            CREATED             SIZE
registry.aliyuncs.com/google_containers/kube-proxy   v1.13.1             fdb321fd30a0        2 weeks ago         80.2MB
quay.io/coreos/flannel                               v0.10.0-amd64       f0fad859c909        11 months ago       44.6MB
registry.aliyuncs.com/google_containers/pause        3.1                 da86e6ba6ca1        12 months ago       742kB
[root@k8s-node1 ~]#
```



## 测试集群各个组件

**首先验证kube-apiserver, kube-controller-manager, kube-scheduler, pod network 是否正常：**
部署一个 Nginx Deployment，包含2个Pod
参考：<https://kubernetes.io/docs/concepts/workloads/controllers/deployment/>

```
[centos@k8s-master ~]$ kubectl create deployment nginx --image=nginx:alpine
deployment.apps/nginx created
[centos@k8s-master ~]$ kubectl scale deployment nginx --replicas=2
deployment.extensions/nginx scaled
[centos@k8s-master ~]$
```



验证Nginx Pod是否正确运行，并且会分配10.244.开头的集群IP

```
[centos@k8s-master ~]$ kubectl get pods -l app=nginx -o wide
NAME                     READY   STATUS    RESTARTS   AGE    IP           NODE        NOMINATED NODE   READINESS GATES
nginx-54458cd494-p2qgx   1/1     Running   0          111s   10.244.1.2   k8s-node1   <none>           <none>
nginx-54458cd494-sdlm7   1/1     Running   0          103s   10.244.2.2   k8s-node2   <none>           <none>
[centos@k8s-master ~]$
```



**再验证一下kube-proxy是否正常：**

以 NodePort 方式对外提供服务
参考：<https://kubernetes.io/docs/concepts/services-networking/connect-applications-service/>

```
[centos@k8s-master ~]$ kubectl expose deployment nginx --port=80 --type=NodePort
service/nginx exposed
[centos@k8s-master ~]$ kubectl get services nginx
NAME    TYPE       CLUSTER-IP    EXTERNAL-IP   PORT(S)        AGE
nginx   NodePort   10.108.17.2   <none>        80:30670/TCP   12s
[centos@k8s-master ~]$
```



可以通过任意 NodeIP:Port 在集群外部访问这个服务：

```
[centos@k8s-master ~]$ curl 192.168.58.103:30670
[centos@k8s-master ~]$ curl 192.168.58.102:30670
[centos@k8s-master ~]$ curl 192.168.58.101:30670
```



访问k8s-master ip

访问k8s-node1 ip

访问k8s-node2 ip

**最后验证一下dns, pod network是否正常：**
运行Busybox并进入交互模式

```
[centos@k8s-master ~]$ kubectl run -it curl --image=radial/busyboxplus:curl
kubectl run --generator=deployment/apps.v1 is DEPRECATED and will be removed in a future version. Use kubectl run --generator=run-pod/v1 or kubectl create instead.
If you don't see a command prompt, try pressing enter.
[ root@curl-66959f6557-s5qqs:/ ]$
```



输入`nslookup nginx`查看是否可以正确解析出集群内的IP，以验证DNS是否正常

```
[ root@curl-66959f6557-s5qqs:/ ]$ nslookup nginx
Server:    10.96.0.10
Address 1: 10.96.0.10 kube-dns.kube-system.svc.cluster.local

Name:      nginx
Address 1: 10.108.17.2 nginx.default.svc.cluster.local
```



通过服务名进行访问，验证kube-proxy是否正常

```
[ root@curl-66959f6557-q472z:/ ]$ curl http://nginx/
<!DOCTYPE html>
<html>
<head>
<title>Welcome to nginx!</title>
......
</body>
</html>
[ root@curl-66959f6557-q472z:/ ]$
```



分别访问一下2个Pod的内网IP，验证跨Node的网络通信是否正常

```
[ root@curl-66959f6557-s5qqs:/ ]$ curl 10.244.1.2
<!DOCTYPE html>
<html>
<head>
<title>Welcome to nginx!</title>
......
</body>
</html>
[ root@curl-66959f6557-s5qqs:/ ]$ curl 10.244.2.2
<!DOCTYPE html>
<html>
<head>
<title>Welcome to nginx!</title>
......
</body>
</html>
[ root@curl-66959f6557-s5qqs:/ ]$
```



## Pod调度到Master节点

出于安全考虑，默认配置下Kubernetes不会将Pod调度到Master节点。查看Taints字段默认配置：

```
[centos@k8s-master ~]$ kubectl describe node k8s-master
......
Taints:             node-role.kubernetes.io/master:NoSchedule
```



如果希望将k8s-master也当作Node节点使用，可以执行如下命令,其中k8s-master是主机节点hostname：

```
kubectl taint node k8s-master node-role.kubernetes.io/master-
```



修改后Taints字段状态：

```
[centos@k8s-master ~]$ kubectl describe node k8s-master                             
......
Taints:             <none>
```



如果要恢复Master Only状态，执行如下命令：

```
kubectl taint node k8s-master node-role.kubernetes.io/master=""
```



## kube-proxy开启ipvs

修改ConfigMap的kube-system/kube-proxy中的config.conf，mode: “ipvs”：

```
[centos@k8s-master ~]$ kubectl edit cm kube-proxy -n kube-system
configmap/kube-proxy edited
```



之后重启各个节点上的kube-proxy pod：

```
[centos@k8s-master ~]$ kubectl get pod -n kube-system | grep kube-proxy | awk '{system("kubectl delete pod "$1" -n kube-system")}'
pod "kube-proxy-2w9sh" deleted
pod "kube-proxy-gw4lx" deleted
pod "kube-proxy-thv4c" deleted
[centos@k8s-master ~]$ kubectl get pod -n kube-system | grep kube-proxy
kube-proxy-6qlgv                        1/1     Running   0          65s
kube-proxy-fdtjd                        1/1     Running   0          47s
kube-proxy-m8zkx                        1/1     Running   0          52s
[centos@k8s-master ~]$
```



查看日志：

```
[centos@k8s-master ~]$ kubectl logs kube-proxy-6qlgv -n kube-system
I1213 09:50:15.414493       1 server_others.go:189] Using ipvs Proxier.
W1213 09:50:15.414908       1 proxier.go:365] IPVS scheduler not specified, use rr by default
I1213 09:50:15.415021       1 server_others.go:216] Tearing down inactive rules.
I1213 09:50:15.461658       1 server.go:464] Version: v1.13.0
I1213 09:50:15.467827       1 conntrack.go:52] Setting nf_conntrack_max to 131072
I1213 09:50:15.467997       1 config.go:202] Starting service config controller
I1213 09:50:15.468010       1 controller_utils.go:1027] Waiting for caches to sync for service config controller
I1213 09:50:15.468092       1 config.go:102] Starting endpoints config controller
I1213 09:50:15.468100       1 controller_utils.go:1027] Waiting for caches to sync for endpoints config controller
I1213 09:50:15.568766       1 controller_utils.go:1034] Caches are synced for endpoints config controller
I1213 09:50:15.568950       1 controller_utils.go:1034] Caches are synced for service config controller
[centos@k8s-master ~]$
```



日志中打印出了Using ipvs Proxier，说明ipvs模式已经开启。
[学习](https://blog.csdn.net/networken/article/details/84991940)
