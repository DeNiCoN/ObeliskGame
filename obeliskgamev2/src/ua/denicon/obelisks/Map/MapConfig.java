package ua.denicon.obelisks.Map;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import ua.denicon.obelisks.GameType;
import ua.denicon.obelisks.Main;
import ua.denicon.obelisks.ObeliskConfig;
import ua.denicon.obelisks.TeamColour;
import ua.denicon.obelisks.Utils.ConfigUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapConfig {

    private GameType gameType;
    private String name;
    private ConfigUtil cfg;
    private int id;
    private Map<TeamColour, Location> teams = new HashMap<>();
    private int toStart;
    private int time;
    private Map<String, Integer> defaultRates = new HashMap<>();
    private ItemStack itemInMapInv = null;

    public MapConfig(String name, ConfigUtil cfg, int id) {
        this.name = name;
        this.cfg = cfg;
        this.id = id;
        Set<String> nums = cfg.getConfig().getConfigurationSection("teams").getKeys(false);
        for (String num : nums) {
            int parsed = Integer.parseInt(num);
            teams.put(TeamColour.getById(parsed), ObeliskConfig.getTeamLocationFromConfig(this.cfg, parsed));
        }
        gameType = GameType.getByName(cfg.getConfig().getString("gameType"));
        toStart = teams.size() < 5 ? 2 * teams.size() : teams.size();
        time = cfg.getConfig().getInt("timeInSec");
        defaultRates.put("SWORD", cfg.getConfig().getInt("defaultItems.swordRate"));
        defaultRates.put("BOW", cfg.getConfig().getInt("defaultItems.bowRate"));
        defaultRates.put("HELMET", cfg.getConfig().getInt("defaultItems.helmRate"));
        defaultRates.put("CHESTPLATE", cfg.getConfig().getInt("defaultItems.chestRate"));
        defaultRates.put("LEGGINGS", cfg.getConfig().getInt("defaultItems.legRate"));
        defaultRates.put("BOOTS", cfg.getConfig().getInt("defaultItems.bootRate"));
    }

    public int getToStart() {
        return toStart;
    }

    public String getName() {
        return name;
    }

    public ConfigUtil getCfg() {
        return cfg;
    }

    public int getId() {
        return id;
    }

    public Map<TeamColour, Location> getTeams() {
        return teams;
    }

    public Map<String, Integer> getDefaultRates() {
        return defaultRates;
    }

    public int getTime() {
        return time;
    }

    public ItemStack getItemInMapInv() {
        return itemInMapInv;
    }

    public void setItemInMapInv(ItemStack itemInMapInv) {
        this.itemInMapInv = itemInMapInv;
    }

    public GameType getGameType() {
        return gameType;
    }
}
