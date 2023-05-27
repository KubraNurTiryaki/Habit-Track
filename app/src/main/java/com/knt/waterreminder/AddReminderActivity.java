package com.knt.waterreminder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.knt.waterreminder.data.AlarmReminderContract;
import com.knt.waterreminder.reminder.AlarmScheduler;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

public class AddReminderActivity extends AppCompatActivity implements
        TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener, LoaderManager.LoaderCallbacks<Cursor> { //androidx'teki Loader'ı kullanınca sorun çözlüdü

    private static final int EXISTING_VEHICLE_LOADER = 0;

    private Toolbar mToolbar;
    private EditText mTitleText;
    private TextView mDateText, mTimeText, mRepeatText, mRepeatNoText, mRepeatTypeText;
    private FloatingActionButton mFAB1;
    private FloatingActionButton mFAB2;
    private Calendar mCalendar;
    private int mYear, mMonth, mHour, mMinute, mDay;
    private long mRepeatTime;
    private Switch mRepeatSwitch;
    private String mTitle;
    private String mTime;
    private String mDate;
    private String mRepeat;
    private String mRepeatNo;
    private String mRepeatType;
    private String mActive;

    private Uri mCurrentReminderUri;
    private boolean mVehicleHasChanged = false;

    //values fpr orientation change
    private static final String KEY_TITLE = "title_key";
    private static final String KEY_TIME = "time_key";
    private static final String KEY_DATE = "date_key";
    private static final String KEY_REPEAT = "repeat_key";
    private static final String KEY_REPEAT_NO = "repeat_no_key";
    private static final String KEY_REPEAT_TYPE = "repeat_type_key";
    private static final String KEY_ACTIVE = "active_key";


    //constrant values in milliseconds

    private static final long milMinute = 60000L;
    private static final long milHour = 3600000L;
    private static final long milDay = 86400000L;
    private static final long milWeek = 604800000L;
    private static final long milMonth = 2592000000L;

    private View.OnTouchListener mTouchListener = (view, motionEvent) -> {
        mVehicleHasChanged = true;
        return false;
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        Intent intent = getIntent();
        mCurrentReminderUri = intent.getData();

        if (mCurrentReminderUri == null) {
            setTitle("Add Reminder Details");

            /*Invalide the options menu, so the "Delete" menu option can be hidden.
             * (It doesnt make sense to delete a reminder that hasnt created yet.)*/

            invalidateOptionsMenu();
        } else {
            setTitle("Edit Reminder");
            getSupportLoaderManager().initLoader(EXISTING_VEHICLE_LOADER, null, this);
        }

        //initialize Views

        mToolbar = findViewById(R.id.toolbar);
        mTitleText = findViewById(R.id.reminder_title);
        mDateText = findViewById(R.id.set_date);
        mTimeText = findViewById(R.id.set_time);
        mRepeatText = findViewById(R.id.set_repeat);
        mRepeatNoText = findViewById(R.id.set_repeat_no);
        mRepeatTypeText = findViewById(R.id.set_repeat_type);
        mRepeatSwitch = findViewById(R.id.repeat_switch);
        mFAB1 = findViewById(R.id.starred1);
        mFAB2 = findViewById(R.id.starred2);
        mToolbar = findViewById(R.id.toolbar);


        //initialize default values
        mActive = "true";
        mRepeat = "true";
        mRepeatNo = Integer.toString(1);
        mRepeatType = "Hour";

        mCalendar = Calendar.getInstance();
        mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        mMinute = mCalendar.get(Calendar.MINUTE);
        mYear = mCalendar.get(Calendar.YEAR);
        mMonth = mCalendar.get(Calendar.MONTH) + 1;
        mDay = mCalendar.get(Calendar.DATE);

        mDate = mDay + "/" + mMonth + "/" + mYear;
        mTime = mHour + ":" + mMinute;

        //setup reminder title EditText
        mTitleText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                mTitle = s.toString().trim();
                mTitleText.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        //setup textviews using reminder values
        mDateText.setText(mDate);
        mTimeText.setText(mTime);
        mRepeatNoText.setText(mRepeatNo);
        mRepeatTypeText.setText(mRepeatType);
        mRepeatText.setText("Every " + mRepeatNo + " " + mRepeatType + "(s)");


        //to save state on device rotation
        if (savedInstanceState != null) {

            String savedTitle = savedInstanceState.getString(KEY_TITLE);
            mTitleText.setText(savedTitle);
            mTitle = savedTitle;


            String savedTime = savedInstanceState.getString(KEY_TIME);
            mTimeText.setText(savedTime);
            mTime = savedTime;


            String savedDate = savedInstanceState.getString(KEY_DATE);
            mDateText.setText(savedDate);
            mDate = savedDate;


            String savedRepeat = savedInstanceState.getString(KEY_REPEAT);
            mRepeatText.setText(savedRepeat);
            mRepeat = savedRepeat;


            String savedRepeatNo = savedInstanceState.getString(KEY_REPEAT_NO);
            mRepeatNoText.setText(savedRepeatNo);
            mRepeatNo = savedRepeatNo;


            String savedRepeatType = savedInstanceState.getString(KEY_REPEAT_TYPE);
            mRepeatTypeText.setText(savedRepeatType);
            mRepeatType = savedRepeatType;

            mActive = savedInstanceState.getString(KEY_ACTIVE);

            //setup active buttons

            if (mActive.equals("false")) {
                mFAB1.setVisibility(View.VISIBLE);
                mFAB2.setVisibility(View.GONE);
            } else if (mActive.equals("true")) {
                mFAB1.setVisibility(View.GONE);
                mFAB2.setVisibility(View.VISIBLE);
            }

            setSupportActionBar(mToolbar);
            getSupportActionBar().setTitle("Add Reminder");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

        }


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(KEY_TITLE, mTitleText.getText());
        outState.putCharSequence(KEY_TIME, mTimeText.getText());
        outState.putCharSequence(KEY_DATE, mDateText.getText());
        outState.putCharSequence(KEY_REPEAT, mRepeatText.getText());
        outState.putCharSequence(KEY_REPEAT_NO, mRepeatNoText.getText());
        outState.putCharSequence(KEY_REPEAT_TYPE, mRepeatTypeText.getText());
        outState.putCharSequence(KEY_ACTIVE, mActive);
    }

    //On cliccikng Time picker
    public void setTime(View v) {
        Calendar now = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                false

        );
        tpd.setThemeDark(false);
        tpd.show(getFragmentManager(), "Timepickerdialog");
    }


    //On cliccikng Date picker
    public void setDate(View v) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)


        );
        dpd.show(getFragmentManager(), "Datepickerdialog");
    }

    //obtain time from time picker
    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        mHour = hourOfDay;
        mMinute = minute;
        if ((mMinute < 10)) {
            mTime = hourOfDay + ":" + "0" + minute;
        } else {
            mTime = hourOfDay + ":" + minute;
        }
        mTimeText.setText(mTime);
    }


    //obtaşn date from picker
    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        monthOfYear++;
        mDay = dayOfMonth;
        mMonth = monthOfYear;
        mYear = year;
        mDate = dayOfMonth + "/" + monthOfYear + "/" + year;
        mDateText.setText(mDate);

    }


    //on clicking the active button
    public void selectFab1(View v) {
        mFAB1 = findViewById(R.id.starred1);
        mFAB1.setVisibility(View.GONE);
        mFAB2 = findViewById(R.id.starred2);
        mFAB2.setVisibility(View.VISIBLE);
        mActive = "true";
    }


    //on clicking the inactive button
    public void selectFab2(View v) {
        mFAB2 = findViewById(R.id.starred2);
        mFAB2.setVisibility(View.GONE);
        mFAB1 = findViewById(R.id.starred1);
        mFAB1.setVisibility(View.VISIBLE);
        mActive = "false";
    }

    //on clicking the repeat switch
    public void onSwitchRepeat(View view) {
        boolean on = ((Switch) view).isChecked();
        if (on) {
            mRepeat = "true";
            mRepeatText.setText("Every " + mRepeatNo + " " + mRepeatType + "(s)");
        } else {
            mRepeat = "false";
            mRepeatText.setText("Off");
        }
    }

    //on clicking repeat type button
    public void selectRepeatType(View v) {
        final String[] items = new String[5];
        items[0] = "Minute";
        items[1] = "Hour";
        items[2] = "Day";
        items[3] = "Week";
        items[4] = "Month";


        //create list dialog

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Type");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(DialogInterface dialog, int item) {


                    mRepeatType = items[item];
                    mRepeatTypeText.setText(mRepeatType);
                    mRepeatText.setText("Every " + mRepeatNo + " " + mRepeatType + "(s)");

                }

        });

        AlertDialog alert = builder.create();
        alert.show();
    }


    //on clicking repeat interval button
    public void setRepeatNo(View v) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Enter Number");

        //create EditText box to input repeat number
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        alert.setView(input);
        alert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        if (input.getText().toString().length() == 0) {
                            mRepeatNo = Integer.toString(1);
                            mRepeatNoText.setText(mRepeatNo);
                            mRepeatText.setText("Every " + mRepeatNo + " " + mRepeatType + "(s)");
                        } else {
                            mRepeatNo = input.getText().toString().trim();
                            mRepeatNoText.setText(mRepeatNo);
                            mRepeatText.setText("Every " + mRepeatNo + " " + mRepeatType + "(s)");
                        }
                    }
                });

        alert.setNegativeButton("Cancel", ((dialog, which) -> {
            //do nothing
        }));
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*Inflate the menu options from the res/menu/menueditor.xml file,
         * this adds menu items to the app bar*/

        getMenuInflater().inflate(R.menu.menu_add_reminder, menu);

        return true;
    }

    /*This method is called afret invalidateOptionsMenu(), so that the menu can be updated
     * (some menu items can be hidden or visible).*/

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        //if this is a new reminder, hide the "Delete" menu item.
        if (mCurrentReminderUri == null) {
            MenuItem menuItem = menu.findItem(R.id.discard_reminder);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        //user clicked on a menu options in the app bar overflow menu
        switch (item.getItemId()) {
            //respond to a click on the "Save" menu option
            case R.id.save_reminder:

                if (mTitleText.getText().toString().length() == 0) {
                    mTitleText.setError("Aşkom boşluğu mu hatırlıycan bi başlık ekle!");
                    Toast.makeText(this, "Aşkom boşluğu mu hatırlıycan bi başlık ekle!", Toast.LENGTH_SHORT).show();
                } else {
                    saveReminder();
                    finish();
                }
                return true;
            //respond to a click on the "Delete" menu option
            case R.id.discard_reminder:
                //popup confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                /*if the reminder hasnt changed, continue with navigating up to parent activity
                 * which is the MainActivity*/
                if (!mVehicleHasChanged) {
                    NavUtils.navigateUpFromSameTask(AddReminderActivity.this);
                    return true;

                }

                /*Otherwise if there are unsaved changes, setup a dialog to warn the user.
                 * Create a click listener to handle the user confirming that
                 * changes should be discarded*/
                DialogInterface.OnClickListener discardButtonClickListener =
                        ((dialogInterface, i) -> {
                            //user clicked "Discard" button, navigate to parent activity
                            NavUtils.navigateUpFromSameTask(AddReminderActivity.this);
                        });
                //show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }


        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        /*Create an AlertDialog.Builder and set the message, and click listeners
         * for the positive and negative buttons on the dialog.*/
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Değişiklikleri kaydedip çıkmak istediğine emin misin aşkom ?");
        builder.setPositiveButton("Sil dedim mi sil", discardButtonClickListener);
        builder.setNegativeButton("Bi düşünim ya", (dialog, id) -> {

            /*user clicked the "Bi düşünim ya" button, so dismiss the dialog
             * and continue editing the reminder */
            if (dialog != null) {
                dialog.dismiss();
            }

        });

        //create and show AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void showDeleteConfirmationDialog() {
        /*Create an AlertDialog.Builder and set the message, and click listeners
         * for the positive and negative buttons on the dialog.*/
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Bu hatırlatıcıyı silmek istediğine emin misin aşkom ?");
        builder.setPositiveButton("Sil dedim mi sil", (dialog, id) -> {

            //user clicked the "Sil dedim mi sil" button, so delete the reminder

            deleteReminder();
        });

        builder.setNegativeButton("Dursun ya da ya", (dialog, id) -> {

            /*user clicked the "Dursun ya da ya" button, so dismiss the dialog
             * and continue editing the reminder */
            if (dialog != null) {
                dialog.dismiss();
            }
        });


        //create and show AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }


    private void deleteReminder() {
        //only perform the delete if this is an existing reminder

        if (mCurrentReminderUri != null) {
            /*Call the ContentResolver to delete the reminder at the given content URI.
             * Pass in null for the selection and selection args cuz the mCuttentReminderUri
             * content URI already identifes the reminder that we want.
             * */

            int rowsDeleted = getContentResolver().delete(mCurrentReminderUri, null, null);

            //Show a toast message depending on whether or not the delete was successful
            if (rowsDeleted == 0) {
                //if no rows were deleted, then there was an error with the delete
                Toast.makeText(this, "Hatırlatıcıyı silerken bişi oldu", Toast.LENGTH_SHORT).show();

            } else {
                //otherwise, the delete was successful and we can display a toast
                Toast.makeText(this, "Sildim", Toast.LENGTH_SHORT).show();
            }

        }

        //close the activity
        finish();

    }

    //on clicking the save button
    public void saveReminder() {

        /*if(mCurrentReminderUri == null){
         * //Since no fields were modified, we can return early without creating a new reminder.
         * //no need to create ContentValues and no need to do ant ContentPRovider operations.
         * return;
         * }
         * */


        ContentValues values = new ContentValues();

        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_TITLE, mTitle);
        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_DATE, mDate);
        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_TIME, mTime);
        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT, mRepeat);
        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_NO, mRepeatNo);
        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_TYPE, mRepeatType);
        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE, mActive);

