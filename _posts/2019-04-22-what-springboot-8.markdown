---
layout: post
title:  BIO/NIO/AIO之网络IO
date:   2019-04-22 21:52:12 +08:00
category: 疑难点
tags: 必背常识
comments: true
---

* content
{:toc}


要求(理解)。























## bio

bio是block io

io 分两种情况：从网络读写、从文件读写

从网络读写才会有bio的概念，从文件读取系统会假设不存在阻塞的情况（因为无法预见磁盘的特殊情况）。

所以谈论bio只是在说网络io

在网络中read / write /connect 一类的系统调用时都会卡主，所以叫阻塞。例如，reed网络中的数据时，无法预知对方已经发送数据，在收到数据之前，能做的只有等待，直到数据发送过来，或者超时。

对于单线程的网络服务，这样就会存在卡死的问题。由于现在的系统是多任务的操作系统，任务直接的切换是抢占时间片执行的。所以不会影响其他进程的运行，阻塞只会阻塞本进程。

so,网络服务为了能响应多客户请求，就必须是多线程的。每个线程处理一个客户的网络请求。2000年之前都是这么做的，线程会越来越多，线程越多，线程的上下文切换就越多，上下文切换由于需要保存当前线程的运行时状态和数据，所以消耗了cpu，造成浪费。

每个线程都会占用线程栈，每个栈是占空间的，那么浪费空间了。

其实以上的问题，其实是调用read时，卡主，没数据时，实际上是可以干别的，别当个二傻子，一直瞎等着。使用多线程其实就是仅仅因为block发送。你聪明一点搞个线程池，但是，一旦超过线程池的数量的响应后还是会block。为了让程序向人类一样智能，那么就得改了。

就是当你读数据时，操作系统告诉你，没有，你就干别的去，而不是干等着。于是NIO出现了。


## NIO

NIO是指Non-Blocking

bio调用read时，如果没有数据到到，就会Block。

NIO调用read时，如果没有数据到达时，立刻返回-1，并且errno被设为eagain

底层代码：

```java

struct timespec sleep_interval{.tv_sec = 0, .tv_nsec = 1000};
ssize_t nbytes;
while (1) {
    /* 尝试读取 */
    if ((nbytes = read(fd, buf, sizeof(buf))) < 0) {
        if (errno == EAGAIN) { // 没数据到
            perror("nothing can be read");
        } else {
            perror("fatal error");
            exit(EXIT_FAILURE);
        }
    } else { // 有数据
        process_data(buf, nbytes);
    }
    // 处理其他事情，做完了就等一会，再尝试
    nanosleep(sleep_interval, NULL);
}

```

简单理解：就是轮询，不断的尝试有没有数据到达，有了之后就去处理，没有就eagain，等一小会再试，这个好处就是，不会被卡死。

好吧，问题是：

1.等待的时间不好设置，设置长了，浪费了自己的时间，设置短了，又会频繁重试，干耗cpu

2.虽然不用多线程了，但是一堆客户站在你面前问你要数据，你得一直查询，一个客户你会切换一次，去socket里查，每次切换其实就是一个上下文切换（其实就是用户态和核心态切换一次）

那么我要是操作系统，我该怎么做，一堆客户来了要问我有没有数据，我该咋办？我能不能准备一个表格，一次告诉一部分客户，你们的数据到了，其他的没到，也别来再问我了

## IO多路复用

IO多路复用：程序注册一组socket给操作系统，表示我要监听这些客户的情况，有了就告诉这些客户去处理。

IO多路复用是一个单独的事情，nio也是个单独事情（立即返回数据，或处理，或再轮询）

io多路复用其实只是操作系统的事情

那么

IO多路复用+bio呢，bio是上层，还是会卡主

IO多路复用+nio呢，有点意思了

一些错误的误解关于IO多路复用

```

1:多路复用是指多个数据流共享一个socket。明显错误，操作系统监听socket列表，客户列表

2：多路复用和nio减少了io。错误，要了解这个事情，必须清楚什么事io,io是读写过程，多路复用解决的是cpu调度的问题，都是避免浪费cpu。把系统瓶颈聚焦到网络宽带，而不是cpu和内存。那么怎么减少io呢，简单，提高网速（网线、交换机、网卡），依靠并发传输（HDFS的数据多副本并发传输）

```

那么操作系统怎么支持IO多路复用，select和poll

