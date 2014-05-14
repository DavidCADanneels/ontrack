package net.nemerosa.ontrack.model.form;

import lombok.Data;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class Form {

    public static Form nameAndDescription() {
        return Form.create().name().description();
    }

    public static Form create() {
        return new Form();
    }

    private final Map<String, Field> fields = new LinkedHashMap<>();

    public Form name() {
        return with(Text.of("name").label("Name").length(40).regex("[A-Za-z0-9_\\.\\-]+"));
    }

    private Form description() {
        return with(Memo.of("description").label("Description").optional().length(500).rows(3));
    }

    public Form with(Field field) {
        fields.put(field.getName(), field);
        return this;
    }

    public Collection<? extends Field> getFields() {
        return fields.values();
    }

    public Form name(String value) {
        return fill("name", value);
    }

    public Form description(String value) {
        return fill("description", value);
    }

    public Form fill(String name, Object value) {
        Field field = fields.get(name);
        if (field != null) {
            field = field.value(value);
        } else {
            throw new FormFieldNotFoundException(name);
        }
        return this;
    }
}
