package com.braintreepayments.api.dropin;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.braintreepayments.api.DataCollector;
import com.braintreepayments.api.PayPal;
import com.braintreepayments.api.models.GooglePaymentRequest;
import com.google.android.gms.identity.intents.model.CountrySpecification;
import com.google.android.gms.wallet.Cart;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to start {@link DropInActivity} with specified options.
 */
public class DropInRequest implements Parcelable {

    public static final String EXTRA_CHECKOUT_REQUEST = "com.braintreepayments.api.EXTRA_CHECKOUT_REQUEST";

    private String mAuthorization;

    private String mAmount;
    private boolean mCollectDeviceData;
    private boolean mRequestThreeDSecureVerification;

    private Cart mAndroidPayCart;
    private GooglePaymentRequest mGooglePaymentRequest;
    private boolean mAndroidPayShippingAddressRequired;
    private boolean mAndroidPayPhoneNumberRequired;
    private boolean mAndroidPayEnabled = true;
    private boolean mGooglePaymentEnabled = true;
    private boolean mMaskCardNumber = false;
    private boolean mMaskSecurityCode = false;
    private ArrayList<CountrySpecification> mAndroidAllowedCountriesForShipping = new ArrayList<>();

    private List<String> mPayPalAdditionalScopes;
    private boolean mPayPalEnabled = true;

    private boolean mVenmoEnabled = true;

    public DropInRequest() {}

    /**
     * Provide authorization allowing this client to communicate with Braintree. Either
     * {@link #clientToken(String)} or {@link #tokenizationKey(String)} must be set or an
     * {@link com.braintreepayments.api.exceptions.AuthenticationException} will occur.
     *
     * @param clientToken The client token to use for the request.
     */
    public DropInRequest clientToken(String clientToken) {
        mAuthorization = clientToken;
        return this;
    }

    /**
     * Provide authorization allowing this client to communicate with Braintree. Either
     * {@link #clientToken(String)} or {@link #tokenizationKey(String)} must be set or an
     * {@link com.braintreepayments.api.exceptions.AuthenticationException} will occur.
     *
     * @param tokenizationKey The tokenization key to use for the request.
     */
    public DropInRequest tokenizationKey(String tokenizationKey) {
        mAuthorization = tokenizationKey;
        return this;
    }

    /**
     * This method is optional. Amount is only used for 3D Secure verifications.
     *
     * @param amount Amount of the transaction.
     */
    public DropInRequest amount(String amount) {
        mAmount = amount;
        return this;
    }

    /**
     * This method is optional.
     *
     * @param collectDeviceData {@code true} if Drop-in should collect and return device data for
     *        fraud prevention.
     * @see DataCollector
     */
    public DropInRequest collectDeviceData(boolean collectDeviceData) {
        mCollectDeviceData = collectDeviceData;
        return this;
    }

    /**
     * This method is optional.
     *
     * @param request The Google Payment Request {@link GooglePaymentRequest} for the transaction.
     */
    public DropInRequest googlePaymentRequest(GooglePaymentRequest request) {
        mGooglePaymentRequest = request;
        return this;
    }

    /**
     * @deprecated
     * This method is deprecated. Use {@link DropInRequest#googlePaymentRequest(GooglePaymentRequest)} instead.
     *
     * This method is optional.
     *
     * @param cart The Android Pay {@link Cart} for the transaction.
     */
    @Deprecated
    public DropInRequest androidPayCart(Cart cart) {
        mAndroidPayCart = cart;
        return this;
    }

    /**
     * @deprecated
     * This method is deprecated. Please use {@link GooglePaymentRequest#shippingAddressRequired(boolean)} instead.
     *
     * This method is optional.
     *
     * @param shippingAddressRequired {@code true} if Android Pay requests should request a
     *        shipping address from the user.
     */
    @Deprecated
    public DropInRequest androidPayShippingAddressRequired(boolean shippingAddressRequired) {
        mAndroidPayShippingAddressRequired = shippingAddressRequired;
        return this;
    }

