package ua.denicon.obelisks;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;
import ua.denicon.obelisks.Map.MapConfig;
import ua.denicon.obelisks.Utils.ObeliskUtil;

import java.util.*;
import java.util.stream.Collectors;

public class Team {

    public org.bukkit.scoreboard.Team scoreboardTeam = null;
    public TeamColour colour = TeamColour.None;
    public Location spawnLocation = null;
    public Map<Integer, Obelisk> ownedObelisks = new HashMap<>();
    private List<Player> players = new ArrayList<>();
    public Map<String, Integer> rates = new HashMap<>();
    public Map<String, Integer> enchantments = new HashMap<>();
    public int capture = 1;

    public Team(TeamColour colour, Location spawnLocation, Scoreboard scoreboard, MapConfig mapConfig) {
        this.colour = colour;
        this.spawnLocation = spawnLocation;
        this.scoreboardTeam = scoreboard.getTeam("Obelisk:" + colour.getId());
        if (scoreboardTeam == null) {
            scoreboardTeam = scoreboard.registerNewTeam("Obelisk:" + colour.getId());
            scoreboardTeam.setPrefix(colour.chatColor + "");
            scoreboardTeam.setCanSeeFriendlyInvisibles(true);
            scoreboardTeam.setAllowFriendlyFire(false);
            scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        }
        rates.putAll(mapConfig.getDefaultRates());
    }

    @SuppressWarnings("deprecation")
    public void addPlayer(Player p) {
        if (Main.playerData.get(p).team != null) {
            Main.playerData.get(p).team.scoreboardTeam.removePlayer(p);
            Main.playerData.get(p).team.players.remove(p);
        }
        Main.playerData.get(p).team = this;
        players.add(p);
        scoreboardTeam.addPlayer(p);
        Main.updateTeams();
    }
    public void addPlayerWhithUpdate(Player p) {
        addPlayer(p);
        sendUpdateItems();
        Main.updateTeams();
    }

    public void addPlayers(Collection<? extends Player> players) {
        for (Player p : players) {
            addPlayer(p);
        }
        sendUpdateItems();
        Main.updateTeams();
    }

    @SuppressWarnings("deprecation")
    public void removePlayer(Player p) {
        removePlayerWithoutScoreboard(p);
        scoreboardTeam.removePlayer(p);
        Main.updateTeams();
    }

    public void removePlayerWithoutScoreboard(Player p) {
        players.remove(p);
        Main.playerData.get(p).team = null;
        Main.updateTeams();
    }

    @Override
    public String toString() {
        return "Team{" +
                "colour=" + colour +
                ", spawnLocation=" + spawnLocation +
                ", players=" + players +
                '}';
    }
    @SuppressWarnings("deprecation")
    public void sendUpdateObelisks() {
        System.out.println(Main.gameType);
        if (Main.gameType == GameType.ELIMINATION) {
            int i = 0;
            for (Obelisk o: ownedObelisks.values()) {
                if (o.isCaptured())
                    i++;
            }
            if (i <= 0) {
                for (Player p: players) {
                    p.sendTitle(ChatColor.DARK_RED + "У вaшей команды не осталость обелисков", "Вы проиграете если всех членов вашей команды убьют");
                }
            } else if (i == 1){
                for (Player p: players) {
                    if (Main.playerData.get(p).fullyDead) {
                        Main.playerData.get(p).fullyDead = false;
                        MainListener.onDeath(p, Main.playerData.get(p).team.spawnLocation, 6);
                    }
                }
            }
        } else {
            if (ownedObelisks.values().stream().filter(o -> o.isCaptured()).collect(Collectors.toList()).size() >= Main.obelisks.size()) {
                Main.endGame(colour.chatColor + colour.name);
                return;
            }
        }
        sendUpdateItems();
    }

