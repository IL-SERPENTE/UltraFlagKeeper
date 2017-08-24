package net.samagames.ufk.game;

import net.samagames.survivalapi.game.SurvivalTeam;
import net.samagames.survivalapi.game.types.SurvivalTeamGame;
import net.samagames.survivalapi.game.types.team.GuiSelectTeam;
import net.samagames.survivalapi.game.types.team.SurvivalTeamSelector;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.UUID;

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
class UFKGuiSelectorTeam extends GuiSelectTeam
{
    private static SurvivalTeamGame game;

    @Override
    public void display(Player player)
    {
        this.inventory = Bukkit.getServer().createInventory(null, 54, "Sélection d'équipe");

        int last = 10;

        for (SurvivalTeam team : UFKGuiSelectorTeam.game.getTeams())
        {
            String name = team.getChatColor() + "Equipe " + team.getTeamName() + " [" + team.getPlayersUUID().size() + "/" + UFKGuiSelectorTeam.game.getPersonsPerTeam() + "]";
            ArrayList<String> lore = new ArrayList<>();

            if (team.isLocked())
            {
                lore.add(ChatColor.RED + "L'équipe est fermée !");
                lore.add("");
            }

            for (UUID uuid : team.getPlayersUUID().keySet())
            {
                if (UFKGuiSelectorTeam.game.getPlugin().getServer().getPlayer(uuid) != null)
                    lore.add(team.getChatColor() + " - " + Bukkit.getPlayer(uuid).getName());
                else
                    team.removePlayer(uuid);
            }

            this.setSlotData(name, team.getIcon(), last, lore.toArray(new String[lore.size()]), "team_" + team.getChatColor());

            if (last == 16)
                last = 19;
            else
                last++;
        }

        this.setSlotData("Sortir de l'équipe", Material.ARROW, 31, null, "leave");

        String[] lore = new String[]{ChatColor.GREEN + "Réservé aux VIP :)"};

        this.setSlotData("Changer le nom de l'équipe", Material.BOOK_AND_QUILL, 39, lore, "teamname");
        this.setSlotData("Inviter un joueur", Material.FEATHER, 41, lore, "invit");

        player.openInventory(this.inventory);
    }

    @Override
    public void onClick(final Player player, ItemStack stack, String action)
    {
        if (action.startsWith("team_"))
        {
            for (SurvivalTeam team : game.getTeams())
            {
                if (action.equals("team_" + team.getChatColor()))
                {
                    if (!team.isLocked())
                    {
                        int n = (game.getInGamePlayers().size() + 1) / game.getTeams().size();
                        if (team.canJoin())
                        {
                            if (team.getPlayersUUID().size() >= n)
                            {
                                player.sendMessage(game.getCoherenceMachine().getGameTag() + ChatColor.RED + " Il y a trop de monde dans l'équipe !");
                                return ;
                            }
                            if (game.getPlayerTeam(player.getUniqueId()) != null)
                                game.getPlayerTeam(player.getUniqueId()).removePlayer(player.getUniqueId());

                            team.join(player.getUniqueId());
                            player.sendMessage(game.getCoherenceMachine().getGameTag() + " " + ChatColor.YELLOW + "Vous êtes entré dans l'équipe " + team.getChatColor() + team.getTeamName() + ChatColor.YELLOW + " !");
                        }
                        else
                        {
                            player.sendMessage(game.getCoherenceMachine().getGameTag() + " " + ChatColor.RED + "L'équipe choisie est pleine.");
                        }
                    }
                    else
                    {
                        player.sendMessage(game.getCoherenceMachine().getGameTag() + " " + ChatColor.RED + "L'équipe choisie est fermée !");
                    }

                    break;
                }
            }
            SurvivalTeamSelector.getInstance().openGui(player, new UFKGuiSelectorTeam());
        }
        else
            super.onClick(player, stack, action);
    }

    public static void setGame(SurvivalTeamGame instance)
    {
        UFKGuiSelectorTeam.game = instance;
    }
}
