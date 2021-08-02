package com.braintreepayments.api;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.VisibleForTesting;
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

// TODO: unit test after all fragments have been extracted
public class DropInActivity extends BaseActivity {

    private static final String ADD_CARD_TAG = "ADD_CARD";
    private static final String CARD_DETAILS_TAG = "CARD_DETAILS";
    private static final String SELECT_PAYMENT_METHOD_TAG = "SELECT_PAYMENT_METHOD_PARENT_FRAGMENT";

    /**
     * Errors are returned as the serializable value of this key in the data intent in
     * {@link #onActivityResult(int, int, android.content.Intent)} if
     * responseCode is not {@link #RESULT_OK} or
     * {@link #RESULT_CANCELED}.
     */
    public static final String EXTRA_ERROR = "com.braintreepayments.api.dropin.EXTRA_ERROR";

    @VisibleForTesting
    DropInViewModel dropInViewModel;
    ActionBar actionBar;
    private FragmentContainerView fragmentContainerView;

    @Override
    protected void onResume() {
        super.onResume();

        getDropInClient().deliverBrowserSwitchResult(this, new DropInResultCallback() {
            @Override
            public void onResult(@Nullable DropInResult dropInResult, @Nullable Exception error) {
                onDropInResult(dropInResult, error);
            }
        });
    }

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

