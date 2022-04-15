package com.braintreepayments.demo;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.braintreepayments.api.CardNonce;
import com.braintreepayments.api.DropInClient;
import com.braintreepayments.api.DropInListener;
import com.braintreepayments.api.DropInPaymentMethod;
import com.braintreepayments.api.DropInRequest;
import com.braintreepayments.api.DropInResult;
import com.braintreepayments.api.GooglePayCardNonce;
import com.braintreepayments.api.GooglePayRequest;
import com.braintreepayments.api.PayPalAccountNonce;
import com.braintreepayments.api.PaymentMethodNonce;
import com.braintreepayments.api.PostalAddress;
import com.braintreepayments.api.ThreeDSecureAdditionalInformation;
import com.braintreepayments.api.ThreeDSecurePostalAddress;
import com.braintreepayments.api.ThreeDSecureRequest;
import com.braintreepayments.api.VenmoAccountNonce;
import com.braintreepayments.api.VenmoPaymentMethodUsage;
import com.braintreepayments.api.VenmoRequest;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.WalletConstants;

public class MainActivity extends BaseActivity implements DropInListener {

    private static final int DROP_IN_REQUEST = 100;

    private static final String KEY_NONCE = "nonce";

    private PaymentMethodNonce nonce;

    private CardView paymentMethod;
    private ImageView paymentMethodIcon;
    private TextView paymentMethodTitle;
    private TextView paymentMethodDescription;
    private TextView nonceString;
    private TextView nonceDetails;
    private TextView deviceData;

    private Button addPaymentMethodButton;
    private Button purchaseButton;

    private DropInClient dropInClient;

    private boolean purchased = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        paymentMethod = findViewById(R.id.payment_method);
        paymentMethodIcon = findViewById(R.id.payment_method_icon);
        paymentMethodTitle = findViewById(R.id.payment_method_title);
        paymentMethodDescription = findViewById(R.id.payment_method_description);
        nonceString = findViewById(R.id.nonce);
        nonceDetails = findViewById(R.id.nonce_details);
        deviceData = findViewById(R.id.device_data);

