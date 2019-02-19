---
layout: post
title:  JavaScirpt学习笔记五
date:   2019-02-19 21:45:12 +08:00
category: 前端学习
tags: JavaScript
comments: true
---

* content
{:toc}

JavaScript一种面向对象的程序，函数的使用尤其重要。








## 函数表达式

  定义函数的方式有两种：

  一种是函数声明：

  一个重要特征就是函数声明提升（function declaration hoisting），意思是在执行代码之前会先读取函数声明。这就意味着可以把函数声明放在调用它的语句后面。

  ```js

  sayHi();
function sayHi(){
 alert("Hi!");
}

  ```

  另一种就是函数表达式:

  ```js

  //可以这样做
var sayHi;
if(condition){
 sayHi = function(){
 alert("Hi!");
 };
} else {
 sayHi = function(){
 alert("Yo!");
 };
}

  ```

- 递归

```js

function factorial(num){
 if (num <= 1){
 return 1;
 } else {
 return num * factorial(num-1);
 }
}

//以下报错
var anotherFactorial = factorial;
factorial = null;
alert(anotherFactorial(4)); //出错！

//以下不报错

function factorial(num){
 if (num <= 1){
 return 1;
 } else {
 return num * arguments.callee(num-1);
 }
}

//但在严格模式下，不能通过脚本访问 arguments.callee，访问这个属性会导致错误。不过，可以使用命名函数表达式来达成相同的结果。例如：
var factorial = (function f(num){
 if (num <= 1){
 return 1;
 } else {
 return num * f(num-1);
 }
});

//以上代码创建了一个名为 f()的命名函数表达式，然后将它赋值给变量 factorial。即便把函数赋值给了另一个变量，函数的名字 f 仍然有效，所以递归调用照样能正确完成。这种方式在严格模式和非严格模式下都行得通。

```

- 闭包

闭包是指有权访问另一个函数作用域中的变量的函数。