//set up canlendar for creating the notification

        mCalendar.set(Calendar.MONTH, --mMonth);
        mCalendar.set(Calendar.YEAR, mYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, mDay);
        mCalendar.set(Calendar.HOUR_OF_DAY, mHour);
        mCalendar.set(Calendar.MINUTE, mMinute);
        mCalendar.set(Calendar.SECOND, 0);


        long selectedTimestamp = mCalendar.getTimeInMillis();

//check repeat type

        if (mRepeatType.equals("Minute")) {
            mRepeatTime = Integer.parseInt(mRepeatNo) * milMinute;
        } else if (mRepeatType.equals("Hour")) {
            mRepeatTime = Integer.parseInt(mRepeatNo) * milHour;
        } else if (mRepeatType.equals("Day")) {
            mRepeatTime = Integer.parseInt(mRepeatNo) * milDay;
        } else if (mRepeatType.equals("Week")) {
            mRepeatTime = Integer.parseInt(mRepeatNo) * milWeek;
        } else if (mRepeatType.equals("Month")) {
            mRepeatTime = Integer.parseInt(mRepeatNo) * milMonth;
        }

        if (mCurrentReminderUri == null) {
            /*This is a NEW reminder so insert a new reminder into proivder,
             * returning the content URI for the new reminder.*/
            Uri newUri = getContentResolver().insert(AlarmReminderContract.AlarmReminderEntry.CONTENT_URI, values);

            //show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                //If the next content URI is null, then there was an error with  insertion.
                Toast.makeText(this, "hatırlatıcıyı kaydederken bişi oldu", Toast.LENGTH_SHORT).show();
            } else {
                //otherwise , the insertion was successful and we can display a toast.
                Toast.makeText(this, "Hatırlatıcıyı kaydettim aşkom", Toast.LENGTH_SHORT).show();
            }
        } else {

            int rowsAffected = getContentResolver().update(mCurrentReminderUri, values, null, null);

            //show a toast message depending on whether or not the update was successful
            if (rowsAffected == 0) {
                //If no rows were affected, then there was an error with the update.
                Toast.makeText(this, "hatırlatıcıyı updatelerken bişi oldu", Toast.LENGTH_SHORT).show();
            } else {
                //otherwise , the update was successful and we can display a toast.
                Toast.makeText(this, "Hatırlatıcıyı updateledim aşkom", Toast.LENGTH_SHORT).show();
            }
        }

        //create a new notification
        if (mActive.equals("true")) {
            if (mRepeat.equals("true")) {
                new AlarmScheduler().setRepeatAlarm(getApplicationContext(), selectedTimestamp, mCurrentReminderUri, mRepeatTime);
            } else if (mRepeat.equals("false")) {
                new com.knt.waterreminder.reminder.AlarmScheduler().setAlarm(getApplicationContext(), selectedTimestamp, mCurrentReminderUri);
            }
            Toast.makeText(this, "Alarm zamanı: " + selectedTimestamp, Toast.LENGTH_LONG).show();
        }

        Toast.makeText(getApplicationContext(), "Kaydedildi aşkom", Toast.LENGTH_SHORT).show();

    }

    //On pressing the back button
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

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


        //This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, /*parent activity context*/
                mCurrentReminderUri, /*query the content URI for the current reminder*/
                projection, /*columns to include in the resulting Cursor*/
                null, /*no selection clause*/
                null, /*no selection atguments*/
                null); /*default sort order*/
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }


        //procedd with moving to the first row of the cursor and reading data from it
        //(This should be the only row in the cursor)

        if (cursor.moveToFirst()) {
            int titleColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_TITLE);
            int dateColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_DATE);
            int timeColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_TIME);
            int repeatColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT);
            int repeatNoColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_NO);
            int repeatTypeColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_TYPE);
            int activeColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE);


            //Extract out the value from the Cursor for the given column index
            String title = cursor.getString(titleColumnIndex);
            String date = cursor.getString(dateColumnIndex);
            String time = cursor.getString(timeColumnIndex);
            String repeat = cursor.getString(repeatColumnIndex);
            String repeatNo = cursor.getString(repeatNoColumnIndex);
            String repeatType = cursor.getString(repeatTypeColumnIndex);
            String active = cursor.getString(activeColumnIndex);


            //update the views on the screen with the values from the database
            mTitleText.setText(title);
            mDateText.setText(date);
            mTimeText.setText(time);
            mRepeatNoText.setText(repeatNo);
            mRepeatTypeText.setText(repeatType);
            mRepeatText.setText("Every " + repeatNo + " " + repeatType + "(s)");
            //setup active buttons
            //setup repeat switch
            if (repeat.equals("false")) {
                mRepeatSwitch.setChecked(false);
                mRepeatText.setText("off");
            } else if (repeat.equals("true")) {
                mRepeatSwitch.setChecked(true);
            }

        }


    }


    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}