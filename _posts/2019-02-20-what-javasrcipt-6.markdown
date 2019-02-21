---
layout: post
title:  JavaScirpt学习笔记六
date:   2019-02-20 21:45:12 +08:00
category: 前端学习
tags: JavaScript
comments: true
---

* content
{:toc}

闭包是指有权访问另一个函数作用域中的变量的函数。








## 概念

- 作用域链

当某个函数被调用时，会创建一个执行环境（execution context）及相应的作用域链。

然后，使用 arguments 和其他命名参数的值来初始化函数的活动对象（activation object）。但在作用域链中，外部函数的活动对象始终处于第二位，外部函数的外部函数的活动对象处于第三位，……直至作为作用域链终点的全局执行环境。

后台的每个执行环境都有一个表示变量的对象——变量对象。

作用域链本质上是一个指向变量对象的指针列表，它只引用但不实际包含变量对象。

无论什么时候在函数中访问一个变量时，就会从作用域链中搜索具有相应名字的变量。一般来讲，当函数执行完毕后，`局部活动对象`就会被销毁，内存中仅保存全局作用域（全局执行环境的变量对象）。但是，`闭包的情况又有所不同`。

在另一个函`数内部定义的函数`会将包含函数（即外部函数）的活动对象添加到`它的作用域链`中。因此，在 createComparisonFunction()函数内部定义的匿名函数的作用域链中，实际上将会包含外部函数 createComparisonFunction()的活动对象，包含函数与内部匿名函数的作用域链。

匿名函数就可以访问在createComparisonFunction()中定义的所有变量。

更为重要的是，createComparisonFunction()函数在执行完毕后，其活动对象也不会被销毁，因为匿名函数的作用域链仍然在引用这个活动对象。换句话说，当 createComparisonFunction()函数返回后，其执行环境的作用域链会被销毁，但它的活动对象仍然会留在内存中；直到匿名函数被销毁后，createComparisonFunction()的活动对象才会被销毁，例如：

```js

//创建函数
var compareNames = createComparisonFunction("name");
//调用函数
var result = compareNames({ name: "Nicholas" }, { name: "Greg" });
//解除对匿名函数的引用（以便释放内存）
compareNames = null;

//首先，创建的比较函数被保存在变量compareNames 中。而通过将compareNames 设置为等于null解除该函数的引用，就等于通知垃圾回收例程将其清除。随着匿名函数的作用域链被销毁，其他作用域（除了全局作用域）也都可以安全地销毁了。

```

>>由于闭包会携带包含它的函数的作用域，因此会比其他函数占用更多的内存。过度使用闭包可能会导致内存占用过多，我们建议读者只在绝对必要时再考虑使用闭
包。

- 闭包与变量

作用域链的这种配置机制引出了一个值得注意的副作用，即闭包只能取得包含函数中任何变量的最后一个值。别忘了闭包所保存的是整个变量对象，而不是某个特殊的变量。下面这个例子可以清晰地说明这个问题:

```js

function createFunctions(){
 var result = new Array();
 for (var i=0; i < 10; i++){
 result[i] = function(){
 return i;
 };
 }
 return result;
}
//实际上，每个函数都返回 10。

//因为每个函数的作用域链中都保存着 createFunctions() 函数的活动对象，所以它们引用的都是同一个变量 i 。 当createFunctions()函数返回后，变量 i 的值是 10，此时每个函数都引用着保存变量 i 的同一个变量对象，所以在每个函数内部 i 的值都是 10。

//可以通过创建另一个匿名函数强制让闭包的行为符合预期

function createFunctions(){
 var result = new Array();
 for (var i=0; i < 10; i++){
 result[i] = function(num){
 return function(){
 return num;
 };
 }(i);
 }
 return result;
}

//由于函数参数是按值传递的，所以就会将变量 i 的当前值复制给参数 num。而在这个匿名函数内部，又创建并返回了一个访问 num 的闭包。这样一来，result 数组中的每个函数都有自己num 变量的一个副本，因此就可以返回各自不同的数值了

```

- 关于this对象

this 对象是在运行时基于函数的执行环境绑定的：在全局函数中，this 等于 window，而当函数被作为某个对象的方法调用时，this 等于那个对象。

不过，匿名函数的执行环境具有全局性，因此其 this 对象通常指向 window;

