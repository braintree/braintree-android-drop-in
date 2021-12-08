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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            dropInRequest = args.getParcelable("EXTRA_DROP_IN_REQUEST");
        }

        dropInViewModel = new ViewModelProvider(requireActivity()).get(DropInViewModel.class);

        View view = inflater.inflate(R.layout.bt_fragment_bottom_sheet, container, false);
        backgroundView = view.findViewById(R.id.background);

        viewPager = view.findViewById(R.id.view_pager);
        // TODO: investigate view pager saveInstanceState restoration
        viewPager.setSaveEnabled(false);

        // it's best to call bind here before any live data / fragment result observers are registered
        bottomSheetPresenter = new BottomSheetPresenter();
        bottomSheetPresenter.bind(this);

        FragmentManager childFragmentManager = getChildFragmentManager();
        childFragmentManager.setFragmentResultListener(DropInEvent.REQUEST_KEY, this,
                (requestKey, result) -> onDropInEvent(DropInEvent.fromBundle(result)));

        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                BottomSheetViewType visibleFragment =
                    bottomSheetPresenter.getVisibleFragment();

                if (visibleFragment != null) {
                    switch (visibleFragment) {
                        case VAULT_MANAGER:
                            bottomSheetPresenter.dismissVaultManager();
                            break;
                        case SUPPORTED_PAYMENT_METHODS:
                            slideDownBottomSheet(() -> {
                                // prevent this fragment from handling additional back presses
                                setEnabled(false);
                                remove();
                            });
                            break;
                    }
                }
            }
        });

        dropInViewModel.getBottomSheetState().observe(requireActivity(), bottomSheetState -> {
            switch (bottomSheetState) {
                case HIDE_REQUESTED:
                    slideDownBottomSheet();
                    break;
                case SHOW_REQUESTED:
                    slideUpBottomSheet();
                    break;
                case SHOWN:
                case HIDDEN:
                default:
                    // do nothing
            }
        });

        Button backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> slideDownBottomSheet());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean isBottomSheetVisible =
            dropInViewModel.getBottomSheetState().getValue() == BottomSheetState.SHOWN;
        if (isBottomSheetVisible) {
            backgroundView.setAlpha(1.0f);
        } else {
            slideUpBottomSheet();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bottomSheetPresenter != null) {
            bottomSheetPresenter.unbind();
        }
    }

    private void slideUpBottomSheet() {
        if (bottomSheetPresenter.isAnimating()) {
            // prevent drop in from being shown while bottom sheet is animating
            return;
        }

        bottomSheetPresenter.slideUpBottomSheet(() -> dropInViewModel.setBottomSheetState(BottomSheetState.SHOWN));
    }

    private void slideDownBottomSheet() {
        slideDownBottomSheet(null);
    }

    private void slideDownBottomSheet(@Nullable final AnimationCompleteCallback callback) {
        if (bottomSheetPresenter.isAnimating()) {
            // prevent drop in from being hidden while bottom sheet is animating
            return;
        }

        bottomSheetPresenter.slideDownBottomSheet(() -> {
            dropInViewModel.setBottomSheetState(BottomSheetState.HIDDEN);
            if (callback != null) {
                callback.onAnimationComplete();
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