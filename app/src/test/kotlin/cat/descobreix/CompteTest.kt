package cat.descobreix

import android.content.Intent
import cat.descobreix.compte.AvisCompte
import cat.descobreix.compte.CompteEstat
import cat.descobreix.compte.CompteViewModel
import cat.descobreix.compte.ModeCompte
import cat.descobreix.data.compte.ErrorCompte
import cat.descobreix.data.compte.EstatCompte
import cat.descobreix.data.compte.ExcepcioCompte
import cat.descobreix.data.compte.NomUsuari
import cat.descobreix.data.compte.Perfil
import cat.descobreix.data.compte.ResultatRegistre
import cat.descobreix.data.compte.ServeiCompte
import cat.descobreix.data.compte.TipusPerfil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Un compte en memòria. Si [error] no és null, totes les operacions fallen amb aquest error. */
class CompteEnMemoria : ServeiCompte {
    override val estat = MutableStateFlow<EstatCompte>(EstatCompte.SenseSessio)
    var error: ErrorCompte? = null
    var calConfirmar = false
    val crides = mutableListOf<String>()

    private fun crida(nom: String) {
        crides += nom
        error?.let { throw ExcepcioCompte(it) }
    }

    override suspend fun entra(correu: String, contrasenya: String) {
        crida("entra $correu")
        estat.value = EstatCompte.CalPerfil
    }

    override suspend fun registra(correu: String, contrasenya: String): ResultatRegistre {
        crida("registra $correu")
        return if (calConfirmar) ResultatRegistre.CAL_CONFIRMAR_CORREU else ResultatRegistre.FET
    }

    override suspend fun enviaEnllac(correu: String) = crida("enllac $correu")

    override suspend fun recuperaContrasenya(correu: String) = crida("recupera $correu")

    override suspend fun canviaContrasenya(nova: String) = crida("contrasenya")

    override suspend fun creaPerfil(nomUsuari: String, tipus: TipusPerfil, public: Boolean) {
        crida("perfil $nomUsuari $tipus $public")
        estat.value = EstatCompte.Llest(Perfil("id", nomUsuari, tipus, public))
    }

    override suspend fun triaVisibilitat(public: Boolean) = crida("visibilitat $public")

    override suspend fun reintenta() = crida("reintenta")

    override suspend fun surt() {
        crida("surt")
        estat.value = EstatCompte.SenseSessio
    }

    override suspend fun esborraDades() = crida("esborra")

