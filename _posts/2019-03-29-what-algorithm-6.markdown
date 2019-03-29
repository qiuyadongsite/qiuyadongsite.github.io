---
layout: post
title:  栈-LeetCode算法题库
date:   2019-03-29 20:45:12 +08:00
category: 算法
tags: 栈
comments: true
---

* content
{:toc}

栈是非常特殊的线性表，为了更加熟悉使用，这里将在LeetCode收集栈的一些题。








## LeetCode-20-easy(有效的括号)

给定一个只包括 '('，')'，'{'，'}'，'['，']' 的字符串，判断字符串是否有效。

有效字符串需满足：

左括号必须用相同类型的右括号闭合。

左括号必须以正确的顺序闭合。

注意空字符串可被认为是有效字符串。

```java

class Solution12 {

    public Map<Character,Character> charMap=new HashMap<>();
    public Solution12(){
        charMap.put(')','(');
        charMap.put('}','{');
        charMap.put(']','[');
    }
    public boolean isValid(String s){
        Stack<Character> stack= new Stack<>();

        for(int i=0;i<s.length();i++){
            char onec=s.charAt(i);
            if(charMap.containsKey(onec)){
                char topchar=stack.empty()?'#':stack.pop();
                if(topchar!= charMap.get(onec)){
                    return false;
                }
            }else{
                stack.push(onec);
            }
        }
        return stack.empty();
    }
}

```
