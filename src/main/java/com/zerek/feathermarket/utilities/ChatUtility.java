package com.zerek.feathermarket.utilities;

import com.zerek.feathermarket.FeatherMarket;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for parsing color codes and adding pixel perfect whitespace to text
 * Requires chatutility.yml
 */
public class ChatUtility {

    private static ChatUtility chatUtility = null;

    private final FeatherMarket plugin;
    private final Map<String, Integer> characterWidths = new HashMap<String, Integer>();

    public ChatUtility(FeatherMarket plugin) {
        this.plugin = plugin;
        init();
    }

    private void init(){
        //establish file connection
        File file = new File(plugin.getDataFolder() + File.separator + "chatutility.yml");
        if (!file.exists()) {plugin.saveResource("chatutility.yml",false);}
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);

        //map characters widths in pixels
        ConfigurationSection charactersYml = yml.getConfigurationSection("characters");
        charactersYml.getKeys(false).forEach(key -> characterWidths.put(charactersYml.getString(key + ".character"),charactersYml.getInt(key + ".width")));
    }

    /**
     * Calculates the pixel width of a given String in minecraft font.
     * @param textComponent - Text to be calculated
     * @return Pixel width
     */
    public int getWidth(TextComponent textComponent){
        String string = PlainTextComponentSerializer.plainText().serialize(textComponent);
        int stringWidth = 0;
        for (char c : string.toCharArray()) stringWidth += characterWidths.get(String.valueOf(c));
        return stringWidth;
    }

    /**
     * Add pixel perfect whitespace to a given string
     * @param textComponent - Text to add whitespace to.
     * @param pixels - Total pixel count for the string and the whitespace.
     * @param isRightAligned - Set true if text should be aligned to right.
     * @return String with added whitespace to match pixel width provided.
     */
    public TextComponent addSpacing(TextComponent textComponent, int pixels, boolean isRightAligned){
        double difference = pixels - getWidth(textComponent);
        int addonSpaces;
        int addonBoldSpaces = 0;

        // Calculate how many regular and bold spaces to append to the given string to meet the requested length
        if (difference % 4 == 1 && difference >= 4){
            addonSpaces = (int) (Math.floor(difference/4) - 1);
            addonBoldSpaces = 1;
        }
        else if (difference % 4 == 2 && difference >= 8){
            addonSpaces = (int) (Math.floor(difference/4) - 2);
            addonBoldSpaces = 2;
        }
        else if (difference % 4 == 3 && difference >= 12){
            addonSpaces = (int) (Math.floor(difference/4) - 3);
            addonBoldSpaces = 3;
        }
        else{
            addonSpaces = (int) (difference/4);
        }
        // Append spaces and bold spaces to the end of the given string
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < addonSpaces; i++) {
            stringBuilder.append(' ');
        }
        stringBuilder.append(org.bukkit.ChatColor.BOLD);
        for (int i = 0; i < addonBoldSpaces; i++) {
            stringBuilder.append(' ');
        }
        stringBuilder.append(org.bukkit.ChatColor.RESET);
        TextComponent spaces = Component.text(String.valueOf(stringBuilder));

        if (isRightAligned) return Component.text("").append(spaces).append(textComponent);
        else return Component.text("").append(textComponent).append(spaces);
    }

    /**
     * Add pixel perfect whitespace to a given string by passing off to full method with isRightAligned = false.
     * @param textComponent - Text to add whitespace to.
     * @param pixels - Total pixel count for the string and the whitespace.
     * @return String with added whitespace to match pixel width provided.
     */
    public TextComponent addSpacing(TextComponent textComponent, int pixels){
        return addSpacing(textComponent,pixels,false);
    }
}