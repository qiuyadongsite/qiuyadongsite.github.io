---
layout: post
title:  RPC之RMI
date:   2019-03-01 22:52:12 +08:00
category: 高并发分布式
tags: RPC
comments: true
---

* content
{:toc}

RMI 全称是 remote method invocation – 远程方法调用，一种用于远程过程调用的应用程序编程接口，是纯 java 的网络分布式应用系统的核心解决方案之一。













## 概念

- RPC

RPC（Remote Procedure Call,远程过程调用），一般用来实现部署在不同机器上的系统之间的方法调用，使得程序能够像访问本地系统资源一样，通过网络传输去访问远端
系统资源；对于客户端来说， 传输层使用什么协议，序列化、反序列化都是透明的。

- Java RMI

RMI 目前使用 Java 远程消息交换协议 JRMP（Java Remote Messageing Protocol）进行通信，由于 JRMP 是专为 Java对象制定的，是分布式应用系统的百分之百纯 java 解决方案,用 Java RMI 开发的应用系统可以部署在任何支持 JRE的平台上，缺点是，由于 JRMP 是专门为 java 对象指定的，因此 RMI 对于非 JAVA 语言开发的应用系统的支持不足，不能与非 JAVA 语言书写的对象进行通信

## 源码分析

- 发布服务的类图

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/rmiclassextends.png)

