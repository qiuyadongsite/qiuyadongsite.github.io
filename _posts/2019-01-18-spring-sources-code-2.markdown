---
layout: post
title:  spring源码解析二
date:   2019-01-18 12:25:12 +08:00
category: 源码学习
tags: spring 源码
comments: true
---

* content
{:toc}

前一篇初略介绍了sping的基础常识，这篇进行详细介绍各个概念及模块的实现原理；




## 什么是 IOC/DI

- IOC(Inversion of Control)控制反转

>所谓控制反转，就是把原先我们代码里面需要实现的对象创建、依赖的代码，反转给容器来帮忙实现。那么必然的我们需要创建一个容器，同时需要一种描述来让容器知道需要创建的对象与对象的关系。这个描述最具体表现就是我们可配置的文件。

- DI(Dependency Injection)依赖注入

>就是指对象是被动接受依赖类而不是自己主动去找，换句话说就是指对象不是从容器中查找它依赖的类，而是在容器实例化对象的时候主动将它依赖的类注入给它。

先从我们自己设计这样一个视角来考虑：
对象和对象关系怎么表示？
可以用 xml，properties 文件等语义化配置文件表示。

描述对象关系的文件存放在哪里？

可能是 classpath，filesystem，或者是 URL 网络资源，servletContext 等。
回到正题，有了配置文件，还需要对配置文件解析。
不同的配置文件对对象的描述不一样，如标准的，自定义声明式的，如何统一？在内部需要有一个统一
的关于对象的定义，所有外部的描述都必须转化成统一的描述定义。

如何对不同的配置文件进行解析？需要对不同的配置文件语法，采用不同的解析器;

## Spring核心容器的体系结构

-  **BeanFactory**

Bean 的创建是典型的工厂模式，这一系列的 Bean 工厂，也即 IOC 容器为开发者管理对象间的依赖关系提供了很多便利和基础服务，在 Spring 中有许多的 IOC 容器的实现供用户选择和使用，其相互关系如下：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/beanfactory1.png)

>简介：其中 BeanFactory 作为最顶层的一个接口类，它定义了 IOC 容器的基本功能规范，BeanFactory 有三
个子类：ListableBeanFactory、HierarchicalBeanFactory 和 AutowireCapableBeanFactory。
但是从上图中我们可以发现最终的默认实现类是 DefaultListableBeanFactory，他实现了所有的接
口。那为何要定义这么多层次的接口呢？查阅这些接口的源码和说明发现，每个接口都有他使用的场合，
它主要是为了区分在 Spring 内部在操作过程中对象的传递和转化过程中，对对象的数据访问所做的限
制。例如 ListableBeanFactory 接口表示这些 Bean 是可列表的，而 HierarchicalBeanFactory 表
示的是这些 Bean 是有继承关系的，也就是每个 Bean 有可能有父 Bean。AutowireCapableBeanFactory
接口定义 Bean 的自动装配规则。这四个接口共同定义了 Bean 的集合、Bean 之间的关系、以及 Bean
行为

最基本的 IOC 容器接口 BeanFactory:

```java
//在 BeanFactory 里只对 IOC 容器的基本行为作了定义，根本不关心你的 Bean 是如何定义怎样加载的。正如我们只关心工厂里得到什么的产品对象，至于工厂是怎么生产这些对象的，这个基本的接口不关心。

public interface BeanFactory {
//对 FactoryBean 的转义定义，因为如果使用 bean 的名字检索 FactoryBean 得到的对象是工厂生成的对象，
//如果需要得到工厂本身，需要转义
String FACTORY_BEAN_PREFIX = "&";
//根据 bean 的名字，获取在 IOC 容器中得到 bean 实例
Object getBean(String name) throws BeansException;
//根据 bean 的名字和 Class 类型来得到 bean 实例，增加了类型安全验证机制。
<T> T getBean(String name, @Nullable Class<T> requiredType) throws BeansException;
Object getBean(String name, Object... args) throws BeansException;
<T> T getBean(Class<T> requiredType) throws BeansException;
<T> T getBean(Class<T> requiredType, Object... args) throws BeansException;
//提供对 bean 的检索，看看是否在 IOC 容器有这个名字的 bean
boolean containsBean(String name);
//根据 bean 名字得到 bean 实例，并同时判断这个 bean 是不是单例
boolean isSingleton(String name) throws NoSuchBeanDefinitionException;
boolean isPrototype(String name) throws NoSuchBeanDefinitionException;
boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException;
boolean isTypeMatch(String name, @Nullable Class<?> typeToMatch) throws NoSuchBeanDefinitionException;
//得到 bean 实例的 Class 类型
@Nullable
Class<?> getType(String name) throws NoSuchBeanDefinitionException;
//得到 bean 的别名，如果根据别名检索，那么其原名也会被检索出来
String[] getAliases(String name);
}

```

>要知道工厂是如何产生对象的，我们需要看具体的 IOC 容器实现，Spring 提供了许多 IOC 容器的实现。比如
`XmlBeanFactory`，`ClasspathXmlApplicationContext `等。其中 XmlBeanFactory 就
是针对最基本的 IOC 容器的实现，这个 IOC 容器可以读取 XML 文件定义的 BeanDefinition（XML 文件
中对 bean 的描述）,如果说 XmlBeanFactory 是容器中的屌丝，ApplicationContext 应该算容器中
的高帅富.

 `ApplicationContext` 是 Spring 提供的一个高级的 IOC 容器，它除了能够提供 IOC 容器的基本功
