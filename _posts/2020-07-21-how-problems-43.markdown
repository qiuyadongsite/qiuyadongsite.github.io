---
layout: post
title:  RxJava官网学习
date:   2020-07-21 21:53:12 +08:00
category: 技术官网
tags: RxJava
comments: true
---

* content
{:toc}

普天皆灭焰，匝地尽藏烟。不知何处火，来就客心然。





### 简单理解

#### RxJava概念

其实就是操作一个生产线上的产品，当产品到了不同的包装环节，都有操作员对其进行查看和包装，不同的制作环节将引起不同的工作流程；

它扩展了观察者模式来支持数据/事件序列，并添加了运算符，使开发者可以声明性地组合序列，同时抽象出对低级线程、同步、线程安全性和并发数据结构等事物的观察。

#### RxJava 1.x 基本用法

- RxJava 1.x

```java

      //创建了一个Observable对象，传入的参数是OnSubscribe接口，故而在参数内实现其接口的call 方法
      Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
                 //在创建Observable中实现OnSubscribe接口的call 方法中，注意其参数为Subscriber订阅者，调用订阅者的onNext方法传入数据，再调用onCompleted 方法。
                  @Override
                  public void call(Subscriber<? super String> subscriber) {
                      if (!subscriber.isUnsubscribed()) {
                          subscriber.onNext("test");
                          subscriber.onCompleted();
                      }
                  }
              });
      //调用Observable对象的订阅subscribe 事件方法，其参数传入Observer接口，故而在参数内实现其接口的onCompleted()、 onError(Throwable e)、onNext(T t)方法。
              Subscription subscription = observable.subscribe(new Observer<String>() {
                  @Override
                  public void onCompleted() {
                      System.out.println("onCompleted");
                  }
                  @Override
                  public void onError(Throwable e) {
                  }
                  @Override
                  public void onNext(String s) {
                      System.out.println("onNext:" + s);
                  }
              });

```

  - Subscription接口

    Subscription是Observable调用subscribe订阅事件方法后返回的一个接口，其内容也很简单，两个方法，一个解除订阅的unsubscribe()方法，一个是判断是否解除的isUnsubscribed() 方法；

```java    

        public interface Subscription {
             void unsubscribe();
             boolean isUnsubscribed();
        }

```

  - Observer 接口

    Observer是Observable调用subscribe订阅事件方法中传入的参数，也是一个接口，三个待实现的方法，分别是回调完成方法onCompleted()、 错误处理方法onError(Throwable e)、数据接收方法onNext(T t)。

```java

        public interface Observer<T> {
            void onCompleted();
            void onError(Throwable e);
             void onNext(T t);
        }

```

  - Subscriber 抽象类

    在创建Observable时，需要给create传入参数Observable.OnSubscribe接口，并实现其接口的call方法，此方法的参数类型就是Subscriber，而开发者也是在此方法中调用onNext、onComplete，因此可以推测Subscriber必然实现了Observer 接口。
    仔细查看源码，确实如此，它还实现了Subscription 接口中的unsubscribe()、isUnsubscribed()两个方法，简单做了封装，但并未详细实现Observer 接口中的三个方法，因此只是一个抽象类。

```java

        public abstract class Subscriber<T> implements Observer<T>, Subscription {
        }

```

  - OnSubscriber 接口

    OnSubscriber是在创建Observable时传入create 方法中的参数类型，也是一个接口。此接口又是继承于Action1 接口，Action1 接口中有一个未实现的call方法，而Action1 接口又继承于Action接口，Action接口是一个空实现，最后它又继承于Function接口，Function接口也是一个空实现。

```
        OnSubscriber -> Action1 -> Action -> Fcuntion
```

  - Observable 工具类

    - create方法

      首先查看Observable的静态创建create方法，可见其只是多做了一层封装，new这个对象时，将参数onSubscribe传入构造方法中，而RxJavaHooks.onCreate(f)也只是多做了一个判断，最终返回的还是onSubscribe。

```java

          public static <T> Observable<T> create(OnSubscribe<T> f) {
                  return new Observable<T>(RxJavaHooks.onCreate(f));
              }

```

    - subscribe方法

