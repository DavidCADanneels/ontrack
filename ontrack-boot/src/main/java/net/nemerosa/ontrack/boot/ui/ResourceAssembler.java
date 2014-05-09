package net.nemerosa.ontrack.boot.ui;

import net.nemerosa.ontrack.boot.resource.Resource;
import net.nemerosa.ontrack.model.structure.Project;

public interface ResourceAssembler {

    Resource<Project> toProjectResource(Project project);

}
