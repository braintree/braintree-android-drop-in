package com.braintreepayments.api.dropin.view;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;

/**
 * Created by pair on 4/4/16.
 */
public class FixedScrollView extends NestedScrollView {

    public FixedScrollView(Context context) {
        super(context);
    }

    public FixedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return false;
    }
}
