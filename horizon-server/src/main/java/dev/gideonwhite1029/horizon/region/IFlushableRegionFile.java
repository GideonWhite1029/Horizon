package dev.gideonwhite1029.horizon.region;

import java.io.IOException;
import java.nio.file.Path;

public interface IFlushableRegionFile {
    boolean isMarkedToSave();
    long getLastWritten();
    boolean isClosedVolatile();
    boolean tryMarkFlushing();
    void syncIfNeeded() throws IOException;
    Path getPath();
}
