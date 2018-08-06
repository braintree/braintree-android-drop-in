package com.braintreepayments.api.dropin.helper;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import android.view.View;

import com.braintreepayments.api.dropin.adapters.VaultManagerPaymentMethodsAdapter;

import static android.support.v7.widget.helper.ItemTouchHelper.LEFT;

public class VaultManagerHelper extends Callback {
    public interface Interaction {
        void onSwipe(int index);
    }

    private boolean swipeBack = false;
    private Interaction mInteraction;

    public VaultManagerHelper(Interaction interaction) {
        super();

        mInteraction = interaction;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, LEFT);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mInteraction.onSwipe(viewHolder.getAdapterPosition());
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        if (swipeBack) {
            swipeBack = false;
            return 0;
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final View foreground = getForeground(viewHolder);

        getDefaultUIUtil().clearView(foreground);
    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        final View foreground = getForeground(viewHolder);

        getDefaultUIUtil().onDrawOver(c, recyclerView, foreground, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        final View foreground = getForeground(viewHolder);

        getDefaultUIUtil().onDraw(c, recyclerView, foreground, dX, dY, actionState, isCurrentlyActive);
    }

    private View getForeground(RecyclerView.ViewHolder viewHolder) {
        return ((VaultManagerPaymentMethodsAdapter.ViewHolder)viewHolder).foreground;
    }
}


