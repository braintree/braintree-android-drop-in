package com.braintreepayments.api;

import android.os.Parcel;

import com.braintreepayments.cardform.view.CardForm;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.WalletConstants;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;

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

        VenmoRequest venmoRequest = new VenmoRequest(VenmoPaymentMethodUsage.SINGLE_USE);
        venmoRequest.setShouldVault(true);
        venmoRequest.setProfileId("profile-id");
        venmoRequest.setDisplayName("display-name");

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

        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setGooglePayRequest(googlePayRequest);
        dropInRequest.setGooglePayDisabled(true);
        dropInRequest.setPayPalRequest(paypalRequest);
        dropInRequest.setPayPalDisabled(true);
        dropInRequest.setVenmoRequest(venmoRequest);
        dropInRequest.setVenmoDisabled(true);
        dropInRequest.setCardDisabled(true);
        dropInRequest.setThreeDSecureRequest(threeDSecureRequest);
        dropInRequest.setMaskCardNumber(true);
        dropInRequest.setMaskSecurityCode(true);
        dropInRequest.setVaultManagerEnabled(true);
        dropInRequest.setAllowVaultCardOverride(true);
        dropInRequest.setVaultCardDefaultValue(true);
        dropInRequest.setCardholderNameStatus(CardForm.FIELD_OPTIONAL);
        dropInRequest.setCardLogosDisabled(true);

        assertNotNull(dropInRequest.getGooglePayRequest());
        assertEquals("10", dropInRequest.getGooglePayRequest().getTransactionInfo().getTotalPrice());
        assertEquals("USD", dropInRequest.getGooglePayRequest().getTransactionInfo().getCurrencyCode());
        assertEquals(WalletConstants.TOTAL_PRICE_STATUS_FINAL, dropInRequest.getGooglePayRequest().getTransactionInfo().getTotalPriceStatus());
        assertTrue(dropInRequest.getGooglePayRequest().isEmailRequired());
        assertTrue(dropInRequest.isGooglePayDisabled());

        PayPalCheckoutRequest checkoutRequest = (PayPalCheckoutRequest) dropInRequest.getPayPalRequest();
        assertEquals("10.00", checkoutRequest.getAmount());
        assertEquals("USD", checkoutRequest.getCurrencyCode());

        assertNotNull(dropInRequest.getVenmoRequest());
        assertEquals(VenmoPaymentMethodUsage.SINGLE_USE, dropInRequest.getVenmoRequest().getPaymentMethodUsage());
        assertTrue(dropInRequest.getVenmoRequest().getShouldVault());
        assertEquals("profile-id", dropInRequest.getVenmoRequest().getProfileId());
        assertEquals("display-name", dropInRequest.getVenmoRequest().getDisplayName());

        assertTrue(dropInRequest.isPayPalDisabled());
        assertTrue(dropInRequest.isVenmoDisabled());
        assertTrue(dropInRequest.isCardDisabled());
        assertTrue(dropInRequest.areCardLogosDisabled());
        assertNotNull(dropInRequest.getThreeDSecureRequest());
        assertEquals("abc-123", dropInRequest.getThreeDSecureRequest().getNonce());
        assertEquals("2", dropInRequest.getThreeDSecureRequest().getVersionRequested());
        assertEquals("10.00", dropInRequest.getThreeDSecureRequest().getAmount());
        assertEquals("tester@example.com", dropInRequest.getThreeDSecureRequest().getEmail());
        assertEquals("3125551234", dropInRequest.getThreeDSecureRequest().getMobilePhoneNumber());
        assertNotNull(dropInRequest.getThreeDSecureRequest().getBillingAddress());
        assertEquals("Given", dropInRequest.getThreeDSecureRequest().getBillingAddress().getGivenName());
        assertEquals("Surname", dropInRequest.getThreeDSecureRequest().getBillingAddress().getSurname());
        assertEquals("555 Smith St.", dropInRequest.getThreeDSecureRequest().getBillingAddress().getStreetAddress());
        assertEquals("#5", dropInRequest.getThreeDSecureRequest().getBillingAddress().getExtendedAddress());
        assertEquals("Chicago", dropInRequest.getThreeDSecureRequest().getBillingAddress().getLocality());
        assertEquals("IL", dropInRequest.getThreeDSecureRequest().getBillingAddress().getRegion());
        assertEquals("54321", dropInRequest.getThreeDSecureRequest().getBillingAddress().getPostalCode());
        assertEquals("US", dropInRequest.getThreeDSecureRequest().getBillingAddress().getCountryCodeAlpha2());
        assertEquals("3125557890", dropInRequest.getThreeDSecureRequest().getBillingAddress().getPhoneNumber());
        assertNotNull(dropInRequest.getThreeDSecureRequest().getAdditionalInformation());
        assertEquals("GEN", dropInRequest.getThreeDSecureRequest().getAdditionalInformation().getShippingMethodIndicator());
        assertTrue(dropInRequest.getMaskCardNumber());
        assertTrue(dropInRequest.getMaskSecurityCode());
        assertTrue(dropInRequest.isVaultManagerEnabled());
        assertTrue(dropInRequest.getVaultCardDefaultValue());
        assertTrue(dropInRequest.getAllowVaultCardOverride());
        assertEquals(CardForm.FIELD_OPTIONAL, dropInRequest.getCardholderNameStatus());
    }

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

        VenmoRequest venmoRequest = new VenmoRequest(VenmoPaymentMethodUsage.SINGLE_USE);
        venmoRequest.setShouldVault(true);
        venmoRequest.setProfileId("profile-id");
        venmoRequest.setDisplayName("display-name");

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
        threeDSecureRequest.setCardAddChallengeRequested(false);

        ThreeDSecureAdditionalInformation additionalInformation = new ThreeDSecureAdditionalInformation();
        additionalInformation.setShippingMethodIndicator("GEN");
        threeDSecureRequest.setAdditionalInformation(additionalInformation);

        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setGooglePayRequest(googlePayRequest);
        dropInRequest.setGooglePayDisabled(true);
        dropInRequest.setPayPalRequest(paypalRequest);
        dropInRequest.setPayPalDisabled(true);
        dropInRequest.setVenmoRequest(venmoRequest);
        dropInRequest.setVenmoDisabled(true);
        dropInRequest.setCardDisabled(true);
        dropInRequest.setThreeDSecureRequest(threeDSecureRequest);
        dropInRequest.setMaskCardNumber(true);
        dropInRequest.setMaskSecurityCode(true);
        dropInRequest.setVaultManagerEnabled(true);
        dropInRequest.setAllowVaultCardOverride(true);
        dropInRequest.setVaultCardDefaultValue(true);
        dropInRequest.setCardholderNameStatus(CardForm.FIELD_OPTIONAL);

        Parcel parcel = Parcel.obtain();
        dropInRequest.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        DropInRequest parceledDropInRequest = DropInRequest.CREATOR.createFromParcel(parcel);

        assertNotNull(parceledDropInRequest.getGooglePayRequest());
        assertEquals("10", parceledDropInRequest.getGooglePayRequest().getTransactionInfo().getTotalPrice());
        assertEquals("USD", parceledDropInRequest.getGooglePayRequest().getTransactionInfo().getCurrencyCode());
        assertEquals(WalletConstants.TOTAL_PRICE_STATUS_FINAL, parceledDropInRequest.getGooglePayRequest().getTransactionInfo().getTotalPriceStatus());
        assertTrue(parceledDropInRequest.getGooglePayRequest().isEmailRequired());

        PayPalCheckoutRequest checkoutRequest = (PayPalCheckoutRequest) parceledDropInRequest.getPayPalRequest();
        assertEquals("10.00", checkoutRequest.getAmount());
        assertEquals("USD", checkoutRequest.getCurrencyCode());

        assertNotNull(parceledDropInRequest.getVenmoRequest());
        assertEquals(VenmoPaymentMethodUsage.SINGLE_USE, parceledDropInRequest.getVenmoRequest().getPaymentMethodUsage());
        assertTrue(parceledDropInRequest.getVenmoRequest().getShouldVault());
        assertEquals("profile-id", parceledDropInRequest.getVenmoRequest().getProfileId());
        assertEquals("display-name", parceledDropInRequest.getVenmoRequest().getDisplayName());

        assertTrue(parceledDropInRequest.isGooglePayDisabled());
        assertTrue(parceledDropInRequest.isPayPalDisabled());
        assertTrue(parceledDropInRequest.isVenmoDisabled());
        assertTrue(parceledDropInRequest.isCardDisabled());
        assertNotNull(parceledDropInRequest.getThreeDSecureRequest());
        assertEquals("abc-123", parceledDropInRequest.getThreeDSecureRequest().getNonce());
        assertEquals("2", parceledDropInRequest.getThreeDSecureRequest().getVersionRequested());
        assertEquals("10.00", parceledDropInRequest.getThreeDSecureRequest().getAmount());
        assertEquals("tester@example.com", parceledDropInRequest.getThreeDSecureRequest().getEmail());
        assertEquals("3125551234", parceledDropInRequest.getThreeDSecureRequest().getMobilePhoneNumber());
        assertNotNull(parceledDropInRequest.getThreeDSecureRequest().getBillingAddress());
        assertEquals("Given", parceledDropInRequest.getThreeDSecureRequest().getBillingAddress().getGivenName());
        assertEquals("Surname", parceledDropInRequest.getThreeDSecureRequest().getBillingAddress().getSurname());
        assertEquals("555 Smith St.", parceledDropInRequest.getThreeDSecureRequest().getBillingAddress().getStreetAddress());
        assertEquals("#5", parceledDropInRequest.getThreeDSecureRequest().getBillingAddress().getExtendedAddress());
        assertEquals("Chicago", parceledDropInRequest.getThreeDSecureRequest().getBillingAddress().getLocality());
        assertEquals("IL", parceledDropInRequest.getThreeDSecureRequest().getBillingAddress().getRegion());
        assertEquals("54321", parceledDropInRequest.getThreeDSecureRequest().getBillingAddress().getPostalCode());
        assertEquals("US", parceledDropInRequest.getThreeDSecureRequest().getBillingAddress().getCountryCodeAlpha2());
        assertEquals("3125557890", parceledDropInRequest.getThreeDSecureRequest().getBillingAddress().getPhoneNumber());
        assertNotNull(parceledDropInRequest.getThreeDSecureRequest().getAdditionalInformation());
        assertEquals("GEN", parceledDropInRequest.getThreeDSecureRequest().getAdditionalInformation().getShippingMethodIndicator());
        assertTrue(parceledDropInRequest.getMaskCardNumber());
        assertTrue(parceledDropInRequest.getMaskSecurityCode());
        assertTrue(parceledDropInRequest.isVaultManagerEnabled());
        assertTrue(parceledDropInRequest.getVaultCardDefaultValue());
        assertTrue(parceledDropInRequest.getAllowVaultCardOverride());
        assertEquals(CardForm.FIELD_OPTIONAL, parceledDropInRequest.getCardholderNameStatus());
    }

    @Test
    public void getCardholderNameStatus_includesCardHolderNameStatus() {
        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setCardholderNameStatus(CardForm.FIELD_REQUIRED);

        assertEquals(CardForm.FIELD_REQUIRED, dropInRequest.getCardholderNameStatus());
    }
}
