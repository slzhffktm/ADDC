package com.example.addc;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

public class AddTodoActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private DatabaseReference table_matakuliah;
    final Calendar calendar = Calendar.getInstance();

    EditText editTextDueDate;
    EditText editTextDueTime;
    EditText editTextMataKuliah;

    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;

    ArrayList<String> stringMataKuliahs = new ArrayList<>();
    SpinnerDialog spinnerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_add_todo);

        table_matakuliah = FirebaseDatabase.getInstance().getReference("mata_kuliahs");
        table_matakuliah.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    try {
                        stringMataKuliahs.add(ds.getKey() + " " + ds.child("name").getValue().toString());
                    } catch (Exception e) {
                        Log.w("RETRIEVE DATA", "name empty");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("RETRIEVE DATA", "retrieving data error");
            }
        });

        editTextMataKuliah = findViewById(R.id.input_matakuliah);
        editTextDueDate = findViewById(R.id.input_duedate);
        editTextDueTime = findViewById(R.id.input_duetime);

        editTextMataKuliah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerDialog = new SpinnerDialog(AddTodoActivity.this, stringMataKuliahs,
                        "Select Mata Kuliah", R.style.DialogAnimations_SmileWindow,"Cancel");
                spinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
                    @Override
                    public void onClick(String item, int position) {
                        editTextMataKuliah.setText(item);
                    }
                });
                spinnerDialog.setCancellable(true); // for cancellable
                spinnerDialog.setShowKeyboard(false);// for open keyboard by default
                spinnerDialog.showSpinerDialog();
            }
        });

        editTextDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog = new DatePickerDialog(AddTodoActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String text = year + "-" + month + "-" + dayOfMonth;
                        editTextDueDate.setText(text);
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        editTextDueTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog = new TimePickerDialog(AddTodoActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String text = hourOfDay + ":" + minute;
                        editTextDueTime.setText(text);
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                        true);
                timePickerDialog.show();
            }
        });
    }
}
