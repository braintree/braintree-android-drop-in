package com.braintreepayments.api;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.braintreepayments.api.dropin.R;

import java.util.List;

public class SupportedPaymentMethodsFragment extends DropInFragment implements SupportedPaymentMethodSelectedListener, VaultedPaymentMethodSelectedListener {

    @VisibleForTesting
    enum ViewState {
        LOADING,
        SHOW_PAYMENT_METHODS,
        DROP_IN_FINISHING
    }

    @VisibleForTesting
    View loadingIndicatorWrapper;

    private TextView supportedPaymentMethodsHeader;

    @VisibleForTesting
    RecyclerView supportedPaymentMethodsView;

    @VisibleForTesting
    RecyclerView vaultedPaymentMethodsView;

    private View vaultedPaymentMethodsContainer;
    private Button vaultManagerButton;

    private DropInRequest dropInRequest;

    @VisibleForTesting
    DropInViewModel dropInViewModel;

    @VisibleForTesting
    ViewState viewState;

    public SupportedPaymentMethodsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            dropInRequest = args.getParcelable("EXTRA_DROP_IN_REQUEST");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bt_fragment_supported_payment_methods, container, false);

        loadingIndicatorWrapper = view.findViewById(R.id.bt_select_payment_method_loader_wrapper);
        supportedPaymentMethodsHeader = view.findViewById(R.id.bt_supported_payment_methods_header);
        supportedPaymentMethodsView = view.findViewById(R.id.bt_supported_payment_methods);
        vaultedPaymentMethodsContainer = view.findViewById(R.id.bt_vaulted_payment_methods_wrapper);
        vaultedPaymentMethodsView = view.findViewById(R.id.bt_vaulted_payment_methods);
        vaultManagerButton = view.findViewById(R.id.bt_vault_edit_button);

        LinearLayoutManager supportedPaymentMethodsLayoutManager =
                new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false);
        supportedPaymentMethodsView.setLayoutManager(supportedPaymentMethodsLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                requireActivity(), supportedPaymentMethodsLayoutManager.getOrientation());
        supportedPaymentMethodsView.addItemDecoration(dividerItemDecoration);

        vaultedPaymentMethodsView.setLayoutManager(new LinearLayoutManager(requireActivity(),
                LinearLayoutManager.HORIZONTAL, false));
        new LinearSnapHelper().attachToRecyclerView(vaultedPaymentMethodsView);

        dropInViewModel = new ViewModelProvider(requireActivity()).get(DropInViewModel.class);
        if (hasSupportedPaymentMethods()) {
            setViewState(ViewState.SHOW_PAYMENT_METHODS);
        } else {
            setViewState(ViewState.LOADING);
        }

        dropInViewModel.getSupportedPaymentMethods().observe(getViewLifecycleOwner(), paymentMethodTypes -> {
            if (hasSupportedPaymentMethods()) {
                setViewState(ViewState.SHOW_PAYMENT_METHODS);
            }
        });

        dropInViewModel.getVaultedPaymentMethods().observe(getViewLifecycleOwner(), paymentMethodNonces -> {
            if (hasVaultedPaymentMethods()) {
                refreshView();
            }
        });

        dropInViewModel.getDropInState().observe(getViewLifecycleOwner(), dropInState -> {
            if (dropInState == DropInState.WILL_FINISH) {
                setViewState(ViewState.DROP_IN_FINISHING);
            }
        });

        dropInViewModel.getUserCanceledError().observe(getViewLifecycleOwner(), exception -> {
            if (exception instanceof UserCanceledException && hasSupportedPaymentMethods()) {
                setViewState(ViewState.SHOW_PAYMENT_METHODS);
            }
        });

        vaultManagerButton.setOnClickListener(v -> sendDropInEvent(new DropInEvent(DropInEventType.SHOW_VAULT_MANAGER)));

        sendAnalyticsEvent("appeared");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (viewState == ViewState.LOADING && hasSupportedPaymentMethods()) {
            setViewState(ViewState.SHOW_PAYMENT_METHODS);
        }
    }

    private boolean hasSupportedPaymentMethods() {
        return dropInViewModel.getSupportedPaymentMethods().getValue() != null;
    }

    private boolean hasVaultedPaymentMethods() {
        return dropInViewModel.getVaultedPaymentMethods().getValue() != null;
    }

    private void setViewState(ViewState viewState) {
        this.viewState = viewState;
        refreshView();
    }

    private void refreshView() {
        // TODO: consider extracting a presenter
        switch (viewState) {
            case LOADING:
            case DROP_IN_FINISHING:
                // hide vault manager (if necessary) and show loader
                vaultedPaymentMethodsContainer.setVisibility(View.GONE);
                showLoader();
                break;
            case SHOW_PAYMENT_METHODS:
                hideLoader();
                showSupportedPaymentMethods();
                if (hasVaultedPaymentMethods()) {
                    showVaultedPaymentMethods();
                }
                break;
        }
    }

    private void showLoader() {
        loadingIndicatorWrapper.setVisibility(View.VISIBLE);
    }

    private void hideLoader() {
        loadingIndicatorWrapper.setVisibility(View.GONE);
    }

    private void showSupportedPaymentMethods() {
        List<DropInPaymentMethod> availablePaymentMethods =
                dropInViewModel.getSupportedPaymentMethods().getValue();
        SupportedPaymentMethodsAdapter adapter =
                new SupportedPaymentMethodsAdapter(availablePaymentMethods, this);
        supportedPaymentMethodsView.setAdapter(adapter);
    }

    @Override
    public void onPaymentMethodSelected(DropInPaymentMethod type) {
        if (viewState == ViewState.SHOW_PAYMENT_METHODS) {
            boolean paymentTypeWillInitiateAsyncRequest = (type == DropInPaymentMethod.PAYPAL)
                    || (type == DropInPaymentMethod.VENMO);

            if (paymentTypeWillInitiateAsyncRequest) {
                setViewState(ViewState.LOADING);
            }
            sendDropInEvent(
                    DropInEvent.createSupportedPaymentMethodSelectedEvent(type));
        }
    }

    @Override
    public void onVaultedPaymentMethodSelected(PaymentMethodNonce paymentMethodNonce) {
        sendDropInEvent(
                DropInEvent.createVaultedPaymentMethodSelectedEvent(paymentMethodNonce));
    }

    private void showVaultedPaymentMethods() {
        List<PaymentMethodNonce> paymentMethodNonces =
                dropInViewModel.getVaultedPaymentMethods().getValue();

        if (containsCardNonce(paymentMethodNonces)) {
            sendAnalyticsEvent("vaulted-card.appear");
        }
        
        if (paymentMethodNonces != null && paymentMethodNonces.size() > 0) {
            supportedPaymentMethodsHeader.setText(R.string.bt_other);
            vaultedPaymentMethodsContainer.setVisibility(View.VISIBLE);

            VaultedPaymentMethodsAdapter vaultedPaymentMethodsAdapter =
                    new VaultedPaymentMethodsAdapter(paymentMethodNonces, this);

            vaultedPaymentMethodsView.setAdapter(vaultedPaymentMethodsAdapter);

            if (dropInRequest.isVaultManagerEnabled()) {
                vaultManagerButton.setVisibility(View.VISIBLE);
            }

        } else {
            supportedPaymentMethodsHeader.setText(R.string.bt_select_payment_method);
            vaultedPaymentMethodsContainer.setVisibility(View.GONE);
        }
    }

    private static boolean containsCardNonce(@Nullable List<PaymentMethodNonce> paymentMethodNonces) {
        if (paymentMethodNonces != null) {
            for (PaymentMethodNonce nonce : paymentMethodNonces) {
                if (nonce instanceof CardNonce) {
                    return true;
                }
            }
        }
        return false;
    }
}