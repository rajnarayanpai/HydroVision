package com.example.hydrovision2;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class heavy_metalpred extends AppCompatActivity {
    TextView tds, turbidity, ph, predictedValue, microgramValue;
    String tdsval, turbval, phval;
    Interpreter tflite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_heavy_metalpred);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent in = getIntent();
        phval = in.getStringExtra("ph");
        turbval = in.getStringExtra("turbidity");
        tdsval = in.getStringExtra("tds");

        tds = findViewById(R.id.tdsval2);
        turbidity = findViewById(R.id.turbval2);
        ph = findViewById(R.id.phval2);
        predictedValue = findViewById(R.id.textViewPredictedValue);
       // microgramValue = findViewById(R.id.textViewMicrogramValue);

        tds.setText(tdsval);
        turbidity.setText(turbval);
        ph.setText(phval);

        try {
            tflite = new Interpreter(loadModelFile());
        } catch (IOException e) {
            e.printStackTrace();
            predictedValue.setText("Model load error");
            return;
        }

        runPrediction();
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("arsenic_model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void runPrediction() {
        try {
            // Convert string values from Intent to float
            float phFloat = Float.parseFloat(phval);
            float turbFloat = Float.parseFloat(turbval);
            float tdsFloat = Float.parseFloat(tdsval);

// Prepare input in the correct order:
            float[][] input = new float[1][3];
            input[0][0] = turbFloat;
            input[0][1] = phFloat;
            input[0][2] = tdsFloat;

// Prepare output
            float[][] output = new float[1][1];

// Run inference
            tflite.run(input, output);

// Get prediction result in mg/L
            float arsenicMgL = output[0][0];
            String arsenicStr = String.format("%.6f", arsenicMgL);
            predictedValue.setText(arsenicStr);


            // Optionally update safety status TextViews here based on arsenicMgL

        } catch (NumberFormatException e) {
            e.printStackTrace();
            predictedValue.setText("Input error");
            microgramValue.setText("");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tflite != null) {
            tflite.close();
            tflite = null;
        }
    }
}
