package com.braintreepayments.api;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used to retrieve a customer's payment methods.
 */
public class PaymentMethodClient {

    private static final String PAYMENT_METHOD_NONCE_COLLECTION_KEY = "paymentMethods";
    private static final String PAYMENT_METHOD_TYPE_KEY = "type";

    private static final String PAYMENT_METHOD_TYPE_CARD = "CreditCard";
    private static final String PAYMENT_METHOD_TYPE_PAYPAL = "PayPalAccount";
    private static final String PAYMENT_METHOD_TYPE_VENMO = "VenmoAccount";

    private static final String SINGLE_USE_TOKEN_ID = "singleUseTokenId";
    private static final String VARIABLES = "variables";
    private static final String INPUT = "input";
    private static final String CLIENT_SDK_META_DATA = "clientSdkMetadata";

    private final BraintreeClient braintreeClient;

    /**
     * Creates a new instance of {@link PaymentMethodClient}
     *
     * @param braintreeClient a {@link BraintreeClient}
     */
    public PaymentMethodClient(BraintreeClient braintreeClient) {
        this.braintreeClient = braintreeClient;
    }

    private static List<PaymentMethodNonce> parsePaymentMethodNonces(String jsonBody) throws JSONException {
        JSONArray paymentMethods =
                new JSONObject(jsonBody).getJSONArray(PAYMENT_METHOD_NONCE_COLLECTION_KEY);

        List<PaymentMethodNonce> result = new ArrayList<>();
        for (int i = 0; i < paymentMethods.length(); i++) {
            JSONObject json = paymentMethods.getJSONObject(i);
            PaymentMethodNonce paymentMethodNonce = parseVaultSupportedPaymentMethodNonce(json);
            if (paymentMethodNonce != null) {
                result.add(paymentMethodNonce);
            }
        }

        return result;
    }

    private static PaymentMethodNonce parseVaultSupportedPaymentMethodNonce(JSONObject json) throws JSONException {
        // NOTE: Since 3.x, Card, PayPal, Venmo, and Visa Checkout were the only payment methods supported by the vault manager
        String type = json.getString(PAYMENT_METHOD_TYPE_KEY);
        switch (type) {
            case PAYMENT_METHOD_TYPE_CARD:
                return CardNonce.fromJSON(json);
            case PAYMENT_METHOD_TYPE_PAYPAL:
                return PayPalAccountNonce.fromJSON(json);
            case PAYMENT_METHOD_TYPE_VENMO:
                return VenmoAccountNonce.fromJSON(json);
            default:
                return null;
        }
    }

    /**
     * Retrieves the current list of {@link PaymentMethodNonce} for the current user.
     *
     * @param defaultFirst when {@code true}, the user's default payment method will be first in the
     *                     list
     * @param callback a {@link GetPaymentMethodNoncesCallback} to handle results
     */
    public void getPaymentMethodNonces(boolean defaultFirst, final GetPaymentMethodNoncesCallback callback) {
        final Uri uri = Uri.parse(ApiClient.versionedPath(ApiClient.PAYMENT_METHOD_ENDPOINT))
                .buildUpon()
                .appendQueryParameter("default_first", String.valueOf(defaultFirst))
                .appendQueryParameter("session_id", braintreeClient.getSessionId())
                .build();

        braintreeClient.sendGET(uri.toString(), (responseBody, httpError) -> {
            if (responseBody != null) {
                try {
                    callback.onResult(parsePaymentMethodNonces(responseBody), null);
                    braintreeClient.sendAnalyticsEvent("get-payment-methods.succeeded");
                } catch (JSONException e) {
                    callback.onResult(null, e);
                    braintreeClient.sendAnalyticsEvent("get-payment-methods.failed");
                }
            } else {
                callback.onResult(null, httpError);
                braintreeClient.sendAnalyticsEvent("get-payment-methods.failed");
            }
        });
    }

    void getPaymentMethodNonces(GetPaymentMethodNoncesCallback callback) {
        getPaymentMethodNonces(false, callback);
    }

    /**
     * Deletes a payment method for the user whose ID was used to generate the {@link ClientToken}
     * used to instantiate the {@link BraintreeClient}.
     *
     * @param context an Android {@link Context}
     * @param paymentMethodNonce the {@link PaymentMethodNonce} that references a vaulted payment
     *                           method to be deleted.
     * @param callback a {@link DeletePaymentMethodNonceCallback} to handle results
     */
    public void deletePaymentMethod(final Context context, final PaymentMethodNonce paymentMethodNonce, final DeletePaymentMethodNonceCallback callback) {
        braintreeClient.getAuthorization(new AuthorizationCallback() {
            @Override
            public void onAuthorizationResult(@Nullable Authorization authorization, @Nullable Exception error) {
                boolean usesClientToken = authorization instanceof ClientToken;

                if (!usesClientToken) {
                    Exception clientTokenRequiredError =
                            new BraintreeException("A client token with a customer id must be used to delete a payment method nonce.");
                    callback.onResult(null, clientTokenRequiredError);
                    return;
                }

                final JSONObject base = new JSONObject();
                JSONObject variables = new JSONObject();
                JSONObject input = new JSONObject();

                try {
                    base.put(CLIENT_SDK_META_DATA, new MetadataBuilder()
                            .sessionId(braintreeClient.getSessionId())
                            .source("client")
                            .integration(braintreeClient.getIntegrationType())
                            .build());

                    base.put(GraphQLConstants.Keys.QUERY, GraphQLQueryHelper.getQuery(
                            context, R.raw.delete_payment_method_mutation));
                    input.put(SINGLE_USE_TOKEN_ID, paymentMethodNonce.getString());
                    variables.put(INPUT, input);
                    base.put(VARIABLES, variables);
                    base.put(GraphQLConstants.Keys.OPERATION_NAME,
                            "DeletePaymentMethodFromSingleUseToken");
                } catch (Resources.NotFoundException | IOException | JSONException e) {
                    Exception graphQLError = new BraintreeException("Unable to read GraphQL query");
                    callback.onResult(null, graphQLError);
                }

                braintreeClient.sendGraphQLPOST(base.toString(), (responseBody, httpError) -> {
                    if (responseBody != null) {
                        callback.onResult(paymentMethodNonce, null);
                        braintreeClient.sendAnalyticsEvent("delete-payment-methods.succeeded");
                    } else {
                        Exception deletePaymentMethodError =
                            new PaymentMethodDeleteException(paymentMethodNonce, httpError);
                        callback.onResult(null, deletePaymentMethodError);
                        braintreeClient.sendAnalyticsEvent("delete-payment-methods.failed");
                    }
                });
            }
        });
    }
}
