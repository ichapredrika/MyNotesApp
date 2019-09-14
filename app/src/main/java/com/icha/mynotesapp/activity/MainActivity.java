package com.icha.mynotesapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.icha.mynotesapp.LoadNotesCallback;
import com.icha.mynotesapp.R;
import com.icha.mynotesapp.adapter.NoteAdapter;
import com.icha.mynotesapp.entity.Note;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.icha.mynotesapp.activity.FormAddUpdateActivity.REQUEST_UPDATE;
import static com.icha.mynotesapp.db.DatabaseContract.NoteColumns.CONTENT_URI;
import static com.icha.mynotesapp.helper.MappingHelper.mapCursorToArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, LoadNotesCallback {
    private RecyclerView rvNotes;
    private ProgressBar progressBar;
    private FloatingActionButton fabAdd;
    private static final String EXTRA_STATE = "EXTRA_STATE";
    private NoteAdapter adapter;

    private static HandlerThread handlerThread;
    private DataObserver myObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Notes");
        rvNotes = findViewById(R.id.rv_notes);
        rvNotes.setLayoutManager(new LinearLayoutManager(this));
        rvNotes.setHasFixedSize(true);

        progressBar = findViewById(R.id.progressbar);

        handlerThread = new HandlerThread("DataObserver");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());
        myObserver = new DataObserver(handler, this);
        getContentResolver().registerContentObserver(CONTENT_URI, true, myObserver);


        fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(this);

        adapter = new NoteAdapter(this);
        rvNotes.setAdapter(adapter);

        if (savedInstanceState == null) {
            new LoadNoteAsync(this, this).execute();
        } else {
            ArrayList<Note> list = savedInstanceState.getParcelableArrayList(EXTRA_STATE);
            if (list != null) {
                adapter.setListNotes(list);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == FormAddUpdateActivity.REQUEST_ADD) {
                if (resultCode == FormAddUpdateActivity.RESULT_ADD) {
                    Note note = data.getParcelableExtra(FormAddUpdateActivity.EXTRA_NOTE);
                    adapter.addItem(note);
                    rvNotes.smoothScrollToPosition(adapter.getItemCount() - 1);
                    showSnackbarMessage("Satu item berhasil ditambahkan");
                } else if (requestCode == REQUEST_UPDATE) {
                    if (resultCode == FormAddUpdateActivity.RESULT_UPDATE) {
                        Note note = data.getParcelableExtra(FormAddUpdateActivity.EXTRA_NOTE);
                        int position = data.getIntExtra(FormAddUpdateActivity.EXTRA_POSITION, 0);
                        adapter.updateItem(position, note);
                        rvNotes.smoothScrollToPosition(position);
                        showSnackbarMessage("Satu item berhasil diubah");
                    }
                } else if (resultCode == FormAddUpdateActivity.RESULT_DELETE) {
                    int position = data.getIntExtra(FormAddUpdateActivity.EXTRA_POSITION, 0);
                    adapter.removeItem(position);
                    showSnackbarMessage("Satu item berhasil dihapus");
                }
            }
        }
    }

    private void showSnackbarMessage(String message) {
        Snackbar.make(rvNotes, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab_add) {
            Intent intent = new Intent(MainActivity.this, FormAddUpdateActivity.class);
            startActivityForResult(intent, FormAddUpdateActivity.REQUEST_ADD);
        }
    }

    @Override
    public void preExecute() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void postExecute(Cursor notes) {
        progressBar.setVisibility(View.INVISIBLE);
        ArrayList<Note> listNotes = mapCursorToArrayList(notes);
        if (listNotes.size() > 0) {
            adapter.setListNotes(listNotes);
        } else {
            adapter.setListNotes(new ArrayList<Note>());
            showSnackbarMessage("Tidak ada data saat ini");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(EXTRA_STATE, adapter.getListNotes());
    }

    private static class LoadNoteAsync extends AsyncTask<Void, Void, Cursor> {
        private final WeakReference<Context> weakContext;
        private final WeakReference<LoadNotesCallback> weakCallback;

        private LoadNoteAsync(Context context, LoadNotesCallback callback) {
            weakContext = new WeakReference<>(context);
            weakCallback = new WeakReference<>(callback);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            weakCallback.get().preExecute();
        }
        @Override
        protected Cursor doInBackground(Void... voids) {
            Context context = weakContext.get();
            return context.getContentResolver().query(CONTENT_URI, null, null, null, null);
        }
        @Override
        protected void onPostExecute(Cursor notes) {
            super.onPostExecute(notes);
            weakCallback.get().postExecute(notes);
        }
    }

    public static class DataObserver extends ContentObserver {
        final Context context;
        public DataObserver(Handler handler, Context context) {
            super(handler);
            this.context = context;
        }
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            new LoadNoteAsync(context, (LoadNotesCallback) context).execute();
        }
    }
}
