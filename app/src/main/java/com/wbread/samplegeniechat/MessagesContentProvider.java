/*
 * Copyright (C) 2015 Gleb WBread Ivanov
 *
 */

package com.wbread.samplegeniechat;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Content provider for chat messages DB
 */
public class MessagesContentProvider extends ContentProvider {
    static String DB_NAME = "messagesDB";
    static final int DB_VERSION = 1;

    static String DB_TABLE_NAME = "messages_table";

    static public final String MES_ID = "_id";
    static public final String MES_side = "side";
    static public final String MES_text = "text";

    static final String DB_CREATE = "create table " + DB_TABLE_NAME + "("
            + MES_ID   + " integer primary key autoincrement, "
            + MES_side + " integer, "
            + MES_text + " text"
            + ");";

    static final String AUTHORITY = "com.wbread.samplegeniechat.chat";

    static final String MESSAGES_PATH = "messages";

    public static final Uri MESSAGES_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + MESSAGES_PATH);

    static final String MESSAGES_CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + "." + MESSAGES_PATH;

    static final String MESSAGES_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY + "." + MESSAGES_PATH;

    static final int URI_MESSAGES = 1;

    static final int URI_MESSAGES_ID = 2;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, MESSAGES_PATH, URI_MESSAGES);
        uriMatcher.addURI(AUTHORITY, MESSAGES_PATH + "/#", URI_MESSAGES_ID);
    }

    DBHelper dbHelper;
    SQLiteDatabase db;

    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        switch (uriMatcher.match(uri)) {
            case URI_MESSAGES:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = MES_ID + " ASC";
                }
                break;
            case URI_MESSAGES_ID:
                String id = uri.getLastPathSegment();

                if (TextUtils.isEmpty(selection)) {
                    selection = MES_ID + " = " + id;
                } else {
                    selection = selection + " AND " + MES_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(DB_TABLE_NAME, projection, selection,
                selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(),
                MESSAGES_CONTENT_URI);
        return cursor;
    }

    public Uri insert(Uri uri, ContentValues values) {
        if (uriMatcher.match(uri) != URI_MESSAGES)
            throw new IllegalArgumentException("Wrong URI: " + uri);

        db = dbHelper.getWritableDatabase();
        long rowID = db.insert(DB_TABLE_NAME, null, values);
        Uri resultUri = ContentUris.withAppendedId(MESSAGES_CONTENT_URI, rowID);

        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case URI_MESSAGES:
                //
                break;
            case URI_MESSAGES_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = MES_ID + " = " + id;
                } else {
                    selection = selection + " AND " + MES_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        int cnt = db.delete(DB_TABLE_NAME, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case URI_MESSAGES:
                //
                break;
            case URI_MESSAGES_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = MES_ID + " = " + id;
                } else {
                    selection = selection + " AND " + MES_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        int cnt = db.update(DB_TABLE_NAME, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case URI_MESSAGES:
                return MESSAGES_CONTENT_TYPE;
            case URI_MESSAGES_ID:
                return MESSAGES_CONTENT_ITEM_TYPE;
        }
        return null;
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

}
