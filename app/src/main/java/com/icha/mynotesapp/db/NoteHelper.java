package com.icha.mynotesapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.icha.mynotesapp.entity.Note;

import java.util.ArrayList;

import static android.provider.MediaStore.Audio.Playlists.Members._ID;
import static com.icha.mynotesapp.db.DatabaseContract.NoteColumns.DATE;
import static com.icha.mynotesapp.db.DatabaseContract.NoteColumns.DESCRIPTION;
import static com.icha.mynotesapp.db.DatabaseContract.NoteColumns.TITLE;
import static com.icha.mynotesapp.db.DatabaseContract.TABLE_NOTE;

public class NoteHelper {
    private static final String DATABASE_TABLE = TABLE_NOTE;
    private static DatabaseHelper dataBaseHelper;
    private static NoteHelper INSTANCE;
    private static SQLiteDatabase database;

    //Singleton pattern >> modifier is private
    //constructor
    private NoteHelper(Context context) {
        dataBaseHelper = new DatabaseHelper(context);
    }

    //initializie db
    public static NoteHelper getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SQLiteOpenHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NoteHelper(context);
                }
            }
        }
        return INSTANCE;
    }

    //Open-close db connection
    public void open() throws SQLException {
        database = dataBaseHelper.getWritableDatabase();
    }
    public void close() {
        dataBaseHelper.close();
        if (database.isOpen())
            database.close();
    }

    //CRUD
    public ArrayList<Note> getAllNotes() {
        ArrayList<Note> arrayList = new ArrayList<>();
        Cursor cursor = database.query(DATABASE_TABLE, null,
                null,
                null,
                null,
                null,
                _ID + " ASC",
                null);
        cursor.moveToFirst();
        Note note;
        if (cursor.getCount() > 0) {
            do {
                note = new Note();
                note.setId(cursor.getInt(cursor.getColumnIndexOrThrow(_ID)));
                note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(TITLE)));
                note.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DESCRIPTION)));
                note.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DATE)));
                arrayList.add(note);
                cursor.moveToNext();
            } while (!cursor.isAfterLast());
        }
        cursor.close();
        return arrayList;
    }

    //insertion
    public long insertNote(Note note) {
        ContentValues args = new ContentValues();
        args.put(TITLE, note.getTitle());
        args.put(DESCRIPTION, note.getDescription());
        args.put(DATE, note.getDate());
        return database.insert(DATABASE_TABLE, null, args);
    }

    //update
    public int updateNote(Note note) {
        ContentValues args = new ContentValues();
        args.put(TITLE, note.getTitle());
        args.put(DESCRIPTION, note.getDescription());
        args.put(DATE, note.getDate());
        return database.update(DATABASE_TABLE, args, _ID + "= '" + note.getId() + "'", null);
    }

    //deletion
    public int deleteNote(int id) {
        return database.delete(TABLE_NOTE, _ID + " = '" + id + "'", null);
    }
}
