package com.braintreepayments.api.test;

import android.app.Application;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.pm.ActivityInfo.LAUNCH_SINGLE_TASK;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class PackageManagerUtils {

    public static PackageManager mockPackageManagerSupportsThreeDSecure()
            throws PackageManager.NameNotFoundException {
        ActivityInfo activityInfo = new ActivityInfo();
        PackageInfo packageInfo = new PackageInfo();
        Application app = RuntimeEnvironment.application;

        activityInfo.name = "com.braintreepayments.api.BraintreeBrowserSwitchActivity";
        activityInfo.launchMode = LAUNCH_SINGLE_TASK;

        packageInfo.activities = new ActivityInfo[] { activityInfo };

        PackageManager packageManager = spy(RuntimeEnvironment.application.getPackageManager());
        doReturn(packageInfo).when(packageManager)
                .getPackageInfo("com.braintreepayments.api.dropin", PackageManager.GET_ACTIVITIES);
        doAnswer(new Answer() {
            @Override
            public List<ResolveInfo> answer(InvocationOnMock invocation) throws Throwable {
                String browserSwitchUri = "com.braintreepayments.api.dropin.braintree";
                String data = ((Intent) invocation.getArguments()[0]).getDataString();
                if (data == null) {
                    data = "";
                }

                if (data.contains(browserSwitchUri)) {
                    return Arrays.asList(new ResolveInfo());
                } else {
                    return (List<ResolveInfo>) invocation.callRealMethod();
                }
            }
        }).when(packageManager).queryIntentActivities(any(Intent.class), eq(0));

        return packageManager;
    }
}
