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
    @Query("SELECT * FROM missions_propies WHERE codiIne = :codi AND esborratEl IS NULL ORDER BY creatEl")
    fun de(codi: String): Flow<List<MissioPropiaEntity>>

    @Query("SELECT * FROM missions_propies WHERE esborratEl IS NULL ORDER BY creatEl")
    suspend fun totesAra(): List<MissioPropiaEntity>

    @Query("SELECT * FROM missions_propies WHERE id = :id AND esborratEl IS NULL")
    suspend fun perId(id: String): MissioPropiaEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insereix(m: MissioPropiaEntity)

    @Update
    suspend fun actualitza(m: MissioPropiaEntity)

    @Query("UPDATE missions_propies SET esborratEl = :ara, modificatEl = :ara WHERE id = :id")
    suspend fun esborra(id: String, ara: Long)
}

@Dao
interface FotosDao {
    @Query("SELECT * FROM fotos WHERE esborratEl IS NULL ORDER BY creatEl DESC")
    fun totes(): Flow<List<FotoEntity>>

    @Query("SELECT * FROM fotos WHERE esborratEl IS NULL ORDER BY creatEl DESC")
    suspend fun totesAra(): List<FotoEntity>

    @Query("SELECT * FROM fotos WHERE codiIne = :codi AND esborratEl IS NULL ORDER BY creatEl DESC")
    fun de(codi: String): Flow<List<FotoEntity>>

    @Query("SELECT * FROM fotos WHERE id = :id AND esborratEl IS NULL")
    fun perId(id: String): Flow<FotoEntity?>

    @Query("SELECT * FROM fotos WHERE id = :id AND esborratEl IS NULL")
    suspend fun perIdAra(id: String): FotoEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insereix(f: FotoEntity)

    @Query("UPDATE fotos SET visibilitat = :visibilitat, modificatEl = :ara WHERE id = :id")
    suspend fun canviaVisibilitat(id: String, visibilitat: String, ara: Long)

    @Query("UPDATE fotos SET esPortada = (id = :id), modificatEl = :ara WHERE codiIne = :codi AND esborratEl IS NULL AND (esPortada = 1 OR id = :id)")
    suspend fun fesPortada(codi: String, id: String, ara: Long)

    @Query("UPDATE fotos SET esborratEl = :ara, esPortada = 0, modificatEl = :ara WHERE id = :id")
    suspend fun esborra(id: String, ara: Long)
}

@Dao
interface SegellsDao {
    @Query("SELECT * FROM segells ORDER BY creatEl")
    fun tots(): Flow<List<SegellEntity>>

    @Query("SELECT * FROM segells ORDER BY creatEl")
    suspend fun totsAra(): List<SegellEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insereix(s: SegellEntity)
}


@Dao
interface SacsDao {
    @Query("SELECT * FROM sacs ORDER BY creatEl")
    fun tots(): Flow<List<SacEntity>>

    @Query("SELECT * FROM sacs ORDER BY creatEl")
    suspend fun totsAra(): List<SacEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insereix(s: SacEntity): Long

    @Update
    suspend fun actualitza(s: SacEntity)
}

/** La cua del que s'ha de pujar al servidor. Les files les hi posen els triggers de [TriggersSincronitzacio]. */
@Dao
interface SincronitzacioDao {
    @Query("SELECT * FROM canvis_pendents")
    suspend fun pendents(): List<CanviPendentEntity>

    @Query("SELECT COUNT(*) FROM canvis_pendents")
    fun quantsPendents(): Flow<Int>

    @Query("DELETE FROM canvis_pendents WHERE taula = :taula AND id = :id")
    suspend fun treu(taula: String, id: String)

    /** Mentre s'apliquen les dades baixades del servidor, els triggers no les tornen a posar a la cua. */
    @Query("UPDATE control_sincronitzacio SET aplicant = :aplicant WHERE id = 0")
    suspend fun aplicant(aplicant: Boolean)

    @Query("SELECT * FROM municipis_descoberts WHERE id IN (:ids)")
    suspend fun descoberts(ids: List<String>): List<MunicipiDescobertEntity>

    @Query("SELECT * FROM missions_completades WHERE id IN (:ids)")
    suspend fun completades(ids: List<String>): List<MissioCompletadaEntity>

    @Query("SELECT * FROM moviments_punts WHERE id IN (:ids)")
    suspend fun moviments(ids: List<String>): List<MovimentPuntsEntity>

    @Query("SELECT * FROM missions_propies WHERE id IN (:ids)")
    suspend fun propies(ids: List<String>): List<MissioPropiaEntity>

    @Query("SELECT * FROM fotos WHERE id IN (:ids)")
    suspend fun fotos(ids: List<String>): List<FotoEntity>

    @Query("SELECT * FROM segells WHERE id IN (:ids)")
    suspend fun segells(ids: List<String>): List<SegellEntity>

    @Query("SELECT * FROM sacs WHERE id IN (:ids)")
    suspend fun sacs(ids: List<String>): List<SacEntity>

    // Aplicar el que ve del servidor. Les files que només s'afegeixen no se substitueixen mai (IGNORE);
    // les altres, sí (la fusió ja ha decidit que la del servidor és més nova).

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insereixDescoberts(l: List<MunicipiDescobertEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insereixCompletades(l: List<MissioCompletadaEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insereixMoviments(l: List<MovimentPuntsEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insereixSegells(l: List<SegellEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun desaPropies(l: List<MissioPropiaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun desaFotos(l: List<FotoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun desaSacs(l: List<SacEntity>)

    @Query("SELECT * FROM missions_propies")
    suspend fun totesLesPropies(): List<MissioPropiaEntity>

    @Query("SELECT * FROM fotos")
    suspend fun totesLesFotos(): List<FotoEntity>

    @Query("SELECT * FROM sacs")
    suspend fun totsElsSacs(): List<SacEntity>
}
