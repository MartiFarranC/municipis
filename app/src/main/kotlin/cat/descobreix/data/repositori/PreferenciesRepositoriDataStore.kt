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

    // Abans dels sacs es desava el nom de l'enum (GRANAT, BLAU, APP): en minúscules és l'id d'ara.
    override val tapaPassaport: Flow<String> = dataStore.data.map { p -> p[TAPA]?.lowercase() ?: TapesPassaport.GRANAT }

    override suspend fun desaTapaPassaport(tapa: String) {
        dataStore.edit { it[TAPA] = tapa }
    }

    override val colorApp: Flow<String?> = dataStore.data.map { it[COLOR] }

    override suspend fun desaColorApp(color: String?) {
        dataStore.edit { if (color == null) it.remove(COLOR) else it[COLOR] = color }
    }

    override val animacioCarrega: Flow<String?> = dataStore.data.map { it[ANIMACIO] }

    override suspend fun desaAnimacioCarrega(animacio: String?) {
        dataStore.edit { if (animacio == null) it.remove(ANIMACIO) else it[ANIMACIO] = animacio }
    }

    private companion object {
        val X = floatPreferencesKey("mapa_x")
        val Y = floatPreferencesKey("mapa_y")
        val ESCALA = floatPreferencesKey("mapa_escala")
        val TAPA = stringPreferencesKey("tapa_passaport")
        val COLOR = stringPreferencesKey("color_app")
        val ANIMACIO = stringPreferencesKey("animacio_carrega")
    }
}
