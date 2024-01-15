package mdteam.ait.client.screens;

import mdteam.ait.AITMod;
import mdteam.ait.tardis.util.TardisUtil;
import mdteam.ait.tardis.wrapper.client.ClientTardis;
import mdteam.ait.tardis.wrapper.client.manager.ClientTardisManager;
import mdteam.ait.tardis.Tardis;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.UUID;

public abstract class TardisScreen extends Screen {
    UUID tardisId;
    protected TardisScreen(Text title, UUID tardis) {
        super(title);
        this.tardisId = tardis;
    }
    protected ClientTardis tardis() {
        return ClientTardisManager.getInstance().LOOKUP.get(tardisId).get();
    }
}
