package net.samagames.ufk.game;

import net.samagames.survivalapi.game.SurvivalPlayer;
import net.samagames.ufk.UltraFlagKeeper;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

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
public class Flag implements Listener
{
    private static final String OLD_STUFF_KEY = "old_stuff";
    
    private UltraFlagKeeper plugin;
    private Location location;
    private List<Vector> temporaryLocations;
    private byte color;
    private UUID wearer;
    private UFKTeam team;
    private List<UUID> captures;
    private List<ArmorStand> armorStands;
    private BukkitTask effectTask;
    private BukkitTask respawnTask;

    Flag(UltraFlagKeeper plugin, Location location, byte color)
    {
        this.location = location;
        this.location.setY(this.location.getWorld().getHighestBlockYAt(this.location));
        while (this.location.getBlock().getType() == Material.WOOL || this.location.getBlock().getType() == Material.WOOD_BUTTON)
            this.location.subtract(0D, 1D, 0D);
        this.location.add(0D, 1D, 0D);

        this.temporaryLocations = new ArrayList<>();
        this.color = color;
        this.team = null;
        this.effectTask = null;
        this.captures = new ArrayList<>();
        this.armorStands = new ArrayList<>();
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
        this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, () ->
        {
            if (!this.armorStands.isEmpty())
                this.armorStands.get(0).setHeadPose(this.armorStands.get(0).getHeadPose().setY(this.armorStands.get(0).getHeadPose().getY() + 0.05D));
        }, 1L, 1L);
    }

    public byte getColor()
    {
        return this.color;
    }

    public Location getLocation()
    {
        return this.location;
    }

    public void destroy()
    {
        this.temporaryLocations.clear();
        this.foreachFlagBlock(block ->
        {
            this.temporaryLocations.add(new Vector(block.getX(), block.getY(), block.getZ()));
            block.setType(Material.AIR);
        });
    }

    @SuppressWarnings("deprecation")
    public void respawn()
    {
        this.temporaryLocations.forEach(vector -> this.location.getWorld().getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ()).setTypeIdAndData(Material.WOOL.getId(), this.color, true));
        this.temporaryLocations.clear();
    }

    private void foreachFlagBlock(Consumer<Block> consumer)
    {
        this.foreachFlagBlock(this.location.clone(), consumer);
    }

    private void foreachFlagBlock(Location baseLocation, Consumer<Block> consumer)
    {
        if (baseLocation.getBlock().getType() != Material.WOOL)
            return ;
        consumer.accept(baseLocation.getBlock());
        for (int i = -1; i < 2; i++)
            for (int j = -1; j < 2; j++)
                for (int k = -1; k < 2; k++)
                    if (i != 0 || j != 0 || k != 0)
                        this.foreachFlagBlock(baseLocation.clone().add(i, j, k), consumer);
    }

    @SuppressWarnings("deprecation")
    void drop(Location location)
    {
        if (!this.armorStands.isEmpty())
            return ;

        ItemStack itemStack = new ItemStack(Material.BANNER);
        BannerMeta bannerMeta = (BannerMeta)itemStack.getItemMeta();
        bannerMeta.setBaseColor(DyeColor.getByWoolData((byte)this.team.getIcon().getDurability()));
        itemStack.setItemMeta(bannerMeta);
        for (int i = 0; i < 4; i++)
        {
            ArmorStand armorStand = location.getWorld().spawn(location.clone().add(0D, i == 3 ? 2.5 : i, 0D), ArmorStand.class);
            armorStand.setGravity(false);
            armorStand.setVisible(false);
            if (i == 0)
                armorStand.setHelmet(itemStack);
            else if (i == 3)
            {
                armorStand.setCustomName(this.team.getChatColor() + "Drapeau " + this.team.getTeamName());
                armorStand.setCustomNameVisible(true);
            }
            this.armorStands.add(armorStand);
        }
        this.respawnTask = this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () ->
        {
            this.unDrop();
            this.respawn();
            this.plugin.getGame().getCoherenceMachine().getMessageManager().writeCustomMessage(ChatColor.YELLOW + "Le drapeau de l'équipe " + this.team.getChatColor() + this.team.getTeamName() + ChatColor.YELLOW + " est revenu à sa base.", true);
        }, 600L);
    }

    void unDrop()
    {
        if (this.armorStands.isEmpty())
            return ;
        this.armorStands.forEach(ArmorStand::remove);
        this.armorStands.clear();
        if (this.respawnTask != null)
            this.respawnTask.cancel();
    }

    public UUID getWearer()
    {
        return this.wearer;
    }

    @SuppressWarnings("deprecation")
    public void setWearer(UUID wearer)
    {
        if (this.wearer != null)
        {
            Player player = this.plugin.getServer().getPlayer(this.wearer);
            if (player != null)
            {
                player.getInventory().setHelmet(player.hasMetadata(OLD_STUFF_KEY) ? (ItemStack) player.getMetadata(OLD_STUFF_KEY).get(0).value() : new ItemStack(Material.AIR));
                player.removeMetadata(OLD_STUFF_KEY, this.plugin);
                player.removePotionEffect(PotionEffectType.WEAKNESS);
                player.removePotionEffect(PotionEffectType.SLOW);
                player.removePotionEffect(PotionEffectType.WITHER);
            }
            if (this.effectTask != null)
                this.effectTask.cancel();
            this.effectTask = null;
        }
        this.wearer = wearer;
        if (this.wearer != null)
        {
            Player player = this.plugin.getServer().getPlayer(this.wearer);
            if (player != null)
            {
                ItemStack itemStack = new ItemStack(Material.BANNER);
                BannerMeta bannerMeta = (BannerMeta) itemStack.getItemMeta();
                bannerMeta.setBaseColor(DyeColor.getByWoolData((byte) this.getTeam().getIcon().getDurability()));
                itemStack.setItemMeta(bannerMeta);
                ItemStack save = player.getInventory().getHelmet();
                player.getInventory().setHelmet(itemStack);
                if (save != null)
                    player.setMetadata(OLD_STUFF_KEY, new FixedMetadataValue(this.plugin, save));
                this.effectTask = this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () ->
                {
                    player.sendMessage(ChatColor.RED + "Vous portez le drapeau depuis trop longtemps. Son poids vous fatigue et vous devenez plus faible.");
                    player.addPotionEffect(PotionEffectType.WEAKNESS.createEffect(18000, 0));
                    player.addPotionEffect(PotionEffectType.SLOW.createEffect(18000, 0));
                    this.effectTask = this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () ->
                    {
                        player.sendMessage(ChatColor.RED + "Vous croulez sous le poids du drapeau. Vous prenez maintenant du dégat.");
                        player.addPotionEffect(PotionEffectType.WITHER.createEffect(18000, 1));
                        this.effectTask = null;
                    }, 800L);
                }, 1400L);
            }
        }
    }

    public UFKTeam getTeam()
    {
        return this.team;
    }

    void setTeam(UFKTeam team)
    {
        this.team = team;
    }

    public void addCapture(UUID uuid)
    {
        this.captures.add(uuid);
    }

    List<UUID> getCaptures()
    {
        return this.captures;
    }

    List<ArmorStand> getArmorStands()
    {
        return this.armorStands;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractAtEntityEvent event)
    {
        SurvivalPlayer player;
        if (this.armorStands.stream().filter(armorStand -> armorStand.getEntityId() == event.getRightClicked().getEntityId()).findFirst().orElse(null) != null && (player = this.plugin.getGame().getPlayer(event.getPlayer().getUniqueId())) != null && !player.isSpectator())
        {
            if (player.getTeam().equals(this.team))
            {
                this.unDrop();
                this.respawn();
                this.plugin.getGame().getCoherenceMachine().getMessageManager().writeCustomMessage(event.getPlayer().getDisplayName() + ChatColor.YELLOW + " a remis le drapeau de l'équipe " + this.team.getChatColor() + this.team.getTeamName() + ChatColor.YELLOW + " à sa base.", true);
            }
            else
            {
                this.unDrop();
                this.setWearer(player.getUUID());
                this.plugin.getGame().getCoherenceMachine().getMessageManager().writeCustomMessage(event.getPlayer().getDisplayName() + ChatColor.YELLOW + " a récupéré le drapeau de l'équipe " + this.team.getChatColor() + this.team.getTeamName() + ChatColor.YELLOW + ".", true);
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event)
    {
        SurvivalPlayer player;
        if (this.armorStands.stream().filter(armorStand -> armorStand.getEntityId() == event.getEntity().getEntityId()).findFirst().orElse(null) != null && (player = this.plugin.getGame().getPlayer(event.getDamager().getUniqueId())) != null && !player.isSpectator())
        {
            if (player.getTeam().equals(this.team))
            {
                this.unDrop();
                this.respawn();
                this.plugin.getGame().getCoherenceMachine().getMessageManager().writeCustomMessage(((Player)event.getDamager()).getDisplayName() + ChatColor.YELLOW + " a remis le drapeau de l'équipe " + this.team.getChatColor() + this.team.getTeamName() + ChatColor.YELLOW + " à sa base.", true);

                ((UFKStatisticsHelper) this.plugin.getGame().getSurvivalGameStatisticsHelper()).increaseFlagsReturned(player.getUUID());
            }
            else
            {
                this.unDrop();
                this.setWearer(player.getUUID());
                this.plugin.getGame().getCoherenceMachine().getMessageManager().writeCustomMessage(((Player)event.getDamager()).getDisplayName() + ChatColor.YELLOW + " a récupéré le drapeau de l'équipe " + this.team.getChatColor() + this.team.getTeamName() + ChatColor.YELLOW + ".", true);
            }
        }
    }

    public boolean isSafe()
    {
        return this.wearer == null && this.armorStands.isEmpty();
    }
}
