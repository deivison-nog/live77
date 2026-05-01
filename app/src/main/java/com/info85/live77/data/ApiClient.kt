package com.info85.live77.data

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
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

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Authenticates a user against the backend database.
     *
     * POSTs a JSON body with `login_code` and `password_code` to `$API_URL?action=login`.
     * The `action` parameter must be in the query string because the server reads it from
     * `$_GET`/`$_POST`, not from the JSON body.
     *
     * @return `true` if the server confirms valid credentials, `false` otherwise.
     * @throws IOException on network or HTTP-level errors.
     */
    @Throws(IOException::class)
    fun login(login: String, senha: String): Boolean {
        val json = JSONObject().apply {
            put("login_code", login)
            put("password_code", senha)
        }
        val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)

        val request = Request.Builder()
            .url("$API_URL?action=login")
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