能外，还为用户提供了以下的附加服务。
从 ApplicationContext 接口的实现，我们看出其特点：
  1. 支持信息源，可以实现国际化。（实现 MessageSource 接口）

  2. 访问资源。(实现 ResourcePatternResolver 接口，后面章节会讲到)

  3. 支持应用事件。(实现 ApplicationEventPublisher 接口)

- BeanDefinition

SpringIOC 容器管理了我们定义的各种 Bean 对象及其相互的关系，Bean 对象在 Spring 实现中是以 BeanDefinition 来描述的，其继承体系如下：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/beanDefintion1.png)

Bean 的解析过程非常复杂，功能被分的很细，因为这里需要被扩展的地方很多，必须保证有足够的灵
活性，以应对可能的变化。Bean 的解析主要就是对 Spring 配置文件的解析。这个解析过程主要通过下
图中的类完成：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/beanreader0.png)

## IOC 容器的初始化

IOC 容器的初始化包括 BeanDefinition 的 Resource: `定位`、`载入`和`注册`这三个基本的过程。

这里以`ClasspathXmlApplicationContext` 为例：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/classpathcontext.png)

>ApplicationContext 允许上下文嵌套，通过保持父上下文可以维持一个上下文体系。对于 Bean 的查
找可以在这个上下文体系中发生，首先检查当前上下文，其次是父上下文，逐级向上，这样为不同的
Spring 应用提供了一个共享的 Bean 定义环境。

简单地演示一下两种 IOC 容器的创建过程:

 1. XmlBeanFactory(屌丝 IOC)的整个流程

 ```java
 public class XmlBeanFactory extends DefaultListableBeanFactory {
private final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);
public XmlBeanFactory(Resource resource) throws BeansException {
this(resource, null);
}
public XmlBeanFactory(Resource resource, BeanFactory parentBeanFactory) throws BeansException {
super(parentBeanFactory);
this.reader.loadBeanDefinitions(resource);
}
}

 ```

 参照源码，自己演示一遍，理解定位、载入、注册:

 ```java
 // 根据 Xml 配置文件创建 Resource 资源对象，该对象中包含了 BeanDefinition 的信息
ClassPathResource resource = new ClassPathResource("application-context.xml");
// 创建 DefaultListableBeanFactory
DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
//创建 XmlBeanDefinitionReader 读取器，用于载入 BeanDefinition。
// 之所以需要 BeanFactory 作为参数，是因为会将读取的信息回调配置给 factory
XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
// XmlBeanDefinitionReader 执行载入 BeanDefinition 的方法，最后会完成 Bean 的载入和注册。
// 完成后 Bean 就成功的放置到 IOC 容器当中，以后我们就可以从中取得 Bean 来使用
reader.loadBeanDefinitions(resource);


 ```
 2. FileSystemXmlApplicationContext 的 IOC 容器流程

源码：

 ```java
 public FileSystemXmlApplicationContext(
String[] configLocations, boolean refresh, @Nullable ApplicationContext parent)
throws BeansException {
super(parent);
setConfigLocations(configLocations);
if (refresh) {
refresh();
}
}

 ```
 >通 过 分 析 FileSystemXmlApplicationContext 的 源 代 码 可 以 知 道 ， 在 创 建
 FileSystemXmlApplicationContext 容器时，构造方法做以下两项重要工作：
 首先，调用父类容器的构造方法(super(parent)方法)为容器设置好 Bean 资源加载器。
 然 后 ， 再 调 用 父 类 AbstractRefreshableConfigApplicationContext 的
 setConfigLocations(configLocations)方法设置 Bean 定义资源文件的定位路径。
 通 过 追 踪 FileSystemXmlApplicationContext 的 继 承 体 系 ， 发 现 其 父 类 的 父 类
 AbstractApplicationContext 中初始化 IOC 容器所做的主要源码如下：

 ```java
 public abstract class AbstractApplicationContext extends DefaultResourceLoader
implements ConfigurableApplicationContext {
//静态初始化块，在整个容器创建过程中只执行一次
static {
//为了避免应用程序在 Weblogic8.1 关闭时出现类加载异常加载问题，加载 IOC 容
//器关闭事件(ContextClosedEvent)类
ContextClosedEvent.class.getName();
}
public AbstractApplicationContext() {
this.resourcePatternResolver = getResourcePatternResolver();
}
public AbstractApplicationContext(@Nullable ApplicationContext parent) {
this();
setParent(parent);
}
//获取一个 Spring Source 的加载器用于读入 Spring Bean 定义资源文件
protected ResourcePatternResolver getResourcePatternResolver() {
//AbstractApplicationContext 继承 DefaultResourceLoader，因此也是一个资源加载器
//Spring 资源加载器，其 getResource(String location)方法用于载入资源
return new PathMatchingResourcePatternResolver(this);
}

}

 ```
 接 下 来 FileSystemXmlApplicationContext 执 行
 setConfigLocations 方法通过调用其父类 AbstractRefreshableConfigApplicationContext 的
 方法进行对 Bean 定义资源文件的定位，该方法的源码如下：

  ```java
  //处理单个资源文件路径为一个字符串的情况
public void setConfigLocation(String location) {
//String CONFIG_LOCATION_DELIMITERS = ",; /t/n";
//即多个资源文件路径之间用” ,; \t\n”分隔，解析成数组形式
setConfigLocations(StringUtils.tokenizeToStringArray(location, CONFIG_LOCATION_DELIMITERS));
}
//解析 Bean 定义资源文件的路径，处理多个资源文件字符串数组
public void setConfigLocations(@Nullable String... locations) {
if (locations != null) {
Assert.noNullElements(locations, "Config locations must not be null");
this.configLocations = new String[locations.length];
for (int i = 0; i < locations.length; i++) {
// resolvePath 为同一个类中将字符串解析为路径的方法
this.configLocations[i] = resolvePath(locations[i]).trim();
}
}
else {
this.configLocations = null;
}
}

  ```



 注：多个资源文件路径之间可以是用” , ; \t\n”等分隔

