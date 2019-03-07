---
layout: post
title:  分布式服务治理之dubbo源码分析
date:   2019-03-06 22:52:12 +08:00
category: 高并发分布式
tags: dubbo
comments: true
---

* content
{:toc}

  Dubbo的使用很简单，如果要掌握，需要详细分析源码。












## SPI机制

  SPI全称（service provider interface），是JDK内置的一种服务提供发现机制，目前市面上有很多框架都是用它来做服务的扩展发现，大家耳熟能详的如JDBC、日志框架都有用到；

  简单来说，它是一种动态替换发现的机制。举个简单的例子，如果我们定义了一个规范，需要第三方厂商去实现，那么对于我们应用方来说，只需要集成对应厂商的插件，既可以完成对应规范的实现机制。 形成一种插拔式的扩展手段。

  代码实例：

定义规范：

  ```java
  public interface DataBaseDriver {

    String connect(String hospt);
}

  ```

MysqlDriver去实现：

```java

public class MysqlDriver implements DataBaseDriver{

    @Override
    public String connect(String s) {
        return "begin build Mysql connection";
    }
}
//并且在resources/services/com.XX.spi.DataBaseDriver文件中添加

//com.gupaoedu.spi.MysqlDriver

```

测试demo:

```java
public class DataBaseConnection {

    public static void main(String[] args) {
        ServiceLoader<DataBaseDriver> serviceLoader=
                ServiceLoader.load(DataBaseDriver.class);

        for(DataBaseDriver driver:serviceLoader){
            System.out.println(driver.connect("localhost"));
        }
    }
}
//将实现者的项目添加到测试pom中
```

SPI的缺点

1	JDK标准的SPI会一次性加载实例化扩展点的所有实现，什么意思呢？就是如果你在META-INF/service下的文件里面加了N个实现类，那么JDK启动的时候都会一次性全部加载。那么如果有的扩展点实现初始化很耗时或者如果有些实现类并没有用到，那么会很浪费资源

2	如果扩展点加载失败，会导致调用方报错，而且这个错误很难定位到是这个原因

## Dubbo优化后的SPI实现

Dubbo的SPI机制规范

  大部分的思想都是和SPI是一样，只是下面两个地方有差异。

1	需要在resource目录下配置META-INF/dubbo或者META-INF/dubbo/internal或者META-INF/services，并基于SPI接口去创建一个文件

2	文件名称和接口名称保持一致，文件内容和SPI有差异，内容是KEY对应Value

跟源码查看详细的过程

SPI接口定义

定义了@SPI注解

```java
public @interface SPI {

       Stringvalue() default ""; //指定默认的扩展点

}

```

只有在接口打了@SPI注解的接口类才会去查找扩展点实现

会依次从这几个文件中读取扩展点

META-INF/dubbo/internal/   //dubbo内部实现的各种扩展都放在了这个目录了

META-INF/dubbo/

META-INF/services/

我们以Protocol接口为例， 接口上打上SPI注解，默认扩展点名字为dubbo

```java
@SPI("dubbo")
public interface Protocol {
    int getDefaultPort();

    @Adaptive
    <T> Exporter<T> export(Invoker<T> var1) throws RpcException;

    @Adaptive
    <T> Invoker<T> refer(Class<T> var1, URL var2) throws RpcException;

    void destroy();
}

```

看看该接口的类图

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/protocol001.png)

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/dubboprotocol.png)

了解了基本设计之后开始研究使用

测试demo

```java
//研究一下下边代码的实现过程，输出dubbo
ExtensionLoader.getExtensionLoader(Protocol.class).getDefaultExtensionName();

```

跟源码：ExtensionLoader.getExtensionLoader(Protocol.class)

```java
//Type为Protocol
public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
       if (type == null)
           throw new IllegalArgumentException("Extension type == null");
       if(!type.isInterface()) {
           throw new IllegalArgumentException("Extension type(" + type + ") is not interface!");
       }
       if(!withExtensionAnnotation(type)) {
           throw new IllegalArgumentException("Extension type(" + type +
                   ") is not extension, because WITHOUT @" + SPI.class.getSimpleName() + " Annotation!");
       }

       ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
       if (loader == null) {
           EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type));
           loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
       }
       return loader;
   }

```

跟源码：new ExtensionLoader<T>(type)

```java

private ExtensionLoader(Class<?> type) {
        this.type = type;
        objectFactory = (type == ExtensionFactory.class ? null : ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension());
        //此处由于是protocol
        //则先ExtensionLoader.getExtensionLoader(ExtensionFactory.class),此举又会调用，再次调入该函数时
        //type=ExtensionFactory
        //objectFactory=null
        //返回的loader为ExtensionLoader<ExtensionFactory>

        //继续调用ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension()
    }

```

调用ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension()

