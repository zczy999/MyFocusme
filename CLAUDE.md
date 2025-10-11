# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目简介

MyFocusme 是一个基于 JavaFX 的 macOS 专注学习应用，通过双模式系统帮助用户管理网站访问：
- **普通模式**：仅启用基础屏蔽功能（硬编码的不良网站）
- **学习模式**：启用完整屏蔽功能（基础 + 用户自定义网站）

## 技术栈

- Java 11
- JavaFX 17
- Maven 项目管理
- Jackson JSON 处理
- AppleScript 用于 macOS 系统集成

## 开发命令

### 构建和运行
```bash
# 编译项目
mvn clean compile

# 运行应用
mvn javafx:run

# 打包应用
mvn clean package

# 运行测试
mvn test

# 运行完整测试套件
mvn test -Dtest=TestSuite

# 运行单个测试类
mvn test -Dtest=ModeManagerTest

# 使用脚本快速构建
./scripts/build.sh

# 使用脚本快速运行
./scripts/run.sh
```

### 打包 Mac 应用
```bash
# 打包为 Mac 应用程序（最小化版本）
./scripts/package-mac-minimal.sh
```

## 核心架构设计

### 1. 双模式系统架构

**核心原则**：用户自定义屏蔽功能仅在学习模式下生效，基础屏蔽功能在所有模式下生效。

- **ModeManager** (`mode/ModeManager.java`): 模式管理核心
  - 管理模式切换（普通模式 ↔ 学习模式）
  - 学习模式时间管理和倒计时
  - 限制：17:00 后禁止切换到学习模式
  - 每天 17:00 自动切换回普通模式

- **ModeState** (`mode/ModeState.java`): 模式状态数据类
  - 存储当前模式、学习时长、结束时间等状态
  - 支持序列化和反序列化

- **AppBlocker** (`AppBlocker.java`): 网站屏蔽核心逻辑
  - 监控 Microsoft Edge 浏览器活动标签页
  - 根据当前模式决定屏蔽策略（通过 `shouldBlock()` 方法）
  - 硬编码屏蔽（所有模式）：`BlockedSitesConfig.isHardcodedBlocked()`
  - 用户自定义屏蔽（仅学习模式）：`isBlocked()`

### 2. 配置管理架构

- **ConfigManager** (`config/ConfigManager.java`): 统一配置管理
  - 注意：模式状态不再持久化，每次启动都从默认状态（普通模式）开始
  - 用户配置（屏蔽网站列表）仍会持久化

- **AppConfig** (`config/AppConfig.java`): 应用级常量
  - 配置文件路径、时间限制、监控间隔等

- **BlockedSitesConfig** (`config/BlockedSitesConfig.java`): 屏蔽站点配置
  - 硬编码的不良网站列表（所有模式下生效）
  - 用户自定义屏蔽网站列表（仅学习模式生效）

### 3. UI 管理架构

- **AppBlockerController** (`AppBlockerController.java`): 主界面控制器
  - 协调 ModeManager 和 AppBlocker
  - 处理用户交互和界面更新

- **UIStateManager** (`ui/UIStateManager.java`): UI 状态管理
  - 根据当前模式更新界面显示
  - 管理学习模式和普通模式的不同样式

- **FocusModeDialogController** (`ui/FocusModeDialogController.java`): 模式切换对话框
  - 处理学习模式时长选择
  - 验证切换条件（如 17:00 时间限制）

## 重要的业务逻辑

### 模式切换规则

1. **切换到学习模式的限制**：
   - 17:00 后禁止切换（`ModeManager.switchToFocusMode()` 第 80-85 行）
   - 时长必须在 15 分钟到 8 小时之间
   - 已在学习模式时不能重复切换

2. **自动模式切换**：
   - 每天 17:00 自动切换到普通模式（`ModeManager.scheduleDailySwitch()`）
   - 学习模式倒计时结束后自动切换到普通模式

3. **状态持久化策略**：
   - 应用启动时总是进入普通模式（不恢复之前的状态）
   - 窗口默认隐藏，通过托盘图标显示/隐藏
   - 关闭窗口不会退出应用，只是隐藏窗口

### 网站屏蔽逻辑

1. **硬编码屏蔽**（所有模式下生效）：
   - 在 `BlockedSitesConfig` 中维护
   - 检测到访问时直接关闭标签页

2. **用户自定义屏蔽**（仅学习模式）：
   - 通过 `AppBlocker.shouldBlock()` 判断是否应该执行
   - 仅当 `modeManager.isInFocusMode()` 返回 true 时才生效
   - 检测到访问时打开新的空白标签页

3. **白名单机制**：
   - 白名单中的网站不受任何屏蔽限制
   - 配置文件位置：`config/whiteWebsites.txt`

### AppleScript 集成

应用使用 AppleScript 与 macOS 系统交互：
- 获取活动应用名称
- 获取 Microsoft Edge 当前标签页 URL 和标题
- 关闭标签页或应用程序
- 所有 AppleScript 命令通过 `CommandUtil.executeAppleScript()` 执行

## 代码约定

