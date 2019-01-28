---
layout: post
title:  Web技术基础
date:   2019-01-24 23:52:12 +08:00
category: 微服务架构
tags: web 概念
comments: true
---

* content
{:toc}

对于服务来说， Web应用是最广泛的也是最常用的，本篇将介绍基础的web技术概念！












## HTTP协议




test
![](http://git.inspur.com/cloud-doc/vpc-doc/blob/master/media/logo.png)

- TCP/IP4层传输

![](https://github.com/qiuyadongsite/qiuyadongsite.github.io/blob/master/slfimg/SBW_createSBW01.png)


![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/tcpip4f.png)

- ARP
 是一种用以解析地址的协议，根据通信方的 IP 地址就可以反查出对应的 MAC 地址。

- TCP可靠传输

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/tcp.png)

- DNS域名解析

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/dns.png)

- 整个传输过程

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/translate9.png)

- URL（Uniform Resource Locator，统一资源定位符）

是使用 Web 浏览器等访问 Web 页面时需要输入的网页地址

- URI（Uniform Resource Locator，统一资源定位符）

用字符串标识某一互联网资源。

URI 用字符串标识某一互联网资源，而 URL表示资源的地点（互联网上所处的位置）。可见 URL是 URI 的子集。

>ps:
ftp://ftp.is.co.za/rfc/rfc1808.txt
http://www.ietf.org/rfc/rfc2396.txt
ldap://[2001:db8::7]/c=GB?objectClass?one
mailto:John.Doe@example.com
news:comp.infosystems.www.servers.unix
tel:+1-816-555-1212
telnet://192.0.2.16:80/
urn:oasis:names:specification:docbook:dtd:xml:4.1.2

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/URI3.png)

- 发送报文

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/postmessage1.png)

- 接收报文

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/resposemessage1.png)

- HTTP 是不保存状态的协议

- 推荐的方法

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/httpmethod.png)

- 持续连接

HTTP 协议的初始版本中，每进行一次 HTTP 通信就要断开一次 TCP连接。

太累了

为解决上述 TCP 连接的问题，HTTP/1.1 和一部分的 HTTP/1.0 想出了持久连接（HTTP Persistent Connections，也称为 HTTP keep-alive 或HTTP connection reuse）的方法。持久连接的特点是，只要任意一端没有明确提出断开连接，则保持 TCP 连接状态。

- 使用 Cookie 的状态管理

HTTP 是无状态协议，它不对之前发生过的请求和响应的状态进行管理。
![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/whycookeie.png)

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/hascookie1.png)

- http状态码
![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/httpstatus.png)

204 No Content

206 Partial Content

301 Moved Permanently

302 Found 临时性重定向 303 See Other

304 Not Modified  该状态码表示客户端发送附带条件的请求 2 时，服务器端允许请求访问资源，但未满足条件的情况。

400 Bad Request 该状态码表示请求报文中存在语法错

401 Unauthoriz  该状态码表示发送的请求需要有通过 HTTP 认证（BASIC 认证、DIGEST 认证）的认证信息。

403 Forbidden 该状态码表明对请求资源的访问被服务器拒绝了。

404 Not Found 该状态码表明服务器上无法找到请求的资源。除此之外，也可以在服务器端拒绝请求且不想说明理由时使用。

5XX 服务器错误

500 Internal Server Error 该状态码表明服务器端在执行请求时发生了错误。也有可能是 Web 应用存在的 bug 或某些临时的故障

503 Service Unavailable 该状态码表明服务器暂时处于超负载或正在进行停机维护，现在无法处理请求。

## HTTP首部

参数太多，当做字典使用吧

## HTTPS与HTTP

- HTTP 主要有这些不足

例举如下:

 通信使用明文（不加密），内容可能会被窃听

 不验证通信方的身份，因此有可能遭遇伪装

 无法证明报文的完整性，所以有可能已遭篡改

