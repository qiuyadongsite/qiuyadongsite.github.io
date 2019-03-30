---
layout: post
title:  分布式缓存技术Redis原理分析
date:   2019-03-30 21:52:12 +08:00
category: 高并发分布式
tags: Redis
comments: true
---

* content
{:toc}

过期时间、发布订阅、单线程、lua相关内容。











## 过期时间设置

在Redis中提供了Expire命令设置一个键的过期时间，到期以后Redis会自动删除它。这个在我们实际使用过程中用得非常多。

EXPIRE命令的使用方法为

`EXPIRE key seconds`

其中seconds 参数表示键的过期时间，单位为秒。

EXPIRE 返回值为1表示设置成功，0表示设置失败或者键不存在

如果向知道一个键还有多久时间被删除，可以使用TTL命令

`TTL key`

当键不存在时，TTL命令会返回-2

而对于没有给指定键设置过期时间的，通过TTL命令会返回-1

如果向取消键的过期时间设置（使该键恢复成为永久的），可以使用PERSIST命令，如果该命令执行成功或者成功清除了过期时间，则返回1 。 否则返回0（键不存在或者本身就是永久的）

EXPIRE命令的seconds命令必须是整数，所以最小单位是1秒，如果向要更精确的控制键的过期时间可以使用

PEXPIRE命令，当然实际过程中用秒的单位就够了。 PEXPIRE命令的单位是毫秒。即`PEXPIRE key 1000`与`EXPIRE key 1`相等；对应的PTTL以毫秒单位获取键的剩余有效时间

还有一个针对字符串独有的过期时间设置方式setex(String key,int seconds,String value)

过期删除的原理

Redis 中的主键失效是如何实现的，即失效的主键是如何删除的？实际上，Redis 删除失效主键的方法主要有两种：

消极方法（passive way）

在主键被访问时如果发现它已经失效，那么就删除它

积极方法（active way）

周期性地从设置了失效时间的主键中选择一部分失效的主键删除

对于那些从未被查询的key，即便它们已经过期，被动方式也无法清除。因此Redis会周期性地随机测试一些key，

已过期的key将会被删掉。Redis每秒会进行10次操作，具体的流程：

1 随机测试 20 个带有timeout信息的key；

2 删除其中已经过期的key；

3 如果超过25%的key被删除，则重复执行步骤1；

这是一个简单的概率算法（trivial probabilistic algorithm），基于假设我们随机抽取的key代表了全部的key空间

## Redis发布订阅
