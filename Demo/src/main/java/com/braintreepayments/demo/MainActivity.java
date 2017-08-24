package com.braintreepayments.demo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.braintreepayments.api.AndroidPay;
import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.PayPal;
import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.AndroidPayCardNonce;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.ClientToken;
import com.braintreepayments.api.models.PayPalAccountNonce;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.models.PostalAddress;
import com.braintreepayments.api.models.VenmoAccountNonce;
import com.google.android.gms.identity.intents.model.CountrySpecification;
import com.google.android.gms.identity.intents.model.UserAddress;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.LineItem;

import java.util.ArrayList;
import java.util.Collections;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends BaseActivity implements PaymentMethodNonceCreatedListener,
        BraintreeCancelListener, BraintreeErrorListener, DropInResult.DropInResultListener {

    private static final int DROP_IN_REQUEST = 100;

    private static final String KEY_NONCE = "nonce";

    private PaymentMethodType mPaymentMethodType;
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

            try {
                if (ClientToken.fromString(mAuthorization) instanceof ClientToken) {
                    DropInResult.fetchDropInResult(this, mAuthorization, this);
                } else {
                    mAddPaymentMethodButton.setVisibility(VISIBLE);
                }
            } catch (InvalidArgumentException e) {
                mAddPaymentMethodButton.setVisibility(VISIBLE);
            }
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
        DropInRequest dropInRequest = new DropInRequest()
                .clientToken(mAuthorization)
                .amount("1.00")
                .requestThreeDSecureVerification(Settings.isThreeDSecureEnabled(this))
                .collectDeviceData(Settings.shouldCollectDeviceData(this))
                .androidPayCart(getAndroidPayCart())
                .androidPayShippingAddressRequired(Settings.isAndroidPayShippingAddressRequired(this))
                .androidPayPhoneNumberRequired(Settings.isAndroidPayPhoneNumberRequired(this))
                .androidPayAllowedCountriesForShipping(Settings.getAndroidPayAllowedCountriesForShipping(this));

        if (Settings.isPayPalAddressScopeRequested(this)) {
            dropInRequest.paypalAdditionalScopes(Collections.singletonList(PayPal.SCOPE_ADDRESS));
        }

        startActivityForResult(dropInRequest.getIntent(this), DROP_IN_REQUEST);
    }

    public void purchase(View v) {
        if (mPaymentMethodType == PaymentMethodType.ANDROID_PAY && mNonce == null) {
            ArrayList<CountrySpecification> countries = new ArrayList<>();
            for(String countryCode : Settings.getAndroidPayAllowedCountriesForShipping(this)) {
                countries.add(new CountrySpecification(countryCode));
            }

            mShouldMakePurchase = true;

            AndroidPay.requestAndroidPay(mBraintreeFragment, getAndroidPayCart(),
                    Settings.isAndroidPayShippingAddressRequired(this),
                    Settings.isAndroidPayPhoneNumberRequired(this), countries);
        } else {
            Intent intent = new Intent(this, CreateTransactionActivity.class)
                    .putExtra(CreateTransactionActivity.EXTRA_PAYMENT_METHOD_NONCE, mNonce);
            startActivity(intent);

            mPurchased = true;
        }
    }

    @Override
    public void onResult(DropInResult result) {
        if (result.getPaymentMethodType() == null) {
            mAddPaymentMethodButton.setVisibility(VISIBLE);
        } else {
            mAddPaymentMethodButton.setVisibility(GONE);

            mPaymentMethodType = result.getPaymentMethodType();

            mPaymentMethodIcon.setImageResource(result.getPaymentMethodType().getDrawable());
            if (result.getPaymentMethodNonce() != null) {
                displayResult(result.getPaymentMethodNonce(), result.getDeviceData());
            } else if (result.getPaymentMethodType() == PaymentMethodType.ANDROID_PAY) {
                mPaymentMethodTitle.setText(PaymentMethodType.ANDROID_PAY.getLocalizedName());
                mPaymentMethodDescription.setText("");
                mPaymentMethod.setVisibility(VISIBLE);
            }

            mPurchaseButton.setEnabled(true);
        }
    }

    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
        super.onPaymentMethodNonceCreated(paymentMethodNonce);

        displayResult(paymentMethodNonce, null);
        safelyCloseLoadingView();

        if (mShouldMakePurchase) {
            purchase(null);
        }
    }

    @Override
    public void onCancel(int requestCode) {
        super.onCancel(requestCode);

        safelyCloseLoadingView();

        mShouldMakePurchase = false;
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
            displayResult(result.getPaymentMethodNonce(), result.getDeviceData());
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
        try {
            mBraintreeFragment = BraintreeFragment.newInstance(this, mAuthorization);

            if (ClientToken.fromString(mAuthorization) instanceof ClientToken) {
                DropInResult.fetchDropInResult(this, mAuthorization, this);
            } else {
                mAddPaymentMethodButton.setVisibility(VISIBLE);
            }
        } catch (InvalidArgumentException e) {
            showDialog(e.getMessage());
        }
    }

    private void displayResult(PaymentMethodNonce paymentMethodNonce, String deviceData) {
        mNonce = paymentMethodNonce;
        mPaymentMethodType = PaymentMethodType.forType(mNonce);

        mPaymentMethodIcon.setImageResource(PaymentMethodType.forType(mNonce).getDrawable());
        mPaymentMethodTitle.setText(paymentMethodNonce.getTypeLabel());
        mPaymentMethodDescription.setText(paymentMethodNonce.getDescription());
        mPaymentMethod.setVisibility(VISIBLE);

        mNonceString.setText(getString(R.string.nonce) + ": " + mNonce.getNonce());
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
        } else if (mNonce instanceof AndroidPayCardNonce) {
            AndroidPayCardNonce androidPayCardNonce = (AndroidPayCardNonce) mNonce;

            details = "Underlying Card Last Two: " + androidPayCardNonce.getLastTwo() + "\n";
            details += "Email: " + androidPayCardNonce.getEmail() + "\n";
            details += "Billing address: " + formatAddress(androidPayCardNonce.getBillingAddress()) + "\n";
            details += "Shipping address: " + formatAddress(androidPayCardNonce.getShippingAddress());
        } else if (mNonce instanceof VenmoAccountNonce) {
            VenmoAccountNonce venmoAccountNonce = (VenmoAccountNonce) mNonce;

            details = "Username: " + venmoAccountNonce.getUsername();
        }

        mNonceDetails.setText(details);
        mNonceDetails.setVisibility(VISIBLE);

        mDeviceData.setText("Device Data: " + deviceData);
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

    private Cart getAndroidPayCart() {
        return Cart.newBuilder()
                .setCurrencyCode(Settings.getAndroidPayCurrency(this))
                .setTotalPrice("1.00")
                .addLineItem(LineItem.newBuilder()
                        .setCurrencyCode("USD")
                        .setDescription("Description")
                        .setQuantity("1")
                        .setUnitPrice("1.00")
                        .setTotalPrice("1.00")
                        .build())
                .build();
    }

    private void safelyCloseLoadingView() {
        if (mLoading != null && mLoading.isShowing()) {
            mLoading.dismiss();
        }
    }
}
