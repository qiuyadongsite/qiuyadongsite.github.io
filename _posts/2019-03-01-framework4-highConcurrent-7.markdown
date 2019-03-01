---
layout: post
title:  序列化技术
date:   2019-03-01 21:52:12 +08:00
category: 高并发分布式
tags: 序列化
comments: true
---

* content
{:toc}

Java 平台允许我们在内存中创建可复用的 Java 对象，但一般情况下，只有当 JVM 处于运行时，这些对象才可能存在，即，这些对象的生命周期不会比 JVM 的生命周期更长。但在现实应用中，就可能要求在 JVM停止运行之后能够保存(持久化)指定的对象，并在将来重新读取被保存的对象。Java 对象序列化就能够帮助我们实现该功能。












## 概念

  - 序列化

  是把对象的状态信息转化为可存储或传输的形式过程，也就是把对象转化为字节序列的过程称为对象的序列化

  - 反序列化

  是序列化的逆向过程，把字节数组反序列化为对象，把字节序列恢复为对象的过程成为对象的反序列化

  - 序列化面临的挑战

  评价一个序列化算法优劣的两个重要指标是：序列化以后的数据大小；序列化操作本身的速度及系统资源开销（CPU、内存）；

  Java 语言本身提供了对象序列化机制，也是 Java 语言本身最重要的底层机制之一,Java 本身提供的序列化机制存在两个问题:  1. 序列化的数据比较大，传输效率低;2. 其他语言无法识别和对接

  - 如何实现一个序列化操作

  在 Java 中，只要一个类实现了 java.io.Serializable 接口，那么它就可以被序列化;

  定义一个接口：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/serilalizer001.png)

基于 JDK 序列化方式实现

JDK 提供了Java对象的序列化方式，主要通过输出流java.io.ObjectOutputStream 和对象输入流 java.io.ObjectInputStream来实现。其中，被序列化的对象需要实现 java.io.Serializable 接口

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/serializer002.png)


具体实现通过对一个 user 对象进行序列化操作

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/serializerdemo001.png)


## 序列化的高阶认识

  - serialVersionUID 的作用

  java 的序列化机制是通过判断类的 serialVersionUID 来验证版本一致性的。在进行反序列化时，JVM 会把传来的字节流中的 serialVersionUID与本地相应实体类的 serialVersionUID 进行比较，如果相同就认为是一致的，可以进行反序列化，否则就会出现序列化版本不一致的异常，即是 InvalidCastException

  如果没有为指定的 class 配置 serialVersionUID，那么 java 编译器会自动给这个 class 进行一个摘要算法，类似于指纹算法，只要这个文件有任何改动，得到的 UID 就会截然不同的，可以保证在这么多类中，这个编号是唯一的

  serialVersionUID 有两种显示的生成方式：

  一是默认的 1L，比如：private static final long serialVersionUID = 1L;

  二是根据类名、接口名、成员方法及属性等来生成一个 64 位的哈希字段

  当实现 java.io.Serializable 接口的类没有显式地定义一个serialVersionUID 变量时候，Java 序列化机制会根据编译的 Class 自动生成一个 serialVersionUID 作序列化版本比较用，这种情况下，如果Class 文件(类名，方法明等)没有发生变化(增加空格，换行，增加注释等等)，就算再编译多次，serialVersionUID 也不会变化的。

  - 静态变量序列化

  在 User 中添加一个全局的静态变量 num ， 在执行序列化以后修改num 的值为 10， 然后通过反序列化以后得到的对象去输出 num

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/staticSerialize1.png)

最后的输出是 10，理论上打印的 num 是从读取的对象里获得的，应该是保存时的状态才对。之所以打印 10 的原因在于序列化时，并不保存静态变量，这其实比较容易理解，序列化保存的是对象的状态，静态变量属于类的状态，因此 序列化并不保存静态变量。

  - 父类的序列化

  一个子类实现了 Serializable 接口，它的父类都没有实现 Serializable 接口，在子类中设置父类的成员变量的值，接着序列化该子类对象。再反序列化出来以后输出父类属性的值。结果应该是什么

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/superclasserializer001.png)