```
int select(int nfds, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, struct timeval *timeout);

```

分别监听：读取、写入、异常事件

代码：

```

struct timeval tv = {.tv_sec = 1, .tv_usec = 0};

ssize_t nbytes;
while(1) {
    FD_ZERO(&read_fds);
    setnonblocking(fd1);
    setnonblocking(fd2);
    FD_SET(fd1, &read_fds);
    FD_SET(fd2, &read_fds);
    // 把要监听的fd拼到一个数组里，而且每次循环都得重来一次...
    if (select(FD_SETSIZE, &read_fds, NULL, NULL, &tv) < 0) { // block住，直到有事件到达
        perror("select出错了");
        exit(EXIT_FAILURE);
    }
    for (int i = 0; i < FD_SETSIZE; i++) {
        if (FD_ISSET(i, &read_fds)) {
            /* 检测到第[i]个读取fd已经收到了，这里假设buf总是大于到达的数据，所以可以一次read完 */
            if ((nbytes = read(i, buf, sizeof(buf))) >= 0) {
                process_data(nbytes, buf);
            } else {
                perror("读取出错了");
                exit(EXIT_FAILURE);
            }
        }
    }
}

```

首先，select构建一个数组，之后用select监听read_fds中的多个socket的读取事件。调用select后会block住，直到一个事件发生了，或者等待最大1秒钟就返回。之后需要遍历所有注册的fd,挨个检查那个fd事件到达。到达的socket读取后就可以进行数据处理了。

缺点：（类似办驾照的服务大厅，分为登记交表、办卡、交钱）

1 由于创建的fd数组长度时1024，在高并发的情况下不可接受。（我是服务前台，我能查看的长度是1024，那客户大厅人不能来太多的）

2 fd数组监听事件分为3个数组，需要分别3个内存去构造，而且每次调用select前都要重设它们，调用select后，这三个数组从用户态复制一份到内核态，事件到达后要遍历3个数组。（我是前台，我监听的三个任务，登记/办卡/交钱，每次我去工作的时候都要看三个表单，并且还得对比一下进度，有了之后还有根据登记名字找到那个人，告诉他你可以下一步操作了），想要忙死我吗？不干了

3 select返回后要遍历fd，找到那个有事情发生的客户去处理，比较低效。

4 select是无状态的，每次调用select，内核都要重新检查所有被注册的fd的状态（我脑子有限，每次系统select后，，我都要查表，找客户去）


poll

poll跟select类似

优化了select的一些问题;

1 不再是三个数组了，并且不会每次重设了，个数也不是1024了（我工作能力很强，分类能力强分为红黄蓝三色，我脑子好，不用重新设置状态回位）

问题：

1 依然无状态，性能与select类似

2 应用程序仍然无法拿到有事件发生的fd，就是（我还需要查表叫号），还要遍历所有的fd

如果是追求性能的话，linux提供了epoll api。java nio，nginx都使用这些api实现的。

因为大部分服务器都是用Linux做服务器，所以以Linux为例子解释多路复用。

## 用epoll实现了IO多路复用

与select和poll不同，使用epoll只需要创建一下；

```

int epfd = epoll_create(10);

```

epoll_create在内核创建一个数据表，接口会返回一个epoll的文件描述符，指向这个表。这个设置大小是可动态调整的，不需过多关注。

为啥epoll要创建一个用文件描述符来执行的表呢？

1 epoll要有状态，不像select和poll那样每次都要重新传入所有要监听的fd,这避免了很多无畏的数据复制。epoll的数据时用接口epoll_ctl来管理增删改的

2 epoll文件描述符在进程被fork时，子进程是可以继承的。这可以给对多进程共享一份epoll数据，实现并行监听网络请求带来的遍历。

epoll创建后，第二步是使用epoll_ctl接口来注册要监听的事件。

```

int epoll_ctl(int epfd, int op, int fd, struct epoll_event *event);

```

其中第一个参数就是上面创建的epfd。第二个参数op表示如何对文件名进行操作，共有3种。

EPOLL_CTL_ADD - 注册一个事件

EPOLL_CTL_DEL - 取消一个事件的注册

EPOLL_CTL_MOD - 修改一个事件的注册

第三个参数是要操作的fd，这里必须是支持NIO的fd（比如socket）。

