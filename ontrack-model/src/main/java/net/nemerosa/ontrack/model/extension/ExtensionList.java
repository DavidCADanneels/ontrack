package net.nemerosa.ontrack.model.extension;

import lombok.Data;

import java.util.List;

/**
 * List of all extensions, and the relationship between them.
 */
@Data
public class ExtensionList {

    /**
     * Raw list of extensions
     */
    private final List<ExtensionFeatureDescription> extensions;

}
