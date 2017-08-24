package net.samagames.ufk.listener;

import net.samagames.api.games.Status;
import net.samagames.survivalapi.game.SurvivalPlayer;
import net.samagames.survivalapi.game.SurvivalTeam;
import net.samagames.ufk.UltraFlagKeeper;
import net.samagames.ufk.game.Flag;
import net.samagames.ufk.game.UFKStatisticsHelper;
import net.samagames.ufk.game.UFKTeam;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.ArrayList;
import java.util.List;

/*
 * This file is part of UltraFlagKeeper (Run4Flag).
 *
 * UltraFlagKeeper (Run4Flag) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UltraFlagKeeper (Run4Flag) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UltraFlagKeeper (Run4Flag).  If not, see <http://www.gnu.org/licenses/>.
 */
public class UFKListener implements Listener
{
    private UltraFlagKeeper plugin;

    public UFKListener(UltraFlagKeeper plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event)
    {
        event.getWorld().setGameRuleValue("keepInventory", "true");
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if (this.plugin.getGame().getStatus() != Status.IN_GAME)
            return ;
        for (Flag flag : this.plugin.getGame().getFlags())
            if (flag.getLocation().clone().subtract(0D, 3D, 0D).distanceSquared(event.getBlockPlaced().getLocation()) < 25)
            {
                event.getPlayer().sendMessage(ChatColor.RED + "Vous ne pouvez pas placer de bloc aussi près du drapeau.");
                event.setCancelled(true);
                return ;
            }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(PlayerBucketEmptyEvent event)
    {
        if (this.plugin.getGame().getStatus() != Status.IN_GAME || event.getBucket() == Material.MILK_BUCKET)
            return ;
        for (Flag flag : this.plugin.getGame().getFlags())
        {
            Location location = event.getBlockClicked().getLocation();
            if (location.getBlockY() > flag.getLocation().getBlockY())
                location.setY(flag.getLocation().getY());
            if (flag.getLocation().clone().subtract(0D, 3D, 0D).distanceSquared(location) < 49)
            {
                event.getPlayer().sendMessage(ChatColor.RED + "Vous ne pouvez pas placer d" + (event.getBucket() == Material.LAVA_BUCKET ? "e lave" : "'eau") + " aussi près du drapeau.");
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (this.plugin.getGame().getStatus() != Status.IN_GAME)
            return ;
        for (Flag flag : this.plugin.getGame().getFlags())
            if (flag.getLocation().clone().subtract(0D, 3D, 0D).distanceSquared(event.getBlock().getLocation()) < 25)
            {
                event.getPlayer().sendMessage(ChatColor.RED + "Vous ne pouvez pas casser de bloc aussi près du drapeau.");
                event.setCancelled(true);
                return ;
            }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (this.plugin.getGame().getStatus() != Status.IN_GAME)
            return ;
        SurvivalPlayer survivalPlayer;
        if ((event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK)
                || event.getClickedBlock().getType() != Material.WOOL
                || this.plugin.getGame().getStatus() != Status.IN_GAME
                || !this.plugin.getGame().isPvPActivated()
                || (survivalPlayer = this.plugin.getGame().getPlayer(event.getPlayer().getUniqueId())) == null)
            return ;

        for (Flag flag : this.plugin.getGame().getFlags())
            if (flag.getLocation().distanceSquared(event.getClickedBlock().getLocation()) < 25 && flag.getColor() != survivalPlayer.getTeam().getIcon().getDurability())
            {
                SurvivalTeam team = flag.getTeam();
                flag.destroy();
                flag.setWearer(event.getPlayer().getUniqueId());
                this.plugin.getGame().getCoherenceMachine().getMessageManager().writeCustomMessage(event.getPlayer().getDisplayName() + ChatColor.YELLOW + " a volé le drapeau de l'équipe " + team.getChatColor() + team.getTeamName(), true);

                return ;
            }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        if (this.plugin.getGame().getStatus() != Status.IN_GAME)
            return ;
        this.plugin.getGame().getFlags().forEach(flag ->
        {
            SurvivalPlayer survivalPlayer;
            if (flag.getWearer() != null && flag.getWearer().equals(event.getPlayer().getUniqueId()) && (survivalPlayer = this.plugin.getGame().getPlayer(event.getPlayer().getUniqueId())) != null && ((UFKTeam)survivalPlayer.getTeam()).getFlag().getLocation().distanceSquared(event.getPlayer().getLocation()) < 25)
            {
                Flag own = ((UFKTeam)survivalPlayer.getTeam()).getFlag();
                if (!own.isSafe())
                    return ;
                SurvivalTeam team = flag.getTeam();
                flag.addCapture(event.getPlayer().getUniqueId());
                flag.setWearer(null);
                flag.respawn();

                ((UFKStatisticsHelper) this.plugin.getGame().getSurvivalGameStatisticsHelper()).increaseFlagsCaptured(survivalPlayer.getUUID());

                this.plugin.getGame().getCoherenceMachine().getMessageManager().writeCustomMessage(event.getPlayer().getDisplayName() + ChatColor.YELLOW + " a ramené le drapeau de l'équipe " + team.getChatColor() + team.getTeamName() + ChatColor.YELLOW + " a sa base.", true);
                ((UFKTeam)survivalPlayer.getTeam()).setScore(((UFKTeam)survivalPlayer.getTeam()).getScore() + 1);
                if (((UFKTeam)survivalPlayer.getTeam()).getScore() >= 5)
                    this.plugin.getGame().win(survivalPlayer.getTeam());
                Noteblocks.playFlagCapturedMelody(this.plugin);
            }
        });
    }

    @EventHandler
    public void onExplosion(BlockExplodeEvent event)
    {
        List<Block> list = new ArrayList<>(event.blockList());
        this.plugin.getGame().getFlags().forEach(flag -> list.stream().filter(block -> block.getLocation().distanceSquared(flag.getLocation()) < 49).forEach(block -> event.blockList().remove(block)));
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event)
    {
        List<Block> list = new ArrayList<>(event.blockList());
        this.plugin.getGame().getFlags().forEach(flag -> list.stream().filter(block -> block.getLocation().distanceSquared(flag.getLocation()) < 49).forEach(block -> event.blockList().remove(block)));
    }

    @EventHandler
    public void onBlockFire(BlockBurnEvent event)
    {
        if (event.getBlock().getType() == Material.WOOL || event.getBlock().getType() == Material.FENCE)
            event.setCancelled(true);
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event)
    {
        if (event.getRecipe().getResult().getType() == Material.WOOL)
            event.getRecipe().getResult().setType(Material.AIR);
    }

    @EventHandler
    public void onDrop(EntitySpawnEvent event)
    {
        if (event.getEntityType() == EntityType.DROPPED_ITEM && ((Item)event.getEntity()).getItemStack().getType() == Material.WOOD_BUTTON)
            event.setCancelled(true);
    }
}