```java

          public final Subscription subscribe(final Observer<? super T> observer) {
                  if (observer instanceof Subscriber) {
                      return subscribe((Subscriber<? super T>)observer);
                  }
                  if (observer == null) {
                      throw new NullPointerException("observer is null");
                  }
                  return subscribe(new ObserverSubscriber<T>(observer));
              }

```

      这里使用Subscriber将我们传入的observer接口做了一层简单的封装，来查看ObserverSubscriber的具体实现：

```java

          public final class ObserverSubscriber<T> extends Subscriber<T> {
              final Observer<? super T> observer;

              public ObserverSubscriber(Observer<? super T> observer) {
                  this.observer = observer;
              }

              @Override
              public void onNext(T t) {
                  observer.onNext(t);
              }

              @Override
              public void onError(Throwable e) {
                  observer.onError(e);
              }

              @Override
              public void onCompleted() {
                  observer.onCompleted();
              }
          }

```

      这里使用Subscriber将我们传入的observer接口做了一层简单的封装。还是回到它重载的另一个subscribe 方法:

```java

          public final Subscription subscribe(Subscriber<? super T> subscriber) {
                  return Observable.subscribe(subscriber, this);
              }

```

      由以上代码可知，从调用的参数为observer接口的subscribe 方法内做了一层封装，调用了参数为subscriber抽象类的subscribe 方法，最终调用的是参数为subscriber、observable的静态subscribe 方法。

```java

           RxJavaHooks.onObservableStart(observable, observable.onSubscribe).call(subscriber);
           return RxJavaHooks.onObservableReturn(subscriber);

```

      第一行代码调用OnSubscriber接口的call方法，这意味着我们创建Observable时实现的call方法回被调用，那么call方法中对数据传递、标志完成的操作会执行，即实现的Observer接口中的onNext方法中接收到数据，onComplete()方法也被调用。最后返回Subscription。

#### RxJava 1.x 理解

- Observable（被观察者）

  - 通过Observable创建一个可观察序列

  - 通过subscribe去注册一个观察者

- Observer（观察者）

  - 用于接收数据的观察者

  - 作为Observable的subscribe方法的参数

- Subscription(订阅者)

  - 订阅，用于描述观察者和被观察者之间的关系

  - 用于取消订阅和获取当前订阅的状态

- OnSubscribe

  - 当订阅时会发送此接口的调用

  - 在Observable内部，实际作用是向订阅者发布数据

- Subscriber

  - 实现了Observer和Subscription接口

#### RxJava 2.x基本用法

- Rx2.x

```java

       Observable.create(new ObservableOnSubscribe<String>() {
                          @Override
                          public void subscribe(ObservableEmitter<String> e) throws Exception {
                              if (!e.isDisposed()) {
                                  e.onNext("test");
                                  e.onComplete();
                              }
                          }
                      }).subscribe(new Observer<String>() {
                          @Override
                          public void onSubscribe(Disposable d) {
                              System.out.println("onSubscribe");
                          }
                          @Override
                          public void onNext(String value) {
                              System.out.println("onNext:" + value);
                          }
                          @Override
                          public void onError(Throwable e) {

                          }
                          @Override
                          public void onComplete() {
                              System.out.println("onCompleted");
                          }
                      });

```

  - 在通过调用Observable的create() 方法创建对象时，传入的参数是ObservableOnSubscribe接口，实现的是其接口的subscribe 方法，方法内的参数是ObservableEmitter，不再是1.x版本的OnSubscribe接口（call 方法）。

  - 在调用Observable对象的订阅subscribe 事件方法，其参数传入的Observer接口，多了一个需要实现onSubscribe(Disposable d)方法，方法内的参数是Disposable。

- 与RxJava 1.x区别

  - Observer接口

    多了一个void onSubscribe(Disposable d);方法，用于观察者取消事件订阅，来查看Disposable接口组成：（注意：2.x版本新增的Disposable可以做到切断订阅事件的操作，让观察者Observer不再接收上游事件，避免内存泄漏）

