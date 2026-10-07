package cat.descobreix

import android.app.Application
import cat.descobreix.data.sincronitzacio.PlanificadorSincronitzacio
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DescobreixApp : Application() {
    @Inject
    lateinit var sincronitzacio: PlanificadorSincronitzacio

    override fun onCreate() {
        super.onCreate()
        sincronitzacio.comenca()
    }
}
