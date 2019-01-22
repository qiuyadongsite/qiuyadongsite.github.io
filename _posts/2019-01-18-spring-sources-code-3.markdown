---
layout: post
title:  spring源码解析三
date:   2019-01-18 22:25:12 +08:00
category: 源码学习
tags: spring 源码
comments: true
---

* content
{:toc}

前一篇详细从源码介绍了sping的定位、加载、注册，这一篇将从具体使用介绍spring,基于 XML 的依赖注入、基于 Annotation 的依赖注入；






## 总结一下上一篇的时序图

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/springjiazaibean.png)

## 基于 XML 的依赖注入

- 依赖注入发生的时间
当 Spring IOC 容器完成了 Bean 定义资源的定位、载入和解析注册以后，IOC 容器中已经管理类 Bean定义的相关数据，但是此时 IOC 容器还没有对所管理的 Bean 进行依赖注入，依赖注入在以下两种情况发生：
  1. 用户第一次通过 getBean 方法向 IOC 容索要 Bean 时，IOC 容器触发依赖注入。

  2. 当用户在 Bean 定义资源中为<bean>元素配置了 lazy-init 属性，即让容器在解析注册 Bean 定义时进行预实例化，触发依赖注入。

  BeanFactory 接口定义了 Spring IOC 容器的基本功能规范，是 Spring IOC 容器所应遵守的最底层最基本的编程规范。BeanFactory 接口中定义了几个 getBean 方法，就是用户向 IOC 容器索取管理的Bean 的方法，我们通过分析其子类的具体实现，理解 Spring IOC 容器在用户索取 Bean 时如何完成依赖注入。

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/defaultListablebf.png)

在 BeanFactory 中我们看到 getBean（String...）函数，它的具体实现在 AbstractBeanFactory的doGetBean
中。

- AbstractBeanFactory 通过 getBean 向 IOC 容器获取被管理的 Bean，AbstractBeanFactory 的 getBean 相关方法的源码如下：

