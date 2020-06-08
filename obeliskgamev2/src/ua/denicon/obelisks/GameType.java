package ua.denicon.obelisks;

public enum GameType {
    DEFAULT("Default", "Обычный"),
    ELIMINATION("Elimination", "Выбывание");

    String name;
    String nameRus;

    GameType(String name, String nameRus) {
        this.name = name;
        this.nameRus = nameRus;
    }

    public static GameType getByName(String name) {
        for (GameType gt : GameType.values()) {
            if (gt.name.equalsIgnoreCase(name)) {
                return gt;
            }
        }
        return null;
    }

}
