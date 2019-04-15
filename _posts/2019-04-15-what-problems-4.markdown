---
layout: post
title:  JAVA-HashMap
date:   2019-04-15 21:52:12 +08:00
category: 疑难点
tags: JAVASE
comments: true
---

* content
{:toc}

随着JDK（Java Developmet Kit）版本的更新，JDK1.8对HashMap底层的实现进行了优化，例如引入红黑树的数据结构和扩容的优化等。













## HashMap的概述

### HashMap的数据结构

HashMap的内存结构和原理，以及线程安全都是面试的热点问题。Java中的数据结构基本可以用数组+链表的解决。

数组的优缺点:通过下标索引方便查找，但是在数组中插入或删除一个元素比较困难。

链表的优缺点:由于在链表中查找一个元素需要以遍历链表的方式去查找，而插入，删除快速。因此链表适合快速插入和删除的场景，不利于查找。

而HashMap就是综合了上述的两种数据结构的优点，HashMap由Entry数组+链表组成，如下图所示:

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/hasmap003.png)

从上图我们可以发现HashMap是由Entry数组+链表组成的，一个长度为16的数组中，每个元素存储的是一个链表的头结点。那么这些元素是按照什么样的规则存储到数组中呢。一般情况是通过hash(key)%len获得，也就是元素的key的哈希值对数组长度取模得到。比如上述哈希表中，12%16=12,28%16=12,108%16=12,140%16=12。所以12、28、108以及140都存储在数组下标为12的位置。

### HashMap的存取实现简单说明

#### HashMap put方法实现

首先HashMap里面实现一个静态内部类Entry，其重要的属性有 key , value, next，从属性key,value我们就能很明显的看出来Entry就是HashMap键值对实现的一个基础bean，我们上面说到HashMap的基础就是一个线性数组，这个数组就是Entry[]，Map里面的内容都保存在Entry[]里面。

```java

static class Entry<K,V> implements Map.Entry<K,V> {
      final K key;//Key-value结构的key
      V value;//存储值
      Entry<K,V> next;//指向下一个链表节点
      final int hash;//哈希值
}
```

既然是线性数组，为什么能随机存取？这里HashMap用了一个小算法，大致是这样实现：

```java

//存储时:
// 这个hashCode方法这里不详述,只要理解每个key的hash是一个固定的int值
int hash = key.hashCode();
int index = hash % Entry[].length;
Entry[index] = value;
//取值时:
int hash = key.hashCode();
int index = hash % Entry[].length;
return Entry[index];

```

到这里我们轻松的理解了HashMap通过键值对实现存取的基本原理

#### 疑问：如果两个key通过hash%Entry[].length得到的index相同，会不会有覆盖的危险？

　　这里HashMap里面用到链式数据结构的一个概念。上面我们提到过Entry类里面有一个next属性，作用是指向下一个Entry。打个比方， 第一个键值对A进来，通过计算其key的hash得到的index=0，记做:Entry[0] = A。一会后又进来一个键值对B，通过计算其index也等于0，现在怎么办？HashMap会这样做:B.next = A,Entry[0] = B,如果又进来C,index也等于0,那么C.next = B,Entry[0] = C；这样我们发现index=0的地方其实存取了A,B,C三个键值对,他们通过next这个属性链接在一起。所以疑问不用担心。也就是说数组中存储的是最后插入的元素。到这里为止，HashMap的大致实现，我们应该已经清楚了。

　　当然HashMap里面也包含一些优化方面的实现，这里也说一下。比如：Entry[]的长度一定后，随着map里面数据的越来越长，这样同一个index的链就会很长，会不会影响性能？HashMap里面设置一个因素（也称为因子），随着map的size越来越大，Entry[]会以一定的规则加长长度。 　

## HashMap非线程安全

### HashMap进行Put操作　 　　

#### Jdk8以下HashMap的Put操作

put操作主要是判空，对key的hashcode执行一次HashMap自己的哈希函数，得到bucketindex位置，还有对重复key的覆盖操作。

在HashMap做put操作的时候会调用到以下的方法，addEntry和createEntry

