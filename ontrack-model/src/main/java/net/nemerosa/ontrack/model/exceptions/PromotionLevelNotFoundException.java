package net.nemerosa.ontrack.model.exceptions;

import net.nemerosa.ontrack.model.structure.ID;

public class PromotionLevelNotFoundException extends NotFoundException {

    public PromotionLevelNotFoundException(ID id) {
        super("Promotion level ID not found: %s", id);
    }
}
