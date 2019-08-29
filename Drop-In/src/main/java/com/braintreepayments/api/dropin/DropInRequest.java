package com.braintreepayments.api.dropin;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.braintreepayments.api.DataCollector;
import com.braintreepayments.api.models.GooglePaymentRequest;
import com.braintreepayments.api.models.PayPalRequest;
import com.braintreepayments.api.models.ThreeDSecurePostalAddress;
import com.braintreepayments.api.models.ThreeDSecureRequest;
import com.braintreepayments.cardform.view.CardForm;

/**
 * Used to start {@link DropInActivity} with specified options.
 */
public class DropInRequest implements Parcelable {

    public static final String EXTRA_CHECKOUT_REQUEST = "com.braintreepayments.api.EXTRA_CHECKOUT_REQUEST";

    private String mAuthorization;

    private String mAmount;
    private boolean mCollectDeviceData;
    private boolean mRequestThreeDSecureVerification;
    private ThreeDSecureRequest mThreeDSecureRequest;

    private GooglePaymentRequest mGooglePaymentRequest;
    private PayPalRequest mPayPalRequest;
    private boolean mGooglePaymentEnabled = true;
    private boolean mMaskCardNumber = false;
    private boolean mMaskSecurityCode = false;
    private boolean mVaultManagerEnabled = false;
    private boolean mPayPalEnabled = true;
    private boolean mVenmoEnabled = true;
    private boolean mCardEnabled = true;
    private boolean mDefaultVaultValue = true;
    private boolean mShowCheckBoxToAllowVaultOverride = false;
    private int mCardholderNameStatus = CardForm.FIELD_DISABLED;

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
     * Deprecated. Use {@link ThreeDSecureRequest#amount(String)}
     *
     * This method is optional. Amount is only used for 3D Secure verifications.
     *
     * This value must be a non-negative number and must match the currency format of the merchant account.
     * It can only contain numbers and optionally one decimal point with exactly 2 decimal place precision (e.g., x.xx).
     *
     * @param amount Amount of the transaction.
     */
    @Deprecated
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
     * This method is optional.
     *
     * @param request The PayPal Request {@link PayPalRequest} for the transaction.
     * If no amount is set, PayPal will default to the billing agreement (Vault) flow.
     * If amount is set, PayPal will follow the one time payment (Checkout) flow.
     */
    public DropInRequest paypalRequest(PayPalRequest request) {
        mPayPalRequest = request;
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
     * Disables Card in Drop-in.
     */
    public DropInRequest disableCard() {
        mCardEnabled = false;
        return this;
    }

    /**
     * If 3D Secure has been enabled in the control panel and an amount is specified in
     * {@link DropInRequest#amount(String)} or a {@link ThreeDSecureRequest} is provided,
     * Drop-In will request a 3D Secure verification for any new cards added by the user.
     *
     * @param requestThreeDSecure {@code true} to request a 3D Secure verification as part of Drop-In,
     * {@code false} to not request a 3D Secure verification. Defaults to {@code false}.
     */
    public DropInRequest requestThreeDSecureVerification(boolean requestThreeDSecure) {
        mRequestThreeDSecureVerification = requestThreeDSecure;
        return this;
    }

    /**
     * This method is optional.
     *
     * @param threeDSecureRequest {@link ThreeDSecureRequest} to specify options and additional information for 3D Secure.
     * To encourage 3DS 2.0 flows, set {@link ThreeDSecureRequest#billingAddress(ThreeDSecurePostalAddress)},
     * {@link ThreeDSecureRequest#email(String)}, and {@link ThreeDSecureRequest#mobilePhoneNumber(String)} for best results.
     * If no amount is set, the {@link DropInRequest#amount(String)} will be used.
     */
    public DropInRequest threeDSecureRequest(ThreeDSecureRequest threeDSecureRequest) {
        mThreeDSecureRequest = threeDSecureRequest;
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
     * @param vaultManager {@code true} to allow customers to manage their vaulted payment methods.
     * Defaults to {@code false}.
     */
    public DropInRequest vaultManager(boolean vaultManager) {
        mVaultManagerEnabled = vaultManager;
        return this;
    }

    /**
     * @param defaultValue the default value used to determine if Drop-in should vault the customer's card. This setting can be overwritten by the customer if the save card checkbox is visible using {@link #allowVaultCardOverride(boolean)}
     * If the save card CheckBox is shown, and default vault value is true: the save card CheckBox will appear pre-checked.
     * If the save card CheckBox is shown, and default vault value is false: the save card Checkbox will appear un-checked.
     * If the save card CheckBox is not shown, and default vault value is true: card always vaults.
     * If the save card CheckBox is not shown, and default vault value is false: card never vaults.
     *
     * This value is {@code true} by default.
     */
    public DropInRequest vaultCard(boolean defaultValue) {
        mDefaultVaultValue = defaultValue;
        return this;
    }

    /**
     * @param customerCheckBoxEnabled {@code true} shows save card CheckBox to allow user to choose whether or not to vault their card.
     * {@code false} does not show Save Card CheckBox.
     */
    public DropInRequest allowVaultCardOverride(boolean customerCheckBoxEnabled) {
        mShowCheckBoxToAllowVaultOverride = customerCheckBoxEnabled;
        return this;
    }

    /**
     * Sets the Cardholder Name field status, which is how it will behave in {@link CardForm}.
     * Default is {@link CardForm#FIELD_DISABLED}.
     *
     * Can be {@link CardForm#FIELD_DISABLED}, {@link CardForm#FIELD_OPTIONAL}, or
     * {@link CardForm#FIELD_REQUIRED}.
     */
    public DropInRequest cardholderNameStatus(int fieldStatus) {
        mCardholderNameStatus = fieldStatus;
        return this;
    }

    /**
     * Get an {@link Intent} that can be used in {@link androidx.appcompat.app.AppCompatActivity#startActivityForResult(Intent, int)}
     * to launch {@link DropInActivity} and the Drop-in UI.
     *
     * @param context
     * @return {@link Intent} containing all of the options set in {@link DropInRequest}.
     */
    public Intent getIntent(Context context) {
        return new Intent(context, DropInActivity.class)
                .putExtra(EXTRA_CHECKOUT_REQUEST, this);
    }

    public String getAuthorization() {
        return mAuthorization;
    }

    String getAmount() {
        return mAmount;
    }

    boolean shouldCollectDeviceData() {
        return mCollectDeviceData;
    }

    public boolean isPayPalEnabled() {
        return mPayPalEnabled;
    }

    public PayPalRequest getPayPalRequest() { return mPayPalRequest; }

    public boolean isVenmoEnabled() {
        return mVenmoEnabled;
    }

    public boolean isCardEnabled() {
        return mCardEnabled;
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

    public ThreeDSecureRequest getThreeDSecureRequest() { return mThreeDSecureRequest; }

    boolean shouldMaskCardNumber() {
        return mMaskCardNumber;
    }

    boolean shouldMaskSecurityCode() {
        return mMaskSecurityCode;
    }

    boolean isVaultManagerEnabled() {
        return mVaultManagerEnabled;
    }

    public int getCardholderNameStatus() {
        return mCardholderNameStatus;
    }

    public boolean getDefaultVaultSetting() {
        return mDefaultVaultValue;
    }

    public boolean isSaveCardCheckBoxShown() {
        return mShowCheckBoxToAllowVaultOverride;
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
        dest.writeParcelable(mGooglePaymentRequest, 0);
        dest.writeByte(mGooglePaymentEnabled ? (byte) 1 : (byte) 0);
        dest.writeParcelable(mPayPalRequest, 0);
        dest.writeByte(mPayPalEnabled ? (byte) 1 : (byte) 0);
        dest.writeByte(mVenmoEnabled ? (byte) 1 : (byte) 0);
        dest.writeByte(mCardEnabled ? (byte) 1 : (byte) 0);
        dest.writeByte(mRequestThreeDSecureVerification ? (byte) 1 : (byte) 0);
        dest.writeParcelable(mThreeDSecureRequest, 0);
        dest.writeByte(mMaskCardNumber ? (byte) 1 : (byte) 0);
        dest.writeByte(mMaskSecurityCode ? (byte) 1 : (byte) 0);
        dest.writeByte(mVaultManagerEnabled ? (byte) 1 : (byte) 0);
        dest.writeInt(mCardholderNameStatus);
        dest.writeByte(mDefaultVaultValue ? (byte) 1 : (byte) 0);
        dest.writeByte(mShowCheckBoxToAllowVaultOverride ? (byte) 1 : (byte) 0);
    }

    protected DropInRequest(Parcel in) {
        mAuthorization = in.readString();
        mAmount = in.readString();
        mCollectDeviceData = in.readByte() != 0;
        mGooglePaymentRequest = in.readParcelable(GooglePaymentRequest.class.getClassLoader());
        mGooglePaymentEnabled = in.readByte() != 0;
        mPayPalRequest = in.readParcelable(PayPalRequest.class.getClassLoader());
        mPayPalEnabled = in.readByte() != 0;
        mVenmoEnabled = in.readByte() != 0;
        mCardEnabled = in.readByte() != 0;
        mRequestThreeDSecureVerification = in.readByte() != 0;
        mThreeDSecureRequest = in.readParcelable(ThreeDSecureRequest.class.getClassLoader());
        mMaskCardNumber = in.readByte() != 0;
        mMaskSecurityCode = in.readByte() != 0;
        mVaultManagerEnabled = in.readByte() != 0;
        mCardholderNameStatus = in.readInt();
        mDefaultVaultValue = in.readByte() != 0;
        mShowCheckBoxToAllowVaultOverride = in.readByte() != 0;
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