    public void sendUpdateItems() {
        rates.putAll(Main.currentMap.getDefaultRates());
        enchantments.clear();
        for (int i = -1; i <= 4; i++) {
            for (Obelisk ob : ownedObelisks.values()) {
                if (ob.isCaptured()) {
                    if (ob.enchantment == null) {
                        //Bukkit.broadcastMessage(ob.toString());
                        if (ob.item != null && ob.item != "") {
                            if (ob.needRate == -2 && i == -1 && rates.get(ob.item) < 4) {
                                rates.put(ob.item, rates.get(ob.item) + 1);
                            } else if (ob.needRate == rates.get(ob.item))
                                rates.put(ob.item, ob.rate);
                        }
                    }
                }
            }
        }
        for (Obelisk ob : ownedObelisks.values()) {
            if (ob.isCaptured()) {
                if (ob.enchantment != null) {
                    String name = ob.enchantment.getName() + ":" + ob.item;
                    if (!enchantments.containsKey(name)) {
                        enchantments.put(name, 1);
                    } else {
                        enchantments.put(name, enchantments.get(name) + 1);
                    }
                }
            }
        }
        for (Map.Entry<String, Integer> rate : rates.entrySet()) {
            if (ObeliskUtil.isArmor(rate.getKey())) {
                String itemName = ArmorMaterial.getById(rate.getValue()).getName() + "_" + rate.getKey();
                Material material = Material.AIR;
                if (rate.getValue() >= 0)
                    material = Material.getMaterial(itemName);
                if (rate.getKey().equalsIgnoreCase("HELMET")) {
                    for (Player p : players) {
                        p.getInventory().setHelmet(new ItemStack(material));
                        if (material != Material.AIR) {
                            ItemMeta meta = p.getInventory().getHelmet().getItemMeta();
                            meta.setUnbreakable(true);
                            p.getInventory().getHelmet().setItemMeta(meta);
                        }
                        Main.playerData.get(p).getCurentClass().onItemGiven(p, p.getInventory().getHelmet(), rate);
                    }
                } else if (rate.getKey().equalsIgnoreCase("CHESTPLATE")) {
                    for (Player p : players) {
                        p.getInventory().setChestplate(new ItemStack(material));
                        if (material != Material.AIR) {
                            ItemMeta meta = p.getInventory().getChestplate().getItemMeta();
                            meta.setUnbreakable(true);
                            p.getInventory().getChestplate().setItemMeta(meta);
                        }
                        Main.playerData.get(p).getCurentClass().onItemGiven(p, p.getInventory().getChestplate(), rate);
                    }
                } else if (rate.getKey().equalsIgnoreCase("LEGGINGS")) {
                    for (Player p : players) {
                        p.getInventory().setLeggings(new ItemStack(material));
                        if (material != Material.AIR) {
                            ItemMeta meta = p.getInventory().getLeggings().getItemMeta();
                            meta.setUnbreakable(true);
                            p.getInventory().getLeggings().setItemMeta(meta);
                        }
                        Main.playerData.get(p).getCurentClass().onItemGiven(p, p.getInventory().getLeggings(), rate);
                    }
                } else if (rate.getKey().equalsIgnoreCase("BOOTS")) {
                    for (Player p : players) {
                        p.getInventory().setBoots(new ItemStack(material));
                        if (material != Material.AIR) {
                            ItemMeta meta = p.getInventory().getBoots().getItemMeta();
                            meta.setUnbreakable(true);
                            p.getInventory().getBoots().setItemMeta(meta);
                        }
                        Main.playerData.get(p).getCurentClass().onItemGiven(p, p.getInventory().getBoots(), rate);
                    }
                }
            } else if (rate.getKey().equalsIgnoreCase("SWORD")) {
                String itemName = ItemMaterial.getById(rate.getValue()).getName() + "_SWORD";
                Material material = Material.AIR;
                if (rate.getValue() >= 0)
                    material = Material.getMaterial(itemName);
                for (Player p : players) {
                    p.getInventory().setItem(0, new ItemStack(material));
                    if (material != Material.AIR) {
                        ItemMeta meta = p.getInventory().getItem(0).getItemMeta();
                        meta.setUnbreakable(true);
                        p.getInventory().getItem(0).setItemMeta(meta);
                    }
                    Main.playerData.get(p).getCurentClass().onItemGiven(p, p.getInventory().getItem(0), rate);
                }
            } else if (rate.getKey().equalsIgnoreCase("BOW")) {
                String itemName = "BOW";
                Material material = Material.AIR;
                if (rate.getValue() >= 0)
                    material = Material.getMaterial(itemName);
                for (Player p : players) {
                    if (material == Material.AIR) {
                        p.getInventory().clear(8);
                        Main.playerData.get(p).sheduling.remove(Material.ARROW);
                    } else Main.playerData.get(p).getCurentClass().sheduleArrow(p, false);
                    p.getInventory().setItem(1, new ItemStack(material));
                    if (material != Material.AIR) {
                        ItemMeta meta = p.getInventory().getItem(1).getItemMeta();
                        meta.setUnbreakable(true);
                        p.getInventory().getItem(1).setItemMeta(meta);
                    }
                    Main.playerData.get(p).getCurentClass().onItemGiven(p, p.getInventory().getItem(1), rate);
                }
            } else {
                for (Player p : players)
                    Main.playerData.get(p).getCurentClass().onItemGiven(p, rate);
            }
        }
        if (enchantments.size() > 0) {
            for (Map.Entry<String, Integer> ench : enchantments.entrySet()) {
                String[] keySplit = ench.getKey().split(":");
                String enchName = keySplit[0];
                String itemType = keySplit[1];
                if (ObeliskUtil.isArmor(itemType)) {
                    if (itemType.equalsIgnoreCase("HELMET")) {
                        for (Player p : players) {
                            if (p.getInventory().getHelmet().getType() != Material.AIR) {
                                ItemMeta meta = p.getInventory().getHelmet().getItemMeta();
                                meta.addEnchant(Enchantment.getByName(enchName), ench.getValue(), true);
                                p.getInventory().getHelmet().setItemMeta(meta);
                                p.getInventory().setHelmet(p.getInventory().getHelmet().clone());
                                Main.playerData.get(p).getCurentClass().onEnchGiven(p, Enchantment.getByName(enchName), ench.getValue(), p.getInventory().getHelmet());
                            }
                        }
                    } else if (itemType.equalsIgnoreCase("CHESTPLATE")) {
                        for (Player p : players) {
                            if (p.getInventory().getChestplate().getType() != Material.AIR) {
                                ItemMeta meta = p.getInventory().getChestplate().getItemMeta();
                                meta.addEnchant(Enchantment.getByName(enchName), ench.getValue(), true);
                                p.getInventory().getChestplate().setItemMeta(meta);
                                p.getInventory().setChestplate(p.getInventory().getChestplate().clone());
                                Main.playerData.get(p).getCurentClass().onEnchGiven(p, Enchantment.getByName(enchName), ench.getValue(), p.getInventory().getChestplate());
                            }
                        }
                    } else if (itemType.equalsIgnoreCase("LEGGINGS")) {
                        for (Player p : players) {
                            if (p.getInventory().getLeggings().getType() != Material.AIR) {
                                ItemMeta meta = p.getInventory().getLeggings().getItemMeta();
                                meta.addEnchant(Enchantment.getByName(enchName), ench.getValue(), true);
                                p.getInventory().getLeggings().setItemMeta(meta);
                                p.getInventory().setLeggings(p.getInventory().getLeggings().clone());
                                Main.playerData.get(p).getCurentClass().onEnchGiven(p, Enchantment.getByName(enchName), ench.getValue(), p.getInventory().getLeggings());
                            }
                        }
                    } else if (itemType.equalsIgnoreCase("BOOTS")) {
                        for (Player p : players) {
                            if (p.getInventory().getBoots().getType() != Material.AIR) {
                                ItemMeta meta = p.getInventory().getBoots().getItemMeta();
                                meta.addEnchant(Enchantment.getByName(enchName), ench.getValue(), true);
                                p.getInventory().getBoots().setItemMeta(meta);
                                p.getInventory().setBoots(p.getInventory().getBoots().clone());
                                Main.playerData.get(p).getCurentClass().onEnchGiven(p, Enchantment.getByName(enchName), ench.getValue(), p.getInventory().getBoots());
                            }
                        }
                    }
                } else if (itemType.equalsIgnoreCase("SWORD")) {
                    for (Player p : players) {
                        if (p.getInventory().getItem(0) != null)
                            if (p.getInventory().getItem(0).getType() != Material.AIR) {
                                ItemMeta meta = p.getInventory().getItem(0).getItemMeta();
                                meta.addEnchant(Enchantment.getByName(enchName), ench.getValue(), true);
                                p.getInventory().getItem(0).setItemMeta(meta);
                                p.getInventory().setItem(0, p.getInventory().getItem(0).clone());
                                Main.playerData.get(p).getCurentClass().onEnchGiven(p, Enchantment.getByName(enchName), ench.getValue(), p.getInventory().getItem(0));
                            }
                    }
                } else if (itemType.equalsIgnoreCase("BOW")) {
                    for (Player p : players) {
                        if (p.getInventory().getItem(1).getType() != Material.AIR) {
                            ItemMeta meta = p.getInventory().getItem(1).getItemMeta();
                            meta.addEnchant(Enchantment.getByName(enchName), ench.getValue(), true);
                            p.getInventory().getItem(1).setItemMeta(meta);
                            p.getInventory().setItem(1, p.getInventory().getItem(1).clone());
                            Main.playerData.get(p).getCurentClass().onEnchGiven(p, Enchantment.getByName(enchName), ench.getValue(), p.getInventory().getItem(1));
                        }
                    }
                }
            }
        }
    }

    public List<Player> getPlayers() {
        return players;
    }
}

