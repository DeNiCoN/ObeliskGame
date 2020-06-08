package ua.denicon.obelisks.Classes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ua.denicon.obelisks.Main;
import ua.denicon.obelisks.Obelisk;
import ua.denicon.obelisks.Utils.ObeliskUtil;

public class Defender extends Class {

    public Defender() {
        super("Защитник", 1000, "defender", ObeliskUtil.createItem(Material.INK_SACK, 1, (short) 6, "Защитник"));
        lore.add(ChatColor.WHITE + "Класс поддержки, имеет 7 сердец,");
        lore.add(ChatColor.WHITE + "но удвоенный захват и отхват обелисков, когда он ЗАХВАЧЕН.");
        lore.add(ChatColor.WHITE + "За захват обелиска получает 3 монет.");
        lore.add(ChatColor.WHITE + "Не может захватывать/отхватывать НЕЗАХВАЧЕНЫЕ обелиски");
    }

    @Override
    public void init(Player p) {
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(14);
        if (p.getInventory().contains(Material.BOW)) {
            sheduleArrow(p, false);
        }
    }

    @Override
    public int onCapturing(Player p, Obelisk obelisk) {
        if (obelisk.isCaptured()) {
            return 200;
        }
        return 0;
    }

    @Override
    public void onCaptured(Player p, Obelisk obelisk) {
        if (obelisk.teamOwner != Main.getPlayerData(p).team) {
            p.sendMessage(ChatColor.GRAY + "За отхват обелиска вам начислено " + ChatColor.GOLD + 5*Main.COIN_MULTIP + " монет");
            Main.playerData.get(p).setCoins(Main.playerData.get(p).getCoins() + 3*Main.COIN_MULTIP);
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }

    }

}
