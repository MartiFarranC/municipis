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
        SacEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class BaseDades : RoomDatabase() {
    abstract fun progres(): ProgresDao

    abstract fun missionsPropies(): MissionsPropiesDao

    abstract fun fotos(): FotosDao

    abstract fun segells(): SegellsDao

    abstract fun sacs(): SacsDao

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

        /** Versió 4: les fotos del cartell que van al catàleg. */
        val MIGRACIO_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `fotos` ADD COLUMN `esCromo` INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** Versió 3: la taula dels sacs. */
        val MIGRACIO_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `sacs` (`id` TEXT NOT NULL, `origen` TEXT NOT NULL, `objecteTipus` TEXT, `objecteId` TEXT, " +
                        "`punts` INTEGER, `obertEl` INTEGER, `creatEl` INTEGER NOT NULL, `modificatEl` INTEGER NOT NULL, PRIMARY KEY(`id`))",
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_sacs_origen` ON `sacs` (`origen`)")
            }
        }
    }
}
