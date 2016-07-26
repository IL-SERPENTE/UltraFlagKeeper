package net.samagames.ufk.listener;

import net.samagames.survivalapi.game.SurvivalPlayer;
import net.samagames.survivalapi.game.SurvivalTeam;
import net.samagames.tools.Titles;
import net.samagames.ufk.UltraFlagKeeper;
import net.samagames.ufk.game.Flag;
import net.samagames.ufk.game.UFKTeam;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;

public class FlagCommandExecutor implements CommandExecutor
{
    private UltraFlagKeeper plugin;

    public FlagCommandExecutor(UltraFlagKeeper plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
    {
        if (!(commandSender instanceof Player) || !this.plugin.getGame().getSurvivalGameLoop().isFlagCommandEnabled())
        {
            commandSender.sendMessage(ChatColor.RED + "Vous ne pouvez pas faire ça maintenant.");
            return true;
        }
        SurvivalPlayer survivalPlayer = this.plugin.getGame().getPlayer(((Player)commandSender).getUniqueId());
        if (survivalPlayer == null)
            return true;
        SurvivalTeam team = survivalPlayer.getTeam();
        Location location = ((UFKTeam) team).getFlag().getLocation();

        Location spawn;
        Random random = new Random();
        int i = 0;
        do
        {
            spawn = location.clone().add(random.nextDouble() % 4D, random.nextDouble() % 2D, random.nextDouble() % 4D);
            i++;
        }
        while (i < 10 && (spawn.getBlock().getType() == Material.AIR || spawn.getBlock().getRelative(BlockFace.UP).getType() == Material.AIR));
        final Location finalSpawn = spawn;
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> ((Player)commandSender).teleport(finalSpawn), 1L);
        return true;
    }
}
