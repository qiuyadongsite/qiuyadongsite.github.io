---
layout: post
title:  关于使用字节码
date:   2019-05-17 21:52:12 +08:00
category: 性能优化
tags: 字节码
comments: true
---

* content
{:toc}


Java字节码是将源代码编译后的代码并且可以在JVM上运行。所以掌握字节码可以提高编写Java代码的质量，查找问题原因。




















## 基础篇

### 查看字节码

字节码文件是二进制文件，查看它通过Java字节码结构表可以一一读取，但是没有必要，要想理解具体含义，需要工具或者命令查看如下：

第一种方式（命令行，建议使用）：

```

javap -c k:\**\**.class   //对代码进行反汇编

 -v  -verbose            // 输出附加信息
```

第二种工具（ideal插件Bytecode Viewer-自动已经安装了）：

点击ideal：菜单View-->Show bytecode

java源代码：

```java

public class ByteCode {
    public static void main(String[] args) {
        int i1=1;
        int i2=2;
        int sum=sum(i1,i2);
        print("两个数之和："+sum);
    }

    private static int sum(int i1, int i2) {
        return i1+i2;
    }

    private static void print(String s) {
        System.out.println(s);
    }
}

```

class文件查看代码：

```

Classfile /K:/ideal_work/showmycode/show-me-common/target/classes/com/qyd/learn/show/ByteCode.class
  Last modified 2019-5-17; size 1043 bytes
  MD5 checksum 13894cbfe3b431b3dabc581b0d61af09
  Compiled from "ByteCode.java"
public class com.qyd.learn.show.ByteCode
  minor version: 0
  major version: 52                      //版本是1.8
  flags: ACC_PUBLIC, ACC_SUPER         //类标志
Constant pool:
   #1 = Methodref          #13.#36        // java/lang/Object."<init>":()V   //这是要调用默认构造方法<init>  看#13默认继承object
   #2 = Methodref          #12.#37        //com/qyd/learn/show/ByteCode.sum:(II)I //sum方法  看由于是静态方法，依赖于类ByteCode和常量标识符号
   #3 = Class              #38            // java/lang/StringBuilder  
   #4 = Methodref          #3.#36         // java/lang/StringBuilder."<init>":()V
   #5 = String             #39            // 两个数之和：
   #6 = Methodref          #3.#40         // java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
   #7 = Methodref          #3.#41         // java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
   #8 = Methodref          #3.#42         // java/lang/StringBuilder.toString:()Ljava/lang/String;
   #9 = Methodref          #12.#43        // com/qyd/learn/show/ByteCode.print:(Ljava/lang/String;)V
  #10 = Fieldref           #44.#45        // java/lang/System.out:Ljava/io/PrintStream;
  #11 = Methodref          #46.#47        // java/io/PrintStream.println:(Ljava/lang/String;)V
  #12 = Class              #48            // com/qyd/learn/show/ByteCode
  #13 = Class              #49            // java/lang/Object
  #14 = Utf8               <init>
  #15 = Utf8               ()V
  #16 = Utf8               Code
  #17 = Utf8               LineNumberTable
  #18 = Utf8               LocalVariableTable
  #19 = Utf8               this
  #20 = Utf8               Lcom/qyd/learn/show/ByteCode;
  #21 = Utf8               main
  #22 = Utf8               ([Ljava/lang/String;)V
  #23 = Utf8               args
  #24 = Utf8               [Ljava/lang/String;
  #25 = Utf8               i1
  #26 = Utf8               I
  #27 = Utf8               i2
  #28 = Utf8               sum
  #29 = Utf8               (II)I
  #30 = Utf8               print
  #31 = Utf8               (Ljava/lang/String;)V
  #32 = Utf8               s
  #33 = Utf8               Ljava/lang/String;
  #34 = Utf8               SourceFile
  #35 = Utf8               ByteCode.java
  #36 = NameAndType        #14:#15        // "<init>":()V
  #37 = NameAndType        #28:#29        // sum:(II)I
  #38 = Utf8               java/lang/StringBuilder
  #39 = Utf8               两个数之和：
  #40 = NameAndType        #50:#51        // append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
  #41 = NameAndType        #50:#52        // append:(I)Ljava/lang/StringBuilder;
  #42 = NameAndType        #53:#54        // toString:()Ljava/lang/String;
  #43 = NameAndType        #30:#31        // print:(Ljava/lang/String;)V
  #44 = Class              #55            // java/lang/System
  #45 = NameAndType        #56:#57        // out:Ljava/io/PrintStream;
  #46 = Class              #58            // java/io/PrintStream
  #47 = NameAndType        #59:#31        // println:(Ljava/lang/String;)V
  #48 = Utf8               com/qyd/learn/show/ByteCode
  #49 = Utf8               java/lang/Object
  #50 = Utf8               append
  #51 = Utf8               (Ljava/lang/String;)Ljava/lang/StringBuilder;
  #52 = Utf8               (I)Ljava/lang/StringBuilder;
  #53 = Utf8               toString
  #54 = Utf8               ()Ljava/lang/String;
  #55 = Utf8               java/lang/System
  #56 = Utf8               out
  #57 = Utf8               Ljava/io/PrintStream;
  #58 = Utf8               java/io/PrintStream
  #59 = Utf8               println
{
  public com.qyd.learn.show.ByteCode();
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 3: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       5     0  this   Lcom/qyd/learn/show/ByteCode;

  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=4, args_size=1
         0: iconst_1                               //push integer n onto stack
         1: istore_1                               //store integer on top of stack to local var ///  slot 1
         2: iconst_2                              //push integer n onto stack
         3: istore_2                              //store integer on top of stack to local var ///  slot 2
         4: iload_1                               //retrieves integer from local var ///  slot 1
         5: iload_2                               //retrieves integer from local var ///  slot 2
         6: invokestatic  #2                  // Method sum:(II)I
         9: istore_3                            //store integer on top of stack to local var ///  slot 3
        10: new           #3                  // class java/lang/StringBuilder
        13: dup                               //duplicate top of stack
        14: invokespecial #4                  // Method java/lang/StringBuilder."<init>":()V
        17: ldc           #5                  // String 两个数之和：
        19: invokevirtual #6                  // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
        22: iload_3
        23: invokevirtual #7                  // Method java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
        26: invokevirtual #8                  // Method java/lang/StringBuilder.toString:()Ljava/lang/String;
        29: invokestatic  #9                  // Method print:(Ljava/lang/String;)V
        32: return
      LineNumberTable:
        line 5: 0                //从第5行实际代码开始运行，0代表，从0: iconst_1 开始
        line 6: 2                 //6，2代表，从2: iconst_2 开始
        line 7: 4                 //7，4代表，从4: iload_1
        line 8: 10                //8，10代表，从10: new   #3 依赖于java/lang/StringBuilder  
        line 9: 32                 //9，32代表，从32: return
      LocalVariableTable:          //本地变量表
        Start  Length  Slot  Name   Signature
            0      33     0  args   [Ljava/lang/String;
            2      31     1    i1   I
            4      29     2    i2   I
           10      23     3   sum   I
}
SourceFile: "ByteCode.java"

```

