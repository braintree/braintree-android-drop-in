package com.braintreepayments.api

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

@RunWith(RobolectricTestRunner::class)
class StreamHelperUnitTest {

    @Test
    fun getString_readsAStringFromAStream() {
        val inputStream = ByteArrayInputStream("Test string".toByteArray(StandardCharsets.UTF_8));
        assertEquals("Test string", StreamHelper2.getString(inputStream))
    }
}
