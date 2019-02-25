package com.example.addc;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class AddMatkul extends AppCompatActivity {

    private DatabaseReference mDatabase_matkul;
    private DatabaseReference mDatabase_enrollment;

    private static final String TAG = "AddMatkul";
    private String[] array_matkul;
    private ListView listView;

    private ArrayList<String> matkul;
    private ValueEventListener eventListener;

    private List<String> selected_matkul;
    private ArrayAdapter adapter;

    private String personId;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_matkul);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDatabase_matkul = FirebaseDatabase.getInstance().getReference("mata_kuliahs");
        mDatabase_enrollment = FirebaseDatabase.getInstance().getReference();

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(AddMatkul.this);
        personId = acct.getId();


        matkul = new ArrayList<>();
        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    try {
                        String id = ds.getKey();
                        String nama = ds.child("name").getValue().toString();
                        String mataKuliah = id + " - " + nama;
                        Log.w("isi",mataKuliah);
                        matkul.add(mataKuliah);
                    } catch (Exception e){
                        Log.w(TAG, "Failed to retrieve data");
                    }
                }
                array_matkul = new String[matkul.size()];
                matkul.toArray(array_matkul);
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to retrieve data");
            }
        };
        mDatabase_matkul.addValueEventListener(eventListener);

        listView = (ListView) findViewById(R.id.selected_matkul);
        selected_matkul = new ArrayList<String>();

        adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview,R.id.view_matkul,selected_matkul);

        listView.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_matkul, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_matkul:

                AlertDialog.Builder list_matkul = new AlertDialog.Builder(AddMatkul.this);
                list_matkul.setTitle("Pilih Mata Kuliah");
                list_matkul.setItems(array_matkul, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        selected_matkul.add(array_matkul[which]);
                        adapter.notifyDataSetChanged();

                        //send data to database
                        String selected_id = array_matkul[which].split("\\s+")[0];
                        Log.w("bangsat",selected_id);

                        mDatabase_enrollment.child("matakuliah_enrollment").child(selected_id).child(personId).setValue(true);
                        mDatabase_enrollment.child("user_enrollment").child(personId).child(selected_id).setValue(true);
                    }
                });

                list_matkul.show();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void deleteMatkul(View view){
        View parent = (View) view.getParent();
        TextView matkulTextView = (TextView) parent.findViewById(R.id.view_matkul);
        String matkul = String.valueOf(matkulTextView.getText());
        String delete_matkul_id = matkul.split("\\s+")[0];

        //Remove data from database
        mDatabase_enrollment.child("matakuliah_enrollment").child(delete_matkul_id).child(personId).removeValue();
        mDatabase_enrollment.child("user_enrollment").child(personId).child(delete_matkul_id).removeValue();

        selected_matkul.remove(selected_matkul.indexOf(matkul));

        adapter.notifyDataSetChanged();
    }

}