```java

public T getAdaptiveExtension() {
       Object instance = cachedAdaptiveInstance.get();
       if (instance == null) {//多线程的安全机制
           if(createAdaptiveInstanceError == null) {
               synchronized (cachedAdaptiveInstance) {//多线程的安全机制
                   instance = cachedAdaptiveInstance.get();
                   if (instance == null) {
                       try {
                         //调用ExtensionFactory的createAdaptiveExtension
                           instance = createAdaptiveExtension();
                           cachedAdaptiveInstance.set(instance);
                       } catch (Throwable t) {
                           createAdaptiveInstanceError = t;
                           throw new IllegalStateException("fail to create adaptive instance: " + t.toString(), t);
                       }
                   }
               }
           }
           else {
               throw new IllegalStateException("fail to create adaptive instance: " + createAdaptiveInstanceError.toString(), createAdaptiveInstanceError);
           }
       }

       return (T) instance;
   }

//继续
   private T createAdaptiveExtension() {
        try {
            return injectExtension((T) getAdaptiveExtensionClass().newInstance());
        } catch (Exception e) {
            throw new IllegalStateException("Can not create adaptive extenstion " + type + ", cause: " + e.getMessage(), e);
        }
    }


//again  getAdaptiveExtensionClass

private Class<?> getAdaptiveExtensionClass() {
        getExtensionClasses();
        if (cachedAdaptiveClass != null) {
            return cachedAdaptiveClass;
        }
        return cachedAdaptiveClass = createAdaptiveExtensionClass();
    }

  //getExtensionClasses()

  private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.get();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = loadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
	}

  //     loadExtensionClasses()

  // 此方法已经getExtensionClasses方法同步过。ExtensionFactory

//  @SPI
//  public interface ExtensionFactory {


  //    <T> T getExtension(Class<T> type, String name);

  //}



    private Map<String, Class<?>> loadExtensionClasses() {
        final SPI defaultAnnotation = type.getAnnotation(SPI.class);
        if(defaultAnnotation != null) { //如上此处为空
            String value = defaultAnnotation.value();
            if(value != null && (value = value.trim()).length() > 0) {
                String[] names = NAME_SEPARATOR.split(value);
                if(names.length > 1) {
                    throw new IllegalStateException("more than 1 default extension name on extension " + type.getName()
                            + ": " + Arrays.toString(names));
                }
                if(names.length == 1) cachedDefaultName = names[0];
            }
        }

        Map<String, Class<?>> extensionClasses = new HashMap<String, Class<?>>();
        loadFile(extensionClasses, DUBBO_INTERNAL_DIRECTORY);
        loadFile(extensionClasses, DUBBO_DIRECTORY);
        loadFile(extensionClasses, SERVICES_DIRECTORY);
        return extensionClasses;//得到spiExtensionFactory springExtensionFactory
    }


    private void loadFile(Map<String, Class<?>> extensionClasses, String dir) {
           String fileName = dir + type.getName();
           try {
               Enumeration<java.net.URL> urls;
               ClassLoader classLoader = findClassLoader();
               if (classLoader != null) {
                   urls = classLoader.getResources(fileName);
               } else {
                   urls = ClassLoader.getSystemResources(fileName);
               }
               if (urls != null) {
                   while (urls.hasMoreElements()) {
                       java.net.URL url = urls.nextElement();
                       try {
                           BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
                           try {
                               String line = null;
                               while ((line = reader.readLine()) != null) {
                                   final int ci = line.indexOf('#');
                                   if (ci >= 0) line = line.substring(0, ci);
                                   line = line.trim();
                                   if (line.length() > 0) {
                                       try {
                                           String name = null;
                                           int i = line.indexOf('=');
                                           if (i > 0) {
                                               name = line.substring(0, i).trim();
                                               line = line.substring(i + 1).trim();
                                           }
                                           if (line.length() > 0) {
                                               Class<?> clazz = Class.forName(line, true, classLoader);
                                               if (! type.isAssignableFrom(clazz)) {
                                                   throw new IllegalStateException("Error when load extension class(interface: " +
                                                           type + ", class line: " + clazz.getName() + "), class "
                                                           + clazz.getName() + "is not subtype of interface.");
                                               }
                                               if (clazz.isAnnotationPresent(Adaptive.class)) {
                                                   if(cachedAdaptiveClass == null) {
                                                       cachedAdaptiveClass = clazz;//这里讲AdaptiveExtensionFactory存储到cachedAdaptiveClass
                                                   } else if (! cachedAdaptiveClass.equals(clazz)) {
                                                       throw new IllegalStateException("More than 1 adaptive class found: "
                                                               + cachedAdaptiveClass.getClass().getName()
                                                               + ", " + clazz.getClass().getName());
                                                   }
                                               } else {
                                                   try {
                                                       clazz.getConstructor(type);
                                                       Set<Class<?>> wrappers = cachedWrapperClasses;
                                                       if (wrappers == null) {
                                                           cachedWrapperClasses = new ConcurrentHashSet<Class<?>>();
                                                           wrappers = cachedWrapperClasses;
                                                       }
                                                       wrappers.add(clazz);
                                                   } catch (NoSuchMethodException e) {
                                                       clazz.getConstructor();
                                                       if (name == null || name.length() == 0) {
                                                           name = findAnnotationName(clazz);
                                                           if (name == null || name.length() == 0) {
                                                               if (clazz.getSimpleName().length() > type.getSimpleName().length()
                                                                       && clazz.getSimpleName().endsWith(type.getSimpleName())) {
                                                                   name = clazz.getSimpleName().substring(0, clazz.getSimpleName().length() - type.getSimpleName().length()).toLowerCase();
                                                               } else {
                                                                   throw new IllegalStateException("No such extension name for the class " + clazz.getName() + " in the config " + url);
                                                               }
                                                           }
                                                       }
                                                       String[] names = NAME_SEPARATOR.split(name);
                                                       if (names != null && names.length > 0) {
                                                           Activate activate = clazz.getAnnotation(Activate.class);
                                                           if (activate != null) {
                                                               cachedActivates.put(names[0], activate);
                                                           }
                                                           for (String n : names) {
                                                               if (! cachedNames.containsKey(clazz)) {
                                                                   cachedNames.put(clazz, n);
                                                               }
                                                               Class<?> c = extensionClasses.get(n);
                                                               if (c == null) {
                                                                   extensionClasses.put(n, clazz);
                                                               } else if (c != clazz) {
                                                                   throw new IllegalStateException("Duplicate extension " + type.getName() + " name " + n + " on " + c.getName() + " and " + clazz.getName());
                                                               }
                                                           }
                                                       }
                                                   }
                                               }
                                           }
                                       } catch (Throwable t) {
                                           IllegalStateException e = new IllegalStateException("Failed to load extension class(interface: " + type + ", class line: " + line + ") in " + url + ", cause: " + t.getMessage(), t);
                                           exceptions.put(line, e);
                                       }
                                   }
                               } // end of while read lines
                           } finally {
                               reader.close();
                           }
                       } catch (Throwable t) {
                           logger.error("Exception when load extension class(interface: " +
                                               type + ", class file: " + url + ") in " + url, t);
                       }
                   } // end of while urls
               }
           } catch (Throwable t) {
               logger.error("Exception when load extension class(interface: " +
                       type + ", description file: " + fileName + ").", t);
           }
       }    

```


