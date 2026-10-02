package cat.descobreix.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgresDao {
    @Query("SELECT * FROM municipis_descoberts ORDER BY creatEl")
    fun descoberts(): Flow<List<MunicipiDescobertEntity>>

    @Query("SELECT * FROM municipis_descoberts ORDER BY creatEl")
    suspend fun descobertsAra(): List<MunicipiDescobertEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insereixDescobert(m: MunicipiDescobertEntity)

    @Query("SELECT * FROM missions_completades ORDER BY creatEl")
    fun completades(): Flow<List<MissioCompletadaEntity>>

    @Query("SELECT * FROM missions_completades ORDER BY creatEl")
    suspend fun completadesAra(): List<MissioCompletadaEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insereixCompletada(m: MissioCompletadaEntity)

    @Query("SELECT * FROM moviments_punts ORDER BY creatEl")
    fun moviments(): Flow<List<MovimentPuntsEntity>>

    @Query("SELECT * FROM moviments_punts ORDER BY creatEl")
    suspend fun movimentsAra(): List<MovimentPuntsEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insereixMoviment(m: MovimentPuntsEntity)
}

@Dao
interface MissionsPropiesDao {
    @Query("SELECT * FROM missions_propies WHERE codiIne = :codi ORDER BY creatEl")
    fun de(codi: String): Flow<List<MissioPropiaEntity>>

    @Query("SELECT * FROM missions_propies ORDER BY creatEl")
    suspend fun totesAra(): List<MissioPropiaEntity>

    @Query("SELECT * FROM missions_propies WHERE id = :id")
    suspend fun perId(id: String): MissioPropiaEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insereix(m: MissioPropiaEntity)

    @Update
    suspend fun actualitza(m: MissioPropiaEntity)

    @Query("DELETE FROM missions_propies WHERE id = :id")
    suspend fun esborra(id: String)
}

@Dao
interface FotosDao {
    @Query("SELECT * FROM fotos ORDER BY creatEl DESC")
    fun totes(): Flow<List<FotoEntity>>

    @Query("SELECT * FROM fotos ORDER BY creatEl DESC")
    suspend fun totesAra(): List<FotoEntity>

    @Query("SELECT * FROM fotos WHERE codiIne = :codi ORDER BY creatEl DESC")
    fun de(codi: String): Flow<List<FotoEntity>>

    @Query("SELECT * FROM fotos WHERE id = :id")
    fun perId(id: String): Flow<FotoEntity?>

    @Query("SELECT * FROM fotos WHERE id = :id")
    suspend fun perIdAra(id: String): FotoEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insereix(f: FotoEntity)

    @Query("UPDATE fotos SET visibilitat = :visibilitat, modificatEl = :ara WHERE id = :id")
    suspend fun canviaVisibilitat(id: String, visibilitat: String, ara: Long)

    @Query("UPDATE fotos SET esPortada = (id = :id), modificatEl = :ara WHERE codiIne = :codi AND (esPortada = 1 OR id = :id)")
    suspend fun fesPortada(codi: String, id: String, ara: Long)

    @Query("DELETE FROM fotos WHERE id = :id")
    suspend fun esborra(id: String)
}
