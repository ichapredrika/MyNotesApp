package com.icha.mynotesapp;

import android.database.Cursor;

import com.icha.mynotesapp.entity.Note;

import java.util.ArrayList;

public interface LoadNotesCallback {
    void preExecute();
    void postExecute(Cursor notes);
}
