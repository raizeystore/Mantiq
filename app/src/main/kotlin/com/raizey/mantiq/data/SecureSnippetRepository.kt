package com.raizey.mantiq.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.raizey.mantiq.diagnostics.CrashStore
import java.nio.ByteBuffer
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import org.json.JSONArray
import org.json.JSONObject

class SecureSnippetRepository(private val context: Context) {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    @Synchronized
    fun list(): List<UserSnippet> = runCatching {
        val encrypted = preferences.getString(KEY_PAYLOAD, null) ?: return emptyList()
        decode(JSONArray(decrypt(encrypted)))
    }.onFailure {
        CrashStore.record(context, "SecureSnippetRepository.list", it)
    }.getOrDefault(emptyList())

    @Synchronized
    fun upsert(snippet: UserSnippet) {
        validate(snippet)
        val current = list().toMutableList()
        val duplicate = current.any { it.id != snippet.id && it.trigger == snippet.trigger }
        require(!duplicate) { "Trigger already exists" }
        val index = current.indexOfFirst { it.id == snippet.id }
        if (index >= 0) current[index] = snippet else current.add(snippet)
        persist(current)
    }

    @Synchronized
    fun delete(id: String) {
        persist(list().filterNot { it.id == id })
    }

    fun create(
        trigger: String,
        template: String,
        enabled: Boolean,
        allowedPackages: Set<String>,
    ) = UserSnippet(
        id = UUID.randomUUID().toString(),
        trigger = trigger.trim(),
        template = template.trim(),
        enabled = enabled,
        allowedPackages = allowedPackages,
        createdAt = System.currentTimeMillis(),
    )

    private fun validate(snippet: UserSnippet) {
        require(snippet.trigger.isNotBlank()) { "Trigger cannot be empty" }
        require(snippet.trigger.length <= 48) { "Trigger is too long" }
        require(snippet.trigger.none(Char::isWhitespace)) { "Trigger cannot contain spaces" }
        require(snippet.template.isNotBlank()) { "Replacement cannot be empty" }
        require(snippet.template.length <= 4_000) { "Replacement is too long" }
    }

    private fun persist(snippets: List<UserSnippet>) {
        val payload = JSONArray().apply {
            snippets.forEach { snippet ->
                put(JSONObject().apply {
                    put("id", snippet.id)
                    put("trigger", snippet.trigger)
                    put("template", snippet.template)
                    put("enabled", snippet.enabled)
                    put("createdAt", snippet.createdAt)
                    put("allowedPackages", JSONArray(snippet.allowedPackages.toList()))
                })
            }
        }.toString()
        preferences.edit().putString(KEY_PAYLOAD, encrypt(payload)).apply()
    }

    private fun decode(array: JSONArray): List<UserSnippet> = buildList {
        for (index in 0 until array.length()) {
            val item = array.getJSONObject(index)
            val packages = item.optJSONArray("allowedPackages") ?: JSONArray()
            add(
                UserSnippet(
                    id = item.getString("id"),
                    trigger = item.getString("trigger"),
                    template = item.getString("template"),
                    enabled = item.optBoolean("enabled", true),
                    createdAt = item.optLong("createdAt", 0L),
                    allowedPackages = buildSet {
                        for (packageIndex in 0 until packages.length()) add(packages.getString(packageIndex))
                    },
                ),
            )
        }
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val ciphertext = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        val buffer = ByteBuffer.allocate(1 + cipher.iv.size + ciphertext.size)
            .put(cipher.iv.size.toByte())
            .put(cipher.iv)
            .put(ciphertext)
            .array()
        return Base64.encodeToString(buffer, Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String {
        val buffer = ByteBuffer.wrap(Base64.decode(value, Base64.NO_WRAP))
        val iv = ByteArray(buffer.get().toInt() and 0xFF)
        buffer.get(iv)
        val ciphertext = ByteArray(buffer.remaining())
        buffer.get(ciphertext)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, iv))
        return cipher.doFinal(ciphertext).toString(Charsets.UTF_8)
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build(),
            )
            generateKey()
        }
    }

    private companion object {
        const val FILE_NAME = "mantiq_secure_snippets"
        const val KEY_PAYLOAD = "encrypted_payload"
        const val KEY_ALIAS = "mantiq_snippets_aes_v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