发现父类的 sex 字段的值为 null。也就是父类没有实现序列化。

结论：

1. 当一个父类没有实现序列化时，子类继承该父类并且实现了序列化。在反序列化该子类后，是没办法获取到父类的属性值的

2. 当一个父类实现序列化，子类自动实现序列化，不需要再显示实现Serializable 接口

3. 当一个对象的实例变量引用了其他对象，序列化该对象时也会把引用对象进行序列化，但是前提是该引用对象必须实现序列化接口

  - Transient 关键字

Transient 关键字的作用是控制变量的序列化，在变量声明前加上该关键字，可以阻止该变量被序列化到文件中，在被反序列化后，transient 变量的值被设为初始值，如 int 型的是 0，对象型的是 null

绕开 transient 机制的办法

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/readwriteObj001.png)

注意：writeObject和 readObject 这两个私有的方法，既不属于 Object、也不是 Serializable，为什么能够在序列化的时候被调用呢？ 原因是，ObjectOutputStream使用了反射来寻找是否声明了这两个方法。因为 ObjectOutputStream使用 getPrivateMethod，所以这些方法必须声明为 priate 以至于供
ObjectOutputStream 来使用。

  - 序列化的存储规则

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/saveSerializer.png)

同一对象两次（开始写入文件到最终关闭流这个过程算一次，上面的演示效果是不关闭流的情况才能演示出效果）写入文件，打印出写入一次对象后的存储大小和写入两次后的存储大小，第二次写入对象时文件只增加了 5 字节Java 序列化机制为了节省磁盘空间，具有特定的存储规则，当写入文件的为同一对象时，并不会再将对象的内容进行存储，而只是再次存储一份引用，上面增加的 5 字节的存储空间就是新增引用和一些控制信息的空间。反序列化时，恢复引用关系.该存储规则极大的节省了存储空间。

## 序列化实现深克隆

  在 Java 中存在一个 Cloneable 接口，通过实现这个接口的类都会具备clone 的能力，同时 clone 是在内存中进行，在性能方面会比我们直接通过 new 生成对象要高一些，特别是一些大的对象的生成，性能提升相对比较明显。那么在 Java 领域中，克隆分为深度克隆和浅克隆；

  - 浅克隆

  被复制对象的所有变量都含有与原来的对象相同的值，而所有的对其他对象的引用仍然指向原来的对象。实现一个邮件通知功能，告诉每个人今天晚上的上课时间，通过浅克隆；

实现如下：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/clonedemo001.png)

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/clonedemo002.png)


但是，当我们只希望，修改“黑白”的上课时间，调整为 20：30 分。通过结果发现，所有人的通知消息都发生了改变。这是因为 p2 克隆的这个
对象的 Email 引用地址指向的是同一个。这就是浅克隆。

  - 深克隆

  被复制对象的所有变量都含有与原来的对象相同的值，除去那些引用其他对象的变量。那些引用其他对象的变量将指向被复制过的新对象，而不再是原有的那些被引用的对象。换言之，深拷贝把要复制的对象所引用的对象都复制了一遍

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/deepclone001.png)

  这样就能实现深克隆效果，原理是把对象序列化输出到一个流中，然后在把对象从序列化流中读取出来，这个对象就不是原来的对象了。

## 常见的序列化技术

使用 JAVA 进行序列化有他的优点，也有他的缺点

优点：JAVA 语言本身提供，使用比较方便和简单

缺点：不支持跨语言处理、 性能相对不是很好，序列化以后产生的数据相对较大

  - XML 序列化框架

  XML 序列化的好处在于可读性好，方便阅读和调试。但是序列化以后的字节码文件比较大，而且效率不高，适用于对性能不高，而且 QPS 较低的企业级内部系统之间的数据交换的场景，同时 XML 又具有语言无关性，所以还可以用于异构系统之间的数据交换和协议。比如我们熟知的 `Webservice`，就是采用 XML 格式对数据进行序列化的

  - JSON 序列化框架

  JSON（JavaScript Object Notation）是一种轻量级的数据交换格式，相对于 XML 来说，JSON 的字节流更小，而且可读性也非常好。现在 JSON数据格式在企业运用是最普遍的JSON 序列化常用的开源工具有很多

