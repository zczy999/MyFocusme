# MyFocusme 项目修改计划

## TODO List

### 🎯 第一阶段：核心架构搭建 (第1-2天)
- [x] **依赖更新**
  - [x] 更新 `pom.xml` 添加 Jackson JSON 处理依赖
  
- [x] **创建包结构**
  - [x] 创建 `src/main/java/com/tsymq/mode/` 包
  - [x] 创建 `src/main/java/com/tsymq/config/` 包
  - [x] 创建 `src/main/java/com/tsymq/ui/` 包
  - [x] 创建 `src/main/java/com/tsymq/utils/` 包

- [x] **核心类实现**
  - [x] 实现 `ModeManager.java` - 模式管理核心类
  - [x] 实现 `ModeState.java` - 模式状态数据类
  - [x] 实现 `ConfigManager.java` - 配置管理类
  - [x] 实现 `UserConfig.java` - 用户配置数据类
  - [x] 实现 `AppConfig.java` - 应用配置常量类
  - [x] 实现 `TimeUtils.java` - 时间工具类

### 🎨 第二阶段：UI界面重构 (第3-4天)
- [x] **FXML界面设计**
  - [x] 重新设计 `AppBlocker.fxml` 主界面
  - [x] 创建 `FocusModeDialog.fxml` 模式切换对话框

- [x] **样式文件更新**
  - [x] 更新 `styles.css` 支持双模式显示
  - [x] 创建 `focus-mode-styles.css` 学习模式专用样式

- [x] **UI控制器实现**
  - [x] 实现 `FocusModeDialogController.java` 对话框控制器
  - [x] 实现 `UIStateManager.java` UI状态管理类
  - [x] 实现 `NotificationManager.java` 通知管理类

### ⚙️ 第三阶段：业务逻辑集成 (第5-7天)
- [x] **AppBlocker类重构**
  - [x] 添加 `ModeManager` 依赖注入
  - [x] 修改 `monitorActiveEdgeUrl()` 方法支持模式检查
  - [x] 添加模式状态检查逻辑
  - [x] 重构屏蔽逻辑以支持模式切换

- [x] **Controller类重构**
  - [x] 重构 `AppBlockerController.java` 支持双模式UI
  - [x] 添加模式切换按钮事件处理
  - [x] 实现时长选择界面逻辑
  - [x] 添加倒计时显示更新逻辑
  - [x] 实现UI元素的动态显示/隐藏

- [x] **Main类修改**
  - [x] 添加模式状态恢复逻辑
  - [x] 初始化模式管理器
  - [x] 集成配置管理系统

### ⏰ 第四阶段：时间管理和持久化 (第8-10天)
- [ ] **状态持久化**
  - [ ] 实现JSON格式的配置文件读写
  - [ ] 实现模式状态的保存和恢复
  - [ ] 添加配置文件备份机制
  - [ ] 实现应用重启后状态恢复

- [ ] **安全机制**
  - [ ] 实现进程保护机制

### 🧪 第五阶段：测试和优化 (第11-14天)
- [ ] **功能测试**
  - [ ] 测试模式切换正确性
  - [ ] 测试时间管理准确性
  - [ ] 测试状态持久化可靠性
  - [ ] 测试屏蔽功能在不同模式下的行为
  - [ ] 测试紧急退出机制

- [ ] **边界测试**
  - [ ] 测试应用异常退出后的状态恢复
  - [ ] 测试系统时间修改的处理
  - [ ] 测试长时间运行的稳定性
  - [ ] 测试内存泄漏和性能问题

- [ ] **用户体验优化**
  - [ ] 优化界面响应速度
  - [ ] 完善错误提示信息
  - [ ] 优化操作流程
  - [ ] 添加使用引导和帮助信息

### 📋 验收标准检查
- [ ] **功能验收**
  - [ ] 双模式切换功能正常工作
  - [ ] 普通模式下屏蔽功能完全禁用
  - [ ] 学习模式下屏蔽功能完全启用
  - [ ] 模式切换按钮在学习模式下正确隐藏
  - [ ] 倒计时显示准确无误
  - [ ] 状态在应用重启后正确恢复

- [ ] **性能验收**
  - [ ] 模式切换响应时间 < 2秒
  - [ ] 界面更新流畅无卡顿
  - [ ] 内存使用增长 < 20%
  - [ ] CPU使用率保持稳定

- [ ] **用户体验验收**
  - [ ] 界面直观易懂
  - [ ] 操作流程简单明确
  - [ ] 错误提示友好准确
  - [ ] 紧急退出机制可用但不易误触

---

## 1. 项目概述

