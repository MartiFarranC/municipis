package cat.descobreix

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cat.descobreix.data.db.BaseDades
import cat.descobreix.data.db.SacEntity
import cat.descobreix.data.db.TriggersSincronitzacio
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Els triggers de la cua no han de fallar quan una fila que ja hi és torna a canviar. */
@RunWith(AndroidJUnit4::class)
class CuaSincronitzacioTest {
    private lateinit var db: BaseDades

    @Before
    fun obre() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), BaseDades::class.java)
            .addCallback(TriggersSincronitzacio.callback)
            .build()
    }

    @After
    fun tanca() = db.close()

    @Test
    fun actualitzarUnSacQueJaEsALaCuaNoFalla() = runBlocking {
        val sac = SacEntity("s1", "missio:1", null, null, null, null, creatEl = 1, modificatEl = 1)
        db.sacs().insereix(sac)
        db.sacs().actualitza(sac.copy(punts = 10, obertEl = 2, modificatEl = 2))
        db.sacs().actualitza(sac.copy(punts = 10, obertEl = 2, modificatEl = 3))

        assertEquals(1, db.sincronitzacio().pendents().size)
    }
}
