package com.braintreepayments.demo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.braintreepayments.api.CardNonce;
import com.braintreepayments.api.DropInResultCallback;
import com.braintreepayments.api.DropInActivity;
import com.braintreepayments.api.DropInClient;
import com.braintreepayments.api.DropInRequest;
import com.braintreepayments.api.DropInResult;
import com.braintreepayments.api.FetchMostRecentPaymentMethodCallback;
import com.braintreepayments.api.GooglePayCardNonce;
import com.braintreepayments.api.GooglePayRequest;
import com.braintreepayments.api.PayPalAccountNonce;
import com.braintreepayments.api.PaymentMethodNonce;
import com.braintreepayments.api.DropInPaymentMethodType;
import com.braintreepayments.api.PostalAddress;
import com.braintreepayments.api.ThreeDSecureAdditionalInformation;
import com.braintreepayments.api.ThreeDSecurePostalAddress;
import com.braintreepayments.api.ThreeDSecureRequest;
import com.braintreepayments.api.VenmoAccountNonce;
import com.google.android.gms.identity.intents.model.UserAddress;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.WalletConstants;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends BaseActivity {

    private static final int DROP_IN_REQUEST = 100;

    private static final String KEY_NONCE = "nonce";

    private DropInPaymentMethodType mPaymentMethodType;
    private PaymentMethodNonce mNonce;

    private CardView mPaymentMethod;
    private ImageView mPaymentMethodIcon;
    private TextView mPaymentMethodTitle;
    private TextView mPaymentMethodDescription;
    private TextView mNonceString;
    private TextView mNonceDetails;
    private TextView mDeviceData;

    private Button mAddPaymentMethodButton;
    private Button mPurchaseButton;
    private ProgressDialog mLoading;

    private boolean mShouldMakePurchase = false;
    private boolean mPurchased = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mPaymentMethod = findViewById(R.id.payment_method);
        mPaymentMethodIcon = findViewById(R.id.payment_method_icon);
        mPaymentMethodTitle = findViewById(R.id.payment_method_title);
        mPaymentMethodDescription = findViewById(R.id.payment_method_description);
        mNonceString = findViewById(R.id.nonce);
        mNonceDetails = findViewById(R.id.nonce_details);
        mDeviceData = findViewById(R.id.device_data);

        mAddPaymentMethodButton = findViewById(R.id.add_payment_method);
        mPurchaseButton = findViewById(R.id.purchase);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_NONCE)) {
                mNonce = savedInstanceState.getParcelable(KEY_NONCE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mPurchased) {
            mPurchased = false;
            clearNonce();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mNonce != null) {
            outState.putParcelable(KEY_NONCE, mNonce);
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
                .putExtra(CreateTransactionActivity.EXTRA_PAYMENT_METHOD_NONCE, mNonce);
        startActivity(intent);

        mPurchased = true;
    }

    public void handleDropInResult(DropInResult result) {
        if (result.getPaymentMethodType() == null) {
            mAddPaymentMethodButton.setVisibility(VISIBLE);
        } else {
            mAddPaymentMethodButton.setVisibility(GONE);

            mPaymentMethodType = result.getPaymentMethodType();

            mPaymentMethodIcon.setImageResource(result.getPaymentMethodType().getDrawable());
            if (result.getPaymentMethodNonce() != null) {
                displayResult(result);
            }

            mPurchaseButton.setEnabled(true);
        }
    }

    @Override
    public void onError(Exception error) {
        super.onError(error);

        safelyCloseLoadingView();

        mShouldMakePurchase = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        safelyCloseLoadingView();

        if (resultCode == RESULT_OK) {
            DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
            displayResult(result);
            mPurchaseButton.setEnabled(true);
        } else if (resultCode != RESULT_CANCELED) {
            safelyCloseLoadingView();
            showDialog(((Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR))
                    .getMessage());
        }
    }

    @Override
    protected void reset() {
        mPurchaseButton.setEnabled(false);

        mAddPaymentMethodButton.setVisibility(GONE);

        clearNonce();
    }

    @Override
    protected void onAuthorizationFetched() {
        if (dropInClient != null) {
            return;
        }

        DropInRequest dropInRequest = new DropInRequest()
                .clientToken(mAuthorization)
                .requestThreeDSecureVerification(Settings.isThreeDSecureEnabled(this))
                .collectDeviceData(Settings.shouldCollectDeviceData(this))
                .googlePaymentRequest(getGooglePaymentRequest())
                .maskCardNumber(true)
                .maskSecurityCode(true)
                .allowVaultCardOverride(Settings.isSaveCardCheckBoxVisible(this))
                .vaultCard(Settings.defaultVaultSetting(this))
                .vaultManager(Settings.isVaultManagerEnabled(this))
                .cardholderNameStatus(Settings.getCardholderNameStatus(this));

        if (Settings.isThreeDSecureEnabled(this)) {
            dropInRequest.threeDSecureRequest(demoThreeDSecureRequest());
        }

        dropInClient = new DropInClient(this, mAuthorization, dropInRequest);
        dropInClient.fetchMostRecentPaymentMethod(this, new FetchMostRecentPaymentMethodCallback() {
            @Override
            public void onResult(DropInResult dropInResult, Exception error) {
                if (dropInResult != null) {
                    handleDropInResult(dropInResult);
                } else {
                    mAddPaymentMethodButton.setVisibility(VISIBLE);
                }
            }
        });

        dropInClient.deliverBrowserSwitchResult(this, new DropInResultCallback() {
            @Override
            public void onResult(@Nullable DropInResult dropInResult, @Nullable Exception error) {
                if (dropInResult != null) {
                    handleDropInResult(dropInResult);
                } else {
                    onError(error);
                }
            }
        });
    }

    private void displayResult(DropInResult dropInResult) {
        mNonce = dropInResult.getPaymentMethodNonce();
        mPaymentMethodType = DropInPaymentMethodType.forType(mNonce);

        mPaymentMethodIcon.setImageResource(DropInPaymentMethodType.forType(mNonce).getDrawable());

        mPaymentMethodTitle.setText(dropInResult.getPaymentMethodType().getCanonicalName());
        mPaymentMethodDescription.setText(dropInResult.getPaymentDescription());

        mPaymentMethod.setVisibility(VISIBLE);

        mNonceString.setText(getString(R.string.nonce) + ": " + mNonce.getString());
        mNonceString.setVisibility(VISIBLE);

        String details = "";
        if (mNonce instanceof CardNonce) {
            CardNonce cardNonce = (CardNonce) mNonce;

            details = "Card Last Two: " + cardNonce.getLastTwo() + "\n";
            details += "3DS isLiabilityShifted: " + cardNonce.getThreeDSecureInfo().isLiabilityShifted() + "\n";
            details += "3DS isLiabilityShiftPossible: " + cardNonce.getThreeDSecureInfo().isLiabilityShiftPossible();
        } else if (mNonce instanceof PayPalAccountNonce) {
            PayPalAccountNonce paypalAccountNonce = (PayPalAccountNonce) mNonce;

            details = "First name: " + paypalAccountNonce.getFirstName() + "\n";
            details += "Last name: " + paypalAccountNonce.getLastName() + "\n";
            details += "Email: " + paypalAccountNonce.getEmail() + "\n";
            details += "Phone: " + paypalAccountNonce.getPhone() + "\n";
            details += "Payer id: " + paypalAccountNonce.getPayerId() + "\n";
            details += "Client metadata id: " + paypalAccountNonce.getClientMetadataId() + "\n";
            details += "Billing address: " + formatAddress(paypalAccountNonce.getBillingAddress()) + "\n";
            details += "Shipping address: " + formatAddress(paypalAccountNonce.getShippingAddress());
        } else if (mNonce instanceof VenmoAccountNonce) {
            VenmoAccountNonce venmoAccountNonce = (VenmoAccountNonce) mNonce;

            details = "Username: " + venmoAccountNonce.getUsername();
        } else if (mNonce instanceof GooglePayCardNonce) {
            GooglePayCardNonce googlePaymentCardNonce = (GooglePayCardNonce) mNonce;

            details = "Underlying Card Last Two: " + googlePaymentCardNonce.getLastTwo() + "\n";
            details += "Email: " + googlePaymentCardNonce.getEmail() + "\n";
            details += "Billing address: " + formatAddress(googlePaymentCardNonce.getBillingAddress()) + "\n";
            details += "Shipping address: " + formatAddress(googlePaymentCardNonce.getShippingAddress());
        }

        mNonceDetails.setText(details);
        mNonceDetails.setVisibility(VISIBLE);

        mDeviceData.setText("Device Data: " + dropInResult.getDeviceData());
        mDeviceData.setVisibility(VISIBLE);

        mAddPaymentMethodButton.setVisibility(GONE);
        mPurchaseButton.setEnabled(true);
    }

    private void clearNonce() {
        mPaymentMethod.setVisibility(GONE);
        mNonceString.setVisibility(GONE);
        mNonceDetails.setVisibility(GONE);
        mDeviceData.setVisibility(GONE);
        mPurchaseButton.setEnabled(false);
    }

    private String formatAddress(PostalAddress address) {
        return address.getRecipientName() + " " + address.getStreetAddress() + " " +
            address.getExtendedAddress() + " " + address.getLocality() + " " + address.getRegion() +
                " " + address.getPostalCode() + " " + address.getCountryCodeAlpha2();
    }

    private String formatAddress(UserAddress address) {
        if(address == null) {
            return "null";
        }
        return address.getName() + " " + address.getAddress1() + " " + address.getAddress2() + " " +
                address.getAddress3() + " " + address.getAddress4() + " " + address.getAddress5() + " " +
                address.getLocality() + " " + address.getAdministrativeArea() + " " + address.getPostalCode() + " " +
                address.getSortingCode() + " " + address.getCountryCode();
    }

    private GooglePayRequest getGooglePaymentRequest() {
        GooglePayRequest googlePayRequest = new GooglePayRequest();
        googlePayRequest.setTransactionInfo(TransactionInfo.newBuilder()
                .setTotalPrice("1.00")
                .setCurrencyCode("USD")
                .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                .build());
        googlePayRequest.setEmailRequired(true);
        return googlePayRequest;
    }

    private void safelyCloseLoadingView() {
        if (mLoading != null && mLoading.isShowing()) {
            mLoading.dismiss();
        }
    }
}
