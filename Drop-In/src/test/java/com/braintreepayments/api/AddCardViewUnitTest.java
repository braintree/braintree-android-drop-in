package com.braintreepayments.api;

import android.os.Bundle;
import android.text.SpannableString;
import android.view.View;
import android.widget.Button;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.TestConfigurationBuilder.TestUnionPayConfigurationBuilder;
import com.braintreepayments.cardform.utils.CardType;
import com.braintreepayments.cardform.view.CardForm;
import com.braintreepayments.cardform.view.PaddedImageSpan;
import com.braintreepayments.cardform.view.SupportedCardTypesView;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;

import java.util.Arrays;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import static com.braintreepayments.api.CardNumber.AMEX;
import static com.braintreepayments.api.CardNumber.VISA;
import static com.braintreepayments.api.ReflectionHelper.getField;
import static com.braintreepayments.api.TestConfigurationBuilder.basicConfig;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.assertj.android.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
public class AddCardViewUnitTest {

    private ActivityController mActivityController;
    private AppCompatActivity mActivity;
    private AddCardView mView;

    @Before
    public void setup() {
        UnitTestActivity.view = R.layout.bt_add_card_activity;
        mActivityController = Robolectric.buildActivity(UnitTestActivity.class);
        mActivity = (AppCompatActivity) mActivityController.setup().get();
        mView = mActivity.findViewById(R.id.bt_add_card_view);
        mView.setup(mActivity, (Configuration) new TestConfigurationBuilder()
                .creditCards(new TestConfigurationBuilder.TestCardConfigurationBuilder()
                        .supportedCardTypes(DropInPaymentMethodType.VISA.getCanonicalName()))
                .buildConfiguration(), false);
    }

    @Test
    public void buttonTextIsAddCard() {
        assertThat((Button) mView.findViewById(R.id.bt_button)).hasText(R.string.bt_next);
    }

    @Test
    public void setup_hidesNextButtonIfUnionPayIsNotEnabled() {
        mView.setup(mActivity, (Configuration) basicConfig(), true);

        assertThat(mView.findViewById(R.id.bt_animated_button_view)).isGone();
    }

    @Test
    public void setup_showsNextButtonIfUnionPayIsEnabled() {
        Configuration configuration = new TestConfigurationBuilder()
                .unionPay(new TestUnionPayConfigurationBuilder()
                        .enabled(true))
                .buildConfiguration();

        mView.setup(mActivity, configuration, true);

        assertThat(mView.findViewById(R.id.bt_animated_button_view)).isVisible();
    }

    @Test
    public void setup_showsSupportedCardTypesFromConfiguration() throws NoSuchFieldException,
            IllegalAccessException {
        Configuration configuration = new TestConfigurationBuilder()
                .creditCards(new TestConfigurationBuilder.TestCardConfigurationBuilder()
                        .supportedCardTypes(DropInPaymentMethodType.AMEX.getCanonicalName(),
                                DropInPaymentMethodType.VISA.getCanonicalName(),
                                DropInPaymentMethodType.MASTERCARD.getCanonicalName(),
                                DropInPaymentMethodType.DISCOVER.getCanonicalName(),
                                DropInPaymentMethodType.JCB.getCanonicalName()))
                .buildConfiguration();

        mView.setup(mActivity, configuration, true);

        List<CardType> cardTypes = (List<CardType>) getField(
                mView.findViewById(R.id.bt_supported_card_types), "mSupportedCardTypes");
        assertEquals(5, cardTypes.size());
        assertTrue(cardTypes.contains(CardType.AMEX));
        assertTrue(cardTypes.contains(CardType.VISA));
        assertTrue(cardTypes.contains(CardType.MASTERCARD));
        assertTrue(cardTypes.contains(CardType.DISCOVER));
        assertTrue(cardTypes.contains(CardType.JCB));
    }

