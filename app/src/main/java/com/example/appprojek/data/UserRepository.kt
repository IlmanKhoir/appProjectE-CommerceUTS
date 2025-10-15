package com.example.appprojek.data

import android.util.Log
import com.example.appprojek.network.ApiClient
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.IOException
import java.net.URLEncoder

class UserRepository(private val gson: Gson = Gson()) {

        data class LoginResult(
                val success: Boolean,
                val user_id: Int?,
                val email: String?,
                val name: String?,
                val phone: String?,
                val address: String?,
                val error: String?
        )

        private fun parseResult(rawBody: String, fallbackEmail: String?): LoginResult {
                return try {
                        val obj: JsonObject = JsonParser.parseString(rawBody).asJsonObject
                        val success = obj.get("success")?.asBoolean == true
                        val userEmail =
                                if (obj.has("email") && !obj.get("email").isJsonNull)
                                        obj.get("email").asString
                                else fallbackEmail
                        val userId =
                                if (obj.has("user_id") && !obj.get("user_id").isJsonNull)
                                        obj.get("user_id").asInt
                                else null
                        val name =
                                if (obj.has("name") && !obj.get("name").isJsonNull)
                                        obj.get("name").asString
                                else null
                        val phone =
                                if (obj.has("phone") && !obj.get("phone").isJsonNull)
                                        obj.get("phone").asString
                                else null
                        val address =
                                if (obj.has("address") && !obj.get("address").isJsonNull)
                                        obj.get("address").asString
                                else null
                        val error =
                                if (obj.has("error") && !obj.get("error").isJsonNull)
                                        obj.get("error").asString
                                else null
                        LoginResult(success, userId, userEmail, name, phone, address, error)
                } catch (e: Exception) {
                        Log.e("UserRepository", "Invalid JSON from server: $rawBody", e)
                        LoginResult(
                                false,
                                null,
                                fallbackEmail,
                                null,
                                null,
                                null,
                                "Invalid server response"
                        )
                }
        }

        @Throws(IOException::class)
        fun register(
                email: String,
                password: String,
                phone: String? = null,
                address: String? = null,
                name: String? = null
        ): LoginResult {
                val req =
                        ApiClient.buildPostForm(
                                "users.php?action=register",
                                mapOf(
                                        "email" to email,
                                        "password" to password,
                                        // Nama / fullname (beberapa backend mensyaratkan ini untuk
                                        // menyimpan field lain)
                                        "name" to (name ?: ""),
                                        "nama" to (name ?: ""),
                                        "full_name" to (name ?: ""),
                                        "username" to (name ?: ""),
                                        // Field utama yang app gunakan
                                        "phone" to (phone ?: ""),
                                        "address" to (address ?: ""),
                                        // Kompat: beberapa backend memakai nama lain
                                        "nohp" to (phone ?: ""),
                                        "telepon" to (phone ?: ""),
                                        "phone_number" to (phone ?: ""),
                                        "no_telp" to (phone ?: ""),
                                        "telp" to (phone ?: ""),
                                        "hp" to (phone ?: ""),
                                        "alamat" to (address ?: ""),
                                        "alamat_lengkap" to (address ?: "")
                                )
                        )
                val result =
                        ApiClient.httpClient.newCall(req).execute().use { resp ->
                                val body = resp.body?.string().orEmpty()
                                if (!resp.isSuccessful) {
                                        Log.e(
                                                "UserRepository",
                                                "Register failed HTTP ${resp.code}: $body"
                                        )
                                        return@use LoginResult(
                                                false,
                                                null,
                                                email,
                                                null, // name
                                                null,
                                                null,
                                                "HTTP ${resp.code}: $body"
                                        )
                                }
                                parseResult(body, email)
                        }
                return result
        }

        @Throws(IOException::class)
        fun login(email: String, password: String): LoginResult {
                val req =
                        ApiClient.buildPostForm(
                                "users.php?action=login",
                                mapOf(
                                        // Nama field utama sesuai app
                                        "email" to email,
                                        "password" to password,
                                        // Kompat: beberapa backend pakai nama berbeda
                                        "username" to email,
                                        "pass" to password
                                )
                        )
                val result =
                        ApiClient.httpClient.newCall(req).execute().use { resp ->
                                val body = resp.body?.string().orEmpty()
                                if (!resp.isSuccessful) {
                                        Log.e(
                                                "UserRepository",
                                                "Login failed HTTP ${resp.code}: $body"
                                        )
                                        return@use LoginResult(
                                                false,
                                                null,
                                                email,
                                                null, // name
                                                null,
                                                null,
                                                "HTTP ${resp.code}: $body"
                                        )
                                }
                                parseResult(body, email)
                        }
                return result
        }

        @Throws(IOException::class)
        fun getProfileByEmail(email: String): LoginResult {
                val encoded = URLEncoder.encode(email, "UTF-8")
                val req = ApiClient.buildGet("users.php?action=me&email=$encoded")
                val result =
                        ApiClient.httpClient.newCall(req).execute().use { resp ->
                                val body = resp.body?.string().orEmpty()
                                if (!resp.isSuccessful) {
                                        Log.e(
                                                "UserRepository",
                                                "Profile fetch failed HTTP ${resp.code}: $body"
                                        )
                                        return@use LoginResult(
                                                false,
                                                null,
                                                email,
                                                null, // name
                                                null,
                                                null,
                                                "HTTP ${resp.code}: $body"
                                        )
                                }
                                parseResult(body, email)
                        }
                return result
        }

        @Throws(IOException::class)
        fun updateProfile(
                userId: Int?,
                email: String?,
                name: String?,
                phone: String?,
                address: String?,
                latitude: Double? = null,
                longitude: Double? = null
        ): Boolean {
                val form = mutableMapOf<String, String>()
                userId?.let { form["user_id"] = it.toString() }
                email?.let { form["email"] = it }
                name?.let {
                        form["name"] = it
                        form["nama"] = it
                        form["full_name"] = it
                }
                phone?.let {
                        form["phone"] = it
                        form["nohp"] = it
                        form["telepon"] = it
                        form["phone_number"] = it
                }
                address?.let {
                        form["address"] = it
                        form["alamat"] = it
                        form["alamat_lengkap"] = it
                }
                latitude?.let { form["latitude"] = it.toString() }
                longitude?.let { form["longitude"] = it.toString() }

                val req = ApiClient.buildPostForm("users.php?action=update", form)
                ApiClient.httpClient.newCall(req).execute().use { resp ->
                        val body = resp.body?.string().orEmpty()
                        if (!resp.isSuccessful) {
                                Log.e("UserRepository", "Update failed HTTP ${resp.code}: $body")
                                return false
                        }
                        try {
                            val obj = JsonParser.parseString(body).asJsonObject
                            return obj.get("success")?.asBoolean == true
                        } catch (e: Exception) {
                            Log.e("UserRepository", "Malformed JSON response: $body", e)
                            return false
                        }
                }
        }
}
