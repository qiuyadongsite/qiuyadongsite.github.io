---
layout: post
title:  ArrayList源码分析
date:   2018-12-09 20:52:12 +08:00
category: 数据结构
tags: 线性表
comments: true
---

* content
{:toc}

算法第一节，讲线性表，java对应的是ArrayList,因此这里简要介绍ArrayList源码！












## 1.ArrayList<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable
>* DEFAULT_CAPACITY = 10
* public ArrayList(Collection<? extends E> c)
* trimToSize()
* System.arraycopy(a, 0, elementData, size, numNew);
* protected void removeRange(int fromIndex, int toIndex)
* private boolean batchRemove(Collection<?> c, boolean complement)
