package android.boot.common

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import com.petterp.floatingx.FloatingX
import com.petterp.floatingx.assist.FxScopeType

class App : Application() {
    @SuppressLint("InflateParams", "MissingInflatedId")
    override fun onCreate() {
        super.onCreate()
     /*   val view = LayoutInflater.from(this@App)
            .inflate(android.boot.common.tools.R.layout.global_menu, null, false)
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

        FloatingX.install {
            setContext(this@App)
            setLayoutView(view)
            setScopeType(FxScopeType.SYSTEM_AUTO)
        }*/
    }
}