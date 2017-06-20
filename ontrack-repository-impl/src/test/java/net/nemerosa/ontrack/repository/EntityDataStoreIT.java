package net.nemerosa.ontrack.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.IntNode;
import net.nemerosa.ontrack.common.Time;
import net.nemerosa.ontrack.model.structure.Branch;
import net.nemerosa.ontrack.model.structure.Signature;
import net.nemerosa.ontrack.repository.support.store.EntityDataStore;
import net.nemerosa.ontrack.repository.support.store.EntityDataStoreRecord;
import net.nemerosa.ontrack.repository.support.store.EntityDataStoreRecordAudit;
import net.nemerosa.ontrack.repository.support.store.EntityDataStoreRecordAuditType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static net.nemerosa.ontrack.test.TestUtils.assertJsonEquals;
import static net.nemerosa.ontrack.test.TestUtils.uid;
import static org.junit.Assert.*;

/**
 * Integration tests for {@link EntityDataStore}.
 */
public class EntityDataStoreIT extends AbstractRepositoryTestSupport {

    public static final String CATEGORY = "TEST";
    public static final String TEST_USER = "test";
    @Autowired
    private EntityDataStore store;

    @Test
    public void add() throws JsonProcessingException {
        // Entity
        Branch branch = do_create_branch();
        // Adds some data
        String name = uid("T");
        EntityDataStoreRecord record = store.add(branch, CATEGORY, name, Signature.of(TEST_USER), null, new IntNode(15));
        // Checks
        assertNotNull(record);
        assertTrue(record.getId() > 0);
        assertEquals(CATEGORY, record.getCategory());
        assertNotNull(record.getSignature());
        assertEquals(name, record.getName());
        assertNull(record.getGroupName());
        assertEquals(branch.getProjectEntityType(), record.getEntity().getProjectEntityType());
        assertEquals(branch.getId(), record.getEntity().getId());
        assertJsonEquals(
                new IntNode(15),
                record.getData()
        );
    }

    @Test
    public void replaceOrAdd() throws JsonProcessingException {
        // Entity
        Branch branch = do_create_branch();
        // Adds some data
        String name = uid("T");
        EntityDataStoreRecord record = store.add(branch, CATEGORY, name, Signature.of(TEST_USER), null, new IntNode(15));
        // Checks
        assertTrue(record.getId() > 0);
        assertEquals(CATEGORY, record.getCategory());
        assertJsonEquals(
                new IntNode(15),
                record.getData()
        );
        // Updates the same name
        EntityDataStoreRecord secondRecord = store.replaceOrAdd(branch, CATEGORY, name, Signature.of(TEST_USER), null, new IntNode(16));
        // Checks
        assertEquals(record.getId(), secondRecord.getId());
        assertJsonEquals(
                new IntNode(16),
                secondRecord.getData()
        );
    }

    @Test
    public void replaveOrAddForNewRecord() throws JsonProcessingException {
        // Entity
        Branch branch = do_create_branch();
        // Adds some data
        String name = uid("T");
        EntityDataStoreRecord record = store.add(branch, CATEGORY, name, Signature.of(TEST_USER), null, new IntNode(15));
        // Checks
        assertTrue(record.getId() > 0);
        assertEquals(CATEGORY, record.getCategory());
        assertJsonEquals(
                new IntNode(15),
                record.getData()
        );
        // Updates with a different same name
        EntityDataStoreRecord secondRecord = store.replaceOrAdd(branch, CATEGORY, name + "2", Signature.of(TEST_USER), null, new IntNode(16));
        // Checks
        assertNotEquals(record.getId(), secondRecord.getId());
        assertJsonEquals(
                new IntNode(16),
                secondRecord.getData()
        );
    }

