package dev.gideonwhite1029.horizon.replay;

public class BukkitRecorderOption {

    // public int recordDistance = -1;
    public String serverName = "Horizon";
    public BukkitRecordWeather forceWeather = BukkitRecordWeather.NULL;
    public int forceDayTime = -1;
    public boolean ignoreChat = false;
    // public boolean ignoreItem = false;

    public enum BukkitRecordWeather {
        CLEAR,
        RAIN,
        THUNDER,
        NULL
    }
}