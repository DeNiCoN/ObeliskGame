package ua.denicon.obelisks.Classes;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ua.denicon.obelisks.Main;
import ua.denicon.obelisks.Obelisk;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Class {
    String name;
    public int cost;
    String nameInSQL;
    public ItemStack icon;
    List<String> lore = new ArrayList<>();

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }

    public Class (String name, int cost, String nameInSQL, ItemStack icon) {
        this.name = name;
        this.cost = cost;
        this.nameInSQL = nameInSQL;
        this.icon = icon;
    }

    public void reset() {

    }

    public void init(Player p) {
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        if (p.getInventory().contains(Material.BOW)) {
            sheduleArrow(p, false);
        }
    }

    public void onKill(Player p, Player killed, EntityDamageEvent.DamageCause cause) {

    }
    public void onBow(EntityShootBowEvent e) {
        Main.playerData.get(e.getEntity()).getCurentClass().sheduleArrow((Player) e.getEntity(), true);
    }

    public String onItemGiven(Player p, ItemStack item, Map.Entry<String, Integer> rate) {
        return null;
    }

    public String onItemGiven(Player p, Map.Entry<String, Integer> rate) {
        return null;
    }

    public void onEnchGiven(Player p, Enchantment ench, int level, ItemStack item) {

    }

    public void onRespawn(Player p) {

    }

    public int onCapturing(Player p, Obelisk obelisk) {
        return 100;
    }

    public void onCaptured(Player p, Obelisk obelisk) {

    }

    public void projectileHit(ProjectileHitEvent e) {

    }

    public void sheduleArrow(Player p, boolean needToStart) {
        Main.playerData.get(p).sheduleItem(new ItemStack(Material.ARROW), 1, 60, 8, needToStart);
    }

    public String getNameInSQL() {
        return nameInSQL;
    }

    @Override
    public String toString() {
        return name;
    }

    public int countCoins(Player p, EntityDamageEvent.DamageCause cause) {
        return 3;
    }

    public void onInteract(PlayerInteractEvent e) {

    }

    public void onDamaged(Player p) {

    }

    public void onDamageDeal(Player p) {

    }

    public void onDied(Player p) {

    }
}
