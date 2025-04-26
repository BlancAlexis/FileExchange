package fr.ablanc.fileexchangeandroid.data.di

import fr.ablanc.fileexchangeandroid.data.SocketDataSource
import fr.ablanc.fileexchangeandroid.data.SocketDataSourceImpl
import fr.ablanc.fileexchangeandroid.domain.CacheManager
import fr.ablanc.fileexchangeandroid.domain.ConvertIntoFileUseCase
import fr.ablanc.fileexchangeandroid.domain.CryptoManager
import fr.ablanc.fileexchangeandroid.domain.DecryptResourceUseCase
import fr.ablanc.fileexchangeandroid.domain.EncryptUseCase
import fr.ablanc.fileexchangeandroid.domain.FileReader
import fr.ablanc.fileexchangeandroid.domain.ListenDataUseCase
import fr.ablanc.fileexchangeandroid.domain.SocketRepository
import fr.ablanc.fileexchangeandroid.domain.SocketRepositoryImpl
import fr.ablanc.fileexchangeandroid.presentation.BaseViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


const val SERVER_ADDRESS = "ws://10.0.2.2:8181"
fun injectFeatures() = loadFeatures


private val loadFeatures by lazy {
    loadKoinModules(
        listOf(
            appModule,
        )
    )
}

val appModule = module {

    single {
        HttpClient(CIO) {
            install(Logging)
            install(WebSockets)
        }
    }
    single { CacheManager(get(), get()) }
    single { CryptoManager() }
    single { FileReader(androidContext()) }
    single { EncryptUseCase(get(), get()) }
    single { DecryptResourceUseCase() }
    single<SocketDataSource> { SocketDataSourceImpl(get(), SERVER_ADDRESS, get()) }
    single<SocketRepository> { SocketRepositoryImpl(get(), get()) }
    single { ConvertIntoFileUseCase() }
    single { ListenDataUseCase(get(), get(), get()) }
    viewModelOf(::BaseViewModel)

}

