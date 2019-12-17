---
layout: post
title:  Netty
date:   2019-12-17 20:52:12 +08:00
category: 签到系列
tags: 高并发分布式
comments: true
---

* content
{:toc}


签到3！






## 主要概念

### 阻塞和非阻塞

当进程访问需要的数据的时候，数据是否准备就绪反应的一种方式；

阻塞体现在一直等待直到数据准备好，非阻塞无论是否准备好，都会返回一定的信息；

### 同步和异步

应用系统和操作系统处理io事件所采用的方式；

同步：应用系统直接参与到操作系统的处理io事件中，或等待或轮询，直到此io事件完成；

异步：应用系统将io事件发送给操作系统处理，自己可以干自己的事情，等待通知就行了；

### BIO 和 NIO

BIO是一种面向流的阻塞io,NIO是一种面向缓冲的非阻塞io(使用轮询机制)；

NIO使用与连接数多且数据少的场景，如聊天系统；

BIO适用于连接数少且数据量多的场景；

## NIO

实现NIO需要的三大利器：选择器Selector、缓存区Buffer、通道Channel;

- Buffer 可以当成一个数组所有的读写都是先读写到该buffer,而传统io直接面向stream

1、使用position、limit、capacity三个游标来操作该数组；
2、使用allocate来分配一个大小的缓存区，也可以使用wrap来包装一个数组使其变成相应的buffer
3、可以显示用position和limit表示当前缓存的位置，然后使用slice来分片，分片和父类的buffer数据是共享的
4、可以设置某个缓存区是只读的
5、避免缓冲区内容拷贝到中间缓存区，或者从中间缓存区写，只需要调用allocateDirect(),其他用法不变
6、内存映射，快速读写文件RandomAccessFile

- Selector 我们把事件（SelectionKey）注册的地方，当该事件发生的时候再来通知我们

1、ServerSocketChannel默认是阻塞式的
2、创建一个Selector注册给该serversocketChannel，并且给该server注册一个接受事件
3、创建一个线程轮询至少一个事件发生selector.select
4、对每一种selctkey进行处理

- Channel 通道是读写的对象，当然所有数据是通过buffer进行读写的

1、使用NIO读取数据：通过FileInputStream获取到channel,创建buffer,将channel的数据读取到buffer中

2、使用NIO写数据：通过FileOutputStreanm获取channel,创建buffer,写到buffer数据，通过channel把buffer写到文件中

IO的多路复用技术适合于高并发的场景，一毫秒上千请求才算；

### 反应堆

在阻塞io中，serverSocker.accept()和inputStream.read(),在没有数据的时候会一直阻塞；

在bio模型中问题：当客户端多的时候就会创建很多线程，浪费宝贵的系统资源；当线程阻塞的时候，系统会频繁的上下文切换，而这些切换时无意义的，那么及时响应的nio就很有效

nio:

1、有一个单独线程处理所有的事件，并且进行事件分发

2、有事件发生的时候才触发事件的发生，而不是同步的区监听

3、线程通过wait/notify进行通信，减少无畏的上下文切换，每次切换都是有意义的；

每一个处理线程都会：接受消息-解码-计算处理-编码-发送

## Netty

Netty是异步的、事件驱动的高性能、高可靠通讯框架；

支持多种底层协议，并且灵活可配置；

解决的很多nio的不易开发的问题；

社区活跃，很多高性能框架的底层通宵都是用Netty
