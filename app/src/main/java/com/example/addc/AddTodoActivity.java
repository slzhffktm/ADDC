package com.example.addc;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

// TODO: 2/25/2019 CREATE FOR EDIT
public class AddTodoActivity extends AppCompatActivity {
    final Calendar calendar = Calendar.getInstance();

    String yourId;

    EditText editTextName;
    EditText editTextDescription;
    EditText editTextDueDate;
    EditText editTextDueTime;
    EditText editTextMataKuliah;
    EditText editTextUsers;
    ViewGroup usersCheckBoxContainer;
    Button addButton;

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

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        yourId = acct.getId();

        getMataKuliahs();

        editTextName = findViewById(R.id.input_name);
        editTextMataKuliah = findViewById(R.id.input_matakuliah);
        editTextDescription = findViewById(R.id.input_description);
        editTextDueDate = findViewById(R.id.input_duedate);
        editTextDueTime = findViewById(R.id.input_duetime);
        editTextUsers = findViewById(R.id.input_users);
        usersCheckBoxContainer = findViewById(R.id.users_checkbox_container);
        addButton = findViewById(R.id.btn_add);

        editTextMataKuliah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerDialog = new SpinnerDialog(AddTodoActivity.this, mataKuliahsString,
                        "Select Mata Kuliah", R.style.DialogAnimations_SmileWindow, "Cancel");
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
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
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
                        "Select User in Your Group", R.style.DialogAnimations_SmileWindow, "Cancel");
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

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTodo();
            }
        });
    }

    private void getMataKuliahs() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.child("user_enrollment").child(yourId).getChildren()) {
                    try {
                        String mataKuliahId = ds.getKey();
                        String mataKuliahName = dataSnapshot.child("mata_kuliahs").child(mataKuliahId).child("name").getValue().toString();
                        mataKuliahsString.add(mataKuliahId + " " + mataKuliahName);
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
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.child("matakuliah_enrollment").child(idMataKuliah).getChildren()) {
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
        for (User u : usersList) {
            usersString.add(u.getEmail() + " (" + u.getName() + ")");
        }

        return usersString;
    }

    private void generateUsersCheckBoxes() {
        usersCheckBoxContainer.removeAllViews();
        int id = 0;
        for (String userString : getUsersStringArrayList(usersAdded)) {
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

    private void addTodo() {
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        Todo todo = new Todo(editTextName.getText().toString(), editTextDescription.getText().toString(),
                editTextDueDate.getText().toString(), editTextDueTime.getText().toString(),
                editTextMataKuliah.getText().toString(), false);

        final String todoId = mDatabase.push().getKey();

        mDatabase.child("todos").child(todoId).setValue(todo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mDatabase.child("user_todos").child(yourId).child(todoId).setValue(true);
                        mDatabase.child("todo_users").child(todoId).child(yourId).setValue(true);
                        for (User user: usersAdded) {
                            mDatabase.child("user_todos").child(user.getId()).child(todoId).setValue(true);
                            mDatabase.child("todo_users").child(todoId).child(user.getId()).setValue(true);
                        }

                        Toast.makeText(getApplicationContext(), "Success.",
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(AddTodoActivity.this, TodoListActivity.class);
                        startActivity(intent);
                    }
                })
                .addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Snackbar.make(findViewById(R.id.todoListCoordinatorLayout), "No internet connection.", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }
}
