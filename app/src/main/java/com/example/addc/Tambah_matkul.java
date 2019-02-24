package com.example.addc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class Tambah_matkul extends AppCompatActivity {

    private static final String TAG = "AddMatkul";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_matkul);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_matkul:
                Log.d(TAG, "Add a new task");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
