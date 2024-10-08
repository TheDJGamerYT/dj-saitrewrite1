package loqor.ait.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import org.joml.Matrix4f;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import loqor.ait.AITMod;
import loqor.ait.core.util.vortex.VortexNode;

/**
 * @author - ThePlaceHolder (someElseisHere), Loqor
 * @implNote - Referenced from here, and originally from Dalek Mod 1.12 by my
 *           understanding. Will be replaced sometime in the near future - this
 *           was just a good jumping off point. - <a href=
 *           "https://github.com/someElseIsHere/DalekMod-TimeVortex/blob/master/src/main/java/org/theplaceholder/dmtv/client/vortex/Vortex.java">...</a>
 **/
public class VortexUtil {
    public Identifier TEXTURE_LOCATION;
    private final float distortionSpeed;
    private final float distortionSeparationFactor;
    private final float distortionFactor;
    private final float scale;
    private final float rotationFactor;
    private final float rotationSpeed;
    private final float speed;
    private float time = 0;

    public VortexUtil(String name /* , float distortionFactor */) {
        TEXTURE_LOCATION = new Identifier(AITMod.MOD_ID, "textures/vortex/" + name + ".png");
        this.distortionSpeed = 0.5f;
        this.distortionSeparationFactor = 32f;
        this.distortionFactor = 8; // distortionFactor;
        this.scale = 21f;
        this.rotationFactor = 1f;
        this.rotationSpeed = 1f;
        this.speed = 4f;
    }

