package com.example.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import com.example.myapplication.FeedReaderContract.FeedEntry;

public class FeedReaderDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FeedReader.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry._ID + " INTEGER PRIMARY KEY, " +
                    FeedEntry.COLUMN_NAME_TITLE + " TEXT, " +
                    FeedEntry.COLUMN_NAME_SUBTITLE + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;

    public FeedReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion,
                          int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db,
                            int oldVersion,
                            int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    // Ajouter une entrée
    public long addEntry(String title, String subtitle) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_TITLE, title);
        values.put(FeedEntry.COLUMN_NAME_SUBTITLE, subtitle);

        return db.insert(FeedEntry.TABLE_NAME, null, values);
    }

    // Lire toutes les entrées
    public String readEntries() {
        SQLiteDatabase db = getReadableDatabase();
        StringBuilder result = new StringBuilder();

        String[] projection = {
                FeedEntry._ID,
                FeedEntry.COLUMN_NAME_TITLE,
                FeedEntry.COLUMN_NAME_SUBTITLE
        };

        try (Cursor cursor = db.query(
                FeedEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                FeedEntry._ID + " DESC")) {

            while (cursor.moveToNext()) {
                long id = cursor.getLong(
                        cursor.getColumnIndexOrThrow(FeedEntry._ID));

                String title = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                FeedEntry.COLUMN_NAME_TITLE));

                String subtitle = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                FeedEntry.COLUMN_NAME_SUBTITLE));

                result.append(id)
                        .append(" : ")
                        .append(title)
                        .append(" — ")
                        .append(subtitle)
                        .append("\n");
            }
        }

        return result.length() == 0
                ? "Aucune entrée."
                : result.toString();
    }

    // Modifier une entrée à partir de son identifiant
    public int updateEntry(long id, String title, String subtitle) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_TITLE, title);
        values.put(FeedEntry.COLUMN_NAME_SUBTITLE, subtitle);

        return db.update(
                FeedEntry.TABLE_NAME,
                values,
                FeedEntry._ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // Supprimer une entrée à partir de son identifiant
    public int deleteEntry(long id) {
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                FeedEntry.TABLE_NAME,
                FeedEntry._ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }
}