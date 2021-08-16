package com.braintreepayments.api;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.braintreepayments.api.dropin.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.List;

public class DropInActivity extends AppCompatActivity {

    private static final String ADD_CARD_TAG = "ADD_CARD";
    private static final String CARD_DETAILS_TAG = "CARD_DETAILS";
    private static final String BOTTOM_SHEET_TAG = "BOTTOM_SHEET";

    @VisibleForTesting
    DropInViewModel dropInViewModel;

    @VisibleForTesting
    DropInRequest mDropInRequest;

    private DropInClient dropInClient;
    private FragmentContainerView fragmentContainerView;
    private DropInResult pendingDropInResult;

    @VisibleForTesting
    boolean mClientTokenPresent;

    private AlertPresenter alertPresenter;

    @Override
    protected void onResume() {
        super.onResume();

        if (willDeliverSuccessfulBrowserSwitchResult()) {
            // when browser switch is successful, a tokenization http call will be made by DropInClient
            // show the loader to signal to the user that an asynchronous operation is underway
            dropInViewModel.setDropInState(DropInState.WILL_FINISH);
        }

        getDropInClient().deliverBrowserSwitchResult(this, new DropInResultCallback() {
            @Override
            public void onResult(@Nullable DropInResult dropInResult, @Nullable Exception error) {
                onDropInResult(dropInResult, error);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        setIntent(newIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_drop_in_activity);

        if (getDropInClient().getAuthorization() instanceof InvalidAuthorization) {
            finish(new InvalidArgumentException("Tokenization Key or Client Token was invalid."));
            return;
        }

        alertPresenter = new AlertPresenter();
        mDropInRequest = getIntent().getParcelableExtra(DropInClient.EXTRA_CHECKOUT_REQUEST);

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

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onDropInCanceled();
            }
        });

        dropInViewModel.getBottomSheetState().observe(this, new Observer<BottomSheetState>() {
            @Override
            public void onChanged(BottomSheetState bottomSheetState) {
                switch (bottomSheetState) {
                    case SHOWN:
                        onDidShowBottomSheet();
                        break;
                    case HIDDEN:
                        onDidHideBottomSheet();
                        break;
                    case HIDE_REQUESTED:
                    case SHOW_REQUESTED:
                    default:
                        // do nothing
                }
            }
        });

        showBottomSheet();
    }

    protected void finish(PaymentMethodNonce paymentMethod, String deviceData) {
        DropInResult result = new DropInResult()
                .paymentMethodNonce(paymentMethod)
                .deviceData(deviceData);

        setResult(RESULT_OK,
                new Intent().putExtra(DropInResult.EXTRA_DROP_IN_RESULT, result));
        finish();
    }

    protected void finish(Exception e) {
        setResult(RESULT_FIRST_USER, new Intent().putExtra(DropInResult.EXTRA_ERROR, e));
        finish();
    }

    @VisibleForTesting
    DropInClient getDropInClient() {
        // lazily instantiate dropInClient for testing purposes
        if (dropInClient != null) {
            return dropInClient;
        }
        Intent intent = getIntent();
        String authorization = intent.getStringExtra(DropInClient.EXTRA_AUTHORIZATION);
        String sessionId = intent.getStringExtra(DropInClient.EXTRA_SESSION_ID);
        DropInRequest dropInRequest = intent.getParcelableExtra(DropInClient.EXTRA_CHECKOUT_REQUEST);
        dropInClient = new DropInClient(this, authorization, sessionId, dropInRequest);

        mClientTokenPresent = dropInClient.getAuthorization() instanceof ClientToken;
        return dropInClient;
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
            case CANCEL_DROPIN:
                onDropInCanceled();
                break;
            case DELETE_VAULTED_PAYMENT_METHOD:
                onDeleteVaultedPaymentMethod(event);
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

    private void onDropInCanceled() {
        dropInViewModel.setBottomSheetState(BottomSheetState.HIDE_REQUESTED);
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
                prefetchSupportedCardTypes();
                startAddCardFlow(null);
                break;
        }
    }

    private void onDeleteVaultedPaymentMethod(DropInEvent event) {
        deleteVaultedPaymentMethod(
                event.getPaymentMethodNonce(DropInEventProperty.VAULTED_PAYMENT_METHOD));
    }

    private void deleteVaultedPaymentMethod(final PaymentMethodNonce paymentMethodNonceToDelete) {
        alertPresenter.showConfirmNonceDeletionDialog(this, paymentMethodNonceToDelete, new DialogInteractionCallback() {
            @Override
            public void onDialogInteraction(DialogInteraction interaction) {
                switch (interaction) {
                    case POSITIVE:
                        sendAnalyticsEvent("manager.delete.confirmation.positive");
                        removePaymentMethodNonce(paymentMethodNonceToDelete);
                        break;
                    case NEGATIVE:
                        sendAnalyticsEvent("manager.delete.confirmation.negative");
                        break;
                }
            }
        });
    }

