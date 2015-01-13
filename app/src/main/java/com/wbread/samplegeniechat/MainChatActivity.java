/*
 * Copyright (C) 2015 Gleb WBread Ivanov
 *
 */

package com.wbread.samplegeniechat;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Random;


/**
 *  Main Chat Activity.
 */


public class MainChatActivity extends ActionBarActivity {

    public static final Uri MESSAGES_URI = Uri
            .parse("content://com.wbread.samplegeniechat.chat/messages");

    ImageView    ivBtnSend;
    LinearLayout llScroll;
    ScrollView   svMessages;
    String[]     munchhausen_speach;

    /** Handles messages for chat emulation */
    final Handler MunchhausenHandler = new Handler() {
        boolean isActive = true;
        int i = 0;

        public void handleMessage(Message msg) {
            if(!msg.getData().isEmpty()){
                isActive = msg.getData().getBoolean("active",true);
            }
            if(isActive){
                PostMessage(munchhausen_speach[i], false);
                if(i<5) i++;
                   else i=0;
                Random r = new Random();
                sendMessageDelayed(obtainMessage(), (r.nextInt(10) + 5) * 2000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat);

        final EditText edMessage = (EditText) findViewById(R.id.edMessage);
        llScroll   = (LinearLayout) findViewById(R.id.llScroll);
        svMessages = (ScrollView) findViewById(R.id.svMessages);

        int resID = getResources().getIdentifier("munchhausen_speach", "array",  getPackageName());
        munchhausen_speach = getResources().getStringArray(resID);

        LoadChatDB();

        ivBtnSend = (ImageView) findViewById(R.id.ivBtnSend);
        ivBtnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String sMessage = edMessage.getText().toString();
                if (!sMessage.equals("")) {
                    PostMessage(sMessage, true);
                }
                edMessage.clearFocus();
                edMessage.setText("");
            }
        });

        RunMunchhausen();
    }

    /** Loads chat messages from DB and shows them on the chat screen */
    void LoadChatDB() {
        Cursor c = getContentResolver().query(MESSAGES_URI, null, null, null, null);

        if (c != null) {
            if (c.moveToFirst()) {
                int MES_sideIndex = c.getColumnIndex(MessagesContentProvider.MES_side);
                int MES_textIndex = c.getColumnIndex(MessagesContentProvider.MES_text);

                do {
                    int side = c.getInt(MES_sideIndex);
                    String text = c.getString(MES_textIndex);

                    ShowMessage(text, (side == 0));

                } while (c.moveToNext());
            }
        }

    }

    /** Saves chat messages into DB and shows it on the chat screen */
    void PostMessage(String sMessage, boolean isRight) {
        int side = 0;
        if (!isRight) {
            side = 1;
        }
        ContentValues cv = new ContentValues();
        cv.put(MessagesContentProvider.MES_side, side);
        cv.put(MessagesContentProvider.MES_text, sMessage);

        getContentResolver().insert(MESSAGES_URI, cv);

        ShowMessage(sMessage, isRight);
    }


    /** Deletes all chat messages from DB and clears chat screen */
    void DeleteMessages(){
        String where = MessagesContentProvider.MES_ID + ">-1";

        getContentResolver().delete(MESSAGES_URI, where, null);

        llScroll.removeAllViews();
    }

    /** Shows one chat message */
    void ShowMessage(String sMessage, boolean isRight) {
        LinearLayout.LayoutParams ivParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView tvMessage = new TextView(this);
        if (isRight) {
            tvMessage.setBackground(getResources().getDrawable(R.drawable.right_bubble));
        } else {
            tvMessage.setBackground(getResources().getDrawable(R.drawable.left_bubble));
        }
        tvMessage.setText(CatchHashtags(sMessage));
        tvMessage.setTextSize(20);
        tvMessage.setPadding(30, 20, 30, 20);
        llScroll.addView(tvMessage, ivParam);

        if(llScroll.getMeasuredHeight() <= svMessages.getScrollY() + svMessages.getHeight()) {
            svMessages.post(new Runnable() {
                @Override
                public void run() {
                    svMessages.fullScroll(View.FOCUS_DOWN);
                }
            });
        }
    }

    /** Catches #hashtags and makes them red */
    Spannable CatchHashtags(String msg){
        Spannable spn = new SpannableString(msg);
        int prev = 0;
        int iTag = msg.indexOf("#");
        while(iTag > -1){
            iTag = prev + iTag;
            String tail = msg.substring(iTag);
            int[] iSep = new int[]{-1,-1,-1,-1,-1,-1,-1};
            iSep[0] = tail.indexOf(" ");
            iSep[1] = tail.indexOf(".");
            iSep[2] = tail.indexOf("!");
            iSep[3] = tail.indexOf("?");
            iSep[4] = tail.indexOf("-");
            iSep[5] = tail.indexOf("#",1);
            iSep[6] = tail.indexOf(",");
            int min = tail.length();
            for(int i=0;i<=6;i++){
                if((iSep[i]>-1)&&(iSep[i]<min)){
                    min = iSep[i];
                }
            }
            if(min < tail.length()+1){
                spn.setSpan(new ForegroundColorSpan(Color.RED),iTag,iTag+min,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            prev = iTag;
            iTag = tail.indexOf("#",1);
        }
        return spn;
    }

    /** Runs chat emulator */
    void RunMunchhausen() {
        MunchhausenHandler.sendMessage(MunchhausenHandler.obtainMessage());
    }


    /** Deactivate chat emulator */
    @Override
    public void onPause() {
        super.onPause();

        Bundle data = new Bundle();
        data.putBoolean("active",false);
        Message msg = new Message();
        msg.setData(data);
        MunchhausenHandler.sendMessage(msg);

    }

    /** Activate chat emulator */
    @Override
    public void onResume() {
        super.onResume();

        Bundle data = new Bundle();
        data.putBoolean("active",true);
        Message msg = new Message();
        msg.setData(data);
        MunchhausenHandler.sendMessage(msg);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_delete:
                DeleteMessages();
                return true;
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}