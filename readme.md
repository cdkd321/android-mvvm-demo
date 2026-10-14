# MVVM Demo

一个单模块 Android 应用，用最精简的代码演示 **MVVM（Model-View-ViewModel）** 架构，
同时附带一套面向生产环境的 **构建配置与冷启动优化实践**（R8 Full Mode、资源压缩、Baseline Profile、Trace 埋点）。

## 项目概览

| 项 | 值 |
|---|---|
| 包名 / ApplicationId | `com.example.mvvm` |
| 语言 | Kotlin 1.9.20（JVM 17） |
| 构建 | Gradle 8.10.2 + AGP 8.2.0（Kotlin DSL） |
| SDK | compileSdk / targetSdk 35，minSdk 26 |
| 模块 | 单模块 `:app` |
| UI 框架 | View 体系（ConstraintLayout + ViewBinding），非 Compose |

## 技术栈与依赖

| 用途 | 依赖 | 版本 |
|---|---|---|
| 基础库 | `androidx.core:core-ktx` | 1.12.0 |
| 兼容库 | `androidx.appcompat:appcompat` | 1.6.1 |
| 布局 | `androidx.constraintlayout:constraintlayout` | 2.1.4 |
| ViewModel | `androidx.lifecycle:lifecycle-viewmodel-ktx` | 2.6.2 |
| LiveData | `androidx.lifecycle:lifecycle-livedata-ktx` | 2.6.2 |
| ViewModel 委托 | `androidx.activity:activity-ktx`（`by viewModels()`） | 1.8.1 |
| 启动耗时分析 | `androidx.tracing:tracing`（`Trace.beginSection`，配合 Perfetto / systrace） | 1.0.0 |
| Baseline Profile | `androidx.profileinstaller:profileinstaller` | 1.3.1 |

> 网络环境适配：Gradle 插件与依赖仓库均走阿里云 Maven 镜像（见 `settings.gradle.kts`），
> Gradle 发行包同样使用阿里云镜像下载（见 `gradle/wrapper/gradle-wrapper.properties`）。

## 项目结构

```
android-mvvm-demo/
├── build.gradle.kts                  # 根构建脚本：声明 AGP / Kotlin 插件版本
├── settings.gradle.kts               # 模块声明 + 阿里云镜像仓库
├── gradle.properties                 # R8 Full Mode 等全局开关
├── gradle/wrapper/                   # Gradle Wrapper（8.10.2，阿里云镜像）
└── app/
    ├── build.gradle.kts              # app 模块：SDK 配置、构建类型、依赖
    ├── proguard-rules.pro            # R8 混淆保留规则
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/example/mvvm/
        │   ├── MvvmApplication.kt    # Application 入口：启动初始化策略
        │   ├── MainActivity.kt       # View 层：ViewBinding + LiveData 观察
        │   └── MainViewModel.kt      # ViewModel 层：状态与业务逻辑
        └── res/
            ├── layout/activity_main.xml
            ├── values/strings.xml
            └── values/themes.xml
```

## 架构设计

### MVVM 分层

| 层级 | 文件 | 职责 |
|---|---|---|
| **View** | `activity_main.xml` + `MainActivity.kt` | UI 展示与用户交互。XML 定义布局；Activity 通过 ViewBinding 绑定控件、观察 LiveData 刷新 UI、将点击事件转发给 ViewModel。**不持有业务逻辑** |
| **ViewModel** | `MainViewModel.kt` | 持有 UI 状态（`_displayText`），以只读 `LiveData` 暴露给 View；处理业务逻辑（`increment()` 计数 + 刷新文本）。**不持有任何 View 引用**，屏幕旋转等配置变更时存活 |
| **Model** | 无显式类（`count: Int` 变量） | 本 demo 中 Model 退化为 ViewModel 中的一个计数器。真实项目中的 Model 层负责数据结构定义与数据获取（网络 / 数据库 / 文件），通常以 Repository 形式被 ViewModel 调用 |

### 单向数据流

```
Button 点击
    │  setOnClickListener
    ▼
viewModel.increment()
    │  count++，更新 _displayText.value
    ▼
LiveData 通知 Observer
    │  observe(this) { ... }
    ▼
binding.textView.text = text   ← UI 自动刷新
```

View 只做两件事：**把事件交给 ViewModel**、**观察数据刷新 UI**。数据永远单向流动（ViewModel → View），View 不直接改写状态。

### 关键机制

