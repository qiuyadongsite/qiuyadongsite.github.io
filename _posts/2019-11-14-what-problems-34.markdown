---
layout: post
title:  docker入门
date:   2019-11-14 20:52:12 +08:00
category: docker
tags: docker
comments: true
---

* content
{:toc}


keys!






## 为啥需要docker

第一个阶段：

开发一个项目，并部署的话，需要基础设施（电脑必要的硬件）、操作系统+必须的软件环境、应用。

问题：成本高、部署慢、浪费资源、硬件限制、不利于迁移扩展等

第二个阶段：

引入虚拟化技术，基础设施（电脑必要的硬件）、操作系统、虚拟化技术（vmware/virtualbox）、虚拟机（操作系统）、必须的软件环境、应用

相对利用好了资源、相对的扩展变得容易

问题：虚拟机比较重量级，一上来就申请了资源，资源利用率低；

第三个阶段：

容器时代，基础设施、操作系统、docker、容器+应用

好处：充分的利用资源，易于扩展和部署，易于迁移

## docker概念

- images

镜像，满足相应应用运行的所需要的环境，类似于类

- container

容器，具体的执行单元，就是一个应用的实例，类似于对象

- 容器与虚拟机

容器之间依赖于共同的一个主机的内核，而虚拟机申请的资源更多，远远多于一个应用所需要的资源。

- docker引擎

cs结构，客户通过docker client（images,container）通过docker restful api 连接docker daemon(后台进程)

## docker初步实践

安装docker

配置我的阿里镜像加速器

```

sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://wrji6r5y.mirror.aliyuncs.com"]
}
EOF
sudo systemctl daemon-reload
sudo systemctl restart docker

//也可以修改daemon配置文件/etc/docker/daemon.json来使用加速器

```

常用命令

```

docker pull        拉取镜像到本地
docker run         根据某个镜像创建容器
-d                 让容器在后台运行，其实就是一个进程
--name             给容器指定一个名字
-p                 将容器的端口映射到宿主机的端口
docker exec -it    进入到某个容器中并交互式运行

```

发布tomcat和mysql服务

```

docker pull tomcat
docker run -d --name my-tomcat -p 9090:8080 tomcat

docker run -d --name my-mysql -p 3301:3306 -e MYSQL_ROOT_PASSWORD=root --privileged mysql:5.7

docker exec -it containerid /bin/bash

退出exit

docker 拉取地址hub.docker.com

```


## dockerfile

docker镜像其实就是一层一层在基础镜像的叠加的，如centosimages;

在https://github.com/docker-library查看所有的dockerfile文件：比如tomcat的dockerFile文件地址

https://github.com/docker-library/tomcat/blob/master/8.5/jdk8/openjdk/Dockerfile

- FROM 指定基础镜像FROM openjdk:8-jdk
- RUN 执行命令
- ENV 设置变量的值 ENV TOMCAT_VERSION 8.5.47 也可以通过 -e key=val
- LABEL 设置镜像的标签
- VOLUME 指定数据挂的目录
- copy 将主机的文件复制到镜像内
- ADD 将主机的文件复制到镜像内,会对压缩文件进行提取和解压
- WORKDIR 指定镜像的工作目录，之后的命令都是在这里执行
- CMD 容器启动的时候默认执行的命令，最后一个生效
- ENTRYPOINT 跟CMD类似，但是当docker run执行时会覆盖cmd，而它不会被覆盖
- EXPOSE 指定镜像要暴露的端口，启动镜像时，可以使用-p将该端口映射给宿主主机

制作自己的dockerfile

```

FROM openjdk:8
MAINTAINER qiuyd
LABEL name="file-system" version="1.0" author="qiuyd"
copy fast-simple-file-system-0.0.1-SNAPSHOT.jar file-system-image.jar
CMD ["java","-jar","file-system-image.jar"]
docker run -d --name my-filesystem -p 7000:7003 qiuyd-file-image

并将其推送到阿里镜像服务器上，并且拉取使用

docker pull registry.cn-beijing.aliyuncs.com/qiuyd/qiuydsite:laster

```

查看docker的资源使用情况：docker stats

如果不加限制，将无限制的使用物理机的资源

- 内存限制

docker run -d --memory 100M --name tomcat1 tomcat

- cpu限制

docker run -d --cpu-shares 10 --name tomcat2 tomcat

Container是一种轻量级的虚拟技术，不用模拟硬件创建虚拟机

docker是基于Linux Kernel的Namespace、CGroups、UnionFileSystem等技术封装成一种自定义的容器格式，从而提供一套虚拟运行环境。

Namespace用来隔离：pid（进程）、net(网络)、mnt(挂载点)

CGroups用来隔离：资源的限制如cpu,内存

UnionFileSystem：用来做images和container分层。

- 卸载与安装docker

yum list installed | grep docker

yum remove docker-ce.x86_64 ddocker-ce-cli.x86_64 -y

rm -rf /var/lib/docker

yum list docker-ce --showduplicates | sort -r

yum install docker-ce-17.12.0.ce

启动、设置开启开机启动

sudo systemctl start docker

sudo systemctl enable docker
