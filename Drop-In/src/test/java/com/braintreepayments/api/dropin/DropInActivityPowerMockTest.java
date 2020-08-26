package com.braintreepayments.api.dropin;

import android.widget.ListView;
import android.widget.ViewSwitcher;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.GooglePayment;
import com.braintreepayments.api.PayPal;
import com.braintreepayments.api.Venmo;
import com.braintreepayments.api.dropin.adapters.SupportedPaymentMethodsAdapter;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.exceptions.GoogleApiClientException;
import com.braintreepayments.api.interfaces.BraintreeResponseListener;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.models.GooglePaymentRequest;
import com.braintreepayments.api.models.PayPalRequest;
import com.braintreepayments.api.test.TestConfigurationBuilder;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowActivity.IntentForResult;

import static com.braintreepayments.api.test.ReflectionHelper.setField;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*" })
@PrepareForTest({ PayPal.class, Venmo.class, GooglePayment.class })
public class DropInActivityPowerMockTest {

    @Rule
    public PowerMockRule mPowerMockRule = new PowerMockRule();

    private DropInUnitTestActivity mActivity;

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        mActivity = Robolectric.buildActivity(DropInUnitTestActivity.class).get();
        setField(DropInActivity.class, mActivity, "mLoadingViewSwitcher",
                new ViewSwitcher(RuntimeEnvironment.application));
    }

    @Test
    public void googleApiClientExceptionDisablesGooglePaymentButDoesNotExitActivity() {
        mockStatic(GooglePayment.class);
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((BraintreeResponseListener<Boolean>) invocation.getArguments()[1]).onResponse(true);
                return null;
            }
        }).when(GooglePayment.class);
        GooglePayment.isReadyToPay(any(BraintreeFragment.class), any(BraintreeResponseListener.class));
        Configuration configuration = new TestConfigurationBuilder()
                .googlePayment(new TestConfigurationBuilder.TestGooglePaymentConfigurationBuilder()
                        .enabled(true))
                .buildConfiguration();
        mActivity.setDropInRequest(new DropInRequest()
                .googlePaymentRequest(new GooglePaymentRequest()));
        mActivity.mSupportedPaymentMethodListView = mock(ListView.class);
        mActivity.onConfigurationFetched(configuration);

        ArgumentCaptor<SupportedPaymentMethodsAdapter> captor = ArgumentCaptor.forClass(SupportedPaymentMethodsAdapter.class);
        verify(mActivity.mSupportedPaymentMethodListView, times(1)).setAdapter(captor.capture());
        assertEquals(1, captor.getValue().getCount());

        mActivity.onError(new GoogleApiClientException(GoogleApiClientException.ErrorType.ConnectionFailed, 1));

        assertFalse(mActivity.isFinishing());
        verify(mActivity.mSupportedPaymentMethodListView, times(2)).setAdapter(captor.capture());
        assertEquals(0, captor.getValue().getCount());
    }

    @Test
    public void onPaymentMethodSelected_withoutPayPalRequest_startsRequestBillingAgreement() {
        DropInRequest dropInRequest = new DropInRequest();
        mActivity.setDropInRequest(dropInRequest);
        mockStatic(PayPal.class);
        doNothing().when(PayPal.class);
        PayPal.requestBillingAgreement(any(BraintreeFragment.class), any(PayPalRequest.class));

        mActivity.onPaymentMethodSelected(PaymentMethodType.PAYPAL);

        verifyStatic();
        PayPal.requestBillingAgreement(eq(mActivity.braintreeFragment), any(PayPalRequest.class));
    }

    @Test
    public void onPaymentMethodSelected_withPayPalRequestAndAmount_startsRequestOneTimePayment() {
        PayPalRequest paypalRequest = new PayPalRequest("10")
                .currencyCode("USD");
        DropInRequest dropInRequest = new DropInRequest()
                .paypalRequest(paypalRequest);
        mActivity.setDropInRequest(dropInRequest);
        mockStatic(PayPal.class);
        doNothing().when(PayPal.class);
        PayPal.requestOneTimePayment(any(BraintreeFragment.class), any(PayPalRequest.class));

        mActivity.onPaymentMethodSelected(PaymentMethodType.PAYPAL);

        verifyStatic();
        PayPal.requestOneTimePayment(eq(mActivity.braintreeFragment), any(PayPalRequest.class));
    }

    @Test
    public void onPaymentMethodSelected_withPayPalRequestWithoutAmount_startsRequestBillingAgreement() {
        PayPalRequest paypalRequest = new PayPalRequest()
                .billingAgreementDescription("Peter Pipers Pepper Pickers");
        DropInRequest dropInRequest = new DropInRequest()
                .paypalRequest(paypalRequest);
        mActivity.setDropInRequest(dropInRequest);
        mockStatic(PayPal.class);
        doNothing().when(PayPal.class);
        PayPal.requestBillingAgreement(any(BraintreeFragment.class), any(PayPalRequest.class));

        mActivity.onPaymentMethodSelected(PaymentMethodType.PAYPAL);

        verifyStatic();
        PayPal.requestBillingAgreement(eq(mActivity.braintreeFragment), any(PayPalRequest.class));
    }

    @Test
    public void onPaymentMethodSelected_startsGooglePayment() {
        DropInRequest dropInRequest = new DropInRequest()
                .googlePaymentRequest(new GooglePaymentRequest());

        mActivity.setDropInRequest(dropInRequest);
        mockStatic(GooglePayment.class);
        doNothing().when(GooglePayment.class);
        GooglePayment.requestPayment(any(BraintreeFragment.class), any(GooglePaymentRequest.class));

        mActivity.onPaymentMethodSelected(PaymentMethodType.GOOGLE_PAYMENT);

        verifyStatic();
        GooglePayment.requestPayment(mActivity.braintreeFragment, dropInRequest.getGooglePaymentRequest());
    }

    @Test
    public void onPaymentMethodSelected_startsVenmo() {
        DropInRequest dropInRequest = new DropInRequest()
                .vaultVenmo(true);
        mActivity.setDropInRequest(dropInRequest);

        mockStatic(Venmo.class);
        doNothing().when(Venmo.class);
        Venmo.authorizeAccount(any(BraintreeFragment.class));

        mActivity.onPaymentMethodSelected(PaymentMethodType.PAY_WITH_VENMO);

        verifyStatic();
        Venmo.authorizeAccount(mActivity.braintreeFragment, true);
    }

    @Test
    public void onPaymentMethodSelected_startsAddCardActivity() {
        ShadowActivity shadowActivity = shadowOf(mActivity);

        mActivity.onPaymentMethodSelected(PaymentMethodType.UNKNOWN);

        IntentForResult intent = shadowActivity.peekNextStartedActivityForResult();
        assertEquals(AddCardActivity.class.getName(), intent.intent.getComponent().getClassName());
    }
}