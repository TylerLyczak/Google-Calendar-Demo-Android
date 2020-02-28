package com.tylerlyczak.googlecalanderdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.icu.util.TimeZone;
import android.net.ParseException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private void addToDeviceCalendar(String startDate,String endDate, String title,String description, String location) {

        // Make the strings for the formatted times
        String startDateFormatted = "";
        String endDateFormatted = "";

        // Get a calendar instance
        GregorianCalendar calDate = new GregorianCalendar();

        // Formats used to format the date into the correct format
        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy,MM,dd,HH,mm");

        // Convert the dates given to the correct format
        try {
            // Convert date to the first format
            Date date = originalFormat.parse(startDate);
            Date eDate = originalFormat.parse(endDate);

            // Convert it into the second format
            startDateFormatted = targetFormat.format(date);
            endDateFormatted = targetFormat.format(eDate);

        } catch (ParseException ex) {} catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        // Makes an ArrayList of the date split into different sections
        ArrayList<String> startDateList = new ArrayList<String>(Arrays.asList(startDateFormatted.split(",")));
        ArrayList<String> endDateList = new ArrayList<String>(Arrays.asList(endDateFormatted.split(",")));

        // Converts the strings of the start and end list into ints
        int startYear = Integer.parseInt(startDateList.get(0));
        int startMonth = Integer.parseInt(startDateList.get(1));
        int startDay = Integer.parseInt(startDateList.get(2));
        int startHour = Integer.parseInt(startDateList.get(3));
        int startMinute = Integer.parseInt(startDateList.get(4));

        int endYear = Integer.parseInt(endDateList.get(0));
        int endMonth = Integer.parseInt(endDateList.get(1));
        int endDay = Integer.parseInt(endDateList.get(2));
        int endHour = Integer.parseInt(endDateList.get(3));
        int endMinute = Integer.parseInt(endDateList.get(4));

        // Sets the date and gets the time in milliseconds
        calDate.set(startYear, startMonth-1, startDay, startHour, startMinute);
        long startMillis = calDate.getTimeInMillis();

        // Sets the date and gets the time in milliseconds
        calDate.set(endYear, endMonth-1, endDay, endHour, endMinute);
        long endMillis = calDate.getTimeInMillis();

        try {
            // Puts the values into a event for the calendar
            ContentResolver cr = this.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);
            values.put(CalendarContract.Events.TITLE, title);
            values.put(CalendarContract.Events.DESCRIPTION, description);
            values.put(CalendarContract.Events.EVENT_LOCATION,location);
            values.put(CalendarContract.Events.HAS_ALARM,1);
            values.put(CalendarContract.Events.CALENDAR_ID, 1);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, Calendar.getInstance()
                    .getTimeZone().getID());
            System.out.println(Calendar.getInstance().getTimeZone().getID());

            // If the user still hasn't given permission, then re-ask
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                // Ask the user for permission and then re-run
                Log.i("No Permission", "No Permission");
                Toast.makeText(this, "Failed to give permission, try again", Toast.LENGTH_SHORT).show();
                int requestCode = 1;
                requestPermissions(new String[]{"android.permission.WRITE_CALENDAR", "android.permission.READ_CALENDAR"}, requestCode);
                return;
            }
            // Add the event to the calendar
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

            long eventId = Long.parseLong(uri.getLastPathSegment());
            Log.i("Event_Id", String.valueOf(eventId));

            try {
                values.clear();
                values.put(CalendarContract.Reminders.EVENT_ID, eventId);
                values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
                values.put(CalendarContract.Reminders.MINUTES, 5);
                getContentResolver().insert(CalendarContract.Reminders.CONTENT_URI, values);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Toast.makeText(this, "Now check Google Calendar, you may need to refresh it, it takes time", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onMakeEvent (View view) {
        // Get the EditText views by ID
        EditText startText = (EditText) findViewById(R.id.startText);
        EditText endText = (EditText) findViewById(R.id.endText);
        EditText titleText = (EditText) findViewById(R.id.titleText);
        EditText descText = (EditText) findViewById(R.id.descText);
        EditText locText = (EditText) findViewById(R.id.locText);

        // Converts the EditTexts to Strings
        String startDate = startText.getText().toString();
        String endDate = endText.getText().toString();
        String titleString = titleText.getText().toString();
        String descString = descText.getText().toString();
        String locString = locText.getText().toString();

        // Calls the function to add the event to the calender
        addToDeviceCalendar(startDate, endDate, titleString, descString, locString);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // This is how we request the user for Calendar write and read permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // Ask the user for permission and then re-run
            Log.i("No Permission", "No Permission");
            int requestCode = 1;
            // Re-request to ask for permission
            requestPermissions(new String[]{"android.permission.WRITE_CALENDAR", "android.permission.READ_CALENDAR"}, requestCode);
        }
    }
}
