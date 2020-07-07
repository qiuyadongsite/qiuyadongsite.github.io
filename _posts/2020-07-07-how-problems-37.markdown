---
layout: post
title:  Nacos源码分析
date:   2020-07-07 20:53:12 +08:00
category: 源码学习
tags: 注册中心 nacos
comments: true
---

* content
{:toc}




Nacos源码分析

启动客户端（SDK方式调用）

1、实例化ConfigService类

    //使用工厂模式
    ConfigService configService=NacosFactory.createConfigService(properties);
    //具体硬编码，使用反射实例化
    Class<?> driverImplClass = Class.forName("com.alibaba.nacos.client.config.NacosConfigService");
    Constructor constructor = driverImplClass.getConstructor(Properties.class);
    ConfigService vendorImpl = (ConfigService) constructor.newInstance(properties);

2、初始化ConfigService

     //使用装饰器模式，添加性能统计功能，包装一个httpAgent
     agent = new MetricsHttpAgent(new ServerHttpAgent(properties));
     //初始化时，没有操作
     agent.start();
     //创建一个ClientWorker，并做进一步任务启动
     worker = new ClientWorker(agent, configFilterChainManager, properties);

3、初始化ClientWorker，并开始检查配置信息

    //初始化一个由一个核心线程数的定时任务，并重新命名线程名字， 目的是客户端的work，并设置后台执行
    executor = Executors.newScheduledThreadPool(1, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName("com.alibaba.nacos.client.Worker." + agent.getName());
                    t.setDaemon(true);
                    return t;
                }
            });
    //初始化一个由cpu核心数的核心线程定时任务线程池，并重新名,目的是客户端的长轮询线程，并后台执行
    executorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName("com.alibaba.nacos.client.Worker.longPolling." + agent.getName());
                    t.setDaemon(true);
                    return t;
                }
            });
    //客户端work，推迟1毫秒执行，10毫秒一个间隔检查配置信息
    executor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    try {
                        checkConfigInfo();
                    } catch (Throwable e) {
                        LOGGER.error("[" + agent.getName() + "] [sub-check] rotate check error", e);
                    }
                }
            }, 1L, 10L, TimeUnit.MILLISECONDS);

4、检查配置信息

    //如果配置3000个，每3000个一个分片，提交到长轮询线程池进行，检查更新
    executorService.execute(new LongPollingRunnable(i));

    //将缓存中的数据取出，如果是此任务的缓存，添加到map集合中，并把map中的缓存信息检查本地更新
     if (cacheData.getTaskId() == taskId) {
       cacheDatas.add(cacheData);
       checkLocalConfig(cacheData);
      if (cacheData.isUseLocalConfigInfo()) {
        cacheData.checkListenerMd5();
       }
     }                      
    //
    checkLocalConfig(cacheData);

5、检查本地更新，创建本地文件

     private void checkLocalConfig(CacheData cacheData) {
         //持久化的配置文件{user.home}/nacos/config/group/dataId
            final String dataId = cacheData.dataId;
            final String group = cacheData.group;
            final String tenant = cacheData.tenant;
            //根据配置的信息创建本地的配置问题
            File path = LocalConfigInfoProcessor.getFailoverFile(agent.getName(), dataId, group, tenant);
            // 默认不使用本地配置，如果本地配置文件，并设置
            if (!cacheData.isUseLocalConfigInfo() && path.exists()) {
                String content = LocalConfigInfoProcessor.getFailover(agent.getName(), dataId, group, tenant);
                String md5 = MD5.getInstance().getMD5String(content);
                cacheData.setUseLocalConfigInfo(true);
                cacheData.setLocalConfigInfoVersion(path.lastModified());
                cacheData.setContent(content);

                LOGGER.warn("[{}] [failover-change] failover file created. dataId={}, group={}, tenant={}, md5={}, content={}",
                    agent.getName(), dataId, group, tenant, md5, ContentUtils.truncateContent(content));
                return;
            }

            // 有 -> 没有。不通知业务监听器，从server拿到配置后通知。
            if (cacheData.isUseLocalConfigInfo() && !path.exists()) {
                cacheData.setUseLocalConfigInfo(false);
                LOGGER.warn("[{}] [failover-change] failover file deleted. dataId={}, group={}, tenant={}", agent.getName(),
                    dataId, group, tenant);
                return;
            }

            // 有变更
            if (cacheData.isUseLocalConfigInfo() && path.exists()
                && cacheData.getLocalConfigInfoVersion() != path.lastModified()) {
                String content = LocalConfigInfoProcessor.getFailover(agent.getName(), dataId, group, tenant);
                String md5 = MD5.getInstance().getMD5String(content);
                cacheData.setUseLocalConfigInfo(true);
                cacheData.setLocalConfigInfoVersion(path.lastModified());
                cacheData.setContent(content);
                LOGGER.warn("[{}] [failover-change] failover file changed. dataId={}, group={}, tenant={}, md5={}, content={}",
                    agent.getName(), dataId, group, tenant, md5, ContentUtils.truncateContent(content));
            }
        }

