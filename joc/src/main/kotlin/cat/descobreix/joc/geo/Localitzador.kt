package cat.descobreix.joc.geo

import cat.descobreix.joc.dades.FormatLimits
import cat.descobreix.joc.dades.IndexLimits
import cat.descobreix.joc.model.CodiIne
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min

/** Límits d'un municipi a resolució completa. Cada anell és [lon0, lat0, lon1, lat1, ...]. */
class LimitMunicipi(val poligons: List<List<DoubleArray>>) {
    fun conte(lat: Double, lon: Double): Boolean = poligons.any { Geometria.dinsPoligonD(lon, lat, it) }

    /** Distància en metres del punt a la vora més propera del municipi. */
    fun distanciaAVoraMetres(lat: Double, lon: Double): Double {
        // Projecció equirectangular local: prou precisa a l'escala d'un municipi.
        val mx = METRES_PER_GRAU * cos(lat * PI / 180)
        val my = METRES_PER_GRAU
        var d = Double.MAX_VALUE
        for (pol in poligons) for (anell in pol) {
            val n = anell.size / 2
            var j = n - 1
            for (i in 0 until n) {
                val dist = Geometria.distanciaASegment(
                    0.0, 0.0,
                    (anell[j * 2] - lon) * mx, (anell[j * 2 + 1] - lat) * my,
                    (anell[i * 2] - lon) * mx, (anell[i * 2 + 1] - lat) * my,
                )
                d = min(d, dist)
                j = i
            }
        }
        return d
    }

    companion object {
        const val METRES_PER_GRAU = 111_195.0
    }
}

/** On és l'usuari. */
sealed interface Localitzacio {
    /** Clarament dins d'un municipi. */
    data class Dins(val codi: CodiIne) : Localitzacio

    /** A prop d'una frontera, o amb poca precisió: cal que l'usuari triï entre aquests municipis. */
    data class Dubte(val candidats: List<CodiIne>) : Localitzacio

    /** Fora de Catalunya. */
    data object Fora : Localitzacio
}

/**
 * Decideix en quin municipi és un punt, amb els límits a resolució completa.
 * Només carrega els municipis que poden contenir el punt (segons el requadre de l'índex).
 *
 * @param carregaBloc retorna els bytes del bloc del municipi i de `limits.bin`.
 */
class Localitzador(
    private val index: IndexLimits,
    private val codis: List<CodiIne>,
    private val margeFronteraMetres: Double,
    private val carregaBloc: (i: Int, offset: Int, mida: Int) -> ByteArray,
) {
    private val memoria = object : LinkedHashMap<Int, LimitMunicipi>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, LimitMunicipi>?): Boolean = size > MIDA_MEMORIA
    }

    init {
        require(codis.size == index.total) { "L'índex de límits i la llista de municipis no coincideixen" }
    }

    @Synchronized
    fun limit(i: Int): LimitMunicipi = memoria.getOrPut(i) {
        FormatLimits.llegeixBloc(carregaBloc(i, index.offsets[i], index.midaBloc(i)))
    }

    fun limit(codi: CodiIne): LimitMunicipi = limit(codis.indexOf(codi))

    /**
     * @param precisioMetres precisió del GPS (radi, en metres).
     */
    fun localitza(lat: Double, lon: Double, precisioMetres: Double): Localitzacio {
        val marge = maxOf(margeFronteraMetres, precisioMetres)
        val margeLat = marge / LimitMunicipi.METRES_PER_GRAU
        val margeLon = marge / (LimitMunicipi.METRES_PER_GRAU * cos(lat * PI / 180))
        val microLat = lat * 1e6
        val microLon = lon * 1e6

        val contenidors = mutableListOf<Pair<Int, Double>>()
        val propers = mutableListOf<Pair<Int, Double>>()
        for (i in 0 until index.total) {
            val r = index.requadres[i]
            if (microLon < r[0] - margeLon * 1e6 || microLon > r[2] + margeLon * 1e6) continue
            if (microLat < r[1] - margeLat * 1e6 || microLat > r[3] + margeLat * 1e6) continue
            val limit = limit(i)
            val distancia = limit.distanciaAVoraMetres(lat, lon)
            if (limit.conte(lat, lon)) {
                contenidors += i to distancia
            } else if (distancia <= marge) {
                propers += i to distancia
            }
        }

        if (contenidors.isEmpty()) {
            return when (propers.size) {
                0 -> Localitzacio.Fora
                1 -> Localitzacio.Dins(codis[propers[0].first])
                else -> Localitzacio.Dubte(propers.sortedBy { it.second }.map { codis[it.first] })
            }
        }
        // Normalment només n'hi ha un; si n'hi ha més (per arrodoniments), el més endins.
        val (principal, distanciaVora) = contenidors.maxBy { it.second }
        val aPropDeFrontera = distanciaVora < margeFronteraMetres || precisioMetres > distanciaVora
        if (!aPropDeFrontera || propers.isEmpty()) return Localitzacio.Dins(codis[principal])
        val candidats = listOf(principal) + propers.sortedBy { it.second }.map { it.first }
        return Localitzacio.Dubte(candidats.distinct().map { codis[it] })
    }

    private companion object {
        const val MIDA_MEMORIA = 24
    }
}