    @Test
    public void audit() throws JsonProcessingException {
        // Entity
        Branch branch = do_create_branch();
        // Adds some data
        String name = uid("T");
        EntityDataStoreRecord record = store.add(branch, CATEGORY, name, Signature.of(TEST_USER), null, new IntNode(15));
        // Gets the audit
        List<EntityDataStoreRecordAudit> audit = store.getRecordAudit(record.getId());
        assertEquals(1, audit.size());
        assertEquals(EntityDataStoreRecordAuditType.CREATED, audit.get(0).getType());
        assertEquals(TEST_USER, audit.get(0).getSignature().getUser().getName());
        // Updates the same name
        store.replaceOrAdd(branch, CATEGORY, name, Signature.of("other"), null, new IntNode(16));
        // Checks
        audit = store.getRecordAudit(record.getId());
        assertEquals(2, audit.size());
        assertEquals(EntityDataStoreRecordAuditType.UPDATED, audit.get(0).getType());
        assertEquals("other", audit.get(0).getSignature().getUser().getName());
        assertEquals(EntityDataStoreRecordAuditType.CREATED, audit.get(1).getType());
        assertEquals(TEST_USER, audit.get(1).getSignature().getUser().getName());
    }

    @Test
    public void delete_by_name() {
        // Entity
        Branch branch = do_create_branch();
        // Adds some data
        String name = uid("T");
        int id = store.add(branch, CATEGORY, name, Signature.of(TEST_USER), null, new IntNode(15)).getId();
        // Gets by ID
        assertTrue(store.getById(branch, id).isPresent());
        // Deletes by name
        store.deleteByName(branch, CATEGORY, name);
        // Gets by ID ot possible any longer
        assertFalse(store.getById(branch, id).isPresent());
    }

    @Test
    public void delete_by_group() {
        // Entity
        Branch branch = do_create_branch();
        // Adds some data for the group
        String group = uid("G");
        String name = uid("T");
        int id1 = store.add(branch, CATEGORY, name + 1, Signature.of(TEST_USER), group, new IntNode(10)).getId();
        int id2 = store.add(branch, CATEGORY, name + 2, Signature.of(TEST_USER), group, new IntNode(10)).getId();
        // Gets by ID
        assertTrue(store.getById(branch, id1).isPresent());
        assertTrue(store.getById(branch, id2).isPresent());
        // Deletes by group
        store.deleteByGroup(branch, CATEGORY, group);
        // Gets by ID ot possible any longer
        assertFalse(store.getById(branch, id1).isPresent());
        assertFalse(store.getById(branch, id2).isPresent());
    }

    @Test
    public void delete_by_category() {
        // Entities
        Branch branch1 = do_create_branch();
        Branch branch2 = do_create_branch();
        // Adds some data with same name for different entities
        String name = uid("T");
        int id1 = store.add(branch1, CATEGORY, name, Signature.of(TEST_USER), null, new IntNode(10)).getId();
        int id2 = store.add(branch2, CATEGORY, name, Signature.of(TEST_USER), null, new IntNode(10)).getId();
        // Gets by ID
        assertTrue(store.getById(branch1, id1).isPresent());
        assertTrue(store.getById(branch2, id2).isPresent());
        // Deletes by category
        store.deleteByCategoryBefore(CATEGORY, Time.now());
        // Gets by ID ot possible any longer
        assertFalse(store.getById(branch1, id1).isPresent());
        assertFalse(store.getById(branch2, id2).isPresent());
    }

    @Test
    public void last_by_category_and_name() throws JsonProcessingException {
        // Entity
        Branch branch = do_create_branch();
        // Adds some data, twice, for the same name
        String name = uid("T");
        @SuppressWarnings("unused") int id1 = store.add(branch, CATEGORY, name, Signature.of(TEST_USER), null, new IntNode(15)).getId();
        int id2 = store.add(branch, CATEGORY, name, Signature.of(TEST_USER), null, new IntNode(16)).getId();
        // Gets last by category / name
        EntityDataStoreRecord record = store.findLastByCategoryAndName(branch, CATEGORY, name, Time.now()).orElse(null);
        // Checks
        assertNotNull(record);
        assertEquals(record.getId(), id2);
        assertJsonEquals(
                new IntNode(16),
                record.getData()
        );
    }

