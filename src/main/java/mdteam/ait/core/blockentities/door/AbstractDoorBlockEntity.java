package mdteam.ait.core.blockentities.door;

import mdteam.ait.core.blocks.types.HorizontalDirectionalBlock;
import mdteam.ait.core.item.KeyItem;
import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.TardisDoor;
import mdteam.ait.tardis.TardisTravel;
import mdteam.ait.tardis.linkable.LinkableBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Objects;

public abstract class AbstractDoorBlockEntity extends LinkableBlockEntity {
    private TardisDoor door;
    public AbstractDoorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    public void useOn(World world, PlayerEntity player) {
        if(player == null)
            return;
        if (this.getTravel().getState() != TardisTravel.State.LANDED)
            return;
        if(player.getMainHandStack().getItem() instanceof KeyItem) {
            ItemStack key = player.getMainHandStack();
            NbtCompound tag = key.getOrCreateNbt();
            if(!tag.contains("tardis")) {
                return;
            }
            if(Objects.equals(this.getTardis().getUuid().toString(), tag.getUuid("tardis").toString())) {
                this.door.setLocked(!this.door.isLocked());
                String lockedState = this.door.isLocked() ? "\uD83D\uDD12" : "\uD83D\uDD13";
                player.sendMessage(Text.literal(lockedState).fillStyle(Style.EMPTY.withBold(true)), true);
            } else {
                world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), SoundCategory.BLOCKS, 1F, 0.2F);
                player.sendMessage(Text.literal("TARDIS does not identify with key"), true);
            }
            return;
        }
        if (!this.door.isLocked()) {
            this.door.nextOrClosed(this.getTardis().getExterior().getType().isDoubleDoor());
            this.sync();
            world.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 0.6f, 1f);
        } else {
            player.sendMessage(Text.literal("\uD83D\uDD12").fillStyle(Style.EMPTY.withBold(true)), true);
            world.playSound(null, pos, SoundEvents.BLOCK_CHAIN_STEP, SoundCategory.BLOCKS, 0.6F, 1F);
        }
    }
    public Direction getFacing() {
        return this.getCachedState().get(HorizontalDirectionalBlock.FACING);
    }
    public void onEntityCollision(Entity entity) {
        if (!(entity instanceof ServerPlayerEntity player))
            return;

        if (this.door.getState() != TardisDoor.State.CLOSED) {
            this.teleport(player);
        }
    }
    protected abstract void teleport(Entity entity);
    @Override
    public void setTardis(Tardis tardis) {
        super.setTardis(tardis);
        this.linkDoor();
    }
    @Override
    public void setDoor(TardisDoor door) {
        this.door = door;
    }
}
