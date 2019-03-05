---
layout: post
title:  分布式协调之zookeeper概念二
date:   2019-03-04 22:52:12 +08:00
category: 高并发分布式
tags: zookeeper
comments: true
---

* content
{:toc}

安装好zk后，就是简单的crud简单操作，这里不介绍这部分，这里将要介绍zk的基本原理。












## zk的由来

  分布式系统的很多难题（服务发现、服务监听、服务的调用、负载等），都是由于缺少协调机制造成的。  

  在分布式协调这块做得比较好的，有 Google 的 Chubby 以及 Apache 的 Zookeeper。

  Google Chubby 是一个分布式锁服务，通过 GoogleChubby 来解决分布式协作、 Master 选举等与分布式锁服务相关的问题。

  Zookeeper 也是类似，因为当时在雅虎内部的很多系统都需要依赖一个系统来进行分布式协调，但是谷歌的Chubby是不开源的，所以后来雅虎基于 Chubby 的思想开发了zookeeper，并捐赠给了 Apache。在上面这个架构下 zookeeper 以后，可以用来解决 task 执行问题，各个服务先去 zookeeper 上去注册节点，然后获得权限以后再来访问 task

  - zk的设计猜想

  zookeeper 主要是解决分布式环境下的服务协调问题而产生的，如果我们要去实现一个 zookeeper 这样的中间件，我们需要做什么？

  1 防止单点故障

  如果要防止 zookeeper 这个中间件的单点故障，那就势必要做集群。而且这个集群如果要满足高性能要求的话，还得是一个高性能高可用的集群。高性能意味着这个集群能够分担客户端的请求流量，高可用意味着集群中的某一个节点宕机以后，不影响整个集群的数据和继续提供服务的可能性。

  结论： 所以这个中间件需要考虑到集群,而且这个集群还需要分摊客户端的请求流量

  2 接着上面那个结论再来思考，如果要满足这样的一个高性能集群

  我们最直观的想法应该是，每个节点都能接收到请求，并且每个节点的数据都必须要保持一致。要实现各个节点的数据一致性，就势必要一个 leader 节点责协调和数据同步操作。这个我想大家都知道，如果在这样一个集群中没有 leader 节点，每个节点都可以接收所有请求，那么这个集群的数据同步的复杂度是非常大。

  结论：所以这个集群中涉及到数据同步以及会存在leader 节点

  3 继续，如何在这些节点中选举出 leader 节点，以及leader 挂了以后，如何恢复呢？

  结论：所以 zookeeper 用了基于 paxos 理论所衍生出来的 ZAB 协议

  4 leader 节点如何和其他节点保证数据一致性，并且要求是强一致的。

  在分布式系统中，每一个机器节点虽然都能够明确知道自己进行的事务操作过程是成功和失败，但是却无法直接获取其他分布式节点的操作结果。所以当一个事务操作涉及到跨节点的时候，就需要用到分布式事务，分布式事务的数据一致性协议有 2PC 协议和3PC 协议。

  我们基本上知道 zookeeper 为什么要用到zab 理论来做选举、为什么要做集群、为什么要用到分布式事务来实现数据一致性了。接下来我们逐步去剖析zookeeper 里面的这些内容

## 关于 2PC 提交

  （Two Phase Commitment Protocol）当一个事务操作需要跨越多个分布式节点的时候，为了保持事务处理的 ACID特性，就需要引入一个“协调者”（TM）来统一调度所有分布式节点的执行逻辑，这些被调度的分布式节点被称为 AP。TM 负责调度 AP 的行为，并最终决定这些 AP 是否要把事务真正进行提交；因为整个事务是分为两个阶段提交，所以叫 2pc。

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/2pc.png)

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/2pc1.png)


  阶段一：提交事务请求（投票）

  1 事务询问

  协调者向所有的参与者发送事务内容，询问是否可以执行事务提交操作，并开始等待各参与者的响应

  2 执行事务

  各个参与者节点执行事务操作，并将 Undo 和 Redo 信息记录到事务日志中，尽量把提交过程中所有消耗时间的操作和准备都提前完成确保后面 100%成功提交事务

  3 各个参与者向协调者反馈事务询问的响应

  如果各个参与者成功执行了事务操作，那么就反馈给参与者yes 的响应，表示事务可以执行；如果参与者没有成功执行事务，就反馈给协调者 no 的响应，表示事务不可以执行，

  上面这个阶段有点类似协调者组织各个参与者对一次事务操作的投票表态过程，因此 2pc 协议的第一个阶段称为“投票阶段”，即各参与者投票表名是否需要继续执行接下去的事务提交操作。

  阶段二：执行事务提交

  在这个阶段，协调者会根据各参与者的反馈情况来决定最终是否可以进行事务提交操作，正常情况下包含两种可能:执行事务、中断事务

