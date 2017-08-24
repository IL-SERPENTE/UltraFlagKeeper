package net.samagames.ufk.listener;

import net.samagames.ufk.UltraFlagKeeper;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Collection;

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
class Noteblocks
{
    private Noteblocks()
    {
    }

    private static void playNote(Collection<? extends Player> players, int note, Instrument instrument)
    {
        players.forEach(player -> player.playNote(player.getLocation(), instrument, new Note(note)));
    }

    static void playFlagCapturedMelody(UltraFlagKeeper plugin)
    {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();

        scheduler.runTaskLater(plugin, () -> playNote(players, 3, Instrument.PIANO), 2L);
        scheduler.runTaskLater(plugin, () -> playNote(players, 5, Instrument.PIANO), 4L);
        scheduler.runTaskLater(plugin, () -> playNote(players, 7, Instrument.PIANO), 6L);
        scheduler.runTaskLater(plugin, () -> playNote(players, 3, Instrument.PIANO), 9L);
        scheduler.runTaskLater(plugin, () -> playNote(players, 5, Instrument.PIANO), 11L);
        scheduler.runTaskLater(plugin, () -> playNote(players, 7, Instrument.PIANO), 13L);
        scheduler.runTaskLater(plugin, () -> playNote(players, 9, Instrument.PIANO), 15L);
        scheduler.runTaskLater(plugin, () -> playNote(players, 10, Instrument.PIANO), 17L);
        scheduler.runTaskLater(plugin, () -> playNote(players, 0, Instrument.PIANO), 19L);
        scheduler.runTaskLater(plugin, () -> playNote(players, 5, Instrument.PIANO), 20L);

        scheduler.runTaskLater(plugin, () -> playNote(players, 7, Instrument.BASS_GUITAR), 3L);
        scheduler.runTaskLater(plugin, () -> playNote(players, 9, Instrument.BASS_GUITAR), 5L);
        scheduler.runTaskLater(plugin, () -> playNote(players, 0, Instrument.BASS_GUITAR), 9L);
        scheduler.runTaskLater(plugin, () -> playNote(players, 1, Instrument.BASS_GUITAR), 11L);
        scheduler.runTaskLater(plugin, () -> playNote(players, 6, Instrument.BASS_GUITAR), 15L);
        scheduler.runTaskLater(plugin, () -> playNote(players, 7, Instrument.BASS_GUITAR), 17L);
        scheduler.runTaskLater(plugin, () -> playNote(players, 5, Instrument.BASS_GUITAR), 20L);

        scheduler.runTaskLater(plugin, () -> playNote(players, 0, Instrument.BASS_DRUM), 4L);
        scheduler.runTaskLater(plugin, () -> playNote(players, 1, Instrument.BASS_DRUM), 6L);
        scheduler.runTaskLater(plugin, () -> playNote(players, 2, Instrument.BASS_DRUM), 10L);
        scheduler.runTaskLater(plugin, () -> playNote(players, 1, Instrument.BASS_DRUM), 12L);
        scheduler.runTaskLater(plugin, () -> playNote(players, 0, Instrument.BASS_DRUM), 16L);
        scheduler.runTaskLater(plugin, () -> playNote(players, 21, Instrument.BASS_DRUM), 18L);
    }
}
