package com.braintreepayments.api.dropin.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ViewAnimator;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.interfaces.AddPaymentUpdateListener;

public class EditCardView extends RelativeLayout {

    private EditText mCardNumber;
    private EditText mExpirationDate;
    private EditText mCvv;
    private View mUnionPayGroup;
    private EditText mPhoneNumber;
    private ViewAnimator mViewAnimator;
    private Button mNext;
    private ProgressBar mProgressBar;

    private AddPaymentUpdateListener mListener;

    public EditCardView(Context context) {
        super(context);
        init(context);
    }

    public EditCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EditCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        if (isInEditMode()) {
            return;
        }
        LayoutInflater.from(context).inflate(R.layout.bt_edit_card, this);

        mCardNumber = (EditText)findViewById(R.id.card_number);
        mExpirationDate = (EditText)findViewById(R.id.expiration_date);
        mCvv = (EditText)findViewById(R.id.cvv);
        mUnionPayGroup = findViewById(R.id.union_pay_group);
        mPhoneNumber = (EditText)findViewById(R.id.phone_number);
        mViewAnimator = (ViewAnimator)findViewById(R.id.view_animator);
        mNext = (Button)findViewById(R.id.add_card_button);
        mProgressBar = (ProgressBar)findViewById(R.id.progress_bar);

        Animation fadeIn = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        Animation fadeOut = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        mViewAnimator.setInAnimation(fadeIn);
        mViewAnimator.setOutAnimation(fadeOut);
        mNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewAnimator.showNext();
                if (mListener != null) {
                    mListener.onPaymentUpdated(EditCardView.this);
                }
            }
        });
    }

    public void setAddPaymentUpdatedListener(AddPaymentUpdateListener listener) {
        mListener = listener;
    }

    public String getCvv() {
        return mCvv.getText().toString();
    }

    public String getExpirationDate() {
        return mExpirationDate.getText().toString();
    }

    public String getPhoneNumber() {
        return mPhoneNumber.getText().toString();
    }

    public void setCardNumber(String cardNumber) {
        mCardNumber.setText(cardNumber);
    }

    public void useUnionPay(boolean useUnionPay) {
        mUnionPayGroup.setVisibility(useUnionPay? VISIBLE : GONE);
    }
}
