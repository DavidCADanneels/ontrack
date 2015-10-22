package net.nemerosa.ontrack.extension.svn.model;


import com.fasterxml.jackson.databind.JsonNode;
import net.nemerosa.ontrack.model.form.Form;

import java.util.function.Function;

/**
 * Defines the way to link builds to Svn revision, in order to manage the change logs, the Svn searches
 * and synchronisations.
 *
 * @param <T> Type of configuration data
 */
public interface BuildSvnRevisionLink<T> {

    // Meta information

    /**
     * ID of the link
     */
    String getId();

    /**
     * Display name for the link
     */
    String getName();

    /**
     * Clones the configuration.
     */
    T clone(T data, Function<String, String> replacementFunction);

    // Configuration

    /**
     * Parses the configuration from a JSON node
     */
    T parseData(JsonNode node);

    /**
     * Formats the configuration data as JSON
     */
    JsonNode toJson(T data);

    /**
     * Creates a form for the edition of the link configuration.
     */
    Form getForm();

    // TODO SVN integration

}
