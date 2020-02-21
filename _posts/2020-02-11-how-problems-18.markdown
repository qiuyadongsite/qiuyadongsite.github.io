---
layout: post
title:  多线程1
date:   2020-02-11 20:53:12 +08:00
category: 签到系列
tags: 多线程
comments: true
---

* content
{:toc}



签到18！



## 学习总结

学习的目标不是吹牛炫技，而是让自己得到充实，空虚的生命能有点意义！



## 1、如何使用多线程

- 定义线程的两种方式：

>*Provide a Runnable object.*  实现Runable接口
>
>*Subclass Thread.*  继承Thread类

- 如何使用多线程

  - 实现Runable接口

    > ```java
    >  (new Thread(new HelloRunnable())).start();
    > ```

  -  继承Thread类

    >```java
    >(new HelloThread()).start();
    >```

  - 调用Executor

    >```java
    >Executor executor = anExecutor;
    > executor.execute(new RunnableTask1());
    >```

  - 调用`ExecutorService`

    >```
    ><T> Future<T> submit(Callable<T> task);
    >//通过Futrue获取值
    >```

## 2、线程的状态

- Thread.State

>- [`NEW`](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.State.html#NEW)
>  A thread that has not yet started is in this state.
>- [`RUNNABLE`](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.State.html#RUNNABLE)
>  A thread executing in the Java virtual machine is in this state.
>- [`BLOCKED`](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.State.html#BLOCKED)
>  A thread that is blocked waiting for a monitor lock is in this state.(一个线程因为等待临界区的锁被阻塞产生的状态  Lock 或者synchronize 关键字产生的状态 )
>- [`WAITING`](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.State.html#WAITING)
>  A thread that is waiting indefinitely for another thread to perform a particular action is in this state.(一个线程进入了锁，但是需要等待其他线程执行某些操作。时间不确定  当wait，join，park方法调用时，进入waiting状态。前提是这个线程已经拥有锁了。)
>- [`TIMED_WAITING`](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.State.html#TIMED_WAITING)
>  A thread that is waiting for another thread to perform an action for up to a specified waiting time is in this state.
>- [`TERMINATED`](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.State.html#TERMINATED)
>  A thread that has exited is in this state.

## 3、中断一个线程

调用thread.interrupt()

>中断是线程应该停止正在执行的操作并执行其他操作的指示。
>
>This depends on what it's currently doing.  If the thread is frequently invoking methods that throw `InterruptedException`, it simply returns from the `run`method after it catches that exception.
>
>Many methods that throw `InterruptedException`, such as `sleep`, are designed to cancel their current operation and return immediately when an interrupt is received.
>
>许多抛出InterruptedException的方法(如sleep)被设计成取消当前操作，并在接收到中断时立即返回。

判断中断：

- Thread.interrupted,会复位标志位
- thread.isInterrupted，不会复位标志位

## 4、保证线程有序运行

调用thread.join方法

>The `join` method allows one thread to wait for the completion of another.
>
>Like `sleep`, `join` responds to an interrupt by exiting with an `InterruptedException`.

## 5、缓存一致性问题与解决

为了消除处理器与主存的运算速度的差异，引入高速缓存，将中间结果和需要的处理数据暂时存入缓存，运算结束再同步到内存之中；

- 缓存一致性问题

  每一个cpu从主存中获取处理数据进行修改，当高速缓存没有及时的同步到主内存，那么就存在了错误预期的结果；

  - 总线锁

    > 当一个cpu处理一个主内存数据时，就表示lock,其他cpu无法对该数据进行操作，性能太低

  - 缓存锁

    >通过缓存一致性协议保证缓存的数据一致性：
    >
    >Lock锁的是缓存、该缓存回写内存会导致其他处理器的缓存无效

- 缓存一致性协议

  一套完整的协议保证Cache一致，经典的是MESI协议；

  - 状态

    - 修改状态M(Modified)，当前cpu缓存已经修改，和主内存已经不一致
    - 失效状态I(Invalid )，cpu缓存中的数据已经不能再使用了
    - 独占状态E(Exclusive)，当前cpu缓存的数据跟主内存一致，没有其他cpu缓存改数据
    - 共享状态S(Shared)，数据和内存数据一致，并且多个cpu缓存了该数据

  - cpu的缓存控制器

    >不仅知道自己的读写操作还监听了其他Cache的读写操作

  - 原则

    - 如果缓存是I,则从内存中读取，否则从缓存中读取；
    - 如果缓存处于M或E的cpu嗅探到其他CPU的读操作，就把自己的缓存写入到内存，并把自己的状态设置为S
    - 只有缓存状态是M或者E的时候，CPU才可以修改缓存中的数据，修改后，缓存状态修改为M
    - 当一个cpu要修改数据时，将缓存行改为M,其他cpu的缓存修改为I

## 6、JMM

前一个问题保证了cpu与主内存的效率，在cpu内部也可以优化，根据处理单元进行乱序处理，但保证结果跟顺序执行一致；

- 乱序理解

  >cpu对数据处理后，会发送指令给其他cpu缓存管理器，如果同步执行没有问题，但是阻塞必然引起性能开销，引入了store buffer来缓存指令，异步处理，那问题来了？
  >
  >cpu缓存管理器又得先去store buffer中去取，没有再去缓存中取，硬件工程师表示问题更多了，这就是指令重排或者重排序问题。那就写了内存屏障指令交给软件工程师调用解决吧！

- 为了解决乱序问题提出了JMM模型

  > 内存模型定义了共享内存系统中多线程程序读写操作行为的规范，来屏蔽各种硬件和操作系统的内存访问差异，来实现 Java 程序在各个平台下都能达到一致的内存访问效果。

  - 主内存

    线程共享的，引用对象、数组、静态变量等存储在堆中的变量

  - 工作内存

    线程私有的工作内存对变量的改变都是线程内的，不能直接改变主内存中的变量进行读写，线程直接的变量传递都是通过主内存的共享变量进行的

    内存模型解决并发问题主要采用两种方式：限制处理器优化和使用内存屏障。

- 内存屏障

  - 写屏障：写屏障之后的所有读或者写可见，写屏障让store buffer中的数据同步到主存中，刷新store buffer
  - 读屏障：读屏障之后的读都对于读可见
  - 全屏障：结合读写屏障

- 如何使用内存屏障

  使用关键字进行标示，在虚拟机内部进行调用；

## 7、可见性、原子性、有序性

- 可见性（缓存导致的可见性问题）

  一个线程对共享变量的修改，另一个线程可以立即看到，这称之为可见性。

  > 除了`volatile`，java中还有`synchronized`和`final`关键字能实现可见性

- 原子性（线程切换带来的原子性问题）

   一个或者多个操作在CPU执行期间不被打断的特性成为原子性。

   >`monitorenter`和 `monitorexit`来隐式地使用这两个操作，这两个字节码指令反映到java代码之中就是同步块（`synchronized`），所以在`synchronized`块之间的操作也是具备有原子性的

- 有序性（编译优化带来的有序性问题）

  >比如：Object  obj = new Object()，
  >
  >　　这条语句对应的指令为：
  >
  >1. 分配一块内存M；
  >
  >2. 在M上初始化 Object 对象；
  >
  >3. 将M的地址赋值给 obj；
  >
  >   计算机经过优化后可能先执行第三步，再第二步，如果执行完第三步后切换到别的线程，若此时访问该变量则会发生空指针异常；
  >
  >   java提供了`volatile`和`synchronized`两个关键字来保证线程之间的操作的有序性


## 8、理解同步

线程主要通过共享对字段和对象引用字段的访问进行通信。

>but makes two kinds of errors possible: *thread interference* and *memory consistency errors*.  
>
>**synchronization** can introduce *thread contention*, which occurs when two or more threads try to access the same resource simultaneously *and* cause the Java runtime to execute one or more threads more slowly, or even suspend their execution.

线程干扰和内存一致性错误：导致同步被提出来！

饥饿和活锁是线程争用的两种形式。

- synchronized 使用

  - 普通方法

    需要获取当前实例对象的锁(对象锁)

  - 静态方法

    需要获取当前类的锁(类锁)

  - synchronized (this)

    需要获取当前实例对象的锁(对象锁)

  - synchronized (Xxx.class)

    需要获取当前类的锁(类锁)

- synchronized 状态（无锁状态、偏向锁状态、轻量级锁状态、重量级锁状态 ）

  - 偏向锁状态

    >**在无竞争的情况下会把整个同步都消除掉**

  - 轻量级锁状态

    > 轻量级锁不是为了代替重量级锁，它的本意是**在没有多线程竞争的前提**下，减少传统的重量级锁使用操作系统互斥量产生的性能消耗，因为使用轻量级锁时，不需要申请互斥量。另外，轻量级锁的加锁和解锁都用到了CAS操作。

  - 自旋锁和自适应自旋

    > **一般线程持有锁的时间都不是太长，所以仅仅为了这一点时间去挂起线程/恢复线程是得不偿失的**。
    >
    > 互斥同步对性能最大的影响就是阻塞的实现，因为挂起线程/恢复线程的操作都需要转入内核态中完成（用户态转换到内核态会耗费时间）。

  - 锁清除

  - 锁粗化

- synchronized与Lock的区别

  - synchronized是java内置关键字，在jvm层面，Lock是个java类；
  - synchronized无法判断是否获取锁的状态，Lock可以判断是否获取到锁；
  - synchronized会自动释放锁(a 线程执行完同步代码会释放锁 ；b 线程执行过程中发生异常会释放锁)，Lock需在finally中手工释放锁（unlock()方法释放锁），否则容易造成线程死锁；
  - 用synchronized关键字的两个线程1和线程2，如果当前线程1获得锁，线程2线程等待。如果线程1阻塞，线程2则会一直等待下去，而Lock锁就不一定会等待下去，如果尝试获取不到锁，线程可以不用一直等待就结束了；
  - synchronized的锁可重入、不可中断、非公平，而Lock锁可重入、可判断、可公平（两者皆可）
  - Lock锁适合大量同步的代码的同步问题，synchronized锁适合代码少量的同步问题。

## 9、理解AQS

为了理解和使用lock接口及实现ReenTrantLock,底层是基于AQS(AbstractQueuedSynchronizer );

> AQS是一个用来构建锁和同步器的框架，使用AQS能简单且高效地构造出应用广泛的大量的同步器，比如我们提到的ReentrantLock，Semaphore，其他的诸如ReentrantReadWriteLock，SynchronousQueue，FutureTask等等皆是基于AQS的。当然，我们自己也能利用AQS非常轻松容易地构造出符合我们自己需求的同步器。

- 核心思想

  >如果被请求的共享资源空闲，则将当前请求资源的线程设置为有效的工作线程，并且将共享资源设置为锁定状态。如果被请求的共享资源被占用，那么就需要一套线程阻塞等待以及被唤醒时锁分配的机制，这个机制AQS是用CLH队列锁实现的，即将暂时获取不到锁的线程加入到队列中。
  >
  >CLH(Craig,Landin,and Hagersten)队列是一个虚拟的双向队列（虚拟的双向队列即不存在队列实例，仅存在结点之间的关联关系）。AQS是将每条请求共享资源的线程封装成一个CLH锁队列的一个结点（Node）来实现锁的分配。

- 资源共享方式

  - Exclusive（独占）：只有一个线程能执行，如ReentrantLock。又可分为公平锁和非公平锁：
    - 公平锁：按照线程在队列中的排队顺序，先到者先拿到锁
    - 非公平锁：当线程要获取锁时，无视队列顺序直接去抢锁，谁抢到就是谁的
  - Share（共享）：多个线程可同时执行，如Semaphore/CountDownLatch。Semaphore、CountDownLatCh、 CyclicBarrier、ReadWriteLock

- 自定义同步器

  使用AQS的各个同步器，都是实现了AQS,使用魔板方法模式进行的实现；

  > 一般来说，自定义同步器要么是独占方法，要么是共享方式，他们也只需实现`tryAcquire-tryRelease`、`tryAcquireShared-tryReleaseShared`中的一种即可。但AQS也支持自定义同步器同时实现独占和共享两种方式，如`ReentrantReadWriteLock`。

## 10、介绍几种同步器

- Semaphore(信号量)-允许多个线程同时访问

  ```java
  /**
   *
   * @Description: 需要一次性拿一个许可的情况
   */
  public class SemaphoreExample1 {
    // 请求的数量
    private static final int threadCount = 550;

    public static void main(String[] args) throws InterruptedException {
      // 创建一个具有固定线程数量的线程池对象（如果这里线程池的线程数量给太少的话你会发现执行的很慢）
      ExecutorService threadPool = Executors.newFixedThreadPool(300);
      // 一次只能允许执行的线程数量。
      final Semaphore semaphore = new Semaphore(20);

      for (int i = 0; i < threadCount; i++) {
        final int threadnum = i;
        threadPool.execute(() -> {// Lambda 表达式的运用
          try {
            semaphore.acquire(x);// 获取x个许可，所以可运行线程数量为20/x
            test(threadnum);
            semaphore.release(x);// 释放x个许可
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }

        });
      }
      threadPool.shutdown();
      System.out.println("finish");
    }

    public static void test(int threadnum) throws InterruptedException {
      Thread.sleep(1000);// 模拟请求的耗时操作
      System.out.println("threadnum:" + threadnum);
      Thread.sleep(1000);// 模拟请求的耗时操作
    }
  }
  ```

- CountDownLatch （倒计时器）

  - 某一线程在开始运行前等待n个线程执行完毕

    一个典型应用场景就是启动一个服务时，主线程需要等待多个组件加载完毕，之后再继续执行。

  - 实现多个线程开始执行任务的最大并行性

    类似于赛跑，将多个线程放到起点，等待发令枪响，然后同时开跑。

  - 死锁检测

    一个非常方便的使用场景是，你可以使用n个线程访问共享资源，在每次测试阶段的线程数目是不同的，并尝试产生死锁。



  ```java
  /**
   *
   * @Description: CountDownLatch 使用方法示例
   */
  public class CountDownLatchExample1 {
    // 请求的数量
    private static final int threadCount = 550;

    public static void main(String[] args) throws InterruptedException {
      // 创建一个具有固定线程数量的线程池对象（如果这里线程池的线程数量给太少的话你会发现执行的很慢）
      ExecutorService threadPool = Executors.newFixedThreadPool(300);
      final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
      for (int i = 0; i < threadCount; i++) {
        final int threadnum = i;
        threadPool.execute(() -> {// Lambda 表达式的运用
          try {
            test(threadnum);
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } finally {
            countDownLatch.countDown();// 表示一个请求已经被完成
          }

        });
      }
      countDownLatch.await();
      threadPool.shutdown();
      System.out.println("finish");
    }

    public static void test(int threadnum) throws InterruptedException {
      Thread.sleep(1000);// 模拟请求的耗时操作
      System.out.println("threadnum:" + threadnum);
      Thread.sleep(1000);// 模拟请求的耗时操作
    }
  }
  ```



  - 不足

    CountDownLatch是一次性的，计数器的值只能在构造方法中初始化一次，之后没有任何机制再次对其设置值，当CountDownLatch使用完毕后，它不能再次被使用。

- CyclicBarrier(循环栅栏)

  >CyclicBarrier 的字面意思是可循环使用（Cyclic）的屏障（Barrier）。它要做的事情是，让一组线程到达一个屏障（也可以叫同步点）时被阻塞，直到最后一个线程到达屏障时，屏障才会开门，所有被屏障拦截的线程才会继续干活。CyclicBarrier默认的构造方法是 `CyclicBarrier(int parties)`，其参数表示屏障拦截的线程数量，每个线程调用`await`方法告诉 CyclicBarrier 我已经到达了屏障，然后当前线程被阻塞。



```java
/**
 *
 * @Description: 测试 CyclicBarrier 类中带参数的 await() 方法
 */
public class CyclicBarrierExample2 {
  // 请求的数量
  private static final int threadCount = 550;
  // 需要同步的线程数量
  private static final CyclicBarrier cyclicBarrier = new CyclicBarrier(5);

  public static void main(String[] args) throws InterruptedException {
    // 创建线程池
    ExecutorService threadPool = Executors.newFixedThreadPool(10);

    for (int i = 0; i < threadCount; i++) {
      final int threadNum = i;
      Thread.sleep(1000);
      threadPool.execute(() -> {
        try {
          test(threadNum);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (BrokenBarrierException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      });
    }
    threadPool.shutdown();
  }

  public static void test(int threadnum) throws InterruptedException, BrokenBarrierException {
    System.out.println("threadnum:" + threadnum + "is ready");
    try {
      /**等待60秒，保证子线程完全执行结束*/  
      cyclicBarrier.await(60, TimeUnit.SECONDS);
    } catch (Exception e) {
      System.out.println("-----CyclicBarrierException------");
    }
    System.out.println("threadnum:" + threadnum + "is finish");
  }

}
```

```
threadnum:0is ready
threadnum:1is ready
threadnum:2is ready
threadnum:3is ready
threadnum:4is ready
threadnum:4is finish
threadnum:0is finish
threadnum:1is finish
threadnum:2is finish
threadnum:3is finish
threadnum:5is ready
threadnum:6is ready
threadnum:7is ready
threadnum:8is ready
threadnum:9is ready
threadnum:9is finish
threadnum:5is finish
threadnum:8is finish
threadnum:7is finish
threadnum:6is finish
......
```

```java
/**
 *
 * @Description: 新建 CyclicBarrier 的时候指定一个 Runnable
 */
public class CyclicBarrierExample3 {
  // 请求的数量
  private static final int threadCount = 550;
  // 需要同步的线程数量
  private static final CyclicBarrier cyclicBarrier = new CyclicBarrier(5, () -> {
    System.out.println("------当线程数达到之后，优先执行------");
  });

  public static void main(String[] args) throws InterruptedException {
    // 创建线程池
    ExecutorService threadPool = Executors.newFixedThreadPool(10);

    for (int i = 0; i < threadCount; i++) {
      final int threadNum = i;
      Thread.sleep(1000);
      threadPool.execute(() -> {
        try {
          test(threadNum);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (BrokenBarrierException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      });
    }
    threadPool.shutdown();
  }

  public static void test(int threadnum) throws InterruptedException, BrokenBarrierException {
    System.out.println("threadnum:" + threadnum + "is ready");
    cyclicBarrier.await();
    System.out.println("threadnum:" + threadnum + "is finish");
  }

}
```

```
threadnum:0is ready
threadnum:1is ready
threadnum:2is ready
threadnum:3is ready
threadnum:4is ready
------当线程数达到之后，优先执行------
threadnum:4is finish
threadnum:0is finish
threadnum:2is finish
threadnum:1is finish
threadnum:3is finish
threadnum:5is ready
threadnum:6is ready
threadnum:7is ready
threadnum:8is ready
threadnum:9is ready
------当线程数达到之后，优先执行------
threadnum:9is finish
threadnum:5is finish
threadnum:6is finish
threadnum:8is finish
threadnum:7is finish
......
```

- #### CyclicBarrier和CountDownLatch的区别

  - CountDownLatch是计数器，只能使用一次，而CyclicBarrier的计数器提供reset功能，可以多次使用。

  - 对于CountDownLatch来说，重点是“一个线程（多个线程）等待”，而其他的N个线程在完成“某件事情”之后，可以终止，也可以等待。而对于CyclicBarrier，重点是多个线程，在任意一个线程没有完成，所有的线程都必须等待。

  - CountDownLatch是计数器，线程完成一个记录一个，只不过计数不是递增而是递减，而CyclicBarrier更像是一个阀门，需要所有线程都到达，阀门才能打开，然后继续执行。
