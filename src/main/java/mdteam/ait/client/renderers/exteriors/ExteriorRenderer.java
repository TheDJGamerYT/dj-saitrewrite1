package mdteam.ait.client.renderers.exteriors;

import mdteam.ait.AITMod;
import mdteam.ait.client.models.exteriors.ExteriorModel;
import mdteam.ait.client.models.exteriors.SiegeModeModel;
import mdteam.ait.client.registry.ClientExteriorVariantRegistry;
import mdteam.ait.client.registry.exterior.ClientExteriorVariantSchema;
import mdteam.ait.client.renderers.AITRenderLayers;
import mdteam.ait.core.blockentities.ExteriorBlockEntity;
import mdteam.ait.core.blocks.ExteriorBlock;
import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.util.TardisUtil;
import mdteam.ait.tardis.wrapper.client.ClientTardis;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class ExteriorRenderer<T extends ExteriorBlockEntity> implements BlockEntityRenderer<T> {
    private ExteriorModel model;
    private SiegeModeModel siege;
    private final EntityRenderDispatcher dispatcher;

    public static final Identifier DOOM_FRONT_BACK = new Identifier(AITMod.MOD_ID, "textures/blockentities/exteriors/doom/doom_front_back.png");
    public static final Identifier DOOM_LEFT_SIDE = new Identifier(AITMod.MOD_ID, "textures/blockentities/exteriors/doom/doom_left_side.png");
    public static final Identifier DOOM_RIGHT_SIDE = new Identifier(AITMod.MOD_ID, "textures/blockentities/exteriors/doom/doom_right_side.png");
    public static final Identifier DOOM_LEFT_DIAGONAL = new Identifier(AITMod.MOD_ID, "textures/blockentities/exteriors/doom/doom_left_diagonal.png");
    public static final Identifier DOOM_RIGHT_DIAGONAL = new Identifier(AITMod.MOD_ID, "textures/blockentities/exteriors/doom/doom_right_diagonal.png");
    public static final Identifier DOOM_BLANK_DIAGONAL = new Identifier(AITMod.MOD_ID, "textures/blockentities/exteriors/doom/doom_blank_diagonal.png");
    public static final Identifier DOOM_TEXTURE_EMISSION = new Identifier(AITMod.MOD_ID, "textures/blockentities/exteriors/doom/doom_emission.png");
    public static final Identifier DOOM_LEFT_SIDE_EMISSION = new Identifier(AITMod.MOD_ID, "textures/blockentities/exteriors/doom/doom_left_side_emission.png");
    public static final Identifier DOOM_RIGHT_SIDE_EMISSION = new Identifier(AITMod.MOD_ID, "textures/blockentities/exteriors/doom/doom_right_side_emission.png");
    public static final Identifier DOOM_DIAGONAL_EMISSION = new Identifier(AITMod.MOD_ID, "textures/blockentities/exteriors/doom/doom_diagonal_emission.png");
    public static final Identifier DOOM_LEFT_DIAGONAL_OPEN = new Identifier(AITMod.MOD_ID, "textures/blockentities/exteriors/doom/doom_left_diagonal_open.png");
    public static final Identifier DOOM_RIGHT_DIAGONAL_OPEN = new Identifier(AITMod.MOD_ID, "textures/blockentities/exteriors/doom/doom_right_diagonal_open.png");
    public static final Identifier DOOM_LEFT_SIDE_OPEN = new Identifier(AITMod.MOD_ID, "textures/blockentities/exteriors/doom/doom_left_side_open.png");
    public static final Identifier DOOM_RIGHT_SIDE_OPEN = new Identifier(AITMod.MOD_ID, "textures/blockentities/exteriors/doom/doom_right_side_open.png");
    public static final Identifier DOOM_FRONT_BACK_OPEN = new Identifier(AITMod.MOD_ID, "textures/blockentities/exteriors/doom/doom_front_back_open.png");
    public static final Identifier DOOM_LEFT_DIAGONAL_OPEN_EMISSION = new Identifier(AITMod.MOD_ID, "textures/blockentities/exteriors/doom/doom_left_diagonal_open_emission.png");
    public static final Identifier DOOM_RIGHT_DIAGONAL_OPEN_EMISSION = new Identifier(AITMod.MOD_ID, "textures/blockentities/exteriors/doom/doom_right_diagonal_open_emission.png");
    public static final Identifier DOOM_FRONT_BACK_OPEN_EMISSION = new Identifier(AITMod.MOD_ID, "textures/blockentities/exteriors/doom/doom_front_back_open_emission.png");

    public ExteriorRenderer(BlockEntityRendererFactory.Context ctx) {
        this.dispatcher = ctx.getEntityRenderDispatcher();
    }

    @Override
    public void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.getClientTardis() == null) {
            return;
        }

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        ClientExteriorVariantSchema exteriorVariant = ClientExteriorVariantRegistry.withParent(TardisUtil.isClient() ? entity.getClientTardis().getExterior().getExteriorVariantSchema() : entity.getTardis().getExterior().getVariant());
        Class<? extends ExteriorModel> modelClass = exteriorVariant.model().getClass();

        if (model != null && !(model.getClass().isInstance(modelClass))) // fixme this is bad it seems to constantly create a new one anyway but i didnt realise.
            model = null;

        if (model == null)
            this.model = exteriorVariant.model();

        BlockState blockState = entity.getCachedState();
        float f = blockState.get(ExteriorBlock.FACING).asRotation();
        int maxLight = 0xF000F0;
        matrices.push();
        matrices.translate(0.5, 0, 0.5);

        // Doom Custom Stuff, might move to a different class or a subclass

