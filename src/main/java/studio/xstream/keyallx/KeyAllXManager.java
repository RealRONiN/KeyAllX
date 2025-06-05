package studio.xstream.keyallx;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import studio.xstream.keyallx.Config.ConfigSettings;
import studio.xstream.keyallx.Config.ReminderObj;
import studio.xstream.keyallx.Display.*;
import studio.xstream.keyallx.Timer.KeyTimer;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class KeyAllXManager {

    private final KeyAllX plugin;
    private final TextDisplayAbs textDisplay;
    private final KeyTimer keyTimer;
    private final HashMap<Integer, ReminderObj> messages;
    private Integer taskId;
    private Object taskHandle = null;

    public KeyAllXManager(KeyAllX plugin) {
        this.plugin = plugin;
        MorePaperLib morePaperLib = new MorePaperLib(plugin);
        this.textDisplay = setupVersion();
        this.messages = new HashMap<>();
        reloadConfig(plugin.getConfig());
        this.keyTimer = new KeyTimer(plugin.getLogger(), textDisplay, messages);

        if(textDisplay != null)
            return;

        plugin.getLogger().warning("Invalid version detected, plugin will shut down...");
        Bukkit.getPluginManager().disablePlugin(plugin);
    }

    public void startTimer() {
    stopTimer(); // Ensure any previously running timer is stopped.

    boolean isFolia = false;
        try {
            // Check if Folia's global region scheduler is available
            if (Bukkit.getServer().getClass().getMethod("getGlobalRegionScheduler") != null) {
                isFolia = Bukkit.isGlobalRegionSchedulerAvailable();
            }
        } catch (NoSuchMethodException | SecurityException e) {
            // Not a Folia-compatible server or an older version
            isFolia = false;
        }

        if (isFolia) {
            plugin.getLogger().info("Starting timer using Folia's GlobalRegionScheduler.");
            // Assuming keyTimer is your Runnable
            this.taskHandle = plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, (scheduledTask) -> {
                try {
                    keyTimer.run();
                } catch (Exception ex) {
                    plugin.getLogger().severe("Error occurred in Folia scheduled task: " + ex.getMessage());
                    ex.printStackTrace();
                    // Optionally: scheduledTask.cancel();
                }
            }, 20L, 20L); // 20 ticks delay, 20 ticks period (1 second)
        } else {
            plugin.getLogger().info("Starting timer using Bukkit Scheduler.");
            // Assuming keyTimer is your Runnable
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, keyTimer, 20L, 20L);
            this.taskHandle = bukkitTask.getTaskId();
        }
    }

    public void stopTimer() {
        if (taskHandle != null) {
            if (taskHandle instanceof ScheduledTask) {
                // Folia ScheduledTask
                ((ScheduledTask) taskHandle).cancel();
                plugin.getLogger().info("Folia timer task cancelled.");
            } else if (taskHandle instanceof Integer) {
                // Bukkit task ID
                Bukkit.getScheduler().cancelTask((Integer) taskHandle);
                plugin.getLogger().info("Bukkit timer task (ID: " + taskHandle + ") cancelled.");
            } else {
                plugin.getLogger().warning("Timer task handle was of an unknown type: " + taskHandle.getClass().getName());
            }
            taskHandle = null; // Clear the handle
        }

        // Assuming your keyTimer object has an assignInterval method
        // If keyTimer is just a Runnable and doesn't have this method, you'll need to adjust.
        if (keyTimer instanceof YourKeyTimerClass) { // Replace YourKeyTimerClass with its actual class
            ((YourKeyTimerClass) keyTimer).assignInterval();
        } else {
            // Handle the case where keyTimer doesn't have assignInterval, or log a warning
            // plugin.getLogger().info("keyTimer.assignInterval() called (or would be if method existed).");
        }
    }

    public int getTimerInterval(){
        return keyTimer.getInterval();
    }

    public void reloadConfig(FileConfiguration config){
        messages.clear();

        //Original message using the same format so users won't have to change anything
        messages.put(0, new ReminderObj(ConfigSettings.getMessage(), null, ConfigSettings.getHotbarMessage(), ConfigSettings.getTitleMessage(), ConfigSettings.getSubTitleMessage(), ConfigSettings.getTitleFadeIn(), ConfigSettings.getTitleStay(), ConfigSettings.getTitleFadeOut(), ConfigSettings.getSound(), ConfigSettings.getVolume(), ConfigSettings.getPitch()));

        ConfigurationSection sec = config.getConfigurationSection("reminders");

        if(sec == null)
            return;

        Set<String> keys = sec.getKeys(false);

        if(keys.isEmpty())
            return;

        for(String key : keys){
            Integer interval = ConfigSettings.getInteger(key);

            if(interval == null || interval < 1)
                continue;

            String path = "reminders." + key + ".";
            List<String> message = ConfigSettings.color(config.getStringList(path + "message"));
            String backupMessage = ConfigSettings.translateColorCodes(config.getString(path + "message"));
            String hotbar = ConfigSettings.translateColorCodes(config.getString(path + "hotbar"));
            String title = ConfigSettings.translateColorCodes(config.getString(path + "title"));
            String subtitle = ConfigSettings.translateColorCodes(config.getString(path + "subtitle"));
            int fadeIn = config.getInt(path + "fadeIn");
            int stay = config.getInt(path + "stay");
            int fadeOut = config.getInt(path + "fadeOut");
            Sound sound = ConfigSettings.getSound(config.getString(path + "sound"));
            float volume = (float) config.getDouble(path + "volume");
            float pitch = (float) config.getDouble(path + "pitch");

            messages.put(interval, new ReminderObj(message, backupMessage, hotbar, title, subtitle, fadeIn, stay, fadeOut, sound, volume, pitch));
        }
    }

    public TextDisplayAbs getTextDisplay(){
        return textDisplay;
    }

    private TextDisplayAbs setupVersion() {
        String version = Bukkit.getServer().getClass().getPackage().getName();
        String v = version.equalsIgnoreCase("org.bukkit.craftbukkit") ? "v1_11_R1" : version.split("\\.")[3];

        if (is11OrUp(v))
            return new TextDisplay_Abs_1_11_Up();

        switch (v) {
            case "v1_8_R1":
                return new TextDisplay_Abs_1_8_R1();

            case "v1_8_R2":
                return new TextDisplay_Abs_1_8_R2();

            case "v1_8_R3":
                return new TextDisplay_Abs_1_8_R3();

            case "v1_9_R1":
                return new TextDisplay_Abs_1_9_R1();

            case "v1_9_R2":
                return new TextDisplay_Abs_1_9_R2();

            case "v1_10_R1":
                return new TextDisplay_Abs_1_10_R1();

            default:
                return null;
        }
    }

    private boolean is11OrUp(String s) {
        if(s.split("_").length < 3)
            return false;

        Integer p0 = ConfigSettings.getInteger(s.split("_")[1]);
        Integer p1 = ConfigSettings.getInteger(s.split("_")[0]);

        return (p0 != null && p0 >= 11) || (p1 != null && p1 >= 2);
    }

}
