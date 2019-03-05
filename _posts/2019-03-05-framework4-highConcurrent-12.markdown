---
layout: post
title:  分布式协调之zookeeper实践一
date:   2019-03-05 22:52:12 +08:00
category: 高并发分布式
tags: zookeeper
comments: true
---

* content
{:toc}

zk的原理基本了解之后，对其进行应用研究以及实践。












## 简单的crud

对zk进行简单使用,连接及增删改查

```java

public class ConnectionDemo {

    public static void main(String[] args) {
        try {
            final CountDownLatch countDownLatch=new CountDownLatch(1);
            ZooKeeper zooKeeper=
                    new ZooKeeper("192.168.11.153:2181," +
                            "192.168.11.154:2181,192.168.11.155:2181",
                            4000, new Watcher() {
                        @Override
                        public void process(WatchedEvent event) {
                            if(Event.KeeperState.SyncConnected==event.getState()){
                                //如果收到了服务端的响应事件，连接成功
                                countDownLatch.countDown();
                            }
                        }
                    });
            countDownLatch.await();
            System.out.println(zooKeeper.getState());//CONNECTED

            //添加节点
            zooKeeper.create("/zk-persis","0".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
            Thread.sleep(1000);
            Stat stat=new Stat();

            //得到当前节点的值
            byte[] bytes=zooKeeper.getData("/zk-persis",null,stat);
            System.out.println(new String(bytes));

            //修改节点值
            zooKeeper.setData("/zk-persis-mic","1".getBytes(),stat.getVersion());

            //得到当前节点的值
            byte[] bytes1=zooKeeper.getData("/zk-persis",null,stat);
            System.out.println(new String(bytes1));

            zooKeeper.delete("/zk-persis-mic",stat.getVersion());

            zooKeeper.close();

            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }
}


```


## 事件机制

Watcher 监听机制是 Zookeeper 中非常重要的特性，我们基于 zookeeper 上创建的节点，可以对这些节点绑定监听事件，比如可以监听节点数据变更、节点删除、子节点状态变更等事件，通过这个事件机制，可以基于 zookeeper实现`分布式锁`、`集群管理`等功能

watcher 特性：当数据发生变化的时候， zookeeper 会产生一个 watcher 事件，并且会发送到客户端。但是客户端只会收到一次通知。如果后续这个节点再次发生变化，那么之前设置 watcher 的客户端不会再次收到消息。（watcher 是一次性的操作）。 可以通过循环监听去达到永久监听效果

如何注册事件机制

  通过这三个操作来绑定事件 ：getData、Exists、getChildren

  如何触发事件？ 凡是事务类型的操作，都会触发监听事件。create /delete /setData

watcher 事件类型

  `None` (-1), 客户端链接状态发生变化的时候，会收到 none 的事件

  `NodeCreated` (1), 创建节点的事件。 比如 zkpersis-mic

  `NodeDeleted `(2), 删除节点的事件

  `NodeDataChanged `(3), 节点数据发生变更

  `NodeChildrenChanged` (4); 子节点被创建、被删除、会发生事件触发

- 原理

watcher 绑定是什么？

是 ServerCnxn， 所以 w.process(e)，其实调用的应该是 ServerCnxn 的 process 方法。而 servercnxn 又是一个抽象方法，有两个实现类，分别是：NIOServerCnxn 和NettyServerCnxn。那接下来我们扒开 NettyServerCnxn 这个类的 process 方法看看究竟  

