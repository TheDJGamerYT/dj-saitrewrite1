package mdteam.ait.tardis.handler;

import mdteam.ait.tardis.AbstractTardisComponent;
import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.linkable.Linkable;

import java.util.HashMap;
import java.util.UUID;

import static mdteam.ait.tardis.handler.PropertiesHandler.AUTO_LAND;

public class PropertiesHolder extends AbstractTardisComponent implements Linkable {
    private final HashMap<String, Object> data; // might replace the generic object with a property class that has impls eg Property.Boolean, etc
    public PropertiesHolder(Tardis tardis) {
        this(tardis, createDefaultProperties());
    }
    public PropertiesHolder(Tardis tardis, HashMap<String, Object> data) {
        super(tardis);
        this.data = data;
    }

    public HashMap<String, Object> getData() {
        return this.data;
    }

    public static HashMap<String, Object> createDefaultProperties() {
        HashMap<String, Object> map = new HashMap<>();

        map.put(AUTO_LAND, false);

        return map;
    }

    @Override
    public void setTardis(Tardis tardis) {
        this.tardis = tardis;
    }
}
