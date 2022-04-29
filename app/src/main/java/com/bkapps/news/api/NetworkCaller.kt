package com.bkapps.news.api

import com.bkapps.news.utils.NetworkResult
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

abstract class NetworkCaller {
    protected suspend fun <T : Any> getResult(call: suspend () -> Response<T>): NetworkResult<T> {
        try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null)
                    return NetworkResult.Success(body)
            }
            //Falha no status 2xx
            return NetworkResult.Error(HttpException(response))
        } catch (e: IOException) {
            // IOException erro de rede.
            return NetworkResult.Error(e)
        }
    }
}