- **`by viewModels()` 委托**（`activity-ktx`）：ViewModel 在首次访问时懒创建，自动绑定 Activity 生命周期。
- **ViewModel 存活原理**：`ViewModelProvider` 创建的 ViewModel 存储在 `ViewModelStore` 中，其生命周期长于 Activity —— Activity 因旋转销毁重建时，Store 被保留并交还给新实例，`count` 得以延续，界面状态不丢失。
- **ViewBinding**：编译期生成 `ActivityMainBinding`，替代 `findViewById`，零反射、类型安全、无运行时查找开销。
- **LiveData 只读暴露**：`MutableLiveData` 保持 `private`，对外仅暴露 `LiveData`，防止 View 层反向篡改状态。

## 冷启动优化体系

项目围绕启动耗时做了四层优化，策略与注释分布在构建脚本和代码中：

### 1. Application 初始化原则（`MvvmApplication.kt`）

`onCreate` 在主线程执行，耗时初始化会直接拉长冷启动。原则：

- 能延迟到首次使用的，绝不在 Application 中初始化；
- 必须初始化的，扔到后台线程。

当前 `onCreate` 保持空实现，代码中以注释预留了 StrictMode（仅 debug 构建）、后台线程初始化等示例。

### 2. Release 构建：R8 压缩（`app/build.gradle.kts` + `gradle.properties`）

| 配置 | 位置 | 作用 |
|---|---|---|
| `isMinifyEnabled` + `proguard-android-optimize.txt` | buildTypes.release | R8 移除未使用代码，Google 官方优化规则（内联、分支剪枝等），缩小 DEX、减少 ART 验证开销 |
| `isShrinkResources` | buildTypes.release | 移除未引用资源，配合代码压缩进一步瘦身 |
| `android.enableR8.fullMode=true` | gradle.properties | 开启更激进的全程序优化：方法内联、虚方法去虚拟化、分支剪枝、类层次分析 |

> debug 构建刻意不开 R8，避免拖慢日常编译。

### 3. Baseline Profile（`profileinstaller`）

接入 `androidx.profileinstaller:profileinstaller:1.3.1`：安装时写入 Baseline Profile，ART 在首次启动前对启动路径上的热点方法做 AOT 编译，跳过解释执行。
（注：正式使用需配合 `baselineprofile` Gradle 插件生成 profile 文件，当前仅完成依赖接入。）

### 4. Trace 埋点与启动指标（`MainActivity.kt`）

- `Trace.beginSection("MainActivity#onCreate")` / `endSection()`：用 Perfetto / systrace 定位启动耗时瓶颈。
- `reportFullyDrawn()`：通知系统 Activity 已完全绘制，保证 Google Play / Android Vitals 的冷启动测量点落在真正可交互的时机。

## ProGuard / R8 保留规则（`app/proguard-rules.pro`）

R8 会移除"未使用"的类和方法，以下场景依赖反射，必须显式保留：

- **ViewModel**：通过类名反射创建实例，保留所有 `ViewModel` 子类的构造函数；
- **项目自身代码**：`-keep class com.example.mvvm.**`，防止 R8 过度优化导致 LiveData 回调被裁剪；
- **未来扩展规则已注释预留**：数据层（Room / Retrofit）、DI 容器、`Signature`（泛型签名，Retrofit / Gson 需要）、`*Annotation*`（运行时注解，Room / Moshi 需要）。

## 构建与运行

```bash
# Windows
gradlew.bat assembleDebug      # 构建 debug 包
gradlew.bat assembleRelease    # 构建 release 包（开启 R8 压缩）

# 安装运行
gradlew.bat installDebug
```

本地 SDK 路径配置在 `local.properties`（`sdk.dir`），该文件不入库。

## 已预留的扩展点

项目刻意保持单文件分层的最小形态，同时在注释中标注了向生产项目演进的路径：

| 扩展点 | 位置 | 说明 |
|---|---|---|
| AppContainer | `MvvmApplication.kt` | 手动 DI 容器，替代 Hilt / Koin，存放 Repository、DataSource 等全局单例 |
| Model / 数据层 | `MainViewModel.kt` | 当前 `count` 仅为演示，可替换为 Repository → 网络 / Room / 文件 |
| Logger / WorkManager / StrictMode / Coil / App Startup | `MvvmApplication.kt` | 应用级初始化项的接入位 |
| R8 数据层保留规则 | `proguard-rules.pro` | `data.**`、`di.**`、`Signature`、`*Annotation*` 规则已注释备好 |
| 多模块初始化编排 | `MvvmApplication.kt` | 拆分多模块后可用 `androidx.startup` 的 `InitializationProvider` 替代手动编排 |

## 演示功能

应用启动后显示 "Hello MVVM"，点击按钮计数并实时刷新文本——完整走一遍
`点击 → ViewModel.increment() → LiveData 更新 → Observer 刷新 UI` 的 MVVM 链路；
旋转屏幕可验证 ViewModel 状态存活。
