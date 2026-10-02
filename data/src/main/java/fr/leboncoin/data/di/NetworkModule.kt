package fr.leboncoin.data.di

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.leboncoin.data.BuildConfig
import fr.leboncoin.data.network.api.AlbumApiService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okio.Path.Companion.toPath
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(AlbumApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideAlbumApiService(retrofit: Retrofit): AlbumApiService {
        return retrofit.create()
    }

    // Share one ImageLoader and its caches across the entire app.
    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
    ): ImageLoader = ImageLoader.Builder(context)
        .memoryCache {
            // Keep decoded images in memory for fast reuse while scrolling.
            MemoryCache.Builder()
                .maxSizePercent(context, 0.20) // Allow up to 20% of available app memory.
                .build()
        }
        .diskCache {
            // Store downloaded images on disk so they can survive app restarts.
            // Android may clear this directory when storage is low.
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("image_cache").absolutePath.toPath())
                .maxSizeBytes(100L * 1024 * 1024) // Limit the cache to 100 MB.
                .build()
        }
        .build()
}