- 远程引用类图

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/rmiclassextends.png)


  简单介绍

  远程对象必须实现 UnicastRemoteObject，这样才能保证客户端访问获得远程对象时，该远程对象会把自身的一个拷贝以 Socket 形式传输给客户端，客户端获得的拷贝称为“stub” ，而服务器端本身已经存在的远程对象成为“skeleton”，此时客户端的 stub 是客户端的一个代理，用于与服务器端进行通信，而 skeleton 是服务端的一个代理，用于接收客户端的请求之后调用远程方法来响应客户端的请求。

  - 跟代码

  从HelloServiceImpl的构造函看起 。调用了父类UnicastRemoteObject的构造方法，追溯到UnicastRemoteObject 的私有方法 exportObject()。这里做了一个判断，判断服务的实现是不是UnicastRemoteObject 的子类，如果是，则直接赋值其 ref（RemoteRef）对象为传入的 UnicastServerRef 对象。反之则调用UnicastServerRef 的 exportObject()方法。

  发布实现用例


  ```java

  public class Server {

      public static void main(String[] args) {

              IHelloService helloService=new HelloServiceImpl();//已经发布了一个远程对象

              LocateRegistry.createRegistry(1099);

              Naming.rebind("rmi://127.0.0.1/Hello",helloService); //注册中心 key - value
              System.out.println("服务启动成功");

      }
  }


  public interface IHelloService extends Remote {

      String sayHello(String msg) throws RemoteException;
  }

    public class HelloServiceImpl extends UnicastRemoteObject implements IHelloService{
      protected HelloServiceImpl() throws RemoteException {
         // super();
      }
      @Override
      public String sayHello(String msg) throws RemoteException{
          return "Hello,"+msg;
      }
  }

  ```

  这里继承了UnicastRemoteObject，所以会实例化UnicastRemoteObject掉用构造方法,下边是构造过程

  ```java

//由于this指带HelloServiceImpl，而其实现的接口继承了Remote,可以转换为Remote
protected UnicastRemoteObject(int port) throws RemoteException
   {
       this.port = port;
       exportObject((Remote) this, port);
   }
  //将port转换成UnicastServerRef对象，由于在该对象内部有LiveRef可以用于tcp通讯
   public static Remote exportObject(Remote obj, int port)
       throws RemoteException
   {
       return exportObject(obj, new UnicastServerRef(port));
   }
//接着调用构造的发布对象方法

   private static Remote exportObject(Remote obj, UnicastServerRef sref)
        throws RemoteException
    {
        //此处由于就是继承了该类，因此将上面用于通讯的UnicastServerRef，作为属性值赋值给UnicastRemoteObject内（主要方便调用）
        if (obj instanceof UnicastRemoteObject) {
            ((UnicastRemoteObject) obj).ref = sref;
        }
        return sref.exportObject(obj, null, false);
    }
//接下来，包装了各个对象后发布对象实例（UnicastServerRef的方法）
    public Remote exportObject(Remote var1, Object var2, boolean var3) throws RemoteException {
            Class var4 = var1.getClass();

            Remote var5;
            try {
              //此处创建了具体实现类的一个代理类，
                var5 = Util.createProxy(var4, this.getClientRef(), this.forceStubUse);
            } catch (IllegalArgumentException var7) {
                throw new ExportException("remote object implements illegal remote interface", var7);
            }

            if (var5 instanceof RemoteStub) {
                this.setSkeleton(var1);
            }

           //此处会讲具体实现类还有代理类包装一下，并将绑定到具体port上，等待客户端请求
            Target var6 = new Target(var1, this, var5, this.ref.getObjID(), var3);
            this.ref.exportObject(var6);
            this.hashToMethod_Map = (Map)hashToMethod_Maps.get(var4);
            return var5;
        }


  ```

  解释一下上面的代理过程

  ```java

  //this.getClientRef()
  //由于this.ref是根据port创建的猜想UnicastRef类应该与连接相关
  protected RemoteRef getClientRef() {
        return new UnicastRef(this.ref);
    }

//UnicastRef主要方法，之后详解
    public Object invoke(Remote var1, Method var2, Object[] var3, long var4) throws Exception {
}

  //此处是创建代理的方法
          public static Remote createProxy(Class<?> var0, RemoteRef var1, boolean var2) throws StubNotFoundException {
                  Class var3;
                  try {
                      var3 = getRemoteClass(var0);
                  } catch (ClassNotFoundException var9) {
                      throw new StubNotFoundException("object does not implement a remote interface: " + var0.getName());
                  }

                  if (var2 || !ignoreStubClasses && stubClassExists(var3)) {
                      return createStub(var3, var1);
                  } else {
                      final ClassLoader var4 = var0.getClassLoader();
                      final Class[] var5 = getRemoteInterfaces(var0);
                      final RemoteObjectInvocationHandler var6 = new RemoteObjectInvocationHandler(var1);

                      try {
                          return (Remote)AccessController.doPrivileged(new PrivilegedAction<Remote>() {
                              public Remote run() {
                                  return (Remote)Proxy.newProxyInstance(var4, var5, var6);
                              }
                          });
                      } catch (IllegalArgumentException var8) {
                          throw new StubNotFoundException("unable to create proxy", var8);
                      }
                  }
              }


  ```

  - 服务端启动 Registry 服务

  ```java
  LocateRegistry.createRegistry(1099);

  public static Registry createRegistry(int port) throws RemoteException {
    //创建一个RegistryImpl
         return new RegistryImpl(port);
     }

     public RegistryImpl(final int var1) throws RemoteException {
        if (var1 == 1099 && System.getSecurityManager() != null) {
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                    public Void run() throws RemoteException {
                        LiveRef var1x = new LiveRef(RegistryImpl.id, var1);
                        RegistryImpl.this.setup(new UnicastServerRef(var1x));
                        return null;
                    }
                }, (AccessControlContext)null, new SocketPermission("localhost:" + var1, "listen,accept"));
            } catch (PrivilegedActionException var3) {
                throw (RemoteException)var3.getException();
            }
        } else {
            LiveRef var2 = new LiveRef(id, var1);
            this.setup(new UnicastServerRef(var2));
        }

    }

    //这里做了一个判断，如果服务端指定的端口是 1099 并且系统开启了安全管理器，那么就可以在限定的权限集内绕过系统的安全校验。这里纯粹是为 了 提 高 效 率，真正的逻辑在 this.setup(new UnicastServerRef())这个方法里面

    private void setup(UnicastServerRef var1) throws RemoteException {
        this.ref = var1;
        var1.exportObject(this, (Object)null, true);
    }

   //setup 方法将指向正在初始化的 RegistryImpl 对象的远程引用 ref(RemoteRef)赋值为传入的 UnicastServerRef 对象，这里涉及到向上转型，然后继续执行 UnicastServerRef 的exportObject 方法

  // 进入 UnicastServerRef 的 exportObject()方法。可以看到，这里首先为传入的 RegistryImpl 创建一个代理，这个代理我们可以推断出就是后面服务于客户端的 RegistryImpl 的Stub（RegistryImpl_Stub）对象。然后将 UnicastServerRef的 skel（skeleton）对象设置为当前 RegistryImpl 对象。最后用 skeleton、stub、UnicastServerRef 对象、id 和一个boolean 值构造了一个 Target 对象，也就是这个 Target 对象基本上包含了全部的信息，等待 TCP 调用。调用UnicastServerRef 的 ref（LiveRef）变量的 exportObject()方法。

  ```

  到上面为止，我们看到的都是一些变量的赋值和创建工作，还没有到连接层，这些引用对象将会被 Stub 和 Skeleton对象使用。接下来就是连接层上的了。追溯 LiveRef 的exportObject()方法，很容易找到了 TCPTransport 的exportObject()方法。这个方法做的事情就是将上面构造的Target 对象暴露出去。调用 TCPTransport 的 listen()方法，listen()方法创建了一个 ServerSocket，并且启动了一条线程等待客户端的请求。接着调用父类 Transport 的exportObject()将 Target 对象存放进 ObjectTable 中。

  ```java

//Target var6 = new Target(var1, this, var5, this.ref.getObjID(), var3);
//this.ref.exportObject(var6);

public void exportObject(Target var1) throws RemoteException {
        this.transport.exportObject(var1);
    }

  public void exportObject(Target var1) throws RemoteException {
        synchronized(this) {
            this.listen();
            ++this.exportCount;
        }

        boolean var2 = false;
        boolean var12 = false;

        try {
            var12 = true;
            super.exportObject(var1);
            var2 = true;
            var12 = false;
        } finally {
            if (var12) {
                if (!var2) {
                    synchronized(this) {
                        this.decrementExportCount();
                    }
                }

            }
        }

        if (!var2) {
            synchronized(this) {
                this.decrementExportCount();
            }
        }

    }

  ```

  到这里，我们已经将 RegistryImpl 对象创建并且起了服务等待客户端的请求。

