---
layout: post
title:  高并发编程-AQS深入解析
date:   2019-04-08 20:52:12 +08:00
category: 并发编程
tags: 并发编程
comments: true
---

* content
{:toc}


AbstractQueuedSynchronizer简称AQS，它是java.util.concurrent包下CountDownLatch/FutureTask/ReentrantLock/RenntrantReadWriteLock/Semaphore实现的基础，所以深入理解AQS非常有必要。







## 概念

AQS通过内部实现的FIFO同步等待队列来完成资源获取线程的等待工作，如果当前线程获取资源失败，AQS则会将当前线程以及等待状态等信息构造成一个Node结构的节点，并将其加入等待队列中，同时会阻塞当前线程；

当其它获取到资源的线程释放持有的资源时，则会把等待队列节点中的线程唤醒，使其再次尝试获取对应资源。

## 源码解析

AbstractQueuedSynchronizer源码比较长，这里只分析主要的功能代码。首先，先看一下它内部定义的Node类的代码。

```java


static  final class Node {
       //声明共享模式下的等待节点
       static final Node SHARED = new Node();

       //声明独占模式下的等待节点
       static final Node EXCLUSIVE = null;

       //waitStatus的一常量值，表示线程已取消
       static final int CANCELLED =  1;

       //waitStatus的一常量值，表示后继线程需要取消挂起
       static final int SIGNAL    = -1;

       //waitStatus的一常量值，表示线程正在等待条件
       static final int CONDITION = -2;

       //waitStatus的一常量值，表示下一个acquireShared应无条件传播
       static final int PROPAGATE = -3;

       //waitStatus,其值只能为CANCELLED、SIGNAL、CONDITION、PROPAGATE或0
       //初始值为0
       volatile int waitStatus;

       //前驱节点
       volatile Node prev;

       //后继节点
       volatile Node next;

       //当前节点的线程，在节点初始化时赋值，使用后为null
       volatile Thread thread;

       //下一个等待节点
       Node nextWaiter;

       Node() {

       }
       Node(Thread thread, Node mode) {    
           // Used by addWaiter
           this.nextWaiter = mode;
           this.thread = thread;
       }
       Node(Thread thread, int waitStatus) {

           // Used by Condition

           this.waitStatus = waitStatus;

           this.thread = thread;

       }

   }

```

上面的Node就是等待队列里的一个节点，具体结构如下：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/aqs001.png)

接着，来看一下AbstractQueuedSynchronizer的三个重要属性：

```java

   //等待队列的头结点
   private transient volatile Node head;

   //等待队列的尾节点
   private transient volatile Node tail;
   //同步状态，这个很重要
   private volatile int state;

```

从这就可以得到同步队列的基本结构：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/aqs002.png)

同时，同步器中提供了三个方法用于操作同步状态：

```java


protected final int getState() {

        return state;

    }
    protected final void setState(int newState) {
        state = newState;
    }

    //使用CAS设置同步状态，确保线程安全

    protected final boolean compareAndSetState(int expect, int update) {
        return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
    }

```

AbstractQueuedSynchronizer类中其它方法主要是用于插入节点、释放节点，插入节点过程如下图所示：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/aqs003.png)

释放头结点过程如下图所示：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/aqs004.png)

## 分析小结

AbstractQueuedSynchronizer实现了对资源获取与释放的基础实现，真正使用到的地方还在是各个具体的功能类中，如CountDownLatch、ReentrantLock等，后面在这些类中会具体分析。
