---
layout: post
title:  Tomcat的优化
date:   2019-05-31 21:52:12 +08:00
category: 算法
tags: 集合类
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


### 验证



如何入手，既然servlet规范中要讲servlet配置到web.xml中，则启动tomcat肯定会初始化，肯定会扫描该文件目录。


- 先看看web.xml的调用路径

1 在源码中检索 "/WEB-INF/web.xml"

```java

public class Constants {
    public static final String WEB_XML_LOCATION = "/WEB-INF/web.xml";
}

```

2 那么久查找WEB_XML_LOCATION的使用位置 JspCServletContext中使用

```java


private WebXml buildMergedWebXml(boolean validate, boolean blockExternal)
           throws JasperException {
               //...
             URL url = getResource(
                   org.apache.tomcat.util.descriptor.web.Constants.WEB_XML_LOCATION);

            //    ...   
           }

//见名知意，向上找
public JspCServletContext(PrintWriter aLogWriter, URL aResourceBaseURL,
           ClassLoader classLoader, boolean validate, boolean blockExternal)
    throws JasperException {

       myAttributes = new HashMap<>();
       myParameters = new ConcurrentHashMap<>();
       myParameters.put(Constants.XML_BLOCK_EXTERNAL_INIT_PARAM,
               String.valueOf(blockExternal));
       myLogWriter = aLogWriter;
       myResourceBaseURL = aResourceBaseURL;
       this.loader = classLoader;
       this.webXml = buildMergedWebXml(validate, blockExternal);
       jspConfigDescriptor = webXml.getJspConfigDescriptor();
   }

```

3 JspCServletContext有个实现位置Jspc.java的

```java

protected void initServletContext(ClassLoader classLoader)
            throws IOException, JasperException {
              //...
              context = new JspCServletContext(log, resourceBase, classLoader,
               isValidateXml(), isBlockExternal());
                 //...
            }

```

4 initServletContext方法的调用位置JspC.java的

```java

 public void execute() {
 //...
    initServletContext(loader);
     //...
 }

```

而public class JspC extends Task implements Options。可以见名知意，Task是根据某个选项的任务。

```java

//查看是个模板方法模式开发的类
public abstract class Task extends ProjectComponent {

}

```

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/tomcat009.png)

这个在ant包下，又有copyfile/mkdir,则用于ant构建时使用。

- 再看看web.xml的解析

再JspCServletContext中根据web.xml构建了

```java

 this.webXml = buildMergedWebXml(validate, blockExternal);

```

使用位置也只用

```java

jspConfigDescriptor = webXml.getJspConfigDescriptor();

```

找到如下：

```java

public class JspConfig {


    private Vector<JspPropertyGroup> jspProperties = null;
    private final ServletContext ctxt;

   private void processWebDotXml() {
  JspConfigDescriptor jspConfig = ctxt.getJspConfigDescriptor();

        if (jspConfig == null) {
            return;
        }

        jspProperties = new Vector<>();
        Collection<JspPropertyGroupDescriptor> jspPropertyGroups =
                jspConfig.getJspPropertyGroups();

        for (JspPropertyGroupDescriptor jspPropertyGroup : jspPropertyGroups) {

            Collection<String> urlPatterns = jspPropertyGroup.getUrlPatterns();

            if (urlPatterns.size() == 0) {
                continue;
            }

            JspProperty property = new JspProperty(jspPropertyGroup.getIsXml(),
                    jspPropertyGroup.getElIgnored(),
                    jspPropertyGroup.getScriptingInvalid(),
                    jspPropertyGroup.getPageEncoding(),
                    jspPropertyGroup.getIncludePreludes(),
                    jspPropertyGroup.getIncludeCodas(),
                    jspPropertyGroup.getDeferredSyntaxAllowedAsLiteral(),
                    jspPropertyGroup.getTrimDirectiveWhitespaces(),
                    jspPropertyGroup.getDefaultContentType(),
                    jspPropertyGroup.getBuffer(),
                    jspPropertyGroup.getErrorOnUndeclaredNamespace());
        }


  }
}

```

查找到jspconfig的

```java

private void init() {

       if (!initialized) {
           synchronized (this) {
               if (!initialized) {
                   processWebDotXml();
                   defaultJspProperty = new JspProperty(defaultIsXml,
                           defaultIsELIgnored,
                           defaultIsScriptingInvalid,
                           null, null, null,
                           defaultDeferedSyntaxAllowedAsLiteral,
                           defaultTrimDirectiveWhitespaces,
                           defaultDefaultContentType,
                           defaultBuffer,
                           defaultErrorOnUndeclaredNamespace);
                   initialized = true;
               }
           }
       }
   }

```

既然是容器，那么肯定有context（规范），确实有：

```java

public interface Context extends Container {}

```

再找一个实现：

```java

//不看set get方法
//肯定有load方法
public class StandardContext extends ContainerBase
        implements Context{

public boolean loadOnStartup(Container children[]) {


}

        }

```
