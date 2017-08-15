package com.braintreepayments.api.test;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.robolectric.RuntimeEnvironment;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class PackageManagerUtils {

    public static PackageManager mockPackageManagerWithThreeDSecureWebViewActivity()
            throws PackageManager.NameNotFoundException {
        ActivityInfo activityInfo = new ActivityInfo();
        activityInfo.name = "com.braintreepayments.api.threedsecure.ThreeDSecureWebViewActivity";
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.activities = new ActivityInfo[] { activityInfo };

        PackageManager packageManager = spy(RuntimeEnvironment.application.getPackageManager());
        doReturn(packageInfo).when(packageManager)
                .getPackageInfo("com.braintreepayments.api.dropin", PackageManager.GET_ACTIVITIES);

        return packageManager;
    }
}
