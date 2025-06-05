package studio.xstream.keyallx.Config;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import studio.xstream.keyallx.KeyAllX;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConfigSettings {

    private static int timeInterval;
    private static Sound sound;
    private static float volume;
    private static float pitch;
    private static String titleMessage;
    private static String subTitleMessage;
    private static int titleFadeIn;
    private static int titleStay;
    private static int titleFadeOut;
    private static String hotbarMessage;
    private static List<String> command;
    private static List<String> perPlayerCommand;
    private static boolean debug;
    private static List<String> message;


    public static void reloadConfig(KeyAllX plugin) {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();


        timeInterval = config.getInt("timeInterval", 60);
        sound = getSound(config.getString("sound", "ENTITY_EXPERIENCE_ORB_PICKUP"));
        volume = (float) config.getDouble("volume", 1.0);
        pitch = (float) config.getDouble("pitch", 1.0);
        titleMessage = translateColorCodes(config.getString("title-message"));
        subTitleMessage = translateColorCodes(config.getString("subtitle-message"));
        titleFadeIn = config.getInt("title-fade-in", 10);
        titleStay = config.getInt("title-stay", 70);
        titleFadeOut = config.getInt("title-fade-out", 20);
        hotbarMessage = translateColorCodes(config.getString("hotbar-message"));
        command = config.getStringList("command");
        perPlayerCommand = config.getStringList("perPlayerCommand");

        if(command.isEmpty())
            command.add(config.getString("command", "give @p minecraft:diamond"));

        debug = config.getBoolean("debug", false);
        message = color(config.getStringList("message"));

        if(message.isEmpty()){
            String single = translateColorCodes(config.getString("message"));

            if(single != null)
                message.add(single);
        }

        if(message.isEmpty())
            message.add(translateColorCodes("&aCongratulations! You've been awarded a key as part of our Key All event!"));

    }

    public static int getTimeInterval() {
        return timeInterval;
    }

    public static Sound getSound() {
        return sound;
    }

    public static float getVolume() {
        return volume;
    }

    public static float getPitch() {
        return pitch;
    }

    public static String getTitleMessage() {
        return titleMessage;
    }

    public static String getSubTitleMessage() {
        return subTitleMessage;
    }

    public static int getTitleFadeIn() {
        return titleFadeIn;
    }

    public static int getTitleStay() {
        return titleStay;
    }

    public static int getTitleFadeOut() {
        return titleFadeOut;
    }

    public static String getHotbarMessage() {
        return hotbarMessage;
    }

    public static List<String> getCommand() {
        return command;
    }

    public static List<String> getPerPlayerCommand() {
        return perPlayerCommand;
    }

    public static List<String> getMessage(){
        return message;
    }

    public static List<String> getPerPlayerCommand(String player) {
        List<String> clonedList = new LinkedList<>();
        for (String line : perPlayerCommand)
            clonedList.add(line.replace("{player}", player).replace("player", player));

        return clonedList;
    }

    public static boolean isDebug() {
        return debug;
    }

    public static String translateColorCodes(String message) {
        return message == null ? null : ChatColor.translateAlternateColorCodes('&', message);
    }

    public static Integer getInteger(String s){
        try{
            return Integer.parseInt(s);
        }
        catch(Throwable e){
            return null;
        }
    }

    public static List<String> color(List<String> list) {
        return list == null || list.isEmpty() ? new ArrayList<>() : list.stream().filter(Objects::nonNull).map(ConfigSettings::translateColorCodes).collect(Collectors.toList());
    }

    public static Sound getSound(String s){
        try{
            return Sound.valueOf(s.toUpperCase());
        }
        catch (Throwable e){
            return null;
        }
    }

    public static Object createTitle(String titleMessage, String subTitleMessage, int titleFadeIn, int titleStay, int titleFadeOut){
        try{
            Class.forName("net.kyori.adventure.text.Component");
            return ConfigPaperHelper.createTitle(titleMessage, subTitleMessage, titleFadeIn, titleStay, titleFadeOut);
        }
        catch(Throwable e){
            return null;
        }
    }

    public static Object createHotbar(String hotbarMessage){
        try{
            Class.forName("net.kyori.adventure.text.Component");
            return ConfigPaperHelper.createHotbar(hotbarMessage);
        }
        catch(Throwable e){
            return null;
        }
    }
}

