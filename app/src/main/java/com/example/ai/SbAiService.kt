package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class SbAiPersona(val id: String, val displayName: String, val description: String) {
    EDITOR("editor", "Editor Novel", "Alami, kreatif, peka alur, emosi & dialog tanpa basa-basi"),
    LUCU("lucu", "Santai & Lucu", "Spontan, humoris, cerdas dan menghibur"),
    PROFESIONAL("profesional", "Profesional", "Terstruktur, sopan, efisien dan to the point")
}

data class SbAiContext(
    val bookTitle: String? = null,
    val chapterTitle: String? = null,
    val selectedText: String? = null,
    val chapterSnippet: String? = null,
    val totalWords: Int = 0,
    val activeNotesSnippet: String? = null
)

class SbAiService(
    private val customKey1: String = "",
    private val customKey2: String = ""
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val targetModel = "gemini-3.5-flash-lite"

    /**
     * Memperoleh daftar kunci API yang tersedia secara berurutan.
     * Mengambil dari Secrets backend (BuildConfig) atau input konfigurasi lokal.
     */
    private fun getApiKeys(): List<String> {
        val keys = mutableListOf<String>()

        // Kunci kustom dari preferensi (jika ada)
        if (customKey1.isNotBlank()) keys.add(customKey1.trim())
        if (customKey2.isNotBlank()) keys.add(customKey2.trim())

        // Kunci dari BuildConfig (Secrets backend AI Studio)
        try {
            val key1Field = BuildConfig::class.java.getField("GEMINI_API_KEY_1")
            val val1 = key1Field.get(null) as? String
            if (!val1.isNullOrBlank() && val1 != "MY_GEMINI_API_KEY" && val1 != "MY_GEMINI_API_KEY_1" && !keys.contains(val1)) {
                keys.add(val1.trim())
            }
        } catch (_: Exception) {}

        try {
            val key2Field = BuildConfig::class.java.getField("GEMINI_API_KEY_2")
            val val2 = key2Field.get(null) as? String
            if (!val2.isNullOrBlank() && val2 != "MY_GEMINI_API_KEY" && val2 != "MY_GEMINI_API_KEY_2" && !keys.contains(val2)) {
                keys.add(val2.trim())
            }
        } catch (_: Exception) {}

        return keys
    }

    /**
     * Membangun system instruction sesuai gaya persona dan aturan SB_Media.
     */
    private fun buildSystemInstruction(persona: SbAiPersona): String {
        val personaRule = when (persona) {
            SbAiPersona.EDITOR -> """
                Gaya bicara: EDITOR NOVEL.
                Kamu adalah editor senior berpengalaman di SB_Media Production.
                Kamu memahami struktur plot, tensi dramatik, perkembangan karakter, 'show don't tell', ritme kalimat, dan dialog yang mengalir alami.
                Jangan gunakan gaya bicara kaku AI, jangan banyak basa-basi, jangan memuji berlebihan.
                Ketika diminta menyunting atau memperbaiki naskah, langsung sajikan hasil tulisan yang matang, berbobot emosional, dan bernyawa.
            """.trimIndent()

            SbAiPersona.LUCU -> """
                Gaya bicara: SANTAI & LUCU.
                Kamu asisten penulisan yang santai, spontan, cerdas, bersahabat, dan punya selera humor segar khas ruang kreatif SB_Media.
                Bantu penulis mengatasi kebuntuan dengan analogi seru dan candaan cerdas yang membakar semangat menulis.
            """.trimIndent()

            SbAiPersona.PROFESIONAL -> """
                Gaya bicara: PROFESIONAL.
                Kamu konsultan naskah profesional SB_Media Production.
                Sampaikan analisis secara jelas, runut, ringkas, sopan, dan efisien tanpa bertele-tele.
            """.trimIndent()
        }

        return """
            Identitas Utama:
            Jika ditanya siapa kamu atau model apa: Jawab selalu dengan jujur dan tegas: "Saya SB_AI, asisten AI dari SB_Media Production."
            Dilarang menyebut model dasar atau provider lain di luar SB_Media Production.

            Aturan Gaya & Format Penting:
            - JANGAN gunakan awalan klise AI seperti "Tentu, berikut adalah...", "Tentu saja!", "Halo! Tentu!", atau pengantar basa-basi sejenis.
            - JANGAN menghasilkan judul dengan tanda pagar berlebihan (###). Gunakan pemisahan paragraf yang bersih dan alami.
            - Fokus pada kualitas sastra dan kenyamanan membaca naskah.

            $personaRule
        """.trimIndent()
    }

    /**
     * Memanggil Gemini API dengan model gemini-3.5-flash-lite, fallback antar kedua key.
     */
    suspend fun generateResponse(
        prompt: String,
        persona: SbAiPersona,
        context: SbAiContext?,
        chatHistory: List<Pair<String, String>> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val keys = getApiKeys()
        if (keys.isEmpty()) {
            return@withContext Result.failure(
                Exception("Kunci GEMINI_API_KEY belum dikonfigurasi di Secrets backend. Silakan periksa pengaturan proyek.")
            )
        }

        var lastError: Exception? = null

        for (apiKey in keys) {
            try {
                val responseText = executeGeminiRequest(apiKey, prompt, persona, context, chatHistory)
                if (responseText.isNotBlank()) {
                    return@withContext Result.success(cleanResponse(responseText))
                }
            } catch (e: Exception) {
                lastError = e
                // Lanjut coba kunci berikutnya jika ada
            }
        }

        Result.failure(lastError ?: Exception("Gagal mendapatkan respons dari SB_AI."))
    }

    private fun executeGeminiRequest(
        apiKey: String,
        prompt: String,
        persona: SbAiPersona,
        context: SbAiContext?,
        chatHistory: List<Pair<String, String>>
    ): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent"

        val rootJson = JSONObject()

        // 1. System Instruction
        val sysInstructionObj = JSONObject()
        val sysParts = JSONArray().put(JSONObject().put("text", buildSystemInstruction(persona)))
        sysInstructionObj.put("parts", sysParts)
        rootJson.put("systemInstruction", sysInstructionObj)

        // 2. Contents Array
        val contentsArray = JSONArray()

        // Kirim riwayat percakapan singkat untuk kontinuitas (maksimal 4 giliran terakhir untuk hemat token)
        val recentHistory = chatHistory.takeLast(4)
        for ((role, text) in recentHistory) {
            val historyRole = if (role.equals("user", ignoreCase = true)) "user" else "model"
            contentsArray.put(
                JSONObject()
                    .put("role", "user")
                    .put("parts", JSONArray().put(JSONObject().put("text", "[$historyRole]: $text")))
            )
        }

        // Format prompt bersama konteks editor aktif
        val userPromptBuilder = StringBuilder()

        if (context != null) {
            userPromptBuilder.append("[KONTEKS INTERNAL DOKUMEN AKTIF]\n")
            context.bookTitle?.let { userPromptBuilder.append("Judul Buku: $it\n") }
            context.chapterTitle?.let { userPromptBuilder.append("Bab Aktif: $it\n") }
            if (context.totalWords > 0) userPromptBuilder.append("Total Kata Saat Ini: ${context.totalWords}\n")

            if (!context.selectedText.isNullOrBlank()) {
                userPromptBuilder.append("\n[TEKS YANG DIPILIH PENULIS]:\n\"\"\"\n${context.selectedText.take(2000)}\n\"\"\"\n")
            } else if (!context.chapterSnippet.isNullOrBlank()) {
                userPromptBuilder.append("\n[CUPLIKAN BAB AKTIF (Penggalan Terakhir)]:\n\"\"\"\n${context.chapterSnippet.takeLast(2500)}\n\"\"\"\n")
            }

            if (!context.activeNotesSnippet.isNullOrBlank()) {
                userPromptBuilder.append("\n[CATATAN TERKAIT]: ${context.activeNotesSnippet.take(1000)}\n")
            }
            userPromptBuilder.append("\n[PERMINTAAN PENULIS]:\n")
        }

        userPromptBuilder.append(prompt)

        val currentTurn = JSONObject()
            .put("role", "user")
            .put("parts", JSONArray().put(JSONObject().put("text", userPromptBuilder.toString())))
        contentsArray.put(currentTurn)

        rootJson.put("contents", contentsArray)

        // 3. Generation Config
        val genConfig = JSONObject()
            .put("temperature", if (persona == SbAiPersona.LUCU) 0.85 else 0.7)
            .put("maxOutputTokens", 2500)
        rootJson.put("generationConfig", genConfig)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = rootJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .addHeader("x-goog-api-key", apiKey)
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(responseBody).getJSONObject("error").getString("message")
                } catch (_: Exception) {
                    "HTTP ${response.code}: $responseBody"
                }
                throw Exception(errorMsg)
            }

            val resJson = JSONObject(responseBody)
            val candidates = resJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val contentObj = firstCandidate.optJSONObject("content")
                val parts = contentObj?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return parts.getJSONObject(0).optString("text", "")
                }
            }
            return ""
        }
    }

    private fun cleanResponse(raw: String): String {
        var clean = raw.trim()
        // Bersihkan awalan klise yang tidak diinginkan jika model secara tak sengaja menghasilkannya
        val prefixesToRemove = listOf(
            "Tentu, berikut adalah",
            "Tentu, ini adalah",
            "Tentu saja,",
            "Tentu!",
            "Halo, tentu!",
            "Tentu, berikut hasil suntingannya:",
            "Tentu, ini hasil lanjutannya:"
        )
        for (p in prefixesToRemove) {
            if (clean.startsWith(p, ignoreCase = true)) {
                clean = clean.substring(p.length).trim()
            }
        }
        return clean
    }
}
