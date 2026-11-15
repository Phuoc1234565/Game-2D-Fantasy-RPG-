package com.example.game2dfantasy;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * [DEBUG CODE] Lớp Application bắt và ghi nhận crash log trên thiết bị thật.
 * Ghi chú: Đây là mã gỡ lỗi hỗ trợ kiểm tra trên máy thật, sẽ gỡ trước khi nộp đồ án.
 */
public class IsekaiApp extends Application {

    public static final String CRASH_LOG_FILE = "crash_log.txt";
    private static final String TAG = "IsekaiApp";

    @Override
    public void onCreate() {
        super.onCreate();

        // Lưu lại handler mặc định của hệ thống
        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        // Cài đặt UncaughtExceptionHandler để ghi log khi xảy ra ngoại lệ chưa bắt
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                throwable.printStackTrace(pw);
                String stackTrace = sw.toString();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String timeStr = sdf.format(new Date());

                StringBuilder logBuilder = new StringBuilder();
                logBuilder.append("=== CRASH LOG ===\n");
                logBuilder.append("Thời gian: ").append(timeStr).append("\n");
                logBuilder.append("Thiết bị: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n");
                logBuilder.append("Android SDK: ").append(Build.VERSION.SDK_INT)
                        .append(" (Phiên bản: ").append(Build.VERSION.RELEASE).append(")\n");
                logBuilder.append("Thread: ").append(thread.getName()).append("\n\n");
                logBuilder.append("Chi tiết lỗi (Stack Trace):\n");
                logBuilder.append(stackTrace);
                logBuilder.append("\n=================\n");

                File logFile = new File(getFilesDir(), CRASH_LOG_FILE);
                FileOutputStream fos = new FileOutputStream(logFile, false);
                fos.write(logBuilder.toString().getBytes());
                fos.flush();
                fos.close();
            } catch (Exception e) {
                Log.e(TAG, "Không thể ghi crash log: " + e.getMessage());
            } finally {
                // Chuyển tiếp tới handler mặc định để hệ thống xử lý tiếp
                if (defaultHandler != null) {
                    defaultHandler.uncaughtException(thread, throwable);
                }
            }
        });
    }
}
