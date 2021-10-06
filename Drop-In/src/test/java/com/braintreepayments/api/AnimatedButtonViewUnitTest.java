package com.braintreepayments.api;

import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.fragment.app.FragmentActivity;

import com.braintreepayments.api.dropin.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import static org.assertj.android.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class AnimatedButtonViewUnitTest {

    private AnimatedButtonView view;

    @Before
    public void setup() {
        ActivityController<FragmentActivity> activityController =
            Robolectric.buildActivity(FragmentActivity.class);
        FragmentActivity activity = activityController.get();

        AttributeSet attributeSet = Robolectric.buildAttributeSet().build();
        view = new AnimatedButtonView(activity, attributeSet);
    }

    @Test
    public void showsButtonByDefault() {
        assertThat((View) view.findViewById(R.id.bt_button)).isVisible();
        assertThat((View) view.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();
    }

    @Test
    public void onClick_showsLoadingView() {
        view.onClick(null);

        assertThat((View) view.findViewById(R.id.bt_button)).isGone();
        assertThat((View) view.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();
    }

    @Test
    public void onClick_doesNothingIfNoListenerSet() {
        view.onClick(null);
    }

    @Test
    public void onClick_callsOnClickListener() {
        OnClickListener listener = mock(OnClickListener.class);
        view.setClickListener(listener);

        view.onClick(null);

        verify(listener).onClick(view);
    }

    @Test
    public void showLoading_showsLoading() {
        assertThat((View) view.findViewById(R.id.bt_button)).isVisible();
        assertThat((View) view.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();

        view.showLoading();

        assertThat((View) view.findViewById(R.id.bt_button)).isGone();
        assertThat((View) view.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();
    }

    @Test
    public void showButton_showsButton() {
        view.showLoading();
        assertThat((View) view.findViewById(R.id.bt_button)).isGone();
        assertThat((View) view.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();

        view.showButton();

        assertThat((View) view.findViewById(R.id.bt_button)).isVisible();
        assertThat((View) view.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();
    }
}