- HTTP+ 加密 + 认证 + 完整性保护=HTTPS

   在采用 SSL后，HTTP 就拥有了 HTTPS 的加密、证书和完整性保护这些功能.
   ![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/https1.png)

 SSL使用公开密钥加密：    

 使用两把密钥的公开密钥加密，使用一对非对称的密钥。一把叫做私有密钥（private key），另一把叫做公开密钥（public key）。顾名思
义，私有密钥不能让其他任何人知道，而公开密钥则可以随意发布，任何人都可以获得。使用公开密钥加密方式，发送密文的一方使用对方的公开密钥进行加密处理，对方收到被加密的信息后，再使用自己的私有密钥进行解密。利用这种方式，不需要发送用来解密的私有密钥，也不必担心密钥被攻击者窃听而盗走。

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/secrete0.png)

- HTTPS 采用混合加密机制

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/addsececreat1.png)

还是有可能出现问题，需要加上数字证书：
![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/secreate4.png)

- HTTP问题瓶颈

一条连接上只可发送一个请求。

请求只能从客户端开始。客户端不可以接收除响应以外的指令。

请求 / 响应首部未经压缩就发送。首部信息越多延迟越大。

发送冗长的首部。每次互相发送相同的首部造成的浪费较多。

可任意选择数据压缩格式。非强制压缩发送。

解决方案：

 1 Ajax 的解决方法

 XMLHttpRequest 的 API，通过 JavaScript 脚本语言的调用就能和服务器进行 HTTP 通信。借由这种手段，就能从已加载完毕的 Web 页面上发起请求，只更新局部页面。

 但是：有可能会导致大量请求产生。另外，Ajax 仍未解决 HTTP 协议本身存在的问题。

 2  Comet 的解决方法

 服务器端接收到请求，在处理完毕后就会立即返回响应，但为了实现推送功能，Comet 会先将响应置于挂起状态，当服务器端有内容更新时，再返回该响应。因此，服务器端一旦有更新，就可以立即反馈给客户端。

 但是：内容上虽然可以做到实时更新，但为了保留响应，一次连接的持续时间也变长了。期间，为了维持连接会消耗更多的资源。另外，Comet
也仍未解决 HTTP 协议本身存在的问题。

 3 SPDY的目标

 是在 TCP/IP 的应用层与运输层之间通过新加会话层的形式运作。同时，考虑到安全性问题，SPDY 规定通信中使用 SSL。


 希望使用 SPDY 时，Web 的内容端不必做什么特别改动，而 Web 浏览器及 Web 服务器都要为对应 SPDY 做出一定程度上的改动。有好
 几家 Web 浏览器已经针对 SPDY 做出了相应的调整。另外，Web 服务器也进行了实验性质的应用，但把该技术导入实际的 Web 网站却
 进展不佳。
 因为 SPDY 基本上只是将单个域名（ IP 地址）的通信多路复用，所以当一个 Web 网站上使用多个域名下的资源，改善效果就会受到限
 制。
 SPDY 的确是一种可有效消除 HTTP 瓶颈的技术，但很多 Web 网站存在的问题并非仅仅是由 HTTP 瓶颈所导致。对 Web 本身的速度提
 升，还应该从其他可细致钻研的地方入手，比如改善 Web 内容的编写方式等。

- WebSocket 协议

利用 Ajax 和 Comet 技术进行通信可以提升 Web 的浏览速度。但问题在于通信若使用 HTTP 协议，就无法彻底解决瓶颈问题。WebSocket
网络技术正是为解决这些问题而实现的一套新协议及 API。

推送功能

减少通信量

使用浏览器进行全双工通信的

CGI（Common Gateway Interface，通用网关接口）是指 Web 服务器在接收到客户端发送过来的请求后转发给程序的一组机制。Servlet 作为解决 CGI 问题的对抗技术 ，随 Java 一起得到了普及。

说对抗的原因在于，这个方向上已存在用 Perl 编写的 CGI，实现在 Apache HTTP Server 上内置 mod_php 模块后可运行 PHP 程序、微软主导的 ASP 等技术。

## Web 的攻击技术

- 主要的攻击分布：
![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/duikang.png)

