package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.FragmentActivity;

import com.braintreepayments.cardform.utils.CardType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Used to launch Drop-in and handle results
 */
public class DropInClient {

    static final String EXTRA_CHECKOUT_REQUEST = "com.braintreepayments.api.EXTRA_CHECKOUT_REQUEST";
    static final String EXTRA_CHECKOUT_REQUEST_BUNDLE = "com.braintreepayments.api.EXTRA_CHECKOUT_REQUEST_BUNDLE";
    static final String EXTRA_SESSION_ID = "com.braintreepayments.api.EXTRA_SESSION_ID";
    static final String EXTRA_AUTHORIZATION = "com.braintreepayments.api.EXTRA_AUTHORIZATION";

    private static final String CARD_TYPE_UNION_PAY = "UnionPay";

    @VisibleForTesting
    final BraintreeClient braintreeClient;
    private final PaymentMethodClient paymentMethodClient;
    private final GooglePayClient googlePayClient;
    private final PayPalClient payPalClient;
    private final VenmoClient venmoClient;
    private final CardClient cardClient;
    private final UnionPayClient unionPayClient;

    private final DropInRequest dropInRequest;
    private final ThreeDSecureClient threeDSecureClient;
    private final DataCollector dataCollector;

    private final DropInSharedPreferences dropInSharedPreferences;

    private final PaymentMethodInspector paymentMethodInspector = new PaymentMethodInspector();

    private static DropInClientParams createDefaultParams(Context context, String authorization, DropInRequest dropInRequest, String sessionId) {
        BraintreeClient braintreeClient = new BraintreeClient(context, authorization, sessionId, IntegrationType.DROP_IN);
        return new DropInClientParams()
                .dropInRequest(dropInRequest)
                .braintreeClient(braintreeClient)
                .threeDSecureClient(new ThreeDSecureClient(braintreeClient))
                .paymentMethodClient(new PaymentMethodClient(braintreeClient))
                .payPalClient(new PayPalClient(braintreeClient))
                .venmoClient(new VenmoClient(braintreeClient))
                .cardClient(new CardClient(braintreeClient))
                .unionPayClient(new UnionPayClient(braintreeClient))
                .dataCollector(new DataCollector(braintreeClient))
                .googlePayClient(new GooglePayClient(braintreeClient))
                .dropInSharedPreferences(DropInSharedPreferences.getInstance());
    }

    public DropInClient(Context context, String authorization, DropInRequest dropInRequest) {
        this(context, authorization, null, dropInRequest);
    }

    DropInClient(Context context, String authorization, String sessionId, DropInRequest dropInRequest) {
        this(createDefaultParams(context, authorization, dropInRequest, sessionId));
    }

    @VisibleForTesting
    DropInClient(DropInClientParams params) {
        this.dropInRequest = params.getDropInRequest();
        this.braintreeClient = params.getBraintreeClient();
        this.googlePayClient = params.getGooglePayClient();
        this.paymentMethodClient = params.getPaymentMethodClient();
        this.threeDSecureClient = params.getThreeDSecureClient();
        this.payPalClient = params.getPayPalClient();
        this.venmoClient = params.getVenmoClient();
        this.cardClient = params.getCardClient();
        this.unionPayClient = params.getUnionPayClient();
        this.dataCollector = params.getDataCollector();
        this.dropInSharedPreferences = params.getDropInSharedPreferences();
    }

    Authorization getAuthorization() {
        return braintreeClient.getAuthorization();
    }

    void getConfiguration(ConfigurationCallback callback) {
        braintreeClient.getConfiguration(callback);
    }

    void sendAnalyticsEvent(String eventName) {
        braintreeClient.sendAnalyticsEvent(eventName);
    }

    void collectDeviceData(FragmentActivity activity, DataCollectorCallback callback) {
        dataCollector.collectDeviceData(activity, callback);
    }

