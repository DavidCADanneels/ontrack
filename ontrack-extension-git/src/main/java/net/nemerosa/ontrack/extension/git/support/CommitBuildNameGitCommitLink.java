package net.nemerosa.ontrack.extension.git.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import net.nemerosa.ontrack.extension.git.model.BuildGitCommitLink;
import net.nemerosa.ontrack.extension.git.model.GitBranchConfiguration;
import net.nemerosa.ontrack.git.GitRepositoryClient;
import net.nemerosa.ontrack.json.ObjectMapperFactory;
import net.nemerosa.ontrack.model.exceptions.JsonParsingException;
import net.nemerosa.ontrack.model.form.Form;
import net.nemerosa.ontrack.model.form.YesNo;
import net.nemerosa.ontrack.model.structure.Build;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class CommitBuildNameGitCommitLink implements BuildGitCommitLink<CommitLinkConfig> {

    private final Pattern abbreviatedPattern = Pattern.compile("[0-9a-f]{7}");
    private final Pattern fullPattern = Pattern.compile("[0-9a-f]{40}");

    @Override
    public String getId() {
        return "commit";
    }

    @Override
    public String getName() {
        return "Commit as name";
    }

    @Override
    public CommitLinkConfig clone(CommitLinkConfig data, Function<String, String> replacementFunction) {
        return data;
    }

    @Override
    public String getCommitFromBuild(Build build, CommitLinkConfig data) {
        return build.getName();
    }

    @Override
    public CommitLinkConfig parseData(JsonNode node) {
        try {
            return ObjectMapperFactory.create().treeToValue(node, CommitLinkConfig.class);
        } catch (JsonProcessingException e) {
            throw new JsonParsingException("CommitLinkConfig json", e);
        }
    }

    @Override
    public JsonNode toJson(CommitLinkConfig data) {
        return ObjectMapperFactory.create().valueToTree(data);
    }

    @Override
    public Form getForm() {
        return Form.create()
                .with(
                        YesNo.of("abbreviated")
                                .label("Abbreviated")
                                .help("Using abbreviated commit hashes or not.")
                                .value(true)
                );
    }

    @Override
    public Stream<String> getBuildCandidateReferences(String commit, GitRepositoryClient gitClient, GitBranchConfiguration branchConfiguration, CommitLinkConfig data) {
        return gitClient.log(
                String.format("%s~1", commit),
                gitClient.getBranchRef(branchConfiguration.getBranch())
        )
                .sorted()
                .map(
                        gitCommit -> data.isAbbreviated() ?
                                gitCommit.getShortId() :
                                gitCommit.getId()
                );
    }

    @Override
    public boolean isBuildEligible(Build build, CommitLinkConfig data) {
        return true;
    }

    @Override
    public boolean isBuildNameValid(String name, CommitLinkConfig data) {
        if (data.isAbbreviated()) {
            return abbreviatedPattern.matcher(name).matches();
        } else {
            return fullPattern.matcher(name).matches();
        }
    }

}
