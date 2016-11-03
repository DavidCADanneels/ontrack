package net.nemerosa.ontrack.ui.resource;

import net.nemerosa.ontrack.model.structure.ProjectEntity;

import java.util.List;

/**
 * Service used to register all {@link ResourceDecorationContributor} instances.
 */
public interface ResourceDecorationContributorService {

    /**
     * Gets the link definitions for a class of project entity
     *
     * @param projectClass Project entity class to get the link definitions for
     * @param <T>          Type of project entity
     * @return List of link definitions
     */
    <T extends ProjectEntity> List<LinkDefinition<T>> getLinkDefinitions(Class<T> projectClass);
}
