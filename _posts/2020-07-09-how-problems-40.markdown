---
layout: post
title:  springboot启动分析
date:   2020-07-09 20:53:12 +08:00
category: 源码学习
tags: springboot
comments: true
---

* content
{:toc}

万里因循成久客，一年容易又秋风。





1、SpringApplication初始化

    public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
             //设置资源类
    		this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
             //推测web容器类
    		this.webApplicationType = WebApplicationType.deduceFromClasspath();
             //设置SpringFactories中的ApplicationContextInitializer
    		setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
             //设置SpringFactories中的ApplicationListener
    		setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
             //设置启动类
    		this.mainApplicationClass = deduceMainApplicationClass();
    	}

- 推测web容器类

    static WebApplicationType deduceFromClasspath() {
        //存在reactive.DispatcherHandler类且没有servlet.ServletContainer类，就是REACTIVE
    		if (ClassUtils.isPresent(WEBFLUX_INDICATOR_CLASS, null) && !ClassUtils.isPresent(WEBMVC_INDICATOR_CLASS, null)
    				&& !ClassUtils.isPresent(JERSEY_INDICATOR_CLASS, null)) {
    			return WebApplicationType.REACTIVE;
    		}
        //存在Servlet且ConfigurableWebApplicationContext则是SERVLET，否则就是none
    		for (String className : SERVLET_INDICATOR_CLASSES) {
    			if (!ClassUtils.isPresent(className, null)) {
    				return WebApplicationType.NONE;
    			}
    		}
    		return WebApplicationType.SERVLET;
    	}

- 设置SpringFactories中的ApplicationContextInitializer和ApplicationListener

    # Application Context Initializers
    org.springframework.context.ApplicationContextInitializer=\
    org.springframework.boot.context.ConfigurationWarningsApplicationContextInitializer,\
    org.springframework.boot.context.ContextIdApplicationContextInitializer,\
    org.springframework.boot.context.config.DelegatingApplicationContextInitializer,\
    org.springframework.boot.rsocket.context.RSocketPortInfoApplicationContextInitializer,\
    org.springframework.boot.web.context.ServerPortInfoApplicationContextInitializer

    # Application Listeners
    org.springframework.context.ApplicationListener=\
    org.springframework.boot.ClearCachesApplicationListener,\
    org.springframework.boot.builder.ParentContextCloserApplicationListener,\
    org.springframework.boot.cloud.CloudFoundryVcapEnvironmentPostProcessor,\
    org.springframework.boot.context.FileEncodingApplicationListener,\
    org.springframework.boot.context.config.AnsiOutputApplicationListener,\
    org.springframework.boot.context.config.ConfigFileApplicationListener,\
    org.springframework.boot.context.config.DelegatingApplicationListener,\
    org.springframework.boot.context.logging.ClasspathLoggingApplicationListener,\
    org.springframework.boot.context.logging.LoggingApplicationListener,\
    org.springframework.boot.liquibase.LiquibaseServiceLocatorApplicationListener

- 介绍一个ConfigurationWarningsApplicationContextInitializer

    //用来报告Spring容器的一些常见的错误配置
    public class ConfigurationWarningsApplicationContextInitializer
    		implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        //构建一个ConfigurationWarningsPostProcessor注册到context实例中
    public void initialize(ConfigurableApplicationContext context) {
    		context.addBeanFactoryPostProcessor(new ConfigurationWarningsPostProcessor(getChecks()));
    	}
        //检查的check
        protected Check[] getChecks() {
    		return new Check[] { new ComponentScanPackageCheck() };
            //这里初始化一个ComponentScanPackage的检查
    	}    
        protected static final class ConfigurationWarningsPostProcessor
    			implements PriorityOrdered, BeanDefinitionRegistryPostProcessor {
            void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry){
                //允许在Spring容器会自动检测容器的bean definition之前，进一步的注册bean definiton到容器中。特定情况下还可以通过进一步的注册bean definiton而反过来定义BeanFactoryPostProcessor接口的实例
                for (Check check : this.checks) {
    				String message = check.getWarning(registry);
    				if (StringUtils.hasLength(message)) {
    					warn(message);
    				}
    			}
            }
            void postProcessBeanFactory(ConfigurableListableBeanFactory var1){
                //允许对spring容器的bean definition进行自定义的修改，可改变容器底层管理的bean的属性值
                //Spring容器会自动检测容器的实现了BeanFactoryPostProcessor接口的Bean,如果有话将会在创建其他Bean之前首先执行该接口的代码。
                //此处为空
                ;
            }

        }
    }

- 介绍一个ClearCachesApplicationListener

    //使用观察者模式，当上下文加载完成时通知触发该监听器
    class ClearCachesApplicationListener implements ApplicationListener<ContextRefreshedEvent> {
        //当上下文加载完成做一些清理工作
    }

2、调用run执行

    public ConfigurableApplicationContext run(String... args) {
             //创建一个跑表
    		StopWatch stopWatch = new StopWatch();
             //启动跑表
    		stopWatch.start();

    		ConfigurableApplicationContext context = null;
    		Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
            //配置Headless模式System.setProperty("java.awt.headless", "true");
    		configureHeadlessProperty();
            //获取SpringApplicationRunListener
    		SpringApplicationRunListeners listeners = getRunListeners(args);
             //# Run Listeners
             //org.springframework.boot.SpringApplicationRunListener=\
             //org.springframework.boot.context.event.EventPublishingRunListener

             //启动一个任务监听器
    		listeners.starting();
    		try {
                //获取默认参数和配置参数
    			ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
    			//提前准备配置环境
                ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);
    			configureIgnoreBeanInfo(environment);
                //根据配置的参数打印banner
    			Banner printedBanner = printBanner(environment);
                //servlet容器创建AnnotationConfigServletWebServerApplicationContext
    			context = createApplicationContext();
                //在SpringFactorie实例化SpringBootExceptionReporter的导出上下文异常信息
    			exceptionReporters = getSpringFactoriesInstances(SpringBootExceptionReporter.class,
    					new Class[] { ConfigurableApplicationContext.class }, context);
                //上下文前置准备
    			prepareContext(context, environment, listeners, applicationArguments, printedBanner);
                //实现上下文容器
    			refreshContext(context);
                //上下文后置处理
    			afterRefresh(context, applicationArguments);
    			stopWatch.stop();
    			if (this.logStartupInfo) {
    				new StartupInfoLogger(this.mainApplicationClass).logStarted(getApplicationLog(), stopWatch);
    			}
    			listeners.started(context);
    			callRunners(context, applicationArguments);
    		}
    		catch (Throwable ex) {
    			handleRunFailure(context, ex, exceptionReporters, listeners);
    			throw new IllegalStateException(ex);
    		}

    		try {
    			listeners.running(context);
    		}
    		catch (Throwable ex) {
    			handleRunFailure(context, ex, exceptionReporters, null);
    			throw new IllegalStateException(ex);
    		}
    		return context;
    	}
