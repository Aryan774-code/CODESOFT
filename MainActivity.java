package com.example.todolistapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.database.Cursor;
import android.app.AlarmManager;
import android.app.PendingIntent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Button add;
    AlertDialog dialog;
    LinearLayout layout;

    String selectedDate = "";
    String selectedTime = "";
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);

        add = findViewById(R.id.add);
        layout = findViewById(R.id.container);

        buildDialog();
        loadTasks();
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        setupHourlyReminder();
    }

    public void buildDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog, null);

        final EditText name = view.findViewById(R.id.nameEdit);
        Button selectDateButton = view.findViewById(R.id.selectDateButton);
        Button selectTimeButton = view.findViewById(R.id.selectTimeButton);
        final Spinner prioritySpinner = view.findViewById(R.id.prioritySpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.priority_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(adapter);

        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        selectTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });

        builder.setView(view);
        builder.setTitle("Enter your Task")
                .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String priority = prioritySpinner.getSelectedItem().toString();
                        addCard(name.getText().toString(), selectedDate + " " + selectedTime, priority);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cancel button action
                    }
                });

        dialog = builder.create();
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                selectedTime = hourOfDay + ":" + (minute < 10 ? "0" + minute : minute);
            }
        }, hour, minute, true);
        timePickerDialog.show();
    }

    private void addCard(String name, String deadline, String priority) {
        if (db.addTask(name, deadline, priority)) {
            final View view = getLayoutInflater().inflate(R.layout.card, null);

            TextView nameView = view.findViewById(R.id.name);
            TextView deadlineView = view.findViewById(R.id.deadline);
            TextView priorityView = view.findViewById(R.id.priority);
            TextView timerView = view.findViewById(R.id.timer);
            Button delete = view.findViewById(R.id.delete);

            nameView.setText(name);
            deadlineView.setText("Deadline: " + deadline);
            priorityView.setText("Priority: " + priority);

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                Date deadlineDate = sdf.parse(deadline);
                startTimer(timerView, deadlineDate);
            } catch (Exception e) {
                e.printStackTrace();
            }

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    layout.removeView(view);
                    db.deleteTask(view.getId());
                }
            });

            layout.addView(view);
        }
    }

    private void loadTasks() {
        Cursor cursor = db.getTasks();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String deadline = cursor.getString(2);
            String priority = cursor.getString(3);

            final View view = getLayoutInflater().inflate(R.layout.card, null);

            TextView nameView = view.findViewById(R.id.name);
            TextView deadlineView = view.findViewById(R.id.deadline);
            TextView priorityView = view.findViewById(R.id.priority);
            TextView timerView = view.findViewById(R.id.timer);
            Button delete = view.findViewById(R.id.delete);

            nameView.setText(name);
            deadlineView.setText("Deadline: " + deadline);
            priorityView.setText("Priority: " + priority);

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                Date deadlineDate = sdf.parse(deadline);
                startTimer(timerView, deadlineDate);
            } catch (Exception e) {
                e.printStackTrace();
            }

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    layout.removeView(view);
                    db.deleteTask(id);
                }
            });

            layout.addView(view);
        }
    }

    private void startTimer(final TextView timerView, Date deadlineDate) {
        long currentTime = System.currentTimeMillis();
        long deadlineTime = deadlineDate.getTime();
        long timerValue = deadlineTime - currentTime;

        new CountDownTimer(timerValue, 1000) {
            public void onTick(long millisUntilFinished) {
                long days = millisUntilFinished / (1000 * 60 * 60 * 24);
                long hours = (millisUntilFinished / (1000 * 60 * 60)) % 24;
                long minutes = (millisUntilFinished / (1000 * 60)) % 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                timerView.setText(String.format("Time Remaining: %d days %02d:%02d:%02d", days, hours, minutes, seconds));
            }

            public void onFinish() {
                timerView.setText("Time Remaining: Done!");
            }
        }.start();
    }

    private void setupHourlyReminder() {
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_HOUR, pendingIntent);
    }
}
