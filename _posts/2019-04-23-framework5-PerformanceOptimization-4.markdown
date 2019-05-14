---
layout: post
title:  索引机制
date:   2019-04-23 21:52:12 +08:00
category: 性能优化
tags: mysql
comments: true
---

* content
{:toc}


mysql必背，正确的创建索引是提高性能的基础。























## 索引

索引是为了加速对表中数据行的检索而创建的一种分散存储的数据结构。

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mysql001.png)

索引能极大的减少存储引擎需要扫描的数据量

索引可以把随机IO变成顺序IO

索引可以帮助我们在进行分组、排序等操作时，避免使用临时表

索引底层使用B+树

解释一下：

首先是`二分查找树`

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mysql006.png)

问题是，有可能o(n)

提高二分树的查询问题，引入`平衡二叉树`

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mysql007.png)

左右子树高度差不大于1

但也会存在问题：

它太深了
（每一次都要将节点复制到内存中，数据越多，io的次数越多）
数据处的（高）深度决定着他的IO操作次数，IO操作耗时大

它太小了
（每一次io其实可以达到4k,但是单个节点存的数据仅仅几个字节，造成io浪费）

每一个磁盘块（节点/页）保存的数据量太小了
没有很好的利用操作磁盘IO的数据交换特性，
也没有利用好磁盘IO的预读能力（空间局部性原理），从而带来频繁的IO操作

`多路平衡二叉树` B-树(balance)

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mysql009.png)

mysql采用加强版b树-b+树

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mysql010.png)

### B-树与B+树的区别

1，B+节点关键字搜索采用闭合区间

2，B+非叶节点不保存数据相关信息，只保存关键字和子节点的引用

3，B+关键字对应的数据保存在叶子节点中

4，B+叶子节点是顺序排列的，并且相邻节点具有顺序引用的关系

### 为啥选择B+树？

B+树是B-树的变种（PLUS版）多路绝对平衡查找树，他拥有B-树的优势

B+树扫库、表能力更强

B+树的磁盘读写能力更强

B+树的排序能力更强

B+树的查询效率更加稳定（仁者见仁、智者见智）

### myisam

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mysql011.png)

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mysql012.png)

### innodb

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mysql013.png)

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mysql021.png)

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mysql023.png)

### 索引原则

离散性越高  选择性就越好（区分性越大，树的可选分支就越好）方便查找

对索引中关键字进行计算（对比），一定是从左往右依次进行，且不可跳过

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mysql024.png)

### 联合索引

单列索引

节点中关键字[name]

联合索引
节点中关键字[name,phoneNum]

单列索引是特殊的联合索引

联合索引列选择原则

1，经常用的列优先 【最左匹配原则】

2，选择性（离散度）高的列优先【离散度高原则】

3，宽度小的列优先【最少空间原则】

`覆盖索引`

如果查询列可通过索引节点中的关键字直接返回，则该索引称之为覆盖索引。

覆盖索引可减少数据库IO，将随机IO变为顺序IO，可提高查询性能

## 总结

索引列的数据长度能少则少。

索引一定不是越多越好，越全越好，一定是建合适的。

匹配列前缀可用到索引 like 9999%，like %9999%、like %9999用不到索引；

Where 条件中 not in 和 <>操作无法使用索引；

匹配范围值，order by 也可用到索引；

多用指定列查询，只返回自己想到的数据列，少用select `*`；

联合索引中如果不是按照索引最左列开始查找，无法使用索引；

联合索引中精确匹配最左前列并范围匹配另外一列可以用到索引；

联合索引中如果查询中有某个列的范围查询，则其右边的所有列都无法使用索引；