- 主动攻击

SQL注入攻击和 OS 命令注入攻击。

- 被动攻击

是指利用圈套策略执行攻击代码的攻击模式。在被动攻击过程中，攻击者不直接对目标 Web 应用访问发起攻击。

被动攻击通常的攻击模式如下所示。

步骤 1： 攻击者诱使用户触发已设置好的陷阱，而陷阱会启动发送已嵌入攻击代码的 HTTP 请求。

步骤 2： 当用户不知不觉中招之后，用户的浏览器或邮件客户端就会触发这个陷阱。

步骤 3： 中招后的用户浏览器会把含有攻击代码的 HTTP 请求发送给作为攻击目标的 Web 应用，运行攻击代码。

步骤 4： 执行完攻击代码，存在安全漏洞的 Web 应用会成为攻击者的跳板，可能导致用户所持的 Cookie 等个人信息被窃取，
登录状态中的用户权限遭恶意滥用等后果。

- 跨站脚本攻击

跨站脚本攻击（Cross-Site Scripting，XSS）是指通过存在安全漏洞的Web 网站注册用户的浏览器内运行非法的 HTML标签或 JavaScript 进行的一种攻击。动态创建的 HTML部分有可能隐藏着安全漏洞。就这样，攻击者编写脚本设下陷阱，用户在自己的浏览器上运行时，一不小心就会受到被动攻击。

- SQL 注入攻击

会执行非法 SQL 的 SQL 注入攻击SQL注入（SQLInjection）是指针对 Web 应用使用的数据库，通过运行非法的 SQL而产生的攻击。该安全隐患有可能引发极大的威胁，有时会直接导致个人信息及机密信息的泄露。

- OS 命令注入攻击

指通过 Web 应用，执行非法的操作系统命令达到攻击的目的。只要在能调用 Shell 函数的地方就有存在被攻击的风险。

- HTTP 首部注入攻击

是指攻击者通过在响应首部字段内插入换行，添加任意响应首部或主体的一种攻击。%0D%0A 代表 HTTP 报文中的换行符

- HTTP 响应截断攻击

要将两个 %0D%0A%0D%0A 并排插入字符串后发送。利用这两个连续的换行就可作出 HTTP 首部与主体分隔所需的空行了，这样就能显示伪造的主体，达到攻击目的。这样的攻叫做 HTTP 响应截断攻击

- 邮件首部注入攻击

是指 Web 应用中的邮件发送功能，攻击者通过向邮件首部 To 或 Subject 内任意添加非法内容发起的攻击。利用存在安全漏洞的 Web 网站，可对任意邮件地址发送广告邮件或病毒邮件。

%0D%0A 在邮件报文中代表换行符。一旦咨询表单所在的 Web应用接收了这个换行符，就可能实现对 Bcc 邮件地址的追加发送，而这原本是无法指定的。

- 目录遍历攻击

是指对本无意公开的文件目录，通过非法截断其目录路径后，达成访问目的的一种攻击。这种攻击有时也称为路径遍历（Path Traversal）攻击。用户可使用 .../ 等相对路径定位到 /etc/passed 等绝对路径上，因此服务器上任意的文件或文件目录皆有可能被访问到。

- 远程文件包含漏洞

是指当部分脚本内容需要从其他文件读入时，攻击者利用指定外部服务器的 URL充当依赖文件，让脚本读取之后，就可运行任意脚本的一种攻击。

- 会话劫持

指攻击者通过某种手段拿到了用户的会话 ID，并非法使用此会话 ID 伪装成用户，达到攻击的目的

- DoS 攻击（Denial of Service attack）

是一种让运行中的服务呈停止状态的攻击。有时也叫做服务停止攻击或拒绝服务攻击。DoS 攻击的对象不仅限于 Web 网站，还包括网络设备及服务器等。

多台计算机发起的 DoS 攻击称为 DDoS 攻击（Distributed Denial of Service attack）。DDoS 攻击通常利用那些感染病毒的计算机作为攻击者的攻击跳板。
