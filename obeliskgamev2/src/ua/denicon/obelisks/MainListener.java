package ua.denicon.obelisks;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import ua.denicon.obelisks.Classes.Class;
import ua.denicon.obelisks.Classes.ClassManager;
import ua.denicon.obelisks.Map.MapConfig;
import ua.denicon.obelisks.Utils.ObeliskUtil;

import java.util.stream.Collectors;

public class MainListener implements Listener {

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        p.removePotionEffect(PotionEffectType.SATURATION);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 99999, 1));
        p.getInventory().clear();
        Main.playerData.put(p, new PlayerData(p));
        p.setScoreboard(Main.board);
        if (p.getGameMode() != GameMode.CREATIVE) {
            p.setGameMode(GameMode.ADVENTURE);
            p.teleport(p.getWorld().getSpawnLocation());
        }
        if (Main.gamePhase == GamePhase.InGame) {
            for (Team team : Main.teams.values()) {
                if (team.scoreboardTeam.getPlayers().contains(p)) {
                    team.addPlayerWhithUpdate(p);
                    Main.getPlayerData(p).getCurentClass().init(p);
                    onDeath(p, team.spawnLocation, 30);
                }
            }
            p.getInventory().setItem(9, new ItemStack(Material.GOLD_INGOT));
            ItemMeta meta1 = p.getInventory().getItem(9).getItemMeta();
            meta1.setDisplayName(ChatColor.WHITE + "Меню");
            p.getInventory().getItem(9).setItemMeta(meta1);
        } else {
            p.getInventory().setItem(0, new ItemStack(Material.SLIME_BALL));
            ItemMeta meta = p.getInventory().getItem(0).getItemMeta();
            meta.setDisplayName(ChatColor.WHITE + "В лобби голосования");
            p.getInventory().getItem(0).setItemMeta(meta);

            p.getInventory().setItem(8, new ItemStack(Material.GOLD_INGOT));
            ItemMeta meta1 = p.getInventory().getItem(8).getItemMeta();
            meta1.setDisplayName(ChatColor.WHITE + "Меню");
            p.getInventory().getItem(8).setItemMeta(meta1);
        }

    }
    @EventHandler
    public void disableFrameRotate(PlayerInteractEntityEvent event){
        if (event.getRightClicked().getType() == EntityType.ITEM_FRAME && !event.getPlayer().isOp()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void disableFrameRotate(EntityDamageByEntityEvent event){
        if (event.getEntityType() == EntityType.ITEM_FRAME && !event.getDamager().isOp()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDisconect(PlayerQuitEvent e) {
        if (Main.playerData.get(e.getPlayer()).team != null)
            Main.playerData.get(e.getPlayer()).team.removePlayerWithoutScoreboard(e.getPlayer());
        if (Main.voters.contains(e.getPlayer()))
            Main.voters.remove(e.getPlayer());
        if (Main.gamePhase == GamePhase.VaitingForStart && Main.voters.size() < Main.minPlayersToVote && Main.voters.contains(e.getPlayer()))
            Bukkit.broadcastMessage(Main.OBELISK_PREFIX + ChatColor.GRAY + "Нужно ещё " + (Main.minPlayersToVote - Main.voters.size()) + " человек для начала голосования");
        Main.playerData.get(e.getPlayer()).save();
        Main.playerData.remove(e.getPlayer());
    }

    @EventHandler
    public static void bowEvent (EntityShootBowEvent e) {
        if (e.getEntityType() == EntityType.PLAYER) {
            Player p = (Player) e.getEntity();
            Main.playerData.get(p).getCurentClass().onBow(e);
        }
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event){
        if (event.getEntity().getShooter() instanceof Player) {
            Main.playerData.get(event.getEntity().getShooter()).getCurentClass().projectileHit(event);
        }
        if(event.getEntity() instanceof Arrow){
            Arrow arrow = (Arrow) event.getEntity();
            arrow.remove();
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (!e.getPlayer().isOp() && e.getPlayer().getGameMode() == GameMode.ADVENTURE) {
            e.getPlayer().sendMessage(ChatColor.DARK_RED + "Изменять расположение предметов запрещено");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (Main.gamePhase == GamePhase.Voting) {
            if (e.getClickedInventory().getName().equalsIgnoreCase(ChatColor.WHITE + "Карты")) {
                e.setCancelled(true);
                if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR && e.getCurrentItem().hasItemMeta()) {
                    if (Main.voted.get(p) == Main.maps.get(e.getSlot() + 1)) {
                        p.sendMessage(ChatColor.DARK_RED + "Вы уже проголосовали за " + e.getCurrentItem().getItemMeta().getDisplayName());
                        return;
                    }
                    if (Main.voted.get(p) != null) {
                        Main.mapsVote.put(Main.voted.get(p), Main.mapsVote.get(Main.voted.get(p)) - 1);
                    }
                    Main.voted.put(p, Main.maps.get(e.getSlot() + 1));
                    Main.mapsVote.put(Main.voted.get(p), Main.mapsVote.get(Main.voted.get(p)) + 1);
                    p.sendMessage(ChatColor.GREEN + "Вы проголосовали за " + e.getCurrentItem().getItemMeta().getDisplayName());
                    updateMapInv();
                    return;
                }
                return;
            }
        } else if (Main.gamePhase == GamePhase.Starting || Main.gamePhase == GamePhase.Pause) {
            if (e.getClickedInventory().getName().equalsIgnoreCase(ChatColor.WHITE + "Команды")) {
                e.setCancelled(true);
                if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                    TeamColour colour = TeamColour.getById(e.getCurrentItem().getDurability());
                    int max = Main.teams.get(colour).getPlayers().size();
                    for (Team team: Main.teams.values()) {
                        if (max > team.getPlayers().size() && team.colour != colour) {
                            if ((max - team.getPlayers().size()) >= (Main.currentMap.getToStart() <= 4 ? 2:1)) {
                                p.sendMessage(ChatColor.DARK_RED + "Команда переполнена");
                                return;
                            }
                        }
                    }
                    if (Main.playerData.get(p).team != null) {
                        Main.playerData.get(p).team.removePlayer(p);
                    }
                    Main.teams.get(colour).addPlayer(p);
                    p.sendMessage(ChatColor.GREEN + "Вы были добавлены в команду " + colour.chatColor + colour.name);
                }
                return;
            }
        } else if (Main.gamePhase == GamePhase.InGame) {
            if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                if (e.getCurrentItem().getType() == Material.GOLD_INGOT && e.getCurrentItem().hasItemMeta()) {
                    if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.WHITE + "Меню")) {
                        Inventory inv = Bukkit.createInventory(null, 9, "Меню");
                        inv.setItem(0, ObeliskUtil.createItem(Material.STAINED_GLASS_PANE, 1, (short) 5, ChatColor.WHITE + "Классы"));
                        Class c = Main.playerData.get(p).getCurentClass();
                        inv.setItem(4, c.icon);
                        inv.setItem(8, ObeliskUtil.createItem(Material.GOLD_NUGGET, 1, (short) 0, ChatColor.WHITE + "Монеты: " + Main.playerData.get(p).getCoins()));
                        p.openInventory(inv);
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }
        if (e.getInventory().getTitle().equalsIgnoreCase("Меню")) {
            e.setCancelled(true);
            if (e.getCurrentItem().getType() != Material.AIR) {
                if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.WHITE + "Классы") || Main.playerData.get(p).getCurentClass().icon == e.getCurrentItem()) {
                    p.openInventory(Main.classManager.createClassesInventory(Main.playerData.get(p)));
                }
            }
            return;
        } else if (e.getInventory().getTitle().equalsIgnoreCase("Выбор класса")) {
            e.setCancelled(true);
            if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                if (!e.getCurrentItem().getItemMeta().hasEnchant(Enchantment.DURABILITY))
                    p.openInventory(ClassManager.drawClassInventory(Main.classManager.getByName(e.getCurrentItem().getItemMeta().getDisplayName()), Main.playerData.get(p)));
            }
            return;
        } else  {
            Class c =  Main.classManager.getByName(e.getInventory().getTitle());
            if (c != null) {
                e.setCancelled(true);
                if (e.getCurrentItem().getDurability() == (short) 5) {
                    if (Main.gamePhase == GamePhase.InGame) {
                        if (Main.getPlayerData(p).team.colour != TeamColour.None) {
                            Main.getPlayerData(p).getCurentClass().reset();
                            Main.playerData.get(p).setCurentClass(c);
                            p.getInventory().clear();
                            Main.playerData.get(p).team.sendUpdateItems();
                            Main.getPlayerData(p).getCurentClass().init(p);
                            if (!Main.getPlayerData(p).dead)
                                onDeath(p, Main.getPlayerData(p).team.spawnLocation, 20);

                            p.getInventory().setItem(9, new ItemStack(Material.GOLD_INGOT));
                            ItemMeta meta1 = p.getInventory().getItem(9).getItemMeta();
                            meta1.setDisplayName(ChatColor.WHITE + "Меню");
                            p.getInventory().getItem(9).setItemMeta(meta1);
                        }
                    } else
                        Main.playerData.get(p).setCurentClass(c);
                    p.closeInventory();
                } else if (e.getCurrentItem().getDurability() == (short) 4) {
                    if (Main.playerData.get(p).getCoins() >= c.cost) {
                        Main.playerData.get(p).setCoins(Main.playerData.get(p).getCoins() - c.cost);
                        Main.playerData.get(p).buyedClasses.add(c);
                        p.openInventory(ClassManager.drawClassInventory(c, Main.playerData.get(p)));
                    }
                } else if (e.getCurrentItem().getDurability() == (short) 14) {
                    Inventory inv = Bukkit.createInventory(null, 9, "Меню");
                    inv.setItem(0, ObeliskUtil.createItem(Material.STAINED_GLASS_PANE, 1, (short) 5, ChatColor.WHITE + "Классы"));
                    Class c1 = Main.playerData.get(p).getCurentClass();
                    inv.setItem(4, c1.icon);
                    inv.setItem(8, ObeliskUtil.createItem(Material.GOLD_NUGGET, 1, (short) 0, ChatColor.WHITE + "Монеты: " + Main.playerData.get(p).getCoins()));
                    p.openInventory(inv);
                }
                return;
            }
        }
        if (e.getWhoClicked().getGameMode() == GameMode.ADVENTURE && !e.getWhoClicked().isOp()) {
            e.getWhoClicked().sendMessage(ChatColor.DARK_RED + "Изменять расположение предметов во время игры запрещено");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (Main.gamePhase != GamePhase.InGame) {
            if (e.getItem() != null && e.getItem().hasItemMeta()) {
                if (e.getItem().getType() == Material.GOLD_INGOT && e.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.WHITE + "Меню") && e.getAction() != Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK) {
                    Inventory inv = Bukkit.createInventory(null, 9, "Меню");
                    inv.setItem(0, ObeliskUtil.createItem(Material.STAINED_GLASS_PANE, 1, (short) 5, ChatColor.WHITE + "Классы"));
                    Class c = Main.playerData.get(p).getCurentClass();
                    inv.setItem(4, c.icon);
                    inv.setItem(8, ObeliskUtil.createItem(Material.GOLD_NUGGET, 1, (short) 0, ChatColor.WHITE + "Монеты: " + Main.playerData.get(p).getCoins()));
                    p.openInventory(inv);
                } else if (e.getItem().getType() == Material.SLIME_BALL && e.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.WHITE + "В лобби голосования") && !Main.voters.contains(p) && e.getAction() != Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK) {
                    p.teleport(ObeliskConfig.getVotingLocationFromConfig(Main.mapsCfg));
                    Main.voters.add(p);
                    p.setGameMode(GameMode.ADVENTURE);
                    if (Main.gamePhase == GamePhase.VaitingForStart && Main.voters.size() >= Main.minPlayersToVote) {
                        Main.getInstance().startVoting();
                    } else if (Main.gamePhase == GamePhase.VaitingForStart && Main.voters.size() < Main.minPlayersToVote) {
                        Bukkit.broadcastMessage(Main.OBELISK_PREFIX + ChatColor.GRAY + "Нужно ещё " + (Main.minPlayersToVote - Main.voters.size()) + " человек для начала голосования");
                    }

                    if (Main.gamePhase == GamePhase.Pause && Main.voters.size() >= Main.currentMap.getToStart()) {
                        Main.getInstance().endVoting(Main.currentMap.getId());
                    }
                    if (Main.gamePhase == GamePhase.Voting) {
                        p.getInventory().setItem(0, new ItemStack(Material.MAGMA_CREAM));
                        ItemMeta meta = p.getInventory().getItem(0).getItemMeta();
                        meta.setDisplayName(ChatColor.WHITE + "Карты");
                        p.getInventory().getItem(0).setItemMeta(meta);
                    } else if (Main.gamePhase == GamePhase.Starting) {
                        p.getInventory().setItem(0, new ItemStack(Material.CONCRETE));
                        ItemMeta meta = p.getInventory().getItem(0).getItemMeta();
                        meta.setDisplayName(ChatColor.WHITE + "Команды");
                        p.getInventory().getItem(0).setItemMeta(meta);
                    }
                } else if (e.getItem().getType() == Material.MAGMA_CREAM && e.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.WHITE + "Карты") && e.getAction() != Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK) {
                    p.openInventory(Main.mapInv);
                } else if (e.getItem().getType() == Material.CONCRETE && e.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.WHITE + "Команды")) {
                    p.openInventory(Main.teamsInv);
                }
            }
        }
        if (Main.gamePhase == GamePhase.InGame && e.getItem() != null)
            Main.playerData.get(p).getCurentClass().onInteract(e);
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onDamage (EntityDamageEvent e) {
        if (e.getEntity().getType() == EntityType.PLAYER) {
            final Player p = (Player) e.getEntity();
            if (Main.gamePhase != GamePhase.InGame && Main.playerData.get(p).team == null) {
                e.setCancelled(true);
                return;
            }
            if (Main.playerData.get(p).isShielded()) {
                e.setCancelled(true);
                return;
            }
            if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                if(((EntityDamageByEntityEvent) e).getDamager().getType() == EntityType.PLAYER) {
                    Player damager = (Player) ((EntityDamageByEntityEvent) e).getDamager();
                    Main.playerData.get(damager).getCurentClass().onDamageDeal(damager);
                    Main.playerData.get(p).setLastDamager(damager);
                }
            } else if(e.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                Player damager;
                if (((Projectile) ((EntityDamageByEntityEvent) e).getDamager()).getShooter() instanceof Player) {
                    damager = (Player) ((Projectile) ((EntityDamageByEntityEvent) e).getDamager()).getShooter();
                    Main.playerData.get(p).setLastDamager(damager);
                }
            }
            Main.playerData.get(p).getCurentClass().onDamaged(p);

            if (e.getFinalDamage() >= p.getHealth()) {
                if (Main.gamePhase == GamePhase.InGame) {
                    Player damager = null;
                    if(e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                            || e.getCause() == EntityDamageEvent.DamageCause.FIRE
                            || e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK
                            || e.getCause() == EntityDamageEvent.DamageCause.FALL
                            || e.getCause() == EntityDamageEvent.DamageCause.PROJECTILE
                            || e.getCause() == EntityDamageEvent.DamageCause.LAVA
                            || e.getCause() == EntityDamageEvent.DamageCause.MELTING
                            || e.getCause() == EntityDamageEvent.DamageCause.WITHER
                            || e.getCause() == EntityDamageEvent.DamageCause.DROWNING
                            || e.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION
                            || e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                        damager = Main.playerData.get(p).getLastDamager();
                    }

                    if (damager != null) {
                        if (Main.playerData.get(p).team != null && Main.playerData.get(damager).team != null &&
                                Main.playerData.get(p).team.colour != TeamColour.None && Main.playerData.get(damager).team.colour != TeamColour.None) {
                            Main.playerData.get(damager).getCurentClass().onKill(damager, p, e.getCause());
                            Main.playerData.get(p).getCurentClass().onDied(p);
                            int coins = Main.playerData.get(damager).getCurentClass().countCoins(damager ,e.getCause());
                            damager.sendMessage(Main.OBELISK_PREFIX + ChatColor.GRAY + "Вы убили " + p.getName() + ChatColor.GRAY + "," + ChatColor.GOLD + " +" + coins*Main.COIN_MULTIP +" монет");
                            damager.playSound(damager.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                            Main.playerData.get(damager).setCoins(Main.playerData.get(damager).getCoins() + coins*Main.COIN_MULTIP);
                            Main.playerData.get(damager).setKills(Main.playerData.get(damager).getKills() + 1);
                            Bukkit.broadcastMessage(Main.OBELISK_PREFIX + ChatColor.GRAY + "Игрок " + Main.getPlayerData(p).team.colour + p.getDisplayName() + ChatColor.GRAY + " был убит игроком " + Main.getPlayerData(damager).team.colour + damager.getDisplayName());
                        }
                    }
                }
                final int timer1;
                final Location loc;
                if (Main.playerData.get(p).team != null && Main.playerData.get(p).team.colour != TeamColour.None) {
                    timer1 = 5 + Main.playerData.get(p).team.ownedObelisks.values().stream().filter(Obelisk::isCaptured).collect(Collectors.toList()).size();
                    loc = Main.playerData.get(p).team.spawnLocation;
                } else {
                    timer1 = 0;
                    loc = Bukkit.getWorld(Main.worldName).getSpawnLocation();
                }
                onDeath(p, loc, timer1);
                e.setCancelled(true);
            }
        }
    }

    public static void onDeath(final Player p, final Location loc, final int timer1) {
        p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        p.setGameMode(GameMode.SPECTATOR);
        p.teleport(loc);
        p.playSound(p.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, 1);
        Main.playerData.get(p).dead = true;
        Main.playerData.get(p).setLastDamager(null);
        if (Main.gameType == GameType.ELIMINATION && Main.playerData.get(p).team.ownedObelisks.values().stream().filter(Obelisk::isCaptured).collect(Collectors.toList()).size() <= 0) {
            Main.playerData.get(p).fullyDead = true;
            Main.winByElimination();
            return;
        }
        new BukkitRunnable() {
            int timer = timer1;
            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                if (!p.isOnline() || !Main.playerData.containsKey(p)) {
                    this.cancel();
                    return;
                }
                if (p.getGameMode() != GameMode.SPECTATOR) {
                    Main.playerData.get(p).setShielded(false);
                    Main.playerData.get(p).dead = false;
                    Main.playerData.get(p).fullyDead = false;
                    Main.playerData.get(p).getCurentClass().onRespawn(p);
                    this.cancel();
                    return;
                }

                if (timer == 0) {
                    p.teleport(loc);
                    p.setGameMode(GameMode.ADVENTURE);
                    p.setHealth(p.getMaxHealth());
                    timer--;
                    Main.playerData.get(p).dead = false;
                    Main.playerData.get(p).fullyDead = false;
                    Main.playerData.get(p).getCurentClass().onRespawn(p);
                    Main.playerData.get(p).setShielded(true);
                } else if (timer < 0) {
                    if (timer == -4) {
                        Main.playerData.get(p).setShielded(false);
                        this.cancel();
                    }
                    timer--;
                } else {
                    p.sendTitle(ChatColor.GREEN + "Вы будете возрождены церез " + timer + " секунд", "");
                    timer--;
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 20L);
    }

    public void updateMapInv() {
        for (MapConfig map : Main.mapsVote.keySet()) {
            if (Main.mapsVote.get(map) > 0) {
                map.getItemInMapInv().setType(Material.GLOWSTONE_DUST);
                map.getItemInMapInv().setAmount(Main.mapsVote.get(map));
            } else {
                map.getItemInMapInv().setType(Material.SULPHUR);
            }
        }
    }

}
