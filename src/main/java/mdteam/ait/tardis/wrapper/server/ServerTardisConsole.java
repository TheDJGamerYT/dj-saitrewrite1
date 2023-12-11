package mdteam.ait.tardis.wrapper.server;

import mdteam.ait.client.renderers.consoles.ConsoleEnum;
import mdteam.ait.core.blockentities.console.ConsoleBlockEntity;
import mdteam.ait.core.blocks.ConsoleBlock;
import mdteam.ait.core.util.TardisUtil;
import mdteam.ait.tardis.ControlTypes;
import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.TardisConsole;
import mdteam.ait.tardis.linkable.Linkable;
import mdteam.ait.tardis.wrapper.server.manager.ServerTardisManager;

public class ServerTardisConsole extends TardisConsole {

    public ServerTardisConsole(Tardis tardis, ConsoleEnum console, ControlTypes[] controlTypes) {
        super(tardis, console, controlTypes);
    }

    @Override
    public void setControlTypes(ControlTypes[] controlTypes) {
        super.setControlTypes(controlTypes);

        this.sync();
    }

    @Override
    public void setType(ConsoleEnum console) {
        super.setType(console);
        if(TardisUtil.getTardisDimension().getBlockEntity(this.getTardis().getDesktop().getConsolePos())
                instanceof ConsoleBlockEntity consoleBlockEntity) {
            consoleBlockEntity.setConsole(this);
        }

        this.sync();
    }

    private void sync() {
        ServerTardisManager.getInstance().sendToSubscribers(this.tardis);
    }
}
