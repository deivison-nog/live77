package com.info85.live77.data

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * HTTP client for the Live 77 backend API.
 *
 * All requests go to [API_URL]. The server is expected to return a JSON object
 * with at least a boolean "success" field:
 *   {"success": true}   → operation succeeded
 *   {"success": false}  → operation failed (wrong credentials, etc.)
 */
object ApiClient {

    const val API_URL = "http://192.168.1.14/live77/api.php"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Authenticates a user against the backend database.
     *
     * POSTs `action=login`, `login` and `senha` (password) as form fields.
     *
     * @return `true` if the server confirms valid credentials, `false` otherwise.
     * @throws IOException on network or HTTP-level errors.
     */
    @Throws(IOException::class)
    fun login(login: String, senha: String): Boolean {
        val body = FormBody.Builder()
            .add("action", "login")
            .add("login", login)
            .add("senha", senha)
            .build()

        val request = Request.Builder()
            .url(API_URL)
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("HTTP ${response.code}: ${response.message}")
        }

        val bodyStr = response.body?.string()
            ?: throw IOException("Resposta vazia do servidor")

        return JSONObject(bodyStr).optBoolean("success", false)
    }
}
