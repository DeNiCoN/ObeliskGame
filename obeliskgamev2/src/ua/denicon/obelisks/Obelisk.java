package ua.denicon.obelisks;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import ua.denicon.obelisks.Utils.ObeliskUtil;

import java.math.BigDecimal;
import java.util.*;


public class Obelisk {
    public double captureLimit = 0;
    public double capturedCount = 0;
    public Team teamOwner = null;
    List<Player> containedPlayers = new ArrayList<>();
    public Location obeliskLocation = null;
    public int radius = 0;
    public String item = null;
    public Enchantment enchantment = null;
    public int id = -1;
    public int rate;
    public int needRate;
    public int priority = 0;
    public boolean IsCaptured = false;
    public double unCaptureCount = 0;
    public List<Location> capturedBlocks = new ArrayList<>();
    public boolean oldIncreasing = false;
    private int lastOTPC = 0;


    public Obelisk(Location obeliskLocation, int radius, String item, Enchantment enchantment, int id, int rate, int needRate, int priority) {
        this.obeliskLocation = obeliskLocation;
        this.radius = radius;
        this.item = item;
        this.enchantment = enchantment;
        this.id = id;
        this.rate = rate;
        this.needRate = needRate;
        this.priority = priority;
        this.captureLimit = Math.pow(radius*2 + 1, 2) * 2;
        this.unCaptureCount = captureLimit*0.5;
    }

    public boolean checkOnThis(Location loc) {
        Coords min = getMinCoords();
        Coords max = getMaxCoords();
        boolean isOnThis = (loc.getBlockX() >= min.x && loc.getBlockZ() >= min.y && loc.getBlockX() <= max.x && loc.getBlockZ() <= max.y);
        return isOnThis;

    }

    public Coords getMinCoords() {
        return new Coords(obeliskLocation.getBlockX() - radius, obeliskLocation.getBlockZ() - radius);
    }

    public Coords getMaxCoords() {
        return new Coords(obeliskLocation.getBlockX() + radius, obeliskLocation.getBlockZ() + radius);
    }

    @SuppressWarnings("deprecation")
    public void captureProgress(double oldCC, boolean increasing) {
        double cc = capturedCount;
        if (capturedCount % 2 != 0 && increasing != oldIncreasing && oldCC != captureLimit)
            cc = oldCC;
        //Bukkit.broadcastMessage(capturedCount + " / " + oldCC + " / " + unCaptureCount + " / " + captureLimit + " / " + capturedBlocks.size() + "|| " + id + " team: " + teamOwner.colour.name);
        //System.out.println(capturedCount + "/" + oldCC);
//        if (capturedCount != oldCC && capturedCount % 1 == 0) {
//            float[] ints;
//            ints = ObeliskUtil.getColorArrayFromChatColor(teamOwner.colour.chatColor);
//            Main.drawObeliskLine(obeliskLocation.clone().add(0.5, 0, 0.5), 10, captureLimit, capturedCount, ints[0], ints[1], ints[2], ints[3], ints[4], ints[5]);
//        }
        if (capturedCount > oldCC) {
            if (capturedCount >= captureLimit) {
                if (!IsCaptured) {
                    IsCaptured = true;
                    obeliskLocation.getBlock().setData((byte) teamOwner.colour.getId());
                    for (Player p : containedPlayers) {
                        Main.playerData.get(p).getCurentClass().onCaptured(p ,this);
                    }
                    teamOwner.sendUpdateObelisks();
                    Bukkit.broadcastMessage(Main.OBELISK_PREFIX + teamOwner.colour.chatColor + teamOwner.colour.name + ChatColor.GRAY + " захватили обелиск");
                    // TODO заполнен
                }
                return;
            }
            if (cc % 2 == 0 && cc != 0) {
                //Bukkit.broadcastMessage("Add");
                //System.out.println(capturedCount + "/" + captureLimit);
                Location loc = new Location(Bukkit.getWorld(Main.worldName) ,0, 0, 0);
                int counter = 0;
                while (counter < 1000) {
                    Coords min = getMinCoords();
                    Coords max = getMaxCoords();
                    loc = new Location(Bukkit.getWorld(Main.worldName), ObeliskUtil.rnd(min.x, max.x), obeliskLocation.getBlockY(), ObeliskUtil.rnd(min.y, max.y));
                    //System.out.println(loc.getBlock().getData() == teamOwner.colour.getId());
                    counter++;
                    if (loc.getBlock().getData() != teamOwner.colour.getId() && !ObeliskUtil.locEquals(loc, obeliskLocation))
                        break;
                }
                loc.getBlock().setData((byte) teamOwner.colour.getId());
                capturedBlocks.add(loc);
            }
        } else if (capturedCount < oldCC) {
            if (capturedCount <= unCaptureCount && IsCaptured) {
                IsCaptured = false;
                for (Player p : containedPlayers) {
                    Main.playerData.get(p).getCurentClass().onCaptured(p,this);
                }
                obeliskLocation.getBlock().setData((byte) 0);
                teamOwner.sendUpdateObelisks();
                // TODO белый
                return;
            }
            if (capturedCount <= 0) {
                teamOwner.ownedObelisks.remove(id);
                teamOwner = null;
                return;
            }
            if (cc % 2 == 0) {
                //Bukkit.broadcastMessage("Remove");
                if (capturedBlocks.size() > 0) {
                    int lastIndex = capturedBlocks.size() - 1;
                    Location loc = capturedBlocks.get(lastIndex);
                    loc.getBlock().setData((byte) 0);
                    capturedBlocks.remove(lastIndex);
                }
            }
        }
    }