    @Test
    public void setup_showsUnionPaySupportedWhenInTheConfigurationAndSupported()
            throws NoSuchFieldException, IllegalAccessException {
        Configuration configuration = new TestConfigurationBuilder()
                .creditCards(new TestConfigurationBuilder.TestCardConfigurationBuilder()
                        .supportedCardTypes(DropInPaymentMethodType.UNIONPAY.getCanonicalName(),
                                DropInPaymentMethodType.VISA.getCanonicalName()))
                .buildConfiguration();

        mView.setup(mActivity, configuration, true);

        List<CardType> cardTypes = (List<CardType>) getField(
                mView.findViewById(R.id.bt_supported_card_types), "mSupportedCardTypes");
        assertEquals(2, cardTypes.size());
        assertTrue(cardTypes.contains(CardType.UNIONPAY));
        assertTrue(cardTypes.contains(CardType.VISA));
    }

    @Test
    public void setup_doesNotShowUnionPaySupportedWhenInTheConfigurationAndNotSupported()
            throws NoSuchFieldException, IllegalAccessException {
        Configuration configuration = new TestConfigurationBuilder()
                .creditCards(new TestConfigurationBuilder.TestCardConfigurationBuilder()
                        .supportedCardTypes(DropInPaymentMethodType.UNIONPAY.getCanonicalName(),
                                DropInPaymentMethodType.VISA.getCanonicalName()))
                .buildConfiguration();

        mView.setup(mActivity, configuration, false);

        List<CardType> cardTypes = (List<CardType>) getField(
                mView.findViewById(R.id.bt_supported_card_types), "mSupportedCardTypes");
        assertEquals(1, cardTypes.size());
        assertTrue(cardTypes.contains(CardType.VISA));
    }

    @Test
    public void setup_doesNotShowUnionPaySupportedWhenNotInTheConfigurationAndNotSupported()
            throws NoSuchFieldException, IllegalAccessException {
        Configuration configuration = new TestConfigurationBuilder()
                .creditCards(new TestConfigurationBuilder.TestCardConfigurationBuilder()
                        .supportedCardTypes(DropInPaymentMethodType.VISA.getCanonicalName()))
                .buildConfiguration();

        mView.setup(mActivity, configuration, false);

        List<CardType> cardTypes = (List<CardType>) getField(
                mView.findViewById(R.id.bt_supported_card_types), "mSupportedCardTypes");
        assertEquals(1, cardTypes.size());
        assertTrue(cardTypes.contains(CardType.VISA));
    }

    @Test
    public void getCardForm_returnsCardForm() {
        CardForm cardForm = (CardForm) mView.findViewById(R.id.bt_card_form);

        assertEquals(cardForm, mView.getCardForm());
    }

    @Test
    public void setVisibility_toVisibleClearsButtonLoadingView() {
        mView.getCardForm().getCardEditText().setText(VISA);
        mView.onCardFormSubmit();
        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();

        mView.setVisibility(View.VISIBLE);

        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();
    }

    @Test
    public void setVisibility_toVisibleFocusesCardEditText() {
        assertThat(mView.getCardForm().getCardEditText()).isNotFocused();

        mView.setVisibility(View.VISIBLE);

        assertThat(mView.getCardForm().getCardEditText()).isFocused();
    }

