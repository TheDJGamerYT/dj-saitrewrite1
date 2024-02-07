package mdteam.ait.client.screens;

import mdteam.ait.AITMod;
import mdteam.ait.core.AITItems;
import mdteam.ait.core.screenhandlers.UpgradesScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Set;

import static mdt.k9mod.item.K9LithiumCellItem.BATTERY_KEY;

public class UpgradesScreen extends HandledScreen<UpgradesScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(AITMod.MOD_ID, "textures/gui/tardis/upgrades/upgrades_screen.png");

    public UpgradesScreen(UpgradesScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        // Center the title
        titleX = (backgroundWidth - textRenderer.getWidth("Upgrades")) / 2;
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        super.drawForeground(context, mouseX, mouseY);
        /*String batteryLevel = this.getScreenHandler().getBattery() + "%";
        for (int i = 0; i < 8; i++) {
            if(!this.handler.getSlot(i).getStack().isEmpty()) {
                int j = this.handler.getSlot(i).getStack().getItem() == AITItems.UPGRADE_COMPONENT ? 19 : 0;
                if(this.handler.getSlot(i).getStack().getItem() == AITItems.UPGRADE_COMPONENT) {
                    NbtCompound nbt = this.handler.getSlot(i).getStack().getNbt();
                    if (nbt != null) {
                        int other = nbt.getInt(BATTERY_KEY) <= 5 && nbt.getInt(BATTERY_KEY) > 0 ? 38 : 19;
                        j = nbt.getInt(BATTERY_KEY) <= 0 ? 57 : other;
                    }
                }
                context.drawTexture(TEXTURE, this.handler.getSlot(i).x - 1, this.handler.getSlot(i).y + 16, j, 167, 18, 6);
            }
        }
        context.drawCenteredTextWithShadow(this.textRenderer, Text.of(batteryLevel), (this.backgroundWidth - this.textRenderer.getWidth(batteryLevel) + 150) / 2, (this.backgroundHeight - 153) / 2, 0x00ff00);*/
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int i = (this.width - this.backgroundWidth) / 2;
        int j = ((this.height) - this.backgroundHeight) / 2;
        context.drawTexture(TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
        //System.out.println(this.handler.getSlot(0).getStack());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}