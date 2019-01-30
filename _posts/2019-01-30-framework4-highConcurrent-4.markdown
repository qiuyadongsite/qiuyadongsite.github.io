---
layout: post
title:  缓存技术-MongoDB
date:   2019-01-30 23:52:12 +08:00
category: 高并发分布式
tags: 多线程
comments: true
---

* content
{:toc}

MongoDB 是一个基于分布式文件存储的数据库。是一个介于关系数据库和非关系数据库之间的产品，是非关系数据库当中功能最丰富，最像关系数据库的。












## 非关系型数据库(NOSQL:Not Only SQL)

用于超大规模数据的存储,这些类型的数据存储不需要固定的模式，无需多余操作就可以横向扩展。

- 关系型数据库与NOSQL对比

| 关系型数据    | NoSQL     |
| :------------- | :------------- |
| 数据库高度组织化结构化数据     | 代表着不仅仅是 SQL       |
| 结构化查询语言（SQL）     | 没有声明性查询语言      |
| 数据和关系都存储在单独的表中    | 没有预定义的模式       |
| 数据操作语言，数据定义语言     | 键-值对存储，列存储，文档存储，图形数据库       |
| 严格的一致性     | 最终一致性，而非 ACID 属性      |
| 基础事务    | 非结构化和不可预知的数据CAP 定理高性能，高可用性和可伸      |

- 非关系型数据库分类

| 类型    | 典型代表    |概念    |
| :------------- | :------------- | :------------- |
| 列存储      | Hbase       |顾名思义，是按照列存储数据的。最大的特点是方便存储结构化和半结构化的数据，方便做数据压缩，对针对某一列或者某几列的查询有非常大的 IO 优势    |
| 文档存储     | MongoDB       |文档存储一般用类似 json 的格式存储，存储的内容是文档型的。这样也就有机会对某些字段建立索引，实现关系数据库的某些功能。   |
| Key-value 存储 | Memcache/Redis      |可以通过key快速查询到其value 。一般来说 ，存储不管value 的格式，照单全收。(Redis包含了其他功能)   |

- MongoDB 的数据结构与关系型数据库数据结构对比

| 关系型数据库术语/概念   | MongoDB 术语/概念    |解释/说明    |
| :------------- | :------------- | :------------- |
| Database |  Database |  数据库 |  
|  Table |   Collection |   数据库表/集合 |  
|  Row   |   Document  |  数据记录行/文档 |  
|  Column |   Field |   数据列/数据字段 |  
|  Index |   Index  |  索引 |  
|  Table joins |    |  表关联/MongoDB 不支持 |  
|  Primary Key |   Object ID |  主键/MongoDB 自动将_id 设置为主键 |  

## MongoDB底层原理

MongoDB 的集群部署方案中有三类角色：`实际数据存储结点`、`配置文件存储结点`和`路由接入结点`。

连接的客户端直接与路由结点相连，从配置结点上查询数据，根据查询结果到实际的存储结点上查询和存储数据。MongoDB 的部署方案有单机部署、复本集（主备）部署、分片部署、复本集与分片混合部署。混合的部署方式如图：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/mongodb1.png)

对于复本集，又有主和从两种角色，写数据和读数据也是不同，写数据的过程是只写到主结点中，由主
结点以异步的方式同步到从结点中,而读数据则只要从任一结点中读取，具体到哪个结点读取是可以指定的;

对于 MongoDB 的分片，假设我们以某一索引键（ID）为片键，ID 的区间[0,50]，划分成 5 个 chunk，分别存储到 3 个片服务器中，如图所示：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/sharekey.png)

假如数据量很大，需要增加片服务器时可以只要移动 chunk 来均分数据即可。

路由结点：

路由角色的结点在分片的情况下起到负载均衡的作用。

## MongoDB 的应用场景和不适用场景

- 适用场景

1）网站实时数据:MongoDB 非常适合实时的插入，更新与查询，并具备网站实时数据存储所需的复制及高度伸缩性。

2）数据缓存:由于性能很高，MongoDB 也适合作为信息基础设施的缓存层。在系统重启之后，由 MongoDB搭建的持久化缓存层可以避免下层的数据源过载。

3）大尺寸、低价值数据存储:使用传统的关系型数据库存储一些数据时可能会比较昂贵，在此之前，很多时候程序员往往会选择传统的文件进行存储。

4）高伸缩性场景:MongoDB 非常适合由数十或数百台服务器组成的数据库。MongoDB 的路线图中已经包含对 MapReduce 引擎的内置支持。

5）对象或 JSON 数据存储:MongoDB 的 BSON 数据格式非常适合文档化格式的存储及查询。

- 不适用场景

1）高度事务性系统:例如银行或会计系统。传统的关系型数据库目前还是更适用于需要大量原子性复杂事务的应用程序。

2）传统的商业智能应用:针对特定问题的 BI 数据库会对产生高度优化的查询方式。对于此类应用，数据仓库可能是更合适的选择。

3）需要复杂 SQL 查询的问题。

## MongoDB 基本配置及常用命令

- 安装注意
  - 取消掉Install MongoDB Compass
  - 配置到path中

- 配置注意
  - mongod.cfg

  ```
  storage:
  dbPath: D:\mongoDB\data\db

  systemLog:
  destination: file
  logAppend: true
  path:  D:\mongoDB\data\log\mongod.log
  ```

- 这里使用 RoboMongo 客户端管理

- 常用命令

  - 创建数据库

  ```
  use testdb
  ```
  - 创建表（集合）

  ```
  db.createCollection("runoob")
  ```
   - 插入

   ```
   db.COLLECTION_NAME.insert(document)
   ```
   - 更新

   ```
   db.col.insert({
    title: 'MongoDB 教程',
    description: 'MongoDB 是一个 Nosql 数据库',
    by: '菜鸟教程',
    url: 'http://www.runoob.com',
    tags: ['mongodb', 'database', 'NoSQL'],
    likes: 100
   })

   db.col.update({'title':'MongoDB 教程'},{$set:{'title':'MongoDB'}})
   ```

   - 查询

   ```
      db.col.find().pretty();

      pretty() 方法以格式化的方式来显示所有文档。

      db.col.find({"by":"菜鸟教程", "title":"MongoDB 教程"});

      //分页
      db.t_member.find({},{_id:0,name:1,age:1}).limit(5).skip(3).sort({age:
1})
   
   ```

   - 删除

   ```
   ＃删除满足条件的第一条 只删除数据 不删除索引
    db.t_member.remove({age:1})
   ＃删除集合
    db.t_member.drop();
   ＃删除数据库
    db.dropDatabase();

   ```
