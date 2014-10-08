package net.nemerosa.ontrack.repository;

import com.fasterxml.jackson.databind.JsonNode;
import net.nemerosa.ontrack.model.Ack;
import net.nemerosa.ontrack.model.structure.ID;

import java.util.Collection;
import java.util.Optional;
import java.util.OptionalInt;

public interface BuildFilterRepository {

    Collection<TBuildFilter> findForBranch(int branchId);

    Collection<TBuildFilter> findForBranch(OptionalInt accountId, int branchId);

    Optional<TBuildFilter> findByBranchAndName(int accountId, int branchId, String name);

    Ack save(OptionalInt accountId, int branchId, String name, String type, JsonNode data);

    Ack delete(int accountId, int branchId, String name);
}
