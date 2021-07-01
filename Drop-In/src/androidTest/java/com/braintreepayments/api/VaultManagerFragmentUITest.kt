package com.braintreepayments.api

import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class VaultManagerFragmentUITest {

    val vaultedPaymentMethodNonces = ArrayList<PaymentMethodNonce>()

    @Before
    fun beforeEach() {
        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))

        vaultedPaymentMethodNonces.add(cardNonce)
    }

    @Test
    fun whenStateIsRESUMED_setsVaultedPaymentMethodNoncesInAdapter() {
        val scenario = FragmentScenario.launchInContainer(VaultManagerFragment::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setVaultedPaymentMethods(vaultedPaymentMethodNonces)
            assertEquals(1, fragment.adapter.paymentMethodNonces.size)
        }
    }
}