```java
@SuppressWarnings("unchecked")
//真正实现向 IOC 容器获取 Bean 的功能，也是触发依赖注入功能的地方
protected <T> T doGetBean(final String name, @Nullable final Class<T> requiredType,
@Nullable final Object[] args, boolean typeCheckOnly) throws BeansException {
//根据指定的名称获取被管理 Bean 的名称，剥离指定名称中对容器的相关依赖
//如果指定的是别名，将别名转换为规范的 Bean 名称
final String beanName = transformedBeanName(name);
Object bean;


//先从缓存中取是否已经有被创建过的单态类型的 Bean
//对于单例模式的 Bean 整个 IOC 容器中只创建一次，不需要重复创建
Object sharedInstance = getSingleton(beanName);
//IOC 容器创建单例模式 Bean 实例对象
if (sharedInstance != null && args == null) {
if (logger.isDebugEnabled()) {
//如果指定名称的 Bean 在容器中已有单例模式的 Bean 被创建
//直接返回已经创建的 Bean
if (isSingletonCurrentlyInCreation(beanName)) {
logger.debug("Returning eagerly cached instance of singleton bean '" + beanName +
"' that is not fully initialized yet - a consequence of a circular reference");
}
else {
logger.debug("Returning cached instance of singleton bean '" + beanName + "'");
}
}
//获取给定 Bean 的实例对象，主要是完成 FactoryBean 的相关处理
//注意：BeanFactory 是管理容器中 Bean 的工厂，而 FactoryBean 是
//创建创建对象的工厂 Bean，两者之间有区别
bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
}
else {
//缓存没有正在创建的单例模式 Bean
//缓存中已经有已经创建的原型模式 Bean
//但是由于循环引用的问题导致实例化对象失败
if (isPrototypeCurrentlyInCreation(beanName)) {
throw new BeanCurrentlyInCreationException(beanName);
}
//对 IOC 容器中是否存在指定名称的 BeanDefinition 进行检查，首先检查是否
//能在当前的 BeanFactory 中获取的所需要的 Bean，如果不能则委托当前容器
//的父级容器去查找，如果还是找不到则沿着容器的继承体系向父级容器查找
BeanFactory parentBeanFactory = getParentBeanFactory();
//当前容器的父级容器存在，且当前容器中不存在指定名称的 Bean
if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
//解析指定 Bean 名称的原始名称
String nameToLookup = originalBeanName(name);
if (parentBeanFactory instanceof AbstractBeanFactory) {
return ((AbstractBeanFactory) parentBeanFactory).doGetBean(
nameToLookup, requiredType, args, typeCheckOnly);
}
else if (args != null) {
咕泡出品，必属精品 www.gupaoedu.com
53
//委派父级容器根据指定名称和显式的参数查找
return (T) parentBeanFactory.getBean(nameToLookup, args);
}
else {
//委派父级容器根据指定名称和类型查找
return parentBeanFactory.getBean(nameToLookup, requiredType);
}
}
//创建的 Bean 是否需要进行类型验证，一般不需要
if (!typeCheckOnly) {
//向容器标记指定的 Bean 已经被创建
markBeanAsCreated(beanName);
}
try {
//根据指定 Bean 名称获取其父级的 Bean 定义
//主要解决 Bean 继承时子类合并父类公共属性问题
final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
checkMergedBeanDefinition(mbd, beanName, args);
//获取当前 Bean 所有依赖 Bean 的名称
String[] dependsOn = mbd.getDependsOn();
//如果当前 Bean 有依赖 Bean
if (dependsOn != null) {
for (String dep : dependsOn) {
if (isDependent(beanName, dep)) {
throw new BeanCreationException(mbd.getResourceDescription(), beanName,
"Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
}
//递归调用 getBean 方法，获取当前 Bean 的依赖 Bean
registerDependentBean(dep, beanName);
//把被依赖 Bean 注册给当前依赖的 Bean
getBean(dep);
}
}
//创建单例模式 Bean 的实例对象
if (mbd.isSingleton()) {
//这里使用了一个匿名内部类，创建 Bean 实例对象，并且注册给所依赖的对象
sharedInstance = getSingleton(beanName, () -> {
try {
//创建一个指定 Bean 实例对象，如果有父级继承，则合并子类和父类的定义
return createBean(beanName, mbd, args);


}
catch (BeansException ex) {
//显式地从容器单例模式 Bean 缓存中清除实例对象
destroySingleton(beanName);
throw ex;
}
});
//获取给定 Bean 的实例对象
bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
}
//IOC 容器创建原型模式 Bean 实例对象
else if (mbd.isPrototype()) {
//原型模式(Prototype)是每次都会创建一个新的对象
Object prototypeInstance = null;
try {
//回调 beforePrototypeCreation 方法，默认的功能是注册当前创建的原型对象
beforePrototypeCreation(beanName);
//创建指定 Bean 对象实例
prototypeInstance = createBean(beanName, mbd, args);
}
finally {
//回调 afterPrototypeCreation 方法，默认的功能告诉 IOC 容器指定 Bean 的原型对象不再创建
afterPrototypeCreation(beanName);
}
//获取给定 Bean 的实例对象
bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
}
//要创建的 Bean 既不是单例模式，也不是原型模式，则根据 Bean 定义资源中
//配置的生命周期范围，选择实例化 Bean 的合适方法，这种在 Web 应用程序中
//比较常用，如：request、session、application 等生命周期
else {
String scopeName = mbd.getScope();
final Scope scope = this.scopes.get(scopeName);
//Bean 定义资源中没有配置生命周期范围，则 Bean 定义不合法
if (scope == null) {
throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
}
try {
//这里又使用了一个匿名内部类，获取一个指定生命周期范围的实例
Object scopedInstance = scope.get(beanName, () -> {
beforePrototypeCreation(beanName);
try {


return createBean(beanName, mbd, args);
}
finally {
afterPrototypeCreation(beanName);
}
});
//获取给定 Bean 的实例对象
bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
}
catch (IllegalStateException ex) {
throw new BeanCreationException(beanName,
"Scope '" + scopeName + "' is not active for the current thread; consider " +
"defining a scoped proxy for this bean if you intend to refer to it from a singleton",
ex);
}
}
}
catch (BeansException ex) {
cleanupAfterBeanCreationFailure(beanName);
throw ex;
}
}
//对创建的 Bean 实例对象进行类型检查
if (requiredType != null && !requiredType.isInstance(bean)) {
try {
T convertedBean = getTypeConverter().convertIfNecessary(bean, requiredType);
if (convertedBean == null) {
throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
}
return convertedBean;
}
catch (TypeMismatchException ex) {
if (logger.isDebugEnabled()) {
logger.debug("Failed to convert bean '" + name + "' to required type '" +
ClassUtils.getQualifiedName(requiredType) + "'", ex);
}
throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
}
}
return (T) bean;
}

```
通过上面对向 IOC 容器获取 Bean 方法的分析，我们可以看到在 Spring 中，如果 Bean 定义的单例模式
(Singleton)，则容器在创建之前先从缓存中查找，以确保整个容器中只存在一个实例对象。如果 Bean
定义的是原型模式(Prototype)，则容器每次都会创建一个新的实例对象。除此之外，Bean 定义还可
以扩展为指定其生命周期范围。
上面的源码只是定义了根据 Bean 定义的模式，采取的不同创建 Bean 实例对象的策略，具体的 Bean 实
例 对 象 的 创 建 过 程 由 实 现 了 ObejctFactory 接 口 的 匿 名 内 部 类 的 createBean 方 法 完 成 ，
ObejctFactory 使 用 委 派 模 式 ， 具 体 的 Bean 实 例 创 建 过 程 交 由 其 实 现 类
AbstractAutowireCapableBeanFactory 完 成 ， 我 们 继 续 分 析
AbstractAutowireCapableBeanFactory 的 createBean 方法的源码，理解其创建 Bean 实例的具体
实现过程.

