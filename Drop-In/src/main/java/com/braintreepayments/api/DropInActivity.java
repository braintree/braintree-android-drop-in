package com.braintreepayments.api;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;

import com.braintreepayments.api.dropin.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.List;

import static com.braintreepayments.api.DropInClient.EXTRA_AUTHORIZATION;
import static com.braintreepayments.api.DropInClient.EXTRA_SESSION_ID;
import static com.braintreepayments.api.DropInRequest.EXTRA_CHECKOUT_REQUEST;

// TODO: unit test after all fragments have been extracted
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
    ActionBar actionBar;
    private FragmentContainerView fragmentContainerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_drop_in_activity);

        if (getDropInClient().getAuthorization() instanceof InvalidAuthorization) {
            finish(new InvalidArgumentException("Tokenization Key or Client Token was invalid."));
            return;
        }

        dropInViewModel = new ViewModelProvider(this).get(DropInViewModel.class);
        fragmentContainerView = findViewById(R.id.fragment_container_view);

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

        // TODO: consider a single event type with a context of type "Object" to prevent having
        // to create a new event type every time
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
        } else if (braintreeResult instanceof CardDetailsEvent) {
            onCardDetailsEvent((CardDetailsEvent) braintreeResult);
        } else if (braintreeResult instanceof EditCardNumberEvent) {
            startAddCardFlow((EditCardNumberEvent) braintreeResult);
        } else if (braintreeResult instanceof DeleteVaultedPaymentMethodNonceEvent) {
            DeleteVaultedPaymentMethodNonceEvent deleteVaultedPaymentMethodNonceEvent =
                    (DeleteVaultedPaymentMethodNonceEvent) braintreeResult;
            onDeleteVaultedPaymentMethodSelected(deleteVaultedPaymentMethodNonceEvent);
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
                startAddCardFlow(null);
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
            case DropInUIEventType.DISMISS_VAULT_MANAGER:
                showSelectPaymentMethodFragment();
                break;
        }
    }

    void onDeleteVaultedPaymentMethodSelected(DeleteVaultedPaymentMethodNonceEvent event) {
        final PaymentMethodNonce paymentMethodNonceToDelete = event.getPaymentMethodNonceToDelete();

        PaymentMethodItemView dialogView = new PaymentMethodItemView(this);
        dialogView.setPaymentMethod(paymentMethodNonceToDelete, false);

        new AlertDialog.Builder(this,
                R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle(R.string.bt_delete_confirmation_title)
                .setMessage(R.string.bt_delete_confirmation_description)
                .setView(dialogView)
                .setPositiveButton(R.string.bt_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getDropInClient().sendAnalyticsEvent("manager.delete.confirmation.positive");
                        removePaymentMethodNonce(paymentMethodNonceToDelete);
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        getDropInClient().sendAnalyticsEvent("manager.delete.confirmation.negative");
                    }
                })
                .setNegativeButton(R.string.bt_cancel, null)
                .create()
                .show();
    }

    private void removePaymentMethodNonce(PaymentMethodNonce paymentMethodNonceToDelete) {
        // proactively remove from view model
        dropInViewModel.removeVaultedPaymentMethodNonce(paymentMethodNonceToDelete);

        getDropInClient().deletePaymentMethod(DropInActivity.this, paymentMethodNonceToDelete, new DeletePaymentMethodNonceCallback() {
            @Override
            public void onResult(@Nullable PaymentMethodNonce deletedNonce, @Nullable Exception error) {
                if (deletedNonce != null) {
                    getDropInClient().sendAnalyticsEvent("manager.delete.succeeded");
                } else if (error instanceof PaymentMethodDeleteException) {
                    Snackbar.make(fragmentContainerView, R.string.bt_vault_manager_delete_failure, Snackbar.LENGTH_LONG).show();
                    getDropInClient().sendAnalyticsEvent("manager.delete.failed");
                    // TODO: hide loading view switcher
                    //mLoadingViewSwitcher.setDisplayedChild(0);
                } else {
                    getDropInClient().sendAnalyticsEvent("manager.unknown.failed");
                    // TODO: determine how to handle unexpected error when deleting payment method (previously finished drop in)
                }
            }
        });
    }

    void sendAnalyticsEvent(String eventFragment) {
        getDropInClient().sendAnalyticsEvent(eventFragment);
    }

    void showVaultManager() {
        // TODO: consider caching nonces or use a ViewModel for handling nonces
        // TODO: show loading indicator while fetching vaulted payment methods
        getDropInClient().getVaultedPaymentMethods(this, false, new GetPaymentMethodNoncesCallback() {
            @Override
            public void onResult(@Nullable List<PaymentMethodNonce> paymentMethodNonceList, @Nullable Exception error) {
                if (paymentMethodNonceList != null) {
                    dropInViewModel.setVaultedPaymentMethods(paymentMethodNonceList);
                    showVaultManagerFragment();
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
                    .replace(R.id.fragment_container_view, SelectPaymentMethodFragment.class, args, "SELECT_PAYMENT_METHOD")
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

    private void showVaultManagerFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment fragment = fragmentManager.findFragmentByTag("VAULT_MANAGER");
        if (fragment == null) {
            Bundle args = new Bundle();
            args.putParcelable("EXTRA_DROP_IN_REQUEST", mDropInRequest);

            fragmentManager
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_container_view, VaultManagerFragment.class, args, "VAULT_MANAGER")
                    .commit();
            getDropInClient().sendAnalyticsEvent("manager.appeared");
        }
    }

    public void onError(final Exception error) {
        if (error instanceof ErrorWithResponse) {
            ErrorWithResponse errorResponse = (ErrorWithResponse) error;
            dropInViewModel.setCardTokenizationError(errorResponse);
        } else if (error instanceof AuthenticationException || error instanceof AuthorizationException || error instanceof UpgradeRequiredException) {
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

    private void startAddCardFlow(EditCardNumberEvent editCardNumberEvent) {
        setActionBarTitle(R.string.bt_card_details);
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
            if (editCardNumberEvent != null) {
                args.putString("EXTRA_CARD_NUMBER", editCardNumberEvent.getCardNumber());
            }

            fragmentManager
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_container_view, AddCardFragment.class, args, "ADD_CARD")
                    .commit();
        }
    }

    private void setActionBarTitle(@StringRes int titleResId) {
        if (actionBar == null) {
            setSupportActionBar((Toolbar) findViewById(R.id.bt_toolbar));
            actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        actionBar.setTitle(titleResId);
        findViewById(R.id.bt_toolbar).setVisibility(View.VISIBLE);
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

    void onCardDetailsEvent(CardDetailsEvent cardDetailsEvent) {
        Card card = cardDetailsEvent.getCard();
        getDropInClient().tokenizeCard(card, new CardTokenizeCallback() {
            @Override
            public void onResult(@Nullable CardNonce cardNonce, @Nullable Exception error) {
                if (error != null) {
                    dropInViewModel.setCardTokenizationError(error);
                    return;
                }
                onPaymentMethodNonceCreated(cardNonce);
            }
        });
    }

    void onPaymentMethodNonceCreated(final PaymentMethodNonce paymentMethod) {
        getDropInClient().shouldRequestThreeDSecureVerification(paymentMethod, new ShouldRequestThreeDSecureVerification() {
            @Override
            public void onResult(boolean shouldRequestThreeDSecureVerification) {
                if (shouldRequestThreeDSecureVerification) {
                    getDropInClient().performThreeDSecureVerification(DropInActivity.this, paymentMethod, new DropInResultCallback() {
                        @Override
                        public void onResult(@Nullable DropInResult dropInResult, @Nullable Exception error) {
                            if (error != null) {
                                onError(error);
                                return;
                            }
                            getDropInClient().sendAnalyticsEvent("sdk.exit.success");
                            finish(dropInResult.getPaymentMethodNonce(), dropInResult.getDeviceData());
                        }
                    });
                } else {
                    getDropInClient().sendAnalyticsEvent("sdk.exit.success");
                    finish(paymentMethod, null);
                }
            }
        });
    }
}
