package com.braintreepayments.api;

import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

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

    private AnimatedButtonView mView;

    @Before
    public void setup() {
        ActivityController<FragmentActivity> activityController =
            Robolectric.buildActivity(FragmentActivity.class);
        FragmentActivity activity = activityController.get();

        AttributeSet attributeSet = Robolectric.buildAttributeSet().build();
        mView = new AnimatedButtonView(activity, attributeSet);
    }

    @Test
    public void showsButtonByDefault() {
        assertThat((View) mView.findViewById(R.id.bt_button)).isVisible();
        assertThat((View) mView.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();
    }

    @Test
    public void onClick_showsLoadingView() {
        mView.onClick(null);

        assertThat((View) mView.findViewById(R.id.bt_button)).isGone();
        assertThat((View) mView.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();
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
        assertThat((View) mView.findViewById(R.id.bt_button)).isVisible();
        assertThat((View) mView.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();

        mView.showLoading();

        assertThat((View) mView.findViewById(R.id.bt_button)).isGone();
        assertThat((View) mView.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();
    }

    @Test
    public void showButton_showsButton() {
        mView.showLoading();
        assertThat((View) mView.findViewById(R.id.bt_button)).isGone();
        assertThat((View) mView.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();

        mView.showButton();

        assertThat((View) mView.findViewById(R.id.bt_button)).isVisible();
        assertThat((View) mView.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();
    }
}
