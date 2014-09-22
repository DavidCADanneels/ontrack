package net.nemerosa.ontrack.model.events;

import com.google.common.collect.Maps;
import lombok.Data;
import net.nemerosa.ontrack.model.structure.*;
import net.nemerosa.ontrack.model.support.NameValue;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.nemerosa.ontrack.model.events.PlainEventRenderer.INSTANCE;

/**
 * Definition of an event
 */
@Data
public final class Event {

    private static final Pattern EXPRESSION = Pattern.compile("\\$\\{([:a-zA-Z_]+)\\}");

    private final String template;
    private final Signature signature;
    private final Map<ProjectEntityType, ProjectEntity> entities;
    private final Map<String, NameValue> values;

    public String renderText() {
        return render(INSTANCE);
    }

    public String render(EventRenderer eventRenderer) {
        Matcher m = EXPRESSION.matcher(template);
        StringBuffer output = new StringBuffer();
        while (m.find()) {
            String value = expandExpression(m.group(1), eventRenderer);
            m.appendReplacement(output, value);
        }
        m.appendTail(output);
        return output.toString();
    }

    private String expandExpression(String expression, EventRenderer eventRenderer) {
        if (StringUtils.startsWith(expression, ":")) {
            String valueKey = expression.substring(1);
            NameValue value = values.get(valueKey);
            if (value == null) {
                throw new EventMissingValueException(template, valueKey);
            }
            return eventRenderer.render(valueKey, value, this);
        } else {
            // Project entity type
            ProjectEntityType projectEntityType = ProjectEntityType.valueOf(expression);
            // Gets the corresponding entity
            ProjectEntity projectEntity = entities.get(projectEntityType);
            if (projectEntity == null) {
                throw new EventMissingEntityException(template, projectEntityType);
            }
            // Rendering
            return eventRenderer.render(projectEntity, this);
        }
    }

    public static EventBuilder of(String template) {
        return new EventBuilder(template);
    }

    public Event withSignature(Signature signature) {
        return new Event(
                template,
                signature,
                entities,
                values
        );
    }

    public static class EventBuilder {

        private final String template;
        private Signature signature;
        private Collection<ProjectEntity> entities = new ArrayList<>();
        private Map<String, NameValue> values = new LinkedHashMap<>();

        public EventBuilder(String template) {
            this.template = template;
        }

        public EventBuilder with(Signature signature) {
            this.signature = signature;
            return this;
        }

        public EventBuilder withBuild(Build build) {
            return withBranch(build.getBranch()).with(build).with(build.getSignature());
        }

        public EventBuilder withPromotionRun(PromotionRun promotionRun) {
            return withBuild(promotionRun.getBuild()).with(promotionRun.getPromotionLevel()).with(promotionRun.getSignature());
        }

        public EventBuilder withValidationRun(ValidationRun validationRun) {
            return withBuild(validationRun.getBuild()).with(validationRun.getValidationStamp()).with(validationRun).with(validationRun.getLastStatus().getSignature());
        }

        public EventBuilder withBranch(Branch branch) {
            return withProject(branch.getProject()).with(branch);
        }

        public EventBuilder withProject(Project project) {
            return with(project);
        }

        private EventBuilder with(ProjectEntity entity) {
            entities.add(entity);
            return this;
        }

        public EventBuilder withValidationRunStatus(ValidationRunStatusID statusID) {
            return with("status", new NameValue(statusID.getId(), statusID.getName()));
        }

        public EventBuilder with(String name, NameValue value) {
            values.put(name, value);
            return this;
        }

        public EventBuilder with(String name, String value) {
            return with(name, new NameValue(name, value));
        }

        public Event get() {
            // Creates the event
            Event event = new Event(
                    template,
                    signature,
                    Maps.uniqueIndex(
                            entities,
                            ProjectEntity::getProjectEntityType
                    ),
                    values
            );
            // Checks the event can be resolved with all its references
            event.renderText();
            // OK
            return event;
        }
    }

    public static Event newProject(Project project) {
        return Event.of("New project ${PROJECT}.").withProject(project).get();
    }

    public static Event updateProject(Project project) {
        return Event.of("Project ${PROJECT} has been updated.").withProject(project).get();
    }

    public static Event deleteProject(Project project) {
        return Event.of("Project ${:project} has been deleted.").with("project", project.getName()).get();
    }

    public static Event newBranch(Branch branch) {
        return Event.of("New branch ${BRANCH} for project ${PROJECT}.").withBranch(branch).get();
    }

    public static Event updateBranch(Branch branch) {
        return Event.of("Branch ${BRANCH} in ${PROJECT} has been updated.").withBranch(branch).get();
    }

    public static Event deleteBranch(Branch branch) {
        return Event.of("Branch ${:branch} has been deleted from ${PROJECT}.")
                .withProject(branch.getProject())
                .with("branch", branch.getName())
                .get();
    }

    public static Event newBuild(Build build) {
        return Event.of("New build ${BUILD} for branch ${BRANCH} in ${PROJECT}.")
                .withBuild(build)
                .get();
    }

    public static Event newPromotionRun(PromotionRun promotionRun) {
        return Event.of("Build ${BUILD} has been promoted to ${PROMOTION_LEVEL} for branch ${BRANCH} in ${PROJECT}.")
                .withPromotionRun(promotionRun)
                .get();
    }

    public static Event newValidationRun(ValidationRun validationRun) {
        return Event.of("Build ${BUILD} has run for ${VALIDATION_STAMP} with status ${:status} in branch ${BRANCH} in ${PROJECT}.")
                .withValidationRun(validationRun)
                .withValidationRunStatus(validationRun.getLastStatus().getStatusID())
                .get();
    }

    public static Event newValidationRunStatus(ValidationRun validationRun) {
        return Event.of("Status for ${VALIDATION_STAMP} validation ${VALIDATION_RUN} for build ${BUILD} in branch ${BRANCH} of ${PROJECT} has changed to ${:status}.")
                .withValidationRun(validationRun)
                .withValidationRunStatus(validationRun.getLastStatus().getStatusID())
                .get();
    }

}
