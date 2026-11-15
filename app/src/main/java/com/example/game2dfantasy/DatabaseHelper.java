package com.example.game2dfantasy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DB_NAME = "isekai_rpg.db";
    private static final int DB_VERSION = 1;
    // Fixed salt – enough for a student project
    private static final String SALT = "IsekaiRPG_S@lt_2025";

    // Table: Users
    private static final String TABLE_USERS = "Users";
    private static final String COL_U_ID = "id";
    private static final String COL_U_USERNAME = "username";
    private static final String COL_U_PASSWORD = "password";

    // Table: Characters
    private static final String TABLE_CHARS = "Characters";
    private static final String COL_C_USER_ID = "user_id";
    private static final String COL_C_CHAR_NAME = "char_name";
    private static final String COL_C_JOB_CLASS = "job_class";
    private static final String COL_C_LEVEL = "level";
    private static final String COL_C_EXP = "exp";
    private static final String COL_C_HP = "hp";
    private static final String COL_C_MAX_HP = "max_hp";
    private static final String COL_C_ATK = "atk";
    private static final String COL_C_EQUIPPED_WEAPON = "equipped_weapon";

    // Table: Inventory_History
    private static final String TABLE_INV = "Inventory_History";
    private static final String COL_I_ID = "id";
    private static final String COL_I_USER_ID = "user_id";
    private static final String COL_I_ITEM_NAME = "item_name";
    private static final String COL_I_RARITY = "rarity";
    private static final String COL_I_ITEM_TYPE = "item_type";
    private static final String COL_I_IS_EQUIPPED = "is_equipped";
    private static final String COL_I_OBTAINED_TIME = "obtained_time";

    // Job classes
    public static final String JOB_WARRIOR = "Kiếm Sĩ";
    public static final String JOB_MAGE = "Pháp Sư";

    // Item types
    public static final String TYPE_WEAPON = "WEAPON";
    public static final String TYPE_POTION = "POTION";
    public static final String TYPE_MATERIAL = "MATERIAL";

    // Rarity
    public static final String RARITY_COMMON = "Thuong";
    public static final String RARITY_RARE = "Hiem";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context ctx) {
        if (instance == null) {
            instance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " ("
                + COL_U_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_U_USERNAME + " TEXT UNIQUE NOT NULL, "
                + COL_U_PASSWORD + " TEXT NOT NULL"
                + ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CHARS + " ("
                + COL_C_USER_ID + " INTEGER PRIMARY KEY, "
                + COL_C_CHAR_NAME + " TEXT, "
                + COL_C_JOB_CLASS + " TEXT, "
                + COL_C_LEVEL + " INTEGER DEFAULT 1, "
                + COL_C_EXP + " INTEGER DEFAULT 0, "
                + COL_C_HP + " INTEGER DEFAULT 100, "
                + COL_C_MAX_HP + " INTEGER DEFAULT 100, "
                + COL_C_ATK + " INTEGER DEFAULT 10, "
                + COL_C_EQUIPPED_WEAPON + " TEXT, "
                + "FOREIGN KEY(" + COL_C_USER_ID + ") REFERENCES "
                + TABLE_USERS + "(" + COL_U_ID + ") ON DELETE CASCADE"
                + ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_INV + " ("
                + COL_I_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_I_USER_ID + " INTEGER, "
                + COL_I_ITEM_NAME + " TEXT, "
                + COL_I_RARITY + " TEXT, "
                + COL_I_ITEM_TYPE + " TEXT, "
                + COL_I_IS_EQUIPPED + " INTEGER DEFAULT 0, "
                + COL_I_OBTAINED_TIME + " TEXT, "
                + "FOREIGN KEY(" + COL_I_USER_ID + ") REFERENCES "
                + TABLE_USERS + "(" + COL_U_ID + ") ON DELETE CASCADE"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            // Drop tables theo thứ tự khóa ngoại an toàn
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_INV);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHARS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            onCreate(db);
        } catch (Exception e) {
            Log.e(TAG, "Error in onUpgrade", e);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Tái sử dụng logic xóa an toàn và tạo lại bảng khi hạ cấp phiên bản db
        onUpgrade(db, oldVersion, newVersion);
    }

    // ─── Password hashing ──────────────────────────────────────────────────────

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String salted = SALT + password;
            byte[] hash = digest.digest(salted.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "SHA-256 not available", e);
            return password; // fallback (should never happen on Android)
        }
    }

    // ─── Users ─────────────────────────────────────────────────────────────────

    /**
     * Register a new user.
     * @return true on success, false if username already exists or input invalid.
     */
    public boolean registerUser(String username, String password) {
        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            return false;
        }
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_U_USERNAME, username.trim());
            cv.put(COL_U_PASSWORD, hashPassword(password));
            long result = db.insertOrThrow(TABLE_USERS, null, cv);
            return result != -1;
        } catch (Exception e) {
            Log.w(TAG, "registerUser failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Authenticate user.
     * @return user_id on success, -1 on failure.
     */
    public int loginUser(String username, String password) {
        if (username == null || password == null) return -1;
        try {
            SQLiteDatabase db = getReadableDatabase();
            String hashed = hashPassword(password);
            try (Cursor cursor = db.query(TABLE_USERS,
                    new String[]{COL_U_ID},
                    COL_U_USERNAME + "=? AND " + COL_U_PASSWORD + "=?",
                    new String[]{username.trim(), hashed},
                    null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getInt(0);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "loginUser error: " + e.getMessage());
        }
        return -1;
    }

    // ─── Characters ────────────────────────────────────────────────────────────

    /**
     * Check whether a character already exists for this user.
     */
    public boolean hasCharacter(int userId) {
        try {
            SQLiteDatabase db = getReadableDatabase();
            try (Cursor cursor = db.query(TABLE_CHARS,
                    new String[]{COL_C_USER_ID},
                    COL_C_USER_ID + "=?",
                    new String[]{String.valueOf(userId)},
                    null, null, null)) {
                return cursor != null && cursor.moveToFirst();
            }
        } catch (Exception e) {
            Log.e(TAG, "hasCharacter error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Create a new character for a user.
     * Initial stats by job:
     *   Kiếm Sĩ → hp/max_hp 120, atk 12
     *   Pháp Sư  → hp/max_hp 80,  atk 18
     * Also inserts the starting weapon into Inventory_History with is_equipped=1.
     */
    public boolean createCharacter(int userId, String name, String jobClass, String startWeapon) {
        if (name == null || name.trim().isEmpty()) return false;
        if (hasCharacter(userId)) return false;

        int hp, atk;
        if (JOB_WARRIOR.equals(jobClass)) {
            hp = 120; atk = 12;
        } else if (JOB_MAGE.equals(jobClass)) {
            hp = 80;  atk = 18;
        } else {
            hp = 100; atk = 10;
        }

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // (1) Insert character
            ContentValues cv = new ContentValues();
            cv.put(COL_C_USER_ID, userId);
            cv.put(COL_C_CHAR_NAME, name.trim());
            cv.put(COL_C_JOB_CLASS, jobClass);
            cv.put(COL_C_LEVEL, 1);
            cv.put(COL_C_EXP, 0);
            cv.put(COL_C_HP, hp);
            cv.put(COL_C_MAX_HP, hp);
            cv.put(COL_C_ATK, atk);
            cv.put(COL_C_EQUIPPED_WEAPON, startWeapon != null ? startWeapon : "");
            long charResult = db.insertOrThrow(TABLE_CHARS, null, cv);
            if (charResult == -1) return false;

            // (2) Insert starting weapon into inventory (is_equipped = 1)
            String now = getCurrentTime();
            if (startWeapon != null && !startWeapon.isEmpty()) {
                ContentValues icv = new ContentValues();
                icv.put(COL_I_USER_ID, userId);
                icv.put(COL_I_ITEM_NAME, startWeapon);
                icv.put(COL_I_RARITY, RARITY_COMMON);
                icv.put(COL_I_ITEM_TYPE, TYPE_WEAPON);
                icv.put(COL_I_IS_EQUIPPED, 1);
                icv.put(COL_I_OBTAINED_TIME, now);
                db.insertOrThrow(TABLE_INV, null, icv);
            }

            // (3) Chèn 2 dòng "Bình Máu Nhỏ" riêng (rarity "Thuong", item_type "POTION", is_equipped=0)
            for (int i = 0; i < 2; i++) {
                ContentValues pcv = new ContentValues();
                pcv.put(COL_I_USER_ID, userId);
                pcv.put(COL_I_ITEM_NAME, "Bình Máu Nhỏ");
                pcv.put(COL_I_RARITY, RARITY_COMMON);
                pcv.put(COL_I_ITEM_TYPE, TYPE_POTION);
                pcv.put(COL_I_IS_EQUIPPED, 0);
                pcv.put(COL_I_OBTAINED_TIME, now);
                db.insertOrThrow(TABLE_INV, null, pcv);
            }

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "createCharacter failed: " + e.getMessage());
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Load CharacterData for a user. Returns null if not found.
     */
    public CharacterData getCharacter(int userId) {
        try {
            SQLiteDatabase db = getReadableDatabase();
            try (Cursor cursor = db.query(TABLE_CHARS, null,
                    COL_C_USER_ID + "=?",
                    new String[]{String.valueOf(userId)},
                    null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    CharacterData c = new CharacterData();
                    int idxUserId = cursor.getColumnIndex(COL_C_USER_ID);
                    int idxCharName = cursor.getColumnIndex(COL_C_CHAR_NAME);
                    int idxJobClass = cursor.getColumnIndex(COL_C_JOB_CLASS);
                    int idxLevel = cursor.getColumnIndex(COL_C_LEVEL);
                    int idxExp = cursor.getColumnIndex(COL_C_EXP);
                    int idxHp = cursor.getColumnIndex(COL_C_HP);
                    int idxMaxHp = cursor.getColumnIndex(COL_C_MAX_HP);
                    int idxAtk = cursor.getColumnIndex(COL_C_ATK);
                    int idxWeapon = cursor.getColumnIndex(COL_C_EQUIPPED_WEAPON);

                    c.userId = idxUserId != -1 ? cursor.getInt(idxUserId) : userId;
                    c.charName = idxCharName != -1 ? cursor.getString(idxCharName) : "";
                    c.jobClass = idxJobClass != -1 ? cursor.getString(idxJobClass) : "";
                    c.level = idxLevel != -1 ? cursor.getInt(idxLevel) : 1;
                    c.exp = idxExp != -1 ? cursor.getInt(idxExp) : 0;
                    c.hp = idxHp != -1 ? cursor.getInt(idxHp) : 100;
                    c.maxHp = idxMaxHp != -1 ? cursor.getInt(idxMaxHp) : 100;
                    c.atk = idxAtk != -1 ? cursor.getInt(idxAtk) : 10;
                    c.equippedWeapon = idxWeapon != -1 ? cursor.getString(idxWeapon) : "";
                    return c;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getCharacter error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Update all character stats. Uses user_id as the key (PK).
     */
    public boolean updateCharacter(CharacterData c) {
        if (c == null) return false;
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_C_CHAR_NAME, c.charName);
            cv.put(COL_C_JOB_CLASS, c.jobClass);
            cv.put(COL_C_LEVEL, c.level);
            cv.put(COL_C_EXP, c.exp);
            cv.put(COL_C_HP, c.hp);
            cv.put(COL_C_MAX_HP, c.maxHp);
            cv.put(COL_C_ATK, c.atk);
            cv.put(COL_C_EQUIPPED_WEAPON, c.equippedWeapon);
            int rows = db.update(TABLE_CHARS, cv,
                    COL_C_USER_ID + "=?",
                    new String[]{String.valueOf(c.userId)});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "updateCharacter error: " + e.getMessage());
            return false;
        }
    }

    // ─── Inventory / History ───────────────────────────────────────────────────

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Add an item to Inventory_History.
     * @return rowId of the new item, or -1 on failure.
     */
    public long addItem(int userId, String itemName, String rarity, String itemType) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_I_USER_ID, userId);
            cv.put(COL_I_ITEM_NAME, itemName);
            cv.put(COL_I_RARITY, rarity);
            cv.put(COL_I_ITEM_TYPE, itemType);
            cv.put(COL_I_IS_EQUIPPED, 0);
            cv.put(COL_I_OBTAINED_TIME, getCurrentTime());
            return db.insert(TABLE_INV, null, cv);
        } catch (Exception e) {
            Log.e(TAG, "addItem error: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Get inventory: all items.
     */
    public List<ItemData> getInventory(int userId) {
        List<ItemData> list = new ArrayList<>();
        try {
            SQLiteDatabase db = getReadableDatabase();
            try (Cursor cursor = db.query(TABLE_INV, null,
                    COL_I_USER_ID + "=?",
                    new String[]{String.valueOf(userId)},
                    null, null, COL_I_OBTAINED_TIME + " DESC")) {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        ItemData item = cursorToItem(cursor);
                        if (item != null) {
                            list.add(item);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getInventory error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get full history sorted newest first.
     */
    public List<ItemData> getHistory(int userId) {
        List<ItemData> list = new ArrayList<>();
        try {
            SQLiteDatabase db = getReadableDatabase();
            try (Cursor cursor = db.query(TABLE_INV, null,
                    COL_I_USER_ID + "=?",
                    new String[]{String.valueOf(userId)},
                    null, null, COL_I_OBTAINED_TIME + " DESC")) {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        ItemData item = cursorToItem(cursor);
                        if (item != null) {
                            list.add(item);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getHistory error: " + e.getMessage());
        }
        return list;
    }

    private ItemData cursorToItem(Cursor cursor) {
        if (cursor == null) return null;
        try {
            ItemData item = new ItemData();
            int idxId = cursor.getColumnIndex(COL_I_ID);
            int idxUserId = cursor.getColumnIndex(COL_I_USER_ID);
            int idxName = cursor.getColumnIndex(COL_I_ITEM_NAME);
            int idxRarity = cursor.getColumnIndex(COL_I_RARITY);
            int idxType = cursor.getColumnIndex(COL_I_ITEM_TYPE);
            int idxEquip = cursor.getColumnIndex(COL_I_IS_EQUIPPED);
            int idxTime = cursor.getColumnIndex(COL_I_OBTAINED_TIME);

            item.id = idxId != -1 ? cursor.getInt(idxId) : -1;
            item.userId = idxUserId != -1 ? cursor.getInt(idxUserId) : -1;
            item.itemName = idxName != -1 ? cursor.getString(idxName) : "";
            item.rarity = idxRarity != -1 ? cursor.getString(idxRarity) : "";
            item.itemType = idxType != -1 ? cursor.getString(idxType) : "";
            item.isEquipped = idxEquip != -1 && cursor.getInt(idxEquip) == 1;
            item.obtainedTime = idxTime != -1 ? cursor.getString(idxTime) : "";
            return item;
        } catch (Exception e) {
            Log.e(TAG, "cursorToItem error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Equip a WEAPON item.
     * Logic:
     *   1. Unequip all other WEAPON items for this user (is_equipped → 0).
     *   2. Set is_equipped = 1 for the target item.
     *   3. Update Characters.equipped_weapon with the item name.
     * @return true on success.
     */
    public boolean equipItem(int userId, int itemId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. Fetch item info to verify it's a WEAPON
            String itemName = null;
            String itemType = null;
            try (Cursor cursor = db.query(TABLE_INV, null,
                    COL_I_ID + "=? AND " + COL_I_USER_ID + "=?",
                    new String[]{String.valueOf(itemId), String.valueOf(userId)},
                    null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idxType = cursor.getColumnIndex(COL_I_ITEM_TYPE);
                    int idxName = cursor.getColumnIndex(COL_I_ITEM_NAME);
                    if (idxType != -1) itemType = cursor.getString(idxType);
                    if (idxName != -1) itemName = cursor.getString(idxName);
                }
            }
            if (!TYPE_WEAPON.equals(itemType)) return false;

            // 2. Unequip all weapons for this user
            ContentValues unequip = new ContentValues();
            unequip.put(COL_I_IS_EQUIPPED, 0);
            db.update(TABLE_INV, unequip,
                    COL_I_USER_ID + "=? AND " + COL_I_ITEM_TYPE + "=?",
                    new String[]{String.valueOf(userId), TYPE_WEAPON});

            // 3. Equip the target item
            ContentValues equip = new ContentValues();
            equip.put(COL_I_IS_EQUIPPED, 1);
            db.update(TABLE_INV, equip,
                    COL_I_ID + "=?",
                    new String[]{String.valueOf(itemId)});

            // 4. Update Characters.equipped_weapon
            ContentValues charCv = new ContentValues();
            charCv.put(COL_C_EQUIPPED_WEAPON, itemName != null ? itemName : "");
            db.update(TABLE_CHARS, charCv,
                    COL_C_USER_ID + "=?",
                    new String[]{String.valueOf(userId)});

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "equipItem failed: " + e.getMessage());
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Remove an item (e.g., after using a POTION).
     * @return true if deleted.
     */
    public boolean removeItem(int userId, int itemId) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            int rows = db.delete(TABLE_INV,
                    COL_I_ID + "=? AND " + COL_I_USER_ID + "=?",
                    new String[]{String.valueOf(itemId), String.valueOf(userId)});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "removeItem failed: " + e.getMessage());
            return false;
        }
    }
}
