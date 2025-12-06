# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目简介

MyFocusme 是一个基于 JavaFX 的 macOS 专注学习应用，通过双模式系统帮助用户管理网站访问：
- **普通模式**：仅启用基础屏蔽功能（硬编码的不良网站）
- **学习模式**：启用完整屏蔽功能（基础 + 用户自定义网站）

## 技术栈

- Java 11 / JavaFX 17
- Maven 项目管理
- Jackson JSON 处理
- AppleScript 用于 macOS 系统集成

## 开发命令

```bash
# 编译项目
mvn clean compile

# 运行应用
mvn javafx:run

# 运行测试
mvn test

# 运行单个测试类
mvn test -Dtest=ModeManagerTest

# 生成覆盖率报告（最低60%）
mvn clean test jacoco:report
open target/site/jacoco/index.html

# 打包 Mac 应用（最小化版本）
./scripts/package-mac-minimal.sh
```

## 核心架构设计

### 双模式系统

**核心原则**：用户自定义屏蔽功能仅在学习模式下生效，基础屏蔽功能在所有模式下生效。

| 组件 | 路径 | 职责 |
|------|------|------|
| ModeManager | `mode/ModeManager.java` | 模式切换、时间管理、17:00限制 |
| ModeState | `mode/ModeState.java` | 模式状态数据、序列化 |
| AppBlocker | `AppBlocker.java` | 网站屏蔽核心逻辑、Edge 监控 |

### 配置管理

| 组件 | 路径 | 职责 |
|------|------|------|
| ConfigManager | `config/ConfigManager.java` | 统一配置管理（注：模式状态不持久化） |
| AppConfig | `config/AppConfig.java` | 应用级常量 |
| BlockedSitesConfig | `config/BlockedSitesConfig.java` | 屏蔽站点配置 |

### UI 管理

| 组件 | 路径 | 职责 |
|------|------|------|
| AppBlockerController | `AppBlockerController.java` | 主界面控制器 |
| UIStateManager | `ui/UIStateManager.java` | UI 状态管理 |
| FocusModeDialogController | `ui/FocusModeDialogController.java` | 模式切换对话框 |

## 重要业务规则

### 模式切换限制
- **17:00 后禁止**切换到学习模式（硬编码规则）
- 时长范围：15 分钟 - 8 小时
- 每天 17:00 自动切换回普通模式

### 网站屏蔽策略
- **硬编码屏蔽**（所有模式）：`BlockedSitesConfig.isHardcodedBlocked()` → 关闭标签页
- **用户自定义屏蔽**（仅学习模式）：`AppBlocker.shouldBlock()` → 打开空白页

### AppleScript 集成
所有 macOS 系统调用通过 `CommandUtil.executeAppleScript()` 执行，包括：
- 获取 Edge 活动标签页 URL/标题
- 关闭标签页或应用程序

## 配置文件

运行时配置位于 `~/.config/myfocusme/`：
- `blockedWebsites.txt` - 用户自定义屏蔽网站
- `whiteWebsites.txt` - 白名单网站
- `launchagent.log` - LaunchAgent 日志

## 系统级保活（LaunchAgent）

防止应用被关闭，实现开机自启动和进程守护。

### 安装保活
```bash
# 前提：应用已安装到 /Applications/MyFocusme.app
./scripts/install-launchagent.sh
```

### 卸载保活
```bash
./scripts/uninstall-launchagent.sh
```

### 保活效果

| 场景 | 效果 |
|------|------|
| Force Quit (Cmd+Opt+Esc) | 5秒内自动重启 |
| kill / kill -9 命令 | 5秒内自动重启 |
| 活动监视器强杀 | 5秒内自动重启 |
| 重启/注销后登录 | 自动启动 |

### 相关文件
- 配置模板：`scripts/launchagent/com.tsymq.myfocusme.plist`
- 安装位置：`~/Library/LaunchAgents/com.tsymq.myfocusme.plist`

## 常见开发任务

| 任务 | 修改位置 |
|------|----------|
| 添加硬编码屏蔽网站 | `BlockedSitesConfig.HARDCODED_BLOCKED_SITES` |
| 修改学习模式时长限制 | `AppConfig.MIN/MAX_FOCUS_DURATION_MINUTES` |
| 修改定时切换时间 | `ModeManager.scheduleDailySwitch(17, 0)` |
| 调整监控间隔 | `AppConfig.MONITOR_INTERVAL_MS` |

## 测试规范

### 测试框架
JUnit 5 + Mockito + TestFX + AssertJ

### 🚨 测试安全警告

**核心原则**：测试代码必须与生产环境完全隔离，绝对不能修改用户配置文件。

```java
// ✅ 正确做法：使用 MockedStatic 隔离文件路径
private MockedStatic<Paths> mockedPaths;

@BeforeEach
void setUp() throws IOException {
    blockedWebsitesFile = tempDir.resolve("blockedWebsites.txt");
    Files.createFile(blockedWebsitesFile);

    mockedPaths = mockStatic(Paths.class, Mockito.CALLS_REAL_METHODS);
    mockedPaths.when(() -> Paths.get(AppConfig.BLOCKED_WEBSITES_FILE))
        .thenReturn(blockedWebsitesFile);
}

@AfterEach
void tearDown() {
    if (mockedPaths != null) mockedPaths.close();
}
```

### 时间敏感测试
ModeManagerTest 中的测试会检测当前时间，17:00 后自动跳过学习模式相关测试。

## 代码风格

- 类名：PascalCase
- 方法/字段：camelCase
- 常量：SCREAMING_SNAKE_CASE
- 4 空格缩进，UTF-8 编码
- 控制器成员需与 FXML `fx:id` 对齐
- 优先使用 JavaFX 绑定而非手动监听器

## 注意事项

1. **macOS 专属**：仅支持 macOS（使用 AppleScript）
2. **浏览器限制**：目前仅支持 Microsoft Edge
3. **权限要求**：需要辅助功能权限
4. **启动状态**：应用启动时总是从普通模式开始
