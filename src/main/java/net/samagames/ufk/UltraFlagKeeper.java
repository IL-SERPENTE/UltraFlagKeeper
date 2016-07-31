package net.samagames.ufk;

import com.google.gson.JsonPrimitive;
import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.GamesNames;
import net.samagames.survivalapi.SurvivalAPI;
import net.samagames.survivalapi.modules.block.RandomChestModule;
import net.samagames.survivalapi.modules.block.RapidOresModule;
import net.samagames.survivalapi.modules.block.TorchThanCoalModule;
import net.samagames.survivalapi.modules.block.WorldDropModule;
import net.samagames.survivalapi.modules.combat.OneShootPassiveModule;
import net.samagames.survivalapi.modules.craft.OneShieldModule;
import net.samagames.survivalapi.modules.craft.RapidToolsModule;
import net.samagames.survivalapi.modules.gameplay.AutomaticLapisModule;
import net.samagames.survivalapi.modules.gameplay.ConstantPotionModule;
import net.samagames.survivalapi.modules.gameplay.RapidFoodModule;
import net.samagames.survivalapi.modules.gameplay.RapidUsefullModule;
import net.samagames.ufk.game.UFKGame;
import net.samagames.ufk.game.UFKStatisticsHelper;
import net.samagames.ufk.listener.FlagCommandExecutor;
import net.samagames.ufk.listener.UFKListener;
import net.samagames.ufk.modules.CustomRapidFoodModuleConfiguration;
import net.samagames.ufk.modules.CustomRapidOresModuleConfiguration;
import net.samagames.ufk.modules.CustomRapidUsefullModuleConfiguration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Rigner for project UltraFlagKeeper.
 */
public class UltraFlagKeeper extends JavaPlugin
{
    private UFKGame game;

    @Override
    public void onEnable()
    {
        /** Enable DoubleRunner modules */
        SurvivalAPI.get().loadModule(RapidOresModule.class, new CustomRapidOresModuleConfiguration().build());
        SurvivalAPI.get().loadModule(RapidFoodModule.class, new CustomRapidFoodModuleConfiguration().build());
        SurvivalAPI.get().loadModule(RapidUsefullModule.class, new CustomRapidUsefullModuleConfiguration().build());

        RapidToolsModule.ConfigurationBuilder rapidToolsConfiguration = new RapidToolsModule.ConfigurationBuilder();
        rapidToolsConfiguration.setToolsMaterial(RapidToolsModule.ConfigurationBuilder.ToolMaterial.IRON);

        SurvivalAPI.get().loadModule(RapidToolsModule.class, rapidToolsConfiguration.build());

        TorchThanCoalModule.ConfigurationBuilder torchThanCoalConfiguration = new TorchThanCoalModule.ConfigurationBuilder();
        torchThanCoalConfiguration.setTorchAmount(8);

        SurvivalAPI.get().loadModule(TorchThanCoalModule.class, torchThanCoalConfiguration.build());

        WorldDropModule.ConfigurationBuilder worldDropConfiguration = new WorldDropModule.ConfigurationBuilder();
        worldDropConfiguration.addCustomDrop(Material.OBSIDIAN, new ItemStack(Material.OBSIDIAN, 4));
        worldDropConfiguration.addCustomDrop(Material.APPLE, new ItemStack(Material.GOLDEN_APPLE, 2));

        SurvivalAPI.get().loadModule(WorldDropModule.class, worldDropConfiguration.build());

        ConstantPotionModule.ConfigurationBuilder constantPotionConfiguration = new ConstantPotionModule.ConfigurationBuilder();
        constantPotionConfiguration.addPotionEffect(PotionEffectType.SPEED, 1);
        constantPotionConfiguration.addPotionEffect(PotionEffectType.FAST_DIGGING, 0);

        SurvivalAPI.get().loadModule(ConstantPotionModule.class, constantPotionConfiguration.build());
        SurvivalAPI.get().loadModule(OneShootPassiveModule.class, null);
        SurvivalAPI.get().loadModule(AutomaticLapisModule.class, null);
        SurvivalAPI.get().loadModule(OneShieldModule.class, null);

        /** Initiate game */
        int nb = SamaGamesAPI.get().getGameManager().getGameProperties().getOption("playersPerTeam", new JsonPrimitive(2)).getAsInt();
        this.game = new UFKGame(this, nb);
        SurvivalAPI.get().unloadModule(RandomChestModule.class);

        SamaGamesAPI.get().getStatsManager().setStatsToLoad(GamesNames.ULTRAFLAGKEEPER, true);
        SamaGamesAPI.get().getGameManager().setMaxReconnectTime(10);
        SamaGamesAPI.get().getGameManager().setGameStatisticsHelper(new UFKStatisticsHelper());
        SamaGamesAPI.get().getGameManager().registerGame(this.game);

        this.getServer().getPluginCommand("flag").setExecutor(new FlagCommandExecutor(this));
        this.getServer().getPluginManager().registerEvents(new UFKListener(this), this);
    }

    public UFKGame getGame()
    {
        return this.game;
    }
}
