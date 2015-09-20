package net.nemerosa.ontrack.extension.api;

import net.nemerosa.ontrack.model.extension.Extension;
import net.nemerosa.ontrack.model.structure.PropertyType;

/**
 * This extension allows the definition of a property.
 */
@Deprecated
public interface PropertyTypeExtension<T> extends Extension {

    /**
     * Property type defined by this extension.
     */
    PropertyType<T> getPropertyType();

}
