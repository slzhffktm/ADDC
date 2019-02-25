package com.example.addc;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

public class AddTodoActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    final Calendar calendar = Calendar.getInstance();

    EditText editTextDueDate;
    EditText editTextDueTime;
    EditText editTextMataKuliah;
    EditText editTextUsers;
    ViewGroup usersCheckBoxContainer;

    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;

    ArrayList<String> mataKuliahsString = new ArrayList<>();
    ArrayList<User> users = new ArrayList<>();
    ArrayList<User> usersAdded = new ArrayList<>();
    SpinnerDialog spinnerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_add_todo);

        getMataKuliahs();

        editTextMataKuliah = findViewById(R.id.input_matakuliah);
        editTextDueDate = findViewById(R.id.input_duedate);
        editTextDueTime = findViewById(R.id.input_duetime);
        editTextUsers = findViewById(R.id.input_users);
        usersCheckBoxContainer = findViewById(R.id.users_checkbox_container);

        editTextMataKuliah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerDialog = new SpinnerDialog(AddTodoActivity.this, mataKuliahsString,
                        "Select Mata Kuliah", R.style.DialogAnimations_SmileWindow,"Cancel");
                spinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
                    @Override
                    public void onClick(String item, int position) {
                        editTextMataKuliah.setText(item);
                        resetUsers(item.split("\\s+")[0]);
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

        editTextUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayList<String> usersArrayList = getUsersStringArrayList(users);
                spinnerDialog = new SpinnerDialog(AddTodoActivity.this, usersArrayList,
                        "Select User in Your Group", R.style.DialogAnimations_SmileWindow,"Cancel");
                spinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
                    @Override
                    public void onClick(String item, int position) {
                        usersAdded.add(users.get(position));
                        users.remove(position);
                        editTextUsers.setText(String.format(Locale.US, "Total friends: %d", usersAdded.size()));
                        generateUsersCheckBoxes();
                    }
                });
                spinnerDialog.setCancellable(true); // for cancellable
                spinnerDialog.setShowKeyboard(false);// for open keyboard by default
                spinnerDialog.showSpinerDialog();
            }
        });
    }

    private void getMataKuliahs() {
        DatabaseReference table_matakuliah = FirebaseDatabase.getInstance().getReference("mata_kuliahs");
        table_matakuliah.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    try {
                        mataKuliahsString.add(ds.getKey() + " " + ds.child("name").getValue().toString());
                    } catch (Exception e) {
                        Log.w("RETRIEVE DATA", "name empty");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("RETRIEVE DATA", "retrieving data error with code=" + databaseError.getCode());
            }
        });
    }

    private void resetUsers(String idMataKuliah) {
        users.clear();
        usersAdded.clear();
        getUsers(idMataKuliah);
    }

    private void getUsers(final String idMataKuliah) {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        final String yourId = acct.getId();

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.child("matakuliah_enrollment").child(idMataKuliah).getChildren()) {
                    String userId = ds.getKey();
                    if (!userId.equals(yourId)) {
                        String userEmail = dataSnapshot.child("users").child(userId).child("email").getValue().toString();
                        String userName = dataSnapshot.child("users").child(userId).child("name").getValue().toString();

                        users.add(new User(userId, userEmail, userName, ""));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("RETRIEVE DATA", "retrieving data error with code=" + databaseError.getCode());
            }
        });
    }

    private ArrayList<String> getUsersStringArrayList(ArrayList<User> usersList) {
        ArrayList<String> usersString = new ArrayList<>();
        for (User u: usersList) {
            usersString.add(u.getEmail() + " ("  + u.getName() + ")");
        }

        return usersString;
    }

    private void generateUsersCheckBoxes() {
        usersCheckBoxContainer.removeAllViews();
        int id = 0;
        for (String userString: getUsersStringArrayList(usersAdded)) {
            CheckBox checkBox = new CheckBox(AddTodoActivity.this);
            checkBox.setText(userString);
            checkBox.setChecked(true);
            checkBox.setId(id);
            id++;
            usersCheckBoxContainer.addView(checkBox);

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    users.add(usersAdded.get(v.getId()));
                    usersAdded.remove(v.getId());
                    editTextUsers.setText(String.format(Locale.US, "Total friends: %d", usersAdded.size()));
                    generateUsersCheckBoxes();
                }
            });
        }
    }
}
