package com.braintreepayments.api;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.exceptions.AuthenticationException;
import com.braintreepayments.api.exceptions.AuthorizationException;
import com.braintreepayments.api.exceptions.ConfigurationException;
import com.braintreepayments.api.exceptions.DownForMaintenanceException;
import com.braintreepayments.api.exceptions.GoogleApiClientException;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.exceptions.ServerException;
import com.braintreepayments.api.exceptions.UnexpectedException;
import com.braintreepayments.api.exceptions.UpgradeRequiredException;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.BraintreeResponseListener;
import com.braintreepayments.api.interfaces.ConfigurationListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.interfaces.PaymentMethodNoncesUpdatedListener;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.ClientToken;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.models.GooglePaymentCardNonce;
import com.braintreepayments.api.models.PayPalRequest;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.models.ThreeDSecureRequest;

import java.util.List;

import static com.braintreepayments.api.DropInRequest.EXTRA_CHECKOUT_REQUEST;

public class DropInActivity extends BaseActivity implements ConfigurationListener, PaymentMethodNonceCreatedListener, PaymentMethodNoncesUpdatedListener, BraintreeErrorListener {

    /**
     * Errors are returned as the serializable value of this key in the data intent in
     * {@link #onActivityResult(int, int, android.content.Intent)} if
     * responseCode is not {@link #RESULT_OK} or
     * {@link #RESULT_CANCELED}.
     */
    public static final String EXTRA_ERROR = "com.braintreepayments.api.dropin.EXTRA_ERROR";
    public static final int ADD_CARD_REQUEST_CODE = 1;
    public static final int DELETE_PAYMENT_METHOD_NONCE_CODE = 2;

    private static final String EXTRA_DEVICE_DATA = "com.braintreepayments.api.EXTRA_DEVICE_DATA";
    static final String EXTRA_PAYMENT_METHOD_NONCES = "com.braintreepayments.api.EXTRA_PAYMENT_METHOD_NONCES";

    private String mDeviceData;

