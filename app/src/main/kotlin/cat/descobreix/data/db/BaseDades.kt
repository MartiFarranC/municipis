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
        CanviPendentEntity::class,
        ControlSincronitzacioEntity::class,
    ],
    version = 5,
    exportSchema = true,
)
abstract class BaseDades : RoomDatabase() {
    abstract fun progres(): ProgresDao

    abstract fun missionsPropies(): MissionsPropiesDao

    abstract fun fotos(): FotosDao

    abstract fun segells(): SegellsDao

    abstract fun sacs(): SacsDao

    abstract fun sincronitzacio(): SincronitzacioDao

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

        /**
         * Versió 5: la sincronització. La cua del que s'ha de pujar (tot el que ja hi havia hi entra) i els esborrats
         * lògics de les missions pròpies i les fotos.
         */
        val MIGRACIO_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `missions_propies` ADD COLUMN `esborratEl` INTEGER")
                db.execSQL("ALTER TABLE `fotos` ADD COLUMN `esborratEl` INTEGER")
                db.execSQL("CREATE TABLE IF NOT EXISTS `canvis_pendents` (`taula` TEXT NOT NULL, `id` TEXT NOT NULL, PRIMARY KEY(`taula`, `id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `control_sincronitzacio` (`id` INTEGER NOT NULL, `aplicant` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                TriggersSincronitzacio.crea(db)
                for (t in TriggersSincronitzacio.TAULES) db.execSQL("INSERT OR IGNORE INTO canvis_pendents (taula, id) SELECT '$t', id FROM `$t`")
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

/**
 * Triggers que apunten a la cua (`canvis_pendents`) cada fila que s'afegeix o es modifica a les taules que es
 * sincronitzen, excepte mentre s'apliquen les dades baixades del servidor. Així cap escriptura es queda sense pujar.
 */
object TriggersSincronitzacio {
    val TAULES = listOf("municipis_descoberts", "missions_completades", "moviments_punts", "missions_propies", "fotos", "segells", "sacs")

    fun crea(db: SupportSQLiteDatabase) {
        db.execSQL("INSERT OR IGNORE INTO control_sincronitzacio (id, aplicant) VALUES (0, 0)")
        for (t in TAULES) {
            for (quan in listOf("INSERT", "UPDATE")) {
                db.execSQL(
                    "CREATE TRIGGER IF NOT EXISTS `cua_${t}_${quan.lowercase()}` AFTER $quan ON `$t` " +
                        "WHEN COALESCE((SELECT aplicant FROM control_sincronitzacio WHERE id = 0), 0) = 0 " +
                        "BEGIN INSERT OR IGNORE INTO canvis_pendents (taula, id) VALUES ('$t', NEW.id); END",
                )
            }
        }
    }

    /** En crear la base de dades de zero (instal·lació nova) i en obrir-la, per si de cas. */
    val callback = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) = crea(db)

        override fun onOpen(db: SupportSQLiteDatabase) = crea(db)
    }
}
