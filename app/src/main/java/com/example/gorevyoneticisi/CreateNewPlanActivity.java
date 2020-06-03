package com.example.gorevyoneticisi;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class CreateNewPlanActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference planRef;
    private ProgressDialog progressDialog;
    private TextView startDate, endDate, startTime, endTime;
    private EditText title, note;
    private Button createBtn;
    private String selectedStartDate, selectedEndDate, titleTxt, noteTxt, selectedStartTime, selectedEndTime;
    private String planId;
    private int mYear, mMonth, mDay, mHour, mMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_plan);

        planRef = FirebaseDatabase.getInstance().getReference().child("Plans");

        startDate = (TextView) findViewById(R.id.create_plan_start_date);
        endDate = (TextView) findViewById(R.id.create_plan_end_date);
        title = (EditText) findViewById(R.id.create_plan_title);
        note = (EditText) findViewById(R.id.create_plan_note);
        startTime = (TextView) findViewById(R.id.create_plan_start_time);
        endTime = (TextView) findViewById(R.id.create_plan_end_time);
        createBtn = (Button) findViewById(R.id.popup_create_plan_btn);

        progressDialog = new ProgressDialog(this);
        getWindow().setLayout(980, 750);        //popup screen size

        database = FirebaseDatabase.getInstance();
        planId = database.getReference("id").push().getKey();       //create plan id

        datePicker();
        timePicker();

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                titleTxt = title.getText().toString();
                noteTxt = note.getText().toString();

                validation();

            }
        });

    }

    private void validation() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
        Date currentDate = new Date();

        try {

            Date currentDateFin = dateFormatter.parse(dateFormatter.format(currentDate));
            Date currentTimeFin = timeFormatter.parse(timeFormatter.format(currentDate));
            //if any field is empty
            if (selectedStartDate != null && selectedEndDate != null && selectedStartTime != null) {
                Date startDateGmt = dateFormatter.parse(selectedStartDate);
                Date endDateGmt = dateFormatter.parse(selectedEndDate);
                Date startTimeGmt = timeFormatter.parse(selectedStartTime);

                if (startDateGmt.before(currentDateFin)) {          //if start date before current date

                    Toast.makeText(getApplicationContext(), "You can't pick a past date!", Toast.LENGTH_SHORT).show();
                    startDate.setText("dd/mm/yyyy");
                    selectedStartDate = null;
                }
                else if (endDateGmt.before(currentDateFin)) {             //if end date before current date

                    Toast.makeText(getApplicationContext(), "You can't pick a past date!", Toast.LENGTH_SHORT).show();
                    endDate.setText("dd/mm/yyyy");
                    selectedEndDate = null;
                }
                else if (endDateGmt.before(startDateGmt)) {              //if end date before start date
                    Toast.makeText(getApplicationContext(), "End date can't be before start date!", Toast.LENGTH_SHORT).show();
                    endDate.setText("dd/mm/yyyy");
                    selectedEndDate = null;
                }
                else if (startDateGmt.equals(endDateGmt)) {      //if start date equals to end date

                    boolean b = startDateGmt.equals(currentDateFin) && endDateGmt.equals(currentDateFin);       //if start and end dates equal to current date
                    boolean check = false;

                    if (selectedEndTime != null) {
                        Date endTimeGmt = timeFormatter.parse(selectedEndTime);

                        check = true;

                        if (endTimeGmt.before(startTimeGmt) || endTimeGmt.equals(startTimeGmt)) {        //if end time before or equals to start time
                            Toast.makeText(getApplicationContext(), "End time can't be before or equals to start time!", Toast.LENGTH_SHORT).show();
                            endTime.setText("hh:mm");
                            selectedEndTime = null;
                        }
                        else if (b) {        //if start and end dates equal to current date

                            if (endTimeGmt.before(currentTimeFin) || endTimeGmt.equals(currentTimeFin)) {      //if end time before or equals to current time
                                Toast.makeText(getApplicationContext(), "End time can't be before or equal to current time!", Toast.LENGTH_SHORT).show();
                                endTime.setText("hh:mm");
                                selectedEndTime = null;
                            }
                            else if (startTimeGmt.before(currentTimeFin) || startTimeGmt.equals(currentTimeFin)) {       //if start time before or equals to current time
                                Toast.makeText(getApplicationContext(), "Start time can't be before or equal to current time!", Toast.LENGTH_SHORT).show();
                                startTime.setText("hh:mm");
                                selectedStartTime = null;
                            }
                            else {
                                addDatabase();
                            }

                        }
                        else{
                            addDatabase();
                        }
                    }

                    if (b) {          //if start and end dates equal to current date
                        if (startTimeGmt.before(currentTimeFin) || startTimeGmt.equals(currentTimeFin)) {       //if start time before or equals to current time
                            Toast.makeText(getApplicationContext(), "Start time can't be before or equal to current time!", Toast.LENGTH_SHORT).show();
                            startTime.setText("hh:mm");
                            selectedStartTime = null;
                        }
                        else {
                            if (!check){        //if enter to (selectedEndTime != null) condition, do not call addDatabase method!
                                addDatabase();
                            }

                        }
                    }
                    else{
                        addDatabase();
                    }

                }
                else {
                    addDatabase();
                }

            }
            else {
                Toast.makeText(getApplicationContext(), "Please fill the fields!", Toast.LENGTH_SHORT).show();
            }


        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void addDatabase() {
        if (selectedEndTime == null) {
            selectedEndTime = "";
        }

        if (titleTxt.isEmpty() || noteTxt.isEmpty()) {         //if title or note is empty
            Toast.makeText(getApplicationContext(), "Please fill the fields!", Toast.LENGTH_SHORT).show();
        }
        else {

            HashMap<String, Object> planMap = new HashMap<>();
            planMap.put("startDate", selectedStartDate);
            planMap.put("endDate", selectedEndDate);
            planMap.put("title", titleTxt);
            planMap.put("note", noteTxt);
            planMap.put("startTime", selectedStartTime);
            planMap.put("endTime", selectedEndTime);

            planRef.child(planId).updateChildren(planMap);          //add plan to datebase

            Toast.makeText(getApplicationContext(), "Plan is created", Toast.LENGTH_SHORT).show();

            finish();       //close popup
        }
    }

    private void datePicker() {

        startDate.setOnClickListener(new View.OnClickListener() {           //When press dd/mm/yyyy, open time picker
            @Override
            public void onClick(View v) {

                // Get Current Date
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(CreateNewPlanActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                startDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);       //set startDate text

                                selectedStartDate = startDate.getText().toString();         //get startDate text

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {         //When press dd/mm/yyyy, open time picker
            @Override
            public void onClick(View v) {

                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(CreateNewPlanActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                endDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);     //set endDate text

                                selectedEndDate = endDate.getText().toString();         //get endDate text

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

    }

    private void timePicker() {

        startTime.setOnClickListener(new View.OnClickListener() {          //When press hh:mm, open time picker
            @Override
            public void onClick(View v) {

                // Get Current TimePicker
                final Calendar c = Calendar.getInstance();
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);

                // Launch TimePicker Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(CreateNewPlanActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {

                                startTime.setText(hourOfDay + ":" + minute);       //set startTime textview

                                selectedStartTime = startTime.getText().toString();         //get startTime text

                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {          //When press hh:mm, open time picker
            @Override
            public void onClick(View v) {

                // Get Current TimePicker
                final Calendar c = Calendar.getInstance();
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);

                // Launch TimePicker Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(CreateNewPlanActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {

                                endTime.setText(hourOfDay + ":" + minute);       //set endTime textview

                                selectedEndTime = endTime.getText().toString();         //get endTime text

                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();
            }
        });

    }
}