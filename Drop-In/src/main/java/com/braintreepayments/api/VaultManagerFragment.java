package com.braintreepayments.api;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewSwitcher;

import com.braintreepayments.api.dropin.R;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class VaultManagerFragment extends Fragment implements View.OnClickListener {

    private RecyclerView vaultManagerView;
    private ViewSwitcher loadingViewSwitcher;
    private DropInViewModel dropInViewModel;

    public VaultManagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bt_fragment_vault_manager, container, false);

        loadingViewSwitcher = view.findViewById(R.id.bt_loading_view_switcher);
//        loadingViewSwitcher.setDisplayedChild(0);
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
                // TODO: send event back to activity to switch fragments
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
        VaultManagerPaymentMethodsAdapter adapter = new VaultManagerPaymentMethodsAdapter(this, vaultedPaymentMethodNonces);
        vaultManagerView.setAdapter(adapter);
    }

    private void sendBraintreeEvent(Parcelable eventResult) {
        Bundle result = new Bundle();
        result.putParcelable("BRAINTREE_RESULT", eventResult);
        getParentFragmentManager().setFragmentResult("BRAINTREE_EVENT", result);
    }
}