//        if(MinecraftClient.getInstance().player == null) {
//            matrices.pop();
//            return;
//        }

        Identifier texture = exteriorVariant.texture();
        Identifier emission = exteriorVariant.emission();
//        Direction direction = TardisUtil.isClient() ? entity.getClientTardis().getTravel().getPosition().getDirection() : entity.getTardis().getTravel().getPosition().getDirection();
//        float wrappedDegrees = MathHelper.wrapDegrees(MinecraftClient.getInstance().player.getHeadYaw() +
//                (direction == Direction.NORTH ||
//                        direction == Direction.SOUTH ? f + 180f : f));
//
//        if(exteriorVariant.equals(ClientExteriorVariantRegistry.DOOM)) {
//            if (TardisUtil.isClient()) {
//                texture = getTextureForRotationClient(wrappedDegrees, entity.getClientTardis());
//                emission = getEmissionForRotationClient(texture, entity.getClientTardis());
//            } else {
//                texture = getTextureForRotationServer(wrappedDegrees, entity.getTardis());
//                emission = getEmissionForRotationServer(texture, entity.getTardis());
//            }
//        }
//
//        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(!exteriorVariant.equals(ClientExteriorVariantRegistry.DOOM) ? f :
//                MinecraftClient.getInstance().player.getHeadYaw() + ((wrappedDegrees > -135 && wrappedDegrees < 135) ? 180f : 0f)));

        //System.out.println(wrappedDegrees);

        // -------------------------------------------------------------------------------------------------------------------

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f));

        if (entity.getClientTardis().isInSiegeMode()) {
            if (siege == null) siege = new SiegeModeModel(SiegeModeModel.getTexturedModelData().createModel());
            siege.renderWithAnimations(entity, this.siege.getPart(), matrices, vertexConsumers.getBuffer(AITRenderLayers.getEntityTranslucentCull(SiegeModeModel.TEXTURE)), maxLight, overlay, 1, 1, 1, 1);
        }

        if (model != null) {
            model.animateTile(entity);
            model.renderWithAnimations(entity, this.model.getPart(), matrices, vertexConsumers.getBuffer(AITRenderLayers.getEntityTranslucentCull(texture)), light, overlay, 1, 1, 1, 1);
            if (entity.getClientTardis() == null) {
                return; // WHY IS THIS NULL HERE, BUT NOT AT THE BEGINNING OF THIS FUCKING FUNCTION THREAD
            }
            if (TardisUtil.isClient() ? entity.getClientTardis().getExterior().isOvergrown() : entity.getTardis().getHandlers().getOvergrownHandler().isOvergrown()) {
                Identifier overgrown_texture = TardisUtil.isClient() ? entity.getClientTardis().getExterior().getOvergrownTexture() : entity.getTardis().getHandlers().getOvergrownHandler().getOvergrownTexture();
                model.renderWithAnimations(entity, this.model.getPart(), matrices, vertexConsumers.getBuffer(AITRenderLayers.getEntityTranslucentCull(overgrown_texture)), light, overlay, 1, 1, 1, 1);
            }
            if (emission != null && (TardisUtil.isClient() ? entity.getClientTardis().isPowered() : entity.getTardis().hasPower())) {
                boolean alarms = TardisUtil.isClient() ? entity.getClientTardis().isAlarmsEnabled() : entity.getTardis().getHandlers().getAlarms().isEnabled();
                model.renderWithAnimations(entity, this.model.getPart(), matrices, vertexConsumers.getBuffer(AITRenderLayers.tardisRenderEmissionCull(emission, false)), maxLight, overlay, 1, alarms ? 0.3f : 1 , alarms ? 0.3f : 1, 1);
            }
        }
        matrices.pop();
    }

    @Override
    public boolean rendersOutsideBoundingBox(T blockEntity) {
        return true;
    }

    public Identifier getTextureForRotationClient(float rotation, ClientTardis tardis) {
        boolean bl = tardis.getExterior().isDoorOpen();
        if (rotation > 70 && rotation < 110) {
            return bl ? DOOM_RIGHT_SIDE_OPEN : DOOM_RIGHT_SIDE;
        } else if (rotation < -90 && rotation > -110) {
            return bl ? DOOM_LEFT_SIDE_OPEN : DOOM_LEFT_SIDE;
        }else if (rotation > 25 && rotation < 70) {
            return bl ? DOOM_RIGHT_DIAGONAL_OPEN : DOOM_RIGHT_DIAGONAL;
        } else if (rotation < -25 && rotation > -90 ) {
            return bl ? DOOM_LEFT_DIAGONAL_OPEN : DOOM_LEFT_DIAGONAL;
        } else if (rotation > 110 && rotation < 155 || rotation < -110 && rotation > -155) {
            return DOOM_BLANK_DIAGONAL;
        } else {
            return bl ? DOOM_FRONT_BACK_OPEN : DOOM_FRONT_BACK;
        }
    }

    public Identifier getTextureForRotationServer(float rotation, Tardis tardis) {
        boolean bl = tardis.getDoor().isOpen();
        if (rotation > 70 && rotation < 110) {
            return bl ? DOOM_RIGHT_SIDE_OPEN : DOOM_RIGHT_SIDE;
        } else if (rotation < -90 && rotation > -110) {
            return bl ? DOOM_LEFT_SIDE_OPEN : DOOM_LEFT_SIDE;
        }else if (rotation > 25 && rotation < 70) {
            return bl ? DOOM_RIGHT_DIAGONAL_OPEN : DOOM_RIGHT_DIAGONAL;
        } else if (rotation < -25 && rotation > -90 ) {
            return bl ? DOOM_LEFT_DIAGONAL_OPEN : DOOM_LEFT_DIAGONAL;
        } else if (rotation > 110 && rotation < 155 || rotation < -110 && rotation > -155) {
            return DOOM_BLANK_DIAGONAL;
        } else {
            return bl ? DOOM_FRONT_BACK_OPEN : DOOM_FRONT_BACK;
        }
    }

    public Identifier getEmissionForRotationClient(Identifier identifier, ClientTardis tardis) {
        boolean bl = tardis.getExterior().isDoorOpen();
        if(identifier == DOOM_RIGHT_DIAGONAL || identifier == DOOM_RIGHT_DIAGONAL_OPEN) {
            return bl ? DOOM_RIGHT_DIAGONAL_OPEN_EMISSION : DOOM_DIAGONAL_EMISSION;
        } else if (identifier == DOOM_LEFT_DIAGONAL || identifier == DOOM_LEFT_DIAGONAL_OPEN) {
            return bl ? DOOM_LEFT_DIAGONAL_OPEN_EMISSION : DOOM_DIAGONAL_EMISSION;
        } else if (identifier == DOOM_BLANK_DIAGONAL) {
            return DOOM_DIAGONAL_EMISSION;
        } else if (identifier == DOOM_LEFT_SIDE || identifier == DOOM_LEFT_SIDE_OPEN) {
            return DOOM_LEFT_SIDE_EMISSION;
        } else if (identifier == DOOM_RIGHT_SIDE || identifier == DOOM_RIGHT_SIDE_OPEN) {
            return DOOM_RIGHT_SIDE_EMISSION;
        } else {
            return bl ? DOOM_FRONT_BACK_OPEN_EMISSION : DOOM_TEXTURE_EMISSION;
        }
    }
    public Identifier getEmissionForRotationServer(Identifier identifier, Tardis tardis) {
        boolean bl = tardis.getDoor().isOpen();
        if(identifier == DOOM_RIGHT_DIAGONAL || identifier == DOOM_RIGHT_DIAGONAL_OPEN) {
            return bl ? DOOM_RIGHT_DIAGONAL_OPEN_EMISSION : DOOM_DIAGONAL_EMISSION;
        } else if (identifier == DOOM_LEFT_DIAGONAL || identifier == DOOM_LEFT_DIAGONAL_OPEN) {
            return bl ? DOOM_LEFT_DIAGONAL_OPEN_EMISSION : DOOM_DIAGONAL_EMISSION;
        } else if (identifier == DOOM_BLANK_DIAGONAL) {
            return DOOM_DIAGONAL_EMISSION;
        } else if (identifier == DOOM_LEFT_SIDE || identifier == DOOM_LEFT_SIDE_OPEN) {
            return DOOM_LEFT_SIDE_EMISSION;
        } else if (identifier == DOOM_RIGHT_SIDE || identifier == DOOM_RIGHT_SIDE_OPEN) {
            return DOOM_RIGHT_SIDE_EMISSION;
        } else {
            return bl ? DOOM_FRONT_BACK_OPEN_EMISSION : DOOM_TEXTURE_EMISSION;
        }
    }
}
