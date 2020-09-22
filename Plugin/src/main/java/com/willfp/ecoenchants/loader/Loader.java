package com.willfp.ecoenchants.loader;

import com.comphenix.protocol.ProtocolLibrary;
import com.willfp.ecoenchants.EcoEnchantsPlugin;
import com.willfp.ecoenchants.anvil.AnvilListeners;
import com.willfp.ecoenchants.command.commands.CommandEcodebug;
import com.willfp.ecoenchants.command.commands.CommandEcoreload;
import com.willfp.ecoenchants.command.commands.CommandEcoskip;
import com.willfp.ecoenchants.command.commands.CommandEnchantinfo;
import com.willfp.ecoenchants.config.ConfigManager;
import com.willfp.ecoenchants.display.EnchantDisplay;
import com.willfp.ecoenchants.display.packets.PacketOpenWindowMerchant;
import com.willfp.ecoenchants.display.packets.PacketSetCreativeSlot;
import com.willfp.ecoenchants.display.packets.PacketSetSlot;
import com.willfp.ecoenchants.display.packets.PacketWindowItems;
import com.willfp.ecoenchants.enchantments.EcoEnchant;
import com.willfp.ecoenchants.enchantments.EcoEnchants;
import com.willfp.ecoenchants.enchantments.meta.EnchantmentRarity;
import com.willfp.ecoenchants.enchantments.meta.EnchantmentTarget;
import com.willfp.ecoenchants.events.armorequip.ArmorListener;
import com.willfp.ecoenchants.events.armorequip.DispenserArmorListener;
import com.willfp.ecoenchants.events.entitydeathbyentity.EntityDeathByEntityListeners;
import com.willfp.ecoenchants.events.naturalexpgainevent.NaturalExpGainListeners;
import com.willfp.ecoenchants.extensions.ExtensionManager;
import com.willfp.ecoenchants.grindstone.GrindstoneListeners;
import com.willfp.ecoenchants.integrations.anticheat.AnticheatManager;
import com.willfp.ecoenchants.integrations.anticheat.plugins.AnticheatAAC;
import com.willfp.ecoenchants.integrations.anticheat.plugins.AnticheatMatrix;
import com.willfp.ecoenchants.integrations.antigrief.AntigriefManager;
import com.willfp.ecoenchants.integrations.antigrief.plugins.*;
import com.willfp.ecoenchants.integrations.essentials.EssentialsManager;
import com.willfp.ecoenchants.integrations.essentials.plugins.IntegrationEssentials;
import com.willfp.ecoenchants.listeners.ArrowListeners;
import com.willfp.ecoenchants.listeners.EnchantingListeners;
import com.willfp.ecoenchants.listeners.PlayerJoinListener;
import com.willfp.ecoenchants.listeners.VillagerListeners;
import com.willfp.ecoenchants.naturalloot.LootPopulator;
import com.willfp.ecoenchants.nms.BlockBreak;
import com.willfp.ecoenchants.nms.Cooldown;
import com.willfp.ecoenchants.nms.TridentStack;
import com.willfp.ecoenchants.util.EcoRunnable;
import com.willfp.ecoenchants.util.Logger;
import com.willfp.ecoenchants.util.UpdateChecker;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.generator.BlockPopulator;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing methods for the loading and unloading of EcoEnchants
 */
public class Loader {

