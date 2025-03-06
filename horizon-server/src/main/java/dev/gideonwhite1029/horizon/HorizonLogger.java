package dev.gideonwhite1029.horizon;

import org.bukkit.Bukkit;

import java.util.logging.Level;
import java.util.logging.Logger;

public class HorizonLogger extends Logger {
    public static final HorizonLogger LOGGER = new HorizonLogger();

    private HorizonLogger() {
        super("Horizon", null);
        setParent(Bukkit.getLogger());
        setLevel(Level.ALL);
    }

    public void severe(String msg, Exception exception) {
        this.log(Level.SEVERE, msg, exception);
    }

    public void warning(String msg, Exception exception) {
        this.log(Level.WARNING, msg, exception);
    }

}