AbstractApplicationContext 的 refresh 函数载入 Bean 定义过程：

>SpringIOC 容器对 Bean 定义资源的载入是从 refresh()函数开始的，refresh()是一个模板方法，refresh()方法的作用是：在创建 IOC 容器前，如果已经有容器存在，则需要把已有的容器销毁和关闭，以保证在 refresh 之后使用的是新建立起来的 IOC 容器。refresh 的作用类似于对 IOC 容器的重启，在新建立好的容器中对容器进行初始化，对 Bean 定义资源进行载入FileSystemXmlApplicationContext 通过调用其父类 AbstractApplicationContext 的 refresh()函数启动整个 IOC 容器对 Bean 定义的载入过程：

```java

public void refresh() throws BeansException, IllegalStateException {
synchronized (this.startupShutdownMonitor) {
//调用容器准备刷新的方法，获取容器的当时时间，同时给容器设置同步标识
prepareRefresh();
//告诉子类启动 refreshBeanFactory()方法，Bean 定义资源文件的载入从
//子类的 refreshBeanFactory()方法启动
ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
//为 BeanFactory 配置容器特性，例如类加载器、事件处理器等
prepareBeanFactory(beanFactory);
try {
//为容器的某些子类指定特殊的 BeanPost 事件处理器
postProcessBeanFactory(beanFactory);
//调用所有注册的 BeanFactoryPostProcessor 的 Bean
invokeBeanFactoryPostProcessors(beanFactory);
//为 BeanFactory 注册 BeanPost 事件处理器.
//BeanPostProcessor 是 Bean 后置处理器，用于监听容器触发的事件
registerBeanPostProcessors(beanFactory);
//初始化信息源，和国际化相关.
initMessageSource();
//初始化容器事件传播器.
initApplicationEventMulticaster();
//调用子类的某些特殊 Bean 初始化方法
onRefresh();
//为事件传播器注册事件监听器.
registerListeners();
//初始化所有剩余的单例 Bean
finishBeanFactoryInitialization(beanFactory);
//初始化容器的生命周期事件处理器，并发布容器的生命周期事件
finishRefresh();
}
catch (BeansException ex) {
if (logger.isWarnEnabled()) {
logger.warn("Exception encountered during context initialization - " +
"cancelling refresh attempt: " + ex);
}
//销毁已创建的 Bean
destroyBeans();
//取消 refresh 操作，重置容器的同步标识.
cancelRefresh(ex);
throw ex;
}
finally {
resetCommonCaches();
}
}
}

```

refresh()方法主要为 IOC 容器 Bean 的生命周期管理提供条件，Spring IOC 容器载入 Bean 定义资源
文 件 从 其 子 类 容 器 的 refreshBeanFactory() 方 法 启 动 ， 所 以 整 个 refresh() 中
“ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();”这句以后
代码的都是注册容器的信息源和生命周期事件，载入过程就是从这句代码启动。
refresh()方法的作用是：在创建 IOC 容器前，如果已经有容器存在，则需要把已有的容器销毁和关
闭，以保证在 refresh 之后使用的是新建立起来的 IOC 容器。refresh 的作用类似于对 IOC 容器的重
启，在新建立好的容器中对容器进行初始化，对 Bean 定义资源进行载入;

AbstractApplicationContext 的 obtainFreshBeanFactory() 方 法 调 用 子 类 容 器 的
refreshBeanFactory()方法，启动容器载入 Bean 定义资源文件的过程，代码如下：

```java
protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
//这里使用了委派设计模式，父类定义了抽象的 refreshBeanFactory()方法，具体实现调用子类容器的 refreshBeanFactory()方
法
refreshBeanFactory();
ConfigurableListableBeanFactory beanFactory = getBeanFactory();
if (logger.isDebugEnabled()) {
logger.debug("Bean factory for " + getDisplayName() + ": " + beanFactory);
}
return beanFactory;
}


```
AbstractApplicationContext 类中只抽象定义了 refreshBeanFactory()方法，容器真正调用的是
其子类 AbstractRefreshableApplicationContext 实现的 refreshBeanFactory()方法，方法的源
码如下：

