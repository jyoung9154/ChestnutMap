package com.bamtori.chestnutmap.data.network


import android.util.Log
import com.bamtori.chestnutmap.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import okhttp3.OkHttpClient
import retrofit2.converter.scalars.ScalarsConverterFactory


object NetworkModule {
    // 구글 플레이스 전용 Retrofit 인스턴스
    val googleRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .client(OkHttpClient.Builder().build()) // 필요한 인터셉터 추가 가능
            // ScalarsConverterFactory : String형 응답을 받음
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }

    val googleApiService: ApiService by lazy {
        googleRetrofit.create(ApiService::class.java)
    }

    // val firebaseApi: FirebaseApiService = ...
}
