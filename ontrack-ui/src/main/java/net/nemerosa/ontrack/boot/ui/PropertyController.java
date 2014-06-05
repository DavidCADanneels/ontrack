package net.nemerosa.ontrack.boot.ui;

import net.nemerosa.ontrack.model.structure.*;
import net.nemerosa.ontrack.ui.controller.AbstractResourceController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * UI end point for the management of properties.
 */
@RestController
@RequestMapping("/properties")
public class PropertyController extends AbstractResourceController {

    private final PropertyService propertyService;
    private final StructureService structureService;

    @Autowired
    public PropertyController(PropertyService propertyService, StructureService structureService) {
        this.propertyService = propertyService;
        this.structureService = structureService;
    }

    /**
     * Gets the list of properties for a given entity and for the current user.
     *
     * @param entityType Entity type
     * @param id         Entity ID
     * @return List of properties
     */
    @RequestMapping(value = "{entityType}/{id}/view", method = RequestMethod.GET)
    public List<Property<?>> getProperties(@PathVariable ProjectEntityType entityType, @PathVariable ID id) {
        ProjectEntity entity = getEntity(entityType, id);
        return propertyService.getProperties(entity);
    }

    /**
     * Gets the list of editable properties for a given entity and for the current user.
     *
     * @param entityType Entity type
     * @param id         Entity ID
     * @return List of editable properties
     */
    @RequestMapping(value = "{entityType}/{id}/editable", method = RequestMethod.GET)
    public List<PropertyTypeDescriptor> getEditableProperties(@PathVariable ProjectEntityType entityType, @PathVariable ID id) {
        ProjectEntity entity = getEntity(entityType, id);
        return propertyService.getEditableProperties(entity);
    }

    protected ProjectEntity getEntity(ProjectEntityType entityType, ID id) {
        return entityType.getEntityFn(structureService).apply(id);
    }
}
