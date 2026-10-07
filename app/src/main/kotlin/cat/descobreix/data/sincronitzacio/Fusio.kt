package cat.descobreix.data.sincronitzacio

/**
 * Com s'ajunta el que baixa del servidor amb el que hi ha al mòbil (requisits.md, secció 9.2). No depèn de Room ni de
 * Supabase, perquè es pugui provar sol.
 *
 * Cada fila té un id (UUID) i una clau natural que no es pot repetir per usuari (el codi del municipi, la missió,
 * l'origen del sac…). Si dos mòbils han fet el mateix sense connexió, hi haurà dues files amb la mateixa clau i ids
 * diferents: val la del servidor, que ja hi era.
 */
object Fusio {
    /** El que cal fer al mòbil després de baixar: quines files del servidor s'hi afegeixen o s'hi desen i quines locals es treuen. */
    data class Resultat<T>(val desa: List<T>, val treuLocals: List<String>, val tornaAPujar: List<String> = emptyList())

    /**
     * Files que només s'afegeixen (municipis descoberts, missions completades, moviments de punts, segells): s'afegeixen
     * les del servidor que no hi són, i es treuen les locals que tenen la mateixa clau amb un altre id.
     */
    fun <T> afegibles(locals: List<T>, remotes: List<T>, id: (T) -> String, clau: (T) -> String): Resultat<T> {
        val perId = locals.associateBy(id)
        val perClau = locals.associateBy(clau)
        val desa = mutableListOf<T>()
        val treu = mutableListOf<String>()
        for (r in remotes) {
            if (id(r) in perId) continue
            val local = perClau[clau(r)]
            if (local != null) treu += id(local)
            desa += r
        }
        return Resultat(desa, treu)
    }

    /**
     * Files que es modifiquen (missions pròpies, fotos, sacs): guanya la modificació més recent. Si una local té la
     * mateixa clau que una del servidor amb un altre id, se'n queda l'id del servidor i les dades de [ajunta]; si el
     * resultat no és igual que el del servidor, s'ha de tornar a pujar.
     */
    fun <T> modificables(
        locals: List<T>,
        remotes: List<T>,
        id: (T) -> String,
        clau: (T) -> String,
        modificatEl: (T) -> Long,
        ajunta: (local: T, remot: T) -> T = { _, r -> r },
    ): Resultat<T> {
        val perId = locals.associateBy(id)
        val perClau = locals.associateBy(clau)
        val desa = mutableListOf<T>()
        val treu = mutableListOf<String>()
        val puja = mutableListOf<String>()
        for (r in remotes) {
            val mateixa = perId[id(r)]
            if (mateixa != null) {
                if (modificatEl(r) > modificatEl(mateixa)) desa += r
                continue
            }
            val altra = perClau[clau(r)]
            if (altra == null) {
                desa += r
                continue
            }
            val junta = ajunta(altra, r)
            treu += id(altra)
            desa += junta
            if (junta != r) puja += id(r)
        }
        return Resultat(desa, treu, puja)
    }
}
