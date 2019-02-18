---
layout: post
title:  JavaScirpt学习笔记四
date:   2019-02-18 23:45:12 +08:00
category: 前端学习
tags: JavaScript
comments: true
---

* content
{:toc}

JavaScript一种面向对象的程序，这里将介绍设计。








## 单体内置对象

- Global对象（不属于任何其他对象的属性和方法，最终都是它的属性和方法）

  a.  URI 编码方法

  Global 对象的 encodeURI()和 encodeURIComponent()方法可以对 URI（Uniform Resource Identifiers，通用资源标识符）进行编码，以便发送给浏览器。有效的 URI 中不能包含某些字符，例如空格。

  ```

  //般来说，我们使用 encodeURIComponent() 方法的时候要比使用encodeURI()更多，因为在实践中更常见的是对查询字符串参数而不是对基础 URI进行编码。
  var uri = "http://www.wrox.com/illegal value.htm#start";
//"http://www.wrox.com/illegal%20value.htm#start"
alert(encodeURI(uri));
//"http%3A%2F%2Fwww.wrox.com%2Fillegal%20value.htm%23start"
alert(encodeURIComponent(uri));

  ```

  >>使用 encodeURI()编码后的结果是除了空格之外的其他字符都原封不动，只有空格被替换成了%20。而 encodeURIComponent()方法则会使用对应的编码替换所有非字母数字字符。这也正是可以对整个 URI 使用 encodeURI()，而只能对附加在现有 URI 后面的字符串使用 encodeURIComponent()的原因所在。

  与 encodeURI()和 encodeURIComponent()方法对应的两个方法分别是 decodeURI()和decodeURIComponent()。

  ```
  var uri = "http%3A%2F%2Fwww.wrox.com%2Fillegal%20value.htm%23start";
//http%3A%2F%2Fwww.wrox.com%2Fillegal value.htm%23start
alert(decodeURI(uri));
//http://www.wrox.com/illegal value.htm#start
alert(decodeURIComponent(uri));

  ```

  b. eval()方法

  就像是一个完整的 ECMAScript 解析器，它只接受一个参数，即要执行的 ECMAScript（或 JavaScript）字符串。

  ```
  eval("alert('hi')");
  //等价于alert("hi");

  var msg = "hello world";
  eval("alert(msg)"); //"hello world"

  eval("function sayHi() { alert('hi'); }");
  sayHi();//同样可以调用

  eval("var msg = 'hello world'; ");
  alert(msg); //"hello world"  


  ```

  c. Global 对象的属性

  ![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/globalparams.png)

  ECMAScript 5 明确禁止给 undefined、NaN 和 Infinity 赋值，这样做即使在非严格模式下也会导致错误。

  d. window 对象

  ECMAScript 虽然没有指出如何直接访问 Global 对象，但 Web 浏览器都是将这个全局对象作为window 对象的一部分加以实现的。

  ```
  var color = "red";
function sayColor(){
 alert(window.color);
}
window.sayColor(); //"red"

  ```

  在没有给函数明确指定this 值的情况下（无论是通过将函数添加为对象的方法，还是通过调用 call()或 apply()），this值等于 Global 对象。

- Math对象

    a. 属性

    ```
Math.E 自然对数的底数，即常量e的值
Math.LN10 10的自然对数
Math.LN2 2的自然对数
Math.LOG2E 以2为底e的对数
Math.LOG10E 以10为底e的对数
Math.PI π的值
Math.SQRT1_2 1/2的平方根（即2的平方根的倒数）
Math.SQRT2 2的平方根

    ```

    b. 方法

     min()和 max()方法：

     ```
     var max = Math.max(3, 54, 32, 16);
     alert(max); //54
     var min = Math.min(3, 54, 32, 16);
     alert(min); //3

     ```
    舍入方法:

    ```
    Math.ceil()执行向上舍入，即它总是将数值向上舍入为最接近的整数；
    Math.floor()执行向下舍入，即它总是将数值向下舍入为最接近的整数；
    Math.round()执行标准舍入，即它总是将数值四舍五入为最接近的整数（这也是我们在数学课上学到的舍入规则）。

    alert(Math.ceil(25.9)); //26
alert(Math.ceil(25.5)); //26
alert(Math.ceil(25.1)); //26
alert(Math.round(25.9)); //26
alert(Math.round(25.5)); //26
alert(Math.round(25.1)); //25

alert(Math.floor(25.9)); //25
alert(Math.floor(25.5)); //25
alert(Math.floor(25.1)); //25

    ```

     random()方法:

     ```

    // 值 = Math.floor(Math.random() * 可能值的总数 + 第一个可能的值)
     //Math.random()方法返回大于等于 0 小于 1 的一个随机数。
    var num = Math.floor(Math.random() * 10 + 1);//1~10

    var num = Math.floor(Math.random() * 9 + 2);// 2~10 9是总数

    //活学活用

function selectFrom(lowerValue, upperValue) {
 var choices = upperValue - lowerValue + 1;
 return Math.floor(Math.random() * choices + lowerValue);
}
var num = selectFrom(2, 10);
alert(num);// 介于 2 和 10 之间（包括 2 和 10）的一个数值

var colors = ["red", "green", "blue", "yellow", "black", "purple", "brown"];
var color = colors[selectFrom(0, colors.length-1)];
alert(color); // 可能是数组中包含的任何一个字符串

     ```

