---
layout: post
title:  数据库分库分表-mycat
date:   2019-04-03 22:52:12 +08:00
category: 高并发分布式
tags: mycat
comments: true
---

* content
{:toc}

1 数据库的瓶颈

2 大数据量数据库性能解决方案

3 Mycat












## 数据库的瓶颈

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mycat001.png)

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mycat002.png)

月增长：用户表200W+，会员订单表1000W+，订单商品表：3000W+

产生瓶颈的原因分析

1、数据库连接数

2、表数据量大【空间】

3、硬件资源(QPS/TPS)

。。。

## 大数据量数据库性能解决方案

1、读写分离

区别读、写多数据源方式进行数据的存储和加载。

数据的存储（增删改）一般指定写数据源，数据的读取查询指定读数据源
(读写分离会基于主从复制)

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mycat004.png)

主从形式

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mycat007.png)



2、分库分表区别读、写多数据源方式进行数据的存储和加载。数据的存储（增删改）一般指定写数据源，数据的读取（查询指定读数据源对数据的库表进行拆分，用分片的方式对数据进行管

分库分表的拆分规则

1>.垂直拆分

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mycat008.png)

2>.水平拆分

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mycat009.png)

## mycat

Mycat 是开源的分布式数据库中间件，基于阿里的cobar的开源框架之上。

它处于数据库服务与应用服务之间。

它是进行数据处理与整合的中间服务。

通俗点讲，应用层可以将它看作是一个数据库的代理（或者直接看成加强版数据库）

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mycat010.png)

逻辑库

逻辑表

分片表

分片规则

全局表

ER表

非分片表

节点

节点主机（写、读节点主机）

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mycat011.png)

mysql是基于binlog的主从复制原理

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mycat013.png)

1 master将操作记录到二进制日志(binary log)中（这些记录叫做二进制日志事件，binary log events）

2 Slave通过I/O Thread异步将master的binary log events拷贝到它的中继日志(relay log)；

3 Slave执行relay日志中的事件，匹配自己的配置将需要执行的数据，在slave服务上执行一遍从而达到复制数据的目的

Master操作：

```

1.接入mysql并创建主从复制的用户

create user m2ssync identified by 'Qq123!@#';

2.给新建的用户赋权
GRANT REPLICATION SLAVE ON *.* TO
'm2ssync'@'%' IDENTIFIED BY 'Qq123!@#';

3.指定服务ID，开启binlog日志记录，在my.cnf
中加入
server-id=137
log-bin=dbstore_binlog
binlog-do-db=db_store
4.通过SHOW MASTER STATUS;查看Master db状态.


```

Slave操作：

```

1.指定服务器ID，指定同步的binlog存储位置，在my.cnf中加入
server-id=101
relay-log=slave-relay-bin
relay-log-index=slave-relay-bin.index
read_only=1
replicate_do_db=db_store

2.接入slave的mysql服务，并配置change master to master_host='192.168.8.137',
master_port=3306,master_user=
'm2ssync'
,master_p
assword=
'Qq123!@#'
,master_log_file=
'db_stoere_bi
nlog'
,master_log_pos=0;

3.start slave;

4. show slave status\G ;查看slave服务器状态

```

mycat目录

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mycat021.png)

bin 程序目录，存放了 window 版本和 linux 版本可执行文件./mycat {start|restart|stop|status…}

conf 目录下存放配置文件，

```
server.xml 是 Mycat 服务器参数调整和用户授权的配置文件
schema.xml 是逻辑库定义和表
rule.xml 是分片规则的配置文件，分片规则的具体一些参数信息单独存放为文件，也在
这个目录下
log4j2.xml配置logs目录日志输出规则
wrapper.conf JVM相关参数调整

```

lib 目录下主要存放 mycat 依赖的一些 jar 文件

logs目录日志存放日志文件

mycat目录详解

| 文件   | 作用     |
| :------------- | :------------- |
| schema.xml      |  逻辑库表      |
| server.xml      |  启动参数      |
| rule.xml      |  拆分规则     |
| wrapper.conf     |   jvm内核调整      |


 schema.xml

 ```

 dataHost
balance, writeType, switchType
writeHost\readHost
usingDecrypt
dataNode
sqlMaxLimit
table
rule, primaryKey, autoIncrement, needAddLimit
childTable
joinKey, parentKey

 ```