```java

        public interface Disposable {
            void dispose();
            boolean isDisposed();
        }

```

    接口中两个方法，一个dispose方法，另一个事检测是否dispose方法，其结构与Subscription类似。

  - ObservableOnSubscribe接口

    是创建Observable时传入的接口参数，在2.x版本中单独独立出来了。为观察者提供了取消订阅连接的功能，该接口中的subscribe方法用于接收ObservableEmitter的实例，该实例允许用安全的方式取消事件。

```java

        public interface ObservableOnSubscribe<T> {
            void subscribe(@NonNull ObservableEmitter<T> emitter) throws Exception;
        }

```

  - ObservableEmitter接口

    前两个接口我们一直都在强调2.x版本新增的Disposable切断订阅事件，使得观察者不再接收上游事件的功能，可预先此接口也是为它所用，作用是设置Emitter的disposable和cancellable

```java

        public interface ObservableEmitter<T> extends Emitter<T> {
             void setDisposable(@Nullable Disposable d);
            void setCancellable(@Nullable Cancellable c);
             boolean isDisposed();
             boolean tryOnError(@NonNull Throwable t);
        }

```

    继续查看Emitter接口的组成，会发现其中包含的三个方法竟然与Observer接口完全相同，其中缘由后续讲解。

```java

        public interface Emitter<T> {
            void onNext(@NonNull T value);
            void onError(@NonNull Throwable error);
             void onComplete();
        }

```

  - Observable类

    - create方法

```java

            public static <T> Observable<T> create(ObservableOnSubscribe<T> source) {
                  ObjectHelper.requireNonNull(source, "source is null");
                  return RxJavaPlugins.onAssembly(new ObservableCreate<T>(source));
              }

```

      此方法中可以得出两个信息，第一个是调用了RxJavaPlugins的静态onAssembly方法，第二个是传入此方法的参数，将ObservableOnSubscribe接口通过ObservableCreate做了一次封装。首先来了解onAssembly方法：

```java

           public static <T> Observable<T> onAssembly(@NonNull Observable<T> source) {
                  Function<? super Observable, ? extends Observable> f = onObservableAssembly;
                  if (f != null) {
                      return apply(f, source);
                  }
                  return source;
              }

```

      此方法中的一个关键成员变量onObservableAssembly，它最初被赋值为null，为外界提供了set方法，因此当我们刚开始调用时f 被判断为null，直接将source返回。再来查看new ObservableCreate<T>(source) 具体构成：

```java

          public final class ObservableCreate<T> extends Observable<T> {
              final ObservableOnSubscribe<T> source;

              public ObservableCreate(ObservableOnSubscribe<T> source) {
                  this.source = source;
              }

              @Override
              protected void subscribeActual(Observer<? super T> observer) {
                  CreateEmitter<T> parent = new CreateEmitter<T>(observer);
                  observer.onSubscribe(parent);

                  try {
                      source.subscribe(parent);
                  } catch (Throwable ex) {
                      Exceptions.throwIfFatal(ex);
                      parent.onError(ex);
                  }
              }
          }

```

    - subscribe方法

      回到Observable，查看subscribe方法：

```java

          public final void subscribe(Observer<? super T> observer) {     
                      observer = RxJavaPlugins.onSubscribe(this, observer);
                      subscribeActual(observer);
          }

```

      首先查看到observer = RxJavaPlugins.onSubscribe(this, observer);，在Observable的create中也出现了RxJavaPlugins相关用法，而此处它的作用也是类似，就是将传入的参数observer返回，重点在于后面的subscribeActual(observer);，也就是刚介绍ObservableCreate实现的subscribeActual 方法。

    - 总结

      到了这里，必须强调了一下，再次回顾RxJava 1.x版本中Observable的subscribe处理，通过调用创建Observable传入的OnSubscribe接口的call方法正式触发订阅事件，后续Observe接口中onNext、onComplete方法才回被调用。而RxJava 2.x版本中的处理亦如是，只不过OnSubscribe接口换成了ObservableOnSubscribe接口，call方法换成了subscribe方法，参数由subscriber更换成了ObservableEmitter，这些变换也是RxJava 2.x的改进，新增的Disposable切断订阅事件，使得观察者不再接收上游事件的功能，来避免内存泄漏。由此可见以上处理过程，RxJava 2.x 与 1.x的区别不大。

