package android.boot.common

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
import android.view.WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
import android.view.WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
import android.view.WindowManager.LayoutParams.WRAP_CONTENT
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.isVisible
import java.io.File
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.Executors

@SuppressLint("InflateParams")
class NavigationAccessibilityService : AccessibilityService() {
    private val usbFile = if (Build.MODEL == "C10X")
        "/sys/devices/platform/yj_micro_usb/yj_micro_usb" else "/sys/devices/platform/hardware_ports_manager/usb_interfaces_sel"

    private val executor by lazy {
        Executors.newCachedThreadPool()
    }

    private var usbMode = -1

    private val view by lazy {
        val view = LayoutInflater.from(this)
            .inflate(android.boot.common.tools.R.layout.global_menu, null, false) as ViewGroup
        view.findViewById<View>(android.boot.common.tools.R.id.settings_btn).setOnClickListener {
            Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(this)
            }
        }
        view.findViewById<View>(android.boot.common.tools.R.id.apps_btn).setOnClickListener {

            kotlin.runCatching {
                Intent(Settings.ACTION_APPLICATION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(this)
                }
            }
        }
        view.findViewById<View>(android.boot.common.tools.R.id.dev_btn).setOnClickListener {
            kotlin.runCatching {
                Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(this)
                }
            }
        }
        view.findViewById<View>(android.boot.common.tools.R.id.usb_switch).setOnClickListener {
            executor.execute {
                runCatching {
                    File(usbFile).writeText(if (usbMode == 0) "1" else "0")
                }.onFailure { it.printStackTrace() }
            }

        }

        view.findViewById<View>(android.boot.common.tools.R.id.home_btn).setOnClickListener {
            sendHomeKey(view.context)
        }

        view
    }

    private val windowManager by lazy {
        getSystemService(WINDOW_SERVICE) as? WindowManager
    }

    private fun sendHomeKey(context: Context) {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    private var lastX = 0
    private var lastY = 0

    private fun initView() {

        val lp = WindowManager.LayoutParams().apply {
            type = TYPE_ACCESSIBILITY_OVERLAY // 因为此权限才能展示处理
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            format = PixelFormat.TRANSLUCENT
            flags = FLAG_NOT_TOUCH_MODAL or FLAG_WATCH_OUTSIDE_TOUCH or FLAG_NOT_FOCUSABLE

            width = WRAP_CONTENT
            height = WRAP_CONTENT
            gravity = Gravity.END
            y = 300

        }

        windowManager?.addView(view, lp)

        view.setOnTouchListener { _, event ->
            val screenX = event.rawX.toInt()
            val screenY = event.rawY.toInt()

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.rawX.toInt()
                    lastY = event.rawY.toInt()
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    windowManager?.run {
                        lp.x -= screenX - lastX
                        lp.y += screenY - lastY
                        updateViewLayout(view, lp)
                        lastX = screenX
                        lastY = screenY
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    false
                }

                else -> false
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        initView()
        startTimer()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            stopTimer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            stopTimer()
        }
    }


    private fun updateView() {
        executor.execute {
            usbMode = runCatching {
                val text = File(usbFile).readText(Charsets.UTF_8)
                Log.i("_usb", "usbMode: $text")
                text.replace(" ","").replace("\n","")
            }.onFailure {
                it.printStackTrace()
            }.getOrElse { "-1" }.toIntOrNull() ?: -1
            view.post {
                view.findViewById<View>(android.boot.common.tools.R.id.usb_switch).apply {
                    isVisible = usbMode != -1
                    setBackgroundResource(if (usbMode == 0) android.boot.common.tools.R.mipmap.ic_typec else android.boot.common.tools.R.mipmap.ic_usb)
                }
            }
        }
    }


    private var timerTask: TimerTask? = null
    private val timer = Timer()
    private fun startTimer() {
        runCatching {
            stopTimer()
            timerTask = object : TimerTask() {
                override fun run() {
                    updateView()
                }
            }
            timer.schedule(timerTask, 0, 3000)
        }
    }

    private fun stopTimer() {
        runCatching {
            timerTask?.cancel()
            timerTask = null
        }
    }
}
