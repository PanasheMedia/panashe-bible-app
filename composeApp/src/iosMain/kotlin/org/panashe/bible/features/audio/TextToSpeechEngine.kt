package org.panashe.bible.features.audio

import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechUtterance
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechBoundaryImmediate

class IosTtsEngine : TtsEngine {
    private val synthesizer = AVSpeechSynthesizer()
    private var speechRate: Float = 0.5f
    private var _selectedVoiceIndex: Int = 0
    private val _voices = mutableListOf<String>()

    override val availableVoices: List<String> get() {
        if (_voices.isEmpty()) refreshVoices()
        return _voices.toList()
    }

    override var selectedVoiceIndex: Int
        get() = _selectedVoiceIndex
        set(value) { _selectedVoiceIndex = value.coerceIn(0, _voices.size - 1) }

    override var isSpeaking: Boolean
        get() = synthesizer.speaking
        private set

    private fun refreshVoices() {
        _voices.clear()
        val voices = AVSpeechSynthesisVoice.speechVoices()
        voices.forEach { voice ->
            val lang = voice.language ?: "en-US"
            if (lang.startsWith("en")) {
                _voices.add("${voice.name ?: voice.identifier} ($lang)")
            }
        }
        if (_voices.isEmpty()) _voices.add("Default voice")
    }

    override fun speak(text: String) {
        synthesizer.stopSpeakingAtBoundary(AVSpeechBoundaryImmediate)
        val utterance = AVSpeechUtterance.alloc().initWithString(text)
        utterance.setRate(speechRate)
        val voices = AVSpeechSynthesisVoice.speechVoices()
            .filter { it.language?.startsWith("en") == true }
        if (_selectedVoiceIndex in voices.indices) {
            utterance.setVoice(voices[_selectedVoiceIndex])
        }
        synthesizer.speakUtterance(utterance)
    }

    override fun stop() {
        synthesizer.stopSpeakingAtBoundary(AVSpeechBoundaryImmediate)
    }

    override fun setSpeed(rate: Float) {
        speechRate = rate * 0.5f
    }
}

actual fun createTtsEngine(context: Any?): TtsEngine = IosTtsEngine()
