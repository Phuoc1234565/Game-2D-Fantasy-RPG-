package com.example.game2dfantasy;

import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;

public class SplashActivity extends AppCompatActivity {

    private ValueAnimator animator;
    private ProgressBar pb;
    private TextView tvLoading;
    private View llSplashBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        UiUtils.hideSystemUI(this);

        // Nạp ảnh nền và logo an toàn: ưu tiên menu_bg -> splash_bg -> gradient tím đêm
        setupVisuals();

        pb = findViewById(R.id.pb_splash);
        tvLoading = findViewById(R.id.tv_loading);
        llSplashBottom = findViewById(R.id.ll_splash_bottom);

        // Đẩy thanh tiến trình ra khỏi vùng camera đục lỗ (DisplayCutout) từ API 28
        int padH = (int) (32 * getResources().getDisplayMetrics().density);
        UiUtils.applyCutoutPadding(llSplashBottom, padH, 0, padH, 0);

        // [DEBUG CODE] Kiểm tra và hiển thị hộp thoại crash log nếu có lỗi từ lần chạy trước.
        // Ghi chú: Đây là mã gỡ lỗi hỗ trợ kiểm tra trên máy thật, sẽ gỡ trước khi nộp đồ án.
        boolean hasCrashLog = checkAndShowCrashLog();
        if (!hasCrashLog) {
            startSplashAnimation();
        }
    }

    private void setupVisuals() {
        try {
            ImageView ivBg = findViewById(R.id.iv_splash_bg);
            if (ivBg != null) {
                // Ưu tiên menu_bg -> splash_bg -> gradient tím đêm dự phòng
                int bgId = getResources().getIdentifier("menu_bg", "drawable", getPackageName());
                if (bgId == 0) {
                    bgId = getResources().getIdentifier("splash_bg", "drawable", getPackageName());
                }
                if (bgId != 0) {
                    ivBg.setImageResource(bgId);
                } else {
                    ivBg.setImageResource(R.drawable.splash_bg_fallback);
                }
            }

            ImageView ivLogo = findViewById(R.id.iv_splash_logo);
            if (ivLogo != null) {
                int logoId = getResources().getIdentifier("game_logo", "drawable", getPackageName());
                if (logoId != 0) {
                    ivLogo.setImageResource(logoId);
                }
            }
        } catch (Exception ignored) {}
    }

    /**
     * [DEBUG CODE] Hiển thị hộp thoại crash log nếu phát hiện file crash_log.txt từ lần chạy trước.
     * Ghi chú: Đây là mã gỡ lỗi hỗ trợ kiểm tra trên máy thật, sẽ gỡ trước khi nộp đồ án.
     */
    private boolean checkAndShowCrashLog() {
        try {
            File logFile = new File(getFilesDir(), IsekaiApp.CRASH_LOG_FILE);
            if (!logFile.exists() || logFile.length() == 0) {
                return false;
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }

            final String logContent = sb.toString();

            ScrollView scrollView = new ScrollView(this);
            TextView textView = new TextView(this);
            textView.setText(logContent);
            textView.setTextSize(12f);
            textView.setPadding(32, 24, 32, 24);
            textView.setTextIsSelectable(true);
            scrollView.addView(textView);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Phát hiện lỗi lần chạy trước (Crash Log)")
                    .setView(scrollView)
                    .setCancelable(false)
                    .setPositiveButton("Xóa log & tiếp tục", (d, which) -> {
                        try {
                            if (logFile.exists()) {
                                //noinspection ResultOfMethodCallIgnored
                                logFile.delete();
                            }
                        } catch (Exception ignored) {}
                        startSplashAnimation();
                    })
                    .setNeutralButton("Sao chép", null)
                    .create();

            dialog.setOnShowListener(d -> {
                Button copyBtn = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                if (copyBtn != null) {
                    copyBtn.setOnClickListener(v -> {
                        try {
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            if (clipboard != null) {
                                ClipData clip = ClipData.newPlainText("Crash Log", logContent);
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(SplashActivity.this, "Đã sao chép crash log vào bộ nhớ tạm", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception ex) {
                            Toast.makeText(SplashActivity.this, "Không thể sao chép: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            dialog.show();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void startSplashAnimation() {
        if (isFinishing() || isDestroyed()) return;

        animator = ValueAnimator.ofInt(0, 100);
        animator.setDuration(2500);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            if (isFinishing() || isDestroyed()) return;
            int value = (int) animation.getAnimatedValue();
            if (pb != null) {
                pb.setProgress(value);
            }
            if (tvLoading != null) {
                tvLoading.setText(String.format(Locale.getDefault(),
                        getString(R.string.splash_loading), value));
            }
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (isFinishing() || isDestroyed()) return;
                // Chuyển sang MainMenuActivity theo luồng thống nhất
                Intent intent = new Intent(SplashActivity.this, MainMenuActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });
        animator.start();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            UiUtils.hideSystemUI(this);
            int padH = (int) (32 * getResources().getDisplayMetrics().density);
            UiUtils.updateCutoutPadding(llSplashBottom, padH, 0, padH, 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (animator != null) {
            if (animator.isRunning()) {
                animator.cancel();
            }
            animator.removeAllUpdateListeners();
            animator.removeAllListeners();
            animator = null;
        }
    }
}
