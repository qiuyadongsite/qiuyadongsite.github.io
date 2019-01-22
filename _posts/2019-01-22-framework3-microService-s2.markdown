---
layout: post
title:  分布式架构二
date:   2019-01-22 20:52:12 +08:00
category: 微服务架构
tags: 分布式
comments: true
---

* content
{:toc}

对于微服务来说，分布式架构将是研究的重点，本篇继续介绍分布式架构的一些概念！












## 阶段七，数据库的水平/垂直拆分

我们的网站演进的变化过程，交易、商品、用户的数据都还在同一个数据库中，尽管采取了增加缓存，读写分离的方式，但是随着数据库的压力持续增加，数据库的瓶颈仍然是个最大的问题。因此我们可以考虑对数据的垂直拆分和水平拆分

垂直拆分：把数据库中不同业务数据拆分到不同的数据库：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/vesplitdb.png)

水平拆分：把同一个表中的数据拆分到两个甚至跟多的数据库中，水平拆分的原因是某些业务数据量已经达到了单个数据库的瓶颈，这时可以采取讲表拆分到多个数据库中：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/bansplitdb.png)

## 阶段八，应用的拆分

随着业务的发展，业务越来越多，应用的压力越来越大。工程规模也越来越庞大。这个时候就可以考虑讲应用拆分，按照领域模型讲我们的用户、商品、交易拆分成多个子系统：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/domainsplitdb.png)

这样拆分以后，可能会有一些相同的代码，比如用户操作，在商品和交易都需要查询，所以会导致每个系统都会有用户查询访问相关操作。这些相同的操作一定是要抽象出来，否则就会是一个坑。所以通过走服务化路线的方式来解决:

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/sevicesplitdb.png)

 那么服务拆分以后，各个服务之间如何进行远程通信呢？
 >通过 RPC 技术，比较典型的有：webservice、hessian、http、RMI等等
前期通过这些技术能够很好的解决各个服务之间通信问题，but，互联网的发展是持续的，所以架构的演变和优化还在持续。

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/sosplitdb.png)
