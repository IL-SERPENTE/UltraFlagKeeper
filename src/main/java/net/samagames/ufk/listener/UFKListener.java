package net.samagames.ufk.listener;

import net.minecraft.server.v1_9_R2.EnumItemSlot;
import net.minecraft.server.v1_9_R2.PacketPlayOutEntityEquipment;
import net.samagames.api.games.Status;
import net.samagames.survivalapi.game.SurvivalPlayer;
import net.samagames.survivalapi.game.SurvivalTeam;
import net.samagames.ufk.UltraFlagKeeper;
import net.samagames.ufk.game.Flag;
import net.samagames.ufk.game.UFKTeam;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rigner for project UltraFlagKeeper.
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
            if (flag.getLocation().clone().subtract(0D, 3D, 0D).distanceSquared(event.getBlockPlaced().getLocation()) < 36)
            {
                event.getPlayer().sendMessage(ChatColor.RED + "Vous ne pouvez pas placer de bloc aussi près du drapeau.");
                event.setCancelled(true);
                return ;
            }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (this.plugin.getGame().getStatus() != Status.IN_GAME)
            return ;
        for (Flag flag : this.plugin.getGame().getFlags())
            if (flag.getLocation().clone().subtract(0D, 3D, 0D).distanceSquared(event.getBlock().getLocation()) < 36)
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
            if (flag.getLocation().distanceSquared(event.getClickedBlock().getLocation()) < 36 && flag.getColor() != survivalPlayer.getTeam().getIcon().getDurability())
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
                if (own.getWearer() != null)
                    return ;
                SurvivalTeam team = flag.getTeam();
                flag.addCapture(event.getPlayer().getUniqueId());
                flag.setWearer(null);
                flag.respawn();
                this.plugin.getGame().getCoherenceMachine().getMessageManager().writeCustomMessage(event.getPlayer().getDisplayName() + ChatColor.YELLOW + " a ramené le drapeau de l'équipe " + team.getChatColor() + team.getTeamName() + ChatColor.YELLOW + " a sa base.", true);
                ((UFKTeam)survivalPlayer.getTeam()).setScore(((UFKTeam)survivalPlayer.getTeam()).getScore() + 1);
                PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(event.getPlayer().getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(event.getPlayer().getInventory().getHelmet() == null ? new ItemStack(Material.AIR) : event.getPlayer().getPlayer().getInventory().getHelmet()));
                event.getPlayer().getWorld().getPlayers().forEach(p -> ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet));
                if (((UFKTeam)team).getScore() >= 5)
                    this.plugin.getGame().win(team);
            }
        });
    }

    @EventHandler
    public void onExplosion(BlockExplodeEvent event)
    {
        List<Block> list = new ArrayList<>(event.blockList());
        this.plugin.getGame().getFlags().forEach(flag -> list.stream().filter(block -> block.getLocation().distanceSquared(flag.getLocation()) < 36).forEach(block -> event.blockList().remove(block)));
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event)
    {
        List<Block> list = new ArrayList<>(event.blockList());
        this.plugin.getGame().getFlags().forEach(flag -> list.stream().filter(block -> block.getLocation().distanceSquared(flag.getLocation()) < 36).forEach(block -> event.blockList().remove(block)));
    }
}
