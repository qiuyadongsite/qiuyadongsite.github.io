---
layout: post
title:  分布式缓存技术Redis介绍和使用
date:   2019-03-28 21:52:12 +08:00
category: 高并发分布式
tags: Redis
comments: true
---

* content
{:toc}

缓存大致可以分为两类，一种是应用内缓存，比如Map(简单的数据结构)，以及EH Cache(Java第三方库)，另一种就是缓存组件，比如Memached，Redis；Redis（remote dictionary server）是一个基于KEY-VALUE的高性能的存储系统，通过提供多种键值数据类型来适应不同场景下的缓存与存储需求












## 概念

字典类型的数据结构，比如map ，通过key value的方式存储的结构。

redis的全称是remotedictionary server(远程字典服务器)，它以字典结构存储数据，并允许其他应用通过TCP协议读写字典中的内容。数据结构如下

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/redis1.png)

Redis的安装

redis约定次版本号（第一个小数点后的数字）为偶数版本是稳定版，如2.8、3.0， 奇数版本为非稳定版，生产环境需要使用稳定版；

目前最新版本为Redis4.0.9

安装配置

1 下载redis的安装包

2 tar -zxvf 解压

3 cd 到解压后的目录

4 执行make 完成编译

可能会遇到的错误,根据提示安装其他依赖

```

1. 需要安装tcl yum install tcl 、 yum install gcc

2. error: jemalloc/jemalloc.h: No such file or directory
说关于分配器allocator， 如果有MALLOC 这个 环境变量， 会有用这个环境变量的 去建立Redis。
而且libc 并不是默认的 分配器， 默认的是 jemalloc, 因为 jemalloc 被证明 有更少的 fragmentation
problems 比libc。

但是如果你又没有jemalloc 而只有 libc 当然 make 出错。 所以加这么一个参数。
解决办法
make MALLOC=libc

```

5 make test 测试编译状态

6 make install {PREFIX=/path}

### 启动停止redis

| 名称     | 作用   |
| :------------- | :------------- |
| IRedis-server  | Redis服务器     |
| Redis-cli |  Redis命令行客户端     |
| Redis-benchmark  |  Redis性能测试工具     |
| Redis-check-aof  |  Aof文件修复工具     |
| Redis-check-dump  |  Rdb文件检查工具     |
| Redis-sentinel |  Sentinel服务器（2.8以后）    |


###  我们常用的命令是redis-server和redis-cli

1 直接启动

redis-server ../redis.conf

服务器启动后默认使用的是6379的端口 ，通过--port可以自定义端口 ； 6379在手机键盘上MERZ对应，MERZ是一名意大利歌女的名字

Redis-server --port 6380

以守护进程的方式启动，需要修改redis.conf配置文件中daemonize yes

2 停止redis

redis-cli SHUTDOWN

考虑到redis有可能正在将内存的数据同步到硬盘中，强行终止redis进程可能会导致数据丢失，正确停止redis的方式应该是向Redis发送SHUTDOW命令

当redis收到SHUTDOWN命令后，会先断开所有客户端连接，然后根据配置执行持久化，最终完成退出

### 数据类型

字符串类型

字符串类型是redis中最基本的数据类型，它能存储任何形式的字符串，包括二进制数据。你可以用它存储用户的邮箱、json化的对象甚至是图片。一个字符类型键允许存储的最大容量是512M

```

内部数据结构
在Redis内部，String类型通过 int、SDS(simple dynamic string)作为结构存储，int用来存放整型数据，sds存放字节/字符串和浮点型数据。在C的标准字符串结构下进行了封装，用来提升基本操作的性能，同时也充分利用已有的C的标准库，简化实现逻辑。


```

待续。。。