还是很难理解，如何解释这个class呢？

一些特殊的标识符需要记录：

```

标志符	含义

B	基本数据类型byte
C	基本数据类型char
D	基本数据类型double
F	基本数据类型float
I	基本数据类型int
J	基本数据类型long
S	基本数据类型short
Z	基本数据类型boolean
V	基本数据类型void
L	对象类型,如Ljava/lang/Object
[ 数组

如：java.lang.String[][]类型的二维数组，将被记录为：[[Ljava/lang/String;一个整型数组int[]被记录为[I。
如：如方法java.lang.String toString()的描述符为( ) LJava/lang/String;，方法int abc(int[] x, int y)的描述符为([II) I。

```
一些命令可参见：https://wds.gitee.io/jvm-code/codeByNo.html

### jvm的运行原理

读懂字节码之后，大概了解了字节码的一些构造和原理。jvm接下来加载字节码文件即可运行。

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/jvm00001.png)

类的装载器分为系统类加载器+用户自定义的加载器，加载了这些class文件后，之后进行校验后，关联一些解释器后，在硬件上执行；（与开发无关的简述）

运行是在jvm上执行的：

PC寄存器：每个线程都有一个程序计数器，是线程私有的,就是一个指针，指向方法区中的方法字节码（用来存储指向下一条指令的地址,也即将要执行的指令代码），由执行引擎读取下一条指令，是一个非常小的内存空间，几乎可以忽略不记。