#### RxJava 2.x理解

在以上基本元素的对比上，两个版本其实没有很大的区别，只是部分写法做了改变，2.x版本中将精华ObservableCreate独立出来，但其核心内容还是与1.x 相同，调用传进来的OnSubscribe接口中的subscribe（call）方法，并传入observe接口到其中。而2.x中还特地使用了CreateEmitter对传入的observe接口做了包装，因此我们手动调用onNext方法时，实际上就是通过observe接口调用这些方法。

- Observable

  被观察者，不支持背压

  可通过Observable创建一个可观察到序列

  通过subscribe去注册一个观察者

- Observer

  用于接收数据的观察者

  作为Observable的subscribe方法的参数

- Disposable

  和RxJava1的Subscription作用相当

  用于取消订阅和获取当前的订阅状态

- ObservableOnSubscriber

  当订阅时会出发此接口的调用

  在Observable内部，实际作用时向订阅者发布数据

- Emitter

  一个发射数据的接口，和Observer 的方法类似

  本质是对Observer和Subscribe的包装

#### 被压

- 概念

  异步环境下产生的问题：同步环境下会等待一件事处理完后再进行下一步，而异步环境下是处理完一件事，未等它得出结果接着处理下一步，在获得结果之后进行回调，再处理结果。

  发送和处理速度不统一：例如生产者生产出的产品放置到缓存队列中，供消费者消费。若生产者生产的速度大于消费者消耗的速度，则会出现缓存队列溢出的问题。

  是一种流速控制及解决策略：例如背压中的丢弃策略，一旦发现缓存队列已满，为了整个过程顺利进行，则会丢弃最新产生的产品，避免溢出，因此背压也是一种流速控制的解决策略

- 案例

```java

       Flowable.create(new FlowableOnSubscribe<String>() {
                  @Override
                  public void subscribe(FlowableEmitter<String> e) throws Exception {
                      if (!e.isCancelled()) {
                          e.onNext("Flowable test");
                          e.onComplete();
                      }
                  }
              }, BackpressureStrategy.DROP).subscribe(new Subscriber<String>() {
                  @Override
                  public void onSubscribe(Subscription s) {
                      s.request(Long.MAX_VALUE);
                      System.out.println("onSubscribe");
                  }
                  @Override
                  public void onNext(String s) {
                      System.out.println("onNext:" + s);
                  }
                  @Override
                  public void onError(Throwable t) {
                  }
                  @Override
                  public void onComplete() {
                      System.out.println("onCompleted");
                  }
              });

```

  - 首先创建通过调用Flowable的create方法创建实例，传入两个参数，第一个是OnSubscribe接口实例，并实现其subscribe方法，第二参数则是背压特有的背压策略；

  - 调用Flowable的subscribe方法，你会发现不同于之前，传入的并非是observer接口，而是Subscriber接口，实现onSubscribe、onNext、onComplete、onError方法。



### RxJava 3.0官方文档

#### 概念

- RxJava

  jvm响应式编程的扩展：就是一个lib包通过使用观察序列实现同步、基于事件的编程方式；
  它扩展了观察者模式以支持数据/事件序列，并添加了操作符，允许您以声明的方式将序列组合在一起，同时抽象出对底层线程、同步、线程安全和并发数据结构等问题的关注。

- 基础类

  - Flowable

    0 . .N流，支持反应流和被压

  - Observable

    0 . .N流，不支持被压

  - Single

    一个流，期望是一项或者一个错误

  - Completable

    一个流没有项但是是一个完成了的或者一个错误标示

  - Maybe

    一个流没有项，期望是一项或者一个错误

#### 术语

- Upstream(上游)、downstream(下游)

  RxJava中的数据流由一个源、零个或更多中间步骤组成，后面跟着一个数据消费者或组合子步骤(其中步骤负责通过某种方式消耗数据流)

