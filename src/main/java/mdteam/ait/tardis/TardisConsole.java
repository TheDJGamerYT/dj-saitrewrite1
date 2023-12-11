package mdteam.ait.tardis;

import mdteam.ait.client.renderers.consoles.ConsoleEnum;

public class TardisConsole extends AbstractTardisComponent {

    private ConsoleEnum console;
    private ControlTypes[] controlTypes;

    public TardisConsole(Tardis tardis) {
        this(tardis, ConsoleEnum.BOREALIS, ConsoleEnum.BOREALIS.getControlTypesList());
    }

    public TardisConsole(Tardis tardis, ConsoleEnum consoleType) {
        this(tardis, consoleType, consoleType.getControlTypesList());
    }

    public TardisConsole(Tardis tardis, ControlTypes[] controlTypes) {
        this(tardis, ConsoleEnum.BOREALIS, controlTypes);
    }

    protected TardisConsole(Tardis tardis, ConsoleEnum console, ControlTypes[] controlTypes) {
        super(tardis);
        this.console = console;
        this.controlTypes = controlTypes;
    }

    public ControlTypes[] getControlTypes() {
        return controlTypes;
    }

    public void setControlTypes(ControlTypes[] controlTypes) {
        this.controlTypes = controlTypes;
    }

    public ConsoleEnum getType() {
        return console;
    }

    public void setType(ConsoleEnum console) {
        this.console = console;
    }
}
