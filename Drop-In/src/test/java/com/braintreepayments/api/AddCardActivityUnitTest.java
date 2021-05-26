package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.test.core.app.ApplicationProvider;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.cardform.view.CardForm;
import com.braintreepayments.cardform.view.ErrorEditText;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;

import static androidx.appcompat.app.AppCompatActivity.RESULT_CANCELED;
import static androidx.appcompat.app.AppCompatActivity.RESULT_FIRST_USER;
import static androidx.appcompat.app.AppCompatActivity.RESULT_OK;
import static com.braintreepayments.api.Assertions.assertIsANonce;
import static com.braintreepayments.api.CardNumber.AMEX;
import static com.braintreepayments.api.CardNumber.UNIONPAY_CREDIT;
import static com.braintreepayments.api.CardNumber.UNIONPAY_DEBIT;
import static com.braintreepayments.api.CardNumber.UNIONPAY_SMS_NOT_REQUIRED;
import static com.braintreepayments.api.CardNumber.VISA;
import static com.braintreepayments.api.PackageManagerUtils.mockPackageManagerSupportsThreeDSecure;
import static com.braintreepayments.api.TestTokenizationKey.TOKENIZATION_KEY;
import static com.braintreepayments.api.UnitTestFixturesHelper.base64EncodedClientTokenFromFixture;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.assertj.android.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class AddCardActivityUnitTest {

    private ActivityController mActivityController;
    private AddCardUnitTestActivity mActivity;
    private ShadowActivity mShadowActivity;
    private AddCardView mAddCardView;
    private EditCardView mEditCardView;
    private EnrollmentCardView mEnrollmentCardView;

    @Before
    public void setup() {
        mActivityController = Robolectric.buildActivity(AddCardUnitTestActivity.class);
        mActivity = (AddCardUnitTestActivity) mActivityController.get();
        mShadowActivity = shadowOf(mActivity);
    }

    @Test
    public void sendsAnalyticsEventWhenStarted() throws JSONException {
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
                .build();
        setup(dropInClient);

        verify(dropInClient).sendAnalyticsEvent("card.selected");
    }

    @Test
    public void showsLoadingViewWhileWaitingForConfiguration() {
        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        setup(dropInClient);

        assertEquals(0, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher))
                .getDisplayedChild());
        assertThat(mAddCardView).isGone();
        assertThat(mEditCardView).isGone();
        assertThat(mEnrollmentCardView).isGone();
    }

    @Test
    public void setsTitleToCardDetailsWhenStarted() throws JSONException {
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
                .build();
        setup(dropInClient);

        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_card_details),
                mActivity.getSupportActionBar().getTitle());
    }

    @Test
    public void tappingUpExitsActivityWithResultCanceled() throws JSONException {
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
                .build();
        setup(dropInClient);

        mShadowActivity.clickMenuItem(android.R.id.home);

        assertTrue(mActivity.isFinishing());
        assertEquals(RESULT_CANCELED, mShadowActivity.getResultCode());
    }

    @Test
    public void pressingBackExitsActivityWithResultCanceled() throws JSONException {
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
                .build();
        setup(dropInClient);

        mActivity.onBackPressed();

        assertTrue(mActivity.isFinishing());
        assertEquals(RESULT_CANCELED, mShadowActivity.getResultCode());
    }

    @Test
    public void showsAddCardViewAfterConfigurationIsFetched() throws JSONException {
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
                .build();
        setup(dropInClient);

        assertThat(mAddCardView).isVisible();
        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher))
                .getDisplayedChild());
        assertThat(mEditCardView).isGone();
        assertThat(mEnrollmentCardView).isGone();
    }

    // TODO: test configuration changes
    @Test
    @Ignore("Determine what we're testing here. The concept of a configuration change may be different in v4.")
    public void configurationChangeReturnsToAddCardView() {
//        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
//                .configuration(new TestConfigurationBuilder().build());
//        setup(httpClient);
        assertThat(mAddCardView).isVisible();
        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher))
                .getDisplayedChild());
        assertThat(mEditCardView).isGone();
        assertThat(mEnrollmentCardView).isGone();

//        triggerConfigurationChange(httpClient);

        assertThat(mAddCardView).isVisible();
        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher))
                .getDisplayedChild());
        assertThat(mEditCardView).isGone();
        assertThat(mEnrollmentCardView).isGone();
    }

    @Test
    public void enteringACardNumberGoesToCardDetailsView() throws JSONException {
        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .build());
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(configuration)
                .build();
        setup(dropInClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);

        assertThat(mEditCardView).isVisible();
        assertThat(mAddCardView).isGone();
        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher))
                .getDisplayedChild());
        assertThat(mEnrollmentCardView).isGone();
    }

    @Test
    @Ignore("Determine what we're testing here. The concept of a configuration change may be different in v4.")
    public void configurationChangeReturnsToEditCardView() {
//        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
//                .configuration(new TestConfigurationBuilder()
//                        .creditCards(getSupportedCardConfiguration())
//                        .build());
//        setup(httpClient);
        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
        assertThat(mEditCardView).isVisible();
        assertThat(mAddCardView).isGone();
        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher))
                .getDisplayedChild());
        assertThat(mEnrollmentCardView).isGone();

