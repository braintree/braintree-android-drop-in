package com.braintreepayments.api;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.interfaces.AddPaymentUpdateListener;
import com.braintreepayments.api.dropin.view.AddCardView;
import com.braintreepayments.api.dropin.view.EditCardView;
import com.braintreepayments.api.dropin.view.EnrollmentCardView;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.interfaces.UnionPayListener;
import com.braintreepayments.api.models.CardBuilder;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.models.UnionPayCapabilities;
import com.braintreepayments.api.models.UnionPayCardBuilder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AddCardActivity extends AppCompatActivity implements AddPaymentUpdateListener,
        PaymentMethodNonceCreatedListener, BraintreeErrorListener, UnionPayListener {

    private static final String EXTRA_STATE = "com.braintreepayments.api.EXTRA_STATE";
    private static final String EXTRA_ENROLLMENT_ID = "com.braintreepayments.api.EXTRA_ENROLLMENT_ID";
    private static final String EXTRA_CAPABILITIES = "com.braintreepayments.api.EXTRA_CAPABILITIES";

    private UnionPayCapabilities mCapabilities;
    private String mEnrollmentId;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            CARD_ENTRY,
            DETAILS_ENTRY,
            ENROLLMENT_ENTRY,
            SUBMIT
    })
    private @interface State {}
    public static final int CARD_ENTRY = 1;
    public static final int DETAILS_ENTRY = 2;
    public static final int ENROLLMENT_ENTRY = 3;
    public static final int SUBMIT = 4;

    private Toolbar mToolbar;
    private AddCardView mAddCardView;
    private EditCardView mEditCardView;
    private EnrollmentCardView mEnrollmentCardView;

    private BraintreeFragment mBraintreeFragment;

    @State
    private int mState = CARD_ENTRY;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_add_card_activity);
        mToolbar = (Toolbar) findViewById(R.id.toobar);
        mAddCardView = (AddCardView)findViewById(R.id.add_card_view);
        mEditCardView = (EditCardView)findViewById(R.id.edit_card_view);
        mEnrollmentCardView = (EnrollmentCardView)findViewById(R.id.enrollment_card_view);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        
        if (savedInstanceState != null) {
            //TODO is there a better way to respect this State interface?
            @State int state = savedInstanceState.getInt(EXTRA_STATE);
            mState = state;
            mEnrollmentId = savedInstanceState.getString(EXTRA_ENROLLMENT_ID);
            mCapabilities = savedInstanceState.getParcelable(EXTRA_CAPABILITIES);
        }
        enterState(mState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_STATE, mState);
        outState.putString(EXTRA_ENROLLMENT_ID, mEnrollmentId);
        outState.putParcelable(EXTRA_CAPABILITIES, mCapabilities);
    }

    @Override
    public void onPaymentUpdated(final View v) {
        int lastState = mState;
        int nextState = determineNextState(v);
        if (nextState == lastState) {
            return;
        }
        leaveState(lastState);
        enterState(nextState);
    }

    private void leaveState(int state) {
        switch (state) {
            case CARD_ENTRY:
                mAddCardView.setVisibility(View.GONE);
                break;
            case DETAILS_ENTRY:
                mEditCardView.setVisibility(View.GONE);
                break;
            case ENROLLMENT_ENTRY:
                mEnrollmentCardView.setVisibility(View.GONE);
                break;
        }
    }

    private void enterState(int state) {
        switch(state) {
            case CARD_ENTRY:
                getSupportActionBar().setTitle("Enter Card Details");
                mAddCardView.setVisibility(View.VISIBLE);
                break;
            case DETAILS_ENTRY:
                getSupportActionBar().setTitle("Card Details");
                mEditCardView.setCardNumber(mAddCardView.getNumber());
                mEditCardView.useUnionPay(isCardUnionPay());
                mEditCardView.setVisibility(View.VISIBLE);
                break;
            case ENROLLMENT_ENTRY:
                getSupportActionBar().setTitle("Confirm Enrollment");
                mEnrollmentCardView.setVisibility(View.VISIBLE);
                break;
            case SUBMIT:
                createCard();
                break;
        }
        mState = state;
    }

    @Override
    public void onBackRequested(View v) {
        if (v.getId() == mEditCardView.getId()) {
            leaveState(DETAILS_ENTRY);
            enterState(CARD_ENTRY);
        } else if (v.getId() == mEnrollmentCardView.getId()) {
            leaveState(ENROLLMENT_ENTRY);
            enterState(DETAILS_ENTRY);
        }
    }

    @State
    private int determineNextState(View v) {
        int nextState = mState;
        boolean unionPayEnabled = mBraintreeFragment.getConfiguration().getUnionPay().isEnabled();
        if (v.getId() == mAddCardView.getId() && !TextUtils.isEmpty(mAddCardView.getNumber())) {
            if (!unionPayEnabled) {
                mEditCardView.useUnionPay(false);
            } else if (unionPayEnabled && mCapabilities == null) {
                UnionPay.fetchCapabilities(mBraintreeFragment, mAddCardView.getNumber());
            } else {
                nextState = DETAILS_ENTRY;
            }
        } else if (v.getId() == mEditCardView.getId()) {
            if (isCardUnionPay()) {
                if (TextUtils.isEmpty(mEnrollmentId)) {
                    UnionPayCardBuilder unionPayCardBuilder = new UnionPayCardBuilder()
                            .cardNumber(mAddCardView.getNumber())
                            .mobileCountryCode(mEditCardView.getMobileCountryCode())
                            .mobilePhoneNumber(mEditCardView.getPhoneNumber())
                            .expirationDate(mEditCardView.getExpirationDate())
                            .cvv(mEditCardView.getCvv());
                    UnionPay.enroll(mBraintreeFragment, unionPayCardBuilder);
                } else {
                    nextState = ENROLLMENT_ENTRY;
                }
            } else {
                nextState = SUBMIT;
            }
        } else if (v.getId() == mEnrollmentCardView.getId()) {
            nextState = SUBMIT;
        }

        return nextState;
    }

    private void createCard() {
        if (isCardUnionPay()) {
            UnionPayCardBuilder unionPayCardBuilder = new UnionPayCardBuilder()
                    .cardNumber(mAddCardView.getNumber())
                    .expirationDate(mEditCardView.getExpirationDate())
                    .cvv(mEditCardView.getCvv())
                    .mobileCountryCode(mEditCardView.getMobileCountryCode())
                    .mobilePhoneNumber(mEditCardView.getPhoneNumber())
                    .enrollmentId(mEnrollmentId)
                    .smsCode(mEnrollmentCardView.getSmsCode());
            UnionPay.tokenize(mBraintreeFragment, unionPayCardBuilder);
        } else {
            CardBuilder cardBuilder = new CardBuilder()
                    .cardNumber(mAddCardView.getNumber())
                    .expirationDate(mEditCardView.getExpirationDate())
                    .cvv(mEditCardView.getCvv());
            Card.tokenize(mBraintreeFragment, cardBuilder);

        }
    }

    private boolean isCardUnionPay() {
        return mBraintreeFragment.getConfiguration().getUnionPay().isEnabled() && mCapabilities != null &&
                mCapabilities.isUnionPay();
    }

    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethod) {
        Intent result = new Intent();
        result.putExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE, paymentMethod);
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    @Override
    public void onCapabilitiesFetched(UnionPayCapabilities capabilities) {
        mCapabilities = capabilities;
        mEditCardView.useUnionPay(capabilities.isUnionPayEnrollmentRequired());
        onPaymentUpdated(mAddCardView);
    }

    @Override
    public void onSmsCodeSent(String enrollmentId) {
        mEnrollmentId = enrollmentId;
        onPaymentUpdated(mEditCardView);
    }

    @Override
    public void onError(Exception e) {
        throw new RuntimeException(e);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
