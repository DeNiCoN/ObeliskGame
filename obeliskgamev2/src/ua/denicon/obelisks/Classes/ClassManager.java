package ua.denicon.obelisks.Classes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ua.denicon.obelisks.Main;
import ua.denicon.obelisks.PlayerData;
import ua.denicon.obelisks.Utils.ObeliskUtil;

import java.util.*;

public class ClassManager {

    public HashMap<ItemStack, Class> classes = new HashMap<>();

    public ClassManager(List<Class> classes) {
        for (Class c : classes) {
            this.classes.put(c.icon, c);
        }
    }

    public Inventory createClassesInventory(PlayerData data) {
        Inventory inventory = Bukkit.createInventory(null, 54, "Выбор класса");
        for (Class c : classes.values()) {
            List<String> lore = new ArrayList<>();
            lore.addAll(c.lore);
            lore.add("");
            lore.add(ChatColor.GRAY + "Цена: " + c.cost);
            if (!data.buyedClasses.contains(c)) {
                inventory.addItem(ObeliskUtil.createItem(Material.STAINED_GLASS_PANE, 1, (short) 14, c.name, lore));
            } else if (data.getCurentClass() == c) {
                inventory.addItem(ObeliskUtil.setItemEnchant(ObeliskUtil.createItem(c.icon.getType(), 1, c.icon.getDurability(), c.name, lore), Enchantment.DURABILITY));
            } else {
                inventory.addItem(ObeliskUtil.createItem(c.icon.getType(), 1, c.icon.getDurability(), c.name, lore));
            }
        }
        return inventory;
    }

    public Class getByName(String name) {
        for (Class c : classes.values()) {
            if (c.name.equalsIgnoreCase(name) || c.nameInSQL.equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    public boolean hasClass(String name) {
        for (Class c : classes.values()) {
            if (c.name.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public static Inventory drawClassInventory(Class c, PlayerData data) {
        Inventory inv = Bukkit.createInventory(null, 27, c.name);
        inv.setItem(4, ObeliskUtil.createItem(Material.GOLD_NUGGET, 1, (short) 0, ChatColor.WHITE + "Монеты: " + data.getCoins()));
        inv.setItem(13, ObeliskUtil.createItem(c.icon.getType(), 1, c.icon.getDurability(), c.name, c.lore));
        for (int j = 0; j < 3; j++)
            for (int i = 0; i < 9; i++) {
                if (i < 3) {
                    if (data.buyedClasses.contains(c))
                        inv.setItem(i + j*9, ObeliskUtil.createItem(Material.STAINED_GLASS_PANE, 1, (short) 5, ChatColor.WHITE + "Выбрать класс"));
                    else
                        inv.setItem(i + j*9, ObeliskUtil.createItem(Material.STAINED_GLASS_PANE, 1, (short) 4, ChatColor.WHITE + "Купить класс", ChatColor.WHITE + "Цена: " + c.cost));
                } else if (i > 5) {
                    inv.setItem(i + j*9, ObeliskUtil.createItem(Material.STAINED_GLASS_PANE, 1, (short) 14, ChatColor.WHITE + "Отмена"));
                }
        }
        return inv;
    }

}
