package com.speedreader.trainer.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.speedreader.trainer.BuildConfig
import com.speedreader.trainer.data.remote.OpenAIService
import com.speedreader.trainer.data.repository.AuthRepository
import com.speedreader.trainer.data.repository.DocumentRepository
import com.speedreader.trainer.data.repository.ReadingSessionRepository
import com.speedreader.trainer.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenAIService(okHttpClient: OkHttpClient): OpenAIService {
        return Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository = AuthRepository(firebaseAuth, firestore)

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): UserRepository = UserRepository(firestore, firebaseAuth)

    @Provides
    @Singleton
    fun provideDocumentRepository(
        @ApplicationContext context: Context,
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): DocumentRepository = DocumentRepository(context, firestore, firebaseAuth)

    @Provides
    @Singleton
    fun provideReadingSessionRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        openAIService: OpenAIService
    ): ReadingSessionRepository = ReadingSessionRepository(firestore, firebaseAuth, openAIService)
}

