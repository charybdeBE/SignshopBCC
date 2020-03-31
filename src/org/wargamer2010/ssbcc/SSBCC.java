package org.wargamer2010.ssbcc;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.commands.CommandDispatcher;
import org.wargamer2010.signshop.configuration.*;
import org.wargamer2010.signshop.metrics.setupMetrics;
import org.wargamer2010.signshop.util.commandUtil;
import org.wargamer2010.ssbcc.command.AjustCommand;

public class SSBCC extends JavaPlugin {
    private static final Logger logger = Logger.getLogger("Minecraft");
    private static SSBCC instance = null;
    private static CommandDispatcher commandDispatcher = new CommandDispatcher();

    private static YamlConfiguration storage;
    /**
     * Log given message at given level for SSBCC
     * @param message Message to log
     * @param level Level to log at
     */
    public static void log(String message, Level level) {
        if(!message.isEmpty())
            logger.log(level,("[SignShopBCC] " + message));
    }

    @Override
    public void onEnable() {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        if(!pm.isPluginEnabled("SignShop")) {
            log("SignShop is not loaded, can not continue.", Level.SEVERE);
            pm.disablePlugin(this);
            return;
        }
        createDir();
        pm.registerEvents(new SignShopListenerU(), this);
        pm.registerEvents(new SignShopPlumeListener(), this);

        String filename = "config.yml";
        FileConfiguration ymlThing = configUtil.loadYMLFromPluginFolder(this, filename);
        if(ymlThing != null) {
            configUtil.loadYMLFromJar(this, SSBCC.class, ymlThing, filename);
            SignShopConfig.setupOperations(configUtil.fetchStringStringHashMap("signs", ymlThing));
            SignShopConfig.registerErrorMessages(configUtil.fetchStringStringHashMap("errors", ymlThing));
            for(Map.Entry<String, HashMap<String, String>> entry : configUtil.fetchHasmapInHashmap("messages", ymlThing).entrySet()) {
                SignShopConfig.registerMessages(entry.getKey(), entry.getValue());
            }
        }

        setupMetrics metrics = new setupMetrics(this);
        if(!metrics.isOptOut()) {
            if(metrics.setup())
                log("Succesfully started Metrics, see http://mcstats.org for more information.", Level.INFO);
            else
                log("Could not start Metrics, see http://mcstats.org for more information.", Level.INFO);
        }


        new UpdateTime(this);
        commandDispatcher.registerHandler("ajust", AjustCommand.getInstance());

        setInstance(this);
        log("Enabled", Level.INFO);
    }


    @Override
    public void onDisable() {
        log("Disabled", Level.INFO);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]) {
        String commandName = cmd.getName().toLowerCase();
        if(!commandName.equalsIgnoreCase("sbcc"))
            return true;

        return commandUtil.handleCommand(sender, cmd, commandLabel, args, commandDispatcher);
    }

    private void createDir() {
        if(!this.getDataFolder().exists()) {
            if(!this.getDataFolder().mkdir()) {
                log("Could not create plugin folder!", Level.SEVERE);
            }
        }
    }


    private static void setInstance(SSBCC newinstance) {
        instance = newinstance;
    }

    /**
     * Gets the instance of SSBCC
     * @return instance
     */
    public static SSBCC getInstance() {
        return instance;
    }


    /**
     * Returns the SignShopHotel Command Dispatcher
     * @return CommandDispatcher
     */
    public static CommandDispatcher getCommandDispatcher() {
        return commandDispatcher;
    }

    public static YamlConfiguration getStorage() {
        if(storage == null) {
            File filename = new File(SSBCC.getInstance().getDataFolder(), "worth.yml");
            YamlConfiguration ymlThing = new YamlConfiguration();
            try {
                ymlThing.load(filename);
                storage = ymlThing;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
        return storage;
    }

    public static void saveStorage() {
        File filename = new File(SSBCC.getInstance().getDataFolder(), "worth.yml");
        try {
            storage.save(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateSign(String key, double price) {
        Storage.get().getShopsWithMiscSetting("BCCU", key).forEach((b) -> {
            Seller seller = Storage.get().getSeller(b.getLocation());
            if (seller.getMisc("BCCU").equalsIgnoreCase(key)) {
                double modif = Double.parseDouble(seller.getMisc("modif"));
                if (modif == Double.NaN) {
                    modif = 1;
                }
                double nrStack = 0;
                for (ItemStack is : seller.getItems()) {
                    nrStack += is.getAmount();
                }
                double newPrice = price * (nrStack / seller.getItems()[0].getMaxStackSize()) * modif;
                if (newPrice <= 1.0 * modif) {
                    newPrice = 1.0 * modif;
                }
                newPrice = Math.round(newPrice * 100.0) / 100.0;

                Sign s = (Sign) b.getState();
                s.setLine(3, Double.toString(newPrice));
                s.update();
            }
        });
    }

}