    void performThreeDSecureVerification(final FragmentActivity activity, PaymentMethodNonce paymentMethodNonce, final DropInResultCallback callback) {
        final ThreeDSecureRequest threeDSecureRequest = dropInRequest.getThreeDSecureRequest();
        threeDSecureRequest.setNonce(paymentMethodNonce.getString());

        threeDSecureClient.performVerification(activity, threeDSecureRequest, (lookupResult, error) -> {
            if (lookupResult != null) {
                threeDSecureClient.continuePerformVerification(activity, threeDSecureRequest, lookupResult, (threeDSecureResult, continueError) -> {
                    if (continueError != null) {
                        callback.onResult(null, continueError);
                    } else if (threeDSecureResult != null) {
                        final DropInResult dropInResult = new DropInResult();
                        dropInResult.setPaymentMethodNonce(threeDSecureResult.getTokenizedCard());
                        dataCollector.collectDeviceData(activity, (deviceData, dataCollectionError) -> {
                            if (deviceData != null) {
                                dropInResult.setDeviceData(deviceData);
                                callback.onResult(dropInResult, null);
                            } else {
                                callback.onResult(null, dataCollectionError);
                            }
                        });
                    }
                });
            } else {
                callback.onResult(null, error);
            }
        });
    }

    void shouldRequestThreeDSecureVerification(PaymentMethodNonce paymentMethodNonce, final ShouldRequestThreeDSecureVerification callback) {
        if (paymentMethodCanPerformThreeDSecureVerification(paymentMethodNonce)) {
            braintreeClient.getConfiguration((configuration, error) -> {
                if (configuration == null) {
                    callback.onResult(false);
                    return;
                }

                boolean hasAmount = (dropInRequest.getThreeDSecureRequest() != null && !TextUtils.isEmpty(dropInRequest.getThreeDSecureRequest().getAmount()));
                boolean shouldRequestThreeDSecureVerification = configuration.isThreeDSecureEnabled() && hasAmount;
                callback.onResult(shouldRequestThreeDSecureVerification);
            });

        } else {
            callback.onResult(false);
        }
    }

    void tokenizePayPalRequest(FragmentActivity activity, PayPalFlowStartedCallback callback) {
        PayPalRequest paypalRequest = dropInRequest.getPayPalRequest();
        if (paypalRequest == null) {
            paypalRequest = new PayPalVaultRequest();
        }
        payPalClient.tokenizePayPalAccount(activity, paypalRequest, callback);
    }

    void requestGooglePayPayment(FragmentActivity activity, GooglePayRequestPaymentCallback callback) {
        googlePayClient.requestPayment(activity, dropInRequest.getGooglePayRequest(), callback);
    }

    void tokenizeVenmoAccount(FragmentActivity activity, VenmoTokenizeAccountCallback callback) {
        VenmoRequest venmoRequest = dropInRequest.getVenmoRequest();
        if (venmoRequest == null) {
            venmoRequest = new VenmoRequest(VenmoPaymentMethodUsage.SINGLE_USE);
        }
        venmoClient.tokenizeVenmoAccount(activity, venmoRequest, callback);
    }

    void deletePaymentMethod(FragmentActivity activity, PaymentMethodNonce paymentMethodNonce, DeletePaymentMethodNonceCallback callback) {
        paymentMethodClient.deletePaymentMethod(activity, paymentMethodNonce, callback);
    }

    void tokenizeCard(Card card, CardTokenizeCallback callback) {
        cardClient.tokenize(card, callback);
    }

    void fetchUnionPayCapabilities(String cardNumber, UnionPayFetchCapabilitiesCallback callback) {
        unionPayClient.fetchCapabilities(cardNumber, callback);
    }

    void enrollUnionPay(UnionPayCard unionPayCard, UnionPayEnrollCallback callback) {
        unionPayClient.enroll(unionPayCard, callback);
    }

    void tokenizeUnionPay(UnionPayCard unionPayCard, UnionPayTokenizeCallback callback) {
        unionPayClient.tokenize(unionPayCard, callback);
    }

