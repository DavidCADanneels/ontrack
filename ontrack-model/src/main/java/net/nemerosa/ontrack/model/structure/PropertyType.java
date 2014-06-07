package net.nemerosa.ontrack.model.structure;

import com.fasterxml.jackson.databind.JsonNode;
import net.nemerosa.ontrack.model.form.Form;
import net.nemerosa.ontrack.model.security.SecurityService;

import java.util.Set;

/**
 * Defines the type for a property.
 *
 * @param <T> Type of object supported by this type
 */
public interface PropertyType<T> {

    /**
     * Display name for this property
     */
    String getName();

    /**
     * Description for this property
     */
    String getDescription();

    /**
     * Icon path.
     * <p/>
     * This path is relative to the Web root. It may be <code>null</code> if no icon is associated to this property.
     */
    String getIconPath();

    /**
     * Template path for the short representation. It is mainly used for short lists.
     * See {@link #getFullTemplatePath()} for an explanation.
     */
    String getShortTemplatePath();

    /**
     * Template path for the full representation. It is used for the full representation of the property value.
     * <p/>
     * This path is relative to the Web root and is merged with the property raw value.
     */
    String getFullTemplatePath();

    /**
     * List of entity types this property applies to.
     */
    Set<ProjectEntityType> getSupportedEntityTypes();

    /**
     * Edition policy.
     * <p/>
     * Can this property be directly edited by a used on the given
     * associated entity.
     *
     * @param entity          Entity where to edit the property
     * @param securityService The access to the security layer
     * @return Authorization policy for this entity
     */
    boolean canEdit(ProjectEntity entity, SecurityService securityService);

    /**
     * Defines the authorization policy for viewing this property.
     *
     * @param entity          Entity where to view the property
     * @param securityService The access to the security layer
     * @return Authorization policy for this entity
     */
    boolean canView(ProjectEntity entity, SecurityService securityService);

    /**
     * TODO Obfuscates any sensitive value from the property value before it is sent at client side.
     */

    /**
     * Form to create/update this property.
     *
     * @param value Value to update if set. If not set, this means the creation of a new property.
     */
    Form getEditionForm(T value);

    /**
     * Creation of a property value from a value. Should perform validation.
     */
    Property<T> of(T value);

    /**
     * Gets the JSON representation of a property value
     */
    JsonNode forStorage(T value);

    /**
     * Parses the client JSON representation for a property value. This method
     * <i>MUST</i> perform validation before accepting the value.
     *
     * @see #getEditionForm(Object)
     */
    T fromClient(JsonNode node);

    /**
     * Parses the storage JSON representation for a property value.
     */
    T fromStorage(JsonNode node);

    /**
     * Parses the JSON representation for a property value and creates the property value directly.
     *
     * @see #fromStorage(com.fasterxml.jackson.databind.JsonNode)
     * @see #of(Object)
     */
    default Property<T> of(JsonNode node) {
        return of(fromStorage(node));
    }

    /**
     * Given the value of a property, computes some text that can be used as a hint to search among all
     * the properties when looking for a key word. Typically, in a database store, the resulting value
     * will be stored in an indexed column.
     *
     * @param value Value to index
     * @return Index value
     */
    String getSearchKey(T value);
}
