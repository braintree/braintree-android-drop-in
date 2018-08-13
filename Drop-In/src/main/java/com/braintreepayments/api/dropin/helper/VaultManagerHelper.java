package com.braintreepayments.api.dropin.helper;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;

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
}


