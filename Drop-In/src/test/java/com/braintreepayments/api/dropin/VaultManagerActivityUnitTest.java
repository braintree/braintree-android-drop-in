package com.braintreepayments.api.dropin;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.ViewSwitcher;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.VaultManagerUnitTestActivity;
import com.braintreepayments.api.dropin.adapters.VaultManagerPaymentMethodsAdapter;
import com.braintreepayments.api.dropin.view.PaymentMethodItemView;
import com.braintreepayments.api.exceptions.PaymentMethodDeleteException;
import com.braintreepayments.api.interfaces.HttpResponseCallback;
import com.braintreepayments.api.internal.BraintreeGraphQLHttpClient;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.PayPalAccountNonce;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.test.UnitTestFixturesHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
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
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class VaultManagerActivityUnitTest {

    private PaymentMethodNonce mCardNonce;
    private Intent mIntent = new Intent();
    private List<PaymentMethodNonce> mPaymentMethodNonces = new ArrayList<>();
    private VaultManagerUnitTestActivity mActivity;
    private ActivityController<VaultManagerUnitTestActivity> mActivityController;
    private ShadowActivity mShadowActivity;
    private BraintreeGraphQLHttpClient mGraphQlClient;
    private PaymentMethodItemView mPaymentMethodItemView;

    @Before
    public void setup() {
        mGraphQlClient = mock(BraintreeGraphQLHttpClient.class);
        mCardNonce = mock(CardNonce.class);
        when(mCardNonce.getNonce()).thenReturn("card-nonce");

        PaymentMethodNonce payPalNonce = mock(PayPalAccountNonce.class);
        when(payPalNonce.getNonce()).thenReturn("paypal-nonce");

        mPaymentMethodNonces.add(mCardNonce);
        mPaymentMethodNonces.add(payPalNonce);

        DropInRequest dropInRequest = new DropInRequest()
                .clientToken(UnitTestFixturesHelper.stringFromFixture("client_token.json"));

        Bundle in = new Bundle();
        in.putParcelableArrayList(EXTRA_PAYMENT_METHOD_NONCES, (ArrayList<? extends Parcelable>) mPaymentMethodNonces);
        in.putParcelable(EXTRA_CHECKOUT_REQUEST, dropInRequest);
        mIntent.putExtras(in);

        mActivityController = Robolectric.buildActivity(VaultManagerUnitTestActivity.class, mIntent);
        mActivity = mActivityController.get();
        mShadowActivity = shadowOf(mActivity);

        mActivity.graphQLHttpClient = mGraphQlClient;
        mActivityController.setup();

        mPaymentMethodItemView = new PaymentMethodItemView(mActivity);
    }

    @Test
    public void onCreate_setsPaymentMethodNoncesInAdapter() {
        VaultManagerPaymentMethodsAdapter adapter = mActivity.mockAdapter();

        assertEquals(mPaymentMethodNonces.size(), adapter.getItemCount());
    }

    @Test
    public void onRestart_setsPaymentMethodNoncesInAdapter() {
        VaultManagerPaymentMethodsAdapter adapter = mActivity.mockAdapter();
        ArrayList<PaymentMethodNonce> nonces = new ArrayList<>();
        nonces.add(mCardNonce);
        adapter.setPaymentMethodNonces(nonces);

        mActivityController.pause();

        verify(adapter).setPaymentMethodNonces(nonces);
    }

    @Test
    public void onPaymentMethodNonceDeleted_sendsAnalyticCall() {
        BraintreeFragment fragment = mActivity.mockFragment();
        mActivity.onPaymentMethodNonceDeleted(mCardNonce);

        verify(fragment).sendAnalyticsEvent("manager.delete.succeeded");
    }

    @Test
    public void onPaymentMethodNonceDeleted_setsOkResult() {
        mActivity.onPaymentMethodNonceDeleted(mCardNonce);

        assertEquals(BaseActivity.RESULT_OK, mShadowActivity.getResultCode());
    }

    @Test
    public void onPaymentMethodNonceDelete_returnsAvailablePaymentMethods() {
         mActivity.onPaymentMethodNonceDeleted(mCardNonce);

        ArrayList<PaymentMethodNonce> availableNonces = mShadowActivity.getResultIntent()
                .getParcelableArrayListExtra("com.braintreepayments.api.EXTRA_PAYMENT_METHOD_NONCES");

        assertTrue(availableNonces.size() == 1);
        assertEquals("paypal-nonce", availableNonces.get(0).getNonce());
    }

    @Test
    public void onPaymentMethodNonceDeleted_removesLoadingView() {
        ((ViewSwitcher)mActivity.findViewById(R.id.bt_loading_view_switcher)).setDisplayedChild(1);
        mActivity.onPaymentMethodNonceDeleted(mCardNonce);

        assertEquals(0, ((ViewSwitcher)mActivity
                .findViewById(R.id.bt_loading_view_switcher)).getDisplayedChild());
    }

    @Test
    public void onError_whenPaymentMethodDeletedException_sendsAnalyticCall() {
        BraintreeFragment fragment = mActivity.mockFragment();
        Exception originalException = new RuntimeException("Real Exception");
        Exception error = new PaymentMethodDeleteException(mCardNonce, originalException);

        mActivity.onError(error);

        verify(fragment).sendAnalyticsEvent("manager.delete.failed");
    }

    @Test
    public void onError_whenPaymentMethodDeletedException_doesntDeleteThePaymentMethod() {
        VaultManagerPaymentMethodsAdapter adapter = mActivity.mockAdapter();
        Exception originalException = new RuntimeException("Real Exception");
        Exception error = new PaymentMethodDeleteException(mCardNonce, originalException);

        mActivity.onError(error);

        verifyZeroInteractions(adapter);
    }

    @Test
    public void onError_whenPaymentMethodDeletedException_removesLoadingView() {
        Exception originalException = new RuntimeException("Real Exception");
        Exception error = new PaymentMethodDeleteException(mCardNonce, originalException);

        ((ViewSwitcher)mActivity.findViewById(R.id.bt_loading_view_switcher)).setDisplayedChild(1);
        mActivity.onError(error);

        assertEquals(0, ((ViewSwitcher)mActivity
                .findViewById(R.id.bt_loading_view_switcher)).getDisplayedChild());
    }

    @Test
    public void onError_whenUnknownError_sendsAnalyticCall() {
        BraintreeFragment fragment = mActivity.mockFragment();
        mActivity.onError(new RuntimeException());

        verify(fragment).sendAnalyticsEvent("manager.unknown.failed");
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
    public void onClick_selectingPositiveButton_sendsAnalyticEvent() {
        BraintreeFragment fragment = mActivity.mockFragment();
        mockGraphQlResponse(true);

        mPaymentMethodItemView.setPaymentMethod(mCardNonce, false);
        mActivity.onClick(mPaymentMethodItemView);

        getDeleteConfirmationDialog().getButton(DialogInterface.BUTTON_POSITIVE).callOnClick();

        verify(fragment).sendAnalyticsEvent("manager.delete.confirmation.positive");
    }

    @Test
    public void onClick_selectingPositiveButton_callsDeletePaymentMethod() {
        VaultManagerPaymentMethodsAdapter adapter = mActivity.mockAdapter();
        mockGraphQlResponse(true);

        mPaymentMethodItemView.setPaymentMethod(mCardNonce, false);
        mActivity.onClick(mPaymentMethodItemView);

        getDeleteConfirmationDialog().getButton(DialogInterface.BUTTON_POSITIVE).callOnClick();

        verify(adapter).paymentMethodDeleted(eq(mCardNonce));
    }

    @Test
    public void onClick_selectingPositiveButton_showsLoadingView() {
        mockGraphQlResponseNotToCallback();

        mPaymentMethodItemView.setPaymentMethod(mCardNonce, false);
        mActivity.onClick(mPaymentMethodItemView);

        getDeleteConfirmationDialog().getButton(DialogInterface.BUTTON_POSITIVE).callOnClick();

        assertEquals(1, ((ViewSwitcher)mActivity.findViewById(R.id.bt_loading_view_switcher)).getDisplayedChild());
    }

    @Test
    public void onClick_selectingPositiveButton_doesNotSendNegativeAnalyticEvent() {
        BraintreeFragment fragment = mActivity.mockFragment();
        mockGraphQlResponse(true);

        mPaymentMethodItemView.setPaymentMethod(mCardNonce, false);
        mActivity.onClick(mPaymentMethodItemView);

        getDeleteConfirmationDialog().getButton(DialogInterface.BUTTON_POSITIVE).callOnClick();

        verify(fragment, times(0)).sendAnalyticsEvent("manager.delete.confirmation.negative");
    }

    @Test
    public void onClick_selectingNegativeButton_doesntDeletePaymentMethod() {
        VaultManagerPaymentMethodsAdapter adapter = mActivity.mockAdapter();

        mPaymentMethodItemView.setPaymentMethod(mCardNonce, false);
        mActivity.onClick(mPaymentMethodItemView);

        getDeleteConfirmationDialog().getButton(DialogInterface.BUTTON_NEGATIVE).callOnClick();

        verifyZeroInteractions(adapter);
    }

    @Test
    public void onClick_selectingNegativeButton_sendsAnalyticEvent() {
        BraintreeFragment fragment = mActivity.mockFragment();

        mPaymentMethodItemView.setPaymentMethod(mCardNonce, false);
        mActivity.onClick(mPaymentMethodItemView);

        getDeleteConfirmationDialog().getButton(DialogInterface.BUTTON_NEGATIVE).callOnClick();

        verify(fragment).sendAnalyticsEvent("manager.delete.confirmation.negative");
    }

    @Test
    public void onClick_dismissingDialog_doesntDeletePaymentMethod() {
        VaultManagerPaymentMethodsAdapter adapter = mActivity.mockAdapter();

        mPaymentMethodItemView.setPaymentMethod(mCardNonce, false);
        mActivity.onClick(mPaymentMethodItemView);

        getDeleteConfirmationDialog().dismiss();

        verifyZeroInteractions(adapter);
    }

    @Test
    public void onClick_dismissingDialog_sendsAnalyticEvent() {
        BraintreeFragment fragment = mActivity.mockFragment();
        mActivity.mockAdapter();

        mPaymentMethodItemView.setPaymentMethod(mCardNonce, false);
        mActivity.onClick(mPaymentMethodItemView);

        getDeleteConfirmationDialog().dismiss();

        verify(fragment).sendAnalyticsEvent("manager.delete.confirmation.negative");
    }

    @Test
    public void onBackPressed_leavesActivity() {
        mActivity.onBackPressed();

        assertTrue(mShadowActivity.isFinishing());
    }

    @Test
    public void onBackPressed_whileLoading_doesNotLeaveActivity() {
        ((ViewSwitcher)mActivity.findViewById(R.id.bt_loading_view_switcher)).setDisplayedChild(1);

        mActivity.onBackPressed();

        assertFalse(mShadowActivity.isFinishing());
    }

    private static android.support.v7.app.AlertDialog getDeleteConfirmationDialog() {
        // ShadowAlertDialog#getLatestDialog does not return
        // the support AlertDialog.
        //
        // This allows us to return the first (and only) v7 alert dialog
        return (android.support.v7.app.AlertDialog) ShadowAlertDialog.getShownDialogs().get(0);
    }

    private void mockGraphQlResponseNotToCallback() {
        doNothing().when(mGraphQlClient).post(anyString(), any(HttpResponseCallback.class));
    }

    private void mockGraphQlResponse(final boolean success) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HttpResponseCallback callback = (HttpResponseCallback) invocation.getArguments()[1];
                if (success) {
                    callback.success(null);
                } else {
                    callback.failure(null);
                }
                return null;
            }
        }).when(mGraphQlClient).post(anyString(), any(HttpResponseCallback.class));
    }
}