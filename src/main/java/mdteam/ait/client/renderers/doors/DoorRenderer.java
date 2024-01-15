package mdteam.ait.client.renderers.doors;

import mdteam.ait.client.models.doors.DoomDoorModel;
import mdteam.ait.client.models.doors.DoorModel;
import mdteam.ait.client.registry.ClientDoorRegistry;
import mdteam.ait.client.registry.ClientExteriorVariantRegistry;
import mdteam.ait.client.registry.door.ClientDoorSchema;
import mdteam.ait.client.registry.exterior.ClientExteriorVariantSchema;
import mdteam.ait.client.renderers.AITRenderLayers;
import mdteam.ait.compat.DependencyChecker;
import mdteam.ait.core.blockentities.DoorBlockEntity;
import mdteam.ait.core.blocks.ExteriorBlock;
import mdteam.ait.tardis.TardisTravel;
import mdteam.ait.tardis.handler.DoorHandler;
import mdteam.ait.tardis.handler.properties.PropertiesHandler;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.DecoratedPotBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import mdteam.ait.tardis.TardisExterior;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Objects;

public class DoorRenderer<T extends DoorBlockEntity> implements BlockEntityRenderer<T> {

    private DoorModel model;

    public DoorRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.getClientTardis() == null) return;

        ClientExteriorVariantSchema exteriorVariant = ClientExteriorVariantRegistry.withParent(entity.getClientTardis().getExterior().getExteriorVariantSchema());
        if (exteriorVariant == null) return;
        ClientDoorSchema variant = ClientDoorRegistry.withParent(exteriorVariant.parent().door());
        if (variant == null) return;
        Class<? extends DoorModel> modelClass = variant.model().getClass();

        if (model != null && !(model.getClass().isInstance(modelClass)))
            model = null;

        if (model == null)
            this.model = variant.model();

        BlockState blockState = entity.getCachedState();
        float f = blockState.get(ExteriorBlock.FACING).asRotation();
        int maxLight = 0xFFFFFF;
        matrices.push();
        matrices.translate(0.5, 0, 0.5);
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f));
        Identifier texture = exteriorVariant.texture();
        Identifier emission = exteriorVariant.emission();

        if (exteriorVariant.equals(ClientExteriorVariantRegistry.DOOM)) {
            texture = entity.getClientTardis().getExterior().isDoorOpen() ? DoomDoorModel.DOOM_DOOR_OPEN : DoomDoorModel.DOOM_DOOR;
            emission = null;
        }
        int red = 1;
        int green = 1;
        int blue = 1;

        if (DependencyChecker.hasPortals() && entity.getClientTardis().getTravel().getState() == TardisTravel.State.LANDED && !entity.getClientTardis().isFalling() && entity.getClientTardis().getExterior().getDoorState() != DoorHandler.DoorStateEnum.CLOSED) {
            BlockPos pos = entity.getClientTardis().getExterior().getExteriorBlockPos();
            if (entity.getClientTardis().getLoadCache().__getExteriorBlockEntity() == null) return;
            World world = entity.getClientTardis().getLoadCache().__getExteriorBlockEntity().getWorld();
            if (world != null) {
                World doorWorld = entity.getWorld();
                BlockPos doorPos = entity.getPos();
                int lightConst = 524296; // 1 / maxLight;
                int i = world.getLightLevel(LightType.SKY, pos);
                int j = world.getLightLevel(LightType.BLOCK, pos);
                int k = Objects.requireNonNull(doorWorld).getLightLevel(LightType.BLOCK, doorPos);
                light = (i + j > 15 ? (15 * 2) + (j > 0 ? 0 : -5) : world.isNight() ? (i / 15) + j > 0 ? j + 13 : j : i + (world.getRegistryKey().equals(World.NETHER) ? j * 2 : j)) * lightConst;
            }
        }

        if (model != null) {
            if (!entity.getClientTardis().isInSiegeMode()) {
                model.animateTile(entity);
                model.renderWithAnimations(entity, this.model.getPart(), matrices, vertexConsumers.getBuffer(AITRenderLayers.getEntityTranslucentCull(texture)), light, overlay, 1, 1, 1 /*0.5f*/, 1);
                if (entity.getClientTardis().getExterior().isOvergrown()) {
                    model.renderWithAnimations(entity, this.model.getPart(), matrices, vertexConsumers.getBuffer(AITRenderLayers.getEntityTranslucentCull(entity.getClientTardis().getExterior().getOvergrownTexture())), light, overlay, 1, 1, 1, 1);
                }
                if (emission != null && entity.getClientTardis().isPowered()) {
                    boolean alarms = entity.getClientTardis().isAlarmsEnabled();
                    model.renderWithAnimations(entity, this.model.getPart(), matrices, vertexConsumers.getBuffer(AITRenderLayers.tardisRenderEmissionCull(emission, false)), light, overlay, 1, alarms ? 0.3f : 1 , alarms ? 0.3f : 1, 1);
                }
            }
        }
        matrices.pop();
    }
}
