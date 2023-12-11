
package mdteam.ait.tardis.linkable;

import mdteam.ait.tardis.*;

public interface Linkable {

    Tardis getTardis();
    void setTardis(Tardis tardis);
    default TardisDesktop getDesktop() {
        return this.getTardis().getDesktop();
    }
    default void setDesktop(TardisDesktop desktop) { }

    /**
     * This method forces the {@link Linkable} to update its desktop!
     */
    default void linkDesktop() {
        if (this.getTardis() == null)
            return;

        if (this.getDesktop() != null)
            this.setDesktop(this.getDesktop());
    }

    default TardisTravel getTravel() {
        return this.getTardis().getTravel();
    }
    default void setTravel(TardisTravel travel) { }

    /**
     * This method forces the {@link Linkable} to update its travel!
     */
    default void linkTravel() {
        if (this.getTardis() == null)
            return;

        TardisTravel travel = this.getTardis().getTravel();

        if (travel != null)
            this.setTravel(travel);
    }

    default TardisDoor getDoor() {
        return this.getTardis().getDoor();
    }
    default void setDoor(TardisDoor door) {}

    default TardisConsole getConsole() {
        return this.getTardis().getConsole();
    }
    default void setConsole(TardisConsole console) {}

    /**
     * This method forces the {@link Linkable} to update its door.
     */
    default void linkDoor() {
        if (this.getTardis() == null)
            return;

        TardisDoor door = this.getTardis().getDoor();

        if (door != null)
            this.setDoor(door);
    }

    default void linkConsole() {
        if (this.getTardis() == null)
            return;

        TardisConsole console = this.getTardis().getConsole();

        if (console != null)
            this.setConsole(console);
    }

    /**
     * If false, calling {@link Linkable#setTardis(Tardis)} might throw an exception!
     */
    default boolean linkable() {
        return true;
    }
}