    @Test
    public void showCardNotSupportedError_showsErrorMessage() {
        mView.getCardForm().getCardEditText().setText(VISA);
        mView.findViewById(R.id.bt_button).performClick();
        assertThat(mView.findViewById(R.id.bt_button)).isGone();
        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();

        mView.showCardNotSupportedError();

        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_card_not_accepted),
                mView.getCardForm().getCardEditText().getTextInputLayoutParent().getError());
        assertThat(mView.findViewById(R.id.bt_button)).isVisible();
        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();
    }

    @Test
    public void isCardNumberError_returnsFalseForNonCardNumberError() throws JSONException {
        ErrorWithResponse errorWithResponse = ErrorWithResponse.fromJson(Fixtures.CREDIT_CARD_EXPIRATION_ERROR_RESPONSE);

        assertFalse(mView.isCardNumberError(errorWithResponse));
    }

    @Test
    public void isCardNumberError_returnsTrueForCardNumberError() throws JSONException {
        ErrorWithResponse errorWithResponse = ErrorWithResponse.fromJson(Fixtures.CREDIT_CARD_ERROR_RESPONSE);

        assertTrue(mView.isCardNumberError(errorWithResponse));
    }

    @Test
    public void setErrors_doesNothingForNonCardNumberError() throws JSONException {
        ErrorWithResponse errorWithResponse = ErrorWithResponse.fromJson(Fixtures.CREDIT_CARD_EXPIRATION_ERROR_RESPONSE);

        mView.getCardForm().getCardEditText().setText(VISA);
        mView.findViewById(R.id.bt_button).performClick();
        assertThat(mView.findViewById(R.id.bt_button)).isGone();
        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();

        mView.setErrors(errorWithResponse);

        assertThat(mView.findViewById(R.id.bt_button)).isVisible();
        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();
        assertNull(mView.getCardForm().getCardEditText().getTextInputLayoutParent().getError());
    }

    @Test
    public void setErrors_displaysError() throws JSONException {
        ErrorWithResponse errorWithResponse = ErrorWithResponse.fromJson(Fixtures.CREDIT_CARD_ERROR_RESPONSE);

        mView.getCardForm().getCardEditText().setText(VISA);
        mView.findViewById(R.id.bt_button).performClick();
        assertThat(mView.findViewById(R.id.bt_button)).isGone();
        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();

        mView.setErrors(errorWithResponse);

        assertThat(mView.findViewById(R.id.bt_button)).isVisible();
        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_card_number_invalid),
                mView.getCardForm().getCardEditText().getTextInputLayoutParent().getError());
    }

    @Test
    public void onCardTypeChanged_showsAllCardTypesWhenEmpty() throws NoSuchFieldException,
            IllegalAccessException {
        mView.onCardTypeChanged(CardType.EMPTY);

        SupportedCardTypesView supportedCardTypesView = mView.findViewById(
                R.id.bt_supported_card_types);
        List<PaddedImageSpan> allSpans = Arrays.asList(new SpannableString(supportedCardTypesView.getText())
                .getSpans(0, supportedCardTypesView.length(), PaddedImageSpan.class));
        assertEquals(1, allSpans.size());
        assertFalse((Boolean) getField(allSpans.get(0), "mDisabled"));
    }

    @Test
    public void onCardTypeChanged_showsEnteredCardType() throws NoSuchFieldException,
            IllegalAccessException {
        mView.setup(mActivity, (Configuration) new TestConfigurationBuilder()
                .creditCards(new TestConfigurationBuilder.TestCardConfigurationBuilder()
                        .supportedCardTypes(DropInPaymentMethodType.VISA.getCanonicalName(),
                                DropInPaymentMethodType.AMEX.getCanonicalName()))
                .buildConfiguration(), true);

        mView.getCardForm().getCardEditText().setText(VISA);

        SupportedCardTypesView supportedCardTypesView = mView.findViewById(
                R.id.bt_supported_card_types);
        List<PaddedImageSpan> allSpans = Arrays.asList(new SpannableString(supportedCardTypesView.getText())
                .getSpans(0, supportedCardTypesView.length(), PaddedImageSpan.class));
        assertEquals(2, allSpans.size());
        for (PaddedImageSpan span : allSpans) {
            if ((int) getField(span, "mResourceId") == R.drawable.bt_ic_visa)  {
                assertFalse((Boolean) getField(span, "mDisabled"));
            } else {
                assertTrue((Boolean) getField(span, "mDisabled"));
            }
        }
    }

    @Test
    public void onClick_doesNothingIfListenerNotSet() {
        mView.setAddPaymentUpdatedListener(null);
        mView.getCardForm().getCardEditText().setText(VISA);

        mView.onClick(null);
    }

    @Test
    public void onClick_showsErrorMessageIfCardFormInvalid() {
        AddPaymentUpdateListener listener = mock(AddPaymentUpdateListener.class);
        mView.setAddPaymentUpdatedListener(listener);
        mView.getCardForm().getCardEditText().setText("4");

        mView.onClick(null);

        verifyZeroInteractions(listener);
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_card_number_invalid),
                mView.getCardForm().getCardEditText().getTextInputLayoutParent().getError());
        assertThat(mView.findViewById(R.id.bt_button)).isVisible();
        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();
    }

    @Test
    public void onClick_showsErrorMessageIfCardTypeNotSupported() {
        AddPaymentUpdateListener listener = mock(AddPaymentUpdateListener.class);
        mView.setAddPaymentUpdatedListener(listener);
        mView.getCardForm().getCardEditText().setText(AMEX);

        mView.onClick(null);

        verifyZeroInteractions(listener);
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_card_not_accepted),
                mView.getCardForm().getCardEditText().getTextInputLayoutParent().getError());
        assertThat(mView.findViewById(R.id.bt_button)).isVisible();
        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();
    }

    @Test
    public void clickingNextDoesNotShowLoadingIfCardFormInvalid() {
        mView.getCardForm().getCardEditText().setText("4");

        mView.findViewById(R.id.bt_button).performClick();

        assertThat(mView.findViewById(R.id.bt_button)).isVisible();
        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();
    }

    @Test
    public void onClick_callsListenerIfCardValidAndListenerSet() {
        AddPaymentUpdateListener listener = mock(AddPaymentUpdateListener.class);
        mView.setAddPaymentUpdatedListener(listener);
        mView.getCardForm().getCardEditText().setText(VISA);

        mView.onClick(null);

        verify(listener, times(2)).onPaymentUpdated(mView);
    }

    @Test
    public void cardNumberIsRestoredOnConfigurationChange() {
        mView.getCardForm().getCardEditText().setText(VISA);

        Bundle bundle = new Bundle();
        mActivityController.saveInstanceState(bundle)
                .pause()
                .stop()
                .destroy();
        mActivityController = Robolectric.buildActivity(UnitTestActivity.class)
                .setup(bundle);
        mActivity = (AppCompatActivity) mActivityController.get();
        mView = mActivity.findViewById(R.id.bt_add_card_view);
        mView.setup(mActivity, (Configuration) basicConfig(), true);

        assertEquals(VISA, mView.getCardForm().getCardNumber());
    }

    @Test
    public void onCardFormValid_showLoadingIndicatorAndCallsListenerWhenCardFormIsValid() {
        AddPaymentUpdateListener listener = mock(AddPaymentUpdateListener.class);
        mView.setAddPaymentUpdatedListener(listener);

        mView.getCardForm().getCardEditText().setText(VISA);

        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();
        verify(listener).onPaymentUpdated(mView);
    }

    @Test
    public void onCardFormValid_doesNothingWhenCardFormIsInvalid() {
        AddPaymentUpdateListener listener = mock(AddPaymentUpdateListener.class);
        mView.setAddPaymentUpdatedListener(listener);
        mView.getCardForm().getCardEditText().setText("");

        mView.onCardFormValid(false);

        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();
        verifyZeroInteractions(listener);
    }

    @Test
    public void onCardFormSubmit_showsLoadingIndicatorAndCallsListenerIfValid() {
        AddPaymentUpdateListener listener = mock(AddPaymentUpdateListener.class);
        mView.setAddPaymentUpdatedListener(listener);
        mView.getCardForm().getCardEditText().setText(VISA);

        mView.onCardFormSubmit();

        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();
        verify(listener, times(2)).onPaymentUpdated(mView);
    }

    @Test
    public void onCardFormSubmit_showsErrorIfCardIsInvalid() {
        AddPaymentUpdateListener listener = mock(AddPaymentUpdateListener.class);
        mView.setAddPaymentUpdatedListener(listener);
        mView.getCardForm().getCardEditText().setText("4");

        mView.onCardFormSubmit();

        verifyZeroInteractions(listener);
        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_card_number_invalid),
                mView.getCardForm().getCardEditText().getTextInputLayoutParent().getError());
    }

    @Test
    public void onCardFormSubmit_showsErrorMessageIfCardTypeNotSupported() {
        AddPaymentUpdateListener listener = mock(AddPaymentUpdateListener.class);
        mView.setAddPaymentUpdatedListener(listener);
        mView.getCardForm().getCardEditText().setText(AMEX);

        mView.onCardFormSubmit();

        verifyZeroInteractions(listener);
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_card_not_accepted),
                mView.getCardForm().getCardEditText().getTextInputLayoutParent().getError());
        assertThat(mView.findViewById(R.id.bt_button)).isVisible();
        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();
    }
}
