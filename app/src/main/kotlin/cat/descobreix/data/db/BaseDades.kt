package cat.descobreix.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        MunicipiDescobertEntity::class,
        MissioCompletadaEntity::class,
        MovimentPuntsEntity::class,
        MissioPropiaEntity::class,
        FotoEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class BaseDades : RoomDatabase() {
    abstract fun progres(): ProgresDao

    abstract fun missionsPropies(): MissionsPropiesDao

    abstract fun fotos(): FotosDao

    companion object {
        const val NOM = "descobreix.db"
    }
}
