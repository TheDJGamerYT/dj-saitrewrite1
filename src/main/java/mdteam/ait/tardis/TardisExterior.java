package mdteam.ait.tardis;

import mdteam.ait.client.renderers.exteriors.ExteriorEnum;
import mdteam.ait.tardis.wrapper.client.manager.ClientTardisManager;
import mdteam.ait.tardis.wrapper.server.manager.ServerTardisManager;

public class TardisExterior extends AbstractTardisComponent {

    private ExteriorEnum exterior;

    public TardisExterior(Tardis tardis, ExteriorEnum exterior) {
        super(tardis);

        this.exterior = exterior;
    }

    public ExteriorEnum getType() {
        return exterior;
    }

    public void setType(ExteriorEnum exterior) {
        this.exterior = exterior;

        ServerTardisManager.getInstance().sendToSubscribers(this.tardis);
    }
}
