package com.braintreepayments.demo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.BraintreePaymentActivity;
import com.braintreepayments.api.NewDropInActivity;
import com.braintreepayments.api.PayPal;
import com.braintreepayments.api.PaymentRequest;
import com.braintreepayments.api.ThreeDSecure;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.AndroidPayCardNonce;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.PayPalAccountNonce;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.models.PostalAddress;
import com.braintreepayments.api.models.VenmoAccountNonce;
import com.google.android.gms.identity.intents.model.UserAddress;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.LineItem;

import java.util.Collections;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends BaseActivity implements PaymentMethodNonceCreatedListener,
        BraintreeCancelListener, BraintreeErrorListener {

    static final String EXTRA_PAYMENT_REQUEST = "payment_request";
    static final String EXTRA_COLLECT_DEVICE_DATA = "collect_device_data";
    static final String EXTRA_ANDROID_PAY_CART = "android_pay_cart";

    private static final int DROP_IN_REQUEST = 100;
    private static final int PAYMENT_BUTTON_REQUEST = 200;

    private static final String KEY_NONCE = "nonce";

    private BraintreeFragment mBraintreeFragment;
    private PaymentMethodNonce mNonce;

    private ImageView mNonceIcon;
    private TextView mNonceString;
    private TextView mNonceDetails;
    private TextView mDeviceData;

    private Button mDropInButton;
    private Button mDropIn2Button;
    private Button mPaymentButtonButton;
    private Button mCreateTransactionButton;
    private ProgressDialog mLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mNonceIcon = (ImageView) findViewById(R.id.nonce_icon);
        mNonceString = (TextView) findViewById(R.id.nonce);
        mNonceDetails = (TextView) findViewById(R.id.nonce_details);
        mDeviceData = (TextView) findViewById(R.id.device_data);

        mDropInButton = (Button) findViewById(R.id.drop_in);
        mDropIn2Button = (Button) findViewById(R.id.drop_in_2);
        mPaymentButtonButton = (Button) findViewById(R.id.payment_button);
        mCreateTransactionButton = (Button) findViewById(R.id.create_transaction);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_NONCE)) {
                mNonce = savedInstanceState.getParcelable(KEY_NONCE);
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
        startActivityForResult(getPaymentRequest().getIntent(this), DROP_IN_REQUEST);
    }

    public void launchNewDropIn(View v) {
        Intent intent = getPaymentRequest().getIntent(this)
                .setClass(this, NewDropInActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivityForResult(intent, DROP_IN_REQUEST);
    }

    public void launchPaymentButton(View v) {
        Intent intent = new Intent(this, PaymentButtonActivity.class)
                .putExtra(EXTRA_COLLECT_DEVICE_DATA, Settings.shouldCollectDeviceData(this))
                .putExtra(EXTRA_ANDROID_PAY_CART, getAndroidPayCart())
                .putExtra(EXTRA_PAYMENT_REQUEST, getPaymentRequest());
        startActivityForResult(intent, PAYMENT_BUTTON_REQUEST);
    }

    private PaymentRequest getPaymentRequest() {
        PaymentRequest paymentRequest = new PaymentRequest()
                .clientToken(mAuthorization)
                .collectDeviceData(Settings.shouldCollectDeviceData(this))
                .androidPayCart(getAndroidPayCart())
                .androidPayShippingAddressRequired(Settings.isAndroidPayShippingAddressRequired(this))
                .androidPayPhoneNumberRequired(Settings.isAndroidPayPhoneNumberRequired(this))
                .androidPayAllowedCountriesForShipping(Settings.getAndroidPayAllowedCountriesForShipping(this))
                .primaryDescription(getString(R.string.cart))
                .secondaryDescription("1 Item")
                .amount("$1.00")
                .submitButtonText(getString(R.string.buy))
                .defaultFirst(true);

        if (Settings.isPayPalAddressScopeRequested(this)) {
            paymentRequest.paypalAdditionalScopes(Collections.singletonList(PayPal.SCOPE_ADDRESS));
        }

        return paymentRequest;
    }

    public void createTransaction(View v) {
        Intent intent = new Intent(this, CreateTransactionActivity.class)
                .putExtra(CreateTransactionActivity.EXTRA_PAYMENT_METHOD_NONCE, mNonce);
        startActivity(intent);

        mCreateTransactionButton.setEnabled(false);
        clearNonce();
    }

    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
        super.onPaymentMethodNonceCreated(paymentMethodNonce);

        displayResult(new Intent()
                .putExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE, paymentMethodNonce));
        safelyCloseLoadingView();
    }

    @Override
    public void onCancel(int requestCode) {
        super.onCancel(requestCode);

        safelyCloseLoadingView();
    }

    @Override
    public void onError(Exception error) {
        super.onError(error);

        safelyCloseLoadingView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        safelyCloseLoadingView();

        if (resultCode == RESULT_OK) {
            displayResult(data);
            if (mNonce instanceof CardNonce && Settings.isThreeDSecureEnabled(this)) {
                mLoading = ProgressDialog.show(this, getString(R.string.loading),
                        getString(R.string.loading), true, false);
                ThreeDSecure.performVerification(mBraintreeFragment, mNonce.getNonce(), "1");
            } else {
                mCreateTransactionButton.setEnabled(true);
            }
        } else if (resultCode != RESULT_CANCELED) {
            safelyCloseLoadingView();
            showDialog(data.getStringExtra(BraintreePaymentActivity.EXTRA_ERROR_MESSAGE));
        }
    }

    @Override
    protected void reset() {
        enableButtons(false);
        mCreateTransactionButton.setEnabled(false);

        clearNonce();
    }

    @Override
    protected void onAuthorizationFetched() {
        try {
            mBraintreeFragment = BraintreeFragment.newInstance(this, mAuthorization);
            enableButtons(true);
        } catch (InvalidArgumentException e) {
            showDialog(e.getMessage());
        }
    }

    private void displayResult(Intent data) {
        mNonce = data.getParcelableExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE);

        mNonceIcon.setImageResource(PaymentMethodType.forType(mNonce).getDrawable());
        mNonceIcon.setVisibility(VISIBLE);

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

        mDeviceData.setText("Device Data: " +
                data.getStringExtra(BraintreePaymentActivity.EXTRA_DEVICE_DATA));
        mDeviceData.setVisibility(VISIBLE);

        mCreateTransactionButton.setEnabled(true);
    }

    private void clearNonce() {
        mNonceIcon.setVisibility(GONE);
        mNonceString.setVisibility(GONE);
        mNonceDetails.setVisibility(GONE);
        mDeviceData.setVisibility(GONE);
        mCreateTransactionButton.setEnabled(false);
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

    private void enableButtons(boolean enable) {
        mDropInButton.setEnabled(enable);
        mDropIn2Button.setEnabled(enable);
        mPaymentButtonButton.setEnabled(enable);
    }

    private void safelyCloseLoadingView() {
        if (mLoading != null && mLoading.isShowing()) {
            mLoading.dismiss();
        }
    }
}