        getSupportFragmentManager().setFragmentResultListener(DropInEvent.REQUEST_KEY, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                onDropInEvent(DropInEvent.fromBundle(result));
            }
        });

        showSelectPaymentMethodParentFragment();
    }

    @VisibleForTesting
    void onDropInEvent(DropInEvent event) {
        switch (event.getType()) {
            case ADD_CARD_SUBMIT:
                onAddCardSubmit(event);
                break;
            case CARD_DETAILS_SUBMIT:
                onCardDetailsSubmit(event);
                break;
            case DELETE_VAULTED_PAYMENT_METHOD:
                onDeleteVaultedPaymentMethod(event);
                break;
            case DID_DISPLAY_SUPPORTED_PAYMENT_METHODS:
                onDidDisplaySupportedPaymentMethods(event);
                break;
            case EDIT_CARD_NUMBER:
                onEditCardNumber(event);
                break;
            case SEND_ANALYTICS:
                onSendAnalytics(event);
                break;
            case SHOW_VAULT_MANAGER:
                refreshVaultedPaymentMethods();
                break;
            case SUPPORTED_PAYMENT_METHOD_SELECTED:
                onSupportedPaymentMethodSelected(event);
                break;
            case VAULTED_PAYMENT_METHOD_SELECTED:
                onVaultedPaymentMethodSelected(event);
                break;
        }
    }

    @VisibleForTesting
    void onSupportedPaymentMethodSelected(DropInEvent event) {
        DropInPaymentMethodType paymentMethodType =
                event.getDropInPaymentMethodType(DropInEventProperty.SUPPORTED_PAYMENT_METHOD);
        startPaymentFlow(paymentMethodType);
    }

    void startPaymentFlow(DropInPaymentMethodType paymentMethodType) {
        switch (paymentMethodType) {
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

    private void onDeleteVaultedPaymentMethod(DropInEvent event) {
        deleteVaultedPaymentMethod(
                event.getPaymentMethodNonce(DropInEventProperty.VAULTED_PAYMENT_METHOD));
    }

    private void deleteVaultedPaymentMethod(final PaymentMethodNonce paymentMethodNonceToDelete) {
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

    private void onSendAnalytics(DropInEvent event) {
        String eventName = event.getString(DropInEventProperty.ANALYTICS_EVENT_NAME);
        sendAnalyticsEvent(eventName);
    }

    private void sendAnalyticsEvent(String eventName) {
        getDropInClient().sendAnalyticsEvent(eventName);
    }

    void refreshVaultedPaymentMethods() {
        // TODO: consider caching nonces or use a ViewModel for handling nonces
        // TODO: show loading indicator while fetching vaulted payment methods
        getDropInClient().getVaultedPaymentMethods(this, false, new GetPaymentMethodNoncesCallback() {
            @Override
            public void onResult(@Nullable List<PaymentMethodNonce> paymentMethodNonceList, @Nullable Exception error) {
                if (paymentMethodNonceList != null) {
                    dropInViewModel.setVaultedPaymentMethods(paymentMethodNonceList);
                } else if (error != null) {
                    onError(error);
                }
            }
        });
    }

    private void onDidDisplaySupportedPaymentMethods(DropInEvent event) {
        // TODO: consider pull to refresh to allow user to request an updated
        // instead of having this event respond to the visual presentation of supported
        // payment methods
        updateVaultedPaymentMethodNonces(false);
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

    private void onAddCardSubmit(DropInEvent event) {
        String cardNumber = event.getString(DropInEventProperty.CARD_NUMBER);
        showCardDetailsFragment(cardNumber);
    }

    private boolean shouldAddFragment(String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        return (fragment == null);
    }

    private void replaceExistingFragment(Class <? extends Fragment> fragmentClass, String tag, Bundle args) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container_view, fragmentClass, args, tag)
                .commit();
    }

    private void showSelectPaymentMethodParentFragment() {
        if (shouldAddFragment(SELECT_PAYMENT_METHOD_TAG)) {
            Bundle args = new Bundle();
            args.putParcelable("EXTRA_DROP_IN_REQUEST", mDropInRequest);
            replaceExistingFragment(SelectPaymentMethodParentFragment.class, SELECT_PAYMENT_METHOD_TAG, args);
        }
    }

    private void showCardDetailsFragment(final String cardNumber) {
        getDropInClient().getConfiguration(new ConfigurationCallback() {
            @Override
            public void onResult(@Nullable Configuration configuration, @Nullable Exception error) {
                if (configuration != null) {
                    CardFormConfiguration cardFormConfiguration =
                        new CardFormConfiguration(configuration.isCvvChallengePresent(), configuration.isPostalCodeChallengePresent());

                    if (shouldAddFragment(CARD_DETAILS_TAG)) {
                        Bundle args = new Bundle();
                        args.putParcelable("EXTRA_DROP_IN_REQUEST", mDropInRequest);
                        args.putString("EXTRA_CARD_NUMBER", cardNumber);
                        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", cardFormConfiguration);

                        replaceExistingFragment(CardDetailsFragment.class, CARD_DETAILS_TAG, args);
                    }
                }
            }
        });

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

    private void onEditCardNumber(DropInEvent event) {
        startAddCardFlow(event.getString(DropInEventProperty.CARD_NUMBER));
    }

    private void startAddCardFlow(@Nullable String cardNumber) {
        setActionBarTitle(R.string.bt_card_details);
        getDropInClient().getSupportedCardTypes(new GetSupportedCardTypesCallback() {
            @Override
            public void onResult(List<String> supportedCardTypes, Exception error) {
                dropInViewModel.setSupportedCardTypes(Arrays.asList(DropInPaymentMethodType.getCardsTypes(supportedCardTypes)));
            }
        });

        if (shouldAddFragment(ADD_CARD_TAG)) {
            Bundle args = new Bundle();
            args.putParcelable("EXTRA_DROP_IN_REQUEST", mDropInRequest);
            if (cardNumber != null) {
                args.putString("EXTRA_CARD_NUMBER", cardNumber);
            }
            replaceExistingFragment(AddCardFragment.class, ADD_CARD_TAG, args);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getDropInClient().handleActivityResult(this, requestCode, resultCode, data, new DropInResultCallback() {
            @Override
            public void onResult(@Nullable DropInResult dropInResult, @Nullable Exception error) {
                onDropInResult(dropInResult, error);
            }
        });
    }

    private void onDropInResult(DropInResult dropInResult, Exception error) {
        if (dropInResult != null) {
            finishWithDropInResult(dropInResult);
        } else if (error instanceof UserCanceledException) {
            dropInViewModel.setUserCanceledError(error);
        } else {
            onError(error);
        }
    }

    @VisibleForTesting
    void onVaultedPaymentMethodSelected(DropInEvent event) {
        final PaymentMethodNonce paymentMethodNonce =
                event.getPaymentMethodNonce(DropInEventProperty.VAULTED_PAYMENT_METHOD);

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

    private void onCardDetailsSubmit(DropInEvent event) {
        Card card = event.getCard(DropInEventProperty.CARD);
        tokenizeCard(card);
    }

    void tokenizeCard(Card card) {
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
