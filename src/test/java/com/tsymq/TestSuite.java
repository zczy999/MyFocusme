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
    "com.tsymq.config",
    "com.tsymq.mode", 
    "com.tsymq.utils"
})
public class TestSuite {
    // 测试套件类，用于组织和运行所有测试
} 