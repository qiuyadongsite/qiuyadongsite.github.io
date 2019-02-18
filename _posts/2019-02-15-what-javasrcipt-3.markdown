---
layout: post
title:  JavaScirpt学习笔记三
date:   2019-02-15 23:45:12 +08:00
category: 前端学习
tags: JavaScript
comments: true
---

* content
{:toc}

JavaScript一种直译式脚本语言，是一种动态类型、弱类型、基于原型的语言，内置支持类型。








## Function 类型

  由于函数是对象，因此函数名实际上也是一个指向函数对象的指针，不会与某个函数绑定。函数通常是使用函数声明语法定义的，如下面的例子所示：
  ```
  function sum (num1, num2) {
 return num1 + num2;
  }
//这与下面使用函数表达式定义函数的方式几乎相差无几。
var sum = function(num1, num2){
 return num1 + num2;
};


  ```

`没有重载`,名字相同会被覆盖。

- 函数声明与函数表达式

 ```
 //解析器会率先读取函数声明，并使其在执行任何代码之前可用（可以访问）；至于函数表达式，则必须等到解析器执行到它所在的代码行，才会真正被解释执行。
//正常运行
 alert(sum(10,10));
function sum(num1, num2){
 return num1 + num2;
}
//报错
alert(sum(10,10));
var sum = function(num1, num2){
 return num1 + num2;
};

 ```
- 作为值的函数

```
function callSomeFunction(someFunction, someArgument){
 return someFunction(someArgument);
}


```
- 函数内部属性

  在函数内部，有两个特殊的对象：arguments 和 this。其中，arguments 在第 3 章曾经介绍过，它是一个类数组对象，包含着传入函数中的所有参数。虽然 arguments 的主要用途是保存函数参数，但这个对象还有一个名叫 callee 的属性，该属性是一个指针，指向拥有这个 arguments 对象的函数。请看下面这个非常经典的阶乘函数。

  ```
  //普通的阶乘运算
  function factorial(num){
 if (num <=1) {
 return 1;
 } else {
 return num * factorial(num-1)
 }
}
  //可以使用内部的方法，解耦

  function factorial(num){
 if (num <=1) {
 return 1;
 } else {
 return num * arguments.callee(num-1)
 }
}

  ```

- this

```
window.color = "red";
var o = { color: "blue" };
function sayColor(){
 alert(this.color);
}
sayColor(); //"red"
o.sayColor = sayColor;
o.sayColor(); //"blue"

```

  每个函数都包含两个非继承而来的方法：apply()和 call()。这两个方法的用途都是在特定的作用域中调用函数，实际上等于设置函数体内 this 对象的值。
  ```
  function sum(num1, num2){
 return num1 + num2;
}
function callSum1(num1, num2){
 return sum.apply(this, arguments); // 传入 arguments 对象
}
function callSum2(num1, num2){
 return sum.apply(this, [num1, num2]); // 传入数组
}
alert(callSum1(10,10)); //20
alert(callSum2(10,10)); //20


function sum(num1, num2){
 return num1 + num2;
}
function callSum(num1, num2){
 return sum.call(this, num1, num2); //需要逐个列出来
}
alert(callSum(10,10)); //20


  ```
  事实上，传递参数并非 apply()和 call()真正的用武之地；它们真正强大的地方是能够扩充函数赖以运行的作用域。下面来看一个例子。

  ```
  window.color = "red";
var o = { color: "blue" };
function sayColor(){
 alert(this.color);
}
sayColor(); //red
sayColor.call(this); //red
sayColor.call(window); //red
sayColor.call(o); //blue

  ```

  使用 call()（或 apply()）来扩充作用域的最大好处，就是对象不需要与方法有任何耦合关系。

  bind()。这个方法会创建一个函数的实例，其 this 值会被绑定到传给 bind()函数的值。

  ```
  window.color = "red";
var o = { color: "blue" };
function sayColor(){
 alert(this.color);
}
var objectSayColor = sayColor.bind(o);
objectSayColor(); //blue

  ```
## 基本包装类型

  为了便于操作基本类型值，ECMAScript 还提供了 3 个特殊的引用类型：Boolean、Number 和String。  

  ```
  var s1 = "some text";
var s2 = s1.substring(2);
//等价于
var s1 = new String("some text");
var s2 = s1.substring(2);
s1 = null;

//所以(自动包装的类型生命周期是一瞬间的事情，完成就销毁了)
var s1 = "some text";
s1.color = "red";
alert(s1.color); //undefined

//显示的使用

var obj = new Object("some text");
alert(obj instanceof String); //true


  ```
- String类型

```
var stringValue = "hello world";
alert(stringValue.charAt(1)); //"e"

var stringValue = "hello world";
alert(stringValue[1]); //"e"

var stringValue = "hello ";
var result = stringValue.concat("world");
alert(result); //"hello world"
alert(stringValue); //"hello"

var stringValue = "hello world";
alert(stringValue.slice(3)); //"lo world"
alert(stringValue.substring(3)); //"lo world"
alert(stringValue.substr(3)); //"lo world"
alert(stringValue.slice(3, 7)); //"lo w"
alert(stringValue.substring(3,7)); //"lo w"
alert(stringValue.substr(3, 7)); //"lo worl"

var stringValue = "hello world";
alert(stringValue.slice(-3)); //"rld"
alert(stringValue.substring(-3)); //"hello world"
alert(stringValue.substr(-3)); //"rld"
alert(stringValue.slice(3, -4)); //"lo w"
alert(stringValue.substring(3, -4)); //"hel"
alert(stringValue.substr(3, -4)); //""（空字符串）

var stringValue = " hello world ";
var trimmedStringValue = stringValue.trim();
alert(stringValue); //" hello world "
alert(trimmedStringValue); //"hello world"

var text = "cat, bat, sat, fat";
var pos = text.search(/at/);
alert(pos); //1

```
