---
layout: post
title:  keys
date:   2019-08-08 20:52:12 +08:00
category: 归零
tags: 代码优化
comments: true
---

* content
{:toc}


keys!










## HTTP

请求头：

方法 url 协议版本

Accept：浏览器可接受的MIME类型。
Accept-Charset：浏览器可接受的字符集。
Accept-Language：浏览器所希望的语言种类，当服务器能够提供一种以上的语言版本时要用到。
Connection：表示是否需要持久连接。如果Servlet看到这里的值为“Keep-Alive”。
Content-Length：表示请求消息正文的长度。
Cookie：这是最重要的请求头信息之一。
Host：初始URL中的主机和端口。
If-Modified-Since：只有当所请求的内容在指定的日期之后又经过修改才返回它，否则返回304“Not Modified”应答。
Pragma：指定“no-cache”值表示服务器必须返回一个刷新后的文档，即使它是代理服务器而且已经有了页面的本地拷贝。
Referer：包含一个URL，用户从该URL代表的页面出发访问当前请求的页面。

响应头：

协议版本 状态码 状态码释义

Allow： 服务器支持哪些请求方法（如GET、POST等）。
Content-Encoding： 文档的编码（Encode）方法。只有在解码之后才可以得到Content-Type头指定的内容类型。
Content-Length： 表示内容长度。
Content-Type： 表示后面的文档属于什么MIME类型。Servlet默认为text/plain，但通常需要显式地指定为text/html。
Date： 当前的GMT时间。你可以用setDateHeader来设置这个头以避免转换时间格式的麻烦。
Expires： 应该在什么时候认为文档已经过期，从而不再缓存它？
Last-Modified： 文档的最后改动时间。客户可以通过If-Modified-Since请求头提供一个日期，该请求将被视为一个条件GET，只有改动时间迟于指定时间的文档才会返回，否则返回一个304（Not Modified）状态。Last-Modified也可用setDateHeader方法来设置。
Location： 表示客户应当到哪里去提取文档。Location通常不是直接设置的，而是通过HttpServletResponse的sendRedirect方法，该方法同时设置状态代码为302。
Refresh： 表示浏览器应该在多少时间之后刷新文档，以秒计。
Set-Cookie 设置和页面关联的Cookie。Servlet不应使用response.setHeader("Set-Cookie", ...)，而是应使用HttpServletResponse提供的专用方法addCookie。
WWW-Authenticate：客户应该在Authorization头中提供什么类型的授权信息？在包含401（Unauthorized）状态行的应答中这个头是必需的。


204：	No Content	无内容。服务器成功处理，但未返回内容。在未更新网页的情况下，可确保浏览器继续显示当前文档
206：	Partial Content	部分内容。服务器成功处理了部分GET请求
301：	Moved Permanently	永久移动。请求的资源已被永久的移动到新URI，返回信息会包括新的URI，浏览器会自动定向到新URI。今后任何新的请求都应使用新的URI代替
304：	Not Modified	未修改。所请求的资源未修改，服务器返回此状态码时，不会返回任何资源。客户端通常会缓存访问过的资源，通过提供一个头信息指出客户端希望只返回在指定日期之后修改的资源
403：	Forbidden	服务器理解请求客户端的请求，但是拒绝执行此请求
405：	Method Not Allowed	客户端请求中的方法被禁止
415：	Unsupported Media Type	服务器无法处理请求附带的媒体格式
500：	Internal Server Error	服务器内部错误，无法完成请求
503：	Service Unavailable	由于超载或系统维护，服务器暂时的无法处理客户端的请求。延时的长度可包含在服务器的Retry-After头信息中
504:	Gateway Time-out	充当网关或代理的服务器，未及时从远端服务器获取请求

## 分布式

CAP:Consistency （一致性，即保证事务）、Availability（可用性，保证可用）Partition tolerance（分区容错无法避免），三者不可同时满足。

ACID:Atomicity（原子性：一个事务中所有操作都必须全部完成，要么全部不完成）、Consistency（一致性. 在事务开始或结束时，数据库应该在一致状态）、Isolation(隔离层. 事务将假定只有它自己在操作数据库，彼此不知晓)、Durability（ 一旦事务完成，就不能返回）。关系数据库的ACID模型拥有 高一致性 + 可用性，但很难进行分区。

BASE:反ACID模型，完全不同ACID模型，牺牲高一致性，获得可用性或可靠性。Basically Available基本可用。支持分区失败(e.g. sharding碎片划分数据库),Soft state软状态 状态可以有一段时间不同步，异步。Eventually consistent最终一致，最终数据是一致的就可以了，而不是时时高一致。

限流策略：计数器（简单粗暴约定qps,设置每秒有多少请求其余的拒绝引发突刺效应，1秒钟内某达到请求以后的时间无法处理任务）、漏桶（从消息队列中取任务，不管任务生产的多快，消费者的速度得到保障，无法短时间内的突发流量）、令牌桶（可以准备一个队列，用来保存令牌，另外通过一个线程池定期生成令牌放到队列中，每来一个请求，就从队列中获取一个令牌，并继续执行）等单机限流、计数器存放redis(使用incr原子增)分布式限流。

