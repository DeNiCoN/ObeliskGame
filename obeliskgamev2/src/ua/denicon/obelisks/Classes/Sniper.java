package ua.denicon.obelisks.Classes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import ua.denicon.obelisks.Main;
import ua.denicon.obelisks.Utils.ObeliskUtil;

public class Sniper extends Class {
    public Sniper() {
        super("Снайпер", 1000, "sniper", ObeliskUtil.createItem(Material.BOW, 1, (short) 0, "Снайпер"));
        lore.add(ChatColor.WHITE + "Имеет 7 сердец.");
        lore.add(ChatColor.WHITE + "Имеет 3 стрелы с перезарядкой 3 секунд каждая.");
        lore.add(ChatColor.WHITE + "Удвоенные монеты за убийства из лука");
    }

    public void sheduleArrow(Player p, boolean needToStart) {
        Main.playerData.get(p).sheduleItem(new ItemStack(Material.ARROW), 2, 100, 8, needToStart);
    }

    @Override
    public void init(Player p) {
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(14);
        if (p.getInventory().contains(Material.BOW)) {
            sheduleArrow(p, false);
        }
    }

    @Override
    public int countCoins(Player p, EntityDamageEvent.DamageCause cause) {
        if (cause == EntityDamageEvent.DamageCause.PROJECTILE) {
            return 6;
        }
        return 3;
    }
}