```java
protected final void refreshBeanFactory() throws BeansException {
//如果已经有容器，销毁容器中的 bean，关闭容器
if (hasBeanFactory()) {
destroyBeans();
closeBeanFactory();
}
try {
//创建 IOC 容器
DefaultListableBeanFactory beanFactory = createBeanFactory();
beanFactory.setSerializationId(getId());
//对 IOC 容器进行定制化，如设置启动参数，开启注解的自动装配等
customizeBeanFactory(beanFactory);
//调用载入 Bean 定义的方法，主要这里又使用了一个委派模式，在当前类中只定义了抽象的 loadBeanDefinitions 方法，具体的实现调用子类容器
loadBeanDefinitions(beanFactory);
synchronized (this.beanFactoryMonitor) {
this.beanFactory = beanFactory;
}
}
catch (IOException ex) {
throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(),
ex);
}
}

```
在这个方法中，先判断 BeanFactory 是否存在，如果存在则先销毁 beans 并关闭 beanFactory，接着
创建 DefaultListableBeanFactory，并调用 loadBeanDefinitions(beanFactory)装载 bean 定义;

AbstractRefreshableApplicationContext 子类的 loadBeanDefinitions 方法：

AbstractRefreshableApplicationContext 中只定义了抽象的 loadBeanDefinitions 方法，容器
真 正 调 用 的 是 其 子 类 AbstractXmlApplicationContext 对 该 方 法 的 实 现 ，
AbstractXmlApplicationContext 的主要源码如下：
loadBeanDefinitions 方 法 同 样 是 抽 象 方 法 ， 是 由 其 子 类 实 现 的 ， 也 即 在
AbstractXmlApplicationContext 中。

```java
public abstract class AbstractXmlApplicationContext extends AbstractRefreshableConfigApplicationContext {
...
//实现父类抽象的载入 Bean 定义方法
@Override
protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException
{
//创建 XmlBeanDefinitionReader，即创建 Bean 读取器，并通过回调设置到容器中去，容器使用该读取器读取 Bean 定义资源
XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
//为 Bean 读取器设置 Spring 资源加载器，AbstractXmlApplicationContext 的
//祖先父类 AbstractApplicationContext 继承 DefaultResourceLoader，因此，容器本身也是一个资源加载器
beanDefinitionReader.setEnvironment(this.getEnvironment());
beanDefinitionReader.setResourceLoader(this);
//为 Bean 读取器设置 SAX xml 解析器
beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));
//当 Bean 读取器读取 Bean 定义的 Xml 资源文件时，启用 Xml 的校验机制
initBeanDefinitionReader(beanDefinitionReader);
//Bean 读取器真正实现加载的方法
loadBeanDefinitions(beanDefinitionReader);
}
protected void initBeanDefinitionReader(XmlBeanDefinitionReader reader) {
reader.setValidating(this.validating);
}
//Xml Bean 读取器加载 Bean 定义资源
protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
//获取 Bean 定义资源的定位
Resource[] configResources = getConfigResources();
if (configResources != null) {
//Xml Bean 读取器调用其父类 AbstractBeanDefinitionReader 读取定位的 Bean 定义资源
reader.loadBeanDefinitions(configResources);
}
// 如果子类中获取的 Bean 定义资源定位为空，则获取 FileSystemXmlApplicationContext
// 构造方法中 setConfigLocations 方法设置的资源
String[] configLocations = getConfigLocations();
if (configLocations != null) {
//Xml Bean 读取器调用其父类 AbstractBeanDefinitionReader 读取定位
//的 Bean 定义资源
reader.loadBeanDefinitions(configLocations);
}
}
//这里又使用了一个委托模式，调用子类的获取 Bean 定义资源定位的方法
//该方法在 ClassPathXmlApplicationContext 中进行实现，对于我们
//举例分析源码的 FileSystemXmlApplicationContext 没有使用该方法
@Nullable
protected Resource[] getConfigResources() {
return null;
}
}
```

>Xml Bean 读取器(XmlBeanDefinitionReader)调用其父类 AbstractBeanDefinitionReader 的
reader.loadBeanDefinitions 方法读取 Bean 定义资源。
由于我们使用 FileSystemXmlApplicationContext 作为例子分析，因此 getConfigResources 的返
回值为 null，因此程序执行 reader.loadBeanDefinitions(configLocations)分支。

AbstractBeanDefinitionReader 读 取 Bean 定 义 资 源 , 在 其 抽 象 父 类
AbstractBeanDefinitionReader 中定义了载入过程。
AbstractBeanDefinitionReader 的 loadBeanDefinitions 方法源码如下：

