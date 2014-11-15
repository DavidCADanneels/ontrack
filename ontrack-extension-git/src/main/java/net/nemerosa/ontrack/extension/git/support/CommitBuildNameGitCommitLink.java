package net.nemerosa.ontrack.extension.git.support;

import net.nemerosa.ontrack.extension.git.model.BuildGitCommitLink;
import net.nemerosa.ontrack.model.structure.Build;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class CommitBuildNameGitCommitLink implements BuildGitCommitLink<NoConfig> {

    @Override
    public String getId() {
        return "commit";
    }

    @Override
    public String getName() {
        return "Commit as name";
    }

    @Override
    public NoConfig clone(NoConfig data, Function<String, String> replacementFunction) {
        return data;
    }

    @Override
    public String getCommitFromBuild(Build build, NoConfig data) {
        return build.getName();
    }

}
