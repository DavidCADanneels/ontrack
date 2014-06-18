package net.nemerosa.ontrack.extension.svn;

import net.nemerosa.ontrack.extension.api.ExtensionFeatureDescription;
import net.nemerosa.ontrack.extension.issues.IssueServiceRegistry;
import net.nemerosa.ontrack.extension.support.AbstractExtensionController;
import net.nemerosa.ontrack.extension.svn.indexation.IndexationService;
import net.nemerosa.ontrack.model.Ack;
import net.nemerosa.ontrack.model.form.Form;
import net.nemerosa.ontrack.model.security.GlobalSettings;
import net.nemerosa.ontrack.model.security.SecurityService;
import net.nemerosa.ontrack.ui.resource.Link;
import net.nemerosa.ontrack.ui.resource.Resource;
import net.nemerosa.ontrack.ui.resource.Resources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

@RestController
@RequestMapping("extension/svn")
public class SVNController extends AbstractExtensionController<SVNExtensionFeature> {

    private final SVNConfigurationService svnConfigurationService;
    private final IndexationService indexationService;
    private final IssueServiceRegistry issueServiceRegistry;
    private final SecurityService securityService;

    @Autowired
    public SVNController(SVNExtensionFeature feature, SVNConfigurationService svnConfigurationService, IndexationService indexationService, IssueServiceRegistry issueServiceRegistry, SecurityService securityService) {
        super(feature);
        this.svnConfigurationService = svnConfigurationService;
        this.indexationService = indexationService;
        this.issueServiceRegistry = issueServiceRegistry;
        this.securityService = securityService;
    }

    @Override
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Resource<ExtensionFeatureDescription> getDescription() {
        return Resource.of(
                feature.getFeatureDescription(),
                uri(MvcUriComponentsBuilder.on(getClass()).getDescription())
        )
                .with("configurations", uri(on(getClass()).getConfigurations()), securityService.isGlobalFunctionGranted(GlobalSettings.class))
                ;
    }

    /**
     * Gets the configurations
     */
    @RequestMapping(value = "configurations", method = RequestMethod.GET)
    public Resources<SVNConfiguration> getConfigurations() {
        return Resources.of(
                svnConfigurationService.getConfigurations(),
                uri(on(getClass()).getConfigurations())
        )
                .with(Link.CREATE, uri(on(getClass()).getConfigurationForm()))
                ;
    }

    /**
     * Form for a configuration
     */
    @RequestMapping(value = "configurations/create", method = RequestMethod.GET)
    public Form getConfigurationForm() {
        return SVNConfiguration.form(issueServiceRegistry.getAvailableIssueServiceConfigurations());
    }

    /**
     * Creating a configuration
     */
    @RequestMapping(value = "configurations/create", method = RequestMethod.POST)
    public SVNConfiguration newConfiguration(@RequestBody SVNConfiguration configuration) {
        return svnConfigurationService.newConfiguration(configuration);
    }

    /**
     * Gets one configuration
     */
    @RequestMapping(value = "configurations/{name}", method = RequestMethod.GET)
    public SVNConfiguration getConfiguration(@PathVariable String name) {
        return svnConfigurationService.getConfiguration(name);
    }

    /**
     * Gets the last revision for a configuration
     */
    @RequestMapping(value = "configurations/{name}/indexation", method = RequestMethod.GET)
    @ResponseBody
    public LastRevisionInfo getLastRevisionInfo(@PathVariable String name) {
        return indexationService.getLastRevisionInfo(name);
    }

    /**
     * Indexation from latest
     */
    @RequestMapping(value = "configurations/{name}/indexation/latest", method = RequestMethod.POST)
    @ResponseBody
    public Ack indexFromLatest(@PathVariable String name) {
        // Full indexation
        if (indexationService.isIndexationRunning(name)) {
            return Ack.NOK;
        } else {
            indexationService.indexFromLatest(name);
            return Ack.OK;
        }
    }

    /**
     * Indexation of a range
     */
    @RequestMapping(value = "configurations/{name}/indexation/range", method = RequestMethod.POST)
    @ResponseBody
    public Ack indexRange(@PathVariable String name, @RequestParam long from, @RequestParam long to) {
        // Full indexation
        if (indexationService.isIndexationRunning(name)) {
            return Ack.NOK;
        } else {
            indexationService.indexRange(name, from, to);
            return Ack.OK;
        }
    }

    /**
     * Full indexation
     */
    @RequestMapping(value = "configurations/{name}/indexation/full", method = RequestMethod.POST)
    @ResponseBody
    public Ack full(@PathVariable String name) {
        // Full indexation
        if (indexationService.isIndexationRunning(name)) {
            return Ack.NOK;
        } else {
            indexationService.reindex(name);
            return Ack.OK;
        }
    }

    /**
     * Deleting one configuration
     */
    @RequestMapping(value = "configurations/{name}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Ack deleteConfiguration(@PathVariable String name) {
        svnConfigurationService.deleteConfiguration(name);
        return Ack.OK;
    }

    /**
     * Update form
     */
    @RequestMapping(value = "configurations/{name}/update", method = RequestMethod.GET)
    public Form updateConfigurationForm(@PathVariable String name) {
        return svnConfigurationService.getConfiguration(name).asForm(issueServiceRegistry.getAvailableIssueServiceConfigurations());
    }

    /**
     * Updating one configuration
     */
    @RequestMapping(value = "configurations/{name}/update", method = RequestMethod.PUT)
    public SVNConfiguration updateConfiguration(@PathVariable String name, @RequestBody SVNConfiguration configuration) {
        svnConfigurationService.updateConfiguration(name, configuration);
        return getConfiguration(name);
    }

}
