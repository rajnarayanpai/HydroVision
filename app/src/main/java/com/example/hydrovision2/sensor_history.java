package com.example.hydrovision2;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class sensor_history extends AppCompatActivity {

    private static final int REQUEST_WRITE_PERMISSION = 100;

    private SensorDataDbHelper dbHelper;
    private ListView listView;
    private ArrayList<String> historyList;
    private ArrayAdapter<String> adapter;
    private Button exportButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_history);

        listView = findViewById(R.id.list_history);
        exportButton = findViewById(R.id.exportButton);
        historyList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyList);
        listView.setAdapter(adapter);

        dbHelper = new SensorDataDbHelper(this);

        loadHistory();

        exportButton.setOnClickListener(v -> {
            // Check permission before exporting
            if (ContextCompat.checkSelfPermission(sensor_history.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(sensor_history.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
            } else {
                exportToCSV();
            }
        });
    }

    private void loadHistory() {
        historyList.clear();
        Cursor cursor = dbHelper.getReadableDatabase().query(
                SensorDataDbHelper.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                SensorDataDbHelper.COL_TIMESTAMP + " DESC"
        );

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault());

        while (cursor.moveToNext()) {
            long ts = cursor.getLong(cursor.getColumnIndexOrThrow(SensorDataDbHelper.COL_TIMESTAMP));
            String time = sdf.format(new Date(ts));

            String ph = cursor.getString(cursor.getColumnIndexOrThrow(SensorDataDbHelper.COL_PH));
            String turbidity = cursor.getString(cursor.getColumnIndexOrThrow(SensorDataDbHelper.COL_TURBIDITY));
            String tds = cursor.getString(cursor.getColumnIndexOrThrow(SensorDataDbHelper.COL_TDS));
            String bac = cursor.getString(cursor.getColumnIndexOrThrow(SensorDataDbHelper.COL_BACTERIA));
            String temp = cursor.getString(cursor.getColumnIndexOrThrow(SensorDataDbHelper.COL_TEMPERATURE));
            String ec = cursor.getString(cursor.getColumnIndexOrThrow(SensorDataDbHelper.COL_EC));

            historyList.add(time + "\n"
                    + "pH: " + ph
                    + " | Turbidity: " + turbidity
                    + " | TDS: " + tds
                    + " | EC: " + ec
                    + " | Bacteria: " + bac
                    + " | Temp: " + temp);
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private void exportToCSV() {
        File exportDir = getExternalFilesDir(null);
        if (exportDir != null && !exportDir.exists()) {
            exportDir.mkdirs();
        }
        File csvFile = new File(exportDir, "sensor_data_export.csv");

        try (FileWriter fw = new FileWriter(csvFile)) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(
                    SensorDataDbHelper.TABLE_NAME,
                    null, null, null, null, null,
                    SensorDataDbHelper.COL_TIMESTAMP + " DESC"
            );

            fw.append("Timestamp,pH,Turbidity,TDS,EC,Bacteria,Temperature\n");
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault());

            while (cursor.moveToNext()) {
                long ts = cursor.getLong(cursor.getColumnIndexOrThrow(SensorDataDbHelper.COL_TIMESTAMP));
                String time = sdf.format(new Date(ts));
                String ph = cursor.getString(cursor.getColumnIndexOrThrow(SensorDataDbHelper.COL_PH));
                String turbidity = cursor.getString(cursor.getColumnIndexOrThrow(SensorDataDbHelper.COL_TURBIDITY));
                String tds = cursor.getString(cursor.getColumnIndexOrThrow(SensorDataDbHelper.COL_TDS));
                String ec = cursor.getString(cursor.getColumnIndexOrThrow(SensorDataDbHelper.COL_EC));
                String bac = cursor.getString(cursor.getColumnIndexOrThrow(SensorDataDbHelper.COL_BACTERIA));
                String temp = cursor.getString(cursor.getColumnIndexOrThrow(SensorDataDbHelper.COL_TEMPERATURE));

                fw.append(String.format(Locale.US, "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        time, ph, turbidity, tds, ec, bac, temp));
            }
            cursor.close();
            fw.flush();

            Toast.makeText(this, "CSV exported: " + csvFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            showCsvNotification(csvFile);

        } catch (IOException e) {
            Toast.makeText(this, "Error exporting CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportToCSV();
            } else {
                Toast.makeText(this, "Permission denied to write to storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class SensorDataDbHelper extends android.database.sqlite.SQLiteOpenHelper {
        private static final String DB_NAME = "sensor_data.db";
        private static final int DB_VERSION = 1;
        public static final String TABLE_NAME = "sensor_readings";

        public static final String COL_ID = "id";
        public static final String COL_PH = "ph";
        public static final String COL_TURBIDITY = "turbidity";
        public static final String COL_TDS = "tds";
        public static final String COL_BACTERIA = "bacteria";
        public static final String COL_TEMPERATURE = "temperature";
        public static final String COL_EC = "ec";
        public static final String COL_TIMESTAMP = "timestamp";

        public SensorDataDbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_PH + " TEXT, " +
                    COL_TURBIDITY + " TEXT, " +
                    COL_TDS + " TEXT, " +
                    COL_BACTERIA + " TEXT, " +
                    COL_TEMPERATURE + " TEXT, " +
                    COL_EC + " TEXT, " +
                    COL_TIMESTAMP + " INTEGER)";
            db.execSQL(createTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
    private void showCsvNotification(File csvFile) {
        Uri fileUri = FileProvider.getUriForFile(this,
                getPackageName() + ".provider",
                csvFile);

        // Intent to open the file
        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        openIntent.setDataAndType(fileUri, "text/csv");

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "csv_export_channel";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "CSV Export", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("CSV Exported")
                .setContentText("Tap to open sensor_data_export.csv")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, builder.build());
    }

}
