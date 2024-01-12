package mdteam.ait.tardis.handler;

import mdteam.ait.api.tardis.TardisEvents;
import mdteam.ait.tardis.TardisTravel;
import mdteam.ait.tardis.handler.properties.PropertiesHandler;
import mdteam.ait.tardis.util.FlightUtil;
import mdteam.ait.tardis.util.TardisUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

import java.util.UUID;

public class FlightHandler extends TardisLink {
    private int flightTicks = 0;
    private int targetTicks = 0;

    public FlightHandler(UUID tardisId) {
        super(tardisId);

        // todo this doesnt seem to work.
        TardisEvents.LANDED.register((tardis -> {
            if (!tardis.equals(this.getTardis())) return;

            flightTicks = 0;
            targetTicks = 0;
        }));
        TardisEvents.DEMAT.register((tardis -> {
            if (!tardis.equals(this.getTardis())) return false;

            flightTicks = 0;
            targetTicks = FlightUtil.getFlightDuration(tardis.getTravel().getPosition(), tardis.getTravel().getDestination());

            return false;
        }));
    }

    private boolean isInFlight() {
        return this.getTardis().getTravel().getState().equals(TardisTravel.State.FLIGHT) || this.getTardis().getTravel().getState().equals(TardisTravel.State.MAT);
    }

    private boolean isFlightTicking() {
        return this.getTardis().getTravel().getState() == TardisTravel.State.FLIGHT && this.targetTicks != 0;
    }

    public boolean hasFinishedFlight() {
        return flightTicks >= targetTicks || targetTicks == 0 || getTardis().getTravel().isCrashing();
    }
    private void onFlightFinished() {
        this.flightTicks = 0;
        this.targetTicks = 0;
        FlightUtil.playSoundAtConsole(getTardis(), SoundEvents.BLOCK_BEACON_POWER_SELECT); // temp sound

        if (shouldAutoLand()) {
            this.getTardis().getTravel().materialise();
        }
    }
    private boolean shouldAutoLand() {
        return PropertiesHandler.willAutoPilot(getTardis().getHandlers().getProperties()) || !TardisUtil.isInteriorNotEmpty(this.getTardis()); // todo im not too sure if this second check should exist, but its so funny ( ghost monument reference )
    }

    public void increaseFlightTime(int ticks) {
        targetTicks = targetTicks + ticks;
    }

    public int getDurationAsPercentage() {
        if (this.targetTicks == 0 || this.flightTicks == 0) {
            if (this.getTardis().getTravel().getState() == TardisTravel.State.DEMAT) return 0;
            // if (this.tardis().getTravel().getState() == TardisTravel.State.MAT) return 100;
            return 100;
        }

        return FlightUtil.getDurationAsPercentage(this.flightTicks, this.targetTicks);
    }

    public void recalculate() {
        this.targetTicks = FlightUtil.getFlightDuration(getTardis().position(), getTardis().destination());
        this.flightTicks = this.isInFlight() ? MathHelper.clamp(this.flightTicks, 0, this.targetTicks) : 0;
    }

    @Override
    public void tick(MinecraftServer server) {
        super.tick(server);

        if ((this.targetTicks > 0 || this.flightTicks > 0) && this.getTardis().getTravel().getState() == TardisTravel.State.LANDED) {
            this.recalculate();
        }

        if (this.isInFlight() && !this.getTardis().getTravel().isCrashing() && !(this.flightTicks >= this.targetTicks) && this.targetTicks == 0) {
            this.recalculate();
        }

        if (this.isFlightTicking()) {
            if (this.hasFinishedFlight()) {
                this.onFlightFinished();
            }

            this.flightTicks = this.flightTicks + this.getTardis().getTravel().getSpeed();
        }
    }
}
