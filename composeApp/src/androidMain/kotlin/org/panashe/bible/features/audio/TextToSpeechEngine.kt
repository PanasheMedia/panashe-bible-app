package org.panashe.bible.features.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

actual fun createTtsEngine(context: Any?): TtsEngine {
    if (context is Context) {
        return AndroidTtsHolder.getOrCreate(context.applicationContext)
    }
    return AndroidTtsHolder.current ?: NoopTtsEngine
}

private object AndroidTtsHolder {
    var current: TtsEngine? = null
        private set

    fun getOrCreate(context: Context): TtsEngine =
        current ?: AndroidTtsEngine(context).also { current = it }
}

private object NoopTtsEngine : TtsEngine {
    override fun speak(text: String) = Unit
    override fun stop() = Unit
    override fun setSpeed(rate: Float) = Unit
    override val isSpeaking: Boolean = false
    override val availableVoices: List<String> = listOf("Default voice")
    override var selectedVoiceIndex: Int = 0
}

class AndroidTtsEngine(context: Context) : TtsEngine {
    private var tts: TextToSpeech? = null
    private var initialized = false
    private var pendingText: String? = null
    private var speechSpeed: Float = 1f
    private var _selectedVoiceIndex: Int = 0
    private val _voices = mutableListOf<String>()

    override val availableVoices: List<String> get() = _voices.toList()
    override var selectedVoiceIndex: Int
        get() = _selectedVoiceIndex
        set(value) {
            _selectedVoiceIndex = if (_voices.isEmpty()) 0 else value.coerceIn(0, _voices.size - 1)
            applyVoice()
        }

    override var isSpeaking: Boolean = false
        private set

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                initialized = true
                refreshVoices()
                pendingText?.let { speakInternal(it) }
                pendingText = null
            }
        }
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(uttId: String?) { isSpeaking = true }
            override fun onDone(uttId: String?) { isSpeaking = false }
            override fun onError(uttId: String?) { isSpeaking = false }
            override fun onStop(uttId: String?, interrupted: Boolean) { isSpeaking = false }
        })
    }

    private fun refreshVoices() {
        _voices.clear()
        val available = tts?.voices?.filter { it.locale?.language == "en" }.orEmpty()
        available.forEach { _voices.add("${it.name} (${it.locale?.displayName ?: "en"})") }
        if (_voices.isEmpty()) _voices.add("Default voice")
    }

    private fun applyVoice() {
        if (!initialized) return
        val voices = tts?.voices?.filter { it.locale?.language == "en" }.orEmpty()
        if (_selectedVoiceIndex in voices.indices) {
            tts?.voice = voices[_selectedVoiceIndex]
        }
    }

    override fun speak(text: String) {
        if (!initialized) { pendingText = text; return }
        speakInternal(text)
    }

    private fun speakInternal(text: String) {
        applyVoice()
        tts?.setSpeechRate(speechSpeed)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utt")
    }

    override fun stop() {
        tts?.stop()
        isSpeaking = false
    }

    override fun setSpeed(rate: Float) {
        speechSpeed = rate.coerceIn(0.5f, 2.0f)
        if (initialized) tts?.setSpeechRate(speechSpeed)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