    public void onTick() {
        containedPlayers.clear();
        Map<TeamColour, Integer> players = new HashMap<>();
         for (Player p : Bukkit.getOnlinePlayers()) {
            Team team = Main.playerData.get(p).team;
            if (team == null)
                continue;
            if (checkOnThis(p.getLocation()) && p.getGameMode() == GameMode.ADVENTURE) {
                //System.out.println((teamOwner != null) + " / " + isCaptured());
                containedPlayers.add(p);
                if (players.containsKey(team.colour)) {
                    players.put(team.colour, players.get(team.colour) + Main.playerData.get(p).getCurentClass().onCapturing(p,this));
                } else {
                    players.put(team.colour, Main.playerData.get(p).getCurentClass().onCapturing(p,this));
                }
            }
        }
        if (players.size() > 0) {
            if (teamOwner == null) {
                int maxCount = 0;
                TeamColour teamWithMC = TeamColour.None;
                for (Map.Entry<TeamColour, Integer> p : players.entrySet()) {
                    if (p.getValue() > maxCount) {
                        maxCount = p.getValue();
                        teamWithMC = p.getKey();
                    }
                }
                int otherCount = players.size() - maxCount;
                if (otherCount >= maxCount)
                    return;
                else if (teamWithMC != TeamColour.None && maxCount > 0) {
                    int addCount = maxCount - otherCount;
                    teamOwner = Main.teams.get(teamWithMC);
                    teamOwner.ownedObelisks.put(id, this);
                    BigDecimal add = new BigDecimal(0.1).multiply(new BigDecimal(addCount));
                    capturedCount = new BigDecimal(capturedCount).add(add).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
                }
            } else {
                int OTPC = 0;
                int ticks = 0;
                for (Map.Entry<TeamColour, Integer> pl : players.entrySet()) {
                    if (pl.getKey() == teamOwner.colour)
                        OTPC += pl.getValue() * Main.teams.get(pl.getKey()).capture;
                    else OTPC -= pl.getValue();
                }
                if (OTPC == 0)
                    return;
                OTPC += lastOTPC;
                boolean increasing = OTPC > 0;
                if (!increasing)
                    OTPC *= -1;
                ticks = OTPC/100;
                lastOTPC = OTPC%100;
                for (int i = 0; i < ticks; i++) {
                    double oldCC = capturedCount;
                    if (increasing) {
                        if (capturedCount < captureLimit)
                            capturedCount = new BigDecimal(capturedCount).add(new BigDecimal(0.1)).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
                        else return;
                    } else {
                        if (capturedCount > 0)
                            capturedCount = new BigDecimal(capturedCount).add(new BigDecimal(-0.1)).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
                        else return;
                    }
                    if (oldCC == capturedCount)
                        return;
                    captureProgress(oldCC, increasing);
                    oldIncreasing = increasing;
                }
            }
        }
    }