    BrowserSwitchResult getBrowserSwitchResult(FragmentActivity activity) {
        return braintreeClient.getBrowserSwitchResult(activity);
    }

    void deliverBrowserSwitchResult(final FragmentActivity activity, final DropInResultCallback callback) {
        BrowserSwitchResult browserSwitchResult = braintreeClient.deliverBrowserSwitchResult(activity);
        if (browserSwitchResult != null) {
            int requestCode = browserSwitchResult.getRequestCode();

            switch (requestCode) {
                case BraintreeRequestCodes.PAYPAL:
                    payPalClient.onBrowserSwitchResult(browserSwitchResult, (payPalAccountNonce, error) ->
                            notifyDropInResult(activity, payPalAccountNonce, error, callback));
                    break;
                case BraintreeRequestCodes.THREE_D_SECURE:
                    threeDSecureClient.onBrowserSwitchResult(browserSwitchResult, (threeDSecureResult, error) -> {
                        PaymentMethodNonce paymentMethodNonce = null;
                        if (threeDSecureResult != null) {
                            paymentMethodNonce = threeDSecureResult.getTokenizedCard();
                        }
                        notifyDropInResult(activity, paymentMethodNonce, error, callback);
                    });
                    break;
            }
        }
    }

    void handleActivityResult(FragmentActivity activity, int requestCode, int resultCode, @Nullable Intent data, DropInResultCallback callback) {
        switch (requestCode) {
            case BraintreeRequestCodes.THREE_D_SECURE:
                handleThreeDSecureActivityResult(activity, resultCode, data, callback);
                return;
            case BraintreeRequestCodes.GOOGLE_PAY:
                handleGooglePayActivityResult(activity, resultCode, data, callback);
                return;
            case BraintreeRequestCodes.VENMO:
                handleVenmoActivityResult(activity, resultCode, data, callback);
        }
    }

    void handleThreeDSecureActivityResult(final FragmentActivity activity, int resultCode, Intent data, final DropInResultCallback callback) {
        threeDSecureClient.onActivityResult(resultCode, data, (threeDSecureResult, error) -> {
            PaymentMethodNonce paymentMethodNonce = null;
            if (threeDSecureResult != null) {
                paymentMethodNonce = threeDSecureResult.getTokenizedCard();
            }
            notifyDropInResult(activity, paymentMethodNonce, error, callback);
        });
    }

    void handleGooglePayActivityResult(final FragmentActivity activity, int resultCode, Intent data, final DropInResultCallback callback) {
        googlePayClient.onActivityResult(resultCode, data, (paymentMethodNonce, error) ->
                notifyDropInResult(activity, paymentMethodNonce, error, callback));
    }

    void handleVenmoActivityResult(final FragmentActivity activity, int resultCode, Intent data, final DropInResultCallback callback) {
        venmoClient.onActivityResult(activity, resultCode, data, (venmoAccountNonce, error) ->
                notifyDropInResult(activity, venmoAccountNonce, error, callback));
    }

    private void notifyDropInResult(FragmentActivity activity, PaymentMethodNonce paymentMethodNonce, Exception dropInResultError, final DropInResultCallback callback) {
        if (dropInResultError != null) {
            callback.onResult(null, dropInResultError);
            return;
        }

        final DropInResult dropInResult = new DropInResult();
        dropInResult.setPaymentMethodNonce(paymentMethodNonce);
        dataCollector.collectDeviceData(activity, (deviceData, dataCollectionError) -> {
            if (dataCollectionError != null) {
                callback.onResult(null, dataCollectionError);
                return;
            }

            if (deviceData != null) {
                dropInResult.setDeviceData(deviceData);
                callback.onResult(dropInResult, null);
            }
        });
    }

    private boolean paymentMethodCanPerformThreeDSecureVerification(final PaymentMethodNonce paymentMethodNonce) {
        if (paymentMethodNonce instanceof CardNonce) {
            return true;
        }

        if (paymentMethodNonce instanceof GooglePayCardNonce) {
            return !((GooglePayCardNonce) paymentMethodNonce).isNetworkTokenized();
        }

        return false;
    }

