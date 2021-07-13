package com.braintreepayments.api;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewSwitcher;

import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.braintreepayments.api.dropin.R;

import java.util.List;

import static com.braintreepayments.api.DropInUIEventType.DISMISS_VAULT_MANAGER;

public class VaultManagerFragment extends Fragment implements View.OnClickListener {

    private ViewSwitcher loadingViewSwitcher;
    private RecyclerView vaultManagerView;
    private View baseView;

    @VisibleForTesting
    VaultManagerPaymentMethodsAdapter adapter;
    @VisibleForTesting
    DropInViewModel dropInViewModel;

    public VaultManagerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bt_fragment_vault_manager, container, false);

        // TODO: handling switching between fragment and loading
        baseView = view.findViewById(R.id.bt_base_view);
        loadingViewSwitcher = view.findViewById(R.id.bt_loading_view_switcher);
        vaultManagerView = view.findViewById(R.id.bt_vault_manager_list);
        vaultManagerView.setLayoutManager(new LinearLayoutManager(
                requireActivity(), RecyclerView.VERTICAL, false));
        dropInViewModel = new ViewModelProvider(requireActivity()).get(DropInViewModel.class);

        dropInViewModel.getVaultedPaymentMethods().observe(getViewLifecycleOwner(), new Observer<List<PaymentMethodNonce>>() {
            @Override
            public void onChanged(List<PaymentMethodNonce> paymentMethodNonces) {
                showVaultedPaymentMethods(paymentMethodNonces);
            }
        });

        View closeButton = view.findViewById(R.id.bt_vault_manager_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBraintreeEvent(new DropInUIEvent(DISMISS_VAULT_MANAGER));
            }
        });

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v instanceof PaymentMethodItemView) {
            PaymentMethodItemView paymentMethodItemView = (PaymentMethodItemView) v;
            final PaymentMethodNonce paymentMethodNonceToDelete = paymentMethodItemView.getPaymentMethodNonce();

            DeleteVaultedPaymentMethodNonceEvent event = new DeleteVaultedPaymentMethodNonceEvent(paymentMethodNonceToDelete);
            sendBraintreeEvent(event);
        }
    }

    private void showVaultedPaymentMethods(List<PaymentMethodNonce> vaultedPaymentMethodNonces) {
        adapter = new VaultManagerPaymentMethodsAdapter(this, vaultedPaymentMethodNonces);
        vaultManagerView.setAdapter(adapter);
    }

    private void sendBraintreeEvent(Parcelable eventResult) {
        Bundle result = new Bundle();
        result.putParcelable("BRAINTREE_RESULT", eventResult);
        getParentFragmentManager().setFragmentResult("BRAINTREE_EVENT", result);
    }
}