## zookeeper 的集群

在 zookeeper 中，客户端会随机连接到 zookeeper 集群中的一个节点，如果是读请求，就直接从当前节点中读取数据，如果是写请求，那么请求会被转发给 leader 提交事务，然后 leader 会广播事务，只要有超过半数节点写入成功，那么写请求就会被提交（类 2PC 事务）

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/zkcluster.png)

  所有事务请求必须由一个全局唯一的服务器来协调处理，这个服务器就是 Leader 服务器，其他的服务器就是follower。leader 服务器把客户端的失去请求转化成一个事务 Proposal（提议），并把这个 Proposal 分发给集群中的所有 Follower 服务器。之后 Leader 服务器需要等待所有Follower 服务器的反馈，一旦超过半数的 Follower 服务器进行了正确的反馈，那么 Leader 就会再次向所有的Follower 服务器发送 Commit 消息，要求各个 follower 节点对前面的一个 Proposal 进行提交;

  - 集群角色
    - Leader 角色

    Leader 服务器是整个 zookeeper 集群的核心，主要的工作任务有两项

    1 事物请求的唯一调度和处理者，保证集群事务处理的顺序性

    2 集群内部各服务器的调度者

    - Follower 角色

    Follower 角色的主要职责是

    1 处理客户端非事物请求、转发事物请求给 leader 服务器

    2 参与事物请求 Proposal 的投票（需要半数以上服务器通过才能通知 leader commit 数据; Leader 发起的提案，要求 Follower 投票）

    3 参与 Leader 选举的投票

    - Observer 角色

    Observer 是 zookeeper3.3 开始引入的一个全新的服务器角色，从字面来理解，该角色充当了观察者的角色。观察 zookeeper 集群中的最新状态变化并将这些状态变化同步到 observer 服务器上。Observer 的工作原理与follower 角色基本一致，

    而它和 follower 角色唯一的不同在于 observer 不参与任何形式的投票，包括事物请求Proposal的投票和leader选举的投票。简单来说，observer服务器只提供非事物请求服务，通常在于不影响集群事物处理能力的前提下提升集群非事物处理的能力


    集群组成

    通常 zookeeper 是由 2n+1 台 server 组成，每个 server 都知道彼此的存在。对于 2n+1 台 server，只要有 n+1 台（大多数）server 可用，整个系统保持可用。我们已经了解到，一个 zookeeper 集群如果要对外提供可用的服务，那么集群中必须要有过半的机器正常工作并且彼此之间能够正常

    通信，基于这个特性，如果向搭建一个能够允许 F 台机器down 掉的集群，那么就要部署 2*F+1 台服务器构成的zookeeper 集群。因此 3 台机器构成的 zookeeper 集群，能够在挂掉一台机器后依然正常工作。一个 5 台机器集群的服务，能够对 2 台机器怪调的情况下进行容灾。如果一台由 6 台服务构成的集群，同样只能挂掉 2 台机器。因此，5 台和 6 台在容灾能力上并没有明显优势，反而增加了网络通信负担。系统启动时，集群中的 server 会选举出一台server 为 Leader，其它的就作为 follower（这里先不考虑observer 角色）。

    之所以要满足这样一个等式，是因为一个节点要成为集群中的 leader，需要有超过及群众过半数的节点支持，这个涉及到 leader 选举算法。同时也涉及到事务请求的提交投票

