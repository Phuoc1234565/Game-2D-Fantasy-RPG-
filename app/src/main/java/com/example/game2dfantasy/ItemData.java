package com.example.game2dfantasy;

public class ItemData {
    public int id;
    public int userId;
    public String itemName;
    public String rarity;      // "Thuong" | "Hiem"
    public String itemType;    // "WEAPON" | "POTION" | "MATERIAL"
    public boolean isEquipped;
    public String obtainedTime;

    public ItemData() {}

    public ItemData(int id, int userId, String itemName, String rarity,
                    String itemType, boolean isEquipped, String obtainedTime) {
        this.id = id;
        this.userId = userId;
        this.itemName = itemName;
        this.rarity = rarity;
        this.itemType = itemType;
        this.isEquipped = isEquipped;
        this.obtainedTime = obtainedTime;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getItemName() { return itemName; }
    public String getRarity() { return rarity; }
    public String getItemType() { return itemType; }
    public boolean isEquipped() { return isEquipped; }
    public String getObtainedTime() { return obtainedTime; }
}