第四个参数是一个epoll_event的类型的数据，表达了注册的事件的具体信息。

```

typedef union epoll_data {
    void    *ptr;
    int      fd;
    uint32_t u32;
    uint64_t u64;
} epoll_data_t;

struct epoll_event {
    uint32_t     events;    /* Epoll events */
    epoll_data_t data;      /* User data variable */
};
```

比方说，想关注一个fd1的读取事件事件，并采用边缘触发(下文会解释什么是边缘触发），大概要这么写：

  ```

struct epoll_data ev;
ev.events = EPOLLIN | EPOLLET; // EPOLLIN表示读事件；EPOLLET表示边缘触发
ev.data.fd = fd1;

  ```

通过epoll_ctl就可以灵活的注册/取消注册/修改注册某个fd的某些事件。

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/epoll001.png)

第三步，使用epoll_wait来等待事件的发生。

```
int epoll_wait(int epfd, struct epoll_event *evlist, int maxevents, int timeout);

```

特别留意，这一步是"block"的。只有当注册的事件至少有一个发生，或者timeout达到时，该调用才会返回。这与select和poll几乎一致。但不一样的地方是evlist，它是epoll_wait的返回数组，里面只包含那些被触发的事件对应的fd，而不是像select和poll那样返回所有注册的fd。

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/epoll002.png)

综合起来，一段比较完整的epoll代码大概是这样的。

```

#define MAX_EVENTS 10
struct epoll_event ev, events[MAX_EVENTS];
int nfds, epfd, fd1, fd2;

// 假设这里有两个socket，fd1和fd2，被初始化好。
// 设置为non blocking
setnonblocking(fd1);
setnonblocking(fd2);

// 创建epoll
epfd = epoll_create(MAX_EVENTS);
if (epollfd == -1) {
    perror("epoll_create1");
    exit(EXIT_FAILURE);
}

//注册事件
ev.events = EPOLLIN | EPOLLET;
ev.data.fd = fd1;
if (epoll_ctl(epollfd, EPOLL_CTL_ADD, fd1, &ev) == -1) {
    perror("epoll_ctl: error register fd1");
    exit(EXIT_FAILURE);
}
if (epoll_ctl(epollfd, EPOLL_CTL_ADD, fd2, &ev) == -1) {
    perror("epoll_ctl: error register fd2");
    exit(EXIT_FAILURE);
}

// 监听事件
for (;;) {
    nfds = epoll_wait(epdf, events, MAX_EVENTS, -1);
    if (nfds == -1) {
        perror("epoll_wait");
        exit(EXIT_FAILURE);
    }

    for (n = 0; n < nfds; ++n) { // 处理所有发生IO事件的fd
        process_event(events[n].data.fd);
        // 如果有必要，可以利用epoll_ctl继续对本fd注册下一次监听，然后重新epoll_wait
    }
}

```

所有的基于IO多路复用的代码都会遵循这样的写法：注册——监听事件——处理——再注册，无限循环下去。

## epoll的优势

我猜：

为啥要强：

1 不需要在遍历所有的监听socket了，我底层操作系统会返回该进程中有事件发生的socket列表（比如驾照办好了，它就给我/前台,所有的驾照，我根据驾照上的名字喊，而不是在轮询所有客户的状态）

2 由于进程中每个客户都是有状态的，所以我区分更加清楚，而不是拿个表格去一一匹配。

实际（官方术语）：

