package org.panashe.bible.platform

expect fun readFile(path: String): String
expect fun writeFile(path: String, content: String)
