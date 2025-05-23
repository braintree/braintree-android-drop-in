package com.braintreepayments.api

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

internal object StreamHelper2 {
    @Throws(IOException::class)
    fun getString(inputStream: InputStream?): String {
        val buffReader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
        return buffReader.use { reader ->
            val data = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                data.append(line)
            }
            data.toString()
        }
    }
}