    void getSupportedPaymentMethods(final FragmentActivity activity, final GetSupportedPaymentMethodsCallback callback) {
        braintreeClient.getConfiguration((configuration, error) -> {
            if (error != null) {
                callback.onResult(null, error);
                return;
            }

            if (!dropInRequest.isGooglePayDisabled()) {
                googlePayClient.isReadyToPay(activity, (isReadyToGooglePay, isReadyToPayError) -> {
                    List<DropInPaymentMethod> availablePaymentMethods =
                            filterSupportedPaymentMethods(activity, configuration, isReadyToGooglePay);
                    callback.onResult(availablePaymentMethods, null);
                });
            } else {
                List<DropInPaymentMethod> availablePaymentMethods =
                        filterSupportedPaymentMethods(activity, configuration, false);
                callback.onResult(availablePaymentMethods, null);
            }
        });
    }

    private List<DropInPaymentMethod> filterSupportedPaymentMethods(Context context, Configuration configuration, boolean showGooglePay) {
        List<DropInPaymentMethod> availablePaymentMethods = new ArrayList<>();

        if (!dropInRequest.isPayPalDisabled() && configuration.isPayPalEnabled()) {
            availablePaymentMethods.add(DropInPaymentMethod.PAYPAL);
        }

        if (!dropInRequest.isVenmoDisabled() && configuration.isVenmoEnabled() && venmoClient.isVenmoAppSwitchAvailable(context)) {
            availablePaymentMethods.add(DropInPaymentMethod.VENMO);
        }

        if (!dropInRequest.isCardDisabled()) {
            Set<String> supportedCardTypes =
                    new HashSet<>(configuration.getSupportedCardTypes());
            if (!configuration.isUnionPayEnabled()) {
                supportedCardTypes.remove(CARD_TYPE_UNION_PAY);
            }
            if (supportedCardTypes.size() > 0) { 
                availablePaymentMethods.add(DropInPaymentMethod.UNKNOWN);
            }
        }

        if (showGooglePay) {
            if (!dropInRequest.isGooglePayDisabled()) {
                availablePaymentMethods.add(DropInPaymentMethod.GOOGLE_PAY);
            }
        }
        return availablePaymentMethods;
    }

    void getSupportedCardTypes(final GetSupportedCardTypesCallback callback) {
        braintreeClient.getConfiguration((configuration, error) -> {
            if (configuration != null) {
                List<CardType> supportedCardTypes = new ArrayList<>();
                for (String cardTypeAsString : configuration.getSupportedCardTypes()) {
                    CardType cardType = paymentMethodInspector.getCardTypeFromString(cardTypeAsString);
                    if (cardType != null) {
                        supportedCardTypes.add(cardType);
                    }
                }

                if (!configuration.isUnionPayEnabled()) {
                    supportedCardTypes.remove(CardType.UNIONPAY);
                }
                callback.onResult(supportedCardTypes, null);
            } else {
                callback.onResult(null, error);
            }
        });
    }

