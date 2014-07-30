package net.nemerosa.ontrack.extension.git.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.nemerosa.ontrack.extension.scm.model.SCMBuildView;
import net.nemerosa.ontrack.extension.scm.model.SCMChangeLog;
import net.nemerosa.ontrack.model.structure.Branch;

@EqualsAndHashCode(callSuper = false)
@Data
public class GitChangeLog extends SCMChangeLog<GitConfiguration, GitBuildInfo> {

    @JsonIgnore // Not sent to the client
    private GitChangeLogCommits commits;
    @JsonIgnore // Not sent to the client
    private GitChangeLogIssues issues;
    @JsonIgnore // Not sent to the client
    private GitChangeLogFiles files;

    public GitChangeLog(
            String uuid,
            Branch branch,
            GitConfiguration configuration,
            SCMBuildView<GitBuildInfo> scmBuildFrom,
            SCMBuildView<GitBuildInfo> scmBuildTo) {
        super(uuid, branch, configuration, scmBuildFrom, scmBuildTo);
    }

    public GitChangeLog withCommits(GitChangeLogCommits commits) {
        this.commits = commits;
        return this;
    }

    public GitChangeLog withIssues(GitChangeLogIssues issues) {
        this.issues = issues;
        return this;
    }

    public GitChangeLog withFiles(GitChangeLogFiles files) {
        this.files = files;
        return this;
    }

}
