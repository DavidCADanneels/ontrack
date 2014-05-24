package net.nemerosa.ontrack.model.form;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
public class Selection extends AbstractField<Selection> {

    public static Selection of(String name) {
        return new Selection(name);
    }

    private List<?> items = new ArrayList<>();
    private String itemId = "id";
    private String itemName = "name";

    protected Selection(String name) {
        super("selection", name);
    }

    public Selection items(List<?> values) {
        this.items = values;
        return this;
    }

    public Selection itemId(String value) {
        this.itemId = value;
        return this;
    }

    public Selection itemName(String value) {
        this.itemName = value;
        return this;
    }
}
