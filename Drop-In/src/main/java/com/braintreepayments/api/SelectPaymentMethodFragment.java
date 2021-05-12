package com.braintreepayments.api;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.exceptions.AuthenticationException;
import com.braintreepayments.api.exceptions.AuthorizationException;
import com.braintreepayments.api.exceptions.ConfigurationException;
import com.braintreepayments.api.exceptions.DownForMaintenanceException;
import com.braintreepayments.api.exceptions.GoogleApiClientException;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.exceptions.ServerException;
import com.braintreepayments.api.exceptions.UnexpectedException;
import com.braintreepayments.api.exceptions.UpgradeRequiredException;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.BraintreeResponseListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.interfaces.PaymentMethodNoncesUpdatedListener;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.ClientToken;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.models.GooglePaymentCardNonce;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.models.ThreeDSecureRequest;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import static com.braintreepayments.api.DropInActivity.DELETE_PAYMENT_METHOD_NONCE_CODE;
import static com.braintreepayments.api.DropInActivity.EXTRA_PAYMENT_METHOD_NONCES;
import static com.braintreepayments.api.DropInRequest.EXTRA_CHECKOUT_REQUEST;

public class SelectPaymentMethodFragment extends Fragment implements BraintreeCancelListener, BraintreeErrorListener, PaymentMethodNoncesUpdatedListener, PaymentMethodNonceCreatedListener, SupportedPaymentMethodsAdapter.PaymentMethodSelectedListener {

    private String mDeviceData;
    private ViewSwitcher mLoadingViewSwitcher;
    private TextView mSupportedPaymentMethodsHeader;

    private boolean mPerformedThreeDSecureVerification;

    @VisibleForTesting
    protected ListView mSupportedPaymentMethodListView;

    private View mVaultedPaymentMethodsContainer;
    private RecyclerView mVaultedPaymentMethodsView;
    private Button mVaultManagerButton;

    private Configuration configuration;
    private DropInRequest dropInRequest;
    private boolean isClientTokenPresent;

    private BraintreeFragment braintreeFragment;

    public SelectPaymentMethodFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            try {
                configuration = Configuration.fromJson(args.getString("EXTRA_CONFIGURATION"));

                dropInRequest = args.getParcelable("EXTRA_DROP_IN_REQUEST");
                braintreeFragment = BraintreeFragment.newInstance(this, dropInRequest.getAuthorization());
                braintreeFragment.addListener(this);

                isClientTokenPresent = braintreeFragment.getAuthorization() instanceof ClientToken;

            } catch (InvalidArgumentException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bt_fragment_select_payment_method, container, false);

        mLoadingViewSwitcher = view.findViewById(R.id.bt_loading_view_switcher);
        mSupportedPaymentMethodsHeader = view.findViewById(R.id.bt_supported_payment_methods_header);
        mSupportedPaymentMethodListView = view.findViewById(R.id.bt_supported_payment_methods);
        mVaultedPaymentMethodsContainer = view.findViewById(R.id.bt_vaulted_payment_methods_wrapper);
        mVaultedPaymentMethodsView = view.findViewById(R.id.bt_vaulted_payment_methods);
        mVaultManagerButton = view.findViewById(R.id.bt_vault_edit_button);

        mVaultManagerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onVaultEditButtonClick(view);
            }
        });

        mVaultedPaymentMethodsView.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false));
        new LinearSnapHelper().attachToRecyclerView(mVaultedPaymentMethodsView);

        if (dropInRequest.shouldCollectDeviceData() && TextUtils.isEmpty(mDeviceData)) {
            DataCollector.collectDeviceData(braintreeFragment, new BraintreeResponseListener<String>() {
                @Override
                public void onResponse(String deviceData) {
                    mDeviceData = deviceData;
                }
            });
        }

        if (dropInRequest.isGooglePaymentEnabled()) {
            GooglePayment.isReadyToPay(braintreeFragment, new BraintreeResponseListener<Boolean>() {
                @Override
                public void onResponse(Boolean isReadyToPay) {
                    showSupportedPaymentMethods(isReadyToPay);
                }
            });
        } else {
            showSupportedPaymentMethods(false);
        }

        braintreeFragment.sendAnalyticsEvent("appeared");

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // TODO: show spinner while fetching nonces
        fetchPaymentMethodNonces(true);
    }

    private void showSupportedPaymentMethods(boolean googlePaymentEnabled) {
        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(getActivity(), this);
        adapter.setup(configuration, dropInRequest, googlePaymentEnabled, isClientTokenPresent);
        mSupportedPaymentMethodListView.setAdapter(adapter);
        mLoadingViewSwitcher.setDisplayedChild(1);
        fetchPaymentMethodNonces(false);
    }

    @Override
    public void onPaymentMethodSelected(PaymentMethodType type) {
        mLoadingViewSwitcher.setDisplayedChild(0);
        DropInActivity activity = ((DropInActivity) getActivity());
        activity.onPaymentMethodSelected(type);
    }

    private void fetchPaymentMethodNonces(final boolean refetch) {
        if (isClientTokenPresent) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    FragmentActivity activity = getActivity();
                    if (activity != null && !activity.isFinishing()) {
                        if (braintreeFragment.hasFetchedPaymentMethodNonces() && !refetch) {
                            onPaymentMethodNoncesUpdated(braintreeFragment.getCachedPaymentMethodNonces());
                        } else {
                            PaymentMethod.getPaymentMethodNonces(braintreeFragment, true);
                        }
                    }
                }
            }, getResources().getInteger(android.R.integer.config_shortAnimTime));
        }
    }

    @Override
    public void onPaymentMethodNoncesUpdated(List<PaymentMethodNonce> paymentMethodNonces) {
        final List<PaymentMethodNonce> noncesRef = paymentMethodNonces;
        if (paymentMethodNonces.size() > 0) {
            if (dropInRequest.isGooglePaymentEnabled()) {
                GooglePayment.isReadyToPay(braintreeFragment, new BraintreeResponseListener<Boolean>() {
                    @Override
                    public void onResponse(Boolean isReadyToPay) {
                        showVaultedPaymentMethods(noncesRef, isReadyToPay);
                    }
                });
            } else {
                showVaultedPaymentMethods(paymentMethodNonces, false);
            }
        } else {
            mSupportedPaymentMethodsHeader.setText(R.string.bt_select_payment_method);
            mVaultedPaymentMethodsContainer.setVisibility(View.GONE);
        }
    }

    private void showVaultedPaymentMethods(List<PaymentMethodNonce> paymentMethodNonces, boolean googlePayEnabled) {
        mSupportedPaymentMethodsHeader.setText(R.string.bt_other);
        mVaultedPaymentMethodsContainer.setVisibility(View.VISIBLE);

        VaultedPaymentMethodsAdapter vaultedPaymentMethodsAdapter = new VaultedPaymentMethodsAdapter(new PaymentMethodNonceCreatedListener() {
            @Override
            public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
                if (paymentMethodNonce instanceof CardNonce) {
                    braintreeFragment.sendAnalyticsEvent("vaulted-card.select");
                }

                SelectPaymentMethodFragment.this.onPaymentMethodNonceCreated(paymentMethodNonce);
            }
        }, paymentMethodNonces);

        vaultedPaymentMethodsAdapter.setup(
                getActivity(), configuration, dropInRequest, googlePayEnabled, isClientTokenPresent);
        mVaultedPaymentMethodsView.setAdapter(vaultedPaymentMethodsAdapter);

        if (dropInRequest.isVaultManagerEnabled()) {
            mVaultManagerButton.setVisibility(View.VISIBLE);
        }

        if (vaultedPaymentMethodsAdapter.hasCardNonce()) {
            braintreeFragment.sendAnalyticsEvent("vaulted-card.appear");
        }
    }

    @Override
    public void onPaymentMethodNonceCreated(final PaymentMethodNonce paymentMethodNonce) {
        if (!mPerformedThreeDSecureVerification &&
                paymentMethodCanPerformThreeDSecureVerification(paymentMethodNonce) &&
                shouldRequestThreeDSecureVerification()) {
            mPerformedThreeDSecureVerification = true;
            mLoadingViewSwitcher.setDisplayedChild(0);

            if (dropInRequest.getThreeDSecureRequest() == null) {
                ThreeDSecureRequest threeDSecureRequest = new ThreeDSecureRequest().amount(dropInRequest.getAmount());
                dropInRequest.threeDSecureRequest(threeDSecureRequest);
            }

            if (dropInRequest.getThreeDSecureRequest().getAmount() == null && dropInRequest.getAmount() != null) {
                dropInRequest.getThreeDSecureRequest().amount(dropInRequest.getAmount());
            }

            dropInRequest.getThreeDSecureRequest().nonce(paymentMethodNonce.getNonce());
            ThreeDSecure.performVerification(braintreeFragment, dropInRequest.getThreeDSecureRequest());
            return;
        }

        braintreeFragment.sendAnalyticsEvent("sdk.exit.success");

        DropInResult.setLastUsedPaymentMethodType(getActivity(), paymentMethodNonce);

        DropInActivity activity = ((DropInActivity) getActivity());
        activity.finish(paymentMethodNonce, mDeviceData);
    }

    private void handleThreeDSecureFailure() {
        if (mPerformedThreeDSecureVerification) {
            mPerformedThreeDSecureVerification = false;
            fetchPaymentMethodNonces(true);
        }
    }

    private boolean paymentMethodCanPerformThreeDSecureVerification(final PaymentMethodNonce paymentMethodNonce) {
        if (paymentMethodNonce instanceof CardNonce) {
            return true;
        }

        if (paymentMethodNonce instanceof GooglePaymentCardNonce) {
            return ((GooglePaymentCardNonce) paymentMethodNonce).isNetworkTokenized() == false;
        }

        return false;
    }

    protected boolean shouldRequestThreeDSecureVerification() {
        boolean hasAmount = !TextUtils.isEmpty(dropInRequest.getAmount()) ||
                (dropInRequest.getThreeDSecureRequest() != null && !TextUtils.isEmpty(dropInRequest.getThreeDSecureRequest().getAmount()));

        // TODO: NEXT_MAJOR_VERSION use BraintreeClient#getConfiguration and don't cache configuration in memory
        if (configuration == null) {
            return false;
        }

        return dropInRequest.shouldRequestThreeDSecureVerification() &&
                configuration.isThreeDSecureEnabled() &&
                hasAmount;
    }

    public void onVaultEditButtonClick(View view) {
        ArrayList<Parcelable> parcelableArrayList = new ArrayList<Parcelable>(braintreeFragment.getCachedPaymentMethodNonces());

        Intent intent = new Intent(getActivity(), VaultManagerActivity.class)
                .putExtra(EXTRA_CHECKOUT_REQUEST, dropInRequest)
                .putParcelableArrayListExtra(EXTRA_PAYMENT_METHOD_NONCES, parcelableArrayList);
        startActivityForResult(intent, DELETE_PAYMENT_METHOD_NONCE_CODE);

        braintreeFragment.sendAnalyticsEvent("manager.appeared");
    }

    @Override
    public void onCancel(int requestCode) {
        handleThreeDSecureFailure();

        mLoadingViewSwitcher.setDisplayedChild(1);
    }

    @Override
    public void onError(final Exception error) {
        handleThreeDSecureFailure();

        if (error instanceof GoogleApiClientException) {
            showSupportedPaymentMethods(false);
            return;
        }

        if (error instanceof AuthenticationException || error instanceof AuthorizationException ||
                error instanceof UpgradeRequiredException) {
            braintreeFragment.sendAnalyticsEvent("sdk.exit.developer-error");
        } else if (error instanceof ConfigurationException) {
            braintreeFragment.sendAnalyticsEvent("sdk.exit.configuration-exception");
        } else if (error instanceof ServerException || error instanceof UnexpectedException) {
            braintreeFragment.sendAnalyticsEvent("sdk.exit.server-error");
        } else if (error instanceof DownForMaintenanceException) {
            braintreeFragment.sendAnalyticsEvent("sdk.exit.server-unavailable");
        } else {
            braintreeFragment.sendAnalyticsEvent("sdk.exit.sdk-error");
        }

        DropInActivity activity = ((DropInActivity) getActivity());
        activity.finish(error);
    }
}