    /**
     * @deprecated
     * This method is deprecated. Please use {@link GooglePaymentRequest#phoneNumberRequired(boolean)} instead.
     *
     * This method is optional.
     *
     * @param phoneNumberRequired {@code true} if Android Pay requests should request a phone
     *        number from the user.
     */
    @Deprecated
    public DropInRequest androidPayPhoneNumberRequired(boolean phoneNumberRequired) {
        mAndroidPayPhoneNumberRequired = phoneNumberRequired;
        return this;
    }

    /**
     * @deprecated
     * This method is deprecated. Please use {@link GooglePaymentRequest#getShippingAddressRequirements()} instead.
     *
     * This method is optional.
     *
     * @param countryCodes countries to which shipping is supported.
     * Follows the ISO 3166-2 format (ex: "US", "CA", "JP")
     *
     * @see <a href="https://en.wikipedia.org/wiki/ISO_3166-2#Current_codes">ISO 3166 country codes</a>
     */
    @Deprecated
    public DropInRequest androidPayAllowedCountriesForShipping(String... countryCodes) {
        mAndroidAllowedCountriesForShipping.clear();
        for(String countryCode : countryCodes) {
            mAndroidAllowedCountriesForShipping.add(new CountrySpecification(countryCode));
        }
        return this;
    }

    /**
     * Disables Android Pay in Drop-in.
     */
    public DropInRequest disableAndroidPay() {
        mAndroidPayEnabled = false;
        return this;
    }

    /**
     * Disables Google Payment in Drop-in.
     */
    public DropInRequest disableGooglePayment() {
        mGooglePaymentEnabled = false;
        return this;
    }

    /**
     * Set additional scopes to request when a user is authorizing PayPal.
     *
     * This method is optional.
     *
     * @param additionalScopes A {@link java.util.List} of additional scopes.
     *        Ex: PayPal.SCOPE_ADDRESS.
     *        Acceptable scopes are defined in {@link PayPal}.
     */
    public DropInRequest paypalAdditionalScopes(List<String> additionalScopes) {
        mPayPalAdditionalScopes = additionalScopes;
        return this;
    }

    /**
     * Disables PayPal in Drop-in.
     */
    public DropInRequest disablePayPal() {
        mPayPalEnabled = false;
        return this;
    }

    /**
     * Disables Venmo in Drop-in.
     */
    public DropInRequest disableVenmo() {
        mVenmoEnabled = false;
        return this;
    }

    /**
     * If 3D Secure has been enabled in the control panel and an amount is specified in
     * {@link DropInRequest#amount(String)}, Drop-In will request a 3D Secure verification for
     * any new cards added by the user.
     *
     * @param requestThreeDSecure {@code true} to request a 3D Secure verification as part of Drop-In,
     * {@code false} to not request a 3D Secure verification. Defaults to {@code false}.
     */
    public DropInRequest requestThreeDSecureVerification(boolean requestThreeDSecure) {
        mRequestThreeDSecureVerification = requestThreeDSecure;
        return this;
    }

    /**
     * @param maskCardNumber {@code true} to mask the card number when the field is not focused.
     * See {@link com.braintreepayments.cardform.view.CardEditText} for more details. Defaults to
     * {@code false}.
     */
    public DropInRequest maskCardNumber(boolean maskCardNumber) {
        mMaskCardNumber = maskCardNumber;
        return this;
    }

    /**
     * @param maskSecurityCode {@code true} to mask the security code during input. Defaults to {@code false}.
     */
    public DropInRequest maskSecurityCode(boolean maskSecurityCode) {
        mMaskSecurityCode = maskSecurityCode;
        return this;
    }

    /**
     * Get an {@link Intent} that can be used in {@link android.app.Activity#startActivityForResult(Intent, int)}
     * to launch {@link DropInActivity} and the Drop-in UI.
     *
     * @param context
     * @return {@link Intent} containing all of the options set in {@link DropInRequest}.
     */
    public Intent getIntent(Context context) {
        return new Intent(context, DropInActivity.class)
                .putExtra(EXTRA_CHECKOUT_REQUEST, this);
    }

    String getAuthorization() {
        return mAuthorization;
    }

    String getAmount() {
        return mAmount;
    }

    boolean shouldCollectDeviceData() {
        return mCollectDeviceData;
    }

