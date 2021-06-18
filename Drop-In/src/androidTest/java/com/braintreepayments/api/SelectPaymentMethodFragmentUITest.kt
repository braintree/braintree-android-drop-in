package com.braintreepayments.api

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.braintreepayments.api.dropin.R
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.hamcrest.Matchers.not
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

// Ref: https://developer.android.com/guide/fragments/test
@RunWith(AndroidJUnit4::class)
class SelectPaymentMethodFragmentUITest {

    private lateinit var countDownLatch: CountDownLatch

    private val supportedPaymentMethods = listOf(
            DropInPaymentMethodType.PAYPAL,
            DropInPaymentMethodType.PAY_WITH_VENMO,
            DropInPaymentMethodType.UNKNOWN,
            DropInPaymentMethodType.GOOGLE_PAYMENT
    )

    @Before
    fun beforeEach() {
        countDownLatch = CountDownLatch(1)
    }

    @Test
    @Throws(InterruptedException::class)
    fun whenStateIsRESUMED_loaderIsVisible() {
        val scenario = FragmentScenario.launchInContainer(SelectPaymentMethodFragment::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)
        onView(withId(R.id.bt_select_payment_method_loader)).check(matches(isDisplayed()))
    }

    @Test
    fun whenStateIsRESUMED_whenSupportedPaymentMethodsLoaded_displaysPaymentMethods() {
        val scenario = FragmentScenario.launchInContainer(SelectPaymentMethodFragment::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedPaymentMethods(supportedPaymentMethods)
        }

        onView(withId(R.id.bt_select_payment_method_loader)).check(matches(not(isDisplayed())))
        onView(withText("PayPal")).check(matches(isDisplayed()))
        onView(withText("Venmo")).check(matches(isDisplayed()))
        onView(withText("Credit or Debit Card")).check(matches(isDisplayed()))
        onView(withText("Google Pay")).check(matches(isDisplayed()))
    }

    @Test
    fun whenStateIsRESUMED_whenSupportedPaymentMethodsLoaded_requestsForVaultedPaymentMethodsToBeLoaded() {
        // TODO: assert an event is dispatched to load vaulted payment methods
    }

    @Test
    fun whenStateIsRESUMED_whenVaultManagerIsEnabledAndVaultedPaymentMethodsAreLoaded_displaysVaultedPaymentMethods() {
        val dropInRequest = DropInRequest()
                .vaultManager(true)
        val bundle = bundleOf("EXTRA_DROP_IN_REQUEST" to dropInRequest)

        val scenario = FragmentScenario.launchInContainer(SelectPaymentMethodFragment::class.java, bundle)
        scenario.moveToState(Lifecycle.State.RESUMED)

        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD))
        val payPalNonce = PayPalAccountNonce.fromJSON(JSONObject(Fixtures.PAYPAL_ACCOUNT_JSON))
        val venmoAccountNonce = VenmoAccountNonce.fromJSON(JSONObject(Fixtures.PAYMENT_METHODS_VENMO_ACCOUNT_RESPONSE))

        val vaultedPaymentMethods = listOf(cardNonce, payPalNonce, venmoAccountNonce)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedPaymentMethods(supportedPaymentMethods)
            fragment.dropInViewModel.setVaultedPaymentMethods(vaultedPaymentMethods)
        }

        onView(withId(R.id.bt_supported_payment_methods_header)).check(matches(isDisplayed()))
        onView(withText("paypalaccount@example.com")).check(matches(isDisplayed()))
        onView(withText("venmojoe")).check(matches(isDisplayed()))
        onView(withText("1111")).check(matches(isDisplayed()))
    }

    @Test
    fun whenStateIsRESUMED_whenVaultManagerIsEnabledAndVaultedPaymentMethodsAreLoaded_displaysVaultEditButton() {
        val dropInRequest = DropInRequest()
                .vaultManager(true)
        val bundle = bundleOf("EXTRA_DROP_IN_REQUEST" to dropInRequest)

        val scenario = FragmentScenario.launchInContainer(SelectPaymentMethodFragment::class.java, bundle)
        scenario.moveToState(Lifecycle.State.RESUMED)

        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD))
        val vaultedPaymentMethods = listOf(cardNonce)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedPaymentMethods(supportedPaymentMethods)
            fragment.dropInViewModel.setVaultedPaymentMethods(vaultedPaymentMethods)
        }

        onView(withId(R.id.bt_vault_edit_button)).check(matches(isDisplayed()))
    }

    @Test
    fun whenStateIsRESUMED_whenVaultManagerIsDisabled_hidesVaultEditButton() {
        val dropInRequest = DropInRequest()
                .vaultManager(false)
        val bundle = bundleOf("EXTRA_DROP_IN_REQUEST" to dropInRequest)

        val scenario = FragmentScenario.launchInContainer(SelectPaymentMethodFragment::class.java, bundle)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedPaymentMethods(supportedPaymentMethods)
        }

        onView(withId(R.id.bt_vault_edit_button)).check(matches(not(isDisplayed())))
    }

    @Test(timeout=5000)
    fun whenStateIsRESUMED_whenVaultManagerEnabledAndVaultedCardExists_sendsAnalyticEvent() {
        val dropInRequest = DropInRequest()
                .vaultManager(true)
        val bundle = bundleOf("EXTRA_DROP_IN_REQUEST" to dropInRequest)

        val scenario = FragmentScenario.launchInContainer(SelectPaymentMethodFragment::class.java, bundle)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD))
            val vaultedPaymentMethods = listOf(cardNonce)

            fragment.dropInViewModel.setSupportedPaymentMethods(supportedPaymentMethods)
            fragment.dropInViewModel.setVaultedPaymentMethods(vaultedPaymentMethods)

            val activity = fragment.requireActivity()
            val fragmentManager = fragment.parentFragmentManager

            fragmentManager.setFragmentResultListener("BRAINTREE_RESULT", activity) { requestKey, result ->
                val event = result.get("BRAINTREE_EVENT") as DropInAnalyticsEvent
                assertEquals(event.fragment, "vaulted-card.appear")
                countDownLatch.countDown()
            }
        }
        countDownLatch.await()

