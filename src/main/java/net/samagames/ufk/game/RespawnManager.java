package net.samagames.ufk.game;

import net.minecraft.server.v1_9_R2.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_9_R2.PacketPlayOutNamedEntitySpawn;
import net.samagames.tools.Titles;
import net.samagames.ufk.UltraFlagKeeper;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class RespawnManager implements Listener
{
    private UltraFlagKeeper plugin;
    private Map<UUID, Pair<BukkitTask, Location>> players;

    public RespawnManager(UltraFlagKeeper plugin)
    {
        this.players = new HashMap<>();
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void respawn(Player player)
    {
        player.setFireTicks(0);
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> player.setFireTicks(0), 1L);
        this.plugin.getServer().getOnlinePlayers().stream().filter(bPlayer -> bPlayer.getEntityId() != player.getEntityId()).forEach(bPlayer -> ((CraftPlayer)bPlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(((CraftPlayer)player).getHandle().getId())));

        player.setAllowFlight(true);
        player.setFlying(true);
        player.setFlySpeed(0F);

        BukkitTask task = this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, new Runnable()
        {
            private int time = 1;

            @Override
            public void run()
            {
                if (this.time == 10)
                {
                    unstuck(player);
                    return ;
                }
                this.time++;
                Titles.sendTitle(player, 1, 18, 1, "", ChatColor.GOLD + String.valueOf(10 - this.time));
            }
        }, 20L, 20L);

        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> this.players.put(player.getUniqueId(), Pair.of(task, player.getLocation())), 1L);
    }

    private void unstuck(Player player)
    {
        Pair<BukkitTask, Location> task = this.players.get(player.getUniqueId());
        if (task == null)
            return ;
        task.getKey().cancel();
        this.plugin.getServer().getOnlinePlayers().stream().filter(bPlayer -> bPlayer.getEntityId() != player.getEntityId()).forEach(bPlayer -> ((CraftPlayer)bPlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(((CraftPlayer)player).getHandle())));
        player.setFireTicks(0);

        player.setFlying(false);
        player.setAllowFlight(false);
        player.setFlySpeed(0.2F);

        this.players.remove(player.getUniqueId());
    }

    public void cancelAll()
    {
        Map<UUID, Pair<BukkitTask, Location>> tmp = new HashMap<>(this.players);
        tmp.forEach((uuid, respawnTask) ->
        {
            Player player = this.plugin.getServer().getPlayer(uuid);
            if (player != null)
                unstuck(player);
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event)
    {
        if (this.players.containsKey(event.getEntity().getUniqueId()))
        {
            event.setCancelled(true);
            event.setDamage(0D);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event)
    {
        if (this.players.containsKey(event.getEntity().getUniqueId()) || this.players.containsKey(event.getDamager().getUniqueId()))
        {
            event.setCancelled(true);
            event.setDamage(0D);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFly(PlayerToggleFlightEvent event)
    {
        if (!event.isFlying() && this.players.containsKey(event.getPlayer().getUniqueId()))
        {
            event.setCancelled(true);
            event.getPlayer().setFlying(true);
            event.getPlayer().teleport(this.players.get(event.getPlayer().getUniqueId()).getValue());
        }
    }
}
