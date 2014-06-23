package net.nemerosa.ontrack.extension.svn.changelog;

import net.nemerosa.ontrack.extension.svn.SVNController;
import net.nemerosa.ontrack.ui.resource.AbstractResourceDecorator;
import net.nemerosa.ontrack.ui.resource.Link;
import net.nemerosa.ontrack.ui.resource.ResourceContext;

import java.util.List;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

public class SVNChangeLogResourceDecorator extends AbstractResourceDecorator<SVNChangeLog> {

    public SVNChangeLogResourceDecorator() {
        super(SVNChangeLog.class);
    }

    @Override
    public List<Link> links(SVNChangeLog resource, ResourceContext resourceContext) {
        return resourceContext.links()
                .link("_revisions", on(SVNController.class).changeLogRevisions(resource.getUuid()))
                // TODO Issues
                // TODO Files
                .build();
    }

}
