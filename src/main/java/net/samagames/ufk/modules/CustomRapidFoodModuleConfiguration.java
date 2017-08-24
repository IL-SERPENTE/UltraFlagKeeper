package net.samagames.ufk.modules;

import net.samagames.survivalapi.modules.gameplay.RapidFoodModule;
import net.samagames.survivalapi.utils.Meta;
import net.samagames.tools.MojangShitUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
public class CustomRapidFoodModuleConfiguration extends RapidFoodModule.ConfigurationBuilder
{
    public CustomRapidFoodModuleConfiguration()
    {
        this.addDefaults();

        this.addDrop(EntityType.SHEEP, (drops, random) ->
        {
            List<ItemStack> newDrops = drops.stream().filter(stack -> stack.getType() == Material.MUTTON).map(stack -> new ItemStack(Material.COOKED_MUTTON, stack.getAmount() * 2)).collect(Collectors.toList());

            int randomized = random.nextInt(100);

            if (randomized < 30)
                newDrops.add(new ItemStack(Material.BOW, 1));
            else if (randomized < 50)
                newDrops.add(new ItemStack(Material.BOOK, 1));

            return newDrops;
        }, true);

        this.addDrop(EntityType.COW, (drops, random) ->
        {
            List<ItemStack> newDrops = drops.stream().filter(stack -> stack.getType() == Material.RAW_BEEF).map(stack -> new ItemStack(Material.COOKED_BEEF, stack.getAmount() * 2)).collect(Collectors.toList());

            int randomized = random.nextInt(100);

            if (randomized < 5)
                newDrops.add(new ItemStack(Material.MILK_BUCKET, 1));
            else if (randomized < 50)
                newDrops.add(new ItemStack(Material.BOOK, 1));

            return newDrops;
        }, true);

        this.addDrop(EntityType.PIG, (drops, random) ->
        {
            List<ItemStack> newDrops = drops.stream().filter(stack -> stack.getType() == Material.PORK).map(stack -> new ItemStack(Material.GRILLED_PORK, stack.getAmount() * 2)).collect(Collectors.toList());

            if (random.nextInt(100) < 50)
                newDrops.add(new ItemStack(Material.BOOK, 1));

            return newDrops;
        }, true);

        this.addDrop(EntityType.CHICKEN, (drops, random) ->
        {
            List<ItemStack> newDrops = drops.stream().filter(stack -> stack.getType() == Material.RAW_CHICKEN).map(stack -> new ItemStack(Material.COOKED_CHICKEN, stack.getAmount() * 2)).collect(Collectors.toList());
            newDrops.add(new ItemStack(Material.ARROW, 8));

            return newDrops;
        }, true);

        this.addDrop(EntityType.SQUID, (drops, random) ->
        {
            List<ItemStack> newDrops = new ArrayList<>();
            newDrops.add(new ItemStack(Material.COOKED_FISH, random.nextInt(2) + 1));

            if (random.nextInt(100) < 30)
                newDrops.add(new ItemStack(Material.FISHING_ROD, 1));

            return newDrops;
        }, true);

        this.addDrop(EntityType.RABBIT, (drops, random) ->
        {
            List<ItemStack> newDrops = drops.stream().filter(stack -> stack.getType() == Material.RABBIT).map(stack -> new ItemStack(Material.COOKED_RABBIT, stack.getAmount() * 2)).collect(Collectors.toList());

            if (random.nextInt(100) < 30)
                newDrops.add(Meta.addMeta(MojangShitUtils.getPotion("long_swiftness", false, false)));

            return newDrops;
        }, true);

        this.addDrop(EntityType.BAT, (drops, random) ->
        {
            List<ItemStack> newDrops = new ArrayList<>();
            newDrops.add(new ItemStack(Material.COOKED_MUTTON, random.nextInt(2) + 1));

            if (random.nextInt(100) < 15)
                newDrops.add(Meta.addMeta(MojangShitUtils.getPotion("long_night_vision", false, false)));

            return newDrops;
        }, true);

        this.addDrop(EntityType.SKELETON, (drops, random) ->
        {
            List<ItemStack> newDrops = drops.stream().filter(stack -> stack.getType() == Material.ARROW).map(stack -> new ItemStack(Material.ARROW, stack.getAmount() * 2)).collect(Collectors.toList());

            if (random.nextInt(100) < 15)
            {
                ItemStack bow = new ItemStack(Material.BOW, 1);
                bow.addEnchantment(Enchantment.ARROW_DAMAGE, 1);

                newDrops.add(bow);
            }

            return newDrops;
        }, true);

        this.addDrop(EntityType.SPIDER, (drops, random) ->
        {
            if (random.nextInt(100) < 15)
                drops.add(Meta.addMeta(MojangShitUtils.getPotion("long_poison", true, false)));

            return drops;
        }, true);

        this.addDrop(EntityType.ZOMBIE, (drops, random) ->
        {
            List<ItemStack> newDrops = drops.stream().filter(stack -> stack.getType() == Material.ROTTEN_FLESH).map(stack -> new ItemStack(Material.COOKED_BEEF, stack.getAmount() * 2)).collect(Collectors.toList());

            if (random.nextInt(100) < 15)
                newDrops.add(Meta.addMeta(MojangShitUtils.getPotion("long_strength", false, false)));

            return newDrops;
        }, true);
    }
}
