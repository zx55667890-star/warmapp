package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.data.FirebasePaths
import com.example.myapplication.data.repository.DataMigrator
import com.example.myapplication.data.repository.MessageRepositoryFactory
import com.example.myapplication.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {
    single {
        FirebaseDatabase.getInstance().apply {
            setPersistenceEnabled(true)
            getReference(FirebasePaths.QUESTIONS).keepSynced(true)
            getReference("experts").keepSynced(true)
            getReference("experiences").keepSynced(true)
            getReference(FirebasePaths.USERS).keepSynced(true)
        }
    }
    single { FirebaseStorage.getInstance() }
    single { FirebaseAuth.getInstance() }
    single {
        androidContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }
    single { UserRepository(get()) }
    single { DataMigrator(get(), get()) }
    single { MessageRepositoryFactory(get()) }
}