```java
//重载方法，调用下面的 loadBeanDefinitions(String, Set<Resource>);方法
@Override
public int loadBeanDefinitions(String location) throws BeanDefinitionStoreException {
return loadBeanDefinitions(location, null);
}
public int loadBeanDefinitions(String location, @Nullable Set<Resource> actualResources) throws
BeanDefinitionStoreException {
//获取在 IOC 容器初始化过程中设置的资源加载器
ResourceLoader resourceLoader = getResourceLoader();
if (resourceLoader == null) {
throw new BeanDefinitionStoreException(
"Cannot import bean definitions from location [" + location + "]: no ResourceLoader available");
}
if (resourceLoader instanceof ResourcePatternResolver) {
// Resource pattern matching available.
try {
//将指定位置的 Bean 定义资源文件解析为 Spring IOC 容器封装的资源
//加载多个指定位置的 Bean 定义资源文件
Resource[] resources = ((ResourcePatternResolver) resourceLoader).getResources(location);
//委派调用其子类 XmlBeanDefinitionReader 的方法，实现加载功能
int loadCount = loadBeanDefinitions(resources);
if (actualResources != null) {
for (Resource resource : resources) {
actualResources.add(resource);
}
}
if (logger.isDebugEnabled()) {
logger.debug("Loaded " + loadCount + " bean definitions from location pattern [" + location + "]");
}
return loadCount;
}
catch (IOException ex) {
throw new BeanDefinitionStoreException(
"Could not resolve bean definition resource pattern [" + location + "]", ex);
}
}
else {
// Can only load single resources by absolute URL.
//将指定位置的 Bean 定义资源文件解析为 Spring IOC 容器封装的资源
//加载单个指定位置的 Bean 定义资源文件
Resource resource = resourceLoader.getResource(location);
//委派调用其子类 XmlBeanDefinitionReader 的方法，实现加载功能
int loadCount = loadBeanDefinitions(resource);
if (actualResources != null) {
actualResources.add(resource);
}
if (logger.isDebugEnabled()) {
logger.debug("Loaded " + loadCount + " bean definitions from location [" + location + "]");
}
return loadCount;
}
}
//重载方法，调用 loadBeanDefinitions(String);
@Override
public int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException {
Assert.notNull(locations, "Location array must not be null");
int counter = 0;
for (String location : locations) {
counter += loadBeanDefinitions(location);
}
return counter;
}
```
loadBeanDefinitions(Resource...resources)方法和上面分析的 3 个方法类似，同样也是调用
XmlBeanDefinitionReader 的 loadBeanDefinitions 方法。
从对 AbstractBeanDefinitionReader 的 loadBeanDefinitions 方法源码分析可以看出该方法做了
以下两件事：
>首先，调用资源加载器的获取资源方法 resourceLoader.getResource(location)，获取到要加载的
资源。其次，真正执行加载功能是其子类 XmlBeanDefinitionReader 的 loadBeanDefinitions 方法。

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/resourcel1.png)

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/resource2.png)

看到上面的 ResourceLoader 与 ApplicationContext 的继承系图，可以知道其实际调用的是
DefaultResourceLoader 中 的 getSource() 方 法 定 位 Resource ， 因 为
FileSystemXmlApplicationContext 本身就是 DefaultResourceLoader 的实现类，所以此时又回到
了 FileSystemXmlApplicationContext 中来。

- 资源加载器获取要读入的资源：

XmlBeanDefinitionReader 通过调用其父类 DefaultResourceLoader 的 getResource 方法获取要加载的资源，其源码如下:

```java
//获取 Resource 的具体实现方法
@Override
public Resource getResource(String location) {
Assert.notNull(location, "Location must not be null");
for (ProtocolResolver protocolResolver : this.protocolResolvers) {
Resource resource = protocolResolver.resolve(location, this);
if (resource != null) {
return resource;
}
}
//如果是类路径的方式，那需要使用 ClassPathResource 来得到 bean 文件的资源对象
if (location.startsWith("/")) {
return getResourceByPath(location);
}
else if (location.startsWith(CLASSPATH_URL_PREFIX)) {
return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()), getClassLoader());
}
else {
try {
// 如果是 URL 方式，使用 UrlResource 作为 bean 文件的资源对象
URL url = new URL(location);
return (ResourceUtils.isFileURL(url) ? new FileUrlResource(url) : new UrlResource(url));
}
catch (MalformedURLException ex) {
//如果既不是 classpath 标识，又不是 URL 标识的 Resource 定位，则调用
//容器本身的 getResourceByPath 方法获取 Resource
return getResourceByPath(location);
}
}
}

```
FileSystemXmlApplicationContext 容器提供了 getResourceByPath 方法的实现，就是为了处理既
不是 classpath 标识，又不是 URL 标识的 Resource 定位这种情况。

```java
@Override
protected Resource getResourceByPath(String path) {
if (path.startsWith("/")) {
path = path.substring(1);
}
//这里使用文件系统资源对象来定义 bean 文件
return new FileSystemResource(path);
}


```

这样代码就回到了 FileSystemXmlApplicationContext 中来，他提供了 FileSystemResource 来完
成从文件系统得到配置文件的资源定义。
这样，就可以从文件系统路径上对 IOC 配置文件进行加载，当然我们可以按照这个逻辑从任何地方加载，
在 Spring 中 我 们 看 到 它 提 供 的 各 种 资 源 抽 象 ， 比 如
ClassPathResource,URLResource,FileSystemResource 等来供我们使用。上面我们看到的是定位
Resource 的一个过程，而这只是加载过程的一部分

- XmlBeanDefinitionReader 加载 Bean 定义资源：

继续回到 XmlBeanDefinitionReader 的 loadBeanDefinitions(Resource …)方法看到代表
bean 文件的资源定义以后的载入过程。

```java
//XmlBeanDefinitionReader 加载资源的入口方法
@Override
public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
//将读入的 XML 资源进行特殊编码处理
return loadBeanDefinitions(new EncodedResource(resource));
}
//这里是载入 XML 形式 Bean 定义资源文件方法
public int loadBeanDefinitions(EncodedResource encodedResource) throws BeanDefinitionStoreException {
...
try {
//将资源文件转为 InputStream 的 IO 流
InputStream inputStream = encodedResource.getResource().getInputStream();
try {
//从 InputStream 中得到 XML 的解析源
InputSource inputSource = new InputSource(inputStream);
if (encodedResource.getEncoding() != null) {
inputSource.setEncoding(encodedResource.getEncoding());
}
//这里是具体的读取过程
return doLoadBeanDefinitions(inputSource, encodedResource.getResource());
}
finally {
//关闭从 Resource 中得到的 IO 流
inputStream.close();
}
}
...
}
//从特定 XML 文件中实际载入 Bean 定义资源的方法
protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource)
throws BeanDefinitionStoreException {
try {
//将 XML 文件转换为 DOM 对象，解析过程由 documentLoader 实现
Document doc = doLoadDocument(inputSource, resource);
//这里是启动对 Bean 定义解析的详细过程，该解析过程会用到 Spring 的 Bean 配置规则
return registerBeanDefinitions(doc, resource);
}
...
}

```

