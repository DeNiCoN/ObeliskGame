package ua.denicon.obelisks.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ua.denicon.obelisks.Main;

import java.util.*;

import static ua.denicon.obelisks.Utils.ObeliskUtil.sendActionBar;

public class ProgressBar extends BukkitRunnable {

    public List<Player> players = new ArrayList<>();
    public Collection<? extends Player> canSeePlayers = new ArrayList<>();
    private static HashMap<Player, List<ProgressBar>> bars = new HashMap<>();
    private int length;
    private int timer = 0;
    private int startTimer = 0;
    private ChatColor first, second;
    private String name = "";
    private Action<Boolean> condition;

    public ProgressBar(String name, int ticks, int length, Collection<? extends Player> players, ChatColor first, ChatColor second, Action<Boolean> condition) {
        this.name = name;
        this.condition = condition;
        this.first = first;
        this.second = second;
        this.length = length;
        timer = ticks;
        startTimer = timer;
        canSeePlayers = players;
        for (Player p : players) {
            if (!bars.containsKey(p))
                bars.put(p, new ArrayList<>());
            addProgressBarToPlayer(p);
        }
        start();
    }

    public ProgressBar(String name, int ticks, int length, Player p, ChatColor first, ChatColor second, Action<Boolean> condition) {
        this.name = name;
        this.condition = condition;
        this.first = first;
        this.second = second;
        this.length = length;
        timer = ticks;
        startTimer = timer;
        List<Player> players = new ArrayList<>();
        players.add(p);
        canSeePlayers = players;
        if (!bars.containsKey(p))
            bars.put(p, new ArrayList<>());
        addProgressBarToPlayer(p);
        start();
    }

    private void addProgressBarToPlayer(Player p) {
        List<ProgressBar> progressBars = bars.get(p);
        if (progressBars.size() > 0)
            progressBars.get(0).players.remove(p);
        progressBars.add(this);
        progressBars.sort(Comparator.comparingInt(ProgressBar::getTimer));
        if (progressBars.size() > 0)
            progressBars.get(0).players.add(p);
    }

    private void removeProgressFromToPlayer(Player p) {
        List<ProgressBar> progressBars = bars.get(p);
        if (progressBars.size() > 0)
            progressBars.get(0).players.remove(p);
        progressBars.remove(this);
        if (progressBars.size() > 0)
            progressBars.get(0).players.add(p);
    }

    public int getTimer() {
        return timer;
    }

    private void start() {
        runTaskTimer(Main.getInstance(), 0, 1);
    }

    private String getProgressBar(int procent) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(name);
        for (int i = 0; i < length; i++) {
            stringBuilder.append('|');
        }
        stringBuilder.replace(name.length() + procent, name.length() + procent, second + "|");
        stringBuilder.replace(name.length(),name.length(), first + "|");
        return stringBuilder.toString();
    }

    @Override
    public void run() {
        if (timer <= 0) {
            sendActionBar(players, "");
            for (Player p : canSeePlayers) {
                if (p.isOnline())
                    removeProgressFromToPlayer(p);
            }
            this.cancel();
            return;
        }
        if (!condition.invoke()) {
            for (Player p : canSeePlayers) {
                removeProgressFromToPlayer(p);
            }
        }
        String str = getProgressBar(timer * length / startTimer);
        timer--;
        sendActionBar(players, str);
    }
}
