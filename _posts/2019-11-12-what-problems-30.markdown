---
layout: post
title:  ELK
date:   2019-11-12 20:52:12 +08:00
category: ELK
tags: ELK
comments: true
---

* content
{:toc}


ELK!










创建elsearch用户组及elsearch用户：

groupadd elsearch
useradd elsearch -g elsearch
passwd elsearch
更改elasticsearch文件夹及内部文件的所属用户及组为elsearch:elsearch

cd /opt
chown -R elsearch:elsearch  elasticsearch-6.8.0
切换到elsearch用户再启动

su elsearch
cd /opt/elasticsearch-6.8.0/bin
./elasticsearch

chown -R elsearch:elsearch /elk/elasticsearch-6.5.1-slave1/

在   /etc/sysctl.conf文件最后添加一行

vm.max_map_count=262144

sysctl -p

修改elasticsearch-2.3.3\config\elasticsearch.yml文件

network.host:192.168.132.101



问题翻译过来就是：elasticsearch用户拥有的可创建文件描述的权限太低，至少需要65536；

解决办法：

#切换到root用户修改
vim /etc/security/limits.conf

# 在最后面追加下面内容
*** hard nofile 65536
*** soft nofile 65536

***  是启动ES的用户
