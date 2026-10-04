package cat.descobreix.compte

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cat.descobreix.R
import cat.descobreix.data.compte.CONTRASENYA_MINIM
import cat.descobreix.data.compte.ErrorCompte
import cat.descobreix.data.compte.NomUsuari
import cat.descobreix.ui.components.BotoPrincipal
import cat.descobreix.ui.components.BotoSecundari
import cat.descobreix.ui.theme.Colors

/** Inici de sessió i registre (secció 4, pantalla 6). */
@Composable
fun CompteScreen(viewModel: CompteViewModel = hiltViewModel()) {
    val estat by viewModel.estat.collectAsStateWithLifecycle()
    val mode = estat.mode
    PantallaCompte(
        titol = stringResource(
            when (mode) {
                ModeCompte.ENTRA -> R.string.compte_titol_entra
                ModeCompte.REGISTRA -> R.string.compte_titol_registra
                ModeCompte.ENLLAC -> R.string.compte_titol_enllac
                ModeCompte.RECUPERA -> R.string.compte_titol_recupera
            },
        ),
        text = stringResource(
            when (mode) {
                ModeCompte.ENTRA, ModeCompte.REGISTRA -> R.string.compte_text
                ModeCompte.ENLLAC -> R.string.compte_text_enllac
                ModeCompte.RECUPERA -> R.string.compte_text_recupera
            },
        ),
        error = estat.error,
    ) {
        Camp(
            valor = estat.correu,
            onCanvi = viewModel::canviaCorreu,
            etiqueta = stringResource(R.string.compte_correu),
            tipus = KeyboardType.Email,
            accio = if (mode == ModeCompte.ENTRA || mode == ModeCompte.REGISTRA) ImeAction.Next else ImeAction.Done,
            onAccio = viewModel::envia,
        )
        if (mode == ModeCompte.ENTRA || mode == ModeCompte.REGISTRA) {
            Camp(
                valor = estat.contrasenya,
                onCanvi = viewModel::canviaContrasenya,
                etiqueta = stringResource(R.string.compte_contrasenya),
                tipus = KeyboardType.Password,
                accio = if (mode == ModeCompte.REGISTRA) ImeAction.Next else ImeAction.Done,
                onAccio = viewModel::envia,
                ajuda = if (mode == ModeCompte.REGISTRA) stringResource(R.string.compte_contrasenya_ajuda, CONTRASENYA_MINIM) else null,
            )
        }
        if (mode == ModeCompte.REGISTRA) {
            Camp(
                valor = estat.repeticio,
                onCanvi = viewModel::canviaRepeticio,
                etiqueta = stringResource(R.string.compte_repeteix_contrasenya),
                tipus = KeyboardType.Password,
                accio = ImeAction.Done,
                onAccio = viewModel::envia,
                ajuda = if (estat.contrasenyesDiferents) stringResource(R.string.compte_contrasenyes_diferents) else null,
                esError = estat.contrasenyesDiferents,
            )
        }
        BotoPrincipal(
            text = stringResource(
                when (mode) {
                    ModeCompte.ENTRA -> R.string.compte_entra
                    ModeCompte.REGISTRA -> R.string.compte_registra
                    ModeCompte.ENLLAC, ModeCompte.RECUPERA -> R.string.compte_envia_enllac
                },
            ),
            onClick = viewModel::envia,
            enabled = estat.potEnviar,
            carregant = estat.treballant,
        )
        when (mode) {
            ModeCompte.ENTRA -> {
                Enllac(stringResource(R.string.compte_vull_registrar)) { viewModel.canviaMode(ModeCompte.REGISTRA) }
                // De moment l'app no envia correus: l'enllaç màgic i la recuperació de la contrasenya
                // no es mostren fins que el projecte de Supabase tingui un SMTP propi.
            }
            ModeCompte.REGISTRA -> Enllac(stringResource(R.string.compte_ja_tinc_compte)) { viewModel.canviaMode(ModeCompte.ENTRA) }
            ModeCompte.ENLLAC, ModeCompte.RECUPERA ->
                Enllac(stringResource(R.string.compte_entra_amb_contrasenya)) { viewModel.canviaMode(ModeCompte.ENTRA) }
        }
    }

    estat.avis?.let { avis ->
        AlertDialog(
            onDismissRequest = viewModel::tancaAvis,
            containerColor = Colors.Superficie,
            title = { Text(stringResource(R.string.compte_mira_correu)) },
            text = {
                Text(
                    stringResource(
                        when (avis) {
                            AvisCompte.CONFIRMA_CORREU -> R.string.compte_avis_confirma
                            AvisCompte.ENLLAC_ENVIAT -> R.string.compte_avis_enllac
                            AvisCompte.RECUPERACIO_ENVIADA -> R.string.compte_avis_recupera
                        },
                        estat.correu.trim(),
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            confirmButton = { TextButton(onClick = viewModel::tancaAvis) { Text(stringResource(R.string.entesos)) } },
        )
    }
}

/** Triar el nom d'usuari la primera vegada que s'entra a l'app. */
@Composable
fun NomUsuariScreen(viewModel: CompteViewModel = hiltViewModel()) {
    val estat by viewModel.estat.collectAsStateWithLifecycle()
    PantallaCompte(
        titol = stringResource(R.string.compte_titol_nom),
        text = stringResource(R.string.compte_text_nom),
        error = estat.error,
    ) {
        Camp(
            valor = estat.nomUsuari,
            onCanvi = viewModel::canviaNomUsuari,
            etiqueta = stringResource(R.string.compte_nom_usuari),
            tipus = KeyboardType.Ascii,
            accio = ImeAction.Done,
            onAccio = viewModel::creaPerfil,
            ajuda = stringResource(R.string.compte_nom_ajuda, NomUsuari.MIN, NomUsuari.MAX),
        )
        BotoPrincipal(
            text = stringResource(R.string.compte_desa_nom),
            onClick = viewModel::creaPerfil,
            enabled = estat.nomUsuariValid,
            carregant = estat.treballant,
        )
        Enllac(stringResource(R.string.compte_surt)) { viewModel.surt() }
    }
}

/** Triar una contrasenya nova després d'obrir l'enllaç de recuperació. */
@Composable
fun NovaContrasenyaScreen(viewModel: CompteViewModel = hiltViewModel()) {
    val estat by viewModel.estat.collectAsStateWithLifecycle()
    PantallaCompte(
        titol = stringResource(R.string.compte_titol_nova_contrasenya),
        text = stringResource(R.string.compte_text_nova_contrasenya),
        error = estat.error,
    ) {
        Camp(
            valor = estat.contrasenya,
            onCanvi = viewModel::canviaContrasenya,
            etiqueta = stringResource(R.string.compte_contrasenya_nova),
            tipus = KeyboardType.Password,
            accio = ImeAction.Done,
            onAccio = viewModel::desaContrasenyaNova,
            ajuda = stringResource(R.string.compte_contrasenya_ajuda, CONTRASENYA_MINIM),
        )
        BotoPrincipal(
            text = stringResource(R.string.compte_desa_contrasenya),
            onClick = viewModel::desaContrasenyaNova,
            enabled = estat.contrasenyaNovaValida,
            carregant = estat.treballant,
        )
    }
}

/** Hi ha sessió, però cal connexió per saber si l'usuari ja té perfil. */
@Composable
fun SenseConnexioScreen(viewModel: CompteViewModel = hiltViewModel()) {
    val estat by viewModel.estat.collectAsStateWithLifecycle()
    PantallaCompte(
        titol = stringResource(R.string.compte_titol_sense_connexio),
        text = stringResource(R.string.compte_text_sense_connexio),
        error = estat.error,
    ) {
        BotoPrincipal(stringResource(R.string.torna_a_provar), viewModel::reintenta, carregant = estat.treballant)
        BotoSecundari(stringResource(R.string.compte_surt), viewModel::surt, enabled = !estat.treballant)
    }
}

@Composable
private fun PantallaCompte(
    titol: String,
    text: String,
    error: ErrorCompte?,
    contingut: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.app_name).uppercase(), style = MaterialTheme.typography.labelSmall.copy(color = Colors.Ambre))
        Text(titol, style = MaterialTheme.typography.displayLarge, modifier = Modifier.semantics { heading() })
        Text(text, style = MaterialTheme.typography.bodyLarge.copy(color = Colors.TextSecundari))
        if (error != null) {
            Text(textError(error), style = MaterialTheme.typography.bodyMedium.copy(color = Colors.Error))
        }
        contingut()
    }
}

