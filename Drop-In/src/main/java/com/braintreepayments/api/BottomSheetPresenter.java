package com.braintreepayments.api;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.widget.ViewPager2;

import static com.braintreepayments.api.BottomSheetViewType.VAULT_MANAGER;

class BottomSheetPresenter {

    private static final int BOTTOM_SHEET_SLIDE_UP_DELAY = 150;
    private static final int BOTTOM_SHEET_SLIDE_ANIM_DURATION = 150;

    private static final int BACKGROUND_FADE_ANIM_DURATION = 300;
    private static final int VIEW_PAGER_TRANSITION_ANIM_DURATION = 300;

    interface ViewHolder {
        ViewPager2 getViewPager();
        View getBackgroundView();
        void requestLayout();
        FragmentManager getChildFragmentManager();
        Lifecycle getLifecycle();
        DropInRequest getDropInRequest();
    }

    private ViewHolder viewHolder;
    private ViewPager2Animator viewPagerAnimator;
    private BottomSheetViewModel childFragmentList;

    private Animator bottomSheetSlideUpAnimator;
    private Animator bottomSheetSlideDownAnimator;

    private BottomSheetViewAdapter viewPagerAdapter;

    void bind(ViewHolder viewHolder) {
        this.viewHolder = viewHolder;
        this.childFragmentList = new BottomSheetViewModel(
                BottomSheetViewType.SUPPORTED_PAYMENT_METHODS);
        this.viewPagerAnimator = new ViewPager2Animator(VIEW_PAGER_TRANSITION_ANIM_DURATION);

        FragmentManager childFragmentManager = viewHolder.getChildFragmentManager();
        Lifecycle lifecycle = viewHolder.getLifecycle();
        DropInRequest dropInRequest = viewHolder.getDropInRequest();
        this.viewPagerAdapter =
            new BottomSheetViewAdapter(childFragmentManager, lifecycle, childFragmentList, dropInRequest);

        ViewPager2 viewPager = viewHolder.getViewPager();
        viewPager.setUserInputEnabled(false);
        viewPager.setAdapter(viewPagerAdapter);

        // disable animation when smooth scrolling between supported payment methods
        // and vault manager fragments
        viewPager.setPageTransformer(new NoAnimationPageTransformer());
    }

    void unbind() {
        this.viewHolder = null;
        this.viewPagerAdapter = null;
        this.childFragmentList = null;
    }

    boolean isUnbound() {
        return (viewHolder == null);
    }

    @Nullable
    BottomSheetViewType getVisibleFragment() {
        if (isUnbound()) {
            return null;
        }

        ViewPager2 viewPager = viewHolder.getViewPager();
        int position = viewPager.getCurrentItem();
        return childFragmentList.getItem(position);
    }

    void slideUpBottomSheet(final AnimationCompleteCallback callback) {
        if (isUnbound()) {
            return;
        }

        ViewPager2 viewPager = viewHolder.getViewPager();
        View backgroundView = viewHolder.getBackgroundView();

        ObjectAnimator backgroundFadeInAnimator =
                ObjectAnimator.ofFloat(backgroundView, View.ALPHA, 0.0f, 1.0f);
        backgroundFadeInAnimator.setDuration(BACKGROUND_FADE_ANIM_DURATION);

        int viewPagerHeight = getViewGroupMeasuredHeight(viewPager);

        viewPager.setTranslationY(viewPagerHeight);
        ObjectAnimator slideUpAnimator =
                ObjectAnimator.ofFloat(viewPager, View.TRANSLATION_Y, viewPagerHeight, 0);
        slideUpAnimator.setInterpolator(new DecelerateInterpolator());
        slideUpAnimator.setDuration(BOTTOM_SHEET_SLIDE_ANIM_DURATION);
        slideUpAnimator.setStartDelay(BOTTOM_SHEET_SLIDE_UP_DELAY);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(slideUpAnimator).with(backgroundFadeInAnimator);
        animatorSet.start();

        animatorSet.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                callback.onAnimationComplete();
            }
        });

        bottomSheetSlideUpAnimator = animatorSet;
    }

    void slideDownBottomSheet(final AnimationCompleteCallback callback) {
        if (isUnbound()) {
            return;
        }

        ViewPager2 viewPager = viewHolder.getViewPager();
        View backgroundView = viewHolder.getBackgroundView();

        ObjectAnimator backgroundFadeInAnimator =
                ObjectAnimator.ofFloat(backgroundView, View.ALPHA, 1.0f, 0.0f);
        backgroundFadeInAnimator.setDuration(BACKGROUND_FADE_ANIM_DURATION);

        int viewPagerHeight = getViewGroupMeasuredHeight(viewPager);

        viewPager.setTranslationY(viewPagerHeight);
        ObjectAnimator slideUpAnimator =
                ObjectAnimator.ofFloat(viewPager, View.TRANSLATION_Y, 0, viewPagerHeight);
        slideUpAnimator.setInterpolator(new AccelerateInterpolator());
        slideUpAnimator.setDuration(BOTTOM_SHEET_SLIDE_ANIM_DURATION);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(slideUpAnimator).with(backgroundFadeInAnimator);

        animatorSet.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                callback.onAnimationComplete();
            }
        });

        animatorSet.start();

        bottomSheetSlideDownAnimator = animatorSet;
    }

    void showVaultManager() {
        if (isUnbound()) {
            return;
        }

        ViewPager2 viewPager = viewHolder.getViewPager();

        // keep the same height when transitioning to vault manager
        int currentHeight = getViewGroupMeasuredHeight(viewPager);
        setViewGroupHeight(viewPager, currentHeight);
        viewHolder.requestLayout();

        childFragmentList.add(VAULT_MANAGER);
        viewPagerAdapter.notifyDataSetChanged();
        viewPagerAnimator.animateToPosition(viewPager, 1);
    }

    void dismissVaultManager() {
        if (isUnbound()) {
            return;
        }

        final ViewPager2 viewPager = viewHolder.getViewPager();
        viewPagerAnimator.animateToPosition(viewPager, 0, () -> {
            // revert layout height to wrap content
            setViewGroupHeight(viewPager, ViewGroup.LayoutParams.WRAP_CONTENT);
            viewHolder.requestLayout();

            // remove vault manager fragment
            childFragmentList.remove(1);
            viewPagerAdapter.notifyDataSetChanged();
        });
    }

    boolean isAnimating() {
        boolean isSlidingUpBottomSheet =
                (bottomSheetSlideUpAnimator != null && bottomSheetSlideUpAnimator.isRunning());
        boolean isSlidingDownBottomSheet =
                (bottomSheetSlideDownAnimator != null && bottomSheetSlideDownAnimator.isRunning());
        return (isSlidingUpBottomSheet || isSlidingDownBottomSheet);
    }

    private int getViewGroupMeasuredHeight(ViewGroup viewGroup) {
        ViewGroup.LayoutParams viewPagerLayoutParams = viewGroup.getLayoutParams();
        viewGroup.measure(viewPagerLayoutParams.width, viewPagerLayoutParams.height);
        return viewGroup.getMeasuredHeight();
    }

    private void setViewGroupHeight(ViewGroup viewGroup, int newHeight) {
        ViewGroup.LayoutParams layoutParams = viewGroup.getLayoutParams();
        layoutParams.height = newHeight;
        viewGroup.setLayoutParams(layoutParams);
    }
}