## ZAB 协议

  ZAB（Zookeeper Atomic Broadcast） 协议是为分布式协调服务 ZooKeeper 专门设计的一种支持崩溃恢复的原子广播协议。在 ZooKeeper 中，主要依赖 ZAB 协议来实现分布式数据一致性，基于该协议，ZooKeeper 实现了一种主备模式的系统架构来保持集群中各个副本之间的数据一致性。

  ZAB 协议包含两种基本模式，分别是

  1 崩溃恢复

  2 原子广播

  当整个集群在启动时，或者当 leader 节点出现网络中断、崩溃等情况时，ZAB 协议就会进入恢复模式并选举产生新的 Leader，当 leader 服务器选举出来后，并且集群中有过半的机器和该 leader 节点完成数据同步后（同步指的是数据同步，用来保证集群中过半的机器能够和 leader 服务器的数据状态保持一致），ZAB 协议就会退出恢复模式。

  当集群中已经有过半的 Follower 节点完成了和 Leader 状态同步以后，那么整个集群就进入了消息广播模式。这个时候，在 Leader 节点正常工作时，启动一台新的服务器加入到集群，那这个服务器会直接进入数据恢复模式，和leader 节点进行数据同步。同步完成后即可正常对外提供非事务请求的处理。

  消息广播的实现原理

  如果大家了解分布式事务的 2pc 和 3pc 协议的话（不了解也没关系，我们后面会讲），消息广播的过程实际上是一个简化版本的二阶段提交过程

  1 leader 接收到消息请求后，将消息赋予一个全局唯一的64 位自增 id，叫：zxid，通过 zxid 的大小比较既可以实现因果有序这个特征

  2 leader 为每个 follower 准备了一个 FIFO 队列（通过 TCP协议来实现，以实现了全局有序这一个特点）将带有 zxid的消息作为一个提案（proposal）分发给所有的 follower

  3 当 follower 接收到 proposal，先把 proposal 写到磁盘，写入成功以后再向 leader 回复一个 ack

  4 当 leader 接收到合法数量（超过半数节点）的 ACK 后，leader 就会向这些 follower 发送 commit 命令，同时会在本地执行该消息

  5 当 follower 收到消息的 commit 命令以后，会提交该消息

  leader 的投票过程，不需要 Observer 的 ack，也就是Observer 不需要参与投票过程，但是 Observer 必须要同步 Leader 的数据从而在处理请求的时候保证数据的一致性

## 崩溃恢复(数据恢复)  

  ZAB 协议的这个基于原子广播协议的消息广播过程，在正常情况下是没有任何问题的，但是一旦 Leader 节点崩溃，或者由于网络问题导致 Leader 服务器失去了过半的Follower 节点的联系（leader 失去与过半 follower 节点联系，可能是 leader 节点和 follower 节点之间产生了网络分区，那么此时的 leader 不再是合法的 leader 了），那么就会进入到崩溃恢复模式。在 ZAB 协议中，为了保证程序的正确运行，整个恢复过程结束后需要选举出一个新的Leader为了使 leader 挂了后系统能正常工作，需要解决以下两个问题

  1 已经被处理的消息不能丢失

  当 leader 收到合法数量 follower 的 ACKs 后，就向各个 follower 广播 COMMIT 命令，同时也会在本地执行 COMMIT 并向连接的客户端返回「成功」。但是如果在各个 follower 在收到 COMMIT 命令前 leader 就挂了，导致剩下的服务器并没有执行都这条消息。

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/commit1.png)

  leader 对事务消息发起 commit 操作，但是该消息在follower1 上执行了，但是 follower2 还没有收到 commit，就已经挂了，而实际上客户端已经收到该事务消息处理成功的回执了。所以在 zab 协议下需要保证所有机器都要执行这个事务消息  

  2 被丢弃的消息不能再次出现

  当 leader 接收到消息请求生成 proposal 后就挂了，其他 follower 并没有收到此 proposal，因此经过恢复模式重新选了 leader 后，这条消息是被跳过的。 此时，之前挂了的 leader 重新启动并注册成了 follower，他保留了被跳过消息的 proposal 状态，与整个系统的状态是不一致的，需要将其删除。

  ZAB 协议需要满足上面两种情况，就必须要设计一个leader 选举算法：能够确保已经被 leader 提交的事务Proposal能够提交、同时丢弃已经被跳过的事务Proposal。

  针对这个要求

  1 如果 leader 选举算法能够保证新选举出来的 Leader 服务器拥有集群中所有机器最高编号（ZXID 最大）的事务Proposal，那么就可以保证这个新选举出来的 Leader 一定具有已经提交的提案。因为所有提案被 COMMIT 之前必须有超过半数的 follower ACK，即必须有超过半数节点的服务器的事务日志上有该提案的 proposal，因此，只要有合法数量的节点正常工作，就必然有一个节点保存了所有被 COMMIT 消息的 proposal 状态

  另外一个，zxid 是 64 位，高 32 位是 epoch 编号，每经过一次 Leader 选举产生一个新的 leader，新的 leader 会将epoch 号+1，低 32 位是消息计数器，每接收到一条消息这个值+1，新 leader 选举后这个值重置为 0.这样设计的好处在于老的 leader 挂了以后重启，它不会被选举为 leader

  因此此时它的 zxid 肯定小于当前新的 leader。当老的leader 作为 follower 接入新的 leader 后，新的 leader 会让它将所有的拥有旧的 epoch 号的未被 COMMIT 的proposal 清除

  关于 ZXID

  zxid，也就是事务 id，

  为了保证事务的顺序一致性，zookeeper 采用了递增的事务 id 号（zxid）来标识事务。所有的提议（proposal）都在被提出的时候加上了 zxid。实现中 zxid 是一个 64 位的数字，它高 32 位是 epoch（ZAB 协议通过 epoch 编号来区分 Leader 周期变化的策略）用来标识 leader 关系是否改变，每次一个 leader 被选出来，它都会有一个新的epoch=（原来的 epoch+1），标识当前属于那个 leader 的统治时期。低 32 位用于递增计数。

  epoch：可以理解为当前集群所处的年代或者周期，每个leader 就像皇帝，都有自己的年号，所以每次改朝换代，leader 变更之后，都会在前一个年代的基础上加 1。这样就算旧的 leader 崩溃恢复之后，也没有人听他的了，因为follower 只听从当前年代的 leader 的命令。epoch 的变化大家可以做一个简单的实验，

  1 启动一个 zookeeper 集群。

  2 在 /tmp/zookeeper/VERSION-2 路 径 下 会 看 到 一 个currentEpoch 文件。文件中显示的是当前的 epoch

  3 把 leader 节点停机，这个时候在看 currentEpoch 会有变化。 随着每次选举新的 leader，epoch 都会发生变化

