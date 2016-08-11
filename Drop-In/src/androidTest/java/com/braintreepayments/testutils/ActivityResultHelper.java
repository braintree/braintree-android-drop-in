package com.braintreepayments.testutils;

import android.app.Activity;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertTrue;

public class ActivityResultHelper {

    public static Map<String, Object> getActivityResult(Activity activity) {
        assertTrue(activity.isFinishing());
        Map<String, Object> resultMap = new HashMap<String, Object>();

        try {
            Field resultCodeField = Activity.class.getDeclaredField("mResultCode");
            resultCodeField.setAccessible(true);
            resultMap.put("resultCode", resultCodeField.get(activity));

            Field resultDataField = Activity.class.getDeclaredField("mResultData");
            resultDataField.setAccessible(true);
            resultMap.put("resultData", resultDataField.get(activity));

            return resultMap;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Looks like the Android Activity class has changed it's" +
                    "private fields for mResultCode or mResultData. Time to update the reflection code.", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
