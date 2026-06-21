package org.panashe.bible.platform

import java.io.File

actual fun readFile(path: String): String {
    val file = File(path)
    return if (file.exists()) file.readText() else ""
}

actual fun writeFile(path: String, content: String) {
    val file = File(path)
    file.parentFile?.mkdirs()
    file.writeText(content)
}