    /**
     * Called by {@link EcoEnchantsPlugin#onEnable()}
     */
    public static void load() {
        Logger.info("==========================================");
        Logger.info("");
        Logger.info("Loading §aEcoEnchants");
        Logger.info("Made by §aAuxilor§f - willfp.com");
        Logger.info("");
        Logger.info("==========================================");

        /*
        Check for paper
         */

        boolean isPapermc = false;
        try {
            isPapermc = Class.forName("com.destroystokyo.paper.VersionHistoryManager$VersionData") != null;
        } catch (ClassNotFoundException ignored) {}

        if (!isPapermc) {
            Bukkit.getScheduler().runTaskLater(EcoEnchantsPlugin.getInstance(), () -> {
                Logger.info("");
                Logger.info("----------------------------");
                Logger.info("");
                Logger.error("You don't seem to be running paper!");
                Logger.error("Paper is strongly recommended for all servers,");
                Logger.error("and enchantments like Drill may not function properly without it");
                Logger.error("Download Paper from §fhttps://papermc.io");
                Logger.info("");
                Logger.info("----------------------------");
                Logger.info("");
            }, 1);
        }

        /*
        Load Configs
         */

        Logger.info("Loading Configs...");
        ConfigManager.updateConfigs();
        Logger.info("");

        /*
        Load ProtocolLib
         */

        Logger.info("Loading ProtocolLib...");
        EcoEnchantsPlugin.getInstance().protocolManager = ProtocolLibrary.getProtocolManager();
        new PacketOpenWindowMerchant().register();
        new PacketSetCreativeSlot().register();
        new PacketSetSlot().register();
        new PacketWindowItems().register();

        /*
        Load land management support
         */

        Logger.info("Scheduling Integration Loading...");

        Bukkit.getScheduler().runTaskLater(EcoEnchantsPlugin.getInstance(), () -> {

            Logger.info("Loading Integrations...");

            if(Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
                AntigriefManager.registerAntigrief(new AntigriefWorldGuard());
                Logger.info("WorldGuard: §aENABLED");
            } else {
                Logger.info("WorldGuard: §9DISABLED");
            }

            if(Bukkit.getPluginManager().isPluginEnabled("GriefPrevention")) {
                AntigriefManager.registerAntigrief(new AntigriefGriefPrevention());
                Logger.info("GriefPrevention: §aENABLED");
            } else {
                Logger.info("GriefPrevention: §9DISABLED");
            }

            if(Bukkit.getPluginManager().isPluginEnabled("FactionsUUID")) {
                AntigriefManager.registerAntigrief(new AntigriefFactionsUUID());
                Logger.info("FactionsUUID: §aENABLED");
            } else {
                Logger.info("FactionsUUID: §9DISABLED");
            }

            if(Bukkit.getPluginManager().isPluginEnabled("Towny")) {
                AntigriefManager.registerAntigrief(new AntigriefTowny());
                Logger.info("Towny: §aENABLED");
            } else {
                Logger.info("Towny: §9DISABLED");
            }

            if(Bukkit.getPluginManager().isPluginEnabled("Lands")) {
                AntigriefManager.registerAntigrief(new AntigriefLands());
                Logger.info("Lands: §aENABLED");
            } else {
                Logger.info("Lands: §9DISABLED");
            }

            if(Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
                EssentialsManager.registerAntigrief(new IntegrationEssentials());
                Logger.info("Essentials: §aENABLED");
            } else {
                Logger.info("Essentials: §9DISABLED");
            }

            if(Bukkit.getPluginManager().isPluginEnabled("AAC")) {
                AnticheatManager.registerAnticheat(new AnticheatAAC());
                Logger.info("AAC: §aENABLED");
            } else {
                Logger.info("AAC: §9DISABLED");
            }

            if(Bukkit.getPluginManager().isPluginEnabled("Matrix")) {
                AnticheatManager.registerAnticheat(new AnticheatMatrix());
                Logger.info("Matrix: §aENABLED");
            } else {
                Logger.info("Matrix: §9DISABLED");
            }

            if(Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus")) {
                AnticheatManager.registerAnticheat(new AnticheatAAC());
                Logger.info("NCP: §aENABLED");
            } else {
                Logger.info("NCP: §9DISABLED");
            }

            if(Bukkit.getPluginManager().isPluginEnabled("Spartan")) {
                AnticheatManager.registerAnticheat(new AnticheatAAC());
                Logger.info("Spartan: §aENABLED");
            } else {
                Logger.info("Spartan: §9DISABLED");
            }

            Logger.info("");

        }, 1);

        Logger.info("");

        /*
        Load NMS
         */

        Logger.info("Loading NMS APIs...");

        if(Cooldown.init()) {
            Logger.info("Cooldown: §aSUCCESS");
        } else {
            Logger.info("Cooldown: §cFAILURE");
            Logger.error("§cAborting...");
            Bukkit.getPluginManager().disablePlugin(EcoEnchantsPlugin.getInstance());
        }

        if(TridentStack.init()) {
            Logger.info("Trident API: §aSUCCESS");
        } else {
            Logger.info("Trident API: §cFAILURE");
            Logger.error("§cAborting...");
            Bukkit.getPluginManager().disablePlugin(EcoEnchantsPlugin.getInstance());
        }

        if(BlockBreak.init()) {
            Logger.info("Block Break: §aSUCCESS");
        } else {
            Logger.info("Block Break: §cFAILURE");
            Logger.error("§cAborting...");
            Bukkit.getPluginManager().disablePlugin(EcoEnchantsPlugin.getInstance());
        }
        
        Logger.info("");

        /*
        Register Events
         */

        Logger.info("Registering Events...");
        Bukkit.getPluginManager().registerEvents(new ArmorListener(), EcoEnchantsPlugin.getInstance());
        Bukkit.getPluginManager().registerEvents(new DispenserArmorListener(), EcoEnchantsPlugin.getInstance());
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), EcoEnchantsPlugin.getInstance());
        Bukkit.getPluginManager().registerEvents(new EnchantingListeners(), EcoEnchantsPlugin.getInstance());
        Bukkit.getPluginManager().registerEvents(new GrindstoneListeners(), EcoEnchantsPlugin.getInstance());
        Bukkit.getPluginManager().registerEvents(new AnvilListeners(), EcoEnchantsPlugin.getInstance());
        Bukkit.getPluginManager().registerEvents(new EntityDeathByEntityListeners(), EcoEnchantsPlugin.getInstance());
        Bukkit.getPluginManager().registerEvents(new NaturalExpGainListeners(), EcoEnchantsPlugin.getInstance());
        Bukkit.getPluginManager().registerEvents(new VillagerListeners(), EcoEnchantsPlugin.getInstance());
        Bukkit.getPluginManager().registerEvents(new ArrowListeners(), EcoEnchantsPlugin.getInstance());
        Logger.info("");

        /*
        Add Block Populators
         */

        Logger.info("Scheduling Adding Block Populators...");
        Bukkit.getScheduler().runTaskLater(EcoEnchantsPlugin.getInstance(), () -> {
            Bukkit.getServer().getWorlds().forEach((world -> {
                world.getPopulators().add(new LootPopulator());
            }));
        }, 1);
        Logger.info("");

        /*
        Load Extensions
         */

        Logger.info("Loading Extensions...");

        ExtensionManager.loadExtensions();
        if(ExtensionManager.getLoadedExtensions().isEmpty()) {
            Logger.info("§cNo extensions found");
        } else {
            Logger.info("Extensions Loaded:");
            ExtensionManager.getLoadedExtensions().forEach((extension, name) -> {
                Logger.info("- " + name);
            });
        }
        Logger.info("");

        /*
        Create enchantment config files (for first time use)
         */

        Logger.info("Creating Enchantment Configs...");
        ConfigManager.updateEnchantmentConfigs();
        Logger.info("");

        /*
        Load all enchantments, rarities, and targets
         */

        Logger.info("Adding Enchantments to API...");
        EnchantmentRarity.update();
        EnchantmentTarget.update();
        if(EnchantmentRarity.getAll().size() == 0 || EnchantmentTarget.getAll().size() == 0) {
            Logger.error("§cError loading rarities or targets! Aborting...");
            Bukkit.getPluginManager().disablePlugin(EcoEnchantsPlugin.getInstance());
            return;
        } else {
            Logger.info(EnchantmentRarity.getAll().size() + " Rarities Loaded:");
            EnchantmentRarity.getAll().forEach((rarity -> {
                Logger.info("- " + rarity.getName() + ": Table Probability=" + rarity.getProbability() + ", Minimum Level=" + rarity.getMinimumLevel() + ", Villager Probability=" + rarity.getVillagerProbability() + ", Loot Probability=" + rarity.getLootProbability() + ", Has Custom Color=" + rarity.hasCustomColor());
            }));

            Logger.info("");

            Logger.info(EnchantmentTarget.getAll().size() + " Targets Loaded:");
            EnchantmentTarget.getAll().forEach((target) -> {
                Logger.info("- " + target.getName() + ": Materials=" + target.getMaterials().toString());
            });
        }
        Logger.info("");

        if (EcoEnchants.getAll().size() == 0) {
            Logger.error("§cError adding enchantments! Aborting...");
            Bukkit.getPluginManager().disablePlugin(EcoEnchantsPlugin.getInstance());
            return;
        } else {
            Logger.info(EcoEnchants.getAll().size() + " Enchantments Loaded:");
            EcoEnchants.getAll().forEach((ecoEnchant -> {
                if(ecoEnchant.getType().equals(EcoEnchant.EnchantmentType.SPECIAL)) {
                    Logger.info(ChatColor.translateAlternateColorCodes('&', ConfigManager.getLang().getString("special-color")) + "- " + ecoEnchant.getName() + ": " + ecoEnchant.getKey().toString());
                } else if(ecoEnchant.getType().equals(EcoEnchant.EnchantmentType.ARTIFACT)) {
                    Logger.info(ChatColor.translateAlternateColorCodes('&', ConfigManager.getLang().getString("artifact-color")) + "- " + ecoEnchant.getName() + ": " + ecoEnchant.getKey().toString());
                } else {
                    Logger.info("- " + ecoEnchant.getName() + ": " + ecoEnchant.getKey().toString());
                }
            }));
        }
        Logger.info("");

        /*
        Load enchantment configs
         */

        Logger.info("Loading Enchantment Configs...");
        ConfigManager.updateEnchantmentConfigs();
        Logger.info("");

        /*
        Register Enchantments
         */

        Logger.info("Registering Enchantments...");
        EcoEnchants.update();
        EnchantDisplay.update();
        Logger.info("");

        /*
        Register Enchantment Listeners
         */

        Logger.info("Registering Enchantment Listeners...");
        EcoEnchants.getAll().forEach((ecoEnchant -> {
            if(ecoEnchant.isEnabled()) {
                Bukkit.getPluginManager().registerEvents(ecoEnchant, EcoEnchantsPlugin.getInstance());
            }
        }));
        Logger.info("");

        /*
        Register Enchantment Tasks
         */

        Logger.info("Registering Enchantment Tasks...");
        EcoEnchants.getAll().forEach((ecoEnchant -> {
            if(ecoEnchant instanceof EcoRunnable) {
                Bukkit.getScheduler().scheduleSyncRepeatingTask(EcoEnchantsPlugin.getInstance(), (Runnable) ecoEnchant, 5, ((EcoRunnable) ecoEnchant).getTime());
            }
        }));
        Logger.info("");


        /*
        Load Commands
         */

        Logger.info("Loading Commands...");
        Bukkit.getPluginCommand("ecoreload").setExecutor(new CommandEcoreload());
        Bukkit.getPluginCommand("ecodebug").setExecutor(new CommandEcodebug());
        Bukkit.getPluginCommand("enchantinfo").setExecutor(new CommandEnchantinfo());
        Bukkit.getPluginCommand("ecoskip").setExecutor(new CommandEcoskip());
        Logger.info("");
        
        /*
        Start bStats
         */

        Logger.info("Hooking into bStats...");
        new Metrics(EcoEnchantsPlugin.getInstance(), 7666);
        Logger.info("");

        /*
        Start update checker
         */


        new UpdateChecker(EcoEnchantsPlugin.getInstance(), 79573).getVersion((version) -> {
            DefaultArtifactVersion currentVersion = new DefaultArtifactVersion(EcoEnchantsPlugin.getInstance().getDescription().getVersion());
            DefaultArtifactVersion mostRecentVersion = new DefaultArtifactVersion(version);
            Logger.info("----------------------------");
            Logger.info("");
            Logger.info("EcoEnchants Updater");
            Logger.info("");
            if (currentVersion.compareTo(mostRecentVersion) > 0 || currentVersion.equals(mostRecentVersion)) {
                Logger.info("§aEcoEnchants is up to date! (Version " + EcoEnchantsPlugin.getInstance().getDescription().getVersion() + ")");
            } else {
                EcoEnchantsPlugin.outdated = true;
                EcoEnchantsPlugin.newVersion = version;

                Bukkit.getScheduler().runTaskTimer(EcoEnchantsPlugin.getInstance(), () -> {
                    Logger.info("§6EcoEnchants is out of date! (Version " + EcoEnchantsPlugin.getInstance().getDescription().getVersion() + ")");
                    Logger.info("§6The newest version is §f" + version);
                    Logger.info("§6Download the new version here: §fhttps://www.spigotmc.org/resources/ecoenchants.79573/");
                }, 0, 36000);
            }
            Logger.info("");
            Logger.info("----------------------------");
        });

        /*
        Finish
         */

        Logger.info("Loaded §aEcoEnchants!");
    }

    /**
     * Called by {@link EcoEnchantsPlugin#onDisable()}
     */
    public static void unload() {
        Logger.info("§cDisabling EcoEnchants...");
        Logger.info("Removing Block Populators...");
        Bukkit.getServer().getWorlds().forEach((world -> {
            List<BlockPopulator> populators = new ArrayList<>(world.getPopulators());
            populators.forEach((blockPopulator -> {
                if(blockPopulator instanceof LootPopulator) {
                    world.getPopulators().remove(blockPopulator);
                }
            }));
        }));
        Logger.info("");
        Logger.info("§cUnloading Extensions...");
        ExtensionManager.unloadExtensions();
        Logger.info("§fBye! :)");
    }
}
