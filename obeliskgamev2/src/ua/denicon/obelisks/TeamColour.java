package ua.denicon.obelisks;

import org.bukkit.ChatColor;

public enum TeamColour {
    Cyan(3, ChatColor.AQUA, "Голубые"),
    Yellow(4, ChatColor.YELLOW, "Желтые"),
    Lime(5, ChatColor.GREEN, "Лаймовые"),
    Pink(6,ChatColor.LIGHT_PURPLE ,"Розовые"),
    Purple(10, ChatColor.DARK_PURPLE, "Фиолетовые"),
    Blue(11, ChatColor.BLUE, "Синие"),
    Green(13, ChatColor.DARK_GREEN, "Зелёные"),
    Red(14, ChatColor.RED, "Красные"),
    None(255, ChatColor.WHITE, "");

    public int id;
    public ChatColor chatColor;
    public String name;

    public int getId() {
        return id;
    }

    TeamColour(int id, ChatColor chatColor, String name) {
        this.id = id;
        this.chatColor = chatColor;
        this.name = name;
    }

    public static TeamColour getById(int id) {
        for (TeamColour col : TeamColour.values()) {
            if (col.getId() == id) {
                return col;
            }
        }
        return None;
    }
}