- 客户端DEMO代码

```java
public class ClientDemo {

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException {
        IHelloService helloService=
                (IHelloService)Naming.lookup("rmi://127.0.0.1/Hello");
        // HelloServiceImpl实例(HelloServiceImpl_stub)
        // RegistryImpl_stub
        System.out.println(helloService.sayHello("Mic"));
    }
}
```  
从上面的代码看起，容易追溯到LocateRegistry的

```java

public static Remote lookup(String name)
       throws NotBoundException,
           java.net.MalformedURLException,
           RemoteException
   {
       ParsedNamingURL parsed = parseURL(name);
       Registry registry = getRegistry(parsed);

       if (parsed.name == null)
           return registry;
        //最后调用lookup   
       return registry.lookup(parsed.name);
   }
  // getRegistry()方法。这个方法做的事情是通过传入的 host和 port 构造 RemoteRef 对象，并创建了一个本地代理。这个代理对象其实是 RegistryImpl_Stub 对象。这样客户端便有了服务端的RegistryImpl的代理（取决于ignoreStubClasses 变量）。但注意此时这个代理其实还没有和服务端的 RegistryImpl 对象关联，毕竟是两个 VM 上面的对象，这里我们也可以猜测，代理和远程的 Registry对象之间是通过 socket 消息来完成的。

   public static Registry getRegistry(String host, int port,
                                          RMIClientSocketFactory csf)
           throws RemoteException
       {
           Registry registry = null;

           if (port <= 0)
               port = Registry.REGISTRY_PORT;

           if (host == null || host.length() == 0) {
               // If host is blank (as returned by "file:" URL in 1.0.2 used in
               // java.rmi.Naming), try to convert to real local host name so
               // that the RegistryImpl's checkAccess will not fail.
               try {
                   host = java.net.InetAddress.getLocalHost().getHostAddress();
               } catch (Exception e) {
                   // If that failed, at least try "" (localhost) anyway...
                   host = "";
               }
           }

           /*
            * Create a proxy for the registry with the given host, port, and
            * client socket factory.  If the supplied client socket factory is
            * null, then the ref type is a UnicastRef, otherwise the ref type
            * is a UnicastRef2.  If the property
            * java.rmi.server.ignoreStubClasses is true, then the proxy
            * returned is an instance of a dynamic proxy class that implements
            * the Registry interface; otherwise the proxy returned is an
            * instance of the pregenerated stub class for RegistryImpl.
            **/
           LiveRef liveRef =
               new LiveRef(new ObjID(ObjID.REGISTRY_ID),
                           new TCPEndpoint(host, port, csf, null),
                           false);//用于TPC通讯
           RemoteRef ref =
               (csf == null) ? new UnicastRef(liveRef) : new UnicastRef2(liveRef);

           return (Registry) Util.createProxy(RegistryImpl.class, ref, false);//动态代理时进行通信
       }
```

调用 RegistryImpl_Stub 的 ref（RemoteRef）对象的newCall()方法，将 RegistryImpl_Stub 对象传了进去，不要忘了构造它的时候我们将服务器的主机端口等信息传了进去，也就是我们把服务器相关的信息也传进了 newCall()方法。newCall()方法做的事情简单来看就是建立了跟远程RegistryImpl 的 Skeleton 对象的连接。（不要忘了上面我们说到过服务端通过 TCPTransport 的 exportObject()方法等待着客户端的请求）

