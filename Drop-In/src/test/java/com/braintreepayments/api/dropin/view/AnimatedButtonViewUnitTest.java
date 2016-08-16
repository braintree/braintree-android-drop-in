package com.braintreepayments.api.dropin.view;

import android.app.Activity;
import android.view.View.OnClickListener;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.test.UnitTestActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import static org.assertj.android.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricGradleTestRunner.class)
public class AnimatedButtonViewUnitTest {

    private AnimatedButtonView mView;

    @Before
    public void setup() {
        UnitTestActivity.view = R.layout.bt_add_card_activity;
        Activity activity = Robolectric.buildActivity(UnitTestActivity.class)
                .setup()
                .get();
        mView = (AnimatedButtonView) activity.findViewById(R.id.bt_animated_button_view);
    }

    @Test
    public void showsButtonByDefault() {
        assertThat(mView.findViewById(R.id.bt_button)).isVisible();
        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();
    }

    @Test
    public void onClick_showsLoadingView() {
        mView.onClick(null);

        assertThat(mView.findViewById(R.id.bt_button)).isGone();
        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();
    }

    @Test
    public void onClick_doesNothingIfNoListenerSet() {
        mView.onClick(null);
    }

    @Test
    public void onClick_callsOnClickListener() {
        OnClickListener listener = mock(OnClickListener.class);
        mView.setClickListener(listener);

        mView.onClick(null);

        verify(listener).onClick(mView);
    }

    @Test
    public void showLoading_showsLoading() {
        assertThat(mView.findViewById(R.id.bt_button)).isVisible();
        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();

        mView.showLoading();

        assertThat(mView.findViewById(R.id.bt_button)).isGone();
        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();
    }

    @Test
    public void showButton_showsButton() {
        mView.showLoading();
        assertThat(mView.findViewById(R.id.bt_button)).isGone();
        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();

        mView.showButton();

        assertThat(mView.findViewById(R.id.bt_button)).isVisible();
        assertThat(mView.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();
    }
}