```js

var name = "The Window";
var object = {
 name : "My Object",
 getNameFunc : function(){
 return function(){
 return this.name;
 };
 }
};
alert(object.getNameFunc()()); //"The Window"（在非严格模式下）

//每个函数在被调用时都会自动取得两个特殊变量：this 和 arguments。内部函数在搜索这两个变量时，只会搜索到其活动对象为止，因此永远不可能直接访问外部函数中的这两个变量。

//把外部作用域中的 this 对象保存在一个闭包能够访问到的变量里，就可以让闭包访问该对象了

var name = "The Window";
var object = {
 name : "My Object",
 getNameFunc : function(){
   var that = this;
 return function(){
 return that.name;
 };
 }
};
alert(object.getNameFunc()()); //"My Object"

//闭包也可以访问这个变量，因为它是我们在包含函数中特意声名的一个变量。即使在函数返回之后，that 也仍然引用着 object，所以调用object.getNameFunc()()就返回了"My Object"。

var name = "The Window";
var object = {
 name : "My Object",
 getName: function(){
 return this.name;
 }
};

object.getName(); //"My Object"
(object.getName)(); //"My Object"
(object.getName = object.getName)(); //"The Window"，在非严格模式下 因为这个赋值表达式的值是函数本身，所以 this 的值不能得到维持，结果就返回了"The Window"。


```

- 内存泄漏

```js
function assignHandler(){
 var element = document.getElementById("someElement");
 element.onclick = function(){
 alert(element.id);
 };
}

//由于匿名函数保存了一个对 assignHandler()的活动对象的引用，因此就会导致无法减少 element 的引用数。只要匿名函数存在，element 的引用数至少也是 1，因此它所占用的内存就永远不会被回收。


function assignHandler(){
 var element = document.getElementById("someElement");
 var id = element.id;

 element.onclick = function(){
 alert(id);
 };

 element = null;
}

//把 element.id 的一个副本保存在一个变量中，并且在闭包中引用该变量消除了循环引用。闭包会引用包含函数的整个活动对象，而其中包含着 element。
//有必要把 element 变量设置为 null。

```

- 模仿块级作用域

JavaScript 从来不会告诉你是否多次声明了同一个变量；遇到这种情况，它只会对后续的声明视而不见（不过，它会执行后续声明中的变量初始化）。匿名函数可以用来模仿块级作用域并避免这个问题。用作块级作用域（通常称为私有作用域）的匿名函数的语法如下所示

```js
(function(){
 //这里是块级作用域
})();

//为了更好地理解

var someFunction = function(){
 //这里是块级作用域
};
someFunction();

//定义函数的方式是创建一个匿名函数，并把匿名函数赋值给变量 someFunction。而调用函数的方式是在函数名称后面添加一对圆括号，即someFunction()。

function(){
 //这里是块级作用域
}(); //出错！


```

只要临时需要一些变量，就可以使用私有作用域，这种技术经常在全局作用域中被用在函数外部，从而限制向全局作用域中添加过多的变量和函数。例如：

```js

function outputNumbers(count){
 (function () {
 for (var i=0; i < count; i++){
 alert(i);
 }
 })();

 alert(i); //导致一个错误！
}

//在匿名函数中定义的任何变量，都会在执行结束时被销毁。因此，变量 i 只能在循环中使用，使用后即被销毁。
//而在私有作用域中能够访问变量 count，是因为这个匿名函数是一个闭包，它能够访问包含作用域中的所有变量。


```

在一个由很多开发人员共同参与的大型应用程序中，过多的全局变量和函数很容易导致命名冲突。而通过创建私有作用域，每个开发人员既可以使用自己的变量，又不必担心搞乱全局作用域。例如：

```js

(function(){

 var now = new Date(); //其中的变量 now 现在是匿名函数中的局部变量，而我们不必在全局作用域
中创建它
 if (now.getMonth() == 0 && now.getDate() == 1){
 alert("Happy new year!");
 }
})();

```

>>这种做法可以减少闭包占用的内存问题，因为没有指向匿名函数的引用。只要函数执行完毕，就可以立即销毁其作用域链了。

- 私有变量

  任何在函数中定义的变量，都可以认为是私有变量，因为不能在函数的外部访问这些变量。

  私有变量包括函数的参数、局部变量和在函数内部定义的其他函数。来看下面的例子：

```js

function add(num1, num2){
 var sum = num1 + num2;
 return sum;
}

//有 3 个私有变量：num1、num2 和 sum

```

在函数内部可以访问这几个变量，但在函数外部则不能访问它们。如果在这个函数内部创建一个闭包，那么闭包通过自己的作用域链也可以访问这些变量。而利用这一点，就可以创建用于访问私有变量的公有方法。

把有权访问私有变量和私有函数的公有方法称为`特权方法`（privileged method）。有两种在对象上创建特权方法的方式。第一种是在构造函数中定义特权方法，基本模式如下:

```js
function MyObject(){
 //私有变量和私有函数
 var privateVariable = 10;
 function privateFunction(){
 return false;
 }
 //特权方法
 this.publicMethod = function (){
 privateVariable++;
 return privateFunction();
 };
}

//对这个例子而言，变量 privateVariable 和函数 privateFunction()只能通过特权方法 publicMethod()来访问。在创建 MyObject 的实例后，除了使用 publicMethod()这一个途径外，没有任何办法可以直接访问 privateVariable 和 privateFunction()。

```

