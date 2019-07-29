---
layout: post
title:  redis汇总
date:   2019-07-21 20:52:12 +08:00
category: 归零
tags: 性能优化
comments: true
---

* content
{:toc}


redis汇总!











## 概述

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/redis0001.png)

Redis是一个远程内存数据库，非关系型数据库，性能强劲，5种不同的数据结构映射到不同的用户需求，可以通过复制、持久化和客户端分片等特性，帮助用户扩展包含数百GB数据和每秒上百万次的请求系统。

## 数据结构及应用

它是非关系型数据库NOSQL数据库，不使用表的结构存储关系，不使用表，每一个key都是独立不与其他key强制关联。memcached只能存储普通的字符串，所以reids应用更为广泛。

### 数据结构

- 字符串

可以存储：字符串、整数或者浮点，对字符串或者对字符串的一部分执行操作，对整型或者浮点执行自增或者自减操作

GET/SET/DEL操作，可以用它存储用户的邮箱、json化的对象甚至是图片。一个字符类型键允许存储的最大容量是512M

- 列表

一个列表可以有序的存储多个字符串。

LPUSH/RPUSH命令插入、LPOP/RPOP命令弹出元素、LINDEX获取给定位置的元素，LRANGE用于获取列表中给定范围上的元素；

- 集合

SADD添加、SREM移除，SISMEMBER可以检查一个元素是否在集合中，Smembers key 列出所有元素，还可以对两个集合进行集合运算Sinter/Sunion/Sdiff

- 散列

hset hash-key sub-key1 v1   向hashkey中添加一个子key value ;hgetall hash-key 获取所有;hdel has-key key1 删除一个key;hget hash-key subkey,获取一个子key

- 有序集合

zadd zset-key 728 member1 添加一个带score的key，这个score用于排序； zrange zset-key 0 -1 withscores,获取所有带score的值

zrem key subkey 移除一个value

### 数据类型的应用

一个文章应用系统

- 对文章进行投票

Unix时间（从1970.1.1到现在的时间秒数）

如果一篇文章得到至少200张支持票，就说明他是有趣的文章，那么一天中，就需要进行对文章的票数进行排序，最有趣的文章。

一天的秒数是86400秒，如果支持票数是200，则86400/200=432, 那么文章每获取一张支持票，程序就给该文章的评分+432分。

分数知道后，就利用数据结构存储文章，使用一个散列存储文章的标题，指向文章的网址，发布者，发布时间，文章的评论数，文章的分数等，其实就是一个对象啦

如使用article:文章ID,作为该key

文章的投票网站使用两个有序集合存储文章。第一个有序集合存储文章ID,分值存文章的发布时间；第二个有序集合成员同样存储文章ID,分值为文章的评分。

这样网站可以通过两种方式来存储文章，发布时间或者评分。

为了防止用户对同一篇文章进行多次投票，网站需要为每篇文章记录一个已投票的用户名单，voted:文章ID,其中存储用户的ID

为了尽量节约内存，我们约定发布一周后，用户将不能再对文章进行投票，文章的评分固定了，记录文章的投票的用户也会被删除

当文章进行投票的时候，1，通过ZScore命令检查记录文章的有序集合，看看文章是否再时间范围之内，Sadd给文章添加用户投票，使用ZINCRBY命令自增文章的评分。HINCRBY对散列的文章的投票的数量更新

- 发布并获取文章

首先创建一个新的文章ID,可以通过执行一个计数器执行INCR命令来完成

接着给该SADD一个文章ID的投票用户的集合添加发布者的用户，设置Expire这个集合的过期时间是一周，一周以后自动删除该集合。之后使用HMSET命令存储该文章相关信心，并使用ZADD命令添加文章的初试分数和发布时间的有序集合

之后就可以使用Zrevrange（分数是从大到小的）取出多个文章ID,再通过每个文章的ID执行HGETALL命令获取文章的详细信息。

- 对文章进行分组

文章肯定是需要分类的，

zinterstore命令可以接受多个集合和多个有序集合作为输入，找到同时存在与集合和有序集合的成员。

如对groups:programming 和有序集合 score:进行交集，计算出新的有序集合，score:programming;

以上只是简单运用

### 使用redis构建web应用

web应用：就是通过HTTP协议对王爷浏览器发送的请求进行响应的服务器。具体步骤：1，服务器对客户端发送过来的请求（request）进行解析;2,请求被转发给一个预定义的处理器（handler）,3,处理器可能从数据库中取出数据，4，处理器根据取出的数据对模板进行渲染，5，处理器想客户端返回渲染后的内存作为对请求的响应；

那么何时使用redis呢？

Redis查询替换传统的关系型数据库查询，来完成关系型数据库没办法高效完成的任务。

场景：一个商店大约有500万的不用用户，带来的点击1个亿，并购买超过10万件的商品。那么使用几GB内存的redis服务器怎么使用呢？

- 使用redis管理用户会话session

