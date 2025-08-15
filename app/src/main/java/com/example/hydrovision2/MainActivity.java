package com.example.hydrovision2;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {
    private ImageView appLogo;
    private MaterialButton startButton;
    private boolean isDashboardLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Start with splash screen layout
        setContentView(R.layout.activity_main);

        // Apply window insets for splash screen
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        startAnimations();
        ///remove below comment
        setupClickListeners();
    }

    private void initViews() {
        appLogo = findViewById(R.id.app_logo);
        startButton = findViewById(R.id.start_button);
    }

    private void startAnimations() {
        // Logo fade in animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        appLogo.startAnimation(fadeIn);

        // Button bounce animation
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce_animation);
        startButton.startAnimation(bounce);

        // Alternative: Using modern ValueAnimator
        animateButton();
    }

    private void animateButton() {
        ValueAnimator scaleAnimator = ValueAnimator.ofFloat(1.0f, 1.05f);
        scaleAnimator.setDuration(1500);
        scaleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        scaleAnimator.setRepeatMode(ValueAnimator.REVERSE);
        scaleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            startButton.setScaleX(scale);
            startButton.setScaleY(scale);
        });

        scaleAnimator.start();
    }


    /// remove below snippet comment
    private void setupClickListeners() {
        startButton.setOnClickListener(v -> {
            // Add click animation
            v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(100)
                                .withEndAction(this::navigateToDashboard);
                    });
        });
    }








    private void showParameterDetails(String parameter) {
        // TODO: Implement detailed parameter view
        // You can create a new fragment or dialog to show detailed information
        // For now, you can show a simple toast or log
        android.util.Log.d("MainActivity", "Showing details for: " + parameter);
    }

    private void refreshData() {
        // TODO: Implement data refresh logic
        // This is where you would fetch new water quality data
        android.util.Log.d("MainActivity", "Refreshing water quality data....");
    }

    @Override
    public void onBackPressed() {
        if(isDashboardLoaded) {
            // If dashboard is loaded, you might want to show exit dialog
            // or go back to splash screen
            super.onBackPressed();
        } else {
            // If splash screen, exit app
            super.onBackPressed();
        }
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(MainActivity.this, instructions.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}
