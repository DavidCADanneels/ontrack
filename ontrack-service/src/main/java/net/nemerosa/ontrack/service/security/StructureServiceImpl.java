package net.nemerosa.ontrack.service.security;

import net.nemerosa.ontrack.model.exceptions.ImageFileSizeException;
import net.nemerosa.ontrack.model.exceptions.ImageTypeNotAcceptedException;
import net.nemerosa.ontrack.model.security.*;
import net.nemerosa.ontrack.model.structure.*;
import net.nemerosa.ontrack.repository.StructureRepository;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static net.nemerosa.ontrack.model.structure.Entity.isEntityDefined;
import static net.nemerosa.ontrack.model.structure.Entity.isEntityNew;

@Service
@Transactional
public class StructureServiceImpl implements StructureService {

    private static final long ICON_IMAGE_SIZE_MAX = 16 * 1000L;

    private static final String[] ACCEPTED_IMAGE_TYPES = {
            "image/jpeg",
            "image/png",
            "image/gif"
    };

    private final SecurityService securityService;
    private final StructureRepository structureRepository;

    @Autowired
    public StructureServiceImpl(SecurityService securityService, StructureRepository structureRepository) {
        this.securityService = securityService;
        this.structureRepository = structureRepository;
    }

    @Override
    public Project newProject(Project project) {
        isEntityNew(project, "Project must be defined");
        securityService.checkGlobalFunction(ProjectCreation.class);
        return structureRepository.newProject(project);
    }