    private DropInViewModel viewModel;
    private boolean isClientTokenPresent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_drop_in_activity);

        try {
            mBraintreeFragment = getBraintreeFragment();
        } catch (InvalidArgumentException e) {
            finish(e);
            return;
        }

        if (savedInstanceState != null) {
            mDeviceData = savedInstanceState.getString(EXTRA_DEVICE_DATA);
        }

        isClientTokenPresent = mBraintreeFragment.getAuthorization() instanceof ClientToken;

        DropInViewModelFactory viewModelFactory =
            new DropInViewModelFactory(this, mDropInRequest);
        viewModel = new ViewModelProvider(this, viewModelFactory).get(DropInViewModel.class);
    }

    @Override
    public void onConfigurationFetched(Configuration configuration) {
        mConfiguration = configuration;
        showSelectPaymentMethodFragment();

        if (mDropInRequest.isGooglePaymentEnabled()) {
            GooglePayment.isReadyToPay(mBraintreeFragment, new BraintreeResponseListener<Boolean>() {
                @Override
                public void onResponse(Boolean isReadyToPay) {
                    viewModel.updateAvailablePaymentMethods(DropInActivity.this, mConfiguration, mDropInRequest, isReadyToPay, isClientTokenPresent);
                }
            });
        } else {
            viewModel.updateAvailablePaymentMethods(DropInActivity.this, mConfiguration, mDropInRequest, false, isClientTokenPresent);
        }
    }

    private void showSelectPaymentMethodFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag("SELECT_PAYMENT_METHOD");
        if (fragment == null) {
            Bundle args = new Bundle();
            args.putParcelable("EXTRA_DROP_IN_REQUEST", mDropInRequest);
            args.putString("EXTRA_CONFIGURATION", mConfiguration.toJson());

            fragmentManager
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container_view, SelectPaymentMethodFragment.class, args, "SELECT_PAYMENT_METHOD")
                    .commit();
        }
    }

    @Override
    public void onPaymentMethodNoncesUpdated(List<PaymentMethodNonce> paymentMethodNonces) {
        final List<PaymentMethodNonce> noncesRef = paymentMethodNonces;
        if (paymentMethodNonces.size() > 0) {
            viewModel.setVaultedPaymentMethodNonces(paymentMethodNonces);
            if (mDropInRequest.isGooglePaymentEnabled()) {
                GooglePayment.isReadyToPay(mBraintreeFragment, new BraintreeResponseListener<Boolean>() {
                    @Override
                    public void onResponse(Boolean isReadyToPay) {
//                        showVaultedPaymentMethods(noncesRef, isReadyToPay);
                    }
                });
            } else {
//                showVaultedPaymentMethods(paymentMethodNonces, false);
            }
        } else {
//            mSupportedPaymentMethodsHeader.setText(R.string.bt_select_payment_method);
//            mVaultedPaymentMethodsContainer.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_DEVICE_DATA, mDeviceData);
    }

    public void onPaymentMethodSelected(PaymentMethodType type) {
        switch (type) {
            case PAYPAL:
                PayPalRequest paypalRequest = mDropInRequest.getPayPalRequest();
                if (paypalRequest == null) {
                    paypalRequest = new PayPalRequest();
                }
                if (paypalRequest.getAmount() != null) {
                    PayPal.requestOneTimePayment(mBraintreeFragment, paypalRequest);
                } else {
                    PayPal.requestBillingAgreement(mBraintreeFragment, paypalRequest);
                }
                break;
            case GOOGLE_PAYMENT:
                GooglePayment.requestPayment(mBraintreeFragment, mDropInRequest.getGooglePaymentRequest());
                break;
            case PAY_WITH_VENMO:
                Venmo.authorizeAccount(mBraintreeFragment, mDropInRequest.shouldVaultVenmo());
                break;
            case UNKNOWN:
                Intent intent = new Intent(this, AddCardActivity.class)
                        .putExtra(EXTRA_CHECKOUT_REQUEST, mDropInRequest);
                startActivityForResult(intent, ADD_CARD_REQUEST_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED) {
            if (requestCode == ADD_CARD_REQUEST_CODE) {
                viewModel.setIsLoading(false);
            }

        } else if (requestCode == ADD_CARD_REQUEST_CODE) {
            final Intent response;
            if (resultCode == RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                DropInResult.setLastUsedPaymentMethodType(this, result.getPaymentMethodNonce());

                result.deviceData(mDeviceData);
                response = new Intent()
                        .putExtra(DropInResult.EXTRA_DROP_IN_RESULT, result);
            } else {
                response = data;
            }

            setResult(resultCode, response);
            finish();
        } else if (requestCode == DELETE_PAYMENT_METHOD_NONCE_CODE) {
            if (resultCode == RESULT_OK) {
                // TODO: Remove deleted nonce from the view
            }
        }
    }

    public void onBackgroundClicked(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        mBraintreeFragment.sendAnalyticsEvent("sdk.exit.canceled");
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.bt_activity_fade_in, R.anim.bt_activity_fade_out);
    }

    private boolean paymentMethodCanPerformThreeDSecureVerification(final PaymentMethodNonce paymentMethodNonce) {
        if (paymentMethodNonce instanceof CardNonce) {
            return true;
        }

        if (paymentMethodNonce instanceof GooglePaymentCardNonce) {
            return ((GooglePaymentCardNonce) paymentMethodNonce).isNetworkTokenized() == false;
        }

        return false;
    }

    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
        finish(paymentMethodNonce, mDeviceData);
    }

    @Override
    public void onError(Exception error) {
//        handleThreeDSecureFailure();

        if (error instanceof GoogleApiClientException) {
//            showSupportedPaymentMethods();
            viewModel.updateAvailablePaymentMethods(DropInActivity.this, mConfiguration, mDropInRequest, false, isClientTokenPresent);
            return;
        }

        if (error instanceof AuthenticationException || error instanceof AuthorizationException ||
                error instanceof UpgradeRequiredException) {
            mBraintreeFragment.sendAnalyticsEvent("sdk.exit.developer-error");
        } else if (error instanceof ConfigurationException) {
            mBraintreeFragment.sendAnalyticsEvent("sdk.exit.configuration-exception");
        } else if (error instanceof ServerException || error instanceof UnexpectedException) {
            mBraintreeFragment.sendAnalyticsEvent("sdk.exit.server-error");
        } else if (error instanceof DownForMaintenanceException) {
            mBraintreeFragment.sendAnalyticsEvent("sdk.exit.server-unavailable");
        } else {
            mBraintreeFragment.sendAnalyticsEvent("sdk.exit.sdk-error");
        }

        finish(error);
    }
}