```java


@Adaptive
public class AdaptiveExtensionFactory implements ExtensionFactory {

    private final List<ExtensionFactory> factories;

    public AdaptiveExtensionFactory() {
        ExtensionLoader<ExtensionFactory> loader = ExtensionLoader.getExtensionLoader(ExtensionFactory.class);
        List<ExtensionFactory> list = new ArrayList<ExtensionFactory>();
        for (String name : loader.getSupportedExtensions()) {
            list.add(loader.getExtension(name));
        }
        factories = Collections.unmodifiableList(list);//此处实例花了得到spiExtensionFactory springExtensionFactory
    }

    public <T> T getExtension(Class<T> type, String name) {
        for (ExtensionFactory factory : factories) {
            T extension = factory.getExtension(type, name);
            if (extension != null) {
                return extension;
            }
        }
        return null;
    }

}

//将AdaptiveExtensionFactory实例化后



private T injectExtension(T instance) {
        try {
            if (objectFactory != null) {//objectFactory为空
                for (Method method : instance.getClass().getMethods()) {
                    if (method.getName().startsWith("set")
                            && method.getParameterTypes().length == 1
                            && Modifier.isPublic(method.getModifiers())) {
                        Class<?> pt = method.getParameterTypes()[0];
                        try {
                            String property = method.getName().length() > 3 ? method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4) : "";
                            Object object = objectFactory.getExtension(pt, property);
                            if (object != null) {
                                method.invoke(instance, object);
                            }
                        } catch (Exception e) {
                            logger.error("fail to inject via method " + method.getName()
                                    + " of interface " + type.getName() + ": " + e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return instance;//实例化的AdaptiveExtensionFactory
    }




```

接着返回到objectFactory

```java

private ExtensionLoader(Class<?> type) {
        this.type = type;
        objectFactory = (type == ExtensionFactory.class ? null : ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension());//实例化的AdaptiveExtensionFactory
    }

```

获取默认名字

```java

public String getDefaultExtensionName() {
	    getExtensionClasses();
	    return cachedDefaultName;
	}


  private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.get();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = loadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
	}

  //又继续操作// 此方法已经getExtensionClasses方法同步过。
    private Map<String, Class<?>> loadExtensionClasses() {
        final SPI defaultAnnotation = type.getAnnotation(SPI.class);
        if(defaultAnnotation != null) {
            String value = defaultAnnotation.value();//value="dubbo"
            if(value != null && (value = value.trim()).length() > 0) {
                String[] names = NAME_SEPARATOR.split(value);
                if(names.length > 1) {
                    throw new IllegalStateException("more than 1 default extension name on extension " + type.getName()
                            + ": " + Arrays.toString(names));
                }
                if(names.length == 1) cachedDefaultName = names[0];//cachedDefaultName为dubbo
            }
        }

        Map<String, Class<?>> extensionClasses = new HashMap<String, Class<?>>();
        loadFile(extensionClasses, DUBBO_INTERNAL_DIRECTORY);
        loadFile(extensionClasses, DUBBO_DIRECTORY);
        loadFile(extensionClasses, SERVICES_DIRECTORY);
        return extensionClasses;
    }

```
