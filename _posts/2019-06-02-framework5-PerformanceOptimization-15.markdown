---
layout: post
title:  关于Tomcat
date:   2019-06-02 21:52:12 +08:00
category: 性能优化
tags: Tomcat
comments: true
---

* content
{:toc}



Tomcat是Java web最常用的web服务器,有必要对其进行深入了解。



















## Tomcat是什么

>>是免费开源的应用（java）服务器，是java语言实现的sevlet容器，并常被使用在中小型并发量不是很大的情况下.

### 版本差异

当前版本9.0.21，当前最常用版本8.0；

7.x不支持nio2,8.5及以上版本不支持bio；

### 架构

以下是比较形象的描述tomcat架构：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/tomcat013.png)

通过源码阅读、结合架构图以及官网给的[流程图](http://tomcat.apache.org/tomcat-8.0-doc/architecture/startup/serverStartup.pdf)分析得到：

- tomcat启动会加载解析及运行Sever.xml
  - 解析Server节点
    In the Tomcat world, a Server represents the whole container. `rarely` customized by users.

  - 解析Service节点
    ties one or more Connectors to exactly one Engine.`rarely` customized by users.

  - 解析Connector节点
    A Connector handles communications with the client.它会绑定接口及相关的处理协议。

  - 解析Engine节点
    An Engine represents request processing pipeline for a specific Service. As a Service may have multiple Connectors, the Engine receives and processes all requests from these connectors, handing the response back to the appropriate connector for transmission to the client. The Engine interface may be implemented to supply custom Engines, though this is `uncommon`.

  - 解析Host节点
    A Host is an association of a network name.

  - 解析Context节点
    A Context represents a web application.

加载server.xml时，tomcat会一层一层load并且start每一层的节点，Host层会fireLifecycleEvent并且调用HostConfig的lifecycleEvent并deployApps()，Context层会fireLifecycleEvent并且调用ConTextConfig的lifecycleEvent加载configureStart()，加载每个项目的web.xml并且添加到tomcat的servletList中。

当请求来的时候会被socket接收到并处理后返回结果，其请求处理[流程图](http://tomcat.apache.org/tomcat-8.0-doc/architecture/requestProcess/request-process.png);

## Tomcat优化

何为优化，就是当前的系统、应用配置满足不了实际需求时需要的改进，比如需要服务降级、在系统支持的情况下提高应用服务器的配置等。

具体操作包含两项：删除和修改。

### 删除

- server.xml删除

没有使用的监听器如apr支持的

```xml

<Listener className="org.apache.catalina.core.AprLifecycleListener" SSLEngine="on" />
```

如 ajp

```xml

 <Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />

```

如日志

```xml

<Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
               prefix="localhost_access_log" suffix=".txt"
               pattern="%h %l %u %t &quot;%r&quot; %s %b" />

```

- web.xml

  - 不适用的servlet

  如果外部使用nignx处理静态资源，那tomcat没必要处理静态资源，

  ```xml

  <servlet>
       <servlet-name>default</servlet-name>
       <servlet-class>org.apache.catalina.servlets.DefaultServlet</servlet-class>
       <init-param>
           <param-name>debug</param-name>
           <param-value>0</param-value>
       </init-param>
       <init-param>
           <param-name>listings</param-name>
           <param-value>false</param-value>
       </init-param>
       <load-on-startup>1</load-on-startup>
   </servlet>

   ```

   - 没有媒体支持的类型

   ```xml

  <mime-mapping>
       <extension>3ds</extension>
       <mime-type>image/x-3ds</mime-type>
   </mime-mapping>
   <mime-mapping>
       <extension>3g2</extension>
       <mime-type>video/3gpp2</mime-type>
   </mime-mapping>
   <mime-mapping>
       <extension>3gp</extension>
       <mime-type>video/3gpp</mime-type>
   </mime-mapping>

   //...

   ```

   - 不想使用的欢迎页面

   ```xml

    <welcome-file-list>
       <welcome-file>index.html</welcome-file>
       <welcome-file>index.htm</welcome-file>
       <welcome-file>index.jsp</welcome-file>
  </welcome-file-list>

  ```

  - session

```xml

<session-config>
<session-timeout>30</session-timeout>
</session-config>

```

### 修改

再看一下server.xml中的一些节点：

- Host

  autoDeploy在生产环境下一定改为false;

- Context

  reloadable:false等。

Listener：监听特定事件发生时触发的操作；

Global Resources:定义全局资源，如tomcat-users.xml;

Valve: 一种过滤器，如可以存访问日志；

Realm: 一种用户密码与应用的映射关系，达到角色安全管理作用。

Connector：由于与客户端连接请求打交道，可以配置修改：

```xml

<Executor name="tomcatThreadPool" namePrefix="catalina-exec-"
       maxThreads="150" minSpareThreads="4"/>

<Connector executor="tomcatThreadPool"
               port="8080" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8443" />

```

协议支持

```

org.apache.coyote.http11.Http11Protocol - blocking Java connector
org.apache.coyote.http11.Http11NioProtocol - non blocking Java NIO connector
org.apache.coyote.http11.Http11Nio2Protocol - non blocking Java NIO2 connector
org.apache.coyote.http11.Http11AprProtocol - the APR/native connector.

```

Executor可配置：acceptCount、maxConnections、maxThreads、minSpareThreads

在这里配置了executor，可以配合Connector的协议配置，达到对bio/nio/nio2/apr调用支持及线程配置，具体使用配合jmeter进行压测；

### 优化测试

- jvisualvm

命令行输入jvisualvm

选择本地java进程

监控远程tomcat

在catalina.sh中添加

```

JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote
-Djava.rmi.server.hostname=47.95.209.106 -Dcom.sun.management.jmxremote.port=8998
-Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=true
-Dcom.sun.management.jmxremote.password.file=../conf/jmxremote.password
-Dcom.sun.management.jmxremote.access.file=../conf/jmxremote.access"

```

然后在jvisualvm中添加远程连接，JMX类型

- tomcat-manger/probe

tomcat-manger或者probe来监控

ps -ef | grep tomcat

kill pid

测试结果：

连接数小的时候：

bio性能高于nio

连接数大了之后，nio的性能也不高，只是支持的连接数大了。而bio会出现拒绝的行为返回给用户。

Apr是一种直接调用操作系统socket的行为，需要本地安装队apr库的支持。支持连接数会更大。

当然一切优化在业务优化上面前不足一提。

## Jvm的优化

由于Tomcat运行在jvm上，所以也需要优化jvm。

## JVM的结构模型

- 线程私有环境

  - 程序计数器：存放的就是当前正在执行的指令的地址

  - Java虚拟机栈：

  是Java方法执行的内存模型:每个方法在执行的同时都会创建一个栈帧，用于存储局部变量表、操作数栈、动态链接、方法出口等信息。每一个方法从调用直到执行完成的过程，就对应着一个栈帧在虚拟机栈中入栈到出栈的过程

  - 本地方法栈：

  本地方法栈和虚拟机栈锁发挥的作用是非常相似的，它们之间的区别不过是虚拟机栈执行Java方法服务，而本地方法栈则为虚拟机使用到的native方法服务。

- 线程共有的

  - 堆Heap

  是被所有线程共享的一块内存区域，在虚拟机启动时创建。次内存区域的唯一目的就是存放对象实例，几乎所有的对象实例都在这里分配内存。

  - 方法区

  方法区和Java堆一样，是各个线程共享的内存区域，也是在虚拟机启动时创建。它用于存储已被虚拟机加载的类信息、常量、静态变量、即时编译器编译后的代码等数据。

### 优化

优化主要针对线程共有的结构中，那么重点是存储数据的是堆和方法区(非堆)两部分。

那么内存大小与垃圾回收器选择就是选择的方向：

设置最大最小内存：

```

-Xmx -Xms 设置最大最小内存的

-Xms等价于-XX:InitialHeapSize
-Xmx等价于-XX:MaxHeapSize
-Xss等价于-XX:ThreadStackSize

```

算法选择：

```

格式：-XX:[+-]<name> 表示启用或者禁用name属性
比如：
-XX:+UseConcMarkSweepGC 表示启用CMS类型的垃圾回收器
-XX:+UseG1GC 表示启用CMS类型的垃圾回收器

```

一般出现问题，需要查看日志

参数设置自动

```

-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=./

```

jmap手动

```

查看当前进程id PID
jmap -dump:format=b,file=heap.hprof PID
jmap -heap PID 打印出堆内存相关的信息

```

查看目前JVM使用的垃圾回收器

```

jinfo -flag UseParallelGC PID

```

将垃圾回收器修改为G1

```

jinfo -flag UseG1GC PID

```

打印出日志详情信息和日志输出目录文件

```

PrintGCDetails:打印日志详情信息
PrintGCTimeStamps:输出GC的时间戳(以基准时间的形式)

-XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -
Xloggc:$CATALINA_HOME/logs/g1gc.log

```

总结：

内存大小设置——>dump出日志 使用MAT工具分析

垃圾收集器选择———>dump出GC日志 gceasy或者GCViewer

## 其他优化

- Connector
  配置压缩属性compression="500"，文件大于500bytes才会压缩

- 数据库优化
  减少对数据库访问等待的时间，可以从数据库的层面进行优化，或者加缓存等等各种方案。

- 开启浏览器缓存，nginx静态资源部署
