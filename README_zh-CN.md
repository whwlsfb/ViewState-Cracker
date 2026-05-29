# ViewState-Cracker

ASP.net ViewState密钥被动扫描爆破BurpSuite插件。 

[English](./README.md) | 简体中文

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

# 1.3 新增内容

1.3 版本保留原有被动扫描能力，同时增加了更直接的使用方式，避免用户必须先开启 BurpSuite 被动扫描才能触发插件工作。

- 新增独立的 `ViewState-Cracker` 标签页。
- 在 Proxy、Repeater、Target、Logger 的数据包右键菜单中新增 `发送到 ViewState-Cracker`。
- 支持在插件标签页中对当前加载的请求/响应进行手动扫描。
- 新增多个控制项：被动扫描开关、重复 Host 跳过开关、密钥爆破开关、清空结果、重置 Host 缓存、重载密钥字典、是否将发现的问题写入 Site map。
- 新增中英文 UI 切换按钮。

与原版工作流最直接的区别是：现在可以从 Repeater 或其他 Burp 工具里直接把数据包发送到插件并立即扫描，不再必须先依赖被动扫描流量触发。


# 鸣谢
插件中部分代码与灵感借鉴于以下项目

- https://github.com/yuvadm/viewstate
- https://github.com/0xacb/viewgen

MachineKey字典来源于以下项目

- https://github.com/NotSoSecure/Blacklist3r