通过源码分析，载入 Bean 定义资源文件的最后一步是将 Bean 定义资源转换为 Document 对象，该过程
由 documentLoader 实现;

- DocumentLoader 将 Bean 定义资源转换为 Document 对象：
DocumentLoader 将 Bean 定义资源转换成 Document 对象的源码如下:

```java
//使用标准的 JAXP 将载入的 Bean 定义资源转换成 document 对象
@Override
public Document loadDocument(InputSource inputSource, EntityResolver entityResolver,
ErrorHandler errorHandler, int validationMode, boolean namespaceAware) throws Exception {
//创建文件解析器工厂
DocumentBuilderFactory factory = createDocumentBuilderFactory(validationMode, namespaceAware);
if (logger.isDebugEnabled()) {
logger.debug("Using JAXP provider [" + factory.getClass().getName() + "]");
}
//创建文档解析器
DocumentBuilder builder = createDocumentBuilder(factory, entityResolver, errorHandler);
//解析 Spring 的 Bean 定义资源
return builder.parse(inputSource);
}
protected DocumentBuilderFactory createDocumentBuilderFactory(int validationMode, boolean namespaceAware)
throws ParserConfigurationException {
//创建文档解析工厂
DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
factory.setNamespaceAware(namespaceAware);
//设置解析 XML 的校验
if (validationMode != XmlValidationModeDetector.VALIDATION_NONE) {
factory.setValidating(true);
if (validationMode == XmlValidationModeDetector.VALIDATION_XSD) {
// Enforce namespace aware for XSD...
factory.setNamespaceAware(true);
try {
factory.setAttribute(SCHEMA_LANGUAGE_ATTRIBUTE, XSD_SCHEMA_LANGUAGE);
}
catch (IllegalArgumentException ex) {
ParserConfigurationException pcex = new ParserConfigurationException(
"Unable to validate using XSD: Your JAXP provider [" + factory +
"] does not support XML Schema. Are you running on Java 1.4 with Apache Crimson? " +
"Upgrade to Apache Xerces (or Java 1.5) for full XSD support.");
pcex.initCause(ex);
throw pcex;
}
}
}
return factory;
}

```
该解析过程调用 JavaEE 标准的 JAXP 标准进行处理。
至此 Spring IOC 容器根据定位的 Bean 定义资源文件，将其加载读入并转换成为 Document 对象过程
完成。接下来我们要继续分析 Spring IOC 容器将载入的 Bean 定义资源文件转换为 Document 对象之
后，是如何将其解析为 Spring IOC 管理的 Bean 对象并将其注册到容器中的。

- XmlBeanDefinitionReader 解析载入的 Bean 定义资源文件：

XmlBeanDefinitionReader 类中的 doLoadBeanDefinitions 方法是从特定 XML 文件中实际载入
Bean 定义资源的方法，该方法在载入 Bean 定义资源之后将其转换为 Document 对象，接下来调用
registerBeanDefinitions 启 动 Spring IOC 容 器 对 Bean 定 义 的 解 析 过 程 ，
registerBeanDefinitions 方法源码如下：

```java
//按照 Spring 的 Bean 语义要求将 Bean 定义资源解析并转换为容器内部数据结构
public int registerBeanDefinitions(Document doc, Resource resource) throws BeanDefinitionStoreException {
//得到 BeanDefinitionDocumentReader 来对 xml 格式的 BeanDefinition 解析
BeanDefinitionDocumentReader documentReader = createBeanDefinitionDocumentReader();
//获得容器中注册的 Bean 数量
int countBefore = getRegistry().getBeanDefinitionCount();
//解析过程入口，这里使用了委派模式，BeanDefinitionDocumentReader 只是个接口,
//具体的解析实现过程有实现类 DefaultBeanDefinitionDocumentReader 完成
documentReader.registerBeanDefinitions(doc, createReaderContext(resource));
//统计解析的 Bean 数量
return getRegistry().getBeanDefinitionCount() - countBefore;
}

```

Bean 定义资源的载入解析分为以下两个过程：
首先，通过调用 XML 解析器将 Bean 定义资源文件转换得到 Document 对象，但是这些 Document 对象
并没有按照 Spring 的 Bean 规则进行解析。这一步是载入的过程
其次，在完成通用的 XML 解析之后，按照 Spring 的 Bean 规则对 Document 对象进行解析。
按照 Spring 的 Bean 规则对 Document 对象解析的过程是在接口 BeanDefinitionDocumentReader
的实现类 DefaultBeanDefinitionDocumentReader 中实现的。

- DefaultBeanDefinitionDocumentReader 对 Bean 定义的 Document 对象解析：
BeanDefinitionDocumentReader 接 口 通 过 registerBeanDefinitions 方 法 调 用 其 实 现 类
DefaultBeanDefinitionDocumentReader 对 Document 对象进行解析，解析的代码如下:

