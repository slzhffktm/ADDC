package com.example.addc;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TodoListActivity extends AppCompatActivity {

    String yourId;

    ViewGroup doneTodosContainer;
    ViewGroup undoneTodosContainer;

    ArrayList<String> undoneTodoIds = new ArrayList<>();
    ArrayList<String> undoneTodoTexts = new ArrayList<>();
    ArrayList<String> doneTodoIds = new ArrayList<>();
    ArrayList<String> doneTodoTexts = new ArrayList<>();

    private DatabaseReference mDatabase;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_todo_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        yourId = acct.getId();

        doneTodosContainer = findViewById(R.id.doneTodosContainer);
        undoneTodosContainer = findViewById(R.id.undoneTodosContainer);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TodoListActivity.this, AddTodoActivity.class);
                startActivity(intent);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        getTodos();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    protected void getTodos() {
        DatabaseReference todoDatabase = FirebaseDatabase.getInstance().getReference();

        todoDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                undoneTodoIds.clear();
                undoneTodoTexts.clear();
                doneTodoIds.clear();
                doneTodoTexts.clear();
                for (DataSnapshot ds: dataSnapshot.child("user_todos").child(yourId).getChildren()) {
                    String id = ds.getKey();
                    String name = dataSnapshot.child("todos").child(id).child("name").getValue().toString();
                    String dueDate = dataSnapshot.child("todos").child(id).child("dueDate").getValue().toString();
                    Boolean done = Boolean.parseBoolean(dataSnapshot.child("todos").child(id).child("done").getValue().toString());

                    if (!done) {
                        undoneTodoIds.add(id);
                        undoneTodoTexts.add(dueDate + " " + name);
                    } else {
                        doneTodoIds.add(id);
                        doneTodoTexts.add(dueDate + " " + name);
                    }
                }

                generateLists();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Snackbar.make(findViewById(R.id.addTodoCoordinatorLayout), "No internet connection.", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void generateLists() {
        Log.w("UKURAN", String.format("%d", doneTodoTexts.size()));

        undoneTodosContainer.removeAllViews();
        int id = 0;
        for (String undoneTodoText: undoneTodoTexts) {
            CheckBox checkBox = new CheckBox(TodoListActivity.this);
            checkBox.setText(undoneTodoText);
            checkBox.setChecked(true);
            checkBox.setId(id);
            id++;
            undoneTodosContainer.addView(checkBox);

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDatabase.child("todos").child(undoneTodoIds.get(v.getId())).child("done").setValue(true);
                    getTodos();
                }
            });
            checkBox.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Intent intent = new Intent(TodoListActivity.this, TrackFriendsActivity.class);
                    intent.putExtra("todo_id", undoneTodoIds.get(v.getId()));
                    return false;
                }
            });
        }
        Log.w("UKURAN", String.format("%d", doneTodoTexts.size()));

        doneTodosContainer.removeAllViews();
        id = 0;
        for (String doneTodoText: doneTodoTexts) {
            CheckBox checkBox1 = new CheckBox(TodoListActivity.this);
            checkBox1.setText(doneTodoText);
            checkBox1.setChecked(false);
            checkBox1.setId(id);
            id++;
            doneTodosContainer.addView(checkBox1);

            checkBox1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.w("SET TODO", "ONCLICK");
                    mDatabase.child("todos").child(doneTodoIds.get(v.getId())).child("done").setValue(false)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.w("SET TODO", "JALAN");
                                }
                            })
                            .addOnCanceledListener(new OnCanceledListener() {
                                @Override
                                public void onCanceled() {
                                    Log.w("SET TODO", "GA JALAN");
                                }
                            });
                    getTodos();
                }
            });
        }
    }
}
