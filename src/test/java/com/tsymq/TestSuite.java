package com.tsymq;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * MyFocusme 测试套件
 * 运行所有测试用例
 */
@Suite
@SuiteDisplayName("MyFocusme 完整测试套件")
@SelectPackages({
    "com.tsymq",           // 包含AppBlockerTest等根包测试
    "com.tsymq.config",    // 配置相关测试
    "com.tsymq.mode",      // 模式管理测试
    "com.tsymq.utils"      // 工具类测试
})
public class TestSuite {
    // 测试套件类，用于组织和运行所有测试
    // 包含测试类：
    // - AppBlockerTest
    // - ConfigManagerTest
    // - BlockedSitesConfigTest
    // - UserConfigTest
    // - ModeManagerTest
    // - ModeStateTest
    // - TimeUtilsTest
}