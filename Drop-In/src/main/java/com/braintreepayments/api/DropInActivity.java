package com.braintreepayments.api;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.ConfigurationListener;
import com.braintreepayments.api.models.Configuration;

public class DropInActivity extends BaseActivity implements ConfigurationListener {

    /**
     * Errors are returned as the serializable value of this key in the data intent in
     * {@link #onActivityResult(int, int, android.content.Intent)} if
     * responseCode is not {@link #RESULT_OK} or
     * {@link #RESULT_CANCELED}.
     */
    public static final String EXTRA_ERROR = "com.braintreepayments.api.dropin.EXTRA_ERROR";
    public static final int ADD_CARD_REQUEST_CODE = 1;
    public static final int DELETE_PAYMENT_METHOD_NONCE_CODE = 2;

    private static final String EXTRA_SHEET_SLIDE_UP_PERFORMED = "com.braintreepayments.api.EXTRA_SHEET_SLIDE_UP_PERFORMED";
    private static final String EXTRA_DEVICE_DATA = "com.braintreepayments.api.EXTRA_DEVICE_DATA";
    static final String EXTRA_PAYMENT_METHOD_NONCES = "com.braintreepayments.api.EXTRA_PAYMENT_METHOD_NONCES";

    private String mDeviceData;

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
//            mSheetSlideUpPerformed = savedInstanceState.getBoolean(EXTRA_SHEET_SLIDE_UP_PERFORMED, false);
            mDeviceData = savedInstanceState.getString(EXTRA_DEVICE_DATA);
        }
    }

    @Override
    public void onConfigurationFetched(Configuration configuration) {
        mConfiguration = configuration;
        showSelectPaymentMethodFragment();
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putBoolean(EXTRA_SHEET_SLIDE_UP_PERFORMED, mSheetSlideUpPerformed);
        outState.putString(EXTRA_DEVICE_DATA, mDeviceData);
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if (resultCode == RESULT_CANCELED) {
//            if (requestCode == ADD_CARD_REQUEST_CODE) {
//                mLoadingViewSwitcher.setDisplayedChild(0);
//
//                fetchPaymentMethodNonces(true);
//            }
//
//            mLoadingViewSwitcher.setDisplayedChild(1);
//        } else if (requestCode == ADD_CARD_REQUEST_CODE) {
//            final Intent response;
//            if (resultCode == RESULT_OK) {
//                mLoadingViewSwitcher.setDisplayedChild(0);
//
//                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
//                DropInResult.setLastUsedPaymentMethodType(this, result.getPaymentMethodNonce());
//
//                result.deviceData(mDeviceData);
//                response = new Intent()
//                        .putExtra(DropInResult.EXTRA_DROP_IN_RESULT, result);
//            } else {
//                response = data;
//            }
//
//            slideDown(new AnimationFinishedListener() {
//                @Override
//                public void onAnimationFinished() {
//                    setResult(resultCode, response);
//                    finish();
//                }
//            });
//        } else if (requestCode == DELETE_PAYMENT_METHOD_NONCE_CODE) {
//            if (resultCode == RESULT_OK) {
//                mLoadingViewSwitcher.setDisplayedChild(0);
//
//                if (data != null) {
//                    ArrayList<PaymentMethodNonce> paymentMethodNonces = data
//                            .getParcelableArrayListExtra(EXTRA_PAYMENT_METHOD_NONCES);
//
//                    if (paymentMethodNonces != null) {
//                        onPaymentMethodNoncesUpdated(paymentMethodNonces);
//                    }
//                }
//
//                fetchPaymentMethodNonces(true);
//            }
//            mLoadingViewSwitcher.setDisplayedChild(1);
//        }
    }

    public void onBackgroundClicked(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//        if (!mSheetSlideDownPerformed) {
//            mSheetSlideDownPerformed = true;
//            mBraintreeFragment.sendAnalyticsEvent("sdk.exit.canceled");
//
//            slideDown(new AnimationFinishedListener() {
//                @Override
//                public void onAnimationFinished() {
//                    finish();
//                }
//            });
//        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void onVaultEditButtonClick(View view) {
//        ArrayList<Parcelable> parcelableArrayList = new ArrayList<Parcelable>(mBraintreeFragment.getCachedPaymentMethodNonces());
//
//        Intent intent = new Intent(DropInActivity.this, VaultManagerActivity.class)
//                .putExtra(EXTRA_CHECKOUT_REQUEST, mDropInRequest)
//                .putParcelableArrayListExtra(EXTRA_PAYMENT_METHOD_NONCES, parcelableArrayList);
//        startActivityForResult(intent, DELETE_PAYMENT_METHOD_NONCE_CODE);
//
//        mBraintreeFragment.sendAnalyticsEvent("manager.appeared");
    }
}