```java

public V put(K key, V value) {
        if (key == null)
            return putForNullKey(value);
        //得到key的hashcode，同时再做一次hash操作
        int hash = hash(key.hashCode());
        //对数组长度取余，决定下标位置
        int i = indexFor(hash, table.length);
        /**
          * 首先找到数组下标处的链表结点，
          * 判断key对一个的hash值是否已经存在，如果存在将其替换为新的value
          */
        for (Entry<K,V> e = table[i]; e != null; e = e.next) {
            Object k;
            //Hash碰撞的解决
            if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
                V oldValue = e.value;
                e.value = value;
                e.recordAccess(this);
                return oldValue;
            }
        }

        modCount++;
        addEntry(hash, key, value, i);
        return null;
    }

```

涉及到的几个方法：

```java

static int hash(int h) {
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

static int indexFor(int h, int length) {
        return h & (length-1);
    }

```

现在假如A线程和B线程同时进入addEntry，然后计算出了相同的哈希值对应了相同的数组位置，因为此时该位置还没数据，然后对同一个数组位置调用createEntry，两个线程会同时得到现在的头结点，然后A写入新的头结点之后，B也写入新的头结点，那B的写入操作就会覆盖A的写入操作造成A的写入操作丢失。

### jdk8中HashMap的Put操作

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/hasmap004.png)

①.判断键值对数组table[i]是否为空或为null，否则执行resize()进行扩容；

②.根据键值key计算hash值得到插入的数组索引i，如果table[i]==null，直接新建节点添加，转向⑥，如果table[i]不为空，转向③；

③.判断table[i]的首个元素是否和key一样，如果相同直接覆盖value，否则转向④，这里的相同指的是hashCode以及equals；

④.判断table[i] 是否为treeNode，即table[i] 是否是红黑树，如果是红黑树，则直接在树中插入键值对，否则转向⑤；

⑤.遍历table[i]，判断链表长度是否大于8，大于8的话把链表转换为红黑树，在红黑树中执行插入操作，否则进行链表的插入操作；遍历过程中若发现key已经存在直接覆盖value即可；

⑥.插入成功后，判断实际存在的键值对数量size是否超多了最大容量threshold，如果超过，进行扩容。

JDK1.8HashMap的put方法源码如下:

```java

public V put(K key, V value) {
      // 对key的hashCode()做hash
      return putVal(hash(key), key, value, false, true);
  }

  final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                 boolean evict) {
     Node<K,V>[] tab; Node<K,V> p; int n, i;
      // 步骤①：tab为空则创建
     if ((tab = table) == null || (n = tab.length) == 0)
         n = (tab = resize()).length;
     // 步骤②：计算index，并对null做处理
     if ((p = tab[i = (n - 1) & hash]) == null)
         tab[i] = newNode(hash, key, value, null);
     else {
         Node<K,V> e; K k;
         // 步骤③：节点key存在，直接覆盖value
         if (p.hash == hash &&
             ((k = p.key) == key || (key != null && key.equals(k))))
             e = p;
         // 步骤④：判断该链为红黑树
         else if (p instanceof TreeNode)
             e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
         // 步骤⑤：该链为链表
         else {
             for (int binCount = 0; ; ++binCount) {
                 if ((e = p.next) == null) {
                     p.next = newNode(hash, key,value,null);
                        //链表长度大于8转换为红黑树进行处理
                     if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st  
                         treeifyBin(tab, hash);
                     break;
                 }
                    // key已经存在直接覆盖value
                 if (e.hash == hash &&
                     ((k = e.key) == key || (key != null && key.equals(k))))
                            break;
                 p = e;
             }
         }

         if (e != null) { // existing mapping for key
             V oldValue = e.value;
             if (!onlyIfAbsent || oldValue == null)
                 e.value = value;
             afterNodeAccess(e);
             return oldValue;
         }
     }
     ++modCount;
     // 步骤⑥：超过最大容量 就扩容
     if (++size > threshold)
         resize();
     afterNodeInsertion(evict);
     return null;
}

```

#### HashMap进行Get操作

```java

public V get(Object key) {
        if (key == null)
            return getForNullKey();
        int hash = hash(key.hashCode());
        /**
          * 先定位到数组元素，再遍历该元素处的链表
          * 判断的条件是key的hash值相同，并且链表的存储的key值和传入的key值相同
          */
        for (Entry<K,V> e = table[indexFor(hash, table.length)];e != null;e = e.next) {
            Object k;
            if (e.hash == hash && ((k = e.key) == key || key.equals(k)))
                return e.value;
        }
        return null;
      }

```

看一下链表的结点数据结构，保存了四个字段，包括key，value，key对应的hash值以及链表的下一个节点：

