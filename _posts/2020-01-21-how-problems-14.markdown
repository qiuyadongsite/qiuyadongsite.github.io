---
layout: post
title:  Concurrency_tutorials
date:   2020-01-21 20:53:12 +08:00
category: 签到系列
tags: spring
comments: true
---

* content
{:toc}


签到14！



## Processes and Threads

In concurrent programming, there are two basic units of execution: *processes* and *threads*.  Processing time for a single core is shared among processes and threads through an OS feature called time slicing 

- Processes

   >A process generally has a complete, private set of basic run-time resources; in particular, each process has its own memory space. To facilitate communication between processes, most operating systems support *Inter Process Communication* (IPC) resources, such as pipes and sockets. 

- Threads

  >Threads exist within a process — every process has at least one. Threads share the process's resources, including memory and open files.  



## Threads

There are two basic strategies for using `Thread` objects to create a concurrent application. 

- To directly control thread creation and management, simply instantiate `Thread` each time the application needs to initiate an asynchronous task. 
- To abstract thread management from the rest of your application, pass the application's tasks to an *executor*.

## simple Thread

- Defining and Starting a Thread

  > There are two ways to do this:
  >
  >*Provide a Runnable object*：Not only is this approach more flexible, but it is applicable to the high-level thread management APIs covered later； 
  >
  >*Subclass Thread*；

- Pausing Execution with Sleep

  >the sleep period can be terminated by interrupts ;
  >
  >InterruptedException :this is an exception that `sleep` throws when another thread interrupts the current thread while `sleep` is active.  

  ```java
  if (Thread.interrupted()) {
      throw new InterruptedException();
  }
  ```

- Interrupts:

  >A thread sends an interrupt by invoking [`interrupt`](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.html#interrupt--) on the `Thread` object for the thread to be interrupted. 
  >
  >Many methods that throw `InterruptedException`, such as `sleep`, are designed to cancel their current operation and return immediately when an interrupt is received. 
  >
  >The interrupt mechanism is implemented using an internal flag known as the *interrupt status*.
  >
  >
  >
  >Invoking `Thread.interrupt` sets this flag. When a thread checks for an interrupt by invoking the static method `Thread.interrupted`, interrupt status is cleared. The non-static `isInterrupted` method, which is used by one thread to query the interrupt status of another, does not change the interrupt status flag.  

- Joins

  >The `join` method allows one thread to wait for the completion of another. 
  >
  >causes the current thread to pause execution until `t`'s thread terminates. Overloads of `join` allow the programmer to specify a waiting period. However, as with `sleep`, `join` is dependent on the OS for timing, so you should not assume that `join` will wait exactly as long as you specify. 
  >
  >Like `sleep`, `join` responds to an interrupt by exiting with an `InterruptedException`. 

- Synchronization

  > [Starvation and livelock](https://docs.oracle.com/javase/tutorial/essential/concurrency/starvelive.html) are forms of thread contention ;

  - Thread Interference

  - Memory Consistency Errors

    >*Memory consistency errors* occur when different threads have inconsistent views of what should be the same data. 
    >
    >unless the programmer has established a happens-before relationship between these two statements. 
    >
    >happens-before:
    >
    >- When a statement invokes `Thread.start`, every statement that has a happens-before relationship with that statement also has a happens-before relationship with every statement executed by the new thread. The effects of the code that led up to the creation of the new thread are visible to the new thread.
    >- When a thread terminates and causes a `Thread.join` in another thread to return, then all the statements executed by the terminated thread have a happens-before relationship with all the statements following the successful join. The effects of the code in the thread are now visible to the thread that performed the join.

  - Synchronized Methods

    > The Java programming language provides two basic synchronization idioms: *synchronized methods* and *synchronized statements*. 
    >
    > Synchronizing constructors doesn't make sense, because only the thread that creates an object should have access to it while it is being constructed. 

  - Intrinsic Locks and Synchronization

    > Synchronization is built around an internal entity known as the *intrinsic lock* or *monitor lock*.  
    >
    > Intrinsic locks play a role in both aspects of synchronization: enforcing exclusive access to an object's state and establishing happens-before relationships that are essential to visibility. 
    >
    >  By convention, a thread that needs exclusive and consistent access to an object's fields has to *acquire* the object's intrinsic lock before accessing them, and then *release* the intrinsic lock when it's done with them 

    - Locks In Synchronized Methods

      > When a thread invokes a synchronized method, it automatically acquires the intrinsic lock for that method's object and releases it when the method returns. The lock release occurs even if the return was caused by an uncaught exception. 
      >
      > Thus access to class's static fields is controlled by a lock that's distinct from the lock for any instance of the class. 

    - Synchronized Statements

      ```java
      synchronized(this) {
          //needs to avoid synchronizing invocations of other objects' methods
      }
      
      //Synchronized statements are also useful for improving concurrency with fine-grained synchronization,there's no reason to prevent an update of c1 from being interleaved with an update of c2
      public class MsLunch {
          private long c1 = 0;
          private long c2 = 0;
          private Object lock1 = new Object();
          private Object lock2 = new Object();
      
          public void inc1() {
              synchronized(lock1) {
                  c1++;
              }
          }
      
          public void inc2() {
              synchronized(lock2) {
                  c2++;
              }
          }
      }
      
      ```

    - Reentrant Synchronization

      >Recall that a thread cannot acquire a lock owned by another thread.

      

