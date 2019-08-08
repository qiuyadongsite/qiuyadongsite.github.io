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

## 网络通信

## MYSQL