负载均衡算法：轮询、加权轮询、随机、加权随机、源地址哈希法、最少连接数、最佳性能（依赖滑动窗口）等。

## 高并发

AQS:AbstractQueuedSynchronizer(抽象的队列式的同步器),定义了一套多线程访问共享资源的同步器框架。

CAS:比较和交换，用于实现多线程同步的原子指令。

Future：通过实现Callback接口，并用Future可以来接收多线程的执行结果。

## JVM

线程共享：堆、方法区

线程私有：本地方法区、栈、程序寄存器

栈：存放局部变量表、动态链接、返回地址、操作数栈等

堆：存放对象实例、GC的主要区域GC堆，分配内存和数组。

方法区：类信息、常量、静态变量、及时编译器编译后的代码。永久代和元空间就是方法区的实现。

运行时常量池：是方法区的一部分，class文件中除了类的版本、字段、方法、接口等描述信息外还有一些字面量和符号引用。

直接内存：它不是虚拟机运行时数据区的一部分。如nio类，引入通道IO的方式，通过native方法直接分配堆外内存，通过DirectByteBuffer对象对这块内存进行操作。

G1垃圾回收器：将整个Java堆划分多个大小相等的独立区域Region，Region之间的对象引用，都有一个对应Remembered set。

dump日志：`-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=D:\study\log_hprof\gc.hprof -XX:+PrintGCDetails -Xloggc:D:\study\log_gc\gc.log gc.GcTest`,然后使用Eclipse Memory Analyzer tool分析。

## 网络通信

同步:就是一个任务的完成需要依赖另外一个任务时，只有等待被依赖的任务完成后，依赖的任务才能算完成，这是一种可靠的任务序列。

异步：是不需要等待被依赖的任务完成，只是通知被依赖的任务要完成什么工作，依赖的任务也立即执行，只要自己完成了整个任务就算完成了。至于被依赖的任务最终是否真正完成，依赖它的任务无法确定，所以它是不可靠的任务序列。

阻塞：就是 CPU 停下来等待一个慢的操作完成 CPU 才接着完成其它的事。

非阻塞：就是在这个慢的操作在执行时 CPU 去干其它别的事，等这个慢的操作完成时，CPU 再接着完成后续的操作。

BIO:同步阻塞，在读入输入流或者输出流时，在读写动作完成之前，线程会一直阻塞在那里，它们之间的调用时可靠的线性顺序。

NIO:通过Channel、Selector、Buffer 等新的抽象，可以构建多路复用的、同步非阻塞 IO 程序，同时提供了更接近操作系统底层高性能的数据操作方式。

AIO:异步 IO 是基于事件和回调机制实现的，也就是应用操作之后会直接返回，不会堵塞在那里，当后台处理完成，操作系统会通知相应的线程进行后续的操作。

Netty:是一个异步事件驱动的网络应用框架，用于快速开发可维护的高性能服务器和客户端。

阻塞VS非阻塞：人是否坐在水壶前面一直等。

同步VS异步：水壶是不是在水烧开之后主动通知人。

Selector：一般称 为选择器 ，当然你也可以翻译为 多路复用器 。它是Java NIO核心组件中的一个，用于检查一个或多个NIO Channel（通道）的状态是否处于可读、可写。如此可以实现单线程管理多个channels,也就是可以管理多个网络链接。使用select实现Reactor。

Reactor：负责响应IO事件，当检测到一个新的事件，将其发送给相应的Handler去处理。为单个线程，需要处理accept连接，同时发送请求到处理器中。对于多个CPU的机器，为充分利用系统资源，将Reactor拆分为两部分。mainReactor负责监听连接，accept连接给subReactor处理，为什么要单独分一个Reactor来处理监听呢？因为像TCP这样需要经过3次握手才能建立连接，这个建立连接的过程也是要耗时间和资源的，单独分一个Reactor来处理，可以提高性能。

## MYSQL

Innodb:支持事务ACID,行级锁，以聚集索引存储数据。

Myisam:表级锁、不支持事务。1

并发带来的问题：脏读、可重复读、幻读

四种事务级别：未提交读、提交读、可重复读、串行化

锁：共享锁（读锁）、排他锁（写锁）、意向共享锁（表读锁）、意向排他锁（表写锁）、记录锁、间隙锁、临界锁

MVVC:并发多版本控制，为了提高效率，避免写的时候，无法读取引起的并发问题。

undo log：以撤销为目的，undo的数据可作为数据旧版本快照提供其他并发事务进行快照读。

redo log：事务执行过程中就开始写入redo,以恢复为目的，为了实现事务的持久化而出现的产物。

执行计划：select_type(simple、primary、SUBQUERY、MATERIALIZED、UNION)、type(system、const、eq_ref、ref、range、index、All);

Innodb_flush_log_at_trx_commit:mysql提交持久化策略。

定位慢查询：将默认10s改为set long_query_time=1的大于1s记录日志，slow_query_log_file 设置log日志位置，使用mysqldumpslow（自带工具），pt-query-digest分析慢查询日志。
