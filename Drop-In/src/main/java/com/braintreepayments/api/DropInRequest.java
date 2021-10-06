package com.braintreepayments.api;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.braintreepayments.cardform.view.CardForm;

/**
 * Used to start {@link DropInActivity} with specified options.
 */
public class DropInRequest implements Parcelable {

    private boolean requestThreeDSecureVerification;
    private ThreeDSecureRequest threeDSecureRequest;

    private GooglePayRequest googlePayRequest;
    private PayPalRequest payPalRequest;

    private boolean googlePayDisabled = false;
    private boolean maskCardNumber = false;
    private boolean maskSecurityCode = false;
    private boolean vaultManagerEnabled = false;
    private boolean payPalDisabled = false;
    private boolean venmoDisabled = false;
    private boolean cardDisabled = false;
    private boolean vaultCardDefaultValue = true;
    private boolean allowVaultCardOverride = false;
    private boolean vaultVenmoDefaultValue = false;

    private int cardholderNameStatus = CardForm.FIELD_DISABLED;

    public DropInRequest() {}

    /**
     * This method is optional.
     *
     * @param request The Google Pay Request {@link GooglePayRequest} for the transaction.
     */
    public void setGooglePayRequest(@Nullable GooglePayRequest request) {
        googlePayRequest = request;
    }

