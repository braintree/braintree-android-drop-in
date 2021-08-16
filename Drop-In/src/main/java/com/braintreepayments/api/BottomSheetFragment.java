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
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.braintreepayments.api.dropin.R;

public class BottomSheetFragment extends Fragment implements BottomSheetPresenter.ViewHolder {

    @VisibleForTesting
    ViewPager2 viewPager;

    @VisibleForTesting
    DropInViewModel dropInViewModel;

    private View backgroundView;

    private DropInRequest dropInRequest;
    private BottomSheetPresenter bottomSheetPresenter;

    static BottomSheetFragment from(DropInRequest dropInRequest) {
        BottomSheetFragment instance = new BottomSheetFragment();

        Bundle args = new Bundle();
        args.putParcelable("EXTRA_DROP_IN_REQUEST", dropInRequest);

        instance.setArguments(args);
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            dropInRequest = args.getParcelable("EXTRA_DROP_IN_REQUEST");
        }

        dropInViewModel = new ViewModelProvider(requireActivity()).get(DropInViewModel.class);

        View view = inflater.inflate(R.layout.bt_fragment_bottom_sheet, container, false);
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
                BottomSheetViewType visibleFragment =
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

        boolean isBottomSheetPresented = dropInViewModel.isBottomSheetPresented().getValue();
        if (isBottomSheetPresented) {
            backgroundView.setAlpha(1.0f);
        } else {
            bottomSheetPresenter.slideUpBottomSheet(new AnimationCompleteCallback() {
                @Override
                public void onAnimationComplete() {
                    dropInViewModel.setBottomSheetPresented(true);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bottomSheetPresenter.unbind();
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