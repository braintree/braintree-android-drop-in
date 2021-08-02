package com.braintreepayments.api;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.viewpager2.widget.ViewPager2;

import com.braintreepayments.api.dropin.R;

import static com.braintreepayments.api.SelectPaymentMethodChildFragment.VAULT_MANAGER;

public class SelectPaymentMethodParentFragment extends Fragment {

    private static final int BOTTOM_SHEET_SLIDE_UP_DELAY = 150;
    private static final int BOTTOM_SHEET_SLIDE_ANIM_DURATION = 150;

    private static final int BACKGROUND_FADE_ANIM_DURATION = 300;
    private static final int VIEW_PAGER_TRANSITION_ANIM_DURATION = 300;

    private View backgroundView;

    @VisibleForTesting
    ViewPager2 viewPager;

    private ViewPager2Animator viewPagerAnimator;

    private SelectPaymentMethodChildFragmentAdapter viewPagerAdapter;
    private SelectPaymentMethodChildFragmentList childFragmentList;

    private DropInRequest dropInRequest;

    private Animator bottomSheetSlideInAnimator;
    private Animator bottomSheetSlideOutAnimator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bt_fragment_select_payment_method_parent, container, false);

        backgroundView = view.findViewById(R.id.background);
        viewPager = view.findViewById(R.id.view_pager);
        viewPager.setUserInputEnabled(false);

        viewPagerAnimator = new ViewPager2Animator(VIEW_PAGER_TRANSITION_ANIM_DURATION);
        childFragmentList = new SelectPaymentMethodChildFragmentList(
                SelectPaymentMethodChildFragment.SUPPORTED_PAYMENT_METHODS);

        Bundle args = getArguments();
        if (args != null) {
            dropInRequest = args.getParcelable("EXTRA_DROP_IN_REQUEST");
        }

        FragmentManager childFragmentManager = getChildFragmentManager();
        childFragmentManager.setFragmentResultListener(DropInEvent.REQUEST_KEY, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                onDropInEvent(DropInEvent.fromBundle(result));
            }
        });

        viewPagerAdapter = new SelectPaymentMethodChildFragmentAdapter(childFragmentManager, getLifecycle(), childFragmentList, dropInRequest);
        viewPager.setAdapter(viewPagerAdapter);

        // disable animation when smooth scrolling between supported payment methods
        // and vault manager fragments
        viewPager.setPageTransformer(new NoAnimationPageTransformer());

        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                int position = viewPager.getCurrentItem();
                SelectPaymentMethodChildFragment visibleFragment =
                    childFragmentList.getItem(position);

                if (visibleFragment == VAULT_MANAGER) {
                    dismissVaultManager();
                } else {
                    cancelDropIn(new AnimationCompleteCallback() {
                        @Override
                        public void onAnimationComplete() {
                            setEnabled(false);
                            remove();
                        }
                    });
                }
            }
        });

        Button backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelDropIn();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        slideUpBottomSheet();
    }

    private void cancelDropIn() {
        cancelDropIn(null);
    }

    private void cancelDropIn(@Nullable final AnimationCompleteCallback callback) {
        if (isAnimatingBottomSheet()) {
            return;
        }

        slideDownBottomSheet(new AnimationCompleteCallback() {
            @Override
            public void onAnimationComplete() {
                if (callback != null) {
                    callback.onAnimationComplete();
                }
                sendDropInEvent(new DropInEvent(DropInEventType.CANCEL_DROPIN));
            }
        });
    }

    private void slideUpBottomSheet() {
        ObjectAnimator backgroundFadeInAnimator =
                ObjectAnimator.ofFloat(backgroundView, View.ALPHA, 0.0f, 1.0f);
        backgroundFadeInAnimator.setDuration(BACKGROUND_FADE_ANIM_DURATION);

        int viewPagerHeight = getViewPagerMeasuredHeight();

        viewPager.setTranslationY(viewPagerHeight);
        ObjectAnimator slideUpAnimator =
                ObjectAnimator.ofFloat(viewPager, View.TRANSLATION_Y, viewPagerHeight, 0);
        slideUpAnimator.setInterpolator(new DecelerateInterpolator());
        slideUpAnimator.setDuration(BOTTOM_SHEET_SLIDE_ANIM_DURATION);
        slideUpAnimator.setStartDelay(BOTTOM_SHEET_SLIDE_UP_DELAY);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(slideUpAnimator).with(backgroundFadeInAnimator);
        animatorSet.start();

        bottomSheetSlideInAnimator = animatorSet;
    }

    private void slideDownBottomSheet(final AnimationCompleteCallback callback) {
        ObjectAnimator backgroundFadeInAnimator =
                ObjectAnimator.ofFloat(backgroundView, View.ALPHA, 1.0f, 0.0f);
        backgroundFadeInAnimator.setDuration(BACKGROUND_FADE_ANIM_DURATION);

        int viewPagerHeight = getViewPagerMeasuredHeight();

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

        bottomSheetSlideOutAnimator = animatorSet;
    }

    private boolean isAnimatingBottomSheet() {
        boolean isSlidingInBottomSheet =
                (bottomSheetSlideInAnimator != null && bottomSheetSlideInAnimator.isRunning());
        boolean isSlidingOutBottomSheet =
                (bottomSheetSlideOutAnimator != null && bottomSheetSlideOutAnimator.isRunning());
        return (isSlidingInBottomSheet || isSlidingOutBottomSheet);
    }

    @VisibleForTesting
    void onDropInEvent(DropInEvent event) {
        switch (event.getType()) {
            case SHOW_VAULT_MANAGER:
                showVaultManager();
                break;
            case DISMISS_VAULT_MANAGER:
                dismissVaultManager();
                break;
        }

        // propagate event up to the parent activity
        sendDropInEvent(event);
    }

    private void showVaultManager() {
        // keep the same height when transitioning to vault manager
        int currentHeight = getViewPagerMeasuredHeight();
        setViewPagerHeight(currentHeight);
        requestLayout();

        childFragmentList.add(VAULT_MANAGER);
        viewPagerAdapter.notifyDataSetChanged();
        viewPagerAnimator.animateToPosition(viewPager, 1);
    }

    private void dismissVaultManager() {
        viewPagerAnimator.animateToPosition(viewPager, 0, new AnimationCompleteCallback() {

            @Override
            public void onAnimationComplete() {
                // revert layout height to wrap content
                setViewPagerHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                requestLayout();

                // remove vault manager fragment
                childFragmentList.remove(1);
                viewPagerAdapter.notifyDataSetChanged();
            }
        });
    }

    private int getViewPagerMeasuredHeight() {
        ViewGroup.LayoutParams viewPagerLayoutParams = viewPager.getLayoutParams();
        viewPager.measure(viewPagerLayoutParams.width, viewPagerLayoutParams.height);
        return viewPager.getMeasuredHeight();
    }

    private void setViewPagerHeight(int newHeight) {
        ViewGroup.LayoutParams layoutParams = viewPager.getLayoutParams();
        layoutParams.height = newHeight;
        viewPager.setLayoutParams(layoutParams);
    }

    private void requestLayout() {
        View rootView = getView();
        if (rootView != null) {
            rootView.requestLayout();
        }
    }

    private void sendDropInEvent(DropInEvent event) {
        if (isAdded()) {
            getParentFragmentManager().setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle());
        }
    }
}