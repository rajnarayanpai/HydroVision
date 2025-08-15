package com.example.hydrovision2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class sensor_readings extends AppCompatActivity {

    private static final String TAG = "SensorReadingsActivity";

    // TextView references for sensor values
    private TextView phValue, turbidityValue, tdsValue, bacterialValue, temperatureValue, ecValue;
    private Button rec_btn;

    // Firebase database reference
    private DatabaseReference sensorDataRef;

    // SQLite DB helper instance
    private SensorDataDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sensor_readings);

        // Initialize SQLite db helper
        dbHelper = new SensorDataDbHelper(this);

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sensor_readings_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI views and button click listener
        initViews();

        // Setup Firebase connection and data listener
        setupFirebaseListener();
    }

    /**
     * Initialize all UI references.
     */
    private void initViews() {
        phValue = findViewById(R.id.text_ph_value);
        turbidityValue = findViewById(R.id.text_turbidity_value);
        tdsValue = findViewById(R.id.text_tds_value);
        bacterialValue = findViewById(R.id.text_bacterial_value);
        temperatureValue = findViewById(R.id.text_temperature_value);
        ecValue = findViewById(R.id.text_ec_value);
        rec_btn = findViewById(R.id.btn_get_recommendations);

        rec_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent open = new Intent(sensor_readings.this, recommendation.class);
                open.putExtra("ph", phValue.getText().toString());
                open.putExtra("temperature", temperatureValue.getText().toString());
                open.putExtra("tds", tdsValue.getText().toString());
                open.putExtra("ec", ecValue.getText().toString());
                open.putExtra("bacteria", bacterialValue.getText().toString());
                open.putExtra("turbidity", turbidityValue.getText().toString());
                startActivity(open);
            }
        });
    }

    /**
     * Setup Firebase Realtime Database listener to update sensor readings.
     */
    private void setupFirebaseListener() {
        sensorDataRef = FirebaseDatabase.getInstance().getReference("Data");

        sensorDataRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    updateSensorValues(snapshot);
                } else {
                    Toast.makeText(sensor_readings.this, "No sensor data found", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "No sensor data found at location: sensors");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(sensor_readings.this, "Failed to load sensor data", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Firebase data load cancelled or failed", error.toException());
            }
        });
    }

    /**
     * Update UI TextViews with the sensor data and also store data into SQLite.
     */
    private void updateSensorValues(DataSnapshot snapshot) {
        String ph = getValueAsString(snapshot, "pH", "--");
        String turbidity = getValueAsString(snapshot, "Turbidity", "--");
        String tds = getValueAsString(snapshot, "TDS", "--");
        String bacterial = getValueAsString(snapshot, "Impedance", "--");
        String temperature = getValueAsString(snapshot, "Temperature", "--");
        String ec = getValueAsString(snapshot, "EC", "--");






        phValue.setText(ph);
        turbidityValue.setText(turbidity);
        tdsValue.setText(tds);
        bacterialValue.setText(bacterial);
        temperatureValue.setText(temperature);
        ecValue.setText(ec);

        // Store values into local SQLite on background thread
        storeSensorData(ph, turbidity, tds, bacterial, temperature, ec);
    }

    private String getValueAsString(DataSnapshot parent, String key, String defaultValue) {
        DataSnapshot child = parent.child(key);
        if (child.exists() && child.getValue() != null) {
            return String.valueOf(child.getValue());
        } else {
            return defaultValue;
        }
    }

    /**
     * Insert or update the sensor data in SQLite asynchronously.
     */
    private void storeSensorData(String ph, String turbidity, String tds, String bacteria, String temperature, String ec) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put(SensorDataDbHelper.COLUMN_PH, ph);
                    values.put(SensorDataDbHelper.COLUMN_TURBIDITY, turbidity);
                    values.put(SensorDataDbHelper.COLUMN_TDS, tds);
                    values.put(SensorDataDbHelper.COLUMN_BACTERIA, bacteria);
                    values.put(SensorDataDbHelper.COLUMN_TEMPERATURE, temperature);
                    values.put(SensorDataDbHelper.COLUMN_EC, ec);
                    values.put(SensorDataDbHelper.COLUMN_TIMESTAMP, System.currentTimeMillis());

                    // Insert new row
                    db.insert(SensorDataDbHelper.TABLE_NAME, null, values);
                    db.close();
                } catch (Exception e) {
                    Log.e(TAG, "Error storing sensor data to SQLite", e);
                }
                return null;
            }
        }.execute();
    }

    /**
     * SQLite Helper class for sensor data storage.
     */
    static class SensorDataDbHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "sensor_data.db";
        private static final int DATABASE_VERSION = 1;
        public static final String TABLE_NAME = "sensor_readings";

        public static final String COLUMN_ID = "id";
        public static final String COLUMN_PH = "ph";
        public static final String COLUMN_TURBIDITY = "turbidity";
        public static final String COLUMN_TDS = "tds";
        public static final String COLUMN_BACTERIA = "bacteria";
        public static final String COLUMN_TEMPERATURE = "temperature";
        public static final String COLUMN_EC = "ec";
        public static final String COLUMN_TIMESTAMP = "timestamp";

        private static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_PH + " TEXT, " +
                        COLUMN_TURBIDITY + " TEXT, " +
                        COLUMN_TDS + " TEXT, " +
                        COLUMN_BACTERIA + " TEXT, " +
                        COLUMN_TEMPERATURE + " TEXT, " +
                        COLUMN_EC + " TEXT, " +
                        COLUMN_TIMESTAMP + " INTEGER" +
                        ")";

        public SensorDataDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // For now just drop and recreate
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
