---
layout: post
title:  分析Tomcat源码
date:   2019-05-31 21:52:12 +08:00
category: 性能优化
tags: Tomcat
comments: true
---

* content
{:toc}























## 查看Tomcat

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
