package mdteam.ait.tardis.control.impl.pos;

import mdteam.ait.network.ServerAITNetworkManager;
import mdteam.ait.tardis.Tardis;

public class PosManager { // todo can likely be moved into the properties / use properties instead
    // private final ConsoleBlockEntity console;
    public int increment;
    private Tardis tardis;
    private int[] validIncrements = new int[] {
            1,
            10,
            100,
            1000
    };

    // this shouldnt be saved, just create a new one when its null bc imo i dont think the increment really NEEDS saving. but if it does then just create a save/load method here
    public PosManager(Tardis tardis) {
        this.increment = 1;
        ServerAITNetworkManager.sendTardisPosIncrementUpdate(tardis, increment);
    }

    public Tardis getTardis() {
        return this.tardis;
    }

    private int getIncrementPosition() {
        // since indexof doesnt seem to work..
        for (int i = 0; i < validIncrements.length; i++) {
            if (this.increment != validIncrements[i]) continue;

            return i;
        }
        return 0;
    }
    public int nextIncrement() {

        this.increment = validIncrements[(getIncrementPosition() + 1 > validIncrements.length - 1) ? 0 : getIncrementPosition() + 1];
        ServerAITNetworkManager.sendTardisPosIncrementUpdate(tardis, increment);

        return this.increment;
    }
    public int prevIncrement() {
        this.increment = validIncrements[(getIncrementPosition() - 1 < 0) ? validIncrements.length - 1 : getIncrementPosition() - 1];
        ServerAITNetworkManager.sendTardisPosIncrementUpdate(tardis, increment);

        return this.increment;
    }
}
