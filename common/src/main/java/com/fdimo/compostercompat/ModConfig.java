package com.fdimo.compostercompat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {

    public static boolean debug = false;
    public static boolean rottenFleshCompostable = false;
    public static boolean meatCompostable = false;

    static {
        load();
    }

    public static synchronized void load() {
        try {
            Path dir = getConfigDirectory();
            Path file = dir.resolve("composter_compat.toml");

            if (!Files.exists(file)) {
                save();
                return;
            }

            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            boolean hasDebug = false;
            boolean hasRotten = false;
            boolean hasMeat = false;

            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("[")) {
                    continue;
                }
                int eqIdx = line.indexOf('=');
                if (eqIdx != -1) {
                    String key = line.substring(0, eqIdx).trim();
                    String val = line.substring(eqIdx + 1).trim();
                    int commentIdx = val.indexOf('#');
                    if (commentIdx != -1) {
                        val = val.substring(0, commentIdx).trim();
                    }
                    if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                        val = val.substring(1, val.length() - 1).trim();
                    }

                    if ("debug".equalsIgnoreCase(key)) {
                        debug = Boolean.parseBoolean(val);
                        hasDebug = true;
                    } else if ("rotten_flesh_compostable".equalsIgnoreCase(key)) {
                        rottenFleshCompostable = Boolean.parseBoolean(val);
                        hasRotten = true;
                    } else if ("meat_compostable".equalsIgnoreCase(key)) {
                        meatCompostable = Boolean.parseBoolean(val);
                        hasMeat = true;
                    }
                }
            }

            CommonClass.DEBUG = debug;

            // If any key was missing from an existing file, rewrite to include it
            if (!hasDebug || !hasRotten || !hasMeat) {
                save();
            }
        } catch (Exception e) {
            Constants.LOG.error("Failed to load composter_compat.toml: {}", e.getMessage());
        }
    }

    public static synchronized void save() {
        try {
            Path dir = getConfigDirectory();
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path file = dir.resolve("composter_compat.toml");

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

            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Constants.LOG.error("Failed to save composter_compat.toml: {}", e.getMessage());
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
