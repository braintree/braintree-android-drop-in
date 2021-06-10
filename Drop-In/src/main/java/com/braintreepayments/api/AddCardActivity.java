package com.braintreepayments.api;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ViewSwitcher;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.cardform.view.CardForm;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class AddCardActivity extends BaseActivity implements AddPaymentUpdateListener {

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

    private Configuration mConfiguration;

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

        getDropInClient().sendAnalyticsEvent("card.selected");

        getDropInClient().getConfiguration(new ConfigurationCallback() {
            @Override
            public void onResult(@Nullable Configuration configuration, @Nullable Exception error) {
                if (error != null) {
                    onError(error);
                    return;
                }
                onConfigurationFetched(configuration);
            }
        });
    }

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
            if (!mConfiguration.isUnionPayEnabled() || !mClientTokenPresent) {
                mEditCardView.useUnionPay(this, false, false);
                nextState = DETAILS_ENTRY;
            } else {
                getDropInClient().fetchUnionPayCapabilities(mAddCardView.getCardForm().getCardNumber(), new UnionPayFetchCapabilitiesCallback() {
                    @Override
                    public void onResult(@Nullable UnionPayCapabilities capabilities, @Nullable Exception error) {
                        if (error != null) {
                            onError(error);
                            return;
                        }
                        onCapabilitiesFetched(capabilities);
                    }
                });
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
        UnionPayCard unionPayCard = new UnionPayCard();
        unionPayCard.setNumber(mEditCardView.getCardForm().getCardNumber());
        unionPayCard.setExpirationMonth(mEditCardView.getCardForm().getExpirationMonth());
        unionPayCard.setExpirationYear(mEditCardView.getCardForm().getExpirationYear());
        unionPayCard.setCvv(mEditCardView.getCardForm().getCvv());
        unionPayCard.setPostalCode(mEditCardView.getCardForm().getPostalCode());
        unionPayCard.setMobileCountryCode(mEditCardView.getCardForm().getCountryCode());
        unionPayCard.setMobilePhoneNumber(mEditCardView.getCardForm().getMobileNumber());

        getDropInClient().enrollUnionPay(unionPayCard, new UnionPayEnrollCallback() {
            @Override
            public void onResult(@Nullable UnionPayEnrollment enrollment, @Nullable Exception error) {
                if (error != null) {
                    onError(error);
                    return;
                }
               onSmsCodeSent(enrollment.getId(), enrollment.isSmsCodeRequired());
            }
        });
    }

    protected void createCard() {
        CardForm cardForm = mEditCardView.getCardForm();

        if (mUnionPayCard) {
            UnionPayCard unionPayCard = new UnionPayCard();
            unionPayCard.setCardholderName(cardForm.getCardholderName());
            unionPayCard.setNumber(cardForm.getCardNumber());
            unionPayCard.setExpirationMonth(cardForm.getExpirationMonth());
            unionPayCard.setExpirationYear(cardForm.getExpirationYear());
            unionPayCard.setCvv(cardForm.getCvv());
            unionPayCard.setPostalCode(cardForm.getPostalCode());
            unionPayCard.setMobileCountryCode(cardForm.getCountryCode());
            unionPayCard.setMobilePhoneNumber(cardForm.getMobileNumber());
            unionPayCard.setEnrollmentId(mEnrollmentId);
            unionPayCard.setSmsCode(mEnrollmentCardView.getSmsCode());

            getDropInClient().tokenizeUnionPay(unionPayCard, new UnionPayTokenizeCallback() {
                @Override
                public void onResult(@Nullable CardNonce cardNonce, @Nullable Exception error) {
                    if (error != null) {
                        onError(error);
                        return;
                    }
                    onPaymentMethodNonceCreated(cardNonce);
                }
            });
        } else {
            boolean shouldVault = mClientTokenPresent && cardForm.isSaveCardCheckBoxChecked();

            final Card card = new Card();
            card.setCardholderName(cardForm.getCardholderName());
            card.setNumber(cardForm.getCardNumber());
            card.setExpirationMonth(cardForm.getExpirationMonth());
            card.setExpirationYear(cardForm.getExpirationYear());
            card.setCvv(cardForm.getCvv());
            card.setPostalCode(cardForm.getPostalCode());
            card.setShouldValidate(shouldVault);

            getDropInClient().tokenizeCard(this, card, new CardTokenizeCallback() {
                @Override
                public void onResult(@Nullable CardNonce cardNonce, @Nullable Exception error) {
                    if (error != null) {
                        onError(error);
                        return;
                    }
                    onPaymentMethodNonceCreated(cardNonce);
                }
            });
        }
    }

    public void onPaymentMethodNonceCreated(final PaymentMethodNonce paymentMethod) {
        getDropInClient().shouldRequestThreeDSecureVerification(paymentMethod, new ShouldRequestThreeDSecureVerification() {
            @Override
            public void onResult(boolean shouldRequestThreeDSecureVerification) {
                if (shouldRequestThreeDSecureVerification) {
                    getDropInClient().performThreeDSecureVerification(AddCardActivity.this, paymentMethod, new ThreeDSecureResultCallback() {
                        @Override
                        public void onResult(@Nullable ThreeDSecureResult threeDSecureResult, @Nullable Exception error) {
                            if (error != null) {
                                onError(error);
                                return;
                            }
                            getDropInClient().sendAnalyticsEvent("sdk.exit.success");
                            finish(threeDSecureResult.getTokenizedCard(), null);
                        }
                    });
                } else {
                    getDropInClient().sendAnalyticsEvent("sdk.exit.success");
                    finish(paymentMethod, null);
                }
            }
        });
    }

    public void onCapabilitiesFetched(UnionPayCapabilities capabilities) {
        mUnionPayCard = capabilities.isUnionPay();
        mUnionPayDebitCard = capabilities.isDebit();

        if (mUnionPayCard && !capabilities.isSupported()) {
            mAddCardView.showCardNotSupportedError();
        } else {
            setState(mState, DETAILS_ENTRY);
        }
    }

    public void onSmsCodeSent(String enrollmentId, boolean smsRequired) {
        mEnrollmentId = enrollmentId;

        if (smsRequired && mState != ENROLLMENT_ENTRY) {
            onPaymentUpdated(mEditCardView);
        } else {
            createCard();
        }
    }

    public void onCancel(int requestCode) {
        if (requestCode == BraintreeRequestCodes.THREE_D_SECURE) {
            mEditCardView.setVisibility(View.VISIBLE);
        }
    }

    public void onError(Exception error) {
        if (error instanceof ErrorWithResponse) {
            ErrorWithResponse errorResponse = (ErrorWithResponse) error;

            if (mEnrollmentCardView.isEnrollmentError(errorResponse)) {
                setState(mState, ENROLLMENT_ENTRY);
                mEnrollmentCardView.setErrors(errorResponse);
            } else if (mAddCardView.isCardNumberError(errorResponse)) {
                mAddCardView.setErrors(errorResponse);
                mEditCardView.setErrors(errorResponse);
                setState(mState, CARD_ENTRY);
            } else if (mEditCardView.isEditCardError(errorResponse)) {
                mEditCardView.setErrors(errorResponse);
                setState(mState, DETAILS_ENTRY);
            } else {
                finish(error);
            }
        } else {
            if (error instanceof AuthenticationException || error instanceof AuthorizationException ||
                    error instanceof UpgradeRequiredException) {
                getDropInClient().sendAnalyticsEvent("sdk.exit.developer-error");
            } else if (error instanceof ConfigurationException) {
                getDropInClient().sendAnalyticsEvent("sdk.exit.configuration-exception");
            } else if (error instanceof ServerException || error instanceof UnexpectedException) {
                getDropInClient().sendAnalyticsEvent("sdk.exit.server-error");
            } else if (error instanceof ServiceUnavailableException) {
                getDropInClient().sendAnalyticsEvent("sdk.exit.server-unavailable");
            }
            finish(error);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == BraintreeRequestCodes.THREE_D_SECURE) {
            getDropInClient().handleThreeDSecureActivityResult(this, resultCode, data, new DropInResultCallback() {
                @Override
                public void onResult(@Nullable DropInResult dropInResult, @Nullable Exception error) {
                    if (dropInResult != null) {
                        finish(dropInResult.getPaymentMethodNonce(), dropInResult.getDeviceData());
                    } else {
                        // user canceled; show edit card button
                        // TODO: create user canceled error type to differentiate user cancellations from other errors
                        mEditCardView.setVisibility(View.VISIBLE);
                    }
                }
            });
            return;
        }

        // TODO: remove after migrating add card to fragment
        super.onActivityResult(requestCode, resultCode, data);
    }
}
