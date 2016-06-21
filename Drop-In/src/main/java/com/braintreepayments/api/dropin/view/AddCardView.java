package com.braintreepayments.api.dropin.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.interfaces.AddPaymentUpdateListener;

public class AddCardView extends LinearLayout {

    private EditText mCardNumber;
    private Button mNext;
    private AddPaymentUpdateListener mListener;

    public AddCardView(Context context) {
        super(context);
        init(context);
    }

    public AddCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AddCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        if(isInEditMode()) {
            return;
        }
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.bt_add_card, this, true);
        mCardNumber = (EditText)findViewById(R.id.card_number);
        mNext = (Button)findViewById(R.id.next_button);
        mNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onPaymentUpdated(AddCardView.this);
                }
            }
        });
    }

    public String getNumber() {
        return mCardNumber.getText().toString();
    }

    public void setAddPaymentUpdatedListener(AddPaymentUpdateListener listener) {
        mListener = listener;
    }
}