    /**
     * Called to launch a {@link DropInActivity}
     *
     * @param activity the current {@link FragmentActivity}
     * @param requestCode the request code for the activity that will be launched
     */
    public void launchDropInForResult(FragmentActivity activity, int requestCode) {
        Bundle dropInRequestBundle = new Bundle();
        dropInRequestBundle.putParcelable(EXTRA_CHECKOUT_REQUEST, dropInRequest);
        Intent intent = new Intent(activity, DropInActivity.class)
                .putExtra(EXTRA_CHECKOUT_REQUEST_BUNDLE, dropInRequestBundle)
                .putExtra(EXTRA_SESSION_ID, braintreeClient.getSessionId())
                .putExtra(EXTRA_AUTHORIZATION, braintreeClient.getAuthorization().toString());
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Called to get a user's existing payment method, if any. 
     * The payment method returned is not guaranteed to be the most recently added payment method.
     * If your user already has an existing payment method, you may not need to show Drop-In.
     * <p>
     * Note: a client token must be used and will only return a payment method if it contains a
     * customer id.
     *
     * @param activity the current {@link FragmentActivity}
     * @param callback callback for handling result
     */
     // NEXT_MAJOR_VERSION: - update this function name to more accurately represent the behavior of the function
    public void fetchMostRecentPaymentMethod(FragmentActivity activity, final FetchMostRecentPaymentMethodCallback callback) {
        boolean isClientToken = braintreeClient.getAuthorization() instanceof ClientToken;
        if (!isClientToken) {
            InvalidArgumentException error = new InvalidArgumentException("DropInClient#fetchMostRecentPaymentMethods() must " +
                    "be called with a client token");
            callback.onResult(null, error);
            return;
        }

        DropInPaymentMethod lastUsedPaymentMethod =
            dropInSharedPreferences.getLastUsedPaymentMethod(activity);

        if (lastUsedPaymentMethod == DropInPaymentMethod.GOOGLE_PAY) {
            googlePayClient.isReadyToPay(activity, (isReadyToPay, error) -> {
                if (isReadyToPay) {
                    DropInResult result = new DropInResult();
                    result.setPaymentMethodType(DropInPaymentMethod.GOOGLE_PAY);
                    callback.onResult(result, null);
                } else {
                    getPaymentMethodNonces(callback);
                }
            });
        } else {
            getPaymentMethodNonces(callback);
        }
    }

    private void getPaymentMethodNonces(final FetchMostRecentPaymentMethodCallback callback) {
        paymentMethodClient.getPaymentMethodNonces((paymentMethodNonceList, error) -> {
            if (paymentMethodNonceList != null) {
                DropInResult result = new DropInResult();
                if (paymentMethodNonceList.size() > 0) {
                    PaymentMethodNonce paymentMethod = paymentMethodNonceList.get(0);
                    result.setPaymentMethodNonce(paymentMethod);
                }
                callback.onResult(result, null);
            } else if (error != null) {
                callback.onResult(null, error);
            }
        });
    }

    void getVaultedPaymentMethods(final FragmentActivity activity, final GetPaymentMethodNoncesCallback callback) {
        // TODO: cache nonces in ViewModel and allow refresh of vaulted payment methods instead of having a refetch parameter

        braintreeClient.getConfiguration((configuration, error) -> {
            if (error != null) {
                callback.onResult(null, error);
                return;
            }

            paymentMethodClient.getPaymentMethodNonces((paymentMethodNonces, getPaymentMethodNoncesError) -> {
                if (getPaymentMethodNoncesError != null) {
                    callback.onResult(null, getPaymentMethodNoncesError);
                } else if (paymentMethodNonces != null) {
                    if (!dropInRequest.isGooglePayDisabled()) {
                        googlePayClient.isReadyToPay(activity, (isReadyToPay, isReadyToPayError) -> {
                            AvailablePaymentMethodNonceList availablePaymentMethodNonceList =
                                    new AvailablePaymentMethodNonceList(configuration, paymentMethodNonces, dropInRequest, isReadyToPay);
                            callback.onResult(availablePaymentMethodNonceList.getItems(), null);
                        });
                    } else {
                        AvailablePaymentMethodNonceList availablePaymentMethodNonceList =
                                new AvailablePaymentMethodNonceList(configuration, paymentMethodNonces, dropInRequest, false);
                        callback.onResult(availablePaymentMethodNonceList.getItems(), null);
                    }
                }
            });
        });
    }

    void setLastUsedPaymentMethodType(Context context, PaymentMethodNonce paymentMethodNonce) {
        Context appContext = context.getApplicationContext();
        dropInSharedPreferences.setLastUsedPaymentMethod(appContext, paymentMethodNonce);
    }

    void onDropInResult(DropInResult dropInResult) {

    }
}
