import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * 各模块依赖清单。新增模块时在此增加对应 xxxDeps()，
 * 再在该模块 build.gradle.kts 的 dependencies { } 中调用即可。
 */

private fun DependencyHandler.impl(notation: String) {
    add("implementation", notation)
}

/** :app 模块依赖 */
fun DependencyHandler.appDeps() {
    impl("androidx.core:core-ktx:1.12.0")
    impl("androidx.appcompat:appcompat:1.6.1")
    impl("androidx.constraintlayout:constraintlayout:2.1.4")
    impl("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    impl("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    impl("androidx.activity:activity-ktx:1.8.1")

    // Trace API — 用 Perfetto / systrace 分析启动耗时瓶颈
    impl("androidx.tracing:tracing:1.0.0")

    // ProfileInstaller — 安装时写入 Baseline Profile，ART 在首次启动前
    // 对启动路径上的热点方法进行 AOT 编译，跳过解释执行阶段
    impl("androidx.profileinstaller:profileinstaller:1.3.1")
}

/** 示例：其它模块可按同样方式扩展
fun DependencyHandler.libraryDeps() {
    impl("androidx.core:core-ktx:1.12.0")
}
*/
