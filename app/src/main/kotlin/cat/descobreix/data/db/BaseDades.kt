package cat.descobreix.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MunicipiDescobertEntity::class,
        MissioCompletadaEntity::class,
        MovimentPuntsEntity::class,
        MissioPropiaEntity::class,
        FotoEntity::class,
        SegellEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class BaseDades : RoomDatabase() {
    abstract fun progres(): ProgresDao

    abstract fun missionsPropies(): MissionsPropiesDao

    abstract fun fotos(): FotosDao

    abstract fun segells(): SegellsDao

    companion object {
        const val NOM = "descobreix.db"

        /** Versió 2: la taula dels segells del passaport. */
        val MIGRACIO_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `segells` (`id` TEXT NOT NULL, `codiIne` TEXT NOT NULL, `comarca` TEXT NOT NULL, " +
                        "`pagina` INTEGER NOT NULL, `x` REAL NOT NULL, `y` REAL NOT NULL, `gir` REAL NOT NULL, `tinta` INTEGER NOT NULL, " +
                        "`creatEl` INTEGER NOT NULL, `modificatEl` INTEGER NOT NULL, PRIMARY KEY(`id`))",
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_segells_codiIne` ON `segells` (`codiIne`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_segells_comarca` ON `segells` (`comarca`)")
            }
        }
    }
}
