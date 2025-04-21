package com.braintreepayments.api

import android.content.res.Resources.NotFoundException
import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.braintreepayments.api.GraphQLQueryHelper2.getQuery
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class GraphQLQueryHelperTest {

    @Test(expected = NotFoundException::class)
    fun query_throwsResourcesNotFoundExceptionForInvalidResources() {
        getQuery(ApplicationProvider.getApplicationContext(), -1)
    }
}
