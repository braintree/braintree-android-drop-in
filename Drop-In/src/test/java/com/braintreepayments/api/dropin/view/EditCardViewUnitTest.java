package com.braintreepayments.api.dropin.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.interfaces.AddPaymentUpdateListener;
import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.test.TestConfigurationBuilder;
import com.braintreepayments.api.test.UnitTestActivity;
import com.braintreepayments.cardform.view.CardForm;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ActivityController;

import static com.braintreepayments.api.test.CardNumber.VISA;
import static com.braintreepayments.api.test.ExpirationDate.VALID_EXPIRATION;
import static com.braintreepayments.api.test.TestConfigurationBuilder.basicConfig;
import static com.braintreepayments.api.test.UnitTestFixturesHelper.stringFromFixture;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.assertj.android.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class EditCardViewUnitTest {

    private ActivityController mActivityController;
    private Activity mActivity;
    private EditCardView mView;

    @Before
    public void setup() {
        UnitTestActivity.view = R.layout.bt_add_card_activity;
        mActivityController = Robolectric.buildActivity(UnitTestActivity.class);
        mActivity = (Activity) mActivityController.setup().get();
        mView = (EditCardView) mActivity.findViewById(R.id.bt_edit_card_view);
    }

    @Test
    public void buttonTextIsAddCard() {
        assertThat((Button) mView.findViewById(R.id.bt_button)).hasText(R.string.bt_add_card);
    }

    @Test
    public void setup_setsUpCardFormBasedOnConfiguration() {
        Configuration configuration = new TestConfigurationBuilder()
                .challenges("cvv", "postal_code")
                .buildConfiguration();
        mView.setup(mActivity, configuration);

        assertThat(mView.getCardForm().getCvvEditText()).isVisible();
        assertThat(mView.getCardForm().getPostalCodeEditText()).isVisible();

        configuration = new TestConfigurationBuilder()
                .buildConfiguration();
        mView.setup(mActivity, configuration);

        assertThat(mView.getCardForm().getCvvEditText()).isGone();
        assertThat(mView.getCardForm().getPostalCodeEditText()).isGone();
    }

    @Test
    public void fieldsAreRestoredOnConfigurationChange() {
        Configuration configuration = new TestConfigurationBuilder()
                .challenges("cvv", "postal_code")
                .buildConfiguration();
        mView.setup(mActivity, configuration);
        mView.useUnionPay(mActivity, true, false);

        mView.getCardForm().getCardEditText().setText(VISA);
        mView.getCardForm().getExpirationDateEditText().setText(VALID_EXPIRATION);
        mView.getCardForm().getCvvEditText().setText("123");
        mView.getCardForm().getPostalCodeEditText().setText("12345");
        mView.getCardForm().getCountryCodeEditText().setText("88");
        mView.getCardForm().getMobileNumberEditText().setText("888888888");

        Bundle bundle = new Bundle();
        mActivityController.saveInstanceState(bundle)
                .pause()
                .stop()
                .destroy();
        mActivityController = Robolectric.buildActivity(UnitTestActivity.class)
                .setup(bundle);
        mActivity = (Activity) mActivityController.get();
        mView = (EditCardView) mActivity.findViewById(R.id.bt_edit_card_view);
        mView.setup(mActivity, configuration);
        mView.useUnionPay(mActivity, true, false);

        assertEquals(VISA, mView.getCardForm().getCardNumber());
        assertEquals(VALID_EXPIRATION, mView.getCardForm().getExpirationDateEditText().getText().toString());
        assertEquals("123", mView.getCardForm().getCvv());
        assertEquals("12345", mView.getCardForm().getPostalCode());
        assertEquals("88", mView.getCardForm().getCountryCode());
        assertEquals("888888888", mView.getCardForm().getMobileNumber());
    }

    @Test
    public void getCardForm_returnsCardForm() {
        CardForm cardForm = (CardForm) mView.findViewById(R.id.bt_card_form);

        assertEquals(cardForm, mView.getCardForm());
    }

    @Test
    public void setCardNumber_setsCardNumber() {
        mView.setCardNumber(VISA);

        assertEquals(VISA, mView.getCardForm().getCardNumber());
    }

    @Test
    public void setVisibility_toVisibleClearsButtonLoadingView() {
        mView.setup(mActivity, mock(Configuration.class));
        mView.getCardForm().getCardEditText().setText(VISA);
        mView.getCardForm().getExpirationDateEditText().setText(VALID_EXPIRATION);
        mView.onCardFormSubmit();
        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();

        mView.setVisibility(View.VISIBLE);

        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();
    }

    @Test
    public void setVisibility_toVisibleFocusesExpiration() {
        mView.setup(mActivity, mock(Configuration.class));
        assertThat(mView.getCardForm().getExpirationDateEditText()).isNotFocused();

        mView.setVisibility(View.VISIBLE);

        assertThat(mView.getCardForm().getExpirationDateEditText()).isFocused();
    }

    @Test
    public void setVisibility_toVisibleFocusesCvv() {
        Configuration configuration = mock(Configuration.class);
        when(configuration.isCvvChallengePresent()).thenReturn(true);
        mView.setup(mActivity, configuration);
        mView.getCardForm().getExpirationDateEditText().setText(VALID_EXPIRATION);
        assertThat(mView.getCardForm().getCvvEditText()).isNotFocused();

        mView.setVisibility(View.VISIBLE);

        assertThat(mView.getCardForm().getCvvEditText()).isFocused();
    }

    @Test
    public void setVisibility_toVisibleFocusesPostalCode() {
        Configuration configuration = mock(Configuration.class);
        when(configuration.isPostalCodeChallengePresent()).thenReturn(true);
        mView.setup(mActivity, configuration);
        mView.getCardForm().getExpirationDateEditText().setText(VALID_EXPIRATION);
        assertThat(mView.getCardForm().getPostalCodeEditText()).isNotFocused();

        mView.setVisibility(View.VISIBLE);

        assertThat(mView.getCardForm().getPostalCodeEditText()).isFocused();
    }

    @Test
    public void setVisibility_toVisibleFocusesCountryCode() {
        mView.setup(mActivity, mock(Configuration.class));
        mView.useUnionPay(mActivity, true, false);
        mView.getCardForm().getExpirationDateEditText().setText(VALID_EXPIRATION);
        mView.getCardForm().getCvvEditText().setText("123");
        assertThat(mView.getCardForm().getCountryCodeEditText()).isNotFocused();

        mView.setVisibility(View.VISIBLE);

        assertThat(mView.getCardForm().getCountryCodeEditText()).isFocused();
    }

    @Test
    public void setVisibility_toVisibleFocusesMobileNumber() {
        mView.setup(mActivity, mock(Configuration.class));
        mView.useUnionPay(mActivity, true, false);
        mView.getCardForm().getExpirationDateEditText().setText(VALID_EXPIRATION);
        mView.getCardForm().getCvvEditText().setText("123");
        mView.getCardForm().getCountryCodeEditText().setText("123");
        assertThat(mView.getCardForm().getMobileNumberEditText()).isNotFocused();

        mView.setVisibility(View.VISIBLE);

        assertThat(mView.getCardForm().getMobileNumberEditText()).isFocused();
    }

    @Test
    public void setVisibility_toVisibleFocusesSubmitButtonWhenFormIsComplete() {
        mView.setup(mActivity, mock(Configuration.class));
        mView.useUnionPay(mActivity, true, false);
        mView.getCardForm().getExpirationDateEditText().setText(VALID_EXPIRATION);
        mView.getCardForm().getCvvEditText().setText("123");
        mView.getCardForm().getCountryCodeEditText().setText("123");
        mView.getCardForm().getMobileNumberEditText().setText("1231231234");
        assertThat(mView.findViewById(R.id.bt_animated_button_view)).isNotFocused();

        mView.setVisibility(View.VISIBLE);

        assertThat(mView.findViewById(R.id.bt_animated_button_view)).isFocused();
    }

    @Test
    public void setVisibility_toVisibleFocusesExpirationWhenOptional() {
        mView.setup(mActivity, mock(Configuration.class));
        mView.useUnionPay(mActivity, true, true);
        assertThat(mView.getCardForm().getExpirationDateEditText()).isNotFocused();

        mView.setVisibility(View.VISIBLE);

        assertThat(mView.getCardForm().getExpirationDateEditText()).isFocused();
    }

    @Test
    public void setVisibility_toVisibleFocusesCvvWhenOptional() {
        mView.setup(mActivity, mock(Configuration.class));
        mView.useUnionPay(mActivity, true, true);
        mView.getCardForm().getExpirationDateEditText().setText(VALID_EXPIRATION);
        assertThat(mView.getCardForm().getCvvEditText()).isNotFocused();

        mView.setVisibility(View.VISIBLE);

        assertThat(mView.getCardForm().getCvvEditText()).isFocused();
    }

    @Test
    public void setErrors_displaysCardErrors() {
        Configuration configuration = new TestConfigurationBuilder()
                .challenges("cvv", "postal_code")
                .buildConfiguration();
        mView.setup(mActivity, configuration);
        ((AnimatedButtonView) mView.findViewById(R.id.bt_animated_button_view)).showLoading();

        mView.setErrors(new ErrorWithResponse(422, stringFromFixture("responses/credit_card_error_response.json")));

        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_card_number_invalid),
                mView.getCardForm().getCardEditText().getTextInputLayoutParent().getError());
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_expiration_invalid),
                mView.getCardForm().getExpirationDateEditText().getTextInputLayoutParent().getError());
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_cvv_invalid,
                RuntimeEnvironment.application.getString(mView.getCardForm().getCardEditText()
                        .getCardType().getSecurityCodeName())),
                mView.getCardForm().getCvvEditText().getTextInputLayoutParent().getError());
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_postal_code_invalid),
                mView.getCardForm().getPostalCodeEditText().getTextInputLayoutParent().getError());
        assertThat(mView.findViewById(R.id.bt_button)).isVisible();
        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();
    }

    @Test
    public void setErrors_displaysUnionPayErrors() {
        mView.setup(mActivity, (Configuration) new TestConfigurationBuilder().buildConfiguration());
        mView.useUnionPay(mActivity, true, false);
        ((AnimatedButtonView) mView.findViewById(R.id.bt_animated_button_view)).showLoading();

        mView.setErrors(new ErrorWithResponse(422, stringFromFixture("responses/unionpay_enrollment_error_response.json")));

        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_expiration_invalid),
                mView.getCardForm().getExpirationDateEditText().getTextInputLayoutParent().getError());
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_country_code_invalid),
                mView.getCardForm().getCountryCodeEditText().getTextInputLayoutParent().getError());
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_mobile_number_invalid),
                mView.getCardForm().getMobileNumberEditText().getTextInputLayoutParent().getError());
        assertThat(mView.findViewById(R.id.bt_button)).isVisible();
        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();
    }

    @Test
    public void useUnionPay_doesNothingIfUnionPayNotPresent() {
        mView.setup(mActivity, (Configuration) basicConfig());

        mView.useUnionPay(mActivity, false, false);

        assertThat(mView.getCardForm().getCountryCodeEditText()).isGone();
        assertThat(mView.getCardForm().getMobileNumberEditText()).isGone();
    }

    @Test
    public void useUnionPay_setsFieldsForUnionPay() {
        Configuration configuration = new TestConfigurationBuilder()
                .challenges("postal_code")
                .buildConfiguration();
        mView.setup(mActivity, configuration);

        assertThat(mView.getCardForm().getCardEditText()).isVisible();
        assertThat(mView.getCardForm().getExpirationDateEditText()).isVisible();
        assertThat(mView.getCardForm().getCvvEditText()).isGone();
        assertThat(mView.getCardForm().getPostalCodeEditText()).isVisible();
        assertThat(mView.getCardForm().getCountryCodeEditText()).isGone();
        assertThat(mView.getCardForm().getMobileNumberEditText()).isGone();
        assertThat(mView.getCardForm().findViewById(R.id.bt_card_form_mobile_number_explanation))
                .isGone();

        mView.useUnionPay(mActivity, true, false);

        assertThat(mView.getCardForm().getCardEditText()).isVisible();
        assertThat(mView.getCardForm().getExpirationDateEditText()).isVisible();
        assertThat(mView.getCardForm().getCvvEditText()).isVisible();
        assertThat(mView.getCardForm().getPostalCodeEditText()).isVisible();
        assertThat(mView.getCardForm().getCountryCodeEditText()).isVisible();
        assertThat(mView.getCardForm().getMobileNumberEditText()).isVisible();
        assertThat((TextView) mView.getCardForm().findViewById(R.id.bt_card_form_mobile_number_explanation))
                .isVisible()
                .hasText(RuntimeEnvironment.application.getString(R.string.bt_unionpay_mobile_number_explanation));
    }

    @Test
    public void useUnionPay_requiresExpirationAndCvvForNonDebitCards() {
        Configuration configuration = new TestConfigurationBuilder()
                .challenges("postal_code")
                .buildConfiguration();
        mView.setup(mActivity, configuration);

        mView.useUnionPay(mActivity, true, false);
        mView.onCardFormSubmit();

        assertThat(mView.getCardForm().getCardEditText()).isVisible();
        assertTrue(mView.getCardForm().getCardEditText().isError());
        assertThat(mView.getCardForm().getExpirationDateEditText()).isVisible();
        assertTrue(mView.getCardForm().getExpirationDateEditText().isError());
        assertThat(mView.getCardForm().getCvvEditText()).isVisible();
        assertTrue(mView.getCardForm().getCvvEditText().isError());
        assertThat(mView.getCardForm().getPostalCodeEditText()).isVisible();
        assertTrue(mView.getCardForm().getPostalCodeEditText().isError());
        assertThat(mView.getCardForm().getCountryCodeEditText()).isVisible();
        assertTrue(mView.getCardForm().getCountryCodeEditText().isError());
        assertThat(mView.getCardForm().getMobileNumberEditText()).isVisible();
        assertTrue(mView.getCardForm().getMobileNumberEditText().isError());
    }

    @Test
    public void useUnionPay_showsExpirationAndCvvForDebitCardsButDoesNotRequireThem() {
        Configuration configuration = new TestConfigurationBuilder()
                .challenges("postal_code")
                .buildConfiguration();
        mView.setup(mActivity, configuration);

        mView.useUnionPay(mActivity, true, true);
        mView.onCardFormSubmit();

        assertThat(mView.getCardForm().getCardEditText()).isVisible();
        assertTrue(mView.getCardForm().getCardEditText().isError());
        assertThat(mView.getCardForm().getExpirationDateEditText()).isVisible();
        assertFalse(mView.getCardForm().getExpirationDateEditText().isError());
        assertThat(mView.getCardForm().getCvvEditText()).isVisible();
        assertFalse(mView.getCardForm().getCvvEditText().isError());
        assertThat(mView.getCardForm().getPostalCodeEditText()).isVisible();
        assertTrue(mView.getCardForm().getPostalCodeEditText().isError());
        assertThat(mView.getCardForm().getCountryCodeEditText()).isVisible();
        assertTrue(mView.getCardForm().getCountryCodeEditText().isError());
        assertThat(mView.getCardForm().getMobileNumberEditText()).isVisible();
        assertTrue(mView.getCardForm().getMobileNumberEditText().isError());
    }

    @Test
    public void onCardFormSubmit_showsErrorsIfFormIsInvalid() {
        Configuration configuration = new TestConfigurationBuilder()
                .challenges("cvv", "postal_code")
                .buildConfiguration();
        mView.setup(mActivity, configuration);

        mView.onCardFormSubmit();

        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_card_number_required),
                mView.getCardForm().getCardEditText().getTextInputLayoutParent().getError());
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_expiration_required),
                mView.getCardForm().getExpirationDateEditText().getTextInputLayoutParent().getError());
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_cvv_required, "CVV"),
                mView.getCardForm().getCvvEditText().getTextInputLayoutParent().getError());
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_postal_code_required),
                mView.getCardForm().getPostalCodeEditText().getTextInputLayoutParent().getError());
    }

    @Test
    public void onCardFormSubmit_doesNotCallListenerIfValidAndListenerIsNotSet() {
        Configuration configuration = new TestConfigurationBuilder()
                .challenges("cvv", "postal_code")
                .buildConfiguration();
        mView.setup(mActivity, configuration);
        mView.setAddPaymentUpdatedListener(null);

        mView.getCardForm().getCardEditText().setText(VISA);
        mView.getCardForm().getExpirationDateEditText().setText(VALID_EXPIRATION);
        mView.getCardForm().getCvvEditText().setText("123");
        mView.getCardForm().getPostalCodeEditText().setText("12345");

        mView.onCardFormSubmit();

        assertThat(mView.findViewById(R.id.bt_animated_button_view)).isVisible();
    }

    @Test
    public void onCardFormSubmit_callsListenerWhenValidAndSet() {
        Configuration configuration = new TestConfigurationBuilder()
                .challenges("cvv", "postal_code")
                .buildConfiguration();
        mView.setup(mActivity, configuration);
        AddPaymentUpdateListener listener = mock(AddPaymentUpdateListener.class);
        mView.setAddPaymentUpdatedListener(listener);

        mView.getCardForm().getCardEditText().setText(VISA);
        mView.getCardForm().getExpirationDateEditText().setText(VALID_EXPIRATION);
        mView.getCardForm().getCvvEditText().setText("123");
        mView.getCardForm().getPostalCodeEditText().setText("12345");

        mView.onCardFormSubmit();

        assertThat(mView.findViewById(R.id.bt_animated_button_view)).isVisible();
        verify(listener).onPaymentUpdated(mView);
    }

    @Test
    public void clickingNextDoesNotShowLoadingIndicatorIfFormIsInvalid() {
        mView.setup(mActivity, (Configuration) basicConfig());

        mView.findViewById(R.id.bt_button).performClick();

        assertThat(mView.findViewById(R.id.bt_button)).isVisible();
        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();
    }

    @Test
    public void clickingNextShowsLoadingIndicatorIfFormIsValid() {
        mView.setup(mActivity, (Configuration) basicConfig());
        mView.getCardForm().getCardEditText().setText(VISA);
        mView.getCardForm().getExpirationDateEditText().setText(VALID_EXPIRATION);

        mView.findViewById(R.id.bt_button).performClick();

        assertThat(mView.findViewById(R.id.bt_button)).isGone();
        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();
    }

    @Test
    public void onClick_doesNothingIfListenerNotSet() {
        mView.setup(mActivity, (Configuration) basicConfig());
        mView.getCardForm().getCardEditText().setText(VISA);
        mView.getCardForm().getExpirationDateEditText().setText(VALID_EXPIRATION);

        mView.onClick(null);

        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();
    }

    @Test
    public void onClick_callsListener() {
        AddPaymentUpdateListener listener = mock(AddPaymentUpdateListener.class);
        mView.setAddPaymentUpdatedListener(listener);
        mView.setup(mActivity, (Configuration) basicConfig());
        mView.getCardForm().getCardEditText().setText(VISA);
        mView.getCardForm().getExpirationDateEditText().setText(VALID_EXPIRATION);

        mView.onClick(null);

        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();
        verify(listener).onPaymentUpdated(mView);
    }

    @Test
    public void onCardFormFieldFocused_doesNothingIfFieldIsNotACardEditText() {
        AddPaymentUpdateListener listener = mock(AddPaymentUpdateListener.class);
        mView.setAddPaymentUpdatedListener(listener);

        mView.onCardFormFieldFocused(mView.getCardForm().getCvvEditText());

        verifyZeroInteractions(listener);
    }

    @Test
    public void onCardFormFieldFocused_callsListenerWhenFieldIsACardEditText() {
        AddPaymentUpdateListener listener = mock(AddPaymentUpdateListener.class);
        mView.setAddPaymentUpdatedListener(listener);

        mView.onCardFormFieldFocused(mView.getCardForm().getCardEditText());

        verify(listener).onBackRequested(mView);
    }
}
