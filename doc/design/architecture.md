# MyFocusme 项目结构详解

## 目录结构总览

```
MyFocusme/
├── 📁 src/                          # 源代码目录
│   ├── 📁 main/                     # 主要源代码
│   │   ├── 📁 java/com/tsymq/       # Java 源代码包
│   │   └── 📁 resources/            # 资源文件
│   └── 📁 test/                     # 测试代码
│       └── 📁 java/com/tsymq/       # 测试源代码包
├── 📁 config/                       # 配置文件模板
├── 📁 scripts/                      # 构建和部署脚本
├── 📁 doc/                          # 项目文档
├── 📁 target/                       # Maven 构建输出
├── 📄 pom.xml                       # Maven 项目配置
├── 📄 README.md                     # 项目说明
└── 📄 .gitignore                    # Git 忽略规则
```

## 核心包结构

### 1. 主包 (`com.tsymq`)

#### 根级别类
- **Main.java** - 应用程序入口点
- **AppBlocker.java** - 核心屏蔽逻辑实现
- **AppBlockerController.java** - 主界面控制器
- **CommandUtil.java** - 系统命令工具类

### 2. 模式管理包 (`com.tsymq.mode`)

专门处理应用的双模式系统：

- **ModeManager.java** - 模式管理核心类
  - 管理普通模式和学习模式的切换
  - 处理模式状态的持久化
  - 提供模式查询和控制接口

- **ModeState.java** - 模式状态数据类
  - 封装当前模式信息
  - 支持序列化和反序列化
  - 包含模式切换时间戳

- **TimeManager.java** - 时间管理器
  - 处理学习模式的倒计时
  - 管理自动模式切换
  - 提供时间相关的回调机制

### 3. 配置管理包 (`com.tsymq.config`)

统一管理应用配置：

- **ConfigManager.java** - 配置管理器
  - 统一的配置文件读写接口
  - 支持 JSON 格式配置
  - 提供配置验证和默认值

- **UserConfig.java** - 用户配置数据类
  - 用户个性化设置
  - 学习偏好配置
  - 界面显示选项

- **AppConfig.java** - 应用配置常量
  - 应用级别的配置常量
  - 系统默认值定义
  - 路径和文件名常量

- **BlockedSitesConfig.java** - 屏蔽网站配置
  - 屏蔽网站列表管理
  - 网站分类功能
  - 屏蔽规则配置

### 4. UI管理包 (`com.tsymq.ui`)

处理用户界面相关逻辑：

- **UIStateManager.java** - UI状态管理器
  - 根据模式切换界面显示
  - 管理UI组件的可见性
  - 处理界面状态同步

- **FocusModeDialogController.java** - 模式切换对话框控制器
  - 学习模式切换界面逻辑
  - 时长选择和确认
  - 用户交互处理

- **NotificationManager.java** - 通知管理器
  - 系统通知显示
  - 托盘图标管理
  - 用户提醒功能

### 5. 工具类包 (`com.tsymq.utils`)

提供通用工具功能：

- **TimeUtils.java** - 时间工具类
  - 时间格式化方法
  - 时长计算工具
  - 时间戳处理

- **SecurityUtils.java** - 安全工具类
  - 密码验证功能
  - 加密和哈希工具
  - 安全检查方法

- **ProcessWatchdog.java** - 进程监控工具
  - 应用进程保护
  - 系统进程监控
  - 防止恶意终止

## 资源文件结构

### FXML 界面文件
- **AppBlocker.fxml** - 主界面布局
- **FocusModeDialog.fxml** - 模式切换对话框

### CSS 样式文件
- **styles.css** - 主要样式定义
- **focus-mode-styles.css** - 学习模式专用样式

### 图标和资源
- **myfocus.png** - 应用图标

## 测试结构

### 单元测试
- **ModeManagerTest.java** - 模式管理器测试
- **ConfigManagerTest.java** - 配置管理器测试
- **TimeUtilsTest.java** - 时间工具类测试
- **CommandUtilTest.java** - 命令工具类测试

## 配置文件模板

### 应用配置
- **app-config.json** - 应用级配置模板
- **blocked-sites.json** - 屏蔽网站配置模板

## 构建脚本

### Shell 脚本
- **build.sh** - 项目构建脚本
- **run.sh** - 应用运行脚本

## 文档目录

### 项目文档
- **modification_plan.md** - 详细修改计划
- **project-structure.md** - 项目结构说明（本文档）

## 设计原则

### 1. 模块化设计
- 每个包负责特定的功能领域
- 清晰的接口和依赖关系
- 便于维护和扩展

### 2. 分层架构
- UI层：界面控制和用户交互
- 业务层：核心业务逻辑
- 数据层：配置和状态管理
- 工具层：通用工具和服务

### 3. 配置驱动
- 外部配置文件控制行为
- 支持运行时配置修改
- 提供合理的默认值

### 4. 可测试性
- 每个模块都有对应的测试
- 依赖注入便于测试
- 清晰的接口定义

## 扩展指南

### 添加新功能
1. 确定功能所属的包
2. 创建相应的类和接口
3. 添加配置支持（如需要）
4. 编写单元测试
5. 更新文档

### 修改现有功能
1. 识别影响范围
2. 更新相关类和接口
3. 修改配置文件（如需要）
4. 更新测试用例
5. 验证向后兼容性

这种结构设计确保了代码的可维护性、可扩展性和可测试性，为项目的长期发展奠定了良好的基础。 