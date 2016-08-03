package net.samagames.ufk.game;

import net.minecraft.server.v1_9_R2.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_9_R2.PacketPlayOutNamedEntitySpawn;
import net.samagames.tools.Titles;
import net.samagames.ufk.UltraFlagKeeper;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class RespawnManager implements Listener
{
    private UltraFlagKeeper plugin;
    private Map<UUID, BukkitTask> players;

    public RespawnManager(UltraFlagKeeper plugin)
    {
        this.players = new HashMap<>();
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void respawn(Player player)
    {
        player.setWalkSpeed(0F);
        player.removePotionEffect(PotionEffectType.JUMP);
        player.setFireTicks(0);
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> player.setFireTicks(0), 1L);
        player.getActivePotionEffects().forEach(potionEffect -> this.plugin.getLogger().info("DEBUG : " + potionEffect.getType() + " " + potionEffect.getAmplifier() + " " + potionEffect.getDuration()));
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> player.addPotionEffect(PotionEffectType.JUMP.createEffect(200, 128), true), 9L);
        this.plugin.getServer().getOnlinePlayers().stream().filter(bPlayer -> bPlayer.getEntityId() != player.getEntityId()).forEach(bPlayer -> ((CraftPlayer)bPlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(((CraftPlayer)player).getHandle().getId())));
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
        this.players.put(player.getUniqueId(), task);
    }

    private void unstuck(Player player)
    {
        BukkitTask task = this.players.get(player.getUniqueId());
        if (task == null)
            return ;
        task.cancel();
        this.plugin.getServer().getOnlinePlayers().stream().filter(bPlayer -> bPlayer.getEntityId() != player.getEntityId()).forEach(bPlayer -> ((CraftPlayer)bPlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(((CraftPlayer)player).getHandle())));
        player.removePotionEffect(PotionEffectType.JUMP);
        player.setFireTicks(0);
        player.setWalkSpeed(0.2F);
        player.getActivePotionEffects().forEach(potionEffect -> this.plugin.getLogger().info("DEBUG : " + potionEffect.getType() + " " + potionEffect.getAmplifier() + " " + potionEffect.getDuration()));
        this.players.remove(player.getUniqueId());
    }

    public void cancelAll()
    {
        Map<UUID, BukkitTask> tmp = new HashMap<>(this.players);
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
}
