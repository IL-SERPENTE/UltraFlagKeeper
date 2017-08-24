package net.samagames.ufk.listener;

import net.samagames.survivalapi.game.SurvivalPlayer;
import net.samagames.survivalapi.game.SurvivalTeam;
import net.samagames.ufk.UltraFlagKeeper;
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
        while (i < 30 && (spawn.getBlock().getType() == Material.AIR || spawn.getBlock().getRelative(BlockFace.UP).getType() == Material.AIR));
        final Location finalSpawn = spawn;
        ((Player)commandSender).setNoDamageTicks(10);
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> ((Player)commandSender).teleport(finalSpawn), 1L);
        return true;
    }
}
