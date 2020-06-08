package ua.denicon.obelisks.Classes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.denicon.obelisks.Main;
import ua.denicon.obelisks.Obelisk;
import java.util.*;
import ua.denicon.obelisks.Utils.ObeliskUtil;

public class Axeman extends Class  {
    public Axeman() {
        super("Лесник", 450, "axeman", ObeliskUtil.createItem(Material.IRON_AXE, 1, (short) 0, "Лесник"));
        lore.add(ChatColor.WHITE + "Имеет топор.");
        lore.add(ChatColor.WHITE + "За убийства получает удвоенные монеты");
    }


    @Override
    public String onItemGiven(Player p, ItemStack item, Map.Entry<String, Integer> rate) {
        if (item != null)
            if (item.getType() != Material.AIR)
                if (item.getType().name().endsWith("SWORD")) {
                    item.setType(Material.getMaterial(item.getType().name().split("_")[0] + "_" + "AXE"));
                    p.getInventory().setItem(0, p.getInventory().getItem(0).clone());
                }
        return null;
    }

    @Override
    public int countCoins(Player p, EntityDamageEvent.DamageCause cause) {
        return 6;
    }
}