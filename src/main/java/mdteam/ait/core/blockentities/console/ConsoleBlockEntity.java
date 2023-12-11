package mdteam.ait.core.blockentities.console;

import mdteam.ait.client.renderers.consoles.ConsoleEnum;
import mdteam.ait.core.AITBlockEntityTypes;
import mdteam.ait.core.AITEntityTypes;
import mdteam.ait.core.blocks.types.HorizontalDirectionalBlock;
import mdteam.ait.core.entities.ConsoleControlEntity;
import mdteam.ait.core.util.TardisUtil;
import mdteam.ait.core.util.data.AbsoluteBlockPos;
import mdteam.ait.tardis.ControlTypes;
import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.TardisDesktop;
import mdteam.ait.tardis.TardisTravel;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConsoleBlockEntity extends AbstractConsoleBlockEntity {
    public final AnimationState ANIM_FLIGHT = new AnimationState();
    public int animationTimer = 0;
    public final List<ConsoleControlEntity> controlEntities = new ArrayList<>();
    private boolean markedDirty = true;

    public ConsoleBlockEntity(BlockPos pos, BlockState state) {
        super(AITBlockEntityTypes.DISPLAY_CONSOLE_BLOCK_ENTITY_TYPE, pos, state);

        this.setTardis(TardisUtil.findTardisByInterior(pos));
    }

    @Override
    public void setTardis(Tardis tardis) {
        super.setTardis(tardis);
        this.linkDesktop();
    }

    public ConsoleEnum getConsoleType() {
        if(this.getTardis() == null)
            return ConsoleEnum.TEMP;
        return this.getTardis().getConsole().getType();
    }

    @Override
    public void setDesktop(TardisDesktop desktop) {
        desktop.setConsolePos(new AbsoluteBlockPos.Directed(
                this.getPos(), TardisUtil.getTardisDimension(), this.getCachedState().get(HorizontalDirectionalBlock.FACING))
        );
    }

    public void checkAnimations() {
        // DO NOT RUN THIS ON SERVER!!

        animationTimer++;
        if (this.getTardis() == null)
            return;
        TardisTravel.State state = this.getTardis().getTravel().getState();

        if (!ANIM_FLIGHT.isRunning()) {
            ANIM_FLIGHT.start(animationTimer);
        }
    }
    private void stopAllAnimations() {
        // DO NOT RUN ON SERVER
        ANIM_FLIGHT.stop();
    }

    public void killControls() {
        controlEntities.forEach(Entity::discard);
        controlEntities.clear();
    }

    public void spawnControls() {

        BlockPos current = getPos();

        if(!(getWorld() instanceof ServerWorld server))
            return;

        killControls();
        ConsoleEnum consoleType = this.getConsoleType();
        ControlTypes[] controls = consoleType.getControlTypesList();
        Arrays.stream(controls).toList().forEach(control -> {

            ConsoleControlEntity controlEntity = new ConsoleControlEntity(AITEntityTypes.CONTROL_ENTITY_TYPE, getWorld());

            Vector3f position = current.toCenterPos().toVector3f().add(control.getOffset().x(), control.getOffset().y(), control.getOffset().z());
            controlEntity.setPosition(position.x(), position.y(), position.z());
            controlEntity.setYaw(0);
            controlEntity.setPitch(0);

            controlEntity.setControlData(consoleType, control, this.getPos());

            server.spawnEntity(controlEntity);
            this.controlEntities.add(controlEntity);
        });

        this.markedDirty = false;
    }
    public void markDirty() {
        this.markedDirty = true;
    }

    public static <T extends BlockEntity> void tick(T t) {
        if (!(t instanceof ConsoleBlockEntity console))
            return;

        if(console.markedDirty) {
            console.spawnControls();
        }

        if(console.getWorld() == null)
            return;

        if (console.getWorld().isClient())
            console.checkAnimations();
    }

}
