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
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Created by Rigner for project UltraFlagKeeper.
 */
public class Flag implements Listener
{
    private UltraFlagKeeper plugin;
    private Location location;
    private List<Vector> temporaryLocations;
    private byte color;
    private UUID wearer;
    private UFKTeam team;
    private List<UUID> captures;
    private ArmorStand armorStand;

    public Flag(UltraFlagKeeper plugin, Location location, byte color)
    {
        this.location = location;
        this.location.setY(this.location.getWorld().getHighestBlockYAt(this.location));
        while (this.location.getBlock().getType() == Material.WOOL || this.location.getBlock().getType() == Material.WOOD_BUTTON)
            this.location.subtract(0D, 1D, 0D);
        this.location.add(0D, 1D, 0D);

        this.temporaryLocations = new ArrayList<>();
        this.color = color;
        this.team = null;
        this.captures = new ArrayList<>();
        this.armorStand = null;
        this.plugin = plugin;
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

    public void foreachFlagBlock(Consumer<Block> consumer)
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
    public void drop(Location location)
    {
        if (this.armorStand != null)
            return ;
        this.armorStand = location.getWorld().spawn(location, ArmorStand.class);
        this.armorStand.setGravity(false);
        this.armorStand.setVisible(false);
        ItemStack itemStack = new ItemStack(Material.BANNER);
        BannerMeta bannerMeta = (BannerMeta)itemStack.getItemMeta();
        bannerMeta.setBaseColor(DyeColor.getByWoolData((byte)this.team.getIcon().getDurability()));
        itemStack.setItemMeta(bannerMeta);
        this.armorStand.setHelmet(itemStack);
    }

    public void unDrop()
    {
        if (this.armorStand == null)
            return ;
        this.armorStand.remove();
    }

    public UUID getWearer()
    {
        return this.wearer;
    }

    public void setWearer(UUID wearer)
    {
        if (this.wearer != null)
        {
            Player player = this.plugin.getServer().getPlayer(this.wearer);
            if (player != null && player.hasMetadata("oldstuff"))
            {
                player.getInventory().setHelmet((ItemStack) player.getMetadata("oldstuff").get(0).value());
                player.removeMetadata("oldstuff", this.plugin);
            }
        }
        this.wearer = wearer;
    }

    public UFKTeam getTeam()
    {
        return this.team;
    }

    public void setTeam(UFKTeam team)
    {
        this.team = team;
    }

    public void addCapture(UUID uuid)
    {
        this.captures.add(uuid);
    }

    public List<UUID> getCaptures()
    {
        return this.captures;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractAtEntityEvent event)
    {
        SurvivalPlayer player;
        if (this.armorStand != null && event.getRightClicked().getEntityId() == this.armorStand.getEntityId() && (player = this.plugin.getGame().getPlayer(event.getPlayer().getUniqueId())) != null && !player.isSpectator())
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
                this.plugin.getGame().getCoherenceMachine().getMessageManager().writeCustomMessage(ChatColor.YELLOW + "Le drapeau de l'équipe " + this.team.getChatColor() + this.team.getTeamName() + ChatColor.YELLOW + " est au sol.", true);
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event)
    {
        SurvivalPlayer player;
        if (this.armorStand != null && event.getEntity().getEntityId() == this.armorStand.getEntityId() && (player = this.plugin.getGame().getPlayer(event.getDamager().getUniqueId())) != null && !player.isSpectator())
        {
            if (player.getTeam().equals(this.team))
            {
                this.unDrop();
                this.respawn();
                this.plugin.getGame().getCoherenceMachine().getMessageManager().writeCustomMessage(((Player)event.getDamager()).getDisplayName() + ChatColor.YELLOW + " a remis le drapeau de l'équipe " + this.team.getChatColor() + this.team.getTeamName() + ChatColor.YELLOW + " à sa base.", true);
            }
            else
            {
                this.unDrop();
                this.setWearer(player.getUUID());
                this.plugin.getGame().getCoherenceMachine().getMessageManager().writeCustomMessage(ChatColor.YELLOW + "Le drapeau de l'équipe " + this.team.getChatColor() + this.team.getTeamName() + ChatColor.YELLOW + " est au sol.", true);
            }
        }
    }
}
