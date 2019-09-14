package com.icha.mynotesapp;

import com.icha.mynotesapp.entity.Note;

import java.util.ArrayList;

public interface LoadNotesCallback {
    void preExecute();
    void postExecute(ArrayList<Note> notes);
}