对于无状态的HTTP协议，使用cookie是很好的解决登陆验证问题。有两种创建的方法可以将登陆信息存储到cookie中：一种是签名cookie、另一种是令牌（token）cookie;

签名cookie:存储用户名，用户id最后一次登陆时间和觉得网站有用的信息。

令牌cookie:会在cookie里存储遗传随机字节作为令牌，令牌存储在数据库中跟用户id对应。

令牌cookie的优点是体积小，这里使用令牌cookie

用户浏览数据以及最后一次访问页面的时间等信息，通常会导致大量的数据库写入，大多数关系数据库在每台数据库服务器上每秒只能插入、更新或者删除200~2000个数据库行。

如果每秒1200次写入，高峰期每秒接近6000次写入，所以要部署10台关系型数据库处理高峰期的负载量。

redis在这个时候来实现登陆cookie功能，取代关系型数据库实现的登陆cookie功能。

1，用户每次浏览页面，程序对用户存储的登陆散列里面的信息进行更新，并将用户的令牌和当前的时间戳添加到记录最近登陆用户的有序集合里面；

2，如果用户正在浏览的是一个商品页面，那么程序将商品添加到这个用户浏览的商品有序集合里面，被记录的商品数量超过25个时候进行修剪。

存储会后数据所需要的内存会随着时间不断在增加，所以需要我们定期清理旧的会话数据。为了限制会话数量，只保存1000万个会话。清理就会好。

一个线程专门来清理超过会话数量后，从登陆令牌的有序集合中移除最多100个最旧的令牌，并将数据库中旧的令牌用户对应删除，也要根据具体访问量进行动态修改

- 使用Redis实现购物车

老系统中有一种将购物车存储在cookie中的方案。优点是无须对数据库进行写入就可以实现购物车功能，缺点是程序需要重新解析和验证cookie;

既然cookie都可以存redis中，每个用户的购物车都是一个散列，这个散列存储了商品ID和商品订购数量的映射。同时可以对这些修改进行覆盖和删除

- 网页缓存

在动态生成网页的时候，通常使用模板语言来简化网页的生成操作；现在的web页面通常包括首部、尾部、侧栏菜单、工具条、内容域的模板生成，有时候模板还可以生成javascript;现在的动态生成页面已经成为主流，手写每个页面一去不复返了！

但是，动态生成内容大大降低了网站的性能，和浪费时间。缓存哪些不长改变的页面。

- 数据行缓存

- 网页分析

从用户的访问、交互和购买行为中收集有价值的信息。

## 常用命令

- 字符串操作

INCR/DECR +1 -1

INCRBY/DECRBY/INCRBYFLOAT key amount + - amount  

APPEND key value 在当前存储值得末尾添加值

GETRANGE key start end 获取start-end范围内所有字符组成的字串

SETRange key offset value 从offset开始设置的值

GetBit key offset 返回字符串中的偏移量

setbit key offset value

bitcount key start end 统计数量

- 列表

RPUSH LPUSH RPOP LPOP LiNDEX LRange Ltrim(对列表进行修剪)

- 集合

SADD SREM SISMEMBER SCARD SMEMBERS SRANGEMEMBER SPOP SMOVE

SDIFF SDIFFSTORE SINTER SINTERSTORE SUNION SUNIONSTROE

- 散列

HMGET HMSET HDEL HLEN HEXITSTS HKEYS HVALS HGETALL HINCRBY HINCRBYFLOAT

- 有序集合

ZADD ZREM ZCARD ZINCRBY ZCOUNT ZRANK ZSCORE zrange

ZREVRANK ZREVRANGE ZRANGEBYSCORE

- 发布和订阅

SUBSCRIBE UNSUBSCRIBE PUBlish PSUBSCRIBE PUNSUBSCRIBE

- 其他命令

sort:排序

MULTI/EXEC：事务

键的过期时间：persist/ttl、expire/expireat、pttl、pexpire

## 数据安全与性能保障

### 持久化

将数据存储到硬盘里面。一种叫快照，可以将存在于某一时刻的所有数据写入到硬盘里面。另一种叫只追加文件（aof）,它在执行写命令时，将被执行的写命令复制到硬盘里面。

快照选项

save 60 1000

stop-writes-on-bgsave-error no

rdbcompression yes

dbfilename dump.rdb

aof选项

appendonly no

appendsync everysec

no-appendfsync-on-rewrite no

auto-aof-rewrite-percentage 100

auto-aof-rewrite-min-size 64mb

dir 快照文件和aof文件的保存位置

- 快照持久化

Redis通过创建快照来获取存储在内存里面的数据在某个时间点上的副本。快照文件可以用来恢复数据使用，直接拷贝dir配置的路径下即可。在新的快照创建完毕以前，redis/系统、硬件任意一个崩溃就会丢失最近一次创建快照之后的所有数据。

创建快照方法

