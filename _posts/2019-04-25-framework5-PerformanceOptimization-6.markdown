---
layout: post
title:  一篇搞定mysql
date:   2019-04-25 21:52:12 +08:00
category: 性能优化
tags: mysql
comments: true
---

* content
{:toc}


mysql是大量使用的免费小型数据库，搞懂其相关概念、优化方法有助于开发。























## 概念

### 事务

这是个通用概念，体现在mysql中：

```
show variables like 'AUTOCOMMIT';//查看状态

set autocommit = 0; //设置不自动开启

设置了不自动开启使用以下命令方便测试：

BEGIN 开始一个事务

ROLLBACK 事务回滚

COMMIT 事务确认

```

也有ACID原则：

A: atomicity,原子性，一个工作单元要么全做成功，要么全部失败回滚，不允许只执行一部分操作。

C:consistency,一致性，数据库的数据从一个一致性转换到另一个一致性。理解：该事务不存在中间态，在事务前后是两种状态。用于保证数据库的完整性。

I:isolation,隔离型，一个事务所做的修改在提交之前对其他事务不可见。

D:durability,持久性，一旦事务提交，被保存到数据库就会持久化，不可能断电，系统崩溃导致数据丢失。

事务性是基础，所有安全性问题都围绕它展开。

#### 事务的隔离级别

查看隔离级别

```

SELECT @@tx_isolation

```

由于业务对数据的容忍度和性能的差别，mysql分为4个隔离级别，可供自主选择：

Read uncommitted：未提交读，可以读到事务未提交的数据，任何情况都可以发生。级别最低，性能最好，安全性最差。

Read committed:提交读，一个事务要等到另一个事务提交后才能读，可以避免脏读。

Repeatable read:可重复读，就是事务开始时不允许修改操作。可以避免脏读，不可重复读的问题。mysql默认的隔离级别。（innodb解决了此情况下的幻读问题）

Serializable:串行化，可以避免脏读、幻读、不可重复读问题。性能最差，安全性最高。

#### 脏读、不可重复读、幻读

脏读：事务a读取了事务b更新的数据，但是b回滚了，a得到的数据时脏的。

不可重复读：事务a读取了一次，b对数据进行了更新，a再读一次数据，结果不一致问题；

幻读：事务a读取了一次，b对数据进行了插入操作，由于主键不可重复，a进行插入的时候，报错发现主键重复。幻读侧重的方面是某一次的 select 操作得到的结果所表征的数据状态无法支撑后续的业务操作。具体一些：select 某记录是否存在，不存在，准备插入此记录，但执行 insert 时发现此记录已存在，无法插入，此时就发生了幻读。不可重复读侧重表达 读-读，幻读则是说 读-写，用写来证实读的是鬼影。

```

SELECT `id` FROM `users` WHERE `id` = 1 FOR UPDATE;可以解决幻读的问题

FOR UPDATE 也会对此 “记录” 加锁，InnoDB 的行锁（gap锁是范围行锁）锁定的是记录所对应的索引

```

### mysq的存储引擎

myisam:使用数据（MYD）和索引（MYI）分开存储、表级锁、不支持事务

innodb:事务、行级锁

Archive：不支持事务、占用的磁盘少，只支持select和insert

CSV:数据快速导出导入，表格直接转换成csv

#### innodb的四大特性

插入缓存、两次写、自定义哈希索引、预读；

innodb的行锁是加在索引上的

#### myisam与innodb的区别

InooDB支持事务，而MyISAM不支持事务；

InnoDB支持行级锁，而MyISAM支持表级锁；

InnoDB支持MVCC，而MyISAM不支持；

InnoDB支持外键，而MyISAM不支持；

InnoDB不支持全文索引，而MyISAM支持；

InnoDB不能通过直接拷贝表文件的方法拷贝表到另外一台机器， myisam 支持；

InnoDB表支持多种行格式， myisam 不支持；

InnoDB是索引组织表， myisam 是堆表；

myisam与innodb select count(`*`)哪个更快:myisam更快，因为myisam内部维护了一个计数器，可以直接调取。

### 数据库三大范式

第一范式：数据库表中的字段都是单一属性的，不可再分(保持数据的原子性)；严格遵循的话会造成表的列过多，b+树的单次比对数量减少，io操作频繁，浪费cpu;

第二范式：第二范式必须符合第一范式，非主属性必须完全依赖于主键。

第三范式：在满足第二范式的基础上，在实体中不存在其他实体中的非主键属性，传递函数依赖于主键属性，确保数据表中的每一列数据都和主键直接相关，而不能间接相关(表中字段[非主键]不存在对主键的传递依赖)。遵循的话造成链表查询过多。

### sql语句优化，至少五种

避免select `*`，将需要查找的字段列出来；

使用连接（join）来代替子查询；

拆分大的delete或insert语句；

使用limit对查询结果的记录进行限定；

用 exists 代替 in 是一个好的选择；

