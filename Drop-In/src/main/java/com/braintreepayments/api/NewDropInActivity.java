package com.braintreepayments.api;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.adapters.SupportedPaymentMethodsAdapter;
import com.braintreepayments.api.dropin.adapters.SupportedPaymentMethodsAdapter.PaymentMethodSelectedListener;
import com.braintreepayments.api.dropin.adapters.VaultedPaymentMethodsAdapter;
import com.braintreepayments.api.dropin.interfaces.AnimationFinishedListener;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.ConfigurationListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.interfaces.PaymentMethodNoncesUpdatedListener;
import com.braintreepayments.api.models.Authorization;
import com.braintreepayments.api.models.ClientToken;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.models.PaymentMethodNonce;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.view.animation.AnimationUtils.loadAnimation;
import static com.braintreepayments.api.BraintreePaymentActivity.BRAINTREE_RESULT_DEVELOPER_ERROR;
import static com.braintreepayments.api.BraintreePaymentActivity.EXTRA_ERROR_MESSAGE;

public class NewDropInActivity extends Activity implements ConfigurationListener, PaymentMethodSelectedListener,
        PaymentMethodNoncesUpdatedListener, PaymentMethodNonceCreatedListener {

    private static final int ADD_CARD_REQUEST_CODE = 1;

    public static final String EXTRA_PAYMENT_METHOD_NONCE = "com.braintreepayments.api.dropin.EXTRA_PAYMENT_METHOD_NONCE";
    private static final String EXTRA_SHEET_ANIMATION_PERFORMED = "com.braintreepayments.api.EXTRA_SHEET_ANIMATION_PERFORMED";

    @VisibleForTesting
    protected PaymentRequest mPaymentRequest;

    private View mBottomSheet;
    private BraintreeFragment mBraintreeFragment;
    private ViewSwitcher mViewSwitcher;
    private TextView mAvailablePaymentMethodsHeader;
    private ListView mAvailablePaymentMethodListView;
    private View mVaultedPaymentMethodsContainer;
    private RecyclerView mVaultedPaymentMethodsView;
    private AtomicBoolean mSheetAnimationPerformed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_new_dropin_payment_activity);

        mPaymentRequest = getIntent().getParcelableExtra(PaymentRequest.EXTRA_CHECKOUT_REQUEST);
        mBottomSheet = findViewById(R.id.bt_dropin_bottom_sheet);
        mViewSwitcher = (ViewSwitcher) findViewById(R.id.bt_loading_view_switcher);
        mAvailablePaymentMethodsHeader = (TextView) findViewById(R.id.bt_available_payment_methods_header);
        mAvailablePaymentMethodListView = (ListView) findViewById(R.id.bt_available_payment_methods);
        mVaultedPaymentMethodsContainer = findViewById(R.id.bt_vaulted_payment_methods_wrapper);
        mVaultedPaymentMethodsView = (RecyclerView) findViewById(R.id.bt_vaulted_payment_methods);
        mVaultedPaymentMethodsView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        if (savedInstanceState != null) {
            mSheetAnimationPerformed =
                    new AtomicBoolean(savedInstanceState.getBoolean(EXTRA_SHEET_ANIMATION_PERFORMED, false));
        } else {
            mSheetAnimationPerformed = new AtomicBoolean(false);
        }

        try {
            mBraintreeFragment = getBraintreeFragment();

            if (Authorization.fromString(mPaymentRequest.getAuthorization()) instanceof ClientToken
                    && !mBraintreeFragment.hasFetchedPaymentMethodNonces()) {
                PaymentMethod.getPaymentMethodNonces(mBraintreeFragment, true);
            } else if (mBraintreeFragment.hasFetchedPaymentMethodNonces()) {
                onPaymentMethodNoncesUpdated(mBraintreeFragment.getCachedPaymentMethodNonces());
            }
        } catch (InvalidArgumentException e) {
            Intent intent = new Intent()
                    .putExtra(EXTRA_ERROR_MESSAGE, e.getMessage());
            setResult(BRAINTREE_RESULT_DEVELOPER_ERROR, intent);
            finish();
            return;
        }

        if (mBraintreeFragment.getConfiguration() != null) {
            onConfigurationFetched(mBraintreeFragment.getConfiguration());
        }

        if (!mSheetAnimationPerformed.get()) {
            slideUp();
            mSheetAnimationPerformed.set(true);
        }
    }

    @VisibleForTesting
    protected BraintreeFragment getBraintreeFragment() throws InvalidArgumentException {
        if (TextUtils.isEmpty(mPaymentRequest.getAuthorization())) {
            throw new InvalidArgumentException("A client token or client key must be specified " +
                    "in the " + PaymentRequest.class.getSimpleName());
        }

        return BraintreeFragment.newInstance(this, mPaymentRequest.getAuthorization());
    }

    @Override
    public void onPaymentMethodNonceCreated(final PaymentMethodNonce paymentMethodNonce) {
        slideDown(new AnimationFinishedListener() {
            @Override
            public void onAnimationFinished() {
                Intent resultIntent = new Intent().putExtra(EXTRA_PAYMENT_METHOD_NONCE, paymentMethodNonce);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    @Override
    public void onConfigurationFetched(Configuration configuration) {
        mAvailablePaymentMethodListView.setAdapter(new SupportedPaymentMethodsAdapter(this, configuration, this));
    }

    @Override
    public void onPaymentMethodSelected(PaymentMethodType type) {
        switch (type) {
            case PAYPAL:
                PayPal.authorizeAccount(mBraintreeFragment);
                break;
            case ANDROID_PAY:
                AndroidPay.performMaskedWalletRequest(mBraintreeFragment, mPaymentRequest.getAndroidPayCart(),
                        mPaymentRequest.isAndroidPayShippingAddressRequired(),
                        mPaymentRequest.isAndroidPayPhoneNumberRequired(),
                        mPaymentRequest.getAndroidPayAllowedCountriesForShipping(),
                        AndroidPay.ANDROID_PAY_MASKED_WALLET_REQUEST_CODE);
                break;
            case PAY_WITH_VENMO:
                Venmo.authorizeAccount(mBraintreeFragment);
                break;
            case UNKNOWN:
                Intent intent = new Intent(this, AddCardActivity.class)
                        .putExtra(PaymentRequest.EXTRA_CHECKOUT_REQUEST, mPaymentRequest);
                startActivityForResult(intent, ADD_CARD_REQUEST_CODE);
                break;
        }
    }

    @Override
    public void onPaymentMethodNoncesUpdated(final List<PaymentMethodNonce> paymentMethodNonces) {
        if (paymentMethodNonces.size() > 0) {
            mAvailablePaymentMethodsHeader.setText(R.string.bt_other);
            mVaultedPaymentMethodsContainer.setVisibility(View.VISIBLE);
            mVaultedPaymentMethodsView.setAdapter(new VaultedPaymentMethodsAdapter(this, paymentMethodNonces));
            mViewSwitcher.setDisplayedChild(1);
        } else {
            mAvailablePaymentMethodsHeader.setText(R.string.bt_select_payment_method);
            mViewSwitcher.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_SHEET_ANIMATION_PERFORMED, mSheetAnimationPerformed.get());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == AndroidPay.ANDROID_PAY_MASKED_WALLET_REQUEST_CODE ||
                requestCode == AndroidPay.ANDROID_PAY_FULL_WALLET_REQUEST_CODE) && resultCode == RESULT_OK) {
            AndroidPay.onActivityResult(mBraintreeFragment, mPaymentRequest.getAndroidPayCart(), resultCode, data);
        } else if (requestCode == ADD_CARD_REQUEST_CODE && resultCode == RESULT_OK) {
            onPaymentMethodNonceCreated((PaymentMethodNonce) data.getParcelableExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE));
        }
    }

    @Override
    public void onBackPressed() {
        slideDown(new AnimationFinishedListener() {
            @Override
            public void onAnimationFinished() {
                finish();
            }
        });
    }

    public void onBackgroundClicked(View v) {
        onBackPressed();
    }

    private void slideUp() {
        mBottomSheet.startAnimation(loadAnimation(this, R.anim.bt_slide_in_up));
    }

    private void slideDown(final AnimationFinishedListener listener) {
        Animation slideOutAnimation = loadAnimation(this, R.anim.bt_slide_out_down);
        slideOutAnimation.setFillAfter(true);
        if (listener != null) {
            slideOutAnimation.setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    listener.onAnimationFinished();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
        mBottomSheet.startAnimation(slideOutAnimation);
    }
}
