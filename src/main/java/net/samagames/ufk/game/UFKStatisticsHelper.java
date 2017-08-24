package net.samagames.ufk.game;

import net.samagames.api.SamaGamesAPI;
import net.samagames.survivalapi.game.SurvivalGameStatisticsHelper;

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
public class UFKStatisticsHelper implements SurvivalGameStatisticsHelper
{
    @Override
    public void increaseKills(UUID uuid)
    {
        SamaGamesAPI.get().getStatsManager().getPlayerStats(uuid).getUltraFlagKeeperStatistics().incrByKills(1);
    }

    @Override
    public void increaseDeaths(UUID uuid)
    {
        SamaGamesAPI.get().getStatsManager().getPlayerStats(uuid).getUltraFlagKeeperStatistics().incrByDeaths(1);
    }

    @Override
    public void increaseDamages(UUID uuid, double damages)
    {
        SamaGamesAPI.get().getStatsManager().getPlayerStats(uuid).getUltraFlagKeeperStatistics().incrByDamages((int) damages);
    }

    @Override
    public void increasePlayedTime(UUID uuid, long playedTime)
    {
        SamaGamesAPI.get().getStatsManager().getPlayerStats(uuid).getUltraFlagKeeperStatistics().incrByPlayedTime(playedTime);
    }

    @Override
    public void increasePlayedGames(UUID uuid)
    {
        SamaGamesAPI.get().getStatsManager().getPlayerStats(uuid).getUltraFlagKeeperStatistics().incrByPlayedGames(1);
    }

    @Override
    public void increaseWins(UUID uuid)
    {
        SamaGamesAPI.get().getStatsManager().getPlayerStats(uuid).getUltraFlagKeeperStatistics().incrByWins(1);
    }

    public void increaseFlagsCaptured(UUID uuid)
    {
        SamaGamesAPI.get().getStatsManager().getPlayerStats(uuid).getUltraFlagKeeperStatistics().incrByFlagsCaptured(1);
    }

    void increaseFlagsReturned(UUID uuid)
    {
        SamaGamesAPI.get().getStatsManager().getPlayerStats(uuid).getUltraFlagKeeperStatistics().incrByFlagsReturned(1);
    }
}