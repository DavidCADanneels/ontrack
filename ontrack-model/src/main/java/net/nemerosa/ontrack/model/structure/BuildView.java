package net.nemerosa.ontrack.model.structure;

import lombok.Data;

import java.util.List;

/**
 * Build, and its list of promotions runs, and its list of validation runs.
 */
@Data
public class BuildView implements View {

    /**
     * The build for this view
     */
    private final Build build;

    /**
     * The list of its promotion runs
     */
    private final List<PromotionRun> promotionRuns;

    /**
     * The list of its validation runs, one for each validation stamp.
     */
    private final List<ValidationStampRunView> validationStampRunViews;

}