    @Override
    public List<Project> getProjectList() {
        List<Project> list = structureRepository.getProjectList();
        if (securityService.isGlobalFunctionGranted(ProjectList.class)) {
            return list;
        } else {
            return list.stream()
                    .filter(p -> securityService.isProjectFunctionGranted(p.id(), ProjectView.class))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public Project getProject(ID projectId) {
        securityService.checkProjectFunction(projectId.getValue(), ProjectView.class);
        return structureRepository.getProject(projectId);
    }

    @Override
    public void saveProject(Project project) {
        isEntityDefined(project, "Project must be defined");
        securityService.checkProjectFunction(project.id(), ProjectEdit.class);
        structureRepository.saveProject(project);
    }

    @Override
    public Branch getBranch(ID branchId) {
        Branch branch = structureRepository.getBranch(branchId);
        securityService.checkProjectFunction(branch.getProject().id(), ProjectView.class);
        return branch;
    }

    @Override
    public List<Branch> getBranchesForProject(ID projectId) {
        securityService.checkProjectFunction(projectId.getValue(), ProjectView.class);
        return structureRepository.getBranchesForProject(projectId);
    }

    @Override
    public Branch newBranch(Branch branch) {
        // Validation
        isEntityNew(branch, "Branch must be new");
        isEntityDefined(branch.getProject(), "Project must be defined");
        // Security
        securityService.checkProjectFunction(branch.getProject().id(), BranchCreate.class);
        // OK
        return structureRepository.newBranch(branch);
    }

    @Override
    public Build newBuild(Build build) {
        // Validation
        isEntityNew(build, "Build must be new");
        isEntityDefined(build.getBranch(), "Branch must be defined");
        isEntityDefined(build.getBranch().getProject(), "Project must be defined");
        // Security
        securityService.checkProjectFunction(build.getBranch().getProject().id(), BuildCreate.class);
        // Repository
        return structureRepository.newBuild(build);
    }

    @Override
    public Build saveBuild(Build build) {
        // Validation
        isEntityDefined(build, "Build must be defined");
        isEntityDefined(build.getBranch(), "Branch must be defined");
        isEntityDefined(build.getBranch().getProject(), "Project must be defined");
        // Security
        securityService.checkProjectFunction(build.getBranch().getProject().id(), ProjectEdit.class);
        // Repository
        return structureRepository.saveBuild(build);
    }

    @Override
    public List<PromotionLevel> getPromotionLevelListForBranch(ID branchId) {
        Branch branch = getBranch(branchId);
        securityService.checkProjectFunction(branch.getProject().id(), ProjectView.class);
        return structureRepository.getPromotionLevelListForBranch(branchId);
    }

    @Override
    public PromotionLevel newPromotionLevel(PromotionLevel promotionLevel) {
        // Validation
        isEntityNew(promotionLevel, "Promotion level must be new");
        isEntityDefined(promotionLevel.getBranch(), "Branch must be defined");
        isEntityDefined(promotionLevel.getBranch().getProject(), "Project must be defined");
        // Security
        securityService.checkProjectFunction(promotionLevel.getBranch().getProject().id(), PromotionLevelCreate.class);
        // Repository
        return structureRepository.newPromotionLevel(promotionLevel);
    }

    @Override
    public PromotionLevel getPromotionLevel(ID promotionLevelId) {
        PromotionLevel promotionLevel = structureRepository.getPromotionLevel(promotionLevelId);
        securityService.checkProjectFunction(promotionLevel.getBranch().getProject().id(), ProjectView.class);
        return promotionLevel;
    }

    @Override
    public Document getPromotionLevelImage(ID promotionLevelId) {
        // Checks access
        getPromotionLevel(promotionLevelId);
        // Repository access
        return structureRepository.getPromotionLevelImage(promotionLevelId);
    }

    @Override
    public void setPromotionLevelImage(ID promotionLevelId, Document document) {
        checkImage(document);

        // Checks access
        PromotionLevel promotionLevel = getPromotionLevel(promotionLevelId);
        securityService.checkProjectFunction(promotionLevel.getBranch().getProject().id(), PromotionLevelEdit.class);
        // Repository
        structureRepository.setPromotionLevelImage(promotionLevelId, document);
    }

    @Override
    public List<ValidationStamp> getValidationStampListForBranch(ID branchId) {
        Branch branch = getBranch(branchId);
        securityService.checkProjectFunction(branch.getProject().id(), ProjectView.class);
        return structureRepository.getValidationStampListForBranch(branchId);
    }

    @Override
    public ValidationStamp newValidationStamp(ValidationStamp validationStamp) {
        // Validation
        isEntityNew(validationStamp, "Validation stamp must be new");
        isEntityDefined(validationStamp.getBranch(), "Branch must be defined");
        isEntityDefined(validationStamp.getBranch().getProject(), "Project must be defined");
        // Security
        securityService.checkProjectFunction(validationStamp.getBranch().getProject().id(), ValidationStampCreate.class);
        // Repository
        return structureRepository.newValidationStamp(validationStamp);
    }

    @Override
    public ValidationStamp getValidationStamp(ID validationStampId) {
        ValidationStamp validationStamp = structureRepository.getValidationStamp(validationStampId);
        securityService.checkProjectFunction(validationStamp.getBranch().getProject().id(), ProjectView.class);
        return validationStamp;
    }

    @Override
    public Document getValidationStampImage(ID validationStampId) {
        // Checks access
        getValidationStamp(validationStampId);
        // Repository access
        return structureRepository.getValidationStampImage(validationStampId);
    }

    @Override
    public void setValidationStampImage(ID validationStampId, Document document) {
        // Checks the image type
        checkImage(document);
        // Checks access
        ValidationStamp validationStamp = getValidationStamp(validationStampId);
        securityService.checkProjectFunction(validationStamp.getBranch().getProject().id(), ValidationStampEdit.class);
        // Repository
        structureRepository.setValidationStampImage(validationStampId, document);
    }

    protected void checkImage(Document document) {
        // Checks the image type
        if (document != null && !ArrayUtils.contains(ACCEPTED_IMAGE_TYPES, document.getType())) {
            throw new ImageTypeNotAcceptedException(document.getType(), ACCEPTED_IMAGE_TYPES);
        }
        // Checks the image length
        int size = document != null ? document.getContent().length : 0;
        if (size > ICON_IMAGE_SIZE_MAX) {
            throw new ImageFileSizeException(size, ICON_IMAGE_SIZE_MAX);
        }
    }
}
