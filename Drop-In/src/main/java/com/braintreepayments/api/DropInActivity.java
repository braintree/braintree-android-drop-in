package com.braintreepayments.api;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.braintreepayments.api.dropin.R;

import java.util.ArrayList;
import java.util.List;

import static com.braintreepayments.api.DropInClient.EXTRA_AUTHORIZATION;
import static com.braintreepayments.api.DropInClient.EXTRA_SESSION_ID;
import static com.braintreepayments.api.DropInRequest.EXTRA_CHECKOUT_REQUEST;

public class DropInActivity extends BaseActivity implements SupportedPaymentMethodSelectedListener, VaultedPaymentMethodSelectedListener {

    /**
     * Errors are returned as the serializable value of this key in the data intent in
     * {@link #onActivityResult(int, int, android.content.Intent)} if
     * responseCode is not {@link #RESULT_OK} or
     * {@link #RESULT_CANCELED}.
     */
    public static final String EXTRA_ERROR = "com.braintreepayments.api.dropin.EXTRA_ERROR";
    public static final int ADD_CARD_REQUEST_CODE = 1;
    public static final int DELETE_PAYMENT_METHOD_NONCE_CODE = 2;

    static final String EXTRA_PAYMENT_METHOD_NONCES = "com.braintreepayments.api.EXTRA_PAYMENT_METHOD_NONCES";

