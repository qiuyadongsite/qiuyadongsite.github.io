---
layout: post
title:  高性能web容器-Nginx的初步认识及配置
date:   2019-04-01 21:52:12 +08:00
category: 高并发分布式
tags: Nginx
comments: true
---

* content
{:toc}

1 Nginx在分布式架构中的应用分析

2 常用的Web服务器及差异

3 Nginx的安装以及配置分析

4 Nginx虚拟主机配置

5 详解Location的匹配规则











## 什么是Nginx

是一个高性能的反向代理服务器

正向代理代理的是客户端

反向代理代理的是服务端

Apache、Nginx 静态web服务器

Tomcat 动态服务器jsp/servlet服务器

## 安装Nginx

1 下载tar包

2 tar -zxvf nginx.tar.gz

3 ./configure [--prefix]

```

报错：C compiler cc is not found
yum -y install gcc-c++

报错：./configure: error: the HTTP rewrite module requires the PCRE library.
安装pcre-devel解决问题
yum -y install pcre-devel

报错：./configure: error: the HTTP gzip module requires the zlib library

yum install -y zlib-devel

```

4 make && make install

卸载nginx

```
find / -name nginx*

rm -rf file 此处跟查找出来的nginx文件

```

启动和停止

1 sbin/nginx

2 ./nginx -s stop

### conf配置

nginx.conf

```

#user  nobody;
worker_processes  1;

#error_log  logs/error.log;
#error_log  logs/error.log  notice;
#error_log  logs/error.log  info;

#pid        logs/nginx.pid;


events {
    worker_connections  1024;
}


http {
    include       mime.types;
    default_type  application/octet-stream;

    #log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
    #                  '$status $body_bytes_sent "$http_referer" '
    #                  '"$http_user_agent" "$http_x_forwarded_for"';

    #access_log  logs/access.log  main;

    sendfile        on;
    #tcp_nopush     on;

    #keepalive_timeout  0;
    keepalive_timeout  65;

    #gzip  on;

    //虚拟主机的配置
    server {
        listen       80;
        server_name  localhost;

        #charset koi8-r;

        #access_log  logs/host.access.log  main;

        location / {
            root   html;
            index  index.html index.htm;
        }

        #error_page  404              /404.html;

        # redirect server error pages to the static page /50x.html
        #
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }

        # proxy the PHP scripts to Apache listening on 127.0.0.1:80
        #
        #location ~ \.php$ {
        #    proxy_pass   http://127.0.0.1;
        #}

        # pass the PHP scripts to FastCGI server listening on 127.0.0.1:9000
        #
        #location ~ \.php$ {
        #    root           html;
        #    fastcgi_pass   127.0.0.1:9000;
        #    fastcgi_index  index.php;
        #    fastcgi_param  SCRIPT_FILENAME  /scripts$fastcgi_script_name;
        #    include        fastcgi_params;
        #}

        # deny access to .htaccess files, if Apache's document root
        # concurs with nginx's one
        #
        #location ~ /\.ht {
        #    deny  all;
        #}
    }


    # another virtual host using mix of IP-, name-, and port-based configuration
    #
    #server {
    #    listen       8000;
    #    listen       somename:8080;
    #    server_name  somename  alias  another.alias;

    #    location / {
    #        root   html;
    #        index  index.html index.htm;
    #    }
    #}


    # HTTPS server
    #
    #server {
    #    listen       443 ssl;
    #    server_name  localhost;

    #    ssl_certificate      cert.pem;
    #    ssl_certificate_key  cert.key;

    #    ssl_session_cache    shared:SSL:1m;
    #    ssl_session_timeout  5m;

    #    ssl_ciphers  HIGH:!aNULL:!MD5;
    #    ssl_prefer_server_ciphers  on;

    #    location / {
    #        root   html;
    #        index  index.html index.htm;
    #    }
    #}

}



```

虚拟主机

让用户觉得在一台机器上运行了多台服务器

基于ip的

基于port的

```

server {
listen 8080;
server_name localhost;
location / {
root html;
index index.html;
}
}

```

基于域名的虚拟主机

```

server {

listen 80;
server_name www.xx.com;
location / {
root html;
index index.html;
}
}

server {
listen 80;
server_name bbs.xx.com;
location / {
root html;
index bbs.html;
}
}

server {
listen 80;
server_name ask.xx.com;
location / {
root html;
index ask.html;
}
}

```

### location

```

配置语法

location [= | ~* | ^~ ] /uri/ {...}

配置规则
location = /uri 精准匹配
location ^~ /uri 前缀匹配
location ~ /uri
location / 通用匹配

规则的优先级

1 location = /
2 location = /index
3 location ^~ /article/
4 location ^~ /article/files/
5 location ~ \.(gif|png|js|css)$
6 location /

http://192.168.11.154/
http://192.168.11.154/index ->2
http://192.168.11.154/article/files/1.txt ->4
http://192.168.11.154/mic.png ->5

1. 精准匹配是优先级最高
2. 普通匹配（最长的匹配）
3. 正则匹配

实际使用建议

location =/ {
}
location / {
}
location ~* \.(gif|....)${
}

```

## Nginx模块

反向代理、email、nginx core。。。

模块分类

1 核心模块 ngx_http_core_module

```

ngx_http_core_module

location =/ {
} l
ocation / {
} l
ocation ~* \.(gif|....)${
}root ...
}
location 实现uri到文件系统路径的映射
2. error_page

```

2 标准模块 http模块

```

ngx_http_access_module
实现基于ip的访问控制功能
1、allow address | CIDR | unix: | all;
2、deny address | CIDR | unix: | all;
自上而下检查，一旦匹配，将生效，条件严格的置前

```

3 第三方模块

如何添加第三方模块

1 原来所安装的配置，你必在重新安装新模块的时候，加上

```

configure --prefix=/data/program/nginx

```

2 不能直接make install

安装方法

```

./configure --prefix=/安装目录 --add-module = /第三方模块的目录
./configure --prefix=/data/program/nginx --with-http_stub_status_module --withhttp_random_index_module
cp objs/nginx $nginx_home/sbin/nginx

```

http_stub_status_module

```
location /status {
stub_status;
}

Active connections:当前状态，活动状态的连接数

accepts：统计总值，已经接受的客户端请求的总数

handled：统计总值，已经处理完成的客户端请求的总数
requests：统计总值，客户端发来的总的请求数
Reading：当前状态，正在读取客户端请求报文首部的连接的连接数
Writing：当前状态，正在向客户端发送响应报文过程中的连接数
Waiting：当前状态，正在等待客户端发出请求的空闲连接数

```

http_random_index_module

随机显示主页

一般情况下,一个站点默认首页都是定义好的index.html、index.shtml等等,如果想站点下有很多页面想随机展示给用户浏览,那得程序上实现，很麻烦，使用nginx的random index即可简单实现这个功能，凡是以/结尾的请求，都会随机展示当前目录下的文件作为首页

1 添加random_index on 配置，默认是关闭的

```
location / {
root html;
random_index on;
index index.html index.htm;
}

```

2 在html目录下创建多个html页面
