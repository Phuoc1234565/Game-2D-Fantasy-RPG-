package com.example.game2dfantasy;

public final class JobConfig {

    private JobConfig() {}

    public static final String JOB_WARRIOR = DatabaseHelper.JOB_WARRIOR; // "Kiếm Sĩ"
    public static final String JOB_MAGE = DatabaseHelper.JOB_MAGE;       // "Pháp Sư"

    public static final int MAX_HP = 120;
    public static final int MAX_ATK = 18;

    // Kiếm Sĩ: HP 120, ATK 12, tầm đánh "Gần" (40%), vũ khí "Kiếm Tân Thủ"
    public static final int WARRIOR_HP = 120;
    public static final int WARRIOR_ATK = 12;
    public static final String WARRIOR_RANGE = "Gần";
    public static final int WARRIOR_RANGE_PERCENT = 40;
    public static final String WARRIOR_WEAPON = "Kiếm Tân Thủ";
    public static final String WARRIOR_DESC = "HP cao, ATK cân bằng";

    // Pháp Sư: HP 80, ATK 18, tầm đánh "Xa" (100%), vũ khí "Trượng Gỗ Ma Pháp"
    public static final int MAGE_HP = 80;
    public static final int MAGE_ATK = 18;
    public static final String MAGE_RANGE = "Xa";
    public static final int MAGE_RANGE_PERCENT = 100;
    public static final String MAGE_WEAPON = "Trượng Gỗ Ma Pháp";
    public static final String MAGE_DESC = "ATK cao, đánh tầm xa";
}
