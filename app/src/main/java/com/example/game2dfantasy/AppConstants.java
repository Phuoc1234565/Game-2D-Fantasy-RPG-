package com.example.game2dfantasy;

public final class AppConstants {

    private AppConstants() {}

    // SharedPreferences
    public static final String PREF_NAME = "isekai_prefs";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_SFX_ON = "sfx_on";
    public static final String KEY_BGM_ON = "bgm_on";
    public static final String KEY_FULLSCREEN = "fullscreen";

    // Intent Extras
    public static final String EXTRA_USER_ID = "USER_ID";
    public static final String EXTRA_MODE = "MODE";

    // Mode Values
    public static final String MODE_LOGIN = "login";
    public static final String MODE_REGISTER = "register";
}
