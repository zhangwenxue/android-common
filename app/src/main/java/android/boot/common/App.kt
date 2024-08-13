package android.boot.common

import android.annotation.SuppressLint
import android.app.Application

class App : Application() {
    @SuppressLint("InflateParams", "MissingInflatedId")
    override fun onCreate() {
        super.onCreate()
    }
}