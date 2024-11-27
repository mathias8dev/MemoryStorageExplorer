package com.mathias8dev.memoriesstoragexplorer.injection

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single


@Module
class MiscModule {


    @Single
    fun provideGson(): Gson {
        return GsonBuilder()
            .create()
    }
}