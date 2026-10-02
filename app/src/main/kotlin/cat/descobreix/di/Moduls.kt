package cat.descobreix.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import cat.descobreix.data.assets.DadesJoc
import cat.descobreix.data.assets.FontDadesJoc
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
import cat.descobreix.data.ubicacio.ServeiUbicacio
import cat.descobreix.data.ubicacio.ServeiUbicacioFused
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ModulDades {
    @Provides
    @Singleton
    fun baseDades(@ApplicationContext context: Context): BaseDades =
        Room.databaseBuilder(context, BaseDades::class.java, BaseDades.NOM).build()

    @Provides
    @Singleton
    fun preferencies(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("preferencies") }

    @Provides
    fun rellotge(): Rellotge = Rellotge { System.currentTimeMillis() }
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
    abstract fun dades(d: DadesJoc): FontDadesJoc

    @Binds
    @Singleton
    abstract fun ubicacio(s: ServeiUbicacioFused): ServeiUbicacio
}