方法区：方法区是被所有线程共享，所有字段和方法字节码，以及一些特殊方法如构造函数，接口代码也在此定义。简单说，所有定义的方法的信息都保存在该区域，此区属于共享区间。`静态变量`+`常量`+`类信息`(构造方法/接口定义)+`运行时常量池`存在`方法区`中;其实可以理解为堆的一部分，仅仅为了概念区分。

栈也叫本地方法栈，主管Java程序的运行，是在线程创建时创建，它的生命期是跟随线程的生命期，线程结束栈内存也就释放，对于栈来说不存在垃圾回收问题，只要线程一结束该栈就Over，生命周期和线程一致，是线程私有的。8种基本类型的变量+对象的引用变量+实例方法都是在函数的栈内存中分配。（存储：本地变量（Local Variables）:输入参数和输出参数以及方法内的变量；栈操作（Operand Stack）:记录出栈、入栈的操作；）

堆：一个JVM实例只存在一个堆内存，堆内存的大小是可以调节的。类加载器读取了类文件后，需要把类、方法、常变量放到堆内存中，保存所有引用类型的真实信息，以方便执行器执行，堆内存分为三部分：新生、养老、永久。

#### JVM内存调优

内存调优，首先JDK提供的内存查看工具，比如JConsole和Java VisualVM。

内存调优主要的目的是减少GC的频率和Full GC的次数，过多的GC和Full GC是会占用很多的系统资源（主要是CPU），影响系统的吞吐量。
（不是本篇重点）

## 实践篇

jvm识别和运行的都是class文件，如果开发可以动态生成class文件，可以减少重复编写的源码，提高效率。（dubbo就是这么做的，基本原理生成代码，使用动态代理实现相同的功能和提高可扩展性）

### SPI

先介绍一下SPI，java提供了Service Provider Interface，简称SPI,为接口寻找服务实现类,"基于接口的编程＋策略模式＋配置文件"组合实现的动态加载机制。

在META-INF/services/下创建一个以接口名命名的文件，内容为实现类，当使用

```java

ServiceLoader<？> serviceloader = ServiceLoader.load(？.class);//可以加载所有该？接口的包括jar中的实现类，遍历它可以获取

```

现实开发中common-logging、jdbc都是使用了它。

虽然用了懒加载方式，减缓了实现类的初始化，但是问题明显，就是肯定会实例化所有配置的实现类，dubbo对其进行了改进

### dubbo的@spi

dubbo（2.7.1）使用注解@SPI类标注接口,如本篇要提到的字节码编译类：

```java
package org.apache.dubbo.common.compiler;
@SPI("javassist")
public interface Compiler {

    Class<?> compile(String code, ClassLoader classLoader);

}

```

在配置文件org.apache.dubbo.common.compiler.Compiler里写了：

```

adaptive=org.apache.dubbo.common.compiler.support.AdaptiveCompiler
jdk=org.apache.dubbo.common.compiler.support.JdkCompiler
javassist=org.apache.dubbo.common.compiler.support.JavassistCompiler

```

如果调用方没有配置编译类型，默认就使用javassist的JavassistCompiler来编译动态生成的code类编译。


而ExtensionLoader<T>类就是dubbo加载扩展的核心类，其中有个方法createAdaptiveExtensionClass，可以获取编译类去编译生成的code:

1 AdaptiveClassCodeGenerator的代码生成方法:

