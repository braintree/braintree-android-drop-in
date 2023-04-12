package com.braintreepayments.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import android.content.Context;

import org.mockito.stubbing.Answer;

public class MockDataCollectorBuilder {

    private String collectDeviceDataSuccess;
    private Exception collectDeviceDataError;

    public MockDataCollectorBuilder collectDeviceDataSuccess(String collectDeviceDataSuccess) {
        this.collectDeviceDataSuccess = collectDeviceDataSuccess;
        return this;
    }

    public MockDataCollectorBuilder collectDeviceDataError(Exception collectDeviceDataError) {
        this.collectDeviceDataError = collectDeviceDataError;
        return this;
    }

    public DataCollector build() {
        DataCollector dataCollector = mock(DataCollector.class);

        doAnswer((Answer<Void>) invocation -> {
            DataCollectorCallback callback = (DataCollectorCallback) invocation.getArguments()[1];
            if (collectDeviceDataSuccess != null) {
                callback.onResult(collectDeviceDataSuccess, null);
            } else if (collectDeviceDataError != null) {
                callback.onResult(null, collectDeviceDataError);
            }
            return null;
        }).when(dataCollector).collectDeviceData(any(Context.class), any(DataCollectorCallback.class));

        return dataCollector;
    }
}
