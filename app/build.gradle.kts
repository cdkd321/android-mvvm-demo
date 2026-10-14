plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.mvvm"
    compileSdk = 35
    buildToolsVersion = "35.0.1"

    defaultConfig {
        applicationId = "com.example.mvvm"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    // ================================================================
    // DEX 启动优化 — Release 构建类型配置
    // ================================================================
    // 策略说明:
    // 1. isMinifyEnabled     → R8 移除未使用的类/方法，缩小 DEX，减少 ART 验证开销
    // 2. isShrinkResources   → 移除未引用的资源，配合代码压缩进一步瘦身
    // 3. proguard-android-optimize.txt → Google 官方优化规则（含内联、分支剪枝等）
    // 4. Baseline Profile    → 安装时预编译启动热路径，跳过解释执行
    //
    // 注意: debug 构建不开启 R8，否则编译太慢，影响开发效率
    // ================================================================
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        // ViewBinding 替代 findViewById：
        // 编译期生成 binding 类，消除运行时 View 查找开销，类型安全
        viewBinding = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    appDeps()

    // 模块特有依赖写在这里，例如：
    // implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
