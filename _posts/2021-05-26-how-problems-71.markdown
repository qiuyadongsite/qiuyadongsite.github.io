---
layout: post
title:  排序一
date:   2021-05-26 20:53:12 +08:00
category: 算法
tags: 排序
comments: true
---

* content
{:toc}

生如蝼蚁，当立鸿鹄之志。命如薄纸，却有不屈之心！





## 概念

算法：有穷性、输入、输出、确切性、可行性

要素：算术运算、逻辑运算、关系运算、数据传输（输入输出、赋值）

优劣：时间复杂度、空间复杂度、正确性、可读性、健壮性

时间复杂度：O(N^2),O(N),O(NLogN),O(LogN)，O(1)

算法分析方法：
递归法（汉诺塔）、穷举法（暴力破解法）、贪心算法（性价比最高的）、
分治法（先小后大）、动态规划法（导弹拦截）、迭代法（超能生的兔子）、
回溯法（八皇后）

## 排序算法

- 选择排序

`简单选择排序`：每次从后边i后边找到最小的跟i进行交换，时间复杂度n*(n-1)/2优于冒泡排序

```java

public void sortParam(int[] a) {
       for(int i=a.length-1;i>=0;i--){
           int min=choice(a,i);
           exchangeNum(a,i,min);
       }
   }

   private int choice(int[] a, int index) {
       int ret=index;
       int min=a[index];
       for(int i=0;i<=index;i++){
           if(a[i]<min){
               ret=i;
               min=a[i];
           }
       }
       return ret;
   }


```

`堆排序`：构建大根堆，把首元素放最后，依次类推

```java

public void sortParam(int[] a) {
        buildMaxHeep(a);//建一个大堆
        for(int i=a.length-1;i>=1;i--){
            exchangeNum(a,0,i);
            maxHeep(a,0,i);
        }
    }

    private void buildMaxHeep(int[] a) {

        int half=(a.length-1)/2;
        for(int i=half;i>=0;i--){
            maxHeep(a,i,a.length);
        }
    }

    private void maxHeep(int[] a, int i, int length) {
        int left=2*i+1;
        int right=2*i+2;
        int largest=i;
        if(left<length&&a[left]>a[i]){
            largest=left;
        }
        if(right<length&&a[right]>a[largest]){
            largest=right;
        }

        if(largest!=i){
            exchangeNum(a,i,largest);
            maxHeep(a,largest,length);
        }

    }

```

- 插入排序

`直接插入排序`：把后边的数字插入前面有序的队列中

```java

public void sortParam(int[] a) {
       for(int i=1;i<a.length;i++){
           int temp=a[i];
           int index=i;
           for(int j=i-1;j>=0;j--){
               if(a[j]>temp){
                   a[j+1]=a[j];
                   index=j;
               }
           }
           if(i!=index){
               a[index]=temp;
           }
       }
   }

```

`二分插入排序`：不是一个一个进行比较插入，而是使用二分法查找需要查找的位置去插入，left<=right使用left

```java

public void sortParam(int[] a) {
       for(int i=1;i<a.length;i++){

           int left=0;
           int right=i-1;
           int temp=a[i];
           int mid;
           while(left<=right){
               mid=(left+right)/2;
               if(a[mid]<=temp){
                   left=mid+1;
               }else{
                   right=mid-1;
               }
           }

           for(int j=i-1;j>=left;j--){
               a[j+1]=a[j];
           }
           if(left!=i){
               a[left]=temp;
           }
       }
   }

```

`希尔排序`：   

```java

public void sortParam(int[] a) {
        int basic=a.length/2;

        while (basic>=1){
            for(int i=0;i<basic;i++){
                for(int j=i;j+basic<a.length;j=j+basic){
                    for(int k=j;k+basic<a.length;k=k+basic){
                        if(a[j]>a[k]){
                            exchangeNum(a,j,k);
                        }
                    }
                }
            }
            basic--;
        }
    }

```

- 交换排序

`冒泡排序`：

```java

public void sortParam(int[] a) {
        for(int i=0;i<a.length;i++){
            for(int j=i;j<a.length;j++){
                if(a[i]<a[j]){
                    exchangeNum(a,i,j);
                }
            }
        }
    }

```

`快速排序`：

思想：找到自己的位置放进行，自己左右的数组再进行快速排序，同样的思想，找基准元素，放到自己位置；

```java

public void sortParam(int[] a) {

       quickSort(a,0,a.length-1);


   }

   private void quickSort(int[] a, int low, int high) {
       if(low<high){
           int middle = getMiddle(a,low,high);
           quickSort(a, 0, middle-1);
           quickSort(a,middle+1,high);
       }
   }

   private int getMiddle(int[] a, int low, int high) {
       int ret;
       int t=a[low];
           while (low<high){
               while (low<high&&t<=a[high]){
                   high--;
               }
               a[low]=a[high];
               while (low<high&&t>=a[low]){
                   low++;
               }
               a[high]=a[low];
           }
           a[low]=t;
           return low;
   }

```

- 归并排序

思想：先二分，最后把左右两个有序的数组进行合并，递归合并；
合并时，使用临时数组，最后赋值进去；

```java

public void sortParam(int[] a) {
        mergeSort(a,0,a.length-1);

    }

    private void mergeSort(int[] a, int left, int right) {
        if(left<right){
            int half=(left+right)/2;
            mergeSort(a,left,half);
            mergeSort(a,half+1,right);
            merge(a,left,half,right);
        }
    }

    private void merge(int[] a, int left, int half, int right) {
        int ls=left;
        int rs=half+1;
        int index=left;
        int t=left;
        int[] tem=new int[a.length];

        while (ls<=half&&rs<=right){
            if (a[ls]<=a[rs]){
                tem[index++]=a[ls++];
            }else{
                tem[index++]=a[rs++];
            }

        }

        while(ls<=half){
            tem[index++]=a[ls++];
        }
        while(rs<=right){
            tem[index++]=a[rs++];
        }
        while (t<=right){
            a[t]=tem[t++];
        }

    }

```

- 基数排序

思想就是桶排序，先放个位数，再排十位数，收集完，再铺开，最后小的数再最左边，最大的数再最右边。

```java

public void sortParam(int[] a) {
       int max=a[0];
       for(int i=1;i<a.length;i++){
           if(a[i]>max){
               max=a[i];
           }
       }
       int size=1;
       int temp=max;
       while (true){
           temp=temp/10;
           if(temp==0){
               break;
           }else{
               size++;
           }
       }

       List<ArrayList<Integer>> list=new ArrayList<>();
       for(int i=0;i<10;i++){
           list.add(new ArrayList<>());
       }

       for(int i=0;i<size;i++){
           for(int j=0;j<a.length;j++){
               int v=a[j]%(int)Math.pow(10,i+1)/(int)Math.pow(10,i);
               list.get(v).add(a[j]);
           }
           int m=0;
           for(int k=0;k<10;k++){
               ArrayList<Integer> integers = list.get(k);
               while (integers.size()>0){
                   a[m++]=integers.remove(0);

               }
           }

       }

   }

```
