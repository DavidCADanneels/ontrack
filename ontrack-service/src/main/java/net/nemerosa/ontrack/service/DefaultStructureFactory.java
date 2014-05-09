package net.nemerosa.ontrack.service;

import net.nemerosa.ontrack.model.structure.*;
import org.springframework.stereotype.Component;

@Component
public class DefaultStructureFactory implements StructureFactory {

    @Override
    public Project newProject(NameDescription nameDescription) {
        return new Project(
                ID.NONE,
                nameDescription.getName(),
                nameDescription.getDescription()
        );
    }

    @Override
    public Branch newBranch(Project project, NameDescription nameDescription) {
        return new Branch(
                ID.NONE,
                nameDescription.getName(),
                nameDescription.getDescription(),
                project
        );
    }

    @Override
    public PromotionLevel newPromotionLevel(Branch branch, NameDescription nameDescription) {
        // FIXME Method net.nemerosa.ontrack.service.DefaultStructureFactory.newPromotionLevel
        return null;
    }

    @Override
    public ValidationStamp newValidationStamp(Branch branch, NameDescription nameDescription) {
        // FIXME Method net.nemerosa.ontrack.service.DefaultStructureFactory.newValidationStamp
        return null;
    }

    @Override
    public Build newBuild(Branch branch, NameDescription nameDescription) {
        // FIXME Method net.nemerosa.ontrack.service.DefaultStructureFactory.newBuild
        return null;
    }

    @Override
    public PromotionRun newPromotionRun(Build build, PromotionLevel promotionLevel, Signature signature, String description) {
        // FIXME Method net.nemerosa.ontrack.service.DefaultStructureFactory.newPromotionRun
        return null;
    }

    @Override
    public ValidationRun newValidationRun(Build build, ValidationStamp validationStamp, ValidationRunStatusID statusID, Signature signature, String description) {
        // FIXME Method net.nemerosa.ontrack.service.DefaultStructureFactory.newValidationRun
        return null;
    }
}
