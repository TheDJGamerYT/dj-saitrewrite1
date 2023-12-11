package mdteam.ait.client.screens;

import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.linkable.Linkable;
import mdteam.ait.tardis.manager.TardisManager;
import mdteam.ait.tardis.wrapper.client.manager.ClientTardisManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.UUID;

public abstract class TardisScreen extends Screen implements Linkable {

    protected Tardis tardis;
    private final UUID tardisid;

    protected TardisScreen(Text title, UUID tardisID) {
        super(title);
        this.tardisid = tardisID;
        TardisManager.getInstance(false).link(tardisID, this);
    }

    public Tardis updateTardis() {
        if(this.getTardis() == null)
            TardisManager.getInstance(false).link(this.tardisid, this);
        return this.getTardis();
    }

    @Override
    public Tardis getTardis() {
        return tardis;
    }

    @Override
    public void setTardis(Tardis tardis) {
        this.tardis = tardis;
    }
}
