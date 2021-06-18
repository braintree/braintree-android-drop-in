package com.braintreepayments.api

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.braintreepayments.api.dropin.R
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

//        scenario.onFragment { fragment ->
//            val activity = fragment.requireActivity()
//            fragment.parentFragmentManager.setFragmentResultListener("event", activity) { requestKey, result ->
//                assertTrue(true)
//                countDownLatch.countDown()
//            }
//        }
//
//        countDownLatch.await()
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
    fun onCreate_vaultEditButtonIsInvisible() {
//        FragmentScenario<SelectPaymentMethodFragment> scenario =
//                FragmentScenario.launch(SelectPaymentMethodFragment.class);
//
//        scenario.moveToState(Lifecycle.State.RESUMED);
//        scenario.onFragment(new FragmentScenario.FragmentAction<SelectPaymentMethodFragment>() {
//            @Override
//            public void perform(@NotNull SelectPaymentMethodFragment selectPaymentMethodFragment) {
//                DropInViewModel viewModel =
//                        new ViewModelProvider(selectPaymentMethodFragment.getActivity()).get(DropInViewModel.class);
//
//                viewModel.setAvailablePaymentMethods(Collections.<PaymentMethodType>emptyList());
//
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                onView(withId(R.id.bt_supported_payment_methods_header)).check(doesNotExist());
//                onView(withId(R.id.bt_vault_edit_button)).check(matches(not(isDisplayed())));
//            }
//        });
    }

    @Test
    fun onCreate_whenVaultManagerEnabled_vaultEditButtonIsVisible() {
//        String authorization = Fixtures.TOKENIZATION_KEY;
//        DropInRequest dropInRequest = new DropInRequest()
//                .vaultManager(true)
//                .tokenizationKey(authorization);
//        setupDropInActivity(authorization, mock(DropInClient.class), dropInRequest, "sessionId");
//
//        List<PaymentMethodNonce> nonceList = new ArrayList<>();
//        nonceList.add(mock(CardNonce.class));
//        mActivityController.setup();
//        mActivity.showVaultedPaymentMethods(nonceList);
//
//        assertEquals(View.VISIBLE, mActivity.findViewById(R.id.bt_vault_edit_button).getVisibility());
    }

    @Test
    fun onCreate_whenVaultManagerDisabled_vaultEditButtonIsHidden() {
//        String authorization = Fixtures.TOKENIZATION_KEY;
//        DropInRequest dropInRequest = new DropInRequest()
//                .vaultManager(false)
//                .tokenizationKey(authorization);
//        setupDropInActivity(authorization, mock(DropInClient.class), dropInRequest, "sessionId");
//
//        List<PaymentMethodNonce> nonceList = new ArrayList<>();
//        nonceList.add(mock(CardNonce.class));
//        mActivityController.setup();
//        mActivity.showVaultedPaymentMethods(nonceList);
//
//        assertEquals(View.INVISIBLE, mActivity.findViewById(R.id.bt_vault_edit_button).getVisibility());
    }

    @Test
    fun onCreate_whenVaultManagerUnspecified_vaultEditButtonIsHidden() {
//        String authorization = Fixtures.TOKENIZATION_KEY;
//        DropInRequest dropInRequest = new DropInRequest()
//                .tokenizationKey(authorization);
//        setupDropInActivity(authorization, mock(DropInClient.class), dropInRequest, "sessionId");
//
//        List<PaymentMethodNonce> nonceList = new ArrayList<>();
//        nonceList.add(mock(CardNonce.class));
//        mActivityController.setup();
//        mActivity.showVaultedPaymentMethods(nonceList);
//
//        assertEquals(View.INVISIBLE, mActivity.findViewById(R.id.bt_vault_edit_button).getVisibility());
    }

    @Test
    fun onCreate_whenVaultedCardExists_sendsAnalyticEvent() {
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