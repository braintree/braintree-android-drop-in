package com.braintreepayments.api;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;

import com.braintreepayments.api.dropin.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.braintreepayments.api.DropInClient.EXTRA_AUTHORIZATION;
import static com.braintreepayments.api.DropInClient.EXTRA_SESSION_ID;
import static com.braintreepayments.api.DropInRequest.EXTRA_CHECKOUT_REQUEST;

public class DropInActivity extends BaseActivity {

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

        getDropInClient().getSupportedPaymentMethods(this, new GetSupportedPaymentMethodsCallback() {
            @Override
            public void onResult(@Nullable List<DropInPaymentMethodType> paymentMethods, @Nullable Exception error) {
                if (paymentMethods != null) {
                    dropInViewModel.setSupportedPaymentMethods(paymentMethods);
                } else {
                    onError(error);
                }
            }
        });

        getSupportFragmentManager().setFragmentResultListener("BRAINTREE_EVENT", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                handleBraintreeEventBundle(result);
            }
        });

        showSelectPaymentMethodFragment();
    }

    private void handleBraintreeEventBundle(Bundle bundle) {
        Parcelable braintreeResult = bundle.getParcelable("BRAINTREE_RESULT");

        if (braintreeResult instanceof DropInAnalyticsEvent) {
            onAnalyticsEvent((DropInAnalyticsEvent) braintreeResult);
        } else if (braintreeResult instanceof DropInUIEvent) {
            onUIEvent((DropInUIEvent) braintreeResult);
        } else if (braintreeResult instanceof SupportedPaymentMethodSelectedEvent) {
            onSupportedPaymentMethodSelectedEvent((SupportedPaymentMethodSelectedEvent) braintreeResult);
        } else if (braintreeResult instanceof VaultedPaymentMethodSelectedEvent) {
            VaultedPaymentMethodSelectedEvent vaultedPaymentMethodSelectedEvent =
                    (VaultedPaymentMethodSelectedEvent) braintreeResult;
            onVaultedPaymentMethodSelected(vaultedPaymentMethodSelectedEvent.getPaymentMethodNonce());
        } else if (braintreeResult instanceof AddCardEvent) {
            showCardDetailsFragment(((AddCardEvent) braintreeResult).getCardNumber());
        }
    }

    void onAnalyticsEvent(DropInAnalyticsEvent event) {
        sendAnalyticsEvent(event.getName());
    }

    void onSupportedPaymentMethodSelectedEvent(SupportedPaymentMethodSelectedEvent event) {
        switch (event.getPaymentMethodType()) {
            case GOOGLE_PAYMENT:
                startGooglePayFlow();
                break;
            case PAYPAL:
                startPayPalFlow();
                break;
            case PAY_WITH_VENMO:
                startVenmoFlow();
                break;
            default:
            case UNKNOWN:
                startAddCardFlow();
                break;
        }
    }

    void onUIEvent(DropInUIEvent event) {
        switch (event.getType()) {
            case DropInUIEventType.SHOW_VAULT_MANAGER:
                showVaultManager();
                break;
            case DropInUIEventType.DID_DISPLAY_SUPPORTED_PAYMENT_METHODS:
                // TODO: "refetch" was previously false in this case, however it isn't
                // immediately clear why the refetch parameter exists. We should investigate
                // to see if this parameter is necessary
                updateVaultedPaymentMethodNonces(false);
                break;
        }
    }

    void sendAnalyticsEvent(String eventFragment) {
        getDropInClient().sendAnalyticsEvent(eventFragment);
    }

    void showVaultManager() {
        // TODO: consider caching nonces or use a ViewModel for handling nonces
        getDropInClient().getVaultedPaymentMethods(this, false, new GetPaymentMethodNoncesCallback() {
            @Override
            public void onResult(@Nullable List<PaymentMethodNonce> paymentMethodNonceList, @Nullable Exception error) {
                if (paymentMethodNonceList != null) {
                    Intent intent = new Intent(DropInActivity.this, VaultManagerActivity.class)
                            .putParcelableArrayListExtra(EXTRA_PAYMENT_METHOD_NONCES, new ArrayList<Parcelable>(paymentMethodNonceList))
                            .putExtra(EXTRA_CHECKOUT_REQUEST, (DropInRequest) getIntent().getParcelableExtra(EXTRA_CHECKOUT_REQUEST))
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

    void updateVaultedPaymentMethodNonces(boolean refetch) {
        if (mClientTokenPresent) {
            getDropInClient().getVaultedPaymentMethods(this, refetch, new GetPaymentMethodNoncesCallback() {
                @Override
                public void onResult(@Nullable List<PaymentMethodNonce> vaultedPaymentMethods, @Nullable Exception error) {
                    if (vaultedPaymentMethods != null) {
                        dropInViewModel.setVaultedPaymentMethods(vaultedPaymentMethods);
                    } else if (error != null) {
                        onError(error);
                    }
                }
            });
        }
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

    private void showCardDetailsFragment(final String cardNumber) {
        getDropInClient().getConfiguration(new ConfigurationCallback() {
            @Override
            public void onResult(@Nullable Configuration configuration, @Nullable Exception error) {
                if (configuration != null) {
                    CardFormConfiguration cardFormConfiguration = new CardFormConfiguration(configuration.isCvvChallengePresent(), configuration.isPostalCodeChallengePresent());

                    FragmentManager fragmentManager = getSupportFragmentManager();
                    Fragment fragment = fragmentManager.findFragmentByTag("CARD_DETAILS");
                    if (fragment == null) {
                        Bundle args = new Bundle();
                        args.putParcelable("EXTRA_DROP_IN_REQUEST", mDropInRequest);
                        args.putString("EXTRA_CARD_NUMBER", cardNumber);
                        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", cardFormConfiguration);

                        fragmentManager
                                .beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.fragment_container_view, CardDetailsFragment.class, args, "CARD_DETAILS")
                                .commit();
                    }
                }
            }
        });

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

    private void startPayPalFlow() {
        getDropInClient().tokenizePayPalRequest(this, new PayPalFlowStartedCallback() {
            @Override
            public void onResult(@Nullable Exception error) {
                if (error != null) {
                    onError(error);
                }
            }
        });
    }

    private void startGooglePayFlow() {
        getDropInClient().requestGooglePayPayment(this, new GooglePayRequestPaymentCallback() {
            @Override
            public void onResult(Exception error) {
                if (error != null) {
                    onError(error);
                }
            }
        });
    }

    private void startVenmoFlow() {
        getDropInClient().tokenizeVenmoAccount(this, new VenmoTokenizeAccountCallback() {
            @Override
            public void onResult(@Nullable Exception error) {
                if (error != null) {
                    onError(error);
                }
            }
        });
    }

    private void startAddCardFlow() {
        getDropInClient().getSupportedCardTypes(new GetSupportedCardTypesCallback() {
            @Override
            public void onResult(List<String> supportedCardTypes, Exception error) {
                 dropInViewModel.setSupportedCardTypes(Arrays.asList(DropInPaymentMethodType.getCardsTypes(supportedCardTypes)));
            }
        });

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag("ADD_CARD");
        if (fragment == null) {
            Bundle args = new Bundle();
            args.putParcelable("EXTRA_DROP_IN_REQUEST", mDropInRequest);

            fragmentManager
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_container_view, AddCardFragment.class, args, "ADD_CARD")
                    .commit();
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
                        dropInViewModel.setVaultedPaymentMethods(paymentMethodNonces);
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
                    final DropInResult dropInResult = new DropInResult();
                    dropInResult.paymentMethodNonce(paymentMethodNonce);

                    if (mDropInRequest.shouldCollectDeviceData()) {
                        getDropInClient().collectDeviceData(DropInActivity.this, new DataCollectorCallback() {
                            @Override
                            public void onResult(@Nullable String deviceData, @Nullable Exception error) {
                                if (deviceData != null) {
                                    dropInResult.deviceData(deviceData);
                                    finishWithDropInResult(dropInResult);
                                } else {
                                    updateVaultedPaymentMethodNonces(true);
                                    dropInViewModel.setIsLoading(false);
                                    onError(error);
                                }
                            }
                        });
                    } else {
                        finishWithDropInResult(dropInResult);
                    }
                }
            }
        });
    }
}
