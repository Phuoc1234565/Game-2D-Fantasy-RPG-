package com.example.game2dfantasy;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class CharacterSelectActivity extends AppCompatActivity {

    private int userId = -1;
    private String selectedJob = JobConfig.JOB_WARRIOR;
    private String selectedWeapon = JobConfig.WARRIOR_WEAPON;

    private LinearLayout cardWarrior, cardMage;
    private ImageView ivCharPreview, ivWeaponIcon, imgBackground;
    private TextView tvWeaponName, tvHpVal, tvAtkVal, tvRangeVal, tvCharError;
    private ProgressBar pbHp, pbAtk, pbRange;
    private EditText etCharName;
    private Button btnStart;
    private ImageButton btnDice;

    private View llColLeft, llColCenter, svColRight;
    private DatabaseHelper db;
    private int lastDiceIndex = -1;
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_select);
        UiUtils.hideSystemUI(this);

        db = DatabaseHelper.getInstance(this);

        // Xử lý Intent extra USER_ID một cách an toàn
        if (savedInstanceState != null) {
            userId = savedInstanceState.getInt("saved_user_id", -1);
            selectedJob = savedInstanceState.getString("saved_selected_job", JobConfig.JOB_WARRIOR);
            selectedWeapon = savedInstanceState.getString("saved_selected_weapon", JobConfig.WARRIOR_WEAPON);
        }
        if (userId == -1 && getIntent() != null) {
            userId = getIntent().getIntExtra(AppConstants.EXTRA_USER_ID, -1);
        }
        if (userId == -1) {
            try {
                SharedPreferences prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
                userId = prefs.getInt(AppConstants.KEY_USER_ID, -1);
            } catch (Exception ignored) {}
        }

        if (userId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin tài khoản", Toast.LENGTH_SHORT).show();
            navigateBackToMenu();
            return;
        }

        // Nếu người chơi đã có nhân vật thì chuyển thẳng vào GameActivity
        if (db.hasCharacter(userId)) {
            launchGameActivity();
            return;
        }

        // Ánh xạ View
        imgBackground = findViewById(R.id.imgBackground);
        llColLeft     = findViewById(R.id.ll_col_left);
        llColCenter   = findViewById(R.id.ll_col_center);
        svColRight    = findViewById(R.id.sv_col_right);

        cardWarrior   = findViewById(R.id.card_warrior);
        cardMage      = findViewById(R.id.card_mage);
        ivCharPreview = findViewById(R.id.iv_char_preview);
        ivWeaponIcon  = findViewById(R.id.iv_weapon_icon);
        tvWeaponName  = findViewById(R.id.tv_weapon_name);

        etCharName    = findViewById(R.id.et_char_name);
        btnDice       = findViewById(R.id.btn_dice);
        btnStart      = findViewById(R.id.btn_start);
        tvCharError   = findViewById(R.id.tv_char_error);

        pbHp          = findViewById(R.id.pb_hp);
        pbAtk         = findViewById(R.id.pb_atk);
        pbRange       = findViewById(R.id.pb_range);
        tvHpVal       = findViewById(R.id.tv_hp_val);
        tvAtkVal      = findViewById(R.id.tv_atk_val);
        tvRangeVal    = findViewById(R.id.tv_range_val);

        setupBackground();
        applyCutoutInsets();

        // Cài đặt sự kiện chọn job
        if (cardWarrior != null) {
            cardWarrior.setOnClickListener(v -> selectJob(JobConfig.JOB_WARRIOR, true));
        }
        if (cardMage != null) {
            cardMage.setOnClickListener(v -> selectJob(JobConfig.JOB_MAGE, true));
        }

        // Nút xúc xắc tạo tên ngẫu nhiên
        if (btnDice != null) {
            btnDice.setOnClickListener(v -> rollRandomName());
        }

        // Nút Bắt đầu hành trình
        if (btnStart != null) {
            btnStart.setOnClickListener(v -> attemptCreateCharacter());
        }

        // Khởi tạo lựa chọn ban đầu (không animation lúc mở màn)
        selectJob(selectedJob, false);

        // Nút Back vật lý quay về MainMenuActivity
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateBackToMenu();
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

    private void applyCutoutInsets() {
        int padH = (int) (18 * getResources().getDisplayMetrics().density);
        int padV = (int) (10 * getResources().getDisplayMetrics().density);
        UiUtils.applyCutoutPadding(llColLeft, padH, 0, 0, 0);
        UiUtils.applyCutoutPadding(svColRight, 0, padV, padH, padV);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            UiUtils.hideSystemUI(this);
            int padH = (int) (18 * getResources().getDisplayMetrics().density);
            int padV = (int) (10 * getResources().getDisplayMetrics().density);
            UiUtils.updateCutoutPadding(llColLeft, padH, 0, 0, 0);
            UiUtils.updateCutoutPadding(svColRight, 0, padV, padH, padV);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("saved_user_id", userId);
        outState.putString("saved_selected_job", selectedJob);
        outState.putString("saved_selected_weapon", selectedWeapon);
    }

    private void selectJob(String job, boolean animate) {
        selectedJob = job;
        boolean isWarrior = JobConfig.JOB_WARRIOR.equals(job);
        selectedWeapon = isWarrior ? JobConfig.WARRIOR_WEAPON : JobConfig.MAGE_WEAPON;

        // Cập nhật viền & độ mờ 2 card
        if (cardWarrior != null) {
            cardWarrior.setBackgroundResource(isWarrior ? R.drawable.bg_card_job_selected : R.drawable.bg_card_job_normal);
            cardWarrior.setAlpha(isWarrior ? 1.0f : 0.6f);
        }
        if (cardMage != null) {
            cardMage.setBackgroundResource(!isWarrior ? R.drawable.bg_card_job_selected : R.drawable.bg_card_job_normal);
            cardMage.setAlpha(!isWarrior ? 1.0f : 0.6f);
        }

        // Cập nhật vũ khí
        if (tvWeaponName != null) {
            tvWeaponName.setText(selectedWeapon);
        }
        if (ivWeaponIcon != null) {
            int weaponResId = getResources().getIdentifier(
                    isWarrior ? "icon_weapon_sword" : "icon_weapon_staff",
                    "drawable", getPackageName()
            );
            if (weaponResId != 0) {
                ivWeaponIcon.setImageResource(weaponResId);
            } else {
                ivWeaponIcon.setImageResource(isWarrior ? R.drawable.fallback_weapon_sword : R.drawable.fallback_weapon_staff);
            }
        }

        // Cập nhật ảnh nhân vật (fade ~200ms)
        if (ivCharPreview != null) {
            if (animate) {
                ivCharPreview.animate().alpha(0f).setDuration(100).withEndAction(() -> {
                    updateCharImage(isWarrior);
                    ivCharPreview.animate().alpha(1f).setDuration(100).start();
                }).start();
            } else {
                updateCharImage(isWarrior);
            }
        }

        // Cập nhật chỉ số (animate progress ~300ms)
        int targetHp = isWarrior ? JobConfig.WARRIOR_HP : JobConfig.MAGE_HP;
        int targetAtk = isWarrior ? JobConfig.WARRIOR_ATK : JobConfig.MAGE_ATK;
        int targetRange = isWarrior ? JobConfig.WARRIOR_RANGE_PERCENT : JobConfig.MAGE_RANGE_PERCENT;
        String rangeLabel = isWarrior ? getString(R.string.range_near) : getString(R.string.range_far);

        if (pbHp != null) {
            if (animate) {
                ObjectAnimator.ofInt(pbHp, "progress", pbHp.getProgress(), targetHp).setDuration(300).start();
            } else {
                pbHp.setProgress(targetHp);
            }
        }
        if (pbAtk != null) {
            if (animate) {
                ObjectAnimator.ofInt(pbAtk, "progress", pbAtk.getProgress(), targetAtk).setDuration(300).start();
            } else {
                pbAtk.setProgress(targetAtk);
            }
        }
        if (pbRange != null) {
            if (animate) {
                ObjectAnimator.ofInt(pbRange, "progress", pbRange.getProgress(), targetRange).setDuration(300).start();
            } else {
                pbRange.setProgress(targetRange);
            }
        }

        if (tvHpVal != null) tvHpVal.setText(targetHp + "/" + JobConfig.MAX_HP);
        if (tvAtkVal != null) tvAtkVal.setText(String.valueOf(targetAtk));
        if (tvRangeVal != null) tvRangeVal.setText(rangeLabel);

        clearError();
    }

    private void updateCharImage(boolean isWarrior) {
        if (ivCharPreview == null) return;
        int charResId = getResources().getIdentifier(
                isWarrior ? "char_swordsman" : "char_mage",
                "drawable", getPackageName()
        );
        if (charResId != 0) {
            ivCharPreview.setImageResource(charResId);
        } else {
            ivCharPreview.setImageResource(isWarrior ? R.drawable.fallback_char_swordsman : R.drawable.fallback_char_mage);
        }

        // Tắt lọc mịn (anti-aliasing) cho sprite pixel art
        Drawable d = ivCharPreview.getDrawable();
        if (d instanceof BitmapDrawable) {
            ((BitmapDrawable) d).setFilterBitmap(false);
        }
    }

    private void rollRandomName() {
        String[] names = getResources().getStringArray(R.array.fantasy_names);
        if (names.length == 0 || etCharName == null) return;
        int idx;
        if (names.length == 1) {
            idx = 0;
        } else {
            do {
                idx = random.nextInt(names.length);
            } while (idx == lastDiceIndex);
        }
        lastDiceIndex = idx;
        etCharName.setText(names[idx]);
        etCharName.setError(null);
        if (etCharName.getText() != null) {
            etCharName.setSelection(etCharName.getText().length());
        }
        clearError();
    }

    private void clearError() {
        if (tvCharError != null) {
            tvCharError.setText("");
            tvCharError.setVisibility(View.GONE);
        }
    }

    private void showError(String msg) {
        if (tvCharError != null) {
            tvCharError.setText(msg);
            tvCharError.setVisibility(View.VISIBLE);
        }
    }

    private void attemptCreateCharacter() {
        clearError();
        String name = "";
        if (etCharName != null && etCharName.getText() != null) {
            name = etCharName.getText().toString().trim();
        }

        if (name.length() < 2 || name.length() > 12) {
            if (etCharName != null) {
                etCharName.setError(getString(R.string.err_name_length));
                etCharName.requestFocus();
            } else {
                showError(getString(R.string.err_name_length));
            }
            return;
        }

        // Vô hiệu hóa nút chống bấm đúp
        if (btnStart != null) {
            btnStart.setEnabled(false);
        }

        boolean success = db.createCharacter(userId, name, selectedJob, selectedWeapon);
        if (success) {
            Toast.makeText(this, getString(R.string.msg_char_created), Toast.LENGTH_SHORT).show();
            launchGameActivity();
        } else {
            if (btnStart != null) {
                btnStart.setEnabled(true);
            }
            showError("Tạo nhân vật thất bại. Vui lòng thử lại.");
        }
    }

    private void launchGameActivity() {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(AppConstants.EXTRA_USER_ID, userId);
        startActivity(intent);
        finish();
    }

    private void navigateBackToMenu() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
