package com.braintreepayments.api.dropin.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;

public class PaymentMethodDrawable extends Drawable {

    private Drawable mPaymentMethodDrawableResource;
    private Paint mBorderPaint;
    private Paint mBackgroundPaint;
    private int mBorderWidth;
    private Context mContext;

    public PaymentMethodDrawable(Context context, PaymentMethodType paymentMethodType) {
        mPaymentMethodDrawableResource = context.getResources().getDrawable(paymentMethodType.getDrawable());
        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setColor(context.getResources().getColor(R.color.bt_light_border_color));
        mBorderWidth = context.getResources().getDimensionPixelSize(R.dimen.bt_payment_method_border_width);
        mBorderPaint.setStrokeWidth(mBorderWidth);
        mBorderPaint.setStyle(Style.STROKE);

        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Style.FILL);
        mBackgroundPaint.setColor(context.getResources().getColor(android.R.color.white));
        mContext = context;
    }

    @Override
    public void draw(Canvas canvas) {
        int cornerRadius = mContext.getResources().getDimensionPixelSize(R.dimen.bt_add_payment_method_view_radius);
        RectF drawRect = new RectF(getRectWithPadding(new RectF(getBounds()), mBorderWidth / 2));

        canvas.drawRoundRect(drawRect, cornerRadius, cornerRadius, mBackgroundPaint);
        canvas.drawRoundRect(drawRect, cornerRadius, cornerRadius, mBorderPaint);
        mPaymentMethodDrawableResource.setBounds(getBounds());
        mPaymentMethodDrawableResource.draw(canvas);
    }

    @Override
    public void setAlpha(int alpha) {}

    @Override
    public void setColorFilter(ColorFilter colorFilter) {}

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    private RectF getRectWithPadding(RectF rect, float padding) {
        return new RectF(rect.left + padding, rect.top + padding, rect.right - padding, rect.bottom - padding);
    }
}