```java

public void process(WatchedEvent event) {
        ReplyHeader h = new ReplyHeader(-1, -1L, 0);
        if (LOG.isTraceEnabled()) {
            ZooTrace.logTraceMessage(LOG, ZooTrace.EVENT_DELIVERY_TRACE_MASK,
                                     "Deliver event " + event + " to 0x"
                                     + Long.toHexString(this.sessionId)
                                     + " through " + this);
        }

        // Convert WatchedEvent to a type that can be sent over the wire
        WatcherEvent e = event.getWrapper();

        try {
            sendResponse(h, e, "notification");//发送了一个回应
        } catch (IOException e1) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Problem sending to " + getRemoteSocketAddress(), e1);
            }
            close();
        }
    }

```

  客户端处理事件响应 SendThread.readResponse这块代码上面已经贴过了，所以我们只挑选当前流程的代码进行讲解，按照前面我们将到过，notifacation 通知消息的 xid 为-1，意味着~直接找到-1 的判断进行分析

  ```java
  void readResponse(ByteBuffer incomingBuffer) throws IOException {
            ByteBufferInputStream bbis = new ByteBufferInputStream(
                    incomingBuffer);
            BinaryInputArchive bbia = BinaryInputArchive.getArchive(bbis);
            ReplyHeader replyHdr = new ReplyHeader();

            replyHdr.deserialize(bbia, "header");
            if (replyHdr.getXid() == -2) { //?
                // -2 is the xid for pings
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Got ping response for sessionid: 0x"
                            + Long.toHexString(sessionId)
                            + " after "
                            + ((System.nanoTime() - lastPingSentNs) / 1000000)
                            + "ms");
                }
                return;
            }
            if (replyHdr.getXid() == -4) {
                // -4 is the xid for AuthPacket               
                if(replyHdr.getErr() == KeeperException.Code.AUTHFAILED.intValue()) {
                    state = States.AUTH_FAILED;                    
                    eventThread.queueEvent( new WatchedEvent(Watcher.Event.EventType.None,
                            Watcher.Event.KeeperState.AuthFailed, null) );            		            		
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Got auth sessionid:0x"
                            + Long.toHexString(sessionId));
                }
                return;
            }
            if (replyHdr.getXid() == -1) {
                // -1 means notification
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Got notification sessionid:0x"
                        + Long.toHexString(sessionId));
                }
                WatcherEvent event = new WatcherEvent();/

                event.deserialize(bbia, "response");
                //这个地方，是反序列化服务端的 WatcherEvent 事件。


                // convert from a server path to a client path
                if (chrootPath != null) {
                    String serverPath = event.getPath();
                    if(serverPath.compareTo(chrootPath)==0)
                        event.setPath("/");
                    else if (serverPath.length() > chrootPath.length())
                        event.setPath(serverPath.substring(chrootPath.length()));
                    else {
                    	LOG.warn("Got server path " + event.getPath()
                    			+ " which is too short for chroot path "
                    			+ chrootPath);
                    }
                }

                WatchedEvent we = new WatchedEvent(event);///组装 watchedEvent 对象。
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Got " + we + " for sessionid 0x"
                            + Long.toHexString(sessionId));
                }

                eventThread.queueEvent( we );////通过 eventTherad 进行事件处理
                return;
            }

            // If SASL authentication is currently in progress, construct and
            // send a response packet immediately, rather than queuing a
            // response as with other packets.
            if (tunnelAuthInProgress()) {
                GetSASLRequest request = new GetSASLRequest();
                request.deserialize(bbia,"token");
                zooKeeperSaslClient.respondToServer(request.getToken(),
                  ClientCnxn.this);
                return;
            }

            Packet packet;
            synchronized (pendingQueue) {
                if (pendingQueue.size() == 0) {
                    throw new IOException("Nothing in the queue, but got "
                            + replyHdr.getXid());
                }
                packet = pendingQueue.remove();
            }
            /*
             * Since requests are processed in order, we better get a response
             * to the first request!
             */
            try {
                if (packet.requestHeader.getXid() != replyHdr.getXid()) {
                    packet.replyHeader.setErr(
                            KeeperException.Code.CONNECTIONLOSS.intValue());
                    throw new IOException("Xid out of order. Got Xid "
                            + replyHdr.getXid() + " with err " +
                            + replyHdr.getErr() +
                            " expected Xid "
                            + packet.requestHeader.getXid()
                            + " for a packet with details: "
                            + packet );
                }

                packet.replyHeader.setXid(replyHdr.getXid());
                packet.replyHeader.setErr(replyHdr.getErr());
                packet.replyHeader.setZxid(replyHdr.getZxid());
                if (replyHdr.getZxid() > 0) {
                    lastZxid = replyHdr.getZxid();
                }
                if (packet.response != null && replyHdr.getErr() == 0) {
                    packet.response.deserialize(bbia, "response");
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Reading reply sessionid:0x"
                            + Long.toHexString(sessionId) + ", packet:: " + packet);
                }
            } finally {
                finishPacket(packet);
            }
        }

  ```

  eventThread.queueEvent

   SendThread 接收到服务端的通知事件后，会通过调用EventThread 类 的 queueEvent 方 法 将 事 件 传 给EventThread 线程，queueEvent 方法根据该通知事件，从ZKWatchManager 中取出所有相关的 Watcher，如果获取到相应的 Watcher，就会让 Watcher 移除失效

   ```java

   private void queueEvent(WatchedEvent event,
               Set<Watcher> materializedWatchers) {
           if (event.getType() == EventType.None
                   && sessionState == event.getState()) {
               return;
           }
           sessionState = event.getState();
           final Set<Watcher> watchers;
           if (materializedWatchers == null) {
               // materialize the watchers based on the event
               watchers = watcher.materialize(event.getState(),
                       event.getType(), event.getPath());
           } else {
               watchers = new HashSet<Watcher>();
               watchers.addAll(materializedWatchers);
           }
           WatcherSetEventPair pair = new WatcherSetEventPair(watchers, event);//封装 WatcherSetEventPair 对象，添加到waitngEvents 队列中
           // queue the pair (watch set & event) for later processing
           waitingEvents.add(pair);
       }

   ```

   Meterialize 方法通过 dataWatches 或者 existWatches 或者 childWatches的 remove 取出对应的 watch，表明客户端 watch 也是注册一次就移除同时需要根据 keeperState、eventType 和 path 返回应该被通知的 Watcher

   ```java

   public void run() {
          try {
             isRunning = true;
             while (true) {
                Object event = waitingEvents.take();
                if (event == eventOfDeath) {
                   wasKilled = true;
                } else {
                   processEvent(event);
                }
                if (wasKilled)
                   synchronized (waitingEvents) {
                      if (waitingEvents.isEmpty()) {
                         isRunning = false;
                         break;
                      }
                   }
             }
          } catch (InterruptedException e) {
             LOG.error("Event thread exiting due to interruption", e);
          }

           LOG.info("EventThread shut down for session: 0x{}",
                    Long.toHexString(getSessionId()));
       }


   ```

   核心处理even事件方法

   ```java

   private void processEvent(Object event) {
          try {
              if (event instanceof WatcherSetEventPair) {
                  // each watcher will process the event
                  WatcherSetEventPair pair = (WatcherSetEventPair) event;
                  for (Watcher watcher : pair.watchers) {
                      try {
                          watcher.process(pair.event);
                      } catch (Throwable t) {
                          LOG.error("Error while calling watcher ", t);
                      }
                  }
                } else if (event instanceof LocalCallback) {
                    LocalCallback lcb = (LocalCallback) event;
                    if (lcb.cb instanceof StatCallback) {
                        ((StatCallback) lcb.cb).processResult(lcb.rc, lcb.path,
                                lcb.ctx, null);
                    } else if (lcb.cb instanceof DataCallback) {
                        ((DataCallback) lcb.cb).processResult(lcb.rc, lcb.path,
                                lcb.ctx, null, null);
                    } else if (lcb.cb instanceof ACLCallback) {
                        ((ACLCallback) lcb.cb).processResult(lcb.rc, lcb.path,
                                lcb.ctx, null, null);
                    } else if (lcb.cb instanceof ChildrenCallback) {
                        ((ChildrenCallback) lcb.cb).processResult(lcb.rc,
                                lcb.path, lcb.ctx, null);
                    } else if (lcb.cb instanceof Children2Callback) {
                        ((Children2Callback) lcb.cb).processResult(lcb.rc,
                                lcb.path, lcb.ctx, null, null);
                    } else if (lcb.cb instanceof StringCallback) {
                        ((StringCallback) lcb.cb).processResult(lcb.rc,
                                lcb.path, lcb.ctx, null);
                    } else {
                        ((VoidCallback) lcb.cb).processResult(lcb.rc, lcb.path,
                                lcb.ctx);
                    }
                } else {
                  Packet p = (Packet) event;
                  int rc = 0;
                  String clientPath = p.clientPath;
                  if (p.replyHeader.getErr() != 0) {
                      rc = p.replyHeader.getErr();
                  }
                  if (p.cb == null) {
                      LOG.warn("Somehow a null cb got to EventThread!");
                  } else if (p.response instanceof ExistsResponse
                          || p.response instanceof SetDataResponse
                          || p.response instanceof SetACLResponse) {
                      StatCallback cb = (StatCallback) p.cb;
                      if (rc == 0) {
                          if (p.response instanceof ExistsResponse) {
                              cb.processResult(rc, clientPath, p.ctx,
                                      ((ExistsResponse) p.response)
                                              .getStat());
                          } else if (p.response instanceof SetDataResponse) {
                              cb.processResult(rc, clientPath, p.ctx,
                                      ((SetDataResponse) p.response)
                                              .getStat());
                          } else if (p.response instanceof SetACLResponse) {
                              cb.processResult(rc, clientPath, p.ctx,
                                      ((SetACLResponse) p.response)
                                              .getStat());
                          }
                      } else {
                          cb.processResult(rc, clientPath, p.ctx, null);
                      }
                  } else if (p.response instanceof GetDataResponse) {
                      DataCallback cb = (DataCallback) p.cb;
                      GetDataResponse rsp = (GetDataResponse) p.response;
                      if (rc == 0) {
                          cb.processResult(rc, clientPath, p.ctx, rsp
                                  .getData(), rsp.getStat());
                      } else {
                          cb.processResult(rc, clientPath, p.ctx, null,
                                  null);
                      }
                  } else if (p.response instanceof GetACLResponse) {
                      ACLCallback cb = (ACLCallback) p.cb;
                      GetACLResponse rsp = (GetACLResponse) p.response;
                      if (rc == 0) {
                          cb.processResult(rc, clientPath, p.ctx, rsp
                                  .getAcl(), rsp.getStat());
                      } else {
                          cb.processResult(rc, clientPath, p.ctx, null,
                                  null);
                      }
                  } else if (p.response instanceof GetChildrenResponse) {
                      ChildrenCallback cb = (ChildrenCallback) p.cb;
                      GetChildrenResponse rsp = (GetChildrenResponse) p.response;
                      if (rc == 0) {
                          cb.processResult(rc, clientPath, p.ctx, rsp
                                  .getChildren());
                      } else {
                          cb.processResult(rc, clientPath, p.ctx, null);
                      }
                  } else if (p.response instanceof GetChildren2Response) {
                      Children2Callback cb = (Children2Callback) p.cb;
                      GetChildren2Response rsp = (GetChildren2Response) p.response;
                      if (rc == 0) {
                          cb.processResult(rc, clientPath, p.ctx, rsp
                                  .getChildren(), rsp.getStat());
                      } else {
                          cb.processResult(rc, clientPath, p.ctx, null, null);
                      }
                  } else if (p.response instanceof CreateResponse) {
                      StringCallback cb = (StringCallback) p.cb;
                      CreateResponse rsp = (CreateResponse) p.response;
                      if (rc == 0) {
                          cb.processResult(rc, clientPath, p.ctx,
                                  (chrootPath == null
                                          ? rsp.getPath()
                                          : rsp.getPath()
                                    .substring(chrootPath.length())));
                      } else {
                          cb.processResult(rc, clientPath, p.ctx, null);
                      }
                  } else if (p.response instanceof Create2Response) {
                	  Create2Callback cb = (Create2Callback) p.cb;
                      Create2Response rsp = (Create2Response) p.response;
                      if (rc == 0) {
                          cb.processResult(rc, clientPath, p.ctx,
                                  (chrootPath == null
                                          ? rsp.getPath()
                                          : rsp.getPath()
                                    .substring(chrootPath.length())), rsp.getStat());
                      } else {
                          cb.processResult(rc, clientPath, p.ctx, null, null);
                      }                   
                  } else if (p.response instanceof MultiResponse) {
                	  MultiCallback cb = (MultiCallback) p.cb;
                	  MultiResponse rsp = (MultiResponse) p.response;
                	  if (rc == 0) {
                		  List<OpResult> results = rsp.getResultList();
                		  int newRc = rc;
                		  for (OpResult result : results) {
                			  if (result instanceof ErrorResult
                					  && KeeperException.Code.OK.intValue() != (newRc = ((ErrorResult) result)
                					  .getErr())) {
                				  break;
                			  }
                		  }
                		  cb.processResult(newRc, clientPath, p.ctx, results);
                	  } else {
                		  cb.processResult(rc, clientPath, p.ctx, null);
                	  }
                  }  else if (p.cb instanceof VoidCallback) {
                      VoidCallback cb = (VoidCallback) p.cb;
                      cb.processResult(rc, clientPath, p.ctx);
                  }
              }
          } catch (Throwable t) {
              LOG.error("Caught unexpected throwable", t);
          }
       }

   ```
