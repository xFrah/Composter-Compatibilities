package com.fdimo.compostercompat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ModConfig {

    public static final boolean DEFAULT_DEBUG = false;
    public static final boolean DEFAULT_ROTTEN_FLESH_COMPOSTABLE = false;
    public static final boolean DEFAULT_MEAT_COMPOSTABLE = false;
    public static final List<String> DEFAULT_ALLOWED_RECIPE_TYPES = List.of(
            "minecraft:crafting",
            "minecraft:smelting",
            "minecraft:smoking",
            "minecraft:campfire_cooking",
            "farmersdelight:cooking",
            "brewinandchewin:fermenting",
            "create:mixing",
            "create:compacting",
            "create:filling",
            "create:mechanical_crafting"
    );

    public static boolean debug = DEFAULT_DEBUG;
    public static boolean rottenFleshCompostable = DEFAULT_ROTTEN_FLESH_COMPOSTABLE;
    public static boolean meatCompostable = DEFAULT_MEAT_COMPOSTABLE;
    public static List<String> allowedRecipeTypes = new ArrayList<>(DEFAULT_ALLOWED_RECIPE_TYPES);
    private static Set<String> allowedRecipeTypeSet = new LinkedHashSet<>(DEFAULT_ALLOWED_RECIPE_TYPES);

    static {
        load();
    }

    public static boolean isRecipeTypeAllowed(String recipeTypeId) {
        return recipeTypeId != null && allowedRecipeTypeSet.contains(recipeTypeId);
    }

    private static void resetDefaults() {
        debug = DEFAULT_DEBUG;
        rottenFleshCompostable = DEFAULT_ROTTEN_FLESH_COMPOSTABLE;
        meatCompostable = DEFAULT_MEAT_COMPOSTABLE;
        allowedRecipeTypes = new ArrayList<>(DEFAULT_ALLOWED_RECIPE_TYPES);
        allowedRecipeTypeSet = new LinkedHashSet<>(DEFAULT_ALLOWED_RECIPE_TYPES);
    }

    private static Boolean parseStrictBoolean(String val) {
        if ("true".equalsIgnoreCase(val)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(val)) {
            return Boolean.FALSE;
        }
        return null;
    }

    private static String stripComment(String line) {
        boolean inDoubleQuote = false;
        boolean inSingleQuote = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            } else if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
            } else if (c == '#' && !inDoubleQuote && !inSingleQuote) {
                return line.substring(0, i);
            }
        }
        return line;
    }

    private static List<String> parseStringList(String arrayContent) {
        List<String> result = new ArrayList<>();
        StringBuilder current = null;
        char quoteChar = 0;
        for (int i = 0; i < arrayContent.length(); i++) {
            char c = arrayContent.charAt(i);
            if (current == null) {
                if (c == '"' || c == '\'') {
                    quoteChar = c;
                    current = new StringBuilder();
                }
            } else if (c == quoteChar) {
                String entry = current.toString().trim();
                if (!entry.isEmpty()) {
                    result.add(entry);
                }
                current = null;
            } else {
                current.append(c);
            }
        }
        return result;
    }

    public static synchronized void load() {
        Path dir = getConfigDirectory();
        Path file = dir.resolve("composter_compat.toml");
        boolean needsSave = false;

        resetDefaults();

        if (Files.exists(file)) {
            try {
                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                boolean hasDebug = false;
                boolean hasRotten = false;
                boolean hasMeat = false;
                boolean hasAllowedRecipeTypes = false;
                String activeListKey = null;
                StringBuilder activeListBuffer = null;

                for (String rawLine : lines) {
                    String line = stripComment(rawLine).trim();
                    if (line.isEmpty()) {
                        continue;
                    }

                    if (activeListKey != null) {
                        activeListBuffer.append(' ').append(line);
                        int closeIdx = activeListBuffer.indexOf("]");
                        if (closeIdx != -1) {
                            String arrayBody = activeListBuffer.substring(0, closeIdx);
                            if ("allowed_recipe_types".equalsIgnoreCase(activeListKey)) {
                                allowedRecipeTypes = parseStringList(arrayBody);
                                allowedRecipeTypeSet = new LinkedHashSet<>(allowedRecipeTypes);
                                hasAllowedRecipeTypes = true;
                            }
                            activeListKey = null;
                            activeListBuffer = null;
                        }
                        continue;
                    }

                    if (line.startsWith("[")) {
                        continue;
                    }

                    int eqIdx = line.indexOf('=');
                    if (eqIdx != -1) {
                        String key = line.substring(0, eqIdx).trim();
                        String val = line.substring(eqIdx + 1).trim();

                        if (val.startsWith("[")) {
                            String rest = val.substring(1);
                            int closeIdx = rest.indexOf(']');
                            if (closeIdx != -1) {
                                if ("allowed_recipe_types".equalsIgnoreCase(key)) {
                                    allowedRecipeTypes = parseStringList(rest.substring(0, closeIdx));
                                    allowedRecipeTypeSet = new LinkedHashSet<>(allowedRecipeTypes);
                                    hasAllowedRecipeTypes = true;
                                }
                            } else {
                                activeListKey = key;
                                activeListBuffer = new StringBuilder(rest);
                            }
                            continue;
                        }

                        if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                            val = val.substring(1, val.length() - 1).trim();
                        }

                        if ("debug".equalsIgnoreCase(key)) {
                            Boolean parsed = parseStrictBoolean(val);
                            if (parsed != null) {
                                debug = parsed;
                                hasDebug = true;
                            }
                        } else if ("rotten_flesh_compostable".equalsIgnoreCase(key)) {
                            Boolean parsed = parseStrictBoolean(val);
                            if (parsed != null) {
                                rottenFleshCompostable = parsed;
                                hasRotten = true;
                            }
                        } else if ("meat_compostable".equalsIgnoreCase(key)) {
                            Boolean parsed = parseStrictBoolean(val);
                            if (parsed != null) {
                                meatCompostable = parsed;
                                hasMeat = true;
                            }
                        }
                    }
                }

                // Detect missing or malformed keys (e.g., upgrading from an older version of the mod)
                if (!hasDebug || !hasRotten || !hasMeat || !hasAllowedRecipeTypes || activeListKey != null) {
                    needsSave = true;
                }
            } catch (Exception e) {
                Constants.LOG.error("Failed to load composter_compat.toml: {}", e.getMessage());
                resetDefaults();
                needsSave = true;
            }
        } else {
            needsSave = true;
        }

        CommonClass.DEBUG = debug;

        if (needsSave) {
            save();
        }
    }

    public static synchronized void save() {
        Path dir = getConfigDirectory();
        Path file = dir.resolve("composter_compat.toml");
        Path tempPath = file.resolveSibling(file.getFileName() + ".tmp");

        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            List<String> lines = new ArrayList<>();
            lines.add("# Composter Compatibilities Configuration");
            lines.add("");
            lines.add("# Enable debug logging in the console");
            lines.add("debug = " + debug);
            lines.add("");
            lines.add("# Allow rotten flesh to be composted (65% chance)");
            lines.add("rotten_flesh_compostable = " + rottenFleshCompostable);
            lines.add("");
            lines.add("# Allow meat and meat-derived foods to be composted");
            lines.add("meat_compostable = " + meatCompostable);
            lines.add("");
            lines.add("# Recipe types allowed to propagate compostability from ingredients to result");
            if (allowedRecipeTypes.isEmpty()) {
                lines.add("allowed_recipe_types = []");
            } else {
                lines.add("allowed_recipe_types = [");
                for (int i = 0; i < allowedRecipeTypes.size(); i++) {
                    String comma = (i < allowedRecipeTypes.size() - 1) ? "," : "";
                    lines.add("    \"" + allowedRecipeTypes.get(i) + "\"" + comma);
                }
                lines.add("]");
            }
            lines.add("");

            Files.write(tempPath, lines, StandardCharsets.UTF_8);

            try {
                Files.move(tempPath, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempPath, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            Constants.LOG.error("Failed to save composter_compat.toml atomically: {}", e.getMessage());
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException ignored) {
            }
        }
    }

    public static Path getConfigDirectory() {
        Path configDir = null;

        // Try FabricLoader
        try {
            Class<?> fabricLoaderClass = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object instance = fabricLoaderClass.getMethod("getInstance").invoke(null);
            configDir = (Path) fabricLoaderClass.getMethod("getConfigDir").invoke(instance);
        } catch (Throwable ignored) {
        }

        // Try Forge / NeoForge (Legacy) FMLPaths
        if (configDir == null) {
            try {
                Class<?> fmlPathsClass = Class.forName("net.minecraftforge.fml.loading.FMLPaths");
                Object configDirPath = fmlPathsClass.getField("CONFIGDIR").get(null);
                configDir = (Path) configDirPath.getClass().getMethod("get").invoke(configDirPath);
            } catch (Throwable ignored) {
            }
        }

        // Try NeoForge (Modern) FMLPaths
        if (configDir == null) {
            try {
                Class<?> fmlPathsClass = Class.forName("net.neoforged.fml.loading.FMLPaths");
                Object configDirPath = fmlPathsClass.getField("CONFIGDIR").get(null);
                configDir = (Path) configDirPath.getClass().getMethod("get").invoke(configDirPath);
            } catch (Throwable ignored) {
            }
        }

        // Fallback to relative "config" directory
        if (configDir == null) {
            configDir = Paths.get("config");
        }

        return configDir;
    }
}
