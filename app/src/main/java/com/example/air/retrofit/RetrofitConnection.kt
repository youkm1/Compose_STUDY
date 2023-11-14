package com.example.air.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
//컨버터팩토리는 서버에서온json응답을 코틀린 데이터 클래스객체로 바꿈

class RetrofitConnection {

    companion object {
        private const val BASE_URL = "https://api.airvisual.com/v2/"
        private var INSTANCE: Retrofit? = null

        fun getInstance(): Retrofit {
            if (INSTANCE == null) {
                INSTANCE = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return INSTANCE!!
        }
    }
}