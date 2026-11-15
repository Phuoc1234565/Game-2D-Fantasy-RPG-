package com.example.game2dfantasy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private TextView tvError, tvFormTitle, tvSwitchMode;
    private Button btnSubmit, btnBack;
    private ImageButton btnTogglePassword;
    private ImageView imgBackground, ivLoginLogo;
    private View llLeft, svRight;

    private boolean isPasswordVisible = false;
    private String currentMode = AppConstants.MODE_LOGIN;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        UiUtils.hideSystemUI(this);

        db = DatabaseHelper.getInstance(this);

        // Ánh xạ View
        imgBackground = findViewById(R.id.imgBackground);
        ivLoginLogo = findViewById(R.id.iv_login_logo);
        llLeft = findViewById(R.id.ll_left);
        svRight = findViewById(R.id.sv_right);

        tvFormTitle = findViewById(R.id.tv_form_title);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnTogglePassword = findViewById(R.id.btn_toggle_password);
        tvError = findViewById(R.id.tv_error);
        btnSubmit = findViewById(R.id.btn_submit);
        tvSwitchMode = findViewById(R.id.tv_switch_mode);
        btnBack = findViewById(R.id.btn_back);

        // Nạp ảnh nền và logo an toàn
        setupBackground();
        setupLogo();

        // Áp dụng khoảng cách an toàn tránh vùng camera đục lỗ ở cả 2 mép
        applyCutoutInsets();

        // Đọc chế độ khởi tạo từ Intent extra
        if (getIntent() != null && getIntent().hasExtra(AppConstants.EXTRA_MODE)) {
            String mode = getIntent().getStringExtra(AppConstants.EXTRA_MODE);
            if (AppConstants.MODE_REGISTER.equals(mode)) {
                currentMode = AppConstants.MODE_REGISTER;
            }
        }
        applyModeUI();

        // Xử lý các sự kiện click
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                if (AppConstants.MODE_REGISTER.equals(currentMode)) {
                    attemptRegister();
                } else {
                    attemptLogin();
                }
            });
        }

        if (tvSwitchMode != null) {
            tvSwitchMode.setOnClickListener(v -> toggleMode());
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> navigateBack());
        }

        if (btnTogglePassword != null) {
            btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        }

        // Nút Back vật lý quay về MainMenuActivity
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateBack();
            }
        });
    }

    private void setupBackground() {
        if (imgBackground == null) return;
        // Ưu tiên menu_bg -> gradient tím đêm dự phòng
        int bgId = getResources().getIdentifier("menu_bg", "drawable", getPackageName());
        if (bgId != 0) {
            imgBackground.setImageResource(bgId);
        } else {
            imgBackground.setImageResource(R.drawable.splash_bg_fallback);
        }
    }

    private void setupLogo() {
        if (ivLoginLogo == null) return;
        try {
            int logoId = getResources().getIdentifier("game_logo", "drawable", getPackageName());
            if (logoId != 0) {
                ivLoginLogo.setImageResource(logoId);
            } else {
                ivLoginLogo.setImageResource(R.mipmap.ic_launcher);
            }
        } catch (Exception ignored) {}
    }

    private void applyModeUI() {
        clearError();
        boolean isRegister = AppConstants.MODE_REGISTER.equals(currentMode);
        if (tvFormTitle != null) {
            tvFormTitle.setText(isRegister ? R.string.register_mode_title : R.string.login_mode_title);
        }
        if (btnSubmit != null) {
            btnSubmit.setText(isRegister ? R.string.menu_register : R.string.menu_login);
        }
        if (tvSwitchMode != null) {
            tvSwitchMode.setText(isRegister ? R.string.switch_to_login : R.string.switch_to_register);
        }
        if (etUsername != null) {
            etUsername.requestFocus();
        }
    }

    private void toggleMode() {
        if (AppConstants.MODE_REGISTER.equals(currentMode)) {
            currentMode = AppConstants.MODE_LOGIN;
        } else {
            currentMode = AppConstants.MODE_REGISTER;
        }
        applyModeUI();
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (etPassword == null) return;
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            if (btnTogglePassword != null) {
                btnTogglePassword.setImageResource(R.drawable.ic_visibility_on);
            }
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            if (btnTogglePassword != null) {
                btnTogglePassword.setImageResource(R.drawable.ic_visibility_off);
            }
        }
        etPassword.setTypeface(Typeface.SERIF);
        if (etPassword.getText() != null) {
            etPassword.setSelection(etPassword.getText().length());
        }
    }

    private void applyCutoutInsets() {
        int padLeftStart = (int) (24 * getResources().getDisplayMetrics().density);
        int padLeftEnd = (int) (16 * getResources().getDisplayMetrics().density);
        int padRightStart = (int) (8 * getResources().getDisplayMetrics().density);
        int padRightEnd = (int) (24 * getResources().getDisplayMetrics().density);
        int padV = (int) (8 * getResources().getDisplayMetrics().density);

        UiUtils.applyCutoutPadding(llLeft, padLeftStart, 0, padLeftEnd, 0);
        UiUtils.applyCutoutPadding(svRight, padRightStart, padV, padRightEnd, padV);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            UiUtils.hideSystemUI(this);
            int padLeftStart = (int) (24 * getResources().getDisplayMetrics().density);
            int padLeftEnd = (int) (16 * getResources().getDisplayMetrics().density);
            int padRightStart = (int) (8 * getResources().getDisplayMetrics().density);
            int padRightEnd = (int) (24 * getResources().getDisplayMetrics().density);
            int padV = (int) (8 * getResources().getDisplayMetrics().density);

            UiUtils.updateCutoutPadding(llLeft, padLeftStart, 0, padLeftEnd, 0);
            UiUtils.updateCutoutPadding(svRight, padRightStart, padV, padRightEnd, padV);
        }
    }

    private String getUsername() {
        if (etUsername == null || etUsername.getText() == null) return "";
        return etUsername.getText().toString().trim();
    }

    private String getPassword() {
        if (etPassword == null || etPassword.getText() == null) return "";
        return etPassword.getText().toString().trim();
    }

    /** Validate input: Username >= 3 ký tự, Password >= 4 ký tự. */
    private String validate(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            return getString(R.string.err_empty_fields);
        }
        if (username.length() < 3) {
            return getString(R.string.err_username_short);
        }
        if (password.length() < 4) {
            return getString(R.string.err_password_short);
        }
        return null;
    }

    private void showError(String msg) {
        if (tvError != null) {
            tvError.setText(msg);
            tvError.setVisibility(View.VISIBLE);
        }
    }

    private void clearError() {
        if (tvError != null) {
            tvError.setText("");
            tvError.setVisibility(View.GONE);
        }
    }

    private void attemptLogin() {
        clearError();
        String username = getUsername();
        String password = getPassword();

        String err = validate(username, password);
        if (err != null) {
            showError(err);
            return;
        }

        int userId = db.loginUser(username, password);
        if (userId == -1) {
            showError(getString(R.string.err_login_failed));
            return;
        }

        // Lưu user_id vào SharedPreferences dùng chung
        try {
            SharedPreferences prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
            prefs.edit().putInt(AppConstants.KEY_USER_ID, userId).apply();
        } catch (Exception ignored) {}

        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
        navigateBack();
    }

    private void attemptRegister() {
        clearError();
        String username = getUsername();
        String password = getPassword();

        String err = validate(username, password);
        if (err != null) {
            showError(err);
            return;
        }

        boolean success = db.registerUser(username, password);
        if (success) {
            Toast.makeText(this, getString(R.string.msg_register_success), Toast.LENGTH_SHORT).show();
            // Tự động chuyển về giao diện đăng nhập để người dùng đăng nhập ngay
            currentMode = AppConstants.MODE_LOGIN;
            applyModeUI();
            if (etPassword != null) {
                etPassword.setText("");
            }
        } else {
            showError(getString(R.string.err_username_exists));
        }
    }

    private void navigateBack() {
        Intent intent = new Intent(LoginActivity.this, MainMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