    @Test
    public void last_by_category_and_group_and_name() throws JsonProcessingException {
        // Entity
        Branch branch = do_create_branch();
        // Adds some data, twice, for the same name, and several names, but for a same group
        String group = uid("G");
        String name1 = uid("T");
        String name2 = uid("T");
        @SuppressWarnings("unused") int id11 = store.add(branch, CATEGORY, name1, Signature.of(TEST_USER), group, new IntNode(11)).getId();
        int id12 = store.add(branch, CATEGORY, name1, Signature.of(TEST_USER), group, new IntNode(12)).getId();
        @SuppressWarnings("unused") int id21 = store.add(branch, CATEGORY, name2, Signature.of(TEST_USER), group, new IntNode(21)).getId();
        @SuppressWarnings("unused") int id22 = store.add(branch, CATEGORY, name2, Signature.of(TEST_USER), group, new IntNode(22)).getId();
        // Gets last by category / name / group
        EntityDataStoreRecord record = store.findLastByCategoryAndGroupAndName(branch, CATEGORY, group, name1).orElse(null);
        // Checks
        assertNotNull(record);
        assertEquals(record.getId(), id12);
        assertJsonEquals(
                new IntNode(12),
                record.getData()
        );
    }

    @Test
    public void last_by_category() throws JsonProcessingException {
        // Entity
        Branch branch = do_create_branch();
        // Adds some data, twice, for the same name, and several names, but for a same group
        String name1 = uid("T");
        String name2 = uid("T");
        String name3 = uid("T");
        @SuppressWarnings("unused") int id11 = store.add(branch, CATEGORY, name1, Signature.of(TEST_USER), null, new IntNode(11)).getId();
        int id12 = store.add(branch, CATEGORY, name1, Signature.of(TEST_USER), null, new IntNode(12)).getId();
        @SuppressWarnings("unused") int id21 = store.add(branch, CATEGORY, name2, Signature.of(TEST_USER), null, new IntNode(21)).getId();
        int id22 = store.add(branch, CATEGORY, name2, Signature.of(TEST_USER), null, new IntNode(22)).getId();
        @SuppressWarnings("unused") int id31 = store.add(branch, CATEGORY, name3, Signature.of(TEST_USER), null, new IntNode(31)).getId();
        @SuppressWarnings("unused") int id32 = store.add(branch, CATEGORY, name3, Signature.of(TEST_USER), null, new IntNode(32)).getId();
        int id33 = store.add(branch, CATEGORY, name3, Signature.of(TEST_USER), null, new IntNode(33)).getId();
        // Gets last by name in category
        List<EntityDataStoreRecord> records = store.findLastRecordsByNameInCategory(branch, CATEGORY);
        assertEquals(3, records.size());
        // Checks
        assertEquals(id33, records.get(0).getId());
        assertEquals(id22, records.get(1).getId());
        assertEquals(id12, records.get(2).getId());
    }

    @Test
    public void addObject() throws JsonProcessingException {
        // Entity
        Branch branch = do_create_branch();
        // Adds some data
        String name = uid("T");
        EntityDataStoreRecord record = store.addObject(branch, CATEGORY, name, Signature.of(TEST_USER), null, 15);
        // Checks
        assertNotNull(record);
        assertTrue(record.getId() > 0);
        assertEquals(CATEGORY, record.getCategory());
        assertNotNull(record.getSignature());
        assertEquals(name, record.getName());
        assertNull(record.getGroupName());
        assertEquals(branch.getProjectEntityType(), record.getEntity().getProjectEntityType());
        assertEquals(branch.getId(), record.getEntity().getId());
        assertJsonEquals(
                new IntNode(15),
                record.getData()
        );
    }

    @Test
    public void replaceOrAddObject() throws JsonProcessingException {
        // Entity
        Branch branch = do_create_branch();
        // Adds some data
        String name = uid("T");
        EntityDataStoreRecord record = store.addObject(branch, CATEGORY, name, Signature.of(TEST_USER), null, 15);
        // Checks
        assertTrue(record.getId() > 0);
        assertEquals(CATEGORY, record.getCategory());
        assertJsonEquals(
                new IntNode(15),
                record.getData()
        );
        // Updates the same name
        EntityDataStoreRecord secondRecord = store.replaceOrAddObject(branch, CATEGORY, name, Signature.of(TEST_USER), null, 16);
        // Checks
        assertEquals(record.getId(), secondRecord.getId());
        assertJsonEquals(
                new IntNode(16),
                secondRecord.getData()
        );
    }

}
