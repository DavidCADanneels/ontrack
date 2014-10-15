package net.nemerosa.ontrack.model.structure;

import net.nemerosa.ontrack.model.Ack;

import java.util.Optional;

public interface BranchTemplateService {

    /**
     * Gets the template definition for a branch
     */
    Optional<TemplateDefinition> getTemplateDefinition(ID branchId);

    /**
     * Sets the branch as a template definition or updates the existing definition.
     */
    Branch setTemplateDefinition(ID branchId, TemplateDefinition templateDefinition);

    /**
     * Creates a branch template instance for one name.
     * <p>
     * <ul>
     * <li>If the target branch does not exist, creates it.</li>
     * <li>If the target branch exists:
     * <ul>
     * <li>If it is linked to the same definition, updates it.</li>
     * <li>If it is linked to another definition, this is an error.</li>
     * <li>If it is a normal branch, this is an error.</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param branchId ID of the branch template definition
     * @param request  Request for the creation
     * @return Created or updated branch
     */
    Branch createTemplateInstance(ID branchId, BranchTemplateInstanceSingleRequest request);

    /**
     * Sync. this template definition by creating and updating linked template instances.
     */
    Ack sync(ID branchId);
}
