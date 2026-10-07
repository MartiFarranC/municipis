package cat.descobreix.data.repositori

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PreferenciesRepositoriDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : PreferenciesRepositori {
    override val cameraMapa: Flow<CameraMapa?> = dataStore.data.map { p ->
        val x = p[X] ?: return@map null
        val y = p[Y] ?: return@map null
        val escala = p[ESCALA] ?: return@map null
        CameraMapa(x, y, escala)
    }

    override suspend fun desaCameraMapa(camera: CameraMapa) {
        dataStore.edit {
            it[X] = camera.x
            it[Y] = camera.y
            it[ESCALA] = camera.escala
        }
    }

    override val tapaPassaport: Flow<TapaPassaport> = dataStore.data.map { p ->
        TapaPassaport.entries.firstOrNull { it.name == p[TAPA] } ?: TapaPassaport.GRANAT
    }

    override suspend fun desaTapaPassaport(tapa: TapaPassaport) {
        dataStore.edit { it[TAPA] = tapa.name }
    }

    private companion object {
        val X = floatPreferencesKey("mapa_x")
        val Y = floatPreferencesKey("mapa_y")
        val ESCALA = floatPreferencesKey("mapa_escala")
        val TAPA = stringPreferencesKey("tapa_passaport")
    }
}