```java

static class Entry<K,V> implements Map.Entry<K,V> {
      final K key;//Key-value结构的key
      V value;//存储值
      Entry<K,V> next;//指向下一个链表节点
      final int hash;//哈希值
}

```

#### HashMap扩容的时候

扩容(resize)就是重新计算容量，向HashMap对象里不停的添加元素，而HashMap对象内部的数组无法装载更多的元素时，对象就需要扩大数组的长度，以便能装入更多的元素。当然Java里的数组是无法自动扩容的，方法是使用一个新的数组代替已有的容量小的数组，就像我们用一个小桶装水，如果想装更多的水，就得换大水桶。

还是上面那个addEntry方法中，有个扩容的操作，这个操作会新生成一个新的容量的数组，然后对原数组的所有键值对重新进行计算和写入新的数组，之后指向新生成的数组。来看一下扩容的源码：

```java

//用新的容量来给table扩容  
void resize(int newCapacity) {  
    Entry[] oldTable = table; //引用扩容前的Entry数组
    int oldCapacity = oldTable.length; //保存old capacity  
    // 如果旧的容量已经是系统默认最大容量了(扩容前的数组大小如果已经达到最大(2^30)了 )，那么将阈值设置成整形的最大值，退出 ,  
    if (oldCapacity == MAXIMUM_CAPACITY) {  
        threshold = Integer.MAX_VALUE;  
        return;  
    }  
    //初始化一个新的Entry数组  
    Entry[] newTable = new Entry[newCapacity];  
    //将数据转移到新的Entry数组里
    transfer(newTable, initHashSeedAsNeeded(newCapacity));  
    //HashMap的table属性引用新的Entry数组
    table = newTable;  
    //设置阈值  
    threshold = (int)Math.min(newCapacity * loadFactor, MAXIMUM_CAPACITY + 1);  
}

```

这里就是使用一个容量更大的数组来代替已有的容量小的数组，transfer()方法将原有Entry数组的元素拷贝到新的Entry数组里。

那么问题来了，当多个线程同时进来，检测到总数量超过门限值的时候就会同时调用resize操作，各自生成新的数组并rehash后赋给该map底层的数组table，结果最终只有最后一个线程生成的新数组被赋给table变量，其他线程的均会丢失。而且当某些线程已经完成赋值而其他线程刚开始的时候，就会用已经被赋值的table作为原始数组，这样也会有问题。所以在扩容操作的时候也有可能会引起一些并发的问题。

#### 删除数据的时候

```java

//根据指定的key删除Entry，返回对应的value  
public V remove(Object key) {  
    Entry<K,V> e = removeEntryForKey(key);  
    return (e == null ? null : e.value);  
}  
//根据指定的key，删除Entry,并返回对应的value  
final Entry<K,V> removeEntryForKey(Object key) {  
    if (size == 0) {  
        return null;  
    }  
    int hash = (key == null) ? 0 : hash(key);  
    int i = indexFor(hash, table.length);  
    Entry<K,V> prev = table[i];  
    Entry<K,V> e = prev;  
    while (e != null) {  
        Entry<K,V> next = e.next;  
        Object k;  
        if (e.hash == hash &&  
            ((k = e.key) == key || (key != null && key.equals(k)))) {  
            modCount++;  
            size--;  
            if (prev == e) //如果删除的是table中的第一项的引用  
                table[i] = next;//直接将第一项中的next的引用存入table[i]中  
            else  
                prev.next = next; //否则将table[i]中当前Entry的前一个Entry中的next置为当前Entry的next  
            e.recordRemoval(this);  
            return e;  
        }  
        prev = e;  
        e = next;  
    }  
    return e;  
}

```

删除这一块可能会出现两种线程安全问题，第一种是一个线程判断得到了指定的数组位置i并进入了循环，此时，另一个线程也在同样的位置已经删掉了i位置的那个数据了，然后第一个线程那边就没了。但是删除的话，没了倒问题不大。

　　再看另一种情况，当多个线程同时操作同一个数组位置的时候，也都会先取得现在状态下该位置存储的头结点，然后各自去进行计算操作，之后再把结果写会到该数组位置去，其实写回的时候可能其他的线程已经就把这个位置给修改过了，就会覆盖其他线程的修改。 　　

总之HashMap是非线程安全的，在高并发的场合使用的话，要用Collections.synchronizedMap进行包装一下。
