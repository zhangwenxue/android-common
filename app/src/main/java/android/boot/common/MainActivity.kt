package android.boot.common

import android.accessibilityservice.AccessibilityServiceInfo
import android.boot.common.toast.toast
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.lzf.easyfloat.EasyFloat
import com.lzf.easyfloat.anim.DefaultAnimator
import com.lzf.easyfloat.enums.ShowPattern
import com.lzf.easyfloat.enums.SidePattern


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(android.boot.common.tools.R.layout.activity_main)

        if (Build.MODEL == "C10X") {
            checkPermission()
        } else {
            checkWindowOverlay()
        }


        findViewById<View>(android.boot.common.tools.R.id.start_Btn).setOnClickListener {
            if (Build.MODEL == "C10X") {
                checkPermission()
            } else {
                EasyFloat.show("DEV")
                checkWindowOverlay()
            }
        }
        findViewById<View>(android.boot.common.tools.R.id.stop_btn).setOnClickListener {
            if (Build.MODEL == "C10X") {
                "请在设置中关闭辅助功能".toast()
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
                finish()
            } else {
                EasyFloat.hide("DEV")
            }
        }

        findViewById<View>(android.boot.common.tools.R.id.close_btn).setOnClickListener {
            finish()
        }
    }

    private fun checkWindowOverlay() {

        EasyFloat.with(this)
            // 设置浮窗xml布局文件/自定义View，并可设置详细信息
            .setLayout(android.boot.common.tools.R.layout.global_menu) {
                // it.parent.requestDisallowInterceptTouchEvent(true)
                it.postDelayed({
                    (it.parent as? ViewGroup)?.descendantFocusability =
                        ViewGroup.FOCUS_AFTER_DESCENDANTS
                }, 200)
                it.findViewById<View>(android.boot.common.tools.R.id.settings_btn)
                    .setOnClickListener {
                        Intent(Settings.ACTION_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(this)
                        }
                    }
                it.findViewById<View>(android.boot.common.tools.R.id.apps_btn).setOnClickListener {

                    kotlin.runCatching {
                        Intent(Settings.ACTION_APPLICATION_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(this)
                        }
                    }
                }
                it.findViewById<View>(android.boot.common.tools.R.id.dev_btn).setOnClickListener {
                    kotlin.runCatching {
                        Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(this)
                        }
                    }
                }
                it.findViewById<View>(android.boot.common.tools.R.id.home_btn).setOnClickListener {
                    sendHomeKey(this@MainActivity)
                }
                it.findViewById<View>(android.boot.common.tools.R.id.usb_switch).isVisible = false
            }
            // 设置浮窗显示类型，默认只在当前Activity显示，可选一直显示、仅前台显示
            .setShowPattern(ShowPattern.ALL_TIME)
            // 设置吸附方式，共15种模式，详情参考SidePattern
            .setSidePattern(SidePattern.RESULT_HORIZONTAL)
            // 设置浮窗的标签，用于区分多个浮窗
            .setTag("DEV")
            // 设置浮窗是否可拖拽
            .setDragEnable(true)
            // 浮窗是否包含EditText，默认不包含
            .hasEditText(false)
            // 设置浮窗固定坐标，ps：设置固定坐标，Gravity属性和offset属性将无效
            .setLocation(100, 200)
            // 设置浮窗的对齐方式和坐标偏移量
            .setGravity(Gravity.END or Gravity.CENTER_VERTICAL, 0, 200)
            // 设置当布局大小变化后，整体view的位置对齐方式
            .setLayoutChangedGravity(Gravity.END)
            // 设置拖拽边界值
            //.setBorder(100, 100, 800, 800)
            // 设置宽高是否充满父布局，直接在xml设置match_parent属性无效
            .setMatchParent(widthMatch = false, heightMatch = false)
            // 设置浮窗的出入动画，可自定义，实现相应接口即可（策略模式），无需动画直接设置为null
            .setAnimator(DefaultAnimator())
            // 设置系统浮窗的有效显示高度（不包含虚拟导航栏的高度），基本用不到，除非有虚拟导航栏适配问题
            // .setDisplayHeight { context -> DisplayUtils.rejectedNavHeight(context) }
            // 浮窗的一些状态回调，如：创建结果、显示、隐藏、销毁、touchEvent、拖拽过程、拖拽结束。
            // ps：通过Kotlin DSL实现的回调，可以按需复写方法，用到哪个写哪个
            // 创建浮窗（这是关键哦😂）
            .show()
    }

    private fun sendHomeKey(context: Context) {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    private fun checkPermission() {
        if (isStartAccessibilityServiceEnable(this)) {
            "启动调试浮窗".toast()
            startService(Intent(this, NavigationAccessibilityService::class.java))
        } else {
            "申请无障碍权限".toast()
            startService(Intent(this, NavigationAccessibilityService::class.java))

            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
    }

    /**
     * 判断无障碍服务是否开启
     *
     * @param context
     * @return
     */
    private fun isStartAccessibilityServiceEnable(context: Context): Boolean {
        val accessibilityManager =
            checkNotNull(context.getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager)
        val accessibilityServices =
            accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        for (info in accessibilityServices) {
            if (info.id.contains(context.packageName)) {
                return true
            }
        }
        return false
    }
}