    public void renderVortex(WorldRenderContext context) {
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.push();
        Camera camera = context.camera();
        Vec3d targetPosition = new Vec3d(0, 110, 0);
        // Vec3d position = new Vec3d(0, 50, 0);
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        Vec3d transformedPosition = targetPosition.subtract(camera.getPos());
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
        matrixStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);
        RenderSystem.enableBlend();
        RenderSystem.enableCull();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, TEXTURE_LOCATION);

        matrixStack.scale(scale, scale, scale);

        float f0 = (float) Math.toDegrees(this.rotationFactor * Math.sin(time * this.rotationSpeed));
        float f2 = f0 / 90.0f - (int) (f0 / 90.0f);
        // matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f2 * 360.0f));
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90f));

        /*
         * float alternate = (float) (((targetPosition.x - position.x) *
         * (targetPosition.x - position.x)) * ((targetPosition.z - position.z) *
         * (targetPosition.z - position.z)));
         * System.out.println(MathHelper.wrapDegrees((float)(-(MathHelper.atan2(
         * targetPosition.y - position.y, Math.sqrt(alternate)) * 57.2957763671875))) +
         * " " + MathHelper.wrapDegrees((float)(MathHelper.atan2(targetPosition.z -
         * position.z, targetPosition.x - position.x) * 57.2957763671875) - 90.0F));
         * matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.
         * wrapDegrees((float)(-(MathHelper.atan2(targetPosition.y - position.y,
         * Math.sqrt(alternate)) * 57.2957763671875)))));
         * matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.
         * wrapDegrees((float)(MathHelper.atan2(targetPosition.z - position.z,
         * targetPosition.x - position.x) * 57.2957763671875) - 90.0F)));
         */

        MinecraftClient.getInstance().getTextureManager().bindTexture(TEXTURE_LOCATION);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        // float distance = Vector3f.distance((float) targetPosition.x, (float)
        // targetPosition.y,
        // (float)
        // targetPosition.z, (float) position.x, (float) position.y, (float)
        // position.z);

        for (int i = 0; i < 36; ++i) {
            this.renderSection(buffer, i, time * -this.speed, (float) Math.sin(i * Math.PI / 36),
                    (float) Math.sin((i + 1) * Math.PI / 36), matrixStack.peek().getPositionMatrix());
        }

        tessellator.draw();

        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        matrixStack.pop();
        time += MinecraftClient.getInstance().getTickDelta() / 600f;
    }

    public void renderSection(VertexConsumer builder, int zOffset, float textureDistanceOffset, float startScale,
            float endScale, Matrix4f matrix4f) {
        float panel = 1 / 6f;
        float sqrt = (float) Math.sqrt(3) / 2.0f;
        int vOffset = (zOffset * panel + textureDistanceOffset > 1.0) ? zOffset - 6 : zOffset;
        float distortion = this.computeDistortionFactor(time, zOffset);
        float distortionPlusOne = this.computeDistortionFactor(time, zOffset + 1);
        float panelDistanceOffset = panel + textureDistanceOffset;
        float vPanelOffset = (vOffset * panel) + textureDistanceOffset;

        int uOffset = 0;

        float uPanelOffset = uOffset * panel;

        addVertex(builder, matrix4f, 0f, -startScale + distortion, -zOffset, uPanelOffset, vPanelOffset);

        addVertex(builder, matrix4f, 0f, -endScale + distortionPlusOne, -zOffset - 1, uPanelOffset,
                vOffset * panel + panelDistanceOffset);

        addVertex(builder, matrix4f, endScale * -sqrt, endScale / -2f + distortionPlusOne, -zOffset - 1,
                uPanelOffset + panel, vOffset * panel + panelDistanceOffset);

        addVertex(builder, matrix4f, startScale * -sqrt, startScale / -2f + distortion, -zOffset, uPanelOffset + panel,
                vPanelOffset);

        uOffset = 1;

        uPanelOffset = uOffset * panel;

        addVertex(builder, matrix4f, startScale * -sqrt, startScale / -2f + distortion, -zOffset, uPanelOffset,
                vPanelOffset);

        addVertex(builder, matrix4f, endScale * -sqrt, endScale / -2f + distortionPlusOne, -zOffset - 1, uPanelOffset,
                vOffset * panel + panelDistanceOffset);

        addVertex(builder, matrix4f, endScale * -sqrt, endScale / 2f + distortionPlusOne, -zOffset - 1,
                uPanelOffset + panel, vOffset * panel + panelDistanceOffset);

        addVertex(builder, matrix4f, startScale * -sqrt, startScale / 2f + distortion, -zOffset, uPanelOffset + panel,
                vPanelOffset);

        uOffset = 2;

        uPanelOffset = uOffset * panel;

        addVertex(builder, matrix4f, 0f, endScale + distortionPlusOne, -zOffset - 1, uPanelOffset + panel,
                vOffset * panel + panelDistanceOffset);

        addVertex(builder, matrix4f, 0f, startScale + distortion, -zOffset, uPanelOffset + panel, vPanelOffset);

        addVertex(builder, matrix4f, startScale * -sqrt, startScale / 2f + distortion, -zOffset, uPanelOffset,
                vPanelOffset);

        addVertex(builder, matrix4f, endScale * -sqrt, endScale / 2f + distortionPlusOne, -zOffset - 1, uPanelOffset,
                vOffset * panel + panelDistanceOffset);

        uOffset = 3;

        uPanelOffset = uOffset * panel;

        addVertex(builder, matrix4f, 0f, startScale + distortion, -zOffset, uPanelOffset, vPanelOffset);

        addVertex(builder, matrix4f, 0f, endScale + distortionPlusOne, -zOffset - 1, uPanelOffset,
                vOffset * panel + panelDistanceOffset);

        addVertex(builder, matrix4f, endScale * sqrt, (endScale / 2f + distortionPlusOne), -zOffset - 1,
                uPanelOffset + panel, vOffset * panel + panelDistanceOffset);

        addVertex(builder, matrix4f, startScale * sqrt, (startScale / 2f + distortion), -zOffset, uPanelOffset + panel,
                vPanelOffset);

        uOffset = 4;

        uPanelOffset = uOffset * panel;

        addVertex(builder, matrix4f, startScale * sqrt, (startScale / 2f + distortion), -zOffset, uPanelOffset,
                vPanelOffset);

        addVertex(builder, matrix4f, endScale * sqrt, endScale / 2f + distortionPlusOne, -zOffset - 1, uPanelOffset,
                vOffset * panel + panelDistanceOffset);

        addVertex(builder, matrix4f, endScale * sqrt, endScale / -2f + distortionPlusOne, -zOffset - 1,
                uPanelOffset + panel, vOffset * panel + panelDistanceOffset);

        addVertex(builder, matrix4f, startScale * sqrt, startScale / -2f + distortion, -zOffset, uPanelOffset + panel,
                vPanelOffset);

        uOffset = 5;

        uPanelOffset = uOffset * panel;

        addVertex(builder, matrix4f, 0f, -endScale + distortionPlusOne, -zOffset - 1, uPanelOffset + panel,
                vOffset * panel + panelDistanceOffset);

        addVertex(builder, matrix4f, 0f, -startScale + distortion, -zOffset, uPanelOffset + panel, vPanelOffset);

        addVertex(builder, matrix4f, startScale * sqrt, startScale / -2f + distortion, -zOffset, uPanelOffset,
                vPanelOffset);

        addVertex(builder, matrix4f, endScale * sqrt, endScale / -2f + distortionPlusOne, -zOffset - 1, uPanelOffset,
                vOffset * panel + panelDistanceOffset);
    }

    private void addVertex(VertexConsumer builder, Matrix4f matrix, float x, float y, float z, float u, float v) {
        builder.vertex(matrix, x, y, z).texture(u, v).next();
    }

    private float computeDistortionFactor(float time, int t) {
        return 0; // (float) (Math.sin(8 * this.distortionSpeed * 2.0 * Math.PI + (13 - t) *
        // this.distortionSeparationFactor) * this.distortionFactor) / 8;
    }

    public void renderVortexNodes(WorldRenderContext context, VortexNode node) {
        MatrixStack stack = context.matrixStack();
        Matrix4f positionMatrix = stack.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        stack.push();
        stack.translate(node.getPos().x, node.getPos().y, node.getPos().z);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
        buffer.vertex(positionMatrix, 20, 20, 0).color(1f, 1f, 1f, 1f).texture(0, 0).next();
        buffer.vertex(positionMatrix, 20, 60, 0).color(1f, 0f, 0f, 1f).texture(0, 1f).next();
        buffer.vertex(positionMatrix, 60, 60, 0).color(0f, 1f, 0f, 1f).texture(1f, 1f).next();
        buffer.vertex(positionMatrix, 0, 20, 0).color(0f, 0f, 1f, 1f).texture(1f, 0).next();

        RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
        RenderSystem.setShaderTexture(0, TEXTURE_LOCATION);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        tessellator.draw();

        stack.pop();
    }
}
