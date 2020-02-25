---
layout: post
title:  http1
date:   2020-02-18 20:53:12 +08:00
category: 签到系列
tags: http
comments: true
---

* content
{:toc}



签到22！



## 学习总结

古人学问无遗力，少壮工夫老始成。

 纸上得来终觉浅，绝知此事要躬行。

## 1、HTTP基本原理

当在浏览器上输入一个url之后，接下来发生了什么？

将输入的url，查找dns服务器将域名转换成ip,通过tcp传输层协议发送，http是应用层协议；

#### 协议组成

- URI

  统一资源定位符，schema://host[:port]/path/.../?[url-params]#[query-string]

- MIME Type

  服务器接收到用户的请求以后，会返回一个资源给用户浏览器，浏览器会对这个文件进行渲染；

  - 文本文件：text/html,text/plain,text/css,application/xhtml+xml
  - 图片文件：image/jpeg,image/gif,image/png
  - 视频文件：video/mpeg,video/quicktime

  两种方式来渲染文件：

  - Accept

    客户端设置

  - Content-Type

    服务端设置：rs.setContentType("application/json")

  两者不一致就无法解析；

- 状态码

  - 1XX(Informational)

    接收的请求正在处理

  - 2XX(Success)

    请求正常处理完毕

    200：一切正常

  - 3XX(Redirection)

    需要进行附加操作以完成请求

    301：永久重定向

  - 4XX(Client Error)

    服务器无法处理请求

    404：请求的资源不存在

  - 5XX(Server ERROR)

    服务器处理请求出错

    500：服务器内部错误

- 操作方式

  - POST
  - GET
  - HEAD
  - PUT
  - DELETE

完整组成：

-  请求报文

  - 起始行

    方法 URI 协议版本

  - 首部字段

    - Connection:keep-alive 
    - Content-type
    - Content-Length

  - 主体

    name=text&age=90

- 相应报文

  - 起始行

    协议版本 状态码 状态码描述

  - 首部字段

  - 主体

协议扩展

- 文件太大咋办？

  - 浏览器设置Accept-Encoding:gzip,deflate
  - 分块传输

- 每次请求都要建立链接吗？

  Http1.1之后，Connection:keep-alive，也是默认的，没有明确关闭就保持链接

## 2、HTTP特点

- 无状态

  请求和相应的状态不会保存

  - 客户端的cookie技术

    使用cookie技术，保存服务器发送的set-Cookie首部的字段；下次客户端发送请求时，自动携带cookie的值；

  - 服务器的Session技术

    客户端发送请求中如果没有sessionId,说明之前没有连接过，服务器会生成一个sessionid，并保存该sessionid和客户的对应关系，下次访问的时候检索处理对应的客户信息使用；

  