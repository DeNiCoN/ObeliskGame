package ua.denicon.obelisks.Classes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;
import ua.denicon.obelisks.Main;
import ua.denicon.obelisks.Obelisk;
import ua.denicon.obelisks.Utils.ObeliskUtil;

public class Capturer extends Class {
    public Capturer() {
        super("Захватчик", 1500, "capturer", ObeliskUtil.createItem(Material.INK_SACK, 1, (short) 1, "Захватчик"));
        lore.add(ChatColor.WHITE + "Класс поддержки, имеет 5 сердец,");
        lore.add(ChatColor.WHITE + "но удвоенный захват и отхват обелисков, когда он НЕ захвачен.");
        lore.add(ChatColor.WHITE + "За захват обелиска получает 3 монет.");
        lore.add(ChatColor.WHITE + "Не может захватывать/отхватывать ЗАХВАЧЕНЫЕ обелиски");
    }

    @Override
    public void init(Player p) {
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(10);
        if (p.getInventory().contains(Material.BOW)) {
            sheduleArrow(p, false);
        }
    }

    @Override
    public int onCapturing(Player p, Obelisk obelisk) {
        if (!obelisk.isCaptured()) {
            return 200;
        }
        return 0;
    }

    @Override
    public void onCaptured(Player p, Obelisk obelisk) {
        if (obelisk.teamOwner == Main.getPlayerData(p).team) {
            p.sendMessage(ChatColor.GRAY + "За захват обелиска вам начислено " + ChatColor.GOLD + 5*Main.COIN_MULTIP + " монет");
            Main.playerData.get(p).setCoins(Main.playerData.get(p).getCoins() + 3*Main.COIN_MULTIP);
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }

    }
}
