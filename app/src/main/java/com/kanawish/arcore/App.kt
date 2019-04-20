package com.kanawish.arcore

import android.app.Application
import com.google.ar.sceneform.Node
import timber.log.Timber
import java.util.*

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.i(
                "%s %d %s",
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
                BuildConfig.APPLICATION_ID
        )
    }
}

data class AndroidMiniState(
    val name: String,
    val description: String,
    val inCart: Boolean = false,
    val faved: Boolean = false,
    val price: Int = 3
)

object DummyAppState {
    val storeInventory:WeakHashMap<Node,AndroidMiniState> = WeakHashMap()
}