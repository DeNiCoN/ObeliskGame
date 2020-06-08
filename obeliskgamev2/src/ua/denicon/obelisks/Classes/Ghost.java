package ua.denicon.obelisks.Classes;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ua.denicon.obelisks.Main;
import ua.denicon.obelisks.MainListener;
import ua.denicon.obelisks.Obelisk;
import ua.denicon.obelisks.Utils.ObeliskUtil;

import java.util.Map;
import java.util.stream.Collectors;

public class Ghost extends Class {
    public Ghost() {
        super("Призрак", 950, "ghost", ObeliskUtil.createItem(Material.SUGAR, 1, (short) 0, "Призрак"));
        lore.add(ChatColor.WHITE + "Имеет 6 сердец, не может носить броню.");
        lore.add(ChatColor.WHITE + "При нажатии ПКМ сахаром стаёт невидимым на 3");
        lore.add(ChatColor.WHITE + "секунды, при ударе невидимость пропадает.");
        lore.add(ChatColor.WHITE + "Перезарядка 10 секунд.");
        lore.add(ChatColor.WHITE + "За каждый захваченый обелиск");
        lore.add(ChatColor.WHITE + "Перезарядка и действие сахара");
        lore.add(ChatColor.WHITE + "увеличивается на 1/10 секунды");
    }

    private void sheduleAbility(Player p) {
        int toAdd = Main.playerData.get(p).team.ownedObelisks.values().stream().filter(Obelisk::isCaptured).collect(Collectors.toList()).size() * 2;
        Main.playerData.get(p).sheduleItem(new ItemStack(Material.SUGAR), 1, 200 + toAdd, 2);
    }

    @Override
    public void init(Player p) {
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(12);
        if (p.getInventory().contains(Material.BOW)) {
            sheduleArrow(p, false);
        }
        sheduleAbility(p);
    }

    @Override
    public String onItemGiven(Player p, ItemStack item, Map.Entry<String, Integer> rate) {
        if (item == null)
            return null;
        if (ObeliskUtil.isArmor(item.getType().name())) {
            if (item.getType().name().trim().endsWith("HELMET"))
                p.getInventory().setHelmet(null);
            else if (item.getType().name().trim().endsWith("CHESTPLATE"))
                p.getInventory().setChestplate(null);
            else if (item.getType().name().trim().endsWith("LEGGINGS"))
                p.getInventory().setLeggings(null);
            else if (item.getType().name().trim().endsWith("BOOTS"))
                p.getInventory().setBoots(null);
        } else if (item.getType().name().endsWith("SWORD")) {
            ObeliskUtil.setItemEnchant(item, Enchantment.DAMAGE_ALL);
            p.getInventory().setItem(0, p.getInventory().getItem(0).clone());
        }
        return null;
    }

    @Override
    public void onEnchGiven(Player p, Enchantment ench, int level, ItemStack item) {
        if (item != null)
            if (item.getType() != Material.AIR)
                if (item.getType().name().endsWith("SWORD")) {
                    if (ench == Enchantment.DAMAGE_ALL) {
                        ItemMeta meta = item.getItemMeta();
                        meta.removeEnchant(Enchantment.DAMAGE_ALL);
                        meta.addEnchant(Enchantment.DAMAGE_ALL, level + 1, false);
                        item.setItemMeta(meta);
                        p.getInventory().setItem(0, p.getInventory().getItem(0).clone());
                    }
                }
    }

    @Override
    public void onInteract(PlayerInteractEvent e) {
        if (e.getItem().getType().name().endsWith("SUGAR")) {
            Player p = e.getPlayer();
            if (p.getInventory().contains(Material.SUGAR)) {
                p.getInventory().remove(Material.SUGAR);
                int toAdd = Main.playerData.get(p).team.ownedObelisks.values().stream().filter(Obelisk::isCaptured).collect(Collectors.toList()).size() * 2;
                p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60 + toAdd, 1));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60 + toAdd, 1));
                sheduleAbility(p);
            }
        }
    }

    @Override
    public void onDamageDeal(Player p) {
        p.removePotionEffect(PotionEffectType.INVISIBILITY);
        p.removePotionEffect(PotionEffectType.SPEED);
    }

    @Override
    public void onDamaged(Player p) {
        p.removePotionEffect(PotionEffectType.INVISIBILITY);
        p.removePotionEffect(PotionEffectType.SPEED);
    }
}
