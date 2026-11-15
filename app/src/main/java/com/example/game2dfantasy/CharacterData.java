package com.example.game2dfantasy;

public class CharacterData {
    public int userId;
    public String charName;
    public String jobClass;
    public int level;
    public int exp;
    public int hp;
    public int maxHp;
    public int atk;
    public String equippedWeapon;

    public CharacterData() {}

    public CharacterData(int userId, String charName, String jobClass,
                         int level, int exp, int hp, int maxHp,
                         int atk, String equippedWeapon) {
        this.userId = userId;
        this.charName = charName;
        this.jobClass = jobClass;
        this.level = level;
        this.exp = exp;
        this.hp = hp;
        this.maxHp = maxHp;
        this.atk = atk;
        this.equippedWeapon = equippedWeapon;
    }

    public int getUserId() { return userId; }
    public String getCharName() { return charName; }
    public String getJobClass() { return jobClass; }
    public int getLevel() { return level; }
    public int getExp() { return exp; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getAtk() { return atk; }
    public String getEquippedWeapon() { return equippedWeapon; }
}
