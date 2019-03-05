---
layout: post
title:  分布式协调之zookeeper源码一
date:   2019-03-05 21:52:12 +08:00
category: 高并发分布式
tags: zookeeper
comments: true
---

* content
{:toc}

Leader 选举源码分析跟踪：有了理论基础以后，我们先带大家读一下源码，看看他的实现逻辑。












## 源码分析

入口类QuorumPeerMain(QuorumPeer:仲裁人的意思)，一般中间件都是以Main结束的Class是主类：

```java

//QuorumPeerMain.class

public static void main(String[] args) {
       QuorumPeerMain main = new QuorumPeerMain();       
        main.initializeAndRun(args);       
}

//运行args传进来的参数
protected void initializeAndRun(String[] args)
        throws ConfigException, IOException, AdminServerException
    {
        QuorumPeerConfig config = new QuorumPeerConfig();
        if (args.length == 1) {
            config.parse(args[0]);
        }

        // Start and schedule the the purge task
        DatadirCleanupManager purgeMgr = new DatadirCleanupManager(config
                .getDataDir(), config.getDataLogDir(), config
                .getSnapRetainCount(), config.getPurgeInterval());
        purgeMgr.start();
        //判断是standalone模式还是集群模式
        if (args.length == 1 && config.isDistributed()) {
            runFromConfig(config);
        } else {
            LOG.warn("Either no config or no quorum defined in config, running "
                    + " in standalone mode");
            // there is only server in the quorum -- run as standalone
            ZooKeeperServerMain.main(args);
        }
    }

//根据模式来运行那个

    public void runFromConfig(QuorumPeerConfig config)
            throws IOException, AdminServerException
    {
      try {
          ManagedUtil.registerLog4jMBeans();
      } catch (JMException e) {
          LOG.warn("Unable to register log4j JMX control", e);
      }

      LOG.info("Starting quorum peer");
      try {
          ServerCnxnFactory cnxnFactory = null;
          ServerCnxnFactory secureCnxnFactory = null;
          //为客户端提供读写的server， 也就是2181这个端口的访问功能
          if (config.getClientPortAddress() != null) {
              cnxnFactory = ServerCnxnFactory.createFactory();
              cnxnFactory.configure(config.getClientPortAddress(),
                      config.getMaxClientCnxns(),
                      false);
          }

          if (config.getSecureClientPortAddress() != null) {
              secureCnxnFactory = ServerCnxnFactory.createFactory();
              secureCnxnFactory.configure(config.getSecureClientPortAddress(),
                      config.getMaxClientCnxns(),
                      true);
          }
          //ZK的逻辑主线程，负责选举、投票
          quorumPeer = getQuorumPeer();
          quorumPeer.setTxnFactory(new FileTxnSnapLog(
                      config.getDataLogDir(),
                      config.getDataDir()));
          quorumPeer.enableLocalSessions(config.areLocalSessionsEnabled());
          quorumPeer.enableLocalSessionsUpgrading(
              config.isLocalSessionsUpgradingEnabled());
          //quorumPeer.setQuorumPeers(config.getAllMembers());
          quorumPeer.setElectionType(config.getElectionAlg());
          quorumPeer.setMyid(config.getServerId());
          quorumPeer.setTickTime(config.getTickTime());
          quorumPeer.setMinSessionTimeout(config.getMinSessionTimeout());
          quorumPeer.setMaxSessionTimeout(config.getMaxSessionTimeout());
          quorumPeer.setInitLimit(config.getInitLimit());
          quorumPeer.setSyncLimit(config.getSyncLimit());
          quorumPeer.setConfigFileName(config.getConfigFilename());
          quorumPeer.setZKDatabase(new ZKDatabase(quorumPeer.getTxnFactory()));
          quorumPeer.setQuorumVerifier(config.getQuorumVerifier(), false);
          if (config.getLastSeenQuorumVerifier()!=null) {
              quorumPeer.setLastSeenQuorumVerifier(config.getLastSeenQuorumVerifier(), false);
          }
          quorumPeer.initConfigInZKDatabase();
          quorumPeer.setCnxnFactory(cnxnFactory);
          quorumPeer.setSecureCnxnFactory(secureCnxnFactory);
          quorumPeer.setLearnerType(config.getPeerType());
          quorumPeer.setSyncEnabled(config.getSyncEnabled());
          quorumPeer.setQuorumListenOnAllIPs(config.getQuorumListenOnAllIPs());
          //启动主线程
          quorumPeer.start();
          quorumPeer.join();
      } catch (InterruptedException e) {
          // warn, but generally this is ok
          LOG.warn("Quorum Peer interrupted", e);
      }
    }

```

