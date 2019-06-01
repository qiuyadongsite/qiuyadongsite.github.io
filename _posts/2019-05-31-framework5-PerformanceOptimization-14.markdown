---
layout: post
title:  Tomcat的优化
date:   2019-05-31 21:52:12 +08:00
category: 性能优化
tags: Tomcat
comments: true
---

* content
{:toc}























## Tomcat是什么

>>The Apache Tomcat® software is an open source implementation of the Java Servlet, JavaServer Pages, Java Expression Language and Java WebSocket technologies.

翻译：Apache Tomcat 是Java Servlet、JavaServer页面、Java表达式语言和Java WebSoSk技术的开源实现。

[各个版本Tomcat源码下载地址](https://archive.apache.org/dist/tomcat/)或者官网的[Tomcat8.0.15](https://archive.apache.org/dist/tomcat/tomcat-8/v8.0.15/src/)(本节研究,工作常用)

- 源码的编译到调试运行
  - 安装ant
  - 新建pom.xml
    在源目录下新建pom.xml,本例的[pom地址](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/files/tomcat-8.0.15-pom.xml)
  - 在目录下ant
  - 导入ideal目录，引入pom文件
  - 新建Aplication
      在在Man class:中填入，org.apache.catalina.startup.Bootstrap
      在VM options:中填入:

      ```

      -Dcatalina.home="E:\workspace\apache-tomcat-8.0.15-src\output\build"
      -server
      -Xms512m
      -Xmx512m

      ```

      - 运行 localhost:8080,就可以看到页面了

- 研究8.0.15的原因

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/tomcat001.png)

  - 协议支持（如上图8.0支持）

     8.0支持 apr/jio/nio2/nio 四种方式接入

     7.0的源码只支持 apr/jio/nio  

     8.5的源码支持 apr/nio2/nio

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/tomcat002.png)

  - 各个版本的支持

    如上图，8.0与8.5有明显的对照差异

## Tomcat源码分析

tomcat是java语言开发的servlet技术的实现，既然它是一个web容器，

### 猜想

1 客户端过来的连接，tomcat用SeverSocket sever=new ServerSocket(8080);来接受请求

2 除了端口外，socket的inputstream获取请求，回传过来的url,tomcat保存map<url,method>来存放地址方法对应关系

3 method方法执行后，返回给前端页面  outputstream.write返回数据

4 平时用的Java EE规范定义的servlet，就是让自定义的xxServlet extends HttpServlet{}，而这个servlet需要配置到web.xml中，那么tomcat是不是读取了这个servlet

  ```xml

  <servlet>
       <!-- 声明Servlet对象 -->
       <servlet-name>xxServlet</servlet-name>
       <!-- 上面一句指定Servlet对象的名称 -->
       <servlet-class>com.xx.xxServlet</servlet-class>
       <!-- 上面一句指定Servlet对象的完整位置，包含包名和类名 -->
   </servlet>

  <servlet-mapping>
       <!-- 映射Servlet -->
       <servlet-name>xxServlet</servlet-name>
       <!--<servlet-name>与上面<Servlet>标签的<servlet-name>元素相对应，不可以随便起名  -->
       <url-pattern>/xxServlet</url-pattern>
       <!-- 上面一句话用于映射访问URL -->
   </servlet-mapping>

  ```

5 在这个servlet中又可以使用request和response,那么应该是分别封装了socket的输入输出流，url对应servlet的url-pattern,方法则对应的是servlet-class

6 Tomcat发布项目需要默认将项目拷贝到webapps，或者在server.xml配置context,从而映射到指定发布的目录，Context在xml中配置，应该会被解析成对象

```xml

</Server>
 </Service>
     <Engine>
           <Host>
               <Context path="/" docBase="/root/java/xx"  debug="0" reloadable="true"></Context>
           </Host>
    </Engine>
  </Service>
</Server>

```




### 验证

- 验证servlet

首先看[官方文档](http://tomcat.apache.org/tomcat-8.0-doc/architecture/overview.html)中，有context的介绍：

>A Context represents a web application. A Host may contain multiple contexts, each with a unique path. The Context interface may be implemented to create custom Contexts, but this is rarely the case because the StandardContext provides significant additional functionality.

源码中找到Context，发现是个接口，那么Host、Engine、Service、Server都应该是接口，并得以验证;

StandardContext中查找方法，排除掉get,set方法，找load*或者init*方法，找到,

```java

public boolean loadOnStartup(Container children[]) {

       // Collect "load on startup" servlets that need to be initialized
       TreeMap<Integer, ArrayList<Wrapper>> map = new TreeMap<>();
       for (int i = 0; i < children.length; i++) {
           Wrapper wrapper = (Wrapper) children[i];
           int loadOnStartup = wrapper.getLoadOnStartup();
           if (loadOnStartup < 0)
               continue;
           Integer key = Integer.valueOf(loadOnStartup);
           ArrayList<Wrapper> list = map.get(key);
           if (list == null) {
               list = new ArrayList<>();
               map.put(key, list);
           }
           list.add(wrapper);
       }
       for (ArrayList<Wrapper> list : map.values()) {
           for (Wrapper wrapper : list) {
              wrapper.load();}
            }

}

```

根据注释得到验证。

对于这种只需要了解流程的项目没必要看懂每一行代码，而且tomcat的代码没有代表性，接着看文档看看启动流程：

官网给的[流程](http://tomcat.apache.org/tomcat-8.0-doc/architecture/startup/serverStartup.pdf)

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/tomcat011.png)

[上图的解释](http://tomcat.apache.org/tomcat-8.0-doc/architecture/startup/serverStartup.txt)

可以发现Catalina.start()的第三步 ContextConfig.start() 来解析web.xml,详细方法在webConfig();-> configureContext(webXml);

[查看该代码](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/files/configureContext.java)：作用就是解析web.xm中的各个配置errorPage、filter、listener等。

- 验证socket

   根据查看tomcat源码的经验，可以发现Connector配置了端口，

```java

   public class Connector extends LifecycleMBeanBase  {
   }
```

根据Tomcat中生命周期的定义Lifecycle，会实现initInternal()->AbstractProtocol的init()->AbstractEndpoint的bind():

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/tomcat012.png)

根据配置不同的协议绑定端口，apr的[bind方法](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/files/apr.java)

看到int ret = Socket.bind(serverSock, inetAddress)得到验证。并且其中使用了线程池来解决bio的同步阻塞问题。

验证完毕！

## 架构

猜想验证简单的可以达到手写tomcat服务器最基础的功能，那么看看Tomcat还做了什么？

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/tomcat013.png)