1 select和poll每次都需要把完成的fd列表传入内核，迫使内核每次从头扫到尾。epoll完全反过来，epoll在内核的数据建立好了之后，每次某个被监听的fd一旦有事件发生，内核就直接标记。epoll_wait调用时，会尝试直接读取到当前已经标好的fd列表，如果没有就会进入等待状态。

 同时,epoll_wait 直接只返回了被触发的fd列表，这样上层应用起来也好用，再不用从大朗的注册的fd中筛选出fd了。

 简单说，select和poll的代价是（所有注册的fd的数量），而epoll的代价是（发生时间fd的数量）

 于是，高性能网络服务器的场景特别适合用epoll来实现——因为大多数网络服务器都有这样的模式：同时要监听大量（几千，几万，几十万甚至更多）的网络连接，但是短时间内发生的事件非常少。

 但是，如果发生事件的数量接近所有注册的数量，那么epoll的优势就没了，效率差不多了。

 上面的优势主要指性能优势。还有一个优点：水平触发和边沿触发

 水平触发和边沿触发

 默认epoll是使用水平触发，这与select和poll的行为完全一致。在水平触发下，epoll顶多算是“跑得更快的poll”

 而一旦在注册事件时使用了EPOLLET标记（如上文中的例子），那么将其视为边沿触发（或者有地方叫边缘触发，一个意思）。那么到底什么水平触发和边沿触发呢？

 考虑下图中的例子。有两个socket的fd——fd1和fd2。我们设定监听f1的“水平触发读事件“，监听fd2的”边沿触发读事件“。我们使用在时刻t1，使用epoll_wait监听他们的事件。在时刻t2时，两个fd都到了100bytes数据，于是在时刻t3, epoll_wait返回了两个fd进行处理。在t4，我们故意不读取所有的数据出来，只各自读50bytes。然后在t5重新注册两个事件并监听。在t6时，只有fd1会返回，因为fd1里的数据没有读完，仍然处于“被触发”状态；而fd2不会被返回，因为没有新数据到达。

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/epoll004.png)

这个例子很明确的显示了水平触发和边沿触发的区别。

水平触发只关心文件描述符中是否还有没完成处理的数据，如果有，不管怎样epoll_wait，总是会被返回。简单说——水平触发代表了一种“状态”。

边沿触发只关心文件描述符是否有新的事件产生，如果有，则返回；如果返回过一次，不管程序是否处理了，只要没有新的事件产生，epoll_wait不会再认为这个fd被“触发”了。简单说——边沿触发代表了一个“事件”。

那么边沿触发怎么才能迫使新事件产生呢？一般需要反复调用read/write这样的IO接口，直到得到了EAGAIN错误码，再去尝试epoll_wait才有可能得到下次事件。



那么为什么需要边沿触发呢？

边沿触发把如何处理数据的控制权完全交给了开发者，提供了巨大的灵活性。比如，读取一个http的请求，开发者可以决定只读取http中的headers数据就停下来，然后根据业务逻辑判断是否要继续读（比如需要调用另外一个服务来决定是否继续读）。而不是次次被socket尚有数据的状态烦扰；写入数据时也是如此。比如希望将一个资源A写入到socket。当socket的buffer充足时，epoll_wait会返回这个fd是准备好的。但是资源A此时不一定准备好。如果使用水平触发，每次经过epoll_wait也总会被打扰。在边沿触发下，开发者有机会更精细的定制这里的控制逻辑。

但不好的一面时，边沿触发也大大的提高了编程的难度。

一不留神，可能就会miss掉处理部分socket数据的机会。如果没有很好的根据EAGAIN来“重置”一个fd，就会造成此fd永远没有新事件产生，进而导致饿死相关的处理代码。


## 再来思考一下什么是“Block”


上面的所有介绍都在围绕如何让网络IO不会被Block。但是网络IO处理仅仅是整个数据处理中的一部分。如果你留意到上文例子中的“处理事件”代码，就会发现这里可能是有问题的。

处理代码有可能需要读写文件，可能会很慢，从而干扰整个程序的效率；
处理代码有可能是一段复杂的数据计算，计算量很大的话，就会卡住整个执行流程；
处理代码有bug，可能直接进入了一段死循环……

这时你会发现，这里的Block和本文之初讲的O_NONBLOCK是不同的事情。在一个网络服务中，如果处理程序的延迟远远小于网络IO，那么这完全不成问题。但是如果处理程序的延迟已经大到无法忽略了，就会对整个程序产生很大的影响。这时IO多路复用已经不是问题的关键。
试分析和比较下面两个场景：

web proxy。程序通过IO多路复用接收到了请求之后，直接转发给另外一个网络服务。
web server。程序通过IO多路复用接收到了请求之后，需要读取一个文件，并返回其内容。

它们有什么不同？它们的瓶颈可能出在哪里？

总结

小结一下本文：

对于socket的文件描述符才有所谓BIO和NIO。
多线程+BIO模式会带来大量的资源浪费，而NIO+IO多路复用可以解决这个问题。
在Linux下，基于epoll的IO多路复用是解决这个问题的最佳方案；epoll相比select和poll有很大的性能优势和功能优势，适合实现高性能网络服务。

但是IO多路复用仅仅是解决了一部分问题，另外一部分问题如何解决呢？且听下回分解。
