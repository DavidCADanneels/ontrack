package net.nemerosa.ontrack.model.buildfilter;

import com.fasterxml.jackson.databind.JsonNode;
import net.nemerosa.ontrack.model.structure.Branch;
import net.nemerosa.ontrack.model.structure.Build;
import net.nemerosa.ontrack.model.structure.ID;

import java.util.List;
import java.util.Optional;

public interface BuildFilterProvider<T> {

    /**
     * Type
     */
    String getType();

    /**
     * Display name
     */
    String getName();

    /**
     * If this method returns <code>true</code>, there is no need to configure the filter.
     */
    boolean isPredefined();

    /**
     * Gets the form for a new filter on the given branch
     */
    BuildFilterForm newFilterForm(ID branchId);

    /**
     * Gets the form for a pre filled filter
     *
     * @param branchId Branch to filter
     * @param data     Filter data
     * @return Form
     */
    BuildFilterForm getFilterForm(ID branchId, T data);

    /**
     * Performs the filtering
     */
    default List<Build> filterBranchBuilds(Branch branch, T data) {
        throw new UnsupportedOperationException("Filter branch builds must be implemented for " + this.getClass());
    }

    /**
     * Builds an actual filter using the given set of parameters
     */
    @Deprecated
    default BuildFilter filter(ID branchId, T data) {
        throw new UnsupportedOperationException();
    }

    /**
     * Parses the filter data, provided as JSON, into an actual filter data object, when possible.
     *
     * @param data Filter data, as JSON
     * @return Filter data object, or empty when not possible to parse
     */
    Optional<T> parse(JsonNode data);

    default BuildFilterProviderData<T> withData(T data) {
        return BuildFilterProviderData.of(this, data);
    }
}
