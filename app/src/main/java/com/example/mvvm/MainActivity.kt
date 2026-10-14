package com.example.mvvm

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.tracing.Trace
import com.example.mvvm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // ViewBinding: 编译期生成，等价于 findViewById 但零反射、类型安全
    private lateinit var binding: ActivityMainBinding

    // by viewModels() 委托：ViewModel 在首次访问时才创建（lazy），
    // 且自动绑定 Activity 生命周期，屏幕旋转自动恢复
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        Trace.beginSection("MainActivity#onCreate")
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // View 观察 ViewModel 的 LiveData，数据变化自动刷新 UI
        viewModel.displayText.observe(this) { text ->
            binding.textView.text = text
        }

        // 用户交互 → ViewModel 处理 → LiveData 更新 → UI 刷新
        binding.button.setOnClickListener {
            viewModel.increment()
        }

        Trace.endSection()

        // 通知系统 Activity 已完全绘制。
        // 影响 Google Play / Android Vitals 的启动耗时指标，
        // 确保冷启动时间测量点落在真正可交互的时机。
        reportFullyDrawn()
    }
}