server.xml

```

system
sequnceHandlerType
Processors
processorExecutor
serverPort
managerPort
firewall
user
benchmark
usingDecrypt
privileges

```
rule.xml

```

连续分片
优点：扩容无需迁移数据，范围条件查询资源消耗小
缺点：数据热点问题，并发能力受限于分片节点
代表：
• 按日期（天）分片
• 自定义数字范围分片
• 自然月分片

离散分片
优点：数据分布均匀，并发能力强，不受限分片节点
缺点：移植性差，扩容难
代表：
• 枚举分片
• 数字取模分片
• 字符串hash分片
• 一致性哈希分片
• 程序指定

综合类分片
兼并二者
代表：
• 范围求模分片
• 取模范围约束分片

```

连续分片-自定义数字范围分片

```

自定义数字范围分片，提前规划好分片字段某个范围属于哪个分片
<function name=
"rang-long"
class=
"io.mycat.route.function.AutoPartitionByLong">
<property name=
"mapFile">autopartition-long.txt</property>
<property name=
"defaultNode">0</property>
</function>
defaultNode 超过范围后的默认节点。
此配置非常简单，即预先制定可能的id范围到某个分片
0-500M=0
500M-1000M=1
1000M-1500M=2
或
0-10000000=0
10000001-20000000=1
注意： 所有的节点配置都是从0开始，及0代表节点1

```

连续分片-按日期（天）分片

```

按日期（天）分片： 从开始日期算起，按照天数来分片
<function name=
“sharding-by-date” class=
“io.mycat.route.function.PartitionByDate">
<property name=
“dateFormat”>yyyy-MM-dd</property> <!—日期格式-->
<property name=
“sBeginDate”>2014-01-01</property> <!—开始日期-->
<property name=
“sPartionDay”>10</property> <!—每分片天数-->
</function>
按日期（自然月）分片： 从开始日期算起，按照自然月来分片
<function name=
“sharding-by-month” class=
“io.mycat.route.function.PartitionByMonth">
<property name=
“dateFormat”>yyyy-MM-dd</property> <!—日期格式-->
<property name=
“sBeginDate”>2014-01-01</property> <!—开始日期-->
</function>
注意： 需要提前将分片规划好，建好，否则有可能日期超出实际配置分片数
```

连续分片-按月小时分片

```

按单月小时分片：最小粒度是小时，可以一天最多24个分片，最少1个分片，一个月完后下月从头开始循环。
<function name=
"sharding-by-hour" class=
“io.mycat.route.function.LatestMonthPartion">
<property name=
“splitOneDay”>24</property> <!-- 将一天的数据拆解成几个分片-->
</function>
注意事项：每个月月尾，需要手工清理数据
```

离散分片-枚举分片

```

枚举分片：通过在配置文件中配置可能的枚举id，自己配置分片，本规则适用于特定的场景，比如有些业务需要按照省份或区县来做保存，而全国省份区县固定的
<function name=
"hash-int" class=
“io.mycat.route.function.PartitionByFileMap">
<property name=
"mapFile">partition-hash-int.txt</property>
<property name=
"type">0</property>
<property name=
"defaultNode">0</property>
</function>
partition-hash-int.txt 配置：
10000=0
10010=1
mapFile标识配置文件名称
type默认值为0（0表示Integer，非零表示String）
默认节点的作用：枚举分片时，如果碰到不识别的枚举值，就让它路由到默认节点

```

离散分片-十进制取模

```

十进制求模分片：规则为对分片字段十进制取模运算。数据分布最均匀
<function name=
"mod-long" class=
“io.mycat.route.function.PartitionByMod">
<!-- how many data nodes -->
<property name=
"count">3</property>
</function>

```

离散分片-应用指定分片

