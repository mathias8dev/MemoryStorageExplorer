package com.mathias8dev.memoriesstoragexplorer.domain.useCases

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import org.koin.core.annotation.Factory


@Factory
class LoadStringResourceUseCase(
    private val context: Context
) {
    operator fun invoke(@StringRes resId: Int): String = context.getString(resId)
    operator fun invoke(@PluralsRes resId: Int, count: Int): String = context.resources.getQuantityString(resId, count)
}