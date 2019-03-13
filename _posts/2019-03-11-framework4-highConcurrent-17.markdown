---
layout: post
title:  分布式消息通讯AMQ原理分析
date:   2019-03-11 22:52:12 +08:00
category: 高并发分布式
tags: ActiveMQ
comments: true
---

* content
{:toc}

ActiveMQ 的使用很简单，关键要了解其原理才能很有效的结合业务特点，将amq使用起来。












## 持久化消息和非持久化消息的发送策略

#### 消息同步发送和异步发送

ActiveMQ支持同步、异步两种发送模式将消息发送到broker上。

同步发送过程中，发送者发送一条消息会阻塞直到broker反馈一个确认消息，表示消息已经被broker处理。这个机制提供了消息的安全性保障，但是由于是阻塞的操作，会影响到客户端消息发送的性能

异步发送的过程中，发送者不需要等待broker提供反馈，所以性能相对较高。但是可能会出现消息丢失的情况。所以使用异步发送的前提是在某些情况下允许出现数据丢失的情况。

默认情况下，`非持久化消息`是异步发送的，`持久化消息并且是在非事务模式下`是同步发送的。

但是在开启事务的情况下，消息都是异步发送。由于异步发送的效率会比同步发送性能更高。所以在发送持久化消息的时候，尽量去开启事务会话。

除了持久化消息和非持久化消息的同步和异步特性以外，我们还可以通过以下几种方式来设置异步发送

```java
//1.
ConnectionFactory connectionFactory=new ActiveMQConnectionFactory("tcp://xx:61616?
jms.useAsyncSend=true");

//2.
((ActiveMQConnectionFactory) connectionFactory).setUseAsyncSend(true);

//3.
((ActiveMQConnection)connection).setUseAsyncSend(true);

```

消息的发送原理分析图解

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/amq002.png)


ProducerWindowSize的含义

producer每发送一个消息，统计一下发送的字节数，当字节数达到ProducerWindowSize值时，需要等待broker的确认，才能继续发送。

```java

                 this.connection.asyncSendPacket(msg);
                   if (producerWindow != null) {
                       int size = msg.getSize();
                       producerWindow.increaseUsage((long)size);
                   }

```

主要用来约束在异步发送时producer端允许积压的(尚未ACK)的消息的大小，且只对异步发送有意义。每次发送消息之后，都将会导致memoryUsage大小增加(+message.size)，当broker返回producerAck时，memoryUsage尺寸减少(producerAck.size，此size表示先前发送消息的大小)。

可以通过如下2种方式设置:

 在brokerUrl中设置: "tcp://localhost:61616?jms.producerWindowSize=1048576",这种设置将会对所有的producer生效。

 在destinationUri中设置: "test-queue?producer.windowSize=1048576",此参数只会对使用此Destination实例的producer失效，将会覆盖brokerUrl中的producerWindowSize值。

注意：此值越大，意味着消耗Client端的内存就越大;

消息发送的源码分析

以producer.send为入口

待续...
