---
layout: post
title:  并发编程术语
date:   2018-10-25 22:52:12 +08:00
category: 并发编程
tags: 并发编程
comments: true
---

* content
{:toc}

并发编程一直是个挑战，so，那就先挑战这个喽






## 并发编程的目的

目的：为了让程序更快的执行。并不是启动更多的线程就能让程序最大限度的执行。

## 上下文切换

* 任务从保存到再加载的过程就是一次上下文切换

## 多线程一定快吗

* 不一定，当上下文切换的消耗很大时，效率会差不多甚至慢。
* 工具：
   Lmbench3,测试上下文切换的时长
   vmstat,测试上下文切换的次数

## 如何减少上下文切换

1. 无锁并发编程：多线程争取获取锁，造成上下文切换，可以采用业务区分数据ID，按照HASH算法取模分段，不同线程处理不同段的数据；
2. CAS算法：java的Atomic包使用CAS算法更新数据，而不需要枷锁
3. 使用最少线程：避免创建不需要的现场。
4. 使用协程：在单线程里实现多任务调度，并在单线程里维护多个任务间的切换。

## 死锁

- 死锁往往是业务可感知的，只能使用dump线程查看那个线程出现问题；
避免死锁的方法：
1. 避免一个线程同时获取多个锁；
2. 避免一个线程再锁内同时再用多个资源，尽量保证每个锁只占用一个资源
3. 尝试使用定时锁，lock.trylock(timeout)
4. 对于数据库锁，加锁和解锁必须在一个数据库的连接里，否则会出现解锁失败的情况

## 资源限制

- 资源：网络资源，软件资源（数据库连接数、socket连接数）
- 引发的问题：如果受限于资源，并发的任务仍然串行执行，那么并发反而更慢
- 资源限制解决方法：硬件资源（集群并行执行）、软件资源（资源池将资源复用）
- 在当前资源下并发编程：根据不同资源限制调整程序的并发度。
