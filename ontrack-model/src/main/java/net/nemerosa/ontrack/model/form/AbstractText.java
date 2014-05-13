package net.nemerosa.ontrack.model.form;

import lombok.Data;
import org.apache.commons.lang3.Validate;

@Data
public abstract class AbstractText<F extends AbstractText<F>> extends AbstractField<F> {

    private int length = 300;

    protected AbstractText(String name) {
        super(name);
    }

    public F length(int value) {
        Validate.isTrue(value >= 0);
        this.length = value;
        //noinspection unchecked
        return (F) this;
    }
}
