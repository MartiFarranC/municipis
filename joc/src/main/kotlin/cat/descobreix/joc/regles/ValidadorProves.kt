package cat.descobreix.joc.regles

import cat.descobreix.joc.config.ConfiguracioJoc
import cat.descobreix.joc.geo.Geometria
import cat.descobreix.joc.geo.Localitzacio
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.model.Missio
import cat.descobreix.joc.model.TipusProva

data class Ubicacio(val lat: Double, val lon: Double, val precisioMetres: Double)

sealed interface ResultatProva {
    data object Valida : ResultatProva

    /** El GPS no és prou precís. */
    data class PrecisioInsuficient(val precisio: Double, val maxima: Double) : ResultatProva

    /** Massa lluny del lloc de la missió. */
    data class MassaLluny(val distancia: Double, val radi: Double) : ResultatProva

    /** L'usuari és en un altre municipi que sí que té descobert. */
    data class AltreMunicipi(val codi: CodiIne) : ResultatProva

    /** L'usuari és en un municipi que encara no ha desbloquejat: no hi pot fer res. */
    data class MunicipiBloquejat(val codi: CodiIne) : ResultatProva

    /** És a prop d'una frontera: cal que triï en quin municipi és. */
    data class CalTriarMunicipi(val candidats: List<CodiIne>) : ResultatProva

    data object ForaDeCatalunya : ResultatProva
}

/** Validació de les proves de les missions (secció 3.4 de docs/requisits.md). */
class ValidadorProves(private val config: ConfiguracioJoc) {
    /**
     * Comprova on és l'usuari respecte del municipi de la missió.
     * @param municipiTriat el municipi que l'usuari ha dit que és, si abans hi havia dubte.
     */
    fun comprovaMunicipi(
        municipi: CodiIne,
        localitzacio: Localitzacio,
        descoberts: Set<CodiIne>,
        municipiTriat: CodiIne? = null,
    ): ResultatProva {
        val on: CodiIne = when (localitzacio) {
            is Localitzacio.Dins -> localitzacio.codi
            is Localitzacio.Fora -> return ResultatProva.ForaDeCatalunya
            is Localitzacio.Dubte -> when {
                municipiTriat != null && municipiTriat in localitzacio.candidats -> municipiTriat
                else -> return ResultatProva.CalTriarMunicipi(localitzacio.candidats)
            }
        }
        return when {
            on == municipi -> ResultatProva.Valida
            on !in descoberts -> ResultatProva.MunicipiBloquejat(on)
            else -> ResultatProva.AltreMunicipi(on)
        }
    }

    /**
     * Valida una missió amb prova GPS. Les de lloc demanen ser a prop de les coordenades;
     * les genèriques (check-in), ser dins del municipi.
     */
    fun validaGps(
        missio: Missio,
        ubicacio: Ubicacio,
        localitzacio: Localitzacio,
        descoberts: Set<CodiIne>,
        municipiTriat: CodiIne? = null,
    ): ResultatProva {
        require(missio.prova == TipusProva.GPS) { "La missió ${missio.id} no és de GPS" }
        if (missio.municipi !in descoberts) return ResultatProva.MunicipiBloquejat(missio.municipi)
        val gps = config.gps
        if (ubicacio.precisioMetres > gps.precisioMaximaMetres) {
            return ResultatProva.PrecisioInsuficient(ubicacio.precisioMetres, gps.precisioMaximaMetres)
        }
        if (missio.lat != null && missio.lon != null) {
            // Si l'usuari és clarament dins d'un municipi bloquejat, no hi pot fer res.
            if (localitzacio is Localitzacio.Dins && localitzacio.codi != missio.municipi && localitzacio.codi !in descoberts) {
                return ResultatProva.MunicipiBloquejat(localitzacio.codi)
            }
            val d = Geometria.distanciaMetres(ubicacio.lat, ubicacio.lon, missio.lat, missio.lon)
            return if (d <= gps.radiMissioMetres) ResultatProva.Valida else ResultatProva.MassaLluny(d, gps.radiMissioMetres)
        }
        return comprovaMunicipi(missio.municipi, localitzacio, descoberts, municipiTriat)
    }

    /** Valida una foto: s'ha de fer dins del municipi, i el municipi ha d'estar desbloquejat. */
    fun validaFoto(
        municipi: CodiIne,
        localitzacio: Localitzacio,
        descoberts: Set<CodiIne>,
        municipiTriat: CodiIne? = null,
    ): ResultatProva {
        if (municipi !in descoberts) return ResultatProva.MunicipiBloquejat(municipi)
        return comprovaMunicipi(municipi, localitzacio, descoberts, municipiTriat)
    }
}
