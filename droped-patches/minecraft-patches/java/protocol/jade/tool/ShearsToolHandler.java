package dev.gideonwhite1029.horizon.protocol.jade.tool;

import dev.gideonwhite1029.horizon.protocol.jade.JadeProtocol;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class ShearsToolHandler extends SimpleToolHandler {

    private static final ShearsToolHandler INSTANCE = new ShearsToolHandler();

    public static ShearsToolHandler getInstance() {
        return INSTANCE;
    }

    public ShearsToolHandler() {
        super(JadeProtocol.id("shears"), List.of(Items.SHEARS.getDefaultInstance()), true);
    }

    @Override
    public ItemStack test(BlockState state, Level world, BlockPos pos) {
        if (state.is(Blocks.TRIPWIRE)) {
            return tools.getFirst();
        }
        return super.test(state, world, pos);
    }
}