    public double getCaptureLimit() {
        return captureLimit;
    }

    public double getCapturedCount() {
        return capturedCount;
    }

    public Team getTeamOwner() {
        return teamOwner;
    }

    public Location getObeliskLocation() {
        return obeliskLocation;
    }

    public int getRadius() {
        return radius;
    }

    public String getItem() {
        return item;
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }

    public int getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isCaptured() {
        return IsCaptured;
    }

    public double getUnCaptureCount() {
        return unCaptureCount;
    }

    public List<Location> getCapturedBlocks() {
        return capturedBlocks;
    }

    @Override
    public String toString() {
        return "Obelisk{" +
                "captureLimit=" + captureLimit +
                ", capturedCount=" + capturedCount +
                ", teamOwner=" + teamOwner +
                ", obeliskLocation=" + obeliskLocation +
                ", radius=" + radius +
                ", item=" + item +
                ", id=" + id +
                ", rate=" + rate +
                ", needRate=" + needRate +
                ", IsCaptured=" + IsCaptured +
                ", unCaptureCount=" + unCaptureCount +
                ", capturedBlocks=" + capturedBlocks +
                '}';
    }

    public void instantlySetTeamOwner(Team team) {
        Location loc = new Location(Bukkit.getWorld(Main.worldName), 0, 0, 0);
        teamOwner = team;
        while (capturedBlocks.size() < captureLimit / 2 - 1) {
            System.out.println(capturedBlocks.size() + " / " + captureLimit / 2);
            while (true) {
                Coords min = getMinCoords();
                Coords max = getMaxCoords();
                loc = new Location(Bukkit.getWorld(Main.worldName), ObeliskUtil.rnd(min.x, max.x), obeliskLocation.getBlockY(), ObeliskUtil.rnd(min.y, max.y));
                //System.out.println(loc.getBlock().getData() == teamOwner.colour.getId());
                if (loc.getBlock().getData() != teamOwner.colour.getId() && !ObeliskUtil.locEquals(loc, obeliskLocation)) {
                    break;
                }
            }
            loc.getBlock().setData((byte) team.colour.getId());

            capturedBlocks.add(loc);
        }
        capturedCount = captureLimit;
        IsCaptured = true;
        team.ownedObelisks.put(id, this);
        obeliskLocation.getBlock().setData((byte) team.colour.getId());
        System.out.println("block end");
    }

}

enum ItemMaterial {
    WOOD(0, "WOOD"),
    GOLD(1, "GOLD"),
    STONE(2, "STONE"),
    IRON(3, "IRON"),
    DIAMOND(4, "DIAMOND"),
    NONE(-1, "NONE");

    public String name;
    public int id;

    ItemMaterial(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static ItemMaterial getById(int id) {
        for (ItemMaterial material : ItemMaterial.values()) {
            if (material.getId() == id) {
                return material;
            }
        }
        return NONE;
    }

}

enum ArmorMaterial {
    LEATHER(0, "LEATHER"),
    GOLD(1, "GOLD"),
    CHAINMAIL(2, "CHAINMAIL"),
    IRON(3, "IRON"),
    DIAMOND(4, "DIAMOND"),
    NONE(-1, "NONE");

    public int id;
    public String name;

    ArmorMaterial(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static ArmorMaterial getById(int id) {
        for (ArmorMaterial material : ArmorMaterial.values()) {
            if (material.getId() == id) {
                return material;
            }
        }
        return NONE;
    }
}

class Coords {
    int x;
    int y;
    public Coords(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
