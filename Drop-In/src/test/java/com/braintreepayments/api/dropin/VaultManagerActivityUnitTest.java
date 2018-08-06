package com.braintreepayments.api.dropin;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.PaymentMethod;
import com.braintreepayments.api.dropin.adapters.VaultManagerPaymentMethodsAdapter;
import com.braintreepayments.api.exceptions.PaymentMethodDeleteException;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.PayPalAccountNonce;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.test.ReflectionHelper;
import com.braintreepayments.api.test.UnitTestFixturesHelper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;

import java.util.ArrayList;
import java.util.List;

import static com.braintreepayments.api.dropin.DropInActivity.EXTRA_PAYMENT_METHOD_NONCES;
import static com.braintreepayments.api.dropin.DropInRequest.EXTRA_CHECKOUT_REQUEST;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.robolectric.Shadows.shadowOf;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(RobolectricTestRunner.class)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*", "org.json.*"})
@PrepareForTest({PaymentMethod.class})
@Config(constants = BuildConfig.class)
public class VaultManagerActivityUnitTest {

    @Rule
    public PowerMockRule mPowerMockRule = new PowerMockRule();

    private PaymentMethodNonce mCardNonce;
    private Intent mIntent = new Intent();
    private List<PaymentMethodNonce> mPaymentMethodNonces = new ArrayList<>();
    private VaultManagerActivity mActivity;
    private ActivityController<VaultManagerActivity> mActivityController;
    private ShadowActivity mShadowActivity;

    @Before
    public void setup() {
        mCardNonce = mock(CardNonce.class);
        when(mCardNonce.getNonce()).thenReturn("card-nonce");

        PayPalAccountNonce paypalNonce = mock(PayPalAccountNonce.class);
        when(paypalNonce.getNonce()).thenReturn("paypal-nonce");

        mPaymentMethodNonces.add(mCardNonce);
        mPaymentMethodNonces.add(paypalNonce);

        DropInRequest dropInRequest = new DropInRequest()
                .clientToken(UnitTestFixturesHelper.stringFromFixture("client_token.json"));

        Bundle in = new Bundle();
        in.putParcelableArrayList(EXTRA_PAYMENT_METHOD_NONCES, (ArrayList<? extends Parcelable>) mPaymentMethodNonces);
        in.putParcelable(EXTRA_CHECKOUT_REQUEST, dropInRequest);
        mIntent.putExtras(in);

        mActivityController = Robolectric.buildActivity(VaultManagerActivity.class, mIntent);
        mActivity = mActivityController.get();
        mShadowActivity = shadowOf(mActivity);

        mActivityController.create();
        mActivity.mBraintreeFragment = mock(BraintreeFragment.class);
    }

    @Test
    public void onCreate_setsPaymentMethodNoncesInAdapter() {
        VaultManagerPaymentMethodsAdapter adapter = mockAdapter();

        assertEquals(mPaymentMethodNonces.size(), adapter.getItemCount());
    }

    @Test
    public void onRestart_setsPaymentMethodNoncesInAdapter() {
        VaultManagerPaymentMethodsAdapter adapter = mockAdapter();
        ArrayList<PaymentMethodNonce> nonces = new ArrayList<>();
        nonces.add(mCardNonce);
        adapter.setPaymentMethodNonces(nonces);

        mActivityController.pause();

        verify(adapter).setPaymentMethodNonces(nonces);
    }

    @Test
    public void onPaymentMethodNonceDeleted_sendsAnalyticCall() {
        mActivity.onPaymentMethodNonceDeleted(mCardNonce);

        verify(mActivity.mBraintreeFragment).sendAnalyticsEvent("manager.delete.succeeded");
    }

    @Test
    public void onPaymentMethodNonceDeleted_setsOkResult() {
        mActivity.onPaymentMethodNonceDeleted(mCardNonce);

        assertEquals(BaseActivity.RESULT_OK, mShadowActivity.getResultCode());
    }

    @Test
    public void onError_whenPaymentMethodDeletedException_sendsAnalyticCall() {
        Exception originalException = new RuntimeException("Real Exception");
        Exception error = new PaymentMethodDeleteException(mCardNonce, originalException);

        mActivity.onError(error);

        verify(mActivity.mBraintreeFragment).sendAnalyticsEvent("manager.delete.failed");
    }

