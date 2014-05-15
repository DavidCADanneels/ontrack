package net.nemerosa.ontrack.ui.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.net.URI;

/**
 * Definition of a link. A link has a URI and a name.
 */
@Data
public class Link {

    public static final String CREATE = "create";
    public static final String UPDATE = "update";

    @JsonIgnore
    private final String name;
    private final URI href;

    public static Link of(String name, URI uri) {
        return new Link(name, uri);
    }

}
