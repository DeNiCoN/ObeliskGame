package ua.denicon.obelisks.Classes;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ua.denicon.obelisks.Main;
import ua.denicon.obelisks.Obelisk;
import ua.denicon.obelisks.Particle.ParticleEffect;
import ua.denicon.obelisks.Utils.ObeliskUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Priest extends Class {

    public Priest() {
        super("Целитель", 1750, "priest", ObeliskUtil.createItem(Material.INK_SACK, 1, (short) 5, "Целитель"));
        lore.add(ChatColor.WHITE + "Может накапливать заряды. 1 в 2 секунды");
        lore.add(ChatColor.WHITE + "ПКМ топором исцеляет союзников");
        lore.add(ChatColor.WHITE + "в радиусе 2 блоков на 1 еденицу здоровья.");
        lore.add(ChatColor.WHITE + "Тратит 1 заряд. При зажатом SHIFT");
        lore.add(ChatColor.WHITE + "Тратит все заряды, но исцеляет:");
        lore.add(ChatColor.WHITE + "1 заряд = 1 еденица здоровья.");
        lore.add(ChatColor.WHITE + "При SHIFT + ЛКМ топором");
        lore.add(ChatColor.WHITE + "выбрасывает бомбу которая при попадании");
        lore.add(ChatColor.WHITE + "на пол восстанавливает 2 сердца.");
        lore.add(ChatColor.WHITE + "Тратит 5 зарядов.");
        lore.add(ChatColor.WHITE + "За каждую исцелённую еденицу");
        lore.add(ChatColor.WHITE + "здоровья получает 1 монету.");
    }

    private void sheduleAbility(Player p) {
        Main.playerData.get(p).sheduleItem(new ItemStack(Material.SULPHUR), 10, 40, 7);
    }

    @Override
    public void init(Player p) {
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        if (p.getInventory().contains(Material.BOW)) {
            sheduleArrow(p, false);
        }
        sheduleAbility(p);
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
    public void onInteract(PlayerInteractEvent e) {
        if (e.getItem().getType().name().endsWith("AXE")) {
            Player p = e.getPlayer();
            if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (p.isSneaking()) {
                    ItemStack sp = p.getInventory().getItem(7);
                    if (sp != null && sp.getType() != Material.AIR)
                        if (sp.getAmount() >= 5) {
                            sp.setAmount(sp.getAmount() - 5);
                            sendBomb(p);
                        }
                    sheduleAbility(p);
                }
            } else if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                ItemStack sp = p.getInventory().getItem(7);
                if (sp == null || sp.getType() == Material.AIR)
                    return;
                if (sp.getAmount() > 0) {
                    int amount = 1;
                    if (p.isSneaking()) {
                        amount = sp.getAmount();
                    }
                    int coins = (int) ObeliskUtil.heal(p.getLocation(), 2, amount, Main.getPlayerData(p).team.getPlayers(), p);
                    sp.setAmount(sp.getAmount() - amount);

                    p.sendMessage(ChatColor.GRAY + "За исцеление вам начислено " + ChatColor.GOLD + coins*Main.COIN_MULTIP + " монет");
                    Main.playerData.get(p).setCoins(Main.playerData.get(p).getCoins() + coins*Main.COIN_MULTIP);
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }
                sheduleAbility(p);
            }
        }
    }

    public void sendBomb(Player p) {
        ItemStack itemStack = new ItemStack(Material.EMERALD);
        Item item = p.getWorld().dropItemNaturally(p.getEyeLocation(), itemStack);
        item.setVelocity(p.getEyeLocation().getDirection().add(p.getVelocity()));
        item.setPickupDelay(100000);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!item.isDead()) {
                    if (!item.isOnGround()) {
                        ParticleEffect.VILLAGER_HAPPY.display(0, 0, 0, 0, 1, item.getLocation(), 15);
                        p.getWorld().playSound(item.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1, 3);
                    } else {
                        Location loc = item.getLocation();
                        ParticleEffect.VILLAGER_HAPPY.display(2, 2, 2, (float) 0, 60, item.getLocation(), 15);
                        int coins = (int) ObeliskUtil.heal(loc, 3, 4, Main.getPlayerData(p).team.getPlayers(), p);
                        p.sendMessage(Main.OBELISK_PREFIX + ChatColor.GRAY + "За исцеление вам начислено " + ChatColor.GOLD + coins*Main.COIN_MULTIP + " монет");
                        Main.playerData.get(p).setCoins(Main.playerData.get(p).getCoins() + coins*Main.COIN_MULTIP);
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                        item.remove();
                        this.cancel();
                    }
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 2);
    }
}
