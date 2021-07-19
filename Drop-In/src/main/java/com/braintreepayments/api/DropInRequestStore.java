package com.braintreepayments.api;

class DropInRequestStore
{
    // static variable single_instance of type Singleton
    private static DropInRequestStore single_instance = null;
    // variable of type String
    public DropInRequest dropInRequest;
    // private constructor restricted to this class itself
    private DropInRequestStore() {}

    void setDropInRequest(DropInRequest dropInRequest) {
        this.dropInRequest = dropInRequest;
    }

    DropInRequest getDropInRequest() {
        return this.dropInRequest;
    }

    public static DropInRequestStore getInstance()
    {
        if (single_instance == null)
            single_instance = new DropInRequestStore();
        return single_instance;
    }
}