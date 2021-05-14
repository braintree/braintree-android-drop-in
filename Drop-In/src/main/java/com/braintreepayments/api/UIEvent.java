package com.braintreepayments.api;

/**
 * Ref: https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150
 */
class UIEvent {

    private final @UIEventType int type;

    UIEvent(@UIEventType int type) {
        this.type = type;
    }

    public @UIEventType int getType() {
        return type;
    }
}
