---
layout: post
title:  安装k8s
date:   2019-11-17 20:52:12 +08:00
category: k8s
tags: k8s
comments: true
---

* content
{:toc}


keys!






## 卸载：

　　1、查询docker安装过的包：

　　　　yum list installed | grep docker

      　

　　2、删除安装包：

　　　yum remove docker-ce.x86_64 ddocker-ce-cli.x86_64 -y

　　3、删除镜像/容器等

　　　　rm -rf /var/lib/docker

可以查看所有仓库中所有docker版本，并选择特定版本安装：yum list docker-ce --showduplicates | sort -r

 yum install docker-ce-18.06.3.ce