    private DropInViewModel dropInViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_drop_in_activity);

        if (getDropInClient().getAuthorization() instanceof InvalidAuthorization) {
            finish(new InvalidArgumentException("Tokenization Key or Client Token was invalid."));
            return;
        }

        dropInViewModel = new ViewModelProvider(this).get(DropInViewModel.class);

        getDropInClient().getConfiguration(new ConfigurationCallback() {
            @Override
            public void onResult(@Nullable Configuration configuration, @Nullable Exception error) {
                onConfigurationFetched();
            }
        });

        sendAnalyticsEvent("appeared");
    }

    public void onConfigurationFetched() {
        showSelectPaymentMethodFragment();

        getDropInClient().getSupportedPaymentMethods(this, new GetSupportedPaymentMethodsCallback() {
            @Override
            public void onResult(@Nullable List<DropInPaymentMethodType> paymentMethods, @Nullable Exception error) {
                if (paymentMethods != null) {
                    dropInViewModel.setAvailablePaymentMethods(paymentMethods);
                } else {
                    onError(error);
                }
            }
        });
    }

    void showVaultManager() {
        // TODO: consider caching nonces or use a ViewModel for handling nonces
        getDropInClient().getVaultedPaymentMethods(this, false, new GetPaymentMethodNoncesCallback() {
            @Override
            public void onResult(@Nullable List<PaymentMethodNonce> paymentMethodNonceList, @Nullable Exception error) {
                if (paymentMethodNonceList != null) {
                    Intent intent = new Intent(DropInActivity.this, VaultManagerActivity.class)
                            .putParcelableArrayListExtra(EXTRA_PAYMENT_METHOD_NONCES, new ArrayList<Parcelable>(paymentMethodNonceList))
                            .putExtra(EXTRA_CHECKOUT_REQUEST, getIntent().getParcelableExtra(EXTRA_CHECKOUT_REQUEST))
                            .putExtra(EXTRA_AUTHORIZATION, getIntent().getStringExtra(EXTRA_AUTHORIZATION))
                            .putExtra(EXTRA_SESSION_ID, getIntent().getStringExtra(EXTRA_SESSION_ID));
                    startActivityForResult(intent, DELETE_PAYMENT_METHOD_NONCE_CODE);

                    getDropInClient().sendAnalyticsEvent("manager.appeared");
                } else if (error != null) {
                    onError(error);
                }
            }
        });
    }

    void sendAnalyticsEvent(String eventFragment) {
        getDropInClient().sendAnalyticsEvent(eventFragment);
    }

    void updateVaultedPaymentMethodNonces(boolean refetch) {
        getDropInClient().getVaultedPaymentMethods(this, refetch, new GetPaymentMethodNoncesCallback() {
            @Override
            public void onResult(@Nullable List<PaymentMethodNonce> vaultedPaymentMethods, @Nullable Exception error) {
                if (vaultedPaymentMethods != null) {
                    dropInViewModel.setVaultedPaymentMethodNonces(vaultedPaymentMethods);
                } else if (error != null) {
                    onError(error);
                }
            }
        });
    }

    private void showSelectPaymentMethodFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag("SELECT_PAYMENT_METHOD");
        if (fragment == null) {
            Bundle args = new Bundle();
            args.putParcelable("EXTRA_DROP_IN_REQUEST", mDropInRequest);

            fragmentManager
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container_view, SelectPaymentMethodFragment.class, args, "SELECT_PAYMENT_METHOD")
                    .commit();
        }
    }

    public void onError(final Exception error) {
        if (error instanceof AuthenticationException || error instanceof AuthorizationException ||
                error instanceof UpgradeRequiredException) {
            getDropInClient().sendAnalyticsEvent("sdk.exit.developer-error");
        } else if (error instanceof ConfigurationException) {
            getDropInClient().sendAnalyticsEvent("sdk.exit.configuration-exception");
        } else if (error instanceof ServerException || error instanceof UnexpectedException) {
            getDropInClient().sendAnalyticsEvent("sdk.exit.server-error");
        } else if (error instanceof ServiceUnavailableException) {
            getDropInClient().sendAnalyticsEvent("sdk.exit.server-unavailable");
        } else {
            getDropInClient().sendAnalyticsEvent("sdk.exit.sdk-error");
        }

        finish(error);
    }

    private void finishWithDropInResult(DropInResult dropInResult) {
        getDropInClient().sendAnalyticsEvent("sdk.exit.success");
        DropInResult.setLastUsedPaymentMethodType(DropInActivity.this, dropInResult.getPaymentMethodNonce());
        finish(dropInResult.getPaymentMethodNonce(), dropInResult.getDeviceData());
    }

    @Override
    public void onPaymentMethodSelected(DropInPaymentMethodType type) {
        dropInViewModel.setIsLoading(true);
        switch (type) {
            case PAYPAL:
                getDropInClient().tokenizePayPalRequest(this, new PayPalFlowStartedCallback() {
                    @Override
                    public void onResult(@Nullable Exception error) {
                        if (error != null) {
                            onError(error);
                        }
                    }
                });
                break;
            case GOOGLE_PAYMENT:
                getDropInClient().requestGooglePayPayment(this, new GooglePayRequestPaymentCallback() {
                    @Override
                    public void onResult(Exception error) {
                        if (error != null) {
                            onError(error);
                        }
                    }
                });
                break;
            case PAY_WITH_VENMO:
                getDropInClient().tokenizeVenmoAccount(this, new VenmoTokenizeAccountCallback() {
                    @Override
                    public void onResult(@Nullable Exception error) {
                        if (error != null) {
                            onError(error);
                        }
                    }
                });
                break;
            case UNKNOWN:
                Intent intent = new Intent(this, AddCardActivity.class)
                        .putExtra(EXTRA_CHECKOUT_REQUEST, getIntent().getParcelableExtra(EXTRA_CHECKOUT_REQUEST))
                        .putExtra(EXTRA_AUTHORIZATION, getIntent().getStringExtra(EXTRA_AUTHORIZATION))
                        .putExtra(EXTRA_SESSION_ID, getIntent().getStringExtra(EXTRA_SESSION_ID));
                startActivityForResult(intent, ADD_CARD_REQUEST_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED) {
            if (requestCode == ADD_CARD_REQUEST_CODE) {
                dropInViewModel.setIsLoading(true);
                updateVaultedPaymentMethodNonces(true);
            }
            dropInViewModel.setIsLoading(false);

        } else if (requestCode == ADD_CARD_REQUEST_CODE) {
            final Intent response;
            if (resultCode == RESULT_OK) {
                dropInViewModel.setIsLoading(true);

                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                DropInResult.setLastUsedPaymentMethodType(this, result.getPaymentMethodNonce());

                response = new Intent()
                        .putExtra(DropInResult.EXTRA_DROP_IN_RESULT, result);
            } else {
                response = data;
            }
            setResult(resultCode, response);
            finish();
        } else if (requestCode == DELETE_PAYMENT_METHOD_NONCE_CODE) {
            if (resultCode == RESULT_OK) {
                dropInViewModel.setIsLoading(true);

                if (data != null) {
                    ArrayList<PaymentMethodNonce> paymentMethodNonces = data
                            .getParcelableArrayListExtra(EXTRA_PAYMENT_METHOD_NONCES);

                    if (paymentMethodNonces != null) {
                        dropInViewModel.setVaultedPaymentMethodNonces(paymentMethodNonces);
                    }
                }

                updateVaultedPaymentMethodNonces(true);
            }
            dropInViewModel.setIsLoading(false);
        }
    }

    public void onBackgroundClicked(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        getDropInClient().sendAnalyticsEvent("sdk.exit.canceled");
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onVaultedPaymentMethodSelected(final PaymentMethodNonce paymentMethodNonce) {
        if (paymentMethodNonce instanceof CardNonce) {
            getDropInClient().sendAnalyticsEvent("vaulted-card.select");
        }

        getDropInClient().shouldRequestThreeDSecureVerification(paymentMethodNonce, new ShouldRequestThreeDSecureVerification() {
            @Override
            public void onResult(boolean shouldRequestThreeDSecureVerification) {
                if (shouldRequestThreeDSecureVerification) {
                    dropInViewModel.setIsLoading(true);

                    getDropInClient().performThreeDSecureVerification(DropInActivity.this, paymentMethodNonce, new DropInResultCallback() {
                        @Override
                        public void onResult(@Nullable DropInResult dropInResult, @Nullable Exception error) {
                            if (dropInResult != null) {
                                finishWithDropInResult(dropInResult);
                            } else {
                                updateVaultedPaymentMethodNonces(true);
                                dropInViewModel.setIsLoading(false);
                                onError(error);
                            }
                        }
                    });
                } else {
                    // TODO: unit test
                    getDropInClient().collectDeviceData(DropInActivity.this, new DataCollectorCallback() {
                        @Override
                        public void onResult(@Nullable String deviceData, @Nullable Exception error) {
                            if (deviceData != null) {
                                DropInResult dropInResult = new DropInResult();
                                dropInResult.paymentMethodNonce(paymentMethodNonce);
                                dropInResult.deviceData(deviceData);
                                finishWithDropInResult(dropInResult);
                            } else {
                                // TODO: determine if we should fail when device data collection fails
                                updateVaultedPaymentMethodNonces(true);
                                dropInViewModel.setIsLoading(false);
                                onError(error);
                            }
                        }
                    });
                }
            }
        });
    }
}