```java

public Remote lookup(String var1) throws AccessException, NotBoundException, RemoteException {
        try {

          //调用newCall
            RemoteCall var2 = super.ref.newCall(this, operations, 2, 4905912898345647071L);

            try {
                ObjectOutput var3 = var2.getOutputStream();
                var3.writeObject(var1);
            } catch (IOException var18) {
                throw new MarshalException("error marshalling arguments", var18);
            }

            super.ref.invoke(var2);//接下来看看这是干嘛的

            Remote var23;
            try {
                ObjectInput var6 = var2.getInputStream();
                var23 = (Remote)var6.readObject();
            } catch (IOException var15) {
                throw new UnmarshalException("error unmarshalling return", var15);
            } catch (ClassNotFoundException var16) {
                throw new UnmarshalException("error unmarshalling return", var16);
            } finally {
                super.ref.done(var2);
            }

            return var23;
        } catch (RuntimeException var19) {
            throw var19;
        } catch (RemoteException var20) {
            throw var20;
        } catch (NotBoundException var21) {
            throw var21;
        } catch (Exception var22) {
            throw new UnexpectedException("undeclared checked exception", var22);
        }
    }
   //这个newcall为RemoteRef(emoteRef ref =   (csf == null) ? new UnicastRef(liveRef) : new UnicastRef2(liveRef);)


    public RemoteCall newCall(RemoteObject var1, Operation[] var2, int var3, long var4) throws RemoteException {
       clientRefLog.log(Log.BRIEF, "get connection");
       Connection var6 = this.ref.getChannel().newConnection();

       try {
           clientRefLog.log(Log.VERBOSE, "create call context");
           if (clientCallLog.isLoggable(Log.VERBOSE)) {
               this.logClientCall(var1, var2[var3]);
           }

           StreamRemoteCall var7 = new StreamRemoteCall(var6, this.ref.getObjID(), var3, var4);

           try {
               this.marshalCustomCallData(var7.getOutputStream());
           } catch (IOException var9) {
               throw new MarshalException("error marshaling custom call data");
           }

           return var7;
       } catch (RemoteException var10) {
           this.ref.getChannel().free(var6, false);
           throw var10;
       }
   }

```

连接建立之后自然就是发送请求了。我们知道客户端终究只是拥有 Registry 对象的代理，而不是真正地位于服务端的 Registry 对象本身，他们位于不同的虚拟机实例之中，无法直接调用。必然是通过消息进行交互的。看看super.ref.invoke() 这里做了什么？容易追溯到StreamRemoteCall 的 executeCall()方法。看似本地调用，但其实很容易从代码中看出来是通过 tcp 连接发送消息到服务端。由服务端解析并且处理调用。

```java

//UnicastRef

public void invoke(RemoteCall var1) throws Exception {
        try {
            clientRefLog.log(Log.VERBOSE, "execute call");
            var1.executeCall();
        } catch (RemoteException var3) {
            clientRefLog.log(Log.BRIEF, "exception: ", var3);
            this.free(var1, false);
            throw var3;
        } catch (Error var4) {
            clientRefLog.log(Log.BRIEF, "error: ", var4);
            this.free(var1, false);
            throw var4;
        } catch (RuntimeException var5) {
            clientRefLog.log(Log.BRIEF, "exception: ", var5);
            this.free(var1, false);
            throw var5;
        } catch (Exception var6) {
            clientRefLog.log(Log.BRIEF, "exception: ", var6);
            this.free(var1, true);
            throw var6;
        }
    }
```

至此，我们已经将客户端的服务查询请求发出了。服务端接收客户端的服务查询请求并返回给客户端结果这里我们继续跟踪 server 端代码的服务发布代码，一步步往上面翻。

  - 看看服务器

  ```java
  public Remote exportObject(Remote var1, Object var2, boolean var3) throws RemoteException {
       Class var4 = var1.getClass();

       Remote var5;
       try {
           var5 = Util.createProxy(var4, this.getClientRef(), this.forceStubUse);
       } catch (IllegalArgumentException var7) {
           throw new ExportException("remote object implements illegal remote interface", var7);
       }

       if (var5 instanceof RemoteStub) {
           this.setSkeleton(var1);
       }

       Target var6 = new Target(var1, this, var5, this.ref.getObjID(), var3);
       this.ref.exportObject(var6);//看看此处的发布
       this.hashToMethod_Map = (Map)hashToMethod_Maps.get(var4);
       return var5;
   }

   public void exportObject(Target var1) throws RemoteException {
        synchronized(this) {
            this.listen();//看看此处
            ++this.exportCount;
        }

        boolean var2 = false;
        boolean var12 = false;

        try {
            var12 = true;
            super.exportObject(var1);
            var2 = true;
            var12 = false;
        } finally {
            if (var12) {
                if (!var2) {
                    synchronized(this) {
                        this.decrementExportCount();
                    }
                }

            }
        }

        if (!var2) {
            synchronized(this) {
                this.decrementExportCount();
            }
        }

    }


  ```

  在 TCP 协议层发起 socket 监听，并采用多线程循环接收请求：TCPTransport.AcceptLoop(this.server)

