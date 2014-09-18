package net.nemerosa.ontrack.extension.general;

import net.nemerosa.ontrack.model.form.Form;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ReleasePropertyTypeTest {

    @Test
    public void getEditionForm_empty() throws Exception {
        ReleasePropertyType type = new ReleasePropertyType();
        Form form = type.getEditionForm(null);
        assertNull(form.getField("name").getValue());
    }

    @Test
    public void getEditionForm_not_empty() throws Exception {
        ReleasePropertyType type = new ReleasePropertyType();
        Form form = type.getEditionForm(new ReleaseProperty("test"));
        assertEquals("test", form.getField("name").getValue());
    }
}