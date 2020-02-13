---
layout: post
title:  redis1
date:   2020-02-12 20:53:12 +08:00
category: 签到系列
tags: 多线程
comments: true
---



[TOC]



签到19！



## 学习总结

定定住天涯，依依向物华。 寒梅最堪恨，常作去年花。



## 1、介绍Redis

- 概念

  Redis是一个开源(BSD许可)的内存数据结构存储，用作数据库、缓存和消息代理。

- 数据类型

  它支持诸如字符串、散列、列表、集、带范围查询的排序集、位图、hyperloglogs、带半径查询和流的地理空间索引等数据结构。

- 特性

  Redis具有内置的复制、Lua脚本、LRU清除、事务和不同级别的磁盘持久性，并通过Redis Sentinel和带有Redis集群的自动分区提供高可用性（自动故障切换）。

- 原子操作

  包括附加到一个字符串、在散列中增加值、将元素推入列表、计算集的交，并，差、得到排序集中排名最高的成员。

## 2、Redis中的key

Redis的key是二进制安全的，这意味着您可以使用任何二进制序列作为key，从“foo”这样的字符串到JPEG文件的内容。空字符串也是一个有效的键。

**key使用规则**

- 不要太长

- 不要太短

- 使用一定的规则

  点或破折号通常用于多字字段

  - "object-type:id" （"user:1000" ）
  - "comment：1234:reply.to" or "comment：123：reply-to"

- 最大运行512M

## 3、常用数据类型及场景

#### **strings**

- 注意
  - 内容大小不能大于512MB

- 操作

  - GET

  - SET

    如果存在key就失败nail，

  -  INCR

    当时int时使用，原子增加1，多个客户操作能够保证原子性；类似的有INCRBY增加x, DECR and DECRBY.

  - GETSET

    设置为新的值，返回旧的值

  - MSET

    批量设置，MGET批量返回

  - exists

    查询是否有该key,返回1或者0

  - DEL

  - APPEND

  - expire

    设置存活时间，通过ttl查询剩余时间， PERSIST 取消该值的存活时间，设置milliseconds 的单位，需要PEXPIRE和PTTL

#### **Lists**

有序列表

- 操作

  - rpush

    右边添加

  - lpush

  - lrange

    获取从左侧开始的列表子集，如0，-1获取所有，0，-2获取从0到倒数第二个数据

  - rpop

    从右边弹出一个

  -  LTRIM

    可以限制链表，如只保留最新的几个，最新新闻、日志或者什么

  - BRPOP

    在使用阻塞队列进行生产者消费模型时，可以让消费者去等待一定时间，或者设置为0一直等待，超时返回null

- 使用场景

  - 记住用户在社交网络上发布的最新更新。
  -  流程之间的通信，使用消费者-生产者模式，生产者将项目推入列表，消费者(通常是工作人员)使用这些项目并执行操作。Redis有特殊的列表命令，使这个用例更加可靠和高效。
  - 保留最新的新闻、日志
  - 阻塞队列，生产者消费模型

- 创建key和销毁key的规则

  - 当给key添加一个值得时候，如果没有该key,则先增加一个空值得key，然后添加值；
  - 删除一个键的时候，就将该key自动销毁
  - del一个key,或者llen一个key,为0的时候，也销毁该key

#### Hashes

```
> hmset user:1000 username antirez birthyear 1977 verified 1
OK
> hget user:1000 username
"antirez"
> hget user:1000 birthyear
"1977"
> hgetall user:1000
1) "username"
2) "antirez"
3) "birthyear"
4) "1977"
5) "verified"
6) "1"
```

- 操作

  - hmset

  - hget

  - hgetall

  - hmget

  - hincrby

    ```
    > hincrby user:1000 birthyear 10
    (integer) 1987
    > hincrby user:1000 birthyear 10
    (integer) 1997
    ```

####  Sets

```
 > sadd myset 1 2 3
(integer) 3
> smembers myset
1. 3
2. 1
3. 2
```

- 操作

  - sadd

  - smembers

    带序号都列出来

  - sismember

    判断key中是否存在某个值

  - sinter

    两个集合的交集

  - SPOP

    从集合中拿出几个，这个对建模很重要

  - sunionstore

    给你集合拷贝另一个集合的所有值

  - scard

    获取集合中的数量

  - SRANDMEMBER

    随机获取数据，不移除

#### Sorted sets

>混合的数据结构：set与hash的组合

- 操作

  - zadd

  - zrange

    - zrange hackers 0 -1 withscores

      带有分数的列出来，一个值一个score

  - zrevrange

  - zrangebyscore

    - zrangebyscore  hackers -inf 1950

      分数从无穷大到1950的所有值

  - zremrangebyscore

    ```
    zremrangebyscore hackers 1940 1960
    分数在这之间的元素个数
    ```

  - zrank

    ```
    > zrank hackers "Anita Borg"
    (integer) 4
    获取排名
    ```

  - zrangebylex

    字典编排

    ```
    zrangebylex hackers [B [P
    获取首字母在B和P之间的值
    ```

#### Bitmaps

#### HyperLogLogs

## 4、Transactions（事务）

为了保证事务，单个步骤中一组命令：

- 原则

  - 事务中的所有命令都被序列化并按顺序执行。在执行一个Redis事务的过程中，不可能出现另一个客户端发出的请求。这保证了命令作为一个单独的独立操作执行。
  - 要么处理所有命令，要么一个也不处理，因此Redis事务也是原子性的。

- Redis不支持事务回滚

- 命令

  - MULTI

    事务开始前的标志，总是返回ok

  - EXEC

    执行从MULTL之后的queued的命令

  - DISCARD

    退出事务

  - WATCH

    监控键的值得修改，提供a check-and-set (CAS) behavior to Redis transactions.

    ```
    WATCH mykey
    val = GET mykey
    val = val + 1
    MULTI
    SET mykey $val
    EXEC
    ```



