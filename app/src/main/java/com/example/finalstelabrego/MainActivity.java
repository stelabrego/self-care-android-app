package com.example.finalstelabrego;

import android.content.ContentValues;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

// Declare necessary Views
ImageView smile;
SeekBar seekBar;
EditText editLifeEvents;
EditText editFuturePlans;
Button breatheButton;
TextView countdown;
Button saveButton;
Button menuButton;

// Other necessary values for class
boolean breathingCompleted = false;
boolean breathingInProgress = false;
CountDownTimer breathingTimer;
SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the Views being programmatically interacted with.
        smile = findViewById(R.id.smile);
        seekBar = findViewById(R.id.seekBar);
        editLifeEvents = findViewById(R.id.editLifeEvents);
        editFuturePlans = findViewById(R.id.editFuturePlans);
        breatheButton = findViewById(R.id.breatheButton);
        countdown = findViewById(R.id.countdown);
        saveButton = findViewById(R.id.saveButton);
        menuButton = findViewById(R.id.menuButton);

        // Just to be sure everthing is reset, set Views to default state
        resetForm();

        // Give menu button on bottom of Activity a name and intent to start
        menuButton.setText("Past Entries");
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, PastEntries.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

        // set listener on seek bar to update the face that is displayed
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateSmile();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // set listener on "BREATHE" button to start the countdown timer
        breatheButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Only set a new timer if the breathing hasn't started yet
                if (!breathingInProgress && !breathingCompleted) {
                    breathingInProgress = true;
                    breathingTimer = new CountDownTimer(300000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            // Get minutes and seconds in string format
                            String minutes = String.valueOf(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished));
                            String seconds = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                            // Put a leading zero on the seconds when they get to a single digit
                            if (seconds.length() == 1) {
                                seconds = "0" + seconds;
                            }
                            String output = minutes + ":" + seconds;
                            // Set text on the countdown timer TextView
                            countdown.setText(output);
                        }

                        @Override
                        public void onFinish() {
                            // Change countdown TextView background from red to green
                            countdown.setBackgroundResource(R.color.countdownOver);
                            breathingCompleted = true;
                            breathingInProgress = false;
                        }
                    }.start();
                }
            }
        });
        // Set listener on "SAVE ENTRY" button to store data in local database
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEntry();
            }
        });

        // Initialize the database
        DBHelper myDbHelper = new DBHelper(this);
        try {
            myDbHelper.createDatabase();
        } catch (IOException e) {
            throw new Error("Unable to create database");
        }

        try {
            myDbHelper.openDatabase();
        } catch (SQLException e) {

        }
        db = myDbHelper.getWritableDatabase();
    }


    // This method resets the interactive parts of the activity to the default state
    void resetForm() {
        // Set slider to the middle
        seekBar.setProgress(50, true);
        updateSmile();
        // Clear long answer EditText Views
        editFuturePlans.setText("");
        editLifeEvents.setText("");
        // If there's an active countdown timer, get rid of it and reset display to "5:00"
        if (breathingInProgress) breathingTimer.cancel();
        countdown.setText("5:00");
        // Make countdown TextView background red
        countdown.setBackgroundResource(R.color.countdownNotOver);
        breathingCompleted = false;
        breathingInProgress = false;
    }

    // This method looks at the seekbar's progress and sets the emoji to either upset, sad,
    // indifferent, happy, or joyous depending on the value
    void updateSmile() {
        int floorDiv = Math.floorDiv(seekBar.getProgress(), 20);
        switch (floorDiv) {
            case 0:
                smile.setImageDrawable(getDrawable(R.drawable.smile1));
                break;
            case 1:
                smile.setImageDrawable(getDrawable(R.drawable.smile2));
                break;
            case 2:
                smile.setImageDrawable(getDrawable(R.drawable.smile3));
                break;
            case 3:
                smile.setImageDrawable(getDrawable(R.drawable.smile4));
                break;
            case 4:
                smile.setImageDrawable(getDrawable(R.drawable.smile5));
        }
    }

    // This method reads the data from the form and saves it to a local SQLiteDatabase
    void saveEntry() {
        String date = getCurrentLocalDateTimeStamp();
        int mood = seekBar.getProgress();
        String feelings = String.valueOf(editLifeEvents.getText());
        String future = String.valueOf(editFuturePlans.getText());
        // Must store booleans as 1 or 0 because SQLite doesn't support booleans
        int breathed = breathingCompleted ? 1 : 0;
        ContentValues cv = new ContentValues();
        cv.put("Date", date);
        cv.put("Mood", mood);
        cv.put("Feelings", feelings);
        cv.put("Future", future);
        cv.put("Breathed", breathed);
        db.insert("Entries", null, cv);
        db.close();
        resetForm();
    }

    // Returns the current time in a readable format to save with entry in database. Used
    // in Spinner to select past entries in the PastEntries activity
    String getCurrentLocalDateTimeStamp() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm a"));
    }
}
