---
layout: post
title:  Mysql2
date:   2019-07-02 20:52:12 +08:00
category: 归零
tags: 性能优化
comments: true
---

* content
{:toc}


Mysql！











## 学习笔记

mysql中如何开启事务：

begin / start transaction -- 手工

commit / rollback -- 事务提交或回滚

set session autocommit = on/off; -- 设定事务是否自动开启

JDBC 编程：

connection.setAutoCommit（boolean）;

Spring 事务AOP编程：

expression=execution（com.gpedu.dao.*.*(..)）

Read Uncommitted（未提交读） --未解决并发问题
事务未提交对其他事务也是可见的，脏读（dirty read）
Read Committed（提交读） --解决脏读问题
一个事务开始之后，只能看到自己提交的事务所做的修改，不可重复读（nonrepeatable
read）
Repeatable Read (可重复读) --解决不可重复读问题
在同一个事务中多次读取同样的数据结果是一样的，这种隔离级别未定义解决幻读的问题
Serializable（串行化） --解决所有问题
最高的隔离级别，通过强制事务的串行执行

锁是用于管理不同事务对共享资源的并发访问



表锁与行锁的区别：
锁定粒度：表锁 > 行锁
加锁效率：表锁 > 行锁
冲突概率：表锁 > 行锁
并发性能：表锁 < 行锁

InnoDB存储引擎支持行锁和表锁（另类的行锁）

共享锁（行锁）：Shared Locks
排它锁（行锁）：Exclusive Locks
意向锁共享锁（表锁）：Intention Shared Locks
意向锁排它锁（表锁）：Intention Exclusive Locks
自增锁：AUTO-INC Locks

共享锁:
又称为读锁， 简称S锁， 顾名思义， 共享锁就是多个事务对于同一数据可以共享一把锁，
都能访问到数据， 但是只能读不能修改;
加锁释锁方式：
select * from users WHERE id=1 LOCK IN SHARE MODE;
commit/rollback

排他锁:
又称为写锁， 简称X锁， 排他锁不能与其他锁并存， 如一个事务获取了一个数据行的排他
锁， 其他事务就不能再获取该行的锁（共享锁、 排他锁） ， 只有该获取了排他锁的事务是可以对
数据行进行读取和修改， （其他事务要读取数据可来自于快照）
加锁释锁方式：
delete / update / insert 默认加上X锁
SELECT * FROM table_name WHERE ... FOR UPDATE
commit/rollback

InnoDB的行锁是通过给索引上的索引项加锁来实现的。、

只有通过索引条件进行数据检索，InnoDB才使用行级锁，否则，InnoDB
将使用表锁（锁住索引的所有记录）
表锁：lock tables xx read/write；

意向共享锁(IS)
表示事务准备给数据行加入共享锁， 即一个数据行加共享锁前必须先取得该表的IS锁，意向共享锁之间是可以相互兼容的

意向排它锁(IX)
表示事务准备给数据行加入排他锁， 即一个数据行加排他锁前必须先取得该表的IX锁，意向排它锁之间是可以相互兼容的

意向锁(IS、 IX)是InnoDB数据操作之前自动加的， 不需要用户干预意义：

当事务想去进行锁表时， 可以先判断意向锁是否存在， 存在时则可快速返回该表不能启用表锁

针对自增列自增长的一个特殊的表级别锁
show variables like 'innodb_autoinc_lock_mode';
默认取值1， 代表连续， 事务未提交ID永久丢失

按照锁算法类型分为：

Next-key locks=Gap locks+Record locks

Next-key locks：临键锁
锁住记录+区间（左开右闭）type=range
当sql执行按照索引进行数据的检索时,查询条件为范围查找（between and、 <、 >等） 并有数
据命中则此时SQL语句加上的锁为Next-key locks， 锁住索引的记录+区间（左开右闭）,为啥使用临键锁的主要原因就是解决幻读问题，innodb使用的b+s树天然是有序的，所以将相邻的区间锁住解决幻读问题，插不进去了，幻读就是数据行多少的问题，命中行以及相邻区间

Gap locks：当命中的时候使用临键锁，当命不中的时候，临建锁退化成了间隙锁，只在rr中存在
锁住数据不存在的区间（左开右开）
当sql执行按照索引进行数据的检索时， 查询条件的数据不存在， 这时SQL语句加上的锁即为
Gap locks， 锁住索引不存在的区间（左开右开）

Record locks：记录锁type=eq_ref
锁住具体的索引项
当sql执行按照唯一性（Primary key、 Unique key） 索引进行数据的检索时， 查询条件等值匹
配且查询的数据是存在， 这时SQL语句加上的锁即为记录锁Record locks， 锁住具体的索引项。如果是普通键查找的时候会将临界的两个区间都锁住


- 如何解决脏读问题呢？

就是读的时候不允许别人修改

在select的时候加上s锁

- 如何解决不可重复读

同样就是读的时候不允许别人修改

在select的时候加上s锁

- 如何解决幻读问题呢？

在select上加上Next-key locks

- 死锁

多个并发事务（2个或者以上）；

每个事务都持有锁（或者是已经在等待锁）;

每个事务都需要再继续持有锁；

事务之间产生加锁的循环等待，形成死锁

有向无环图

- 如何避免死锁

1）类似的业务逻辑以固定的顺序访问表和行。

2）大事务拆小。大事务更倾向于死锁，如果业务允许，将大事务拆小。

3）在同一个事务中，尽可能做到一次锁定所需要的所有资源，减少死锁概
率。

4）降低隔离级别，如果业务允许，将隔离级别调低也是较好的选择

