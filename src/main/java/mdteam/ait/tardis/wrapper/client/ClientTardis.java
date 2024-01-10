package mdteam.ait.tardis.wrapper.client;

import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.TardisDesktopSchema;
import mdteam.ait.tardis.exterior.ExteriorSchema;
import mdteam.ait.tardis.util.AbsoluteBlockPos;
import mdteam.ait.tardis.variant.exterior.ExteriorVariantSchema;

import java.util.UUID;

public class ClientTardis {
    private final UUID tardisID;
    private ExteriorVariantSchema exteriorVariantSchema;
    private ExteriorSchema exteriorSchema;

    public ClientTardis(UUID tardisID, ExteriorVariantSchema exteriorVariantSchema, ExteriorSchema exteriorSchema) {
        this.tardisID = tardisID;
    }

    /**
     * Returns the Tardis ID.
     *
     * @return the Tardis ID
     */
    public UUID getTardisID() {
        return tardisID;
    }

    /**
     * Retrieves the exterior variant schema.
     *
     * @return  the exterior variant schema
     */
    public ExteriorVariantSchema getExteriorVariant() {
        return exteriorVariantSchema;
    }

    /**
     * Retrieves the exterior type of the object.
     *
     * @return the exterior schema of the object
     */
    public ExteriorSchema getExteriorType() {
        return exteriorSchema;
    }

    /**
     * Sets the exterior variant of the object.
     *
     * @param  exteriorVariantSchema  the exterior variant to set
     */
    public void setExteriorVariant(ExteriorVariantSchema exteriorVariantSchema) {
        if (exteriorVariantSchema == null) return;
        this.exteriorVariantSchema = exteriorVariantSchema;
    }

    /**
     * Sets the exterior type for the object.
     *
     * @param  exteriorSchema    the exterior schema to set
     */
    public void setExteriorType(ExteriorSchema exteriorSchema) {
        if (exteriorSchema == null) return;
        this.exteriorSchema = exteriorSchema;
    }
}