用Where子句替换HAVING 子句 因为HAVING 只会在检索出所有记录之后才对结果集进行过滤；

不要在 where 子句中的“=”左边进行函数、算术运算或其他表达式运算，否则系统将可能无法正确使用索引尽量避免在where 子句中对字段进行 null 值判断，否则将导致引擎放弃使用索引而进行全表扫描；

尽量避免在 where 子句中使用 or 来连接条件，否则将导致引擎放弃使用索引而进行全表扫描；

尽量避免在 where 子句中使用!=或<>操作符，否则将引擎放弃使用索引而进行全表扫描；

### 表结构优化

永远为每张表设置一个ID (所有建表的时候不设置主键的程序猿都应该被辞退)；

选择正确的存储引擎 ;

使用可存下数据的最小的数据类型，整型 < date,time < char,varchar < blob；

使用简单的数据类型，整型比字符处理开销更小，因为字符串的比较更复杂。如，int类型存储时间类型，bigint类型转ip函数；

使用合理的字段属性长度，固定长度的表会更快。

使用enum、char而不是varchar；

尽可能使用not null定义字段(给空字段设置默认值)；

尽量少用text;

给频繁使用和查询的字段建立合适的索引；

### 常使用的函数

sum、count 、avg、min、max

### mysql中常用的命令

Explain、describe、show、truncate

### 常用的关键字答

distinct、limit、offset、order by、union、union all、between、group by

### union、union all的区别

对重复结果的处理：UNION在进行表链接后会筛选掉重复的记录，Union All不会去除重复记录；

对排序的处理：Union将会按照字段的顺序进行排序；UNION ALL只是简单的将两个结果合并后就返回；

从效率上说，UNION ALL 要比UNION快很多

### varchar(100)和varchar(200)的区别

varchar(100)最多存放100个字符，varchar(200)最多存放200个字符，varchar(100)和(200)存储hello所占空间一样，但后者在排序时会消耗更多内存，因为order by col采用fixed_length计算col长度(memory引擎也一样)

### varchar(20)和int(20)中的20含义一样吗

不一样，前者表示最多存放20个字符，后者表示最多显示20个字符，但是存储空间还是占4字节存储，存储范围不变；

### 什么是存储过程？用什么来调用？

存储过程是一个预编译的SQL 语句，优点是允许模块化的设计，就是说只需创建一次，以后在该程序中就可以调用多次。

如果某次操作需要执行多次SQL ，使用存储过程比单纯SQL 语句执行要快。可以用一个命令对象来调用存储过程。

### 什么是触发器？触发器的作用？

触发器是一中特殊的存储过程，主要是通过事件来触发而被执行的。

它可以强化约束，来维护数据的完整性和一致性，可以跟踪数据库内的操作从而不允许未经许可的更新和变化。

可以联级运算。如，某表上的触发器上包含对另一个表的数据操作，而该操作又会导致该表触发器被触发。

### 存储过程与触发器的区别

触发器与存储过程非常相似，触发器也是SQL语句集，两者唯一的区别是触发器不能用EXECUTE语句调用，而是在用户执行Transact-SQL语句时自动触发（激活）执行。

触发器是在一个修改了指定表中的数据时执行的存储过程。通常通过创建触发器来强制实现不同表中的逻辑相关数据的引用完整性和一致性。由于用户不能绕过触发器，所以可以用它来强制实施复杂的业务规则，以确保数据的完整性。

触发器不同于存储过程，触发器主要是通过事件执行触发而被执行的，而存储过程可以通过存储过程名称名字而直接调用。当对某一表进行诸如UPDATE、INSERT、DELETE这些操作时，SQLSERVER就会自动执行触发器所定义的SQL语句，从而确保对数据的处理必须符合这些SQL语句所定义的规则。

### 索引的作用？和它的优点缺点是什么？

索引就一种特殊的查询表，数据库的搜索引擎可以利用它加速对数据的检索。

它很类似与现实生活中书的目录，不需要查询整本书内容就可以找到想要的数据。

索引可以是唯一的，创建索引允许指定单个列或者是多个列。缺点是它减慢了数据录入的速度，同时也增加了数据库的尺寸大小。


### MySQL主要的索引类型

普通索引：是最基本的索引，它没有任何限制；

唯一索引：索引列的值必须唯一，但允许有空值。如果是组合索引，则列值的组合必须唯一；

主键索引：是一种特殊的唯一索引，一个表只能有一个主键，不允许有空值；

组合索引：指多个字段上创建的索引，只有在查询条件中使用了创建索引时的第一个字段，索引才会被使用。使用组合索引时遵循最左前缀集合；

全文索引：主要用来查找文本中的关键字，而不是直接与索引中的值相比较，mysql中MyISAM支持全文索引而InnoDB不支持；

### 使用like 'a%' 、like'%a'、like'%a%'查询时是否会使用索引

'a%'会，其他两个不会；

### 使用索引注意事项

索引不会包含有NULL的列，复合索引中只要有一列含有NULL值，那么这一列对于此符合索引就是无效的；

