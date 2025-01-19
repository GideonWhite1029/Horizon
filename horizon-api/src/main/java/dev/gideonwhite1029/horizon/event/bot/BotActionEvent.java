package dev.gideonwhite1029.horizon.event.bot;

import dev.gideonwhite1029.horizon.entity.Bot;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class BotActionEvent extends BotEvent {

    private final String actionName;
    private final UUID actionUUID;

    public BotActionEvent(@NotNull Bot who, String actionName, UUID actionUUID) {
        super(who);
        this.actionName = actionName;
        this.actionUUID = actionUUID;
    }

    @NotNull
    public String getActionName() {
        return actionName;
    }

    public UUID getActionUUID() {
        return actionUUID;
    }
}