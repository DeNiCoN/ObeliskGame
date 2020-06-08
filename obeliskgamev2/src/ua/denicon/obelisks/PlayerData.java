package ua.denicon.obelisks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ua.denicon.obelisks.Classes.Class;
import ua.denicon.obelisks.Utils.ProgressBar;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class PlayerData {

    public boolean dead = false;
    public boolean fullyDead = false;
    public boolean shielded = false;
    public Player p = null;
    public Team team = null;
    Player lastDamager = null;
    List<Material> sheduling = new ArrayList<>();
    public List<Class> buyedClasses = new ArrayList<>();
    private Class currentClass = null;
    private int coins = 0;
    private int kills = 0;
    private int wins = 0;
    public Map<String, Object> classData = new HashMap<>();

    public PlayerData(Player p) {
        this.p = p;
        ResultSet res = null;
        try {
            res = Main.connection.createStatement().executeQuery("SELECT * FROM players WHERE name = '" + p.getName() + "';");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (!res.next()) {
                Main.connection.createStatement().execute("INSERT INTO players (name, currentclass, classes, coins, kills, wins) VALUES ('" + p.getName() + "', 'default', 'default', 3000, 0, 0);");
                res = Main.connection.createStatement().executeQuery("SELECT * FROM players WHERE name = '" + p.getName() + "';");
                res.next();
                System.out.println("New Player");
            }
            coins = res.getInt("coins");
            kills = res.getInt("kills");
            wins = res.getInt("wins");
            String classes = res.getString("classes");
            for (String clsString : classes.split(":")) {
                Class c = Main.classManager.getByName(clsString);
                if (c != null)
                    buyedClasses.add(Main.classManager.getByName(clsString));
            }
            setCurentClass(Main.classManager.getByName(res.getString("currentclass")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean sheduleItem(final ItemStack item, final int toAmmount, int periodInTicks, final int slot) {
        return this.sheduleItem(item, toAmmount, periodInTicks, slot, false);
    }

    public boolean sheduleItem(final ItemStack item, final int toAmmount, int periodInTicks, final int slot, boolean needToStart) {
        int from;
        if (p.getInventory().getItem(slot) != null && p.getInventory().getItem(slot).getType() == item.getType()) {
            from = p.getInventory().getItem(slot).getAmount();
        } else {
            from = 0;
        }
        if (!sheduling.contains(item.getType()) && from < toAmmount || needToStart && !sheduling.contains(item.getType())) {
            sheduling.add(item.getType());
            new ProgressBar(item.getType().name() + " ", periodInTicks, 100, p, ChatColor.GREEN, ChatColor.RED, () -> true);
            new BukkitRunnable() {
                int from;
                @Override
                public void run() {
                    if (!p.isOnline()) {
                        this.cancel();
                        return;
                    }
                    if (!sheduling.contains(item.getType())) {
                        this.cancel();
                        return;
                    }
                    if (p.getInventory().getItem(slot) != null && p.getInventory().getItem(slot).getType() == item.getType()) {
                        from = p.getInventory().getItem(slot).getAmount();
                    } else {
                        from = 0;
                    }
                    if (from >= toAmmount) {
                        sheduling.remove(item.getType());
                        this.cancel();
                        return;
                    }
                    item.setAmount(from + 1);
                    p.getInventory().setItem(slot, item);
                    if (from + 1 >= toAmmount) {
                        sheduling.remove(item.getType());
                        this.cancel();
                        return;
                    }
                    new ProgressBar(item.getType().name() + " ", periodInTicks, 100, p, ChatColor.GREEN, ChatColor.RED, () -> true);
                }
            }.runTaskTimer(Main.getInstance(), periodInTicks, periodInTicks);
            return true;
        }
        return false;
    }

    public void save() {
        StringBuilder clsString = new StringBuilder();
        for (Class cls : buyedClasses) {
            if (clsString.toString().equalsIgnoreCase("")) {
                clsString.append(cls.getNameInSQL());
            } else {
                clsString.append(":").append(cls.getNameInSQL());
            }
        }
        try {
            Main.connection.createStatement().executeUpdate("UPDATE players SET coins=" + coins + ", currentClass = '" + currentClass.getNameInSQL() + "', kills= " + kills + ", classes='" + clsString + "', wins= " + wins + " WHERE name='" + p.getName() + "';");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Player getLastDamager() {
        return lastDamager;
    }

    public void setLastDamager(Player lastDamager) {
        this.lastDamager = lastDamager;
    }

    public List<Material> getSheduling() {
        return sheduling;
    }

    public void setSheduling(List<Material> sheduling) {
        this.sheduling = sheduling;
    }

    public boolean isShielded() {
        return shielded;
    }

    public void setShielded(boolean shielded) {
        this.shielded = shielded;
    }

    public List<Class> getBuyedClasses() {
        return buyedClasses;
    }

    public Class getCurentClass() {
        return currentClass;
    }

    public void setCurentClass(Class currentClass) {
        if (this.currentClass != null)
            currentClass.reset();
        classData.clear();
        this.currentClass = currentClass;
    }
}
