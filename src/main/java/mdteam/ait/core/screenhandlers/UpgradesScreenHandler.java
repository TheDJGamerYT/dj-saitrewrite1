package mdteam.ait.core.screenhandlers;

import mdteam.ait.core.AITItems;
import mdteam.ait.core.AITScreenHandlerTypes;
import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.data.UpgradesData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.ShulkerBoxSlot;
import net.minecraft.screen.slot.Slot;

public class UpgradesScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    //private final PropertyDelegate propertyDelegate;

    public UpgradesScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory) {
        this(type, syncId, playerInventory, new SimpleInventory(35)/*, new ArrayPropertyDelegate(1)*/);
    }

    public static UpgradesScreenHandler createDefault(int syncId, PlayerInventory playerInventory) {
        return new UpgradesScreenHandler(AITScreenHandlerTypes.UPGRADES_SCREEN_HANDLER_TYPE, syncId, playerInventory);
    }

    public UpgradesScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, Inventory inventory/*, PropertyDelegate propertyDelegate*/) {
        super(type, syncId);
        int l;
        int k;
        GenericContainerScreenHandler.checkSize(inventory, 8);
        this.inventory = inventory;
        //this.propertyDelegate = propertyDelegate;
        //this.addProperties(propertyDelegate);
        inventory.onOpen(playerInventory.player);
        for (k = 0; k < 2; ++k) {
            for (l = 0; l < 4; ++l) {
                this.addSlot(new ShulkerBoxSlot(inventory, (l + k * 4), 26 + l * 36, 24 + k * 23));
            }
        }
        for (k = 0; k < 3; ++k) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + k * 9 + 9, 8 + l * 18, 84 + k * 18));
            }
        }
        for (k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    // Shift + Player Inv Slot
    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot2 = this.slots.get(slot);
        if (slot2 != null && slot2.hasStack()) {
            ItemStack itemStack2 = slot2.getStack();
            for(Item items : UpgradesData.getAllowedItems()) {
                if(itemStack2.getItem() == items)
                    return itemStack;
            }
            itemStack = itemStack2.copy();
            if (slot < 8 ? !this.insertItem(itemStack2, 8, this.slots.size(), true) : !this.insertItem(itemStack2, 0, 8, false)) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot2.setStack(ItemStack.EMPTY);
            } else {
                slot2.markDirty();
            }
        }
        return itemStack;
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        for(Item items : UpgradesData.getAllowedItems()) {
            if(stack.getItem() == items)
                return super.canInsertIntoSlot(stack, slot);
        }
        return super.canInsertIntoSlot(ItemStack.EMPTY, slot);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.inventory.onClose(player);
    }

    public Inventory getInventory() {
        return this.inventory;
    }
}
