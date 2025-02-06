package dev.gideonwhite1029.horizon.replay;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public interface PhotographerManager {
    /**
     * Retrieves a Photographer by their UUID.
     *
     * @param uuid the UUID of the Photographer
     * @return the Photographer instance, or null if not found
     */
    @Nullable
    public Photographer getPhotographer(@NotNull UUID uuid);

    /**
     * Retrieves a Photographer by their ID.
     *
     * @param id the ID of the Photographer
     * @return the Photographer instance, or null if not found
     */
    @Nullable
    public Photographer getPhotographer(@NotNull String id);

    /**
     * Creates a new Photographer at the specified location.
     *
     * @param id the ID of the new Photographer
     * @param location the location of the new Photographer
     * @return the created Photographer instance, or null if creation failed
     */
    @Nullable
    public Photographer createPhotographer(@NotNull String id, @NotNull Location location);

    /**
     * Creates a new Photographer at the specified location with the given recorder options.
     *
     * @param id the ID of the new Photographer
     * @param location the location of the new Photographer
     * @param recorderOption the recorder options for the new Photographer
     * @return the created Photographer instance, or null if creation failed
     */
    @Nullable
    public Photographer createPhotographer(@NotNull String id, @NotNull Location location, @NotNull BukkitRecorderOption recorderOption);

    /**
     * Removes a Photographer by their ID.
     *
     * @param id the ID of the Photographer to remove
     */
    public void removePhotographer(@NotNull String id);

    /**
     * Removes a Photographer by their UUID.
     *
     * @param uuid the UUID of the Photographer to remove
     */
    public void removePhotographer(@NotNull UUID uuid);

    /**
     * Removes all Photographers.
     */
    public void removeAllPhotographers();

    /**
     * Retrieves a collection of all Photographers.
     *
     * @return a collection of all Photographer instances
     */
    public Collection<Photographer> getPhotographers();
}
