package com.example.cinechord.di

import com.example.cinechord.data.repository.AuthRepositoryImpl
import com.example.cinechord.data.repository.WatchRoomRepositoryImpl
import com.example.cinechord.domain.repository.AuthRepository
import com.example.cinechord.domain.repository.WatchRoomRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return Firebase.auth
    }
    
    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return Firebase.database
    }
    
    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        database: FirebaseDatabase
    ): AuthRepository {
        return AuthRepositoryImpl(auth, database)
    }
    
    @Provides
    @Singleton
    fun provideWatchRoomRepository(
        database: FirebaseDatabase
    ): WatchRoomRepository {
        return WatchRoomRepositoryImpl(database)
    }
} 