---
layout: post
title:  限流
date:   2019-05-23 21:52:12 +08:00
category: 开发经验
tags: 开发经验
comments: true
---

* content
{:toc}


互联网高并发项目往往通过加服务器来提高性能，但是加服务器一般都是支持平时的并发量，当并发量达到峰值以后，加服务器一个是消耗资源、二是增加集群的维护成本。为了解决突发情况引入了限流。




















## 什么是限流和降级

在开发高并发系统时，有很多手段来保护系统：缓存、降级、限流。

当访问量快速增长、服务可能会出现一些问题（响应超时），或者会存在非核心服务影响到核心流程的性能时， 仍然需要保证服务的可用性，即便是有损服务。

所以意味着我们在设计服务的时候，需要一些手段或者关键数据进行自动降级，或者配置人工降级的开关。

缓存的目的是提升系统访问速度和增大系统处理的容量，可以说是抗高并发流量的银弹；

降级是当服务出问题或者影响到核心流程的性能则需要暂时屏蔽掉某些功能，等高峰或者问题解决后再打开；

而有些场景并不能用缓存和降级来解决，比如秒杀、抢购；写服务（评论、下单）、频繁的复杂查询，因此需要一种手段来限制这些场景的并发/请求量

### 降级

对于高可用服务的设计，有一个很重要的设计，那就是降级。降级一般有几种实现手段，自动降级和人工降级

1 通过配置降级开关，实现对流程的控制

2 前置化降级开关， 基于 OpenResty+配置中心实现降级

3 业务降级，在大促的时候，我们会有限保证核心业务的流程可用，也就是下单支付。同时，我们也会对核心的支付流程采取一些异步化的方式来提升吞吐量

### 限流

限流的目的是防止恶意请求流量、恶意攻击、或者防止流量超过系统峰值

限流是对资源访问做控制的一个组件或者功能，那么控制这块主要有两个功能：

限流策略和熔断策略，对于熔断策略，不同的系统有不同的熔断策略诉求，有得系统希望直接拒绝服务、有的系统希望排队等待、有的系统希望服务降级。限流服务

这块有两个核心概念：资源和策略

资源：被流量控制的对象，比如接口

策略：限流策略由限流算法和可调节的参数两部份组成

限流的目的是通过对并发访问/请求进行限速或者一个时间窗口内的请求进行限速来保护系统，一旦达到限制速率则可以拒绝服务（定向到错误页或者告知资源没有了）、排队或等待(秒杀、下单)、降级（返回兜底数据或默认数据或默认数据，如商品详情页库存默认有货）

滑动窗口协议是传输层进行流控的一种措施，接收方通过通告发送方自己的窗口大小，从而控制发送方的发送速度，从而达到防止发送方发送速度过快而导致自己被淹没的目的。

简单解释下，发送和接受方都会维护一个数据帧的序列，这个序列被称作窗口。发送方的窗口大小由接受方确定，目的在于控制发送速度，以免接受方的缓存不够大，而导致溢出，同时控制流量也可以避免网络拥塞。

下面图中的 4,5,6 号数据帧已经被发送出去，但是未收到关联的 ACK，7,8,9 帧则是等待发送。可以看出发送端的窗口大小为 6，这是由接受端告知的。此时如果发送端收到 4 号 ACK，则窗口的左边缘向右收缩，窗口的右边缘则向右扩展，此时窗口就向前“滑动了”，即数据帧10 也可以被发送

https://media.pearsoncmg.com/aw/ecs_kurose_compnetwork_7/cw/content/interactiveanimations/selective-repeat-protocol/index.html

### 漏铜

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/xianliu001.png)

桶本身具有一个恒定的速率往下漏水，而上方时快时慢的会有水进入桶内。当桶还未满时，上方的水可以加入。一旦水满，上方的水就无法加入。桶满正是算法中的一个关键的触发条件（即流量异常判断成立的条件）。而此条件下如何处理上方流下来的水，有两种方式,在桶满水之后，常见的两种处理方式为：

1）暂时拦截住上方水的向下流动，等待桶中的一部分水漏走后，再放行上方水。

2）溢出的上方水直接抛弃。

特点

1 漏水的速率是固定的

2 即使存在注水 burst（突然注水量变大）的情况，漏水的速率也是固定的

### 令牌桶(能够解决突发流量)

令牌桶算法是网络流量整形（Traffic Shaping）和速率限制（Rate Limiting）中最常使用的一种算法。典型情况下，令牌桶算法用来控制发送到网络上的数据的数目，并允许突发数据的发送。

