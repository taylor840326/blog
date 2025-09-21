## 使用ProxySQL实现分库和分表
-----

## 1. 简述

数据库的分库和分表是一个老生常谈的话题，原因不做过多解释。

目前常用的、社区积极维护的分库和分表方案有如下几种:

1. ShardingShpere JDBC
2. ProxySQL
3. MyCat

其中第一种是在客户端层实现，通过创建不同数据源然后按照一定规则进行库和表的分割；第二和第三种都是数据库代理层实现。

本文档主要描述如何通过ProxySQL查询规则简单实现数据库的分库和分表。

## 2. 技术实现思路

MySQL数据库中的SQL语句支持注释，例如SELECT /*!100051*/ FROM testdb.t1中/**/中的内容就是注释。

ProxySQL数据库代理可以通过正则表达式来匹配查询规则，进而可以把请求分发给不同的后端数据库实例。

通过在SQL注释中添加一些具有业务意义的字符，可以让ProxySQL去匹配到这些字符进而可以把请求分发给最终的数据库实例。

## 3. 环境依赖

本文档涉及的软件包括MySQL和ProxySQL两个

其中MySQL的版本是5.7，ProxySQL的版本是2.0.17

本文假设已经启动好2个MySQL实例和一个ProxySQL实例。

## 3. 实现步骤

### 3.1. 添加用户

首先在MySQL实例上创建1个用户，用于测试。

```sql
创建业务系统用户dev
> CREATE USER 'dev'@'%' IDENTIFIED BY 'dev';
> GRANT ALL ON *.* TO 'dev'@'%';
```

在ProxySQL代理上添加这个用户

```sql
> INSERT INTO mysql_users(usernane,password,active,default_hostgroup,transaction_persistent,max_connections)
  VALUES('dev','dev',1,0,1,1000);
> LOAD MYSQL USERS TO RUNTIME;SAVE MYSQL USERS TO DISK;
```

### 3.2. 添加两个MySQL实例

这两个MySQL实例就是未来保存分库数据的实例。

```sql
> INSERT INTO mysql_servers(hostgroup_id,hostname,port,weight,max_connections,comment)
  VALUES(0,'192.168.56.11',3306,1,1000,'SHARD2020');
> INSERT INTO mysql_servers(hostgroup_id,hostname,port,weight,max_connections,comment)
  VALUES(0,'192.168.56.11',3306,1,1000,'SHARD2021');
> LOAD MYSQL SERVERS TO RUNTIME;SAVE MYSQL SERVERS TO DISK;
```

### 3.3. 添加查询规则

我们这次通过注释中的关键字进行分片，关键字有SHARD2020和SHARD2021.

```sql
把请求中包含SHARD2020关键字的语句全部分发给主机组0，SHARD2021的请求全部分发给主机组1.
> INSERT INTO mysql_query_rules(rule_id,active,apply,match_pattern,destination_hostgroup,comment)
  VALUES(1,1,1,'SHARD2020',0,'SHARD2020');
> INSERT INTO mysql_query_rules(rule_id,active,apply,match_pattern,destination_hostgroup,comment)
  VALUES(1,1,1,'SHARD2021',1,'SHARD2021');
> LOAD MYSQL QUERY RULES TO RUNTIME;SAVE MYSQL QUERY RULES TO DISK;
```

### 3.4. 测试

使用mysql命令行客户端连接到代理进行测试

```bash
$ mysql -h 127.0.0.1 -P6033 -udev -pdev -c
注意一定要加上-c选项，否则客户端默认过滤掉注释
> INSERT /*SHARD2020*/ INTO testdb.t1 VALUES(1,1,1);
> INSERT /*SHARD2021*/ INTO testdb.t1 VALUES(5,5,5);
> SELECT /*SHARD2020*/ * FROM testdb.t1;
> SELECT /*SHARD2021*/ * FROM testdb.t1;
```

连接到代理的管理端口查看请求分发情况

```sql
$ mysql -h 127.0.0.1 -P6032 -uadmin -padmin
> SELECT * FROM stats.stats_mysql_query_digest;
mysql> select hostgroup,schemaname,username,digest,digest_text from stats.stats_mysql_query_digest;
+-----------+--------------------+----------+--------------------+-------------------------------------+
| hostgroup | schemaname         | username | digest             | digest_text                         |
+-----------+--------------------+----------+--------------------+-------------------------------------+
| 0         | information_schema | monitor  | 0x226CD90D52A2BA0B | select @@version_comment limit ?    |
| 1         | information_schema | monitor  | 0x447DBFBC0B483620 | INSERT INTO testdb.t1 VALUES(?,?,?) |
| 0         | information_schema | monitor  | 0x447DBFBC0B483620 | INSERT INTO testdb.t1 VALUES(?,?,?) |
| 1         | information_schema | monitor  | 0x4AEAB1A1BBE601B2 | SELECT * FROM testdb.t1             |
| 0         | information_schema | monitor  | 0x4AEAB1A1BBE601B2 | SELECT * FROM testdb.t1             |
| 0         | information_schema | monitor  | 0x1C46AE529DD5A40E | SELECT ?                            |
+-----------+--------------------+----------+--------------------+-------------------------------------+
6 rows in set (0.00 sec)
```

通过查看主机组可以返现，不同的关键字请求被发送给了不同的MySQL实例。

