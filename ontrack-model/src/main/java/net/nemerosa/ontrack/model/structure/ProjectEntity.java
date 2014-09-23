package net.nemerosa.ontrack.model.structure;

import com.fasterxml.jackson.annotation.JsonIgnore;

import static org.apache.commons.lang3.Validate.isTrue;

/**
 * A <b>ProjectEntity</b> is an {@link Entity} that belongs into a {@link Project}.
 */
public interface ProjectEntity extends Entity {

    /**
     * Returns the project this entity is associated with
     */
    @JsonIgnore
    Project getProject();

    /**
     * Returns the ID of the project that contains this entity. This method won't return <code>null</code>
     * but the ID could be {@linkplain ID#NONE undefined}.
     */
    @JsonIgnore
    default ID getProjectId() {
        return getProject().getId();
    }

    /**
     * Shortcut to get the ID as a value.
     *
     * @throws java.lang.IllegalArgumentException If the project ID is not {@linkplain ID#isSet() set}.
     */
    default int projectId() {
        ID id = getProjectId();
        isTrue(ID.isDefined(id), "Project ID must be defined");
        return id.get();
    }

    /**
     * Gets the type of entity as an enum.
     */
    @JsonIgnore
    ProjectEntityType getProjectEntityType();

}
