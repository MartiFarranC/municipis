package cat.descobreix.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import cat.descobreix.BuildConfig
import cat.descobreix.data.assets.DadesJoc
import cat.descobreix.data.assets.FontDadesJoc
import cat.descobreix.data.compte.ServeiCompte
import cat.descobreix.data.compte.ServeiCompteSupabase
import cat.descobreix.data.db.BaseDades
import cat.descobreix.data.repositori.FotosRepositori
import cat.descobreix.data.repositori.FotosRepositoriRoom
import cat.descobreix.data.repositori.MissionsPropiesRepositori
import cat.descobreix.data.repositori.MissionsPropiesRepositoriRoom
import cat.descobreix.data.repositori.PreferenciesRepositori
import cat.descobreix.data.repositori.PreferenciesRepositoriDataStore
import cat.descobreix.data.repositori.ProgresRepositori
import cat.descobreix.data.repositori.ProgresRepositoriRoom
import cat.descobreix.data.repositori.Rellotge
import cat.descobreix.data.repositori.SegellsRepositori
import cat.descobreix.data.repositori.SegellsRepositoriRoom
import cat.descobreix.data.ubicacio.ServeiUbicacio
import cat.descobreix.data.ubicacio.ServeiUbicacioFused
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ModulDades {
    @Provides
    @Singleton
    fun baseDades(@ApplicationContext context: Context): BaseDades =
        Room.databaseBuilder(context, BaseDades::class.java, BaseDades.NOM).addMigrations(BaseDades.MIGRACIO_1_2).build()

    @Provides
    @Singleton
    fun preferencies(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("preferencies") }

    @Provides
    fun rellotge(): Rellotge = Rellotge { System.currentTimeMillis() }

    @Provides
    @Singleton
    fun supabase(): SupabaseClient = createSupabaseClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY) {
        install(Auth) {
            flowType = FlowType.PKCE
            scheme = ServeiCompteSupabase.ESQUEMA
            host = ServeiCompteSupabase.AMFITRIO
        }
        // Totes les dades de l'app són a l'esquema descobreix (docs/requisits.md, secció 9.0).
        install(Postgrest) { defaultSchema = "descobreix" }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ModulRepositoris {
    @Binds
    @Singleton
    abstract fun progres(r: ProgresRepositoriRoom): ProgresRepositori

    @Binds
    @Singleton
    abstract fun missionsPropies(r: MissionsPropiesRepositoriRoom): MissionsPropiesRepositori

    @Binds
    @Singleton
    abstract fun fotos(r: FotosRepositoriRoom): FotosRepositori

    @Binds
    @Singleton
    abstract fun preferencies(r: PreferenciesRepositoriDataStore): PreferenciesRepositori

    @Binds
    @Singleton
    abstract fun segells(r: SegellsRepositoriRoom): SegellsRepositori

    @Binds
    abstract fun dades(d: DadesJoc): FontDadesJoc

    @Binds
    @Singleton
    abstract fun ubicacio(s: ServeiUbicacioFused): ServeiUbicacio
}

/** A part, perquè els tests de UI el puguin substituir per un compte fals. */
@Module
@InstallIn(SingletonComponent::class)
abstract class ModulCompte {
    @Binds
    @Singleton
    abstract fun compte(s: ServeiCompteSupabase): ServeiCompte
}
