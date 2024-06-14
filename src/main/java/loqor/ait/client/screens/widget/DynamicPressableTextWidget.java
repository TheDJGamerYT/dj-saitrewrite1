package loqor.ait.client.screens.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.math.MathHelper;

import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class DynamicPressableTextWidget extends ButtonWidget {
    private final TextRenderer textRenderer;
    private final Supplier<Text> text;

    private Text cached;
    private Text hoverText;

    public DynamicPressableTextWidget(int x, int y, int width, int height, Supplier<Text> text, ButtonWidget.PressAction onPress, TextRenderer textRenderer) {
        super(x, y, width, height, text.get(), onPress, DEFAULT_NARRATION_SUPPLIER);

        this.textRenderer = textRenderer;
        this.text = text;

        this.refresh();
    }

    public void refresh() {
        this.cached = this.text.get();
        this.hoverText = Texts.setStyleIfAbsent(this.cached.copy(), Style.EMPTY.withUnderline(true));
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        Text text = this.isSelected() ? this.hoverText : this.cached;
        context.drawTextWithShadow(this.textRenderer, text, this.getX(), this.getY(), 0xFFFFFF | MathHelper.ceil(this.alpha * 255.0f) << 24);
    }
}