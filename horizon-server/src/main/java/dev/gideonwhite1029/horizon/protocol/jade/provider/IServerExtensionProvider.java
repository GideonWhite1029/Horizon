package dev.gideonwhite1029.horizon.protocol.jade.provider;

import dev.gideonwhite1029.horizon.protocol.jade.accessor.Accessor;
import dev.gideonwhite1029.horizon.protocol.jade.util.ViewGroup;

import java.util.List;

public interface IServerExtensionProvider<T> extends IJadeProvider {
    List<ViewGroup<T>> getGroups(Accessor<?> request);
}