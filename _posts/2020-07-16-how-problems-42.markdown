---
layout: post
title:  SpringBoot的environment分析
date:   2020-07-16 20:53:12 +08:00
category: 源码学习
tags: springboot
comments: true
---

* content
{:toc}

思往事，惜流芳。易成伤。拟歌先敛，欲笑还颦，最断人肠。





## SpringBoot的environment分析

Environment就是环境，是springboot运行的环境信息，简单理解为获取系统配置信息的对象；

那么这些配置信息可以从本地加载也可以从远程获取；

这里分析SpringBoot启动时加载配置设置环境信息的流程；

### 启动流程

- 启动

```java

    //启动时可以配置命令行参数，包装成ApplicationArguments
    ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
    //具体初始化环境
    ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);

```

- 配置环境信息

```java

    //根据系统类别选择创建的环境
    ConfigurableEnvironment environment = getOrCreateEnvironment();
    //将命令行参数配置到环境里
    configureEnvironment(environment, applicationArguments.getSourceArgs());
    //附加配置configurationProperties
    ConfigurationPropertySources.attach(environment);
    //真正的加载和解析配置添加到环境对象，具体事件机制实现
    listeners.environmentPrepared(environment);
    bindToSpringApplication(environment);
    if (!this.isCustomEnvironment) {
    environment = new EnvironmentConverter(getClassLoader()).convertEnvironmentIfNecessary(environment,
    					deduceEnvironmentClass());
    		}
    ConfigurationPropertySources.attach(environment);

```

- 创建上下文环境

```java

      private ConfigurableEnvironment getOrCreateEnvironment() {
      		if (this.environment != null) {
      			return this.environment;
      		}
      		switch (this.webApplicationType) {
      		case SERVLET:
      			return new StandardServletEnvironment();
      		case REACTIVE:
      			return new StandardReactiveWebEnvironment();
      		default:
      			return new StandardEnvironment();
      		}
      	}

```        

### 事件加载

- 启动时注册了监听器

```

      org.springframework.boot.SpringApplicationRunListener=\
      org.springframework.boot.context.event.EventPublishingRunListener

```

- 监听器触发

```java

      //监听者发布环境准备事件listeners.environmentPrepared(environment);
      //类EventPublishingRunListener中发布
      public void environmentPrepared(ConfigurableEnvironment environment) {
      this.initialMulticaster
      				.multicastEvent(new ApplicationEnvironmentPreparedEvent(this.application, this.args, environment));
      	}

```

- 之前配置启动的监听器

```

      # Application Listeners
      org.springframework.context.ApplicationListener=\
      org.springframework.boot.context.config.ConfigFileApplicationListener
      ...

```

- 具体触发器

```java

      public class ConfigFileApplicationListener implements EnvironmentPostProcessor, SmartApplicationListener, Ordered {

          //触发应用事件
          @Override
      	public void onApplicationEvent(ApplicationEvent event) {
      		if (event instanceof ApplicationEnvironmentPreparedEvent) {
      			onApplicationEnvironmentPreparedEvent((ApplicationEnvironmentPreparedEvent) event);
      		}
      		if (event instanceof ApplicationPreparedEvent) {
      			onApplicationPreparedEvent(event);
      		}
      	}

          //环境配置的处理
      private void onApplicationEnvironmentPreparedEvent(ApplicationEnvironmentPreparedEvent event) {
      		List<EnvironmentPostProcessor> postProcessors = loadPostProcessors();
              //自己也是环境处理器
      		postProcessors.add(this);
      		AnnotationAwareOrderComparator.sort(postProcessors);
      		for (EnvironmentPostProcessor postProcessor : postProcessors) {
      			postProcessor.postProcessEnvironment(event.getEnvironment(), event.getSpringApplication());
      		}
      	}

      }

```

- 环境处理器

```
      org.springframework.boot.env.EnvironmentPostProcessor=\
      org.springframework.boot.cloud.CloudFoundryVcapEnvironmentPostProcessor,\
      org.springframework.boot.env.SpringApplicationJsonEnvironmentPostProcessor,\
      //systemEnvironment处理器
      org.springframework.boot.env.SystemEnvironmentPropertySourceEnvironmentPostProcessor,\
      org.springframework.boot.reactor.DebugAgentEnvironmentPostProcessor

```

- ConfigFileApplicationListener的环境处理器

```java

      protected void addPropertySources(ConfigurableEnvironment environment, ResourceLoader resourceLoader) {
              //添加随机值配置功能，${random.long}
      		RandomValuePropertySource.addToEnvironment(environment);
      		new Loader(environment, resourceLoader).load();
      }

      void load() {
                    //具体加载配置文件
      			FilteredPropertySource.apply(this.environment, DEFAULT_PROPERTIES, LOAD_FILTERED_PROPERTY,
      					(defaultProperties) -> {
      						this.profiles = new LinkedList<>();
      						this.processedProfiles = new LinkedList<>();
      						this.activatedProfiles = false;
      						this.loaded = new LinkedHashMap<>();
                                //查找配置的profiles
      						initializeProfiles();
                                //支持配置的多个profiles
      						while (!this.profiles.isEmpty()) {
      							Profile profile = this.profiles.poll();
      							if (isDefaultProfile(profile)) {
      								addProfileToEnvironment(profile.getName());
      							}
                                    //具体查询具体配置的文件

      							load(profile, this::getPositiveProfileFilter,
      									addToLoaded(MutablePropertySources::addLast, false));
      							this.processedProfiles.add(profile);
      						}

      						load(null, this::getNegativeProfileFilter, addToLoaded(MutablePropertySources::addFirst, true));
                                 //创建具体的MutablePropertySources，并放入                                                    environment.getPropertySources中
      						addLoadedPropertySources();
                              //激活spring.profiles.include，spring.profiles.active
      						applyActiveProfiles(defaultProperties);
      					});
      		}

```

