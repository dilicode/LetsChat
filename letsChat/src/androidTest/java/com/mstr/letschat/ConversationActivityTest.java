package com.mstr.letschat;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;

/**
 * Created by dilli on 7/26/2015.
 */
public class ConversationActivityTest extends ActivityInstrumentationTestCase2<ConversationActivity> {
    private ConversationActivity activity;

    public ConversationActivityTest() {
        super(ConversationActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        activity = getActivity();
    }

    @MediumTest
    public void testPreconditions() {
        assertNotNull("activity is null", activity);
    }
}