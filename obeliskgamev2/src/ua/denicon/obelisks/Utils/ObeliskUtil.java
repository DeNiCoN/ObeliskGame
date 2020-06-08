package ua.denicon.obelisks.Utils;

import net.minecraft.server.v1_12_R1.ChatMessageType;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import ua.denicon.obelisks.Main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ObeliskUtil {

    public static int rnd(int min, int max)
    {
        max -= min;
        return (int) (Math.random() * ++max) + min;
    }


    public static boolean locEquals(Location loc1, Location loc2) {
        return loc1.getBlockX() == loc2.getBlockX() &&
                loc1.getBlockY() == loc2.getBlockY() &&
                loc1.getBlockZ() == loc2.getBlockZ();
    }

    public static boolean isArmor(String itemName) {
        itemName = itemName.trim();
        if (itemName.endsWith("HELMET") ||
        itemName.endsWith("CHESTPLATE") ||
        itemName.endsWith("LEGGINGS") ||
        itemName.endsWith("BOOTS"))
            return true;
        return false;
    }

    public static void giveItemWithName() {

    }

    public static double heal(Location healLoc, double radius, double heal, Collection<? extends Player> players) {
        Location min = healLoc.clone().subtract(radius, radius, radius);
        Location max = healLoc.clone().add(radius, radius, radius);
        double amount = 0;
        for (Player p : players) {
            if (p.getGameMode() == GameMode.ADVENTURE) {
                boolean isOnThis = (p.getLocation().getX() >= min.getX() && p.getLocation().getY() >= min.getY() && p.getLocation().getZ() >= min.getZ() &&
                        p.getLocation().getX() <= max.getX() && p.getLocation().getY() <= max.getY() && p.getLocation().getZ() <= max.getZ());
                if (isOnThis) {
                    if (p.getHealth() + heal < p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()) {
                        amount += heal;
                        p.setHealth(p.getHealth() + heal);
                    } else {
                        amount += p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() -p.getHealth();
                        p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
                    }
                    p.sendMessage(ChatColor.GREEN + "Вы были исцелены");
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }
            }
        }
        return amount;
    }

    public static double damage(Location damageLoc, double radius, double damage, Entity damager, Collection<? extends Player> players, Collection<? extends Player> noDamage) {
        Location min = damageLoc.clone().subtract(radius, radius, radius);
        Location max = damageLoc.clone().add(radius, radius, radius);
        double amount = 0;
        for (Player p : players) {
            if (p.getGameMode() == GameMode.ADVENTURE) {
                boolean isOnThis = (p.getLocation().getX() >= min.getX() && p.getLocation().getY() >= min.getY() && p.getLocation().getZ() >= min.getZ() &&
                        p.getLocation().getX() <= max.getX() && p.getLocation().getY() <= max.getY() && p.getLocation().getZ() <= max.getZ());
                if (isOnThis) {
                    if (p.getHealth() + damage < p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()) {
                        amount += damage;
                        p.setHealth(p.getHealth() + damage);
                    } else {
                        amount += p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() -p.getHealth();
                        p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
                    }
                    p.sendMessage(ChatColor.GREEN + "Вы были исцелены");
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }
            }
        }
        return amount;
    }

    public static double heal(Location healLoc, double radius, double heal, Collection<? extends Player> players, Player player) {
        Location min = healLoc.clone().subtract(radius, radius, radius);
        Location max = healLoc.clone().add(radius, radius, radius);
        double amount = 0;
        for (Player p : players) {
            if (p.getGameMode() == GameMode.ADVENTURE) {
                boolean isOnThis = (p.getLocation().getX() >= min.getX() && p.getLocation().getY() >= min.getY() && p.getLocation().getZ() >= min.getZ() &&
                        p.getLocation().getX() <= max.getX() && p.getLocation().getY() <= max.getY() && p.getLocation().getZ() <= max.getZ());
                if (isOnThis) {
                    if (p.getHealth() + heal < p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()) {
                        if (p != player)
                            amount += heal;
                        p.setHealth(p.getHealth() + heal);
                    } else {
                        if (p != player)
                            amount += p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() -p.getHealth();
                        p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
                    }
                    if (p != player) {
                        p.sendMessage(ChatColor.GREEN + "Вы были исцелены");
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    }
                }
            }
        }
        return amount;
    }

    public static void sendActionBar(Player p, String msg) {
        if (!p.isOnline())
            return;
        String s = ChatColor.translateAlternateColorCodes('&', msg);
        IChatBaseComponent icbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + s + "\"}");
        PacketPlayOutChat bar = new PacketPlayOutChat(icbc, ChatMessageType.a((byte) 2));
        ((CraftPlayer)p).getHandle().playerConnection.sendPacket(bar);
    }

    public static void sendActionBar(Collection<? extends Player> players, String msg) {

        String s = ChatColor.translateAlternateColorCodes('&', msg);
        IChatBaseComponent icbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + s + "\"}");
        PacketPlayOutChat bar = new PacketPlayOutChat(icbc, ChatMessageType.a((byte) 2));
        for (Player p : players) {
            if (!p.isOnline())
                continue;
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(bar);
        }
    }

    public static float[] getColorArrayFromChatColor(ChatColor color) {
        switch (color) {
            case RED:
                return new float[] {1f, 0f, 0f, 1f, 1f, 1f};
            case AQUA:
                return new float[] {0.1f, 0.3f, 1f, 1f, 1f, 1f};
            case YELLOW:
                return new float[] {1f, 1f, 0f, 1f, 1f, 1f};
            case GREEN:
                return new float[] {0f, 1f, 0f, 1f, 1f, 1f};
            case LIGHT_PURPLE:
                return new float[] {1f, 0f, 0.5f, 1f, 1f, 1f};
            case DARK_PURPLE:
                return new float[] {0.5f, 0f, 1f, 1f, 1f, 1f};
            case BLUE:
                return new float[] {0f, 0f, 1f, 1f, 1f, 1f};
            case DARK_GREEN:
                return new float[] {0f, 0.3f, 0f, 1f, 1f, 1f};
            default:
                return new float[] {1f, 1f, 1f, 0f, 0f, 0f};
        }
    }

    public static ItemStack createItem(Material m, int amount, short damage, String name) {
        ItemStack item = new ItemStack(m, amount, damage);
        setItemName(item, name, null);
        return item;
    }

    public static ItemStack createItem(Material m, int amount, short damage, String name, List<String> lore) {
        ItemStack item = new ItemStack(m, amount, damage);
        setItemName(item, name, lore);
        return item;
    }

    public static ItemStack createItem(Material m, int amount, short damage, String name, String lore) {
        ItemStack item = new ItemStack(m, amount, damage);
        List<String> lore1 = new ArrayList<>();
        lore1.add(lore);
        setItemName(item, name, lore1);
        return item;
    }

    public static void setItemName(ItemStack item, String name, List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null)
            meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public static ItemStack setItemEnchant(ItemStack item, Enchantment enchantment) {
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(enchantment, 1, true);
        item.setItemMeta(meta);
        return item;
    }

}
