---
layout: post
title:  Mysql
date:   2019-07-01 20:52:12 +08:00
category: 归零
tags: 性能优化
comments: true
---

* content
{:toc}


Mysql！











## 学习笔记

正确的创建合适的索引

索引是为了加速对表中数据行的检索而创建的一种分散存储的数据结构

- 索引能极大的减少存储引擎需要扫描的数据量

- 索引可以把随机IO变成顺序IO

- 索引可以帮助我们在进行分组、 排序等操作时， 避免使用临时表

平衡二叉树查找：太深了高度太高了加载的次数多、太小了一次加载的数据量太少了一次IO操作交换的数据少4k大小（空间局部性原理，mysql 16K），瘦高，路数是2

B树,多路平衡查找树（绝对平衡树，关键字等于路数-1）io操作减小，查询的数据大，矮胖，路数很多

冗余的索引会拖垮整个系统的性能，因为要构建b树，进行分裂合并，所有的子节点在同一高度

mysql使用b+树，使用路数与关键字数一样

所以关键字端的长度越小，单个节点存储的数据越多关键字越多，路数越多，越矮胖越好

左闭合区间，B+tree除了叶子节点不存储数据，只保存关键字，所以存的数据越多，叶子节点保存数据，叶子区是有序的，天然有序行

有b树的有点，b+树的扫描叶子节点就行，变成顺序io,可以加载的数据越多，排序能力越强。

b+稳定性越强，必须找到叶子节点才能找到数据，查询效率可算，而不是看数据分布特性。

程序健壮性稳定性好

存储引擎可以用在用在表上

show create table bas_file;//查看创建表的语句，发现 ENGINE=InnoDB DEFAULT

show variables like 'datadir';//查看数据存储位置

innodb 以主键为索引组织数据的存储，没有定义主键，隐形的设置一个主键  ，辅助索引先查找查找的叶子节点，该节点存储主键索引，再去主键索引树查找，（认为主键是稳定的树，辅助索引的叶子节点存id比存地址好，因为当位置迁移了，辅助索引还需要改，两次索引）

聚集索引
数据库表行中数据的物理顺序与键值的逻辑（索引）
顺序相同

frm 表定义文件 ibd 索引文件和数据文件存在ibd文件中

myisam 数据与索引 b+tree

frm 表定义文件  myd 数据文件  myi 索引文件  

列的离散型：

如果sex主键，去查找tree时，发现选择性很差，优化的不好，就去全表查询了，所以不好

最左匹配原则且不可跳跃

联合索引[name,phone] 特殊的联合单列索引

 最左匹配原则-离散度高原则-最少空间原则

 覆盖索引，联合索引中包含了需要所需要的字段，那查询的时候直接获取到了不需要去叶子节点查找。

 索引列的数据长度能少则少。对
索引一定不是越多越好， 越全越好， 一定是建合适的。对
匹配列前缀可用到索引 like 9999%对， like %9999%、 like %9999用不到索引；
Where 条件中 not in 和 <>操作无法使用索引；对（选择性很差）
匹配范围值， order by 也可用到索引；对
多用指定列查询， 只返回自己想到的数据列， 少用select `*`；对
联合索引中如果不是按照索引最左列开始查找， 无法使用索引；
联合索引中精确匹配最左前列并范围匹配另外一列可以用到索引；对
联合索引中如果查询中有某个列的范围查询， 则其右边的所有列都无法使用索引；select * from age > 12 and name='张三' age >12会使用索引，右边就不能使用精确索引了

 如何查看是否使用了索引

 explain select * from table where name like 'asdfasdf%';

 possible_keys:idx_name 可能的索引是name

 key：null 实际的索引为null

 取决去'asdfas%'之前的asdfas的离散型，离散型高的话，使用索引


- 体系结构

可插拔的存储引擎，每个表都可以设置存储引擎，都会使用frm表定义文件；

csv：数据的快速导入导出，表格转换成数据库

Archive:压缩协议，100万数据3M大小，insert select 自增Id,占用磁盘少，日志系统、大量的数据采集`*`

Memory、heap,临时表，存储在内存中，内存大小16M

Myisam：表级锁、不支持失误，myd和myi分别存索引数据

`*` innodb: 行级锁、ACID，聚集索引。

- 客户端服务器通讯

半双工：单条路

数据库查看连接状态：

show PROCESSLIST;

Sleep 线程正在等待客户端发送数据
Query 连接线程正在执行查询
Locked 线程正在等待表锁的释放
Sorting result 线程正在对结果进行排序
Sending data 向请求端返回数据