    /**
     * This method is optional.
     *
     * @param request The PayPal Request {@link PayPalRequest} for the transaction.
     * If no amount is set, PayPal will default to the billing agreement (Vault) flow.
     * If amount is set, PayPal will follow the one time payment (Checkout) flow.
     */
    public void setPayPalRequest(@Nullable PayPalRequest request) {
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
    public void setRequestThreeDSecureVerification(boolean requestThreeDSecure) {
        requestThreeDSecureVerification = requestThreeDSecure;
    }

    /**
     * This method is optional.
     *
     * @param threeDSecureRequest {@link ThreeDSecureRequest} to specify options and additional information for 3D Secure.
     * To encourage 3DS 2.0 flows, set {@link ThreeDSecureRequest#setBillingAddress(ThreeDSecurePostalAddress)},
     * {@link ThreeDSecureRequest#setEmail(String)}, and {@link ThreeDSecureRequest#setMobilePhoneNumber(String)} for best results.
     */
    public void setThreeDSecureRequest(@Nullable ThreeDSecureRequest threeDSecureRequest) {
        this.threeDSecureRequest = threeDSecureRequest;
    }

    /**
     * This method is optional.
     *
     * @param maskCardNumber {@code true} to mask the card number when the field is not focused.
     * See {@link com.braintreepayments.cardform.view.CardEditText} for more details. Defaults to
     * {@code false}.
     */
    public void setMaskCardNumber(boolean maskCardNumber) {
        this.maskCardNumber = maskCardNumber;
    }

    /**
     * This method is optional.
     *
     * @param maskSecurityCode {@code true} to mask the security code during input. Defaults to {@code false}.
     */
    public void setMaskSecurityCode(boolean maskSecurityCode) {
        this.maskSecurityCode = maskSecurityCode;
    }

    /**
     * This method is optional.
     *
     * @param vaultManager {@code true} to allow customers to manage their vaulted payment methods.
     * Defaults to {@code false}.
     */
    public void setVaultManagerEnabled(boolean vaultManager) {
        vaultManagerEnabled = vaultManager;
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
        vaultCardDefaultValue = defaultValue;
    }

    /**
     * This method is optional.
     *
     * @param defaultValue the default value used to determine if Drop-in should vault the customer's Venmo payment method. Must be set to `false` when using a client token without a `customerId`.
     *
     * This value is {@code false} by default.
     */
    public void setVaultVenmoDefaultValue(boolean defaultValue) {
        vaultVenmoDefaultValue = defaultValue;
    }

    /**
     * This method is optional.
     *
     * @param customerCheckBoxEnabled {@code true} shows save card CheckBox to allow user to choose whether or not to vault their card.
     * {@code false} does not show Save Card CheckBox. Default value is false.
     */
    public void setAllowVaultCardOverride(boolean customerCheckBoxEnabled) {
        allowVaultCardOverride = customerCheckBoxEnabled;
    }

    /**
     * This method is optional.
     *
     * Sets the Cardholder Name field status, which is how it will behave in {@link CardForm}.
     * Default is {@link CardForm#FIELD_DISABLED}.
     *
     * Can be {@link CardForm#FIELD_DISABLED}, {@link CardForm#FIELD_OPTIONAL}, or
     * {@link CardForm#FIELD_REQUIRED}.
     */
    public void setCardholderNameStatus(int fieldStatus) {
        cardholderNameStatus = fieldStatus;
    }

    /**
     * @return If PayPal is disabled in Drop-in
     */
    public boolean isPayPalDisabled() {
        return payPalDisabled;
    }

    /**
     * @return The PayPal Request {@link PayPalRequest} for the transaction.
     */
    public PayPalRequest getPayPalRequest() { return payPalRequest; }

    /**
     * @return If Venmo is disabled in Drop-in
     */
    public boolean isVenmoDisabled() {
        return venmoDisabled;
    }

    /**
     * @return If card payments are disabled in Drop-in
     */
    public boolean isCardDisabled() {
        return cardDisabled;
    }

    /**
     * @return The Google Pay Request {@link GooglePayRequest} for the transaction.
     */
    @Nullable
    public GooglePayRequest getGooglePayRequest() {
        return googlePayRequest;
    }

    /**
     * @return If Google Pay disabled in Drop-in
     */
    public boolean isGooglePayDisabled() {
        return googlePayDisabled;
    }

    /**
     * @return If a 3D Secure verification should be requested as part of Drop-in
     */
    public boolean getRequestThreeDSecureVerification() {
        return requestThreeDSecureVerification;
    }

    /**
     * @return The {@link ThreeDSecureRequest} for the transaction.
     */
    @Nullable
    public ThreeDSecureRequest getThreeDSecureRequest() { return threeDSecureRequest; }

    /**
     * @return If the card number field should be masked when the field is not focused.
     */
    public boolean getMaskCardNumber() {
        return maskCardNumber;
    }

    /**
     * @return If the security code should be masked during input.
     */
    public boolean getMaskSecurityCode() {
        return maskSecurityCode;
    }

    /**
     * @return The default value used to determine if Drop-in should vault the customer's Venmo payment method
     */
    public boolean getVaultVenmoDefaultValue() { return vaultVenmoDefaultValue; }

    /**
     * @return If vault manager is enabled to allow users manage their vaulted payment methods
     */
    public boolean isVaultManagerEnabled() {
        return vaultManagerEnabled;
    }

    /**
     * @return The Cardholder Name field status, which is how it will behave in {@link CardForm}.
     */
    public int getCardholderNameStatus() {
        return cardholderNameStatus;
    }

    /**
     * @return The default value used to determine if Drop-in should vault the customer's card.
     */
    public boolean getVaultCardDefaultValue() {
        return vaultCardDefaultValue;
    }

    /**
     * @return If the save card CheckBox will be shown to allow user to choose whether or not to vault their card.
     */
    public boolean getAllowVaultCardOverride() {
        return allowVaultCardOverride;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(googlePayRequest, 0);
        dest.writeByte(googlePayDisabled ? (byte) 1 : (byte) 0);
        dest.writeParcelable(payPalRequest, 0);
        dest.writeByte(payPalDisabled ? (byte) 1 : (byte) 0);
        dest.writeByte(venmoDisabled ? (byte) 1 : (byte) 0);
        dest.writeByte(cardDisabled ? (byte) 1 : (byte) 0);
        dest.writeByte(requestThreeDSecureVerification ? (byte) 1 : (byte) 0);
        dest.writeParcelable(threeDSecureRequest, 0);
        dest.writeByte(maskCardNumber ? (byte) 1 : (byte) 0);
        dest.writeByte(maskSecurityCode ? (byte) 1 : (byte) 0);
        dest.writeByte(vaultManagerEnabled ? (byte) 1 : (byte) 0);
        dest.writeInt(cardholderNameStatus);
        dest.writeByte(vaultCardDefaultValue ? (byte) 1 : (byte) 0);
        dest.writeByte(allowVaultCardOverride ? (byte) 1 : (byte) 0);
        dest.writeByte(vaultVenmoDefaultValue ? (byte) 1 : (byte) 0);
    }

    protected DropInRequest(Parcel in) {
        googlePayRequest = in.readParcelable(GooglePayRequest.class.getClassLoader());
        googlePayDisabled = in.readByte() != 0;
        payPalRequest = in.readParcelable(PayPalRequest.class.getClassLoader());
        payPalDisabled = in.readByte() != 0;
        venmoDisabled = in.readByte() != 0;
        cardDisabled = in.readByte() != 0;
        requestThreeDSecureVerification = in.readByte() != 0;
        threeDSecureRequest = in.readParcelable(ThreeDSecureRequest.class.getClassLoader());
        maskCardNumber = in.readByte() != 0;
        maskSecurityCode = in.readByte() != 0;
        vaultManagerEnabled = in.readByte() != 0;
        cardholderNameStatus = in.readInt();
        vaultCardDefaultValue = in.readByte() != 0;
        allowVaultCardOverride = in.readByte() != 0;
        vaultVenmoDefaultValue = in.readByte() != 0;
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
