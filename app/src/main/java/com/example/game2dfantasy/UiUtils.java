package com.example.game2dfantasy;

import android.app.Activity;
import android.os.Build;
import android.view.DisplayCutout;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

public class UiUtils {

    private UiUtils() {}

    /**
     * Tự động ẩn thanh trạng thái và thanh điều hướng phù hợp theo từng phiên bản Android.
     * API 30+ (Android 11 trở lên): setDecorFitsSystemWindows(false) kèm WindowInsetsController ẩn system bars.
     * API 24-29: cờ LAYOUT_STABLE | LAYOUT_HIDE_NAVIGATION | LAYOUT_FULLSCREEN | HIDE_NAVIGATION | FULLSCREEN | IMMERSIVE_STICKY.
     */
    public static void hideSystemUI(Activity activity) {
        if (activity == null) return;
        hideSystemUI(activity.getWindow());
    }

    public static void hideSystemUI(Window window) {
        if (window == null) return;
        try {
            // API 28+: Kích hoạt chế độ SHORT_EDGES trên Window để phủ kín vùng camera đục lỗ / tai thỏ
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WindowManager.LayoutParams lp = window.getAttributes();
                if (lp.layoutInDisplayCutoutMode != WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES) {
                    lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                    window.setAttributes(lp);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false);
                WindowInsetsController controller = window.getInsetsController();
                if (controller != null) {
                    controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                    controller.setSystemBarsBehavior(
                            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            } else {
                View decorView = window.getDecorView();
                if (decorView != null) {
                    decorView.setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
            }
        } catch (Exception ignored) {
            // Ngăn chặn crash do cửa sổ chưa sẵn sàng trên một số thiết bị thật
        }
    }

    /**
     * Đẩy khung nội dung / nút tương tác quan trọng ra khỏi vùng đục lỗ camera (DisplayCutout) bằng padding.
     * Từ API 28: dùng getRootWindowInsets().getDisplayCutout() hoặc OnApplyWindowInsetsListener,
     * nếu null thì áp dụng padding mặc định.
     */
    public static void applyCutoutPadding(View targetView, int defaultPadLeft, int defaultPadTop, int defaultPadRight, int defaultPadBottom) {
        if (targetView == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                targetView.setOnApplyWindowInsetsListener((v, insets) -> {
                    try {
                        DisplayCutout cutout = insets.getDisplayCutout();
                        int safeLeft = cutout != null ? cutout.getSafeInsetLeft() : 0;
                        int safeRight = cutout != null ? cutout.getSafeInsetRight() : 0;
                        v.setPadding(
                                Math.max(defaultPadLeft, safeLeft),
                                defaultPadTop,
                                Math.max(defaultPadRight, safeRight),
                                defaultPadBottom
                        );
                    } catch (Exception ignored) {}
                    return insets;
                });

                WindowInsets rootInsets = targetView.getRootWindowInsets();
                if (rootInsets != null) {
                    DisplayCutout cutout = rootInsets.getDisplayCutout();
                    int safeLeft = cutout != null ? cutout.getSafeInsetLeft() : 0;
                    int safeRight = cutout != null ? cutout.getSafeInsetRight() : 0;
                    targetView.setPadding(
                            Math.max(defaultPadLeft, safeLeft),
                            defaultPadTop,
                            Math.max(defaultPadRight, safeRight),
                            defaultPadBottom
                    );
                } else {
                    targetView.setPadding(defaultPadLeft, defaultPadTop, defaultPadRight, defaultPadBottom);
                }
            } else {
                targetView.setPadding(defaultPadLeft, defaultPadTop, defaultPadRight, defaultPadBottom);
            }
        } catch (Exception ignored) {
            targetView.setPadding(defaultPadLeft, defaultPadTop, defaultPadRight, defaultPadBottom);
        }
    }

    /**
     * Cập nhật trực tiếp padding an toàn tránh camera khi window đã attach hoặc focus thay đổi.
     */
    public static void updateCutoutPadding(View targetView, int defaultPadLeft, int defaultPadTop, int defaultPadRight, int defaultPadBottom) {
        if (targetView == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WindowInsets rootInsets = targetView.getRootWindowInsets();
                if (rootInsets != null) {
                    DisplayCutout cutout = rootInsets.getDisplayCutout();
                    int safeLeft = cutout != null ? cutout.getSafeInsetLeft() : 0;
                    int safeRight = cutout != null ? cutout.getSafeInsetRight() : 0;
                    targetView.setPadding(
                            Math.max(defaultPadLeft, safeLeft),
                            defaultPadTop,
                            Math.max(defaultPadRight, safeRight),
                            defaultPadBottom
                    );
                    return;
                }
            }
            targetView.setPadding(defaultPadLeft, defaultPadTop, defaultPadRight, defaultPadBottom);
        } catch (Exception ignored) {}
    }
}
