package mdteam.ait.core;

import mdteam.ait.AITMod;
import mdteam.ait.core.screenhandlers.UpgradesScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class AITScreenHandlerTypes {

    public static final ScreenHandlerType<UpgradesScreenHandler> UPGRADES_SCREEN_HANDLER_TYPE = new ScreenHandlerType<>(UpgradesScreenHandler::createDefault, ScreenHandlerType.GENERIC_9X3.getRequiredFeatures());

    public static void init() {
        Registry.register(Registries.SCREEN_HANDLER, new Identifier(AITMod.MOD_ID, "box"), AITScreenHandlerTypes.UPGRADES_SCREEN_HANDLER_TYPE);
    }
}
