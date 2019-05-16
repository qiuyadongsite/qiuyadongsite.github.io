---
layout: post
title:  重新认识ReentrantLock
date:   2019-05-16 21:52:12 +08:00
category: 性能优化
tags: mysql
comments: true
---

* content
{:toc}


由于比synchronized灵活，粒度可操作，使用Lock的api更适合业务的实际使用。























## ReentrantLock的lock方法

三个线程一起走到共享代码块处：调用lock.lock();

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/lock0001.png)

线程1，抢占到资源，源码执行：

```java

//将state设置为1，state可以理解为加锁的个数

if (compareAndSetState(0, 1))
                setExclusiveOwnerThread(Thread.currentThread());

```

线程2、和线程3，发现期望值是0和当前值1不相等，设置1返回false走：


```java

 acquire(1);//如果获取失败，会阻塞，看实现


 public final void acquire(int arg) {
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
}

```

线程2在尝试了一次获取不到锁后，将调用addwaiter方法，具体逻辑如图：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/thread0002.png)

线程3在尝试了一次获取不到锁后，将调用addwaiter方法，具体逻辑如图：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/thread00004.png)

具体代码是：

```java

private Node addWaiter(Node mode) {
       Node node = new Node(Thread.currentThread(), mode);
       // Try the fast path of enq; backup to full enq on failure
       Node pred = tail;
       if (pred != null) {
           node.prev = pred;
           if (compareAndSetTail(pred, node)) {
               pred.next = node;
               return node;
           }
       }
       enq(node);
       return node;
   }

   private Node enq(final Node node) {
        for (;;) {
            Node t = tail;
            if (t == null) { // Must initialize
                if (compareAndSetHead(new Node()))
                    tail = head;
            } else {
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }

```

线程2,3加入队列后还有进行阻塞(否则它还是不会停止下来):

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/thread0005.png)

代码是：

```java

final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return interrupted;
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())//线程2,3都会阻塞到此处
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }
    private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
            int ws = pred.waitStatus;
            if (ws == Node.SIGNAL)

                return true;//线程3到此处返回，因为线程2是signal
            if (ws > 0) {

                do {
                    node.prev = pred = pred.prev;
                } while (pred.waitStatus > 0);//如果某个线程是cancel状态移除等待队列
                pred.next = node;
            } else {

                compareAndSetWaitStatus(pred, ws, Node.SIGNAL);//线程2到此处时将head节点的wait设置为siganl
            }
            return false;
        }

        private final boolean parkAndCheckInterrupt() {
                LockSupport.park(this);//阻塞线程到此处
                return Thread.interrupted();
            }

```

## ReentrantLock的unlock方法

释放锁代码如下：

```java

public final boolean release(int arg) {
        if (tryRelease(arg)) {//线程到此处释放成功
            Node h = head;
            if (h != null && h.waitStatus != 0)//由于head是空的node不等于null,且waitstatus=signal
                unparkSuccessor(h);//
            return true;
        }
        return false;
    }


    protected final boolean tryRelease(int releases) {
            int c = getState() - releases;
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();
            boolean free = false;
            if (c == 0) {
                free = true;
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }

```

根据上述情况会走以下唤醒一个head后面的node:

```java

private void unparkSuccessor(Node node) {

        int ws = node.waitStatus;
        if (ws < 0)
            compareAndSetWaitStatus(node, ws, 0);


        Node s = node.next;
        if (s == null || s.waitStatus > 0) {
            s = null;
            for (Node t = tail; t != null && t != node; t = t.prev)
                if (t.waitStatus <= 0)
                    s = t;
        }
        if (s != null)
            LockSupport.unpark(s.thread);//thread2走到这里就会唤醒lock中LockSupport.park（this）处
    }

```

## LockSupport.unpark(s.thread)后回到阻塞处

唤醒阻塞后，再次自旋获取锁：

```java

final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return interrupted;
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())//回到此处唤醒后
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

```

由于刚唤醒，interrupted为true，接着执行：

```java

public final void acquire(int arg) {
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();//由于之前使用locksupport.park阻塞过，需要设置该线程的中断标志位复位
    }

    static void selfInterrupt() {
            Thread.currentThread().interrupt();
        }

```

具体实验案例：

```java

public class App
{
    public static void main( String[] args ) throws Exception {


        t2();
    }
    public synchronized static void t2() throws Exception
    {
        Thread t = new Thread(new Runnable()
        {
            private int count = 0;

            @Override
            public void run()
            {
                long start = System.currentTimeMillis();
                long end = 0;

                while ((end - start) <= 1000)
                {
                    count++;
                    end = System.currentTimeMillis();
                }

                System.out.println("after 1 second.count=" + count);

                //等待或许许可
                LockSupport.park();
                System.out.println("thread over." + Thread.currentThread().isInterrupted());

            }
        });

        t.start();
        t.isInterrupted();
        Thread.sleep(2000);
     /*   t.interrupt(); 此处是将 Thread.currentThread().isInterrupted()的值设为true*/
        LockSupport.unpark(t);
       // 中断线程
        System.out.println("main over");
    }

}


```
