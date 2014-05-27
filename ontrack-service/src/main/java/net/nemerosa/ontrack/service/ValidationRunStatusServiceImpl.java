package net.nemerosa.ontrack.service;

import net.nemerosa.ontrack.model.exceptions.ValidationRunStatusUnknownDependencyException;
import net.nemerosa.ontrack.model.structure.ValidationRunStatusID;
import net.nemerosa.ontrack.model.structure.ValidationRunStatusService;
import net.nemerosa.ontrack.model.support.StartupService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ValidationRunStatusServiceImpl implements ValidationRunStatusService, StartupService {

    private final Logger logger = LoggerFactory.getLogger(ValidationRunStatusService.class);

    public static final String DEFECTIVE = "DEFECTIVE";
    public static final String EXPLAINED = "EXPLAINED";
    public static final String FAILED = "FAILED";
    public static final String FIXED = "FIXED";
    public static final String INTERRUPTED = "INTERRUPTED";
    public static final String INVESTIGATING = "INVESTIGATING";
    public static final String PASSED = "PASSED";
    public static final String WARNING = "WARNING";

    private final Map<String, ValidationRunStatusID> statuses = new LinkedHashMap<>();

    @Override
    public Collection<ValidationRunStatusID> getValidationRunStatusList() {
        return statuses.values();
    }

    @Override
    public Optional<ValidationRunStatusID> getValidationRunStatus(String id) {
        return Optional.ofNullable(statuses.get(id));
    }

    @Override
    public String getName() {
        return "Loading of validation run statuses";
    }

    @Override
    public int startupOrder() {
        return 10;
    }

    /**
     * Registers the tree of validation run status ids.
     */
    @Override
    public void start() {
        register(PASSED);
        register(WARNING);
        register(FIXED);
        register(DEFECTIVE);
        register(EXPLAINED, FIXED);
        register(INVESTIGATING, DEFECTIVE, EXPLAINED, FIXED);
        register(INTERRUPTED, INVESTIGATING, FIXED);
        register(FAILED, INTERRUPTED, INVESTIGATING, EXPLAINED, DEFECTIVE);
        // TODO Participation from extensions
        // Checks the tree
        for (ValidationRunStatusID statusID : statuses.values()) {
            for (String nextStatus : statusID.getFollowingStatuses()) {
                if (!statuses.containsKey(nextStatus)) {
                    throw new ValidationRunStatusUnknownDependencyException(statusID.getId(), nextStatus);
                }
            }
        }
        // Logging
        for (ValidationRunStatusID statusID : statuses.values()) {
            logger.info(
                    "[status] {} -> {}",
                    statusID.getId(),
                    StringUtils.join(statusID.getFollowingStatuses(), ",")
            );
        }
    }

    private void register(String status, String... followingStatuses) {
        if (statuses.containsKey(status)) {
            ValidationRunStatusID statusID = statuses.get(status);
            statuses.put(status, statusID.addDependencies(followingStatuses));
        } else {
            statuses.put(status, new ValidationRunStatusID(status, Arrays.asList(followingStatuses)));
        }
    }

}
