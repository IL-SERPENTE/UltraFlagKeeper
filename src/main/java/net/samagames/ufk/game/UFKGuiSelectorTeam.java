package net.samagames.ufk.game;

import net.samagames.survivalapi.game.SurvivalTeam;
import net.samagames.survivalapi.game.types.SurvivalTeamGame;
import net.samagames.survivalapi.game.types.team.GuiSelectTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class UFKGuiSelectorTeam extends GuiSelectTeam
{
    private static SurvivalTeamGame game;

    @Override
    public void display(Player player)
    {
        this.inventory = Bukkit.getServer().createInventory(null, 54, "Sélection d'équipe");

        int last = 10;

        for (SurvivalTeam team : game.getTeams())
        {
            String name = team.getChatColor() + "Equipe " + team.getTeamName() + " [" + team.getPlayersUUID().size() + "/" + game.getPersonsPerTeam() + "]";
            ArrayList<String> lores = new ArrayList<>();

            if (team.isLocked())
            {
                lores.add(ChatColor.RED + "L'équipe est fermée !");
                lores.add("");
            }

            for (UUID uuid : team.getPlayersUUID().keySet())
            {
                if (game.getPlugin().getServer().getPlayer(uuid) != null)
                    lores.add(team.getChatColor() + " - " + Bukkit.getPlayer(uuid).getName());
                else
                    team.removePlayer(uuid);
            }

            this.setSlotData(name, team.getIcon(), last, lores.toArray(new String[lores.size()]), "team_" + team.getChatColor());

            if (last == 16)
                last = 19;
            else
                last++;
        }

        this.setSlotData("Sortir de l'équipe", Material.ARROW, 31, null, "leave");

        String[] lores = new String[]{ChatColor.GREEN + "Réservé aux VIP :)"};

        this.setSlotData("Changer le nom de l'équipe", Material.BOOK_AND_QUILL, 39, lores, "teamname");
        this.setSlotData("Inviter un joueur", Material.FEATHER, 41, lores, "invit");

        player.openInventory(this.inventory);
    }

    public static void setGame(SurvivalTeamGame instance)
    {
        game = instance;
    }
}