### 1.1 当前状态分析
- **技术栈：** Java 11 + JavaFX 17 + Maven
- **现有功能：** 基础网站屏蔽，系统托盘，Edge浏览器监控
- **架构：** 单一模式，直接启动即开始屏蔽
- **UI：** 简单的文本输出界面，无模式切换功能

### 1.2 目标状态
- **双模式系统：** 普通模式 + 学习模式
- **模式切换：** 不可逆的模式切换机制
- **时长管理：** 学习模式的时间控制
- **状态持久化：** 应用重启后保持模式状态

## 2. 架构重构计划

### 2.1 新增核心类

#### 2.1.1 模式管理类
```java
// 新增文件：src/main/java/com/tsymq/mode/ModeManager.java
- 管理应用的两种模式状态
- 处理模式切换逻辑
- 状态持久化存储
- 时间管理和倒计时
```

#### 2.1.2 配置管理类
```java
// 新增文件：src/main/java/com/tsymq/config/ConfigManager.java
- 统一管理应用配置
- 模式状态存储
- 用户设置管理
```

#### 2.1.3 UI状态管理类
```java
// 新增文件：src/main/java/com/tsymq/ui/UIStateManager.java
- 根据模式切换UI显示
- 管理按钮状态
- 倒计时显示更新
```

### 2.2 现有类修改计划

#### 2.2.1 AppBlocker.java 修改
- **重构目标：** 支持模式控制的屏蔽逻辑
- **主要修改：**
  - 添加模式状态检查
  - 普通模式下禁用屏蔽功能
  - 学习模式下启用全部屏蔽
  - 重构监控逻辑以支持模式切换

#### 2.2.2 AppBlockerController.java 重构
- **重构目标：** 实现双模式UI控制
- **主要修改：**
  - 添加模式切换按钮和逻辑
  - 实现时长选择界面
  - 添加倒计时显示
  - 根据模式动态调整UI元素

#### 2.2.3 Main.java 修改
- **修改目标：** 支持模式状态恢复
- **主要修改：**
  - 启动时检查并恢复模式状态
  - 初始化模式管理器

## 3. 详细实现计划

### 3.1 第一阶段：核心架构搭建

#### 3.1.1 创建模式管理系统
**文件：** `src/main/java/com/tsymq/mode/ModeManager.java`
```java
public class ModeManager {
    public enum Mode { NORMAL, FOCUS }
    
    // 核心功能
    - getCurrentMode(): Mode
    - switchToFocusMode(int durationMinutes): boolean
    - isInFocusMode(): boolean
    - getRemainingTime(): long
    - saveState(): void
    - loadState(): void
}
```

#### 3.1.2 创建配置管理系统
**文件：** `src/main/java/com/tsymq/config/ConfigManager.java`
```java
public class ConfigManager {
    // 配置文件路径管理
    - MODE_STATE_FILE: String
    - USER_CONFIG_FILE: String
    
    // 核心功能
    - saveModeState(Mode mode, long endTime): void
    - loadModeState(): ModeState
    - saveUserConfig(UserConfig config): void
    - loadUserConfig(): UserConfig
}
```

### 3.2 第二阶段：UI界面重构

#### 3.2.1 重新设计FXML界面
**文件：** `src/main/resources/AppBlocker.fxml`

**普通模式界面元素：**
- 模式状态显示标签
- "进入学习模式"按钮（醒目设计）
- 网站管理区域
- 使用统计显示区域

**学习模式界面元素：**
- 学习模式状态指示器
- 倒计时显示
- 激励性文案标签
- 紧急退出按钮（隐藏位置）

#### 3.2.2 模式切换对话框
**新增文件：** `src/main/resources/FocusModeDialog.fxml`
- 学习模式说明文本
- 时长选择器（15分钟-8小时）
- 确认和取消按钮

### 3.3 第三阶段：业务逻辑集成

#### 3.3.1 AppBlocker类重构
**修改重点：**
```java
// 添加模式检查
public void monitorActiveEdgeUrl(TextArea outputArea) {
    if (!modeManager.isInFocusMode()) {
        return; // 普通模式下不执行屏蔽
    }
    // 原有监控逻辑
}

// 添加模式管理器依赖
private ModeManager modeManager;
```

#### 3.3.2 Controller类重构
**修改重点：**
```java
// 新增UI元素
@FXML private Button focusModeButton;
@FXML private Label modeStatusLabel;
@FXML private Label countdownLabel;
@FXML private VBox normalModePanel;
@FXML private VBox focusModePanel;

// 新增事件处理
@FXML void onFocusModeButtonClicked(ActionEvent event);
@FXML void onEmergencyExitClicked(ActionEvent event);

// 新增UI更新方法
private void updateUIForMode(Mode mode);
private void startCountdownTimer();
```

### 3.4 第四阶段：时间管理和持久化