@Composable
private fun Camp(
    valor: String,
    onCanvi: (String) -> Unit,
    etiqueta: String,
    tipus: KeyboardType,
    accio: ImeAction,
    onAccio: () -> Unit,
    ajuda: String? = null,
    esError: Boolean = false,
) {
    val esContrasenya = tipus == KeyboardType.Password
    OutlinedTextField(
        value = valor,
        onValueChange = onCanvi,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        supportingText = ajuda?.let { { Text(it) } },
        isError = esError,
        singleLine = true,
        visualTransformation = if (esContrasenya) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = tipus,
            imeAction = accio,
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
        ),
        keyboardActions = KeyboardActions(onAny = { onAccio() }),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Colors.Ambre,
            unfocusedBorderColor = Colors.Linia,
            focusedContainerColor = Colors.Superficie,
            unfocusedContainerColor = Colors.Superficie,
            focusedLabelColor = Colors.Ambre,
            cursorColor = Colors.Ambre,
        ),
    )
}

@Composable
private fun Enllac(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Text(text, style = MaterialTheme.typography.labelLarge.copy(color = Colors.Blau))
    }
}

@Composable
private fun textError(error: ErrorCompte): String = stringResource(
    when (error) {
        ErrorCompte.CREDENCIALS_INCORRECTES -> R.string.compte_error_credencials
        ErrorCompte.CORREU_NO_CONFIRMAT -> R.string.compte_error_no_confirmat
        ErrorCompte.CORREU_JA_REGISTRAT -> R.string.compte_error_ja_registrat
        ErrorCompte.CORREU_INVALID -> R.string.compte_error_correu
        ErrorCompte.CONTRASENYA_FEBLE -> R.string.compte_error_contrasenya_feble
        ErrorCompte.MATEIXA_CONTRASENYA -> R.string.compte_error_mateixa_contrasenya
        ErrorCompte.MASSA_INTENTS -> R.string.compte_error_massa_intents
        ErrorCompte.NOM_USUARI_AGAFAT -> R.string.compte_error_nom_agafat
        ErrorCompte.NOM_USUARI_INVALID -> R.string.compte_error_nom_invalid
        ErrorCompte.SENSE_CONNEXIO -> R.string.compte_error_sense_connexio
        ErrorCompte.DESCONEGUT -> R.string.compte_error_desconegut
    },
)