运行quorumPeer.start,这个是发布的主线程

```java

public class QuorumPeer extends ZooKeeperThread implements QuorumStats.Provider {
  public synchronized void start() {
          if (!getView().containsKey(myid)) {
              throw new RuntimeException("My id " + myid + " not in the peer list");
           }
          loadDataBase();  //恢复DB
          startServerCnxnFactory();
          try {
              adminServer.start();
          } catch (AdminServerException e) {
              LOG.warn("Problem starting AdminServer", e);
              System.out.println(e);
          }
          startLeaderElection();//选举初始化
          super.start();
      }



//恢复DB

private void loadDataBase() {
        try {
            zkDb.loadDataBase(); //从本地文件恢复db

            // load the epochs
            //从最新的zxid恢复epoch变量、zxid64位，前32位是epoch的值，后32位是zxid
            long lastProcessedZxid = zkDb.getDataTree().lastProcessedZxid;
            long epochOfZxid = ZxidUtils.getEpochFromZxid(lastProcessedZxid);
            try {
                //从文件中读取当前的epoch
                currentEpoch = readLongFromFile(CURRENT_EPOCH_FILENAME);
            } catch(FileNotFoundException e) {
            	// pick a reasonable epoch number
            	// this should only happen once when moving to a
            	// new code version
            	currentEpoch = epochOfZxid;
            	LOG.info(CURRENT_EPOCH_FILENAME
            	        + " not found! Creating with a reasonable default of {}. This should only happen when you are upgrading your installation",
            	        currentEpoch);
            	writeLongToFile(CURRENT_EPOCH_FILENAME, currentEpoch);
            }
            if (epochOfZxid > currentEpoch) {
                throw new IOException("The current epoch, " + ZxidUtils.zxidToString(currentEpoch) + ", is older than the last zxid, " + lastProcessedZxid);
            }
            try {
                //从文件中读取接收的epoch
                acceptedEpoch = readLongFromFile(ACCEPTED_EPOCH_FILENAME);
            } catch(FileNotFoundException e) {
            	// pick a reasonable epoch number
            	// this should only happen once when moving to a
            	// new code version
            	acceptedEpoch = epochOfZxid;
            	LOG.info(ACCEPTED_EPOCH_FILENAME
            	        + " not found! Creating with a reasonable default of {}. This should only happen when you are upgrading your installation",
            	        acceptedEpoch);
            	writeLongToFile(ACCEPTED_EPOCH_FILENAME, acceptedEpoch);
            }
            if (acceptedEpoch < currentEpoch) {
                throw new IOException("The accepted epoch, " + ZxidUtils.zxidToString(acceptedEpoch) + " is less than the current epoch, " + ZxidUtils.zxidToString(currentEpoch));
            }
        } catch(IOException ie) {
            LOG.error("Unable to load database on disk", ie);
            throw new RuntimeException("Unable to run quorum server ", ie);
        }
    }

//开始选举
    synchronized public void startLeaderElection() {
            try {
                //如果当前节点的状态是LOOKING，则投票给自己
                if (getPeerState() == ServerState.LOOKING) {
                    currentVote = new Vote(myid, getLastLoggedZxid(), getCurrentEpoch());
                }
            } catch(IOException e) {
                RuntimeException re = new RuntimeException(e.getMessage());
                re.setStackTrace(e.getStackTrace());
                throw re;
            }
            //根据配置获取选举选法
            this.electionAlg = createElectionAlgorithm(electionType);
        }

//创建选举对象
        protected Election createElectionAlgorithm(int electionAlgorithm){
                Election le=null;

                //TODO: use a factory rather than a switch
                switch (electionAlgorithm) {
                    case 1:
                        le = new AuthFastLeaderElection(this);
                        break;
                    case 2:
                        le = new AuthFastLeaderElection(this, true);
                        break;
                    case 3:
                        //Leader选举IO负责类
                        qcm = new QuorumCnxManager(this);
                        QuorumCnxManager.Listener listener = qcm.listener;
                        if(listener != null){
                            listener.start(); //启动已绑定端口的选举线程，等待集群中其他机器连接
                            //基于TCP的选举算法
                            FastLeaderElection fle = new FastLeaderElection(this, qcm);
                            fle.start();
                            le = fle;
                        } else {
                            LOG.error("Null listener when initializing cnx manager");
                        }
                        break;
                    default:
                        assert false;
                }
                return le;
            }




}

```