```java

private void listen() throws RemoteException {
        assert Thread.holdsLock(this);

        TCPEndpoint var1 = this.getEndpoint();
        int var2 = var1.getPort();
        if (this.server == null) {
            if (tcpLog.isLoggable(Log.BRIEF)) {
                tcpLog.log(Log.BRIEF, "(port " + var2 + ") create server socket");
            }

            try {
                this.server = var1.newServerSocket();
                Thread var3 = (Thread)AccessController.doPrivileged(new NewThreadAction(new TCPTransport.AcceptLoop(this.server), "TCP Accept-" + var2, true));
                var3.start();
            } catch (BindException var4) {
                throw new ExportException("Port already in use: " + var2, var4);
            } catch (IOException var5) {
                throw new ExportException("Listen failed on port: " + var2, var5);
            }
        } else {
            SecurityManager var6 = System.getSecurityManager();
            if (var6 != null) {
                var6.checkListen(var2);
            }
        }

    }

```



```java

private void executeAcceptLoop() {
            if (TCPTransport.tcpLog.isLoggable(Log.BRIEF)) {
                TCPTransport.tcpLog.log(Log.BRIEF, "listening on port " + TCPTransport.this.getEndpoint().getPort());
            }

            while(true) {
                Socket var1 = null;

                try {
                    var1 = this.serverSocket.accept();
                    InetAddress var16 = var1.getInetAddress();
                    String var3 = var16 != null ? var16.getHostAddress() : "0.0.0.0";

                    try {
                      //继续通过线程池来处理 socket 接收到的请求
                        TCPTransport.connectionThreadPool.execute(TCPTransport.this.new ConnectionHandler(var1, var3));
                    } catch (RejectedExecutionException var11) {
                        TCPTransport.closeSocket(var1);
                        TCPTransport.tcpLog.log(Log.BRIEF, "rejected connection from " + var3);
                    }
                } catch (Throwable var15) {
                    Throwable var2 = var15;

                    try {
                        if (this.serverSocket.isClosed()) {
                            return;
                        }

                        try {
                            if (TCPTransport.tcpLog.isLoggable(Level.WARNING)) {
                                TCPTransport.tcpLog.log(Level.WARNING, "accept loop for " + this.serverSocket + " throws", var2);
                            }
                        } catch (Throwable var13) {
                            ;
                        }
                    } finally {
                        if (var1 != null) {
                            TCPTransport.closeSocket(var1);
                        }

                    }

                    if (!(var15 instanceof SecurityException)) {
                        try {
                            TCPEndpoint.shedConnectionCaches();
                        } catch (Throwable var12) {
                            ;
                        }
                    }

                    if (!(var15 instanceof Exception) && !(var15 instanceof OutOfMemoryError) && !(var15 instanceof NoClassDefFoundError)) {
                        if (var15 instanceof Error) {
                            throw (Error)var15;
                        }

                        throw new UndeclaredThrowableException(var15);
                    }

                    if (!this.continueAfterAcceptFailure(var15)) {
                        return;
                    }
                }
            }
        }

//TCPTransport的运行

        public void run() {
           Thread var1 = Thread.currentThread();
           String var2 = var1.getName();

           try {
               var1.setName("RMI TCP Connection(" + TCPTransport.connectionCount.incrementAndGet() + ")-" + this.remoteHost);
               AccessController.doPrivileged(() -> {
                   this.run0();
                   return null;
               }, TCPTransport.NOPERMS_ACC);
           } finally {
               var1.setName(var2);
           }

       }
// 下面这个 run0 方法里面做了一些判断，具体的功能是干嘛不太清楚，我猜想是对不同的协议来做处理。我们的这个 案例中，会走到如下的代码中来。最终调用

//最终调用TCPTransport.this.handleMessages(var14, true);这个地方也做了判断，你们如果不知道怎么走的话，直接在这里加断点就知道。这里会走到 case 80的段落，执行
serviceCall()这个方法
private void run0() {
            TCPEndpoint var1 = TCPTransport.this.getEndpoint();
            int var2 = var1.getPort();
            TCPTransport.threadConnectionHandler.set(this);

            try {
                this.socket.setTcpNoDelay(true);
            } catch (Exception var31) {
                ;
            }

            try {
                if (TCPTransport.connectionReadTimeout > 0) {
                    this.socket.setSoTimeout(TCPTransport.connectionReadTimeout);
                }
            } catch (Exception var30) {
                ;
            }

            try {
                InputStream var3 = this.socket.getInputStream();
                Object var4 = var3.markSupported() ? var3 : new BufferedInputStream(var3);
                ((InputStream)var4).mark(4);
                DataInputStream var5 = new DataInputStream((InputStream)var4);
                int var6 = var5.readInt();
                if (var6 == 1347375956) {
                    TCPTransport.tcpLog.log(Log.BRIEF, "decoding HTTP-wrapped call");
                    ((InputStream)var4).reset();

                    try {
                        this.socket = new HttpReceiveSocket(this.socket, (InputStream)var4, (OutputStream)null);
                        this.remoteHost = "0.0.0.0";
                        var3 = this.socket.getInputStream();
                        var4 = new BufferedInputStream(var3);
                        var5 = new DataInputStream((InputStream)var4);
                        var6 = var5.readInt();
                    } catch (IOException var29) {
                        throw new RemoteException("Error HTTP-unwrapping call", var29);
                    }
                }

                short var7 = var5.readShort();
                if (var6 == 1246907721 && var7 == 2) {
                    OutputStream var8 = this.socket.getOutputStream();
                    BufferedOutputStream var9 = new BufferedOutputStream(var8);
                    DataOutputStream var10 = new DataOutputStream(var9);
                    int var11 = this.socket.getPort();
                    if (TCPTransport.tcpLog.isLoggable(Log.BRIEF)) {
                        TCPTransport.tcpLog.log(Log.BRIEF, "accepted socket from [" + this.remoteHost + ":" + var11 + "]");
                    }

                    byte var15 = var5.readByte();
                    TCPEndpoint var12;
                    TCPChannel var13;
                    TCPConnection var14;
                    switch(var15) {
                    case 75:
                        var10.writeByte(78);
                        if (TCPTransport.tcpLog.isLoggable(Log.VERBOSE)) {
                            TCPTransport.tcpLog.log(Log.VERBOSE, "(port " + var2 + ") " + "suggesting " + this.remoteHost + ":" + var11);
                        }

                        var10.writeUTF(this.remoteHost);
                        var10.writeInt(var11);
                        var10.flush();
                        String var16 = var5.readUTF();
                        int var17 = var5.readInt();
                        if (TCPTransport.tcpLog.isLoggable(Log.VERBOSE)) {
                            TCPTransport.tcpLog.log(Log.VERBOSE, "(port " + var2 + ") client using " + var16 + ":" + var17);
                        }

                        var12 = new TCPEndpoint(this.remoteHost, this.socket.getLocalPort(), var1.getClientSocketFactory(), var1.getServerSocketFactory());
                        var13 = new TCPChannel(TCPTransport.this, var12);
                        var14 = new TCPConnection(var13, this.socket, (InputStream)var4, var9);
                        TCPTransport.this.handleMessages(var14, true);
                        return;
                    case 76:
                        var12 = new TCPEndpoint(this.remoteHost, this.socket.getLocalPort(), var1.getClientSocketFactory(), var1.getServerSocketFactory());
                        var13 = new TCPChannel(TCPTransport.this, var12);
                        var14 = new TCPConnection(var13, this.socket, (InputStream)var4, var9);
                        TCPTransport.this.handleMessages(var14, false);
                        return;
                    case 77:
                        if (TCPTransport.tcpLog.isLoggable(Log.VERBOSE)) {
                            TCPTransport.tcpLog.log(Log.VERBOSE, "(port " + var2 + ") accepting multiplex protocol");
                        }

                        var10.writeByte(78);
                        if (TCPTransport.tcpLog.isLoggable(Log.VERBOSE)) {
                            TCPTransport.tcpLog.log(Log.VERBOSE, "(port " + var2 + ") suggesting " + this.remoteHost + ":" + var11);
                        }

                        var10.writeUTF(this.remoteHost);
                        var10.writeInt(var11);
                        var10.flush();
                        var12 = new TCPEndpoint(var5.readUTF(), var5.readInt(), var1.getClientSocketFactory(), var1.getServerSocketFactory());
                        if (TCPTransport.tcpLog.isLoggable(Log.VERBOSE)) {
                            TCPTransport.tcpLog.log(Log.VERBOSE, "(port " + var2 + ") client using " + var12.getHost() + ":" + var12.getPort());
                        }

                        ConnectionMultiplexer var18;
                        synchronized(TCPTransport.this.channelTable) {
                            var13 = TCPTransport.this.getChannel(var12);
                            var18 = new ConnectionMultiplexer(var13, (InputStream)var4, var8, false);
                            var13.useMultiplexer(var18);
                        }

                        var18.run();
                        return;
                    default:
                        var10.writeByte(79);
                        var10.flush();
                        return;
                    }
                }

                TCPTransport.closeSocket(this.socket);
            } catch (IOException var32) {
                TCPTransport.tcpLog.log(Log.BRIEF, "terminated with exception:", var32);
                return;
            } finally {
                TCPTransport.closeSocket(this.socket);
            }

        }


  //具体调用一步一步我们找到了 Transport 的 serviceCall()方法，这个方法是关键 。瞻仰一下主要的代码 ， 到ObjectTable.getTarget()为止做的事情是从 socket 流中获取 ObjId，并通过 ObjId 和 Transport 对象获取 Target 对象，这里的 Target 对象已经是服务端的对象。再借由 Target的派发器 Dispatcher，传入参数服务实现和请求对象RemoteCall，将请求派发给服务端那个真正提供服务的RegistryImpl 的 lookUp()方法，这就是 Skeleton 移交给具体实现的过程了，Skeleton 负责底层的操作。
  void handleMessages(Connection var1, boolean var2) {
        int var3 = this.getEndpoint().getPort();

        try {
            DataInputStream var4 = new DataInputStream(var1.getInputStream());

            do {
                int var5 = var4.read();
                if (var5 == -1) {
                    if (tcpLog.isLoggable(Log.BRIEF)) {
                        tcpLog.log(Log.BRIEF, "(port " + var3 + ") connection closed");
                    }
                    break;
                }

                if (tcpLog.isLoggable(Log.BRIEF)) {
                    tcpLog.log(Log.BRIEF, "(port " + var3 + ") op = " + var5);
                }

                switch(var5) {
                case 80://调动此处
                    StreamRemoteCall var6 = new StreamRemoteCall(var1);
                    if (!this.serviceCall(var6)) {
                        return;
                    }
                    break;
                case 81:
                case 83:
                default:
                    throw new IOException("unknown transport op " + var5);
                case 82:
                    DataOutputStream var7 = new DataOutputStream(var1.getOutputStream());
                    var7.writeByte(83);
                    var1.releaseOutputStream();
                    break;
                case 84:
                    DGCAckHandler.received(UID.read(var4));
                }
            } while(var2);
        } catch (IOException var17) {
            if (tcpLog.isLoggable(Log.BRIEF)) {
                tcpLog.log(Log.BRIEF, "(port " + var3 + ") exception: ", var17);
            }
        } finally {
            try {
                var1.close();
            } catch (IOException var16) {
                ;
            }

        }

    }  

    //具体实现

    public boolean serviceCall(final RemoteCall var1) {
        try {
            ObjID var39;
            try {
                var39 = ObjID.read(var1.getInputStream());
            } catch (IOException var33) {
                throw new MarshalException("unable to read objID", var33);
            }

            Transport var40 = var39.equals(dgcID) ? null : this;
            Target var5 = ObjectTable.getTarget(new ObjectEndpoint(var39, var40));
            final Remote var37;
            if (var5 != null && (var37 = var5.getImpl()) != null) {
                final Dispatcher var6 = var5.getDispatcher();
                var5.incrementCallCount();

                boolean var8;
                try {
                    transportLog.log(Log.VERBOSE, "call dispatcher");
                    final AccessControlContext var7 = var5.getAccessControlContext();
                    ClassLoader var41 = var5.getContextClassLoader();
                    ClassLoader var9 = Thread.currentThread().getContextClassLoader();

                    try {
                        setContextClassLoader(var41);
                        currentTransport.set(this);

                        try {
                            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                                public Void run() throws IOException {
                                    Transport.this.checkAcceptPermission(var7);
                                    var6.dispatch(var37, var1);//分发调用
                                    return null;
                                }
                            }, var7);
                            return true;
                        } catch (PrivilegedActionException var31) {
                            throw (IOException)var31.getException();
                        }
                    } finally {
                        setContextClassLoader(var9);
                        currentTransport.set((Object)null);
                    }
                } catch (IOException var34) {
                    transportLog.log(Log.BRIEF, "exception thrown by dispatcher: ", var34);
                    var8 = false;
                } finally {
                    var5.decrementCallCount();
                }

                return var8;
            }

            throw new NoSuchObjectException("no such object in table");
        } catch (RemoteException var36) {
            RemoteException var2 = var36;
            if (UnicastServerRef.callLog.isLoggable(Log.BRIEF)) {
                String var3 = "";

                try {
                    var3 = "[" + RemoteServer.getClientHost() + "] ";
                } catch (ServerNotActiveException var30) {
                    ;
                }

                String var4 = var3 + "exception: ";
                UnicastServerRef.callLog.log(Log.BRIEF, var4, var36);
            }

            try {
                ObjectOutput var38 = var1.getResultStream(false);
                UnicastServerRef.clearStackTraces(var2);
                var38.writeObject(var2);
                var1.releaseOutputStream();
            } catch (IOException var29) {
                transportLog.log(Log.BRIEF, "exception thrown marshalling exception: ", var29);
                return false;
            }
        }

        return true;
    }    



    public void dispatch(Remote var1, RemoteCall var2) throws IOException {
        try {
            long var4;
            ObjectInput var40;
            try {
                var40 = var2.getInputStream();
                int var3 = var40.readInt();
                if (var3 >= 0) {
                    if (this.skel != null) {
                        this.oldDispatch(var1, var2, var3);
                        return;
                    }

                    throw new UnmarshalException("skeleton class not found but required for client version");
                }

                var4 = var40.readLong();
            } catch (Exception var36) {
                throw new UnmarshalException("error unmarshalling call header", var36);
            }

            MarshalInputStream var39 = (MarshalInputStream)var40;
            var39.skipDefaultResolveClass();
            Method var8 = (Method)this.hashToMethod_Map.get(var4);
            if (var8 == null) {
                throw new UnmarshalException("unrecognized method hash: method not supported by remote object");
            }

            this.logCall(var1, var8);
            Class[] var9 = var8.getParameterTypes();
            Object[] var10 = new Object[var9.length];

            try {
                this.unmarshalCustomCallData(var40);

                for(int var11 = 0; var11 < var9.length; ++var11) {
                    var10[var11] = unmarshalValue(var9[var11], var40);
                }
            } catch (IOException var33) {
                throw new UnmarshalException("error unmarshalling arguments", var33);
            } catch (ClassNotFoundException var34) {
                throw new UnmarshalException("error unmarshalling arguments", var34);
            } finally {
                var2.releaseInputStream();
            }

            Object var41;
            try {
                var41 = var8.invoke(var1, var10);
            } catch (InvocationTargetException var32) {
                throw var32.getTargetException();
            }

            try {
                ObjectOutput var12 = var2.getResultStream(true);
                Class var13 = var8.getReturnType();
                if (var13 != Void.TYPE) {
                    marshalValue(var13, var41, var12);
                }
            } catch (IOException var31) {
                throw new MarshalException("error marshalling return", var31);
            }
        } catch (Throwable var37) {
            Object var6 = var37;
            this.logCallException(var37);
            ObjectOutput var7 = var2.getResultStream(false);
            if (var37 instanceof Error) {
                var6 = new ServerError("Error occurred in server thread", (Error)var37);
            } else if (var37 instanceof RemoteException) {
                var6 = new ServerException("RemoteException occurred in server thread", (Exception)var37);
            }

            if (suppressStackTraces) {
                clearStackTraces((Throwable)var6);
            }

            var7.writeObject(var6);
        } finally {
            var2.releaseInputStream();
            var2.releaseOutputStream();
        }

    }
```