1.bgsave命令，redis会fork来创建一个子进程来负责快照写入硬盘，而父进程继续处理命令。
2.save命令，创建快照完成前redis不会再接受到命令，不常用。
3.配置了save配置选项，如save 60 10000,60秒之内10000次写入，任意一个配置满足就触发一次bgsave命令。
4.redis通过shutdown或者接受到标准的Trem信号时会执行一个save命令。然后关闭系统。
5.一个redis服务器连接到另一个redis,并发送sync命令来开始一次复制操作，主服务器如果没有bgave命令或者最近没有bgsave，就触发一次bgsave。

使用快照的场景

1.个人开发

主要降低快照持久化带来的资源消耗。只配置一个save,save 900 1。15分钟内至少一次写入操作，就触发一次bgsave操作。不要过于频繁和过于稀少。

2。对日志进行聚合计算

对日志文件进行聚合计算和页面浏览量进行分析的时候。对丢失日志的容忍度设置相关值，save 3600 1,丢1小时的数据可以容忍，可以增加相关业务，对收集的具体情况进行记录，方便数据恢复之后再进行相关处理。

3.大数据

数据量在几个G的时候用快照是没问题的。当到了十几个G时候剩余内存也不多了，使用bgsave,fork的子进程会导致系统长时间的停顿，导致性能降低或者不可用。

这种情况下，为了防止创建子进程的停顿，需要关闭自动保存，转为手动发送bgsave/save命令。

对于可以容忍大量丢失数据的业务可以使用快照，但是如果不接受15分钟之内的数据丢失，还是使用aof吧。

- AOF持久化

AOF持久化会将被执行的写命令写到AOF的末尾，以便记录数据发生的变化。这样，重新执行一次AOF就可以恢复所记录的数据集。

 appendfsync选项配置同步频率

always ：每个Redis写命令都要同步写入硬盘，严重降低了redis的速度。

它会造成的丢失最少，但是需要对硬盘进行大量的写入，收到了硬盘性能的限制，转盘式硬盘每秒只能处理200个写命令，而固态硬盘ssd每秒几万的写命令。频繁写也会毁坏ssd硬盘。

everysec： 每秒执行一次同步

为了兼顾数据安全和写入性能的选择。

no： 让操作系统绝对何时去同步

问题是不确定系统崩溃了到底丢了多少的数据。

AOF的缺陷是AOF的文件太大了

- 重写、压缩AOF文件

随着操作的不断进行AOF的文件会很大。用户可以使用bgrewriteaof命令，重写AOF文件，使得AOF文件变小，可以理解为重新写入的操作。

bgrewriteaof和bgsave的工作原理一样。问题是：当AOF文件很大的时候，重新写AOF,在删除旧的AOF文件的时候操作系统会挂起数秒。

跟快照持久化可以设置save选项来自动执行bgsave一样，AOF也可以设置auto-aof-rewrite-percentage选项和auto-aof-rewrite-min-size选项自动执行bgrewriteaof.

auto-aof-rewrite-percentage 100,auto-aof-rewrite-min-size 64mb。

当aof文件体积大于64MB并且AOF文件的体积比上次重写后的体积大了至少一倍100%的时候进行重写。

为了防止频繁的重写，可以将auto-aof-rewrite-percentage设置到100以上。

### 复制

对于有扩展平台以适应更高负载经验架构师来说，复制是不可或缺的。复制能让其他服务器有一个不断更新的数据副本，从而使拥有数据副本的服务器可以用于处理客户端的读请求。做到读写分离。

虽然redis性能优秀，但是当对于集合和有序集合进行操作的时候，涉及到上万或者百万，执行操作也需要以秒来计算了。即使一个命令只消耗10毫秒能完成，单个redis实例1秒也只能处理100个命令。

复制到从服务器，在从服务器进行读，减轻了主服务器的负担，从而提高性能。

#### 复制选项

当从服务器连接主服务器的时候，主服务器会执行bgsave操作。

必须配置项：slaveof host port。

可以使用slaveof no one命令停止复制，使用slaveof host port 命令让服务器复制一个新的主服务器。

Redis复制的启动过程

主服务器等待命令-从发送sync命令-主开始执行bgsave,并使用缓存区存放之后的操作-从根据配置决定继续使用来处理客户端的请求-主服务器bgsave完毕后，向从服务器发送快照文件，并且使用缓存区存之后的操作-从丢弃旧的数据，开始载入主服务器发来的快照文件-主快照文件发送完毕，开始向从服务器发送缓存在缓冲区的写命令-完成快照的解释操作像往常一样可以接受命令请求了-主写缓存的命令完毕之后，从当前开始每执行一个写命令就向从服务器发送相同的写命令-从接受并执行每个写命令

为了让实际中可用，一般让主服务器只使用50%~65%的内存，留下30~45%的内存用于执行bgsave命令和创建记录写命令的缓冲区。

Redis不支持主主复制，从服务器在进行同步时，会清空自己的所有数据。
