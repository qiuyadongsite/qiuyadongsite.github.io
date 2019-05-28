---
layout: post
title:  集合类汇总
date:   2019-05-28 21:52:12 +08:00
category: 算法
tags: 集合类
comments: true
---

* content
{:toc}























## Java集合框架

List、Queue、Set的类图：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/javaset001.png)

Map的类图

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/javaset002.png)

这里大概分析一下：

Stack（栈）、Vector、ArrayList、LinkedList、TreeSet、SortedSet、LinkedHashMap、ConcurrentHashMap、HashTable

### Vector

看看定义：

```java

public class Vector<E>
    extends AbstractList<E>
    implements List<E>, RandomAccess, Cloneable, java.io.Serializable
{
  protected Object[] elementData;//存放数据的           ---》数组

   protected int elementCount;//元素个数

   protected int capacityIncrement;//每次增长的个数

   public Vector(int initialCapacity, int capacityIncrement) {//初始化长度，并定义自增个数
       super();
       if (initialCapacity < 0)
           throw new IllegalArgumentException("Illegal Capacity: "+
                                              initialCapacity);
       this.elementData = new Object[initialCapacity];
       this.capacityIncrement = capacityIncrement;
   }
   public Vector() {//默认初始化10的大小，默认capacityIncrement=0
        this(10);
    }
    public Vector(Collection<? extends E> c) {
       elementData = c.toArray();
       elementCount = elementData.length;

       if (elementData.getClass() != Object[].class)
           elementData = Arrays.copyOf(elementData, elementCount, Object[].class);
   }

   public synchronized void copyInto(Object[] anArray) {//线程安全，将自己复制到指定数组中
        System.arraycopy(elementData, 0, anArray, 0, elementCount);
    }

    public synchronized void trimToSize() {//线程安全，降低其占用的空间。minimize the storage of a vector
       modCount++;
       int oldCapacity = elementData.length;
       if (elementCount < oldCapacity) {
           elementData = Arrays.copyOf(elementData, elementCount);
       }
   }

   public synchronized void ensureCapacity(int minCapacity) {
       if (minCapacity > 0) {
           modCount++;
           ensureCapacityHelper(minCapacity);
       }
   }

   private void grow(int minCapacity) {//增长一倍或增长一次，如果增长后大于设置值就用该值否则使用最小值，找个最大的，
        // overflow-conscious code
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + ((capacityIncrement > 0) ?
                                         capacityIncrement : oldCapacity);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    public synchronized void setSize(int newSize) {//如果新的大，则确保数组增一次选个最大的，如果小，剩余的null,大小为newsize
            modCount++;
            if (newSize > elementCount) {
                ensureCapacityHelper(newSize);
            } else {
                for (int i = newSize ; i < elementCount ; i++) {
                    elementData[i] = null;
                }
            }
            elementCount = newSize;
        }

        public synchronized int capacity() {
        return elementData.length;
    }

    public Enumeration<E> elements() {
        return new Enumeration<E>() {
            int count = 0;

            public boolean hasMoreElements() {
                return count < elementCount;
            }

            public E nextElement() {
                synchronized (Vector.this) {
                    if (count < elementCount) {
                        return elementData(count++);
                    }
                }
                throw new NoSuchElementException("Vector Enumeration");
            }
        };
    }

    // ..................

}

```

### Stack

看看定义：

```java

public
class Stack<E> extends Vector<E> {

}

```

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/javaset003.png)
