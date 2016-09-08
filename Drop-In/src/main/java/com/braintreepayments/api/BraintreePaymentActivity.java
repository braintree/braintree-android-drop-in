package com.braintreepayments.api;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
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
import com.braintreepayments.api.exceptions.AuthenticationException;
import com.braintreepayments.api.exceptions.AuthorizationException;
import com.braintreepayments.api.exceptions.ConfigurationException;
import com.braintreepayments.api.exceptions.DownForMaintenanceException;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.exceptions.ServerException;
import com.braintreepayments.api.exceptions.UnexpectedException;
import com.braintreepayments.api.exceptions.UpgradeRequiredException;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
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

public class BraintreePaymentActivity extends Activity implements ConfigurationListener, BraintreeCancelListener,
        BraintreeErrorListener, PaymentMethodSelectedListener, PaymentMethodNoncesUpdatedListener,
        PaymentMethodNonceCreatedListener {

    /**
     * {@link PaymentMethodNonce} returned by successfully exiting the flow.
     */
    public static final String EXTRA_PAYMENT_METHOD_NONCE =
            "com.braintreepayments.api.dropin.EXTRA_PAYMENT_METHOD_NONCE";

    /**
     * {@link String} returned when specified in {@link PaymentRequest} that device data should be
     * collected.
     */
    public static final String EXTRA_DEVICE_DATA =
            "com.braintreepayments.api.dropin.EXTRA_DEVICE_DATA";

    /**
     * Error messages are returned as the value of this key in the data intent in {@link
     * android.app.Activity#onActivityResult(int, int, android.content.Intent)} if {@code
     * responseCode} is not {@link android.app.Activity#RESULT_OK} or {@link
     * android.app.Activity#RESULT_CANCELED}
     */
    public static final String EXTRA_ERROR_MESSAGE =
            "com.braintreepayments.api.dropin.EXTRA_ERROR_MESSAGE";

    /**
     * The payment method flow halted due to a resolvable error (authentication, authorization, SDK
     * upgrade required).
     */
    public static final int BRAINTREE_RESULT_DEVELOPER_ERROR = 2;

    /**
     * The payment method flow halted due to an error from the Braintree gateway. The best recovery
     * path is to try again with a new authorization.
     */
    public static final int BRAINTREE_RESULT_SERVER_ERROR = 3;

    /**
     * The payment method flow halted due to the Braintree gateway going down for maintenance. Try
     * again later.
     */
    public static final int BRAINTREE_RESULT_SERVER_UNAVAILABLE = 4;

    private static final int ADD_CARD_REQUEST_CODE = 1;
    private static final String EXTRA_SHEET_ANIMATION_PERFORMED = "com.braintreepayments.api.EXTRA_SHEET_ANIMATION_PERFORMED";

    @VisibleForTesting
    protected PaymentRequest mPaymentRequest;

    private View mBottomSheet;
    private ViewSwitcher mLoadingViewSwitcher;
    private BraintreeFragment mBraintreeFragment;
    private ViewSwitcher mVaultedPaymentMethodsViewSwitcher;
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
        mLoadingViewSwitcher = (ViewSwitcher) findViewById(R.id.bt_loading_view_switcher);
        mVaultedPaymentMethodsViewSwitcher = (ViewSwitcher) findViewById(R.id.bt_vaulted_payment_methods_view_switcher);
        mAvailablePaymentMethodsHeader = (TextView) findViewById(R.id.bt_available_payment_methods_header);
        mAvailablePaymentMethodListView = (ListView) findViewById(R.id.bt_available_payment_methods);
        mVaultedPaymentMethodsContainer = findViewById(R.id.bt_vaulted_payment_methods_wrapper);
        mVaultedPaymentMethodsView = (RecyclerView) findViewById(R.id.bt_vaulted_payment_methods);
        mVaultedPaymentMethodsView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        new LinearSnapHelper().attachToRecyclerView(mVaultedPaymentMethodsView);

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

        mSheetAnimationPerformed = new AtomicBoolean(false);
        if (savedInstanceState != null) {
            mSheetAnimationPerformed = new AtomicBoolean(savedInstanceState
                    .getBoolean(EXTRA_SHEET_ANIMATION_PERFORMED, false));
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
    public void onConfigurationFetched(Configuration configuration) {
        mAvailablePaymentMethodListView.setAdapter(new SupportedPaymentMethodsAdapter(this, configuration, this));
        mLoadingViewSwitcher.setDisplayedChild(1);
    }

    @Override
    public void onCancel(int requestCode) {
        mLoadingViewSwitcher.setDisplayedChild(1);
    }

    @Override
    public void onError(final Exception error) {
        slideDown(new AnimationFinishedListener() {
            @Override
            public void onAnimationFinished() {
                if (error instanceof AuthenticationException || error instanceof AuthorizationException ||
                        error instanceof UpgradeRequiredException) {
                    mBraintreeFragment.sendAnalyticsEvent("sdk.exit.developer-error");
                    setResult(BRAINTREE_RESULT_DEVELOPER_ERROR, new Intent().putExtra(EXTRA_ERROR_MESSAGE, error));
                } else if (error instanceof ConfigurationException) {
                    mBraintreeFragment.sendAnalyticsEvent("sdk.exit.configuration-exception");
                    setResult(BRAINTREE_RESULT_SERVER_ERROR, new Intent().putExtra(EXTRA_ERROR_MESSAGE, error));
                } else if (error instanceof ServerException || error instanceof UnexpectedException) {
                    mBraintreeFragment.sendAnalyticsEvent("sdk.exit.server-error");
                    setResult(BRAINTREE_RESULT_SERVER_ERROR, new Intent().putExtra(EXTRA_ERROR_MESSAGE, error));
                } else if (error instanceof DownForMaintenanceException) {
                    mBraintreeFragment.sendAnalyticsEvent("sdk.exit.server-unavailable");
                    setResult(BRAINTREE_RESULT_SERVER_UNAVAILABLE, new Intent().putExtra(EXTRA_ERROR_MESSAGE, error));
                }

                finish();
            }
        });
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
    public void onPaymentMethodSelected(PaymentMethodType type) {
        mLoadingViewSwitcher.setDisplayedChild(0);

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
            mVaultedPaymentMethodsViewSwitcher.setDisplayedChild(1);
        } else {
            mAvailablePaymentMethodsHeader.setText(R.string.bt_select_payment_method);
            mVaultedPaymentMethodsViewSwitcher.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_SHEET_ANIMATION_PERFORMED, mSheetAnimationPerformed.get());
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            mLoadingViewSwitcher.setDisplayedChild(1);
        } else if ((requestCode == AndroidPay.ANDROID_PAY_MASKED_WALLET_REQUEST_CODE ||
                requestCode == AndroidPay.ANDROID_PAY_FULL_WALLET_REQUEST_CODE) && resultCode == RESULT_OK) {
            AndroidPay.onActivityResult(mBraintreeFragment, mPaymentRequest.getAndroidPayCart(), resultCode, data);
        } else if (requestCode == ADD_CARD_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                onPaymentMethodNonceCreated((PaymentMethodNonce) data
                        .getParcelableExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE));
            } else {
                slideDown(new AnimationFinishedListener() {
                    @Override
                    public void onAnimationFinished() {
                        setResult(resultCode, data);
                        finish();
                    }
                });
            }
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
