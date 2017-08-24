package net.samagames.ufk.game;

import net.samagames.survivalapi.game.SurvivalTeam;
import net.samagames.survivalapi.game.types.SurvivalTeamGame;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;

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
