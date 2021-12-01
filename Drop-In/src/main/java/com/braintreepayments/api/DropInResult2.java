package com.braintreepayments.api;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;


/**
 * Contains the result from launching {@link DropInActivity} using the {@link DropInActivityContract}.
 */
public class DropInResult2 implements Parcelable  {

    public static final String EXTRA_DROP_IN_RESULT_2 =
            "com.braintreepayments.api.dropin.EXTRA_DROP_IN_RESULT_2";

    private DropInResult dropInResult;
    private Exception error;

    DropInResult2() {}

    protected DropInResult2(Parcel in) {
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

    public static final Creator<DropInResult2> CREATOR = new Creator<DropInResult2>() {
        @Override
        public DropInResult2 createFromParcel(Parcel in) {
            return new DropInResult2(in);
        }

        @Override
        public DropInResult2[] newArray(int size) {
            return new DropInResult2[size];
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
