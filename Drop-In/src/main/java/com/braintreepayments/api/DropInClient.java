package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Used to launch Drop-in and handle results
 */
public class DropInClient {

    public static final String EXTRA_CHECKOUT_REQUEST = "com.braintreepayments.api.EXTRA_CHECKOUT_REQUEST";
    public static final String EXTRA_SESSION_ID = "com.braintreepayments.api.EXTRA_SESSION_ID";
    public static final String EXTRA_AUTHORIZATION = "com.braintreepayments.api.EXTRA_AUTHORIZATION";

    static final String LAST_USED_PAYMENT_METHOD_TYPE =
            "com.braintreepayments.api.dropin.LAST_USED_PAYMENT_METHOD_TYPE";

    private final BraintreeClient braintreeClient;
    private final PaymentMethodClient paymentMethodClient;
    private final GooglePayClient googlePayClient;
    private final PayPalClient payPalClient;
    private final VenmoClient venmoClient;
    private final CardClient cardClient;
    private final UnionPayClient unionPayClient;

    private final DropInRequest dropInRequest;
    private final ThreeDSecureClient threeDSecureClient;
    private final DataCollector dataCollector;

    private static DropInClientParams createDefaultParams(Context context, String authorization, DropInRequest dropInRequest) {
        BraintreeClient braintreeClient = new BraintreeClient(context, authorization);
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
                .googlePayClient(new GooglePayClient(braintreeClient));
    }

    public DropInClient(Context context, String authorization, DropInRequest dropInRequest) {
        this(context, authorization, null, dropInRequest);
    }

    DropInClient(Context context, String authorization, String sessionId, DropInRequest dropInRequest) {
        // TODO: instantiate a BraintreeClient with the input sessionId
        this(createDefaultParams(context, authorization, dropInRequest));
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

        threeDSecureClient.performVerification(activity, threeDSecureRequest, new ThreeDSecureResultCallback() {
            @Override
            public void onResult(@Nullable ThreeDSecureResult lookupResult, @Nullable Exception error) {
                if (lookupResult != null) {
                    threeDSecureClient.continuePerformVerification(activity, threeDSecureRequest, lookupResult, new ThreeDSecureResultCallback() {
                        @Override
                        public void onResult(@Nullable ThreeDSecureResult threeDSecureResult, @Nullable Exception error) {
                            if (error != null) {
                                callback.onResult(null, error);
                            } else if (threeDSecureResult != null) {
                                final DropInResult dropInResult = new DropInResult();
                                dropInResult.paymentMethodNonce(threeDSecureResult.getTokenizedCard());
                                dataCollector.collectDeviceData(activity, new DataCollectorCallback() {
                                    @Override
                                    public void onResult(@Nullable String deviceData, @Nullable Exception error) {
                                        if (deviceData != null) {
                                            dropInResult.deviceData(deviceData);
                                            callback.onResult(dropInResult, null);
                                        } else {
                                            callback.onResult(null, error);
                                        }
                                    }
                                });
                            }
                        }
                    });
                } else {
                    callback.onResult(null, error);
                }
            }
        });
    }

