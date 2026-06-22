package org.panashe.bible.features.audio

import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechUtterance

class IosTtsEngine : TtsEngine {
    private var synthesizer = AVSpeechSynthesizer()
    private var speechRate: Float = 0.5f
    private var _isSpeaking: Boolean = false
    private var _selectedVoiceIndex: Int = 0
    private val _voices = mutableListOf<String>()

    override val availableVoices: List<String> get() = _voices.toList()
    override var selectedVoiceIndex: Int
        get() = _selectedVoiceIndex
        set(value) { _selectedVoiceIndex = value.coerceIn(0, _voices.size - 1) }

    override val isSpeaking: Boolean get() = _isSpeaking

    override fun speak(text: String) {
        synthesizer = AVSpeechSynthesizer()
        val utterance = AVSpeechUtterance(string = text)
        utterance.setRate(speechRate)
        _isSpeaking = true
        synthesizer.speakUtterance(utterance)
    }

    override fun stop() {
        synthesizer = AVSpeechSynthesizer()
        _isSpeaking = false
    }

    override fun setSpeed(rate: Float) {
        speechRate = rate * 0.5f
    }
}

actual fun createTtsEngine(context: Any?): TtsEngine = IosTtsEngine()
