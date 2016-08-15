package com.braintreepayments.api;

import android.app.Activity;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.EditText;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.view.AddCardView;
import com.braintreepayments.api.dropin.view.EditCardView;
import com.braintreepayments.api.dropin.view.EnrollmentCardView;
import com.braintreepayments.api.exceptions.AuthenticationException;
import com.braintreepayments.api.exceptions.AuthorizationException;
import com.braintreepayments.api.exceptions.ConfigurationException;
import com.braintreepayments.api.exceptions.DownForMaintenanceException;
import com.braintreepayments.api.exceptions.ServerException;
import com.braintreepayments.api.exceptions.UnexpectedException;
import com.braintreepayments.api.exceptions.UpgradeRequiredException;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.test.ExpirationDate;
import com.braintreepayments.api.test.TestConfigurationBuilder;
import com.braintreepayments.api.test.TestConfigurationBuilder.TestUnionPayConfigurationBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.util.ActivityController;

import static com.braintreepayments.api.test.Assertions.assertIsANonce;
import static com.braintreepayments.api.test.CardNumber.AMEX;
import static com.braintreepayments.api.test.CardNumber.UNIONPAY_CREDIT;
import static com.braintreepayments.api.test.CardNumber.UNIONPAY_DEBIT;
import static com.braintreepayments.api.test.CardNumber.UNIONPAY_SMS_NOT_REQUIRED;
import static com.braintreepayments.api.test.CardNumber.VISA;
import static com.braintreepayments.api.test.UnitTestFixturesHelper.stringFromFixture;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.assertj.android.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
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
        mActivity.paymentRequest = new PaymentRequest()
                .tokenizationKey("not a tokenization key");

        mActivityController.setup();

        assertEquals(BraintreePaymentActivity.BRAINTREE_RESULT_DEVELOPER_ERROR, mShadowActivity.getResultCode());
        assertEquals("Tokenization Key or client token was invalid.", mShadowActivity.getResultIntent()
                .getStringExtra(BraintreePaymentActivity.EXTRA_ERROR_MESSAGE));
    }

    @Test
    public void returnsExceptionWhenAuthorizationIsEmpty() {
        mActivity.paymentRequest = new PaymentRequest()
                .tokenizationKey(null);

        mActivityController.setup();

        assertEquals(BraintreePaymentActivity.BRAINTREE_RESULT_DEVELOPER_ERROR, mShadowActivity.getResultCode());
        assertEquals("A client token or client key must be specified in the PaymentRequest", mShadowActivity.getResultIntent()
                .getStringExtra(BraintreePaymentActivity.EXTRA_ERROR_MESSAGE));
    }

    @Test
    public void showsLoadingViewWhileWaitingForConfiguration() {
        setup(mock(BraintreeFragment.class));

        assertThat(mActivity.findViewById(R.id.bt_progress_bar)).isVisible();
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
        assertThat(mActivity.findViewById(R.id.bt_progress_bar)).isGone();
        assertThat(mEditCardView).isGone();
        assertThat(mEnrollmentCardView).isGone();
    }

    @Test
    public void enteringACardNumberGoesToCardDetailsView() {
        setup(new BraintreeUnitTestHttpClient().configuration(new TestConfigurationBuilder().build()));

        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);

        assertThat(mEditCardView).isVisible();
        assertThat(mAddCardView).isGone();
        assertThat(mActivity.findViewById(R.id.bt_progress_bar)).isGone();
        assertThat(mEnrollmentCardView).isGone();
    }

    @Test
    public void editingANonUnionPayCardNumberIsPossible() {
        setup(new BraintreeUnitTestHttpClient().configuration(new TestConfigurationBuilder().build()));

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
                        .unionPay(new TestUnionPayConfigurationBuilder()
                                .enabled(true))
                        .build())
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_CAPABILITIES_PATH,
                        stringFromFixture("responses/unionpay_capabilities_success_response.json"));
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
        setup(new BraintreeUnitTestHttpClient().configuration(new TestConfigurationBuilder().build()));

        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        mEditCardView.findViewById(R.id.bt_button).performClick();

        assertThat(mEditCardView).isVisible();
        assertThat(mEditCardView.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();
        assertThat(mEditCardView.findViewById(R.id.bt_button)).isGone();
        assertThat(mActivity.findViewById(R.id.bt_progress_bar)).isGone();
        assertThat(mAddCardView).isGone();
        assertThat(mEnrollmentCardView).isGone();
    }

    @Test
    public void addingACardReturnsANonce() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder().build())
                .successResponse(BraintreeUnitTestHttpClient.TOKENIZE_CREDIT_CARD,
                        stringFromFixture("payment_methods/visa_credit_card.json"));
        setup(httpClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        mEditCardView.findViewById(R.id.bt_button).performClick();

        assertTrue(mActivity.isFinishing());
        PaymentMethodNonce nonce = mShadowActivity.getResultIntent()
                .getParcelableExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE);
        assertEquals(Activity.RESULT_OK, mShadowActivity.getResultCode());
        assertIsANonce(nonce.getNonce());
        assertEquals("11", ((CardNonce) nonce).getLastTwo());
    }

    @Test
    public void cardValidationErrorsAreShownToTheUser() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder()
                        .challenges("cvv", "postal_code")
                        .build())
                .errorResponse(BraintreeUnitTestHttpClient.TOKENIZE_CREDIT_CARD, 422,
                        stringFromFixture("responses/credit_card_error_response.json"));
        setup(httpClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        setText(mEditCardView, R.id.bt_card_form_cvv, "123");
        setText(mEditCardView, R.id.bt_card_form_postal_code, "12345");
        mEditCardView.findViewById(R.id.bt_button).performClick();

        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_card_number_invalid),
                ((TextInputLayout) mEditCardView.findViewById(R.id.bt_card_form_card_number).getParent()).getError());
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_expiration_invalid),
                ((TextInputLayout) mEditCardView.findViewById(R.id.bt_card_form_expiration).getParent()).getError());
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_cvv_invalid),
                ((TextInputLayout) mEditCardView.findViewById(R.id.bt_card_form_cvv).getParent()).getError());
        assertEquals(RuntimeEnvironment.application.getString(R.string.bt_postal_code_invalid),
                ((TextInputLayout) mEditCardView.findViewById(R.id.bt_card_form_postal_code).getParent()).getError());
    }

    @Test
    public void configurationExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("configuration-exception", BraintreePaymentActivity.BRAINTREE_RESULT_SERVER_ERROR,
                new ConfigurationException("Configuration exception"));
    }

    @Test
    public void authenticationExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("developer-error", BraintreePaymentActivity.BRAINTREE_RESULT_DEVELOPER_ERROR,
                new AuthenticationException("Access denied"));
    }

    @Test
    public void authorizationExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("developer-error", BraintreePaymentActivity.BRAINTREE_RESULT_DEVELOPER_ERROR,
                new AuthorizationException("Access denied"));
    }

    @Test
    public void upgradeRequiredExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("developer-error", BraintreePaymentActivity.BRAINTREE_RESULT_DEVELOPER_ERROR,
                new UpgradeRequiredException("Exception"));
    }

    @Test
    public void serverExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("server-error", BraintreePaymentActivity.BRAINTREE_RESULT_SERVER_ERROR,
                new ServerException("Exception"));
    }

    @Test
    public void unexpectedExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("server-error", BraintreePaymentActivity.BRAINTREE_RESULT_SERVER_ERROR,
                new UnexpectedException("Exception"));
    }

    @Test
    public void downForMaintenanceExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("server-unavailable", BraintreePaymentActivity.BRAINTREE_RESULT_SERVER_UNAVAILABLE,
                new DownForMaintenanceException("Exception"));
    }

    @Test
    public void enrollmentViewSetsTitleToConfirmEnrollment() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder()
                        .unionPay(new TestUnionPayConfigurationBuilder()
                                .enabled(true))
                        .build())
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_CAPABILITIES_PATH,
                        stringFromFixture("responses/unionpay_capabilities_success_response.json"))
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_ENROLLMENT_PATH,
                        stringFromFixture("responses/unionpay_enrollment_sms_required.json"));
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
                        .unionPay(new TestUnionPayConfigurationBuilder()
                                .enabled(true))
                        .build())
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_CAPABILITIES_PATH,
                        stringFromFixture("responses/unionpay_capabilities_success_response.json"));
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
                        .unionPay(new TestUnionPayConfigurationBuilder()
                                .enabled(true))
                        .build())
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_CAPABILITIES_PATH,
                        stringFromFixture("responses/unionpay_capabilities_success_response.json"))
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_ENROLLMENT_PATH,
                        stringFromFixture("responses/unionpay_enrollment_sms_required.json"));
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
                        .unionPay(new TestUnionPayConfigurationBuilder()
                                .enabled(true))
                        .build())
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_CAPABILITIES_PATH,
                        stringFromFixture("responses/unionpay_capabilities_success_response.json"))
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_ENROLLMENT_PATH,
                        stringFromFixture("responses/unionpay_enrollment_sms_not_required.json"))
                .successResponse(BraintreeUnitTestHttpClient.TOKENIZE_CREDIT_CARD,
                        stringFromFixture("payment_methods/unionpay_credit_card.json"));
        setup(httpClient);

        setText(mAddCardView, R.id.bt_card_form_card_number, UNIONPAY_SMS_NOT_REQUIRED);
        mAddCardView.findViewById(R.id.bt_button).performClick();
        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
        setText(mEditCardView, R.id.bt_card_form_cvv, "123");
        setText(mEditCardView, R.id.bt_card_form_country_code, "86");
        setText(mEditCardView, R.id.bt_card_form_mobile_number, "8888888888");
        mEditCardView.findViewById(R.id.bt_button).performClick();

        assertTrue(mActivity.isFinishing());
        PaymentMethodNonce nonce = mShadowActivity.getResultIntent()
                .getParcelableExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE);
        assertEquals(Activity.RESULT_OK, mShadowActivity.getResultCode());
        assertIsANonce(nonce.getNonce());
        assertEquals("85", ((CardNonce) nonce).getLastTwo());
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
                ((TextInputLayout) mAddCardView.findViewById(R.id.bt_card_form_card_number).getParent()).getError());
    }

    @Test
    public void enrollingAUnionPayCardRemainsOnEnrollmentCardView() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder()
                        .unionPay(new TestUnionPayConfigurationBuilder()
                                .enabled(true))
                        .build())
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_CAPABILITIES_PATH,
                        stringFromFixture("responses/unionpay_capabilities_success_response.json"))
                .successResponse(BraintreeUnitTestHttpClient.UNIONPAY_ENROLLMENT_PATH,
                        stringFromFixture("responses/unionpay_enrollment_sms_required.json"));
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
        mAddCardView = (AddCardView) mActivity.findViewById(R.id.bt_add_card_view);
        mEditCardView = (EditCardView) mActivity.findViewById(R.id.bt_edit_card_view);
        mEnrollmentCardView = (EnrollmentCardView) mActivity.findViewById(R.id.bt_enrollment_card_view);
    }

    private static void setText(View view, int id, String text) {
        ((EditText) view.findViewById(id)).setText(text);
    }

    private void assertExceptionIsReturned(String analyticsEvent, int responseCode, Exception exception) {
        mActivity.onError(exception);

        verify(mActivity.braintreeFragment).sendAnalyticsEvent("sdk.exit." + analyticsEvent);
        assertTrue(mActivity.isFinishing());
        assertEquals(responseCode, mShadowActivity.getResultCode());
        Exception actualException = (Exception) mShadowActivity.getResultIntent()
                .getSerializableExtra(BraintreePaymentActivity.EXTRA_ERROR_MESSAGE);
        assertEquals(exception.getClass(), actualException.getClass());
        assertEquals(exception.getMessage(), actualException.getMessage());
    }
}
