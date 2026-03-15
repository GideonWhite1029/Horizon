package dev.gideonwhite1029.horizon.commands;

import dev.gideonwhite1029.horizon.HorizonConfig;
import dev.gideonwhite1029.horizon.HorizonLogger;
import dev.gideonwhite1029.horizon.config.ConfigConvert;
import dev.gideonwhite1029.horizon.config.ConfigVerify;
import dev.gideonwhite1029.horizon.config.GlobalConfig;
import dev.gideonwhite1029.horizon.config.RemovedConfig;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class GlobalConfigManager {

    private static boolean firstLoad = true;
    private static final Map<String, VerifiedConfig> verifiedConfigs = new HashMap<>();

    public static void init() {
        verifiedConfigs.clear();

        Class<HorizonConfig> clazz = HorizonConfig.class;
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);

                RemovedConfig removedConfig = field.getAnnotation(RemovedConfig.class);
                if (removedConfig != null) {
                    RemovedVerifiedConfig verifiedConfig = RemovedVerifiedConfig.build(removedConfig, field);
                    verifiedConfig.run();
                }

                GlobalConfig globalConfig = field.getAnnotation(GlobalConfig.class);
                if (globalConfig != null) {
                    try {
                        VerifiedConfig verifiedConfig = VerifiedConfig.build(globalConfig, field);

                        if (globalConfig.lock() && !firstLoad) {
                            verifiedConfigs.put(verifiedConfig.path.substring("settings.".length()), verifiedConfig);
                            continue;
                        }

                        ConfigVerify<? super Object> verify = verifiedConfig.verify;
                        boolean isEnumConfig = verify instanceof ConfigVerify.EnumConfigVerify;

                        Object defValue = isEnumConfig ? field.get(null).toString() : field.get(null);
                        HorizonConfig.config.addDefault(verifiedConfig.path, defValue);

                        List<String> commentLines = buildCommentLines(globalConfig.comment());
                        HorizonConfig.config.setComments(verifiedConfig.path,
                                commentLines.isEmpty() ? null : commentLines);

                        try {
                            Object savedValue = HorizonConfig.config.get(verifiedConfig.path);
                            if (isEnumConfig) {
                                savedValue = verify.convert(savedValue.toString());
                            }
                            String checkInfo = verify.check(null, savedValue);

                            if (checkInfo == null) {
                                field.set(null, savedValue);
                            } else {
                                throw new IllegalArgumentException(checkInfo);
                            }
                        } catch (IllegalArgumentException | ClassCastException e) {
                            HorizonConfig.config.set(verifiedConfig.path, defValue);
                            HorizonLogger.LOGGER.warning(e.getMessage() + ", reset to " + defValue);
                        }

                        verifiedConfigs.put(verifiedConfig.path.substring("settings.".length()), verifiedConfig);
                    } catch (Exception e) {
                        Bukkit.getLogger().log(Level.SEVERE, "Failure to load leaves config", e);
                    }
                }
            }
        }

        verifiedConfigs.forEach((path, config) -> config.verify.runAfterLoader(config.get()));

        firstLoad = false;
        HorizonConfig.save();
    }

    public static List<String> buildCommentLines(String[] rawComments) {
        if (rawComments.length == 0) return Collections.emptyList();
        List<String> lines = new ArrayList<>();
        for (String raw : rawComments) {
            String[] parts = raw.split("\n", -1);
            Collections.addAll(lines, parts);
        }
        return lines;
    }

    public static VerifiedConfig getVerifiedConfig(String path) {
        return verifiedConfigs.get(path);
    }

    public static Set<String> getVerifiedConfigPaths() {
        return verifiedConfigs.keySet();
    }

    public record RemovedVerifiedConfig(RemovedConfig config, ConfigConvert<? super Object> convert, Field field,
                                        String path) {

        public void run() {
            if (config.transform()) {
                if (HorizonConfig.config.contains(path)) {
                    String string = HorizonConfig.config.get(path).toString();
                    try {
                        Object object = convert.convert(string);
                        field.set(null, object);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            HorizonConfig.config.set(path, null);
        }

        public static RemovedVerifiedConfig build(RemovedConfig config, Field field) {
            StringBuilder path = new StringBuilder("settings.");
            for (int i = 0; i < config.category().length; i++) {
                path.append(config.category()[i]).append(".");
            }
            path.append(config.name());

            ConfigConvert configConvert = null;
            try {
                Constructor<? extends ConfigConvert<?>> constructor = config.convert().getDeclaredConstructor();
                constructor.setAccessible(true);
                configConvert = constructor.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new RemovedVerifiedConfig(config, configConvert, field, path.toString());
        }
    }

    public record VerifiedConfig(GlobalConfig config, ConfigVerify<? super Object> verify, Field field, String path) {

        public void set(String realValue) throws IllegalArgumentException {
            if (config.lock()) {
                throw new IllegalArgumentException("locked");
            }

            Object value;
            try {
                value = verify.convert(realValue);
            } catch (Exception e) {
                throw new IllegalArgumentException("value parse error: " + e.getMessage());
            }

            String checkInfo = verify.check(this.get(), value);
            if (checkInfo != null) {
                throw new IllegalArgumentException(checkInfo);
            }

            try {
                field.set(null, value);
                HorizonConfig.config.set(path, verify instanceof ConfigVerify.EnumConfigVerify ? realValue.toUpperCase(Locale.ROOT) : value);
                HorizonConfig.save();
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("?");
            }
        }

        public String getString() {
            return this.get().toString();
        }

        public Object get() {
            try {
                return field.get(null);
            } catch (IllegalAccessException e) {
                HorizonLogger.LOGGER.log(Level.SEVERE, "Failure to get " + path + " value", e);
                return "<VALUE ERROR>";
            }
        }

        public static VerifiedConfig build(GlobalConfig config, Field field) {
            StringBuilder path = new StringBuilder("settings.");
            for (int i = 0; i < config.category().length; i++) {
                path.append(config.category()[i]).append(".");
            }
            path.append(config.name());

            ConfigVerify configVerify = null;
            try {
                Constructor<? extends ConfigVerify<?>> constructor = config.verify().getDeclaredConstructor();
                constructor.setAccessible(true);
                configVerify = constructor.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }


            return new VerifiedConfig(config, configVerify, field, path.toString());
        }
    }
}
