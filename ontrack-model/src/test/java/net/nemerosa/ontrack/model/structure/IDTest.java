package net.nemerosa.ontrack.model.structure;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.nemerosa.ontrack.json.JsonUtils;
import org.junit.Test;

import static net.nemerosa.ontrack.test.TestUtils.assertJsonWrite;
import static org.junit.Assert.*;

public class IDTest {

    @Test
    public void none() {
        ID id = ID.NONE;
        assertNotNull(id);
        assertFalse(id.isSet());
        assertEquals(0, id.getValue());
        assertEquals("0", id.toString());
    }

    @Test
    public void set() {
        ID id = ID.of(1);
        assertNotNull(id);
        assertTrue(id.isSet());
        assertEquals(1, id.getValue());
        assertEquals("1", id.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void not_zero() {
        ID.of(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void not_negative() {
        ID.of(-1);
    }

    @Test
    public void set_to_json() throws JsonProcessingException {
        assertJsonWrite(
                JsonUtils.number(12),
                ID.of(12)
        );
    }

    @Test
    public void unset_to_json() throws JsonProcessingException {
        assertJsonWrite(
                JsonUtils.number(0),
                ID.NONE
        );
    }

}
