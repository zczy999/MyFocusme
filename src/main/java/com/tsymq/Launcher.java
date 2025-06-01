package com.tsymq;

/**
 * JavaFX应用启动器
 * 
 * 这个类用于解决在fat jar环境中JavaFX应用启动的问题。
 * 当JavaFX运行时不在模块路径中时，需要通过这种方式启动应用。
 */
public class Launcher {
    public static void main(String[] args) {
        // 启动JavaFX应用
        Main.main(args);
    }
} 