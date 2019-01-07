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












## 系统准备

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
