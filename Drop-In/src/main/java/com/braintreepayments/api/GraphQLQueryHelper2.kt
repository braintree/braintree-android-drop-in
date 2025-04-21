package com.braintreepayments.api

import android.content.Context
import android.content.res.Resources
import java.io.IOException
import java.io.InputStream

internal object GraphQLQueryHelper2 {
    @JvmStatic
    @Throws(Resources.NotFoundException::class, IOException::class)
    fun getQuery(context: Context, queryResource: Int): String {
        var inputStream: InputStream? = null
        return try {
            inputStream = context.resources.openRawResource(queryResource)
            StreamHelper2.getString(inputStream)
        } finally {
            inputStream?.close()
        }
    }
}
