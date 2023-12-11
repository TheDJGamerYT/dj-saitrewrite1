package mdteam.ait.tardis;

import mdteam.ait.AITMod;
import mdteam.ait.core.blockentities.door.DoorBlockEntity;
import mdteam.ait.core.util.DesktopGenerator;
import mdteam.ait.core.util.TardisUtil;
import mdteam.ait.core.util.data.AbsoluteBlockPos;
import mdteam.ait.core.util.data.Corners;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class TardisDesktop extends AbstractTardisComponent {

    private TardisDesktopSchema schema;
    private final Corners corners;

    private AbsoluteBlockPos.Directed doorPos;
    private AbsoluteBlockPos.Directed consolePos;

    public TardisDesktop(Tardis tardis, TardisDesktopSchema schema) {
        this(tardis, schema, TardisUtil.findInteriorSpot());
    }

    @Override
    public void init() {
        BlockPos doorPos = new DesktopGenerator(schema).place(
                TardisUtil.getTardisDimension(), this.getCorners()
        );

        if (!(TardisUtil.getTardisDimension().getBlockEntity(doorPos) instanceof DoorBlockEntity door)) {
            AITMod.LOGGER.error("Failed to find the interior door!");
            return;
        }

        door.setTardis(this.getTardis());
    }

    protected TardisDesktop(Tardis tardis, TardisDesktopSchema schema, Corners corners) {
        super(tardis);

        this.setSchema(schema);
        this.corners = corners;
    }

    public TardisDesktopSchema getSchema() {
        return schema;
    }

    public void setSchema(TardisDesktopSchema schema) {
        this.schema = schema;
    }

    public AbsoluteBlockPos.Directed getInteriorDoorPos() {
        return doorPos;
    }

    public void setInteriorDoorPos(AbsoluteBlockPos.Directed pos) {
        this.doorPos = pos;
    }

    public AbsoluteBlockPos.Directed getConsolePos() {
        return consolePos;
    }

    public void setConsolePos(AbsoluteBlockPos.Directed pos) {
        this.consolePos = pos;
    }

    public Corners getCorners() {
        return corners;
    }

    public void updateDoor() {
        if (!(TardisUtil.getTardisDimension().getBlockEntity(doorPos) instanceof DoorBlockEntity door)) {
            AITMod.LOGGER.error("Failed to find the interior door!");
            return;
        }
        // this is needed for door and console initialization. when we call #setTardis(ITardis) the desktop field is still null.
        door.setDesktop(this);
        door.setTardis(this.getTardis());
    }
    public void changeInterior(TardisDesktopSchema schema) {
        this.setSchema(schema);
        DesktopGenerator generator = new DesktopGenerator(this.getSchema());
        DesktopGenerator.clearArea(TardisUtil.getTardisDimension(), this.getCorners());
        BlockPos doorPos = generator.place(TardisUtil.getTardisDimension(), this.getCorners());
        this.setInteriorDoorPos(new AbsoluteBlockPos.Directed(doorPos, TardisUtil.getTardisDimension(), Direction.SOUTH));
        this.updateDoor();
    }
}