        addPaymentMethodButton = findViewById(R.id.add_payment_method);
        purchaseButton = findViewById(R.id.purchase);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_NONCE)) {
                nonce = savedInstanceState.getParcelable(KEY_NONCE);
            }
        }

        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setGooglePayRequest(getGooglePayRequest());
        dropInRequest.setVenmoRequest(new VenmoRequest(VenmoPaymentMethodUsage.SINGLE_USE));
        dropInRequest.setMaskCardNumber(true);
        dropInRequest.setMaskSecurityCode(true);
        dropInRequest.setAllowVaultCardOverride(Settings.isSaveCardCheckBoxVisible(this));
        dropInRequest.setVaultCardDefaultValue(Settings.defaultVaultSetting(this));
        dropInRequest.setVaultManagerEnabled(Settings.isVaultManagerEnabled(this));
        dropInRequest.setCardholderNameStatus(Settings.getCardholderNameStatus(this));

        if (Settings.isThreeDSecureEnabled(this)) {
            dropInRequest.setThreeDSecureRequest(demoThreeDSecureRequest());
        }

        if (Settings.useTokenizationKey(this)) {
            String tokenizationKey = Settings.getEnvironmentTokenizationKey(this);
            dropInClient = new DropInClient(this, tokenizationKey, dropInRequest);
        } else {
            dropInClient = new DropInClient(this, dropInRequest, new DemoClientTokenProvider(this));
            dropInClient.setListener(this);
        }

        dropInClient.fetchMostRecentPaymentMethod(this, (dropInResult, error) -> {
            if (dropInResult != null) {
                handleDropInResult(dropInResult);
            } else {
                addPaymentMethodButton.setVisibility(VISIBLE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (purchased) {
            purchased = false;
            clearNonce();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (nonce != null) {
            outState.putParcelable(KEY_NONCE, nonce);
        }
    }

    public void launchDropIn(View v) {
        dropInClient.launchDropInForResult(this, DROP_IN_REQUEST);
    }

    private ThreeDSecureRequest demoThreeDSecureRequest() {
        ThreeDSecurePostalAddress billingAddress = new ThreeDSecurePostalAddress();
        billingAddress.setGivenName("Jill");
        billingAddress.setSurname("Doe");
        billingAddress.setPhoneNumber("5551234567");
        billingAddress.setStreetAddress("555 Smith St");
        billingAddress.setExtendedAddress("#2");
        billingAddress.setLocality("Chicago");
        billingAddress.setRegion("IL");
        billingAddress.setPostalCode("12345");
        billingAddress.setCountryCodeAlpha2("US");

        ThreeDSecureAdditionalInformation additionalInformation = new ThreeDSecureAdditionalInformation();
        additionalInformation.setAccountId("account-id");

        ThreeDSecureRequest threeDSecureRequest = new ThreeDSecureRequest();
        threeDSecureRequest.setAmount("1.00");
        threeDSecureRequest.setVersionRequested(Settings.getThreeDSecureVersion(this));
        threeDSecureRequest.setEmail("test@email.com");
        threeDSecureRequest.setMobilePhoneNumber("3125551234");
        threeDSecureRequest.setBillingAddress(billingAddress);
        threeDSecureRequest.setAdditionalInformation(additionalInformation);

        return threeDSecureRequest;
    }

    public void purchase(View v) {
        Intent intent = new Intent(this, CreateTransactionActivity.class)
                .putExtra(CreateTransactionActivity.EXTRA_PAYMENT_METHOD_NONCE, nonce);
        startActivity(intent);

        purchased = true;
    }

    public void handleDropInResult(DropInResult result) {
        if (result.getPaymentMethodType() == null) {
            addPaymentMethodButton.setVisibility(VISIBLE);
        } else {
            addPaymentMethodButton.setVisibility(GONE);

            paymentMethodIcon.setImageResource(result.getPaymentMethodType().getDrawable());
            if (result.getPaymentMethodNonce() != null) {
                displayResult(result);
            }

            purchaseButton.setEnabled(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
            displayResult(result);
            purchaseButton.setEnabled(true);
        } else if (resultCode != RESULT_CANCELED) {
            showDialog(((Exception) data.getSerializableExtra(DropInResult.EXTRA_ERROR))
                    .getMessage());
        }
    }

    @Override
    protected void reset() {
        purchaseButton.setEnabled(false);

        addPaymentMethodButton.setVisibility(GONE);

        clearNonce();
    }

    private void displayResult(DropInResult dropInResult) {
        nonce = dropInResult.getPaymentMethodNonce();

        DropInPaymentMethod paymentMethodType = dropInResult.getPaymentMethodType();
        if (paymentMethodType != null) {
            paymentMethodTitle.setText(paymentMethodType.getLocalizedName());
            paymentMethodIcon.setImageResource(paymentMethodType.getDrawable());
        }
        paymentMethodDescription.setText(dropInResult.getPaymentDescription());

        paymentMethod.setVisibility(VISIBLE);

        nonceString.setText(getString(R.string.nonce) + ": " + nonce.getString());
        nonceString.setVisibility(VISIBLE);

        String details = "";
        if (nonce instanceof CardNonce) {
            CardNonce cardNonce = (CardNonce) nonce;

            details = "Card Last Two: " + cardNonce.getLastTwo() + "\n";
            details += "3DS isLiabilityShifted: " + cardNonce.getThreeDSecureInfo().isLiabilityShifted() + "\n";
            details += "3DS isLiabilityShiftPossible: " + cardNonce.getThreeDSecureInfo().isLiabilityShiftPossible();
        } else if (nonce instanceof PayPalAccountNonce) {
            PayPalAccountNonce paypalAccountNonce = (PayPalAccountNonce) nonce;

            details = "First name: " + paypalAccountNonce.getFirstName() + "\n";
            details += "Last name: " + paypalAccountNonce.getLastName() + "\n";
            details += "Email: " + paypalAccountNonce.getEmail() + "\n";
            details += "Phone: " + paypalAccountNonce.getPhone() + "\n";
            details += "Payer id: " + paypalAccountNonce.getPayerId() + "\n";
            details += "Client metadata id: " + paypalAccountNonce.getClientMetadataId() + "\n";
            details += "Billing address: " + formatAddress(paypalAccountNonce.getBillingAddress()) + "\n";
            details += "Shipping address: " + formatAddress(paypalAccountNonce.getShippingAddress());
        } else if (nonce instanceof VenmoAccountNonce) {
            VenmoAccountNonce venmoAccountNonce = (VenmoAccountNonce) nonce;

            details = "Username: " + venmoAccountNonce.getUsername();
        } else if (nonce instanceof GooglePayCardNonce) {
            GooglePayCardNonce googlePayCardNonce = (GooglePayCardNonce) nonce;

            details = "Underlying Card Last Two: " + googlePayCardNonce.getLastTwo() + "\n";
            details += "Email: " + googlePayCardNonce.getEmail() + "\n";
            details += "Billing address: " + formatAddress(googlePayCardNonce.getBillingAddress()) + "\n";
            details += "Shipping address: " + formatAddress(googlePayCardNonce.getShippingAddress());
        }

        nonceDetails.setText(details);
        nonceDetails.setVisibility(VISIBLE);

        deviceData.setText("Device Data: " + dropInResult.getDeviceData());
        deviceData.setVisibility(VISIBLE);

        addPaymentMethodButton.setVisibility(GONE);
        purchaseButton.setEnabled(true);
    }

    private void clearNonce() {
        paymentMethod.setVisibility(GONE);
        nonceString.setVisibility(GONE);
        nonceDetails.setVisibility(GONE);
        deviceData.setVisibility(GONE);
        purchaseButton.setEnabled(false);
    }

    private String formatAddress(PostalAddress address) {
        return address.getRecipientName() + " " + address.getStreetAddress() + " " +
            address.getExtendedAddress() + " " + address.getLocality() + " " + address.getRegion() +
                " " + address.getPostalCode() + " " + address.getCountryCodeAlpha2();
    }

    private GooglePayRequest getGooglePayRequest() {
        GooglePayRequest googlePayRequest = new GooglePayRequest();
        googlePayRequest.setTransactionInfo(TransactionInfo.newBuilder()
                .setTotalPrice("1.00")
                .setCurrencyCode("USD")
                .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                .build());
        googlePayRequest.setEmailRequired(true);
        return googlePayRequest;
    }

    @Override
    public void onDropInSuccess(@NonNull DropInResult dropInResult) {
        displayResult(dropInResult);
        purchaseButton.setEnabled(true);
    }

    @Override
    public void onDropInFailure(@NonNull Exception error) {
        showDialog(error.getMessage());
    }
}
