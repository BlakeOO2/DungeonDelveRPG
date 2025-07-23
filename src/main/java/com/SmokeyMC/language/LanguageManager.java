package com.SmokeyMC.language;

import com.SmokeyMC.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private final Main plugin;
    private final String defaultLanguage = "en_us";
    private String currentLanguage;
    private Map<String, FileConfiguration> languageFiles;

     public LanguageManager(Main plugin){
         this.plugin = plugin;
         this.languageFiles = new HashMap<>();
         this.currentLanguage = plugin.getConfig().getString("Language", defaultLanguage);

         loadLanguages();
     }


    private void loadLanguages() {
        // Load default language first
        loadLanguage(defaultLanguage);

        // Load current language if different from default
        if (!currentLanguage.equals(defaultLanguage)) {
            loadLanguage(currentLanguage);
        }
    }


    private void loadLanguage(String language) {
        File langFile = new File(plugin.getDataFolder(), "lang/" + language + ".yml");

        if (!langFile.exists()) {
            // Create default language file if it doesn't exist
            plugin.saveResource("lang/" + language + ".yml", false);
        }

        FileConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);

        // Load default values from jar if this is the default language
        if (language.equals(defaultLanguage)) {
            InputStream defaultStream = plugin.getResource("lang/" + language + ".yml");
            if (defaultStream != null) {
                FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
                langConfig.setDefaults(defaultConfig);
            }
        }

        languageFiles.put(language, langConfig);
    }

    public String getMessage(String key) {
        return getMessage(key, new String[0]);
    }

    public String getMessage(String key, String... placeholders) {
        // Try to get message from current language

        plugin.debug("Key: " + key);
        plugin.debug("Placeholders: " + joinPlaceholders(placeholders) );
        //plugin.debug("Current Language: " + );

        FileConfiguration currentLangFile = languageFiles.get(currentLanguage);
        String message = null;

        if (currentLangFile != null) {
            message = currentLangFile.getString(key);
        }

        // If message not found in current language, try default language
        if (message == null && !currentLanguage.equals(defaultLanguage)) {
            FileConfiguration defaultLangFile = languageFiles.get(defaultLanguage);
            if (defaultLangFile != null) {
                message = defaultLangFile.getString(key);
            }
        }

        // If still not found, return the key itself
        if (message == null) {
            return "Missing translation: " + key;
        }

        // Replace placeholders if any
        plugin.debug("Message before replacement: " + message);
        if (placeholders.length > 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                if (i + 1 < placeholders.length) {
                    String placeholder = "{" + placeholders[i] + "}";
                    String value = placeholders[i + 1];
                    plugin.debug("Replacing " + placeholder + " with " + value);
                    message = message.replace(placeholder, value);
                }
            }
        }
        plugin.debug("Message after replacement: " + message);

        // Convert color codes
        return message.replace("&", "§");
    }

    public void reloadLanguages() {
        languageFiles.clear();
        currentLanguage = plugin.getConfig().getString("language", defaultLanguage);
        loadLanguages();
    }

    public void saveLanguageFile(String language) {
        FileConfiguration langConfig = languageFiles.get(language);
        if (langConfig == null) return;

        try {
            File langFile = new File(plugin.getDataFolder(), "lang/" + language + ".yml");
            langConfig.save(langFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save language file: " + language);
            e.printStackTrace();
        }
    }

    public String joinPlaceholders(String... placeholders) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                String placeholder = placeholders[i];
                String value = placeholders[i + 1];
                result.append("{").append(placeholder).append("} ").append(value).append(" ");
            }
        }

        return result.toString().trim(); // To remove the trailing space
    }
}
