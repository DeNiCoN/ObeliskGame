package ua.denicon.obelisks;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;
import ua.denicon.obelisks.Classes.*;
import ua.denicon.obelisks.Classes.Class;
import ua.denicon.obelisks.Particle.ParticleEffect;
import ua.denicon.obelisks.Map.MapConfig;
import ua.denicon.obelisks.Utils.*;
import ua.denicon.obelisks.mysql.MySQL;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public class Main extends JavaPlugin {

    public static MapConfig currentMap = null;
    private static ConfigUtil mySQLCfg = null;
    static ConfigUtil mapsCfg = null;
    public static GamePhase gamePhase = GamePhase.VaitingForStart;
    public static GameType gameType = GameType.DEFAULT;
    private static Main plugin = null;
    public static Map<Player, PlayerData> playerData = new HashMap<>();
    public static Map<Integer, Obelisk> obelisks = new HashMap<>();
    public static Map<TeamColour, Team> teams = new HashMap<>();
    public static List<Class> classes = new ArrayList<>();
    public static List<Player> voters = new ArrayList<>();
    public static Map<Player, MapConfig> voted = new HashMap();
    public static String worldName = null;
    public static ObeliskConfig obeliskConfig = null;
    public ScoreboardManager manager = null;
    public static Scoreboard board = null;
    public static Map<Integer, MapConfig> maps = new HashMap<>();
    public static Map<MapConfig, Integer> mapsVote = new HashMap<>();
    public static Inventory teamsInv;
    public static Inventory mapInv;
    public static final int minPlayersToVote = 4;
    private static BukkitTask timer;
    public static ClassManager classManager = null;

    public static final int COIN_MULTIP = 1;

    public static List<String> tips = new ArrayList<>();

    public static final String OBELISK_PREFIX = ChatColor.GREEN + "ObelisksGame> ";

    public static MySQL mySQL = null;
    public static Connection connection = null;

    public static void main(String[] args) {

        for (final int BITCOIN_PRICE_IN_DOLLARS = 10000; BITCOIN_PRICE_IN_DOLLARS > 1 ; BITCOIN_PRICE_IN_DOLLARS--) {
            System.out.println(BITCOIN_PRICE_IN_DOLLARS);
        }
    }

    @Override
    public void onEnable() {
        plugin = this;

//    public void UpdateScoreBoard() {
//        board.getObjectives().forEach(o -> o.unregister());
//        Objective mainObjective = board.registerNewObjective("main", "dummy");
//        mainObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
//        mainObjective.setDisplayName("§6§lCrazyChase");
//        int i = 0;
//        mainObjective.getScore("").setScore(i--);
//        String timerInfo = "";
//        if (Phase != GamePhase.InGame && Phase != GamePhase.End) {
//            if(Phase == GamePhase.WaitingForPlayers)
//                timerInfo = "§aОжидание игроков";
//            else if(Phase == GamePhase.Starting)
//                timerInfo = "§7Игра начнётся через: §f" + Timer;
//            mainObjective.getScore(timerInfo).setScore(i--);
//    }
//        else if(Phase == GamePhase.End)
//    {
//        if(Players.size() == 1)
//            mainObjective.getScore("§7Победитель: §6" + Players.get(0).getName()).setScore(i--);
//    }
//
//        for(Player p : Players)
//            p.setScoreboard(board);
//}

        capturingScheduler();
        Bukkit.getServer().getPluginManager().registerEvents(new MainListener(), this);
        loadConfigs();
        mySQL = new MySQL(mySQLCfg.getConfig().getString("hostname"),
                mySQLCfg.getConfig().getString("port"),
                mySQLCfg.getConfig().getString("database"),
                mySQLCfg.getConfig().getString("user"),
                mySQLCfg.getConfig().getString("password"));
        try {
            connection = mySQL.openConnection();
            Statement statement;
            statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS `" + mySQLCfg.getConfig().getString("database") + "`.`players` (" +
                    "`name` VARCHAR(45) NOT NULL," +
                    "`currentclass` VARCHAR(45) NOT NULL," +
                    "`classes` VARCHAR(400) NOT NULL," +
                    "`coins` INT NOT NULL," +
                    "`kills` INT NOT NULL," +
                    "`wins` INT NOT NULL," +
                    "PRIMARY KEY (`name`)," +
                    "UNIQUE INDEX `name_UNIQUE` (`name` ASC))" +
                    "ENGINE = InnoDB");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        loadMaps();
        manager = Bukkit.getScoreboardManager();
        board = manager.getNewScoreboard();
        classes.add(new Default());
        classes.add(new Defender());
        classes.add(new Capturer());
        classes.add(new Sniper());
        classes.add(new Ghost());
        classes.add(new Priest());
        classes.add(new Axeman());
        classes.add(new Swapper());
        classes.add(new Snowman());
        classes.add(new Turret());
        classManager = new ClassManager(classes);
        for (Player p : Bukkit.getOnlinePlayers()) {
            Main.playerData.put(p, new PlayerData(p));
            p.setScoreboard(board);
        }

        tips.add(ChatColor.WHITE + "Для того что бы сменить класс во время игры, откройте инвентарь и нажмите на меню");
        tips.add(ChatColor.WHITE + "Стрелы появляются только тогда когда у вашей команды есть лук");
        tips.add(ChatColor.WHITE + "Для того что бы купить/выбрать класс нажмите на меню (золотой слиток)");
        tips.add(ChatColor.WHITE + "Подсказки появляются каждые 5 минут");
        tips.add(ChatColor.WHITE + "У обелиской есть свой trello: https://trello.com/b/ET6qIVLJ/obelisksgame");
        new BukkitRunnable() {

            @Override
            public void run() {
                if (Bukkit.getOnlinePlayers().size() > 0)
                    Bukkit.broadcastMessage(Main.OBELISK_PREFIX + ChatColor.BLUE + "Tip> " + tips.get(ObeliskUtil.rnd(0, tips.size() - 1)));
            }
        }.runTaskTimer(this, 0, 300*20);
        reload();
    }

    public void onDisable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Main.playerData.get(p).save();
        }
    }

    @SuppressWarnings("deprecation")
    public static void endGame(String team) {
        timer.cancel();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (playerData.get(p).team != null) {
                p.teleport(p.getWorld().getSpawnLocation());
                p.setGameMode(GameMode.ADVENTURE);
                playerData.get(p).team.removePlayer(p);
                p.getInventory().clear();
                getPlayerData(p).sheduling.clear();

            }
            if (team == null)
                p.sendTitle(ChatColor.GRAY + "Игра закончилась ничьей", "");
            else
                p.sendTitle(ChatColor.GRAY + "Команда " + team + ChatColor.GRAY + " победили", "");
        }
        reload();
    }

    public static void winByElimination() {
        List<Team> ts = Main.teams.values().stream().filter(t -> t.getPlayers().stream().filter(p -> !Main.playerData.get(p).fullyDead).collect(Collectors.toList()).size() > 0).collect(Collectors.toList());
        if (ts.size() <= 1) {
            String endGameMessage = null;
            if (ts.size() == 1) {
                endGameMessage = ts.get(0).colour.chatColor + ts.get(0).colour.name;
                //TODO награды и тд
            }
            Main.endGame(endGameMessage);
        }
    }

    public static void reload() {
        teams.clear();
        obelisks.clear();
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.getInventory().setItem(0, new ItemStack(Material.SLIME_BALL));
            ItemMeta meta = p.getInventory().getItem(0).getItemMeta();
            meta.setDisplayName(ChatColor.WHITE + "В лобби голосования");
            p.getInventory().getItem(0).setItemMeta(meta);

            p.getInventory().setItem(8, new ItemStack(Material.GOLD_INGOT));
            ItemMeta meta1 = p.getInventory().getItem(8).getItemMeta();
            meta1.setDisplayName(ChatColor.WHITE + "Меню");
            p.getInventory().getItem(8).setItemMeta(meta1);
        }
        for (Team team : teams.values()) {
            for (Object p : team.getPlayers().toArray()) {
                team.removePlayer((Player) p);
            }
        }
        currentMap = null;
        gamePhase = GamePhase.VaitingForStart;
    }

    public void load() {
        mapInv = null;
        teamsInv = null;
        worldName = Main.currentMap.getCfg().getConfig().getString("serverWorldName");
        reloadAndSetObelisks();
        gamePhase = GamePhase.InGame;
        for (Player p : voters) {
            if (playerData.get(p).team == null) {
                int min = Integer.MAX_VALUE;
                Team minTeam = null;
                for (Team team : teams.values()) {
                    if (team.getPlayers().size() < min) {
                        min = team.getPlayers().size();
                        minTeam = team;
                    }
                }
                minTeam.addPlayer(p);
            }
        }
        for (Team team : teams.values()) {
            for (Player p : team.getPlayers()) {
                p.teleport(team.spawnLocation);
                p.getInventory().clear();

                p.getInventory().setItem(9, new ItemStack(Material.GOLD_INGOT));
                ItemMeta meta1 = p.getInventory().getItem(9).getItemMeta();
                meta1.setDisplayName(ChatColor.WHITE + "Меню");
                p.getInventory().getItem(9).setItemMeta(meta1);

                p.setGameMode(GameMode.ADVENTURE);
                for (PotionEffect effect : p.getActivePotionEffects())
                    p.removePotionEffect(effect.getType());
                p.removePotionEffect(PotionEffectType.SATURATION);
                p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 99999, 1));
                getPlayerData(p).getCurentClass().init(p);
            }
            team.sendUpdateItems();
        }
        voters.clear();
        timer = longTimerAction(0, currentMap.getTime(), () -> {
            Team maxTeam = null;
            int maxValue = 0;
            for (Team team : teams.values()) {
                if (team.getPlayers().size() != 0) {
                    List<Obelisk> obs = team.ownedObelisks.values().stream().filter(Obelisk::isCaptured).collect(Collectors.toList());
                    if (maxValue <= obs.size()) {
                        maxValue = obs.size();
                        maxTeam = team;
                    }
                }
            }
            if (maxTeam != null)
                endGame(maxTeam.colour.chatColor + maxTeam.colour.name);
            else
                endGame(null);
            return null;
        }, "Конец игры");
    }

    public void endVoting(int mapId) {
        mapsVote.clear();
        voted.clear();
        currentMap = maps.get(mapId);
        gameType = currentMap.getGameType();
        obeliskConfig = new ObeliskConfig(currentMap);
        teams = obeliskConfig.loadTeams();
        Bukkit.broadcastMessage(OBELISK_PREFIX + ChatColor.GRAY + "Следующая карта: " + currentMap.getName());
        teamsInv = Bukkit.createInventory(null, 9, ChatColor.WHITE + "Команды");
        int i = 0;
        for (Team team : teams.values()) {
            teamsInv.setItem(i, new ItemStack(Material.CONCRETE));
            ItemMeta meta = teamsInv.getItem(i).getItemMeta();
            meta.setDisplayName(team.colour.chatColor + team.colour.name);
            teamsInv.getItem(i).setItemMeta(meta);
            teamsInv.getItem(i).setDurability((short) team.colour.getId());
            i++;
        }
        for (Player p : voters) {
            p.getInventory().setItem(0, new ItemStack(Material.CONCRETE));
            ItemMeta meta = p.getInventory().getItem(0).getItemMeta();
            meta.setDisplayName(ChatColor.WHITE + "Команды");
            p.getInventory().getItem(0).setItemMeta(meta);

        }
        gamePhase = GamePhase.Starting;
        if (voters.size() < currentMap.getToStart()) {
            gamePhase = GamePhase.Pause;
            Bukkit.broadcastMessage(OBELISK_PREFIX + ChatColor.DARK_RED + "Недостаточно игроков для начала игры");
        } else {
            timerAction(0, 30, () -> {
                load();
                return null;
            }, "Начало игры");
        }

    }

    public void startVoting() {
        int i = 0;
        mapInv = Bukkit.createInventory(null, 54, ChatColor.WHITE + "Карты");
        for (MapConfig mapId : maps.values()) {
            mapInv.setItem(i, new ItemStack(Material.SULPHUR));
            mapId.setItemInMapInv(mapInv.getItem(i));
            ItemMeta meta = mapInv.getItem(i).getItemMeta();
            meta.setDisplayName(ChatColor.WHITE + mapId.getName());
            mapInv.getItem(i).setAmount(1);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.WHITE + (mapId.getTeams().size() + " команд, игроков для старта " + mapId.getToStart()));
            meta.setLore(lore);
            mapInv.getItem(i).setItemMeta(meta);
            i++;
        }

        for (MapConfig map : maps.values()) {
            mapsVote.put(map, 0);
        }

        for (Player p : voters) {
            p.getInventory().setItem(0, new ItemStack(Material.MAGMA_CREAM));
            ItemMeta meta = p.getInventory().getItem(0).getItemMeta();
            meta.setDisplayName(ChatColor.WHITE + "Карты");
            p.getInventory().getItem(0).setItemMeta(meta);
        }
        timerAction(0, 30, () -> {
            int max = 0;
            MapConfig maxMap = null;
            for (MapConfig map : mapsVote.keySet()) {
                if (max <= mapsVote.get(map) || maxMap == null) {
                    maxMap = map;
                    max = mapsVote.get(map);
                }
            }
            int id;
            if (max == 0)
                id = ObeliskUtil.rnd(1, maps.size());
            else
                id = maxMap.getId();
            endVoting(id);
            return null;
        }, "Конец голосования");
        gamePhase = GamePhase.Voting;
    }

    private void capturingScheduler() {
        new BukkitRunnable() {

            @Override
            public void run() {
                for (Obelisk obelisk : obelisks.values()) {
                    obelisk.onTick();
                }
            }
        }.runTaskTimer(plugin, 0, 1L);
    }

    public void loadMaps() {
        Set<String> nums = mapsCfg.getConfig().getConfigurationSection("maps").getKeys(false);
        for (String num : nums) {
            int parsed = Integer.parseInt(num);
            maps.put(parsed, new MapConfig(mapsCfg.getConfig().getString("maps." + parsed + ".name" ), new ConfigUtil(this, "obeliskData" + (parsed - 1) + ".yml"), parsed));
        }
    }

    private void loadConfigs() {
        new ConfigUtil(this, "obeliskData0.yml").saveDefaultConfig();
        mySQLCfg = new ConfigUtil(this, "MySQL.yml");
        mySQLCfg.saveDefaultConfig();
        mapsCfg = new ConfigUtil(this, "maps.yml");
        mapsCfg.saveDefaultConfig();
    }

    private void reloadAndSetObelisks() {
        obelisks = obeliskConfig.getObelisks();
        for (Obelisk obelisk : obelisks.values()) {
            Location loc = obelisk.getObeliskLocation();
            int radius = obelisk.getRadius();
            int y = (int) loc.getY();
            for (int x = (int) loc.getX() - radius; x < loc.getX() + radius + 1; x++){
                for (int z = (int) loc.getZ() - radius ; z < loc.getZ() + radius + 1; z++){
                    loc.getWorld().getBlockAt(x, y, z).setType(Material.CONCRETE);
                }
            }
            Block b = loc.clone().add(0, -1, 0).getBlock();
            b.setType(Material.BEACON);
            loc.getBlock().setType(Material.STAINED_GLASS);
            for (int x = (int) loc.getX() - 1; x < loc.getX() + 2; x++){
                for (int z = (int) loc.getZ() - 1 ; z < loc.getZ() + 2; z++){
                    loc.getWorld().getBlockAt(x, loc.getBlockY() - 2, z).setType(Material.IRON_BLOCK);
                }
            }
            if (obelisk.teamOwner != null)
                obelisk.instantlySetTeamOwner(obelisk.teamOwner);
        }
    }

    public static Main getInstance() {
        return plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getLabel().equalsIgnoreCase("team") && sender.isOp()) {
            try {
                if (args.length == 2) {
                    if (args[1].equalsIgnoreCase("all")) {
                        Main.teams.get(TeamColour.getById(Integer.parseInt(args[0]))).addPlayers(Bukkit.getOnlinePlayers());
                    } else {
                        Main.teams.get(TeamColour.getById(Integer.parseInt(args[0]))).addPlayerWhithUpdate(getServer().getPlayer(args[1]));
                    }
                } else {
                    Main.teams.get(TeamColour.getById(Integer.parseInt(args[0]))).addPlayerWhithUpdate((Player) sender);
                }
            } catch (CommandException e) {
                sender.sendMessage(ChatColor.DARK_RED + "Игрок или команда не существует");
            }
        } else if (command.getLabel().equalsIgnoreCase("spawnpar") && sender.isOp()) {
            Location playerLocation = ((Player) sender).getLocation();
            Location location = playerLocation.clone();
            new BukkitRunnable() {
                Vector vector;
                Vector vector1;
                @Override
                public void run() {
                    Location playerLocation = ((Player) sender).getLocation();
                    if (vector != null) {
                        vector.add(new Vector(playerLocation.getX() - location.getX(), playerLocation.getY() - location.getY() + 1, playerLocation.getZ() - location.getZ()));
                    } else {
                        vector = new Vector(playerLocation.getX() - location.getX(), playerLocation.getY() - location.getY() + 1, playerLocation.getZ() - location.getZ());
                    }
                    vector.multiply(0.1);
                    vector1 = vector.clone().multiply(2);
                    vector.add(vector1);
                    location.add(vector);
                    ParticleEffect.REDSTONE.display(0, 0, 0, 1, 0, location, 15);
                }

            }.runTaskTimer(plugin, 0, 1);
        } else if (command.getLabel().equalsIgnoreCase("faststart") && sender.isOp()) {
            if (Main.gamePhase == GamePhase.VaitingForStart) {
                startVoting();
            } else if (Main.gamePhase == GamePhase.Pause) {
                load();
            } else if (Main.gamePhase == GamePhase.InGame) {
                endGame("Досрочное завершение");
            }
        } else if (command.getLabel().equalsIgnoreCase("setobelisk") && sender.isOp()) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (args.length > 0) {
                    if (args[0].equalsIgnoreCase("help")) {
                        sender.sendMessage("/setobelisk <data id(from 0)> <obelisk id> <item> <enchant> <rate> <needRate> <radius> <team>");
                        sender.sendMessage("Don't forget to create obeliskData<data id(from 0)>.yml first");
                    } else {
                        ConfigUtil cfg = new ConfigUtil(plugin, "obeliskData" + args[0] + ".yml");
                        String obl = "obelisk." + args[1] + ".";
                        cfg.getConfig().set(obl + "item", args[2]);
                        cfg.getConfig().set(obl + "enchant", args[3]);
                        cfg.getConfig().set(obl + "rate", Integer.parseInt(args[4]));
                        cfg.getConfig().set(obl + "needRate", Integer.parseInt(args[5]));
                        cfg.getConfig().set(obl + "x", p.getLocation().getBlockX());
                        cfg.getConfig().set(obl + "y", p.getLocation().getBlockY());
                        cfg.getConfig().set(obl + "z", p.getLocation().getBlockZ());
                        cfg.getConfig().set(obl + "radius", Integer.parseInt(args[6]));
                        cfg.getConfig().set(obl + "team", Integer.parseInt(args[7]));
                        cfg.saveConfig();
                    }
                }
            }
        } else if (command.getLabel().equalsIgnoreCase("coins") && sender.isOp()) {
            PlayerData pd = getPlayerData(getServer().getPlayer(args[1]));
            pd.setCoins(pd.getCoins() + Integer.parseInt(args[0]));
        }
        return true;
    }

    public static void timerAction(int delay, int seconds, Action actionAfter, String actionName) {
        new ProgressBar("", seconds * 20, 100, Bukkit.getOnlinePlayers(), ChatColor.GREEN, ChatColor.RED, () -> true);
        new BukkitRunnable() {
            int counter = seconds;
            @Override
            public void run() {
                if (counter <= 0) {
                    actionAfter.invoke();
                    this.cancel();
                } else {
                    Bukkit.broadcastMessage(OBELISK_PREFIX + ChatColor.GRAY + actionName + " через " + counter + " секунд");
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 5, 1);
                    }
                    counter--;
                }
            }
        }.runTaskTimer(getInstance(), delay, 20L);
    }

    public static BukkitTask longTimerAction(int delay, int seconds, Action actionAfter, String actionName) {
        return new BukkitRunnable() {
            int counter = seconds;
            boolean tick = false;
            @Override
            public void run() {
                if (counter <= 0) {
                    actionAfter.invoke();
                    this.cancel();
                } else if (counter % 60 == 0) {
                    int m = counter / 60;
                    if (m <= 30 && m >= 5) {
                    Bukkit.broadcastMessage(OBELISK_PREFIX + ChatColor.GRAY + actionName + " через " + m + " минут");
                    } else if (m >= 2 && m <= 4) {
                    Bukkit.broadcastMessage(OBELISK_PREFIX + ChatColor.GRAY + actionName + " через " + m + " минуты");
                    } else if (m == 1) {
                    Bukkit.broadcastMessage(OBELISK_PREFIX + ChatColor.GRAY + actionName + " через " + m + " минута");
                    }
                    tick = true;
                } else if (counter <= 30 && counter >= 5) {
                    Bukkit.broadcastMessage(OBELISK_PREFIX + ChatColor.GRAY + actionName + " через " + counter + " секунд");
                    tick = true;
                } else if (counter >= 2 && counter <= 4) {
                    Bukkit.broadcastMessage(OBELISK_PREFIX + ChatColor.GRAY + actionName + " через " + counter + " секунды");
                    tick = true;
                } else if (counter == 1) {
                    Bukkit.broadcastMessage(OBELISK_PREFIX + ChatColor.GRAY + actionName + " через " + counter + " секунду");
                    tick = true;
                }
                if (tick) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 5, 1);
                    }
                    tick = false;
                }
                counter--;
            }
        }.runTaskTimer(getInstance(), delay, 20L);
    }

    public static void drawObeliskLine(Location loc, double maxHeight, double maxCC, double CC, float toR, float toG, float toB, float fromR, float fromG, float fromB) {
        double height = maxHeight * CC / maxCC;
        float r = (float) ((toR - fromR) * height / maxHeight + fromR);
        float g = (float) ((toG - fromG) * height / maxHeight + fromG);
        float b = (float) ((toB - fromB) * height / maxHeight + fromB);
        for (double i = 0.1; i < height; i += 0.1) {
            double x = loc.getX();
            double z = loc.getZ();
            double y = loc.getY() + i;
            double radius = 0.3;
            Location loc1 = new Location(loc.getWorld(), x += radius*Math.sin(i*2), y, z += radius*Math.cos(i*2));
            ParticleEffect.REDSTONE.display(r, g, b, 1, 0, loc1, 15);
        }
    }

    public static PlayerData getPlayerData(Player p) {
        return playerData.get(p);
    }

    public static void updateTeams() {
        Team min = null;
        for (Team team : teams.values()) {
            team.capture = 1;
            if (min == null || (min.getPlayers().size() < team.getPlayers().size() && team.getPlayers().size() > 0)) {
                min = team;
            }
        }
        for (Team team : teams.values()) {
            team.capture = min.getPlayers().size() / team.getPlayers().size();
        }
    }
}

enum GamePhase {
    VaitingForStart,
    Voting,
    Pause,
    Starting,
    InGame;
}
