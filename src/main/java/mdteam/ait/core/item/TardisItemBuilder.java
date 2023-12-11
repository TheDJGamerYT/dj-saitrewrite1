package mdteam.ait.core.item;

import mdteam.ait.AITMod;
import mdteam.ait.client.renderers.consoles.ConsoleEnum;
import mdteam.ait.client.renderers.exteriors.ExteriorEnum;
import mdteam.ait.core.AITDesktops;
import mdteam.ait.core.util.data.AbsoluteBlockPos;
import mdteam.ait.tardis.TardisDesktopSchema;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import mdteam.ait.tardis.wrapper.server.manager.ServerTardisManager;

public class TardisItemBuilder extends Item {
    public static final Identifier DEFAULT_INTERIOR = new Identifier(AITMod.MOD_ID, "regal");
    public static final ExteriorEnum DEFAULT_EXTERIOR = ExteriorEnum.CAPSULE;
    //public static final ConsoleEnum DEFAULT_CONSOLE = ConsoleEnum.BOREALIS; // new Identifier(AITMod.MOD_ID, "borealis");
    // fixme add TardisConsole stuff like TardisDesktop interior registry (datapack console entity positions/models/textures from imgur/datapacks?)

    private final ExteriorEnum exterior;
    private final Identifier desktop;

    public TardisItemBuilder(Settings settings, ExteriorEnum exterior, Identifier desktopId) {
        super(settings);

        this.exterior = exterior;
        this.desktop = desktopId;
    }

    public TardisItemBuilder(Settings settings, ExteriorEnum exterior) {
        this(settings, exterior, DEFAULT_INTERIOR);
    }

    public TardisItemBuilder(Settings settings) {
        this(settings, DEFAULT_EXTERIOR);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();

        if (world.isClient() || player == null)
            return ActionResult.PASS;

        AbsoluteBlockPos.Directed pos = new AbsoluteBlockPos.Directed(context.getBlockPos().up(), world, player.getHorizontalFacing().getOpposite());

        if (context.getHand() == Hand.MAIN_HAND) {
            ServerTardisManager.getInstance().create(pos, this.exterior, AITDesktops.get(this.desktop), ConsoleEnum.BOREALIS);
            context.getStack().decrement(1);
        }

        return ActionResult.SUCCESS;
    }
}