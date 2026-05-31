package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini Moshi Models ---

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val temperature: Float? = 0.2f,
    val responseMimeType: String? = "text/plain"
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null
)

// --- Retrofit Service Interface ---

interface GeminiRestService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

// --- Repository Implementation ---

class GeminiRepository {
    private val tag = "GeminiRepository"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val apiService = retrofit.create(GeminiRestService::class.java)

    /**
     * Call the Gemini API with a strict system instruction to produce clean, helpful flashing insights.
     */
    suspend fun analyzeFlashingIssue(
        deviceModel: String,
        romType: String,
        errorLogs: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(tag, "Gemini API key is not configured.")
            return@withContext "Apologies! Your Gemini API Key is missing. Please configure your API key in the AI Studio Settings Panel to unlock live AI diagnostics."
        }

        val prompt = """
            The user is flashing a ROM on an Android device and encountered an error.
            Device Model: $deviceModel
            ROM / Package Format: $romType
            Terminal Error Logs:
            $errorLogs
            
            Please analyze this log:
            1. Explain exactly what went wrong in plain, beginner-friendly terms.
            2. Provide the precise ADB or FASTBOOT Terminal commands to resolve the issue as a formatted code block.
        """.trimIndent()

        val systemPrompt = """
            You are DroidFlash AI Co-Pilot, an expert in Android low-level flashing, bootloaders, Fastboot, ADB, payload.bin extraction, and custom recoveries.
            Your job is to diagnose flashing failures. Be ultra-technical, accurate, yet accessible. Avoid meta-commentary. Provide direct terminal-style steps.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
            generationConfig = GeminiGenerationConfig(temperature = 0.2f)
        )

        try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "No diagnostics generated. Make sure your device is connected and logs are accurate."
        } catch (e: Exception) {
            Log.e(tag, "API Exception: ", e)
            "Error analyzing issue: ${e.localizedMessage}. Verify your network connection and API key configuration."
        }
    }

    /**
     * Ask Gemini any Android flashing / custom ROM boot question.
     */
    suspend fun askFlashingAssistant(question: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key not configured. Please add your GEMINI_API_KEY in the AI Studio Secrets panel."
        }

        val systemPrompt = """
            You are DroidFlash AI Co-Pilot, a expert Android ROM flasher, partition table expert, and boot repair specialist.
            Answer Android modding questions efficiently, with bold bullet points, exact command syntax blocks, and safe guidelines to prevent bricking devices.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = question)))),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
            generationConfig = GeminiGenerationConfig(temperature = 0.5f)
        )

        try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "No response from Assistant."
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }
}
