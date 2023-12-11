package mdteam.ait.tardis.handler;

import mdteam.ait.core.util.data.AbsoluteBlockPos;
import mdteam.ait.tardis.AbstractTardisComponent;
import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.linkable.Linkable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;

public class WaypointHandler extends AbstractTardisComponent implements Iterable<AbsoluteBlockPos.Directed>, Linkable {
    private final ArrayList<AbsoluteBlockPos.Directed> data;

    public WaypointHandler(Tardis tardis) {
        this(tardis, new ArrayList<>());
    }

    public WaypointHandler(Tardis tardis, ArrayList<AbsoluteBlockPos.Directed> waypoints) {
        super(tardis);

        this.data = waypoints;
    }
    public ArrayList<AbsoluteBlockPos.Directed> getData() {
        return this.data;
    }
    public boolean contains(AbsoluteBlockPos.Directed var) {
        return this.getData().contains(var);
    }
    public void add(AbsoluteBlockPos.Directed var) {
        this.getData().add(var);
    }
    public void remove(AbsoluteBlockPos.Directed var) {
        if (!this.getData().contains(var)) return;

        this.getData().remove(var);
    }
    public void remove(int index) {
        this.getData().remove(index);
    }
    public AbsoluteBlockPos.Directed get(int index) {
        return this.getData().get(index);
    }

    @NotNull
    @Override
    public Iterator<AbsoluteBlockPos.Directed> iterator() {
        return this.getData().iterator();
    }

    @Override
    public void setTardis(Tardis tardis) {
        this.tardis = tardis;
    }
}
