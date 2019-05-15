---
layout: post
title:  mysql引擎机制和执行计划
date:   2019-04-24 21:52:12 +08:00
category: 性能优化
tags: mysql
comments: true
---

* content
{:toc}


mysql必背，正确的创建索引是提高性能的基础。























## 插拔式的存储引擎

1，插拔式的插件方式

2，存储引擎是指定在表之上的，即一个库中的每一个表都可以指定专用的存储引擎。

3，不管表采用什么样的存储引擎，都会在数据区，产生对应的一个frm文件（表结构定义描述文件）

### CSV存储引擎

数据存储以CSV文件

特点：

不能定义没有索引、列定义必须为NOT NULL、不能设置自增列

-->不适用大表或者数据的在线处理
CSV数据的存储用,隔开，可直接编辑CSV文件进行数据的编排

-->数据安全性低


注：编辑之后，要生效使用flush table XXX 命令

应用场景：
数据的快速导出导入
表格直接转换成CSV


### Archive存储引擎

压缩协议进行数据的存储

数据存储为ARZ文件格式

特点：

只支持insert和select两种操作

只允许自增ID列建立索引

行级锁

不支持事务

`数据占用磁盘少`

应用场景：

日志系统

大量的设备数据采集

### Memory存储引擎

数据都是存储在内存中，IO效率要比其他引擎高很多

服务重启数据丢失，内存数据表默认只有16M

特点：

支持hash索引，B tree索引，默认hash（查找复杂度0(1)）

字段长度都是固定长度varchar(32)=char(32)

不支持大数据存储类型字段如 blog，text

表级锁

应用场景：

等值查找热度较高数据

查询结果内存中的计算，大多数都是采用这种存储引擎

作为临时表存储需计算的数据

### myisam存储引擎

Mysql5.5版本之前的默认存储引擎

较多的系统表也还是使用这个存储引擎

系统临时表也会用到Myisam存储引擎

特点：

a，select count(`*`) from table 无需进行数据的扫描

b，数据（MYD）和索引（MYI）分开存储

c，表级锁

d，不支持事务

### innodb存储引擎

Mysql5.5及以后版本的默认存储引擎

Key Advantages：

Its DML operations follow the ACID model [事务ACID]

Row-level locking[行级锁]

InnoDB tables arrange your data on disk to optimize queries based on primary keys[聚集索引（主键索引）方式进行数据存储]

To maintain data integrity, InnoDB supports FOREIGN KEY constraints[支持外键关系保证数据完整性]

### 对比

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mysql025.png)

## mysql体系结构

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mysql026.png)


Client Connectors                   接入方 支持协议很多

Management Serveices & Utilities    系统管理和控制工具，mysqldump、 mysql复制集群、分区管理等

Connection Pool                      连接池：管理缓冲用户连接、用户名、密码、权限校验、线程处理等需要缓存的需求

SQL Interface                        SQL接口：接受用户的SQL命令，并且返回用户需要查询的结果

Parser                           解析器，SQL命令传递到解析器的时候会被解析器验证和解析。解析器是由Lex和YACC实现的

Optimizer               查询优化器，SQL语句在查询之前会使用查询优化器对查询进行优化

Cache和Buffer（高速缓存区）     查询缓存，如果查询缓存有命中的查询结果，查询语句就可以直接去查询缓存中取数据



pluggable storage Engines      插件式存储引擎。存储引擎是MySql中具体的与文件打交道的子系统

file system                   文件系统，数据、日志（redo，undo）、索引、错误日志、查询记录、慢查询等


## MySQL查询优化详解

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mysql027.png)

1，mysql 客户端/服务端通信

2，查询缓存

3，查询优化处理

4，查询执行引擎

5，返回客户端

### 1，mysql 客户端/服务端通信

Mysql客户端与服务端的通信方式是“半双工”；

全双工：双向通信，发送同时也可以接收

半双工：双向通信，同时只能接收或者是发送，无法同时做操作

单工：只能单一方向传送

半双工通信：

在任何一个时刻，要么是有服务器向客户端发送数据，要么是客户端向服务端发送数据，这两个动作不能同时发生。所以我们无法也无需将一个消息切成小块进行传输

