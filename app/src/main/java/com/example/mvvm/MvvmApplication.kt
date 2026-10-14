package com.example.mvvm

import android.app.Application

/**
 * Application — app 级初始化入口。
 *
 * ## 启动优化策略
 * onCreate 在主线程执行，耗时初始化会直接拉长冷启动。
 * 原则：能延迟到首次使用的，绝不在这里做；必须初始化的，扔到后台线程。
 *
 * ## 未来扩展点
 * - [AppContainer]  手动 DI 容器，替代 Hilt/Koin，存放全局单例（如 Repository、DataSource）
 * - [Logger]        日志框架初始化（Timber / 自研），可在后台线程完成
 * - [WorkManager]   后台任务调度初始化
 * - [StrictMode]    仅在 debug 构建开启，检测主线程 IO / 泄露
 * - [Coil / Glide]  图片加载库的全局配置
 * - [App Startup]   如需拆分多模块初始化，可用 androidx.startup 的 InitializationProvider
 *                   替代手动在 Application 中编排顺序
 */
class MvvmApplication : Application() {

    // 未来: lateinit var appContainer: AppContainer
    // 未来: lateinit var logger: Logger

    override fun onCreate() {
        super.onCreate()

        // 示例：debug 构建开启 StrictMode 检测
        // if (BuildConfig.DEBUG) {
        //     StrictMode.setThreadPolicy(
        //         StrictMode.ThreadPolicy.Builder()
        //             .detectDiskReads()
        //             .detectDiskWrites()
        //             .penaltyLog()
        //             .build()
        //     )
        // }

        // 示例：耗时初始化放到后台
        // Thread {
        //     // 初始化日志、数据库等
        // }.start()
    }
}
