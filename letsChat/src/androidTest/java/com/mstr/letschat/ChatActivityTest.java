package com.mstr.letschat;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;

import com.mstr.letschat.databases.ChatContract;
import com.mstr.letschat.databases.ChatDbHelper;
import com.mstr.letschat.databases.ChatMessageTableHelper;

/**
 * Created by dilli on 7/26/2015.
 */
public class ChatActivityTest extends ActivityInstrumentationTestCase2<ChatActivity> {
    private static final String to = "11@chn-dilli2";
    private static final String nickname = "11";

    private ChatActivity activity;
    private ChatDbHelper dbHelper;

    public ChatActivityTest() {
        super(ChatActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Intent intent = new Intent();
        intent.putExtra(ChatActivity.EXTRA_DATA_NAME_TO, to);
        intent.putExtra(ChatActivity.EXTRA_DATA_NAME_NICKNAME, nickname);
        setActivityIntent(intent);

        activity = getActivity();
        dbHelper = ChatDbHelper.getInstance(activity);
        prepareTestData();
    }

    @MediumTest
    public void testPreconditions() {
        assertNotNull("activity is null", activity);
        assertNotNull("dbHelper is null", dbHelper);
    }

    private void prepareTestData() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            database.beginTransaction();

            for (int j = 0; j < 500000; j++) {
                String body = String.valueOf(j);

                ContentValues messageValues = ChatMessageTableHelper.newPlainTextMessage(to, body, System.currentTimeMillis(), true);
                messageValues.put(ChatContract.ChatMessageTable.COLUMN_NAME_STATUS, ChatMessageTableHelper.STATUS_SUCCESS);

                dbHelper.getWritableDatabase().insert(ChatContract.ChatMessageTable.TABLE_NAME, null, messageValues);
            }
            database.setTransactionSuccessful();
        } catch(SQLiteException e) {}
        finally {
            database.endTransaction();
        }

        Log.d("ChatActivityTest", "insert 500000 messages complete");
    }
}