## 面向对象的程序设计

```
var person = {
 name: "Nicholas",
 age: 29,
 job: "Software Engineer",
 sayName: function(){
 alert(this.name);
 }
};



```     

  a.  数据属性

```

//也可以使用Object

var person = {};
Object.defineProperty(person, "name", {
 writable: false,
 value: "Nicholas"
});
alert(person.name); //"Nicholas"
person.name = "Greg";
alert(person.name); //"Nicholas"

var person = {};
Object.defineProperty(person, "name", {
 configurable: false, //把 configurable 设置为 false，表示不能从对象中删除属性
 value: "Nicholas"
});
alert(person.name); //"Nicholas"
delete person.name;
alert(person.name); //"Nicholas"

```

  在调用 Object.defineProperty()方法时，如果不指定，configurable、enumerable 和writable 特性的默认值都是 false。多数情况下，可能都没有必要利用 Object.defineProperty()方法提供的这些高级功能。不过，理解这些概念对理解 JavaScript 对象却非常有用。

  b. 访问器属性

```
var book = {
 _year: 2004,
 edition: 1
};
Object.defineProperty(book, "year", {
 get: function(){
 return this._year;
 },
 set: function(newValue){
 if (newValue > 2004) {
 this._year = newValue;
 this.edition += newValue - 2004;
 }
 }
});
book.year = 2005;
alert(book.edition); //2


以上代码创建了一个 book 对象，并给它定义两个默认的属性：_year 和 edition。_year 前面的下划线是一种常用的记号，用于表示只能通过对象方法访问的属性。而访问器属性 year 则包含一个getter 函数和一个 setter 函数。getter 函数返回_year 的值，setter 函数通过计算来确定正确的版本。因此，
把 year 属性修改为 2005 会导致_year 变成 2005，而 edition 变为 2。这是使用访问器属性的常见方
式，即设置一个属性的值会导致其他属性发生变化

不一定非要同时指定 getter 和 setter。只指定 getter 意味着属性是不能写，尝试写入属性会被忽略。在严格模式下，尝试写入只指定了 getter 函数的属性会抛出错误。类似地，只指定 setter 函数的属性也不能读，否则在非严格模式下会返回 undefined，而在严格模式下会抛出错误。

var book = {};
Object.defineProperties(book, {
 _year: {
 value: 2004
 },

 edition: {
 value: 1
 },
 year: {
 get: function(){
   return this._year;
 },
 set: function(newValue){
 if (newValue > 2004) {
 this._year = newValue;
 this.edition += newValue - 2004;
 }
 }
 }
});


var book = {};
Object.defineProperties(book, {
 _year: {
 value: 2004
 },
 edition: {
 value: 1
 },
 year: {
 get: function(){
 return this._year;
 },
 set: function(newValue){
 if (newValue > 2004) {
 this._year = newValue;
 this.edition += newValue - 2004;
 }
 }
 }
});


var descriptor = Object.getOwnPropertyDescriptor(book, "_year");
alert(descriptor.value); //2004
alert(descriptor.configurable); //false

alert(typeof descriptor.get); //"undefined"
var descriptor = Object.getOwnPropertyDescriptor(book, "year");
alert(descriptor.value); //undefined
alert(descriptor.enumerable); //false
alert(typeof descriptor.get); //"function"
GetPropertyDescriptorExample01.htm


```

- 创建对象

  工厂模式：

  ```
  function createPerson(name, age, job){
 var o = new Object();
 o.name = name;
 o.age = age;
 o.job = job;
 o.sayName = function(){
 alert(this.name);
 };
 return o;
}
var person1 = createPerson("Nicholas", 29, "Software Engineer");
var person2 = createPerson("Greg", 27, "Doctor");

  ```

  构造函数模式:

  ```
  function Person(name, age, job){
 this.name = name;
 this.age = age;
 this.job = job;
 this.sayName = function(){
 alert(this.name);
 };
}
var person1 = new Person("Nicholas", 29, "Software Engineer");
var person2 = new Person("Greg", 27, "Doctor");


// 当作构造函数使用
var person = new Person("Nicholas", 29, "Software Engineer");
person.sayName(); //"Nicholas"
// 作为普通函数调用
Person("Greg", 27, "Doctor"); // 添加到 window
window.sayName(); //"Greg"
// 在另一个对象的作用域中调用
var o = new Object();
Person.call(o, "Kristen", 25, "Nurse");
o.sayName(); //"Kristen

  ```

  原型模式:

  ```
  function Person(){
}
Person.prototype.name = "Nicholas";
Person.prototype.age = 29;
Person.prototype.job = "Software Engineer";
Person.prototype.sayName = function(){
 alert(this.name);
};
var person1 = new Person(); 
person1.sayName(); //"Nicholas"
var person2 = new Person();
person2.sayName(); //"Nicholas"
alert(person1.sayName == person2.sayName); //true

  ```
