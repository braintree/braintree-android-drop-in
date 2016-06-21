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
import com.braintreepayments.api.dropin.view.EnrollmentCardView;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.CardBuilder;
import com.braintreepayments.api.models.PaymentMethodNonce;

public class AddCardActivity extends Activity implements AddPaymentUpdateListener,
        PaymentMethodNonceCreatedListener, BraintreeErrorListener {

    private AddCardView mAddCardView;
    private EditCardView mEditCardView;
    private EnrollmentCardView mEnrollmentCardView;

    private BraintreeFragment mBraintreeFragment;
    private final CardBuilder mCardBuilder = new CardBuilder();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_add_payment_activity);
        mAddCardView = (AddCardView)findViewById(R.id.add_card_view);
        mEditCardView = (EditCardView)findViewById(R.id.edit_card_view);
        mEnrollmentCardView = (EnrollmentCardView)findViewById(R.id.enrollment_card_view);

        mAddCardView.setAddPaymentUpdatedListener(this);
        mEditCardView.setAddPaymentUpdatedListener(this);
        mEnrollmentCardView.setAddPaymentUpdatedListener(this);

        PaymentRequest paymentRequest = getIntent().getParcelableExtra(PaymentRequest.EXTRA_CHECKOUT_REQUEST);

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
                mCardBuilder.cardNumber(mAddCardView.getNumber());
                mEditCardView.setCardNumber(mAddCardView.getNumber());
                mEditCardView.setVisibility(View.VISIBLE);
                // Switch views
            }
        } else if (v.getId() == mEditCardView.getId()) {
            mEditCardView.setVisibility(View.GONE);

            mCardBuilder.expirationDate(mEditCardView.getExpirationDate());
            mCardBuilder.cvv(mEditCardView.getSecurityCode());
            createCard();

            // TODO Possibly show enrollment
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
        finish();
    }

    @Override
    public void onError(Exception e) {
        throw new RuntimeException(e);
    }
}
