package net.samagames.ufk.game;

import net.samagames.api.games.Status;
import net.samagames.survivalapi.game.SurvivalTeam;
import net.samagames.survivalapi.game.types.team.SurvivalTeamSelector;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class UFKTeamSelector extends SurvivalTeamSelector
{
    private UFKGame ufkGame;

    UFKTeamSelector(UFKGame ufkGame) throws IllegalAccessException
    {
        super(ufkGame);
        this.ufkGame = ufkGame;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (this.ufkGame.getStatus().equals(Status.IN_GAME))
            event.getHandlers().unregister(this);
        else if (event.getItem() != null && event.getItem().getType() == Material.NETHER_STAR)
            this.openGui(event.getPlayer(), new UFKGuiSelectorTeam());
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event)
    {
        if (this.ufkGame.getStatus().equals(Status.IN_GAME))
        {
            event.getHandlers().unregister(this);
            return;
        }

        if (!this.ufkGame.getStatus().equals(Status.IN_GAME))
        {
            SurvivalTeam team = this.ufkGame.getPlayerTeam(event.getPlayer().getUniqueId());
            String name = event.getLine(0);
            name = name.trim();

            if (!name.isEmpty())
            {
                team.setTeamName(name);
                event.getPlayer().sendMessage(this.ufkGame.getCoherenceMachine().getGameTag() + " " + ChatColor.GREEN + "Le nom de votre équipe est désormais : " + team.getChatColor() + team.getTeamName());
                this.openGui(event.getPlayer(), new UFKGuiSelectorTeam());
            }
            else
            {
                event.getPlayer().sendMessage(this.ufkGame.getCoherenceMachine().getGameTag() + " " + ChatColor.RED + "Le nom de l'équipe ne peut être vide.");
                this.openGui(event.getPlayer(), new UFKGuiSelectorTeam());
            }
            this.ufkGame.getPlugin().getServer().getScheduler().runTaskLater(this.ufkGame.getPlugin(), () ->
            {
                event.getBlock().setType(Material.AIR);
                event.getBlock().getRelative(BlockFace.DOWN).setType(Material.AIR);
            }, 1L);
        }
    }
}
