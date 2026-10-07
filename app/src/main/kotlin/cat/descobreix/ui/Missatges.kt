package cat.descobreix.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.annotation.PluralsRes
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cat.descobreix.R
import cat.descobreix.domain.Joc
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.regles.ResultatDesbloqueig
import cat.descobreix.joc.regles.ResultatProva
import cat.descobreix.ui.theme.Colors
import kotlin.math.roundToInt

/** Missatges que l'app mostra a l'usuari després d'una acció del joc. */
sealed interface Missatge {
    data class PuntsGuanyats(val punts: Int, val bonus: Int) : Missatge

    data class FotoDesada(val punts: Int, val bonus: Int) : Missatge

    data class Desbloquejat(val nom: String) : Missatge

    data class PrecisioInsuficient(val precisio: Int, val maxima: Int) : Missatge

    data class MassaLluny(val distancia: Int, val radi: Int) : Missatge

    data class AltreMunicipi(val nom: String) : Missatge

    /** L'usuari és dins d'un municipi que no té desbloquejat: no hi pot fer res. */
    data class MunicipiBloquejat(val nom: String, val falten: Int?, val distancia: Int?) : Missatge

    data object ForaDeCatalunya : Missatge

    data object SenseUbicacio : Missatge

    data object SensePermisUbicacio : Missatge

    data object SensePermisCamera : Missatge

    data object Error : Missatge

    /** No es llegeix el nom del municipi a la foto del cartell. */
    data class CartellNoLlegit(val nom: String) : Missatge

    /** S'ha canviat la foto del cartell del catàleg (sense punts: la missió ja estava feta). */
    data object CromoNou : Missatge
}

/** Converteix el resultat d'una prova en un missatge per a l'usuari. */
suspend fun Joc.missatgeDe(resultat: ResultatProva): Missatge? {
    val geografia = dades().geografia
    return when (resultat) {
        ResultatProva.Valida, is ResultatProva.CalTriarMunicipi -> null
        is ResultatProva.PrecisioInsuficient -> Missatge.PrecisioInsuficient(resultat.precisio.roundToInt(), resultat.maxima.roundToInt())
        is ResultatProva.MassaLluny -> Missatge.MassaLluny(resultat.distancia.roundToInt(), resultat.radi.roundToInt())
        is ResultatProva.AltreMunicipi -> Missatge.AltreMunicipi(geografia.municipi(resultat.codi).nom)
        is ResultatProva.MunicipiBloquejat -> missatgeBloquejat(resultat.codi)
        ResultatProva.ForaDeCatalunya -> Missatge.ForaDeCatalunya
    }
}

/** Explica a l'usuari que és en un municipi bloquejat i quants punts li falten. */
suspend fun Joc.missatgeBloquejat(codi: CodiIne): Missatge.MunicipiBloquejat {
    val nom = dades().geografia.municipi(codi).nom
    return when (val r = avaluaDesbloqueig(codi)) {
        is ResultatDesbloqueig.PuntsInsuficients -> Missatge.MunicipiBloquejat(nom, r.falten, null)
        is ResultatDesbloqueig.Permes -> Missatge.MunicipiBloquejat(nom, 0, null)
        is ResultatDesbloqueig.NoDisponible -> Missatge.MunicipiBloquejat(nom, null, r.distancia)
        ResultatDesbloqueig.JaDescobert -> Missatge.MunicipiBloquejat(nom, 0, null)
    }
}

