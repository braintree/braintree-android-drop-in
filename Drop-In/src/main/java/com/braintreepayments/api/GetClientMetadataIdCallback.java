package com.braintreepayments.api;

import androidx.annotation.Nullable;

public interface GetClientMetadataIdCallback {

    void onResult(@Nullable String clientMetadataId, @Nullable Exception error);
}