#### 3.4.1 时间管理实现
```java
// 在ModeManager中实现
private ScheduledExecutorService timeManager;
private long focusModeEndTime;

public void startFocusMode(int durationMinutes) {
    focusModeEndTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000);
    scheduleAutoExit();
    saveState();
}

private void scheduleAutoExit() {
    // 定时检查是否到达结束时间
    // 自动切换回普通模式
}
```

#### 3.4.2 状态持久化实现
```java
// 配置文件格式（JSON）
{
    "currentMode": "FOCUS",
    "focusModeEndTime": 1703123456789,
    "userSettings": {
        "defaultFocusDuration": 60,
        "emergencyPassword": "encrypted_password"
    }
}
```

## 4. 文件结构变更

### 4.1 新增文件
```
src/main/java/com/tsymq/
├── mode/
│   ├── ModeManager.java          # 模式管理核心类
│   ├── ModeState.java           # 模式状态数据类
│   └── TimeManager.java         # 时间管理工具类
├── config/
│   ├── ConfigManager.java       # 配置管理类
│   ├── UserConfig.java         # 用户配置数据类
│   └── AppConfig.java          # 应用配置常量
├── ui/
│   ├── UIStateManager.java     # UI状态管理
│   ├── FocusModeDialogController.java  # 模式切换对话框控制器
│   └── NotificationManager.java # 通知管理
└── utils/
    ├── TimeUtils.java          # 时间工具类
    └── SecurityUtils.java      # 安全工具类（紧急退出验证）

src/main/resources/
├── FocusModeDialog.fxml        # 模式切换对话框
├── EmergencyExitDialog.fxml    # 紧急退出对话框
└── focus-mode-styles.css       # 学习模式专用样式
```

### 4.2 修改文件
```
src/main/java/com/tsymq/
├── Main.java                   # 添加模式状态恢复逻辑
├── AppBlocker.java            # 重构支持模式控制
└── AppBlockerController.java   # 重构UI控制逻辑

src/main/resources/
├── AppBlocker.fxml            # 重新设计界面布局
└── styles.css                 # 更新样式支持双模式
```

## 5. 依赖更新

### 5.1 Maven依赖添加
```xml
<!-- JSON处理 -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>

<!-- 加密工具（紧急退出密码） -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
    <version>5.7.2</version>
</dependency>
```

## 6. 实施时间表

### 6.1 开发阶段（预计2周）

**第1-2天：架构搭建**
- 创建模式管理系统
- 实现配置管理系统
- 设置基础的状态持久化

**第3-4天：UI重构**
- 重新设计FXML界面
- 实现模式切换对话框
- 更新样式文件

**第5-7天：业务逻辑集成**
- 重构AppBlocker支持模式控制
- 实现Controller的模式切换逻辑
- 集成时间管理功能

**第8-10天：功能完善**
- 实现倒计时显示
- 添加紧急退出机制
- 完善状态持久化

**第11-14天：测试和优化**
- 功能测试
- 边界条件测试
- 性能优化
- 用户体验优化

### 6.2 测试重点

#### 6.2.1 功能测试
- 模式切换正确性
- 时间管理准确性
- 状态持久化可靠性
- 屏蔽功能在不同模式下的行为

#### 6.2.2 边界测试
- 应用异常退出后的状态恢复
- 系统时间修改的处理
- 长时间运行的稳定性
- 紧急退出机制的安全性

## 7. 风险评估和缓解

### 7.1 技术风险
**风险：** JavaFX界面复杂度增加可能影响性能
**缓解：** 采用懒加载和状态缓存机制

**风险：** 状态持久化可能失败
**缓解：** 实现多重备份和恢复机制

### 7.2 用户体验风险
**风险：** 模式切换可能造成用户困惑
**缓解：** 提供清晰的状态指示和操作引导

**风险：** 紧急退出可能被滥用
**缓解：** 设计合理的验证机制和使用记录

## 8. 成功标准

### 8.1 功能标准
- [ ] 双模式切换功能正常工作
- [ ] 普通模式下屏蔽功能完全禁用
- [ ] 学习模式下屏蔽功能完全启用
- [ ] 模式切换按钮在学习模式下正确隐藏
- [ ] 倒计时显示准确无误
- [ ] 状态在应用重启后正确恢复

### 8.2 性能标准
- [ ] 模式切换响应时间 < 2秒
- [ ] 界面更新流畅无卡顿
- [ ] 内存使用增长 < 20%
- [ ] CPU使用率保持稳定

### 8.3 用户体验标准
- [ ] 界面直观易懂
- [ ] 操作流程简单明确
- [ ] 错误提示友好准确
- [ ] 紧急退出机制可用但不易误触

---

**文档版本：** v1.0  
**创建日期：** 2024年12月  
**预计完成：** 2024年12月底  
**负责人：** 开发团队 