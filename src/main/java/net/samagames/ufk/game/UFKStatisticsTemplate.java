package net.samagames.ufk.game;

import net.samagames.api.games.themachine.messages.templates.BasicMessageTemplate;
import net.samagames.survivalapi.game.SurvivalPlayer;
import net.samagames.tools.PlayerUtils;
import net.samagames.tools.chat.ChatUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.ChatColor;

import java.util.*;

/**
 * SurvivalStatisticsTemplate class
 *
 * Copyright (c) for SamaGames
 * All right reserved
 */
public class UFKStatisticsTemplate
{
    /**
     * Prepare the message with the team's members
     *
     * @param game The game
     *
     * @return A list of lines
     */
    public List<String> prepare(UFKGame game)
    {
        List<SurvivalPlayer> killers = new ArrayList<>(new HashMap<>(game.getRegisteredGamePlayers()).values());
        List<SurvivalPlayer> damagers = new ArrayList<>(new HashMap<>(game.getRegisteredGamePlayers()).values());
        List<MutablePair<UUID, Integer>> captures = getCaptures(game);

        Collections.sort(killers, (o1, o2) -> Integer.compare(o1.getKills().size(), o2.getKills().size()));
        Collections.sort(damagers, (o1, o2) -> Double.compare(o1.getDamageReporter().getTotalPlayerDamages(), o2.getDamageReporter().getTotalPlayerDamages()));
        Collections.sort(captures, ((o1, o2) -> Integer.compare(o1.getValue(), o2.getValue())));

        Collections.reverse(killers);
        Collections.reverse(damagers);
        Collections.reverse(captures);

        List<String> finalLines = new ArrayList<>();
        finalLines.add(ChatUtils.getCenteredText(ChatColor.WHITE + "•" + ChatColor.BOLD + " Statistiques du jeu " + ChatColor.RESET + ChatColor.WHITE + "•"));
        finalLines.add("");
        finalLines.add(ChatUtils.getCenteredText(ChatColor.WHITE + "★ Classement des meurtres ★"));
        finalLines.add("");

        if (killers.size() > 0)
            finalLines.add(ChatUtils.getCenteredText(ChatColor.GREEN + "1er" + ChatColor.GRAY + " - " + ChatColor.RESET + PlayerUtils.getFullyFormattedPlayerName(killers.get(0).getUUID()) + ChatColor.GRAY + " (" + killers.get(0).getKills().size() + ")"));
        else
            finalLines.add(ChatUtils.getCenteredText(ChatColor.RED + "Aucun dégat"));
        if(killers.size() > 1)
            finalLines.add(ChatUtils.getCenteredText(ChatColor.YELLOW + "2e" + ChatColor.GRAY + " - " + ChatColor.RESET + PlayerUtils.getFullyFormattedPlayerName(killers.get(1).getUUID()) + ChatColor.GRAY + " (" + killers.get(1).getKills().size() + ")"));
        if(killers.size() > 2)
            finalLines.add(ChatUtils.getCenteredText(ChatColor.RED + "3e" + ChatColor.GRAY + " - " + ChatColor.RESET + PlayerUtils.getFullyFormattedPlayerName(killers.get(2).getUUID()) + ChatColor.GRAY + " (" + killers.get(2).getKills().size() + ")"));

        finalLines.add("");
        finalLines.add("");
        finalLines.add(ChatUtils.getCenteredText(ChatColor.WHITE + "★ Classement des dégats ★"));
        finalLines.add("");

        if (damagers.size() > 0)
            finalLines.add(ChatUtils.getCenteredText(ChatColor.GREEN + "1er" + ChatColor.GRAY + " - " + ChatColor.RESET + PlayerUtils.getFullyFormattedPlayerName(damagers.get(0).getUUID()) + ChatColor.GRAY + " (" + damagers.get(0).getDamageReporter().getTotalPlayerDamages() + ")"));
        else
            finalLines.add(ChatUtils.getCenteredText(ChatColor.RED + "Aucun dégat"));
        if(damagers.size() > 1)
            finalLines.add(ChatUtils.getCenteredText(ChatColor.YELLOW + "2e" + ChatColor.GRAY + " - " + ChatColor.RESET + PlayerUtils.getFullyFormattedPlayerName(damagers.get(1).getUUID()) + ChatColor.GRAY + " (" + damagers.get(1).getDamageReporter().getTotalPlayerDamages() + ")"));
        if(damagers.size() > 2)
            finalLines.add(ChatUtils.getCenteredText(ChatColor.RED + "3e" + ChatColor.GRAY + " - " + ChatColor.RESET + PlayerUtils.getFullyFormattedPlayerName(damagers.get(2).getUUID()) + ChatColor.GRAY + " (" + damagers.get(2).getDamageReporter().getTotalPlayerDamages() + ")"));

        finalLines.add("");
        finalLines.add("");
        finalLines.add(ChatUtils.getCenteredText(ChatColor.WHITE + "★ Classement des captures ★"));
        finalLines.add("");

        if (captures.size() > 0)
            finalLines.add(ChatUtils.getCenteredText(ChatColor.GREEN + "1er" + ChatColor.GRAY + " - " + ChatColor.RESET + PlayerUtils.getFullyFormattedPlayerName(captures.get(0).getKey()) + ChatColor.GRAY + " (" + captures.get(0).getValue() + ")"));
        else
            finalLines.add(ChatUtils.getCenteredText(ChatColor.RED + "Aucune capture"));
        if(captures.size() > 1)
            finalLines.add(ChatUtils.getCenteredText(ChatColor.YELLOW + "2e" + ChatColor.GRAY + " - " + ChatColor.RESET + PlayerUtils.getFullyFormattedPlayerName(captures.get(1).getKey()) + ChatColor.GRAY + " (" + captures.get(1).getValue() + ")"));
        if(captures.size() > 2)
            finalLines.add(ChatUtils.getCenteredText(ChatColor.RED + "3e" + ChatColor.GRAY + " - " + ChatColor.RESET + PlayerUtils.getFullyFormattedPlayerName(captures.get(2).getKey()) + ChatColor.GRAY + " (" + captures.get(2).getValue() + ")"));

        finalLines.add("");

        return finalLines;
    }

    private List<MutablePair<UUID, Integer>> getCaptures(UFKGame ufkGame)
    {
        List<MutablePair<UUID, Integer>> list = new ArrayList<>();

        ufkGame.getFlags().forEach(flag -> flag.getCaptures().forEach(uuid ->
        {
            final boolean[] ok = { false };
            list.forEach(pair ->
            {
                if (pair.getKey().equals(uuid))
                {
                    pair.setValue(pair.getValue() + 1);
                    ok[0] = true;
                }
            });
            if (!ok[0])
                list.add(MutablePair.of(uuid, 1));
        }));
        return list;
    }

    /**
     * Send the message with the team's members
     *
     * @param game The game
     */
    public void execute(UFKGame game)
    {
        new BasicMessageTemplate().execute(this.prepare(game));
    }
}