令牌桶是一个存放固定容量令牌（token）的桶，按照固定速率往桶里添加令牌; 令牌桶算法实际上由三部分组成：两个流和一个桶，分别是令牌流、数据流和令牌桶

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/xianliu002.png)

## 代码实现

### Guava 的 RateLimiter 实现

在 Guava 中 RateLimiter 的实现有两种： Bursty 和 WarmUp

- bursty

bursty 是基于 token bucket 的算法实现，比如


```java

RateLimiter rateLimiter=RateLimiter.create(permitPerSecond); //创建一个 bursty实例
rateLimiter.acquire(); //获取 1 个 permit，当令牌数量不够时会阻塞直到获取为止

```

- WarmingUp
 1 基于 Leaky bucket 算法实现

 2 QPS 是固定的

 3 使用于需要预热时间的使用场景

 ```java

 RateLimiter create(double permitsPerSecond, long warmupPeriod, TimeUnit unit)
 //创建一个 SmoothWarmingUp 实例；warmupPeriod 是指预热的时间
 RateLimiter rateLimiter
 =RateLimiter.create(permitsPerSecond,warmupPeriod,timeUnit);
 rateLimiter.acquire();//获取 1 个 permit；可能会被阻塞止到获取到为止


 ```

## 代码实践

```java

public class TokenDemo {
  private int qps;
  private int countOfReq;
  private RateLimiter rateLimiter;
  public TokenDemo(int qps, int countOfReq) {
  this.qps = qps;
  this.countOfReq = countOfReq;
  }
  public TokenDemo processWithTokenBucket(){
  rateLimiter=RateLimiter.create(qps);
  return this;
  }
  public TokenDemo processWithLeakyBucket(){  rateLimiter=RateLimiter.create(qps,00,TimeUnit.MILLISECONDS);
  return this;
  }
  private void processRequest(){
  System.out.println("RateLimiter:"+rateLimiter.getClass());
  long start=System.currentTimeMillis();
  for(int i=0;i<countOfReq;i++){
  rateLimiter.acquire();
  }
  long end=System.currentTimeMillis()-start;
  System.out.println("处理请求数量:"+countOfReq+"," +
  "耗时："+end+"," +
  "qps:"+rateLimiter.getRate()+"," +
  "实际 qps："+Math.ceil(countOfReq / (end / 1000.00)));
  }
  public void doProcess() throws InterruptedException {
  for(int i=0;i<20;i=i+5){
  TimeUnit.SECONDS.sleep(i);
  processRequest();
  }
  }
  public static void main(String[] args) throws InterruptedException
{
  new TokenDemo(50,100).processWithTokenBucket().doProcess();
  new TokenDemo(50,100).processWithLeakyBucket().doProcess();
  }
}


```


## 分布式下的限流策略

### 技术选型

mysql:存储限流策略的参数等元数据

redis+lua:令牌桶算法实现

具体实现

参考 Redisson 中的令牌桶实现逻辑即可

简陋的设计思路：假设一个用户（用IP判断）每分钟访问某一个服务接口的次数不能超过10次，那么我们可以在Redis中创建一个键，并此时我们就设置键的过期时间为60秒，每一个用户对此服务接口的访问就把键值加1，在60秒内当键值增加到10的时候，就禁止访问服务接口。在某种场景中添加访问时间间隔还是很有必要的。

    1）使用Redis的incr命令，将计数器作为Lua脚本

    ```

    local current
     current = redis.call("incr",KEYS[1])
     if tonumber(current) == 1 then
         redis.call("expire",KEYS[1],1)
     end

    ```        
      Lua脚本在Redis中运行，保证了incr和expire两个操作的原子性。

     2）使用Reids的列表结构代替incr命令

```

FUNCTION LIMIT_API_CALL(ip)
current = LLEN(ip)
IF current > 10 THEN
    ERROR "too many requests per second"
ELSE
    IF EXISTS(ip) == FALSE
        MULTI
        
            RPUSH(ip,ip)
            EXPIRE(ip,1)
        EXEC
    ELSE
        RPUSHX(ip,ip)
    END
    PERFORM_API_CALL()
END

```

  Rate Limit使用Redis的列表作为容器，LLEN用于对访问次数的检查，一个事物中包含了RPUSH和EXPIRE两个命令，用于在第一次执行计数是创建列表并设置过期时间，

  RPUSHX在后续的计数操作中进行增加操作。