- AbstractAutowireCapableBeanFactory 创建 Bean 实例对象

AbstractAutowireCapableBeanFactory 类实现了 ObejctFactory 接口，创建容器指定的 Bean 实例
对象，同时还对创建的 Bean 实例对象进行初始化处理。其创建 Bean 实例对象的方法源码如下：

```java
//创建 Bean 实例对象
@Override
protected Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
throws BeanCreationException {
if (logger.isDebugEnabled()) {
logger.debug("Creating instance of bean '" + beanName + "'");
}
RootBeanDefinition mbdToUse = mbd;
//判断需要创建的 Bean 是否可以实例化，即是否可以通过当前的类加载器加载
Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
mbdToUse = new RootBeanDefinition(mbd);
mbdToUse.setBeanClass(resolvedClass);
}
//校验和准备 Bean 中的方法覆盖
try {
mbdToUse.prepareMethodOverrides();
}
catch (BeanDefinitionValidationException ex) {
throw new BeanDefinitionStoreException(mbdToUse.getResourceDescription(),
beanName, "Validation of Method overrides failed", ex);
}
try {
//如果 Bean 配置了初始化前和初始化后的处理器，则试图返回一个需要创建 Bean 的代理对象
Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
if (bean != null) {

return bean;
}
}
catch (Throwable ex) {
throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName,
"BeanPostProcessor before instantiation of bean failed", ex);
}
try {
//创建 Bean 的入口
Object beanInstance = doCreateBean(beanName, mbdToUse, args);
if (logger.isDebugEnabled()) {
logger.debug("Finished creating instance of bean '" + beanName + "'");
}
return beanInstance;
}
catch (BeanCreationException ex) {
throw ex;
}
catch (ImplicitlyAppearedSingletonException ex) {
throw ex;
}
catch (Throwable ex) {
throw new BeanCreationException(
mbdToUse.getResourceDescription(), beanName, "Unexpected exception during bean creation", ex);
}
}
//真正创建 Bean 的方法
protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final @Nullable Object[] args)
throws BeanCreationException {
//封装被创建的 Bean 对象
BeanWrapper instanceWrapper = null;
if (mbd.isSingleton()) {
instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
}
if (instanceWrapper == null) {
instanceWrapper = createBeanInstance(beanName, mbd, args);
}
final Object bean = instanceWrapper.getWrappedInstance();
//获取实例化对象的类型
Class<?> beanType = instanceWrapper.getWrappedClass();
if (beanType != NullBean.class) {

mbd.resolvedTargetType = beanType;
}
//调用 PostProcessor 后置处理器
synchronized (mbd.postProcessingLock) {
if (!mbd.postProcessed) {
try {
applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
}
catch (Throwable ex) {
throw new BeanCreationException(mbd.getResourceDescription(), beanName,
"Post-processing of merged bean definition failed", ex);
}
mbd.postProcessed = true;
}
}
//向容器中缓存单例模式的 Bean 对象，以防循环引用
boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
isSingletonCurrentlyInCreation(beanName));
if (earlySingletonExposure) {
if (logger.isDebugEnabled()) {
logger.debug("Eagerly caching bean '" + beanName +
"' to allow for resolving potential circular references");
}
//这里是一个匿名内部类，为了防止循环引用，尽早持有对象的引用
addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
}
//Bean 对象的初始化，依赖注入在此触发
//这个 exposedObject 在初始化完成之后返回作为依赖注入完成后的 Bean
Object exposedObject = bean;
try {
//将 Bean 实例对象封装，并且 Bean 定义中配置的属性值赋值给实例对象
populateBean(beanName, mbd, instanceWrapper);
//初始化 Bean 对象
exposedObject = initializeBean(beanName, exposedObject, mbd);
}
catch (Throwable ex) {
if (ex instanceof BeanCreationException && beanName.equals(((BeanCreationException) ex).getBeanName())) {
throw (BeanCreationException) ex;
}
else {
throw new BeanCreationException(

mbd.getResourceDescription(), beanName, "Initialization of bean failed", ex);
}
}
if (earlySingletonExposure) {
//获取指定名称的已注册的单例模式 Bean 对象
Object earlySingletonReference = getSingleton(beanName, false);
if (earlySingletonReference != null) {
//根据名称获取的已注册的 Bean 和正在实例化的 Bean 是同一个
if (exposedObject == bean) {
//当前实例化的 Bean 初始化完成
exposedObject = earlySingletonReference;
}
//当前 Bean 依赖其他 Bean，并且当发生循环引用时不允许新创建实例对象
else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
String[] dependentBeans = getDependentBeans(beanName);
Set<String> actualDependentBeans = new LinkedHashSet<>(dependentBeans.length);
//获取当前 Bean 所依赖的其他 Bean
for (String dependentBean : dependentBeans) {
//对依赖 Bean 进行类型检查
if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
actualDependentBeans.add(dependentBean);
}
}
if (!actualDependentBeans.isEmpty()) {
throw new BeanCurrentlyInCreationException(beanName,
"Bean with name '" + beanName + "' has been injected into other beans [" +
StringUtils.collectionToCommaDelimitedString(actualDependentBeans) +
"] in its raw version as part of a circular reference, but has eventually been " +
"wrapped. This means that said other beans do not use the final version of the " +
"bean. This is often the result of over-eager type matching - consider using " +
"'getBeanNamesOfType' with the 'allowEagerInit' flag turned off, for example.");
}
}
}
}
//注册完成依赖注入的 Bean
try {
registerDisposableBeanIfNecessary(beanName, bean, mbd);
}
catch (BeanDefinitionValidationException ex) {
throw new BeanCreationException(
mbd.getResourceDescription(), beanName, "Invalid destruction signature", ex);

}
return exposedObject;
}

```

createBeanInstance：生成 Bean 所包含的 java 对象实例;
populateBean ：对 Bean 属性的依赖注入进行处理;

CGLIB 是一个常用的字节码生成器的类库，它提供了一系列 API 实现 java 字节码的生成和转换功能。
我们在学习 JDK 的动态代理时都知道，JDK 的动态代理只能针对接口，如果一个类没有实现任何接口，
要对其进行动态代理只能使用 CGLIB。

以后再搞这个吧，太复杂了。
