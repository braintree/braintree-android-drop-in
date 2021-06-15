package com.braintreepayments.api;

import android.view.View;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.braintreepayments.api.dropin.R;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class SelectPaymentMethodFragmentTest {

    @Test
    public void onCreate_loaderIsVisible() {
//        FragmentScenario<SelectPaymentMethodFragment> scenario =
//                FragmentScenario.launch(SelectPaymentMethodFragment.class);
//        scenario.moveToState(Lifecycle.State.CREATED);
//        onView(withId(R.id.bt_select_payment_method_loader)).check(matches(isDisplayed()));
    }

    @Test
    public void onCreate_vaultEditButtonIsInvisible() {
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
    public void onCreate_whenVaultManagerEnabled_vaultEditButtonIsVisible() {
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
    public void onCreate_whenVaultManagerDisabled_vaultEditButtonIsHidden() {
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
    public void onCreate_whenVaultManagerUnspecified_vaultEditButtonIsHidden() {
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
    public void onCreate_whenVaultedCardExists_sendsAnalyticEvent() {
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
    public void onCreate_whenVaultedCardDoesNotExist_doesNotSendAnalyticEvent() {
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
    public void onVaultEditButtonClick_sendsAnalyticEvent() {
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
    public void onCreate_whenNoVaultedPaymentMethodsFound_showsNothing() {
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
    public void onCreate_whenVaultedPaymentMethodsFound_showsVaultedPaymentMethods() {
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

    @Test
    public void onCreate_showsLoadingIndicatorInitially() {
//        String authorization = Fixtures.TOKENIZATION_KEY;
//        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);
//        setupDropInActivity(authorization, mock(DropInClient.class), dropInRequest, "sessionId");
//        mActivityController.setup();
//
//        assertEquals(0, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher)).getDisplayedChild());
    }
}