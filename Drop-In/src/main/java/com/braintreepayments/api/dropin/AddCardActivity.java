package com.braintreepayments.api.dropin;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ViewSwitcher;

import com.braintreepayments.api.Card;
import com.braintreepayments.api.ThreeDSecure;
import com.braintreepayments.api.UnionPay;
import com.braintreepayments.api.dropin.interfaces.AddPaymentUpdateListener;
import com.braintreepayments.api.dropin.view.AddCardView;
import com.braintreepayments.api.dropin.view.EditCardView;
import com.braintreepayments.api.dropin.view.EnrollmentCardView;
import com.braintreepayments.api.exceptions.AuthenticationException;
import com.braintreepayments.api.exceptions.AuthorizationException;
import com.braintreepayments.api.exceptions.ConfigurationException;
import com.braintreepayments.api.exceptions.DownForMaintenanceException;
import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.exceptions.ServerException;
import com.braintreepayments.api.exceptions.UnexpectedException;
import com.braintreepayments.api.exceptions.UpgradeRequiredException;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.ConfigurationListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.interfaces.UnionPayListener;
import com.braintreepayments.api.models.BraintreeRequestCodes;
import com.braintreepayments.api.models.CardBuilder;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.models.UnionPayCapabilities;
import com.braintreepayments.api.models.UnionPayCardBuilder;
import com.braintreepayments.cardform.view.CardForm;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class AddCardActivity extends BaseActivity implements ConfigurationListener,
        AddPaymentUpdateListener, PaymentMethodNonceCreatedListener, BraintreeErrorListener,
        BraintreeCancelListener, UnionPayListener {

    private static final String EXTRA_STATE = "com.braintreepayments.api.EXTRA_STATE";
    private static final String EXTRA_ENROLLMENT_ID = "com.braintreepayments.api.EXTRA_ENROLLMENT_ID";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            LOADING,
            CARD_ENTRY,
            DETAILS_ENTRY,
            ENROLLMENT_ENTRY
    })
    private @interface State {}
    private static final int LOADING = 1;
    private static final int CARD_ENTRY = 2;
    private static final int DETAILS_ENTRY = 3;
    private static final int ENROLLMENT_ENTRY = 4;

    private ActionBar mActionBar;
    private ViewSwitcher mViewSwitcher;
    private AddCardView mAddCardView;
    private EditCardView mEditCardView;
    private EnrollmentCardView mEnrollmentCardView;

    private boolean mUnionPayCard;
    private boolean mUnionPayDebitCard;

    private String mEnrollmentId;

    @State
    private int mState = CARD_ENTRY;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_add_card_activity);

        mViewSwitcher = findViewById(R.id.bt_loading_view_switcher);
        mAddCardView = findViewById(R.id.bt_add_card_view);
        mEditCardView = findViewById(R.id.bt_edit_card_view);
        mEnrollmentCardView = findViewById(R.id.bt_enrollment_card_view);
        mEnrollmentCardView.setup(this);

        setSupportActionBar((Toolbar) findViewById(R.id.bt_toolbar));
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mAddCardView.setAddPaymentUpdatedListener(this);
        mEditCardView.setAddPaymentUpdatedListener(this);
        mEnrollmentCardView.setAddPaymentUpdatedListener(this);

        if (savedInstanceState != null) {
            @State int state = savedInstanceState.getInt(EXTRA_STATE);
            mState = state;
            mEnrollmentId = savedInstanceState.getString(EXTRA_ENROLLMENT_ID);
        } else {
            mState = CARD_ENTRY;
        }

        mAddCardView.getCardForm().maskCardNumber(mDropInRequest.shouldMaskCardNumber());
        mEditCardView.getCardForm().maskCardNumber(mDropInRequest.shouldMaskCardNumber());
        mEditCardView.getCardForm().maskCvv(mDropInRequest.shouldMaskSecurityCode());

        enterState(LOADING);

        try {
            mBraintreeFragment = getBraintreeFragment();
        } catch (InvalidArgumentException e) {
            finish(e);
            return;
        }

        mBraintreeFragment.sendAnalyticsEvent("card.selected");
    }

    @Override
    public void onConfigurationFetched(Configuration configuration) {
        mConfiguration = configuration;

        mAddCardView.setup(this, configuration, mClientTokenPresent);
        mEditCardView.setup(this, configuration, mDropInRequest);

        setState(LOADING, mState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_STATE, mState);
        outState.putString(EXTRA_ENROLLMENT_ID, mEnrollmentId);
    }

    @Override
    public void onPaymentUpdated(View v) {
        setState(mState, determineNextState(v));
    }

    private void setState(int currentState, int nextState) {
        if (currentState == nextState) {
            return;
        }

        leaveState(currentState);
        enterState(nextState);

        mState = nextState;
    }

    private void leaveState(int state) {
        switch (state) {
            case LOADING:
                mViewSwitcher.setDisplayedChild(1);
                break;
            case CARD_ENTRY:
                mAddCardView.setVisibility(GONE);
                break;
            case DETAILS_ENTRY:
                mEditCardView.setVisibility(GONE);
                break;
            case ENROLLMENT_ENTRY:
                mEnrollmentCardView.setVisibility(GONE);
                break;
        }
    }

    private void enterState(int state) {
        switch(state) {
            case LOADING:
                mActionBar.setTitle(R.string.bt_card_details);
                mViewSwitcher.setDisplayedChild(0);
                break;
            case CARD_ENTRY:
                mActionBar.setTitle(R.string.bt_card_details);
                mAddCardView.setVisibility(VISIBLE);
                break;
            case DETAILS_ENTRY:
                mActionBar.setTitle(R.string.bt_card_details);
                mEditCardView.setCardNumber(mAddCardView.getCardForm().getCardNumber());
                mEditCardView.useUnionPay(this, mUnionPayCard, mUnionPayDebitCard);
                mEditCardView.setVisibility(VISIBLE);
                break;
            case ENROLLMENT_ENTRY:
                mActionBar.setTitle(R.string.bt_confirm_enrollment);
                mEnrollmentCardView.setPhoneNumber(
                        PhoneNumberUtils.formatNumber(mEditCardView.getCardForm().getCountryCode() +
                                mEditCardView.getCardForm().getMobileNumber()));
                mEnrollmentCardView.setVisibility(VISIBLE);
                break;
        }
    }

    @Override
    public void onBackRequested(View v) {
        if (v.getId() == mEditCardView.getId()) {
            setState(DETAILS_ENTRY, CARD_ENTRY);
        } else if (v.getId() == mEnrollmentCardView.getId()) {
            setState(ENROLLMENT_ENTRY, DETAILS_ENTRY);
        }
    }

    @State
    private int determineNextState(View v) {
        int nextState = mState;
        if (v.getId() == mAddCardView.getId() && !TextUtils.isEmpty(mAddCardView.getCardForm().getCardNumber())) {
            if (!mConfiguration.getUnionPay().isEnabled() || !mClientTokenPresent) {
                mEditCardView.useUnionPay(this, false, false);
                nextState = DETAILS_ENTRY;
            } else {
                UnionPay.fetchCapabilities(mBraintreeFragment, mAddCardView.getCardForm().getCardNumber());
            }
        } else if (v.getId() == mEditCardView.getId()) {
            if (mUnionPayCard) {
                if (TextUtils.isEmpty(mEnrollmentId)) {
                    enrollUnionPayCard();
                } else {
                    nextState = ENROLLMENT_ENTRY;
                }
            } else {
                nextState = mState;
                createCard();
            }
        } else if (v.getId() == mEnrollmentCardView.getId()) {
            nextState = mState;
            if (mEnrollmentCardView.hasFailedEnrollment()) {
                enrollUnionPayCard();
            } else {
                createCard();
            }
        }

        return nextState;
    }

    private void enrollUnionPayCard() {
        UnionPayCardBuilder unionPayCardBuilder = new UnionPayCardBuilder()
                .cardNumber(mEditCardView.getCardForm().getCardNumber())
                .expirationMonth(mEditCardView.getCardForm().getExpirationMonth())
                .expirationYear(mEditCardView.getCardForm().getExpirationYear())
                .cvv(mEditCardView.getCardForm().getCvv())
                .postalCode(mEditCardView.getCardForm().getPostalCode())
                .mobileCountryCode(mEditCardView.getCardForm().getCountryCode())
                .mobilePhoneNumber(mEditCardView.getCardForm().getMobileNumber());

        UnionPay.enroll(mBraintreeFragment, unionPayCardBuilder);
    }

    protected void createCard() {
        CardForm cardForm = mEditCardView.getCardForm();

        if (mUnionPayCard) {
            UnionPayCardBuilder unionPayCardBuilder = new UnionPayCardBuilder()
                    .cardholderName(cardForm.getCardholderName())
                    .cardNumber(cardForm.getCardNumber())
                    .expirationMonth(cardForm.getExpirationMonth())
                    .expirationYear(cardForm.getExpirationYear())
                    .cvv(cardForm.getCvv())
                    .postalCode(cardForm.getPostalCode())
                    .mobileCountryCode(cardForm.getCountryCode())
                    .mobilePhoneNumber(cardForm.getMobileNumber())
                    .enrollmentId(mEnrollmentId)
                    .smsCode(mEnrollmentCardView.getSmsCode());

            UnionPay.tokenize(mBraintreeFragment, unionPayCardBuilder);
        } else {
            CardBuilder cardBuilder = new CardBuilder()
                    .cardholderName(cardForm.getCardholderName())
                    .cardNumber(cardForm.getCardNumber())
                    .expirationMonth(cardForm.getExpirationMonth())
                    .expirationYear(cardForm.getExpirationYear())
                    .cvv(cardForm.getCvv())
                    .postalCode(cardForm.getPostalCode())
                    .validate(mClientTokenPresent);

            if (shouldRequestThreeDSecureVerification()) {
                ThreeDSecure.performVerification(mBraintreeFragment, cardBuilder,
                        mDropInRequest.getAmount());
            } else {
                Card.tokenize(mBraintreeFragment, cardBuilder);
            }
        }
    }

    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethod) {
        mBraintreeFragment.sendAnalyticsEvent("sdk.exit.success");
        finish(paymentMethod, null);
    }

    @Override
    public void onCapabilitiesFetched(UnionPayCapabilities capabilities) {
        mUnionPayCard = capabilities.isUnionPay();
        mUnionPayDebitCard = capabilities.isDebit();

        if (mUnionPayCard && !capabilities.isSupported()) {
            mAddCardView.showCardNotSupportedError();
        } else {
            setState(mState, DETAILS_ENTRY);
        }
    }

    @Override
    public void onSmsCodeSent(String enrollmentId, boolean smsRequired) {
        mEnrollmentId = enrollmentId;

        if (smsRequired && mState != ENROLLMENT_ENTRY) {
            onPaymentUpdated(mEditCardView);
        } else {
            createCard();
        }
    }

    @Override
    public void onCancel(int requestCode) {
        if (requestCode == BraintreeRequestCodes.THREE_D_SECURE) {
            mEditCardView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onError(Exception error) {
        if (error instanceof ErrorWithResponse) {
            ErrorWithResponse errorResponse = (ErrorWithResponse) error;

            if (mEnrollmentCardView.isEnrollmentError(errorResponse)) {
                setState(mState, ENROLLMENT_ENTRY);
                mEnrollmentCardView.setErrors((ErrorWithResponse) error);
            } else {
                mEditCardView.setErrors((ErrorWithResponse) error);

                if (mAddCardView.isCardNumberError(errorResponse)) {
                    mAddCardView.setErrors((ErrorWithResponse) error);
                    setState(mState, CARD_ENTRY);
                } else {
                    setState(mState, DETAILS_ENTRY);
                }
            }
        } else {
            if (error instanceof AuthenticationException || error instanceof AuthorizationException ||
                    error instanceof UpgradeRequiredException) {
                mBraintreeFragment.sendAnalyticsEvent("sdk.exit.developer-error");
            } else if (error instanceof ConfigurationException) {
                mBraintreeFragment.sendAnalyticsEvent("sdk.exit.configuration-exception");
            } else if (error instanceof ServerException || error instanceof UnexpectedException) {
                mBraintreeFragment.sendAnalyticsEvent("sdk.exit.server-error");
            } else if (error instanceof DownForMaintenanceException) {
                mBraintreeFragment.sendAnalyticsEvent("sdk.exit.server-unavailable");
            }

            finish(error);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        if (mAddCardView.getCardForm().isCardScanningAvailable()) {
            getMenuInflater().inflate(R.menu.bt_card_io, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.bt_card_io_button) {
            mAddCardView.getCardForm().scanCard(this);
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
