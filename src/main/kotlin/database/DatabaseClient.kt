package me.airdead.zutils.database

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.CompletableFuture

class DatabaseClient(private val serverAddress: String, private val port: Int) {
    private val client = OkHttpClient()
    private val gson = Gson()

    // Asynchronous data addition
    fun addDataAsync(tableName: String, data: Map<String, Any>): CompletableFuture<Result> {
        val url = "http://$serverAddress:$port/api/$tableName"
        val json = gson.toJson(data)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toRequestBody(mediaType)
        val request = Request.Builder().url(url).post(body).build()

        return getFuture(request)
    }


    // Asynchronous data update
    fun updateDataAsync(tableName: String, id: String, data: Map<String, Any>): CompletableFuture<Result> {
        val url = "http://$serverAddress:$port/api/$tableName/$id"
        val json = gson.toJson(data)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toRequestBody(mediaType)
        val request = Request.Builder().url(url).put(body).build()

        return getFuture(request)
    }


    // Asynchronous retrieval of all data
    fun getAllDataAsync(tableName: String): CompletableFuture<Map<String, Map<String, Any>>> {
        val url = "http://$serverAddress:$port/api/$tableName"
        val request = Request.Builder().url(url).get().build()

        val future = CompletableFuture<Map<String, Map<String, Any>>>()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                future.completeExceptionally(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val typeRef = object : TypeToken<Map<String, Map<String, Any>>>() {}.type
                    future.complete(gson.fromJson(response.body?.string(), typeRef))
                } else {
                    future.complete(emptyMap())
                }
            }
        })
        return future
    }


    // Asynchronous data deletion
    fun deleteDataAsync(tableName: String, id: String): CompletableFuture<Result> {
        val url = "http://$serverAddress:$port/api/$tableName/$id"
        val request = Request.Builder().url(url).delete().build()

        return getFuture(request)
    }


    // Asynchronous table deletion
    fun deleteTableAsync(tableName: String): CompletableFuture<Result> {
        val url = "http://$serverAddress:$port/api/$tableName"
        val request = Request.Builder().url(url).delete().build()

        return getFuture(request)
    }


    fun getFuture(request: Request): CompletableFuture<Result> {
        val future = CompletableFuture<Result>()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                future.completeExceptionally(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBodyString = response.body?.string() ?: ""

                    val resultCode = responseBodyString.trim().toIntOrNull() ?: Result.UNKNOWN.code
                    future.complete(Result.getByCode(resultCode))
                } catch (e: Exception) {
                    future.completeExceptionally(e)
                }
            }
        })

        return future
    }


}