- load过程

```java

      private void load(String location, String name, Profile profile, DocumentFilterFactory filterFactory,DocumentConsumer consumer) {

      			Set<String> processed = new HashSet<>();
                   //遍历所有propertySourceLoaders，根据具体需要选择进行加载
      			for (PropertySourceLoader loader : this.propertySourceLoaders) {
      				for (String fileExtension : loader.getFileExtensions()) {
      					if (processed.add(fileExtension)) {
      						loadForFileExtension(loader, location + name, "." + fileExtension, profile, filterFactory,consumer);
      					}
      				}
      			}
      		}

```

### 属性资源加载器

- springFactorys中配置

```

      org.springframework.boot.env.PropertySourceLoader=\
      org.springframework.boot.env.PropertiesPropertySourceLoader,\
      org.springframework.boot.env.YamlPropertySourceLoader

```

-  介绍一种Properties的资源加载器

```java

      public class PropertiesPropertySourceLoader implements PropertySourceLoader {
      	private static final String XML_FILE_EXTENSION = ".xml";
          //支持的文件类型
      	@Override
      	public String[] getFileExtensions() {
      		return new String[] { "properties", "xml" };
      	}
         //如果是该资源，创建propertyResources列表
      	@Override
      	public List<PropertySource<?>> load(String name, Resource resource) throws IOException {
      		Map<String, ?> properties = loadProperties(resource);
      		if (properties.isEmpty()) {
      			return Collections.emptyList();
      		}
      		return Collections
      				.singletonList(new OriginTrackedMapPropertySource(name, Collections.unmodifiableMap(properties), true));
      	}
         //将上述符合条件的Resources返回key-value值
      	@SuppressWarnings({ "unchecked", "rawtypes" })
      	private Map<String, ?> loadProperties(Resource resource) throws IOException {
      		String filename = resource.getFilename();
      		if (filename != null && filename.endsWith(XML_FILE_EXTENSION)) {
      			return (Map) PropertiesLoaderUtils.loadProperties(resource);
      		}
      		return new OriginTrackedPropertiesLoader(resource).load();
      	}

      }

```

### 加载顺序

- servlet环境首先加载标准参数

```java

      protected void customizePropertySources(MutablePropertySources propertySources) {
      		propertySources.addLast(new StubPropertySource(SERVLET_CONFIG_PROPERTY_SOURCE_NAME));
      		propertySources.addLast(new StubPropertySource(SERVLET_CONTEXT_PROPERTY_SOURCE_NAME));
      		if (JndiLocatorDelegate.isDefaultJndiEnvironmentAvailable()) {
      			propertySources.addLast(new JndiPropertySource(JNDI_PROPERTY_SOURCE_NAME));
      		}
      		super.customizePropertySources(propertySources);
      	}
      //1:servletConfigInitParams
      //2:servletContextInitParams
      //3:jndiProperties

```

- 调用父类加载

```java

        protected void customizePropertySources(MutablePropertySources propertySources) {
              propertySources.addLast(new PropertiesPropertySource("systemProperties", this.getSystemProperties()));
              propertySources.addLast(new SystemEnvironmentPropertySource("systemEnvironment", this.getSystemEnvironment()));
          }
      //4:systemProperties
      //5:systemEnvironment

```

- 加载ConfigFile中的Random

```java

      protected void addPropertySources(ConfigurableEnvironment environment, ResourceLoader resourceLoader) {
      		RandomValuePropertySource.addToEnvironment(environment);
      		new Loader(environment, resourceLoader).load();
      	}
      //6:RandomValueProperty

```

- 加载命令行中

```java

      configureEnvironment(environment, applicationArguments.getSourceArgs());
      //7:加入命令行参数

```

- 加载配置文件

```java

      //8:加载配置了profile中的配置
      //9:加载默认的配置
      while (!this.profiles.isEmpty()) {
      Profile profile = this.profiles.poll();
      	if (isDefaultProfile(profile)) {
      		addProfileToEnvironment(profile.getName());
      	}
      	load(profile, this::getPositiveProfileFilter,addToLoaded(MutablePropertySources::addLast, false));
      		this.processedProfiles.add(profile);
      }
      load(null, this::getNegativeProfileFilter, addToLoaded(MutablePropertySources::addFirst, true));
      addLoadedPropertySources();
      applyActiveProfiles(defaultProperties);

```

### 使用

- 具体注入使用

```java

       @Value("${java.version}")
       private String javaVersion;

```

- 使用环境注入

```java

          @Autowired
          Environment environment;

          @GetMapping("/javaVersion")
          public String javaVersion(){
              return environment.getProperty("java.version");
          }

```
