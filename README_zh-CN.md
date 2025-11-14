# ViewState-Cracker

ASP.net ViewState密钥被动扫描爆破BurpSuite插件。 

# 下载

预编译包：[Releases](https://github.com/whwlsfb/ViewState-Cracker/releases)

# 编译
需要Maven、JDK 17。

```
$ mvn package
```

# 使用

将`machineKeys.txt`下载并放置于插件JAR的相同目录下，然后使用BurpSuite（建议版本大于2024.10）安装插件即可启用。

插件将自动提取请求、响应流量中的ViewState相关数据，在发现无签名或者密钥爆破成功的ViewState后，将会自动生成一个BurpSuite的Issue条目。

扫描、爆破过程中将不会产生请求。


# 鸣谢
插件中部分代码与灵感借鉴于以下项目

- https://github.com/yuvadm/viewstate
- https://github.com/0xacb/viewgen

MachineKey字典来源于以下项目

- https://github.com/NotSoSecure/Blacklist3r