```

      source.operator1().operator2().operator3().subscribe(consumer);

      source.flatMap(value -> source.operator1().operator2().operator3());

```

  如果自己是operator2，那么operator1是上游，operator3就是下游；

- Objects in motion(运行对象)

  emission, emits, item, event, signal,data和 message在Rxjava中被认为是同义词，代表对象在跟随数据量而运行；

- Backpressure（被压）

  当数据流通过异步步骤运行时，每一步可能以不同的速度执行不同的事情。

  这类步骤通常会由于临时缓冲或需要跳过/删除数据而导致内存使用量增加，为了避免这些步骤过于庞大，应用了所谓的backpressure，这是一种流控制形式，其中步骤可以表示它们准备处理多少项。这允许在某些情况下限制数据流的内存使用，通常情况下，一个步骤无法知道上游将发送多少项给它。

  在RxJava中，专用的流型类被指定为支持背压操作，而Observable类被指定为非背压操作(短序列、GUI交互等)。其他类型，Single, Maybe和Completable不支持背压，也不应该支持;总有地方可以暂时存放一些对象。

- Assembly time（装配时）

  使用各种中间操作符进行数据流的准备发生在所谓的装配时间:

- Subscription time（订阅时）

  在内部建立处理步骤链的流程上调用subscribe()时的临时状态

- Runtime（运行时）

  这是流主动发出项、错误或完成信号时的状态

- Simple background computation（简单后台计算）

  RxJava的一个常见用例是在后台线程上运行一些计算、网络请求，并在UI线程上显示结果(或错误)；

```java

      Flowable<String> source = Flowable.fromCallable(() -> {
          Thread.sleep(1000); //  imitate expensive computation
          return "Done";
      });

      Flowable<String> runBackground = source.subscribeOn(Schedulers.io());

      Flowable<String> showForeground = runBackground.observeOn(Schedulers.single());

      showForeground.subscribe(System.out::println, Throwable::printStackTrace);

      Thread.sleep(2000);

```

  通常，您可以通过subscribeOn将计算或阻塞IO移动到其他线程。一旦数据准备好了，就可以确保通过observeOn在前台或GUI线程上处理它们。

- Schedulers(调度者)

  RxJava操作符并不直接与线程或ExecutorServices一起工作，而是与所谓的调度器一起工作，这些调度器抽象出了一个统一API背后的并发源。RxJava 3提供了几个可通过调度器实用程序类访问的标准调度器。

  - Schedulers.computation()
    在后台使用固定数量的专用线程运行密集的计算工作。大多数异步操作符将此作为默认调度程序。

  - Schedulers.io()
    对一组动态变化的线程运行类I/O或阻塞操作。

  - Schedulers.single()
    以顺序和FIFO的方式在单个线程上运行工作。

  - Schedulers.trampoline()
    通常为了测试的目的，在其中一个参与线程中按顺序和FIFO的方式运行工作。

  此外，还有一个选项可以通过schedul. from(Executor)将现有的执行程序(及其子类型，如ExecutorService)包装到调度程序中。例如，可以使用它来拥有更大但仍然固定的线程池(分别与compute()和io()不同)。

- Concurrency within a flow(流的并发性)

  RxJava中的流在本质上是顺序的，被分割为可以彼此并发运行的处理阶段:

```java

      Flowable.range(1, 10)
        .observeOn(Schedulers.computation())
        .map(v -> v * v)
        .blockingSubscribe(System.out::println);

```

  示例流在计算调度程序中将数字从1平方到10，并在“主”线程(更准确地说，是blockingSubscribe的调用线程)上消耗结果。但是，lambda v -> v * v并不在此流中并行运行;它在同一个计算线程上一个接一个地接收值1到10。

- Parallel processing(并行处理)

