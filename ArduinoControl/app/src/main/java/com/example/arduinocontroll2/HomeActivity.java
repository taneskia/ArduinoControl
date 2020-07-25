package com.example.arduinocontroll2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    private Calendar calendarStart, calendarEnd;
    private Button dateButtonStart, dateButtonEnd, timeButtonStart, timeButtonEnd, setButton, setDailyButton, logoutButton;
    private ImageButton refreshButton;
    private TextView startDateText, startTimeText, endDateText, endTimeText, currentStateDisplay, signedinUserText;
    private Switch manualSwitch;
    private CheckBox setManuallyCheck;

    private Date start;
    private Date end;
    private Boolean daily;
    private Boolean manualEnabled;
    private Boolean switchOn;
    private DatabaseReference startDateTimeRef, endDateTimeRef, dailyRef, manualEnabledRef, switchOnRef;

    private FirebaseDatabase database;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        startDateTimeRef = database.getReference().child("StartDateTime");
        endDateTimeRef = database.getReference().child("EndDateTime");
        dailyRef = database.getReference().child("Daily");
        manualEnabledRef = database.getReference().child("ManualControl").child("Enabled");
        switchOnRef = database.getReference().child("ManualControl").child("Value");

        calendarStart = Calendar.getInstance();
        calendarEnd = Calendar.getInstance();

        dateButtonStart = findViewById(R.id.dateButtonStart);
        dateButtonEnd = findViewById(R.id.dateButtonEnd);
        timeButtonStart = findViewById(R.id.timeButtonStart);
        timeButtonEnd = findViewById(R.id.timeButtonEnd);
        setButton = findViewById(R.id.setButton);
        setDailyButton = findViewById(R.id.setDailyButton);
        logoutButton = findViewById(R.id.logoutButton);
        refreshButton = findViewById(R.id.refreshButton);

        startDateText = findViewById(R.id.startDateText);
        startTimeText = findViewById(R.id.startTimeText);
        endDateText = findViewById(R.id.endDateText);
        endTimeText = findViewById(R.id.endTimeText);
        currentStateDisplay = findViewById(R.id.currentStateDisplay);
        signedinUserText = findViewById(R.id.signedinUserText);
        signedinUserText.setText(mAuth.getCurrentUser().getEmail());

        manualSwitch = findViewById(R.id.manualOnOffSwitch);

        setManuallyCheck = findViewById(R.id.setManuallyCheck);

        dateButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleDateButtonStart();
            }
        });
        dateButtonEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleDateButtonEnd();
            }
        });
        timeButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleTimeButtonStart();
            }
        });
        timeButtonEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleTimeButtonEnd();
            }
        });
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCurrentStateDisplay();
            }
        });
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSetButton();
            }
        });
        setDailyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSetDailyButton();
            }
        });

        manualSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateManualControl(true);
            }
        });

        setManuallyCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                handleSetManuallyCheck(isChecked);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        startDateTimeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                try {
                    start = df.parse(dataSnapshot.getValue(String.class));

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                updateCurrentStateDisplay();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
        endDateTimeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                try {
                    end = df.parse(dataSnapshot.getValue(String.class));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                updateCurrentStateDisplay();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
        dailyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                daily = dataSnapshot.getValue(Integer.class) == 1;
                updateCurrentStateDisplay();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
        manualEnabledRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                manualEnabled = dataSnapshot.getValue(Integer.class) == 1;
                updateCurrentStateDisplay();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
        switchOnRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                switchOn = dataSnapshot.getValue(Integer.class) == 1;
                updateCurrentStateDisplay();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });

        Thread currentStateUpdater = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    updateCurrentStateDisplay();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        currentStateUpdater.start();
    }


    private void handleDateButtonStart() {
        Calendar currentCalendar = Calendar.getInstance();
        int YEAR = currentCalendar.get(Calendar.YEAR);
        int MONTH = currentCalendar.get(Calendar.MONTH);
        int DATE = currentCalendar.get(Calendar.DATE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int date) {
                calendarStart.set(Calendar.YEAR, year);
                calendarStart.set(Calendar.MONTH, month);
                calendarStart.set(Calendar.DATE, date);

                startDateText.setText(startDateText.getHint() + ": " + DateFormat.format("dd.MM.yyyy", calendarStart).toString());
            }
        }, YEAR, MONTH, DATE);

        datePickerDialog.show();
    }

    private void handleDateButtonEnd() {
        Calendar currentCalendar = Calendar.getInstance();
        int YEAR = currentCalendar.get(Calendar.YEAR);
        int MONTH = currentCalendar.get(Calendar.MONTH);
        int DATE = currentCalendar.get(Calendar.DATE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int date) {
                calendarEnd.set(Calendar.YEAR, year);
                calendarEnd.set(Calendar.MONTH, month);
                calendarEnd.set(Calendar.DATE, date);

                endDateText.setText(endDateText.getHint() + ": " + DateFormat.format("dd.MM.yyyy", calendarEnd).toString());
            }
        }, YEAR, MONTH, DATE);

        datePickerDialog.show();
    }

    private void handleTimeButtonStart() {
        Calendar currentCalendar = Calendar.getInstance();
        int HOUR = currentCalendar.get(Calendar.HOUR_OF_DAY);
        int MINUTE = currentCalendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                calendarStart.set(Calendar.HOUR_OF_DAY, hour);
                calendarStart.set(Calendar.MINUTE, minute);

                startTimeText.setText(startTimeText.getHint() + ": " + DateFormat.format("HH:mm", calendarStart).toString());
            }
        }, HOUR, MINUTE, true);

        timePickerDialog.show();
    }

    private void handleTimeButtonEnd() {
        Calendar currentCalendar = Calendar.getInstance();
        int HOUR = currentCalendar.get(Calendar.HOUR_OF_DAY);
        int MINUTE = currentCalendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                calendarEnd.set(Calendar.HOUR_OF_DAY, hour);
                calendarEnd.set(Calendar.MINUTE, minute);
                endTimeText.setText(endTimeText.getHint() + ": " + DateFormat.format("HH:mm", calendarEnd).toString());
            }
        }, HOUR, MINUTE, true);

        timePickerDialog.show();
    }

    private void handleSetButton() {
        //checking if all required fields are filled
        if (startDateText.getText().toString().equals("") ||
                endDateText.getText().toString().equals("") ||
                startTimeText.getText().toString().equals("") ||
                endTimeText.getText().toString().equals("")) {
            Toast.makeText(getApplication().getBaseContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        updateDateTime(); //setting new datetimes in database
    }

    private void handleSetDailyButton() {
        if (startDateText.getText().toString().equals("") ||
                endDateText.getText().toString().equals("") ||
                startTimeText.getText().toString().equals("") ||
                endTimeText.getText().toString().equals("")) {
            Toast.makeText(getApplication().getBaseContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        updateTime(); //setting new datetimes in database
    }

    private void handleSetManuallyCheck(boolean isChecked) {
        dateButtonStart.setEnabled(!isChecked);
        dateButtonEnd.setEnabled(!isChecked);
        timeButtonStart.setEnabled(!isChecked);
        timeButtonEnd.setEnabled(!isChecked);
        setButton.setEnabled(!isChecked);
        setDailyButton.setEnabled(!isChecked);

        if (isChecked) {
            startDateText.setText("");
            startTimeText.setText("");
            endDateText.setText("");
            endTimeText.setText("");
            currentStateDisplay.setText("");
        }
        if (!isChecked) {
            manualSwitch.setChecked(false);
        }
        manualSwitch.setEnabled(isChecked);

        updateManualControl(isChecked);
    }

    private void updateDateTime() {  //use only after all fields are filled!!
        //set seconds of start and end times to 0
        calendarStart.set(Calendar.SECOND, 0);
        calendarEnd.set(Calendar.SECOND, 0);

        DatabaseReference startDateTime = database.getReference().child("StartDateTime");
        DatabaseReference endDateTime = database.getReference().child("EndDateTime");
        DatabaseReference manualControl = database.getReference().child("ManualControl");

        startDateTime.setValue(DateFormat.format("yyyy-MM-dd'T'HH:mm:ss'Z'", calendarStart).toString());
        endDateTime.setValue(DateFormat.format("yyyy-MM-dd'T'HH:mm:ss'Z'", calendarEnd).toString());
        manualControl.child("Enabled").setValue(0);
        manualControl.child("Value").setValue(0);
        DatabaseReference daily_ref = database.getReference().child("Daily");
        daily_ref.setValue(0);
    }

    private void updateTime() {
        calendarStart.set(Calendar.SECOND, 0);
        calendarEnd.set(Calendar.SECOND, 0);

        DatabaseReference startDateTime = database.getReference().child("StartDateTime");
        DatabaseReference endDateTime = database.getReference().child("EndDateTime");
        DatabaseReference manualControl = database.getReference().child("ManualControl");
        DatabaseReference daily_ref = database.getReference().child("Daily");

        startDateTime.setValue(DateFormat.format("yyyy-MM-dd'T'HH:mm:ss'Z'", calendarStart).toString());
        endDateTime.setValue(DateFormat.format("yyyy-MM-dd'T'HH:mm:ss'Z'", calendarEnd).toString());
        manualControl.child("Enabled").setValue(0);
        manualControl.child("Value").setValue(0);
        daily_ref.setValue(1);
    }

    private void updateManualControl(boolean enable) {
        DatabaseReference manualControlRef = database.getReference().child("ManualControl");
        if (enable) {
            manualControlRef.child("Enabled").setValue(1);
            manualControlRef.child("Value").setValue(manualSwitch.isChecked() == true ? 1 : 0);
        } else {
            manualControlRef.child("Enabled").setValue(0);
            manualControlRef.child("Value").setValue(0);
        }
    }

    private void updateCurrentStateDisplay() {
        //TODO: implement update screen

        if (manualEnabled == null || switchOn == null || start == null || end == null || daily == null) {
            currentStateDisplay.setText("Loading...");
            return;
        }
        if (manualEnabled) {
            if (switchOn) {
                currentStateDisplay.setText("MANUALLY ON");
                currentStateDisplay.setTextColor(Color.GREEN);
            } else {
                currentStateDisplay.setText("MANUALLY OFF");
                currentStateDisplay.setTextColor(Color.RED);
            }
        } else {
            Date currentDate = Calendar.getInstance().getTime();

            if (daily) {
                start.setYear(currentDate.getYear());
                start.setMonth(currentDate.getMonth());
                start.setDate(currentDate.getDate());
                end.setYear(currentDate.getYear());
                end.setMonth(currentDate.getMonth());
                end.setDate(currentDate.getDate());
            }

            if (currentDate.compareTo(start) >= 0 && currentDate.compareTo(end) <= 0) {
                {
                    if (daily) {
                        currentStateDisplay.setText("DAILY from " + DateFormat.format("HH:mm", start).toString() +
                                " until " + DateFormat.format("HH:mm", end).toString());
                        currentStateDisplay.setTextColor(Color.GREEN);
                    } else {
                        currentStateDisplay.setText("ON from " + DateFormat.format("HH:mm dd.MM.yyyy", start).toString() +
                                " until " + DateFormat.format("HH:mm dd.MM.yyyy", end).toString());
                        currentStateDisplay.setTextColor(Color.GREEN);
                    }

                }
            } else if (currentDate.compareTo(start) < 0 && currentDate.compareTo(end) < 0) {
                {
                    if (daily) {
                        currentStateDisplay.setText("DAILY from " + DateFormat.format("HH:mm", start).toString() +
                                " until " + DateFormat.format("HH:mm", end).toString());
                        currentStateDisplay.setTextColor(Color.parseColor("#b0cf00"));
                    } else {
                        currentStateDisplay.setText("SCHEDULED from " + DateFormat.format("HH:mm dd.MM.yyyy", start).toString() +
                                " until " + DateFormat.format("HH:mm dd.MM.yyyy", end).toString());
                        currentStateDisplay.setTextColor(Color.parseColor("#b0cf00"));
                    }

                }
            } else {
                if (daily) {
                    currentStateDisplay.setText("DAILY from " + DateFormat.format("HH:mm", start).toString() +
                            " until " + DateFormat.format("HH:mm", end).toString());
                    currentStateDisplay.setTextColor(Color.parseColor("#b0cf00"));
                } else {
                    currentStateDisplay.setText("OFF");
                    currentStateDisplay.setTextColor(Color.RED);
                }
            }
        }
    }

    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