所以客户端通过

```java

IHelloService helloService=
                (IHelloService)Naming.lookup("rmi://127.0.0.1/Hello");

```

先会创建一个 RegistryImpl_Stub 的代理类，通过这个代理类进行 socket 网络请求，将 lookup 发送到服务端，服务端通过接收到请求以后，通过服务端的 RegistryImpl_Stub（Skeleton），执行 RegistryImpl 的 lookUp。而服务端的RegistryImpl 返回的就是服务端的 HeloServiceImpl 的实现类;

```java

//RegistryImpl.class
public Remote lookup(String var1) throws RemoteException, NotBoundException {
        Hashtable var2 = this.bindings;
        synchronized(this.bindings) {
            Remote var3 = (Remote)this.bindings.get(var1);
            if (var3 == null) {
                throw new NotBoundException(var1);
            } else {
                return var3;
            }
        }
    }
```

客户端获取通过lookUp() 查询获得的客户端HelloServiceImpl 的 Stub 对象

  客户端通过 Lookup 查询获得的是客户端HelloServiceImpl 的 Stub 对象（这一块我们看不到，因为这块由 Skeleton 为我们屏蔽了），然后后续的处理仍然是通过 HelloServiceImpl_Stub 代理对象通过 socket 网络请求到服务端，通过服务端的HelloServiceImpl_Stub(Skeleton) 进行代理，将请求通过Dispatcher 转发到对应的服务端方法获得结果以后再次通过socket 把结果返回到客户端；

## RMI到底做了什么

  实际上我们看到的应该是有两个代理类一个是 RegistryImpl的代理类和我们HelloServiceImpl 的代理类。

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/rmido.png)

  一定要说明，在 RMI Client 实施正式的 RMI 调用前，它必须通过 LocateRegistry 或者 Naming 方式到 RMI 注册表寻找要调用的 RMI 注册信息。找到 RMI 事务注册信息后，Client 会从 RMI 注册表获取这个 RMI Remote Service 的Stub 信息。这个过程成功后，RMI Client 才能开始正式的调用过程。

  另外要说明的是 RMI Client 正式调用过程，也不是由 RMI Client 直接访问 Remote Service，而是由客户端获取的Stub 作为 RMI Client 的代理访问 Remote Service 的代理Skeleton。

  也就是说真实的请求调用是在 Stub-Skeleton 之间进行的。Registry 并不参与具体的 Stub-Skeleton 的调用过程，只负责记录“哪个服务名”使用哪一个 Stub，并在 Remote Client 询问它时将这个 Stub 拿给 Client（如果没有就会报错）。
