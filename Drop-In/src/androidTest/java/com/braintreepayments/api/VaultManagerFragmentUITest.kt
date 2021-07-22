package com.braintreepayments.api

import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.braintreepayments.api.dropin.R
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertSame
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch


@RunWith(AndroidJUnit4::class)
class VaultManagerFragmentUITest {

    private lateinit var countDownLatch: CountDownLatch
    private lateinit var cardNonce: CardNonce
    private lateinit var scenario: FragmentScenario<VaultManagerFragment>

    private val vaultedPaymentMethodNonces = ArrayList<PaymentMethodNonce>()

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
            fragmentManager.setFragmentResultListener(DropInEvent.REQUEST_KEY, activity) { _, result ->
                val event = DropInEvent.fromBundle(result)
                assertEquals(DropInEventType.DELETE_VAULTED_PAYMENT_METHOD, event.type)

                val paymentMethodNonceToDelete =
                        event.getPaymentMethodNonce(DropInEventProperty.VAULTED_PAYMENT_METHOD)
                assertSame(cardNonce, paymentMethodNonceToDelete)
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
            fragmentManager.setFragmentResultListener(DropInEvent.REQUEST_KEY, activity) { _, result ->
                val event = DropInEvent.fromBundle(result)
                assertEquals(DropInEventType.DISMISS_VAULT_MANAGER, event.type)
                countDownLatch.countDown()
            }
        }

        countDownLatch.await()
    }
}