客户端取参数

1、优先使用本地配置的值

     // 优先使用本地配置
    String content = LocalConfigInfoProcessor.getFailover(agent.getName(), dataId, group, tenant);

2、本地没有获取服务器值

    String result = agent.httpGet(Constants.CONFIG_CONTROLLER_PATH, null, params, agent.getEncode(), readTimeout);

    //获取到则保存到本地并返回
    switch (result.code) {
                case HttpURLConnection.HTTP_OK:
                    LocalConfigInfoProcessor.saveSnapshot(agent.getName(), dataId, group, tenant, result.content);
                    return result.content;
    //服务器没有返回null        
                case HttpURLConnection.HTTP_NOT_FOUND:
                    LocalConfigInfoProcessor.saveSnapshot(agent.getName(), dataId, group, tenant, null);
                    return null;
    }

3、获取出现异常

    //获取本地缓存文件内容。NULL表示没有本地文件或抛出异常。
     content = LocalConfigInfoProcessor.getSnapshot(agent.getName(), dataId, group, tenant);

启动服务器

1、启动了web服务器，提供获取配置接口

    //提供接口/v1/cs/configs
    public Class ConfigController{
        //第一次客户端没有配置时调用
        @GetMapping
        public void getConfig(){
                  ParamUtils.checkTenant(tenant);
    	    tenant = processTenant(tenant);
    		// check params
    		ParamUtils.checkParam(dataId, group, "datumId", "content");
    		ParamUtils.checkParam(tag);
    		final String clientIp = RequestUtil.getRemoteIp(request);
    		inner.doGetConfig(request, response, dataId, group, tenant, tag, clientIp);
        }
    }

2、提供监听接口

    public Class ConfigController{
    @PostMapping("/listener")
        public void listener(）{
            request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
    		String probeModify = request.getParameter("Listening-Configs");
    		if (StringUtils.isBlank(probeModify)) {
    			throw new IllegalArgumentException("invalid probeModify");
    		}
    		probeModify = URLDecoder.decode(probeModify, Constants.ENCODE);
    		Map<String, String> clientMd5Map;
    		try {
    			clientMd5Map = MD5Util.getClientMd5Map(probeModify);
    		}
    		catch (Throwable e) {
    			throw new IllegalArgumentException("invalid probeModify");
    		}
    		// do long-polling
    		inner.doPollingConfig(request, response, clientMd5Map, probeModify.length());
        }
    }

3、没有改变时持有客户端长轮询

      // 长轮询
      if (LongPollingService.isSupportLongPolling(request)) {
      longPollingService.addLongPollingClient(request, response, clientMd5Map, probeRequestSize);
                return HttpServletResponse.SC_OK + "";
     }
    //创建了一个定时任务
    ScheduledExecutorService asyncTimeoutFuture = scheduler.schedule(new Runnable() {
        if (changedGroups.size() > 0) {
            //改变发送改变的配置文件
              sendResponse(changedGroups);
       } else {
            //没改变发送null
              sendResponse(null);
      }

    }, timeoutTime, TimeUnit.MILLISECONDS);


    //并且支持短轮询，并且禁止缓存
    //比较md5返回
     List<String> changedGroups = MD5Util.compareMd5(request, response, clientMd5Map);
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);
    response.setHeader("Cache-Control", "no-cache,no-store");
    response.setStatus(HttpServletResponse.SC_OK);

4、对服务器配置的管理

    public class ConfigCacheService{
        //注入了持久化的接口
        @Autowired
        private static PersistService persistService;
        //提供缓存容器
        static private final ConcurrentHashMap<String, CacheItem> CACHE =
            new ConcurrentHashMap<String, CacheItem>();
         /**
         * 保存配置文件，并缓存md5.
         */
        static public boolean dump(String dataId, String group, String tenant, String content, long lastModifiedTs, String type)；
    }

5、服务器缓存对象（md5和最后修改时间）

        public class CacheItem {
        public volatile boolean isBeta = false;//测试
        public volatile String md54Beta = Constants.NULL;//md5为了测试
        public volatile List<String> ips4Beta;//ips为了测试
        public volatile long lastModifiedTs4Beta;//lastModifiedTs为了测试
        public volatile Map<String, String> tagMd5;
        public volatile Map<String, Long> tagLastModifiedTs;
        public SimpleReadWriteLock rwLock = new SimpleReadWriteLock();
        public String type;
        }

6、服务器持久化规则

    //嵌入式的持久化工具
    EmbeddedStoragePersistServiceImpl{  
    }
    //外部的持久化工具，提供了jdbcTempe
    ExternalStoragePersistServiceImpl{
    }
    //提供了统一的持久化接口，方便更新和查询，reload
