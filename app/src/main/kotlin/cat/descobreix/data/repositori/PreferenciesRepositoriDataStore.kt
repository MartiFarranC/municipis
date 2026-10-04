package cat.descobreix.data.repositori

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import cat.descobreix.ui.theme.ColorSecundari
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

    override val colorSecundari: Flow<ColorSecundari> = dataStore.data.map { ColorSecundari.perNom(it[COLOR_SECUNDARI]) }

    override suspend fun desaColorSecundari(color: ColorSecundari) {
        dataStore.edit { it[COLOR_SECUNDARI] = color.name }
    }

    private companion object {
        val X = floatPreferencesKey("mapa_x")
        val Y = floatPreferencesKey("mapa_y")
        val ESCALA = floatPreferencesKey("mapa_escala")
        val COLOR_SECUNDARI = stringPreferencesKey("color_secundari")
    }
}
