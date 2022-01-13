package com.braintreepayments.demo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;

import com.braintreepayments.api.DropInClient;
import com.braintreepayments.demo.models.ClientToken;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

@SuppressWarnings("deprecation")
public abstract class BaseActivity extends AppCompatActivity implements OnRequestPermissionsResultCallback,
        ActionBar.OnNavigationListener {

    private static final String KEY_AUTHORIZATION = "com.braintreepayments.demo.KEY_AUTHORIZATION";

    protected String customerId;
    protected DropInClient dropInClient;

    private boolean actionBarSetup;

    @Override
    protected void onResume() {
        super.onResume();

        if (!actionBarSetup) {
            setupActionBar();
            actionBarSetup = true;
        }

        handleAuthorizationState();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        handleAuthorizationState();
    }

    private void handleAuthorizationState() {
        boolean shouldReset = !TextUtils.equals(customerId, Settings.getCustomerId(this));
        if (shouldReset) {
            performReset();
        }
    }

    public void onError(Exception error) {
        Log.d(getClass().getSimpleName(), "Error received (" + error.getClass() + "): "  + error.getMessage());
        Log.d(getClass().getSimpleName(), error.toString());

        showDialog("An error occurred (" + error.getClass() + "): " + error.getMessage());
    }

    private void performReset() {
        customerId = Settings.getCustomerId(this);

        reset();
//        fetchAuthorization();
    }

    protected abstract void reset();

    protected abstract void onAuthorizationFetched();

//    protected void fetchAuthorization() {
//        if (authorization != null) {
//            onAuthorizationFetched();
//        } else if (Settings.useTokenizationKey(this)) {
//            authorization = Settings.getEnvironmentTokenizationKey(this);
//            onAuthorizationFetched();
//        } else {
//            DemoApplication.getApiClient(this).getClientToken(Settings.getCustomerId(this),
//                    Settings.getMerchantAccountId(this), new Callback<ClientToken>() {
//                        @Override
//                        public void success(ClientToken clientToken, Response response) {
//                            if (TextUtils.isEmpty(clientToken.getClientToken())) {
//                                showDialog("Client token was empty");
//                            } else {
//                                authorization = clientToken.getClientToken();
//                                onAuthorizationFetched();
//                            }
//                        }
//
//                        @Override
//                        public void failure(RetrofitError error) {
//                            showDialog("Unable to get a client token. Response Code: " +
//                                    error.getResponse().getStatus() + " Response body: " +
//                                    error.getResponse().getBody());
//                        }
//                    });
//        }
//    }

    protected void showDialog(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @SuppressWarnings("ConstantConditions")
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.environments, android.R.layout.simple_spinner_dropdown_item);
        actionBar.setListNavigationCallbacks(adapter, this);
        actionBar.setSelectedNavigationItem(Settings.getEnvironment(this));
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        if (Settings.getEnvironment(this) != itemPosition) {
            Settings.setEnvironment(this, itemPosition);
            performReset();
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.reset:
                performReset();
                return true;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return false;
        }
    }
}
