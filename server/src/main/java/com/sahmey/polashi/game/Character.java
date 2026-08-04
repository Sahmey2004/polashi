package com.sahmey.polashi.game;

public enum Character {
    // Nawab Paksha
    SIRAJ_UD_DAULAH(Faction.NAWAB, "Nawab Siraj-ud-Daulah"),
    LUTFUNNISA_BEGUM(Faction.NAWAB, "Lutfunnisa Begum"),
    MIR_MODON(Faction.NAWAB, "Mir Modon"),
    MOHAN_LAL(Faction.NAWAB, "Mohan Lal"),
    ST_FRAIS(Faction.NAWAB, "St. Frais"),
    BENGALI_NOBLEMAN(Faction.NAWAB, "Bengali Nobleman"),

    // EIC Paksha
    MIR_JAFAR(Faction.EIC, "Mir Jafar"),
    GHOSETI_BEGUM(Faction.EIC, "Ghoseti Begum"),
    RAY_DURLABH(Faction.EIC, "Ray Durlabh"),
    OMICHAND(Faction.EIC, "Omichand");

    private final Faction faction;
    private final String displayName;

    Character(Faction faction, String displayName) {
        this.faction = faction;
        this.displayName = displayName;
    }

    public Faction getFaction() {
        return faction;
    }

    public String getDisplayName() {
        return displayName;
    }
}
