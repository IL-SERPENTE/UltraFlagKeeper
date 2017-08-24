package net.samagames.ufk.modules;

import net.minecraft.server.v1_8_R3.MathHelper;
import net.samagames.survivalapi.modules.block.RapidOresModule;
import net.samagames.tools.MojangShitUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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
public class CustomRapidOresModuleConfiguration extends RapidOresModule.ConfigurationBuilder
{
    public CustomRapidOresModuleConfiguration()
    {
        this.addDefaults();

        this.addDrop(new ItemStack(Material.COAL), new IRapidOresHook()
        {
            @Override
            public ItemStack getDrop(ItemStack base, Random random)
            {
                return new ItemStack(Material.TORCH, 6);
            }

            @Override
            public int getExperienceModifier(Random random)
            {
                return MathHelper.nextInt(random, 3, 6);
            }
        }, true);

        this.addDrop(new ItemStack(Material.IRON_ORE), new IRapidOresHook()
        {
            @Override
            public ItemStack getDrop(ItemStack base, Random random)
            {
                return new ItemStack(Material.IRON_INGOT, 6);
            }

            @Override
            public int getExperienceModifier(Random random)
            {
                return MathHelper.nextInt(random, 5, 7);
            }
        }, true);

        this.addDrop(new ItemStack(Material.DIAMOND), new IRapidOresHook()
        {
            @Override
            public ItemStack getDrop(ItemStack base, Random random)
            {
                return new ItemStack(Material.DIAMOND, 4);
            }

            @Override
            public int getExperienceModifier(Random random)
            {
                return MathHelper.nextInt(random, 7, 9);
            }
        }, true);

        this.addDrop(new ItemStack(Material.QUARTZ_BLOCK, 1), new IRapidOresHook()
        {
            @Override
            public ItemStack getDrop(ItemStack base, Random random)
            {
                int randomized = random.nextInt(100);

                if (randomized < 25)
                    return MojangShitUtils.getPotion("long_strength", false, false);
                else if (randomized < 40)
                    return MojangShitUtils.getPotion("long_swiftness", false, false);
                else if (randomized < 55)
                    return MojangShitUtils.getPotion("long_fire_resistance", false, false);
                else if (randomized < 70)
                    return MojangShitUtils.getPotion("long_leaping", false, false);
                else if (randomized < 85)
                    return MojangShitUtils.getPotion("long_night_vision", false, false);
                else
                    return MojangShitUtils.getPotion("long_poison", true, false);
            }

            @Override
            public int getExperienceModifier(Random random)
            {
                return MathHelper.nextInt(random, 5, 7);
            }
        }, true);
    }
}