```java

      Flowable.range(1, 10)
        .flatMap(v ->
            Flowable.just(v)
              .subscribeOn(Schedulers.computation())
              .map(w -> w * w)
        )
        .blockingSubscribe(System.out::println);

```

  实际上，RxJava中的并行性意味着运行独立的流并将它们的结果合并回单个流。操作符flatMap首先将从1到10的每个数字映射到它自己的流动，运行它们并合并计算得到的结果。

  flatMap不保证任何顺序，并且来自内部流的项可能最终交错。有可供选择的操作符:

  - concatMap

    映射并每次运行一个内部流

  - concatMapEager

    它“立即”运行所有内部流，但输出流将按照创建这些内部流的顺序。
  或者，Flowable.parallel()操作符和ParallelFlowable类型帮助实现相同的并行处理模式:

```java

      Flowable.range(1, 10)
        .parallel()
        .runOn(Schedulers.computation())
        .map(v -> v * v)
        .sequential()
        .blockingSubscribe(System.out::println);

```

- Dependent sub-flows(依赖子流)

  flatMap是一个强大的运算符，在很多情况下都有帮助。例如，给定一个返回Flowable的服务，我们想用第一个服务发出的值调用另一个服务:

```java

      Flowable<Inventory> inventorySource = warehouse.getInventoryAsync();

      inventorySource
          .flatMap(inventoryItem -> erp.getDemandAsync(inventoryItem.getId())
                  .map(demand -> "Item " + inventoryItem.getName() + " has demand " + demand))
          .subscribe(System.out::println);

```

- Continuations(延续)

  有时，当一个项可用时，人们希望对它执行一些依赖的计算。这有时被称为延续，根据应该发生什么以及所涉及的类型，可能会涉及到各种操作符来完成。

  - Dependent

    最典型的场景是给定一个值，调用另一个服务，等待并继续其结果:

```java

        service.apiCall()
        .flatMap(value -> service.anotherApiCall(value))
        .flatMap(next -> service.finalCall(next))

```

    通常情况下，后面的序列会需要来自以前映射的值。这可以通过移动外部的平面到之前的平面的内部来实现，例如:

```java

        service.apiCall()
        .flatMap(value ->
            service.anotherApiCall(value)
            .flatMap(next -> service.finalCallBoth(value, next))
        )

```

    在这里，由lambda变量章节提供的原始值将在内部平面映射中可用。

  - Non-dependent

    在其他情况下，第一个源/数据流的结果是不相关的，人们希望继续使用准独立的另一个源。在这里，flatMap也可以工作:

```java

        Observable continued = sourceObservable.flatMapSingle(ignored -> someSingleSource)
        continued.map(v -> v.toString())
          .subscribe(System.out::println, Throwable::printStackTrace);

```

    然而，在这种情况下，延续保持可观察，而不是可能更合适的单个。(因为从flatMapSingle的角度来看，sourceObservable是一个多值源，因此映射也可能导致多个值)。

    通常有一种更有表现力(也更低开销)的方法，使用Completable作为中介和它的操作符，然后继续使用其他东西:

```java

        sourceObservable
          .ignoreElements()           // returns Completable
          .andThen(someSingleSource)
          .map(v -> v.toString())

```

    sourceObservable和someSingleSource之间唯一的依赖关系是，前者应该正常完成，以便后者被消费。

  - Deferred-dependent

    有时，前一个序列和新序列之间存在隐式的数据依赖关系，由于某种原因，这些依赖关系没有通过“常规通道”传递。人们倾向于将这种延续写如下:

```java

        AtomicInteger count = new AtomicInteger();

        Observable.range(1, 10)
          .doOnNext(ignored -> count.incrementAndGet())
          .ignoreElements()
          .andThen(Single.just(count.get()))
          .subscribe(System.out::println);

```

    不幸的是，这会输出0，因为Single.just(count.get())是在数据流尚未运行时计算的。我们需要一些东西来推迟这个单一源的评估，直到运行时，当主源完成:

```java

        AtomicInteger count = new AtomicInteger();

        Observable.range(1, 10)
          .doOnNext(ignored -> count.incrementAndGet())
          .ignoreElements()
          .andThen(Single.defer(() -> Single.just(count.get())))
          .subscribe(System.out::println);

```

参考文档：

1：https://github.com/ReactiveX/RxJava

2：https://blog.csdn.net/itermeng/article/details/80139074
