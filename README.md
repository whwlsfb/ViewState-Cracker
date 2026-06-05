# ViewState-Cracker

A BurpSuite extension for passive scanning and brute-forcing ASP.NET ViewState keys.

English | [简体中文](./README_zh-CN.md)

# Download

Precompiled package: [Releases](https://github.com/whwlsfb/ViewState-Cracker/releases)

# Compilation

Requires Maven and JDK 17.

```
$ mvn package
```

# Usage

Download `machineKeys.txt` and place it in the same directory as the plugin JAR. Then, install the plugin in BurpSuite (recommended version 2024.10 or later) to enable it.

The plugin will automatically extract ViewState-related data from request and response traffic. When it detects an unsigned ViewState or successfully brute-forces a key, it will automatically generate a BurpSuite issue entry.

No requests will be generated during the scanning and brute-forcing process.

# What's new in 1.3

Version 1.3 keeps the original passive scan behavior, and adds a more direct workflow for users who do not want to depend on BurpSuite passive scanning being enabled first.

- Adds a dedicated `ViewState-Cracker` suite tab.
- Adds a context menu action for Proxy, Repeater, Target, and Logger requests: `Send to ViewState-Cracker`.
- Supports manually scanning the currently loaded request/response from the extension tab.
- Adds UI controls for passive scan registration, duplicate host skipping, key brute-forcing, result clearing, host cache reset, key dictionary reload, and adding found issues to the Site map.
- Adds Chinese/English UI switching from the extension tab.

The main difference from the original workflow is that you can now send a packet directly from Repeater or other Burp tools to the extension and scan it immediately, without first relying on passive scan traffic.

# Acknowledgments

Partial code and inspiration for this plugin are derived from the following projects:

- https://github.com/yuvadm/viewstate
- https://github.com/0xacb/viewgen

The MachineKey dictionary is sourced from:

- https://github.com/NotSoSecure/Blacklist3r
