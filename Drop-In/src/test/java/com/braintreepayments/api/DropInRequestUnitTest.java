package com.braintreepayments.api;

import android.content.Intent;
import android.os.Parcel;

import com.braintreepayments.cardform.view.CardForm;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.WalletConstants;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static com.braintreepayments.api.TestTokenizationKey.TOKENIZATION_KEY;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class DropInRequestUnitTest {

    @Test
    public void includesAllOptions() {
        GooglePayRequest googlePayRequest = new GooglePayRequest();
        googlePayRequest.setTransactionInfo(TransactionInfo.newBuilder()
                .setTotalPrice("10")
                .setCurrencyCode("USD")
                .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                .build());
        googlePayRequest.setEmailRequired(true);

        PayPalCheckoutRequest paypalRequest = new PayPalCheckoutRequest("10.00");
        paypalRequest.setCurrencyCode("USD");

        ThreeDSecureRequest threeDSecureRequest = new ThreeDSecureRequest();
        threeDSecureRequest.setNonce("abc-123");
        threeDSecureRequest.setVersionRequested(ThreeDSecureRequest.VERSION_2);
        threeDSecureRequest.setAmount("10.00");
        threeDSecureRequest.setEmail("tester@example.com");
        threeDSecureRequest.setMobilePhoneNumber("3125551234");

        ThreeDSecurePostalAddress billingAddress = new ThreeDSecurePostalAddress();
        billingAddress.setGivenName("Given");
        billingAddress.setSurname("Surname");
        billingAddress.setStreetAddress("555 Smith St.");
        billingAddress.setExtendedAddress("#5");
        billingAddress.setLocality("Chicago");
        billingAddress.setRegion("IL");
        billingAddress.setPostalCode("54321");
        billingAddress.setCountryCodeAlpha2("US");
        billingAddress.setPhoneNumber("3125557890");
        threeDSecureRequest.setBillingAddress(billingAddress);

        ThreeDSecureAdditionalInformation additionalInformation = new ThreeDSecureAdditionalInformation();
        additionalInformation.setShippingMethodIndicator("GEN");
        threeDSecureRequest.setAdditionalInformation(additionalInformation);

        Intent intent = new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY)
                .collectDeviceData(true)
                .amount("1.00")
                .googlePaymentRequest(googlePayRequest)
                .disableGooglePayment()
                .paypalRequest(paypalRequest)
                .disablePayPal()
                .disableVenmo()
                .disableCard()
                .requestThreeDSecureVerification(true)
                .threeDSecureRequest(threeDSecureRequest)
                .maskCardNumber(true)
                .maskSecurityCode(true)
                .vaultManager(true)
                .allowVaultCardOverride(true)
                .vaultCard(true)
                .cardholderNameStatus(CardForm.FIELD_OPTIONAL)
                .vaultVenmo(true)
                .getIntent(RuntimeEnvironment.application);

        DropInRequest dropInRequest = intent.getParcelableExtra(DropInRequest.EXTRA_CHECKOUT_REQUEST);

        assertEquals(DropInActivity.class.getName(), intent.getComponent().getClassName());
        assertEquals(TOKENIZATION_KEY, dropInRequest.getAuthorization());
        assertTrue(dropInRequest.shouldCollectDeviceData());
        assertEquals("1.00", dropInRequest.getAmount());
        assertEquals("10", dropInRequest.getGooglePaymentRequest().getTransactionInfo().getTotalPrice());
        assertEquals("USD", dropInRequest.getGooglePaymentRequest().getTransactionInfo().getCurrencyCode());
        assertEquals(WalletConstants.TOTAL_PRICE_STATUS_FINAL, dropInRequest.getGooglePaymentRequest().getTransactionInfo().getTotalPriceStatus());
        assertTrue(dropInRequest.getGooglePaymentRequest().isEmailRequired());
        assertFalse(dropInRequest.isGooglePaymentEnabled());

        PayPalCheckoutRequest checkoutRequest = (PayPalCheckoutRequest) dropInRequest.getPayPalRequest();
        assertEquals("10", checkoutRequest.getAmount());
        assertEquals("USD", checkoutRequest.getCurrencyCode());

        assertFalse(dropInRequest.isPayPalEnabled());
        assertFalse(dropInRequest.isVenmoEnabled());
        assertFalse(dropInRequest.isCardEnabled());
        assertTrue(dropInRequest.shouldRequestThreeDSecureVerification());
        assertEquals("abc-123", dropInRequest.getThreeDSecureRequest().getNonce());
        assertEquals("2", dropInRequest.getThreeDSecureRequest().getVersionRequested());
        assertEquals("10.00", dropInRequest.getThreeDSecureRequest().getAmount());
        assertEquals("tester@example.com", dropInRequest.getThreeDSecureRequest().getEmail());
        assertEquals("3125551234", dropInRequest.getThreeDSecureRequest().getMobilePhoneNumber());
        assertEquals("Given", dropInRequest.getThreeDSecureRequest().getBillingAddress().getGivenName());
        assertEquals("Surname", dropInRequest.getThreeDSecureRequest().getBillingAddress().getSurname());
        assertEquals("555 Smith St.", dropInRequest.getThreeDSecureRequest().getBillingAddress().getStreetAddress());
        assertEquals("#5", dropInRequest.getThreeDSecureRequest().getBillingAddress().getExtendedAddress());
        assertEquals("Chicago", dropInRequest.getThreeDSecureRequest().getBillingAddress().getLocality());
        assertEquals("IL", dropInRequest.getThreeDSecureRequest().getBillingAddress().getRegion());
        assertEquals("54321", dropInRequest.getThreeDSecureRequest().getBillingAddress().getPostalCode());
        assertEquals("US", dropInRequest.getThreeDSecureRequest().getBillingAddress().getCountryCodeAlpha2());
        assertEquals("3125557890", dropInRequest.getThreeDSecureRequest().getBillingAddress().getPhoneNumber());
        assertEquals("GEN", dropInRequest.getThreeDSecureRequest().getAdditionalInformation().getShippingMethodIndicator());
        assertTrue(dropInRequest.shouldMaskCardNumber());
        assertTrue(dropInRequest.shouldMaskSecurityCode());
        assertTrue(dropInRequest.isVaultManagerEnabled());
        assertTrue(dropInRequest.getDefaultVaultSetting());
        assertTrue(dropInRequest.isSaveCardCheckBoxShown());
        assertEquals(CardForm.FIELD_OPTIONAL, dropInRequest.getCardholderNameStatus());
        assertTrue(dropInRequest.shouldVaultVenmo());
    }

    @Test
    public void hasCorrectDefaults() {
        Intent intent = new DropInRequest()
                .getIntent(RuntimeEnvironment.application);

        DropInRequest dropInRequest = intent.getParcelableExtra(DropInRequest.EXTRA_CHECKOUT_REQUEST);

        assertEquals(DropInActivity.class.getName(), intent.getComponent().getClassName());
        assertNull(dropInRequest.getAuthorization());
        assertFalse(dropInRequest.shouldCollectDeviceData());
        assertNull(dropInRequest.getAmount());
        assertTrue(dropInRequest.isGooglePaymentEnabled());
        assertNull(dropInRequest.getPayPalRequest());
        assertTrue(dropInRequest.isPayPalEnabled());
        assertTrue(dropInRequest.isVenmoEnabled());
        assertTrue(dropInRequest.isCardEnabled());
        assertFalse(dropInRequest.shouldRequestThreeDSecureVerification());
        assertNull(dropInRequest.getThreeDSecureRequest());
        assertFalse(dropInRequest.shouldMaskCardNumber());
        assertFalse(dropInRequest.shouldMaskSecurityCode());
        assertFalse(dropInRequest.isVaultManagerEnabled());
        assertFalse(dropInRequest.isSaveCardCheckBoxShown());
        assertTrue(dropInRequest.getDefaultVaultSetting());
        assertFalse(dropInRequest.shouldVaultVenmo());
        assertEquals(CardForm.FIELD_DISABLED, dropInRequest.getCardholderNameStatus());
    }

    // TODO: this test is failing because we are parceling PayPalRequest instead of specific typed request
    @Test
    public void isParcelable() {
        GooglePayRequest googlePayRequest = new GooglePayRequest();
        googlePayRequest.setTransactionInfo(TransactionInfo.newBuilder()
                .setTotalPrice("10")
                .setCurrencyCode("USD")
                .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                .build());
        googlePayRequest.setEmailRequired(true);

        PayPalCheckoutRequest paypalRequest = new PayPalCheckoutRequest("10.00");
        paypalRequest.setCurrencyCode("USD");

        ThreeDSecureRequest threeDSecureRequest = new ThreeDSecureRequest();
        threeDSecureRequest.setNonce("abc-123");
        threeDSecureRequest.setVersionRequested(ThreeDSecureRequest.VERSION_2);
        threeDSecureRequest.setAmount("10.00");
        threeDSecureRequest.setEmail("tester@example.com");
        threeDSecureRequest.setMobilePhoneNumber("3125551234");

        ThreeDSecurePostalAddress billingAddress = new ThreeDSecurePostalAddress();
        billingAddress.setGivenName("Given");
        billingAddress.setSurname("Surname");
        billingAddress.setStreetAddress("555 Smith St.");
        billingAddress.setExtendedAddress("#5");
        billingAddress.setLocality("Chicago");
        billingAddress.setRegion("IL");
        billingAddress.setPostalCode("54321");
        billingAddress.setCountryCodeAlpha2("US");
        billingAddress.setPhoneNumber("3125557890");
        threeDSecureRequest.setBillingAddress(billingAddress);

        ThreeDSecureAdditionalInformation additionalInformation = new ThreeDSecureAdditionalInformation();
        additionalInformation.setShippingMethodIndicator("GEN");
        threeDSecureRequest.setAdditionalInformation(additionalInformation);

        DropInRequest dropInRequest = new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY)
                .collectDeviceData(true)
                .amount("1.00")
                .googlePaymentRequest(googlePayRequest)
                .disableGooglePayment()
                .paypalRequest(paypalRequest)
                .disablePayPal()
                .disableVenmo()
                .disableCard()
                .requestThreeDSecureVerification(true)
                .threeDSecureRequest(threeDSecureRequest)
                .maskCardNumber(true)
                .maskSecurityCode(true)
                .vaultManager(true)
                .vaultCard(true)
                .allowVaultCardOverride(true)
                .cardholderNameStatus(CardForm.FIELD_OPTIONAL)
                .vaultVenmo(true);

        Parcel parcel = Parcel.obtain();
        dropInRequest.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        DropInRequest parceledDropInRequest = DropInRequest.CREATOR.createFromParcel(parcel);

        assertEquals(TOKENIZATION_KEY, parceledDropInRequest.getAuthorization());
        assertTrue(parceledDropInRequest.shouldCollectDeviceData());
        assertEquals("1.00", parceledDropInRequest.getAmount());
        assertEquals("10", dropInRequest.getGooglePaymentRequest().getTransactionInfo().getTotalPrice());
        assertEquals("USD", dropInRequest.getGooglePaymentRequest().getTransactionInfo().getCurrencyCode());
        assertEquals(WalletConstants.TOTAL_PRICE_STATUS_FINAL, dropInRequest.getGooglePaymentRequest().getTransactionInfo().getTotalPriceStatus());
        assertTrue(dropInRequest.getGooglePaymentRequest().isEmailRequired());

        PayPalCheckoutRequest checkoutRequest = (PayPalCheckoutRequest) dropInRequest.getPayPalRequest();
        assertEquals("10", checkoutRequest.getAmount());
        assertEquals("USD", checkoutRequest.getCurrencyCode());

        assertFalse(dropInRequest.isGooglePaymentEnabled());
        assertFalse(parceledDropInRequest.isPayPalEnabled());
        assertFalse(parceledDropInRequest.isVenmoEnabled());
        assertFalse(parceledDropInRequest.isCardEnabled());
        assertTrue(parceledDropInRequest.shouldRequestThreeDSecureVerification());
        assertEquals("abc-123", dropInRequest.getThreeDSecureRequest().getNonce());
        assertEquals("2", dropInRequest.getThreeDSecureRequest().getVersionRequested());
        assertEquals("10.00", dropInRequest.getThreeDSecureRequest().getAmount());
        assertEquals("tester@example.com", dropInRequest.getThreeDSecureRequest().getEmail());
        assertEquals("3125551234", dropInRequest.getThreeDSecureRequest().getMobilePhoneNumber());
        assertEquals("Given", dropInRequest.getThreeDSecureRequest().getBillingAddress().getGivenName());
        assertEquals("Surname", dropInRequest.getThreeDSecureRequest().getBillingAddress().getSurname());
        assertEquals("555 Smith St.", dropInRequest.getThreeDSecureRequest().getBillingAddress().getStreetAddress());
        assertEquals("#5", dropInRequest.getThreeDSecureRequest().getBillingAddress().getExtendedAddress());
        assertEquals("Chicago", dropInRequest.getThreeDSecureRequest().getBillingAddress().getLocality());
        assertEquals("IL", dropInRequest.getThreeDSecureRequest().getBillingAddress().getRegion());
        assertEquals("54321", dropInRequest.getThreeDSecureRequest().getBillingAddress().getPostalCode());
        assertEquals("US", dropInRequest.getThreeDSecureRequest().getBillingAddress().getCountryCodeAlpha2());
        assertEquals("3125557890", dropInRequest.getThreeDSecureRequest().getBillingAddress().getPhoneNumber());
        assertEquals("GEN", dropInRequest.getThreeDSecureRequest().getAdditionalInformation().getShippingMethodIndicator());
        assertTrue(parceledDropInRequest.shouldMaskCardNumber());
        assertTrue(parceledDropInRequest.shouldMaskSecurityCode());
        assertTrue(parceledDropInRequest.isVaultManagerEnabled());
        assertTrue(parceledDropInRequest.getDefaultVaultSetting());
        assertTrue(parceledDropInRequest.isSaveCardCheckBoxShown());
        assertEquals(CardForm.FIELD_OPTIONAL, parceledDropInRequest.getCardholderNameStatus());
        assertTrue(parceledDropInRequest.shouldVaultVenmo());
    }

    @Test
    public void getIntent_includesClientToken() {
        DropInRequest dropInRequest = new DropInRequest()
                .clientToken(Fixtures.CLIENT_TOKEN);

        assertEquals(Fixtures.CLIENT_TOKEN, dropInRequest.getAuthorization());
    }

    @Test
    public void getIntent_includesTokenizationKey() {
        DropInRequest dropInRequest = new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY);

        assertEquals(TOKENIZATION_KEY, dropInRequest.getAuthorization());
    }

    @Test
    public void getCardholderNameStatus_includesCardHolderNameStatus() {
        DropInRequest dropInRequest = new DropInRequest()
                .cardholderNameStatus(CardForm.FIELD_REQUIRED);

        assertEquals(CardForm.FIELD_REQUIRED, dropInRequest.getCardholderNameStatus());
    }
}
