package com.example.finalstelabrego;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class PastEntries extends AppCompatActivity {

    // Declare necessary Views and other necessary values
    DBHelper myDbHelper;
    SQLiteDatabase db;
    Spinner spinner;
    ArrayAdapter<String> mySpinnerAdapter;
    TextView pastEntriesTitle;
    ArrayList<Entry> entries;
    LinearLayout entryDetails;
    ImageView smile2;
    TextView feelingsText;
    TextView futureText;
    TextView breathingText;
    Button menuButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_entries);

        // Initialize the Views being programmatically interacted with.
        spinner = findViewById(R.id.spinner);
        pastEntriesTitle = findViewById(R.id.pastEntriesTitle);
        entryDetails = findViewById(R.id.entryDetails);
        smile2 = findViewById(R.id.smile2);
        feelingsText = findViewById(R.id.feelingsText);
        futureText = findViewById(R.id.futureText);
        breathingText = findViewById(R.id.breathingText);
        menuButton = findViewById(R.id.menuButton);

        // Give menu button on bottom of Activity a name and intent to start
        menuButton.setText("New Entry");
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(PastEntries.this, MainActivity.class);
                PastEntries.this.startActivity(myIntent);
            }
        });

        // Initialize the database
        myDbHelper = new DBHelper(this);
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

        // fill spinner with last ten entries
        fillSpinner();
        // display the values of the selected spinner item (the last entry saved)
        updateEntryDetails();

        // Set listener for spinner to update the displayed values when the item selected changes
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateEntryDetails();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    // This method takes the last 10 entries saved chronologically and adds them to the spinner
    // with the most recent entry selected first
    public void fillSpinner() {
        String query = "select * from Entries ORDER BY _rowid_ DESC LIMIT 10";
        Cursor result = db.rawQuery(query, null);
        result.moveToFirst();
        int count = result.getCount();
        entries = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();
        // if there are no saved entries, display that there are none available
        if (count == 0) {
            spinner.setVisibility(View.INVISIBLE);
            entryDetails.setVisibility(View.INVISIBLE);
            pastEntriesTitle.setText("No Past Entries Available");
        } else {
            // If there are entries, make a new Entry object for each one and add them to the
            // entries ArrayList
            spinner.setVisibility(View.VISIBLE);
            entryDetails.setVisibility(View.VISIBLE);
            do {
                boolean breathed = result.getInt(3) == 1;
                entries.add(new Entry(result.getString(0), result.getString(1),
                        result.getString(2), breathed, result.getInt(4)));
            } while (result.moveToNext());
            // for each entry in entries, add the date to the titles ArrayList. This will be used
            // for the spinner
            for (Entry entry : entries) {
                titles.add(entry.date);
            }
            // Fill the spinner with the titles of the entries (which are the dates) and use the
            // custom spinner_item layout view
            mySpinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, titles);
            spinner.setAdapter(mySpinnerAdapter);
            result.close();
        }

    }

    // This method looks at what item is currently selected in the spinner and displays the Entry's
    // data such as mood, question responses, and whether the breathing timer was completed
    void updateEntryDetails() {
        if (entries.size() == 0) return;
        int index = spinner.getSelectedItemPosition();
        Entry selectedEntry = entries.get(index);
        updateSmile(selectedEntry);
        if (selectedEntry.feelings.length() == 0) {
            feelingsText.setText("N/A");
        } else {
            feelingsText.setText(selectedEntry.feelings);
        }
        if (selectedEntry.future.length() == 0) {
            futureText.setText("N/A");
        } else {
            futureText.setText(selectedEntry.future);
        }
        if (selectedEntry.breathing) {
            breathingText.setText("Yes! Great job.");
        } else {
            breathingText.setText("No, but that's OK!");
        }
    }

    // This method looks at the Entry's mood value and sets the emoji to either upset, sad,
    // indifferent, happy, or joyous depending on the value
    void updateSmile(Entry entry) {
        int floorDiv = Math.floorDiv(entry.mood, 20);
        switch (floorDiv) {
            case 0:
                smile2.setImageDrawable(getDrawable(R.drawable.smile1));
                break;
            case 1:
                smile2.setImageDrawable(getDrawable(R.drawable.smile2));
                break;
            case 2:
                smile2.setImageDrawable(getDrawable(R.drawable.smile3));
                break;
            case 3:
                smile2.setImageDrawable(getDrawable(R.drawable.smile4));
                break;
            case 4:
                smile2.setImageDrawable(getDrawable(R.drawable.smile5));
        }
    }


    // When the app is resumed, fill the spinner with the most recent entries again
    @Override
    protected void onResume() {
        super.onResume();
        // fill spinner with last ten entries
        fillSpinner();
        // display the values of the selected spinner item (the last entry saved)
        updateEntryDetails();
        System.out.println("Rewrote spinner");
    }
}