这里使用FastLeaderElection的算法选举

```java


public FastLeaderElection(QuorumPeer self, QuorumCnxManager manager){
        this.stop = false;
        this.manager = manager;
        starter(self, manager);
    }

    private void starter(QuorumPeer self, QuorumCnxManager manager) {
            this.self = self;
            proposedLeader = -1;
            proposedZxid = -1;
            //业务层发送队列，业务对象ToSend
            sendqueue = new LinkedBlockingQueue<ToSend>();
            //业务层接收队列，业务对象Notification
            recvqueue = new LinkedBlockingQueue<Notification>();
            this.messenger = new Messenger(manager);
        }

Messenger(QuorumCnxManager manager) {

            this.ws = new WorkerSender(manager);

            this.wsThread = new Thread(this.ws,
                    "WorkerSender[myid=" + self.getId() + "]");
            this.wsThread.setDaemon(true);

            this.wr = new WorkerReceiver(manager);

            this.wrThread = new Thread(this.wr,
                    "WorkerReceiver[myid=" + self.getId() + "]");
            this.wrThread.setDaemon(true);
}


public void start() {
        this.messenger.start();
}

//调用内部类的run
//wsThread 和 wrThread 的 初 始 化 动 作 在FastLeaderElection 的 starter 方法里面进行，这里面有两个内部类，一个是 WorkerSender，一个是 WorkerReceiver，负责发送投票信息和接收投票信息
void start(){
            this.wsThread.start();//启动业务层发送线程，将消息发送给IO负责类QuorumCnxManager
            this.wrThread.start();//启动业务接收线程，从IO负责类QuorumCnxManager接收消息
}

//FastLeaderElection 初始化完成以后，调用 super.start()，最终运行 QuorumPeer 的run 方法

public synchronized void start() {
       if (!getView().containsKey(myid)) {
           throw new RuntimeException("My id " + myid + " not in the peer list");
        }
       loadDataBase();  //恢复DB
       startServerCnxnFactory();
       try {
           adminServer.start();
       } catch (AdminServerException e) {
           LOG.warn("Problem starting AdminServer", e);
           System.out.println(e);
       }
       startLeaderElection();//选举初始化
       super.start();
   }

```

 QuorumPeer 的run 方法

 ```java

 public void run() {
      updateThreadName();

      LOG.debug("Starting quorum peer");
      try { //此处通过JMX来监控一些属性
          jmxQuorumBean = new QuorumBean(this);
          MBeanRegistry.getInstance().register(jmxQuorumBean, null);
          for(QuorumServer s: getView().values()){
              ZKMBeanInfo p;
              if (getId() == s.id) {
                  p = jmxLocalPeerBean = new LocalPeerBean(this);
                  try {
                      MBeanRegistry.getInstance().register(p, jmxQuorumBean);
                  } catch (Exception e) {
                      LOG.warn("Failed to register with JMX", e);
                      jmxLocalPeerBean = null;
                  }
              } else {
                  RemotePeerBean rBean = new RemotePeerBean(s);
                  try {
                      MBeanRegistry.getInstance().register(rBean, jmxQuorumBean);
                      jmxRemotePeerBean.put(s.id, rBean);
                  } catch (Exception e) {
                      LOG.warn("Failed to register with JMX", e);
                  }
              }
          }
      } catch (Exception e) {
          LOG.warn("Failed to register with JMX", e);
          jmxQuorumBean = null;
      }

      try {
          /*
           * Main loop
           */
          while (running) {
              switch (getPeerState()) { //判断当前节点的状态
              case LOOKING: //如果是LOOKING，则进入选举流程
                  LOG.info("LOOKING");

                  if (Boolean.getBoolean("readonlymode.enabled")) {
                      LOG.info("Attempting to start ReadOnlyZooKeeperServer");

                      // Create read-only server but don't start it immediately
                      final ReadOnlyZooKeeperServer roZk =
                          new ReadOnlyZooKeeperServer(logFactory, this, this.zkDb);

                      // Instead of starting roZk immediately, wait some grace
                      // period before we decide we're partitioned.
                      //
                      // Thread is used here because otherwise it would require
                      // changes in each of election strategy classes which is
                      // unnecessary code coupling.
                      Thread roZkMgr = new Thread() {
                          public void run() {
                              try {
                                  // lower-bound grace period to 2 secs
                                  sleep(Math.max(2000, tickTime));
                                  if (ServerState.LOOKING.equals(getPeerState())) {
                                      roZk.startup();
                                  }
                              } catch (InterruptedException e) {
                                  LOG.info("Interrupted while attempting to start ReadOnlyZooKeeperServer, not started");
                              } catch (Exception e) {
                                  LOG.error("FAILED to start ReadOnlyZooKeeperServer", e);
                              }
                          }
                      };
                      try {
                          roZkMgr.start();
                          reconfigFlagClear();
                          if (shuttingDownLE) {
                              shuttingDownLE = false;
                              startLeaderElection();
                          }
                          //此处通过策略模式来决定当前用哪个选举算法来进行领导选举
                          setCurrentVote(makeLEStrategy().lookForLeader());
                      } catch (Exception e) {
                          LOG.warn("Unexpected exception", e);
                          setPeerState(ServerState.LOOKING);
                      } finally {
                          // If the thread is in the the grace period, interrupt
                          // to come out of waiting.
                          roZkMgr.interrupt();
                          roZk.shutdown();
                      }
                  } else {
                      try {
                         reconfigFlagClear();
                          if (shuttingDownLE) {
                             shuttingDownLE = false;
                             startLeaderElection();
                             }
                          setCurrentVote(makeLEStrategy().lookForLeader());
                      } catch (Exception e) {
                          LOG.warn("Unexpected exception", e);
                          setPeerState(ServerState.LOOKING);
                      }                        
                  }
                  break;
              case OBSERVING:
                  try {
                      LOG.info("OBSERVING");
                      setObserver(makeObserver(logFactory));
                      observer.observeLeader();
                  } catch (Exception e) {
                      LOG.warn("Unexpected exception",e );
                  } finally {
                      observer.shutdown();
                      setObserver(null);  
                     updateServerState();
                  }
                  break;
              case FOLLOWING:
                  try {
                     LOG.info("FOLLOWING");
                      setFollower(makeFollower(logFactory));
                      follower.followLeader();
                  } catch (Exception e) {
                     LOG.warn("Unexpected exception",e);
                  } finally {
                     follower.shutdown();
                     setFollower(null);
                     updateServerState();
                  }
                  break;
              case LEADING:
                  LOG.info("LEADING");
                  try {
                      setLeader(makeLeader(logFactory));
                      leader.lead();
                      setLeader(null);
                  } catch (Exception e) {
                      LOG.warn("Unexpected exception",e);
                  } finally {
                      if (leader != null) {
                          leader.shutdown("Forcing shutdown");
                          setLeader(null);
                      }
                      updateServerState();
                  }
                  break;
              }
              start_fle = Time.currentElapsedTime();
          }
      } finally {
          LOG.warn("QuorumPeer main thread exited");
          MBeanRegistry instance = MBeanRegistry.getInstance();
          instance.unregister(jmxQuorumBean);
          instance.unregister(jmxLocalPeerBean);

          for (RemotePeerBean remotePeerBean : jmxRemotePeerBean.values()) {
              instance.unregister(remotePeerBean);
          }

          jmxQuorumBean = null;
          jmxLocalPeerBean = null;
          jmxRemotePeerBean = null;
      }
  }

 ```

 调用 setCurrentVote(makeLEStrategy().lookForLeader());最终根据策略应该运行 FastLeaderElection 中的选举算法

 ```java

//FastLeaderElection.class
 public Vote lookForLeader() throws InterruptedException {
        try {
            self.jmxLeaderElectionBean = new LeaderElectionBean();
            MBeanRegistry.getInstance().register(
                    self.jmxLeaderElectionBean, self.jmxLocalPeerBean);
        } catch (Exception e) {
            LOG.warn("Failed to register with JMX", e);
            self.jmxLeaderElectionBean = null;
        }
        if (self.start_fle == 0) {
           self.start_fle = Time.currentElapsedTime();
        }
        try {
            //收到的投票
            HashMap<Long, Vote> recvset = new HashMap<Long, Vote>();
            //存储选举结果
            HashMap<Long, Vote> outofelection = new HashMap<Long, Vote>();

            int notTimeout = finalizeWait;

            synchronized(this){
                logicalclock.incrementAndGet(); //增加逻辑时钟
                //吃耍自己的zxid和epoch
                updateProposal(getInitId(), getInitLastLoggedZxid(), getPeerEpoch());
            }

            LOG.info("New election. My id =  " + self.getId() +
                    ", proposed zxid=0x" + Long.toHexString(proposedZxid));
            sendNotifications(); //发送投票，包括发送给自己

            /*
             * Loop in which we exchange notifications until we find a leader
             */

            while ((self.getPeerState() == ServerState.LOOKING) &&
                    (!stop)){//主循环，直到选举出leader
                /*
                 * Remove next notification from queue, times out after 2 times
                 * the termination time
                 */
                // 从IO线程里拿到投票消息，自己的投票也在这里处理
                //LinkedBlockedQueue()
                Notification n = recvqueue.poll(notTimeout,
                        TimeUnit.MILLISECONDS);

                /*
                 * Sends more notifications if haven't received enough.
                 * Otherwise processes new notification.
                 */
                if(n == null){
                    //如果空闲的情况下，消息发完了，继续发送，一直到选出leader为止
                    if(manager.haveDelivered()){
                        sendNotifications();
                    } else {
                        //消息还没投递出去，可能是其他server还没启动，尝试再连接
                        manager.connectAll();
                    }

                    /*
                     * Exponential backoff
                     */
                    //延长超时时间
                    int tmpTimeOut = notTimeout*2;
                    notTimeout = (tmpTimeOut < maxNotificationInterval?
                            tmpTimeOut : maxNotificationInterval);
                    LOG.info("Notification time out: " + notTimeout);
                }
                //收到了投票消息，判断收到的消息是不是属于这个集群内
                else if (self.getCurrentAndNextConfigVoters().contains(n.sid)) {
                    /*
                     * Only proceed if the vote comes from a replica in the current or next
                     * voting view.
                     */
                    switch (n.state) {//判断收到消息的节点的状态
                    case LOOKING:
                        if (getInitLastLoggedZxid() == -1) {
                            LOG.debug("Ignoring notification as our zxid is -1");
                            break;
                        }
                        if (n.zxid == -1) {
                            LOG.debug("Ignoring notification from member with -1 zxid" + n.sid);
                            break;
                        }
                        // If notification > current, replace and send messages out
                        //判断接收到的节点epoch大于logicalclock，则表示当前是新一轮的选举
                        if (n.electionEpoch > logicalclock.get()) {
                            logicalclock.set(n.electionEpoch); //更新本地的logicalclock
                            recvset.clear(); //清空接收队列
                            //检查收到的这个消息是否可以胜出，一次比较epoch，zxid、myid
                            if(totalOrderPredicate(n.leader, n.zxid, n.peerEpoch,
                                    getInitId(), getInitLastLoggedZxid(), getPeerEpoch())) {
                                //胜出以后，把投票改为对方的票据
                                updateProposal(n.leader, n.zxid, n.peerEpoch);
                            } else {//否则，票据不变
                                updateProposal(getInitId(),
                                        getInitLastLoggedZxid(),
                                        getPeerEpoch());
                            }
                            sendNotifications();//继续广播消息，让其他节点知道我现在的票据
                            //如果收到的消息epoch小于当前节点的epoch，则忽略这条消息
                        } else if (n.electionEpoch < logicalclock.get()) {
                            if(LOG.isDebugEnabled()){
                                LOG.debug("Notification election epoch is smaller than logicalclock. n.electionEpoch = 0x"
                                        + Long.toHexString(n.electionEpoch)
                                        + ", logicalclock=0x" + Long.toHexString(logicalclock.get()));
                            }
                            break;
                            //如果是epoch相同的话，就继续比较zxid、myid，如果胜出，则更新自己的票据，并且发出广播
                        } else if (totalOrderPredicate(n.leader, n.zxid, n.peerEpoch,
                                proposedLeader, proposedZxid, proposedEpoch)) {
                            updateProposal(n.leader, n.zxid, n.peerEpoch);
                            sendNotifications();
                        }

                        if(LOG.isDebugEnabled()){
                            LOG.debug("Adding vote: from=" + n.sid +
                                    ", proposed leader=" + n.leader +
                                    ", proposed zxid=0x" + Long.toHexString(n.zxid) +
                                    ", proposed election epoch=0x" + Long.toHexString(n.electionEpoch));
                        }
                        //添加到本机投票集合，用来做选举终结判断
                        recvset.put(n.sid, new Vote(n.leader, n.zxid, n.electionEpoch, n.peerEpoch));

                        //判断选举是否结束，默认算法是超过半数server同意
                        if (termPredicate(recvset,
                                new Vote(proposedLeader, proposedZxid,
                                        logicalclock.get(), proposedEpoch))) {
                            // Verify if there is any change in the proposed leader
                            //一直等新的notification到达，直到超时
                            while((n = recvqueue.poll(finalizeWait,
                                    TimeUnit.MILLISECONDS)) != null){
                                if(totalOrderPredicate(n.leader, n.zxid, n.peerEpoch,
                                        proposedLeader, proposedZxid, proposedEpoch)){
                                    recvqueue.put(n);
                                    break;
                                }
                            }
                            /*
                             * This predicate is true once we don't read any new
                             * relevant message from the reception queue
                             */
                            //确定leader
                            if (n == null) {
                                //修改状态，LEADING or FOLLOWING
                                self.setPeerState((proposedLeader == self.getId()) ?
                                        ServerState.LEADING: learningState());
                                //返回最终投票结果
                                Vote endVote = new Vote(proposedLeader,
                                        proposedZxid, proposedEpoch);
                                leaveInstance(endVote);
                                return endVote;
                            }
                        }
                        break;
                        //如果收到的选票状态不是LOOKING，比如这台机器刚加入一个已经正在运行的zk集群时
                        //OBSERVING机器不参数选举
                    case OBSERVING:
                        LOG.debug("Notification from observer: " + n.sid);
                        break;
                        //这2种需要参与选举
                    case FOLLOWING:
                    case LEADING:
                        /*
                         * Consider all notifications from the same epoch
                         * together.
                         */
                        if(n.electionEpoch == logicalclock.get()){ //判断epoch是否相同
                            //加入到本机的投票集合
                            recvset.put(n.sid, new Vote(n.leader, n.zxid, n.electionEpoch, n.peerEpoch));
                            //投票是否结束，如果结束，再确认LEADER是否有效
                            //如果结束，修改自己的状态并返回投票结果
                            if(termPredicate(recvset, new Vote(n.leader,
                                            n.zxid, n.electionEpoch, n.peerEpoch, n.state))
                                            && checkLeader(outofelection, n.leader, n.electionEpoch)) {
                                self.setPeerState((n.leader == self.getId()) ?
                                        ServerState.LEADING: learningState());

                                Vote endVote = new Vote(n.leader, n.zxid, n.peerEpoch);
                                leaveInstance(endVote);
                                return endVote;
                            }
                        }

                        /*
                         * Before joining an established ensemble, verify that
                         * a majority are following the same leader.
                         * Only peer epoch is used to check that the votes come
                         * from the same ensemble. This is because there is at
                         * least one corner case in which the ensemble can be
                         * created with inconsistent zxid and election epoch
                         * info. However, given that only one ensemble can be
                         * running at a single point in time and that each
                         * epoch is used only once, using only the epoch to
                         * compare the votes is sufficient.
                         *
                         * @see https://issues.apache.org/jira/browse/ZOOKEEPER-1732
                         */
                        outofelection.put(n.sid, new Vote(n.leader,
                                IGNOREVALUE, IGNOREVALUE, n.peerEpoch, n.state));
                        if (termPredicate(outofelection, new Vote(n.leader,
                                IGNOREVALUE, IGNOREVALUE, n.peerEpoch, n.state))
                                && checkLeader(outofelection, n.leader, IGNOREVALUE)) {
                            synchronized(this){
                                logicalclock.set(n.electionEpoch);
                                self.setPeerState((n.leader == self.getId()) ?
                                        ServerState.LEADING: learningState());
                            }
                            Vote endVote = new Vote(n.leader, n.zxid, n.peerEpoch);
                            leaveInstance(endVote);
                            return endVote;
                        }
                        break;
                    default:
                        LOG.warn("Notification state unrecoginized: " + n.state
                              + " (n.state), " + n.sid + " (n.sid)");
                        break;
                    }
                } else {
                    LOG.warn("Ignoring notification from non-cluster member " + n.sid);
                }
            }
            return null;
        } finally {
            try {
                if(self.jmxLeaderElectionBean != null){
                    MBeanRegistry.getInstance().unregister(
                            self.jmxLeaderElectionBean);
                }
            } catch (Exception e) {
                LOG.warn("Failed to unregister with JMX", e);
            }
            self.jmxLeaderElectionBean = null;
        }
    }

    private void sendNotifications() {
            for (long sid : self.getCurrentAndNextConfigVoters()) {//循环发送
                QuorumVerifier qv = self.getQuorumVerifier();
                //消息实体
                ToSend notmsg = new ToSend(ToSend.mType.notification,
                        proposedLeader,
                        proposedZxid,
                        logicalclock.get(),
                        QuorumPeer.ServerState.LOOKING,
                        sid,
                        proposedEpoch, qv.toString().getBytes());
                if(LOG.isDebugEnabled()){
                    LOG.debug("Sending Notification: " + proposedLeader + " (n.leader), 0x"  +
                          Long.toHexString(proposedZxid) + " (n.zxid), 0x" + Long.toHexString(logicalclock.get())  +
                          " (n.round), " + sid + " (recipient), " + self.getId() +
                          " (myid), 0x" + Long.toHexString(proposedEpoch) + " (n.peerEpoch)");
                }
                sendqueue.offer(notmsg); //添加到发送队列，这个队列会被workerSender消费
            }
        }

        private boolean termPredicate(HashMap<Long, Vote> votes, Vote vote) {
               SyncedLearnerTracker voteSet = new SyncedLearnerTracker();
               voteSet.addQuorumVerifier(self.getQuorumVerifier());
               if (self.getLastSeenQuorumVerifier() != null
                       && self.getLastSeenQuorumVerifier().getVersion() > self
                               .getQuorumVerifier().getVersion()) {
                   voteSet.addQuorumVerifier(self.getLastSeenQuorumVerifier());
               }

               /*
                * First make the views consistent. Sometimes peers will have different
                * zxids for a server depending on timing.
                */
               //遍历已经接收的投票集合，把等于当前投票的项放入set
               for (Map.Entry<Long, Vote> entry : votes.entrySet()) {
                   if (vote.equals(entry.getValue())) {
                       voteSet.addAck(entry.getKey());
                   }
               }
               //统计set集合，查看投某个id的票数是否超过一半
               return voteSet.hasAllQuorums();
           }            

 ```

 FastLeaderElection 选举过程

 其实在这个投票过程中就涉及到几个类，FastLeaderElection：FastLeaderElection 实现了 Election 接口，实现各服务器之间基于TCP 协议进行选举Notification：内部类，Notification 表示收到的选举投票信息（其他服务器发来的选举投票信息），其包含了被选举者的 id、zxid、选举周期等信息ToSend：ToSend表示发送给其他服务器的选举投票信息，也包含了被选举者的 id、zxid、选举周期等信息

 Messenger ： Messenger 包 含 了 WorkerReceiver 和WorkerSender 两个内部类；

 WorkerReceiver 实现了 Runnable 接口，是选票接收器。其会不断地从 QuorumCnxManager 中获取其他服务器发来的选举消息，并将其转换成一个选票，然后保存到recvqueue 中

 WorkerSender 也实现了 Runnable 接口，为选票发送器，其会不断地从 sendqueue 中获取待发送的选票，并将其传递到底层 QuorumCnxManager 中
