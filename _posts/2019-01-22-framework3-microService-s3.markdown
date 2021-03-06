---
layout: post
title:  微服务架构三
date:   2019-01-22 21:52:12 +08:00
category: 微服务架构
tags: 微服务 概念
comments: true
---

* content
{:toc}

对于微服务来说，SOA也要了解一些概念，重点是领域驱动设计及业务驱动划分！












## SOA 架构和微服务架构

SOA 全称（Service Oriented Architecture），中文意思为`“面向服务的架构”`，他是一种设计方法，其中包含多个服务，服务之间通过相互依赖最终提供一系列的功能。一个服务通常以独立的形式存在与操作系统进程中。各个服务之间通过网络调用跟 SOA 相提并论的还有一个 ESB（企业服务总线），简单来说 ESB 就是一根管道，用来连接各个服务节点。为了集成不同系统，不同协议的服务，ESB 做了消息的转化解释和路由工作，让不同的服务互联互通；

- SOA 所解决的核心问题

1. 系统集成：站在系统的角度，解决企业系统间的通信问题，把原先散乱、无规划的系统间的网状结构，梳理成规整、可治理的系统间星形结构，这一步往往需要引入一些产品，比如 ESB、以及技术规范、服务管理规范；这一步解决的核心问题是【有序】

2. 系统的服务化：站在功能的角度，把业务逻辑抽象成可复用、可组装的服务，通过服务的编排实现业务的快速再生，目的：把原先固有的业务功能转变为通用的业务服务，实现业务逻辑的快速复用；这一步解决的核心问题是【复用】

3. 业务的服务化：站在企业的角度，把企业职能抽象成可复用、可组装的服务；把原先职能化的企业架构转做技术人的指路明灯，做职场生涯的精神导师变为服务化的企业架构，进一步提升企业的对外服务能力；“前面两步都是从技术层面来解决系统调用、系统功能复用的问题”。第三步，则是以业务驱动把一个业务单元封装成一项服务。这一步解决的核心问题是【高效】

- 微服务架构
微服务架构其实和 SOA 架构类似,微服务是在 SOA 上做的升华，微服务架构强调的一个重点是`“业务需要彻底的组件化和服务化”`，原有的单个业务系统会拆分为多个可以独立开发、设计、运行的小应用。这些小应用之间通过服务完成交互和集成。组件表示一个可以独立更换和升级的单元，就像 PC 中的CPU、内存、显卡、硬盘一样，独立且可以更换升级而不影响其他单元。如果我们把 PC 作为组件以服务的方式构建，那么这台 PC 只需要维护主板和一些必要的外部设备。CPU、内存、硬盘都是以组件方式提供服务，PC 需要调用 CPU 做计算处理，只需要知道 CPU 这个组件的地址即可。

微服务的特征
1. 通过服务实现组件化

2. 按业务能力来划分服务和开发团队

3. 去中心化

4. 基础设施自动化（devops、自动化部署）

- SOA 和微服务架构的差别

1. 微服务不再强调传统 SOA 架构里面比较重的 ESB 企业服务总线，同时 SOA 的思想进入到单个业务系统内部实现真正的组件化

2. Docker 容器技术的出现，为微服务提供了更便利的条件，比如更小的部署单元，每个服务可以通过类似 Node或者 Spring Boot 等技术跑在自己的进程中。

3. 还有一个点大家应该可以分析出来，SOA 注重的是系统集成方面，而微服务关注的是完全分离。

## 领域驱动设计及业务驱动划分

领域驱动设计（DDD,Domain-Driven Design），软件开发不是一蹴而就的事情，我们不可能在不了解产品（或行业领域）的前提下进行软件开发，在开发前，通常需要进行大量的业务知识梳理，然后才到软件设计的层面，最后才是开发。而在业务知识梳理的过程中，我们必然会形成某个领域知识，根据领域知识来一步步驱动软件设计。

>绝大部分公司都是这样一个状态，然后一般的解决方案是不断的重构系统，让系统的设计随着业务成长也进行不断的演进。通过重构出一些独立的类来存放某些通用的逻辑解决混乱问题，但是我们很难给它一个业务上的含义，只能以技术纬度进行描述，这个带来的问题就是其他人接手这块代码的时候不知道这个的含义或者可以通过修改这块通用逻辑来达到某些需求。

>实领域模型本身就不是一个陌生的单词，说直白点，在早期领域模型就是数据库设计. 我们做传统项目的流程或者说包括现在我们做项目的流程，都是首先讨论需求，然后是数据库建模， 在需求逐步确定的过程不断的去更新数据库的设计。接着我们在项目开发阶段，发现有些关系没有建、有些字段少了、有些表结构设计不合理，又在不断的去调整设计。最后上线。在传统项目中，数据库是整个项目的根本，数据模型出来以后后续的开发都是围绕着数
据展开；然后形成如下的一个架构。

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/springmvc.png)

- 传统模型的问题：

1. service 很重，所有逻辑处理基本都放在 service 层。

2. POJO（）作为 service 层的非常重要的一个实体，会因为不同场景的需求做不同的变化和组合，就会早成POJO 的几种不同模型（失血、贫血、充血），用来形容领域模型太胖或者太瘦随着业务变得复杂以后，包括数据结构的变化，那么各个模块就需要进行修改，原本清晰的系统经过不断的演化变得复杂、冗余、耦合度高。后果就非常严重我们试想一下如果一个软件产品不依赖数据库存储设备，那我们怎么去设计这个软件呢？如果没有了数据存储，那么我们的领域模型就得基于程序本身来设计。那这个就是 DDD 需要去考虑的问题。

使用领域模型后，

总结：

领域驱动设计其实我们可以简单认为是一种指导思想，是一种软件开发方法，通过 DDD 可以将系统解构更加合理，最终满足高内聚低耦合的本质。在我的观点来看，有点类似数据库的三范式，我们开始在学的时候并不太理解，当有足够的设计经验以后慢慢发现三范式带来的好处。同时我们也并不一定需要严格按照这三范式去进行实践，有些情况下是可以灵活调整。