//        triggerConfigurationChange(httpClient);

        assertThat(mEditCardView).isVisible();
        assertThat(mAddCardView).isGone();
        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher))
                .getDisplayedChild());
        assertThat(mEnrollmentCardView).isGone();
    }

    @Test
    public void editingANonUnionPayCardNumberIsPossible() throws JSONException {
        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .build());
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(configuration)
                .build();
        setup(dropInClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
        assertThat(mEditCardView).isVisible();
        assertThat(mAddCardView).isGone();

        mEditCardView.findViewById(R.id.bt_card_form_card_number).requestFocus();
        assertThat(mAddCardView).isVisible();
        assertThat(mEditCardView).isGone();

        setText(mAddCardView, R.id.bt_card_form_card_number, "");
        setText(mAddCardView, R.id.bt_card_form_card_number, AMEX);
        assertThat(mEditCardView).isVisible();
        assertThat(mAddCardView).isGone();
    }

    @Test
    public void editingAUnionPayCardNumberIsPossible() throws JSONException {
        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .unionPay(new TestConfigurationBuilder.TestUnionPayConfigurationBuilder()
                        .enabled(true))
                .build());
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(configuration)
                .unionPayCapabilitiesSuccess(UnionPayCapabilities.fromJson(Fixtures.UNIONPAY_CAPABILITIES_SUCCESS_RESPONSE))
                .build();
        mActivity.setDropInRequest(new DropInRequest()
                .clientToken(base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN)));
        mActivity.mClientTokenPresent = true;
        setup(dropInClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, UNIONPAY_CREDIT);
        assertThat(mEditCardView).isVisible();
        assertThat(mAddCardView).isGone();

        mEditCardView.findViewById(R.id.bt_card_form_card_number).requestFocus();
        assertThat(mAddCardView).isVisible();
        assertThat(mEditCardView).isGone();

        setText(mAddCardView, R.id.bt_card_form_card_number, UNIONPAY_CREDIT);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        assertThat(mEditCardView).isVisible();
        assertThat(mAddCardView).isGone();
    }

    @Test
    public void addingACardRemainsOnEditCardView() throws JSONException {
        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .build());
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(configuration)
                .build();
        setup(dropInClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        mEditCardView.findViewById(R.id.bt_button).performClick();

        assertThat(mEditCardView).isVisible();
        assertThat(mEditCardView.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();
        assertThat(mEditCardView.findViewById(R.id.bt_button)).isGone();
        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher))
                .getDisplayedChild());
        assertThat(mAddCardView).isGone();
        assertThat(mEnrollmentCardView).isGone();
    }

    @Test
    public void addingACardReturnsANonce() throws JSONException {
        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .build());
        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(configuration)
                .cardTokenizeSuccess(cardNonce)
                .build();
        setup(dropInClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        mEditCardView.findViewById(R.id.bt_button).performClick();

        assertTrue(mActivity.isFinishing());
        DropInResult result = mShadowActivity.getResultIntent()
                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        assertEquals(RESULT_OK, mShadowActivity.getResultCode());
        assertIsANonce(result.getPaymentMethodNonce().getString());
        assertEquals("11", ((CardNonce) result.getPaymentMethodNonce()).getLastTwo());
    }

    @Test
    public void addingACard_whenCardholderNameOptionalAndEmpty_doesNotSendCardholderNameToTokenize() throws JSONException {
        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .build());
        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(configuration)
                .cardTokenizeSuccess(cardNonce)
                .build();
        setup(dropInClient);

        mActivity.setDropInRequest(new DropInRequest()
                .cardholderNameStatus(CardForm.FIELD_OPTIONAL)
                .clientToken(base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN)));

        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        mEditCardView.findViewById(R.id.bt_button).performClick();

        assertTrue(mActivity.isFinishing());
        DropInResult result = mShadowActivity.getResultIntent()
                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        assertEquals(RESULT_OK, mShadowActivity.getResultCode());
        assertIsANonce(result.getPaymentMethodNonce().getString());
        assertEquals("11", ((CardNonce) result.getPaymentMethodNonce()).getLastTwo());
    }

    @Test
    public void addingACard_whenCardholderNameOptionalAndFilled_sendsCardholderNameToTokenize() throws JSONException {
        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .build());
        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(configuration)
                .cardTokenizeSuccess(cardNonce)
                .build();
        setup(dropInClient);

        mActivity.setDropInRequest(new DropInRequest()
                .cardholderNameStatus(CardForm.FIELD_OPTIONAL)
                .clientToken(base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN)));

        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        setText(mEditCardView, R.id.bt_card_form_cardholder_name, "Brian Tree");
        mEditCardView.findViewById(R.id.bt_button).performClick();

        assertTrue(mActivity.isFinishing());
        DropInResult result = mShadowActivity.getResultIntent()
                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        assertEquals(RESULT_OK, mShadowActivity.getResultCode());
        assertIsANonce(result.getPaymentMethodNonce().getString());
        assertEquals("11", ((CardNonce) result.getPaymentMethodNonce()).getLastTwo());
    }

    @Test
    public void addingACard_whenCardholderNameRequired_sendsCardholderNameToTokenize() throws JSONException {
        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .build());
        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(configuration)
                .cardTokenizeSuccess(cardNonce)
                .build();
        setup(dropInClient);

        mActivity.setDropInRequest(new DropInRequest()
                .cardholderNameStatus(CardForm.FIELD_REQUIRED)
                .clientToken(base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN)));

        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        setText(mEditCardView, R.id.bt_card_form_cardholder_name, "Brian Tree");
        mEditCardView.findViewById(R.id.bt_button).performClick();

        assertTrue(mActivity.isFinishing());
        DropInResult result = mShadowActivity.getResultIntent()
                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        assertEquals(RESULT_OK, mShadowActivity.getResultCode());
        assertIsANonce(result.getPaymentMethodNonce().getString());
        assertEquals("11", ((CardNonce) result.getPaymentMethodNonce()).getLastTwo());
    }

    @Test
    public void cardNumberValidationErrorsAreShownToTheUser() throws JSONException {
        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .challenges("cvv", "postal_code")
                .build());
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(configuration)
                .cardTokenizeError(ErrorWithResponse.fromJson(Fixtures.CREDIT_CARD_ERROR_RESPONSE))
                .build();
        setup(dropInClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        setText(mEditCardView, R.id.bt_card_form_cvv, "123");
        setText(mEditCardView, R.id.bt_card_form_postal_code, "12345");
        mEditCardView.findViewById(R.id.bt_button).performClick();

        assertThat(mAddCardView).isVisible();
        assertEquals(ApplicationProvider.getApplicationContext().getString(R.string.bt_card_number_invalid),
                mAddCardView.getCardForm().getCardEditText().getTextInputLayoutParent().getError());

        assertThat(mEditCardView).isGone();
        assertEquals(ApplicationProvider.getApplicationContext().getString(R.string.bt_expiration_invalid),
                mEditCardView.getCardForm().getExpirationDateEditText().getTextInputLayoutParent().getError());
        assertEquals(ApplicationProvider.getApplicationContext().getString(R.string.bt_cvv_invalid,
                ApplicationProvider.getApplicationContext().getString(
                        mEditCardView.getCardForm().getCardEditText().getCardType()
                                .getSecurityCodeName())),
                mEditCardView.getCardForm().getCvvEditText().getTextInputLayoutParent().getError());
        assertEquals(ApplicationProvider.getApplicationContext().getString(R.string.bt_postal_code_invalid),
                mEditCardView.getCardForm().getPostalCodeEditText().getTextInputLayoutParent().getError());
    }

    @Test
    public void cardValidationErrorsAreShownToTheUser() throws JSONException {
        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .challenges("cvv", "postal_code")
                .build());
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(configuration)
                .cardTokenizeError(ErrorWithResponse.fromJson(Fixtures.CREDIT_CARD_NON_NUMBER_ERROR_RESPONSE))
                .build();
        setup(dropInClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        setText(mEditCardView, R.id.bt_card_form_cvv, "123");
        setText(mEditCardView, R.id.bt_card_form_postal_code, "12345");
        mEditCardView.findViewById(R.id.bt_button).performClick();

        assertThat(mAddCardView).isGone();
        assertNull(mAddCardView.getCardForm().getCardEditText().getTextInputLayoutParent().getError());

        assertThat(mEditCardView).isVisible();
        assertEquals(ApplicationProvider.getApplicationContext().getString(R.string.bt_expiration_invalid),
                mEditCardView.getCardForm().getExpirationDateEditText().getTextInputLayoutParent().getError());
        assertEquals(ApplicationProvider.getApplicationContext().getString(R.string.bt_cvv_invalid,
                ApplicationProvider.getApplicationContext().getString(
                        mEditCardView.getCardForm().getCardEditText().getCardType()
                                .getSecurityCodeName())),
                mEditCardView.getCardForm().getCvvEditText().getTextInputLayoutParent().getError());
        assertEquals(ApplicationProvider.getApplicationContext().getString(R.string.bt_postal_code_invalid),
                mEditCardView.getCardForm().getPostalCodeEditText().getTextInputLayoutParent().getError());
    }

    @Test
    public void unionPayValidationErrorsAreShownToTheUser() throws JSONException {
        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .unionPay(new TestConfigurationBuilder.TestUnionPayConfigurationBuilder()
                        .enabled(true))
                .build());
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(configuration)
                .unionPayCapabilitiesSuccess(UnionPayCapabilities.fromJson(Fixtures.UNIONPAY_CAPABILITIES_SUCCESS_RESPONSE))
                .enrollUnionPayError(ErrorWithResponse.fromJson(Fixtures.UNIONPAY_ENROLLMENT_ERROR_RESPONSE))
                .build();
        mActivity.setDropInRequest(new DropInRequest()
                .clientToken(base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN)));
        mActivity.mClientTokenPresent = true;
        setup(dropInClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, UNIONPAY_CREDIT);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        setText(mEditCardView, R.id.bt_card_form_cvv, "123");
        setText(mEditCardView, R.id.bt_card_form_country_code, "123");
        setText(mEditCardView, R.id.bt_card_form_mobile_number, "12345678");
        mEditCardView.findViewById(R.id.bt_button).performClick();

        assertEquals(ApplicationProvider.getApplicationContext().getString(R.string.bt_expiration_invalid),
                mEditCardView.getCardForm().getExpirationDateEditText().getTextInputLayoutParent().getError());
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_country_code_invalid),
                mEditCardView.getCardForm().getCountryCodeEditText().getTextInputLayoutParent().getError());
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_mobile_number_invalid),
                mEditCardView.getCardForm().getMobileNumberEditText().getTextInputLayoutParent().getError());
    }

    @Test
    public void smsCodeValidationErrorsAreShownToTheUser() throws JSONException {
        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .unionPay(new TestConfigurationBuilder.TestUnionPayConfigurationBuilder()
                        .enabled(true))
                .build());
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(configuration)
                .unionPayCapabilitiesSuccess(UnionPayCapabilities.fromJson(Fixtures.UNIONPAY_CAPABILITIES_SUCCESS_RESPONSE))
                .enrollUnionPaySuccess(new UnionPayEnrollment("enrollment-id", true))
                .unionPayTokenizeError(ErrorWithResponse.fromJson(Fixtures.UNIONPAY_SMS_CODE_ERROR_RESPONSE))
                .build();
        mActivity.setDropInRequest(new DropInRequest()
                .clientToken(base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN)));
        mActivity.mClientTokenPresent = true;
        setup(dropInClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, UNIONPAY_CREDIT);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        setText(mEditCardView, R.id.bt_card_form_cvv, "123");
        setText(mEditCardView, R.id.bt_card_form_country_code, "123");
        setText(mEditCardView, R.id.bt_card_form_mobile_number, "12345678");
        mEditCardView.findViewById(R.id.bt_button).performClick();
        setText(mEnrollmentCardView, R.id.bt_sms_code, "123456");
        mEnrollmentCardView.findViewById(R.id.bt_button).performClick();

        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_unionpay_sms_code_invalid),
                ((ErrorEditText) mEnrollmentCardView.findViewById(R.id.bt_sms_code)).getTextInputLayoutParent().getError());
    }

    @Test
    public void onPaymentMethodNonceCreated_sendsAnalyticsEvent() throws JSONException {
        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        setup(dropInClient);

        mActivity.onPaymentMethodNonceCreated(CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE)));

        verify(dropInClient).sendAnalyticsEvent("sdk.exit.success");
    }

    @Test
    public void nonFormFieldError_callsFinishWithError() {
        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        setup(dropInClient);

        ErrorWithResponse error = new ErrorWithResponse(422, "{\n" +
                "  \"error\": {\n" +
                "    \"message\": \"Error message\"\n" +
                "  }\n" +
                "}");

        mActivity.onError(error);

        assertTrue(mActivity.isFinishing());
        assertEquals(RESULT_FIRST_USER, mShadowActivity.getResultCode());
        Exception actualException = (Exception) mShadowActivity.getResultIntent()
                .getSerializableExtra(DropInActivity.EXTRA_ERROR);
        assertEquals(error.getClass(), actualException.getClass());
        assertEquals("Error message", actualException.getMessage());
    }

    @Test
    public void configurationExceptionExitsActivityWithError() {
        assertExceptionIsReturned("configuration-exception",
                new ConfigurationException("Configuration exception"));
    }

    @Test
    public void authenticationExceptionExitsActivityWithError() {
        assertExceptionIsReturned("developer-error", new AuthenticationException("Access denied"));
    }

    @Test
    public void authorizationExceptionExitsActivityWithError() {
        assertExceptionIsReturned("developer-error", new AuthorizationException("Access denied"));
    }

    @Test
    public void upgradeRequiredExceptionExitsActivityWithError() {
        assertExceptionIsReturned("developer-error", new UpgradeRequiredException("Exception"));
    }

    @Test
    public void serverExceptionExitsActivityWithError() {
        assertExceptionIsReturned("server-error", new ServerException("Exception"));
    }

    @Test
    public void unexpectedExceptionExitsActivityWithError() {
        assertExceptionIsReturned("server-error", new UnexpectedException("Exception"));
    }

    @Test
    public void downForMaintenanceExceptionExitsActivityWithError() {
        assertExceptionIsReturned("server-unavailable",
                new ServiceUnavailableException("Exception"));
    }

    @Test
    public void enrollmentViewSetsTitleToConfirmEnrollment() throws JSONException {
        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .unionPay(new TestConfigurationBuilder.TestUnionPayConfigurationBuilder()
                        .enabled(true))
                .build());
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(configuration)
                .unionPayCapabilitiesSuccess(UnionPayCapabilities.fromJson(Fixtures.UNIONPAY_CAPABILITIES_SUCCESS_RESPONSE))
                .enrollUnionPaySuccess(new UnionPayEnrollment("enrollment-id", true))
                .build();
        mActivity.setDropInRequest(new DropInRequest()
                .clientToken(base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN)));
        mActivity.mClientTokenPresent = true;
        setup(dropInClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, UNIONPAY_CREDIT);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        setText(mEditCardView, R.id.bt_card_form_cvv, "123");
        setText(mEditCardView, R.id.bt_card_form_country_code, "86");
        setText(mEditCardView, R.id.bt_card_form_mobile_number, "8888888888");
        mEditCardView.findViewById(R.id.bt_button).performClick();

        assertThat(mAddCardView).isGone();
        assertThat(mEditCardView).isGone();
        assertThat(mEnrollmentCardView).isVisible();
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_confirm_enrollment),
                mActivity.getSupportActionBar().getTitle());
    }

    @Test
    public void usingAUnionPayCardShowsMobileNumberFields() throws JSONException {
        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .unionPay(new TestConfigurationBuilder.TestUnionPayConfigurationBuilder()
                        .enabled(true))
                .build());
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(configuration)
                .unionPayCapabilitiesSuccess(UnionPayCapabilities.fromJson(Fixtures.UNIONPAY_CAPABILITIES_SUCCESS_RESPONSE))
                .build();
        mActivity.setDropInRequest(new DropInRequest()
                .clientToken(base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN)));
        mActivity.mClientTokenPresent = true;
        setup(dropInClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, UNIONPAY_CREDIT);
        mAddCardView.findViewById(R.id.bt_button).performClick();

        assertThat(mEditCardView).isVisible();
        assertThat(mEditCardView.findViewById(R.id.bt_card_form_country_code)).isVisible();
        assertThat(mEditCardView.findViewById(R.id.bt_card_form_mobile_number)).isVisible();
    }

    @Test
    public void addingAUnionPayCardShowsEnrollment() throws JSONException {
        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .unionPay(new TestConfigurationBuilder.TestUnionPayConfigurationBuilder()
                        .enabled(true))
                .build());
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(configuration)
                .unionPayCapabilitiesSuccess(UnionPayCapabilities.fromJson(Fixtures.UNIONPAY_CAPABILITIES_SUCCESS_RESPONSE))
                .enrollUnionPaySuccess(new UnionPayEnrollment("ennrollment-id", true))
                .build();
        mActivity.setDropInRequest(new DropInRequest()
                .clientToken(base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN)));
        mActivity.mClientTokenPresent = true;
        setup(dropInClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, UNIONPAY_CREDIT);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        setText(mEditCardView, R.id.bt_card_form_cvv, "123");
        setText(mEditCardView, R.id.bt_card_form_country_code, "86");
        setText(mEditCardView, R.id.bt_card_form_mobile_number, "8888888888");
        mEditCardView.findViewById(R.id.bt_button).performClick();

        assertThat(mAddCardView).isGone();
        assertThat(mEditCardView).isGone();
        assertThat(mEnrollmentCardView).isVisible();
    }

    @Test
    public void addingAUnionPayCardDoesNotShowEnrollment() throws JSONException {
        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .unionPay(new TestConfigurationBuilder.TestUnionPayConfigurationBuilder()
                        .enabled(true))
                .build());
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(configuration)
                .unionPayCapabilitiesSuccess(UnionPayCapabilities.fromJson(Fixtures.UNIONPAY_CAPABILITIES_SUCCESS_RESPONSE))
                .enrollUnionPaySuccess(new UnionPayEnrollment("ennrollment-id", false))
                .unionPayTokenizeSuccess(CardNonce.fromJSON(new JSONObject(Fixtures.PAYMENT_METHODS_UNIONPAY_CREDIT_CARD)))
                .build();
        mActivity.setDropInRequest(new DropInRequest()
                .clientToken(base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN)));
        mActivity.mClientTokenPresent = true;
        setup(dropInClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, UNIONPAY_SMS_NOT_REQUIRED);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        setText(mEditCardView, R.id.bt_card_form_cvv, "123");
        setText(mEditCardView, R.id.bt_card_form_country_code, "86");
        setText(mEditCardView, R.id.bt_card_form_mobile_number, "8888888888");
        mEditCardView.findViewById(R.id.bt_button).performClick();

        assertTrue(mActivity.isFinishing());
        DropInResult result = mShadowActivity.getResultIntent()
                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        assertEquals(RESULT_OK, mShadowActivity.getResultCode());
        assertIsANonce(result.getPaymentMethodNonce().getString());
        assertEquals("85", ((CardNonce) result.getPaymentMethodNonce()).getLastTwo());
    }

    @Test
    public void addingAUnionPayCard_whenCardholderNameOptionalAndEmpty_tokenizesWithoutCardholderName() throws JSONException {
        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .unionPay(new TestConfigurationBuilder.TestUnionPayConfigurationBuilder()
                        .enabled(true))
                .build());
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(configuration)
                .unionPayCapabilitiesSuccess(UnionPayCapabilities.fromJson(Fixtures.UNIONPAY_CAPABILITIES_SUCCESS_RESPONSE))
                .enrollUnionPaySuccess(new UnionPayEnrollment("ennrollment-id", false))
                .unionPayTokenizeSuccess(CardNonce.fromJSON(new JSONObject(Fixtures.PAYMENT_METHODS_UNIONPAY_CREDIT_CARD)))
                .build();
        mActivity.setDropInRequest(new DropInRequest()
                .cardholderNameStatus(CardForm.FIELD_OPTIONAL)
                .clientToken(base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN)));
        mActivity.mClientTokenPresent = true;
        setup(dropInClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, UNIONPAY_SMS_NOT_REQUIRED);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        setText(mEditCardView, R.id.bt_card_form_cvv, "123");
        setText(mEditCardView, R.id.bt_card_form_country_code, "86");
        setText(mEditCardView, R.id.bt_card_form_mobile_number, "8888888888");
        mEditCardView.findViewById(R.id.bt_button).performClick();

        assertTrue(mActivity.isFinishing());
        DropInResult result = mShadowActivity.getResultIntent()
                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        assertEquals(RESULT_OK, mShadowActivity.getResultCode());
        assertIsANonce(result.getPaymentMethodNonce().getString());
        assertEquals("85", ((CardNonce) result.getPaymentMethodNonce()).getLastTwo());
    }

    @Test
    public void addingAUnionPayCard_whenCardholderNameOptionalAndFilled_tokenizesWithCardholderName() throws JSONException {
        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .unionPay(new TestConfigurationBuilder.TestUnionPayConfigurationBuilder()
                        .enabled(true))
                .build());
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(configuration)
                .unionPayCapabilitiesSuccess(UnionPayCapabilities.fromJson(Fixtures.UNIONPAY_CAPABILITIES_SUCCESS_RESPONSE))
                .enrollUnionPaySuccess(new UnionPayEnrollment("ennrollment-id", false))
                .unionPayTokenizeSuccess(CardNonce.fromJSON(new JSONObject(Fixtures.PAYMENT_METHODS_UNIONPAY_CREDIT_CARD)))
                .build();

        mActivity.mClientTokenPresent = true;
        mActivity.setDropInRequest(new DropInRequest()
                .cardholderNameStatus(CardForm.FIELD_OPTIONAL)
                .clientToken(Fixtures.BASE64_CLIENT_TOKEN));
        setup(dropInClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, UNIONPAY_SMS_NOT_REQUIRED);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        setText(mEditCardView, R.id.bt_card_form_cvv, "123");
        setText(mEditCardView, R.id.bt_card_form_country_code, "86");
        setText(mEditCardView, R.id.bt_card_form_mobile_number, "8888888888");
        setText(mEditCardView, R.id.bt_card_form_cardholder_name, "Brian Tree");
        mEditCardView.findViewById(R.id.bt_button).performClick();

        assertTrue(mActivity.isFinishing());
        DropInResult result = mShadowActivity.getResultIntent()
                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        assertEquals(RESULT_OK, mShadowActivity.getResultCode());
        assertIsANonce(result.getPaymentMethodNonce().getString());
        assertEquals("85", ((CardNonce) result.getPaymentMethodNonce()).getLastTwo());
    }

    @Test
    public void addingAUnionPayCard_whenCardholderNameRequired_tokenizesWithCardholderName() throws JSONException {
        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .unionPay(new TestConfigurationBuilder.TestUnionPayConfigurationBuilder()
                        .enabled(true))
                .build());
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(configuration)
                .unionPayCapabilitiesSuccess(UnionPayCapabilities.fromJson(Fixtures.UNIONPAY_CAPABILITIES_SUCCESS_RESPONSE))
                .enrollUnionPaySuccess(new UnionPayEnrollment("ennrollment-id", false))
                .unionPayTokenizeSuccess(CardNonce.fromJSON(new JSONObject(Fixtures.PAYMENT_METHODS_UNIONPAY_CREDIT_CARD)))
                .build();
        mActivity.setDropInRequest(new DropInRequest()
                .cardholderNameStatus(CardForm.FIELD_REQUIRED)
                .clientToken(base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN)));
        mActivity.mClientTokenPresent = true;
        setup(dropInClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, UNIONPAY_SMS_NOT_REQUIRED);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        setText(mEditCardView, R.id.bt_card_form_cvv, "123");
        setText(mEditCardView, R.id.bt_card_form_country_code, "86");
        setText(mEditCardView, R.id.bt_card_form_mobile_number, "8888888888");
        setText(mEditCardView, R.id.bt_card_form_cardholder_name, "Brian Tree");
        mEditCardView.findViewById(R.id.bt_button).performClick();

        assertTrue(mActivity.isFinishing());
        DropInResult result = mShadowActivity.getResultIntent()
                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        assertEquals(RESULT_OK, mShadowActivity.getResultCode());
        assertIsANonce(result.getPaymentMethodNonce().getString());
        assertEquals("85", ((CardNonce) result.getPaymentMethodNonce()).getLastTwo());
    }

    @Test
    public void unsupportedUnionPayCardsShowAnErrorMessage() throws JSONException {
        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .unionPay(new TestConfigurationBuilder.TestUnionPayConfigurationBuilder()
                        .enabled(true))
                .build());
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(configuration)
                .unionPayCapabilitiesSuccess(UnionPayCapabilities.fromJson(Fixtures.UNIONPAY_CAPABILITIES_NOT_SUPPORTED_RESPONSE))
                .build();
        mActivity.setDropInRequest(new DropInRequest()
                .clientToken(base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN)));
        mActivity.mClientTokenPresent = true;
        setup(dropInClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, UNIONPAY_DEBIT);
        mAddCardView.findViewById(R.id.bt_button).performClick();

        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_card_not_accepted),
                mAddCardView.getCardForm().getCardEditText().getTextInputLayoutParent().getError());
    }

    @Test
    public void enrollingAUnionPayCardRemainsOnEnrollmentCardView() throws JSONException {
        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .unionPay(new TestConfigurationBuilder.TestUnionPayConfigurationBuilder()
                        .enabled(true))
                .build());
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(configuration)
                .unionPayCapabilitiesSuccess(UnionPayCapabilities.fromJson(Fixtures.UNIONPAY_CAPABILITIES_SUCCESS_RESPONSE))
                .enrollUnionPaySuccess(new UnionPayEnrollment("enrollment-id", true))
                .build();

        mActivity.setDropInRequest(new DropInRequest()
                .clientToken(base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN)));
        mActivity.mClientTokenPresent = true;
        setup(dropInClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, UNIONPAY_CREDIT);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        setText(mEditCardView, R.id.bt_card_form_cvv, "123");
        setText(mEditCardView, R.id.bt_card_form_country_code, "86");
        setText(mEditCardView, R.id.bt_card_form_mobile_number, "8888888888");
        mEditCardView.findViewById(R.id.bt_button).performClick();
        setText(mEnrollmentCardView, R.id.bt_sms_code, "123456");
        mEnrollmentCardView.findViewById(R.id.bt_button).performClick();

        assertThat(mEnrollmentCardView).isVisible();
        assertThat(mEnrollmentCardView.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();
        assertThat(mEnrollmentCardView.findViewById(R.id.bt_button)).isGone();
        assertThat(mAddCardView).isGone();
        assertThat(mEditCardView).isGone();
    }

    @Test
    @Ignore("Investigate Union Pay testing strategy and determine if this is better as a UI test.")
    public void configurationChangeReturnsToEnrollmentView() {
//        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
//                .configuration(new TestConfigurationBuilder()
//                        .creditCards(getSupportedCardConfiguration())
//                        .unionPay(new TestUnionPayConfigurationBuilder()
//                                .enabled(true))
//                        .build())
//                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_CAPABILITIES_PATH, Fixtures.UNIONPAY_CAPABILITIES_SUCCESS_RESPONSE)
//                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_ENROLLMENT_PATH, Fixtures.UNIONPAY_ENROLLMENT_SMS_REQUIRED);
//        mActivity.setDropInRequest(new DropInRequest()
//                .clientToken(base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN)));
//        setup(httpClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, UNIONPAY_CREDIT);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        setText(mEditCardView, R.id.bt_card_form_cvv, "123");
        setText(mEditCardView, R.id.bt_card_form_country_code, "86");
        setText(mEditCardView, R.id.bt_card_form_mobile_number, "8888888888");
        mEditCardView.findViewById(R.id.bt_button).performClick();
        setText(mEnrollmentCardView, R.id.bt_sms_code, "12345");

        assertThat(mEnrollmentCardView).isVisible();
        assertThat((EditText) mEnrollmentCardView.findViewById(R.id.bt_sms_code))
                .hasTextString("12345");
        assertThat((TextView) mEnrollmentCardView.findViewById(R.id.bt_sms_sent_text))
                .hasText("Enter the SMS code sent to 868-888-888888");
        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher))
                .getDisplayedChild());
        assertThat(mAddCardView).isGone();
        assertThat(mEditCardView).isGone();