@Composable
fun textDe(m: Missatge): String = when (m) {
    is Missatge.PuntsGuanyats ->
        if (m.bonus > 0) {
            plural(R.plurals.missatge_punts_amb_bonus, m.punts, m.punts, m.bonus)
        } else {
            plural(R.plurals.missatge_punts, m.punts, m.punts)
        }
    is Missatge.FotoDesada -> when {
        m.punts == 0 -> stringResource(R.string.missatge_foto_desada)
        m.bonus > 0 -> plural(R.plurals.missatge_foto_punts_amb_bonus, m.punts, m.punts, m.bonus)
        else -> plural(R.plurals.missatge_foto_punts, m.punts, m.punts)
    }
    is Missatge.Desbloquejat -> stringResource(R.string.missatge_desbloquejat, m.nom)
    is Missatge.PrecisioInsuficient -> stringResource(R.string.missatge_precisio, m.precisio, m.maxima)
    is Missatge.MassaLluny -> stringResource(R.string.missatge_massa_lluny, m.distancia, m.radi)
    is Missatge.AltreMunicipi -> stringResource(R.string.missatge_altre_municipi, m.nom)
    is Missatge.MunicipiBloquejat -> when {
        m.distancia != null -> plural(R.plurals.missatge_bloquejat_boira, m.distancia, m.nom, m.distancia)
        m.falten == null || m.falten == 0 -> stringResource(R.string.missatge_bloquejat_pots, m.nom)
        else -> plural(R.plurals.missatge_bloquejat_falten, m.falten, m.nom, m.falten)
    }
    Missatge.ForaDeCatalunya -> stringResource(R.string.missatge_fora)
    Missatge.SenseUbicacio -> stringResource(R.string.missatge_sense_ubicacio)
    Missatge.SensePermisUbicacio -> stringResource(R.string.missatge_sense_permis_ubicacio)
    Missatge.SensePermisCamera -> stringResource(R.string.missatge_sense_permis_camera)
    Missatge.Error -> stringResource(R.string.missatge_error)
    is Missatge.CartellNoLlegit -> stringResource(R.string.missatge_cartell_no_llegit, m.nom)
    Missatge.CromoNou -> stringResource(R.string.missatge_cromo_nou)
}

/** Si el missatge és prou important per mostrar-lo en un diàleg (i no en una notificació breu). */
val Missatge.esDialeg: Boolean
    get() = this is Missatge.MunicipiBloquejat || this is Missatge.MassaLluny || this is Missatge.PrecisioInsuficient ||
        this is Missatge.AltreMunicipi || this is Missatge.ForaDeCatalunya || this is Missatge.CartellNoLlegit

@Composable
fun DialegMissatge(missatge: Missatge, onTanca: () -> Unit) {
    AlertDialog(
        onDismissRequest = onTanca,
        confirmButton = { TextButton(onClick = onTanca) { Text(stringResource(R.string.entesos)) } },
        title = {
            Text(
                stringResource(
                    if (missatge is Missatge.MunicipiBloquejat) R.string.titol_municipi_bloquejat else R.string.titol_no_ha_funcionat,
                ),
            )
        },
        text = { Text(textDe(missatge), style = MaterialTheme.typography.bodyLarge) },
        containerColor = Colors.Superficie,
    )
}

/** Diàleg per triar el municipi quan l'usuari és a prop d'una frontera. */
@Composable
fun DialegTriaMunicipi(
    candidats: List<Pair<CodiIne, String>>,
    onTria: (CodiIne) -> Unit,
    onCancela: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancela,
        confirmButton = {},
        dismissButton = { TextButton(onClick = onCancela) { Text(stringResource(R.string.cancela)) } },
        title = { Text(stringResource(R.string.titol_on_ets)) },
        text = {
            Column {
                Text(stringResource(R.string.text_on_ets), style = MaterialTheme.typography.bodyMedium)
                for ((codi, nom) in candidats) {
                    Text(
                        nom,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clickable(role = Role.Button) { onTria(codi) }
                            .padding(vertical = 12.dp),
                    )
                }
            }
        },
        containerColor = Colors.Superficie,
    )
}

/** Text en plural segons [quantitat]. */
@Composable
fun plural(@PluralsRes id: Int, quantitat: Int, vararg arguments: Any): String =
    LocalContext.current.resources.getQuantityString(id, quantitat, *arguments)