## 5、Pub/Sub

​	订阅、取消订阅和发布实现了发布/订阅消息范型，其中(引用Wikipedia)发送方(发布方)没有将其消息发送给特定的接收方(订阅者)。相反，发布的消息被描述成频道，而不知道(如果有的话)订阅者可能是谁。订阅者表示对一个或多个通道感兴趣，只接收感兴趣的消息，而不知道(如果有的话)发布者在哪里。发布者和订阅者之间的这种解耦允许更大的可伸缩性和更动态的网络。

​	一般这种情况使用中间件处理不需要使用Redis,这里不做介绍；

- SUBSCRIBE
- PUBLISH

## 6、Lua scripting

EVAL和EVALSHA用于使用从版本2.6.0开始构建到Redis中的Lua解释器来评估脚本。

```
> eval "return {KEYS[1],KEYS[2],ARGV[1],ARGV[2]}" 2 key1 key2 first second
1) "key1"
2) "key2"
3) "first"
4) "second"
```

- redis命令

  - `redis.call()`

    ```
    > eval "return redis.call('set','foo','bar')" 0
    OK
    ```

  - `redis.pcall()`

  两者的区别是一个失败返回失败，另一个返回异常信息



## 7、Keys with a limited time-to-live

给key设置超时时间，当超时时间到了自动删除。*volatile* 术语在redis当中跟超时有关。

#### 超时时间清除的情况

- 删除

  命令：DEL

- 重写内容

  命令： SET, GETSET and all the *STORE commands

- PERSIST

- 不改变超时时间的行为

  这意味着，所有在概念上更改存储在键上的值而不使用新值替换它的操作都不会影响超时。例如，使用INCR增加键值，使用LPUSH将新值推入列表，或者使用HSET更改散列的字段值，这些操作都不会影响超时。

  - RENAME

#### 设置超时时间

- EXPIRE

  如果key存在返回1，不存在返回0

  - TTL

    查看剩余时间，-1表示没有超时时间

#### 应用场景

- 给用户推荐的感兴趣页面

  设置最新的推荐页面，时间为60s

#### Redis过期一个key

- 消极方式

  当客户访问的时候进行处理，如果不访问将用于存在系统中，这很不好

- 积极方式

  每一秒执行十次

  - 从一组具有关联过期的键中测试20个随机键
  -  删除所有已过期的密钥
  - 如果超过25%的key过期，则重新从步骤1开始

#### 如何在复制链接和AOF文件中处理过期

- 为了在不牺牲一致性的情况下获得正确的行为，当密钥过期时，将在AOF文件中合成DEL操作并获得所有附加的副本节点。
- 然而而独立副本连接到一个主键不会到期(但将等待DEL来自master),他们仍然会把到期的全部状态存在的数据集,所以当一个复制品当选掌握它将能够到期独立的关键,完全充当master。

## 8、LRU eviction of keys

使用Redis作为LRU最少使用缓存

配置最大使用内存，当达到指定的内存量时，可以在不同的行为(称为策略)之间进行选择。

```
maxmemory 100mb
# 如果设置为0，没有限制，这是64位系统的默认行为，而32位系统使用3GB的隐式内存限制。
```

配置策略maxmemory-policy

- noeviction

   没有策略

- **allkeys-lru**

  最近最少使用的删掉；**期望是幂律分布时**；

- volatile-lru

  在设置了超时时间的key中执行，最近最少使用的删掉

- **allkeys-random**

  所有的key中随机删除；**如果您有一个循环访问，其中所有的键被连续地扫描，或者当您期望分布是均匀的**

- volatile-random

  在设置了超时时间的key中执行，随机的删掉

- **volatile-ttl**

  在设置了超时时间的key中执行，删掉存活时间最短的key，**希望能够通过使用不同的TTL值向**

如果没有设置超时时间这个先决条件，那么volatile-lru 、volatile-random、volatile-ttl跟noeviction是一样的

volatile-lru和volatile-random策略在希望使用单个实例进行缓存和拥有一组持久键时非常有用。然而，运行两个Redis实例来解决这样的问题通常是一个更好的主意。

Redis LRU算法不是一个精确的实现(需要大的内存)：那么通过抽样解决问题，通过抽样的数量来配置增加精度;

```
maxmemory-samples 5
```

#### The new LFU mode

最新的最少使用清除模型

```
lfu-log-factor 10
lfu-decay-time 1
```

## 9、Automatic failover

Redis Sentinel哨兵为Redis提供高可用性。

Redis Sentinel还提供其他附属任务，如监控、通知和为客户提供配置。

- **Monitoring**.

  Sentinel constantly checks if your master and replica instances are working as expected.

- **Notification**.

  Sentinel can notify the system administrator, or other computer programs, via an API, that something is wrong with one of the monitored Redis instances.

- **Automatic failover**.

   If a master is not working as expected, Sentinel can start a failover process where a replica is promoted to master, the other additional replicas are reconfigured to use the new master, and the applications using the Redis server are informed about the new address to use when connecting.

- **Configuration provider**.

  Sentinel acts as a source of authority for clients service discovery: clients connect to Sentinels in order to ask for the address of the current Redis master responsible for a given service. If a failover occurs, Sentinels will report the new address.

分布式的哨兵集群

- 当多个哨兵认为某个节点失败，就进行故障检测，降低了误报率；
- 提高了哨兵集群的可靠性

启动哨兵集群需要：

```
redis-sentinel /path/to/sentinel.conf
redis-server /path/to/sentinel.conf --sentinel
# 都需要配置文件和默认监听端口打开26379
```

其余的参数根据情况查文档
