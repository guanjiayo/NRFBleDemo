package zs.xmx.nrfbledemo.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import zs.xmx.nrfbledemo.roller.Roller
import zs.xmx.nrfbledemo.roller.RollerBleManager

@Suppress("unused")
@Module
@InstallIn(ViewModelComponent::class)
abstract class RollerModule {

    companion object {

        @Provides
        @ViewModelScoped
        fun provideRollerManager(
            @ApplicationContext context: Context,
        ) = RollerBleManager(context)


    }

    @Binds
    abstract fun bindRoller(
        rollerBleManager: RollerBleManager
    ): Roller

}