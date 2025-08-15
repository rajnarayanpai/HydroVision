package com.example.hydrovision2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class instructions extends AppCompatActivity {

    private MaterialButton nextButton, skipButton;
    private final View[] instructions = new View[5];
    private final View[] indicators = new View[5];
    private int currentStep = 0;
    private final int TOTAL_STEPS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_instructions);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupClickListeners();
        updateUI();
    }

    private void initViews() {
        nextButton = findViewById(R.id.next_button);
        skipButton = findViewById(R.id.skip_button);

        // Initialize instruction views
        instructions[0] = findViewById(R.id.instruction_1);
        instructions[1] = findViewById(R.id.instruction_2);
        instructions[2] = findViewById(R.id.instruction_3);
        instructions[3] = findViewById(R.id.instruction_4);
        instructions[4] = findViewById(R.id.instruction_5);

        // Initialize indicator views (FIX: proper 5-element array)
        indicators[0] = findViewById(R.id.indicator_1);
        indicators[1] = findViewById(R.id.indicator_2);
        indicators[2] = findViewById(R.id.indicator_3);
        indicators[3] = findViewById(R.id.indicator_4);
        indicators[4] = findViewById(R.id.indicator_5);
    }

    private void setupClickListeners() {
        nextButton.setOnClickListener(v -> {
            if (currentStep < TOTAL_STEPS - 1) {
                // Move to next step
                showNextInstruction();
            } else {
                // Last step, proceed to next activity
                proceedToNextActivity();
            }
        });

        skipButton.setOnClickListener(v -> proceedToNextActivity());
    }

    private void showNextInstruction() {
        // Hide current instruction with slide out animation
        slideOut(instructions[currentStep], true);

        // Update step
        currentStep++;

        // Show next instruction with slide in animation after a short delay
        instructions[currentStep].postDelayed(() -> {
            slideIn(instructions[currentStep], true);
        }, 200);

        // Update UI elements
        updateUI();
    }

    private void updateUI() {
        // Update progress indicators
        for (int i = 0; i < TOTAL_STEPS; i++) {
            if (i <= currentStep) {
                indicators[i].setBackground(getDrawable(R.drawable.progress_indicator_active));
            } else {
                indicators[i].setBackground(getDrawable(R.drawable.progress_indicator_inactive));
            }
        }

        // Update button text
        if (currentStep == TOTAL_STEPS - 1) {
            nextButton.setText("Get Started");
        } else {
            nextButton.setText("Next");
        }

        // Update button icons
        nextButton.setIcon(currentStep == TOTAL_STEPS - 1
                ? getDrawable(R.drawable.ic_check)
                : getDrawable(R.drawable.ic_arrow_forward));
    }

    private void slideOut(View view, boolean toLeft) {
        Animation slideOut = AnimationUtils.loadAnimation(this,
                toLeft ? R.anim.slide_out_left : R.anim.slide_out_right);

        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        view.startAnimation(slideOut);
    }

    private void slideIn(View view, boolean fromRight) {
        view.setVisibility(View.VISIBLE);
        Animation slideIn = AnimationUtils.loadAnimation(this,
                fromRight ? R.anim.slide_in_right : R.anim.slide_in_left);
        view.startAnimation(slideIn);
    }

    private void proceedToNextActivity() {
        Intent intent = new Intent(instructions.this, sensor_readings.class);
        intent.putExtra("show_dashboard", true);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (currentStep > 0) {
            // Go back to previous step
            slideOut(instructions[currentStep], false);
            currentStep--;

            instructions[currentStep].postDelayed(() -> {
                slideIn(instructions[currentStep], false);
            }, 200);

            updateUI();
        } else {
            super.onBackPressed();
        }
    }
}
