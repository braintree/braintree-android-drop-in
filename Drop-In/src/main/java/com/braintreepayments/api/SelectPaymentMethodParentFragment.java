package com.braintreepayments.api;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class SelectPaymentMethodParentFragment extends Fragment implements BottomSheetPresenter.ViewHolder {

    @VisibleForTesting
    ViewPager2 viewPager;

    private View backgroundView;

    private DropInRequest dropInRequest;
    private BottomSheetPresenter bottomSheetPresenter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            dropInRequest = args.getParcelable("EXTRA_DROP_IN_REQUEST");
        }

        View view = inflater.inflate(R.layout.bt_fragment_select_payment_method_parent, container, false);
        backgroundView = view.findViewById(R.id.background);
        viewPager = view.findViewById(R.id.view_pager);

        FragmentManager childFragmentManager = getChildFragmentManager();
        childFragmentManager.setFragmentResultListener(DropInEvent.REQUEST_KEY, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                onDropInEvent(DropInEvent.fromBundle(result));
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                SelectPaymentMethodChildFragment visibleFragment =
                    bottomSheetPresenter.getVisibleFragment();

                switch (visibleFragment) {
                    case VAULT_MANAGER:
                        bottomSheetPresenter.dismissVaultManager();
                        break;
                    case SUPPORTED_PAYMENT_METHODS:
                        cancelDropIn(new AnimationCompleteCallback() {
                            @Override
                            public void onAnimationComplete() {
                                // prevent this fragment from handling additional back presses
                                setEnabled(false);
                                remove();
                            }
                        });
                        break;
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

        bottomSheetPresenter = new BottomSheetPresenter();
        bottomSheetPresenter.bind(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        bottomSheetPresenter.slideUpBottomSheet(new AnimationCompleteCallback() {
            @Override
            public void onAnimationComplete() {
                sendDropInEvent(
                        new DropInEvent(DropInEventType.DID_PRESENT_BOTTOM_SHEET));
            }
        });
    }

    private void cancelDropIn() {
        cancelDropIn(null);
    }

    private void cancelDropIn(@Nullable final AnimationCompleteCallback callback) {
        if (bottomSheetPresenter.isAnimatingBottomSheet()) {
            // prevent drop in cancellation while bottom sheet is animating
            return;
        }

        bottomSheetPresenter.slideDownBottomSheet(new AnimationCompleteCallback() {
            @Override
            public void onAnimationComplete() {
                if (callback != null) {
                    callback.onAnimationComplete();
                }
                sendDropInEvent(new DropInEvent(DropInEventType.CANCEL_DROPIN));
            }
        });
    }

    @VisibleForTesting
    void onDropInEvent(DropInEvent event) {
        switch (event.getType()) {
            case SHOW_VAULT_MANAGER:
                bottomSheetPresenter.showVaultManager();
                break;
            case DISMISS_VAULT_MANAGER:
                bottomSheetPresenter.dismissVaultManager();
                break;
        }

        // propagate event up to the parent activity
        sendDropInEvent(event);
    }

    private void sendDropInEvent(DropInEvent event) {
        if (isAdded()) {
            getParentFragmentManager().setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle());
        }
    }

    public void requestLayout() {
        View rootView = getView();
        if (rootView != null) {
            rootView.requestLayout();
        }
    }

    @Override
    public DropInRequest getDropInRequest() {
        return dropInRequest;
    }
    @Override
    public ViewPager2 getViewPager() {
        return viewPager;
    }

    @Override
    public View getBackgroundView() {
        return backgroundView;
    }
}