    Cart getAndroidPayCart() throws NoClassDefFoundError {
        return mAndroidPayCart;
    }

    boolean isAndroidPayShippingAddressRequired() {
        return mAndroidPayShippingAddressRequired;
    }

    boolean isAndroidPayPhoneNumberRequired() {
        return mAndroidPayPhoneNumberRequired;
    }

    public boolean isAndroidPayEnabled() {
        return mAndroidPayEnabled;
    }

    ArrayList<CountrySpecification> getAndroidPayAllowedCountriesForShipping() {
        return mAndroidAllowedCountriesForShipping;
    }

    List<String> getPayPalAdditionalScopes() {
        return mPayPalAdditionalScopes;
    }

    public boolean isPayPalEnabled() {
        return mPayPalEnabled;
    }

    public boolean isVenmoEnabled() {
        return mVenmoEnabled;
    }

    public GooglePaymentRequest getGooglePaymentRequest() {
        return mGooglePaymentRequest;
    }

    public boolean isGooglePaymentEnabled() {
        return mGooglePaymentEnabled;
    }

    boolean shouldRequestThreeDSecureVerification() {
        return mRequestThreeDSecureVerification;
    }

    boolean shouldMaskCardNumber() {
        return mMaskCardNumber;
    }

    boolean shouldMaskSecurityCode() {
        return mMaskSecurityCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mAuthorization);
        dest.writeString(mAmount);
        dest.writeByte(mCollectDeviceData ? (byte) 1 : (byte) 0);

        try {
            Cart.class.getClassLoader();
            dest.writeParcelable(mAndroidPayCart, 0);
            dest.writeByte(mAndroidPayShippingAddressRequired ? (byte) 1 : (byte) 0);
            dest.writeByte(mAndroidPayPhoneNumberRequired ? (byte) 1 : (byte) 0);
            dest.writeTypedList(mAndroidAllowedCountriesForShipping);
        } catch (NoClassDefFoundError ignored) {}

        dest.writeParcelable(mGooglePaymentRequest, 0);
        dest.writeByte(mGooglePaymentEnabled ? (byte) 1 : (byte) 0);
        dest.writeByte(mAndroidPayEnabled ? (byte) 1 : (byte) 0);
        dest.writeStringList(mPayPalAdditionalScopes);
        dest.writeByte(mPayPalEnabled ? (byte) 1 : (byte) 0);
        dest.writeByte(mVenmoEnabled ? (byte) 1 : (byte) 0);
        dest.writeByte(mRequestThreeDSecureVerification ? (byte) 1 : (byte) 0);
        dest.writeByte(mMaskCardNumber ? (byte) 1 : (byte) 0);
        dest.writeByte(mMaskSecurityCode ? (byte) 1 : (byte) 0);
    }

    protected DropInRequest(Parcel in) {
        mAuthorization = in.readString();
        mAmount = in.readString();
        mCollectDeviceData = in.readByte() != 0;

        try {
            mAndroidPayCart = in.readParcelable(Cart.class.getClassLoader());
            mAndroidPayShippingAddressRequired = in.readByte() != 0;
            mAndroidPayPhoneNumberRequired = in.readByte() != 0;
            in.readTypedList(mAndroidAllowedCountriesForShipping, CountrySpecification.CREATOR);
        } catch (NoClassDefFoundError ignored) {}

        mGooglePaymentRequest = in.readParcelable(GooglePaymentRequest.class.getClassLoader());
        mGooglePaymentEnabled = in.readByte() != 0;
        mAndroidPayEnabled = in.readByte() != 0;
        mPayPalAdditionalScopes = in.createStringArrayList();
        mPayPalEnabled = in.readByte() != 0;
        mVenmoEnabled = in.readByte() != 0;
        mRequestThreeDSecureVerification = in.readByte() != 0;
        mMaskCardNumber = in.readByte() != 0;
        mMaskSecurityCode = in.readByte() != 0;
    }

    public static final Creator<DropInRequest> CREATOR = new Creator<DropInRequest>() {
        public DropInRequest createFromParcel(Parcel source) {
            return new DropInRequest(source);
        }

        public DropInRequest[] newArray(int size) {
            return new DropInRequest[size];
        }
    };
}
