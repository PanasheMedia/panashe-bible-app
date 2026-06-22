package org.panashe.bible.features.audio

/** Shared TTS state and interface for platform text-to-speech engines. */
interface TtsEngine {
    fun speak(text: String)
    fun stop()
    fun setSpeed(rate: Float)
    val isSpeaking: Boolean
    val availableVoices: List<String>
    var selectedVoiceIndex: Int
}

expect fun createTtsEngine(context: Any? = null): TtsEngine