特点和限制：

客户端一旦开始发送消息，另一端要接收完整个消息才能响应。

客户端一旦开始接收数据没法停下来发送指令。

#### mysql 客户端/服务端通信-查询状态

对于一个mysql连接，或者说一个线程，时刻都有一个状态来标识这个连接正在做什么

查看命令 show full processlist / show processlist


[所有状态](https://dev.mysql.com/doc/refman/5.7/en/general-thread-states.html)

Sleep           线程正在等待客户端发送数据

Query             连接线程正在执行查询

Locked          线程正在等待表锁的释放

Sorting result   线程正在对结果进行排序

Sending data    向请求端返回数据

可通过kill {id}的方式进行连接的杀掉

#### 查询缓存

工作原理：

缓存SELECT操作的结果集和SQL语句；

新的SELECT语句，先去查询缓存，判断是否存在可用的记录集；

判断标准：

与缓存的SQL语句，是否完全一样，区分大小写 (简单认为存储了一个key-value结构，key为sql，value为sql查询结果集)

query_cache_type

值：0 -– 不启用查询缓存，默认值；

值：1 -– 启用查询缓存，只要符合查询缓存的要求，客户端的查询语句和记录集都可以缓存起来，供其他客户端使用，加上 SQL_NO_CACHE将不缓存

值：2 -– 启用查询缓存，只要查询语句中添加了参数：SQL_CACHE，且符合查询缓存的要求，客户端的查询语句和记录集，则可以缓存起来，供其他客户端使用

query_cache_size

允许设置query_cache_size的值最小为40K，默认1M，推荐设置 为：64M/128M；

query_cache_limit

限制查询缓存区最大能缓存的查询记录集，默认设置为1M  show status like 'Qcache%' 命令可查看缓存情况

`不会缓存的情况`：

1.当查询语句中有一些不确定的数据时，则不会被缓存。如包含函数NOW()，CURRENT_DATE()等类似的函数，或者用户自定义的函数，存储函数，用户变量等都不会被缓存

2.当查询的结果大于query_cache_limit设置的值时，结果不会被缓存

3.对于InnoDB引擎来说，当一个语句在事务中修改了某个表，那么在这个事务提交之前，所有与这个表相关的查询都无法被缓存。因此长时间执行事务，会大大降低缓存命中率

4，查询的表是系统表

5，查询语句不涉及到表

`为什么mysql默认关闭了缓存开启`？？

1.在查询之前必须先检查是否命中缓存,浪费计算资源

2.如果这个查询可以被缓存，那么执行完成后，MySQL发现查询缓存中没有这个查询，则会将结果存入查询缓存，这会带来额外的系统消耗

3.针对表进行写入或更新数据时，将对应表的所有缓存都设置失效。

4.如果查询缓存很大或者碎片很多时，这个操作可能带来很大的系统消耗

`查询缓存的应用场景`：

以读为主的业务，数据生成之后就不常改变的业务

比如门户类、新闻类、报表类、论坛类等

### 查询优化的处理

查询优化处理的三个阶段：

- 解析sql

通过lex词法分析,yacc语法分析将sql语句解析成解析树https://www.ibm.com/developerworks/cn/linux/sdk/lex/

- 预处理阶段

根据mysql的语法的规则进一步检查解析树的合法性，如：检查数据的表和列是否存在，解析名字和别名的设置。还会进行权限的验证

- 查询优化器

优化器的主要作用就是找到最优的执行计划

#### 查询优化器如何找到最优查询计划

使用等价变化规则

5 = 5 and a > 5 改写成 a > 5

a < b and a = 5 改写成 b > 5 and a = 5

基于联合索引，调整条件位置等

- 优化count 、min、max等函数

min函数只需找索引最左边

max函数只需找索引最右边

 myisam引擎count(`*`)

• 覆盖索引扫描

• 子查询优化

• 提前终止查询

用了limit关键字或者使用不存在的条件

• IN的优化

先进性排序，再采用二分查找的方式

... Mysql的查询优化器是基于成本计算的原则。他会尝试各种执行计划。

数据抽样的方式进行试验（随机的读取一个4K的数据块进行分析）

使用explain sql `\G`查看优化情况，优化执行的规则如下：

select查询的序列号，标识执行的顺序

1、id相同，执行顺序由上至下

2、id不同，如果是子查询，id的序号会递增，id值越大优先级越高，越先被执行

3、id相同又不同即两种情况同时存在，id如果相同，可以认为是一组，从上往下顺序执行；在所有组中，id值越大，优先级越高，越先执行


参数解释：

查询的类型，主要是用于区分普通查询、联合查询、子查询等

SIMPLE：简单的select查询，查询中不包含子查询或者union

PRIMARY：查询中包含子部分，最外层查询则被标记为primary SUBQUERY/MATERIALIZED：SUBQUERY表示在select 或 where列表中包含了子查询

MATERIALIZED表示where 后面in条件的子查询

UNION：若第二个select出现在union之后，则被标记为union；

UNION RESULT：从union表获取结果的select

查询涉及到的表table

直接显示`表名或者表的别名`

<unionM,N> 由ID为M,N 查询union产生的结果

<subqueryN> 由ID为N查询生产的结果

- 执行计划-`type`

访问类型，sql查询优化中一个很重要的指标，结果值从好到坏依次是：

system > const > eq_ref > ref > range > index > ALL

system：表只有一行记录（等于系统表），const类型的特例，基本不会出现，可以忽略不计

const：表示通过索引一次就找到了，const用于比较primary key 或者 unique索引

eq_ref：唯一索引扫描，对于每个索引键，表中只有一条记录与之匹配。常见于主键 或 唯一索引扫描

ref：非唯一性索引扫描，返回匹配某个单独值的所有行，本质是也是一种索引访问

range：只检索给定范围的行，使用一个索引来选择行

index：Full Index Scan，索引全表扫描，把索引从头到尾扫一遍

ALL：Full Table Scan，遍历全表以找到匹配的行


possible_keys   查询过程中有可能用到的索引

key             实际使用的索引，如果为NULL，则没有使用索引

rows 根据表统计信息或者索引选用情况，大致估算出找到所需的记录所需要读取的行数

filtered  它指返回结果的行占需要读到的行(rows列的分比   表示返回结果的行数占需读取行数的百分比，filtered的值越大越好

- 执行计划的额外-extra

1、Using filesort ：

mysql对数据使用一个外部的文件内容进行了排序，而不是按照表内的索引进行排序读取

2、Using temporary：

使用临时表保存中间结果，也就是说mysql在对查询结果排序时使用了临时表，常见于order by 或 group by

3、Using index：

表示相应的select操作中使用了覆盖索引（Covering Index），避免了访问表的数据行，效率高

4、Using where ：

使用了where过滤条件

5、select tables optimized away：

基于索引优化MIN/MAX操作或者MyISAM存储引擎优化COUNT(`*`)操作，不必等到执行阶段在进行计算，查询执行计划生成的阶段即可完成优化

- 查询执行引擎

调用插件式的存储引擎的原子API的功能进行执行计划的执行

- 返回客户端

1、有需要做缓存的，执行缓存操作

2、增量的返回结果：

开始生成第一条结果时,mysql就开始往请求方逐步返回数据

好处： mysql服务器无须保存过多的数据，浪费内存用户体验好，马上就拿到了数据

## 如何定位慢sql

1、业务驱动

2、测试驱动

3、慢查询日志

### 慢查询日志

show variables like 'slow_query_log'

set global slow_query_log = on

set global slow_query_log_file = '/var/lib/mysql/gupaoedu-slow.log'

set global log_queries_not_using_indexes = on set global long_query_time = 0.1 (秒)

### 慢查询日志分析

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mysql0010.png)


Time ：日志记录的时间

User@Host：执行的用户及主机

Query_time：查询耗费时间 Lock_time 锁表时间

Rows_sent 发送给请求方的记录条数 Rows_examined 语句扫描的记录条数

SET timestamp 语句执行的时间点

select .... 执行的具体语句

### 慢查询日志分析工具

mysqldumpslow -t 10 -s at /var/lib/mysql/gupaoedu-slow.log

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mysql434.png)
