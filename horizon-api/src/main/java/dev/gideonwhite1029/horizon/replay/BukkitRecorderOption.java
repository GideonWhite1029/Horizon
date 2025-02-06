package dev.gideonwhite1029.horizon.replay;

/**
 * Represents the options for recording in the Bukkit environment.
 */
public class BukkitRecorderOption {

    // public int recordDistance = -1;

    /**
     * The name of the server.
     */
    public String serverName = "Horizon";

    /**
     * The weather condition to force during recording.
     */
    public BukkitRecordWeather forceWeather = BukkitRecordWeather.NULL;

    /**
     * The time of day to force during recording.
     */
    public int forceDayTime = -1;

    /**
     * Whether to ignore chat during recording.
     */
    public boolean ignoreChat = false;

    // public boolean ignoreItem = false;

    /**
     * Represents the possible weather conditions for recording.
     */
    public enum BukkitRecordWeather {
        CLEAR,
        RAIN,
        THUNDER,
        NULL
    }
}
