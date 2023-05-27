package com.knt.waterreminder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.knt.waterreminder.data.AlarmReminderContract;
import com.knt.waterreminder.data.AlarmReminderDbHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public MainActivity() {
    }

    private FloatingActionButton mAddReminderButton;
    private Toolbar mToolbar;
    AlarmCursorAdapter mCursorAdapter;
    AlarmReminderDbHelper alarmReminderDbHelper = new AlarmReminderDbHelper(this);
    ListView reminderListView;
    ProgressDialog prgDialog;

    private static final int VEHICLE_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

/*        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("AlarmReminder");*/

        reminderListView = findViewById(R.id.list);
        View emptyView = findViewById(R.id.empty_view);
        reminderListView.setEmptyView(emptyView);

        mCursorAdapter = new AlarmCursorAdapter(this, null);
        reminderListView.setAdapter(mCursorAdapter);

        reminderListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void  onItemClick(AdapterView<?> adapterView, View view, int position, long id){
                Intent intent = new Intent(MainActivity.this, AddReminderActivity.class);

                Uri currentVehicleUri = ContentUris.withAppendedId(AlarmReminderContract.AlarmReminderEntry.CONTENT_URI, id);

                //set the URI on the data field of the intent
                intent.setData(currentVehicleUri);

                startActivity(intent);
            }
        });

        mAddReminderButton = findViewById(R.id.fab);

        mAddReminderButton.setOnClickListener((v) -> {
            Intent intent = new Intent(v.getContext(), AddReminderActivity.class);
            startActivity(intent);
        });

        getSupportLoaderManager().initLoader(VEHICLE_LOADER, null, this);

    }
/*

    public Loader<Cursor> onCreateLoader(int i , Bundle bundle){

        String[] projection = {
                AlarmReminderContract.AlarmReminderEntry._ID,
                AlarmReminderContract.AlarmReminderEntry.KEY_TITLE,
                AlarmReminderContract.AlarmReminderEntry.KEY_DATE,
                AlarmReminderContract.AlarmReminderEntry.KEY_TIME,
                AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT,
                AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_NO,
                AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_TYPE,
                AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE
        };


        return new CursorLoader(this, */
/*parent activity context*//*

                AlarmReminderContract.AlarmReminderEntry.CONTENT_URI, */
/*provider content URI to query*//*

                projection, */
/*columns to include in the resulting Curson*//*

                null, */
/*no selection clause*//*

                null, */
/*no selection atguments*//*

                null); */
/*default sort order*//*

    }
*/



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String[] projection = {
                AlarmReminderContract.AlarmReminderEntry._ID,
                AlarmReminderContract.AlarmReminderEntry.KEY_TITLE,
                AlarmReminderContract.AlarmReminderEntry.KEY_DATE,
                AlarmReminderContract.AlarmReminderEntry.KEY_TIME,
                AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT,
                AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_NO,
                AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_TYPE,
                AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE
        };


        /*https://developer.android.com/reference/android/app/LoaderManager.LoaderCallbacks*/

        return new CursorLoader(this, //parent activity context
                AlarmReminderContract.AlarmReminderEntry.CONTENT_URI, //provider content URI to query
                projection, //columns to include in the resulting Curson
                null, //no selection clause
                null, //no selection atguments
                null); //default sort order
    }

    @Override
    public void onLoadFinished(@NonNull androidx.loader.content.Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull androidx.loader.content.Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

}