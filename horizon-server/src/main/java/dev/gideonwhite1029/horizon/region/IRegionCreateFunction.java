package dev.gideonwhite1029.horizon.region;

import java.io.IOException;

public interface IRegionCreateFunction {
    IRegionFile create(RegionCreatorInfo info) throws IOException;
}