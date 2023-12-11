package mdteam.ait.tardis;

import mdteam.ait.AITMod;
import mdteam.ait.core.AITBlocks;
import mdteam.ait.core.AITSounds;
import mdteam.ait.core.blockentities.door.ExteriorBlockEntity;
import mdteam.ait.core.blocks.ExteriorBlock;
import mdteam.ait.core.entities.control.impl.pos.PosManager;
import mdteam.ait.core.sounds.MatSound;
import mdteam.ait.core.util.TardisUtil;
import mdteam.ait.core.util.data.AbsoluteBlockPos;
import mdteam.ait.tardis.travel.TravelContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TardisTravel extends AbstractTardisComponent {

    private State state;
    private AbsoluteBlockPos.Directed position;
    private AbsoluteBlockPos.Directed destination;
    private static final double FORCE_LAND_TIMER = 15;
    private static final double FORCE_FLIGHT_TIMER = 10;
    private static final int CHECK_LIMIT = 50; // todo, this should also be defined by the console, but the UPPER limit should be defined by config. - Loqor
    private static final boolean CHECK_DOWN = true; // todo, don't make this config, make this a setting on the console with the land type :/ - Loqor
    private PosManager posManager;
    private boolean shouldRemat = false;

    public TardisTravel(Tardis tardis, AbsoluteBlockPos.Directed pos) {
        super(tardis);

        this.position = pos;
        this.destination = this.getPosition();
        this.state = State.LANDED;
    }

    protected TardisTravel(Tardis tardis, AbsoluteBlockPos.Directed pos, AbsoluteBlockPos.Directed dest, State state) {
        super(tardis, false);

        this.position = pos;
        this.destination = dest;
        this.state = state;
    }

    @Override
    public void init() {
        this.runAnimations();
        this.placeExterior();
    }

    public void setPosition(AbsoluteBlockPos.Directed pos) {
        this.position = pos;
    }

    public AbsoluteBlockPos.Directed getPosition() {
        return position;
    }

    public AbsoluteBlockPos.Client getClientPosition() {
        return new AbsoluteBlockPos.Client(position, position.getDirection(), position.getWorld());
    }

    public static int getSoundLength(MatSound sound) {
        if (sound == null)
            return (int) FORCE_LAND_TIMER;
        return sound.timeLeft() / 20;
    }

    public boolean __subCheckForPos(BlockPos.Mutable mutable, int i) {
        BlockState state = this.getDestination().getWorld().getBlockState(mutable.setY(i));
        if (state.isReplaceable() && !this.getDestination().getWorld().getBlockState(mutable.down()).isReplaceable()) {
            AbsoluteBlockPos.Directed abpd = new AbsoluteBlockPos.Directed(mutable.setY(i),
                    this.getDestination().getWorld(), this.getDestination().getDirection());
            this.setDestination(abpd, false);
            return true;
        }
        return false;
    }

    //Yeah I know, I'm so cool :) - Loqor
    public void checkPositionAndMaterialise(boolean landType) {

        if(this.getDestination() == null)
            return;

        if(this.getDestination().getWorld().isClient())
            return;

        BlockPos.Mutable mutable = new BlockPos.Mutable(this.getDestination().getX(), this.getDestination().getY(), this.getDestination().getZ());
        if(landType) {
            for (int i = this.getDestination().getY(); i > this.getDestination().getWorld().getBottomY(); i--) {
                if (__subCheckForPos(mutable, i)) {
                    materialise();
                    return;
                }
            }
        } else {
            for (int i = this.getDestination().getY(); i < this.getDestination().getWorld().getBottomY(); i++) {
                if (__subCheckForPos(mutable, i)) {
                    materialise();
                    return;
                }
            }
        }
        if (this.getTardis() != null) {
            PlayerEntity player = TardisUtil.getPlayerInsideInterior(this.getTardis());
            if (player != null) {
                player.sendMessage(Text.literal("Position not viable for translocation: " + mutable.getX() + " | " + mutable.getY() + " | " + mutable.getZ()), true);
            }
        }
    }

    /**
     * Checks whether the destination is valid otherwise searches for a new one
     * @param limit how many times the search can happen (should stop hanging)
     * @param downwards whether to search downwards or upwards
     * @return whether its safe to land
     */
    private boolean checkDestination(int limit, boolean downwards) {
        World world = this.getDestination().getWorld();

        if(world.isClient()) return false;

        if (isDestinationTardisExterior()) { // fixme this portion of the code just deletes the other tardis' exterior!
            ExteriorBlockEntity target = (ExteriorBlockEntity) world.getBlockEntity(this.getDestination()); // safe

            if (target.getTardis() == null) return false;

            setDestinationToTardisInterior(target.getTardis(), false);
            return this.checkDestination(2,CHECK_DOWN); // limit at a small number cus it might get too laggy
        }

        BlockPos.Mutable temp = this.getDestination().mutableCopy(); // loqor told me mutables were better, is this true? fixme if not

        for (int i = 0; i < limit; i++) {
            if (world.getBlockState(temp).isAir() && world.getBlockState(temp.up()).isAir()) { // check two blocks cus tardis is two blocks tall yk
                this.setDestination(new AbsoluteBlockPos.Directed(temp, world, this.getDestination().getDirection()), false);
                return true;
            }

            if (downwards) temp = temp.down().mutableCopy();
            else temp = temp.up().mutableCopy();
        }

        return false;
    }

    private boolean isDestinationTardisExterior() {
        ServerWorld world = (ServerWorld) this.getDestination().getWorld();

        // bad code but its 4am i cba anymore
        if (world.getBlockEntity(this.getDestination()) instanceof ExteriorBlockEntity) {
            return true;
        }

        if (world.getBlockEntity(this.getDestination().down()) instanceof ExteriorBlockEntity) {
            this.setDestination(new AbsoluteBlockPos.Directed(this.getDestination().down(), world, this.getDestination().getDirection()),false);
            return true;
        }

        return false;
    }
    private void setDestinationToTardisInterior(Tardis target, boolean checks) {
        if (target == null) return; // i hate null shit

        this.setDestination(new AbsoluteBlockPos.Directed(
                        TardisUtil.offsetInteriorDoorPosition(target),
                        TardisUtil.getTardisDimension(),
                        this.getDestination().getDirection()),
                checks
        );
    }

    public void materialise() {
        if (this.getDestination() == null)
            return;

        if (this.getDestination().getWorld().isClient())
            return;

        if (!this.checkDestination(CHECK_LIMIT, CHECK_DOWN)) {
            // Not safe to land here!
            if(this.getTardis() != null) {
                ServerPlayerEntity player = (ServerPlayerEntity) TardisUtil.getPlayerInsideInterior(this.getTardis()); // may not necessarily be the person piloting the tardis, but todo this can be replaced with the player with the highest loyalty in future

                if (player == null) return; // Interior is probably empty

                player.sendMessage(Text.literal("Unable to land!")); // fixme translatable
                return;
            }
        }

        this.shouldRemat = false;
        this.setState(State.MAT);

        ServerWorld destWorld = (ServerWorld) this.getDestination().getWorld();
        destWorld.getChunk(this.getDestination());

        this.getDestination().getWorld().playSound(null, this.getDestination(), AITSounds.MAT, SoundCategory.BLOCKS,1f,1f);

        ExteriorBlock block = (ExteriorBlock) AITBlocks.EXTERIOR_BLOCK;
        BlockState state = block.getDefaultState().with(Properties.HORIZONTAL_FACING,this.getDestination().getDirection());
        destWorld.setBlockState(this.getDestination(), state);
        ExteriorBlockEntity blockEntity = new ExteriorBlockEntity(this.getDestination(), state);
        destWorld.addBlockEntity(blockEntity);
        this.setPosition(this.getDestination());

        this.runAnimations(blockEntity);
        // A definite thing just in case the animation isnt run

        Timer animTimer = new Timer();
        TardisTravel travel = this;
        Tardis tardis = this.getTardis();

        animTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (travel.getState() != State.MAT)
                    return;

                travel.setState(TardisTravel.State.LANDED);
                travel.setDestination(travel.getPosition(), true);
                /*travel.runAnimations(blockEntity);
                blockEntity.sync();*/
                if(tardis != null) tardis.getDoor().setLocked(tardis.getDoor().isLocked()); // force unlock door @todo should remember last locked state before takeoff
            }
        }, (long) getSoundLength(this.getMatSoundForCurrentState()) * 1000L);
    }

    public void dematerialise(boolean withRemat) {
        if (this.getPosition().getWorld().isClient())
            return;

        this.shouldRemat = withRemat;

        ServerWorld world = (ServerWorld) this.getPosition().getWorld();
        world.getChunk(this.getPosition());

        this.setState(State.DEMAT);

        world.playSound(null, this.getPosition(), AITSounds.DEMAT, SoundCategory.BLOCKS);

        this.runAnimations();

        // A definite thing just in case the animation isnt run

        Timer animTimer = new Timer();
        TardisTravel travel = this;

        animTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (travel.getState() != State.DEMAT)
                    return;

                travel.toFlight();
            }
        }, (long) getSoundLength(this.getMatSoundForCurrentState()) * 1000L);
    }

    public PosManager getPosManager() {
        if (this.posManager == null)
            this.posManager = new PosManager();

        return this.posManager;
    }


    public void toFlight() {
        this.setState(TardisTravel.State.FLIGHT);
        this.deleteExterior();
        this.checkShouldRemat();
    }
    public void runAnimations() {
        ServerWorld level = (ServerWorld) this.position.getWorld();
        level.getChunk(this.getPosition());
        BlockEntity entity = level.getBlockEntity(this.getPosition());
        if (entity instanceof ExteriorBlockEntity) {
            ((ExteriorBlockEntity) entity).getAnimation().setupAnimation(this.state);
        }
    }

    public void runAnimations(ExteriorBlockEntity exterior) {
        if(exterior.getAnimation() != null)
            exterior.getAnimation().setupAnimation(this.state);
    }

    public void setDestination(AbsoluteBlockPos.Directed pos, boolean withChecks) {
        this.destination = pos;
    }

    public AbsoluteBlockPos.Directed getDestination() {
        return destination;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void toggleHandbrake() {
        this.state.next(new TravelContext(this, this.position, this.destination));
    }

    public void placeExterior() {
        this.position.setBlockState(AITBlocks.EXTERIOR_BLOCK.getDefaultState());

        ExteriorBlockEntity exterior = new ExteriorBlockEntity(
                this.position, this.position.getBlockState()
        );

        // this is needed for the initialization. when we call #setTardis(ITardis) the travel field is still null.
        exterior.setTardis(this.tardis);
        this.position.addBlockEntity(exterior);
    }

    public void deleteExterior() {
        this.destination.getWorld().getChunk(this.getDestination());
        this.destination.getWorld().removeBlock(this.getPosition(),false);
    }

    public void checkShouldRemat() {
        if (!this.shouldRemat)
            return;

        this.materialise();
    }

    @NotNull
    public SoundEvent getSoundForCurrentState() {
        if(this.getTardis() != null)
            return this.getTardis().getExterior().getType().getSound(this.getState()).sound();
        return SoundEvents.INTENTIONALLY_EMPTY;
    }
    public MatSound getMatSoundForCurrentState() {
        if (this.getTardis() != null)
            return this.getTardis().getExterior().getType().getSound(this.getState());
        return AITSounds.LANDED_ANIM;
    }

    public enum State {
        LANDED(true) {
            @Override
            public void onEnable(TravelContext context) {
                AITMod.LOGGER.info("ON: LANDED");
            }

            @Override
            public void onDisable(TravelContext context) {
                AITMod.LOGGER.info("OFF: LANDED");
            }

            @Override
            public State getNext() {
                return DEMAT;
            }
        },
        DEMAT {
            @Override
            public void onEnable(TravelContext context) {
                AITMod.LOGGER.info("ON: DEMAT");

                context.travel().dematerialise(false);
            }

            @Override
            public void onDisable(TravelContext context) {
                AITMod.LOGGER.info("OFF: DEMAT");
            }

            @Override
            public long schedule(TravelContext context) {
                return 2000;
            }

            @Override
            public State getNext() {
                return FLIGHT;
            }
        },
        FLIGHT(true) {
            @Override
            public void onEnable(TravelContext context) {
                AITMod.LOGGER.info("ON: FLIGHT");
            }

            @Override
            public void onDisable(TravelContext context) {
                AITMod.LOGGER.info("OFF: LANDED");
            }

            @Override
            public State getNext() {
                return MAT;
            }
        },
        MAT {
            @Override
            public void onEnable(TravelContext context) {
                AITMod.LOGGER.info("ON: MAT");

                // context.travel().materialise();
            }

            @Override
            public void onDisable(TravelContext context) {
                AITMod.LOGGER.info("OFF: LANDED");
            }

            @Override
            public long schedule(TravelContext context) {
                return 2000;
            }

            @Override
            public State getNext() {
                return LANDED;
            }
        };

        private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

        private final boolean isStatic;

        State(boolean isStatic) {
            this.isStatic = isStatic;
        }

        State() {
            this(false);
        }

        public boolean isStatic() {
            return isStatic;
        }

        public ScheduledExecutorService getService() {
            return service;
        }

        public abstract void onEnable(TravelContext context);
        public abstract void onDisable(TravelContext context);
        public abstract State getNext();

        public void next(TravelContext context) {
            this.service.shutdown();
            this.onDisable(context);

            State next = this.getNext();
            next.schedule(context);

            next.onEnable(context);
            context.travel().setState(next);
        }

        public long schedule(TravelContext context) {
            return -1;
        }

        public void scheduleAndRun(TravelContext context) {
            long duration = this.schedule(context);

            if (duration < 0)
                throw new IllegalArgumentException("Schedule method was not implemented for non-static state " + this);

            this.service.schedule(() -> {
                if (this.isStatic)
                    return;

                this.next(context);
            }, duration, TimeUnit.MILLISECONDS);
        }
    }
}