```java

public String generate() {
       if (!hasAdaptiveMethod()) {
           throw new IllegalStateException("No adaptive method exist on extension " + type.getName() + ", refuse to create the adaptive class!");
       }

       StringBuilder code = new StringBuilder();
       code.append(generatePackageInfo());
       code.append(generateImports());
       code.append(generateClassDeclaration());

       Method[] methods = type.getMethods();
       for (Method method : methods) {
           code.append(generateMethod(method));
       }
       code.append("}");

       if (logger.isDebugEnabled()) {
           logger.debug(code.toString());
       }
       return code.toString();
   }
```

2 ExtensionLoader<T>的createAdaptiveExtensionClass创建类，以及对代码进行编译方法

```java   


private Class<?> createAdaptiveExtensionClass() {
        String code = new AdaptiveClassCodeGenerator(type, cachedDefaultName).generate();
        ClassLoader classLoader = findClassLoader();
        org.apache.dubbo.common.compiler.Compiler compiler = ExtensionLoader.getExtensionLoader(org.apache.dubbo.common.compiler.Compiler.class).getAdaptiveExtension();
        return compiler.compile(code, classLoader);
    }

```

3 ExtensionLoader<T>的createAdaptiveExtension()根据配置可以实例化

```java

private T createAdaptiveExtension() {
       try {
           return injectExtension((T) getAdaptiveExtensionClass().newInstance());
       } catch (Exception e) {
           throw new IllegalStateException("Can't create adaptive extension " + type + ", cause: " + e.getMessage(), e);
       }
   }

```

这就很聪明的通过用户配置动态的创建与加载某个类，同时也节约大量代码的编写，通过这种策略，使用方也可以很容易扩展。

### javassist的编译方法

对于Javassist的简单运用：

```java

public class JavassistCompiler extends AbstractCompiler {

    private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+([\\w\\.\\*]+);\n");//impot

    private static final Pattern EXTENDS_PATTERN = Pattern.compile("\\s+extends\\s+([\\w\\.]+)[^\\{]*\\{\n");//extends

    private static final Pattern IMPLEMENTS_PATTERN = Pattern.compile("\\s+implements\\s+([\\w\\.]+)\\s*\\{\n");//implements

    private static final Pattern METHODS_PATTERN = Pattern.compile("\n(private|public|protected)\\s+");// 方法的查找

    private static final Pattern FIELD_PATTERN = Pattern.compile("[^\n]+=[^\n]+;"); //属性的查找

    @Override
    public Class<?> doCompile(String name, String source) throws Throwable {
        CtClassBuilder builder = new CtClassBuilder();
        builder.setClassName(name);//创建一个类名

        // process imported classes
        Matcher matcher = IMPORT_PATTERN.matcher(source);
        while (matcher.find()) {
            builder.addImports(matcher.group(1).trim());//导入所有引用的包
        }

        // process extended super class
        matcher = EXTENDS_PATTERN.matcher(source);
        if (matcher.find()) {
            builder.setSuperClassName(matcher.group(1).trim());
        }

        // process implemented interfaces
        matcher = IMPLEMENTS_PATTERN.matcher(source);
        if (matcher.find()) {
            String[] ifaces = matcher.group(1).trim().split("\\,");
            Arrays.stream(ifaces).forEach(i -> builder.addInterface(i.trim()));
        }

        // process constructors, fields, methods
        String body = source.substring(source.indexOf('{') + 1, source.length() - 1);
        String[] methods = METHODS_PATTERN.split(body);
        String className = ClassUtils.getSimpleClassName(name);
        Arrays.stream(methods).map(String::trim).filter(m -> !m.isEmpty()).forEach(method-> {
            if (method.startsWith(className)) {
                builder.addConstructor("public " + method);
            } else if (FIELD_PATTERN.matcher(method).matches()) {
                builder.addField("private " + method);
            } else {
                builder.addMethod("public " + method);
            }
        });

        // compile
        ClassLoader classLoader = ClassHelper.getCallerClassLoader(getClass());
        CtClass cls = builder.build(classLoader);
        return cls.toClass(classLoader, JavassistCompiler.class.getProtectionDomain());
    }

}


```
