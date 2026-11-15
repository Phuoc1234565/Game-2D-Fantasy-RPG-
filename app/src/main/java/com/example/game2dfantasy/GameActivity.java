package com.example.game2dfantasy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Bản Stub cho GameActivity:
 * Hiển thị thông tin nhân vật cơ bản và trạng thái sẵn sàng vào game.
 * TODO: Thay thế khu vực hiển thị bằng GameSurfaceView để render thế giới 2D Fantasy.
 */
public class GameActivity extends AppCompatActivity {

    private int userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        UiUtils.hideSystemUI(this);

        if (getIntent() != null) {
            userId = getIntent().getIntExtra(AppConstants.EXTRA_USER_ID, -1);
        }

        ImageView imgBackground = findViewById(R.id.imgBackground);
        if (imgBackground != null) {
            int bgId = getResources().getIdentifier("menu_bg", "drawable", getPackageName());
            if (bgId != 0) {
                imgBackground.setImageResource(bgId);
            } else {
                imgBackground.setImageResource(R.drawable.splash_bg_fallback);
            }
        }

        TextView tvGreeting = findViewById(R.id.tv_game_greeting);
        if (userId != -1 && tvGreeting != null) {
            CharacterData charData = DatabaseHelper.getInstance(this).getCharacter(userId);
            if (charData != null) {
                String greeting = String.format("Chào, %s - %s Lv%d - HP: %d/%d | ATK: %d",
                        charData.getCharName(),
                        charData.getJobClass(),
                        charData.getLevel(),
                        charData.getHp(),
                        charData.getMaxHp(),
                        charData.getAtk());
                tvGreeting.setText(greeting);
            }
        }

        Button btnBack = findViewById(R.id.btn_game_back_menu);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> returnToMenu());
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                returnToMenu();
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            UiUtils.hideSystemUI(this);
        }
    }

    private void returnToMenu() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
