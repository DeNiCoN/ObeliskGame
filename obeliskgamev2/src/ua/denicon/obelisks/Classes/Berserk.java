package ua.denicon.obelisks.Classes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ua.denicon.obelisks.Utils.ObeliskUtil;

import java.util.Map;

public class Berserk extends Class {
    public Berserk() {
        super("Берсерк", 750, "berserk", ObeliskUtil.createItem(Material.DIAMOND_AXE, 1, (short) 0, "Берсерк"));
        lore.add(ChatColor.WHITE + "Вместо меча топор.");
        lore.add(ChatColor.WHITE + "За каждое убийство топором");
        lore.add(ChatColor.WHITE + "получает +0.5 урона.");
        lore.add(ChatColor.WHITE + "Максимум +2 урона");
    }

    @Override
    public String onItemGiven(Player p, ItemStack item, Map.Entry<String, Integer> rate) {
        return super.onItemGiven(p, item, rate);
    }
}
