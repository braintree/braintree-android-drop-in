package com.braintreepayments.api.dropin;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.Card;
import com.braintreepayments.api.ThreeDSecure;
import com.braintreepayments.api.dropin.view.EditCardView;
import com.braintreepayments.api.models.CardBuilder;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.cardform.view.CardForm;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import static com.braintreepayments.api.test.ReflectionHelper.setField;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(RobolectricGradleTestRunner.class)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*" })
@PrepareForTest({ ThreeDSecure.class, Card.class })
public class AddCardActivityPowerMockTest {

    @Rule
    public PowerMockRule mPowerMockRule = new PowerMockRule();

    private AddCardUnitTestActivity mActivity;
    private BraintreeFragment mFragment;

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        mFragment = mock(BraintreeFragment.class);

        mActivity = Robolectric.buildActivity(AddCardUnitTestActivity.class).get();

        setField(AddCardActivity.class, mActivity, "mBraintreeFragment", mFragment);

        EditCardView editCardView = mock(EditCardView.class);
        when(editCardView.getCardForm()).thenReturn(mock(CardForm.class));
        setField(AddCardActivity.class, mActivity, "mEditCardView", editCardView);

        mockStatic(ThreeDSecure.class);
        ThreeDSecure.performVerification(any(BraintreeFragment.class), any(CardBuilder.class),
                anyString());
        mockStatic(Card.class);
        Card.tokenize(any(BraintreeFragment.class), any(CardBuilder.class));
    }

    @Test
    public void skipsThreeDSecureWhenNotRequested() throws NoSuchFieldException,
            IllegalAccessException {
        setConfiguration(new DropInRequest(), mock(Configuration.class));

        mActivity.createCard();

        verifyStatic(never());
        ThreeDSecure.performVerification(eq(mFragment), any(CardBuilder.class), anyString());
        verifyStatic();
        Card.tokenize(eq(mFragment), any(CardBuilder.class));
    }

    @Test
    public void skipsThreeDSecureWhenRequestedButNotEnabled()
            throws NoSuchFieldException, IllegalAccessException {
        DropInRequest dropInRequest = new DropInRequest()
                .amount("1.00")
                .requestThreeDSecureVerification(true);
        Configuration configuration = mock(Configuration.class);
        setConfiguration(dropInRequest, configuration);

        mActivity.createCard();

        verifyStatic(never());
        ThreeDSecure.performVerification(eq(mFragment), any(CardBuilder.class), anyString());
        verifyStatic();
        Card.tokenize(eq(mFragment), any(CardBuilder.class));
    }

    @Test
    public void skipsThreeDSecureWhenAmountMissing() throws NoSuchFieldException,
            IllegalAccessException {
        DropInRequest dropInRequest = new DropInRequest()
                .requestThreeDSecureVerification(true);
        Configuration configuration = mock(Configuration.class);
        when(configuration.isThreeDSecureEnabled()).thenReturn(true);
        setConfiguration(dropInRequest, configuration);

        mActivity.createCard();

        verifyStatic(never());
        ThreeDSecure.performVerification(eq(mFragment), any(CardBuilder.class), anyString());
        verifyStatic();
        Card.tokenize(eq(mFragment), any(CardBuilder.class));
    }

    @Test
    public void performsThreeDSecureVerificationWhenRequestedEnableAndAmountPresent()
            throws NoSuchFieldException, IllegalAccessException {
        DropInRequest dropInRequest = new DropInRequest()
                .amount("1.00")
                .requestThreeDSecureVerification(true);
        Configuration configuration = mock(Configuration.class);
        when(configuration.isThreeDSecureEnabled()).thenReturn(true);
        setConfiguration(dropInRequest, configuration);

        mActivity.createCard();

        verifyStatic();
        ThreeDSecure.performVerification(eq(mFragment), any(CardBuilder.class), anyString());
        verifyStatic(never());
        Card.tokenize(eq(mFragment), any(CardBuilder.class));
    }

    private void setConfiguration(DropInRequest dropInRequest, Configuration configuration)
            throws NoSuchFieldException, IllegalAccessException {
        setField(AddCardActivity.class, mActivity, "mDropInRequest", dropInRequest);
        setField(AddCardActivity.class, mActivity, "mConfiguration", configuration);
    }
}
