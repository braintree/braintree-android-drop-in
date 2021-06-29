package com.braintreepayments.api

import org.junit.Test

class CardDetailsFragmentUITest {

    @Test
    fun whenStateIsRESUMED_onCardNumberFieldFocus_returnsToAddCardFragment() {
//        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
//                .creditCards(getSupportedCardConfiguration())
//                .build());
//        DropInClient dropInClient = new MockDropInClientBuilder()
//                .getConfigurationSuccess(configuration)
//                .build();
//        setup(dropInClient);
//
//        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
//        assertThat(mEditCardView).isVisible();
//        assertThat(mAddCardView).isGone();
//
//        mEditCardView.findViewById(R.id.bt_card_form_card_number).requestFocus();
//        assertThat(mAddCardView).isVisible();
//        assertThat(mEditCardView).isGone();
//
//        setText(mAddCardView, R.id.bt_card_form_card_number, "");
//        setText(mAddCardView, R.id.bt_card_form_card_number, AMEX);
//        assertThat(mEditCardView).isVisible();
//        assertThat(mAddCardView).isGone();
    }

    @Test
    fun whenStateIsRESUMED_onCardFormSubmit_sendsCardDetailsEvent() {
//        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
//                .creditCards(getSupportedCardConfiguration())
//                .build());
//        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));
//        DropInClient dropInClient = new MockDropInClientBuilder()
//                .getConfigurationSuccess(configuration)
//                .cardTokenizeSuccess(cardNonce)
//                .build();
//        setup(dropInClient);
//
//        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
//        mAddCardView.findViewById(R.id.bt_button).performClick();
//        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
//        mEditCardView.findViewById(R.id.bt_button).performClick();
//
//        assertTrue(mActivity.isFinishing());
//        DropInResult result = mShadowActivity.getResultIntent()
//                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
//        assertEquals(RESULT_OK, mShadowActivity.getResultCode());
//        assertIsANonce(result.getPaymentMethodNonce().getString());
//        assertEquals("11", ((CardNonce) result.getPaymentMethodNonce()).getLastTwo());
    }

    @Test
    fun whenStateIsRESUMED_onCardFormSubmit_whenOptionalCardholderNameFieldIsEmpty_sendsCardDetailsEventWithoutCardholderName() {
//        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
//                .creditCards(getSupportedCardConfiguration())
//                .build());
//        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));
//        DropInClient dropInClient = new MockDropInClientBuilder()
//                .getConfigurationSuccess(configuration)
//                .cardTokenizeSuccess(cardNonce)
//                .build();
//        setup(dropInClient);
//
//        mActivity.setDropInRequest(new DropInRequest()
//                .cardholderNameStatus(CardForm.FIELD_OPTIONAL)
//                .clientToken(base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN)));
//
//        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
//        mAddCardView.findViewById(R.id.bt_button).performClick();
//        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
//        mEditCardView.findViewById(R.id.bt_button).performClick();
//
//        assertTrue(mActivity.isFinishing());
//        DropInResult result = mShadowActivity.getResultIntent()
//                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
//        assertEquals(RESULT_OK, mShadowActivity.getResultCode());
//        assertIsANonce(result.getPaymentMethodNonce().getString());
//        assertEquals("11", ((CardNonce) result.getPaymentMethodNonce()).getLastTwo());
    }

    @Test
    fun whenStateIsRESUMED_onCardFormSubmit_whenOptionalCardholderNameFieldIsFilled_sendsCardDetailsEventWithCardholderName() {
//        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
//                .creditCards(getSupportedCardConfiguration())
//                .build());
//        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));
//        DropInClient dropInClient = new MockDropInClientBuilder()
//                .getConfigurationSuccess(configuration)
//                .cardTokenizeSuccess(cardNonce)
//                .build();
//        setup(dropInClient);
//
//        mActivity.setDropInRequest(new DropInRequest()
//                .cardholderNameStatus(CardForm.FIELD_OPTIONAL)
//                .clientToken(base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN)));
//
//        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
//        mAddCardView.findViewById(R.id.bt_button).performClick();
//        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
//        setText(mEditCardView, R.id.bt_card_form_cardholder_name, "Brian Tree");
//        mEditCardView.findViewById(R.id.bt_button).performClick();
//
//        assertTrue(mActivity.isFinishing());
//        DropInResult result = mShadowActivity.getResultIntent()
//                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
//        assertEquals(RESULT_OK, mShadowActivity.getResultCode());
//        assertIsANonce(result.getPaymentMethodNonce().getString());
//        assertEquals("11", ((CardNonce) result.getPaymentMethodNonce()).getLastTwo());
    }

    @Test
    fun whenStateIsRESUMED_onCardFormSubmit_whenCardholderNameRequired_sendsCardDetailsEventWithCardholderName() {
//        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
//                .creditCards(getSupportedCardConfiguration())
//                .build());
//        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));
//        DropInClient dropInClient = new MockDropInClientBuilder()
//                .getConfigurationSuccess(configuration)
//                .cardTokenizeSuccess(cardNonce)
//                .build();
//        setup(dropInClient);
//
//        mActivity.setDropInRequest(new DropInRequest()
//                .cardholderNameStatus(CardForm.FIELD_REQUIRED)
//                .clientToken(base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN)));
//
//        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
//        mAddCardView.findViewById(R.id.bt_button).performClick();
//        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
//        setText(mEditCardView, R.id.bt_card_form_cardholder_name, "Brian Tree");
//        mEditCardView.findViewById(R.id.bt_button).performClick();
//
//        assertTrue(mActivity.isFinishing());
//        DropInResult result = mShadowActivity.getResultIntent()
//                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
//        assertEquals(RESULT_OK, mShadowActivity.getResultCode());
//        assertIsANonce(result.getPaymentMethodNonce().getString());
//        assertEquals("11", ((CardNonce) result.getPaymentMethodNonce()).getLastTwo());
    }

