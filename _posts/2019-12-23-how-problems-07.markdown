---
layout: post
title:  多线程2
date:   2019-12-23 20:52:12 +08:00
category: 签到系列
tags: 分布式高并发
comments: true
---

* content
{:toc}


签到7！






## lock的类关系

Lock接口定义了获取锁和释放锁的相关方法；

lock():如果锁可用就获取锁，否则就阻塞直到锁释放

lockInterruptibly,阻塞后是可以中断的

trylock();尝试获取锁

ReetrantLock和synchronized都是可重入锁

lock为了实现灵活的锁机制，使用AQS

### AbstractQueuedSynchronizer

AQS有两个功能：独占和共享

一个锁在一个时间点只能有一个线程获取，独占，否则就是共享

ReetrantLock就是独占锁，ReentrantReadWriteLock就是共享锁

AQS中内部维护了一个FIFO的双向链表；当线程抢锁失败后就会以node节点对象存入到AQS中

设置tail节点需要使用cas，而设置head节点不需要使用cas，因为已经获取锁了，只能获取锁的线程执行取消head指向的操作

看源码分析：

ReentrantLock默认是非公平的Lock,一上来就抢占

```java

if (compareAndSetState(0, 1))
               setExclusiveOwnerThread(Thread.currentThread());


```

FairSync:所有线程严格按照FIFO进行获取锁

 unsafe.compareAndSwapInt(this, stateOffset, expect, update);

 通过乐观锁cas进行比较和替换，不需要枷锁

 Unsafe：sun.misc包下的类，java留下的后门，提供了低层次操作，如：直接内存访问，线程的挂起和恢复、cas、线程同步、内存屏障

  LockSupport.park(this);

  只有一个许可，park就会阻塞，unpark就将许可加一，让阻塞运行，不能叠加，只能有一个许可

  为啥释放锁的时候从tail进行扫描呢？

  synchronized可以通过notify和wait控制线程的基本组合；

  JUC中使用Condition来实现线程走到某个条件进行唤醒 :lock.newCondition();
