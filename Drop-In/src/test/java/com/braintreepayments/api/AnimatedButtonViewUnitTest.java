package com.braintreepayments.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentActivity;

import com.braintreepayments.api.dropin.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

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
        assertEquals(getViewVisibility(R.id.bt_button), View.VISIBLE);
        assertEquals(getViewVisibility(R.id.bt_animated_button_loading_indicator), View.GONE);
    }

    @Test
    public void onClick_showsLoadingView() {
        view.onClick(null);

        assertEquals(getViewVisibility(R.id.bt_button), View.GONE);
        assertEquals(getViewVisibility(R.id.bt_animated_button_loading_indicator), View.VISIBLE);
    }

    @Test
    public void onClick_doesNothingIfNoListenerSet() {
        view.onClick(null);
    }

    @Test
    public void onClick_whenLoaderIsHidden_callsOnClickListener() {
        OnClickListener listener = mock(OnClickListener.class);
        view.setClickListener(listener);

        view.onClick(null);

        verify(listener).onClick(view);
    }

    @Test
    public void onClick_whenLoaderIsVisible_doesNotCallOnClickListener() {
        view.showLoading();

        OnClickListener listener = mock(OnClickListener.class);
        view.setClickListener(listener);
        view.onClick(null);

        verify(listener, never()).onClick(view);
    }

    @Test
    public void showLoading_showsLoading() {
        assertEquals(getViewVisibility(R.id.bt_button), View.VISIBLE);
        assertEquals(getViewVisibility(R.id.bt_animated_button_loading_indicator), View.GONE);

        view.showLoading();

        assertEquals(getViewVisibility(R.id.bt_button), View.GONE);
        assertEquals(getViewVisibility(R.id.bt_animated_button_loading_indicator), View.VISIBLE);
    }

    @Test
    public void showButton_showsButton() {
        view.showLoading();
        assertEquals(getViewVisibility(R.id.bt_button), View.GONE);
        assertEquals(getViewVisibility(R.id.bt_animated_button_loading_indicator), View.VISIBLE);

        view.showButton();

        assertEquals(getViewVisibility(R.id.bt_button), View.VISIBLE);
        assertEquals(getViewVisibility(R.id.bt_animated_button_loading_indicator), View.GONE);
    }

    private int getViewVisibility(@IdRes int viewResId) {
        return view.findViewById(viewResId).getVisibility();
    }
}
