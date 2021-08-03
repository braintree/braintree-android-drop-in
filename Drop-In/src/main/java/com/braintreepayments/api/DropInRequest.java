package com.braintreepayments.api;

import android.os.Parcel;
import android.os.Parcelable;

import com.braintreepayments.cardform.view.CardForm;

/**
 * Used to start {@link DropInActivity} with specified options.
 */
public class DropInRequest implements Parcelable {

    private boolean shouldCollectDeviceData;
    private boolean requestThreeDSecureVerification;
    private ThreeDSecureRequest threeDSecureRequest;

    private GooglePayRequest googlePayRequest;
    private PayPalRequest payPalRequest;

    private boolean googlePayDisabled = false;
    private boolean shouldMaskCardNumber = false;
    private boolean shouldMaskSecurityCode = false;
    private boolean enableVaultManager = false;
    private boolean payPalDisabled = false;
    private boolean venmoDisabled = false;
    private boolean cardDisabled = false;
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
        shouldCollectDeviceData = collectDeviceData;
    }

    /**
     * This method is optional.
     *
     * @param request The Google Pay Request {@link GooglePayRequest} for the transaction.
     */
    public void setGooglePayRequest(GooglePayRequest request) {
        googlePayRequest = request;
    }

    /**
     * This method is optional.
     *
     * @param request The PayPal Request {@link PayPalRequest} for the transaction.
     * If no amount is set, PayPal will default to the billing agreement (Vault) flow.
     * If amount is set, PayPal will follow the one time payment (Checkout) flow.
     */
    public void setPayPalRequest(PayPalRequest request) {
        payPalRequest = request;
    }

    /**
     * This method is optional.
     *
     * @param disableGooglePay If set to true, disables Google Pay in Drop-in. Default value is false.
     */
    public void setGooglePayDisabled(boolean disableGooglePay) {
        googlePayDisabled = disableGooglePay;
    }

    /**
     * This method is optional.
     *
     * @param disablePayPal If set to true, disables PayPal in Drop-in. Default value is false.
     */
    public void setPayPalDisabled(boolean disablePayPal) {
        payPalDisabled = disablePayPal;
    }

    /**
     * This method is optional.
     *
     * @param disableVenmo If set to true, disables Venmo in Drop-in. Default value is false.
     */
    public void setVenmoDisabled(boolean disableVenmo) {
        venmoDisabled = disableVenmo;
    }

    /**
     * This method is optional.
     *
     * @param disableCard If set to true, disables Card in Drop-in. Default value is false.
     */
    public void setCardDisabled(boolean disableCard) {
        cardDisabled = disableCard;
    }

    /**
     * This method is optional.
     *
     * If 3D Secure has been enabled in the control panel and an amount is specified in
     * a {@link ThreeDSecureRequest} that is provided, Drop-In will request a 3D Secure verification
     * for any new cards added by the user.
     *
     * @param requestThreeDSecure {@code true} to request a 3D Secure verification as part of Drop-In,
     * {@code false} to not request a 3D Secure verification. Defaults to {@code false}.
     */
    public void setShouldRequestThreeDSecureVerification(boolean requestThreeDSecure) {
        requestThreeDSecureVerification = requestThreeDSecure;
    }

    /**
     * This method is optional.
     *
     * @param threeDSecureRequest {@link ThreeDSecureRequest} to specify options and additional information for 3D Secure.
     * To encourage 3DS 2.0 flows, set {@link ThreeDSecureRequest#setBillingAddress(ThreeDSecurePostalAddress)},
     * {@link ThreeDSecureRequest#setEmail(String)}, and {@link ThreeDSecureRequest#setMobilePhoneNumber(String)} for best results.
     */
    public void setThreeDSecureRequest(ThreeDSecureRequest threeDSecureRequest) {
        this.threeDSecureRequest = threeDSecureRequest;
    }

    /**
     * This method is optional.
     *
     * @param maskCardNumber {@code true} to mask the card number when the field is not focused.
     * See {@link com.braintreepayments.cardform.view.CardEditText} for more details. Defaults to
     * {@code false}.
     */
    public void setShouldMaskCardNumber(boolean maskCardNumber) {
        shouldMaskCardNumber = maskCardNumber;
    }

    /**
     * This method is optional.
     *
     * @param maskSecurityCode {@code true} to mask the security code during input. Defaults to {@code false}.
     */
    public void setShouldMaskSecurityCode(boolean maskSecurityCode) {
        shouldMaskSecurityCode = maskSecurityCode;
    }

    /**
     * This method is optional.
     *
     * @param vaultManager {@code true} to allow customers to manage their vaulted payment methods.
     * Defaults to {@code false}.
     */
    public void setEnableVaultManager(boolean vaultManager) {
        enableVaultManager = vaultManager;
    }

    /**
     * This method is optional.
     *
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
    }

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

    boolean getShouldCollectDeviceData() {
        return shouldCollectDeviceData;
    }

    public boolean getPayPalDisabled() {
        return payPalDisabled;
    }

    public PayPalRequest getPayPalRequest() { return payPalRequest; }

    public boolean getVenmoDisabled() {
        return venmoDisabled;
    }

    public boolean getCardDisabled() {
        return cardDisabled;
    }

    public GooglePayRequest getGooglePayRequest() {
        return googlePayRequest;
    }

    public boolean getGooglePayDisabled() {
        return googlePayDisabled;
    }

    boolean getShouldRequestThreeDSecureVerification() {
        return requestThreeDSecureVerification;
    }

    public ThreeDSecureRequest getThreeDSecureRequest() { return threeDSecureRequest; }

    boolean getShouldMaskCardNumber() {
        return shouldMaskCardNumber;
    }

    boolean getShouldMaskSecurityCode() {
        return shouldMaskSecurityCode;
    }

    boolean getVaultVenmoDefaultValue() { return mVaultVenmo; }

    boolean getEnableVaultManager() {
        return enableVaultManager;
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

    boolean isPayPalEnabled() {
        return !payPalDisabled;
    }

    boolean isVenmoEnabled() {
        return !venmoDisabled;
    }

    boolean isGooglePayEnabled() {
        return !googlePayDisabled;
    }

    boolean isCardEnabled() {
        return !cardDisabled;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(shouldCollectDeviceData ? (byte) 1 : (byte) 0);
        dest.writeParcelable(googlePayRequest, 0);
        dest.writeByte(googlePayDisabled ? (byte) 1 : (byte) 0);
        dest.writeParcelable(payPalRequest, 0);
        dest.writeByte(payPalDisabled ? (byte) 1 : (byte) 0);
        dest.writeByte(venmoDisabled ? (byte) 1 : (byte) 0);
        dest.writeByte(cardDisabled ? (byte) 1 : (byte) 0);
        dest.writeByte(requestThreeDSecureVerification ? (byte) 1 : (byte) 0);
        dest.writeParcelable(threeDSecureRequest, 0);
        dest.writeByte(shouldMaskCardNumber ? (byte) 1 : (byte) 0);
        dest.writeByte(shouldMaskSecurityCode ? (byte) 1 : (byte) 0);
        dest.writeByte(enableVaultManager ? (byte) 1 : (byte) 0);
        dest.writeInt(mCardholderNameStatus);
        dest.writeByte(mDefaultVaultValue ? (byte) 1 : (byte) 0);
        dest.writeByte(mShowCheckBoxToAllowVaultOverride ? (byte) 1 : (byte) 0);
        dest.writeByte(mVaultVenmo ? (byte) 1 : (byte) 0);
    }

    protected DropInRequest(Parcel in) {
        shouldCollectDeviceData = in.readByte() != 0;
        googlePayRequest = in.readParcelable(GooglePayRequest.class.getClassLoader());
        googlePayDisabled = in.readByte() != 0;
        payPalRequest = in.readParcelable(PayPalRequest.class.getClassLoader());
        payPalDisabled = in.readByte() != 0;
        venmoDisabled = in.readByte() != 0;
        cardDisabled = in.readByte() != 0;
        requestThreeDSecureVerification = in.readByte() != 0;
        threeDSecureRequest = in.readParcelable(ThreeDSecureRequest.class.getClassLoader());
        shouldMaskCardNumber = in.readByte() != 0;
        shouldMaskSecurityCode = in.readByte() != 0;
        enableVaultManager = in.readByte() != 0;
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
