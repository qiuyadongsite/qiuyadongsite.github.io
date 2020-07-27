---
layout: post
title:  spring-cloud-netflix负载均衡器Ribbon实践
date:   2020-07-27 20:53:12 +08:00
category: 源码学习
tags: sprincloud Ribbon
comments: true
---

* content
{:toc}

宁可枝头抱香死，何曾吹落北风中。





### Ribbon简单使用

负载均衡，拿到目标服务器地址列表，根据规则选择一个进行调用；

#### api方式

```java

    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Autowired
    RestTemplate restTemplate;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder){
         return restTemplateBuilder.build();
    }
    @GetMapping("/test/{id}")
    public String test(@PathVariable("id")int id){
     ServiceInstance serviceInstance=loadBalancerClient.choose("xxx-service");
     String url=String.format("http://%s:%s",serviceInstance.getHost(),serviceInstance.getPort()+"/xx");
    return restTemplate.getForObject(url,String.class);
    }

```    

#### 注解方式

```java

    @Autowired
    RestTemplate restTemplate;

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder){
         return restTemplateBuilder.build();
    }

    @GetMapping("/test/{id}")
    public String test(@PathVariable("id")int id){
        return restTemplate.getForObject("http://xxx-service/xx",String.class);
    }

```    

### 调用代码分析

#### RestTemplate调用

```java

     public <T> T getForObject(String url, Class<T> responseType, Object... uriVariables) throws RestClientException {
            RequestCallback requestCallback = this.acceptHeaderRequestCallback(responseType);
            HttpMessageConverterExtractor<T> responseExtractor = new HttpMessageConverterExtractor(responseType, this.getMessageConverters(), this.logger);
            return this.execute(url, HttpMethod.GET, requestCallback, responseExtractor, (Object[])uriVariables);
        }

    protected <T> T doExecute(URI url, @Nullable HttpMethod method, @Nullable RequestCallback requestCallback, @Nullable ResponseExtractor<T> responseExtractor) throws RestClientException {
             //具体的发送请求工具
       		 ClientHttpRequest request = this.createRequest(url, method);

               response = request.execute();
               this.handleResponse(url, method, response);
               var14 = responseExtractor != null ? responseExtractor.extractData(response) : null;

    }

```

#### HttpAccessor生成请求客户端

```java

     protected ClientHttpRequest createRequest(URI url, HttpMethod method) throws IOException {
            ClientHttpRequest request = this.getRequestFactory().createRequest(url, method);
            this.initialize(request);
            return request;
        }

```

#### 具体生成请求工厂

 InterceptingHttpAccessor

```java

    public ClientHttpRequestFactory getRequestFactory() {
           //获取是否restTemplate有拦截器
           //具体在LoadBalancerInterceptorConfig对resteplate进行了设置
            List<ClientHttpRequestInterceptor> interceptors = this.getInterceptors();
            if (!CollectionUtils.isEmpty(interceptors)) {
                ClientHttpRequestFactory factory = this.interceptingRequestFactory;
                if (factory == null) {
                    factory = new InterceptingClientHttpRequestFactory(super.getRequestFactory(), interceptors);
                    this.interceptingRequestFactory = (ClientHttpRequestFactory)factory;
                }

                return (ClientHttpRequestFactory)factory;
            } else {
                return super.getRequestFactory();
            }
        }

```

#### 具体生成的请求客户端

InterceptingClientHttpRequest

```java

    //并进行具体调用
    public ClientHttpResponse execute(HttpRequest request, byte[] body) throws IOException {
    if (this.iterator.hasNext()) {
     ClientHttpRequestInterceptor nextInterceptor = (ClientHttpRequestInterceptor)this.iterator.next();
        //调用的请求拦截器LoadBalancerInterceptor
      return nextInterceptor.intercept(request, body, this);
        }
    }

```

### Ribbon原理

#### 初始化操作

 LoadBalancerClient方式

 ```java

      //初始化之前，LoadBalancerAutoConfiguration进行自动装配
      @AutoConfigureBefore({LoadBalancerAutoConfiguration.class, AsyncLoadBalancerAutoConfiguration.class})
      public class RibbonAutoConfiguration {
           @Bean
          @ConditionalOnMissingBean({LoadBalancerClient.class})
          public LoadBalancerClient loadBalancerClient() {
              return new RibbonLoadBalancerClient(this.springClientFactory());
          }
      }

```      

