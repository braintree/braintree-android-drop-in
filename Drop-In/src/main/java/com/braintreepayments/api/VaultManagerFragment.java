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

import static com.braintreepayments.api.DropInUIEventType.DISMISS_VAULT_MANAGER;

public class VaultManagerFragment extends Fragment implements View.OnClickListener {

    private RecyclerView vaultManagerView;
    private ViewSwitcher loadingViewSwitcher;
    private DropInViewModel dropInViewModel;
    private VaultManagerPaymentMethodsAdapter adapter;

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

        // TODO: Remove in favor of loading indicator fragment before this fragment is loaded?
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
            PaymentMethodItemView dialogView = new PaymentMethodItemView(requireActivity());
            dialogView.setPaymentMethod(paymentMethodNonceToDelete, false);

            final AtomicBoolean positiveSelected = new AtomicBoolean(false);
            new AlertDialog.Builder(requireActivity(),
                    R.style.Theme_AppCompat_Light_Dialog_Alert)
                    .setTitle(R.string.bt_delete_confirmation_title)
                    .setMessage(R.string.bt_delete_confirmation_description)
                    .setView(dialogView)
                    .setPositiveButton(R.string.bt_delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            positiveSelected.set(true);

                            DeleteVaultedPaymentMethodNonceEvent event = new DeleteVaultedPaymentMethodNonceEvent(paymentMethodNonceToDelete);
                            sendBraintreeEvent(event);
                            // TODO: this is required because there is a delay on the back end after
                            //  deleting a payment method, so when immediately refetching nonces it
                            //  still exists. Updating the adapter directly removes the deleted nonce
                            //  from the view. This is also why the alert dialog lives in the fragment
                            //  instead of the activity. Is there a better way to do this?
                            adapter.paymentMethodDeleted(paymentMethodNonceToDelete);
                        }
                    })
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            if (!positiveSelected.get()) {
                                // TODO: Is this analytics event important? It requires another event to be dispatched and handled in DropInActivity
//                                getDropInClient().sendAnalyticsEvent("manager.delete.confirmation.negative");
                            }
                        }
                    })
                    .setNegativeButton(R.string.bt_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .create()
                    .show();
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