package cat.descobreix.domain

import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.model.Visibilitat
import cat.descobreix.joc.config.ConfiguracioJoc.TipusObjecte
import cat.descobreix.joc.progressio.SegellPosat
import cat.descobreix.joc.regles.EstatJoc

/** Progrés de l'usuari. */
data class Progres(
    val inici: CodiIne?,
    /** Municipis descoberts, en l'ordre en què es van descobrir. */
    val descoberts: List<CodiIne>,
    /** Missions automàtiques completades, per identificador. */
    val completades: Map<String, MissioCompletada>,
    val puntsGuanyats: Int,
    val puntsGastats: Int,
) {
    val partidaIniciada: Boolean get() = inici != null
    val saldo: Int get() = puntsGuanyats - puntsGastats
    val conjuntDescoberts: Set<CodiIne> by lazy { descoberts.toSet() }

    fun estatJoc(): EstatJoc = EstatJoc(conjuntDescoberts, puntsGuanyats, puntsGastats, completades.keys)

    companion object {
        val BUIT = Progres(null, emptyList(), emptyMap(), 0, 0)
    }
}

data class MissioCompletada(
    val missioId: String,
    val codiIne: CodiIne,
    val punts: Int,
    val bonus: Int,
    val completadaEl: Long,
)

data class MissioPropia(
    val id: String,
    val codiIne: CodiIne,
    val titol: String,
    val descripcio: String?,
    val completada: Boolean,
    val creatEl: Long,
)

data class Foto(
    val id: String,
    val codiIne: CodiIne,
    /** Camí absolut del fitxer de la foto. */
    val fitxer: String,
    val miniatura: String,
    val lat: Double?,
    val lon: Double?,
    val visibilitat: Visibilitat,
    val esPortada: Boolean,
    val missioId: String?,
    val creatEl: Long,
)

/** Un segell del passaport amb la data en què es va posar. */
data class Segell(val posat: SegellPosat, val creatEl: Long)


/** Una cosa de la col·lecció (emoji, portada, animació o color), pel seu tipus i el seu id. */
data class ClauObjecte(val tipus: TipusObjecte, val id: String)

/** Un sac guanyat. Si està obert, en va sortir [objecte] o, si ja es tenia tot, [punts]. */
data class Sac(val origen: String, val creatEl: Long, val obert: Boolean, val objecte: ClauObjecte?, val punts: Int?)
