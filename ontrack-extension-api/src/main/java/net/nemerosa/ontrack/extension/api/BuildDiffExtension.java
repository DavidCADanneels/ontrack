package net.nemerosa.ontrack.extension.api;

import net.nemerosa.ontrack.model.structure.Branch;

/**
 * This extension allows to an action on the selection of two {@link net.nemerosa.ontrack.model.structure.Build builds}
 * for the same {@link net.nemerosa.ontrack.model.structure.Branch branch}.
 */
public interface BuildDiffExtension extends ActionExtension {

    /**
     * Checks if this action is applicable for the given <code>branch</code>.
     */
    boolean apply(Branch branch);

}