5）为表添加合理的索引。可以看到如果不走索引将会为表的每一行记录添加上锁（或者说是表锁）

查看mysql的设置的事务隔离级别

select global.@@tx_isolation; select @@tx_isolation;

默认是REPEATABLE-READ级别

- MVCC 多版本控制

先读的不会出现问题

后读发现出现了不可重复读问题

- 不是出现在mvcc的问题，那么问题出在哪里呢？

脏读，不可重复读都不是通过mvcc处理的

Undo Log 是什么：

undo意为取消， 以撤销操作为目的， 返回指定某个状态的操作

undo log指事务开始之前， 在操作任何数据之前,首先将需操作的数据备份到一个地方 (Undo Log)

UndoLog是为了实现事务的原子性而出现的产物

Undo Log实现事务原子性：

事务处理过程中如果出现了错误或者用户执行了 ROLLBACK语句,Mysql可以利用Undo Log中的备份
将数据恢复到事务开始之前的状态

UndoLog在Mysql innodb存储引擎中用来实现多版本并发控制

Undo log实现多版本并发控制：

事务未提交之前， Undo保存了未提交之前的版本数据， Undo 中的数据可作为数据旧版本快照供
其他并发事务进行快照读


快照读：解决幻读问题，mvcc

SQL读取的数据是快照版本， 也就是历史版本， 普通的SELECT就是快照读
innodb快照读， 数据的读取将由 cache(原本数据) + undo(事务修改过的数据) 两部分组成

当前读：解决可重复读,锁的机制

SQL读取的数据是最新版本。 通过锁机制来保证读取的数据无法通过其他事务进行修改
UPDATE、 DELETE、 INSERT、 SELECT … LOCK IN SHARE MODE、 SELECT … FOR UPDATE都是当前读


Redo Log 是什么：
Redo， 顾名思义就是重做。 以恢复操作为目的， 重现操作；
Redo log指事务中操作的任何数据,将最新的数据备份到一个地方 (Redo Log)
Redo log的持久：
不是随着事务的提交才写入的， 而是在事务的执行过程中， 便开始写入redo 中。 具体
的落盘策略可以进行配置
RedoLog是为了实现事务的持久性而出现的产物
Redo Log实现事务持久性：
防止在发生故障的时间点， 尚有脏页未写入磁盘， 在重启mysql服务的时候， 根据redo
log进行重做， 从而达到事务的未入磁盘数据进行持久化这一特性。

指定Redo log 记录在{datadir}/ib_logfile1&ib_logfile2 可通过innodb_log_group_home_dir 配置指定
目录存储
一旦事务成功提交且数据持久化落盘之后， 此时Redo log中的对应事务数据记录就失去了意义， 所
以Redo log的写入是日志文件循环写入的
指定Redo log日志文件组中的数量 innodb_log_files_in_group 默认为2
指定Redo log每一个日志文件最大存储量innodb_log_file_size 默认48M
指定Redo log在cache/buffer中的buffer池大小innodb_log_buffer_size 默认16M
Redo buffer 持久化Redo log的策略， Innodb_flush_log_at_trx_commit：
取值 0 每秒提交 Redo buffer --> Redo log OS cache -->flush cache to disk[可能丢失一秒内
的事务数据]
取值 1 默认值， 每次事务提交执行Redo buffer --> Redo log OS cache -->flush cache to disk
[最安全， 性能最差的方式]
取值 2 每次事务提交执行Redo buffer --> Redo log OS cache 再每一秒执行 ->flush cache to
disk操作，推荐使用，mysql挂了，最后一次事务失败，系统挂了，1秒钟事务丢失

- 配置优化


最大连接数配置

max_connections

系统句柄数配置

/etc/security/limits.conf

ulimit -a

mysql句柄数配置

/usr/lib/systemd/system/mysqld.service

每一个connection内存参数配置：
sort_buffer_size connection排序缓冲区大小
建议256K(默认值)-> 2M之内
当查询语句中有需要文件排序功能时， 马上为connection分配配置的内
存大小
join_buffer_size connection关联查询缓冲区大小
建议256K(默认值)-> 1M之内
当查询语句中有关联查询时， 马上分配配置大小的内存用这个关联查
询， 所以有可能在一个查询语句中会分配很多个关联查询缓冲区
上述配置4000连接占用内存：
`4000*(0.256M+0.256M) = 2G`

`Innodb_buffer_pool_size`
innodb buffer/cache的大小（默认128M）
Innodb_buffer_pool

数据缓存

索引缓存

缓冲数据

内部结构

大的缓冲池可以减小多次磁盘I/O访问相同的表数据以提高性能

参考计算公式：

Innodb_buffer_pool_size = （总物理内存 - 系统运行所用 - connection 所用） * 90%

wait_timeout
服务器关闭非交互连接之前等待活动的秒数

innodb_open_files
限制Innodb能打开的表的个数

innodb_write_io_threads
innodb_read_io_threads
innodb使用后台线程处理innodb缓冲区数据页上的读写 I/O(输入输出)请求

innodb_lock_wait_timeout
InnoDB事务在被回滚之前可以等待一个锁定的超时秒数

https://www.cnblogs.com/wyy123/p/6092976.html 常见配置的帖子

- 数据库表设计

简单一点：

1， 每一列只有一个单一的值， 不可再拆分,,,将建立太多的列，可以放json

2， 每一行都有主键能进行区分

3， 每一个表都不包含其他表已经包含的非主键信息。,,,太多表的关联关系，订单表中冗余用户名的字段
