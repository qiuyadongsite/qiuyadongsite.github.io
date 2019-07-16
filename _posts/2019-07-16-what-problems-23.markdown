---
layout: post
title:  redis
date:   2019-07-16 20:52:12 +08:00
category: 归零
tags: 性能优化
comments: true
---

* content
{:toc}


redis!











## 安装

磨刀不误砍柴工！

1.安装xshell和xftp

官网地址：（选择家庭学生版）

https://www.netsarang.com/download/down_form.html?code=522

填上邮箱地址，到时候发送给下载地址；安装 so easy!

2.安装redis5.0.5

上官网：redis.io,下载后拷贝到centos的安装目录下

tar -zxvf redis5.0.5.tar.gz

进入目录下，make一下

再make test报错：You need tcl 8.5 or newer in order to run the Redis test.

下载之后configure、make && make install一条龙就好了：

$ wget http://downloads.sourceforge.net/tcl/tcl8.6.9-src.tar.gz
$ tar xzvf tcl8.6.9-src.tar.gz  -C /usr/local/
$ cd /usr/local/tcl8.6.9/unix/
$ ./configure
$ make && make install

$ cd /redis-5.0.5/deps
$ make hiredis lua jemalloc linenoise

然后再make test，这次就能通过test了。然后make && make install，出现一排install就安装完了：

然后再进行一些简单的配置，主要是修改Redis根目录下的redis.conf，找到守护进程并打开。守护进程的作用主要就是防止在后台被kill掉，不然每次都要nohup，多麻烦：

$ vim /usr/local/redis-5.0.5/redis.conf
$ daemonize yes     # 打开守护进程

然后就可以启动看一下效果了，完成一套增和查，顺便删库（不会删库怎么行）。

$ /usr/local/redis-5.0.5/src/redis-server /usr/local/redis-5.0.5/redis.conf
$ redis-cli
127.0.0.1:6379> set foo bar
OK
127.0.0.1:6379> get foo
"bar"
127.0.0.1:6379> flushall
OK

## 数据结构

操作对应表

http://redisdoc.com/index.html

1，String

字符串，整数，浮点

内部存储的类型：int和sds(char \* sds)(二进制安全的结构意识是不能有空格\0 这个就是结束标识，不能存图片视频，只能存字符串)

00 sdshdr5 10 sdshdr8 11 sdshdr16

原子曾

2，list

双向链表，

3.2之前linkedlist和 ziplist
3.2之后使用quicklist，特定ziplist和linkedlist结合，由ziplist组成的双向链表

使用场景：分布式队列，生产者lpush,消费者brpop.栈：lpush lpop  队列：lpush rpop

3,hash

数据量小的时候使用ziplist
ziplist/hash
二位表格，

存储对象

4,set

无序不重复

数据结构：intset hashtable(key，null)

使用场景：打标签，共同好友，diff做个差集

5,sorted-set

有序集合，score分数，zadd,加个搜索排名

zscore获取分数

zrange key 0 3 withscore

zrevage key 0 ,-1 withscore倒序

数据结构

ziplist 和skiplist+ hashtable

里面有个level