## leader 选举

Leader 选举会分两个过程启动的时候的 leader 选举、 leader 崩溃的时候的的选举

- 服务器启动时的 leader 选举

  每个节点启动的时候状态都是 LOOKING，处于观望状态，接下来就开始进行选主流程进行 Leader 选举，至少需要两台机器（具体原因前面已经
讲过了），我们选取 3 台机器组成的服务器集群为例。在集群初始化阶段，当有一台服务器 Server1 启动时，它本身是无法进行和完成 Leader 选举，当第二台服务器 Server2 启动时，这个时候两台机器可以相互通信，每台机器都试图找到 Leader，于是进入 Leader 选举过程。选举过程如下

  (1) 每个 Server 发出一个投票。由于是初始情况，Server1和 Server2 都会将自己作为 Leader 服务器来进行投票，每次投票会包含所推举的服务器的 myid 和 ZXID、epoch，使用(myid, ZXID,epoch)来表示，此时 Server1的投票为(1, 0)，Server2 的投票为(2, 0)，然后各自将这个投票发给集群中其他机器。

  (2) 接受来自各个服务器的投票。集群的每个服务器收到投票后，首先判断该投票的有效性，如检查是否是本轮投票（epoch）、是否来自LOOKING状态的服务器。

  (3) 处理投票。针对每一个投票，服务器都需要将别人的投票和自己的投票进行 PK，PK 规则如下

  i. 优先检查 ZXID。ZXID 比较大的服务器优先作为Leader

  ii. 如果 ZXID 相同，那么就比较 myid。myid 较大的服务器作为 Leader 服务器。对于 Server1 而言，它的投票是(1, 0)，接收 Server2
的投票为(2, 0)，首先会比较两者的 ZXID，均为 0，再比较 myid，此时 Server2 的 myid 最大，于是更新自己的投票为(2, 0)，然后重新投票，对于 Server2 而言，它不需要更新自己的投票，只是再次向集群中所有机器发出上一次投票信息即可。

  (4) 统计投票。每次投票后，服务器都会统计投票信息，判断是否已经有过半机器接受到相同的投票信息，对于 Server1、Server2 而言，都统计出集群中已经有两台机器接受了(2, 0)的投票信息，此时便认为已经选出了 Leader。

  (5) 改变服务器状态。一旦确定了 Leader，每个服务器就会更新自己的状态，如果是 Follower，那么就变更为FOLLOWING，如果是 Leader，就变更为 LEADING。

  - 运行过程中的 leader 选举

  当集群中的 leader 服务器出现宕机或者不可用的情况时，那么整个集群将无法对外提供服务，而是进入新一轮的Leader 选举，服务器运行期间的 Leader 选举和启动时期的 Leader 选举基本过程是一致的。

  (1) 变更状态。Leader 挂后，余下的非 Observer 服务器都会将自己的服务器状态变更为 LOOKING，然后开始进入 Leader 选举过程。

  (2) 每个 Server 会发出一个投票。在运行期间，每个服务器上的 ZXID 可能不同，此时假定 Server1 的 ZXID 为123，Server3的ZXID为122；在第一轮投票中，Server1和 Server3 都会投自己，产生投票(1, 123)，(3, 122)，然后各自将投票发送给集群中所有机器。接收来自各个服务器的投票。与启动时过程相同。

  (3) 处理投票。与启动时过程相同，此时，Server1 将会成为 Leader。

  (4) 统计投票。与启动时过程相同。

  (5) 改变服务器的状态。与启动时过程相同
