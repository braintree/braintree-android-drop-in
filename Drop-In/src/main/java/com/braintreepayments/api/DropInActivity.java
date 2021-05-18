package com.braintreepayments.api;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.braintreepayments.api.SupportedPaymentMethodsAdapter.PaymentMethodSelectedListener;
import com.braintreepayments.api.dropin.R;

import java.util.ArrayList;
import java.util.List;

import static android.view.animation.AnimationUtils.loadAnimation;
import static com.braintreepayments.api.DropInRequest.EXTRA_CHECKOUT_REQUEST;

public class DropInActivity extends BaseActivity implements PaymentMethodSelectedListener, VaultedPaymentMethodSelectedListener {

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

    private View mBottomSheet;
    private ViewSwitcher mLoadingViewSwitcher;
    private TextView mSupportedPaymentMethodsHeader;
    @VisibleForTesting
    protected ListView mSupportedPaymentMethodListView;
    private View mVaultedPaymentMethodsContainer;
    private RecyclerView mVaultedPaymentMethodsView;
    private Button mVaultManagerButton;

    private boolean mSheetSlideUpPerformed;
    private boolean mSheetSlideDownPerformed;
    private boolean mPerformedThreeDSecureVerification;

    private DropInClient dropInClient;

//    private BraintreeClient braintreeClient;
//    private PayPalClient payPalClient;
//    private GooglePayClient googlePayClient;
//    private VenmoClient venmoClient;
//    private ThreeDSecureClient threeDSecureClient;
//    private PaymentMethodClient paymentMethodClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_drop_in_activity);

        // TODO: create drop in client using bundle extras
        mBottomSheet = findViewById(R.id.bt_dropin_bottom_sheet);
        mLoadingViewSwitcher = findViewById(R.id.bt_loading_view_switcher);
        mSupportedPaymentMethodsHeader = findViewById(R.id.bt_supported_payment_methods_header);
        mSupportedPaymentMethodListView = findViewById(R.id.bt_supported_payment_methods);
        mVaultedPaymentMethodsContainer = findViewById(R.id.bt_vaulted_payment_methods_wrapper);
        mVaultedPaymentMethodsView = findViewById(R.id.bt_vaulted_payment_methods);
        mVaultManagerButton = findViewById(R.id.bt_vault_edit_button);
        mVaultedPaymentMethodsView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        new LinearSnapHelper().attachToRecyclerView(mVaultedPaymentMethodsView);

        Intent intent = getIntent();
        String authorization = intent.getStringExtra(DropInClient.EXTRA_AUTHORIZATION);
        String sessionId = intent.getStringExtra(DropInClient.EXTRA_SESSION_ID);
        DropInRequest dropInRequest = getIntent().getParcelableExtra(DropInRequest.EXTRA_CHECKOUT_REQUEST);

        dropInClient = new DropInClient(this, authorization, sessionId, dropInRequest);

//        braintreeClient = getBraintreeClient();
//        payPalClient = new PayPalClient(getBraintreeClient());
//        googlePayClient = new GooglePayClient(getBraintreeClient());
//        venmoClient = new VenmoClient(getBraintreeClient());
//        threeDSecureClient = new ThreeDSecureClient(getBraintreeClient());
//        paymentMethodClient = new PaymentMethodClient(getBraintreeClient());

        if (savedInstanceState != null) {
            mSheetSlideUpPerformed = savedInstanceState.getBoolean(EXTRA_SHEET_SLIDE_UP_PERFORMED,
                    false);
            mDeviceData = savedInstanceState.getString(EXTRA_DEVICE_DATA);
        }

        slideUp();

