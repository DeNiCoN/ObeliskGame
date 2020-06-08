package ua.denicon.obelisks.Classes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ua.denicon.obelisks.Main;
import ua.denicon.obelisks.Utils.ObeliskUtil;

import java.util.Vector;

public class Turret extends Class {
    public Turret() {
        super("Турель", 1000, "turret", ObeliskUtil.createItem(Material.ARROW, 1, (short) 0, "Турель"));
        lore.add(ChatColor.WHITE + "Накапливает стрелы до 10 шт.");
        lore.add(ChatColor.WHITE + "Перезарядка одной стрелы 4 секунды.");
        lore.add(ChatColor.WHITE + "Если выстрелить с зажатым SHIFT");
        lore.add(ChatColor.WHITE + "поочерёдно выпускает все стрелы.");
    }

    @Override
    public void onBow(EntityShootBowEvent e) {
        super.onBow(e);
        Player p = (Player) e.getEntity();
        if (p.isSneaking()) {
            ItemStack itemStack = p.getInventory().getItem(8);
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                int timer1 = itemStack.getAmount() - 1;
                itemStack.setAmount(0);
                new BukkitRunnable() {
                    int timer = timer1;
                    org.bukkit.util.Vector vector = e.getProjectile().getVelocity().clone();
                    @Override
                    public void run() {
                        if (timer <= 0) {
                            this.cancel();
                            return;
                        }
                        p.launchProjectile(Arrow.class, vector);
                        timer--;
                    }
                }.runTaskTimer(Main.getInstance(), 2, 2);
            }
        }
    }

    @Override
    public void sheduleArrow(Player p, boolean needToStart) {
        Main.playerData.get(p).sheduleItem(new ItemStack(Material.ARROW), 10, 80, 8, needToStart);
    }
}
