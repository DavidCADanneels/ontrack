package net.nemerosa.ontrack.service;

import net.nemerosa.ontrack.extension.api.ExtensionManager;
import net.nemerosa.ontrack.extension.api.PropertyTypeExtension;
import net.nemerosa.ontrack.model.exceptions.PropertyTypeNotFoundException;
import net.nemerosa.ontrack.model.form.Form;
import net.nemerosa.ontrack.model.security.SecurityService;
import net.nemerosa.ontrack.model.structure.*;
import net.nemerosa.ontrack.repository.PropertyRepository;
import net.nemerosa.ontrack.repository.TProperty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final SecurityService securityService;
    private final ExtensionManager extensionManager;

    @Autowired
    public PropertyServiceImpl(PropertyRepository propertyRepository, SecurityService securityService, ExtensionManager extensionManager) {
        this.propertyRepository = propertyRepository;
        this.securityService = securityService;
        this.extensionManager = extensionManager;
    }

    @Override
    public List<PropertyType<?>> getPropertyTypes() {
        Collection<PropertyTypeExtension> extensions = extensionManager.getExtensions(PropertyTypeExtension.class);
        List<PropertyType<?>> propertyTypes = new ArrayList<>();
        for (PropertyTypeExtension extension : extensions) {
            propertyTypes.add(
                    extension.getPropertyType()
            );
        }
        return propertyTypes;
    }

    protected <T> PropertyType<T> getPropertyTypeByName(String propertyTypeName) {
        //noinspection unchecked
        return (PropertyType<T>) getPropertyTypes().stream()
                .filter(p -> StringUtils.equals(propertyTypeName, p.getClass().getName()))
                .findFirst()
                .orElseThrow(() -> new PropertyTypeNotFoundException(propertyTypeName));
    }

    @Override
    public List<Property<?>> getProperties(ProjectEntity entity) {
        // With all the existing properties...
        return getPropertyTypes().stream()
                // ... filters them by entity
                .filter(type -> type.applies(entity.getClass()))
                        // ... filters them by access right
                .filter(type -> type.canView(entity, securityService))
                        // ... loads them from the store
                .map(type -> getProperty(type, entity))
                        // ... removes the null values
                .filter(prop -> prop != null)
                        // ... and returns them
                .collect(Collectors.toList());
    }

    @Override
    public <T> Property<T> getProperty(ProjectEntity entity, String propertyTypeName) {
        // Gets the property using its fully qualified type name
        PropertyType<T> propertyType = getPropertyTypeByName(propertyTypeName);
        // Access
        return getProperty(propertyType, entity);
    }

    protected <T> Property<T> getProperty(PropertyType<T> type, ProjectEntity entity) {
        T value = getPropertyValue(type, entity);
        return value != null ? Property.of(type, value) : null;
    }

    protected <T> T getPropertyValue(PropertyType<T> type, ProjectEntity entity) {
        // Checks for edition
        if (!type.canView(entity, securityService)) {
            throw new AccessDeniedException("Property is not opened for viewing.");
        }
        // Gets the raw information from the repository
        TProperty t = propertyRepository.loadProperty(
                type.getClass().getName(),
                entity.getProjectEntityType(),
                entity.getId());
        // If null, returns null
        if (t == null) {
            return null;
        }
        // Converts the stored value into an actual value
        return type.fromStorage(t.getJson());
    }

    @Override
    public List<PropertyTypeDescriptor> getEditableProperties(ProjectEntity entity) {
        //noinspection Convert2MethodRef
        return getPropertyTypes().stream()
                .filter(p -> p.applies(entity.getClass()))
                .filter(p -> p.canEdit(entity, securityService))
                .map(p -> PropertyTypeDescriptor.of(p))
                .collect(Collectors.toList());
    }

    @Override
    public Form getPropertyEditionForm(ProjectEntity entity, String propertyTypeName) {
        // Gets the property using its fully qualified type name
        PropertyType<?> propertyType = getPropertyTypeByName(propertyTypeName);
        // Gets the edition form for this type
        return getPropertyEditionForm(entity, propertyType);
    }

    protected <T> Form getPropertyEditionForm(ProjectEntity entity, PropertyType<T> propertyType) {
        // Checks for edition
        if (!propertyType.canEdit(entity, securityService)) {
            throw new AccessDeniedException("Property is not opened for edition.");
        }
        // Gets the value for this property
        T value = getPropertyValue(propertyType, entity);
        // Gets the form
        return propertyType.getEditionForm(Optional.ofNullable(value));
    }
}
