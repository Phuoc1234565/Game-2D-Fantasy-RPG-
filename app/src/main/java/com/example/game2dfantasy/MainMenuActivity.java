package com.example.game2dfantasy;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class MainMenuActivity extends AppCompatActivity {

    private boolean soundOn = true;
    private boolean musicOn = true;
    private boolean fullScreen = true;
    private SharedPreferences prefs;
    private DatabaseHelper db;

    private ImageButton btnSound;
    private ImageButton btnMusic;
    private ImageButton btnFullscreen;
    private View llLogoContainer;
    private View llTopActions;
    private View llButtonColumn;
    private View tvVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        UiUtils.hideSystemUI(this);

        db = DatabaseHelper.getInstance(this);
        prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);

        // Khôi phục cài đặt âm thanh, nhạc, toàn màn hình
        soundOn = prefs.getBoolean(AppConstants.KEY_SFX_ON, true);
        musicOn = prefs.getBoolean(AppConstants.KEY_BGM_ON, true);
        fullScreen = prefs.getBoolean(AppConstants.KEY_FULLSCREEN, true);

        // Nạp ảnh nền menu_bg (fallback gradient tím đêm nếu thiếu file, không nạp rpg_background)
        setupBackground();

        // Nạp logo hoặc fallback chữ tiêu đề và nạp phiên bản ứng dụng
        setupLogoAndVersion();

        // Ánh xạ View null-safe
        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);
        Button btnSettings = findViewById(R.id.btnSettings);
        Button btnSave = findViewById(R.id.btnSave);
        btnSound = findViewById(R.id.btnSound);
        btnMusic = findViewById(R.id.btnMusic);
        btnFullscreen = findViewById(R.id.btnFullscreen);
        llLogoContainer = findViewById(R.id.ll_logo_container);
        llTopActions = findViewById(R.id.ll_top_actions);
        llButtonColumn = findViewById(R.id.ll_button_column);
        tvVersion = findViewById(R.id.tv_version);

        // Áp dụng khoảng cách an toàn tránh vùng đục lỗ camera (DisplayCutout)
        applyCutoutInsets();

        updateAudioButtonsState();

        if (btnPlay != null) {
            btnPlay.setOnClickListener(v -> handlePlayGame());
        }
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> openLogin(AppConstants.MODE_LOGIN));
        }
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> openLogin(AppConstants.MODE_REGISTER));
        }
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> showSettingsDialog());
        }
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveGameState());
        }

        if (btnSound != null) {
            btnSound.setOnClickListener(v -> {
                soundOn = !soundOn;
                prefs.edit().putBoolean(AppConstants.KEY_SFX_ON, soundOn).apply();
                updateAudioButtonsState();
                Toast.makeText(this, soundOn ? "Âm thanh: Bật" : "Âm thanh: Tắt", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnMusic != null) {
            btnMusic.setOnClickListener(v -> {
                musicOn = !musicOn;
                prefs.edit().putBoolean(AppConstants.KEY_BGM_ON, musicOn).apply();
                updateAudioButtonsState();
                Toast.makeText(this, musicOn ? "Nhạc nền: Bật" : "Nhạc nền: Tắt", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnFullscreen != null) {
            btnFullscreen.setOnClickListener(v -> {
                fullScreen = !fullScreen;
                prefs.edit().putBoolean(AppConstants.KEY_FULLSCREEN, fullScreen).apply();
                updateAudioButtonsState();
                if (fullScreen) {
                    UiUtils.hideSystemUI(this);
                } else {
                    View decorView = getWindow().getDecorView();
                    if (decorView != null) {
                        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                }
            });
        }

        // Xử lý nút Back: xác nhận thoát ứng dụng, không quay về SplashActivity
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(MainMenuActivity.this)
                        .setTitle("THOÁT TRÒ CHƠI")
                        .setMessage("Bạn có chắc chắn muốn rời khỏi thế giới RPG?")
                        .setPositiveButton("THOÁT", (d, w) -> finishAffinity())
                        .setNegativeButton("Ở LẠI", null)
                        .show();
            }
        });
    }

    private void applyCutoutInsets() {
        int padStart = (int) (20 * getResources().getDisplayMetrics().density);
        int padEnd = (int) (16 * getResources().getDisplayMetrics().density);
        UiUtils.applyCutoutPadding(llLogoContainer, padStart, 0, 0, 0);
        UiUtils.applyCutoutPadding(tvVersion, padStart, 0, 0, 0);
        UiUtils.applyCutoutPadding(llTopActions, 0, 0, padEnd, 0);
        UiUtils.applyCutoutPadding(llButtonColumn, 0, 0, padEnd, 0);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            UiUtils.hideSystemUI(this);
            int padStart = (int) (20 * getResources().getDisplayMetrics().density);
            int padEnd = (int) (16 * getResources().getDisplayMetrics().density);
            UiUtils.updateCutoutPadding(llLogoContainer, padStart, 0, 0, 0);
            UiUtils.updateCutoutPadding(tvVersion, padStart, 0, 0, 0);
            UiUtils.updateCutoutPadding(llTopActions, 0, 0, padEnd, 0);
            UiUtils.updateCutoutPadding(llButtonColumn, 0, 0, padEnd, 0);
        }
    }

    private void setupBackground() {
        ImageView imgBackground = findViewById(R.id.imgBackground);
        if (imgBackground == null) return;

        // Ưu tiên menu_bg -> gradient tím đêm dự phòng. Tuyệt đối không nạp rpg_background.
        int bgId = getResources().getIdentifier("menu_bg", "drawable", getPackageName());
        if (bgId != 0) {
            imgBackground.setImageResource(bgId);
        } else {
            imgBackground.setImageResource(R.drawable.splash_bg_fallback);
        }
    }

    private void setupLogoAndVersion() {
        try {
            ImageView ivLogo = findViewById(R.id.iv_game_logo);
            TextView tvTitle = findViewById(R.id.tv_game_title);
            int logoId = getResources().getIdentifier("game_logo", "drawable", getPackageName());
            if (logoId != 0 && ivLogo != null) {
                ivLogo.setImageResource(logoId);
                ivLogo.setVisibility(View.VISIBLE);
                if (tvTitle != null) {
                    tvTitle.setVisibility(View.GONE);
                }
            } else {
                if (ivLogo != null) {
                    ivLogo.setVisibility(View.GONE);
                }
                if (tvTitle != null) {
                    tvTitle.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception ignored) {}

        try {
            TextView tvVer = findViewById(R.id.tv_version);
            if (tvVer != null) {
                String versionName = "1.0.0";
                try {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    if (pInfo != null && pInfo.versionName != null) {
                        versionName = pInfo.versionName;
                    }
                } catch (Exception ignored) {}
                tvVer.setText(getString(R.string.menu_version_format, versionName));
            }
        } catch (Exception ignored) {}
    }

    private void updateAudioButtonsState() {
        if (btnSound != null) {
            btnSound.setImageResource(soundOn ? R.drawable.ic_sound_on : R.drawable.ic_sound_off);
        }
        if (btnMusic != null) {
            btnMusic.setImageResource(musicOn ? R.drawable.ic_music_on : R.drawable.ic_music_off);
        }
        if (btnFullscreen != null) {
            btnFullscreen.setImageResource(fullScreen ? R.drawable.ic_fullscreen : R.drawable.ic_fullscreen_exit);
        }
    }

    private int getLoggedInUserId() {
        if (prefs == null) {
            prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
        }
        return prefs.getInt(AppConstants.KEY_USER_ID, -1);
    }

    private void handlePlayGame() {
        int userId = getLoggedInUserId();
        if (userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập trước khi chơi!", Toast.LENGTH_SHORT).show();
            openLogin(AppConstants.MODE_LOGIN);
            return;
        }

        if (db.hasCharacter(userId)) {
            Intent gameIntent = new Intent(this, GameActivity.class);
            gameIntent.putExtra(AppConstants.EXTRA_USER_ID, userId);
            startActivity(gameIntent);
        } else {
            Intent intent = new Intent(this, CharacterSelectActivity.class);
            intent.putExtra(AppConstants.EXTRA_USER_ID, userId);
            startActivity(intent);
        }
    }

    private void openLogin(String mode) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(AppConstants.EXTRA_MODE, mode);
        startActivity(intent);
    }

    private void showSettingsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_settings);

        Window window = dialog.getWindow();
        if (window != null) {
            // Nền trong suốt để hiển thị trọn vẹn bo góc 12dp của khung gỗ
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Bật cờ FLAG_NOT_FOCUSABLE tạm thời trước khi show để không làm hiện thanh điều hướng
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

            // Cho phép phủ mép tai thỏ từ Android 9 (API 28)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                window.setAttributes(lp);
            }
        }

        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        SwitchCompat switchSfx = dialog.findViewById(R.id.switch_sfx);
        SwitchCompat switchBgm = dialog.findViewById(R.id.switch_bgm);
        SwitchCompat switchFullscreen = dialog.findViewById(R.id.switch_fullscreen);
        Button btnClose = dialog.findViewById(R.id.btn_dialog_close);

        if (switchSfx != null) {
            switchSfx.setChecked(soundOn);
            switchSfx.setOnCheckedChangeListener((buttonView, isChecked) -> {
                soundOn = isChecked;
                prefs.edit().putBoolean(AppConstants.KEY_SFX_ON, soundOn).apply();
                updateAudioButtonsState();
            });
        }

        if (switchBgm != null) {
            switchBgm.setChecked(musicOn);
            switchBgm.setOnCheckedChangeListener((buttonView, isChecked) -> {
                musicOn = isChecked;
                prefs.edit().putBoolean(AppConstants.KEY_BGM_ON, musicOn).apply();
                updateAudioButtonsState();
            });
        }

        if (switchFullscreen != null) {
            switchFullscreen.setChecked(fullScreen);
            switchFullscreen.setOnCheckedChangeListener((buttonView, isChecked) -> {
                fullScreen = isChecked;
                prefs.edit().putBoolean(AppConstants.KEY_FULLSCREEN, fullScreen).apply();
                updateAudioButtonsState();
                if (fullScreen) {
                    UiUtils.hideSystemUI(MainMenuActivity.this);
                    if (window != null) {
                        UiUtils.hideSystemUI(window);
                    }
                } else {
                    View decorView = getWindow().getDecorView();
                    if (decorView != null) {
                        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                    if (window != null && window.getDecorView() != null) {
                        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                }
            });
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.setOnDismissListener(d -> {
            if (fullScreen) {
                UiUtils.hideSystemUI(MainMenuActivity.this);
            }
        });

        dialog.show();

        if (window != null) {
            if (fullScreen) {
                UiUtils.hideSystemUI(window);
            }
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

            // Giới hạn chiều rộng tối đa ~58% - 60% màn hình, căn giữa
            DisplayMetrics dm = getResources().getDisplayMetrics();
            int dialogWidth = (int) (dm.widthPixels * 0.58f);
            window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
        }
    }

    private void saveGameState() {
        int userId = getLoggedInUserId();
        prefs.edit()
                .putBoolean("has_saved_game", true)
                .putLong("saved_at", System.currentTimeMillis())
                .apply();
        if (userId != -1) {
            Toast.makeText(this, "Đã lưu trạng thái tài khoản vào thiết bị!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Đã lưu cài đặt sảnh game!", Toast.LENGTH_SHORT).show();
        }
    }
}
