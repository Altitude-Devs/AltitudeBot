package com.alttd.config;

import com.alttd.AltitudeBot;
import com.alttd.util.Logger;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@SuppressWarnings({"unused", "SameParameterValue"})
abstract class AbstractConfig {
    private static final Pattern PATH_PATTERN = Pattern.compile("\\.");
    private static final String HEADER = "";

    private YamlConfigurationLoader configLoader;
    private ConfigurationNode config;

    AbstractConfig(String filename) {
        init(new File(AltitudeBot.getInstance().getDataFolder(), filename), filename);
    }

    private void init(File file, String filename) {
        configLoader = YamlConfigurationLoader.builder()
                .file(file)
                .nodeStyle(NodeStyle.BLOCK)
                .build();
        if (!file.getParentFile().exists()) {
            if(!file.getParentFile().mkdirs()) {
                return;
            }
        }
        if (!file.exists()) {
            try {
                if(!file.createNewFile()) {
                    return;
                }
            } catch (IOException error) {
                error.printStackTrace();
            }
        }

        try {
            config = configLoader.load(ConfigurationOptions.defaults().header(HEADER).shouldCopyDefaults(false));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void readConfig(Class<?> clazz, Object instance) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPrivate(method.getModifiers())) {
                if (method.getParameterTypes().length == 0 && method.getReturnType() == Void.TYPE) {
                    try {
                        method.setAccessible(true);
                        method.invoke(instance);
                    } catch (InvocationTargetException ex) {
                        throw new RuntimeException(ex.getCause());
                    } catch (Exception ex) {
                        Logger.severe("Error invoking %.", method.toString());
                        ex.printStackTrace();
                    }
                }
            }
        }

        save();
    }

    private void save() {
        try {
            configLoader.save(config);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static Object[] splitPath(String key) {
        return PATH_PATTERN.split(key);
    }

    protected void set(String path, Object def) {
        if(config.node(splitPath(path)).virtual()) {
            try {
                config.node(splitPath(path)).set(def);
            } catch (SerializationException e) {
            }
        }
    }

    protected void setString(String path, String def) {
        try {
            if(config.node(splitPath(path)).virtual())
                config.node(splitPath(path)).set(io.leangen.geantyref.TypeToken.get(String.class), def);
        } catch(SerializationException ex) {
        }
    }

    protected boolean getBoolean(String path, boolean def) {
        set(path, def);
        return config.node(splitPath(path)).getBoolean(def);
    }

    protected double getDouble(String path, double def) {
        set(path, def);
        return config.node(splitPath(path)).getDouble(def);
    }

    protected int getInt(String path, int def) {
        set(path, def);
        return config.node(splitPath(path)).getInt(def);
    }

    protected String getString(String path, String def) {
        setString(path, def);
        return config.node(splitPath(path)).getString(def);
    }

    protected Long getLong(String path, Long def) {
        set(path, def);
        return config.node(splitPath(path)).getLong(def);
    }

    protected <T> List<String> getList(String path, T def) {
        try {
            set(path, def);
            return config.node(splitPath(path)).getList(TypeToken.get(String.class));
        } catch(SerializationException ex) {
        }
        return new ArrayList<>();
    }

    protected ConfigurationNode getNode(String path) {
        if(config.node(splitPath(path)).virtual()) {
            //new RegexConfig("Dummy");
        }
        config.childrenMap();
        return config.node(splitPath(path));
    }
}