    @VisibleForTesting
    void removePaymentMethodNonce(PaymentMethodNonce paymentMethodNonceToDelete) {
        // proactively remove from view model
        dropInViewModel.removeVaultedPaymentMethodNonce(paymentMethodNonceToDelete);

        getDropInClient().deletePaymentMethod(DropInActivity.this, paymentMethodNonceToDelete, new DeletePaymentMethodNonceCallback() {
            @Override
            public void onResult(@Nullable PaymentMethodNonce deletedNonce, @Nullable Exception error) {
                if (deletedNonce != null) {
                    sendAnalyticsEvent("manager.delete.succeeded");
                } else if (error instanceof PaymentMethodDeleteException) {
                    sendAnalyticsEvent("manager.delete.failed");

                    int snackBarTextResId = R.string.bt_vault_manager_delete_failure;
                    alertPresenter.showSnackbarText(
                            fragmentContainerView, snackBarTextResId, Snackbar.LENGTH_LONG);
                    // TODO: hide loading view switcher
                    //mLoadingViewSwitcher.setDisplayedChild(0);
                } else {
                    sendAnalyticsEvent("manager.unknown.failed");
                    // TODO: determine how to handle unexpected error when deleting payment method (previously finished drop in)
                    onError(error);
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

    private void onDidShowBottomSheet() {
        getDropInClient().getSupportedPaymentMethods(this, new GetSupportedPaymentMethodsCallback() {
            @Override
            public void onResult(@Nullable List<DropInPaymentMethodType> paymentMethods, @Nullable Exception error) {
                if (paymentMethods != null) {
                    dropInViewModel.setSupportedPaymentMethods(paymentMethods);

                    // TODO: consider pull to refresh to allow user to request an updated
                    // instead of having this event respond to the visual presentation of supported
                    // payment methods
                    updateVaultedPaymentMethodNonces(false);
                } else {
                    onError(error);
                }
            }
        });
    }

    private void onDidHideBottomSheet() {
        sendActivityResult();
    }

    private void sendActivityResult() {
        if (pendingDropInResult != null) {
            getDropInClient().sendAnalyticsEvent("sdk.exit.success");
            DropInResult.setLastUsedPaymentMethodType(DropInActivity.this, pendingDropInResult.getPaymentMethodNonce());
            finish(pendingDropInResult.getPaymentMethodNonce(), pendingDropInResult.getDeviceData());
        } else {
            // assume drop in cancelled
            getDropInClient().sendAnalyticsEvent("sdk.exit.canceled");
            setResult(RESULT_CANCELED);
            finish();
        }
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

    private void replaceExistingFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.bt_fragment_fade_in, R.anim.bt_fragment_fade_out)
                .replace(R.id.fragment_container_view, fragment, tag)
                .addToBackStack(null)
                .commit();
    }

    private void showBottomSheet() {
        if (shouldAddFragment(BOTTOM_SHEET_TAG)) {
            BottomSheetFragment bottomSheetFragment = BottomSheetFragment.from(mDropInRequest);
            replaceExistingFragment(bottomSheetFragment, BOTTOM_SHEET_TAG);
        }
        dropInViewModel.setBottomSheetState(BottomSheetState.SHOW_REQUESTED);
    }

    private void showCardDetailsFragment(final String cardNumber) {
        if (shouldAddFragment(CARD_DETAILS_TAG)) {
            getDropInClient().getConfiguration(new ConfigurationCallback() {
                @Override
                public void onResult(@Nullable Configuration configuration, @Nullable Exception error) {
                    if (configuration != null) {
                        // TODO: implement getDropInClient().hasAuthType(AuthType.TOKENIZATION_KEY)
                        boolean hasTokenizationKeyAuth =
                                Authorization.isTokenizationKey(getDropInClient().getAuthorization().toString());

                        CardDetailsFragment cardDetailsFragment = CardDetailsFragment.from(
                                mDropInRequest, cardNumber, configuration, hasTokenizationKeyAuth);
                        replaceExistingFragment(cardDetailsFragment, CARD_DETAILS_TAG);
                    }
                }
            });
        }
    }

    public void onError(final Exception error) {
        if (error instanceof ErrorWithResponse) {
            ErrorWithResponse errorResponse = (ErrorWithResponse) error;
            dropInViewModel.setCardTokenizationError(errorResponse);
        } else if (error instanceof AuthenticationException || error instanceof AuthorizationException || error instanceof UpgradeRequiredException) {
            sendAnalyticsEvent("sdk.exit.developer-error");
        } else if (error instanceof ConfigurationException) {
            sendAnalyticsEvent("sdk.exit.configuration-exception");
        } else if (error instanceof ServerException || error instanceof UnexpectedException) {
            sendAnalyticsEvent("sdk.exit.server-error");
        } else if (error instanceof ServiceUnavailableException) {
            sendAnalyticsEvent("sdk.exit.server-unavailable");
        } else {
            sendAnalyticsEvent("sdk.exit.sdk-error");
        }

        finish(error);
    }

    private void finishWithDropInResult(DropInResult dropInResult) {
        pendingDropInResult = dropInResult;

        boolean isBottomSheetVisible = shouldAddFragment(BOTTOM_SHEET_TAG);
        if (isBottomSheetVisible) {
            dropInViewModel.setBottomSheetState(BottomSheetState.HIDE_REQUESTED);
        } else {
            sendActivityResult();
        }
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

    private void prefetchSupportedCardTypes() {
        getDropInClient().getSupportedCardTypes(new GetSupportedCardTypesCallback() {
            @Override
            public void onResult(List<String> supportedCardTypes, Exception error) {
                if (error != null) {
                    onError(error);
                } else if (supportedCardTypes != null) {
                    dropInViewModel.setSupportedCardTypes(Arrays.asList(DropInPaymentMethodType.getCardsTypes(supportedCardTypes)));
                }
            }
        });
    }

    private void startAddCardFlow(@Nullable String cardNumber) {
        if (shouldAddFragment(ADD_CARD_TAG)) {
            AddCardFragment addCardFragment = AddCardFragment.from(mDropInRequest, cardNumber);
            replaceExistingFragment(addCardFragment, ADD_CARD_TAG);
        }
    }

    @Override
    public void finish() {
        super.finish();
        boolean isBottomSheetVisible = shouldAddFragment(BOTTOM_SHEET_TAG);
        if (isBottomSheetVisible) {
            // bottom sheet animates on its own
            overridePendingTransition(0, 0);
        }
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
            sendAnalyticsEvent("vaulted-card.select");
        }

        dropInViewModel.setDropInState(DropInState.WILL_FINISH);
        getDropInClient().shouldRequestThreeDSecureVerification(paymentMethodNonce, new ShouldRequestThreeDSecureVerification() {
            @Override
            public void onResult(boolean shouldRequestThreeDSecureVerification) {
                if (shouldRequestThreeDSecureVerification) {

                    getDropInClient().performThreeDSecureVerification(DropInActivity.this, paymentMethodNonce, new DropInResultCallback() {
                        @Override
                        public void onResult(@Nullable DropInResult dropInResult, @Nullable Exception error) {
                            if (dropInResult != null) {
                                finishWithDropInResult(dropInResult);
                            } else {
                                updateVaultedPaymentMethodNonces(true);
                                onError(error);
                            }
                        }
                    });
                } else {
                    final DropInResult dropInResult = new DropInResult();
                    dropInResult.paymentMethodNonce(paymentMethodNonce);

                    if (mDropInRequest.getCollectDeviceData()) {
                        getDropInClient().collectDeviceData(DropInActivity.this, new DataCollectorCallback() {
                            @Override
                            public void onResult(@Nullable String deviceData, @Nullable Exception error) {
                                if (deviceData != null) {
                                    dropInResult.deviceData(deviceData);
                                    finishWithDropInResult(dropInResult);
                                } else {
                                    updateVaultedPaymentMethodNonces(true);
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
        dropInViewModel.setDropInState(DropInState.WILL_FINISH);

        getDropInClient().tokenizeCard(card, new CardTokenizeCallback() {
            @Override
            public void onResult(@Nullable CardNonce cardNonce, @Nullable Exception error) {
                if (error != null) {
                    if (error instanceof ErrorWithResponse) {
                        dropInViewModel.setCardTokenizationError(error);
                    } else {
                        onError(error);
                    }
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
                            sendAnalyticsEvent("sdk.exit.success");
                            finishWithDropInResult(dropInResult);
                        }
                    });
                } else {
                    sendAnalyticsEvent("sdk.exit.success");

                    DropInResult dropInResult = new DropInResult();
                    dropInResult.paymentMethodNonce(paymentMethod);
                    finishWithDropInResult(dropInResult);
                }
            }
        });
    }

    private boolean willDeliverSuccessfulBrowserSwitchResult() {
        BrowserSwitchResult browserSwitchResult =
                getDropInClient().getBrowserSwitchResult(this);
        if (browserSwitchResult != null) {
            return (browserSwitchResult.getStatus() == BrowserSwitchStatus.SUCCESS);
        }
        return false;
    }
}
