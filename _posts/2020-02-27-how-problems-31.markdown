---
layout: post
title:  mysql2
date:   2020-02-27 20:53:12 +08:00
category: 签到系列
tags: mysql
comments: true
---

* content
{:toc}



签到29！



## 学习总结

去年今日此门中，人面桃花相映红。 

人面不知何处去，桃花依旧笑春风。 

## 1、什么是数据库事务？

项目中配置事务，开启事务，方法上加注解，配置切面；

**事务**

是数据库管理系统中执行过程中的一个单元，由有限的数据库操作序列组成；InnoDB支持事务；

ACID:

- A：atomicity原子性，要么成功，要么失败；
- C：Consistent一致性，数据库的完整性，操作完成之后，数据不能丢失或错误；
- I：Isolation隔离性,多个事务共同操作时，互相不干扰；
- D：Durable持久性，只要提交成功，结果就永久保存；

ACID最终都是实现一致性；

**查询参数**

```
select version();//版本5.7
show variables like '%engine%';//innodb
show global variables like '%tx_isolation%';//RR隔离级别
show  variables like '%autocommit';//on，默认自动开启事务
```

**开启事务**

修改autocommit可以将提交事务改为手动方式；

使用begin/start transaction开始，以commit或者roll back结束；

**多个事务带来的问题**

A事务进行两次读，中间穿插了B事务的操作

- 脏读

  B事务进行了更新操作，但是没有提交事务，A再进行的读取操作；B回滚，那么A读取的是修改后的值，由于其他事务没有提交导致跟第一次不一致；脏读；

- 不可重复读

  B事务进行了更新操作，并且提交了，A读取的数据跟第一次读取的不一致；不可重复读；

- 幻读

  B事务插入了数据，并且提交了，A读取的数据多了几条数据；幻读；区别于不可重复度，一个是更新操作，一个是插入或者删除操作；

这三个问题导致了读不一致现象，在一个事务里两次读不一样的现象，mysql提供了不同的隔离级别来帮我们解决；

**隔离级别**

- 未提交读（read uncommitted）

  没有解决任何问题，可以读取到脏数据；

- 可提交读(read committed)

  另一个事务只能读取已经提交的数据，解决了脏读问题；

- 可重复读（Repeatable Read）

  多次读取数据都是一致的，但这个级别没有定义解决幻读问题；innodb模型在rr级别解决了幻读问题；

- 串行化（Serializable）

  所有事务串行化执行，解决所有问题

**解决读一致性问题两个方案**

- LBCC

  基于锁的并发控制，lock Base Concurrency Control，读取的数据进行锁定，不允许其他事务进行修改；但是大大影响了效率；

- MVCC

  多版本并发控制，multi version Concurrency Control,如果读取，对读取的数据建立快照或者备份，下次直接从快照读取；

  那么这个快照如何创建呢？

  - 两个版本号

    每一条数据行都有两个隐藏列，创建版本号，删除版本号；

    **规则：**只能查询创建版本号到小于自己版本号的、大于自己删除版本号的数据行；所以当读取过一次后，无论是插入、还是删除，对数据都没有影响；修改是删除+插入，一样不受影响；

同时MVCC是通过undo log实现的。那么，MVCC和LBCC是协同使用的，那么LBCC怎么实现读一致性问题呢？

## 2、锁

**锁的粒度**

- 表锁

  - 意向共享锁

    只要有一行共享锁，在表上就有了意向共享锁

  - 意向排它锁

    只要有一行排它锁，在表上就有了意向排它锁

  这两个可以看两个表的标示位；

- 行锁

  - 共享锁（shared locks）

    在select语句后 ...+ lock  in share mode;

    共享锁，可以充分获取；

  - 排它锁（Exclusive locks）

    加了排他锁，就不能再加共享锁和拍它锁了；

    增删改都会自动加上排它锁，在select +for update;加上了排他锁；

**锁的算法**

- 记录锁

  record锁，主键索引或者唯一索引，锁住一行

- 间隙锁

  没有命中一行，等值与范围查询会锁住一个范围，左开右开区间

- 临建锁

  使用范围查询的时候，不仅仅命中了行，还包含了间隙，左开右闭；解决幻读问题；

## 3、隔离级别的实现

结合以上，那么各个隔离级别如何实现的呢？

- Read Uncommited(未提交读)

  不加锁

- Read Commited(已提交读)

  普通的select使用快照读，底层使用mvcc实现；

  枷锁的select使用记录锁，不使用gap锁，所以产生幻读；

- Repeatable Read(可重复读)

  普通的select使用快照读，底层使用mvvc实现

  select加+ in share mode/for update;以及更新操作使用当前读；底层使用记录锁、间隙锁、临建锁；

- Serializable(串行化)

  在select的语句中默认加上in share mode;跟update、delete互斥；

## 4、死锁

**锁释放的时机**：

commit/rollback、客户端关闭；

锁等待：

```
show variables like 'innodb_lock_wait_timeout';//查询等待锁时间
```

查看锁日志：

```
show status like 'innodb_row_lock_%';
# innodb_row_lock_current_waits;当前正在等待锁的时间
# select * from information_schema.INNODB_TRX;当前运行的所有事务；
# select * from information_schema.INNODB_LOCKS；当前出现的锁
# select * from information_schema.INNODB_LOCK_WAITS;锁等待的相应关系

```

如果事务长时间持有锁不释放，使用kill事务相应的线程ID,就是INNODB_TRX的trx_mysql_thread_id；

**避免死锁**：

1. 操作多张表，尽量以相同的顺序访问
2. 批量操作单表数据的时候，先进行排序
3. 申请足够的锁级别，尽量使用排他锁
4. 尽量使用索引访问数据，避免锁表
5. 大事务变小事务
6. 使用等值而不是范围查询，避免间隙锁对并发的影响；