package com.example.todolistapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Reminder: Check your tasks!", Toast.LENGTH_SHORT).show();
        // Here you can add more sophisticated notification logic if needed.
    }
}
