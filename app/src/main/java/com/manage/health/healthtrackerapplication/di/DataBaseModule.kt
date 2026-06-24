package com.manage.health.healthtrackerapplication.di

import android.content.Context
import com.example.fitnessapp.GoogleFitService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.manage.health.healthtrackerapplication.data.dao.HealthDataDao
import com.manage.health.healthtrackerapplication.data.dao.SleepLogDao
import com.manage.health.healthtrackerapplication.data.dao.StepLogDao
import com.manage.health.healthtrackerapplication.data.dao.UserGoalsDao
import com.manage.health.healthtrackerapplication.data.dao.WaterLogDao
import com.manage.health.healthtrackerapplication.data.database.HealthDataBase
import com.manage.health.healthtrackerapplication.data.repository.HealthRepository
import com.manage.health.healthtrackerapplication.data.service.ExportServices
import com.manage.health.healthtrackerapplication.data.service.FirebaseDataService
import com.manage.health.healthtrackerapplication.data.service.HealthTipsServices
import com.manage.health.healthtrackerapplication.data.service.NotificationScheduler
import com.manage.health.healthtrackerapplication.data.service.NotificationService
import com.manage.health.healthtrackerapplication.data.service.WearableDeviceService
import com.manage.health.healthtrackerapplication.ui.viewModel.ThemeViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataBaseModule {

    @Provides
    @Singleton
    fun provideHealthDataBase(@ApplicationContext context: Context): HealthDataBase {
        return HealthDataBase.getDataBase(context)
    }


    fun provideUserSpecificDataBase(context: Context, userId: String): HealthDataBase{
        return HealthDataBase.getDataBase(context,userId)
    }

    @Provides
    fun provideHealthDataDao(dataBase: HealthDataBase): HealthDataDao{
        return dataBase.healthDataDao()
    }

    @Provides
    fun provideWaterLogDao(dataBase: HealthDataBase): WaterLogDao{
        return dataBase.waterLogDao()
    }

    @Provides
    fun provideStepLogDao(dataBase: HealthDataBase): StepLogDao{
        return dataBase.stepLogDao()
    }

    @Provides
    fun provideSleepLogDao(dataBase: HealthDataBase): SleepLogDao{
        return dataBase.sleepLogDao()
    }

    @Provides
    fun provideUserGoalsDao(dataBase: HealthDataBase): UserGoalsDao{
        return dataBase.userGoalsDao()
    }


    @Provides
    @Singleton
    fun provideFireBaseDataService(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): FirebaseDataService{
        return FirebaseDataService(firebaseAuth,firestore)
    }

    @Provides
    @Singleton
    fun provideHealthRepository(
        healthDataDao: HealthDataDao,
        waterLogDao: WaterLogDao,
        stepLogDao: StepLogDao,
        sleepLogDao: SleepLogDao,
        userGoalsDao: UserGoalsDao,
        firebaseDataService: FirebaseDataService,
        firebaseAuth: FirebaseAuth
    ): HealthRepository{
        return HealthRepository(
            healthDataDao,
            waterLogDao,
            stepLogDao,
            sleepLogDao,
            userGoalsDao,
            firebaseDataService,
            firebaseAuth
        )
    }
    @Provides
    @Singleton
    fun provideHealthTipsService(): HealthTipsServices{
        return HealthTipsServices()
    }


    @Provides
    @Singleton
    fun provideExportService(@ApplicationContext context: Context): ExportServices{
        return ExportServices(context = context)
    }

    @Provides
    @Singleton
    fun provideNotificationService(@ApplicationContext context: Context): NotificationService{
        return NotificationService(context = context)
    }


    @Provides
    @Singleton
    fun provideExportScheduler(@ApplicationContext context: Context): NotificationScheduler{
        return NotificationScheduler(context = context)
    }

    @Provides
    @Singleton
    fun provideGoogleFitService(@ApplicationContext context: Context): GoogleFitService{
        return GoogleFitService(context = context)
    }

    @Provides
    @Singleton
    fun provideWearableDeviceService(@ApplicationContext context: Context): WearableDeviceService{
        return WearableDeviceService(context = context)
    }


    @Provides
    @Singleton
    fun provideThemeViewModel(@ApplicationContext context: Context): ThemeViewModel {
        return ThemeViewModel()
    }



}