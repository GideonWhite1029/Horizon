package dev.gideonwhite1029.horizon.protocol.rei.display;

import dev.gideonwhite1029.horizon.protocol.rei.ingredient.EntryIngredient;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class CraftingDisplay extends Display {

    public CraftingDisplay(@NotNull List<EntryIngredient> inputs,
                           @NotNull List<EntryIngredient> outputs,
                           @NotNull Identifier location) {
        super(inputs, outputs, location);
    }

    public abstract int getWidth();

    public abstract int getHeight();

}
