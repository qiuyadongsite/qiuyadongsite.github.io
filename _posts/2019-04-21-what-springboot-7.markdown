---
layout: post
title:  HTTP状态码
date:   2019-04-21 21:52:12 +08:00
category: 疑难点
tags: 必背常识
comments: true
---

* content
{:toc}


要求。























## HTTP状态码

200 - 请求成功

301 - 资源（网页等）被永久转移到其他URL

404 - 请求的资源（网页等）不存在

500 - 内部服务器错误

## HTTP状态码分类

1** - 信息，服务器收到请求，需要请求者继续执行操作

2** - 成功，操作被成功接收并处理

3** - 重定向，需要进一步的操作以完成请求

4** - 客户端错误，请求包含语法错误或无法完成请求

5** - 服务器错误，服务器在处理请求的过程中发生了错误

详细的：

`100` ： continue ，继续，客户端继续其请求

101 ： Switching protocols,切换到更改的http协议

`200 `： ok ,请求成功一般用于get和post请求

201 ： created,请求成功并创建了新的资源

202 ： Accepted,已经接受请求，但未处理完成

203 ： Non-Authoritative information,非授权信息，请求成功，但返回的meta信息不在原始的服务器，而是一个副本

204 : No Content ,无内容。服务器成功处理，但未返回内容，在未更新网页的情况下，可确保浏览器继续显示当前文档。

205 ： Reset Content ,重置内容。服务器处理成功，用户终端应重置文档视图。可以通过该返回码清除浏览器的表单域

`206` ： Partial Content ,部分内容。服务器成功处理了部分get请求

300 ： Multiple Choices ,多种选择。请求的资源包括了多个位置，相应的可返回一个资源特征和地址列表

`301` ： Moved Permanently,永久移动。请求的资源被永久的移动到了新的URL,返回信息包括新的URI，浏览器会自动定向到新的URI。今后使用新的URI代替

302 ： FOUND,临时移动，与301类似。但是资源只是临时被移动。客户端继续使用原有的URI

303 : See Other,查看其它地址。与301类似。使用GET和POST请求查看

`304` ： Not Modified。未修改。所请求的资源未修改，服务器返回此状态时，不会返回任何资源。客户端通常会缓存访问过的资源，通过提供一个头信息指出客户端希望只返回在指定日期之后修改的资源

`305` ： Use Proxy,使用代理。所请求的资源必须通过代理访问。

306 ： Unused 已经被废弃的HTTP状态码

307 ： Temporary Redirect 临时重定向，类似302，使用Get请求重定向

`400` ： Bad Request ,客户端请求的语法错误，服务器无法理解

`401` ： Unauthorized ,请求要求用户的身份认证

402 ： Payment Required， 保留将来使用

`403` ： Forbidden ,服务器理解请求客户端的请求，但是拒绝执行此请求

`404` ： NOt Found,服务器无法根据客户端的请求找到资源。通过此代码，网站设计人员可设置“您所请求的资源无法找到”的个性页面

`405` ： Method Not Allowed，客户端请求中的方法被禁止

`406 `： Not Acceptable 服务无法根据客户端请求的内容特征完成请求

`407` ： Proxy Authentication Required 请求要求代理的身份认证，与401类似，但请求者应当使用代理进行授权

`408` ： Request Time-out 服务器等待客户端发送的请求时间过长，超时

409 ： conflict 服务器完成客户端的put请求是可能返回此代码，服务器处理请求时发生了冲突

410 ： Gone 客户端请求的资源已经不存在。410不同于404，如果资源疫情有现在被永久的删除了可使用410代码，网站设计人员可通过301代码指定资源的新位置

411 ：Length Required 服务器无法处理客户端发送的不带Content-Length的请求信息

412 ： Precondition Failed 客户端请求信息的先决条件错误

`413` ： Request Entity Too Large 由于请求的实体过大，服务器无法处理，因此拒绝了请求。为了防止客户端的连续请求，服务器可能会关闭连接。如果只是服务器暂时无法处理，则会包含一个Retry-After的相应信息

`415` ： Unsupported Media Type 服务器无法处理附带的媒体格式

`416` ： Requested range not satisfiable 客户端请求的范围无效

`417` ： Expectation Failed 服务器无法满足Expect的请求头信息

`500` ：internal Server Error 服务器内部错误，无法完成请求

`501` ： not implemented 服务器不支持请求的功能，无法完成请求

`502` ： Bad Gateway 作为网关或者代理工作的服务器城市执行请求时，从远程服务器接收到了一个无效的相应

`503 `： service unavailable 用于超载或系统维护，服务器暂时的无法处理客户端的请求。延时的长度可包含在服务器的Retry-After中

`504` ：Gateway Time-out 充当网关或代理的服务器，未及时从远端服务器获取请求

505 ： HTTP version not supported 服务不支持HTTP协议的版本，无法完成处理


`ARP 协议`:是一种用以解析地址的协议，根据通信方的 IP 地址就可以反查出对应的 MAC 地址。

`TCP 协议`:协议为了更容易传送大数据才把数据分割，而且 TCP 协议能够确认数据最终是否送达到对方。
