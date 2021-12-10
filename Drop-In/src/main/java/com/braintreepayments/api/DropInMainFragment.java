package com.braintreepayments.api;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.braintreepayments.api.dropin.R;

public class DropInMainFragment extends Fragment {

    private static final String ADD_CARD_TAG = "ADD_CARD";
    private static final String CARD_DETAILS_TAG = "CARD_DETAILS";
    private static final String BOTTOM_SHEET_TAG = "BOTTOM_SHEET";

    static DropInMainFragment from(DropInRequest dropInRequest, String sessionId, Authorization authorization) {
        DropInMainFragment instance = new DropInMainFragment();

        Bundle args = new Bundle();
        args.putParcelable("EXTRA_DROP_IN_REQUEST", dropInRequest);
        args.putString(DropInClient.EXTRA_SESSION_ID, sessionId);
        args.putString(DropInClient.EXTRA_AUTHORIZATION, authorization.toString());

        instance.setArguments(args);
        return instance;
    }

    @VisibleForTesting
    DropInViewModel dropInViewModel;

    @VisibleForTesting
    DropInRequest dropInRequest;

    @VisibleForTesting
    DropInClient dropInClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        dropInRequest = args.getParcelable("EXTRA_DROP_IN_REQUEST");

        if (dropInClient == null) {
            String authorization = args.getString(DropInClient.EXTRA_AUTHORIZATION);
            String sessionId = args.getString(DropInClient.EXTRA_SESSION_ID);
            dropInClient = new DropInClient(requireContext(), authorization, sessionId, dropInRequest);
        }

        dropInViewModel = new ViewModelProvider(requireActivity()).get(DropInViewModel.class);

        View view = inflater.inflate(R.layout.bt_drop_in_activity, container, false);

        getChildFragmentManager().setFragmentResultListener(DropInEvent.REQUEST_KEY, this,
                (requestKey, result) -> onDropInEvent(DropInEvent.fromBundle(result)));

        dropInViewModel.getBottomSheetState().observe(requireActivity(), bottomSheetState -> {
            switch (bottomSheetState) {
                case SHOWN:
                    onDidShowBottomSheet();
                    break;
                case HIDDEN:
                    onDidHideBottomSheet();
                    break;
                case HIDE_REQUESTED:
                case SHOW_REQUESTED:
                default:
                    // do nothing
            }
        });

        showBottomSheet();
        return view;
    }

    @VisibleForTesting
    void onDropInEvent(DropInEvent event) {
        switch (event.getType()) {
            case ADD_CARD_SUBMIT:
//                onAddCardSubmit(event);
                break;
            case CARD_DETAILS_SUBMIT:
//                onCardDetailsSubmit(event);
                break;
            case DELETE_VAULTED_PAYMENT_METHOD:
//                onDeleteVaultedPaymentMethod(event);
                break;
            case EDIT_CARD_NUMBER:
//                onEditCardNumber(event);
                break;
            case SEND_ANALYTICS:
//                onSendAnalytics(event);
                break;
            case SHOW_VAULT_MANAGER:
//                refreshVaultedPaymentMethods();
                break;
            case SUPPORTED_PAYMENT_METHOD_SELECTED:
//                onSupportedPaymentMethodSelected(event);
                break;
            case VAULTED_PAYMENT_METHOD_SELECTED:
//                onVaultedPaymentMethodSelected(event);
                break;
        }
    }

    private void onDidShowBottomSheet() {
        dropInClient.getSupportedPaymentMethods(requireActivity(), (paymentMethods, error) -> {
            if (paymentMethods != null) {
                dropInViewModel.setSupportedPaymentMethods(paymentMethods);

                // TODO: consider pull to refresh to allow user to request an updated
                // instead of having this event respond to the visual presentation of supported
                // payment methods
//                updateVaultedPaymentMethodNonces(false);
            } else {
//                onError(error);
            }
        });
    }

    private void onDidHideBottomSheet() {
//        finishDropInWithPendingResult(DropInExitTransition.FADE_OUT);
    }

    private void showBottomSheet() {
        if (shouldAddFragment(BOTTOM_SHEET_TAG)) {
            BottomSheetFragment bottomSheetFragment = BottomSheetFragment.from(dropInRequest);
            replaceExistingFragment(bottomSheetFragment, BOTTOM_SHEET_TAG);
        }
        dropInViewModel.setBottomSheetState(BottomSheetState.SHOW_REQUESTED);
    }

    private boolean shouldAddFragment(String tag) {
        FragmentManager fragmentManager = getParentFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        return (fragment == null);
    }

    private void replaceExistingFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager
                .beginTransaction()
//                .setCustomAnimations(R.anim.bt_fade_in, R.anim.bt_fade_out)
                .replace(R.id.fragment_container_view, fragment, tag)
                .addToBackStack(null)
                .commit();
    }
}
