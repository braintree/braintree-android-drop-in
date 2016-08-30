package com.braintreepayments.api.dropin.view;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

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
import static org.assertj.android.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

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
    public void setCardNumber_focusesNextView() {
        mView.setup(mActivity, (Configuration) basicConfig());
        assertThat(mView.getCardForm().getExpirationDateEditText()).isNotFocused();

        mView.setCardNumber(VISA);

        assertThat(mView.getCardForm().getExpirationDateEditText()).isFocused();
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
    public void setVisibility_toVisibleFocusesExpirationEditText() {
        mView.setup(mActivity, mock(Configuration.class));
        assertThat(mView.getCardForm().getExpirationDateEditText()).isNotFocused();

        mView.setVisibility(View.VISIBLE);

        assertThat(mView.getCardForm().getExpirationDateEditText()).isFocused();
    }

    @Test
    public void setErrors_displaysAllErrors() {
        Configuration configuration = new TestConfigurationBuilder()
                .challenges("cvv", "postal_code")
                .buildConfiguration();
        mView.setup(mActivity, configuration);

        mView.setErrors(new ErrorWithResponse(422, stringFromFixture("responses/credit_card_error_response.json")));

        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_card_number_invalid),
                mView.getCardForm().getCardEditText().getTextInputLayoutParent().getError());
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_expiration_invalid),
                mView.getCardForm().getExpirationDateEditText().getTextInputLayoutParent().getError());
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_cvv_invalid),
                mView.getCardForm().getCvvEditText().getTextInputLayoutParent().getError());
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_postal_code_invalid),
                mView.getCardForm().getPostalCodeEditText().getTextInputLayoutParent().getError());
    }

    @Test
    public void useUnionPay_doesNothingIfUnionPayNotPresent() {
        mView.setup(mActivity, (Configuration) basicConfig());

        mView.useUnionPay(mActivity, false);

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

        mView.useUnionPay(mActivity, true);

        assertThat(mView.getCardForm().getCardEditText()).isVisible();
        assertThat(mView.getCardForm().getExpirationDateEditText()).isVisible();
        assertThat(mView.getCardForm().getCvvEditText()).isVisible();
        assertThat(mView.getCardForm().getPostalCodeEditText()).isVisible();
        assertThat(mView.getCardForm().getCountryCodeEditText()).isVisible();
        assertThat(mView.getCardForm().getMobileNumberEditText()).isVisible();
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
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_cvv_required),
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