```java
//根据 Spring DTD 对 Bean 的定义规则解析 Bean 定义 Document 对象
@Override
public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
//获得 XML 描述符
this.readerContext = readerContext;
logger.debug("Loading bean definitions");
//获得 Document 的根元素
Element root = doc.getDocumentElement();
doRegisterBeanDefinitions(root);
}
...
protected void doRegisterBeanDefinitions(Element root) {
//具体的解析过程由 BeanDefinitionParserDelegate 实现，
//BeanDefinitionParserDelegate 中定义了 Spring Bean 定义 XML 文件的各种元素
BeanDefinitionParserDelegate parent = this.delegate;
this.delegate = createDelegate(getReaderContext(), root, parent);
if (this.delegate.isDefaultNamespace(root)) {
String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
if (StringUtils.hasText(profileSpec)) {
String[] specifiedProfiles = StringUtils.tokenizeToStringArray(
profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
if (logger.isInfoEnabled()) {
logger.info("Skipped XML bean definition file due to specified profiles [" + profileSpec +
"] not matching: " + getReaderContext().getResource());
}
return;
}
}
}
//在解析 Bean 定义之前，进行自定义的解析，增强解析过程的可扩展性
preProcessXml(root);
//从 Document 的根元素开始进行 Bean 定义的 Document 对象
parseBeanDefinitions(root, this.delegate);
//在解析 Bean 定义之后，进行自定义的解析，增加解析过程的可扩展性
postProcessXml(root);
this.delegate = parent;
}
//创建 BeanDefinitionParserDelegate，用于完成真正的解析过程
protected BeanDefinitionParserDelegate createDelegate(
XmlReaderContext readerContext, Element root, @Nullable BeanDefinitionParserDelegate parentDelegate) {
BeanDefinitionParserDelegate delegate = new BeanDefinitionParserDelegate(readerContext);
//BeanDefinitionParserDelegate 初始化 Document 根元素
delegate.initDefaults(root, parentDelegate);
return delegate;
}
//使用 Spring 的 Bean 规则从 Document 的根元素开始进行 Bean 定义的 Document 对象
protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
//Bean 定义的 Document 对象使用了 Spring 默认的 XML 命名空间
if (delegate.isDefaultNamespace(root)) {
//获取 Bean 定义的 Document 对象根元素的所有子节点
NodeList nl = root.getChildNodes();
for (int i = 0; i < nl.getLength(); i++) {
Node node = nl.item(i);
//获得 Document 节点是 XML 元素节点
if (node instanceof Element) {
Element ele = (Element) node;
//Bean 定义的 Document 的元素节点使用的是 Spring 默认的 XML 命名空间
if (delegate.isDefaultNamespace(ele)) {
//使用 Spring 的 Bean 规则解析元素节点
parseDefaultElement(ele, delegate);
}
else {
//没有使用 Spring 默认的 XML 命名空间，则使用用户自定义的解//析规则解析元素节点
delegate.parseCustomElement(ele);
}
}
}
}
else {
//Document 的根节点没有使用 Spring 默认的命名空间，则使用用户自定义的
//解析规则解析 Document 根节点
delegate.parseCustomElement(root);
}
}
//使用 Spring 的 Bean 规则解析 Document 元素节点
private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
//如果元素节点是<Import>导入元素，进行导入解析
if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
importBeanDefinitionResource(ele);
}
//如果元素节点是<Alias>别名元素，进行别名解析
else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
processAliasRegistration(ele);
}
//元素节点既不是导入元素，也不是别名元素，即普通的<Bean>元素，
//按照 Spring 的 Bean 规则解析元素
else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
processBeanDefinition(ele, delegate);
}
else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
// recurse
doRegisterBeanDefinitions(ele);
}
}
//解析<Import>导入元素，从给定的导入路径加载 Bean 定义资源到 Spring IOC 容器中
protected void importBeanDefinitionResource(Element ele) {
//获取给定的导入元素的 location 属性
String location = ele.getAttribute(RESOURCE_ATTRIBUTE);
//如果导入元素的 location 属性值为空，则没有导入任何资源，直接返回
if (!StringUtils.hasText(location)) {
getReaderContext().error("Resource location must not be empty", ele);
return;
}
//使用系统变量值解析 location 属性值
location = getReaderContext().getEnvironment().resolveRequiredPlaceholders(location);
Set<Resource> actualResources = new LinkedHashSet<>(4);
//标识给定的导入元素的 location 是否是绝对路径
boolean absoluteLocation = false;
try {
absoluteLocation = ResourcePatternUtils.isUrl(location) || ResourceUtils.toURI(location).isAbsolute();
}
catch (URISyntaxException ex) {
//给定的导入元素的 location 不是绝对路径
}
// Absolute or relative?
//给定的导入元素的 location 是绝对路径
if (absoluteLocation) {
try {
//使用资源读入器加载给定路径的 Bean 定义资源
int importCount = getReaderContext().getReader().loadBeanDefinitions(location, actualResources);
if (logger.isDebugEnabled()) {
logger.debug("Imported " + importCount + " bean definitions from URL location [" + location + "]");
}
}
catch (BeanDefinitionStoreException ex) {
getReaderContext().error(
"Failed to import bean definitions from URL location [" + location + "]", ele, ex);
}
}
else {
//给定的导入元素的 location 是相对路径
try {
int importCount;
//将给定导入元素的 location 封装为相对路径资源
Resource relativeResource = getReaderContext().getResource().createRelative(location);
//封装的相对路径资源存在
if (relativeResource.exists()) {
  //使用资源读入器加载 Bean 定义资源
importCount = getReaderContext().getReader().loadBeanDefinitions(relativeResource);
actualResources.add(relativeResource);
}
//封装的相对路径资源不存在
else {
//获取 Spring IOC 容器资源读入器的基本路径
String baseLocation = getReaderContext().getResource().getURL().toString();
//根据 Spring IOC 容器资源读入器的基本路径加载给定导入路径的资源
importCount = getReaderContext().getReader().loadBeanDefinitions(
StringUtils.applyRelativePath(baseLocation, location), actualResources);
}
if (logger.isDebugEnabled()) {
logger.debug("Imported " + importCount + " bean definitions from relative location [" + location +
"]");
}
}
catch (IOException ex) {
getReaderContext().error("Failed to resolve current resource location", ele, ex);
}
catch (BeanDefinitionStoreException ex) {
getReaderContext().error("Failed to import bean definitions from relative location [" + location + "]",
ele, ex);
}
}
Resource[] actResArray = actualResources.toArray(new Resource[actualResources.size()]);
//在解析完<Import>元素之后，发送容器导入其他资源处理完成事件
getReaderContext().fireImportProcessed(location, actResArray, extractSource(ele));
}
//解析<Alias>别名元素，为 Bean 向 Spring IOC 容器注册别名
protected void processAliasRegistration(Element ele) {
//获取<Alias>别名元素中 name 的属性值
String name = ele.getAttribute(NAME_ATTRIBUTE);
//获取<Alias>别名元素中 alias 的属性值
String alias = ele.getAttribute(ALIAS_ATTRIBUTE);
boolean valid = true;
//<alias>别名元素的 name 属性值为空
if (!StringUtils.hasText(name)) {
getReaderContext().error("Name must not be empty", ele);
valid = false;
}
//<alias>别名元素的 alias 属性值为空
if (!StringUtils.hasText(alias)) {
getReaderContext().error("Alias must not be empty", ele);
valid = false;
}
if (valid) {
try {
//向容器的资源读入器注册别名
getReaderContext().getRegistry().registerAlias(name, alias);
}
catch (Exception ex) {
getReaderContext().error("Failed to register alias '" + alias +
"' for bean with name '" + name + "'", ele, ex);
}
//在解析完<Alias>元素之后，发送容器别名处理完成事件
getReaderContext().fireAliasRegistered(name, alias, extractSource(ele));
}
}
//解析 Bean 定义资源 Document 对象的普通元素
protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
// BeanDefinitionHolder 是对 BeanDefinition 的封装，即 Bean 定义的封装类
//对 Document 对象中<Bean>元素的解析由 BeanDefinitionParserDelegate 实现
// BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
if (bdHolder != null) {
bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
try {
//向 Spring IOC 容器注册解析得到的 Bean 定义，这是 Bean 定义向 IOC 容器注册的入口
BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
}
catch (BeanDefinitionStoreException ex) {
getReaderContext().error("Failed to register bean definition with name '" +
bdHolder.getBeanName() + "'", ele, ex);
}
//在完成向 Spring IOC 容器注册解析得到的 Bean 定义之后，发送注册事件
getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
}
}

```