//        triggerConfigurationChange(httpClient);

        assertThat(mEnrollmentCardView).isVisible();
        assertThat(((EditText) mEnrollmentCardView.findViewById(R.id.bt_sms_code)))
                .hasTextString("12345");
        assertThat((TextView) mEnrollmentCardView.findViewById(R.id.bt_sms_sent_text))
                .hasText("Enter the SMS code sent to 868-888-888888");
        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher))
                .getDisplayedChild());
        assertThat(mAddCardView).isGone();
        assertThat(mEditCardView).isGone();
    }

    // TODO: call onActivityResult here to simulate a 3DS V2 user cancelation error
    @Test
    public void showsSubmitButtonAgainWhenThreeDSecureIsCanceled() throws PackageManager.NameNotFoundException, JSONException {
//        PackageManager packageManager = mockPackageManagerSupportsThreeDSecure();
//        Context context = spy(RuntimeEnvironment.application);
//        when(context.getPackageManager()).thenReturn(packageManager);
//        mActivity.context = context;
        mActivity.setDropInRequest(new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY)
                .amount("1.00")
                .requestThreeDSecureVerification(true));

        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .threeDSecureEnabled(true)
                .build());
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getConfigurationSuccess(configuration)
                .cardTokenizeSuccess(CardNonce.fromJSON(new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD)))
                .threeDSecureSuccess(ThreeDSecureResult.fromJson(Fixtures.THREE_D_SECURE_LOOKUP_RESPONSE))
                .shouldPerformThreeDSecureVerification(true)
                .build();
        setup(dropInClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        mEditCardView.findViewById(R.id.bt_button).performClick();

        Intent nextStartedActivity = shadowOf(mActivity).peekNextStartedActivity();
        assertEquals(Intent.ACTION_VIEW, nextStartedActivity.getAction());
        assertTrue(nextStartedActivity.getDataString().contains("com.braintreepayments.api.dropin.test.braintree"));

        assertThat(mEditCardView.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();
        assertThat(mEditCardView.findViewById(R.id.bt_button)).isGone();

        mActivity.onCancel(BraintreeRequestCodes.THREE_D_SECURE);

        assertThat(mEditCardView.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();
        assertThat(mEditCardView.findViewById(R.id.bt_button)).isVisible();
    }

    private void setup(DropInClient dropInClient) {
        mActivity.dropInClient = dropInClient;
        mActivityController.setup();
        setupViews();
    }

    private void setupViews() {
        mAddCardView = mActivity.findViewById(R.id.bt_add_card_view);
        mEditCardView = mActivity.findViewById(R.id.bt_edit_card_view);
        mEnrollmentCardView = mActivity.findViewById(R.id.bt_enrollment_card_view);
    }

    private static void setText(View view, int id, String text) {
        ((EditText) view.findViewById(id)).setText(text);
    }

    private void assertExceptionIsReturned(String analyticsEvent, Exception exception) {
        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        setup(dropInClient);
        mActivity.onError(exception);

        verify(dropInClient).sendAnalyticsEvent("sdk.exit." + analyticsEvent);
        assertTrue(mActivity.isFinishing());
        assertEquals(RESULT_FIRST_USER, mShadowActivity.getResultCode());
        Exception actualException = (Exception) mShadowActivity.getResultIntent()
                .getSerializableExtra(DropInActivity.EXTRA_ERROR);
        assertEquals(exception.getClass(), actualException.getClass());
        assertEquals(exception.getMessage(), actualException.getMessage());
    }

//    private void triggerConfigurationChange(BraintreeUnitTestHttpClient httpClient) {
//        Bundle bundle = new Bundle();
//        mActivityController.saveInstanceState(bundle)
//                .pause()
//                .stop()
//                .destroy();
//
//        mActivityController = Robolectric.buildActivity(AddCardUnitTestActivity.class);
//        mActivity = (AddCardUnitTestActivity) mActivityController.get();
//        mShadowActivity = shadowOf(mActivity);
//
//        mActivity.httpClient = httpClient;
//        mActivityController.setup(bundle);
//        mActivity.braintreeFragment.onAttach(mActivity);
//        mActivity.braintreeFragment.onResume();
//
//        setupViews();
//    }

    private TestConfigurationBuilder.TestCardConfigurationBuilder getSupportedCardConfiguration() {
        return new TestConfigurationBuilder.TestCardConfigurationBuilder()
                .supportedCardTypes(DropInPaymentMethodType.VISA.getCanonicalName(),
                        DropInPaymentMethodType.AMEX.getCanonicalName(),
                        DropInPaymentMethodType.UNIONPAY.getCanonicalName());
    }
}
