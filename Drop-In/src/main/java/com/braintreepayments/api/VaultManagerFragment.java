package com.braintreepayments.api;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.braintreepayments.api.dropin.R;

import java.util.List;

public class VaultManagerFragment extends DropInFragment implements View.OnClickListener {

    private RecyclerView vaultManagerView;

    @VisibleForTesting
    VaultManagerPaymentMethodsAdapter adapter;
    @VisibleForTesting
    DropInViewModel dropInViewModel;

    public VaultManagerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bt_fragment_vault_manager, container, false);

        vaultManagerView = view.findViewById(R.id.bt_vault_manager_list);
        vaultManagerView.setLayoutManager(new LinearLayoutManager(
                requireActivity(), RecyclerView.VERTICAL, false));
        dropInViewModel = new ViewModelProvider(requireActivity()).get(DropInViewModel.class);

        dropInViewModel.getVaultedPaymentMethods().observe(getViewLifecycleOwner(), this::showVaultedPaymentMethods);

        View closeButton = view.findViewById(R.id.bt_vault_manager_close);
        closeButton.setOnClickListener(v -> sendDropInEvent(new DropInEvent(DropInEventType.DISMISS_VAULT_MANAGER)));

        sendAnalyticsEvent("manager.appeared");

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v instanceof PaymentMethodItemView) {
            PaymentMethodItemView paymentMethodItemView = (PaymentMethodItemView) v;
            final PaymentMethodNonce paymentMethodNonceToDelete = paymentMethodItemView.getPaymentMethodNonce();

            sendDropInEvent(
                    DropInEvent.createDeleteVaultedPaymentMethodNonceEvent(paymentMethodNonceToDelete));
        }
    }

    private void showVaultedPaymentMethods(List<PaymentMethodNonce> vaultedPaymentMethodNonces) {
        adapter = new VaultManagerPaymentMethodsAdapter(this, vaultedPaymentMethodNonces);
        vaultManagerView.setAdapter(adapter);
    }
}