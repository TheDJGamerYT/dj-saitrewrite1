package mdteam.ait.client.util;

import mdteam.ait.core.AITDimensions;
import mdteam.ait.tardis.util.TardisUtil;
import mdteam.ait.tardis.wrapper.client.ClientTardis;
import mdteam.ait.tardis.wrapper.client.manager.ClientTardisManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class ClientTardisUtil {
    public static final int MAX_POWER_DELTA_TICKS = 3 * 20;
    public static final int MAX_ALARM_DELTA_TICKS = 60;
    private static int alarmDeltaTick;
    private static boolean alarmDeltaDirection; // true for increasing false for decreasing
    private static int powerDeltaTick;

    public static boolean isPlayerInATardis() {
        return MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().world.getRegistryKey() == AITDimensions.TARDIS_DIM_WORLD;
    }

    public static ClientTardis getCurrentClientTardis() {
        if (!isPlayerInATardis()) return null;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return null;
        return findClientTardisByInterior(player.getBlockPos());

    }

    public static ClientTardis findClientTardisByInterior(BlockPos pos) {
        for (Map.Entry<UUID, Supplier<ClientTardis>> entry : ClientTardisManager.getInstance().LOOKUP.entrySet()) {
            ClientTardis tardis = entry.getValue().get();
            if (TardisUtil.inBox(tardis.getDesktop().getCorners(), pos)) return tardis;
        }
        return null;
    }

    public static double distanceFromConsole() {
        if (!isPlayerInATardis()) return 0;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return 0;

        ClientTardis tardis = getCurrentClientTardis();
        if (tardis == null) return 0;
        BlockPos console = tardis.getDesktop().getConsolePos();
        BlockPos pos = player.getBlockPos();

        if (console == null) console = pos;

        return Math.sqrt(pos.getSquaredDistance(console));
    }

    public static void tickPowerDelta() {
        if (!isPlayerInATardis()) return;
        ClientTardis clientTardis = getCurrentClientTardis();
        if (clientTardis == null) return;
        if (clientTardis.isPowered() && getPowerDelta() < MAX_POWER_DELTA_TICKS) {
            setPowerDelta(getPowerDelta() + 1);
        }
        else if (!clientTardis.isPowered() && getPowerDelta() > 0) {
            setPowerDelta(getPowerDelta() - 1);
        }
    }
    public static int getPowerDelta() {
        if (!isPlayerInATardis()) return 0;
        return powerDeltaTick;
    }
    public static float getPowerDeltaForLerp() {
        return (float) getPowerDelta() / MAX_POWER_DELTA_TICKS;
    }
    public static void setPowerDelta(int delta) {
        if (!isPlayerInATardis()) return;
        powerDeltaTick = delta;
    }

    public static void tickAlarmDelta() {
        if (!isPlayerInATardis()) return;
        ClientTardis clientTardis = getCurrentClientTardis();
        if (clientTardis == null) return;
        if (!clientTardis.isAlarmsEnabled()) {
            if (getAlarmDelta() != MAX_ALARM_DELTA_TICKS)
                setAlarmDelta(getAlarmDelta() + 1);
            return;
        }

        if (getAlarmDelta() < MAX_ALARM_DELTA_TICKS && alarmDeltaDirection) {
            setAlarmDelta(getAlarmDelta() + 1);
        }
        else if (getAlarmDelta() > 0 && !alarmDeltaDirection) {
            setAlarmDelta(getAlarmDelta() - 1);
        }

        if (getAlarmDelta() >= MAX_ALARM_DELTA_TICKS) {
            alarmDeltaDirection = false;
        }
        if (getAlarmDelta() == 0) {
            alarmDeltaDirection = true;
        }
    }
    public static int getAlarmDelta() {
        if (!isPlayerInATardis()) return 0;
        return alarmDeltaTick;
    }
    public static float getAlarmDeltaForLerp() {
        return (float) getAlarmDelta() / MAX_ALARM_DELTA_TICKS;
    }
    public static void setAlarmDelta(int delta) {
        if (!isPlayerInATardis()) return;
        alarmDeltaTick = delta;
    }
}
