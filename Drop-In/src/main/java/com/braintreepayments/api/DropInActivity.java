package com.braintreepayments.api;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.braintreepayments.api.dropin.R;
import com.google.android.material.snackbar.Snackbar;

public class DropInActivity extends AppCompatActivity {

    private static final String ADD_CARD_TAG = "ADD_CARD";
    private static final String CARD_DETAILS_TAG = "CARD_DETAILS";
    private static final String BOTTOM_SHEET_TAG = "BOTTOM_SHEET";

    @VisibleForTesting
    DropInViewModel dropInViewModel;

    @VisibleForTesting
    DropInRequest dropInRequest;

    @VisibleForTesting
    DropInInternalClient dropInInternalClient;
    private FragmentContainerView fragmentContainerView;

    @VisibleForTesting
    DropInResult pendingDropInResult;

    @VisibleForTesting
    AlertPresenter alertPresenter;

    @Override
    protected void onResume() {
        super.onResume();

        if (willDeliverSuccessfulBrowserSwitchResult()) {
            // when browser switch is successful, a tokenization http call will be made by DropInClient
            // show the loader to signal to the user that an asynchronous operation is underway
            dropInViewModel.setDropInState(DropInState.WILL_FINISH);
        }

        dropInInternalClient.deliverBrowserSwitchResult(this, this::onDropInResult);
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

        Intent intent = getIntent();

        Exception error =
            (Exception) intent.getSerializableExtra(DropInClient.EXTRA_AUTHORIZATION_ERROR);
        if (error != null) {
            // echo back error to merchant via activity result
            finishDropInWithError(error);
            return;
        }

        if (dropInInternalClient == null) {
            String authorization = intent.getStringExtra(DropInClient.EXTRA_AUTHORIZATION);
            String sessionId = intent.getStringExtra(DropInClient.EXTRA_SESSION_ID);
            DropInRequest dropInRequest = getDropInRequest(intent);
            dropInInternalClient = new DropInInternalClient(this, authorization, sessionId, dropInRequest);
        }

        alertPresenter = new AlertPresenter();
        dropInRequest = getDropInRequest(getIntent());

        dropInViewModel = new ViewModelProvider(this).get(DropInViewModel.class);
        fragmentContainerView = findViewById(R.id.fragment_container_view);

        getSupportFragmentManager().setFragmentResultListener(DropInEvent.REQUEST_KEY, this,
                (requestKey, result) -> onDropInEvent(DropInEvent.fromBundle(result)));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                dropInViewModel.setBottomSheetState(BottomSheetState.HIDE_REQUESTED);
            }
        });

        dropInViewModel.getBottomSheetState().observe(this, bottomSheetState -> {
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
        });

        showBottomSheetIfNecessary();
    }

    @VisibleForTesting
    void finishDropInWithError(Exception e) {
        setResult(RESULT_FIRST_USER, new Intent().putExtra(DropInResult.EXTRA_ERROR, e));
        finish();
    }

    private DropInRequest getDropInRequest(Intent intent) {
        Bundle bundle = intent.getParcelableExtra(DropInClient.EXTRA_CHECKOUT_REQUEST_BUNDLE);
        bundle.setClassLoader(DropInRequest.class.getClassLoader());
        return bundle.getParcelable(DropInClient.EXTRA_CHECKOUT_REQUEST);
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

    private void onSupportedPaymentMethodSelected(DropInEvent event) {
        if (getLifecycle().getCurrentState() == Lifecycle.State.RESUMED) {
            DropInPaymentMethod paymentMethodType =
                    event.getDropInPaymentMethodType(DropInEventProperty.SUPPORTED_PAYMENT_METHOD);
            startPaymentFlow(paymentMethodType);
        }
    }

    private void startPaymentFlow(DropInPaymentMethod paymentMethodType) {
        switch (paymentMethodType) {
            case GOOGLE_PAY:
                startGooglePayFlow();
                break;
            case PAYPAL:
                startPayPalFlow();
                break;
            case VENMO:
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
        alertPresenter.showConfirmNonceDeletionDialog(this, paymentMethodNonceToDelete, interaction -> {
            switch (interaction) {
                case POSITIVE:
                    sendAnalyticsEvent("manager.delete.confirmation.positive");
                    removePaymentMethodNonce(paymentMethodNonceToDelete);
                    break;
                case NEGATIVE:
                    sendAnalyticsEvent("manager.delete.confirmation.negative");
                    break;
            }
        });
    }

    @VisibleForTesting
    void removePaymentMethodNonce(PaymentMethodNonce paymentMethodNonceToDelete) {
        // proactively remove from view model
        dropInViewModel.removeVaultedPaymentMethodNonce(paymentMethodNonceToDelete);

        dropInInternalClient.deletePaymentMethod(DropInActivity.this, paymentMethodNonceToDelete, (deletedNonce, error) -> {
            if (deletedNonce != null) {
                sendAnalyticsEvent("manager.delete.succeeded");
            } else if (error instanceof PaymentMethodDeleteException) {
                sendAnalyticsEvent("manager.delete.failed");

                int snackBarTextResId = R.string.bt_vault_manager_delete_failure;
                alertPresenter.showSnackbarText(
                        fragmentContainerView, snackBarTextResId, Snackbar.LENGTH_LONG);
            } else {
                sendAnalyticsEvent("manager.unknown.failed");
                // TODO: determine how to handle unexpected error when deleting payment method (previously finished drop in)
                onError(error);
            }
        });
    }

    private void onSendAnalytics(DropInEvent event) {
        String eventName = event.getString(DropInEventProperty.ANALYTICS_EVENT_NAME);
        sendAnalyticsEvent(eventName);
    }

    private void sendAnalyticsEvent(String eventName) {
        dropInInternalClient.sendAnalyticsEvent(eventName);
    }

    private void refreshVaultedPaymentMethods() {
        // TODO: consider caching nonces or use a ViewModel for handling nonces
        // TODO: show loading indicator while fetching vaulted payment methods
        dropInInternalClient.getVaultedPaymentMethods(this, (paymentMethodNonceList, error) -> {
            if (paymentMethodNonceList != null) {
                dropInViewModel.setVaultedPaymentMethods(paymentMethodNonceList);
            } else if (error != null) {
                onError(error);
            }
        });
    }

    private void onDidShowBottomSheet() {
        dropInInternalClient.getSupportedPaymentMethods(this, (paymentMethods, error) -> {
            if (paymentMethods != null) {
                dropInViewModel.setSupportedPaymentMethods(paymentMethods);

                // TODO: consider pull to refresh to allow user to request an updated
                // instead of having this event respond to the visual presentation of supported
                // payment methods
                updateVaultedPaymentMethodNonces(false);
            } else {
                onError(error);
            }
        });
    }

    private void onDidHideBottomSheet() {
        finishDropInWithPendingResult(DropInExitTransition.FADE_OUT);
    }

    private void finishDropInWithPendingResult(DropInExitTransition transition) {
        if (pendingDropInResult != null) {
            sendAnalyticsEvent("sdk.exit.success");

            PaymentMethodNonce paymentMethodNonce = pendingDropInResult.getPaymentMethodNonce();
            dropInInternalClient.setLastUsedPaymentMethodType(paymentMethodNonce);

            Intent intent = new Intent()
                    .putExtra(DropInResult.EXTRA_DROP_IN_RESULT, pendingDropInResult);
            setResult(RESULT_OK, intent);
        } else {
            // assume drop in cancelled
            sendAnalyticsEvent("sdk.exit.canceled");
            setResult(RESULT_CANCELED);
        }

        finish();
        switch (transition) {
            case NO_ANIMATION:
                overridePendingTransition(0, 0);
                break;
            case FADE_OUT:
                overridePendingTransition(R.anim.bt_fade_in, R.anim.bt_fade_out);
                break;
        }
    }

    private void updateVaultedPaymentMethodNonces(boolean refetch) {
        dropInInternalClient.getAuthorization(new AuthorizationCallback() {
            @Override
            public void onAuthorizationResult(@Nullable Authorization authorization, @Nullable Exception authorizationError) {
                boolean clientTokenPresent = authorization instanceof ClientToken;
                if (clientTokenPresent) {
                    dropInInternalClient.getVaultedPaymentMethods(DropInActivity.this, (vaultedPaymentMethods, vaultedPaymentMethodsError) -> {
                        if (vaultedPaymentMethods != null) {
                            dropInViewModel.setVaultedPaymentMethods(vaultedPaymentMethods);
                        } else if (vaultedPaymentMethodsError != null) {
                            onError(vaultedPaymentMethodsError);
                        }
                    });
                }
            }
        });
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
                .setCustomAnimations(R.anim.bt_fade_in, R.anim.bt_fade_out)
                .replace(R.id.fragment_container_view, fragment, tag)
                .addToBackStack(null)
                .commit();
    }

    private void showBottomSheetIfNecessary() {
        // fragment manager will restore entire backstack on configuration change; here, we only
        // show the bottom sheet if no fragments are currently being displayed. This fixes an issue
        // where a configuration change causes card tokenization to appear idle after it is complete
        FragmentManager fragmentManager = getSupportFragmentManager();
        int numFragments = fragmentManager.getFragments().size();
        if (numFragments == 0) {
            BottomSheetFragment bottomSheetFragment = BottomSheetFragment.from(dropInRequest);
            replaceExistingFragment(bottomSheetFragment, BOTTOM_SHEET_TAG);
            dropInViewModel.setBottomSheetState(BottomSheetState.SHOW_REQUESTED);
        }
    }

    private void showCardDetailsFragment(final String cardNumber) {
        if (shouldAddFragment(CARD_DETAILS_TAG)) {
            dropInInternalClient.getAuthorization((authorization, authorizationError) -> {
                if (authorization != null) {
                    dropInInternalClient.getConfiguration((configuration, configurationError) -> {
                        if (configuration != null) {
                            boolean hasTokenizationKeyAuth = (authorization instanceof TokenizationKey);

                            CardDetailsFragment cardDetailsFragment = CardDetailsFragment.from(
                                    dropInRequest, cardNumber, configuration, hasTokenizationKeyAuth);
                            replaceExistingFragment(cardDetailsFragment, CARD_DETAILS_TAG);
                        } else {
                            finishDropInWithError(configurationError);
                        }
                    });
                } else {
                    finishDropInWithError(authorizationError);
                }
            });
        }
    }

    @VisibleForTesting
    void onError(final Exception error) {
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

        finishDropInWithError(error);
    }

    private void animateBottomSheetClosedAndFinishDropInWithResult(DropInResult dropInResult) {
        pendingDropInResult = dropInResult;

        if (isBottomSheetVisible()) {
            // when the bottom sheet transitions to the "HIDDEN" state; the activity will finish
            dropInViewModel.setBottomSheetState(BottomSheetState.HIDE_REQUESTED);
        } else {
            // no need to animate activity hidden since bottom sheet is not visible
            finishDropInWithPendingResult(DropInExitTransition.NO_ANIMATION);
        }
    }

    private void startPayPalFlow() {
        dropInInternalClient.tokenizePayPalRequest(this, error -> {
            if (error != null) {
                onError(error);
            }
        });
    }

    private void startGooglePayFlow() {
        dropInInternalClient.requestGooglePayPayment(this, error -> {
            if (error != null) {
                onError(error);
            }
        });
    }

    private void startVenmoFlow() {
        dropInInternalClient.tokenizeVenmoAccount(this, error -> {
            if (error != null) {
                onError(error);
            }
        });
    }

    private void onEditCardNumber(DropInEvent event) {
        startAddCardFlow(event.getString(DropInEventProperty.CARD_NUMBER));
    }

    private void prefetchSupportedCardTypes() {
        dropInInternalClient.getSupportedCardTypes((supportedCardTypes, error) -> {
            if (error != null) {
                onError(error);
            } else if (supportedCardTypes != null) {
                dropInViewModel.setSupportedCardTypes(supportedCardTypes);
            }
        });
    }

    private void startAddCardFlow(@Nullable String cardNumber) {
        // clear card tokenization error to prevent Error UI from a previous duplicate card submission
        dropInViewModel.setCardTokenizationError(null);
        // TODO: 👆 isolate transactional state within the calling fragment to prevent stale data
        // in the activity view model from causing UI bugs; in this case, we should migrate the card
        // tokenization error out of the activity view model's scope and contain the error state
        // within the scope of the CardDetailsFragment that's making the request

        if (shouldAddFragment(ADD_CARD_TAG)) {
            AddCardFragment addCardFragment = AddCardFragment.from(dropInRequest, cardNumber);
            replaceExistingFragment(addCardFragment, ADD_CARD_TAG);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        dropInInternalClient.handleActivityResult(this, requestCode, resultCode, data, this::onDropInResult);
    }

    private void onDropInResult(DropInResult dropInResult, Exception error) {
        if (dropInResult != null) {
            animateBottomSheetClosedAndFinishDropInWithResult(dropInResult);
        } else if (error instanceof UserCanceledException) {
            dropInViewModel.setUserCanceledError(error);
        } else {
            onError(error);
        }
    }

    private void onVaultedPaymentMethodSelected(DropInEvent event) {
        final PaymentMethodNonce paymentMethodNonce =
                event.getPaymentMethodNonce(DropInEventProperty.VAULTED_PAYMENT_METHOD);

        if (paymentMethodNonce instanceof CardNonce) {
            sendAnalyticsEvent("vaulted-card.select");
        }

        dropInViewModel.setDropInState(DropInState.WILL_FINISH);
        dropInInternalClient.shouldRequestThreeDSecureVerification(paymentMethodNonce, shouldRequestThreeDSecureVerification -> {
            if (shouldRequestThreeDSecureVerification) {
                dropInInternalClient.performThreeDSecureVerification(DropInActivity.this, paymentMethodNonce, (dropInResult, error) -> {
                    if (dropInResult != null) {
                        animateBottomSheetClosedAndFinishDropInWithResult(dropInResult);
                    } else {
                        updateVaultedPaymentMethodNonces(true);
                        onError(error);
                    }
                });
            } else {
                final DropInResult dropInResult = new DropInResult();
                dropInResult.setPaymentMethodNonce(paymentMethodNonce);
                dropInInternalClient.collectDeviceData(DropInActivity.this, (deviceData, error) -> {
                    if (deviceData != null) {
                        dropInResult.setDeviceData(deviceData);
                        animateBottomSheetClosedAndFinishDropInWithResult(dropInResult);
                    } else {
                        updateVaultedPaymentMethodNonces(true);
                        onError(error);
                    }
                });
            }
        });
    }

    private void onCardDetailsSubmit(DropInEvent event) {
        Card card = event.getCard(DropInEventProperty.CARD);
        dropInViewModel.setDropInState(DropInState.WILL_FINISH);

        dropInInternalClient.tokenizeCard(card, (cardNonce, error) -> {
            if (error != null) {
                if (error instanceof ErrorWithResponse) {
                    dropInViewModel.setCardTokenizationError(error);
                    dropInViewModel.setDropInState(DropInState.IDLE);
                } else {
                    onError(error);
                }
                return;
            }
            onPaymentMethodNonceCreated(cardNonce);
        });
    }

    private void onPaymentMethodNonceCreated(final PaymentMethodNonce paymentMethod) {
        dropInInternalClient.shouldRequestThreeDSecureVerification(paymentMethod, shouldRequestThreeDSecureVerification -> {
            if (shouldRequestThreeDSecureVerification) {
                dropInInternalClient.performThreeDSecureVerification(DropInActivity.this, paymentMethod, (dropInResult, error) -> {
                    if (error != null) {
                        onError(error);
                        return;
                    }
                    animateBottomSheetClosedAndFinishDropInWithResult(dropInResult);
                });
            } else {
                DropInResult dropInResult = new DropInResult();
                dropInResult.setPaymentMethodNonce(paymentMethod);
                dropInInternalClient.collectDeviceData(this, (deviceData, deviceDataError) -> {
                    if (deviceData != null) {
                        dropInResult.setDeviceData(deviceData);
                        animateBottomSheetClosedAndFinishDropInWithResult(dropInResult);
                    } else {
                        onError(deviceDataError);
                    }
                });
            }
        });
    }

    private boolean willDeliverSuccessfulBrowserSwitchResult() {
        BrowserSwitchResult browserSwitchResult =
                dropInInternalClient.getBrowserSwitchResult(this);
        if (browserSwitchResult != null) {
            return (browserSwitchResult.getStatus() == BrowserSwitchStatus.SUCCESS);
        }
        return false;
    }

    private boolean isBottomSheetVisible() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(BOTTOM_SHEET_TAG);
        if (fragment != null) {
            return fragment.isVisible();
        }
        return false;
    }
}