注解方式

  获取所有的需要拦截的restTemplate

```java

      public class LoadBalancerAutoConfiguration{
          //相当于@Qualifier，注入所有被@LoadBalanced标记的RestTemplate
          @LoadBalanced
      	@Autowired(required = false)
      	private List<RestTemplate> restTemplates = Collections.emptyList();

      }

```

 对包装之后的RestTemplate进行拦截配置

```java

        static class LoadBalancerInterceptorConfig {
                 //定义拦截器
        		@Bean
        		public LoadBalancerInterceptor ribbonInterceptor(
        				LoadBalancerClient loadBalancerClient,
        				LoadBalancerRequestFactory requestFactory) {
        			return new LoadBalancerInterceptor(loadBalancerClient, requestFactory);
        		}
                 //将自己的拦截器添加的拦截器列表中
        		@Bean
        		@ConditionalOnMissingBean
        		public RestTemplateCustomizer restTemplateCustomizer(
        				final LoadBalancerInterceptor loadBalancerInterceptor) {
        			return restTemplate -> {
        				List<ClientHttpRequestInterceptor> list = new ArrayList<>(
        						restTemplate.getInterceptors());
        				list.add(loadBalancerInterceptor);
        				restTemplate.setInterceptors(list);
        			};
        		}

        	}

```

   对restTemplate进行重设

```java

      public SmartInitializingSingleton loadBalancedRestTemplateInitializerDeprecated(
      			final ObjectProvider<List<RestTemplateCustomizer>> restTemplateCustomizers) {
      		return () -> restTemplateCustomizers.ifAvailable(customizers -> {
      			for (RestTemplate restTemplate : LoadBalancerAutoConfiguration.this.restTemplates) {
      				for (RestTemplateCustomizer customizer : customizers) {
      					customizer.customize(restTemplate);
      				}
      			}
      		});
      	}

```

#### 调用操作

拦截器拦截restTemplate

```java

      public class LoadBalancerInterceptor implements ClientHttpRequestInterceptor {
         //具体工具类
          private LoadBalancerClient loadBalancer;
          public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
      			final ClientHttpRequestExecution execution) throws IOException {
      		final URI originalUri = request.getURI();
      		String serviceName = originalUri.getHost();
      		Assert.state(serviceName != null,
      				"Request URI does not contain a valid hostname: " + originalUri);
      		return this.loadBalancer.execute(serviceName,
      				this.requestFactory.createRequest(request, body, execution));
      	}
      }

```

 调用RibbonLoadBalancerClient具体调用

```java

       public <T> T execute(String serviceId, LoadBalancerRequest<T> request, Object hint) throws IOException {
              //获取负载均衡器
              ILoadBalancer loadBalancer = this.getLoadBalancer(serviceId);
              //根据负载均衡器获取server
              Server server = this.getServer(loadBalancer, hint);
              if (server == null) {
                  throw new IllegalStateException("No instances available for " + serviceId);
              } else {
                  RibbonLoadBalancerClient.RibbonServer ribbonServer = new RibbonLoadBalancerClient.RibbonServer(serviceId, server, this.isSecure(server, serviceId), this.serverIntrospector(serviceId).getMetadata(server));
                  return this.execute(serviceId, (ServiceInstance)ribbonServer, (LoadBalancerRequest)request);
              }
          }

```

   具体的LoadBalancer：ZoneAwareLoadBalancer

```java

        //存储服务器列表的父类
        public class BaseLoadBalancer{
            protected volatile List<Server> allServerList;
            protected volatile List<Server> upServerList;
        }
        //初始化服务器列表的子类DynamicServerListLoadBalancer
        public class DynamicServerListLoadBalancer{
            public DynamicServerListLoadBalancer(){
                this.restOfInit(clientConfig);
            }
             void restOfInit(IClientConfig clientConfig) {  
                 //开启定时任务更新
                  this.enableAndInitLearnNewServersFeature();
                 //更新列表
                this.updateListOfServers();
             }
            public void updateListOfServers() {
                servers = this.serverListImpl.getUpdatedListOfServers();
            }
        }

```

   定时更新