//        String authorization = Fixtures.TOKENIZATION_KEY;
//        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);
//
//        DropInClient dropInClient = new MockDropInClientBuilder()
//                .build();
//        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
//        mActivityController.setup();
//
//        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));
//        PayPalAccountNonce payPalAccountNonce = PayPalAccountNonce.fromJSON(new JSONObject(Fixtures.PAYPAL_ACCOUNT_JSON));
//        List<PaymentMethodNonce> paymentMethodNonces = Arrays.asList(cardNonce, payPalAccountNonce);
//
//        mActivity.showVaultedPaymentMethods(paymentMethodNonces);
//        verify(dropInClient).sendAnalyticsEvent("vaulted-card.appear");
    }

    @Test
    fun onCreate_whenVaultedCardDoesNotExist_doesNotSendAnalyticEvent() {
//        String authorization = Fixtures.TOKENIZATION_KEY;
//        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);
//
//        DropInClient dropInClient = new MockDropInClientBuilder()
//                .build();
//        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
//        mActivityController.setup();
//
//        PayPalAccountNonce payPalAccountNonce = PayPalAccountNonce.fromJSON(new JSONObject(Fixtures.PAYPAL_ACCOUNT_JSON));
//        PaymentMethodNonce googlePayCardNonce = GooglePayCardNonce.fromJSON(new JSONObject(Fixtures.GOOGLE_PAY_NETWORK_TOKENIZED_RESPONSE));
//        List<PaymentMethodNonce> paymentMethodNonces = Arrays.asList(payPalAccountNonce, googlePayCardNonce);
//
//
//        mActivity.showVaultedPaymentMethods(paymentMethodNonces);
//        verify(dropInClient, never()).sendAnalyticsEvent("vaulted-card.appear");
    }

    @Test
    fun onVaultEditButtonClick_sendsAnalyticEvent() {
//        String authorization = Fixtures.TOKENIZATION_KEY;
//        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);
//
//        DropInClient dropInClient = new MockDropInClientBuilder()
//                .getVaultedPaymentMethodsSuccess(new ArrayList<PaymentMethodNonce>())
//                .build();
//        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
//        mActivityController.setup();
//
//        mActivity.onVaultEditButtonClick(null);
//
//        verify(dropInClient).sendAnalyticsEvent("manager.appeared");
    }

    @Test
    fun onCreate_whenNoVaultedPaymentMethodsFound_showsNothing() {
//        String authorization = Fixtures.CLIENT_TOKEN;
//        DropInRequest dropInRequest = new DropInRequest().clientToken(Fixtures.CLIENT_TOKEN);
//
//        DropInClient dropInClient = new MockDropInClientBuilder()
//                .authorization(Authorization.fromString(authorization))
//                .getConfigurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
//                .getSupportedPaymentMethodsSuccess(new ArrayList<DropInPaymentMethodType>())
//                .getVaultedPaymentMethodsSuccess(new ArrayList<PaymentMethodNonce>())
//                .build();
//        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
//        mActivityController.setup();
//
//        mActivity.fetchPaymentMethodNonces(true);
//
//        assertThat(mActivity.findViewById(R.id.bt_vaulted_payment_methods_wrapper)).isNotShown();
//        assertThat((TextView) mActivity.findViewById(R.id.bt_supported_payment_methods_header)).hasText(R.string.bt_select_payment_method);
    }

    @Test
    fun onCreate_whenVaultedPaymentMethodsFound_showsVaultedPaymentMethods() {
//        String authorization = Fixtures.TOKENIZATION_KEY;
//        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);
//
//        ArrayList<PaymentMethodNonce> nonces = new ArrayList<>();
//        nonces.add(mock(PayPalAccountNonce.class));
//        nonces.add(mock(CardNonce.class));
//
//        DropInClient dropInClient = new MockDropInClientBuilder()
//                .getVaultedPaymentMethodsSuccess(nonces)
//                .build();
//        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
//
//        mActivity.mClientTokenPresent = true;
//        mActivityController.setup();
//
//        mActivity.fetchPaymentMethodNonces(true);
//        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
//
//        assertThat(mActivity.findViewById(R.id.bt_vaulted_payment_methods_wrapper)).isVisible();
//        assertEquals(2, Objects.requireNonNull(((RecyclerView) mActivity.findViewById(R.id.bt_vaulted_payment_methods)).getAdapter()).getItemCount());
//        assertThat((TextView) mActivity.findViewById(R.id.bt_supported_payment_methods_header)).hasText(R.string.bt_other);
    }
}