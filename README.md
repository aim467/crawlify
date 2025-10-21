# 🕷️ Crawlify - 分布式爬虫平台

<div align="center">

![Java](https://img.shields.io/badge/Java-11-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.6-green?style=for-the-badge&logo=spring-boot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql)
![Redis](https://img.shields.io/badge/Redis-6.0-red?style=for-the-badge&logo=redis)
![Netty](https://img.shields.io/badge/Netty-4.1.86-yellow?style=for-the-badge)
![WebMagic](https://img.shields.io/badge/WebMagic-1.0.4-purple?style=for-the-badge)

**基于Java Spring Boot的分布式爬虫平台，支持多节点协同爬取，提供灵活的配置管理和强大的数据采集能力**

[项目特性](#-项目特性) • [架构设计](#-架构设计) • [快速开始](#-快速开始) • [API文档](#-api文档) • [部署指南](#-部署指南)

</div>

---

## 📋 目录

- [项目概述](#-项目概述)
- [项目特性](#-项目特性)
- [架构设计](#-架构设计)
- [技术栈](#-技术栈)
- [快速开始](#-快速开始)
- [功能模块](#-功能模块)
- [API文档](#-api文档)
- [部署指南](#-部署指南)
- [开发指南](#-开发指南)
- [贡献指南](#-贡献指南)

---

## 🎯 项目概述

**Crawlify** 是一个分布式爬虫平台，专为大规模数据采集而设计。平台采用单服务架构，支持多节点协同工作，提供灵活的配置管理和强大的数据采集能力。

### 🎨 设计理念

- **🔄 分布式架构**: 支持多节点协同，提高爬取效率和容错能力
- **⚙️ 动态配置**: 无需修改代码即可配置新的爬虫规则
- **🛡️ 稳定可靠**: 完善的错误处理和重试机制
- **📊 实时监控**: 任务状态实时跟踪和监控
- **🔧 易于扩展**: 模块化设计，便于功能扩展

---

## ✨ 项目特性

### 🚀 核心功能

- **🌐 多网站支持**: 支持同时配置和管理多个目标网站
- **📄 动态配置**: 支持GET/POST请求，可配置请求头、Cookie等
- **📊 分页爬取**: 智能分页处理，支持多种分页模式
- **🔍 灵活解析**: 支持JSON/XML响应解析，XPath/JsonPath数据提取
- **🧹 数据清洗**: 正则表达式数据清洗和格式化
- **⚡ 高并发**: 多线程并发爬取，提高采集效率

### 🎛️ 管理功能

- **📋 任务管理**: 任务的创建、启动、停止、删除和状态跟踪
- **🖥️ 节点管理**: 爬虫节点的注册、监控和负载均衡
- **📈 实时监控**: 任务执行进度和节点状态实时监控
- **📊 数据统计**: 爬取数据统计和性能分析
- **🔐 权限控制**: 基于Sa-Token的权限管理系统

### 🔧 技术特性

- **🔄 异步处理**: 异步任务执行，提高系统响应速度
- **💾 数据持久化**: MySQL数据存储，Redis缓存加速
- **🌐 网络通信**: 基于Netty的高性能节点间通信
- **🛡️ 容错机制**: 自动重试、超时控制、异常处理
- **📝 日志记录**: 完整的操作日志和错误日志记录

---

## 🏗️ 架构设计

### 📐 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Web前端/API   │    │  爬虫节点集群    │    │   数据存储层     │
│                 │    │                 │    │                 │
│  ┌───────────┐  │    │  ┌───────────┐  │    │  ┌───────────┐  │
│  │  Platform │◄─┼────┼─►│   Node1   │  │    │  │   MySQL   │  │
│  │  Module   │  │    │  └───────────┘  │    │  └───────────┘  │
│  └───────────┘  │    │  ┌───────────┐  │    │  ┌───────────┐  │
│                 │    │  │   Node2   │  │    │  │   Redis    │  │
│  ┌───────────┐  │    │  └───────────┘  │    │  └───────────┘  │
│  │   Common  │  │    │  ┌───────────┐  │    │                 │
│  │  Module   │  │    │  │   NodeN   │  │    │                 │
│  └───────────┘  │    │  └───────────┘  │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 🧩 模块结构

```
crawlify/
├── crawlify-common/          # 公共模块
│   ├── entity/              # 实体类
│   ├── service/             # 服务接口
│   ├── mapper/              # 数据访问层
│   ├── dto/                 # 数据传输对象
│   ├── utils/               # 工具类
│   └── dynamic/             # 动态爬虫核心
├── crawlify-platform/        # 平台管理模块
│   ├── controller/          # API控制器
│   ├── config/              # 配置类
│   ├── interceptor/         # 拦截器
│   └── netty/               # Netty服务器
└── crawlify-node/           # 爬虫节点模块
    ├── downloader/          # 下载器
    ├── processor/           # 处理器
    ├── pipeline/            # 数据管道
    ├── config/              # 节点配置
    └── netty/               # Netty客户端
```

### 🔄 工作流程

1. **配置阶段** 📝
   - 配置网站基本信息
   - 设置爬虫规则和解析规则
   - 配置请求参数和数据处理逻辑

2. **任务创建** 🚀
   - 通过API创建爬虫任务
   - 任务参数验证和预处理
   - 任务状态初始化

3. **任务分发** 📤
   - 平台将任务分发给可用节点
   - 负载均衡和任务调度
   - 节点状态检查和更新

4. **数据爬取** 🕷️
   - 节点执行爬虫任务
   - 使用WebMagic框架进行页面抓取
   - 数据解析和清洗

5. **结果处理** 📊
   - 通过Pipeline处理爬取数据
   - 数据存储和索引
   - 结果统计和报告

6. **状态同步** 🔄
   - 节点向平台同步任务状态
   - 实时更新任务进度
   - 异常处理和错误报告

---

## 🛠️ 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| **Java** | 11 | 主要开发语言 |
| **Spring Boot** | 2.7.6 | 应用框架 |
| **MyBatis Plus** | 3.5.11 | ORM框架 |
| **MySQL** | 8.0 | 主数据库 |
| **Redis** | 6.0+ | 缓存数据库 |
| **Netty** | 4.1.86 | 网络通信框架 |
| **WebMagic** | 1.0.4 | 爬虫框架 |
| **OkHttp** | 4.12.0 | HTTP客户端 |
| **Sa-Token** | 1.42.0 | 权限认证 |

### 开发工具

| 工具 | 说明 |
|------|------|
| **Maven** | 项目构建工具 |
| **Lombok** | 代码简化工具 |
| **Hutool** | 工具类库 |
| **FastJSON2** | JSON处理库 |
| **Jackson** | JSON序列化库 |

---

## 🚀 快速开始

### 📋 环境要求

- **JDK**: 11+
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **Maven**: 3.6+

### 📥 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/aim467/crawlify.git
cd crawlify
```

2. **数据库初始化**
```bash
# 创建数据库
CREATE DATABASE crawlify CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 执行SQL脚本
mysql -u root -p crawlify < sql/crawlify.sql
```

3. **配置数据库连接**
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/crawlify?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 6379
    password: your_redis_password
```

4. **启动服务**
```bash
# 启动平台服务
cd crawlify-platform
mvn spring-boot:run

# 启动爬虫节点
cd crawlify-node
mvn spring-boot:run
```

## 🚀 部署指南

### ☁️ 云服务器部署

1. **环境准备**
```bash
# 安装Java 11
sudo apt update
sudo apt install openjdk-11-jdk
e
# 安装MySQL
sudo apt install mysql-server

# 安装Redis
sudo apt install redis-server
```

2. **应用部署**
```bash
# 创建应用目录
sudo mkdir -p /opt/crawlify
cd /opt/crawlify

# 上传应用文件
scp crawlify-platform.jar user@server:/opt/crawlify/
scp crawlify-node.jar user@server:/opt/crawlify/

# 创建服务文件
sudo nano /etc/systemd/system/crawlify-platform.service
sudo nano /etc/systemd/system/crawlify-node.service

# 启动服务
sudo systemctl enable crawlify-platform
sudo systemctl start crawlify-platform
sudo systemctl enable crawlify-node
sudo systemctl start crawlify-node
```

---

## 👨‍💻 开发指南

### 🔧 开发环境搭建

1. **IDE配置**
   - 推荐使用IntelliJ IDEA
   - 安装Lombok插件
   - 配置Maven设置

    
## 🤝 贡献指南

### 📋 贡献方式

1. **报告Bug**
   - 使用GitHub Issues报告bug
   - 提供详细的复现步骤
   - 包含错误日志和系统信息

2. **功能建议**
   - 在GitHub Discussions中讨论新功能
   - 提供详细的功能描述和用例
   - 考虑实现的可行性

3. **代码贡献**
   - Fork项目并创建功能分支
   - 编写测试用例
   - 提交Pull Request

### 📄 许可证

本项目采用 [MIT License](LICENSE) 开源许可证。

---


---

<div align="center">

**如果这个项目对您有帮助，请给个 ⭐ Star 支持一下！**

Made with ❤️ by [aim467](https://github.com/aim467)

</div>
    