        dropInClient.getConfiguration(new ConfigurationCallback() {
            @Override
            public void onResult(@Nullable Configuration configuration, @Nullable Exception error) {
                onConfigurationFetched(configuration);
            }
        });
    }

    public void onConfigurationFetched(Configuration configuration) {
        mConfiguration = configuration;

        if (mDropInRequest.shouldCollectDeviceData() && TextUtils.isEmpty(mDeviceData)) {
            DataCollector dataCollector = new DataCollector(getBraintreeClient());
            dataCollector.collectDeviceData(this, new DataCollectorCallback() {
                @Override
                public void onResult(@Nullable String deviceData, @Nullable Exception error) {
                    mDeviceData = deviceData;
                }
            });
        }

        dropInClient.getSupportedPaymentMethods(this, new GetSupportedPaymentMethodsCallback() {
            @Override
            public void onResult(@Nullable List<PaymentMethodType> paymentMethods, @Nullable Exception error) {
                if (paymentMethods != null) {
                    showSupportedPaymentMethods(paymentMethods);
                } else {
                    // TODO: handle error
                }
            }
        });
    }

    private void showSupportedPaymentMethods(List<PaymentMethodType> supportedPaymentMethods) {
        SupportedPaymentMethodsAdapter adapter =
            new SupportedPaymentMethodsAdapter(supportedPaymentMethods, this);
        mSupportedPaymentMethodListView.setAdapter(adapter);
        mLoadingViewSwitcher.setDisplayedChild(1);
        fetchPaymentMethodNonces(false);
    }

    private void handleThreeDSecureFailure() {
        if (mPerformedThreeDSecureVerification) {
            mPerformedThreeDSecureVerification = false;
            fetchPaymentMethodNonces(true);
        }
    }

    public void onCancel(int requestCode) {
        handleThreeDSecureFailure();

        mLoadingViewSwitcher.setDisplayedChild(1);
    }

    public void onError(final Exception error) {
        handleThreeDSecureFailure();

        slideDown(new AnimationFinishedListener() {
            @Override
            public void onAnimationFinished() {
                if (error instanceof AuthenticationException || error instanceof AuthorizationException ||
                        error instanceof UpgradeRequiredException) {
                    getBraintreeClient().sendAnalyticsEvent("sdk.exit.developer-error");
                } else if (error instanceof ConfigurationException) {
                    getBraintreeClient().sendAnalyticsEvent("sdk.exit.configuration-exception");
                } else if (error instanceof ServerException || error instanceof UnexpectedException) {
                    getBraintreeClient().sendAnalyticsEvent("sdk.exit.server-error");
                } else if (error instanceof ServiceUnavailableException) {
                    getBraintreeClient().sendAnalyticsEvent("sdk.exit.server-unavailable");
                } else {
                    getBraintreeClient().sendAnalyticsEvent("sdk.exit.sdk-error");
                }

                finish(error);
            }
        });
    }

    public void onPaymentMethodNonceCreated(final PaymentMethodNonce paymentMethodNonce) {
        if (!mPerformedThreeDSecureVerification) {
            dropInClient.shouldRequestThreeDSecureVerification(paymentMethodNonce, new ShouldRequestThreeDSecureVerification() {
                @Override
                public void onResult(boolean shouldRequestThreeDSecureVerification) {
                    if (shouldRequestThreeDSecureVerification) {
                        mPerformedThreeDSecureVerification = true;
                        mLoadingViewSwitcher.setDisplayedChild(0);

                        dropInClient.performThreeDSecureVerification(DropInActivity.this, paymentMethodNonce, new ThreeDSecureResultCallback() {

                            @Override
                            public void onResult(@Nullable ThreeDSecureResult threeDSecureResult, @Nullable Exception error) {
                                onPaymentMethodNonceCreated(threeDSecureResult.getTokenizedCard());
                            }
                        });
                    } else {
                        finishWithPaymentMethodNonce(paymentMethodNonce);
                    }
                }
            });
            return;
        }
        finishWithPaymentMethodNonce(paymentMethodNonce);
    }

    private void finishWithPaymentMethodNonce(final PaymentMethodNonce paymentMethodNonce) {

        slideDown(new AnimationFinishedListener() {
            @Override
            public void onAnimationFinished() {
                getBraintreeClient().sendAnalyticsEvent("sdk.exit.success");

                DropInResult.setLastUsedPaymentMethodType(DropInActivity.this, paymentMethodNonce);

                finish(paymentMethodNonce, mDeviceData);
            }
        });
    }

    @Override
    public void onPaymentMethodSelected(PaymentMethodType type) {
        mLoadingViewSwitcher.setDisplayedChild(0);

        switch (type) {
            case PAYPAL:
                dropInClient.tokenizePayPalRequest(this, new PayPalFlowStartedCallback() {
                    @Override
                    public void onResult(@Nullable Exception error) {
                        onError(error);
                    }
                });
                break;
            case GOOGLE_PAYMENT:
                dropInClient.requestGooglePayPayment(this, new GooglePayRequestPaymentCallback() {
                    @Override
                    public void onResult(Exception error) {
                        onError(error);
                    }
                });
                break;
            case PAY_WITH_VENMO:
                dropInClient.tokenizeVenmoAccount(this, new VenmoTokenizeAccountCallback() {
                    @Override
                    public void onResult(@Nullable Exception error) {
                        onError(error);
                    }
                });
                break;
            case UNKNOWN:
                Intent intent = new Intent(this, AddCardActivity.class)
                        .putExtra(EXTRA_CHECKOUT_REQUEST, mDropInRequest);
                startActivityForResult(intent, ADD_CARD_REQUEST_CODE);
                break;
        }
    }

    private void fetchPaymentMethodNonces(final boolean refetch) {
        if (mClientTokenPresent) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!DropInActivity.this.isFinishing()) {

                        dropInClient.getVaultedPaymentMethods(DropInActivity.this, refetch, new GetPaymentMethodNoncesCallback() {
                            @Override
                            public void onResult(@Nullable List<PaymentMethodNonce> paymentMethodNonces, @Nullable Exception error) {
                                showVaultedPaymentMethods(paymentMethodNonces);
                            }
                        });
                    }
                }
            }, getResources().getInteger(android.R.integer.config_shortAnimTime));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_SHEET_SLIDE_UP_PERFORMED, mSheetSlideUpPerformed);
        outState.putString(EXTRA_DEVICE_DATA, mDeviceData);
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED) {
            if (requestCode == ADD_CARD_REQUEST_CODE) {
                mLoadingViewSwitcher.setDisplayedChild(0);

                fetchPaymentMethodNonces(true);
            }

            mLoadingViewSwitcher.setDisplayedChild(1);
        } else if (requestCode == ADD_CARD_REQUEST_CODE) {
            final Intent response;
            if (resultCode == RESULT_OK) {
                mLoadingViewSwitcher.setDisplayedChild(0);

                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                DropInResult.setLastUsedPaymentMethodType(this, result.getPaymentMethodNonce());

                result.deviceData(mDeviceData);
                response = new Intent()
                        .putExtra(DropInResult.EXTRA_DROP_IN_RESULT, result);
            } else {
                response = data;
            }

            slideDown(new AnimationFinishedListener() {
                @Override
                public void onAnimationFinished() {
                    setResult(resultCode, response);
                    finish();
                }
            });
        } else if (requestCode == DELETE_PAYMENT_METHOD_NONCE_CODE) {
            if (resultCode == RESULT_OK) {
                mLoadingViewSwitcher.setDisplayedChild(0);

                if (data != null) {
                    ArrayList<PaymentMethodNonce> paymentMethodNonces = data
                            .getParcelableArrayListExtra(EXTRA_PAYMENT_METHOD_NONCES);

                    if (paymentMethodNonces != null) {
                        showVaultedPaymentMethods(paymentMethodNonces);
                    }
                }

                fetchPaymentMethodNonces(true);
            }
            mLoadingViewSwitcher.setDisplayedChild(1);
        }
    }

    public void onBackgroundClicked(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (!mSheetSlideDownPerformed) {
            mSheetSlideDownPerformed = true;
            getBraintreeClient().sendAnalyticsEvent("sdk.exit.canceled");

            slideDown(new AnimationFinishedListener() {
                @Override
                public void onAnimationFinished() {
                    finish();
                }
            });
        }
    }

    private void slideUp() {
        if (!mSheetSlideUpPerformed) {
            getBraintreeClient().sendAnalyticsEvent("appeared");

            mSheetSlideUpPerformed = true;
            mBottomSheet.startAnimation(loadAnimation(this, R.anim.bt_slide_in_up));
        }
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
                public void onAnimationRepeat(Animation animation) {}
            });
        }
        mBottomSheet.startAnimation(slideOutAnimation);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void onVaultEditButtonClick(View view) {
        // TODO: consider caching nonces
        dropInClient.getVaultedPaymentMethods(this, false, new GetPaymentMethodNoncesCallback() {
            @Override
            public void onResult(@Nullable List<PaymentMethodNonce> paymentMethodNonceList, @Nullable Exception error) {
                Intent intent = new Intent(DropInActivity.this, VaultManagerActivity.class)
                        .putExtra(EXTRA_CHECKOUT_REQUEST, mDropInRequest)
                        .putParcelableArrayListExtra(EXTRA_PAYMENT_METHOD_NONCES, new ArrayList<Parcelable>(paymentMethodNonceList));
                startActivityForResult(intent, DELETE_PAYMENT_METHOD_NONCE_CODE);

                getBraintreeClient().sendAnalyticsEvent("manager.appeared");
            }
        });
    }

    private void showVaultedPaymentMethods(List<PaymentMethodNonce> paymentMethodNonces) {
        if (paymentMethodNonces.size() > 0) {
            mSupportedPaymentMethodsHeader.setText(R.string.bt_other);
            mVaultedPaymentMethodsContainer.setVisibility(View.VISIBLE);

            VaultedPaymentMethodsAdapter vaultedPaymentMethodsAdapter
                    = new VaultedPaymentMethodsAdapter(paymentMethodNonces, this);
            mVaultedPaymentMethodsView.setAdapter(vaultedPaymentMethodsAdapter);

            if (mDropInRequest.isVaultManagerEnabled()) {
                mVaultManagerButton.setVisibility(View.VISIBLE);
            }

            if (vaultedPaymentMethodsAdapter.hasCardNonce()) {
                getBraintreeClient().sendAnalyticsEvent("vaulted-card.appear");
            }

        } else {
            mSupportedPaymentMethodsHeader.setText(R.string.bt_select_payment_method);
            mVaultedPaymentMethodsContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onVaultedPaymentMethodSelected(PaymentMethodNonce paymentMethodNonce) {
        if (paymentMethodNonce instanceof CardNonce) {
            getBraintreeClient().sendAnalyticsEvent("vaulted-card.select");
        }

        DropInActivity.this.onPaymentMethodNonceCreated(paymentMethodNonce);
    }
}
