package mdteam.ait.core.item;

import net.minecraft.item.Item;

public class ComponentItem extends Item {

    private final String componentName;

    public ComponentItem(Settings settings, String name) {
        super(settings);
        this.componentName = name;
    }

    public String getComponentName() {
        return componentName;
    }

}
