package net.nemerosa.ontrack.model.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.nemerosa.ontrack.model.structure.Branch;
import net.nemerosa.ontrack.model.structure.ID;
import net.nemerosa.ontrack.model.structure.Project;
import net.nemerosa.ontrack.model.structure.PromotionLevel;
import org.junit.Test;

import static net.nemerosa.ontrack.json.JsonUtils.object;
import static net.nemerosa.ontrack.test.TestUtils.assertJsonWrite;

public class PromotionViewTest {

    @Test
    public void no_build_json() throws JsonProcessingException {
        PromotionView view = new PromotionView(
                new PromotionLevel(ID.NONE, "PL", "Promotion level",
                        new Branch(ID.NONE, "B", "Branch",
                                new Project(ID.NONE, "P", "Project"))
                ),
                null,
                null
        );
        assertJsonWrite(
                object()
                        .with("promotionLevel", object()
                                .with("id", 0)
                                .with("name", "PL")
                                .with("description", "Promotion level")
                                .with("branch", object()
                                        .with("id", 0)
                                        .with("name", "B")
                                        .with("description", "Branch")
                                        .with("project", object()
                                                .with("id", 0)
                                                .with("name", "P")
                                                .with("description", "Project")
                                                .end())
                                        .end())
                                .end())
                        .with("promotedBuild", (String) null)
                        .with("promotionRun", (String) null)
                        .end(),
                view
        );
    }

}
