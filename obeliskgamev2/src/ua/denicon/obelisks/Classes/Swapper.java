package ua.denicon.obelisks.Classes;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import ua.denicon.obelisks.Main;
import ua.denicon.obelisks.Utils.ObeliskUtil;

public class Swapper extends Class {
    public Swapper() {
        super("Заменщик", 750, "swapper", ObeliskUtil.createItem(Material.INK_SACK, 1, (short) 8, "Заменщик"));
        lore.add(ChatColor.WHITE + "Перезарядка стрелы 7 секунд.");
        lore.add(ChatColor.WHITE + "При попадании меняется местами.");
    }
    @Override
    public void projectileHit(ProjectileHitEvent e) {
        if (e.getHitEntity() != null && e.getHitEntity() instanceof Player) {
            Location loc = e.getHitEntity().getLocation();
            e.getHitEntity().teleport((Entity) e.getEntity().getShooter());
            ((Entity) e.getEntity().getShooter()).teleport(loc);
        }
    }

    @Override
    public void sheduleArrow(Player p, boolean needToStart) {
        Main.playerData.get(p).sheduleItem(new ItemStack(Material.ARROW), 1, 140, 8, needToStart);
    }
}
