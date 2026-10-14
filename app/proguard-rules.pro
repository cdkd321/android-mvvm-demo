# ================================================================
# R8 / ProGuard 混淆保留规则
# ================================================================
# R8 会移除"未使用"的类和方法，但以下场景依赖反射或隐式调用，
# 必须显式保留，否则运行时 crash。
# ================================================================

# ViewModel — 通过类名反射创建实例
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# 项目自身代码 — 防止 R8 过度优化导致 LiveData 回调被裁剪
# 未来如有 data class / repository / Retrofit 接口等，也在此追加规则
-keep class com.example.mvvm.** { *; }

# ================================================================
# 未来扩展规则参考 (按需取消注释):
# ================================================================
# -keep class com.example.mvvm.data.** { *; }    # 数据层 (Room entity, DAO, Retrofit API)
# -keep class com.example.mvvm.di.** { *; }      # 手动 DI 容器
# -keepattributes Signature                       # 泛型签名 (Retrofit / Gson 需要)
# -keepattributes *Annotation*                    # 运行时注解 (Room / Moshi 需要)
