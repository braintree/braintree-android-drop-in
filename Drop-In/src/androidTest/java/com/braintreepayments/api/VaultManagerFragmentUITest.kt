package com.braintreepayments.api

import androidx.core.view.get
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.braintreepayments.api.DropInUIEventType.DISMISS_VAULT_MANAGER
import com.braintreepayments.api.dropin.R
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertSame
import org.hamcrest.Matchers
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch


@RunWith(AndroidJUnit4::class)
class VaultManagerFragmentUITest {

    private lateinit var countDownLatch: CountDownLatch
    private lateinit var cardNonce: CardNonce
    private lateinit var scenario: FragmentScenario<VaultManagerFragment>

    val vaultedPaymentMethodNonces = ArrayList<PaymentMethodNonce>()

    @Before
    fun beforeEach() {
        cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        vaultedPaymentMethodNonces.add(cardNonce)

        countDownLatch = CountDownLatch(1)

        scenario = FragmentScenario.launchInContainer(VaultManagerFragment::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)
    }

    @Test
    fun whenStateIsRESUMED_setsVaultedPaymentMethodNoncesInAdapter() {
        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setVaultedPaymentMethods(vaultedPaymentMethodNonces)
            assertEquals(1, fragment.adapter.paymentMethodNonces.size)
        }
    }

    @Test
    fun whenStateIsRESUMED_onDeleteIconClick_sendsDeletePaymentEvent() {
        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setVaultedPaymentMethods(vaultedPaymentMethodNonces)
        }

        onView(isRoot()).perform(waitFor(500))
        onView(withId(R.id.bt_payment_method_delete_icon)).perform(click())

        scenario.onFragment { fragment ->
            val activity = fragment.requireActivity()
            val fragmentManager = fragment.parentFragmentManager
            fragmentManager.setFragmentResultListener("BRAINTREE_EVENT", activity) { requestKey, result ->
                val event = result.get("BRAINTREE_RESULT") as DeleteVaultedPaymentMethodNonceEvent
                assertSame(cardNonce, event.paymentMethodNonceToDelete)
                countDownLatch.countDown()
            }
        }

        countDownLatch.await()
    }

    @Test
    fun whenStateIsRESUMED_onCloseButtonClick_sendsDismissUIEvent() {
        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setVaultedPaymentMethods(vaultedPaymentMethodNonces)
        }

        onView(isRoot()).perform(waitFor(500))
        onView(withId(R.id.bt_vault_manager_close)).perform(click())

        scenario.onFragment { fragment ->
            val activity = fragment.requireActivity()
            val fragmentManager = fragment.parentFragmentManager
            fragmentManager.setFragmentResultListener("BRAINTREE_EVENT", activity) { requestKey, result ->
                val event = result.get("BRAINTREE_RESULT") as DropInUIEvent
                assertSame(DISMISS_VAULT_MANAGER, event.type)
                countDownLatch.countDown()
            }
        }

        countDownLatch.await()
    }
}