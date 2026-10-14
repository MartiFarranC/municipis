package cat.descobreix.joc.model

/** Codi INE de 5 xifres d'un municipi (per exemple, "08019" per a Barcelona). */
typealias CodiIne = String

data class Comarca(
    val codi: String,
    val nom: String,
    val capitals: List<CodiIne>,
)

data class Municipi(
    val codi: CodiIne,
    val nom: String,
    val comarca: String,
    val veins: List<CodiIne>,
)

enum class EstatMunicipi {
    /** L'usuari l'ha desbloquejat, o és el seu municipi d'inici. */
    DESCOBERT,

    /** Fa frontera (o té una connexió especial) amb almenys un municipi descobert. */
    DISPONIBLE,

    /** La resta. */
    BOIRA,
}

/** OFICIAL: les missions que posa un ajuntament (secció 9.6 de docs/requisits.md). */
enum class TipusMissio { LLOC, GENERICA, OFICIAL }

/** QR: llegir el codi d'un punt de segellat d'un ajuntament. */
enum class TipusProva { GPS, FOTO, QR }

/** Visibilitat d'una foto. A la versió 1 totes les fotos són locals, però el camp es guarda. */
enum class Visibilitat { PRIVADA, SEGUIDORS, PUBLICA }

data class Missio(
    val id: String,
    val municipi: CodiIne,
    val titol: String,
    val tipus: TipusMissio,
    /** Per a les genèriques, la clau de la missió (per agafar el títol traduït de l'app). */
    val clau: String?,
    val categoria: String?,
    val lat: Double?,
    val lon: Double?,
    val prova: TipusProva,
    val punts: Int,
    val font: FontMissio,
) {
    val teCoordenades: Boolean get() = lat != null && lon != null
}

data class FontMissio(
    /** "generica", "wikidata", "osm" o "ajuntament". */
    val tipus: String,
    /** Identificador a la font (Q123, node/123...), si en té. */
    val id: String?,
)
