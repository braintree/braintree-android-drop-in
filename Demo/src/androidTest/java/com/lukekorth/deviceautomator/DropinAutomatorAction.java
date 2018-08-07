package com.lukekorth.deviceautomator;

import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import com.lukekorth.deviceautomator.AutomatorAction;

public abstract class DropinAutomatorAction extends AutomatorAction {
   /**
     * Performs the swipe left action on the UiObject. The swipe gesture can be performed over any surface.
     * The targeted UI element does not need to be scrollable.
     *
     * @param steps indicates the number of injected move steps into the system. Steps are injected about 5ms apart.
     *              So a 100 steps may take about 1/2 second to complete.
     * @return
     */
    public static AutomatorAction swipeLeft(final int steps) {
        return new AutomatorAction() {
            @Override
            void wrappedPerform(UiSelector selector, UiObject object) throws UiObjectNotFoundException {
                object.swipeLeft(steps);
            }
        };
    }
}