    @Test
    fun whenStateIsRESUMED_whenCardNumberValidationErrorsArePresentInViewModel_displaysErrorsInlineToUser() {
//        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
//                .creditCards(getSupportedCardConfiguration())
//                .challenges("cvv", "postal_code")
//                .build());
//        DropInClient dropInClient = new MockDropInClientBuilder()
//                .getConfigurationSuccess(configuration)
//                .cardTokenizeError(ErrorWithResponse.fromJson(Fixtures.CREDIT_CARD_ERROR_RESPONSE))
//                .build();
//        setup(dropInClient);
//
//        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
//        mAddCardView.findViewById(R.id.bt_button).performClick();
//        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
//        setText(mEditCardView, R.id.bt_card_form_cvv, "123");
//        setText(mEditCardView, R.id.bt_card_form_postal_code, "12345");
//        mEditCardView.findViewById(R.id.bt_button).performClick();
//
//        assertThat(mAddCardView).isVisible();
//        assertEquals(ApplicationProvider.getApplicationContext().getString(R.string.bt_card_number_invalid),
//                mAddCardView.getCardForm().getCardEditText().getTextInputLayoutParent().getError());
//
//        assertThat(mEditCardView).isGone();
//        assertEquals(ApplicationProvider.getApplicationContext().getString(R.string.bt_expiration_invalid),
//                mEditCardView.getCardForm().getExpirationDateEditText().getTextInputLayoutParent().getError());
//        assertEquals(ApplicationProvider.getApplicationContext().getString(R.string.bt_cvv_invalid,
//                ApplicationProvider.getApplicationContext().getString(
//                        mEditCardView.getCardForm().getCardEditText().getCardType()
//                                .getSecurityCodeName())),
//                mEditCardView.getCardForm().getCvvEditText().getTextInputLayoutParent().getError());
//        assertEquals(ApplicationProvider.getApplicationContext().getString(R.string.bt_postal_code_invalid),
//                mEditCardView.getCardForm().getPostalCodeEditText().getTextInputLayoutParent().getError());
    }

    @Test
    fun whenStateIsRESUMED_whenCardValidationErrorsArePresentInViewModel_displaysErrorsInlineToUser() {
//        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
//                .creditCards(getSupportedCardConfiguration())
//                .challenges("cvv", "postal_code")
//                .build());
//        DropInClient dropInClient = new MockDropInClientBuilder()
//                .getConfigurationSuccess(configuration)
//                .cardTokenizeError(ErrorWithResponse.fromJson(Fixtures.CREDIT_CARD_NON_NUMBER_ERROR_RESPONSE))
//                .build();
//        setup(dropInClient);
//
//        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
//        mAddCardView.findViewById(R.id.bt_button).performClick();
//        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
//        setText(mEditCardView, R.id.bt_card_form_cvv, "123");
//        setText(mEditCardView, R.id.bt_card_form_postal_code, "12345");
//        mEditCardView.findViewById(R.id.bt_button).performClick();
//
//        assertThat(mAddCardView).isGone();
//        assertNull(mAddCardView.getCardForm().getCardEditText().getTextInputLayoutParent().getError());
//
//        assertThat(mEditCardView).isVisible();
//        assertEquals(ApplicationProvider.getApplicationContext().getString(R.string.bt_expiration_invalid),
//                mEditCardView.getCardForm().getExpirationDateEditText().getTextInputLayoutParent().getError());
//        assertEquals(ApplicationProvider.getApplicationContext().getString(R.string.bt_cvv_invalid,
//                ApplicationProvider.getApplicationContext().getString(
//                        mEditCardView.getCardForm().getCardEditText().getCardType()
//                                .getSecurityCodeName())),
//                mEditCardView.getCardForm().getCvvEditText().getTextInputLayoutParent().getError());
//        assertEquals(ApplicationProvider.getApplicationContext().getString(R.string.bt_postal_code_invalid),
//                mEditCardView.getCardForm().getPostalCodeEditText().getTextInputLayoutParent().getError());
    }

    // TODO: Update this test to mock returning a UserCanceledException after change is made in core
    @Test
    fun showsSubmitButtonAgainWhenThreeDSecureIsCanceled() {
//        mActivity.setDropInRequest(new DropInRequest()
//                .tokenizationKey(TOKENIZATION_KEY)
//                .amount("1.00")
//                .requestThreeDSecureVerification(true));
//
//        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
//                .creditCards(getSupportedCardConfiguration())
//                .threeDSecureEnabled(true)
//                .build());
//        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));
//        DropInClient dropInClient = new MockDropInClientBuilder()
//                .getConfigurationSuccess(configuration)
//                .cardTokenizeSuccess(cardNonce)
//                .handleThreeDSecureActivityResultError(new Exception("user canceled"))
//                .shouldPerformThreeDSecureVerification(true)
//                .build();
//        setup(dropInClient);
//
//        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
//        mAddCardView.findViewById(R.id.bt_button).performClick();
//        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
//        mEditCardView.findViewById(R.id.bt_button).performClick();
//
//        verify(dropInClient).performThreeDSecureVerification(same(mActivity), same(cardNonce), any(DropInResultCallback.class));
//
//        assertThat(mEditCardView.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();
//        assertThat(mEditCardView.findViewById(R.id.bt_button)).isGone();
//
//        mActivity.onActivityResult(BraintreeRequestCodes.THREE_D_SECURE, RESULT_CANCELED, null);
//
//        assertThat(mEditCardView.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();
//        assertThat(mEditCardView.findViewById(R.id.bt_button)).isVisible();
    }
}