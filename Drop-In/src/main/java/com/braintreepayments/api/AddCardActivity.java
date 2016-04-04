package com.braintreepayments.api;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.ConfigurationListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.CardBuilder;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.cardform.OnCardFormSubmitListener;
import com.braintreepayments.cardform.view.CardForm;

public class AddCardActivity extends Activity implements ConfigurationListener,
        OnCardFormSubmitListener, PaymentMethodNonceCreatedListener {

    private CardForm mCardForm;
    private BraintreeFragment mBraintreeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_add_card_activity);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mCardForm = (CardForm) findViewById(R.id.bt_card_form);

        try {
            mBraintreeFragment = BraintreeFragment.newInstance(this,
                    ((PaymentRequest) getIntent().getParcelableExtra(PaymentRequest.EXTRA_CHECKOUT_REQUEST)).getAuthorization());
        } catch (InvalidArgumentException e) {
            setResult(RESULT_FIRST_USER);
            finish();
            return;
        }

        mBraintreeFragment.addListener(this);
    }

    @Override
    public void onConfigurationFetched(Configuration configuration) {
        mCardForm.setRequiredFields(this, true, true, configuration.isCvvChallengePresent(),
                configuration.isPostalCodeChallengePresent(), getString(R.string.bt_add_card));
        mCardForm.setOnCardFormSubmitListener(this);
    }

    public void addCard(View v) {
        onCardFormSubmit();
    }

    @Override
    public void onCardFormSubmit() {
        if (!mCardForm.isValid()) {
            mCardForm.validate();
            return;
        }

        CardBuilder cardBuilder = new CardBuilder()
                .cardNumber(mCardForm.getCardNumber())
                .expirationMonth(mCardForm.getExpirationMonth())
                .expirationYear(mCardForm.getExpirationYear());

        if (mBraintreeFragment.getConfiguration().isCvvChallengePresent()) {
            cardBuilder.cvv(mCardForm.getCvv());
        }

        if (mBraintreeFragment.getConfiguration().isPostalCodeChallengePresent()) {
            cardBuilder.postalCode(mCardForm.getPostalCode());
        }

        Card.tokenize(mBraintreeFragment, cardBuilder);
    }

    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
        setResult(Activity.RESULT_OK,
                new Intent().putExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE, paymentMethodNonce));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
