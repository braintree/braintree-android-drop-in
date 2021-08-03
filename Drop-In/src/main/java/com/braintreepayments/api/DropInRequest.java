package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.braintreepayments.cardform.view.CardForm;

/**
 * Used to start {@link DropInActivity} with specified options.
 */
public class DropInRequest implements Parcelable {

    public static final String EXTRA_CHECKOUT_REQUEST = "com.braintreepayments.api.EXTRA_CHECKOUT_REQUEST";

    private String mAuthorization;

    private boolean mCollectDeviceData;
    private boolean mRequestThreeDSecureVerification;
    private ThreeDSecureRequest mThreeDSecureRequest;

    private GooglePayRequest mGooglePaymentRequest;
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
    private boolean mVaultVenmo = false;

    private int mCardholderNameStatus = CardForm.FIELD_DISABLED;

    public DropInRequest() {}

    /**
     * This method is optional.
     *
     * @param collectDeviceData {@code true} if Drop-in should collect and return device data for
     *        fraud prevention.
     * @see DataCollector
     */
    public void setShouldCollectDeviceData(boolean collectDeviceData) {
        mCollectDeviceData = collectDeviceData;
    }

    /**
     * This method is optional.
     *
     * @param request The Google Pay Request {@link GooglePayRequest} for the transaction.
     */
    public void setGooglePayRequest(GooglePayRequest request) {
        mGooglePaymentRequest = request;
    }

    /**
     * This method is optional.
     *
     * @param request The PayPal Request {@link PayPalRequest} for the transaction.
     * If no amount is set, PayPal will default to the billing agreement (Vault) flow.
     * If amount is set, PayPal will follow the one time payment (Checkout) flow.
     */
    public void setPayPalRequest(PayPalRequest request) {
        mPayPalRequest = request;
    }

    /**
     * Disables Google Pay in Drop-in.
     * @param disableGooglePay
     */
    public void setDisableGooglePay(boolean disableGooglePay) {
        mGooglePaymentEnabled = !disableGooglePay;
    }

    /**
     * Disables PayPal in Drop-in.
     * @param disablePayPal
     */
    public void setDisablePayPal(boolean disablePayPal) {
        mPayPalEnabled = !disablePayPal;
    }

    /**
     * Disables Venmo in Drop-in.
     * @param disableVenmo
     */
    public void setDisableVenmo(boolean disableVenmo) {
        mVenmoEnabled = !disableVenmo;
    }

    /**
     * Disables Card in Drop-in.
     * @param disableCard
     */
    public void setDisableCard(boolean disableCard) {
        mCardEnabled = !disableCard;
    }

    /**
     * If 3D Secure has been enabled in the control panel and an amount is specified in
     * a {@link ThreeDSecureRequest} that is provided, Drop-In will request a 3D Secure verification
     * for any new cards added by the user.
     *
     * @param requestThreeDSecure {@code true} to request a 3D Secure verification as part of Drop-In,
     * {@code false} to not request a 3D Secure verification. Defaults to {@code false}.
     */
    public void setShouldRequestThreeDSecureVerification(boolean requestThreeDSecure) {
        mRequestThreeDSecureVerification = requestThreeDSecure;
    }

    /**
     * This method is optional.
     *
     * @param threeDSecureRequest {@link ThreeDSecureRequest} to specify options and additional information for 3D Secure.
     * To encourage 3DS 2.0 flows, set {@link ThreeDSecureRequest#setBillingAddress(ThreeDSecurePostalAddress)},
     * {@link ThreeDSecureRequest#setEmail(String)}, and {@link ThreeDSecureRequest#setMobilePhoneNumber(String)} for best results.
     */
    public void setThreeDSecureRequest(ThreeDSecureRequest threeDSecureRequest) {
        mThreeDSecureRequest = threeDSecureRequest;
    }

    /**
     * @param maskCardNumber {@code true} to mask the card number when the field is not focused.
     * See {@link com.braintreepayments.cardform.view.CardEditText} for more details. Defaults to
     * {@code false}.
     */
    public void setMaskCardNumber(boolean maskCardNumber) {
        mMaskCardNumber = maskCardNumber;
    }

    /**
     * @param maskSecurityCode {@code true} to mask the security code during input. Defaults to {@code false}.
     */
    public void setMaskSecurityCode(boolean maskSecurityCode) {
        mMaskSecurityCode = maskSecurityCode;
    }

    /**
     * @param vaultManager {@code true} to allow customers to manage their vaulted payment methods.
     * Defaults to {@code false}.
     */
    public void setEnableVaultManager(boolean vaultManager) {
        mVaultManagerEnabled = vaultManager;
    }

    /**
     * @param defaultValue the default value used to determine if Drop-in should vault the customer's card. This setting can be overwritten by the customer if the save card checkbox is visible using {@link #setAllowVaultCardOverride(boolean)}
     * If the save card CheckBox is shown, and default vault value is true: the save card CheckBox will appear pre-checked.
     * If the save card CheckBox is shown, and default vault value is false: the save card Checkbox will appear un-checked.
     * If the save card CheckBox is not shown, and default vault value is true: card always vaults.
     * If the save card CheckBox is not shown, and default vault value is false: card never vaults.
     *
     * This value is {@code true} by default.
     */
    public void setVaultCardDefaultValue(boolean defaultValue) {
        mDefaultVaultValue = defaultValue;
    }

    /**
     * @param defaultValue the default value used to determine if Drop-in should vault the customer's venmo payment method. Must be set to `false` when using a client token without a `customerId`.
     *
     * This value is {@code false} by default.
     */
    public void setVaultVenmoDefaultValue(boolean defaultValue) {
        mVaultVenmo = defaultValue;
    };

    /**
     * @param customerCheckBoxEnabled {@code true} shows save card CheckBox to allow user to choose whether or not to vault their card.
     * {@code false} does not show Save Card CheckBox.
     */
    public void setAllowVaultCardOverride(boolean customerCheckBoxEnabled) {
        mShowCheckBoxToAllowVaultOverride = customerCheckBoxEnabled;
    }

    /**
     * Sets the Cardholder Name field status, which is how it will behave in {@link CardForm}.
     * Default is {@link CardForm#FIELD_DISABLED}.
     *
     * Can be {@link CardForm#FIELD_DISABLED}, {@link CardForm#FIELD_OPTIONAL}, or
     * {@link CardForm#FIELD_REQUIRED}.
     */
    public void setCardholderNameStatus(int fieldStatus) {
        mCardholderNameStatus = fieldStatus;
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

    // TODO: rename to get GooglePayRequest
    public GooglePayRequest getGooglePaymentRequest() {
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

    boolean shouldVaultVenmo() { return mVaultVenmo; }

    boolean getEnableVaultManager() {
        return mVaultManagerEnabled;
    }

    public int getCardholderNameStatus() {
        return mCardholderNameStatus;
    }

    public boolean getVaultCardDefaultValue() {
        return mDefaultVaultValue;
    }

    public boolean getAllowVaultCardOverride() {
        return mShowCheckBoxToAllowVaultOverride;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mAuthorization);
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
        dest.writeByte(mVaultVenmo ? (byte) 1 : (byte) 0);
    }

    protected DropInRequest(Parcel in) {
        mAuthorization = in.readString();
        mCollectDeviceData = in.readByte() != 0;
        mGooglePaymentRequest = in.readParcelable(GooglePayRequest.class.getClassLoader());
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
        mVaultVenmo = in.readByte() != 0;
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
