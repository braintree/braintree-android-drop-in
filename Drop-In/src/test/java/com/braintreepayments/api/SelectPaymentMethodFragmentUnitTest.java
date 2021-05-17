package com.braintreepayments.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;

import static com.braintreepayments.api.DropInActivity.EXTRA_PAYMENT_METHOD_NONCES;
import static com.braintreepayments.api.DropInRequest.EXTRA_CHECKOUT_REQUEST;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class SelectPaymentMethodFragmentUnitTest {

    @Test
    public void onVaultEditButtonClick_sendsAnalyticEvent() {
        SelectPaymentMethodFragment sut = new SelectPaymentMethodFragment();

//        mActivity.onVaultEditButtonClick(null);
//
//
//        verify(mActivity.mBraintreeFragment).sendAnalyticsEvent("manager.appeared");
    }

    @Test
    public void onVaultEditButtonClick_launchesVaultManagerActivity() {
//        setup(mock(BraintreeFragment.class));
//
//        mActivity.onVaultEditButtonClick(null);
//
//        ShadowActivity.IntentForResult intent = mShadowActivity.getNextStartedActivityForResult();
//
//        assertEquals(2, intent.requestCode);
//        assertEquals(mActivity.mDropInRequest, intent.intent.getParcelableExtra(EXTRA_CHECKOUT_REQUEST));
//        assertEquals(mActivity.mBraintreeFragment.getCachedPaymentMethodNonces(),
//                intent.intent.getParcelableArrayListExtra(EXTRA_PAYMENT_METHOD_NONCES));
    }
}