    override fun gestionaEnllac(intent: Intent) = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class CompteTest {
    private val compte = CompteEnMemoria()

    @Before
    fun abans() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun despres() = Dispatchers.resetMain()

    @Test
    fun `normes del nom d'usuari`() {
        assertTrue(NomUsuari.esValid("anna"))
        assertTrue(NomUsuari.esValid("biel_2024"))
        assertFalse(NomUsuari.esValid("an"), "massa curt")
        assertFalse(NomUsuari.esValid("a".repeat(21)), "massa llarg")
        assertFalse(NomUsuari.esValid("Anna"), "majúscules")
        assertFalse(NomUsuari.esValid("martí"), "accents")
        assertFalse(NomUsuari.esValid("anna maria"), "espais")
        assertEquals("anna", NomUsuari.normalitza("  Anna "))
    }

    @Test
    fun `comprova el correu i la contrasenya abans d'enviar`() {
        assertTrue(CompteEstat.esCorreu("anna@example.com"))
        assertTrue(CompteEstat.esCorreu(" anna@example.cat "))
        assertFalse(CompteEstat.esCorreu("anna"))
        assertFalse(CompteEstat.esCorreu("anna@example"))
        assertFalse(CompteEstat.esCorreu("an na@example.com"))

        assertFalse(CompteEstat(correu = "anna@example.com").potEnviar, "cal la contrasenya per entrar")
        assertTrue(CompteEstat(correu = "anna@example.com", contrasenya = "x").potEnviar)
        assertFalse(CompteEstat(ModeCompte.REGISTRA, "anna@example.com", "12345").potEnviar, "contrasenya massa curta")
        assertFalse(CompteEstat(ModeCompte.REGISTRA, "anna@example.com", "123456").potEnviar, "cal repetir la contrasenya")
        assertFalse(CompteEstat(ModeCompte.REGISTRA, "anna@example.com", "123456", "123457").potEnviar, "no coincideixen")
        assertTrue(CompteEstat(ModeCompte.REGISTRA, "anna@example.com", "123456", "123456").potEnviar)
        assertTrue(CompteEstat(ModeCompte.ENLLAC, "anna@example.com").potEnviar, "l'enllaç no necessita contrasenya")
    }

    @Test
    fun `entrar amb correu i contrasenya`() = runTest {
        val vm = CompteViewModel(compte)
        vm.canviaCorreu("anna@example.com")
        vm.canviaContrasenya("secreta")
        vm.envia()
        assertEquals(listOf("entra anna@example.com"), compte.crides)
        assertEquals(EstatCompte.CalPerfil, compte.estat.value)
        assertFalse(vm.estat.value.treballant)
        assertNull(vm.estat.value.error)
    }

    @Test
    fun `un error es mostra i s'esborra en tornar a escriure`() = runTest {
        compte.error = ErrorCompte.CREDENCIALS_INCORRECTES
        val vm = CompteViewModel(compte)
        vm.canviaCorreu("anna@example.com")
        vm.canviaContrasenya("dolenta")
        vm.envia()
        assertEquals(ErrorCompte.CREDENCIALS_INCORRECTES, vm.estat.value.error)
        assertFalse(vm.estat.value.treballant)

        vm.canviaContrasenya("dolenta2")
        assertNull(vm.estat.value.error)
    }

    @Test
    fun `registrar-se avisa si cal confirmar el correu`() = runTest {
        compte.calConfirmar = true
        val vm = CompteViewModel(compte)
        vm.canviaMode(ModeCompte.REGISTRA)
        vm.canviaCorreu("anna@example.com")
        vm.canviaContrasenya("secreta")
        vm.canviaRepeticio("secreta")
        vm.envia()
        assertEquals(AvisCompte.CONFIRMA_CORREU, vm.estat.value.avis)
    }

    @Test
    fun `registrar-se sense confirmació entra directament`() = runTest {
        val vm = CompteViewModel(compte)
        vm.canviaMode(ModeCompte.REGISTRA)
        vm.canviaCorreu("anna@example.com")
        vm.canviaContrasenya("secreta")
        vm.canviaRepeticio("secret")
        assertTrue(vm.estat.value.contrasenyesDiferents)
        vm.envia()
        assertTrue(compte.crides.isEmpty(), "no s'envia si les contrasenyes no coincideixen")

        vm.canviaRepeticio("secreta")
        assertFalse(vm.estat.value.contrasenyesDiferents)
        vm.envia()
        assertEquals(listOf("registra anna@example.com"), compte.crides)
        assertNull(vm.estat.value.avis)
    }

    @Test
    fun `els enllaços avisen que cal mirar el correu`() = runTest {
        val vm = CompteViewModel(compte)
        vm.canviaCorreu("anna@example.com")
        vm.canviaMode(ModeCompte.ENLLAC)
        vm.envia()
        assertEquals(AvisCompte.ENLLAC_ENVIAT, vm.estat.value.avis)
        vm.tancaAvis()

        vm.canviaMode(ModeCompte.RECUPERA)
        vm.envia()
        assertEquals(AvisCompte.RECUPERACIO_ENVIADA, vm.estat.value.avis)
        assertEquals(listOf("enllac anna@example.com", "recupera anna@example.com"), compte.crides)
    }

    @Test
    fun `canviar de mode esborra la contrasenya`() {
        val vm = CompteViewModel(compte)
        vm.canviaCorreu("anna@example.com")
        vm.canviaContrasenya("secreta")
        vm.canviaMode(ModeCompte.REGISTRA)
        vm.canviaRepeticio("secreta")
        vm.canviaMode(ModeCompte.ENTRA)
        assertEquals("", vm.estat.value.contrasenya)
        assertEquals("", vm.estat.value.repeticio)
        assertEquals("anna@example.com", vm.estat.value.correu)
    }

    @Test
    fun `un nom d'usuari no vàlid no s'envia al servidor`() = runTest {
        val vm = CompteViewModel(compte)
        vm.canviaNomUsuari("Anna!")
        vm.creaPerfil()
        assertEquals(ErrorCompte.NOM_USUARI_INVALID, vm.estat.value.error)
        assertTrue(compte.crides.isEmpty())
    }

    @Test
    fun `crear el perfil deixa l'usuari llest per jugar`() = runTest {
        val vm = CompteViewModel(compte)
        vm.canviaNomUsuari(" Anna_1 ")
        vm.triaTipus(TipusPerfil.ESPECTADOR)
        vm.triaPublic(false)
        vm.creaPerfil()
        assertEquals(listOf("perfil  Anna_1  ESPECTADOR false"), compte.crides)
        assertEquals(EstatCompte.Llest(Perfil("id", " Anna_1 ", TipusPerfil.ESPECTADOR, false)), compte.estat.value)
    }

    @Test
    fun `cal triar explorador o espectador i públic o privat, no hi ha res per defecte`() = runTest {
        val vm = CompteViewModel(compte)
        vm.canviaNomUsuari("anna")
        assertFalse(vm.estat.value.potCrearPerfil)
        vm.triaTipus(TipusPerfil.EXPLORADOR)
        assertFalse(vm.estat.value.potCrearPerfil)
        vm.creaPerfil()
        assertTrue(compte.crides.isEmpty())
        vm.triaPublic(true)
        assertTrue(vm.estat.value.potCrearPerfil)
    }

    @Test
    fun `si el nom ja és agafat, ho diu`() = runTest {
        compte.error = ErrorCompte.NOM_USUARI_AGAFAT
        val vm = CompteViewModel(compte)
        vm.canviaNomUsuari("anna")
        vm.triaTipus(TipusPerfil.EXPLORADOR)
        vm.triaPublic(true)
        vm.creaPerfil()
        assertEquals(ErrorCompte.NOM_USUARI_AGAFAT, vm.estat.value.error)
    }
}
