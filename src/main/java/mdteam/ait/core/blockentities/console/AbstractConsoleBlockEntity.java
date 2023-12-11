package mdteam.ait.core.blockentities.console;

import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.TardisConsole;
import mdteam.ait.tardis.linkable.LinkableBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;

public class AbstractConsoleBlockEntity extends LinkableBlockEntity {

    private TardisConsole console;

    public AbstractConsoleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public TardisConsole getConsole() {
        return console;
    }

    @Override
    public void setTardis(Tardis tardis) {
        super.setTardis(tardis);
        this.linkConsole();
    }

    @Override
    public void setConsole(TardisConsole console) {
        this.console = console;
    }
}
