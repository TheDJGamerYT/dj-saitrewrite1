package mdteam.ait.tardis.data;

import io.wispforest.owo.util.ImplementedInventory;
import mdteam.ait.core.AITItems;
import mdteam.ait.core.item.KeyItem;
import mdteam.ait.tardis.Exclude;
import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.data.properties.PropertiesHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;

public class UpgradesData extends TardisLink implements ImplementedInventory {

    @Exclude
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(8, ItemStack.EMPTY);
    public UpgradesData(Tardis tardis) {
        super(tardis, "upgrades");
    }

    public boolean hasCloak() {
        if(getTardis().isEmpty()) return false;
        return PropertiesHandler.getBool(getTardis().get().getHandlers().getProperties(), ProtocolConstants.HAS_CLOAK);
    }

    public boolean hasSiegeMode() {
        if(getTardis().isEmpty()) return false;
        return PropertiesHandler.getBool(getTardis().get().getHandlers().getProperties(), ProtocolConstants.HAS_SIEGE_MODE);
    }

    public boolean hasAutopilot() {
        if(getTardis().isEmpty()) return false;
        return PropertiesHandler.getBool(getTardis().get().getHandlers().getProperties(), ProtocolConstants.HAS_AUTOPILOT);
    }

    public boolean hasGroundSearching() {
        if(getTardis().isEmpty()) return false;
        return PropertiesHandler.getBool(getTardis().get().getHandlers().getProperties(), ProtocolConstants.HAS_GROUND_SEARCHING);
    }

    public boolean hasTelepathicLocator() {
        if(getTardis().isEmpty()) return false;
        return PropertiesHandler.getBool(getTardis().get().getHandlers().getProperties(), ProtocolConstants.HAS_TELEPATHIC_LOCATOR);
    }

    public boolean hasSecurity() {
        if(getTardis().isEmpty()) return false;
        return PropertiesHandler.getBool(getTardis().get().getHandlers().getProperties(), ProtocolConstants.HAS_SECURITY);
    }

    public boolean hasAntigravs() {
        if(getTardis().isEmpty()) return false;
        return PropertiesHandler.getBool(getTardis().get().getHandlers().getProperties(), ProtocolConstants.HAS_ANTIGRAVS);
    }

    public boolean hasHailMary() {
        if(getTardis().isEmpty()) return false;
        return PropertiesHandler.getBool(getTardis().get().getHandlers().getProperties(), ProtocolConstants.HAS_HAIL_MARY);
    }

    public void setHasCloak(boolean hasCloak) {
        if(getTardis().isEmpty()) return;
        PropertiesHandler.set(getTardis().get().getHandlers().getProperties(), ProtocolConstants.HAS_CLOAK, hasCloak);
    }

    public void setHasHailMary(boolean hasHailMary) {
        if(getTardis().isEmpty()) return;
        PropertiesHandler.set(getTardis().get().getHandlers().getProperties(), ProtocolConstants.HAS_HAIL_MARY, hasHailMary);
    }

    public void setHasAntigravs(boolean hasAntigravs) {
        if(getTardis().isEmpty()) return;
        PropertiesHandler.set(getTardis().get().getHandlers().getProperties(), ProtocolConstants.HAS_ANTIGRAVS, hasAntigravs);
    }

    public void setHasSecurity(boolean hasSecurity) {
        if(getTardis().isEmpty()) return;
        PropertiesHandler.set(getTardis().get().getHandlers().getProperties(), ProtocolConstants.HAS_SECURITY, hasSecurity);
    }

    public void setHasTelepathicLocator(boolean hasTelepathicLocator) {
        if(getTardis().isEmpty()) return;
        PropertiesHandler.set(getTardis().get().getHandlers().getProperties(), ProtocolConstants.HAS_TELEPATHIC_LOCATOR, hasTelepathicLocator);
    }

    public void setHasGroundSearching(boolean hasGroundSearching) {
        if(getTardis().isEmpty()) return;
        PropertiesHandler.set(getTardis().get().getHandlers().getProperties(), ProtocolConstants.HAS_GROUND_SEARCHING, hasGroundSearching);
    }

    public void setHasAutopilot(boolean hasAutopilot) {
        if(getTardis().isEmpty()) return;
        PropertiesHandler.set(getTardis().get().getHandlers().getProperties(), ProtocolConstants.HAS_AUTOPILOT, hasAutopilot);
    }

    public void setHasSiegeMode(boolean hasSiegeMode) {
        if(getTardis().isEmpty()) return;
        PropertiesHandler.set(getTardis().get().getHandlers().getProperties(), ProtocolConstants.HAS_SIEGE_MODE, hasSiegeMode);
    }

    @Override
    public void tick(ServerWorld world) {
        super.tick(world);
        this.getItems().forEach(itemStack -> {
            this.setHasCloak(itemStack.getItem() == Items.GLASS);
            this.setHasHailMary(itemStack.getItem() == Items.NETHERITE_HOE);
            this.setHasAntigravs(itemStack.getItem() == Items.ENDER_PEARL);
            this.setHasSecurity(itemStack.getItem() == Items.GHAST_TEAR);
            this.setHasTelepathicLocator(itemStack.getItem() == Items.SCULK);
            this.setHasGroundSearching(itemStack.getItem() == Items.SPYGLASS);
            this.setHasAutopilot(itemStack.getItem() == Items.SCULK_CATALYST);
            this.setHasSiegeMode(itemStack.getItem() == Items.CRYING_OBSIDIAN);
        });
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    public static ArrayList<Item> getAllowedItems() {
        ArrayList<Item> allowedItems = new ArrayList<>();
        allowedItems.add(Items.GLASS);
        allowedItems.add(Items.NETHERITE_HOE);
        allowedItems.add(Items.ENDER_PEARL);
        allowedItems.add(Items.GHAST_TEAR);
        allowedItems.add(Items.SCULK);
        allowedItems.add(Items.SPYGLASS);
        allowedItems.add(Items.SCULK_CATALYST);
        allowedItems.add(Items.CRYING_OBSIDIAN);
        return allowedItems;
    }

    @Override
    public int size() {
        return ImplementedInventory.super.size();
    }
}