1. Jackson （https://github.com/FasterXML/jackson）

2. 阿里开源的 FastJson （https://github.com/alibaba/fastjon）

3. Google 的 GSON (https://github.com/google/gson)

这几种 json 序列化工具中，Jackson 与 fastjson 要比 GSON 的性能要好，但是 Jackson、GSON 的稳定性要比 Fastjson 好。而 fastjson 的优
势在于提供的 api 非常容易使用

  - Hessian 序列化框架

  Hessian 是一个支持跨语言传输的二进制序列化协议，相对于 Java 默认的序列化机制来说，Hessian 具有更好的性能和易用性，而且支持多种不同的语言实际上 Dubbo 采用的就是 Hessian 序列化来实现，只不过 Dubbo 对Hessian 进行了重构，性能更高

  - Protobuf 序列化框架

  Protobuf 是 Google 的一种数据交换格式，它独立于语言、独立于平台

  Google 提供了多种语言来实现，比如 Java、C、Go、Python，每一种实现都包含了相应语言的编译器和库文件Protobuf 使用比较广泛，主要是空间开销小和性能比较好，非常适合用于公司内部对性能要求高的 RPC 调用。 另外由于解析性能比较高，序列化以后数据量相对较少，所以也可以应用在对象的持久化场景中

  但是但是要使用 Protobuf 会相对来说麻烦些，因为他有自己的语法，有自己的编译器:

  下载 protobuf 工具 https://github.com/google/protobuf/releases 找到 protoc-3.5.win32.zip编写 proto 文件

  ```
  syntax="proto2";
package com.gupaoedu.serial;
option java_package = "com.gupaoedu.serial";
option java_outer_classname="UserProtos";
message User {
required string name=1;
required int32 age=2;
}


  ```
proto 的语法

1. 包名

2. option 选项

3. 消息模型(消息对象、字段（字段修饰符-required/optional/repeate字段类型（基本数据类型、枚举、消息对象）、字段名、标识号）生成实体类

在 protoc.exe 安装目录下执行如下命令\protoc.exe --java_out=./ ./user.proto运行查看结果

将生成以后的 UserProto.java 拷贝到项目中

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/protobuf001.png)

- Protobuf 原理分析

核心原理： protobuf 使用 varint（zigzag）作为编码方式， 使用 T-L-V 作为存储方式


## 序列化技术的选型

  - 技术层面

1. 序列化空间开销，也就是序列化产生的结果大小，这个影响到传输的性能

2. 序列化过程中消耗的时长，序列化消耗时间过长影响到业务的响应时间

3. 序列化协议是否支持跨平台，跨语言。因为现在的架构更加灵活，如果存在异构系统通信需求，那么这个是必须要考虑的

4. 可扩展性/兼容性，在实际业务开发中，系统往往需要随着需求的快速迭代来实现快速更新，这就要求我们采用的序列化协议基于良好的可扩展性/兼容性，比如在现有的序列化数据结构中新增一个业务字段，不会影响到现有的服务

5. 技术的流行程度，越流行的技术意味着使用的公司多，那么很多坑都已经淌过并且得到了解决，技术解决方案也相对成熟

6. 学习难度和易用性

  - 选型建议

1. 对性能要求不高的场景，可以采用基于 XML 的 SOAP 协议

2. 对性能和间接性有比较高要求的场景，那么 Hessian、Protobuf、Thrift、Avro 都可以。

3. 基于前后端分离，或者独立的对外的 api 服务，选用 JSON 是比较好的，对于调试、可读性都很不错

4. Avro 设计理念偏于动态类型语言，那么这类的场景使用 Avro 是可以的
