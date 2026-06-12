package com.manage.health.healthtrackerapplication.di

import com.google.api.Context
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DataBaseModule {

//    @Provides
//    @Singleton
//    fun provideHealthDataBase(@ApplicationContext context: Context): FirebaseAuth{
//        return Health.getInstance()
//    }
//


}