package net.nemerosa.ontrack.extension.stash.property;

import lombok.Data;
import net.nemerosa.ontrack.extension.stash.model.StashConfiguration;

@Data
public class StashProjectConfigurationProperty {

    /**
     * Link to the Stash configuration
     */
    private final StashConfiguration configuration;

    /**
     * Project in Stash
     */
    private final String project;

    /**
     * Repository in the project
     */
    private final String repository;

}
