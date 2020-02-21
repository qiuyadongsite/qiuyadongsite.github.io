---
layout: post
title:  jvm
date:   2020-01-24 20:53:12 +08:00
category: 签到系列
tags: spring
comments: true
---

* content
{:toc}


签到16！







Focusing on the key

## Introduction

- A Bit of History

  >The Java® programming language is a general-purpose, concurrent, object-oriented language. It was designed to support multiple host architectures and to allow secure delivery of software components.  
  >
  >Programmers can write a program once, and it will run on any machine supplying a Java run-time environment.

- The Java Virtual Machine

  > Like a real computing machine, it has an instruction set and manipulates various memory areas at run time
  >
  > For the sake of security, the Java Virtual Machine imposes strong syntactic and structural constraints on the code in a `class` file
  >
  > any language with functionality that can be expressed in terms of a valid `class` file can be hosted by the Java Virtual Machine.  
  >
  >machine-independent platform, implementors of other languages can turn to the Java Virtual Machine as a delivery vehicle for their languages.

## The Structure of the Java Virtual Machine

- Run-Time Data Areas

  - The pc Register

    > If that method is not `native`, the `pc` register contains the address of the Java Virtual Machine instruction currently being executed.
    >
    >If the method currently being executed by the thread is `native`, the value of the Java Virtual Machine's `pc` register is undefined.
    >
    >The Java Virtual Machine's `pc` register is wide enough to hold a `returnAddress` or a native pointer on the specific platform.

  -  Java Virtual Machine Stacks

    > Each Java Virtual Machine thread has a private *Java Virtual Machine stack*, created at the same time as the thread.  
    >
    > If the computation in a thread requires a larger Java Virtual Machine stack than is permitted, the Java Virtual Machine throws a `StackOverflowError`.
    >
    > If Java Virtual Machine stacks can be dynamically expanded, and expansion is attempted but insufficient memory can be made available to effect the expansion, or if insufficient memory can be made available to create the initial Java Virtual Machine stack for a new thread, the Java Virtual Machine throws an `OutOfMemoryError`.

    - Frames

      > Each frame has its own array of local variables (§2.6.1), its own operand stack (§2.6.2), and a reference to the run-time constant pool (§2.5.5) of the class of the current method.

      -  Local Variables
      -  Operand Stacks
      - Dynamic Linking
      - Normal Method Invocation Completion
      - Abrupt Method Invocation Completion

  - heap

    >The heap is the run-time data area from which memory for all class instances and arrays is allocated.
    >
    >The memory for the heap does not need to be contiguous.
    >
    >If a computation requires more heap than can be made available by the automatic storage management system, the Java Virtual Machine throws an `OutOfMemoryError` .

  - Method Area

    >The method area is analogous to the storage area for compiled code of a conventional language or analogous to the "text" segment in an operating system process.
    >
    >It stores per-class structures such as the run-time constant pool, field and method data, and the code for methods and constructors, including the special methods ([§2.9](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-2.html#jvms-2.9)) used in class and instance initialization and interface initialization.
    >
    >Although the method area is logically part of the heap, simple implementations may choose not to either garbage collect or compact it.  
    >
    >The memory for the method area does not need to be contiguous.

    - Run-Time Constant Pool

  - Native Method Stacks

    > An implementation of the Java Virtual Machine may use conventional stacks, colloquially called "C stacks," to support native methods


##  Compiling for the Java Virtual Machine

The Java Virtual Machine code is written in the informal “virtual machine assembly language” output by Oracle's `javap` utility, distributed with the JDK release.
