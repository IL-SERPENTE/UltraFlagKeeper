package net.samagames.ufk.game;

import net.samagames.survivalapi.game.SurvivalTeam;
import net.samagames.survivalapi.game.types.SurvivalTeamGame;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;

/**
 * Created by Rigner for project UltraFlagKeeper.
 */
public class UFKTeam extends SurvivalTeam
{
    private Flag flag;
    private int score;

    UFKTeam(SurvivalTeamGame game, String name, DyeColor color, ChatColor chatColor)
    {
        super(game, name, color, chatColor);
        this.score = 0;
        this.flag = null;
    }

    public Flag getFlag()
    {
        return this.flag;
    }

    public int getScore()
    {
        return this.score;
    }

    void setFlag(Flag flag)
    {
        this.flag = flag;
    }

    public void setScore(int score)
    {
        this.score = score;
    }
}
