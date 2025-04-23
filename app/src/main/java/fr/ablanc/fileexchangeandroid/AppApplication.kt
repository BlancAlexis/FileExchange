package fr.ablanc.fileexchangeandroid

import android.app.Application
import fr.ablanc.fileexchangeandroid.data.di.appModule
import fr.ablanc.fileexchangeandroid.data.di.injectFeatures
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AppApplication)
            injectFeatures()

        }
    }
}