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
import androidx.lifecycle.ViewModelProvider;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.cardform.utils.CardType;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class DropInActivity extends AppCompatActivity {

    private static final String ADD_CARD_TAG = "ADD_CARD";
    private static final String CARD_DETAILS_TAG = "CARD_DETAILS";
    private static final String BOTTOM_SHEET_TAG = "BOTTOM_SHEET";

    @VisibleForTesting
    DropInViewModel dropInViewModel;

    @VisibleForTesting
    DropInRequest dropInRequest;

    @VisibleForTesting
    DropInClient dropInClient;
    private FragmentContainerView fragmentContainerView;

    @VisibleForTesting
    DropInResult pendingDropInResult;

    @VisibleForTesting
    boolean clientTokenPresent;

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

        dropInClient.deliverBrowserSwitchResult(this, this::onDropInResult);
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

        if (dropInClient == null) {
            Intent intent = getIntent();
            String authorization = intent.getStringExtra(DropInClient.EXTRA_AUTHORIZATION);
            String sessionId = intent.getStringExtra(DropInClient.EXTRA_SESSION_ID);
            DropInRequest dropInRequest = getDropInRequest(intent);
            dropInClient = new DropInClient(this, authorization, sessionId, dropInRequest);
        }

        if (dropInClient.getAuthorization() instanceof InvalidAuthorization) {
            finishDropInWithError(
                    new InvalidArgumentException("Tokenization Key or Client Token was invalid."));
            return;
        }

        alertPresenter = new AlertPresenter();
        dropInRequest = getDropInRequest(getIntent());
        clientTokenPresent = dropInClient.getAuthorization() instanceof ClientToken;

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

        showBottomSheet();
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
        DropInPaymentMethodType paymentMethodType =
                event.getDropInPaymentMethodType(DropInEventProperty.SUPPORTED_PAYMENT_METHOD);
        startPaymentFlow(paymentMethodType);
    }

    private void startPaymentFlow(DropInPaymentMethodType paymentMethodType) {
        switch (paymentMethodType) {
            case GOOGLE_PAY:
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

        dropInClient.deletePaymentMethod(DropInActivity.this, paymentMethodNonceToDelete, (deletedNonce, error) -> {
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
        dropInClient.sendAnalyticsEvent(eventName);
    }

    private void refreshVaultedPaymentMethods() {
        // TODO: consider caching nonces or use a ViewModel for handling nonces
        // TODO: show loading indicator while fetching vaulted payment methods
        dropInClient.getVaultedPaymentMethods(this, (paymentMethodNonceList, error) -> {
            if (paymentMethodNonceList != null) {
                dropInViewModel.setVaultedPaymentMethods(paymentMethodNonceList);
            } else if (error != null) {
                onError(error);
            }
        });
    }

    private void onDidShowBottomSheet() {
        dropInClient.getSupportedPaymentMethods(this, (paymentMethods, error) -> {
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
            dropInClient.setLastUsedPaymentMethodType(pendingDropInResult.getPaymentMethodNonce());

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
        if (clientTokenPresent) {
            dropInClient.getVaultedPaymentMethods(this, (vaultedPaymentMethods, error) -> {
                if (vaultedPaymentMethods != null) {
                    dropInViewModel.setVaultedPaymentMethods(vaultedPaymentMethods);
                } else if (error != null) {
                    onError(error);
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
                .setCustomAnimations(R.anim.bt_fade_in, R.anim.bt_fade_out)
                .replace(R.id.fragment_container_view, fragment, tag)
                .addToBackStack(null)
                .commit();
    }

    private void showBottomSheet() {
        if (shouldAddFragment(BOTTOM_SHEET_TAG)) {
            BottomSheetFragment bottomSheetFragment = BottomSheetFragment.from(dropInRequest);
            replaceExistingFragment(bottomSheetFragment, BOTTOM_SHEET_TAG);
        }
        dropInViewModel.setBottomSheetState(BottomSheetState.SHOW_REQUESTED);
    }

    private void showCardDetailsFragment(final String cardNumber) {
        if (shouldAddFragment(CARD_DETAILS_TAG)) {
            dropInClient.getConfiguration((configuration, error) -> {
                if (configuration != null) {
                    // TODO: implement dropInClient.hasAuthType(AuthType.TOKENIZATION_KEY)
                    boolean hasTokenizationKeyAuth =
                            Authorization.isTokenizationKey(dropInClient.getAuthorization().toString());

                    CardDetailsFragment cardDetailsFragment = CardDetailsFragment.from(
                            dropInRequest, cardNumber, configuration, hasTokenizationKeyAuth);
                    replaceExistingFragment(cardDetailsFragment, CARD_DETAILS_TAG);
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
        dropInClient.tokenizePayPalRequest(this, error -> {
            if (error != null) {
                onError(error);
            }
        });
    }

    private void startGooglePayFlow() {
        dropInClient.requestGooglePayPayment(this, error -> {
            if (error != null) {
                onError(error);
            }
        });
    }

    private void startVenmoFlow() {
        dropInClient.tokenizeVenmoAccount(this, error -> {
            if (error != null) {
                onError(error);
            }
        });
    }

    private void onEditCardNumber(DropInEvent event) {
        startAddCardFlow(event.getString(DropInEventProperty.CARD_NUMBER));
    }

    private void prefetchSupportedCardTypes() {
        dropInClient.getSupportedPaymentMethods((supportedCardTypes, error) -> {
            if (error != null) {
                onError(error);
            } else if (supportedCardTypes != null) {
                dropInViewModel.setSupportedCardTypes(
                        mapPaymentMethodsToCardTypes(supportedCardTypes));
            }
        });
    }

    static List<CardType> mapPaymentMethodsToCardTypes(List<DropInPaymentMethodType> supportedPaymentMethods) {
        List<CardType> convertedCardTypes = new ArrayList<>();
        for (DropInPaymentMethodType paymentMethod : supportedPaymentMethods) {
            if (paymentMethod != DropInPaymentMethodType.UNKNOWN && paymentMethod.getCardType() != null) {
                convertedCardTypes.add(paymentMethod.getCardType());
            }
        }
        return convertedCardTypes;
    }

    private void startAddCardFlow(@Nullable String cardNumber) {
        if (shouldAddFragment(ADD_CARD_TAG)) {
            AddCardFragment addCardFragment = AddCardFragment.from(dropInRequest, cardNumber);
            replaceExistingFragment(addCardFragment, ADD_CARD_TAG);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        dropInClient.handleActivityResult(this, requestCode, resultCode, data, this::onDropInResult);
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
        dropInClient.shouldRequestThreeDSecureVerification(paymentMethodNonce, shouldRequestThreeDSecureVerification -> {
            if (shouldRequestThreeDSecureVerification) {
                dropInClient.performThreeDSecureVerification(DropInActivity.this, paymentMethodNonce, (dropInResult, error) -> {
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
                dropInClient.collectDeviceData(DropInActivity.this, (deviceData, error) -> {
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

        dropInClient.tokenizeCard(card, (cardNonce, error) -> {
            if (error != null) {
                if (error instanceof ErrorWithResponse) {
                    dropInViewModel.setCardTokenizationError(error);
                } else {
                    onError(error);
                }
                return;
            }
            onPaymentMethodNonceCreated(cardNonce);
        });
    }

    private void onPaymentMethodNonceCreated(final PaymentMethodNonce paymentMethod) {
        dropInClient.shouldRequestThreeDSecureVerification(paymentMethod, shouldRequestThreeDSecureVerification -> {
            if (shouldRequestThreeDSecureVerification) {
                dropInClient.performThreeDSecureVerification(DropInActivity.this, paymentMethod, (dropInResult, error) -> {
                    if (error != null) {
                        onError(error);
                        return;
                    }
                    animateBottomSheetClosedAndFinishDropInWithResult(dropInResult);
                });
            } else {
                DropInResult dropInResult = new DropInResult();
                dropInResult.setPaymentMethodNonce(paymentMethod);
                animateBottomSheetClosedAndFinishDropInWithResult(dropInResult);
            }
        });
    }

    private boolean willDeliverSuccessfulBrowserSwitchResult() {
        BrowserSwitchResult browserSwitchResult =
                dropInClient.getBrowserSwitchResult(this);
        if (browserSwitchResult != null) {
            return (browserSwitchResult.getStatus() == BrowserSwitchStatus.SUCCESS);
        }
        return false;
    }

    private boolean isBottomSheetVisible() {
        return !shouldAddFragment(BOTTOM_SHEET_TAG);
    }
}
