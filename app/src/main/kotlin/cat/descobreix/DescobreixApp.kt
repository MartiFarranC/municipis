package cat.descobreix

import android.app.Application
import cat.descobreix.data.ranquing.PujadaAutomatica
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DescobreixApp : Application() {
    @Inject
    lateinit var pujada: PujadaAutomatica

    override fun onCreate() {
        super.onCreate()
        pujada.comenca()
    }
}
