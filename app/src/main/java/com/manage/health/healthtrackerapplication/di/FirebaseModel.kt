package com.manage.health.healthtrackerapplication.di

import androidx.room.PrimaryKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton




@Module
@InstallIn(SingletonComponent::class)
object FirebaseModel {
    @Provides
    @Singleton
    fun provideFireBaseAuth(): FirebaseAuth{
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFireStore(): FirebaseFirestore{
        return FirebaseFirestore.getInstance()
    }

}