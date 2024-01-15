package zs.xmx.nrfbledemo.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import zs.xmx.nrfbledemo.roller.Roller
import javax.inject.Inject

class RollerRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val roller: Roller,
) : Roller by roller {

}