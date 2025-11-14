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

# Acknowledgments

Partial code and inspiration for this plugin are derived from the following projects:

- https://github.com/yuvadm/viewstate
- https://github.com/0xacb/viewgen

The MachineKey dictionary is sourced from:

- https://github.com/NotSoSecure/Blacklist3r