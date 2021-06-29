package com.braintreepayments.api;

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
import static com.braintreepayments.api.TestTokenizationKey.TOKENIZATION_KEY;
import static com.braintreepayments.api.UnitTestFixturesHelper.base64EncodedClientTokenFromFixture;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.assertj.android.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
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
