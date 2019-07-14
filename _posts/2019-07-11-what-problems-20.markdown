---
layout: post
title:  JVM3
date:   2019-07-11 20:52:12 +08:00
category: 归零
tags: 性能优化
comments: true
---

* content
{:toc}


JVM!











## 性能调优

eclipse Memory analyzer //dump文件查看工具

看树状图

看引用关系

## 字节码和数据结构

## 类加载机制

Clinit

init

解析的作用就是将符号引用转换成直接引用



top


top -H

jstack看线程那个高

## mysql调优

CPU密集型-科学计算，主频数

高并发-多核

内存（容量，主频），主板最大的内存

io子系统，顺序读写、随机读写
1，机械硬盘
2，Raid(多个盘)
3,ssD（）

网络：带宽

load average平均负载作用

占cpu占内存，free -m命令看

服务系统：centos7

数据库存储引擎

数据库参数配置

centos7

net.core.somaxconn=65535 监听队列的最大长度，accept   sync:accept队列同步队列大小

net.core.netdev_max_backlog=3000,内核处理网络包，

innodb_file_per_table

redolog
undolog
