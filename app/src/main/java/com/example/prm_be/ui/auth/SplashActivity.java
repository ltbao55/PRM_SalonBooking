package com.example.prm_be.ui.auth;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;

/**
 * SplashActivity - Màn hình chào mừng với style luxury salon
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2500; // 2.5 seconds
    private static final int ANIMATION_DURATION = 800; // Animation duration in ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initViews();
        startAnimations();
        navigateAfterDelay();
    }

    private void initViews() {
        // Views đã được khai báo trong layout
    }

    /**
     * Khởi động animations cho logo và text
     */
    private void startAnimations() {
        View logoContainer = findViewById(R.id.viewLogoContainer);
        TextView tvAppName = findViewById(R.id.tvAppName);
        TextView tvTagline = findViewById(R.id.tvTagline);

        // Logo fade in và scale animation
        if (logoContainer != null) {
            logoContainer.setAlpha(0f);
            logoContainer.setScaleX(0.5f);
            logoContainer.setScaleY(0.5f);

            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(logoContainer, "alpha", 0f, 1f);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoContainer, "scaleX", 0.5f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoContainer, "scaleY", 0.5f, 1f);

            fadeIn.setDuration(ANIMATION_DURATION);
            scaleX.setDuration(ANIMATION_DURATION);
            scaleY.setDuration(ANIMATION_DURATION);

            fadeIn.setInterpolator(new DecelerateInterpolator());
            scaleX.setInterpolator(new DecelerateInterpolator());
            scaleY.setInterpolator(new DecelerateInterpolator());

            fadeIn.start();
            scaleX.start();
            scaleY.start();
        }

        // App name fade in animation (delay 200ms)
        if (tvAppName != null) {
            tvAppName.setAlpha(0f);
            tvAppName.setTranslationY(30f);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(tvAppName, "alpha", 0f, 1f);
                ObjectAnimator translateY = ObjectAnimator.ofFloat(tvAppName, "translationY", 30f, 0f);

                fadeIn.setDuration(ANIMATION_DURATION);
                translateY.setDuration(ANIMATION_DURATION);

                fadeIn.setInterpolator(new DecelerateInterpolator());
                translateY.setInterpolator(new DecelerateInterpolator());

                fadeIn.start();
                translateY.start();
            }, 200);
        }

        // Tagline fade in animation (delay 400ms)
        if (tvTagline != null) {
            tvTagline.setAlpha(0f);
            tvTagline.setTranslationY(20f);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(tvTagline, "alpha", 0f, 1f);
                ObjectAnimator translateY = ObjectAnimator.ofFloat(tvTagline, "translationY", 20f, 0f);

                fadeIn.setDuration(ANIMATION_DURATION);
                translateY.setDuration(ANIMATION_DURATION);

                fadeIn.setInterpolator(new DecelerateInterpolator());
                translateY.setInterpolator(new DecelerateInterpolator());

                fadeIn.start();
                translateY.start();
            }, 400);
        }
    }

    /**
     * Điều hướng sau 2.5 giây
     * Kiểm tra đăng nhập:
     * - Đã đăng nhập -> HomeActivity (màn hình chính)
     * - Chưa đăng nhập -> LoginActivity (màn hình đăng nhập)
     */
    private void navigateAfterDelay() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseRepo repo = FirebaseRepo.getInstance();
                if (repo.isUserLoggedIn()) {
                    // Đã đăng nhập -> Màn hình chính
                    startActivity(new Intent(SplashActivity.this, com.example.prm_be.ui.discovery.HomeActivity.class));
                } else {
                    // Chưa đăng nhập -> Màn hình đăng nhập
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }, SPLASH_DELAY);
    }
}
