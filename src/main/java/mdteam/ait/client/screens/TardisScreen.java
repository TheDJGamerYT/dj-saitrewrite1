package mdteam.ait.client.screens;

import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.linkable.Linkable;
import mdteam.ait.tardis.wrapper.client.manager.ClientTardisManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.UUID;

public abstract class TardisScreen extends Screen implements Linkable {

    private UUID tardisID;

    protected TardisScreen(Text title, UUID tardisID) {
        super(title);
        this.tardisID = tardisID;
    }

    @Override
    public Tardis getTardis() {
        return ClientTardisManager.getInstance().getLookup().get(this.tardisID);
    }

    protected Tardis updateTardis() {
        return getTardis();
    }

    @Override
    public void setTardis(Tardis tardis) {
        this.tardisID = tardis.getUuid();
    }
}
