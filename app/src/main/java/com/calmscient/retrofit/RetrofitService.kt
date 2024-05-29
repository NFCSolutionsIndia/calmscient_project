package com.calmscient.retrofit

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.calmscient.ApiService
import com.calmscient.R
import com.calmscient.di.remote.NetworkService
import com.calmscient.di.remote.Networking
import com.calmscient.repository.LoginRepository
import com.calmscient.utils.network.NetworkHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    // Define base URLs for different API endpoints
    private const val BASE_URL = "https://calmscient.centralindia.cloudapp.azure.com:8090/"
   // private const val BASE_URL_MENU = "http://20.197.5.97:8083/"

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(
            context.getString(R.string.app_name), Context.MODE_PRIVATE
        )

    @Provides
    @Singleton
    fun provideNetworkService(@ApplicationContext context: Context): NetworkService {
        return Networking.createAPI(BASE_URL, context.cacheDir, 10 * 1024 * 1024)
    }

    @Singleton
    @Provides
    fun provideNetworkHelper(@ApplicationContext context: Context): NetworkHelper =
        NetworkHelper(context)

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("OkHttp", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

}
