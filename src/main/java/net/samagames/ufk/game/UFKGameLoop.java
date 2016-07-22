package net.samagames.ufk.game;

import net.samagames.api.SamaGamesAPI;
import net.samagames.survivalapi.game.SurvivalGame;
import net.samagames.survivalapi.game.SurvivalGameLoop;
import net.samagames.survivalapi.game.SurvivalPlayer;
import net.samagames.survivalapi.game.SurvivalTeam;
import net.samagames.survivalapi.game.types.SurvivalTeamGame;
import net.samagames.survivalapi.game.types.run.RunBasedGameLoop;
import net.samagames.survivalapi.utils.TimedEvent;
import net.samagames.tools.Titles;
import net.samagames.tools.chat.ActionBarAPI;
import net.samagames.tools.scoreboards.ObjectiveSign;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.UUID;

/**
 * Created by Rigner for project UltraFlagKeeper.
 */
public class UFKGameLoop extends RunBasedGameLoop implements Listener
{
    private boolean fallDamages;

    public UFKGameLoop(JavaPlugin plugin, Server server, SurvivalGame game)
    {
        super(plugin, server, game);

        this.fallDamages = false;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void createDamageEvent()
    {
        this.nextEvent = new TimedEvent(1, 0, "Dégats actifs", ChatColor.GREEN, false, () ->
        {
            this.game.getCoherenceMachine().getMessageManager().writeCustomMessage("Les dégats sont désormais actifs.", true);
            this.game.getCoherenceMachine().getMessageManager().writeCustomMessage("Créez votre équipement et protégez votre drapeau.", true);
            this.game.getCoherenceMachine().getMessageManager().writeCustomMessage("Vous serez téléporté dans votre base dans 14 minutes. Le PvP sera activé à ce moment là.", true);
            this.game.enableDamages();
            this.createTeleportationEvent();
            this.blocksProtected = false;
        });
    }

    @Override
    public void createTeleportationEvent()
    {
        this.nextEvent = new TimedEvent(9, 0, "Téléportation", ChatColor.YELLOW, true, () ->
        {
            SamaGamesAPI.get().getGameManager().setMaxReconnectTime(-1);
            this.game.disableDamages();
            this.game.teleport();

            for (Object o : this.game.getInGamePlayers().values())
            {
                SurvivalPlayer player = (SurvivalPlayer) o;
                if (player.isOnline())
                {
                    try
                    {
                        player.getPlayerIfOnline().removePotionEffect(PotionEffectType.SPEED);
                        player.getPlayerIfOnline().removePotionEffect(PotionEffectType.FAST_DIGGING);
                    }
                    catch (Exception var4)
                    {
                        var4.printStackTrace();
                    }
                }
            }

            this.game.setWorldBorderSize(249.0D);
            this.game.getCoherenceMachine().getMessageManager().writeCustomMessage("La map est désormais réduite. Les bordures sont en coordonnées " + ChatColor.RED + "-125 +125" + ChatColor.RESET + ".", true);
            this.game.getCoherenceMachine().getMessageManager().writeCustomMessage("Les dégats et le PvP seront activés dans 10 secondes !", true);
            this.createDeathmatchEvent();
        });
    }

    @Override
    public void createDeathmatchEvent()
    {
        this.nextEvent = new TimedEvent(0, 10, "PvP activé", ChatColor.RED, false, () ->
        {
            this.game.enableDamages();
            this.game.enablePVP();

            this.game.getCoherenceMachine().getMessageManager().writeCustomMessage("Les dégats et le PvP sont maintenant activés. Volez le drapeau de l'adversaire. Bonne chance !", true);

            this.createFinalDeathmatchEvent();
        });
    }

    public void createFinalDeathmatchEvent()
    {
        this.fallDamages = true;

        this.nextEvent = new TimedEvent(9, 30, "Fin de capture", ChatColor.YELLOW, false, () ->
        {
            UFKTeam team = ((UFKGame)this.game).getWinnerTeam();
            ((UFKGame)this.game).respawnManager.cancelAll();
            if (team == null)
            {
                ((UFKGame) this.game).teleportDeathMatch();
                this.game.getCoherenceMachine().getMessageManager().writeCustomMessage("Aucune équipe gagnante. Vous êtes donc téléportés pour un match à mort final.", true);

                this.createEndEvent();
            }
            else
                ((UFKGame)this.game).win(team);
        });
    }

    @Override
    public void createEndEvent()
    {
        super.createEndEvent();
        this.nextEvent = this.nextEvent.copy(3, 0);
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.getItem() != null && event.getItem().getType() == Material.POTION && Potion.fromItemStack(event.getItem()).getType() == PotionType.POISON && !this.game.isPvPActivated())
        {
            event.getPlayer().sendMessage(ChatColor.RED + "Vous ne pouvez pas utiliser cet objet hors du PvP.");

            event.setCancelled(true);
            event.getPlayer().updateInventory();
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event)
    {
        if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL && !this.fallDamages)
            event.setCancelled(true);
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event)
    {
        if (event.getRecipe().getResult().getType() == Material.DIAMOND_PICKAXE)
        {
            ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE, 1);
            pickaxe.addEnchantment(Enchantment.DIG_SPEED, 3);

            event.getInventory().setResult(pickaxe);

            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () ->
            {
                for (ItemStack stack : event.getWhoClicked().getInventory().getContents())
                    if (stack != null && stack.getType() == Material.DIAMOND_PICKAXE && !stack.containsEnchantment(Enchantment.DIG_SPEED))
                        stack.addEnchantment(Enchantment.DIG_SPEED, 3);
            }, 5L);
        }
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event)
    {
        if (event.getItem().getType() == Material.LEATHER_BOOTS || event.getItem().getType() == Material.IRON_BOOTS || event.getItem().getType() == Material.CHAINMAIL_BOOTS || event.getItem().getType() == Material.GOLD_BOOTS || event.getItem().getType() == Material.DIAMOND_BOOTS)
            event.getItem().addEnchantment(Enchantment.DEPTH_STRIDER, 2);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (event.getClickedInventory() != null && event.getCurrentItem() != null)
        {
            if (event.getCurrentItem().getType() == Material.LEATHER_BOOTS || event.getCurrentItem().getType() == Material.IRON_BOOTS || event.getCurrentItem().getType() == Material.CHAINMAIL_BOOTS || event.getCurrentItem().getType() == Material.GOLD_BOOTS || event.getCurrentItem().getType() == Material.DIAMOND_BOOTS)
            {
                if ((event.getClickedInventory().getType() == InventoryType.ENCHANTING || event.getClickedInventory().getType() == InventoryType.ANVIL) && event.getSlot() == 0)
                    event.getCurrentItem().removeEnchantment(Enchantment.DEPTH_STRIDER);
                else if (event.getClickedInventory().getType() == InventoryType.ANVIL && event.getSlot() == 2)
                    event.getCurrentItem().addEnchantment(Enchantment.DEPTH_STRIDER, 2);
            }
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event)
    {
        PotionEffect actual = null;

        for (PotionEffect potionEffect : event.getPotion().getEffects())
        {
            if (potionEffect.getType().getName().equals("POISON"))
            {
                actual = potionEffect;
                break;
            }
        }

        if (actual != null)
        {
            event.setCancelled(true);
            event.getAffectedEntities().forEach(entity -> entity.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 8 * 20, 0)));
        }
    }

    /**
     * Calculate the scoreboard and events decreasing
     *
     * Reimplement to change scoreboard content
     */
    @Override
    public void run()
    {
        this.seconds++;

        if (this.seconds >= 60)
        {
            this.minutes++;
            this.seconds = 0;

            if (this.episodeEnabled && this.minutes % 20 == 0)
            {
                this.game.getCoherenceMachine().getMessageManager().writeCustomMessage("Fin de l'épisode " + this.episode, true);
                this.episode++;
            }
        }

        for (UUID playerUUID : this.objectives.keySet())
        {
            ObjectiveSign objective = this.objectives.get(playerUUID);
            Player player = this.server.getPlayer(playerUUID);

            objective.clearScores();

            if (player == null)
            {
                this.objectives.remove(playerUUID);
            }
            else
            {
                objective.setLine(0, ChatColor.DARK_RED + "");
                objective.setLine(1, ChatColor.GRAY + "Joueurs : " + ChatColor.WHITE + this.game.getInGamePlayers().size());

                int lastLine = 1;

                if (this.game instanceof SurvivalTeamGame)
                {
                    objective.setLine(lastLine + 1, ChatColor.GRAY + "Équipes : " + ChatColor.WHITE + ((SurvivalTeamGame) this.game).countAliveTeam());
                    lastLine++;
                }

                objective.setLine(lastLine + 1, ChatColor.RED + "");
                lastLine++;

                if (this.nextEvent != null)
                    ActionBarAPI.sendMessage(player, this.nextEvent.getColor().toString() + this.nextEvent.getName() + " dans " + this.toString(this.nextEvent.getSeconds() == 0 ? this.nextEvent.getMinutes() - 1 : this.nextEvent.getMinutes(), this.nextEvent.getSeconds() == 0 ? 59 : this.nextEvent.getSeconds() - 1));

                SurvivalPlayer gamePlayer = (SurvivalPlayer) this.game.getPlayer(playerUUID);
                int kills = gamePlayer == null ? 0 : gamePlayer.getKills().size();

                objective.setLine(lastLine + 1, ChatColor.GRAY + "Joueurs tués : " + ChatColor.WHITE + kills);
                objective.setLine(lastLine + 2, ChatColor.AQUA + "");

                lastLine += 2;

                objective.setLine(lastLine++, ChatColor.DARK_PURPLE + "");

                if (this.game instanceof SurvivalTeamGame)
                    for (SurvivalTeam team : ((SurvivalTeamGame)this.game).getTeams())
                        if (!team.isDead())
                            objective.setLine(lastLine++, ChatColor.GRAY + "Équipe " + team.getTeamName() + ChatColor.GRAY + " : " + ChatColor.WHITE + ((UFKTeam)team).getScore());

                objective.setLine(lastLine++, ChatColor.BLUE + "");

                if (this.game instanceof SurvivalTeamGame)
                    for (SurvivalTeam team : ((SurvivalTeamGame)this.game).getTeams())
                        if (!team.isDead())
                            objective.setLine(lastLine++, ChatColor.GRAY + "Drapeau " + team.getTeamName() + ChatColor.GRAY + " : " + ChatColor.WHITE + SurvivalGameLoop.getDirection(player.getLocation(), ((UFKTeam)team).getFlag().getWearer() == null ? ((UFKTeam)team).getFlag().getArmorStands().isEmpty() ? ((UFKTeam)team).getFlag().getLocation() : ((UFKTeam)team).getFlag().getArmorStands().get(0).getLocation() : this.plugin.getServer().getPlayer(((UFKTeam)team).getFlag().getWearer()).getLocation()));

                objective.setLine(lastLine++, ChatColor.DARK_AQUA + "");

                objective.setLine(lastLine + 1, ChatColor.GRAY + "Bordure :");
                objective.setLine(lastLine + 2, ChatColor.WHITE + "-" + (int) this.world.getWorldBorder().getSize() / 2 + " +" + (int) this.world.getWorldBorder().getSize() / 2);
                objective.setLine(lastLine + 3, ChatColor.LIGHT_PURPLE + "");
                if (this.episodeEnabled)
                    objective.setLine(lastLine++ + 4, ChatColor.GRAY + "Episode : " + ChatColor.WHITE + this.episode);
                objective.setLine(lastLine + 4, ChatColor.GRAY + "Temps de jeu : " + ChatColor.WHITE + this.toString(this.minutes, this.seconds));

                objective.updateLines();

                this.server.getScheduler().runTaskAsynchronously(this.plugin, objective::updateLines);
            }
        }

        if (this.nextEvent != null)
        {
            if (this.nextEvent.getSeconds() == 0 && this.nextEvent.getMinutes() <= 3 && this.nextEvent.getMinutes() > 0 || this.nextEvent.getMinutes() == 0 && (this.nextEvent.getSeconds() <= 5 || this.nextEvent.getSeconds() == 10 || this.nextEvent.getSeconds() == 30))
            {
                this.game.getCoherenceMachine().getMessageManager().writeCustomMessage(ChatColor.YELLOW + this.nextEvent.getName() + ChatColor.YELLOW + " dans " + (this.nextEvent.getMinutes() != 0 ? this.nextEvent.getMinutes() + " minute" + (this.nextEvent.getMinutes() > 1 ? "s" : "") : this.nextEvent.getSeconds() + " seconde" + (this.nextEvent.getSeconds() > 1 ? "s" : "")) + ".", true);

                if (this.nextEvent.isTitle() && this.nextEvent.getSeconds() <= 5 && this.nextEvent.getSeconds() > 0)
                    for (Player player : Bukkit.getOnlinePlayers())
                        Titles.sendTitle(player, 0, 21, 10, ChatColor.RED + "" + (this.nextEvent.getSeconds() - 1), this.nextEvent.getName());
            }

            if (this.nextEvent.getSeconds() == 0 && this.nextEvent.getMinutes() == 0)
                this.game.getCoherenceMachine().getMessageManager().writeCustomMessage(ChatColor.YELLOW + this.nextEvent.getName() + ChatColor.YELLOW + " maintenant !", true);

            this.nextEvent.decrement();
        }
    }
}
