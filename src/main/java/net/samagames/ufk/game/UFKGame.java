package net.samagames.ufk.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.Status;
import net.samagames.survivalapi.SurvivalAPI;
import net.samagames.survivalapi.game.GameException;
import net.samagames.survivalapi.game.SurvivalPlayer;
import net.samagames.survivalapi.game.SurvivalTeam;
import net.samagames.survivalapi.game.WaitingBlock;
import net.samagames.survivalapi.game.types.SurvivalTeamGame;
import net.samagames.survivalapi.game.types.run.RunBasedTeamGame;
import net.samagames.survivalapi.game.types.team.SurvivalTeamSelector;
import net.samagames.tools.Titles;
import net.samagames.ufk.UltraFlagKeeper;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Created by Rigner for project UltraFlagKeeper.
 */
public class UFKGame extends RunBasedTeamGame<UFKGameLoop> implements Listener
{
    protected List<Flag> flags;
    protected boolean respawn;
    protected RespawnManager respawnManager;

    public UFKGame(UltraFlagKeeper plugin, int nb)
    {
        super(plugin, "ultraflagkeeper", "Run4Flag", "Cours, drapeau, cours !", "⚑", UFKGameLoop.class, nb);

        /** Reimplement team creation, to change order */
        this.teams.forEach(team -> SurvivalAPI.get().registerEvent(SurvivalAPI.EventType.WORLDLOADED, () -> team.getScoreboardTeam().unregister()));
        this.teams.clear();
        this.respawn = true;
        List<SurvivalTeam> temporaryTeams = new ArrayList<>();
        temporaryTeams.add(new UFKTeam(this, "Rouge", DyeColor.RED, ChatColor.RED));
        temporaryTeams.add(new UFKTeam(this, "Bleu Foncé", DyeColor.BLUE, ChatColor.DARK_BLUE));

        temporaryTeams.add(new UFKTeam(this, "Vert Foncé", DyeColor.GREEN, ChatColor.DARK_GREEN));
        temporaryTeams.add(new UFKTeam(this, "Jaune", DyeColor.YELLOW, ChatColor.YELLOW));

        temporaryTeams.add(new UFKTeam(this, "Orange", DyeColor.ORANGE, ChatColor.GOLD));
        temporaryTeams.add(new UFKTeam(this, "Bleu Clair", DyeColor.LIGHT_BLUE, ChatColor.BLUE));
        temporaryTeams.add(new UFKTeam(this, "Cyan", DyeColor.CYAN, ChatColor.AQUA));
        temporaryTeams.add(new UFKTeam(this, "Rose", DyeColor.PINK, ChatColor.LIGHT_PURPLE));

        temporaryTeams.add(new UFKTeam(this, "Violet", DyeColor.PURPLE, ChatColor.DARK_PURPLE));
        temporaryTeams.add(new UFKTeam(this, "Gris", DyeColor.GRAY, ChatColor.GRAY));
        temporaryTeams.add(new UFKTeam(this, "Noir", DyeColor.BLACK, ChatColor.BLACK));
        temporaryTeams.add(new UFKTeam(this, "Blanc", DyeColor.WHITE, ChatColor.WHITE));

        for (int i = 0; i < SamaGamesAPI.get().getGameManager().getGameProperties().getOption("teams", new JsonPrimitive(2)).getAsInt() && i <= temporaryTeams.size(); ++i)
            this.registerTeam(temporaryTeams.get(i));
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
        this.respawnManager = new RespawnManager(plugin);

        try
        {
            Field field1 = SurvivalTeamSelector.class.getDeclaredField("instance");
            field1.setAccessible(true);
            field1.set(null, null);

            UFKGuiSelectorTeam.setGame(this);

            Field field = SurvivalTeamGame.class.getDeclaredField("teamSelector");
            field.setAccessible(true);
            UFKTeamSelector teamSelector = new UFKTeamSelector(this);
            SurvivalTeamSelector selector = (SurvivalTeamSelector)field.get(this);
            field.set(this, teamSelector);
            this.plugin.getServer().getPluginManager().registerEvents(teamSelector, this.plugin);
            HandlerList.unregisterAll(selector);
        }
        catch (Exception e)
        {
            this.plugin.getLogger().log(Level.SEVERE, "Error initiating team selector", e);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void computeLocations()
    {
        this.flags = new ArrayList<>();
        Iterator<SurvivalTeam> iterator = this.teams.iterator();
        SamaGamesAPI.get().getGameManager().getGameProperties().getConfig("flags", new JsonArray()).getAsJsonArray().forEach(json ->
        {
            String[] split = json.getAsString().split(", ");
            Flag flag = new Flag((UltraFlagKeeper)this.plugin, new Location(this.plugin.getServer().getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3])), Byte.parseByte(split[4]));
            this.flags.add(flag);
            SurvivalTeam survivalTeam = iterator.next();
            ((UFKTeam)survivalTeam).setFlag(flag);
            flag.setTeam((UFKTeam)survivalTeam);
        });

        this.flags.forEach(flag -> this.spawns.add(flag.getLocation().clone()));
        this.spawns.forEach(spawn -> spawn.setY(150D));
        this.waitingBlocks.addAll(this.spawns.stream().map(WaitingBlock::new).collect(Collectors.toList()));

        this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, () -> this.flags.forEach(flag ->
        {
            Player player;
            if (flag.getWearer() == null || (player = this.plugin.getServer().getPlayer(flag.getWearer())) == null)
                return ;
            player.getWorld().spawnParticle(Particle.CRIT, player.getLocation().add(0D, 1D, 0D), 3, 0.3D, 1D, 0.3D, 0.1D);

        }), 1L, 1L);
    }

    public List<Flag> getFlags()
    {
        return this.flags;
    }

    @Override
    public void stumpPlayer(UUID playerUUID, boolean logout, boolean silent) throws GameException
    {
        if(logout && !this.getStatus().equals(Status.IN_GAME))
        {
            SurvivalTeam team = this.teams.getTeam(playerUUID);
            if(team != null)
                team.playerDied(playerUUID);
        }

        try
        {
            if (this.status == Status.IN_GAME)
            {
                if (!logout)
                {
                    final Player player = Bukkit.getPlayer(playerUUID);
                    MetadataValue lastDamager = player.hasMetadata("lastDamager") ? player.getMetadata("lastDamager").get(0) : null;
                    Player killer = null;

                    if (lastDamager != null)
                    {
                        if (lastDamager.value() instanceof Player)
                        {
                            killer = (Player) lastDamager.value();

                            if(killer == null)
                                killer = player.getKiller();

                            if (!killer.isOnline() || !this.gamePlayers.containsKey(player.getUniqueId()) || this.gamePlayers.get(player.getUniqueId()).isSpectator())
                                killer = null;
                        }
                        else if (player.hasMetadata("lastDamagerKeepingValue"))
                        {
                            killer = (Player) player.getMetadata("lastDamagerKeepingValue").get(0).value();

                            ((BukkitTask) player.getMetadata("lastDamagerKeeping").get(0).value()).cancel();
                            player.removeMetadata("lastDamagerKeeping", this.plugin);
                        }
                    }

                    if (killer != null)
                    {
                        if (killer.isOnline() && this.gamePlayers.containsKey(player.getUniqueId()) && !this.gamePlayers.get(player.getUniqueId()).isSpectator())
                        {
                            final Player finalKiller = killer;

                            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () ->
                            {
                                SurvivalPlayer gamePlayer = this.getPlayer(finalKiller.getUniqueId());
                                gamePlayer.addKill(player.getUniqueId());
                                gamePlayer.addCoins(5, "Meurtre de " + player.getName());

                                try
                                {
                                    SamaGamesAPI.get().getStatsManager().getPlayerStats(finalKiller.getUniqueId()).getUltraFlagKeeperStatistics().incrByKills(1);
                                }
                                catch (Exception ignored){}
                            });

                            killer.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 400, 1));
                        }

                        String message = this.getPlayer(player.getUniqueId()).getTeam().getChatColor() + player.getName() + ChatColor.YELLOW + " a été tué par " + this.getPlayer(killer.getUniqueId()).getTeam().getChatColor() + killer.getName();

                        this.coherenceMachine.getMessageManager().writeCustomMessage(message, true);
                    }
                    else
                    {
                        String message = this.getPlayer(player.getUniqueId()).getTeam().getChatColor() + player.getName();

                        message += " " + ChatColor.YELLOW;

                        switch (player.getLastDamageCause().getCause())
                        {
                            case FALL:
                            case FALLING_BLOCK:
                                message += "est mort de chute.";
                                break;

                            case FIRE:
                            case FIRE_TICK:
                                message += "a fini carbonisé.";
                                break;

                            case DROWNING:
                                message += "s'est noyé.";
                                break;

                            case LAVA:
                                message += "a essayé de nager dans la lave. Résultat peu concluant.";
                                break;

                            case SUFFOCATION:
                                message += "a essayé de se cacher dans un mur.";
                                break;

                            case BLOCK_EXPLOSION:
                            case ENTITY_EXPLOSION:
                                message += "a mangé un pétard. Allez savoir pourquoi.";
                                break;

                            case POISON:
                            case MAGIC:
                                message += "s'est confronté à meilleur sorcier que lui.";
                                break;

                            case LIGHTNING:
                                message += "s'est transformé en Pikachu !";
                                break;

                            default:
                                message += "est mort.";
                                break;
                        }


                        this.coherenceMachine.getMessageManager().writeCustomMessage(message, true);
                    }

                    try
                    {
                        SamaGamesAPI.get().getStatsManager().getPlayerStats(player.getUniqueId()).getUltraFlagKeeperStatistics().incrByDeaths(1);
                    }
                    catch (Exception ignored) {}

                    if (!this.respawn)
                    {
                        Titles.sendTitle(player, 0, 100, 5, ChatColor.RED + "✞", ChatColor.RED + "Vous êtes mort !");
                        player.setGameMode(GameMode.SPECTATOR);
                        player.setHealth(20.0D);
                    }
                    else
                    {
                        SurvivalPlayer survivalPlayer = this.getPlayer(playerUUID);
                        SurvivalTeam team = survivalPlayer.getTeam();
                        Location location = ((UFKTeam) team).getFlag().getLocation();

                        Flag flag = this.flags.stream().filter(f -> f.getWearer() != null && f.getWearer().equals(playerUUID)).findFirst().orElse(null);
                        if (flag != null)
                        {
                            this.coherenceMachine.getMessageManager().writeCustomMessage(ChatColor.YELLOW + "Le drapeau de l'équipe " + flag.getTeam().getChatColor() + flag.getTeam().getTeamName() + ChatColor.YELLOW + " est au sol.", true);
                            flag.drop(player.getLocation());
                            flag.setWearer(null);
                        }
                        Location spawn;
                        Random random = new Random();
                        int i = 0;
                        do
                        {
                            spawn = location.clone().add(random.nextDouble() % 4D, random.nextDouble() % 1D, random.nextDouble() % 4D);
                            i++;
                        }
                        while (i < 10 && (spawn.getBlock().getType() == Material.AIR || spawn.getBlock().getRelative(BlockFace.UP).getType() == Material.AIR));
                        final Location finalSpawn = spawn;
                        Titles.sendTitle(player, 0, 20, 5, ChatColor.RED + "✞", ChatColor.RED + "Vous êtes mort !");
                        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> player.teleport(finalSpawn), 1L);
                        player.setHealth(20.0D);
                        this.respawnManager.respawn(player);
                    }
                }
                if (!this.respawn)
                {
                    this.plugin.getLogger().info("Stumping player " + playerUUID.toString() + "...");
                    this.checkStump(playerUUID, silent);

                    this.plugin.getLogger().info("Removing player " + playerUUID.toString() + "...");
                    this.removeFromGame(playerUUID);

                    this.dump();
                }
            }
        }
        catch (NullPointerException | IllegalStateException ignored)
        {
            throw new GameException(ignored.getMessage());
        }
    }

    @Override
    public void teleportDeathMatch()
    {
        super.teleportDeathMatch();
        this.respawn = false;
        this.plugin.getServer().getWorlds().forEach(world -> world.setGameRuleValue("keepInventory", "false"));
        this.flags.forEach(flag -> flag.setWearer(null));
        this.flags.forEach(Flag::unDrop);
        this.flags.forEach(Flag::respawn);
        this.getInGamePlayers().values().forEach(survivalPlayer ->
        {
            Player player;
            if ((player = survivalPlayer.getPlayerIfOnline()) != null)
                player.addPotionEffect(PotionEffectType.INCREASE_DAMAGE.createEffect(Integer.MAX_VALUE, 1));
        });
    }

    @Override
    public void drawEndTemplate()
    {
        new UFKStatisticsTemplate().execute(this);
    }

    public UFKTeam getWinnerTeam()
    {
        List<SurvivalTeam> teams = new ArrayList<>(this.teams);
        Collections.sort(teams, ((o1, o2) -> ((UFKTeam)o1).getScore() - ((UFKTeam)o2).getScore()));
        if (teams.size() == 0 || (teams.size() > 1 && ((UFKTeam)teams.get(0)).getScore() == ((UFKTeam)teams.get(1)).getScore()))
            return null;
        return (UFKTeam)teams.get(0);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamage(EntityDamageEvent event)
    {
        if (!this.respawn && event.getCause() == EntityDamageEvent.DamageCause.FALL)
        {
            event.setCancelled(true);
            event.setDamage(0D);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BANNER)
            event.setCancelled(true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event)
    {
        if (this.hasPlayer(event.getEntity()) && !this.isSpectator(event.getEntity()))
            event.getDrops().add(new ItemStack(Material.COOKED_BEEF, 2));
    }
    //TODO Fix teleports
}