可通过kill {id}的方式进行连接的杀掉，释放掉没用的链接，是系统很好的运行起来


- 使用缓存

缓存问题：key-value，select语句完全一样

show variables like 'query_cache%';

query_cache_type  OFF:默认是关闭的

使用set global query_cache_type =0;关闭缓存的语句

使用set global query_cache_type =1;开启缓存的语句

使用set global query_cache_type =2;按需开启缓存的语句，在sql语句中增加关键字SQL_CACHE SQL_no_Cache关闭缓存

query_cache_limit 缓存限制 1M,超过1M将不会缓存，单次查找大小

query_cache_size:总的缓存大小

show status like 'Qcache%';查看使用缓存的状态

Qcache_hits命中缓存次数

- 优化

解析sql，最优的执行计划

怎么优化的

基于成本悬着使用key索引

in有二分查找进行优化所以使用in而不用or  O(logzn)<O(n)

- 查询计划参数详解

id:序列号，

如果id相同，执行顺序子上向下，

如果id不同，如果是子查询，id的序号会递增，id值越大优先级越高，越先被执行

id相同和不同同时存在，id如果相同认为一组，从上向下顺序执行；所有组中id越大越先执行

select_type

SIMPLE： 简单的select查询， 查询中不包含子查询或者union

PRIMARY： 查询中包含子部分， 最外层查询则被标记为primary

SUBQUERY/MATERIALIZED： SUBQUERY表示在select 或 where列表中包含了子查询

MATERIALIZED表示where 后面in条件的子查询

UNION： 若第二个select出现在union之后， 则被标记为union；

UNION RESULT： 从union表获取结果的select

<unionM,N> 由ID为M,N 查询union产生的结果

<subqueryN> 由ID为N查询生产的结果

sql查询优化中很重要的指标就是看type

system： 表只有一行记录（等于系统表） ， const类型的特例， 基本不会出现， 可以忽略不计

const： 表示通过索引一次就找到了， const用于比较primary key 或者 unique索引

eq_ref： 唯一索引扫描， 对于每个索引键， 表中只有一条记录与之匹配。 常见于主键 或 唯一索引扫描

ref： 非唯一性索引扫描， 返回匹配某个单独值的所有行， 本质是也是一种索引访问

range： 只检索给定范围的行， 使用一个索引来选择行

index： Full Index Scan， 索引全表扫描， 把索引从头到尾扫一遍

ALL： Full Table Scan， 遍历全表以找到匹配的行


possible_keys
查询过程中有可能用到的索引

key
实际使用的索引， 如果为NULL， 则没有使用索引

rows
根据表统计信息或者索引选用情况， 大致估算出找到所需的记录所需要读取的行数

filtered

它指返回结果的行占需要读到的行(rows列的值)的百分比表示返回结果的行数占需读取行数的百分比， filtered的值越大越好



十分重要的额外信息

1、 Using filesort ：

mysql对数据使用一个外部的文件内容进行了排序， 而不是按照表内的索引进行排序读取

2、 Using temporary：

使用临时表保存中间结果， 也就是说mysql在对查询结果排序时使用了临时表， 常见于order by 或 group by

3、 Using index：
表示相应的select操作中使用了覆盖索引（Covering Index） ， 避免了访问表的数据行， 效率高

4、 Using where ：
使用了where过滤条件

5、 select tables optimized away：

基于索引优化MIN/MAX操作或者MyISAM存储引擎优化COUNT(`*`)操作， 不必等到执行阶段在进行计算， 查询执行
计划生成的阶段即可完成优化

- 如何定位慢sql

业务驱动

测试驱动

慢查询日志

show variables like 'slow_query_log'

show variables like 'slow_query%';

set global slow_query_log = on
set global slow_query_log_file = '/var/lib/mysql/gupaoedu-slow.log'
set global log_queries_not_using_indexes = on  没有使用索引的记录到慢查询日志里
set global long_query_time = 0.1 (秒)  查询时间大于0.1秒的记录到慢查询日志里

日志里的信息

Time ： 日志记录的时间

User@Host： 执行的用户及主机

Query_time： 查询耗费时间 Lock_time 锁表时间 Rows_sent 发送给请求方的记录

条数 Rows_examined 语句扫描的记录条数

SET timestamp 语句执行的时间点

select .... 执行的具体语句


慢查询日志查看工具：

mysqlsla

pt-query-digest
