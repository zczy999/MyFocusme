# MyFocusme - 专注学习应用

## 项目概述

MyFocusme 是一个基于 JavaFX 的专注学习应用，支持双模式切换：
- **普通模式**：正常使用，不进行网站屏蔽
- **学习模式**：启用网站屏蔽，帮助用户专注学习

## 技术栈

- **Java 11**
- **JavaFX 17**
- **Maven** - 项目管理
- **Jackson** - JSON 处理

## 项目结构

```
MyFocusme/
├── src/
│   ├── main/
│   │   ├── java/com/tsymq/
│   │   │   ├── Main.java                    # 应用入口
│   │   │   ├── AppBlocker.java             # 核心屏蔽逻辑
│   │   │   ├── AppBlockerController.java   # 主界面控制器
│   │   │   ├── CommandUtil.java            # 命令工具类
│   │   │   ├── mode/                       # 模式管理模块
│   │   │   │   ├── ModeManager.java        # 模式管理器
│   │   │   │   ├── ModeState.java          # 模式状态数据
│   │   │   │   └── TimeManager.java        # 时间管理器
│   │   │   ├── config/                     # 配置管理模块
│   │   │   │   ├── ConfigManager.java      # 配置管理器
│   │   │   │   ├── UserConfig.java         # 用户配置
│   │   │   │   ├── AppConfig.java          # 应用配置常量
│   │   │   │   └── BlockedSitesConfig.java # 屏蔽站点配置
│   │   │   ├── ui/                         # UI管理模块
│   │   │   │   ├── UIStateManager.java     # UI状态管理
│   │   │   │   ├── FocusModeDialogController.java # 模式切换对话框
│   │   │   │   └── NotificationManager.java # 通知管理
│   │   │   └── utils/                      # 工具类模块
│   │   │       ├── TimeUtils.java          # 时间工具
│   │   │       ├── SecurityUtils.java      # 安全工具
│   │   │       └── ProcessWatchdog.java    # 进程监控
│   │   └── resources/
│   │       ├── AppBlocker.fxml             # 主界面
│   │       ├── FocusModeDialog.fxml        # 模式切换对话框
│   │       ├── styles.css                  # 主样式文件
│   │       ├── focus-mode-styles.css       # 学习模式样式
│   │       └── myfocus.png                 # 应用图标
│   └── test/
│       └── java/com/tsymq/
│           └── CommandUtilTest.java        # 测试文件
├── target/                                 # 构建输出目录
├── doc/                                    # 文档目录
│   └── modification_plan.md               # 修改计划文档
├── pom.xml                                 # Maven 配置
└── README.md                               # 项目说明
```

## 核心模块说明

### 1. 模式管理模块 (`mode/`)
- **ModeManager**: 核心模式管理器，处理普通模式和学习模式的切换
- **ModeState**: 模式状态数据类，用于状态持久化
- **TimeManager**: 时间管理器，处理学习模式的倒计时和自动退出

### 2. 配置管理模块 (`config/`)
- **ConfigManager**: 统一配置管理，处理配置文件的读写
- **UserConfig**: 用户个人配置数据
- **AppConfig**: 应用级配置常量
- **BlockedSitesConfig**: 屏蔽网站配置管理

### 3. UI管理模块 (`ui/`)
- **UIStateManager**: UI状态管理器，根据模式切换界面显示
- **FocusModeDialogController**: 学习模式切换对话框控制器
- **NotificationManager**: 系统通知管理

### 4. 工具类模块 (`utils/`)
- **TimeUtils**: 时间相关工具方法
- **SecurityUtils**: 安全相关工具，包括密码验证
- **ProcessWatchdog**: 进程监控和保护

## 主要功能

### 双模式系统
- **普通模式**: 应用正常运行，不执行任何屏蔽操作
- **学习模式**: 启用网站屏蔽，支持时间设定和倒计时

### 网站屏蔽
- 监控 Microsoft Edge 浏览器
- 支持自定义屏蔽网站列表
- 实时检测和阻止访问

### 时间管理
- 学习模式时长设定（15分钟 - 8小时）
- 实时倒计时显示
- 自动模式切换

### 状态持久化
- 应用重启后恢复模式状态
- 配置文件自动保存
- 支持紧急退出机制

## 构建和运行

### 环境要求
- Java 11 或更高版本
- Maven 3.6 或更高版本

### 构建项目
```bash
mvn clean compile
```

### 运行应用
```bash
mvn javafx:run
```

### 打包应用
```bash
mvn clean package
```

## 开发计划

详细的开发计划和进度请参考 `doc/modification_plan.md` 文件。

## 许可证

本项目仅供学习和个人使用。 