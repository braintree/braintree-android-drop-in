package com.braintreepayments.api.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.Button;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.interfaces.AddPaymentUpdateListener;
import com.braintreepayments.api.dropin.view.AddCardView;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.test.TestConfigurationBuilder;
import com.braintreepayments.api.test.TestConfigurationBuilder.TestUnionPayConfigurationBuilder;
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
import static com.braintreepayments.api.test.TestConfigurationBuilder.basicConfig;
import static junit.framework.Assert.assertEquals;
import static org.assertj.android.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricGradleTestRunner.class)
public class AddCardViewUnitTest {

    private ActivityController mActivityController;
    private Activity mActivity;
    private AddCardView mView;

    @Before
    public void setup() {
        UnitTestActivity.view = R.layout.bt_add_card_activity;
        mActivityController = Robolectric.buildActivity(UnitTestActivity.class);
        mActivity = (Activity) mActivityController.setup().get();
        mView = (AddCardView) mActivity.findViewById(R.id.bt_add_card_view);
        mView.setup(mActivity, (Configuration) basicConfig());
    }

    @Test
    public void buttonTextIsAddCard() {
        assertThat((Button) mView.findViewById(R.id.bt_button)).hasText(R.string.bt_next);
    }

    @Test
    public void setup_hidesNextButtonIfUnionPayIsNotEnabled() {
        mView.setup(mActivity, (Configuration) basicConfig());

        assertThat(mView.findViewById(R.id.bt_animated_button_view)).isGone();
    }

    @Test
    public void setup_showsNextButtonIfUnionPayIsEnabled() {
        Configuration configuration = new TestConfigurationBuilder()
                .unionPay(new TestUnionPayConfigurationBuilder()
                        .enabled(true))
                .buildConfiguration();

        mView.setup(mActivity, configuration);

        assertThat(mView.findViewById(R.id.bt_animated_button_view)).isVisible();
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
                ((TextInputLayout) mView.getCardForm().getCardEditText().getParent()).getError());
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
        mActivity = (Activity) mActivityController.get();
        mView = (AddCardView) mActivity.findViewById(R.id.bt_add_card_view);

        assertEquals(VISA, mView.getCardForm().getCardNumber());
    }

    @Test
    public void onCardFormValid_showLoadingIndicatorAndCallsListenerWhenTrue() {
        AddPaymentUpdateListener listener = mock(AddPaymentUpdateListener.class);
        mView.setAddPaymentUpdatedListener(listener);

        mView.onCardFormValid(true);

        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();
        verify(listener).onPaymentUpdated(mView);
    }
    @Test
    public void onCardFormValid_doesNothingWhenFalse() {
        AddPaymentUpdateListener listener = mock(AddPaymentUpdateListener.class);
        mView.setAddPaymentUpdatedListener(listener);

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
                ((TextInputLayout) mView.getCardForm().getCardEditText().getParent()).getError());
    }
}