```java

          public void enableAndInitLearnNewServersFeature() {
                  this.serverListUpdater.start(this.updateAction);
              }
          //配置一个定时任务每30s获取一次服务列表
          public synchronized void start(final UpdateAction updateAction) {
                  if (this.isActive.compareAndSet(false, true)) {
                      Runnable wrapperRunnable = new Runnable() {
                          public void run() {
                              if (!PollingServerListUpdater.this.isActive.get()) {
                                  if (PollingServerListUpdater.this.scheduledFuture != null) {
                                      PollingServerListUpdater.this.scheduledFuture.cancel(true);
                                  }

                              } else {
                                  try {
                                      updateAction.doUpdate();
                                      PollingServerListUpdater.this.lastUpdated = System.currentTimeMillis();
                                  } catch (Exception var2) {
                                      PollingServerListUpdater.logger.warn("Failed one update cycle", var2);
                                  }

                              }
                          }
                      };
                      this.scheduledFuture = getRefreshExecutor().scheduleWithFixedDelay(wrapperRunnable, this.initialDelayMs, this.refreshIntervalMs, TimeUnit.MILLISECONDS);
                  } else {
                      logger.info("Already active, no-op");
                  }

              }

```

  本地获取

```java

          //本地获取服务列表 在配置中配置的
          //spring-cloud-order-service.ribbon.listOfServers=\localhost:8080,localhost:8082
          public List<Server> getUpdatedListOfServers() {
                  String listOfServers = (String)this.clientConfig.get(CommonClientConfigKey.ListOfServers);
                  return this.derive(listOfServers);
              }

```

   根据选择器选择服务区

```java

        public Server chooseServer(Object key) {
          return this.rule.choose(key);      
        }
```

   选择规则

```

        RoundRobinRule
        RandomRule
        RetryRule
        BestAvailableRule
        ResponseTimeWeightedRule
        ...

```

   默认的轮询算法

```java

        public abstract class AbstractServerPredicate{
            private int incrementAndGetModulo(int modulo) {
                int current;
                int next;
                do {
                    current = this.nextIndex.get();
                    next = (current + 1) % modulo;
                } while(!this.nextIndex.compareAndSet(current, next) || current >= modulo)
                return current;
            }
        }

```
   随机算法

```java

        public class RandomRule extends AbstractLoadBalancerRule {
        	protected int chooseRandomInt(int serverCount) {
                return ThreadLocalRandom.current().nextInt(serverCount);
            }
        }

```

   根据响应时间选择的算法

```java

        public class WeightedResponseTimeRule{
            //收集所有服务器节点的响应时间
            public Server choose(ILoadBalancer lb, Object key) {
                double randomWeight = this.random.nextDouble() * maxTotalWeight;
                  int n = 0;

                  for(Iterator var13 = currentWeights.iterator(); var13.hasNext(); ++n) {
                   Double d = (Double)var13.next();
                    if (d >= randomWeight) {
                        serverIndex = n;
                             break;
                       }
                   }

             server = (Server)allList.get(serverIndex);
            }
        }

```

#### 具体剔除服务节点

 开启定时任务每10s，ping一次

```java

    public class BaseLoadBalancer{
     void setupPingTask() {
         if (!this.canSkipPing()) {
             if (this.lbTimer != null) {
                 this.lbTimer.cancel();
             }

    this.lbTimer = new ShutdownEnabledTimer("NFLoadBalancer-PingTimer-" + this.name, true);
             //
    this.lbTimer.schedule(new BaseLoadBalancer.PingTask(), 0L, (long)(this.pingIntervalSeconds * 1000));
                this.forceQuickPing();
            }
        }
    }

```
 具体的ping执行器

```java

      class PingTask extends TimerTask {
              PingTask() {
              }

              public void run() {
                  try {
                      (BaseLoadBalancer.this.new Pinger(BaseLoadBalancer.this.pingStrategy)).runPinger();
                  } catch (Exception var2) {
                      BaseLoadBalancer.logger.error("LoadBalancer [{}]: Error pinging", BaseLoadBalancer.this.name, var2);
                  }

              }
          }
```

  - 根据不同的策略选择不同的Pinger
    - 默认情况是dumyping，就是不做ping操作
    - PingUrl根据http进行心跳检查

#### 自定义扩展

- 负载器ILoadBalancer
- 选择器IRule
- 访问心跳IPing
- 服务列表ServerList
- 服务列表过滤器ServerListFilter
