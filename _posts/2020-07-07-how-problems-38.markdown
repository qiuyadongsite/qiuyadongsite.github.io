---
layout: post
title:  转换数组
date:   2020-07-07 21:53:12 +08:00
category: 算法
tags: 栈
comments: true
---

* content
{:toc}




有两个数组：
1： 9、5、1、4、3            
2: 5、9、3 、4、1

如果可以通过压站出站1数组转换成2数组，则返回true;


```java
public static boolean validate(int[] resources,int[] target){
        Stack<Integer> stack=new Stack<>();
        int tIndex=0;
        int k=0;
        for(int i=0;i<resources.length;){
            stack.push(resources[i]);
            int temp=stack.peek();
            for(int j=tIndex;j<target.length;j++){
                if(temp==target[j]){
                    k=j;
                    break;
                }else if(i<resources.length-1){
                    stack.push(resources[++i]);
                }
            }
            for(int m=tIndex;m<=k;m++){
                if(stack.size()>0){
                    int tt=stack.pop();
                    if(tt!=target[m]){
                        return false;
                    }
                }
            }
            if(stack.size()>0){
                return false;
            }
            i=i+1;
            tIndex=k+1;
        }
        return true;
    }
```
