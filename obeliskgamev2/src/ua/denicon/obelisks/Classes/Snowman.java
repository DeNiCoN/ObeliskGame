package ua.denicon.obelisks.Classes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import ua.denicon.obelisks.Main;
import ua.denicon.obelisks.Utils.ObeliskUtil;

public class Snowman extends Class {
    public Snowman() {
        super("Снеговик", 500, "snowman", ObeliskUtil.createItem(Material.SNOW_BALL, 1, (short) 0, "Снеговик"));
        lore.add(ChatColor.WHITE + "Вместо стрел пускает снежки");
        lore.add(ChatColor.WHITE + "Количество стрел - 10");
        lore.add(ChatColor.WHITE + "Перезарядка - 1 секунду");
    }

    @Override
    public void onBow(EntityShootBowEvent e) {
        super.onBow(e);
        e.setProjectile(e.getEntity().launchProjectile(Snowball.class));
    }

    @Override
    public void sheduleArrow(Player p, boolean needToStart) {
        Main.playerData.get(p).sheduleItem(new ItemStack(Material.ARROW), 5, 40, 8, needToStart);
    }
}
