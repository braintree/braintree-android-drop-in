package com.braintreepayments.api;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.interfaces.AddPaymentUpdateListener;
import com.braintreepayments.api.dropin.view.AddCardView;
import com.braintreepayments.api.dropin.view.EditCardView;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.CardBuilder;
import com.braintreepayments.api.models.PaymentMethodNonce;

public class AddCardActivity extends Activity implements AddPaymentUpdateListener,
        PaymentMethodNonceCreatedListener, BraintreeErrorListener {

    private AddCardView mAddCardView;
    private EditCardView mEditCardView;

    private BraintreeFragment mBraintreeFragment;
    private final CardBuilder mCardBuilder = new CardBuilder();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_add_payment_activity);
        mAddCardView = (AddCardView)findViewById(R.id.add_card_view);
        mEditCardView = (EditCardView)findViewById(R.id.edit_card_view);

        mAddCardView.setAddPaymentUpdatedListener(this);
        mEditCardView.setAddPaymentUpdatedListener(this);

        PaymentRequest paymentRequest = getIntent().getParcelableExtra(PaymentRequest.EXTRA_CHECKOUT_REQUEST);

//        String authorization = getIntent().getStringExtra(EXTRA_AUTHORIZATION_TOKEN);
        try {
            mBraintreeFragment = BraintreeFragment.newInstance(this, paymentRequest.getAuthorization());
        } catch (InvalidArgumentException e) {
            // TODO alert the merchant their authorization may be incorrect.
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onPaymentUpdated(View v) {
        if (v.getId() == mAddCardView.getId()) {
            if (mAddCardView.getNumber() != null) {
                Log.d("CardNumber", mAddCardView.getNumber());
                mAddCardView.setVisibility(View.GONE);
                mEditCardView.setVisibility(View.VISIBLE);
                mCardBuilder.cardNumber(mAddCardView.getNumber());
                // Switch views
            }
        } else if (v.getId() == mEditCardView.getId()) {
            mEditCardView.setVisibility(View.GONE);

            // TODO real values
            mCardBuilder.expirationDate("expiration date");
            mCardBuilder.cvv("cvv");
            createCard();

            // Possibly show enrollment
        }
    }

    private void createCard() {
        Card.tokenize(mBraintreeFragment, mCardBuilder);
    }

    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethod) {
        Intent result = new Intent();
        result.putExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE, paymentMethod);
        setResult(Activity.RESULT_OK, result);
    }

    @Override
    public void onError(Exception e) {
        throw new RuntimeException(e);
    }
}