```

应用指定分片：规则为对分片字段进行字符串截取，获取的字符串即指定分片。
<function name=
"sharding-by-substring“ class=
"io.mycat.route.function.PartitionDirectBySubString">
<property name=
"startIndex">0</property><!-- zero-based -->
<property name=
"size">2</property>
<property name=
"partitionCount">8</property>
<property name=
"defaultPartition">0</property>
</function>
startIndex 开始截取的位置
size 截取的长度
partitionCount 分片数量
defaultPartition 默认分片
例如 id=05-100000002
在此配置中代表根据 id 中从 startIndex=0，开始，截取 siz=2 位数字即 05，05 就是获取的分区，如果没传
默认分配到 defaultPartition

```

离散分片-字符串截取数字hash分片

```

截取数字hash分片
此规则是截取字符串中的int数值hash分片
<function name=
"sharding-by-stringhash" class=
“io.mycat.route.function.PartitionByString">
<property name=length>512</property><!-- zero-based -->
<property name=
"count">2</property>
<property name=
"hashSlice">0:2</property>
</function>
length代表字符串hash求模基数，count分区数，其中length*count=1024
hashSlice hash预算位，即根据子字符串中int值 hash运算
0 代表 str.length(), -1 代表 str.length()-1，大于0只代表数字自身
可以理解为substring（start，end），start为0则只表示0
例1：值“45abc”
，hash预算位0:2 ，取其中45进行计算
例2：值“aaaabbb2345”
，hash预算位-4:0 ，取其中2345进行计算

```

离散分片-一致性hash分片

```

一致性Hash分片：
此规则优点在于扩容时迁移数据量比较少，前提分片节点比较多，虚拟节点分配多些。
虚拟节点少的缺点是会造成数据分布不够均匀
如果实际分片数量比较少，迁移量会比较多
<function name=
"murmur" class=
“io.mycat.route.function.PartitionByMurmurHash">
<property name=
“seed”>0</property><!-- 创建hash对象的种子，默认0-->
<property name=
"count">2</property><!-- 要分片的数据库节点数量，必须指定，否则没法分片-->
<property name=
"virtualBucketTimes">160</property>
</ function>
注意：
一个实际的数据库节点被映射为这么多虚拟节点，默认是160倍，也就是虚拟节点数是物理节点数的160倍

```

一致性hash

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/hash00001.png)

综合分片-范围取模分片

```

范围求模分片：先进行范围分片计算出分片组，组内再求模。
优点可以避免扩容时的数据迁移，又可以一定程度上避免范围分片的热点问题
分片组内使用求模可以保证组内数据比较均匀，分片组之间是范围分片可以兼顾范围查询。
最好事先规划好分片的数量，数据扩容时按分片组扩容，则原有分片组的数据不需要迁移。
由于分片组内数据比较均匀，所以分片组内可以避免热点数据问题。
<function name=
"rang-mod" class=
“io.mycat.route.function.PartitionByRangeMod">
<property name=
"mapFile">partition-range-mod.txt</property>
<property name=
"defaultNode">32</property>
</function>
partition-range-mod.txt
以下配置一个范围代表一个分片组，=号后面的数字代表该分片组所拥有的分片的数量。
0-200M=5 //代表有5个分片节点
200M-400M=6
400M-600M=6
600M-800M=8
800M-1000M=7

```

综合分片-取模范围约束分片

```

取模范围约束分片：
对指定分片列进行取模后再由配置决定数据的节点分布。
<function name=
"sharding-by-pattern" class=
“io.mycat.route.function.PartitionByPattern">
<property name=
"patternValue">256</property>
<property name=
"defaultNode">2</property>
<property name=
"mapFile">partition-pattern.txt</property>
</function>
patternValue 即求模基数，
defaoultNode 默认节点
partition-pattern.txt配置
1-32=0
33-64=1
65-96=2
97-128=3
128-256=4
配置文件中，1-32 即代表id%256后分布的范围。
如果id非数字，则会分配在defaoultNode 默认节点
```

分片取舍

数据特点：

活跃的数据热度较高规模可以预期，增长量比较稳定

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mycat0009.png)

数据特点：

活跃的数据为历史数据，热度要求不高。规模可以预期，增长量比较稳定. 优势可定时清理或者迁移数据

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mycat1021.png)

分片总结

1，根据业务数据的特性合理选择分片规则

2，善用全局表、ER关系表解决join操作

3，用好primaryKey让你的性能起飞

待续。。。