### 命名规范
- 类名使用 PascalCase
- 方法名使用 camelCase
- 常量使用 UPPER_SNAKE_CASE
- 包名使用小写字母，按功能模块组织

### 模块组织
```
com.tsymq/
├── Main.java              # 应用入口
├── AppBlocker.java        # 屏蔽核心逻辑
├── AppBlockerController.java  # 主界面控制器
├── mode/                  # 模式管理模块
├── config/                # 配置管理模块
├── ui/                    # UI管理模块
└── utils/                 # 工具类模块
```

### 重要设计原则

1. **关注点分离**：
   - ModeManager 只负责模式管理，不关心 UI
   - AppBlocker 只负责屏蔽逻辑，不关心模式管理
   - Controller 负责协调各个模块

2. **模式感知**：
   - 所有需要根据模式调整行为的组件都应该依赖 ModeManager
   - 通过观察者模式监听模式变化（`setModeChangeListener`）

3. **时间管理**：
   - 使用 ScheduledExecutorService 进行定时任务
   - 学习模式结束时间使用绝对时间戳，避免累积误差

## 常见开发任务

### 添加新的屏蔽网站（硬编码）
在 `BlockedSitesConfig.java` 的 `HARDCODED_BLOCKED_SITES` 集合中添加。

### 修改学习模式时间限制
在 `AppConfig.java` 中修改 `MIN_FOCUS_DURATION_MINUTES` 和 `MAX_FOCUS_DURATION_MINUTES`。

### 修改定时切换时间
在 `ModeManager.java` 构造函数中修改 `scheduleDailySwitch(17, 0)` 的参数。

### 调整监控间隔
在 `AppConfig.java` 中修改 `MONITOR_INTERVAL_MS` 常量。

## 配置文件位置

所有配置文件位于：`config/`
- `blockedWebsites.txt` - 用户自定义屏蔽网站列表
- `whiteWebsites.txt` - 白名单网站列表
- `userConfig.json` - 用户配置
- `modeState.json` - 模式状态（已禁用持久化）

## 测试架构

### 测试框架
- JUnit 5 (Jupiter) - 主测试框架
- AssertJ - 流式断言库
- Mockito - Mock 框架
- TestFX - JavaFX UI 测试框架
- Jackson - JSON 序列化测试

### 测试文件结构
```
src/test/java/com/tsymq/
├── TestSuite.java                    # 测试套件入口
├── mode/
│   ├── ModeManagerTest.java         # 模式管理器测试
│   └── ModeStateTest.java           # 模式状态测试
├── config/
│   ├── BlockedSitesConfigTest.java  # 屏蔽网站配置测试
│   └── UserConfigTest.java          # 用户配置测试
└── utils/
    └── TimeUtilsTest.java           # 时间工具测试
```

### 测试覆盖范围
- **模式管理**：模式切换、时间管理、17:00限制、并发安全
- **配置管理**：用户配置、屏蔽网站配置、JSON序列化
- **工具类**：时间格式化、时间计算、边界条件

## 注意事项

1. **macOS 专属**：应用使用 AppleScript，仅支持 macOS 系统
2. **浏览器限制**：目前仅支持 Microsoft Edge 浏览器的监控
3. **权限要求**：需要辅助功能权限才能监控浏览器标签页
4. **状态管理**：应用启动时总是从普通模式开始，不恢复之前的状态
5. **时间限制**：17:00 后无法切换到学习模式，这是硬编码的业务规则
6. **测试环境**：JavaFX 测试需要 headless 模式，配置在 pom.xml 中
7. **测试运行**：某些测试（如17:00限制测试）结果依赖于运行时间

## 测试架构更新 (2025-10-11)

### 测试套件组成
- **总测试数**: 139个测试，分布在7个测试类
- **AppBlockerTest**: 20个测试 - 核心屏蔽逻辑测试
- **ModeManagerTest**: 18个测试 - 模式管理测试（含时间敏感测试）
- **ModeStateTest**: 17个测试 - 模式状态测试
- **BlockedSitesConfigTest**: 34个测试 - 屏蔽配置测试
- **UserConfigTest**: 18个测试 - 用户配置测试
- **TimeUtilsTest**: 32个测试 - 时间工具测试

### 关键更新

1. **AppBlocker.block() 方法优化**
   - 现支持所有格式的屏蔽项，不仅限于完整URL
   - 可屏蔽关键词如 "facebook" 而非只能屏蔽 "https://facebook.com"

2. **时间敏感测试处理**
   - ModeManagerTest 中的测试会检测当前时间
   - 17:00后自动跳过或调整无法执行的学习模式测试
   - 确保测试在任何时间都能稳定运行

3. **JaCoCo 测试覆盖率**
   - 已配置JaCoCo插件（版本0.8.11）
   - 运行 `mvn clean test jacoco:report` 生成覆盖率报告
   - 报告位置：`target/site/jacoco/index.html`
   - 最低覆盖率要求：60%

### 运行测试

```bash
# 运行所有测试
mvn test

# 运行测试套件
mvn test -Dtest=TestSuite

# 生成覆盖率报告
mvn clean test jacoco:report

# 查看覆盖率报告
open target/site/jacoco/index.html
```