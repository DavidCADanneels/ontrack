package net.nemerosa.ontrack.model.structure;

import lombok.Data;
import net.nemerosa.ontrack.model.form.Form;

@Data
public class Project {

    private final ID id;
    private final String name;
    private final String description;

    public Project withId(ID id) {
        return new Project(id, name, description);
    }

    public static Project of(NameDescription nameDescription) {
        return new Project(ID.NONE, nameDescription.getName(), nameDescription.getDescription());
    }

    public static Form form() {
        return Form.nameAndDescription();
    }
}
