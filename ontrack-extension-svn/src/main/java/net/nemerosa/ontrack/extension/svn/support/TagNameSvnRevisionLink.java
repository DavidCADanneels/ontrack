package net.nemerosa.ontrack.extension.svn.support;

import com.fasterxml.jackson.databind.JsonNode;
import net.nemerosa.ontrack.extension.svn.model.BuildSvnRevisionLink;
import net.nemerosa.ontrack.json.JsonUtils;
import net.nemerosa.ontrack.model.form.Form;
import net.nemerosa.ontrack.model.support.NoConfig;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * Build / revision relationship based on the build name being a subversion revision.
 */
@Component
public class TagNameSvnRevisionLink implements BuildSvnRevisionLink<NoConfig> {

    @Override
    public String getId() {
        return "revision";
    }

    @Override
    public String getName() {
        return "Revision as name";
    }

    @Override
    public NoConfig clone(NoConfig data, Function<String, String> replacementFunction) {
        return data;
    }

    @Override
    public NoConfig parseData(JsonNode node) {
        return NoConfig.INSTANCE;
    }

    @Override
    public JsonNode toJson(NoConfig data) {
        return JsonUtils.object().end();
    }

    @Override
    public Form getForm() {
        return Form.create();
    }
}
