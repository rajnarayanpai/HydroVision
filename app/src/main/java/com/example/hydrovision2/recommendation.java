package com.example.hydrovision2;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class recommendation extends AppCompatActivity {

    Float ph, temp, tds, ec, bac, turb;
    ProgressBar progressPH, progressTurbidity, progressTDS, progressEC, progressBacteria, progressTemp;
    TextView textRecommendationSummary;
    Button history,hm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation);


        // Get intent values (from previous screen)
        Intent i = getIntent();
        ph = Float.parseFloat(i.getStringExtra("ph"));
        temp = Float.parseFloat(i.getStringExtra("temperature"));
        tds = Float.parseFloat(i.getStringExtra("tds"));
        ec = Float.parseFloat(i.getStringExtra("ec"));
        bac = Float.parseFloat(i.getStringExtra("bacteria"));
        turb= Float.parseFloat(i.getStringExtra("turbidity"));



        // Bind ProgressBars (make sure IDs match your XML)
        progressPH = findViewById(R.id.progress_ph);
        progressTurbidity = findViewById(R.id.progress_turbidity);
        progressTDS = findViewById(R.id.progress_tds);
        progressEC = findViewById(R.id.progress_ec);
        progressBacteria = findViewById(R.id.progress_bacteria);
        progressTemp = findViewById(R.id.progress_temp);
        history=findViewById(R.id.btn_hist);
        hm=findViewById(R.id.btn_predict_heavy);
        history.setOnClickListener(view -> {
            Intent intent = new Intent(recommendation.this, sensor_history.class);
            startActivity(intent);
        });

        hm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent op=new Intent(recommendation.this,heavy_metalpred.class);
                op.putExtra("tds",String.valueOf(tds));
                op.putExtra("ph",String.valueOf(ph));
                op.putExtra("turbidity",String.valueOf(turb));
                startActivity(op);


            }
        });

        // Bind the summary TextView
        textRecommendationSummary = findViewById(R.id.text_recommendation_summary);



        // Update UI based on the values
        updateUI();
    }

    private void updateUI() {
        // Scale the values to 0–100 for the progress bars (ensuring limits)
        progressPH.setProgress((int) ((ph / 14.0) * 100)); // pH scale 0–14
        progressTurbidity.setProgress((int) (Math.min(turb, 10) * 10)); // NTU scale 0–10+
        progressTDS.setProgress((int) (Math.min(tds, 2000) * 100 / 2000)); // ppm up to 2000
        progressEC.setProgress((int) (Math.min(ec, 2250) * 100 / 2250)); // µS/cm up to 2250
        progressBacteria.setProgress((int) (Math.min(bac, 1000) * 100 / 1000)); // scale bacteria count
        progressTemp.setProgress((int) ((Math.min(temp, 50) / 50.0) * 100)); // temp up to 50°C

        // Classification logic using general water quality standards
        boolean drinking = ph >= 6.5 && ph <= 8.5 &&
                turb <= 5 &&
                tds <= 500 &&
                ec <= 1500 &&
                bac == 0 ;

        boolean industrial = ph >= 6.0 && ph <= 9.0 &&
                turb <= 7 &&
                tds <= 2000 &&
                ec <= 2250 ;

        boolean agricultural = ph >= 6.0 && ph <= 9.0 &&
                turb <= 10 &&
                tds <= 2000 &&
                bac <= 1000;

        String message;

        if (drinking) {
            message = "🚰 Water is SAFE for Drinking, Industrial, and Agricultural use.";
        } else if (industrial) {
            message = "🏭 Water is suitable for Industrial and Agricultural use, but NOT safe for Drinking.";
        } else if (agricultural) {
            message = "🌾 Water is suitable for Agricultural use only.";
        } else {
            message = "☠ Water is NOT suitable for Drinking, Industrial, or Agricultural use.";
        }

        // Display the recommendation
        textRecommendationSummary.setText(message);
    }
}
