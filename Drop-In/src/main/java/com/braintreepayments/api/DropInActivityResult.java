package com.braintreepayments.api;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;


/**
 * Contains the result from launching {@link DropInActivity} using the {@link DropInActivityContract}.
 */
public class DropInActivityResult implements Parcelable  {

    static final String EXTRA_DROP_IN_ACTIVITY_RESULT =
            "com.braintreepayments.api.dropin.EXTRA_DROP_IN_ACTIVITY_RESULT";

    private DropInResult dropInResult;
    private Exception error;

    DropInActivityResult() {}

    protected DropInActivityResult(Parcel in) {
        dropInResult = in.readParcelable(DropInResult.class.getClassLoader());

        Object[] errors = in.readArray(Exception.class.getClassLoader());
        if (errors.length > 0) {
            error = (Exception) errors[0];
        }
    }

    /**
     * @return a {@link DropInResult}
     */
    @Nullable
    public DropInResult getDropInResult() {
        return dropInResult;
    }

    void setDropInResult(@Nullable DropInResult dropInResult) {
        this.dropInResult = dropInResult;
    }

    /**
     * @return an error that occurred during the Drop-in flow.
     */
    @Nullable
    public Exception getError() {
        return error;
    }

    void setError(@Nullable Exception error) {
        this.error = error;
    }

    public static final Creator<DropInActivityResult> CREATOR = new Creator<DropInActivityResult>() {
        @Override
        public DropInActivityResult createFromParcel(Parcel in) {
            return new DropInActivityResult(in);
        }

        @Override
        public DropInActivityResult[] newArray(int size) {
            return new DropInActivityResult[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(dropInResult, i);

        // This is required because the Exception class is not Parcelable, and Parcel#writeException
        // does not support all exception types.
        Exception[] errors;
        if (error != null) {
            errors = new Exception[]{error};
        } else {
            errors = new Exception[0];
        }
        parcel.writeArray(errors);
    }
}
