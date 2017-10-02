package com.braintreepayments.api.dropin;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.dropin.view.AddCardView;
import com.braintreepayments.api.dropin.view.EditCardView;
import com.braintreepayments.api.dropin.view.EnrollmentCardView;
import com.braintreepayments.api.exceptions.AuthenticationException;
import com.braintreepayments.api.exceptions.AuthorizationException;
import com.braintreepayments.api.exceptions.ConfigurationException;
import com.braintreepayments.api.exceptions.DownForMaintenanceException;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.exceptions.ServerException;
import com.braintreepayments.api.exceptions.UnexpectedException;
import com.braintreepayments.api.exceptions.UpgradeRequiredException;
import com.braintreepayments.api.models.BraintreeRequestCodes;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.test.ExpirationDate;
import com.braintreepayments.api.test.TestConfigurationBuilder;
import com.braintreepayments.api.test.TestConfigurationBuilder.TestUnionPayConfigurationBuilder;
import com.braintreepayments.api.threedsecure.ThreeDSecureWebViewActivity;
import com.braintreepayments.cardform.view.ErrorEditText;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;

import static com.braintreepayments.api.test.Assertions.assertIsANonce;
import static com.braintreepayments.api.test.CardNumber.AMEX;
import static com.braintreepayments.api.test.CardNumber.UNIONPAY_CREDIT;
import static com.braintreepayments.api.test.CardNumber.UNIONPAY_DEBIT;
import static com.braintreepayments.api.test.CardNumber.UNIONPAY_SMS_NOT_REQUIRED;
import static com.braintreepayments.api.test.CardNumber.VISA;
import static com.braintreepayments.api.test.PackageManagerUtils.mockPackageManagerWithThreeDSecureWebViewActivity;
import static com.braintreepayments.api.test.TestTokenizationKey.TOKENIZATION_KEY;
import static com.braintreepayments.api.test.UnitTestFixturesHelper.stringFromFixture;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.assertj.android.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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
    public void returnsExceptionWhenBraintreeFragmentSetupFails() {
        mActivity.setDropInRequest(new DropInRequest()
                .tokenizationKey("not a tokenization key"));

        mActivityController.setup();

        assertEquals(Activity.RESULT_FIRST_USER, mShadowActivity.getResultCode());
        Exception exception = (Exception) mShadowActivity.getResultIntent()
                .getSerializableExtra(DropInActivity.EXTRA_ERROR);
        assertTrue(exception instanceof InvalidArgumentException);
        assertEquals("Tokenization Key or client token was invalid.", exception.getMessage());
    }

    @Test
    public void returnsExceptionWhenAuthorizationIsEmpty() {
        mActivity.setDropInRequest(new DropInRequest()
                .tokenizationKey(null));

        mActivityController.setup();

        assertEquals(Activity.RESULT_FIRST_USER, mShadowActivity.getResultCode());
        Exception exception = (Exception) mShadowActivity.getResultIntent()
                .getSerializableExtra(DropInActivity.EXTRA_ERROR);
        assertTrue(exception instanceof InvalidArgumentException);
        assertEquals("A client token or tokenization key must be specified in the DropInRequest",
                exception.getMessage());
    }

    @Test
    public void sendsAnalyticsEventWhenStarted() {
        setup(mock(BraintreeFragment.class));

        verify(mActivity.braintreeFragment).sendAnalyticsEvent("card.selected");
    }

    @Test
    public void showsLoadingViewWhileWaitingForConfiguration() {
        setup(mock(BraintreeFragment.class));

        assertEquals(0, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher))
                .getDisplayedChild());
        assertThat(mAddCardView).isGone();
        assertThat(mEditCardView).isGone();
        assertThat(mEnrollmentCardView).isGone();
    }

    @Test
    public void setsTitleToCardDetailsWhenStarted() {
        setup(mock(BraintreeFragment.class));

        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_card_details),
                mActivity.getSupportActionBar().getTitle());
    }

    @Test
    public void tappingUpExitsActivityWithResultCanceled() {
        setup(mock(BraintreeFragment.class));

        mShadowActivity.clickMenuItem(android.R.id.home);

        assertTrue(mActivity.isFinishing());
        assertEquals(Activity.RESULT_CANCELED, mShadowActivity.getResultCode());
    }

    @Test
    public void pressingBackExitsActivityWithResultCanceled() {
        setup(mock(BraintreeFragment.class));

        mActivity.onBackPressed();

        assertTrue(mActivity.isFinishing());
        assertEquals(Activity.RESULT_CANCELED, mShadowActivity.getResultCode());
    }

    @Test
    public void showsAddCardViewAfterConfigurationIsFetched() {
        setup(new BraintreeUnitTestHttpClient().configuration(new TestConfigurationBuilder().build()));

        assertThat(mAddCardView).isVisible();
        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher))
                .getDisplayedChild());
        assertThat(mEditCardView).isGone();
        assertThat(mEnrollmentCardView).isGone();
    }

    @Test
    public void configurationChangeReturnsToAddCardView() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder().build());
        setup(httpClient);
        assertThat(mAddCardView).isVisible();
        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher))
                .getDisplayedChild());
        assertThat(mEditCardView).isGone();
        assertThat(mEnrollmentCardView).isGone();

        triggerConfigurationChange(httpClient);

        assertThat(mAddCardView).isVisible();
        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher))
                .getDisplayedChild());
        assertThat(mEditCardView).isGone();
        assertThat(mEnrollmentCardView).isGone();
    }

    @Test
    public void enteringACardNumberGoesToCardDetailsView() {
        setup(new BraintreeUnitTestHttpClient().configuration(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .build()));

        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);

        assertThat(mEditCardView).isVisible();
        assertThat(mAddCardView).isGone();
        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher))
                .getDisplayedChild());
        assertThat(mEnrollmentCardView).isGone();
    }

    @Test
    public void configurationChangeReturnsToEditCardView() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder()
                        .creditCards(getSupportedCardConfiguration())
                        .build());
        setup(httpClient);
        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
        assertThat(mEditCardView).isVisible();
        assertThat(mAddCardView).isGone();
        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher))
                .getDisplayedChild());
        assertThat(mEnrollmentCardView).isGone();

        triggerConfigurationChange(httpClient);

        assertThat(mEditCardView).isVisible();
        assertThat(mAddCardView).isGone();
        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher))
                .getDisplayedChild());
        assertThat(mEnrollmentCardView).isGone();
    }

    @Test
    public void editingANonUnionPayCardNumberIsPossible() {
        setup(new BraintreeUnitTestHttpClient().configuration(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .build()));

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
    public void editingAUnionPayCardNumberIsPossible() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder()
                        .creditCards(getSupportedCardConfiguration())
                        .unionPay(new TestUnionPayConfigurationBuilder()
                                .enabled(true))
                        .build())
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_CAPABILITIES_PATH,
                        stringFromFixture("responses/unionpay_capabilities_success_response.json"));
        mActivity.setDropInRequest(new DropInRequest()
                .clientToken(stringFromFixture("client_token.json")));
        setup(httpClient);

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
    public void addingACardRemainsOnEditCardView() {
        setup(new BraintreeUnitTestHttpClient().configuration(new TestConfigurationBuilder()
                .creditCards(getSupportedCardConfiguration())
                .build()));

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
    public void addingACardReturnsANonce() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder()
                        .creditCards(getSupportedCardConfiguration())
                        .build())
                .successResponse(BraintreeUnitTestHttpClient.TOKENIZE_CREDIT_CARD,
                        stringFromFixture("payment_methods/visa_credit_card.json"));
        setup(httpClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        mEditCardView.findViewById(R.id.bt_button).performClick();

        assertTrue(mActivity.isFinishing());
        DropInResult result = mShadowActivity.getResultIntent()
                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        assertEquals(Activity.RESULT_OK, mShadowActivity.getResultCode());
        assertIsANonce(result.getPaymentMethodNonce().getNonce());
        assertEquals("11", ((CardNonce) result.getPaymentMethodNonce()).getLastTwo());
    }

    @Test
    public void cardNumberValidationErrorsAreShownToTheUser() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder()
                        .creditCards(getSupportedCardConfiguration())
                        .challenges("cvv", "postal_code")
                        .build())
                .errorResponse(BraintreeUnitTestHttpClient.TOKENIZE_CREDIT_CARD, 422,
                        stringFromFixture("responses/credit_card_error_response.json"));
        mActivity.setDropInRequest(new DropInRequest()
                .clientToken(stringFromFixture("client_token.json")));
        setup(httpClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        setText(mEditCardView, R.id.bt_card_form_cvv, "123");
        setText(mEditCardView, R.id.bt_card_form_postal_code, "12345");
        mEditCardView.findViewById(R.id.bt_button).performClick();

        assertThat(mAddCardView).isVisible();
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_card_number_invalid),
                mAddCardView.getCardForm().getCardEditText().getTextInputLayoutParent().getError());

        assertThat(mEditCardView).isGone();
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_expiration_invalid),
                mEditCardView.getCardForm().getExpirationDateEditText().getTextInputLayoutParent().getError());
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_cvv_invalid,
                RuntimeEnvironment.application.getString(
                        mEditCardView.getCardForm().getCardEditText().getCardType()
                                .getSecurityCodeName())),
                mEditCardView.getCardForm().getCvvEditText().getTextInputLayoutParent().getError());
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_postal_code_invalid),
                mEditCardView.getCardForm().getPostalCodeEditText().getTextInputLayoutParent().getError());
    }

    @Test
    public void cardValidationErrorsAreShownToTheUser() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder()
                        .creditCards(getSupportedCardConfiguration())
                        .challenges("cvv", "postal_code")
                        .build())
                .errorResponse(BraintreeUnitTestHttpClient.TOKENIZE_CREDIT_CARD, 422,
                        stringFromFixture("responses/credit_card_non_number_error_response.json"));
        mActivity.setDropInRequest(new DropInRequest()
                .clientToken(stringFromFixture("client_token.json")));
        setup(httpClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        setText(mEditCardView, R.id.bt_card_form_cvv, "123");
        setText(mEditCardView, R.id.bt_card_form_postal_code, "12345");
        mEditCardView.findViewById(R.id.bt_button).performClick();

        assertThat(mAddCardView).isGone();
        assertNull(mAddCardView.getCardForm().getCardEditText().getTextInputLayoutParent().getError());

        assertThat(mEditCardView).isVisible();
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_expiration_invalid),
                mEditCardView.getCardForm().getExpirationDateEditText().getTextInputLayoutParent().getError());
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_cvv_invalid,
                RuntimeEnvironment.application.getString(
                        mEditCardView.getCardForm().getCardEditText().getCardType()
                                .getSecurityCodeName())),
                mEditCardView.getCardForm().getCvvEditText().getTextInputLayoutParent().getError());
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_postal_code_invalid),
                mEditCardView.getCardForm().getPostalCodeEditText().getTextInputLayoutParent().getError());
    }

    @Test
    public void unionPayValidationErrorsAreShownToTheUser() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder()
                        .creditCards(getSupportedCardConfiguration())
                        .unionPay(new TestUnionPayConfigurationBuilder()
                                .enabled(true))
                        .build())
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_CAPABILITIES_PATH,
                        stringFromFixture("responses/unionpay_capabilities_success_response.json"))
                .errorResponse(BraintreeUnitTestHttpClient.UNIONPAY_ENROLLMENT_PATH, 422,
                        stringFromFixture("responses/unionpay_enrollment_error_response.json"));
        mActivity.setDropInRequest(new DropInRequest()
                .clientToken(stringFromFixture("client_token.json")));
        setup(httpClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, UNIONPAY_CREDIT);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        setText(mEditCardView, R.id.bt_card_form_cvv, "123");
        setText(mEditCardView, R.id.bt_card_form_country_code, "123");
        setText(mEditCardView, R.id.bt_card_form_mobile_number, "12345678");
        mEditCardView.findViewById(R.id.bt_button).performClick();

        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_expiration_invalid),
                mEditCardView.getCardForm().getExpirationDateEditText().getTextInputLayoutParent().getError());
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_country_code_invalid),
                mEditCardView.getCardForm().getCountryCodeEditText().getTextInputLayoutParent().getError());
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_mobile_number_invalid),
                mEditCardView.getCardForm().getMobileNumberEditText().getTextInputLayoutParent().getError());
    }

    @Test
    public void smsCodeValidationErrorsAreShownToTheUser() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder()
                        .creditCards(getSupportedCardConfiguration())
                        .unionPay(new TestUnionPayConfigurationBuilder()
                                .enabled(true))
                        .build())
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_CAPABILITIES_PATH,
                        stringFromFixture("responses/unionpay_capabilities_success_response.json"))
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_ENROLLMENT_PATH,
                        stringFromFixture("responses/unionpay_enrollment_sms_required.json"))
                .errorResponse("", 422,
                        stringFromFixture("responses/unionpay_sms_code_error_response.json"));
        setup(httpClient);

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
        setup(mock(BraintreeFragment.class));

        mActivity.onPaymentMethodNonceCreated(CardNonce.fromJson(
                stringFromFixture("responses/visa_credit_card_response.json")));

        verify(mActivity.braintreeFragment).sendAnalyticsEvent("sdk.exit.success");
    }

    @Test
    public void configurationExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("configuration-exception",
                new ConfigurationException("Configuration exception"));
    }

    @Test
    public void authenticationExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("developer-error", new AuthenticationException("Access denied"));
    }

    @Test
    public void authorizationExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("developer-error", new AuthorizationException("Access denied"));
    }

    @Test
    public void upgradeRequiredExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("developer-error", new UpgradeRequiredException("Exception"));
    }

    @Test
    public void serverExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("server-error", new ServerException("Exception"));
    }

    @Test
    public void unexpectedExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("server-error", new UnexpectedException("Exception"));
    }

    @Test
    public void downForMaintenanceExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("server-unavailable",
                new DownForMaintenanceException("Exception"));
    }

    @Test
    public void enrollmentViewSetsTitleToConfirmEnrollment() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder()
                        .creditCards(getSupportedCardConfiguration())
                        .unionPay(new TestUnionPayConfigurationBuilder()
                                .enabled(true))
                        .build())
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_CAPABILITIES_PATH,
                        stringFromFixture("responses/unionpay_capabilities_success_response.json"))
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_ENROLLMENT_PATH,
                        stringFromFixture("responses/unionpay_enrollment_sms_required.json"));
        mActivity.setDropInRequest(new DropInRequest()
                .clientToken(stringFromFixture("client_token.json")));
        setup(httpClient);

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
    public void usingAUnionPayCardShowsMobileNumberFields() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder()
                        .creditCards(getSupportedCardConfiguration())
                        .unionPay(new TestUnionPayConfigurationBuilder()
                                .enabled(true))
                        .build())
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_CAPABILITIES_PATH,
                        stringFromFixture("responses/unionpay_capabilities_success_response.json"));
        mActivity.setDropInRequest(new DropInRequest()
                .clientToken(stringFromFixture("client_token.json")));
        setup(httpClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, UNIONPAY_CREDIT);
        mAddCardView.findViewById(R.id.bt_button).performClick();

        assertThat(mEditCardView).isVisible();
        assertThat(mEditCardView.findViewById(R.id.bt_card_form_country_code)).isVisible();
        assertThat(mEditCardView.findViewById(R.id.bt_card_form_mobile_number)).isVisible();
    }

    @Test
    public void addingAUnionPayCardShowsEnrollment() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder()
                        .creditCards(getSupportedCardConfiguration())
                        .unionPay(new TestUnionPayConfigurationBuilder()
                                .enabled(true))
                        .build())
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_CAPABILITIES_PATH,
                        stringFromFixture("responses/unionpay_capabilities_success_response.json"))
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_ENROLLMENT_PATH,
                        stringFromFixture("responses/unionpay_enrollment_sms_required.json"));
        mActivity.setDropInRequest(new DropInRequest()
                .clientToken(stringFromFixture("client_token.json")));
        setup(httpClient);

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
    public void addingAUnionPayCardDoesNotShowEnrollment() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder()
                        .creditCards(getSupportedCardConfiguration())
                        .unionPay(new TestUnionPayConfigurationBuilder()
                                .enabled(true))
                        .build())
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_CAPABILITIES_PATH,
                        stringFromFixture("responses/unionpay_capabilities_success_response.json"))
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_ENROLLMENT_PATH,
                        stringFromFixture("responses/unionpay_enrollment_sms_not_required.json"))
                .successResponse(BraintreeUnitTestHttpClient.TOKENIZE_CREDIT_CARD,
                        stringFromFixture("payment_methods/unionpay_credit_card.json"));
        mActivity.setDropInRequest(new DropInRequest()
                .clientToken(stringFromFixture("client_token.json")));
        setup(httpClient);

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
        assertEquals(Activity.RESULT_OK, mShadowActivity.getResultCode());
        assertIsANonce(result.getPaymentMethodNonce().getNonce());
        assertEquals("85", ((CardNonce) result.getPaymentMethodNonce()).getLastTwo());
    }

    @Test
    public void unsupportedUnionPayCardsShowAnErrorMessage() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder()
                        .unionPay(new TestUnionPayConfigurationBuilder()
                                .enabled(true))
                        .build())
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_CAPABILITIES_PATH,
                        stringFromFixture("responses/unionpay_capabilities_not_supported_response.json"));
        setup(httpClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, UNIONPAY_DEBIT);
        mAddCardView.findViewById(R.id.bt_button).performClick();

        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_card_not_accepted),
                mAddCardView.getCardForm().getCardEditText().getTextInputLayoutParent().getError());
    }

    @Test
    public void enrollingAUnionPayCardRemainsOnEnrollmentCardView() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder()
                        .creditCards(getSupportedCardConfiguration())
                        .unionPay(new TestUnionPayConfigurationBuilder()
                                .enabled(true))
                        .build())
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_CAPABILITIES_PATH,
                        stringFromFixture("responses/unionpay_capabilities_success_response.json"))
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_ENROLLMENT_PATH,
                        stringFromFixture("responses/unionpay_enrollment_sms_required.json"));
        mActivity.setDropInRequest(new DropInRequest()
                .clientToken(stringFromFixture("client_token.json")));
        setup(httpClient);

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
    public void configurationChangeReturnsToEnrollmentView() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder()
                        .creditCards(getSupportedCardConfiguration())
                        .unionPay(new TestUnionPayConfigurationBuilder()
                                .enabled(true))
                        .build())
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_CAPABILITIES_PATH,
                        stringFromFixture("responses/unionpay_capabilities_success_response.json"))
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_ENROLLMENT_PATH,
                        stringFromFixture("responses/unionpay_enrollment_sms_required.json"));
        mActivity.setDropInRequest(new DropInRequest()
                .clientToken(stringFromFixture("client_token.json")));
        setup(httpClient);

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

        triggerConfigurationChange(httpClient);

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

    @Test
    public void showsSubmitButtonAgainWhenThreeDSecureIsCanceled() throws PackageManager.NameNotFoundException {
        PackageManager packageManager = mockPackageManagerWithThreeDSecureWebViewActivity();
        Context context = spy(RuntimeEnvironment.application);
        when(context.getPackageManager()).thenReturn(packageManager);
        mActivity.context = context;
        mActivity.setDropInRequest(new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY)
                .amount("1.00")
                .requestThreeDSecureVerification(true));
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder()
                        .creditCards(getSupportedCardConfiguration())
                        .threeDSecureEnabled(true)
                        .build())
                .successResponse(BraintreeUnitTestHttpClient.TOKENIZE_CREDIT_CARD,
                        stringFromFixture("payment_methods/visa_credit_card.json"))
                .successResponse(BraintreeUnitTestHttpClient.THREE_D_SECURE_LOOKUP,
                        stringFromFixture("responses/three_d_secure_lookup_response.json"));

        setup(httpClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        mEditCardView.findViewById(R.id.bt_button).performClick();

        assertEquals(ThreeDSecureWebViewActivity.class.getName(),
                shadowOf(mActivity).peekNextStartedActivity().getComponent().getClassName());

        assertThat(mEditCardView.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();
        assertThat(mEditCardView.findViewById(R.id.bt_button)).isGone();

        mActivity.onCancel(BraintreeRequestCodes.THREE_D_SECURE);

        assertThat(mEditCardView.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();
        assertThat(mEditCardView.findViewById(R.id.bt_button)).isVisible();
    }

    private void setup(BraintreeFragment fragment) {
        mActivity.braintreeFragment = fragment;
        mActivityController.setup();
        setupViews();
    }

    private void setup(BraintreeUnitTestHttpClient httpClient) {
        mActivity.httpClient = httpClient;
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
        mActivity.onError(exception);

        verify(mActivity.braintreeFragment).sendAnalyticsEvent("sdk.exit." + analyticsEvent);
        assertTrue(mActivity.isFinishing());
        assertEquals(Activity.RESULT_FIRST_USER, mShadowActivity.getResultCode());
        Exception actualException = (Exception) mShadowActivity.getResultIntent()
                .getSerializableExtra(DropInActivity.EXTRA_ERROR);
        assertEquals(exception.getClass(), actualException.getClass());
        assertEquals(exception.getMessage(), actualException.getMessage());
    }

    private void triggerConfigurationChange(BraintreeUnitTestHttpClient httpClient) {
        Bundle bundle = new Bundle();
        mActivityController.saveInstanceState(bundle)
                .pause()
                .stop()
                .destroy();

        mActivityController = Robolectric.buildActivity(AddCardUnitTestActivity.class);
        mActivity = (AddCardUnitTestActivity) mActivityController.get();
        mShadowActivity = shadowOf(mActivity);

        mActivity.httpClient = httpClient;
        mActivityController.setup(bundle);
        mActivity.braintreeFragment.onAttach(mActivity);
        mActivity.braintreeFragment.onResume();

        setupViews();
    }

    private TestConfigurationBuilder.TestCardConfigurationBuilder getSupportedCardConfiguration() {
        return new TestConfigurationBuilder.TestCardConfigurationBuilder()
                .supportedCardTypes(PaymentMethodType.VISA.getCanonicalName(),
                        PaymentMethodType.AMEX.getCanonicalName(),
                        PaymentMethodType.UNIONPAY.getCanonicalName());
    }
}
