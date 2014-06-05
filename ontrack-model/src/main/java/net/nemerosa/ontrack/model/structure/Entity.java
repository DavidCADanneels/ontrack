package net.nemerosa.ontrack.model.structure;

import static org.apache.commons.lang3.Validate.isTrue;

public interface Entity {

    public static void isEntityNew(Entity e, String message) {
        isTrue(e != null && !ID.isDefined(e.getId()), message);
    }

    public static void isEntityDefined(Entity e, String message) {
        isTrue(e != null && ID.isDefined(e.getId()), message);
    }

    ID getId();

    default int id() {
        ID id = getId();
        isTrue(ID.isDefined(id), "ID must be defined");
        return getId().getValue();
    }

    // FIXME Project id

}
