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

里面有个level数组存连接，有32个数组对象

### 超时时间

expire key seconds

ttl key  查看还有多少秒

设置持久化 persist key 它的过期时间失效。ttl key就等于-1.如果超时了ttl key就被删除了得到-2

原理：

消极删除：使用的时候在删除

积极删除：周期性的从设置了过期时间的key中找到key进行删除

### 发布订阅

publish channel.sx hello 发布消息

subscribe channel 订阅消息

redis不支持消息持久化，回滚等操作，还是需要专业的消息中间件来处理

### Redis持久化及原理

持久化是有代价的

RDB：触发快照规则，fork子进程，耗时间，生成一个dump.rdb快照文件

1.配置规则

save seconds changes，，以下是或者的关系只要满足一个条件就触发

save 900  1   :900s内有个key发生改变，触发生成一个快照

save 300   10   ：在300s之内有10个key发生改变，触发一个快照

save 60  10000   ：在60s之内有10000个key发生改变触发一个快照

2.主动调用持久化命令，通过save或者，会阻塞所有客户端的请求
bgsave：后台异步的持久化，不会阻塞客户端的请求

3.flushall,刷新操作

4.执行复制操作


两次快照触发直接有个时间，可能会引起数据丢失

AOF：实时日志

需要设置一个参数变更的事务就会写，两次写，写到持久化文件，写到内存

appendonly yes

appendonly filename可以修改名字，，xx.aof


appendaof,完整的操作日志的管理操作，记录说有的操作

随着操作的进行，不断的添加aof

当然

auto-aof-rewrite-percentage 100,

auto-aof-rewrite-min-size 64 mb

触发重写，fork子进程，重写从内存中重写一个新的aof，这样就可以比aof日志操作缩减了大量修改操作

重写缓存的追加到aof的文件后面，保证数据不丢失

重写的目的，精简了aof文件的目的，压缩过程  

appendsync everysec,每一秒同步的磁盘上


数据要求到的话，同时使用两种，aof和rdb文件放到redis目录下会自动加载，就是数据恢复

#### redis的内存回收策略

内存操作的中间件,为了保证内存合理使用

数据淘汰策略

LRU

maxmemory-policy noeviction：内存块到的时候就会报错

allkeys-lru:（最少使用）

volatile-random/lru/ttl(即将过期的)

采样的方式

#### Redis单线程为什么性能很高

瓶颈是在于内存和网络，而不是cpu

多路复用技术

 lua脚本

 可以保证操作的原子性，复用，减少网络开销执行多个命令

 可以使用script load命令将lua脚本加载到redis当中，生成一个key,减少脚本的传输造成的网络性能问题，

 然后调用该脚本key

 evalsha key 参数串就好了

 执行redis脚本，其他的应用连接会阻塞

 script kill

 是否能执行script kill还要看它脚本中是否有修改操作已经执行，如果执行那么该命令就不会成功

 需要调用shutdown nosave命令,该命令会终止redis的服务器，需要重启才能运行，结果发现没有变，有效的保证原子性，

 lua保证原子性就要付出一定的代价

## 集群

主从

slaveof 配置从服务器

slaveof masterip masterport

然后在主服务器上使用info replication,从服务器上查看info replication可以查看详细主从信息

还需要配置主服务器上的bind 127.0.0.1给注释掉

其他的调用该端口， master_link_status:up

- 数据同步

原理

全量复制
：初始化的时候，就是全量复制   sync - bgsave - load

增量复制
:使用命令

min-salave-to-write 3

min-slave-to-


无磁盘复制
:不生成快照，因为影响性能

repl-diskless-sync no

replconf  listening-port 6371,监听端口的事件

主服务器发送过来命令执行

选主：

使用哨兵进行选主

当主的挂了，会从从服务区选一个主

哨兵，监控master和slave运行状态，选主

sentinel哨兵：

哨兵的选举主哨兵，发送绝对谁是主，涉及到raft协议

thesecretlivesofdata.com/raft/

redis-cluster/codis/twemproxy

redis分片扩容，需要一致性hash,有一个工具是codis帮我们做了所有相关扩容的事情（twmproxy）

 redis-codis支持分片，扩容，动态扩容

 支持高可用的集群需要3个节点，那为了高可用使用6台，3个主，3个从，gossip协议个无中心化节点集群

 虚拟操的概念0~16833分别放在不同的分片中

 crc16%16838=2000分配到某个分片

 相应的key落到那个片的机器上呢？

 hashTag

 key，如果是 user:{user1}:id

            user:{user1}:id

加了{}后，redis-cluster就会根据{}中的值进行hash,这个标志就是hashtag,那这个hashTag相同了，保证了会落到同一个分配空间内    


  get move进行了重定向了的ip,再从它下找

  分片迁移

  为了解决slot的分配：

  扩容的话，分别从各个分片上找一些数据，拷贝到新的分片上

  ，热扩展，正在迁移状态

  如果客户端访问的key还没有迁移，则正常处理key

  已经迁移或者不存在，恢复ask信息跳转的masterb进行


  当客户端不是从ask跳转的时候，不允许修改，发送moved命令


总之，确保迁移之前在源节点上进行，迁移之后从目标节点进行

主从 、 哨兵 、 cluster

## redis应用

- jedis

连接哨兵模式：

JedisSentinelPool

连接集群模式：

JedisCluster

- Redission

Config configure

config.useClusterServers().addNodeAddress("redis:")

Rediss

- 分布式锁

redis setnx


Redission 版本的分布式锁，实现比jedis方便多了

Rdission优势

jedis的pipeline,减少网络通信，一次性发送多个命令，减少网络连接数和通信数


使用redis无法保证redis和数据库的数据是强一致性的，没用，也没必要，加缓存是为了提高性能，如果改了数据库还要写缓存，性能更低了

业务问题

只要最终一致性

更新缓存还是让缓存失效

先操作数据库还是先操作缓存

先操作数据库还是先操作缓存（哪一种对业务的影响更小）

1，更新数据库，2，让缓存失效


如果让缓存失效失败了，把任务添加的activemq中，app再获取任务重试，这就是达到最终一致性的方案

缓存雪崩

：同一个时刻大量的key失效，全部落到数据库中。从缓存中取不到值的时候就枷锁

缓存过期时间的分布分析形

多级缓存的实现



缓存穿透

：很多key访问的时候都不存在，都到了数据库中

数据库压力

1.对空值进行缓存，放特殊值

2，对key的规则，阻挡直接调用数据库

3.布隆过滤器

算法：

client - bloom - redis- mysql

空间效率非常高的概率型算法

存储40亿数据，一个数据4比特，就是16G内存

压缩内存、

bitmap

对一个key进行n次的hash，标记所有n位的第几个标位1，

BloomFilter bloomFilter=BloomFilter.create(Funnels.stringFunnel(Charset.defaultcharste())0.00001);

blootFilter.map



redission中也有bloom
























JedisCluster
