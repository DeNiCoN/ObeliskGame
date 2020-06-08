package ua.denicon.obelisks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import ua.denicon.obelisks.Map.MapConfig;
import ua.denicon.obelisks.Utils.ConfigUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ObeliskConfig {
    MapConfig config = null;

    public ObeliskConfig(MapConfig config) {
        this.config = config;
    }

    public Map<Integer ,Obelisk> getObelisks() {
        Set<String> nums = config.getCfg().getConfig().getConfigurationSection("obelisk").getKeys(false);
        Map<Integer, Obelisk> obelisks = new HashMap<>();
        for (String num : nums) {
            int parsed = Integer.parseInt(num);
            obelisks.put(parsed, new Obelisk(getObeliskLocationFromConfig(parsed),
                    config.getCfg().getConfig().getInt("obelisk." + parsed + ".radius"),
                    config.getCfg().getConfig().getString("obelisk." + parsed + ".item"),
                    Enchantment.getByName(config.getCfg().getConfig().getString("obelisk." + parsed + ".enchant")),
                    parsed,
                    config.getCfg().getConfig().getInt("obelisk." + parsed + ".rate"),
                    config.getCfg().getConfig().getInt("obelisk." + parsed + ".needRate"),
                    config.getCfg().getConfig().getInt("obelisk." + parsed + ".priority")));
            TeamColour colour = TeamColour.getById(config.getCfg().getConfig().getInt("obelisk." + parsed + ".team"));
            if (colour != TeamColour.None) {
                obelisks.get(parsed).teamOwner = Main.teams.get(colour);
            }
        }
        return obelisks;
    }
    public Location getObeliskLocationFromConfig(int id) {
        return new Location(Bukkit.getWorld(Main.worldName), config.getCfg().getConfig().getInt("obelisk." + id + ".x"),
                config.getCfg().getConfig().getInt("obelisk." + id + ".y"),
                config.getCfg().getConfig().getInt("obelisk." + id + ".z"));
    }

    public static Location getTeamLocationFromConfig(ConfigUtil config, int id) {
        return new Location(Bukkit.getWorld(config.getConfig().getString("serverWorldName")), config.getConfig().getInt("teams." + id + ".x") + 0.5,
                config.getConfig().getInt("teams." + id + ".y") + 1,
                config.getConfig().getInt("teams." + id + ".z") + 0.5);
    }

    public static Location getVotingLocationFromConfig(ConfigUtil config) {
        return new Location(Bukkit.getWorld(config.getConfig().getString("waitingLobby.world")), (config.getConfig().getInt("waitingLobby.x") + 0.5),
                config.getConfig().getInt("waitingLobby.y") + 1,
                (config.getConfig().getInt("waitingLobby.z")) + 0.5);
    }

    public Map<TeamColour, Team> loadTeams() {
        Map<TeamColour, Team> teams = new HashMap<>();
        for (Map.Entry<TeamColour, Location> team : config.getTeams().entrySet()) {
            teams.put(team.getKey(), new Team(team.getKey(), team.getValue(), Main.board, config));
        }
        return teams;
    }
}