利用私有和特权成员，可以隐藏那些不应该被直接修改的数据，例如

```js

function Person(name){
 this.getName = function(){
 return name;
 };
 this.setName = function (value) {
 name = value;
 };
}
var person = new Person("Nicholas");
alert(person.getName()); //"Nicholas"
person.setName("Greg");
alert(person.getName()); //"Greg"

//这两个方法都可以在构造函数外部使用，而且都有权访问私有变量 name。

```

但在 Person 构造函数外部，没有任何办法访问 name。由于这两个方法是在构造函数内部定义的，它们作为闭包能够通过作用域链访问 name。私有变量 name
在 Person 的每一个实例中都不相同，因为每次调用构造函数都会重新创建这两个方法。

函数中定义特权方法也有一个缺点，那就是你必须使用构造函数模式来达到这个目的。

- 静态私有变量

```js
(function(){

 //私有变量和私有函数
 var privateVariable = 10;
 function privateFunction(){
 return false;
 }
 //构造函数

 //初始化未经声明的变量，总是会创建一个全局变量。
 MyObject = function(){
 };
 //公有/特权方法
 MyObject.prototype.publicMethod = function(){
 privateVariable++;
 return privateFunction();
 };
})();

```


```js

(function(){

 var name = "";

 Person = function(value){
 name = value;
 };

 Person.prototype.getName = function(){
 return name;
 };

 Person.prototype.setName = function (value){
   name = value;
 };
})();
var person1 = new Person("Nicholas");
alert(person1.getName()); //"Nicholas"
person1.setName("Greg");
alert(person1.getName()); //"Greg"
var person2 = new Person("Michael");
alert(person1.getName()); //"Michael"
alert(person2.getName()); //"Michael"

```

这个例子中的 Person 构造函数与getName()和setName()方法一样，都有权访问私有变量name。在这种模式下，变量 name 就变成了一个静态的、由所有实例共享的属性。也就是说，在一个实例上调用 setName()会影响所有实例。而调用 setName()或新建一个 Person 实例都会赋予 name 属性一个新值。结果就是所有实例都会返回相同的值。

以这种方式创建静态私有变量会因为使用原型而增进代码复用，但每个实例都没有自己的私有变量。到底是使用实例变量，还是静态私有变量，最终还是要视你的具体需求而定。

多查找作用域链中的一个层次，就会在一定程度上影响查找速度。而这正是使用闭包和私有变量的一个显明的不足之处。

- 模块模式(例创建私有变量和特权方法)

```js
var singleton = {
 name : value,
 method : function () {
 //这里是方法的代码
 }
};

var singleton = function(){

 //私有变量和私有函数
 var privateVariable = 10;

 function privateFunction(){
 return false;
}
//特权/公有方法和属性
 return {
 publicProperty: true,
 publicMethod : function(){
 privateVariable++;
 return privateFunction();
 }
 };
}();

//返回的对象字面量中只包含可以公开的属性和方法。由于这个对象是在匿名函数内部定义的，因此它的公有方法有权访问私有变量和函数。

var application = function(){
 //私有变量和函数
 var components = new Array();
 //初始化
 components.push(new BaseComponent());
 //公共
 return {
 getComponentCount : function(){
 return components.length;
 },
 registerComponent : function(component){
 if (typeof component == "object"){
 components.push(component);
 }
 }
 };
}();


```

简言之，如果必须创建一个对象并以某些数据对其进行初始化，同时还要公开一些能够访问这些私有数据的方法，那么就可以使用模块模式。以这种模式创建的每个单例都是 Object 的实例，因为最终要通过一个对象字面量来表示它。事实上，这也没有什么；

毕竟，单例通常都是作为全局对象存在的，我们不会将它传递给一个函数。因此，也就没有什么必要使用 instanceof 操作符来检查其对象类型了。

- 增强的模块模式

```js

var singleton = function(){
 //私有变量和私有函数
 var privateVariable = 10;
 function privateFunction(){
 return false;
 }
 //创建对象
 var object = new CustomType();
 //添加特权/公有属性和方法
 object.publicProperty = true;
 object.publicMethod = function(){
 privateVariable++;
 return privateFunction();
 };
 //返回这个对象
 return object;
}();


var application = function(){
 //私有变量和函数
 var components = new Array();
 //初始化
 components.push(new BaseComponent());
 //创建 application 的一个局部副本
 var app = new BaseComponent();
 //公共接口
 app.getComponentCount = function(){
 return components.length;
 };
 app.registerComponent = function(component){
 if (typeof component == "object"){
 components.push(component);
 }
 };
 //返回这个副本
 return app;
}();

```
