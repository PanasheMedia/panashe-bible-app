package org.panashe.bible.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.FILE
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fwrite

@OptIn(ExperimentalForeignApi::class)
actual fun readFile(path: String): String = memScoped {
    val file = fopen(path, "r") ?: return@memScoped ""
    try {
        val buf = ByteArray(4096)
        val sb = StringBuilder()
        while (true) {
            val n = buf.usePinned { pinned ->
                fread(pinned.addressOf(0), 1u, buf.size.toULong(), file)
            }
            if (n <= 0uL) break
            sb.append(buf.decodeToString(0, n.toInt()))
        }
        sb.toString()
    } finally {
        fclose(file)
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun writeFile(path: String, content: String) = memScoped {
    val file = fopen(path, "w") ?: return@memScoped
    try {
        val bytes = content.encodeToByteArray()
        bytes.usePinned { pinned ->
            fwrite(pinned.addressOf(0), 1u, bytes.size.toULong(), file)
        }
    } finally {
        fclose(file)
    }
}