    void shouldRequestThreeDSecureVerification(PaymentMethodNonce paymentMethodNonce, final ShouldRequestThreeDSecureVerification callback) {
        if (paymentMethodCanPerformThreeDSecureVerification(paymentMethodNonce)) {
            braintreeClient.getConfiguration(new ConfigurationCallback() {
                @Override
                public void onResult(@Nullable Configuration configuration, @Nullable Exception error) {
                    if (configuration == null) {
                        callback.onResult(false);
                        return;
                    }

                    boolean hasAmount = (dropInRequest.getThreeDSecureRequest() != null && !TextUtils.isEmpty(dropInRequest.getThreeDSecureRequest().getAmount()));

                    boolean shouldRequestThreeDSecureVerification =
                            dropInRequest.getRequestThreeDSecureVerification()
                                    && configuration.isThreeDSecureEnabled()
                                    && hasAmount;
                    callback.onResult(shouldRequestThreeDSecureVerification);
                }
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

    public void deliverBrowserSwitchResult(final FragmentActivity activity, final DropInResultCallback callback) {
        BrowserSwitchResult browserSwitchResult = braintreeClient.deliverBrowserSwitchResult(activity);
        if (browserSwitchResult != null) {
            int requestCode = browserSwitchResult.getRequestCode();

            switch (requestCode) {
                case BraintreeRequestCodes.PAYPAL:
                    payPalClient.onBrowserSwitchResult(browserSwitchResult, new PayPalBrowserSwitchResultCallback() {
                        @Override
                        public void onResult(@Nullable PayPalAccountNonce payPalAccountNonce, @Nullable Exception error) {
                            notifyDropInResult(activity, payPalAccountNonce, error, callback);
                        }
                    });
                    break;
                case BraintreeRequestCodes.THREE_D_SECURE:
                    threeDSecureClient.onBrowserSwitchResult(browserSwitchResult, new ThreeDSecureResultCallback() {
                        @Override
                        public void onResult(@Nullable ThreeDSecureResult threeDSecureResult, @Nullable Exception error) {
                            PaymentMethodNonce paymentMethodNonce = null;
                            if (threeDSecureResult != null) {
                                paymentMethodNonce = threeDSecureResult.getTokenizedCard();
                            }
                            notifyDropInResult(activity, paymentMethodNonce, error, callback);
                        }
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
        threeDSecureClient.onActivityResult(resultCode, data, new ThreeDSecureResultCallback() {
            @Override
            public void onResult(@Nullable ThreeDSecureResult threeDSecureResult, @Nullable Exception error) {
                PaymentMethodNonce paymentMethodNonce = null;
                if (threeDSecureResult != null) {
                    paymentMethodNonce = threeDSecureResult.getTokenizedCard();
                }
                notifyDropInResult(activity, paymentMethodNonce, error, callback);
            }
        });
    }

    void handleGooglePayActivityResult(final FragmentActivity activity, int resultCode, Intent data, final DropInResultCallback callback) {
        googlePayClient.onActivityResult(resultCode, data, new GooglePayOnActivityResultCallback() {
            @Override
            public void onResult(@Nullable PaymentMethodNonce paymentMethodNonce, @Nullable Exception error) {
               notifyDropInResult(activity, paymentMethodNonce, error, callback);
            }
        });
    }

    void handleVenmoActivityResult(final FragmentActivity activity, int resultCode, Intent data, final DropInResultCallback callback) {
        venmoClient.onActivityResult(activity, resultCode, data, new VenmoOnActivityResultCallback() {
            @Override
            public void onResult(@Nullable VenmoAccountNonce venmoAccountNonce, @Nullable Exception error) {
                notifyDropInResult(activity, venmoAccountNonce, error, callback);
            }
        });
    }

    private void notifyDropInResult(FragmentActivity activity, PaymentMethodNonce paymentMethodNonce, Exception dropInResultError, final DropInResultCallback callback) {
        if (dropInResultError != null) {
            callback.onResult(null, dropInResultError);
            return;
        }

        final DropInResult dropInResult = new DropInResult()
                .paymentMethodNonce(paymentMethodNonce);
        dataCollector.collectDeviceData(activity, new DataCollectorCallback() {
            @Override
            public void onResult(@Nullable String deviceData, @Nullable Exception dataCollectionError) {
                if (dataCollectionError != null) {
                    callback.onResult(null, dataCollectionError);
                    return;
                }

                if (deviceData != null) {
                    dropInResult.deviceData(deviceData);
                    callback.onResult(dropInResult, null);
                }
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
        braintreeClient.getConfiguration(new ConfigurationCallback() {
            @Override
            public void onResult(@Nullable final Configuration configuration, @Nullable Exception error) {
                if (error != null) {
                    callback.onResult(null, error);
                    return;
                }

                if (!dropInRequest.isGooglePayDisabled()) {
                    googlePayClient.isReadyToPay(activity, new GooglePayIsReadyToPayCallback() {
                        @Override
                        public void onResult(boolean isReadyToGooglePay, Exception error) {

                            List<DropInPaymentMethodType> availablePaymentMethods =
                                filterSupportedPaymentMethods(configuration, isReadyToGooglePay);
                            callback.onResult(availablePaymentMethods, null);
                        }
                    });
                } else {
                    List<DropInPaymentMethodType> availablePaymentMethods =
                            filterSupportedPaymentMethods(configuration, false);
                    callback.onResult(availablePaymentMethods, null);
                }
            }
        });
    }

    private List<DropInPaymentMethodType> filterSupportedPaymentMethods(Configuration configuration, boolean showGooglePay) {
        List<DropInPaymentMethodType> availablePaymentMethods = new ArrayList<>();

        if (!dropInRequest.isPayPalDisabled() && configuration.isPayPalEnabled()) {
            availablePaymentMethods.add(DropInPaymentMethodType.PAYPAL);
        }

        if (!dropInRequest.isVenmoDisabled() && configuration.isVenmoEnabled()) {
            availablePaymentMethods.add(DropInPaymentMethodType.PAY_WITH_VENMO);
        }

        if (!dropInRequest.isCardDisabled()) {
            Set<String> supportedCardTypes =
                    new HashSet<>(configuration.getSupportedCardTypes());
            if (!configuration.isUnionPayEnabled()) {
                supportedCardTypes.remove(DropInPaymentMethodType.UNIONPAY.getCanonicalName());
            }
            if (supportedCardTypes.size() > 0) {
                availablePaymentMethods.add(DropInPaymentMethodType.UNKNOWN);
            }
        }

        if (showGooglePay) {
            if (!dropInRequest.isGooglePayDisabled()) {
                availablePaymentMethods.add(DropInPaymentMethodType.GOOGLE_PAYMENT);
            }
        }
        return availablePaymentMethods;
    }

    void getSupportedCardTypes(final GetSupportedCardTypesCallback callback) {
        braintreeClient.getConfiguration(new ConfigurationCallback() {
            @Override
            public void onResult(@Nullable Configuration configuration, @Nullable Exception error) {
                if (configuration != null) {
                    List<String> supportedCardTypes = new ArrayList<>(configuration.getSupportedCardTypes());
                    if (!configuration.isUnionPayEnabled()) {
                        supportedCardTypes.remove(DropInPaymentMethodType.UNIONPAY.getCanonicalName());
                    }
                    callback.onResult(supportedCardTypes, null);
                } else {
                    callback.onResult(null, error);
                }
            }
        });
    }

    public void launchDropInForResult(FragmentActivity activity, int requestCode) {
        Intent intent = new Intent(activity, DropInActivity.class)
                .putExtra(EXTRA_CHECKOUT_REQUEST, dropInRequest)
                .putExtra(EXTRA_SESSION_ID, braintreeClient.getSessionId())
                .putExtra(EXTRA_AUTHORIZATION, braintreeClient.getAuthorization().toString());
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Called to get a user's existing payment method, if any. If your user already has an existing
     * payment method, you may not need to show Drop-In.
     * <p>
     * Note: a client token must be used and will only return a payment method if it contains a
     * customer id.
     *
     * @param activity the current {@link AppCompatActivity}
     * @param callback callback for handling result
     */
    public void fetchMostRecentPaymentMethod(FragmentActivity activity, final FetchMostRecentPaymentMethodCallback callback) {
        boolean isClientToken = braintreeClient.getAuthorization() instanceof ClientToken;
        if (!isClientToken) {
            InvalidArgumentException error = new InvalidArgumentException("DropInClient#fetchMostRecentPaymentMethods() must " +
                    "be called with a client token");
            callback.onResult(null, error);
            return;
        }

        final DropInPaymentMethodType lastUsedPaymentMethodType = DropInPaymentMethodType.forType(BraintreeSharedPreferences.getSharedPreferences(activity)
                .getString(LAST_USED_PAYMENT_METHOD_TYPE, null));

        if (lastUsedPaymentMethodType == DropInPaymentMethodType.GOOGLE_PAYMENT) {
            googlePayClient.isReadyToPay(activity, new GooglePayIsReadyToPayCallback() {
                @Override
                public void onResult(boolean isReadyToPay, Exception error) {
                    if (isReadyToPay) {
                        DropInResult result = new DropInResult();
                        result.setPaymentMethodType(lastUsedPaymentMethodType);
                        callback.onResult(result, null);
                    } else {
                        getPaymentMethodNonces(callback);
                    }
                }
            });
        } else {
            getPaymentMethodNonces(callback);
        }
    }

    private void getPaymentMethodNonces(final FetchMostRecentPaymentMethodCallback callback) {
        paymentMethodClient.getPaymentMethodNonces(new GetPaymentMethodNoncesCallback() {
            @Override
            public void onResult(@Nullable List<PaymentMethodNonce> paymentMethodNonceList, @Nullable Exception error) {
                if (paymentMethodNonceList != null) {
                    DropInResult result = new DropInResult();
                    if (paymentMethodNonceList.size() > 0) {
                        PaymentMethodNonce paymentMethod = paymentMethodNonceList.get(0);
                        result.paymentMethodNonce(paymentMethod);
                    }
                    callback.onResult(result, null);
                } else if (error != null) {
                    callback.onResult(null, error);
                }
            }
        });
    }

    void getVaultedPaymentMethods(final FragmentActivity activity, final GetPaymentMethodNoncesCallback callback) {
        // TODO: cache nonces in ViewModel and allow refresh of vaulted payment methods instead of having a refetch parameter

        braintreeClient.getConfiguration(new ConfigurationCallback() {
            @Override
            public void onResult(@Nullable final Configuration configuration, @Nullable Exception error) {
                if (error != null) {
                    callback.onResult(null, error);
                    return;
                }

                paymentMethodClient.getPaymentMethodNonces(new GetPaymentMethodNoncesCallback() {

                    @Override
                    public void onResult(@Nullable final List<PaymentMethodNonce> paymentMethodNonces, @Nullable Exception error) {
                        if (error != null) {
                            callback.onResult(null, error);
                        } else if (paymentMethodNonces != null) {
                            if (!dropInRequest.isGooglePayDisabled()) {
                                googlePayClient.isReadyToPay(activity, new GooglePayIsReadyToPayCallback() {
                                    @Override
                                    public void onResult(boolean isReadyToPay, Exception error) {
                                        AvailablePaymentMethodNonceList availablePaymentMethodNonceList =
                                                new AvailablePaymentMethodNonceList(configuration, paymentMethodNonces, dropInRequest, isReadyToPay);
                                        callback.onResult(availablePaymentMethodNonceList.getItems(), null);
                                    }
                                });
                            } else {
                                AvailablePaymentMethodNonceList availablePaymentMethodNonceList =
                                        new AvailablePaymentMethodNonceList(configuration, paymentMethodNonces, dropInRequest, false);
                                callback.onResult(availablePaymentMethodNonceList.getItems(), null);
                            }
                        }
                    }
                });
            }
        });
    }
}