    @Test
    public void onError_whenPaymentMethodDeletedException_doesntDeleteThePaymentMethod() {
        VaultManagerPaymentMethodsAdapter adapter = mockAdapter();
        Exception originalException = new RuntimeException("Real Exception");
        Exception error = new PaymentMethodDeleteException(mCardNonce, originalException);

        mActivity.onError(error);

        verify(adapter).cancelSwipeOnPaymentMethodNonce(mCardNonce);
    }

    @Test
    public void onError_whenUnknownError_sendsAnalyticCall() {
        mActivity.onError(new RuntimeException());

        verify(mActivity.mBraintreeFragment).sendAnalyticsEvent("manager.unknown.failed");
    }

    @Test
    public void onError_whenUnknownError_finishesWithError() {
        Exception expected = new RuntimeException();

        mActivity.onError(expected);

        assertTrue(mShadowActivity.isFinishing());
        assertEquals(Activity.RESULT_FIRST_USER, mShadowActivity.getResultCode());
        Exception actual = (Exception) mShadowActivity.getResultIntent()
                .getSerializableExtra(DropInActivity.EXTRA_ERROR);

        assertEquals(expected, actual);
    }

    @Test
    public void onSwipe_selectingPositiveButton_sendsAnalyticEvent() {
        mockStatic(PaymentMethod.class);
        mActivity.onSwipe(0);

        getDeleteConfirmationDialog().getButton(DialogInterface.BUTTON_POSITIVE).callOnClick();

        verify(mActivity.mBraintreeFragment).sendAnalyticsEvent("manager.delete.confirmation.positive");
    }

    @Test
    public void onSwipe_selectingPositiveButton_callsDeletePaymentMethod() {
        mockStatic(PaymentMethod.class);
        mActivity.onSwipe(0);

        getDeleteConfirmationDialog().getButton(DialogInterface.BUTTON_POSITIVE).callOnClick();

        verifyStatic();
        PaymentMethod.deletePaymentMethod(eq(mActivity.mBraintreeFragment), eq(mCardNonce));
    }

    @Test
    public void onSwipe_selectingPositiveButton_doesNotSendNegativeAnalyticEvent() {
        mockStatic(PaymentMethod.class);
        mActivity.onSwipe(0);

        getDeleteConfirmationDialog().getButton(DialogInterface.BUTTON_POSITIVE).callOnClick();

        verify(mActivity.mBraintreeFragment, times(0)).sendAnalyticsEvent("manager.delete.confirmation.negative");
    }

    @Test
    public void onSwipe_selectingNegativeButton_doesntDeletePaymentMethod() {
        VaultManagerPaymentMethodsAdapter adapter = mockAdapter();

        mActivity.onSwipe(0);

        getDeleteConfirmationDialog().getButton(DialogInterface.BUTTON_NEGATIVE).callOnClick();

        verify(adapter).cancelSwipeOnPaymentMethodNonce(mCardNonce);
    }

    @Test
    public void onSwipe_selectingNegativeButton_sendsAnalyticEvent() {
        mActivity.onSwipe(0);

        getDeleteConfirmationDialog().getButton(DialogInterface.BUTTON_NEGATIVE).callOnClick();

        verify(mActivity.mBraintreeFragment).sendAnalyticsEvent("manager.delete.confirmation.negative");
    }

    @Test
    public void onSwipe_dismissingDialog_doesntDeletePaymentMethod() {
        VaultManagerPaymentMethodsAdapter adapter = mockAdapter();

        mActivity.onSwipe(0);

        getDeleteConfirmationDialog().dismiss();

        verify(adapter).cancelSwipeOnPaymentMethodNonce(mCardNonce);
    }

    @Test
    public void onSwipe_dismissingDialog_sendsAnalyticEvent() {
        mActivity.onSwipe(0);

        getDeleteConfirmationDialog().dismiss();

        verify(mActivity.mBraintreeFragment).sendAnalyticsEvent("manager.delete.confirmation.negative");
    }

    private static android.support.v7.app.AlertDialog getDeleteConfirmationDialog() {
        // ShadowAlertDialog#getLatestDialog does not return
        // the support AlertDialog.
        //
        // This allows us to return the first (and only) v7 alert dialog
        return (android.support.v7.app.AlertDialog) ShadowAlertDialog.getShownDialogs().get(0);
    }

    private VaultManagerPaymentMethodsAdapter mockAdapter() {
        VaultManagerPaymentMethodsAdapter adapter = null;
        try {
            adapter = spy((VaultManagerPaymentMethodsAdapter)ReflectionHelper.getField(mActivity, "mAdapter"));
            ReflectionHelper.setField(mActivity, "mAdapter", adapter);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        return adapter;
    }
}