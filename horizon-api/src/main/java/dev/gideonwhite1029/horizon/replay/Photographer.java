package dev.gideonwhite1029.horizon.replay;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Represents a Photographer in the Horizon replay system
 */
public interface Photographer extends Player {

    /**
     * Gets the unique identifier of the Photographer.
     *
     * @return the unique identifier of the Photographer
     */
    @NotNull
    public String getId();

    /**
     * Sets the file where the recording will be saved.
     *
     * @param file the file to save the recording
     */
    public void setRecordFile(@NotNull File file);

    /**
     * Stops the recording.
     */
    public void stopRecording();

    /**
     * Stops the recording, optionally performing the operation asynchronously.
     *
     * @param async whether to stop the recording asynchronously
     */
    public void stopRecording(boolean async);

    /**
     * Stops the recording, optionally performing the operation asynchronously and saving the recording.
     *
     * @param async whether to stop the recording asynchronously
     * @param save whether to save the recording
     */
    public void stopRecording(boolean async, boolean save);

    /**
     * Pauses the recording.
     */
    public void pauseRecording();

    /**
     * Resumes the recording.
     */
    public void resumeRecording();

    /**
     * Sets the player to follow during the recording.
     *
     * @param player the player to follow, or null to stop following
     */
    public void setFollowPlayer(@Nullable Player player);
}