使用短索引，对串列进行索引，如果可以就应该指定一个前缀长度；

短索引不仅可以提高查询速度而且可以节省磁盘空间和I/O操作；

mysql查询只使用一个索引，因此数据库默认排序可以符合要求的情况下不要使用排序操作，尽量不要包含多个列的排序，如果需要最好给这些列建复合索引；注意like，上文已经提到；不要在列上进行运算；

不使用NOT IN 、<>、！=操作，但<,<=，=，>,>=,BETWEEN,IN是可以用到索引的；

索引要建立在经常进行select操作的字段上；

索引要建立在值比较唯一的字段上；

对于那些定义为text、image和bit数据类型的列不应该增加索引；

在where和join中出现的列需要建立索引；

如果where字句的查询条件里使用了函数(如：where DAY(column)=…),mysql将无法使用索引；

在join操作中(需要从多个数据表提取数据时)，mysql只有在主键和外键的数据类型相同时才能使用索引，否则及时建立了索引也不会使用；

### 说一说什么是外键，优缺点

外键指的是外键约束，目的是保持数据一致性，完整性，控制存储在外键表中的数据。

使两张表形成关联，外键只能引用外表中列的值；

优点：由数据库自身保证数据一致性，完整性，更可靠，因为程序很难100％保证数据的完整性，而用外键即使在数据库服务器当机或者出现其他问题的时候，也能够最大限度的保证数据的一致性和完整性。有主外键的数据库设计可以增加ER图的可读性，这点在数据库设计时非常重要。外键在一定程度上说明的业务逻辑，会使设计周到具体全面。

缺点：可以用触发器或应用程序保证数据的完整性；过分强调或者说使用外键会平添开发难度，导致表过多，更改业务困难，扩展困难等问题；不用外键时数据管理简单，操作方便，性能高（导入导出等操作，在insert, update, delete 数据的时候更快）；

### 在什么时候你会选择使用外键

在我的业务逻辑非常简单，业务一旦确定不会轻易更改，表结构简单，业务量小的时候我会选择使用外键。

因为当不符合以上条件的时候，外键会影响业务的扩展和修改，当数据量庞大时，会严重影响增删改查的效率。


### 什么叫视图？游标是什么？

视图是一种虚拟的表，具有和物理表相同的功能；

可以对视图进行增，改，查，操作，视图通常是有一个表或者多个表的行或列的子集。

对视图的修改不影响基本表。它使得我们获取数据更容易，相比多表查询。

游标：是对查询出来的结果集作为一个单元来有效的处理。游标可以定在该单元中的特定行，从结果集的当前行检索一行或多行。可以对结果集当前行做修改。一般不使用游标，但是需要逐条处理数据的时候，游标显得十分重要。

### mysql有没有rowid？

没有，InnoDB如果没有定义主键，内部会生成一个主键编号rowid ，但是无法查询到。在平时InnoDB建表的时候我们最好自己确定主键，防止每次插入数据前数据库会去生成rowid。

### mysql怎么在查询时给查出来的数据设置一个自增的序号？

set @i=0;SELECT (@i:=@i+1) 别名 FROM table, (SELECT @i:=0) AS 别名 ;

### 如何使用explain优化sql和索引？

explain sql ;

table：显示这一行的数据是关于哪张表的；

type：这是重要的列，显示连接使用了何种类型。

从最好到最差的连接类型为const、eq_reg、ref、range、index和ALL；

all: full table scan ;MySQL将遍历全表以找到匹配的行；

index ： index scan;

 index 和 all的区别在于index类型只遍历索引；

 range：索引范围扫描，对索引的扫描开始于某一点，返回匹配值的行，常见与between ，< ,>等查询；

 ref：非唯一性索引扫描，返回匹配某个单独值的所有行，常见于使用非唯一索引即唯一索引的非唯一前缀进行查找；

 eq_ref：唯一性索引扫描，对于每个索引键，表中只有一条记录与之匹配，常用于主键或者唯一索引扫描；

 const，system：当MySQL对某查询某部分进行优化，并转为一个常量时，使用这些访问类型；如果将主键置于where列表中，MySQL就能将该查询转化为一个常量；

 possible_keys：显示可能应用在这张表中的索引；如果为空，没有可能的索引；可以为相关的域从WHERE语句中选择一个合适的语句；

 key： 实际使用的索引；如果为NULL，则没有使用索引；

 很少的情况下，MySQL会选择优化不足的索引；这种情况下，可以在SELECT语句中使用USE INDEX(indexname)来强制使用一个索引或者用IGNORE INDEX(indexname)来强制MySQL忽略索引key_len：使用的索引的长度；在不损失精确性的情况下，长度越短越好；

 ref：显示索引的哪一列被使用了，如果可能的话，是一个常数；

 rows：MySQL认为必须检查的用来返回请求数据的行数；

 Extra：关于MySQL如何解析查询的额外信息；

## 实践

###