通过上述 Spring IOC 容器对载入的 Bean 定义 Document 解析可以看出，我们使用 Spring 时，在 Spring
配置文件中可以使用<import>元素来导入 IOC 容器所需要的其他资源，Spring IOC 容器在解析时会首
先将指定导入的资源加载进容器中。使用<ailas>别名时，Spring IOC 容器首先将别名元素所定义的
别名注册到容器中。
对于既不是<import>元素，又不是<alias>元素的元素，即 Spring 配置文件中普通的<bean>元素的解
析由 BeanDefinitionParserDelegate 类的 parseBeanDefinitionElement 方法来实现。

- BeanDefinitionParserDelegate 解析 Bean 定义资源文件中的<bean>元素：

在使用 SpringIOC 容器的时候我们还需要区别两个概念:

>BeanFactory 和 FactoryBean ， 其 中 BeanFactory 指 的 是 IOC 容 器 的 编 程 抽 象 ， 比 如
ApplicationContext，XmlBeanFactory 等，这些都是 IOC 容器的具体表现，需要使用什么样的容器
由客户决定,但 Spring 为我们提供了丰富的选择。FactoryBean 只是一个可以在 IOC 而容器中被管理
的一个 Bean,是对各种处理过程和资源使用的抽象,FactoryBean 在需要时产生另一个对象，而不返回
FactoryBean 本身,我们可以把它看成是一个抽象工厂，对它的调用返回的是工厂生产的产品。所有的
FactoryBean 都实现特殊的 org.springframework.beans.factory.FactoryBean 接口，当使用容
器中 FactoryBean 的时候，该容器不会返回 FactoryBean 本身,而是返回其生成的对象。Spring 包括
了大部分的通用资源和服务访问抽象的 FactoryBean 的实现，其中包括:对 JNDI 查询的处理，对代理
对象的处理，对事务性代理的处理，对 RMI 代理的处理等，这些我们都可以看成是具体的工厂,看成是
Spring 为我们建立好的工厂。也就是说 Spring 通过使用抽象工厂模式为我们准备了一系列工厂来生产
一些特定的对象,免除我们手工重复的工作，我们要使用时只需要在 IOC 容器里配置好就能很方便的使
用了。
