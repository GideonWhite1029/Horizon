package dev.gideonwhite1029.horizon.protocol.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface HorizonProtocol {

    boolean isActive();

    default int tickerInterval(String tickerID) {
        return 1;
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Register {
        String namespace();
    }
}