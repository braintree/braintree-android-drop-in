package com.braintreepayments.api;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.ViewSwitcher;

import com.braintreepayments.api.dropin.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;

import java.util.ArrayList;
import java.util.List;

import static androidx.appcompat.app.AppCompatActivity.RESULT_FIRST_USER;
import static androidx.appcompat.app.AppCompatActivity.RESULT_OK;
import static com.braintreepayments.api.DropInActivity.EXTRA_PAYMENT_METHOD_NONCES;
import static com.braintreepayments.api.DropInRequest.EXTRA_CHECKOUT_REQUEST;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class VaultManagerActivityUnitTest {

    private PaymentMethodNonce mCardNonce;
    private final Intent mIntent = new Intent();
    private final List<PaymentMethodNonce> mPaymentMethodNonces = new ArrayList<>();
    private VaultManagerUnitTestActivity mActivity;
    private ActivityController<VaultManagerUnitTestActivity> mActivityController;
    private ShadowActivity mShadowActivity;
    private PaymentMethodItemView mPaymentMethodItemView;

    @Before
    public void setup() {
        mCardNonce = mock(CardNonce.class);
        when(mCardNonce.getString()).thenReturn("card-nonce");

        PaymentMethodNonce payPalNonce = mock(PayPalAccountNonce.class);
        when(payPalNonce.getString()).thenReturn("paypal-nonce");

        mPaymentMethodNonces.add(mCardNonce);
        mPaymentMethodNonces.add(payPalNonce);

        DropInRequest dropInRequest = new DropInRequest()
                .clientToken(UnitTestFixturesHelper.base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN));

        Bundle in = new Bundle();
        in.putParcelableArrayList(EXTRA_PAYMENT_METHOD_NONCES, (ArrayList<? extends Parcelable>) mPaymentMethodNonces);
        in.putParcelable(EXTRA_CHECKOUT_REQUEST, dropInRequest);
        mIntent.putExtras(in);

        mActivityController = Robolectric.buildActivity(VaultManagerUnitTestActivity.class, mIntent);
        mActivity = mActivityController.get();
        mShadowActivity = shadowOf(mActivity);

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
        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        mActivity.dropInClient = dropInClient;
        mActivity.onPaymentMethodNonceDeleted(mCardNonce);

        verify(dropInClient).sendAnalyticsEvent("manager.delete.succeeded");
    }

    @Test
    public void onPaymentMethodNonceDeleted_setsOkResult() {
        mActivity.onPaymentMethodNonceDeleted(mCardNonce);

        assertEquals(RESULT_OK, mShadowActivity.getResultCode());
    }

    @Test
    public void onPaymentMethodNonceDelete_returnsAvailablePaymentMethods() {
         mActivity.onPaymentMethodNonceDeleted(mCardNonce);

        ArrayList<PaymentMethodNonce> availableNonces = mShadowActivity.getResultIntent()
                .getParcelableArrayListExtra("com.braintreepayments.api.EXTRA_PAYMENT_METHOD_NONCES");

        assertEquals(1, availableNonces.size());
        assertEquals("paypal-nonce", availableNonces.get(0).getString());
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
        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        mActivity.dropInClient = dropInClient;

        Exception originalException = new RuntimeException("Real Exception");
        Exception error = new PaymentMethodDeleteException(mCardNonce, originalException);

        mActivity.onError(error);

        verify(dropInClient).sendAnalyticsEvent("manager.delete.failed");
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
        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        mActivity.dropInClient = dropInClient;

        mActivity.onError(new RuntimeException());

        verify(dropInClient).sendAnalyticsEvent("manager.unknown.failed");
    }

    @Test
    public void onError_whenUnknownError_finishesWithError() {
        Exception expected = new RuntimeException();

        mActivity.onError(expected);

        assertTrue(mActivity.isFinishing());
        assertEquals(RESULT_FIRST_USER, mShadowActivity.getResultCode());
        Exception actual = (Exception) mShadowActivity.getResultIntent()
                .getSerializableExtra(DropInActivity.EXTRA_ERROR);

        assertEquals(expected, actual);
    }

    @Test
    public void onClick_selectingPositiveButton_sendsAnalyticEvent() {
        DropInClient dropInClient = new MockDropInClientBuilder()
                .deletePaymentMethodSuccess(mCardNonce)
                .build();
        mActivity.dropInClient = dropInClient;

        mPaymentMethodItemView.setPaymentMethod(mCardNonce, false);
        mActivity.onClick(mPaymentMethodItemView);

        getDeleteConfirmationDialog().getButton(DialogInterface.BUTTON_POSITIVE).callOnClick();

        verify(dropInClient).sendAnalyticsEvent("manager.delete.confirmation.positive");
    }

    @Test
    public void onClick_selectingPositiveButton_callsDeletePaymentMethod() {
        VaultManagerPaymentMethodsAdapter adapter = mActivity.mockAdapter();
        DropInClient dropInClient = new MockDropInClientBuilder()
                .deletePaymentMethodSuccess(mCardNonce)
                .build();
        mActivity.dropInClient = dropInClient;

        mPaymentMethodItemView.setPaymentMethod(mCardNonce, false);
        mActivity.onClick(mPaymentMethodItemView);

        getDeleteConfirmationDialog().getButton(DialogInterface.BUTTON_POSITIVE).callOnClick();

        verify(dropInClient).deletePaymentMethod(same(mActivity), same(mCardNonce), any(DeletePaymentMethodNonceCallback.class));
        verify(adapter).paymentMethodDeleted(same(mCardNonce));
    }

    @Test
    public void onClick_selectingPositiveButton_showsLoadingView() {
        mPaymentMethodItemView.setPaymentMethod(mCardNonce, false);
        mActivity.onClick(mPaymentMethodItemView);

        getDeleteConfirmationDialog().getButton(DialogInterface.BUTTON_POSITIVE).callOnClick();

        assertEquals(1, ((ViewSwitcher)mActivity.findViewById(R.id.bt_loading_view_switcher)).getDisplayedChild());
    }

    @Test
    public void onClick_selectingPositiveButton_doesNotSendNegativeAnalyticEvent() {
        DropInClient dropInClient = new MockDropInClientBuilder()
                .deletePaymentMethodSuccess(mCardNonce)
                .build();
        mActivity.dropInClient = dropInClient;

        mPaymentMethodItemView.setPaymentMethod(mCardNonce, false);
        mActivity.onClick(mPaymentMethodItemView);

        getDeleteConfirmationDialog().getButton(DialogInterface.BUTTON_POSITIVE).callOnClick();

        verify(dropInClient, never()).sendAnalyticsEvent("manager.delete.confirmation.negative");
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
        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        mActivity.dropInClient = dropInClient;

        mPaymentMethodItemView.setPaymentMethod(mCardNonce, false);
        mActivity.onClick(mPaymentMethodItemView);

        getDeleteConfirmationDialog().getButton(DialogInterface.BUTTON_NEGATIVE).callOnClick();

        verify(dropInClient).sendAnalyticsEvent("manager.delete.confirmation.negative");
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
        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        mActivity.dropInClient = dropInClient;
        mActivity.mockAdapter();

        mPaymentMethodItemView.setPaymentMethod(mCardNonce, false);
        mActivity.onClick(mPaymentMethodItemView);

        getDeleteConfirmationDialog().dismiss();

        verify(dropInClient).sendAnalyticsEvent("manager.delete.confirmation.negative");
    }

    @Test
    public void onBackPressed_leavesActivity() {
        mActivity.onBackPressed();

        assertTrue(mActivity.isFinishing());
    }

    @Test
    public void onBackPressed_whileLoading_doesNotLeaveActivity() {
        ((ViewSwitcher)mActivity.findViewById(R.id.bt_loading_view_switcher)).setDisplayedChild(1);

        mActivity.onBackPressed();

        assertFalse(mActivity.isFinishing());
    }

    @Test
    public void vaultManager_whenVaultedPaymentEdited_returnsToDropIn() {
//        String authorization = Fixtures.TOKENIZATION_KEY;
//        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);
//        setupDropInActivity(authorization, mock(DropInClient.class), dropInRequest, "sessionId");
//        mActivityController.setup();
//
//        PayPalAccountNonce paypalNonce = mock(PayPalAccountNonce.class);
//
//        ArrayList<Parcelable> paymentMethodNonces = new ArrayList<>();
//        paymentMethodNonces.add(paypalNonce);
//
//        assertEquals(0, ((ViewSwitcher) mActivity.findViewById(com.braintreepayments.api.dropin.R.id.bt_loading_view_switcher)).getDisplayedChild());
//
//        mActivity.onActivityResult(DELETE_PAYMENT_METHOD_NONCE_CODE, RESULT_OK, new Intent()
//                .putExtra("com.braintreepayments.api.EXTRA_PAYMENT_METHOD_NONCES", paymentMethodNonces));
//
//        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(com.braintreepayments.api.dropin.R.id.bt_loading_view_switcher)).getDisplayedChild());
    }

    @Test
    public void vaultManager_whenEditingFinished_removesLoadingIndicator() {
//        String authorization = Fixtures.TOKENIZATION_KEY;
//        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);
//
//        DropInClient dropInClient = new MockDropInClientBuilder()
//                .authorization(Authorization.fromString(authorization))
//                .getSupportedPaymentMethodsSuccess(new ArrayList<DropInPaymentMethodType>())
//                .build();
//        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
//        mActivityController.setup();
//
//        assertEquals(0, ((ViewSwitcher) mActivity.findViewById(com.braintreepayments.api.dropin.R.id.bt_loading_view_switcher)).getDisplayedChild());
//
//        mActivity.onActivityResult(2, RESULT_OK, null);
//
//        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(com.braintreepayments.api.dropin.R.id.bt_loading_view_switcher)).getDisplayedChild());
    }

    private static androidx.appcompat.app.AlertDialog getDeleteConfirmationDialog() {
        // ShadowAlertDialog#getLatestDialog does not return
        // the support AlertDialog.
        //
        // This allows us to return the first (and only) v7 alert dialog
        return (androidx.appcompat.app.AlertDialog) ShadowAlertDialog.getShownDialogs().get(0);
    }
}