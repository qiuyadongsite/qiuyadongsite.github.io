---
layout: post
title:  docker安装mysql试验
date:   2019-01-03 16:52:12 +08:00
category: 微服务架构
tags: docker
comments: true
---

* content
{:toc}

对于微服务来说，容器化技术中的docker是当前最重要的工具之一，这里就简单进行搭建使用！












## 安装docker

Docker 运行在 CentOS 7 上，要求系统为64位、系统内核版本为 3.10 以上;
- 1、安装一些必要的系统工具：
```
sudo yum install -y yum-utils device-mapper-persistent-data lvm2
```
- 2、 添加软件源信息：
```
sudo yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
```
- 3、 更新 yum 缓存：
```
sudo yum makecache fast
```
- 4、 安装 Docker-ce：
```
sudo yum -y install docker-ce
```
- 5、 启动 Docker 后台服务
```
sudo systemctl start docker
```

## 使用docker创建mysql镜像

- 1、 启动docker服务：    
```
service docker start
```
- 2、主机新建DockerFile:    
```

FROM mysql:5.7

#设置免密登录
ENV MYSQL_ALLOW_EMPTY_PASSWORD yes

#将所需文件放到容器中
COPY setup.sh /mysql/setup.sh
COPY schema.sql /mysql/schema.sql
COPY privileges.sql /mysql/privileges.sql

#设置容器启动时执行的命令
CMD ["sh", "/mysql/setup.sh"]

```
- 3、 编写容器启动脚本setup.sh：
```
#!/bin/bash
set -e

#查看mysql服务的状态，方便调试，这条语句可以删除
echo `service mysql status`

echo '1.启动mysql....'
#启动mysql
service mysql start
sleep 3
echo `service mysql status`

echo '2.开始导入数据....'
#导入数据
mysql < /mysql/schema.sql
echo '3.导入数据完毕....'

sleep 3
echo `service mysql status`

#重新设置mysql密码
echo '4.开始修改密码....'
mysql < /mysql/privileges.sql
echo '5.修改密码完毕....'

#sleep 3
echo `service mysql status`
echo `mysql容器启动完毕,且数据导入成功`

tail -f /dev/null
```
- 4、 需要导入数据的mysql脚本命令schema.sql：(注意这里不能用‘’括表名)
```
-- 创建数据库
create database docker_mysql default character set utf8 collate utf8_general_ci;

use docker_mysql;

-- 建表
DROP TABLE IF EXISTS user;

CREATE TABLE user (
 id bigint(20) NOT NULL,
 created_at bigint(40) DEFAULT NULL,
 last_modified bigint(40) DEFAULT NULL,
 email varchar(255) DEFAULT NULL,
 first_name varchar(255) DEFAULT NULL,
 last_name varchar(255) DEFAULT NULL,
 username varchar(255) DEFAULT NULL,
 PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- 插入数据
INSERT INTO user (id, created_at, last_modified, email, first_name, last_name, username)
VALUES
  (0,1490257904,1490257904,'john.doe@example.com','John','Doe','user');

```
- 5、 mysql权限设置命令privileges.sql：
```
use mysql;
select host, user from user;
-- 因为mysql版本是5.7，因此新建用户为如下命令：
create user docker identified by '123456';
-- 将docker_mysql数据库的权限授权给创建的docker用户，密码为123456：
grant all on *.* to docker@'%' identified by '123456' with grant option;
-- 这一条命令一定要有：
flush privileges;
```
- 6、 创建镜像
```
docker build -t docker-mysql-1 .
```
- 7、启动容器
```
docker run -d -p 3808:3306 docker-mysql-1
```
- 8、查看日志
```
docker logs 容器Id
```
## 验证
- 1、 进入容器
```
docker exec -it 容器ID /bin/bash

```
- 2、查看数据库
```
使用docker用户登录数据库：mysql -u docker -p
输入密码123456通过登录验证
切换至docker_mysql数据库：use docker_mysql;
查看数据库中的表：show tables;
查看表中的数据：select * from user;
```

## 补充
- 1、 查看所有容器: docker ps -a

- 2、 删除镜像： docker rmi 镜像Id

- 3、 删除单个容器: docker rm 容器ID

- 4、删除所有容器: docker rm -f $(docker ps -a -q)

- 5、修改文件名：mv
